package ch.fhnw.pfcs.satellites.objects;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLProfile;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;

import com.jogamp.opengl.util.awt.ImageUtil;
import com.jogamp.opengl.util.gl2.GLUT;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;

public class Planet extends GLObject {

	private final double secPerDraw;
	private final double degreePerDraw;
	private final double radius;
	private final double azimuth;
	private final boolean isRealPlanet;
	private double curRotation = 0;
	private Texture texture;

	public Planet(double r, double a, double s) {
		this(r,a,s, null, true);
	}
	
	public Planet(double r, double a, double s, String texture) {
		this(r,a,s, texture, true);
	}
	
	public Planet(double r, double a, double s, String texture, boolean isReal) {
		radius = r;
		secPerDraw = s;
		azimuth = a;
		
		final double degresPerSecond = 360.0/(24*3600);
		degreePerDraw = degresPerSecond*secPerDraw;
		isRealPlanet = isReal;
		
		if(texture != null) loadTexture(texture);
	}
	
	private void loadTexture(String file) {
		try {
			InputStream stream = getClass().getResourceAsStream(file);
			BufferedImage img = ImageIO.read(stream);
			ImageUtil.flipImageVertically(img);
			TextureData data = AWTTextureIO.newTextureData(GLProfile.getGL2GL3(), img, false);
			texture = TextureIO.newTexture(data);
		} catch (IOException ex) {
			System.err.println("Could not load earth texture: " + ex.toString());
		}
	}

	@Override
	public void draw(GL2 gl) {
		setCurRotation(curRotation-degreePerDraw);
		gl.glPushAttrib(GL2.GL_ALL_ATTRIB_BITS);
		gl.glPushMatrix();
		gl.glRotated(-90, 1, 0, 0);
		gl.glRotated(-azimuth, 0, 1, 0);
		gl.glRotated(-curRotation, 0, 0, 1);
		drawPlanet(gl);
		gl.glPopMatrix();
		gl.glPopAttrib();
	}

	public double getAzimuth() {
		return azimuth;
	}

	private void drawAxis(GL2 gl) {
		gl.glRotated(-90, 1, 0, 0);
		gl.glRotated(-azimuth, 0, 1, 0);
		gl.glRotated(-curRotation, 0, 0, 1);
		gl.glColor3d(0, 0, 200);
		gl.glLineStipple(4, (short)0xAAAA);
		gl.glEnable(GL2.GL_LINE_STIPPLE);
		gl.glBegin(GL.GL_LINES);
		gl.glVertex3d(0, 0, -25000);
		gl.glVertex3d(0, 0, 25000);
		gl.glEnd();
		gl.glDisable(GL2.GL_LINE_STIPPLE);
	}
	
	protected void drawPlanet(GL2 gl) {
		if(texture != null) {
			texture.enable(gl);
			texture.bind(gl);
			
			/* reset color */
			gl.glColor4f(1, 1, 1, 0);
			
			GLU glu = new GLU();
			GLUquadric planet = glu.gluNewQuadric();
			glu.gluQuadricTexture(planet, true);
			glu.gluQuadricDrawStyle(planet, GLU.GLU_FILL);
			glu.gluQuadricNormals(planet, GLU.GLU_FLAT);
			glu.gluQuadricOrientation(planet, GLU.GLU_OUTSIDE);
			glu.gluSphere(planet, radius, 50, 50);
			glu.gluDeleteQuadric(planet);
		} else {
			GLUT glut = new GLUT();
			gl.glColor3d(0, 0, 200);
			gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_FILL);
			glut.glutSolidSphere(radius, 50, 50);
			
			gl.glColor3d(255, 255, 255);
			gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_LINE);
			glut.glutSolidSphere(radius, 50, 50);
		}
	}
	
	private void setCurRotation(double rotation) {
		if(rotation > 360) curRotation = rotation - 360;
		else if(rotation < 0) curRotation = 360-Math.abs(rotation);
		else curRotation = rotation;
	}

	@Override
	public void showHelp(GL2 gl) {
		if(isRealPlanet) {
			drawAxis(gl);
		}
	}
	
}
