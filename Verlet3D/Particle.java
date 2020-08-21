package Verlet3D;

public class Particle{
	public Vector3D position;
	public Vector3D velocity;
	public Vector3D acceleration;
	public double radius;//DEPRECATED used for rendering still
	
	public Particle(){
		position=new Vector3D();
		velocity=new Vector3D();
		acceleration=new Vector3D(3,4,5);
		radius=0.0d;
	}
	
	
};