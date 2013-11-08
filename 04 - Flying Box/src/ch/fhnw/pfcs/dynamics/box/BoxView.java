package ch.fhnw.pfcs.dynamics.box;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.JPanel;

import ch.fhnw.pfcs.dynamics.Dynamics;
import ch.fhnw.pfcs.util.MainFrame.GLView;

import com.jogamp.opengl.util.FPSAnimator;

public class BoxView implements GLView, GLEventListener, KeyListener {
	private static final int FPS = 30;
	private static final double G = 9.81;
	private static final float MAX_FALL_HEIGHT = 200;
	private static final int MAX_SIZE = 10; 
	private static final int MIN_SIZE = 1;
	private static final double GROUND = -MAX_FALL_HEIGHT/2;
	private static final int BOX_COUNT = 500;
	private static final double MAX_ROTATE_SPEED = 1;
	private GLCanvas canvas;
	private FPSAnimator animator;
	private GL2 gl;
	
	private boolean showCoordinates = false;
	private boolean showOmegas = false;
	
	double[] prototype = {	8/20.0, 5/20.0, -10/20.0, 	/* Winkelgeschwindigkeit */
							1, 0, 0, 0, 
							0, MAX_FALL_HEIGHT, 0,  	/* Position */
							0, 0, 0};					/* Quaternion */
	
	private double dt = 0.03;

	double phi = 0;
	double phi_speed = 1;
	
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
	
	
	private final List<Box> boxes = new LinkedList<Box>();
	
	public BoxView() {
		for(int i = 0; i < BOX_COUNT; i++) boxes.add(generateBox(true));
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
		return "Falling Boxes";
	}

	@Override
	public double getInitialDistance() {
		return MAX_FALL_HEIGHT/2;
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
		float position[] = { MAX_FALL_HEIGHT, MAX_FALL_HEIGHT, -MAX_FALL_HEIGHT, 0.0f };
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, position, 0);
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		
		drawBoxes(gl);
		if(showCoordinates) drawCoordinates();
	}

	private void drawBoxes(GL2 gl) {
		int toCreate = 0;
		Iterator<Box> iter = boxes.iterator();
		
		while(iter.hasNext()) {
			if(!drawBox(iter.next(), gl)) {
				iter.remove();
				toCreate++;
			}
		}
		
		for(int i = 0; i < toCreate; i++) boxes.add(generateBox());
	}
	
	private Box generateBox() {
		return generateBox(false);
	}
	
	private Box generateBox(boolean randomHeight) {
		final double h = Math.random() * MAX_SIZE + MIN_SIZE;
		final double w = Math.random() * MAX_SIZE + MIN_SIZE;
		final double l = Math.random() * MAX_SIZE + MIN_SIZE;
		
		Box b =  new Box(new double[13], h, w, l);
		
		final double x = Math.random()*2*MAX_FALL_HEIGHT-MAX_FALL_HEIGHT*1;
		final double y = randomHeight ? (MAX_FALL_HEIGHT-Math.random()*MAX_FALL_HEIGHT)-MAX_FALL_HEIGHT/2 : MAX_FALL_HEIGHT/2;
		final double z = Math.random()*2*MAX_FALL_HEIGHT-MAX_FALL_HEIGHT*1;
		
		b.field[0] = Math.random() * MAX_ROTATE_SPEED * 2 - MAX_ROTATE_SPEED;
		b.field[1] = Math.random() * MAX_ROTATE_SPEED * 2 - MAX_ROTATE_SPEED;
		b.field[2] = Math.random() * MAX_ROTATE_SPEED * 2 - MAX_ROTATE_SPEED;
		
		b.field[3] = Math.random();
		b.field[4] = Math.random();
		b.field[5] = Math.random();
		b.field[6] = Math.random();
		
		b.field[7] = x;
		b.field[8] = y;
		b.field[9] = z;
		
		return b;
	}
	
	private void drawBox(GL2 gl, double length, double width, double height, double[] color) {
		  gl.glColor3d(0, 1, 0);
	      gl.glBegin(GL2.GL_POLYGON);/* f1: front */
	      	gl.glColor3d(color[0], color[1], color[2]);
	        gl.glNormal3f(-1.0f,0.0f,0.0f);
	        gl.glVertex3d(-length/2,		-width/2,		height/2);
	        gl.glVertex3d(-length/2,		-width/2,		-height/2);
	        gl.glVertex3d(length/2,			-width/2,		-height/2);
	        gl.glVertex3d(length/2,			-width/2,		height/2);
	      gl.glEnd();
	      gl.glBegin(GL2.GL_POLYGON);/* f2: bottom */
	      gl.glColor3d(color[0], color[1], color[2]);
	        gl.glNormal3f(0.0f,0.0f,-1.0f);
	        gl.glVertex3d(-length/2,		-width/2,		height/2);
	        gl.glVertex3d(length/2,			-width/2,		height/2);
	        gl.glVertex3d(length/2,			width/2,		height/2);
	        gl.glVertex3d(-length/2,		width/2,		height/2);
	      gl.glEnd();
	      gl.glBegin(GL2.GL_POLYGON);/* f3:back */
	      	gl.glColor3d(color[0], color[1], color[2]);
	      	gl.glVertex3d(length/2,			width/2,		height/2);
	      	gl.glVertex3d(length/2,			width/2,		-height/2);
	      	gl.glVertex3d(-length/2,		width/2,		-height/2);
	      	gl.glVertex3d(-length/2,		width/2,		height/2);
	      gl.glEnd();
	      gl.glBegin(GL2.GL_POLYGON);/* f4: top */
      		gl.glColor3d(color[0], color[1], color[2]);
	        gl.glNormal3f(0.0f,0.0f,1.0f);
	        gl.glVertex3d(length/2,			width/2,		-height/2);
	        gl.glVertex3d(length/2,			-width/2,		-height/2);
	        gl.glVertex3d(-length/2,		-width/2,		-height/2);
	        gl.glVertex3d(-length/2,		width/2,		-height/2);
	      gl.glEnd();
	      gl.glBegin(GL2.GL_POLYGON);/* f5: left */
	      	gl.glColor3d(color[0], color[1], color[2]);
	        gl.glNormal3f(0.0f,1.0f,0.0f);
	        gl.glVertex3d(-length/2,		-width/2,		height/2);
	        gl.glVertex3d(-length/2,		width/2,		height/2);
	        gl.glVertex3d(-length/2,		width/2,		-height/2);
	        gl.glVertex3d(-length/2,		-width/2,		-height/2);
	      gl.glEnd();
	      gl.glBegin(GL2.GL_POLYGON);/* f6: right */
	      	gl.glColor3d(color[0], color[1], color[2]);
	        gl.glNormal3f(0.0f,-1.0f,0.0f);
	        gl.glVertex3d(length/2,		-width/2,		height/2);
	        gl.glVertex3d(length/2,		-width/2,		-height/2);
	        gl.glVertex3d(length/2,		width/2,		-height/2);
	        gl.glVertex3d(length/2,		width/2,		height/2);
	      gl.glEnd();
	}
	
	private boolean drawBox(Box b, GL2 gl) {
		gl.glPushMatrix();
		
		b.field = b.dynamics.norm(b.dynamics.rungeKutta(b.field, dt));
		
		if(b.field[8] < GROUND) return false;

		final double phiRad = 2*Math.acos(b.field[3]); //in rad
		final double phiDeg = Math.toDegrees(phiRad);
		gl.glTranslated(b.field[7], b.field[8], b.field[9]);
		gl.glRotated(phiDeg, b.field[4], b.field[5], b.field[6]);
		
		if(showOmegas) {
			gl.glBegin(GL2.GL_LINES);
				gl.glVertex3d(0, 0, 0);
				gl.glVertex3d(b.field[0]*MAX_SIZE, b.field[1]*MAX_SIZE, b.field[2]*MAX_SIZE);
			gl.glEnd();
		}
		drawBox(gl, b.length, b.width, b.height, b.color);
		
		gl.glPopMatrix();
		return true;
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
	
	
	private static final class Box {
		public final WurfDynamics dynamics;
		
		public final double radius = 1;
		public final double mass;
		public final double length;
		public final double width;
		public final double height;
		
		//Trägheitsmomente
		public final double I1;
		public final double I2;
		public final double I3;
		public double[] field;
		public double[] color;
		
		public Box(double[] field, double height, double length, double width) {
			this.height = height;
			this.length = length;
			this.width = width;
			this.mass = height * length * width; /* make mass proportional to volume */
			
			I1 = mass*(radius*radius/4.0+length*length/12.0);
			I2 = I1;
			I3 = mass*length*length/2.0;
			dynamics = new WurfDynamics(I1, I2, I3);
			
			this.field = field.clone();
			color = new double[3];
			color[0] = Math.random();
			color[1] = Math.random();
			color[2] = Math.random();
		}
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
				1d/2*(x[3]*x[2] + x[4]*x[1] - x[5]*x[0]),
				
				x[10], 	/* x velocity */
				x[11], 	/* y velocity */
				x[12],  /* z velocity */
				
				0,
				-G,
				0
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
					x[6]/l,
					x[7],
					x[8],
					x[9],
					x[10],
					x[11],
					x[12]
			};
		}
		
	}


	@Override
	public void keyPressed(KeyEvent e) {
		switch(e.getKeyChar()) {
		case 'c':
		case 'C':
			showCoordinates = !showCoordinates;
			break;
		case 'w':
		case 'W':
			showOmegas = !showOmegas;
			break;
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
		return 0;
	}

}
