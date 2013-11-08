package ch.fhnw.pfcs.satellites.objects;

import javax.media.opengl.GL2;

import com.jogamp.opengl.util.gl2.GLUT;

public class Satellite extends GLObject {
	protected static final double ONE_DAY = 24*3600;
	private static final int SATELLITE_RADIUS = 100;
	private final Planet center;
	private final double radius;
	private final double rotationPeriod;
	private final double degreePerDraw;
	private final double azimuth;
	private double curRotation = 0;
	private double displacement;

	public Satellite(Planet center, double radius, double period, double azimuth, double initialRotationPos, double secsPerDraw) {
		this(center, radius, period, azimuth, initialRotationPos, secsPerDraw, 0);
	}
	
	public Satellite(Planet center, double radius, double period, double azimuth, double initialRotationPos, double secsPerDraw, double displacement) {
		this.center = center;
		this.radius = radius;
		this.rotationPeriod = period;
		this.azimuth = azimuth;
		this.displacement = displacement;
		
		degreePerDraw = (360/rotationPeriod)*secsPerDraw;
		curRotation = initialRotationPos;
	}
	
	@Override
	public void draw(GL2 gl) {
		GLUT glut = new GLUT();
		gl.glPushMatrix();
		setColor(gl);
		gl.glRotated(center.getAzimuth(), 0, 0, 1);
		gl.glRotated(displacement, 0, 1, 0);
		gl.glRotated(azimuth, 0, 0, 1);
		gl.glRotated(curRotation, 0, 1, 0);
		gl.glTranslated(0, 0, radius);
		glut.glutSolidSphere(SATELLITE_RADIUS, 30, 30);
		gl.glPopMatrix();
		
		setCurRotation(curRotation+degreePerDraw);
	}
	
	private void drawOrbit(GL2 gl) {
		gl.glPushMatrix();
			setColor(gl);
			gl.glRotated(center.getAzimuth(), 0, 0, 1);
			gl.glRotated(displacement, 0, 1, 0);
			gl.glRotated(azimuth, 0, 0, 1);
			gl.glRotated(curRotation, 0, 1, 0);
			gl.glBegin(GL2.GL_LINE_LOOP);
			for(int i = 0; i < 360; i++)
				gl.glVertex3d(radius*Math.sin(toRad(i)), 0, radius*Math.cos(toRad(i)));
			gl.glEnd();
		gl.glPopMatrix();
	}

	private void setColor(GL2 gl) {
		int[] color = getOrbitColor();
		gl.glColor4f(color[0], color[1], color[2], 1);
	}

	@Override
	public void showHelp(GL2 gl) {
		drawOrbit(gl);
	}
	
	private void setCurRotation(double rotation) {
		if(rotation > 360) curRotation = rotation - 360;
		else if(rotation < 0) curRotation = 360-Math.abs(rotation);
		else curRotation = rotation;
	}
	
	private double toRad(double degree) {
		return degree * Math.PI / 180;
	}
	
	protected int[] getOrbitColor() {
		return new int[] {0,255,0};
	}
}
