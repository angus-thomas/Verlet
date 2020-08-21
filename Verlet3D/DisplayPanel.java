package Verlet3D;
import javax.swing.*;
import java.awt.Graphics;
import java.util.Arrays;
public class DisplayPanel extends JPanel{
	private Particle[] mCircles;
	public DisplayPanel(){
		mCircles=new Particle[0];
	}
	
	@Override
	protected void paintComponent(java.awt.Graphics g){
		int pixX=this.getWidth();
		int pixY=this.getHeight();
		g.setColor(java.awt.Color.BLACK);
		g.fillRect(0,0,pixX,pixY);
		Arrays.sort(mCircles, new SortByDepth());
		for (Particle circle: mCircles){
			//System.out.println(circle.position.z/400.0d);
			int r = (int)(205*(circle.position.z/400.0d));
			g.setColor(java.awt.Color.BLACK);
			g.drawOval((int)(circle.position.x-circle.radius),(int)(circle.position.y-circle.radius),(int)(2*circle.radius),(int)(2*circle.radius));

			g.setColor(new java.awt.Color(Math.min(Math.max(500-2*r, 0),255)  ,0,Math.min(Math.max(2*r,0),255)));
			//System.out.println(255*r);
			g.fillOval((int)(circle.position.x-circle.radius),(int)(circle.position.y-circle.radius),(int)(2*circle.radius),(int)(2*circle.radius));
		}
	}
	
	public void display(Particle[] circles){
		mCircles=circles;
		repaint();
	}
}