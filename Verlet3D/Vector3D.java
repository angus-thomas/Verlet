package Verlet3D;
public class Vector3D {
	public double x;
	public double y;
	public double z;
	
	public Vector3D(double x, double y, double z){
		this.x=x;
		this.y=y;
		this.z=z;
	}
	public Vector3D(){
		x=0.0d;
		y=0.0d;
		z=0.0d;
	}
	
	public Vector3D(Vector3D v){
		x=v.x;
		y=v.y;
		z=v.z;
	}
	
	public static Vector3D distance(Vector3D v1, Vector3D v2){
		return new Vector3D(v2.x-v1.x,v2.y-v1.y, v2.z-v1.z);
	}
	
	public double abs(){
		return Math.sqrt(x*x+y*y+z*z);
	}
	
	public static Vector3D add(Vector3D v1, Vector3D v2){
		return new Vector3D(v1.x+v2.x, v1.y+v2.y, v1.z+v2.z);
	}
	
	public static Vector3D multiply(Vector3D v1, double d){
		return new Vector3D(v1.x*d, v1.y*d, v1.z*d);
	}
	
	public static Vector3D divide(Vector3D v1, double d){
		return new Vector3D(v1.x/d, v1.y/d, v1.z/d);
	}
	
	public static double dot(Vector3D v1, Vector3D v2){
		return v1.x*v2.x+v1.y*v2.y+v1.z*v2.z;
	}
}