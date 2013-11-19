package ch.fhnw.pfcs.federpendel;

public class CircularMovement {
	private final double radius;
	private final Point center;
	
	public CircularMovement(Point center, double r) {
		this.radius = r;
		this.center = center;
	}
	
	public Point calculatePosition(double rad) {
		return calculatePosition(rad, 0);
	}
	
	public Point calculatePosition(double rad, double z) {
		final double x = radius * Math.cos(rad) + center.x;
		final double y = radius * Math.sin(rad) + center.y;
		
		return new Point(x,y,z);
	}
	
	public Point calculatePosition3D(double radX, double radY) {
		final double x = radius * Math.cos(radX) * Math.sin(radY) + center.x;
		final double y = radius * Math.sin(radX) * Math.sin(radY) + center.y;
		final double z = radius * Math.cos(radY) + center.z;
		
		return new Point(x,y,z);
	}
}
