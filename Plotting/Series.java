package Plotting;
import java.util.ArrayList;

public abstract class Series{
	//A data series in terms of drawable shapes (points, lines, etc.).
	protected java.awt.Color mColor;
	protected ArrayList<double[]> mRectangles;
	protected double[] mXs;
	protected double[] mYs;
	protected String mLabel;
	protected boolean mLines;
	
	public Series(){
		mColor = java.awt.Color.GREEN;
		mRectangles = new ArrayList<double[]>();
	}
	
	public java.awt.Color getColor(){return mColor;}
	public ArrayList<double[]> getRectangles(){return mRectangles;}
	
	public abstract double getMaxX();
	public abstract double getMaxY();
	public abstract double getMinX();
	public abstract double getMinY();
}