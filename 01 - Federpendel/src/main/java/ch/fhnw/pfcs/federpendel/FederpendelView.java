package ch.fhnw.pfcs.federpendel;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.fixedfunc.GLMatrixFunc;
import javax.swing.JPanel;

import ch.fhnw.pfcs.util.MainFrame.GLView;

import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.gl2.GLUT;

public class FederpendelView implements GLView, GLEventListener, KeyListener {

	private static final double CIRCLE_RADIUS = 0.05;
	private static final double RADIUS = 0.20;
	private static final int FPS = 80;
	private static final double PHI_STEP = 0.02;
	private static final double HOLDER_HEIGHT = 0.95;
	
	
	private final CircularMovement movement;
	private final Point springStart = new Point(0,HOLDER_HEIGHT,0);
	private GLCanvas canvas;
	private FPSAnimator animator;
	private GL2 gl;
	private GLUT glut;

	private boolean showCoordinates = false;
	private boolean showRotator = false;
	private double phi = 0;
	
	float no_mat[] =
	{ 0.0f, 0.0f, 0.0f, 1.0f };
	float mat_ambient[] =
	{ 0.7f, 0.7f, 0.7f, 1.0f };
	float mat_ambient_color[] =
	{ 0.8f, 0.8f, 0.2f, 1.0f };
	float mat_diffuse[] =
	{ 0.1f, 0.5f, 0.8f, 1.0f };
	float mat_specular[] =
	{ 1.0f, 1.0f, 1.0f, 1.0f };
	float no_shininess[] =
	{ 0.0f };
	float low_shininess[] =
	{ 5.0f };
	float high_shininess[] =
	{ 100.0f };
	float mat_emission[] =
	{ 0.3f, 0.2f, 0.2f, 0.0f };
	
	public FederpendelView() {
		this.movement = new CircularMovement(new Point(0,0), RADIUS);
	}

	public void startAnimation() {
//		stopAnimation();
		this.animator = new FPSAnimator(canvas, FPS, true);
		animator.start();
	}

	public void stopAnimation() {
		if (animator != null) {
			animator.stop();
		}
	}

	@Override
	public void init(GLAutoDrawable drawable) {
		this.canvas.requestFocusInWindow();
		this.gl = canvas.getGL().getGL2();
		this.glut = new GLUT();
		
	    float ambient[] =
	    { 0.0f, 0.0f, 0.0f, 1.0f };
	    float diffuse[] =
	    { 1.0f, 1.0f, 1.0f, 1.0f };
	    float lmodel_ambient[] =
	    { 0.4f, 0.4f, 0.4f, 1.0f };
	    float local_view[] =
	    { 0.0f };

	    /* antialiasing */
	    gl.glBlendFunc (GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
	    gl.glHint (GL2.GL_LINE_SMOOTH_HINT, GL2.GL_DONT_CARE);
//	    gl.glHint(GL2.GL_POLYGON_SMOOTH_HINT, GL2.GL_NICEST);
	    gl.glEnable(GL2.GL_LINE_SMOOTH);
	    gl.glEnable (GL2.GL_BLEND);
//	    gl.glEnable(GL2.GL_POLYGON_SMOOTH);
	    

	    gl.glEnable(GL2.GL_DEPTH_TEST);
	    gl.glDepthFunc(GL2.GL_LESS);

	    gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, ambient, 0);
	    gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, diffuse, 0);
	    gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, lmodel_ambient, 0);
	    gl.glLightModelfv(GL2.GL_LIGHT_MODEL_LOCAL_VIEWER, local_view, 0);

	    enableLighting();

	    gl.glClearColor(0, 0, 0, 0.8f);
	}

	private void enableLighting() {
		gl.glEnable(GL2.GL_LIGHTING);
	    gl.glEnable(GL2.GL_LIGHT0);
	}
	
	private void disableLighting() {
		gl.glDisable(GL2.GL_LIGHTING);
	    gl.glDisable(GL2.GL_LIGHT0);
	}

	@Override
	public void reshape(GLAutoDrawable arg0, int x, int y, int width, int height) {
	}

	@Override
	public void display(GLAutoDrawable drawable) {
		float position[] = { 2.0f, 2.0f, 2.0f, 0.0f };
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, position, 0);
		phi += PHI_STEP;
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		
		Point pos = movement.calculatePosition(phi);
		gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
		
		if(showCoordinates) drawCoordinates();		/* coordinates */
		if(showRotator) drawCircleAt(pos.x, pos.y); /* Draw visualisation */
		drawHolderElement(new Point(0,0,HOLDER_HEIGHT), 0.25);
		drawSpring(CIRCLE_RADIUS/2, springStart, pos); /* Draw spring */
		drawCircleAt(0,pos.y); 						/* Draw ball on spring */
	}

	@Override
	public void dispose(GLAutoDrawable drawable) {

	}

	private void drawCoordinates() {
		disableLighting();
		
		// x-axis
		gl.glColor3d(0, 0, 255);
		gl.glBegin(GL.GL_LINES);
		gl.glVertex2d(-1,0);
		gl.glVertex2d(1,0);
		gl.glEnd();
		
		// y-axis
		gl.glColor3d(255, 0, 0);
		gl.glBegin(GL.GL_LINES);
		gl.glVertex2d(0,HOLDER_HEIGHT-0.01);
		gl.glVertex2d(0,-1);
		gl.glEnd();
		
		// z-axis
		gl.glColor3d(0, 255, 0);
		gl.glBegin(GL.GL_LINES);
		gl.glVertex3d(0,0,-1);
		gl.glVertex3d(0,0,1);
		gl.glEnd();
		
		enableLighting();
		
	}
	
	private void drawHolderElement(Point center, double radius) {
		gl.glPushMatrix();
		gl.glRotated(90, 1, 0, 0);
		gl.glTranslated(center.x, center.y, -center.z);
		glut.glutSolidCylinder(radius, 0.02, 50, 50);
		gl.glPopMatrix();
	}
	
	private void drawSpring(double radius, Point start, Point end) {
		disableLighting();
		
		final double endY = (end.y >= 0 ? Math.abs(end.y) : end.y)+CIRCLE_RADIUS;
		final double distance =  start.y - endY;
		final int springRounds = 18;
		final int pointsPerRound = 10;
		final double zDiff = distance / (springRounds*pointsPerRound); 
		final double angleDiff = (2*Math.PI)/pointsPerRound;
		
		double x,y,z,angle;
		gl.glColor3d(0.35,0.35,0.35);
		gl.glPushMatrix();
		gl.glTranslated(start.x, start.y, 0);
		
		gl.glBegin(GL2.GL_LINE_STRIP);
		z=start.z;
		for(angle = 0; angle <= (2*Math.PI)*springRounds; angle += angleDiff) {
			x = radius *Math.sin(angle);
			y = radius *Math.cos(angle);
			
			gl.glVertex3d(x, z, y);
			z -= zDiff;
		}
		
		gl.glEnd();
		gl.glPopMatrix();
		enableLighting();
	}
	
	private void drawCircleAt(final double cx, final double cy) {
		gl.glPushMatrix();
	    gl.glMaterialfv(GL.GL_FRONT, GL2.GL_AMBIENT, no_mat, 0);
	    gl.glMaterialfv(GL.GL_FRONT, GL2.GL_DIFFUSE, mat_diffuse, 0);
	    gl.glMaterialfv(GL.GL_FRONT, GL2.GL_SPECULAR, mat_specular, 0);
	    gl.glMaterialfv(GL.GL_FRONT, GL2.GL_SHININESS, high_shininess, 0);
	    gl.glMaterialfv(GL.GL_FRONT, GL2.GL_EMISSION, no_mat, 0);
	    gl.glTranslated(cx, cy, 0);
		glut.glutSolidSphere(CIRCLE_RADIUS, 50, 50);
		gl.glPopMatrix();
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if(e.getKeyChar() == 'r') {
			showRotator = !showRotator;
		} else if(e.getKeyChar() == 'c') {
			showCoordinates = !showCoordinates;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void setGLCanvas(GLCanvas canvas) {
		this.canvas = canvas;
		this.canvas.addKeyListener(this);
		this.canvas.addGLEventListener(this);
	}

	@Override
	public String getName() {
		return "pfcs - Federpendel";
	}

	@Override
	public double getInitialDistance() {
		return 2;
	}

	@Override
	public String getHelpText() {
		return "c - Show coordinates\nr - Show rotator\nClick and move mouse - Rotate camera";
	}
	
	@Override
	public JPanel getParameterPanel() {
		return null;
	}

	@Override
	public double getYOffset() {
		return 0;
	}
}
