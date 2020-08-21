package Plotting;
import javax.swing.*;
import java.util.Random;
public class PlotTest extends JFrame{
	private PlotPanel mPlotPanel;
	public PlotTest(){
		super("OX - in Java");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(400,400);
		mPlotPanel = new PlotPanel();
		add(mPlotPanel);
		setVisible(true);
		
		Random r = new Random();

		double[] data = new double[10000];
		for (int i=0;i<data.length;i++){
			data[i] += 15*r.nextGaussian();
		}
		mPlotPanel.addSeries(new Histogram(data,300));
		mPlotPanel.setXLimit(-20d,20d);
		mPlotPanel.setYLimit(0d,50d);
	}
	public static void  main(String[] args){
		PlotTest p = new PlotTest();
		
		
		
	}
}