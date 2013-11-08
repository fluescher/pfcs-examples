package ch.fhnw.pfcs.dynamics.cylinder;

import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.fixedfunc.GLMatrixFunc;
import javax.swing.JPanel;

import ch.fhnw.pfcs.dynamics.Dynamics;
import ch.fhnw.pfcs.util.MainFrame.GLView;

import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.gl2.GLUT;

public class CylinderView implements GLView, GLEventListener {
	private static final int FPS = 50;
	private static final double radius = 0.25;
	private static final int FLUID_LENGTH = 7;
	private static final int START_X = -(FLUID_LENGTH/2);
	private static final int DROP_COUNT = 30;
	private static final double DROP_LENGTH = (double)FLUID_LENGTH/DROP_COUNT;
	private static final int COLOR_SPEED = 2*FPS;
	private static final int COLOR_SCALES = 10;
	private static final double COLOR_STEP = 1.2/COLOR_SCALES;
	
	private final CylinderDynamics dynamics = new CylinderDynamics(radius);
	private final double[][][] streamLineVectors;
	private double offset = 0;
	private GLCanvas canvas;
	private FPSAnimator animator;
	private GL2 gl;
	
	public CylinderView() {
		streamLineVectors = calculateLines();
	}
	
	@Override
	public void setGLCanvas(GLCanvas canvas) {
		this.canvas = canvas;
		canvas.addGLEventListener(this);
		this.animator = new FPSAnimator(canvas, FPS, true);
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
	public void display(GLAutoDrawable draw) {
		float position[] = { 2.0f, 2.0f, 2.0f, 0.0f };
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, position, 0);
		
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
		
//		drawCoordinates();
		drawCylinder(gl);
		
		disableLighting();
		drawFlow(gl);
		enableLighting();
	}

	@Override
	public void startAnimation() {
		stopAnimation();
		animator.start();
	}

	@Override
	public void stopAnimation() {
		if(animator != null)
			animator.stop();
	}

	@Override
	public void dispose(GLAutoDrawable arg0) {
		
	}

	@Override
	public void init(GLAutoDrawable arg0) {
		this.gl = this.canvas.getGL().getGL2();
		gl.glEnable(GL.GL_DEPTH_TEST);
     	gl.glEnable(GL.GL_POLYGON_OFFSET_FILL);
//     	gl.glEnable(GL.GL_LINE_SMOOTH);
     	gl.glPolygonOffset(0.01f, 0.01f);
     	
     	
     	float ambient[] =
	    { 0.0f, 0.0f, 0.0f, 1.0f };
	    float diffuse[] =
	    { 1.0f, 1.0f, 1.0f, 1.0f };
	    float lmodel_ambient[] =
	    { 0.4f, 0.4f, 0.4f, 1.0f };
	    float local_view[] =
	    { 0.0f };
     	
     	gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, ambient, 0);
 	    gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, diffuse, 0);
 	    gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, lmodel_ambient, 0);
 	    gl.glLightModelfv(GL2.GL_LIGHT_MODEL_LOCAL_VIEWER, local_view, 0);
 	    
 	    enableLighting();
	}

	@Override
	public void reshape(GLAutoDrawable arg0, int arg1, int arg2, int arg3, int arg4) {
		
	}
	
	@Override
	public String getName() {
		return "pfcs - Zylinder Strömung";
	}

	
	private void drawCylinder(GL2 gl) {
		gl.glPushMatrix();
		gl.glTranslated(0, 0, -0.1);
		gl.glColor3d((double)170/255,(double)170/255,(double)170/255);
		new GLUT().glutSolidCylinder(radius, 0.2, 50, 50);
		gl.glEnd();
		gl.glPopMatrix();
	}
	
	private double[][][] calculateLines() {
		final List<double[][]> vectors = new ArrayList<double[][]>(20);
		final double startx = START_X;
		
		for(double sy = 0.5; sy > -0.5; sy=sy-0.04) {
			if(Math.abs(sy) < 0.01) continue;
			double[] vec = new double[]{
					startx,
					sy
			};
			
			vectors.add(calculateLines(vec));
		}
		
		return vectors.toArray(new double[0][][]);
	}
	
	private double[][] calculateLines(double[] startVec) {
		final List<double[]> vectors = new ArrayList<double[]>();
		final double step = 0.003;
		
		while(true) {
			startVec = dynamics.rungeKutta(startVec, step);
			if(startVec[0] > FLUID_LENGTH/2) break;
			vectors.add(startVec);
		}
		
		return vectors.toArray(new double[0][]);
	}
	
	private void drawFlow(GL2 gl) {
		final double colorChange = DROP_LENGTH /COLOR_SCALES;
		for(double[][] line : streamLineVectors) {
			gl.glBegin(GL.GL_LINE_STRIP);
			for(double[] vertice : line) {
				double dropPosition = Math.abs(vertice[0]+START_X+offset)%DROP_LENGTH;
				int colorPos = (int)(dropPosition/colorChange);
				
				gl.glColor3d(1-colorPos*COLOR_STEP, 1-colorPos*COLOR_STEP, 1-colorPos*COLOR_STEP);
				gl.glVertex2d(vertice[0], vertice[1]);
			}
			gl.glEnd();
		}
		
		double newOffset = offset - (DROP_LENGTH/COLOR_SPEED);
		offset = newOffset < 0 ? DROP_LENGTH : newOffset;
	}
	
	private static final class CylinderDynamics extends Dynamics {

		private final double radius;
		private final double radiusSquare;

		public CylinderDynamics(double r) {
			radius = r;
			radiusSquare = Math.pow(radius, 2);
		}

		@Override
		public double[] f(double[] x) {
			double xSquare = Math.pow(x[0], 2);
			double ySquere = Math.pow(x[1], 2);

			return new double[] {
					1 + radiusSquare / (xSquare + ySquere)
							- (2 * radiusSquare * xSquare)/ Math.pow(xSquare + ySquere, 2),

					-(2 * radiusSquare * x[0] * x[1])/ Math.pow(xSquare + ySquere, 2)
			};
		}
	}

	@Override
	public double getInitialDistance() {
		return 2;
	}

	@Override
	public String getHelpText() {
		return "Click and move mouse to navigage\n" +
				"Use the Mouse-Wheel to zoom in and out";
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
