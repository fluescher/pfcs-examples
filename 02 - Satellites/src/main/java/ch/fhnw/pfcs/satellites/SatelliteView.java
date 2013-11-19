package ch.fhnw.pfcs.satellites;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.LinkedList;
import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.fixedfunc.GLMatrixFunc;
import javax.swing.JPanel;

import ch.fhnw.pfcs.satellites.objects.GLObject;
import ch.fhnw.pfcs.satellites.objects.GeostationarySatellite;
import ch.fhnw.pfcs.satellites.objects.Planet;
import ch.fhnw.pfcs.satellites.objects.Satellite;
import ch.fhnw.pfcs.util.MainFrame.GLView;

import com.jogamp.opengl.util.FPSAnimator;

public class SatelliteView implements GLView, GLEventListener, KeyListener {
	private static final double EARTH_RADIUS = 6378;
	private static final double EARTH_AZIMUTH = -23.44;
	private static final double GPS_RADIUS = 26560;
	private static final int SECS_PER_REALSEC = 1*3600;
	private static final int FPS = 80;
	private FPSAnimator animator;
	
	private GLCanvas canvas;
	private GL2 gl;
	private Planet sky;
	private boolean showHelp = false;
	private boolean showCoordinates = false;
	private boolean showSky = true;
	
	private final List<GLObject> toDraw = new LinkedList<GLObject>();
	
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
	public void setGLCanvas(GLCanvas canvas) {
		this.canvas = canvas;
		canvas.addGLEventListener(this);
		this.animator = new FPSAnimator(canvas, FPS, true);
		this.canvas.addKeyListener(this);
	}

	@Override
	public void display(GLAutoDrawable draw) {
		setLightPosition();
		gl.glClearColor(0, 0, 0, 0);
		gl.glClearDepth(1.0);
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

		gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
		
		
		disableLighting();
		if(showSky) sky.draw(gl);
		enableLighting();
		
		if(showCoordinates) drawCoordinates();
		for(GLObject obj : toDraw) {
			obj.draw(canvas.getGL().getGL2());
		}
		
		if(showHelp) {
			disableLighting();
			for(GLObject obj : toDraw) {
				obj.showHelp(gl);
			}
			enableLighting();
		}
	}

	@Override
	public void dispose(GLAutoDrawable draw) {
	}

	@Override
	public void init(GLAutoDrawable draw) {
		this.canvas.requestFocusInWindow();
		this.gl = canvas.getGL().getGL2();

		/* enable depth test */
		gl.glEnable(GL.GL_DEPTH_TEST);
     	gl.glEnable(GL.GL_POLYGON_OFFSET_FILL);
     	gl.glPolygonOffset(1, 1);
		
		configureLighting();
		enableLighting();
		
		createObjects();
	}

	private void configureLighting() {
		float[] amb = {0.4f, 0.4f, 0.4f, 1};
		gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, amb, 0);
	}
	
	private void setLightPosition() {
		float[] pos = {80000,0,80000,1};
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, pos, 0);
	}

	@Override
	public void reshape(GLAutoDrawable arg0, int x, int y, int width, int height) {
	}

	/**
	 * Creates the objects to draw. Uses geostationary information from 
	 * 
	 * http://de.wikipedia.org/wiki/Liste_der_geostation%C3%A4ren_Satelliten
	 */
	private void createObjects() {
		toDraw.clear();
		final double secsPerDraw = SECS_PER_REALSEC/FPS;
		Planet earth = new Planet(EARTH_RADIUS, EARTH_AZIMUTH, secsPerDraw, "/earth_small.jpg");
		
		/* some geostationary satellites */
		toDraw.add(new GeostationarySatellite(earth, 54.9, secsPerDraw)); 	/* Astra 1F */
		toDraw.add(new GeostationarySatellite(earth, 13, secsPerDraw)); 	/* Hotbird 13A */
		toDraw.add(new GeostationarySatellite(earth, 148, secsPerDraw)); 	/* Measat-2 */
		toDraw.add(new GeostationarySatellite(earth, 76, secsPerDraw)); 	/* Eutelsat W75/ABS-1B */
		toDraw.add(new GeostationarySatellite(earth, -111.1, secsPerDraw)); /* Anik F2 */
		
		/* GPS satellites */
		for(int i = 0; i<24; i++)
			toDraw.add(new Satellite(earth,GPS_RADIUS, 12*3600, 55, 90*i, secsPerDraw, (i/4)*60));
		
		toDraw.add(earth);
		sky = new Planet(350000,0,0, "/sky.jpg", false);
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

		@Override
		public String getName() {
			return "pfcs - Satelliten";
		}

		@Override
		public void keyPressed(KeyEvent e) {
			switch(e.getKeyChar()) {
			case 'o':
				showHelp = !showHelp;
				break;
			case 'c':
				showCoordinates = !showCoordinates;
				break;
			case 's':
				showSky = !showSky;
				break;
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {
			
		}

		@Override
		public void keyTyped(KeyEvent e) {
			
		}

		@Override
		public double getInitialDistance() {
			return 100000;
		}

		@Override
		public String getHelpText() {
			return "c - Show coordinates\n" +
					"o - Show orbit circles\n" +
					"s - Show sky \n" +
					"Click and move mouse - Rotate camera\n" +
					"Mouse Wheel - Zoom in and out";
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
