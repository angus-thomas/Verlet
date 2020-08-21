package Verlet3D;
import java.util.Comparator;

public class SortByDepth implements Comparator<Particle>{
	public int compare(Particle p1, Particle p2){
		return (int)(p2.position.z-p1.position.z);
	}
}