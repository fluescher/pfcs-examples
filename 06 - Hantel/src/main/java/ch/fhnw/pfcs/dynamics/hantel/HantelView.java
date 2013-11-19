package ch.fhnw.pfcs.dynamics.hantel;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.JPanel;

import ch.fhnw.pfcs.dynamics.Dynamics;
import ch.fhnw.pfcs.util.MainFrame.GLView;

import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.gl2.GLUT;

public class HantelView implements GLView, GLEventListener, KeyListener {
	private static final int INITIAL_DISTANCE = 50;
	private static final int FPS = 60;
	private static final double G = 9.81;
	
	private GLCanvas canvas;
	private FPSAnimator animator;
	private GL2 gl;
	private WurfDynamics dynamics;
	
	private boolean showCoordinates = false;
	
	double radius = 1;
	double length = 10;
	double width = 5;
	double height =5;
	double mass = 1;
	
	//Trägheitsmomente
	double a = 3, b = 1, m = 1;
	double I1 = 0.4 * m * b * b;
	double I2 = 0.4 * m * b * b + m * a * a;
	double I3 = I2;
	double[] q = {	5, 1, -2, 
					1, 0, 0, 0};		//Quaternion
	
	/* schwerpunktbewegung */
	final static double START_Y = 35;
	double ys = START_Y;
	double vs = 0;
	
	private double dt = 1.0 / FPS;

	double phi = 0;
	double phi_speed = 1;
	
	float no_mat[] =
	{ 0.0f, 0.0f, 0.0f, 1.0f };
	float mat_ambient[] =
	{ 0.0f, 0.2f, 0.4f, 1.0f };
	float mat_ambient_color[] =
	{ 0.8f, 0.8f, 0.2f, 1.0f };
	float mat_diffuse[] =
	{ 0.1f, 0.5f, 0.8f, 1.0f };
	float mat_specular[] =
	{ 0.7f, 0.7f, 0.7f, 1.0f };
	float no_shininess[] =
	{ 0.0f };
	float low_shininess[] =
	{ 5.0f };
	float high_shininess[] =
	{ 50.0f };
	float mat_emission[] =
	{ 0.3f, 0.2f, 0.2f, 0.0f };
	
	
	public HantelView() {
		dynamics = new WurfDynamics(I1, I2, I3);
	}
	
	@Override
	public void setGLCanvas(GLCanvas canvas) {
		this.canvas = canvas;
		canvas.addGLEventListener(this);
		this.animator = new FPSAnimator(canvas, FPS, true);
		this.canvas.addKeyListener(this);
	}

	@Override
	public void startAnimation() {
		this.animator.start();
	}

	@Override
	public void stopAnimation() {
		if(animator != null)
			animator.stop();
	}

	@Override
	public String getName() {
		return "Hantel";
	}

	@Override
	public double getInitialDistance() {
		return INITIAL_DISTANCE;
	}

	@Override
	public String getHelpText() {
		return "c - Show coordinates\nClick and move mouse - Rotate camera";
	}

	public void init(GLAutoDrawable drawable){ 
		this.gl = this.canvas.getGL().getGL2();
		this.canvas.requestFocusInWindow();
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
	    gl.glEnable(GL2.GL_LINE_SMOOTH);
	    gl.glEnable (GL2.GL_BLEND);

	    gl.glEnable(GL2.GL_DEPTH_TEST);
	    gl.glDepthFunc(GL2.GL_LESS);

	    gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, ambient, 0);
	    gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, diffuse, 0);
	    gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, lmodel_ambient, 0);
	    gl.glLightModelfv(GL2.GL_LIGHT_MODEL_LOCAL_VIEWER, local_view, 0);

	    enableLighting();

	    gl.glMaterialfv(GL.GL_FRONT, GL2.GL_AMBIENT, no_mat, 0);
	    gl.glMaterialfv(GL.GL_FRONT, GL2.GL_DIFFUSE, mat_diffuse, 0);
	    gl.glMaterialfv(GL.GL_FRONT, GL2.GL_SPECULAR, mat_specular, 0);
	    gl.glMaterialfv(GL.GL_FRONT, GL2.GL_SHININESS, high_shininess, 0);
	    gl.glMaterialfv(GL.GL_FRONT, GL2.GL_EMISSION, no_mat, 0);
	    gl.glEnable(GL2.GL_COLOR_MATERIAL);
	    
	    gl.glClearColor(0, 0, 0, 0.8f);
	}

	public void display(GLAutoDrawable drawable){
		float position[] = { 100, 100, 100, 0.0f };
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, position, 0);
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		gl.glClearColor(0f, 0f, 0f, 0.0f);
		
		if(showCoordinates) drawCoordinates();
		
		gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_FILL);
	    
	    drawBottom(gl);
	    drawHantel(gl);
	}

	private void drawBottom(GL2 gl) {
		GLUT glut = new GLUT();
		gl.glPushMatrix();
			disableLighting();
			gl.glColor3d(0.6, 0.6, 0.6);
			gl.glScaled(1.0, 0.01, 1.0);
			glut.glutSolidCube(30);
			enableLighting();
		gl.glPopMatrix();
	}
	
	private void drawHantel(GL2 gl) {
		gl.glPushMatrix();
		
		GLUT glut = new GLUT();
		double phiRad = 2*Math.acos(q[3]);
	    double phiDeg = Math.toDegrees(phiRad);
	    q = dynamics.norm(q);
	    
	    gl.glTranslated(0, ys, 0);
	    gl.glRotated(phiDeg, q[4], q[5], q[6]);
	    
	    double[][] R = q2matrix(q[3], q[4], q[5], q[6]);
	    
	    final double yA = R[1][0] * a + ys;
	    final double yB = R[1][0] * -a + ys;
	    
	    if(yA <= b || yB <= b) {
	    	final double K;
	    	if(yA <= b) { 
	    		K = kraftstoss(R, a, vs, q);
	    		
	    		vs = vs + K/mass;
		    	q[1] = q[1] - a * R[1][2] * K / I2;
		    	q[2] = q[2] + a * R[1][1] * K / I3;
	    	}else{
	    		K = kraftstoss(R, -a, vs, q);
	    		
	    		vs = vs + K/mass;
		    	q[1] = q[1] + a * R[1][2] * K / I2;
		    	q[2] = q[2] - a * R[1][1] * K / I3;
	    	}
	    }
	    
	    q = dynamics.rungeKutta(q, dt);
	    ys += vs*dt;
	    vs -= G*dt;
	    
	    blueMaterial(gl);
		gl.glTranslated(a, 0, 0);
        glut.glutSolidSphere(b, 30, 30);
        
        /* Verbindungsstück */
        greyMaterial(gl);
        gl.glRotated(-90, 0, 1, 0);
	    glut.glutSolidCylinder(b/10, 2*a, 30, 30);
	    gl.glRotated(90, 0, 1, 0);
	    
	    blueMaterial(gl);
        gl.glTranslated(-2*a, 0, 0);
        glut.glutSolidSphere(b, 30, 30);
		gl.glPopMatrix();
	}
	
	private void blueMaterial(GL2 gl) {
		gl.glMaterialfv(GL.GL_FRONT, GL2.GL_AMBIENT, no_mat, 0);
	    gl.glMaterialfv(GL.GL_FRONT, GL2.GL_DIFFUSE, mat_diffuse, 0);
	    gl.glMaterialfv(GL.GL_FRONT, GL2.GL_SPECULAR, mat_specular, 0);
	    gl.glMaterialfv(GL.GL_FRONT, GL2.GL_SHININESS, high_shininess, 0);
	    gl.glMaterialfv(GL.GL_FRONT, GL2.GL_EMISSION, no_mat, 0);
	    gl.glColor3d(0,0,1);
	}
	
	private void greyMaterial(GL2 gl) {
		float no_mat[] =
			{ 0.7f, 0.7f, 0.7f, 1.0f };
		float mat_diffuse[] =
			{ 0.1f, 0.5f, 0.5f, 1.0f };
		float mat_specular[] =
			{ 0.0f, 0.0f, 0.0f, 1.0f };
		float high_shininess[] =
			{ 100.0f };
		
	    gl.glMaterialfv(GL.GL_FRONT, GL2.GL_AMBIENT, no_mat, 0);
	    gl.glMaterialfv(GL.GL_FRONT, GL2.GL_DIFFUSE, mat_diffuse, 0);
	    gl.glMaterialfv(GL.GL_FRONT, GL2.GL_SPECULAR, mat_specular, 0);
	    gl.glMaterialfv(GL.GL_FRONT, GL2.GL_SHININESS, high_shininess, 0);
	    gl.glMaterialfv(GL.GL_FRONT, GL2.GL_EMISSION, no_mat, 0);
	    gl.glColor3d(0.01, 0.01, 0.01);
	}
	
	
	private void drawCoordinates() {
		disableLighting();
		
		// x-axis
		gl.glColor3d(0, 0, 255);
		gl.glBegin(GL.GL_LINES);
		gl.glVertex2d(-120000,0);
		gl.glVertex2d(120000,0);
		gl.glEnd();
		
		// y-axis
		gl.glColor3d(255, 0, 0);
		gl.glBegin(GL.GL_LINES);
		gl.glVertex2d(0,120000);
		gl.glVertex2d(0,-120000);
		gl.glEnd();
		
		// z-axis
		gl.glColor3d(0, 255, 0);
		gl.glBegin(GL.GL_LINES);
		gl.glVertex3d(0,0,-120000);
		gl.glVertex3d(0,0,120000);
		gl.glEnd();
		
		enableLighting();
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
	public void reshape(GLAutoDrawable drawable, int x, int y,
			int width, int height){  
	}

	@Override
	public void dispose(GLAutoDrawable arg0) {
		
	}
	
	private static final class WurfDynamics extends Dynamics {
		
		private final double I1;
		private final double I2;
		private final double I3;
		
		public WurfDynamics(double I1, double I2, double I3) {
			this.I1 = I1;
			this.I2 = I2;
			this.I3 = I3;
		}
		
		@Override
		public double[] f(double[] x) {
			return new double[] {
				(1/I1) * ((I2-I3) * x[1] * x[2]),
				(1/I2) * ((I3-I1) * x[2] * x[0]),
				(1/I3) * ((I1-I2) * x[0] * x[1]),
				
				-1d/2*(x[4]*x[0] + x[5]*x[1] + x[6]*x[2]),
				1d/2*(x[3]*x[0] + x[5]*x[2] - x[6]*x[1]),
				1d/2*(x[3]*x[1] + x[6]*x[0] - x[4]*x[2]),
				1d/2*(x[3]*x[2] + x[4]*x[1] - x[5]*x[0])
			};
		}
		
		public double[] norm(double[] x) {
			final double l = Math.sqrt(x[3]*x[3] + x[4]*x[4] + x[5]*x[5] + x[6]*x[6]);
			
			return new double[] {
					x[0]/l,
					x[1]/l,
					x[2]/l,
					x[3]/l,
					x[4]/l,
					x[5]/l,
					x[6]/l
			};
		}
		
	}

	public double[] aufprall(double[] state, double[][] R) {
		return state;
	}
	
	public double kraftstoss(double[][] R, final double a, final double vs, double[] q) {
		return -2.0 * ((vs - a * R[1][2] * q[1] + a * R[1][1] * q[2]) 
					/  ((1.0 / mass) + (a*a*R[1][2]*R[1][2])/I2 + (a*a*R[1][1]*R[1][1])/I3));
	}
	
	public double[][] q2matrix(final double q0, final double q1, final double q2, final double q3) {
		final double[][] matrix = {
		{ q0*q0 + q1 * q1 - q2*q2 - q3*q3,		2*(q1*q2-q0*q3),						2*(q1*q3+q0*q2) },
		{ 2*(q1*q2+q0*q3),						q0*q0 - q1 * q1 + q2*q2 - q3*q3,		2*(q2*q3-q0*q1) },
		{ 2*(q1*q3-q0*q2), 						2*(q2*q3+q0*q1), 						q0*q0 - q1 * q1 - q2*q2 + q3*q3}
		};
		
		return matrix;
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		if(e.getKeyChar() == 'c') {
			showCoordinates = !showCoordinates;
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		
	}

	@Override
	public JPanel getParameterPanel() {
		return null;
	}

	@Override
	public double getYOffset() {
		return -INITIAL_DISTANCE/2;
	}

}
