package Verlet3D;
import javax.swing.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Random;
import java.io.*;
import java.awt.*;
import javax.swing.event.*;
import java.awt.event.*;
import javax.swing.border.*;
import Plotting.*;

//Potential improvements:
//TODO: Scale position in Angstroms, not pixels (and universal constants to scale)
//TODO: Density computation (after above)
//TODO: Support for particles of different radii in the same simulation (eg. for diffusion simulating)
//TODO: make the RDF plot a rolling average of several steps of the simulation

public class Verlet extends JFrame{
	
	
	private Particle[] mParticles;
	private int mWidth;
	private int mHeight;
	private int mDepth = 400;
	private double radii=25;//pix
	private boolean mReset=false;
	private boolean mBorderWrap = true;
	
	private DisplayPanel mPanel;
	private PlotPanel mPlotPanel;
	private Timer mTimer;
	private JLabel mTempLabel;
	
	
	private final int NO_PARTICLES=80;
	private final double Kb=100;//Universal constants not to scale
	private final int FPS=20;
	private final double DELTATIME=2e-3;//seconds
	private final double BERENDSEN_TIME=20e-3;//seconds
	private final double EPSILON=10000;
	
	private double mTbath=200;//set by a JSlider
	double A;
	double B;
	
	private int FPSCount=0;
	
	public Verlet(){
		super("Verlet");
		setLayout(new GridLayout(1,2));
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(1000,600);
		JPanel leftPanel = new JPanel(new BorderLayout());
		mPanel = new DisplayPanel();
		mPlotPanel = new PlotPanel();
		mPlotPanel.setTitle("Radial Distribution Function");

		mPanel.addComponentListener(new ComponentListener(){
				@Override
				public void componentResized(ComponentEvent e){
					mHeight=((JPanel)e.getComponent()).getHeight();
					mWidth=((JPanel)e.getComponent()).getWidth();
				}
				@Override
				public void componentHidden(ComponentEvent e){return;}
				@Override
				public void componentMoved(ComponentEvent e){return;}
				@Override
				public void componentShown(ComponentEvent e){return;}
				
			});

		leftPanel.add(mPanel,BorderLayout.CENTER);
		leftPanel.add(createSettingsPanel(), BorderLayout.SOUTH);
		
		add(leftPanel, 0);
		add(mPlotPanel, 1);

		setVisible(true);
		mHeight=mPanel.getHeight();
		mWidth=mPanel.getWidth();
		initialisePositions(NO_PARTICLES,radii);

		mPanel.display(mParticles);
		mTimer = new Timer(true);
		mTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    update();
                }
            }, 0, (int)(1000f/(float)FPS));
		
		A = EPSILON*Math.pow(2*radii,12);
		B = 2*EPSILON*Math.pow(2*radii,6);
		InfoPanel p = new InfoPanel();
	}
	
	private JPanel createSettingsPanel(){
		JPanel settingsPanel = new JPanel(new GridBagLayout());		
		GridBagConstraints c = new GridBagConstraints();
		
		c.gridx=0;
		c.gridy=0;		
		settingsPanel.add(new JLabel(" Temperature /K :"), c);

		mTempLabel = new JLabel(Double.toString(mTbath));
		c.gridx=1;
		settingsPanel.add(mTempLabel, c);

		JSlider tempSlider = new JSlider(JSlider.HORIZONTAL, 0,  400, (int)mTbath);//min,max,initial
		tempSlider.addChangeListener(new ChangeListener(){
				public void stateChanged(ChangeEvent e){
					mTbath = (double)((JSlider)e.getSource()).getValue();
					}});
					
		tempSlider.setMajorTickSpacing(50);
		tempSlider.setMinorTickSpacing(10);
		tempSlider.setPaintTicks(true);
		tempSlider.setPaintLabels(true);
		c.gridx=2;
		c.fill=GridBagConstraints.HORIZONTAL;
		c.weightx=1.0;
		settingsPanel.add(tempSlider, c);
		c.fill=GridBagConstraints.NONE;
		c.weightx=0.0;
		
		JButton resetButton = new JButton("Reset");
		JButton infoButton = new JButton("Info");
		resetButton.addActionListener(e -> mReset=true);
		infoButton.addActionListener(e -> new InfoPanel());
		JPanel pane = new JPanel();
		pane.add(resetButton);
		pane.add(infoButton);
		c.gridx=3;
		settingsPanel.add(pane, c);
		
		c.gridy=1;
		c.gridx=0;
		settingsPanel.add(new JLabel(" Particle Radius /px :"), c);

		JLabel radiiLabel = new JLabel(String.format("%5.2f  ",radii));
		c.gridx=1;
		settingsPanel.add(radiiLabel, c);

		JSlider radiusSlider = new JSlider(JSlider.HORIZONTAL, 2,  82, (int)radii);//min,max,initial
		radiusSlider.addChangeListener(new ChangeListener(){
				public void stateChanged(ChangeEvent e){
					

					radii = (int)((JSlider)e.getSource()).getValue();
					mPlotPanel.setXLimit(0.0d, 20*radii);
					radiiLabel.setText(String.format("%5.2f  ",radii));
					A = EPSILON*Math.pow(2*radii,12);
					B = 2*EPSILON*Math.pow(2*radii,6);
					for (Particle p : mParticles){
						p.radius = radii;
					}
					}});
					
		radiusSlider.setMajorTickSpacing(10);
		radiusSlider.setMinorTickSpacing(1);
		radiusSlider.setPaintTicks(true);
		radiusSlider.setPaintLabels(true);
		c.gridx=2;
		c.fill=GridBagConstraints.HORIZONTAL;
		c.weightx=1.0;
		settingsPanel.add(radiusSlider, c);
		c.fill=GridBagConstraints.NONE;
		c.weightx=0.0;
		JToggleButton borderToggleButton = new JToggleButton("Solid Border");
		borderToggleButton.addActionListener(e->{mBorderWrap=!mBorderWrap; mReset=true;});
		c.gridx=3;
		settingsPanel.add(borderToggleButton, c);

		
		return settingsPanel;
	}
	
	private void update(){
		if (mReset){
			//Having the reset button directly re-initialise mParticles is 
			//not threadsafe with the update() thread!

			initialisePositions(NO_PARTICLES, radii);
			mReset = false;
		}
		
		long startTime = System.currentTimeMillis();
		for (int i=0;i<Math.max(1,(int)(1/((double)FPS*(double)DELTATIME)));i++){
			/*Compute all verlet steps to be displayed in 1 frame (every 1/FPS seconds)
			then display and wait to be caled again. Assumes DELTATIME << 1/FPS
			
			Velocity verlet algorithm with velocity scaling:
			1. v(t + dt/2)= v(t) + 1/2 a(t) dt
			2. x(t + dt)  = x(t) + v(t + dt/2) dt
			3. a(t + dt)  = f(x(t + dt))
			4. v(t + dt)  = v(t + dt/2) + 1/2 a(t + dt) dt
			5. v(t + dt) *= vScaleCoeff
			
			If !mBorderWrap, have elastic collisions with borders, so x and v are
			inverted between steps 2 and 3 for particles crossing the walls.
			*/
			
			//Steps 1 and 2:
			for (Particle p:mParticles){
				p.velocity = Vector3D.add(p.velocity, Vector3D.multiply(p.acceleration, 0.5d*DELTATIME));		
				p.position = Vector3D.add(p.position, Vector3D.multiply(p.velocity, DELTATIME));
				
				if (!mBorderWrap){
					if (p.position.x>mWidth-radii)   {p.position.x = 2*mWidth -2*radii-p.position.x;  p.velocity.x = -Math.abs(p.velocity.x);}
					if (p.position.y>mHeight-radii)  {p.position.y = 2*mHeight-2*radii-p.position.y; p.velocity.y = -Math.abs(p.velocity.y);}
					if (p.position.z>mDepth-radii)   {p.position.z = 2*mDepth -2*radii-p.position.z;  p.velocity.z = -Math.abs(p.velocity.z);}
				
					if (p.position.x<radii) {p.position.x = radii+Math.abs(radii-p.position.x); p.velocity.x = Math.abs(p.velocity.x);}
					if (p.position.y<radii) {p.position.y = radii+Math.abs(radii-p.position.y); p.velocity.y = Math.abs(p.velocity.y);}
					if (p.position.z<radii) {p.position.z = radii+Math.abs(radii-p.position.z); p.velocity.z = Math.abs(p.velocity.z);}

				}
			}
			
			//Step 3:
			applyForces();

			double vScaleCoeff = Math.min(Math.sqrt(1 + DELTATIME / BERENDSEN_TIME*(mTbath/temperature()-1)),10);

			//Steps 4 and 5:
			for (Particle p : mParticles){
				p.velocity = Vector3D.add(p.velocity, Vector3D.multiply(p.acceleration, 0.5d*DELTATIME));
				p.velocity = Vector3D.multiply(p.velocity, vScaleCoeff);
			}
			
			
			if (mBorderWrap){
				//teleport particles to opposite sides of the screen
				for (int k=0;k<mParticles.length;k++){
					Particle p=mParticles[k];
					if (p.position.x>mWidth){
						p.position.x = p.position.x % mWidth;
					}
					else if (p.position.x < 0.0){
						//Mod in java behaves as: -2 % 10 == -2, for example,
						//so cannot just do x = x%mWidth (is in Python, for example).
						p.position.x = mWidth + p.position.x%mWidth;
					}
					
					if (p.position.y > mHeight){
						p.position.y = p.position.y % mHeight;
					}
					else if (p.position.y < 0.0){
						p.position.y = mHeight+ p.position.y%mHeight;
					}

					if (p.position.z > mDepth){
						p.position.z = p.position.z % mDepth;
					}
					else if (p.position.z<0.0){
						p.position.z = mDepth + p.position.z%mDepth;
					}

				}
			}
			
		}
		FPSCount+=1;
		//Can evaluate performance here by computing (systemm.currentTimeMillis - startTime) * FPS.

		if (FPSCount>=FPS){
			//every FPS frames
			mTempLabel.setText(String.format("%5.2f  ",temperature()));
			FPSCount=0;
			mPlotPanel.clear();
			float[] fRDF = getRDF();
			float maxR=0.0f;
			for (float f:fRDF){
				if (f>maxR) maxR=f;
			}
			mPlotPanel.addSeries(new Histogram(fRDF,150));
			mPlotPanel.autoscale();
			mPlotPanel.setXLimit(0,maxR);
		}
		mPanel.display(mParticles);
	}
	
	public double kineticEnergy(){
		double ret = 0.0d;
		
		for (Particle p:mParticles){
			//faster than p.velocity.abs() * p.velocity.abs()
			ret += p.velocity.x*p.velocity.x+p.velocity.y*p.velocity.y + p.velocity.z*p.velocity.z;
		}
		return ret;
	}
	
	public double temperature(){
		return (2.0d/3.0d)*kineticEnergy()/NO_PARTICLES/Kb;
	}
	
	public void applyForces(){
		for (Particle p : mParticles){
			p.acceleration = new Vector3D();
		}
		for (int i=0;i<mParticles.length;i++){
			Particle pi = mParticles[i];
			for (int j=i+1;j<mParticles.length;j++){
				Vector3D dist = Vector3D.distance(mParticles[i].position,mParticles[j].position);
				
				//Particles over half a screen away in x, y, or z direction attract each other
				//across the boundary of the screen.
				if (mBorderWrap){
					if (dist.x > mWidth/2) dist.x -= mWidth;
					if (dist.x < -mWidth/2) dist.x += mWidth;
					if (dist.y > mHeight/2) dist.y -= mHeight;
					if (dist.y < -mHeight/2) dist.y += mHeight;
					if (dist.z > mDepth/2) dist.z -= mDepth;
					if (dist.z < -mDepth/2) dist.z += mDepth;
				}
				
				//Math.max avoids divideby0 for particles on top of each other. 1.0 is 1px separation.
				double distabs=Math.max(dist.abs(),1.0);
				double F = F12(distabs); 
				
				mParticles[i].acceleration = Vector3D.add(Vector3D.multiply(dist, F/distabs), mParticles[i].acceleration);
				mParticles[j].acceleration = Vector3D.add(Vector3D.multiply(dist, (-1)*F/distabs), mParticles[j].acceleration);
			}
			
		}
	}
	
	public float[] getRDF(){
		float[] ret = new float[(NO_PARTICLES-1)*(NO_PARTICLES)/2];
		int n=0;
		for (int i=0;i<mParticles.length;i++){
			Vector3D r = mParticles[i].position; 
			for (int j=i+1;j<mParticles.length;j++){
				ret[n]+= Vector3D.distance(mParticles[i].position,mParticles[j].position).abs();
				n++;
			}
		}
		return ret;
	}
	
	private double F12(double r){
		//Uses Lennard-Jones potential;
		//repulsive if negative.
		return (6*B/Math.pow(r,7)-12*A/Math.pow(r,13));
	}

	private void initialisePositions(int no, double rad){
		mParticles=new Particle[no];
		/*
		This somewhat opaque code:
		 - Creates a simple cubic lattice with p, q and r points parallel to the x, y and z axes,
				p:q:r similar to w:h:d
				pqr > no of particles
		 - Puts the ith particle at the ith lattice point
		*/ 
		double w = mWidth - 2*rad;
		double h = mHeight - 2*rad;
		double d = mDepth - 2*rad;

		int p = (int)Math.ceil(Math.pow(((float)no)*w*w/d/h,1.0/3.0));
		int q = (int)Math.ceil(h*p/w);
		int r = (int)Math.ceil(d*p/w);
		
		for (int i=0;i<no;i++){
			double x = (float)(i/(q*r)) * w/(p-1) + rad;//note integer and float division
			double y = (float)(i%(q*r)/r) * h/(q-1) + rad;
			double z = i%r * d/(r-1) + rad;

			Particle particle =new Particle();
			
			particle.position = new Vector3D(x, y, z);
			particle.radius=rad;
			mParticles[i]=particle;
		}
		return;
	}
	
	public static void main(String[] args){
		Verlet m=new Verlet();
	}
}