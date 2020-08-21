package Plotting;

public class Histogram extends Series{
	private double mMaxX;
	private double mMinX;
	private double mMaxY;
	private double mMinY;
	
	public Histogram(double[] Xs, int nBins){//avoiding sorting to keep O(n)
		int[] ys = new int[nBins];
		double xmin;
		double xmax;
		
		if (Xs.length==0){
			xmin=0;
			xmax=0;
		}
		else{
			xmin=Xs[0];
			xmax=Xs[0];
			for (double x:Xs){
				if (x<xmin) xmin = x;
				if (x>xmax) xmax = x;
			}
		}
		xmax+=0.5*(xmax-xmin)/nBins;
		double step =  (xmax-xmin)/nBins;
		for (double x:Xs){
			ys[(int)((x-xmin)/step)]+=1;
		}
		//h=ys[]/wdth
		for (int i=0;i<ys.length;i++){
			mRectangles.add(new double[]{xmin+i*step, 0,  xmin +(i+1) * step, ys[i]/step});
		}
		
		mMaxX = xmax;
		mMinX = xmin;
		mMinY=0.0d;
		if (ys.length==0){mMaxY=0;}
		else{
			mMaxY=ys[0]/step;
			for (int y:ys){
				if (y/step>mMaxY) mMaxY=y/step;
			}
		}
		
	}
	
	public Histogram(float[] Xs, int nBins){//avoiding sorting to keep O(n)
		int[] ys = new int[nBins];
		double xmin;
		double xmax;
		
		if (Xs.length==0){
			xmin=0;
			xmax=0;
		}
		else{
			xmin=Xs[0];
			xmax=Xs[0];
			for (float x:Xs){
				if (x<xmin) xmin = x;
				if (x>xmax) xmax = x;
			}
		}
		xmax+=0.5*(xmax-xmin)/nBins;
		double step =  (xmax-xmin)/nBins;
		for (float x:Xs){
			//System.out.println("Xmin: "+xmin+", Xmax: "+xmax+", x: "+x+", index: "+(int)((x-xmin)/step));
			ys[(int)((x-xmin)/step)]+=1;
		}
		//h=ys[]/wdth
		for (int i=0;i<ys.length;i++){
			//mRtangles.add(new double[]{xmin,ymin,xmax,ymax})
			mRectangles.add(new double[]{xmin+i*step, 0,  xmin +(i+1) * step, ys[i]/step});
		}
		
		mMaxX = xmax;
		mMinX = xmin;
		mMinY=0.0d;
		if (ys.length==0){mMaxY=0;}
		else{
			mMaxY=ys[0]/step;
			for (int y:ys){
				if (y/step>mMaxY) mMaxY=y/step;
			}
		}
		
	}
	
	@Override
	public double getMaxX(){return mMaxX;}
    public double getMaxY(){return mMaxY;}
    public double getMinX(){return mMinX;}
    public double getMinY(){return mMinY;}
	
	
}