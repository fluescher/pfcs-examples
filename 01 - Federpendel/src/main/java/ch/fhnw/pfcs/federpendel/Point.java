package ch.fhnw.pfcs.federpendel;

public class Point {
	public Point(double x, double y) {
		this(x,y,0);
	}
	
	public Point(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public final double x;
	public final double y;
	public final double z;
	
	@Override
	public String toString() {
		return "Point["+x+","+y+","+z+"]";
	}
}
