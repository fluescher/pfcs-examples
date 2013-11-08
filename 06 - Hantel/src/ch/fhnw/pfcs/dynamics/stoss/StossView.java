package ch.fhnw.pfcs.dynamics.stoss;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.LinkedList;
import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.JPanel;

import ch.fhnw.pfcs.util.MainFrame.GLView;

import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.gl2.GLUT;

public class StossView implements GLView, GLEventListener, KeyListener {
	private static final int INITIAL_DISTANCE = 20;
	private static final int FPS = 60;
	
	private GLCanvas canvas;
	private FPSAnimator animator;
	private GL2 gl;
	
	private boolean showCoordinates = false;
	
	double radius = 1;
	double length = 10;
	double width = 5;
	double height =5;
	double mass = 1;
	
	private static final int FIELD_LENGTH = 20;
	private Wand wand = new Wand(-10, 10, FIELD_LENGTH);
	
	/* Steine */
	private static final int STONE_COUNT = 15;
	private List<Stein> stones = new LinkedList<>();
	
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
	
	
	public StossView() {
		for(int i = 0; i < STONE_COUNT; i++) {
			stones.add(generateStein());
		}
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
		return "Stoss";
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
		gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_LINE);
		drawBorder(gl);
		
		/* kollisionen */
		for(int i = 0; i < stones.size(); i++) {
			for(int j = i+1 ; j < stones.size(); j++) {
				stones.get(i).stoss(stones.get(j));
			}
			/* stein-wand kollision */
			wand.stoss(stones.get(i));
		}
		
		/* zeichnen & stein-wand kollision & bewegen */
		for(Stein s : stones) {
			/* zeichnen */
			blueMaterial(gl);
			gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_FILL);
			s.draw(gl);
			
			/* bewegen */
			s.move(dt);
		}
	}
	
	private Stein generateStein() {
		Stein s = null;
		
		do{
			s = new Stein(1, Math.random() * 10 - 5, Math.random() * 10 -5, Math.random() * 5 - 2.5, Math.random() * 5 - 2.5);
		} while(intersects(s));
		
		return s;
	}
	
	private boolean intersects(Stein a) {
		for(Stein s: stones) {
			if(a.dist(s) < a.radius()+s.radius()+0.1) return true;
		}
		return false;
	}
	
	private void drawBorder(GL2 gl) {
		GLUT glut = new GLUT();
		gl.glPushMatrix();
			disableLighting();
			gl.glColor3d(0.6, 0.6, 0.6);
			gl.glRotated(90, 1, 0, 0);
			
			/* top */
			gl.glScaled(1.0, 0.1, 1.0);
			glut.glutSolidCube(FIELD_LENGTH);
			gl.glTranslated(0, 0, 1);			
			
			enableLighting();
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
		return 0.0;
	}

	private static double[] add(double[] a, double[] b) {
		return new double[] { a[0] + b[0], a[1] + b[1] };
	}
	
	private static double[] sub(double[] a, double[] b) {
		return new double[] { a[0] - b[0], a[1] - b[1] };
	}
	
	private static double[] mult(double[] v, double t) {
		return new double[] {v[0] * t, v[1] * t};
	}
	
	private static double skalarProd(double[] a, double[] b ) {
		return a[0] * b[0] + a[1] * b[1]; 
	}
	
	private static double norm(double[] a) {
		return Math.sqrt(skalarProd(a,a));
	}
	
	private static class Wand {
		private int x;
		private int y;
		private int length;
		
		public Wand(int x, int y, int length) {
			this.x = x;
			this.y = y;
			this.length = length;
		}
		
		private void stoss(Stein s) {
			final double[] vt;
			if(s.x - s.radius() < this.x || s.x + s.radius() > this.x + length) {
				vt = new double[]{-s.vx, s.vy};
			} else if(s.y + s.radius() > this.y || s.y - s.radius() < this.y - length) {
				vt = new double[]{s.vx, -s.vy};
			} else {
				vt = new double[]{s.vx, s.vy};
			}
				
			s.vx = vt[0];
			s.vy = vt[1];
		}
	}
	
	private static class Stein {
		double r1 = 0.2;
		double r2 = 0.4;
		double x, y;
		double vx, vy;
		double m = 1;
		
		public Stein(double m, double x, double y, double vx, double vy) {
			this.m = m;
			this.x = x;
			this.y = y;
			this.vy = vy;
			this.vx = vx;
		}
		
		private void draw(GL2 gl) {
			GLUT glut = new GLUT();
			gl.glPushMatrix();
				gl.glTranslated(x,y,r1);
				glut.glutSolidTorus(r1, r2, 20, 20);
			gl.glPopMatrix();
		}
		
		private void move(double dt) {
			x += vx*dt;
			y += vy*dt;
		}
		
		private double[] stoss1d(double m1, double m2, double v1, double v2, double k) {
			double[] vnew = {
					(m1*v1+m2*v2-(v1-v2)*m2*k)/(m1+m2),
					(m2*v2+m1*v1-(v2-v1)*m1*k)/(m1+m2),
			};
			return vnew;
		}
		
		private double[][] stoss2d(double m1, double m2,
								double[] r1, double[] r2,
								double[] v1, double[] v2, double k) {
			final double[] n = sub(r2, r1);
			final double fac = 1.0/norm(n);
			final double[] ein =  mult(n, fac);;
			
			double v1ns = skalarProd(v1, ein);
			double[] v1n = mult(ein, v1ns);
			double[] v1t = sub(v1, v1n);
			
			double v2ns = skalarProd(v2, ein);
			double[] v2n = mult(ein, v2ns);
			double[] v2t = sub(v2, v2n);
			
			double[] vnew = stoss1d(m1, m2, v1ns, v2ns, k);
			double[] v1nnew = mult( ein, vnew[0]);
			double[] v2nnew = mult( ein, vnew[1]);
			
			double[] v1new = add(v1nnew, v1t);
			double[] v2new = add(v2nnew, v2t);
			
			double[][] res = new double[][]{v1new, v2new};
			
			return res;
		}
		
		private void stoss(Stein other) {
			if(dist(other) < radius()+other.radius()) {
				double [] pos = new double[] { x, y };
				double [] otherPos = new double[] { other.x, other.y };
				double [] v = new double[] { vx, vy };
				double [] otherV = new double[] { other.vx, other.vy };
				double[][] vnew = stoss2d(m, other.m, pos, otherPos, v, otherV, 1);
				
				this.vx = vnew[0][0];
				this.vy = vnew[0][1];
				other.vx = vnew[1][0];
				other.vy = vnew[1][1];
			}
		}
		
		private double dist(Stein other) {
			double dx = this.x - other.x;
			double dy = this.y - other.y;
			return Math.sqrt(dx*dx+dy*dy);
		}
		
		private double radius() {
			return r1 + r2;
		}
	}
}
