package Plotting;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Font;
import java.awt.Color;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;

/*Built to support the Verlet program, providing an encapsulated histogram plotting JPanel;
built to be compatible with other projects, plotting not just histograms, though only histograms
have been implemented*/

public class PlotPanel extends JPanel implements ComponentListener{
	
	private int plotAreaStartX=50;
	private int plotAreaStartY=50;
	private int plotAreaEndX;
	private int plotAreaEndY;
	private ArrayList<Series> mSeries;//label, style, data
	private String mTitle;
	
	private double[] xlim;
	private double[] ylim;

	public PlotPanel(){
		plotAreaEndX = getWidth()-50;
		plotAreaEndY = getHeight()-50;
		addComponentListener(this);
		mSeries = new ArrayList<Series>();
		xlim = new double[2];
		ylim = new double[2];
	}
	
	@Override
	public void componentResized(ComponentEvent e){
		plotAreaEndX = getWidth()-50;
		plotAreaEndY = getHeight()-50;
		repaint();	
	}
	@Override
	public void componentHidden(ComponentEvent e){return;}
	@Override
	public void componentMoved(ComponentEvent e){return;}
	@Override
	public void componentShown(ComponentEvent e){return;}
	
	@Override
	protected void paintComponent(Graphics g){
		/*
		A Series contains lines points and rectangles, this method extracts them
		from the series and draws them. Only rectangles implemented currently for 
		support of histograms. Scatter plots and line plots would need plotting of
		lines and points etc.
		*/
		g.setColor(new Color(238,238,238));//background
		g.fillRect(0,0,getWidth(),getHeight());
		g.setColor(Color.WHITE);
		g.fillRect(plotAreaStartX,plotAreaStartY,plotAreaEndX - plotAreaStartX,plotAreaEndY-plotAreaStartY);
		g.setColor(Color.BLACK);
		g.drawRect(plotAreaStartX,plotAreaStartY,plotAreaEndX - plotAreaStartX,plotAreaEndY-plotAreaStartY);
		
		g.setColor(Color.BLACK);
		g.setFont(new Font("Verdana", Font.BOLD, 16));
		int titleWidth = g.getFontMetrics().stringWidth(mTitle);
		g.drawString(mTitle, (getWidth()-titleWidth)/2, 45);
		
		g.setFont(new Font("Verdana", Font.PLAIN,12));
		g.drawString(String.format("%6.3e", xlim[0]), 50, getHeight()-40);
		g.drawString(String.format("%6.3e", xlim[1]), getWidth()-75, getHeight()-40);

        Graphics2D gx = (Graphics2D) g;
		gx.rotate(-Math.PI/2, 40.0d, 80.0d);
		gx.drawString(String.format("%6.3e", ylim[1]), 40, 80);
		gx.rotate(Math.PI/2, 40.0d, 80.0d);
		gx.rotate(-Math.PI/2, 40.0d, getHeight()-50.0d);
		gx.drawString(String.format("%6.3e", ylim[0]), 40, getHeight()-50);
		gx.rotate(Math.PI/2,  40.0d, getHeight()-50.0d);
		//g.rotate(0);
		for (Series s:mSeries){
			g.setColor(s.getColor());
			//draw rectangles
			for (double[] rect : s.getRectangles()){
				
				/*
				xmin, ymin, xmax, ymax for rectangle correspond to
				rect[0],rect[1]rect[2]rect[3]
				
				top left in plotarea is xmin,ymax
				x-y space to pixel space:
				x:
					(x-xmin)*(plotAreaEndX - plotAreaStartX)/(xmax-xmin) + plotAreaStartX
				y: 
					(ymax-y)/(ymax-ymin)*(pAEY-pASY)+pASY
				*/
				if (rect[3]<ylim[0] || rect[2] < xlim[0] || rect[1] > ylim[1] || rect[0] > xlim[1]){
					//whole rect out of bounds
					continue;
				}
				//else: clip rect if partially out of bounds (min and max functions below),  or just plot
				int tlx = (int)((Math.max(rect[0],xlim[0]) - xlim[0])*((double)(plotAreaEndX - plotAreaStartX))/(xlim[1]-xlim[0])) + plotAreaStartX;
				int tly = (int)((ylim[1]-Math.min(rect[3],ylim[1]))/(ylim[1]-ylim[0])*((double)(plotAreaEndY - plotAreaStartY )))+ plotAreaStartY;
				int w = (int)((Math.min(rect[2],xlim[1])-Math.max(rect[0],xlim[0]))/(xlim[1]-xlim[0])*((double)(plotAreaEndX-plotAreaStartX)));
				int h = (int)((Math.min(rect[3],ylim[1])-Math.max(rect[1],ylim[0]))/(ylim[1]-ylim[0])*((double)(plotAreaEndY-plotAreaStartY)));
				
				g.fillRect(tlx,tly,w+1,h+1);
			
				
			}
			//draw lines, points TODO
		}
		
	}
	
	public void clear(){mSeries = new ArrayList<Series>();}
	
	public void addSeries(Series s){
		mSeries.add(s);
		repaint();
	}
	
	public void setXLimit(double d0,double d1){xlim[0]=d0; xlim[1]=d1;}
	public void setYLimit(double d0,double d1){ylim[0]=d0; ylim[1]=d1;}
	public void title(String s){setTitle(s);}
	public void setTitle(String s){
		mTitle = s;
	}
	public void autoscale(){
		if (mSeries.size()==0){
			xlim = new double[2];
			ylim = new double[2];
		}
		else{
			double x=mSeries.get(0).getMinX();
			double X=mSeries.get(0).getMaxX();
			double y=mSeries.get(0).getMinY();
			double Y=mSeries.get(0).getMaxY();
			for (Series s : mSeries){
				if (s.getMinX() < x) x = s.getMinX();
				if (s.getMaxX() > X) X = s.getMaxX();
				if (s.getMinY() < y) y = s.getMinY();
				if (s.getMaxY() > Y) Y = s.getMaxY();
				
			}
			xlim[0]=x; xlim[1]=X;
			ylim[0]=y; ylim[1]=Y;
		}
	}
	
}