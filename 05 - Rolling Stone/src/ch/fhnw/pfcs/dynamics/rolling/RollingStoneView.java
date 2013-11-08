package ch.fhnw.pfcs.dynamics.rolling;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ch.fhnw.pfcs.dynamics.Dynamics;
import ch.fhnw.pfcs.util.MainFrame.GLView;

import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.awt.ImageUtil;
import com.jogamp.opengl.util.gl2.GLUT;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;

public class RollingStoneView implements GLView, GLEventListener, KeyListener {

	private static final int FPS = 60;
	private static final int STONE_RADIUS = 1;
	private static final int CYLINDER_RADIUS = 20;
	private static final int MAX_SPEEDWAY = 50;
	private static final int SPEEDWAY_LENGTH = 100;
	private static final int SPEEDWAY_HEIGHT = CYLINDER_RADIUS*2;
	private static final int SPEEDWAY_WIDTH = CYLINDER_RADIUS;
	
	private static final double G = 9.81;
	private static final double MAX_FALLING_DISTANCE = -700;
	
	private int startVelocity = 5;
	private double dt = 1.0/(FPS);
	
	private GLCanvas canvas;
	private FPSAnimator animator;
	private GL2 gl;
	private Texture texture;
	
	private JPanel parameterPanel;
	private boolean showCoordinates = false;
	
	private Dynamics flightDynamics = new FlightDynamics();
	private Stone stone;
	
	float no_mat[] =
	{ 0.1f, 0.1f, 0.1f, 1.0f };
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
	
	public RollingStoneView() {
		reset();
		initParameterPanel();
	}

	private void reset() {
		stone = new Stone(-MAX_SPEEDWAY, CYLINDER_RADIUS+STONE_RADIUS, 0);
	}
	
	private void roll() {
		stone.v = new double[] {startVelocity, 0};
	}
	
	private void initParameterPanel() {
		parameterPanel = new JPanel(new GridBagLayout());
		JButton roll = new JButton("Roll!");
		roll.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				reset();
				roll();
			}
		});
		
		JSlider velocity = initVelocitySlider();
		JSlider dt = initDTSlider();
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx=0;
		c.gridy=0;
		c.insets = new Insets(2, 5, 2, 5);
		parameterPanel.add(new JLabel("Start velocity:"), c);
		c.gridx=1;
		c.weightx=0.2;
		parameterPanel.add(velocity, c);
		c.weightx = 0;
		c.gridx=2;
		parameterPanel.add(new JLabel("dt:"), c);
		c.weightx=0.2;
		c.gridx=3;
		parameterPanel.add(dt, c);
		c.weightx = 0;
		c.gridx=4;
		parameterPanel.add(roll, c);
	}

	private JSlider initDTSlider() {
		final JSlider dt = new JSlider();
		dt.setValue(1);
		dt.setMajorTickSpacing(5);
		dt.setMinorTickSpacing(1);
		dt.setPaintTicks(true);
		dt.setPaintLabels(true);
		dt.setMinimum(0);
		dt.setMaximum(10);
		dt.addChangeListener(new ChangeListener(){
			@Override
			public void stateChanged(ChangeEvent e) {
				RollingStoneView.this.dt = (double)dt.getValue()/FPS;
			}
		});
		return dt;
	}

	private JSlider initVelocitySlider() {
		final JSlider velocity = new JSlider();
		velocity.setValue(startVelocity);
		velocity.setMajorTickSpacing(10);
		velocity.setMinorTickSpacing(5);
		velocity.setPaintTicks(true);
		velocity.setPaintLabels(true);
		velocity.setMinimum(0);
		velocity.setMaximum(50);
		velocity.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				startVelocity = velocity.getValue();
			}
		});
		return velocity;
	}
	
	public void startAnimation() {
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
	    loadTexture("/Football.png");
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
	public void reshape(GLAutoDrawable arg0, int x, int y, int width, int height) {	}

	@Override
	public void display(GLAutoDrawable drawable) {
		float position[] = { 200.0f, 200.0f, 100.0f, 0.0f };
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, position, 0);
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		
		if(showCoordinates) drawCoordinates();		/* coordinates */
		drawSurroundingScene(gl);
		drawStone(gl, stone);
	}

	private void drawStone(GL2 gl, Stone s) {
		updateStonePosition(s);
		
		gl.glPushAttrib(GL2.GL_ALL_ATTRIB_BITS);
		gl.glPushMatrix();

		gl.glTranslated(s.x, s.y, s.z);
		gl.glRotated(-s.w, 0, 0, 1);
		
		if(texture != null) {
			gl.glTexEnvf(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_REPLACE);
			texture.enable(gl);
			texture.bind(gl);

			/* reset color */
			gl.glColor4f(1, 1, 1, 1);
			
			GLU glu = new GLU();
			GLUquadric ball = glu.gluNewQuadric();
			glu.gluQuadricTexture(ball, true);
			glu.gluQuadricDrawStyle(ball, GLU.GLU_FILL);
			glu.gluQuadricNormals(ball, GLU.GLU_FLAT);
			glu.gluQuadricOrientation(ball, GLU.GLU_OUTSIDE);
			glu.gluSphere(ball, STONE_RADIUS, 50, 50);
			glu.gluDeleteQuadric(ball);
		} else {
			GLUT glut = new GLUT();
			gl.glColor3d(0, 0, 200);
			gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_FILL);
			glut.glutSolidSphere(STONE_RADIUS, 50, 50);
			
			gl.glColor3d(255, 255, 255);
			gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_LINE);
			glut.glutSolidSphere(STONE_RADIUS, 50, 50);
		}
		
		gl.glPopMatrix();
		gl.glPopAttrib();
	}
	
	private void updateStonePosition(Stone s) {
		if(s.x < 0) {
			s.x = s.x + s.v[0] * dt;
			s.w0 = calculateStoneOmega(s.v());
		} else {
			if(!isCritical(s)) {
				final double omega = calculateOmega(s.v());
				s.a -= omega * dt;
				
				final double h = fallenHeight(s);
				final double v1 = calculateNewV(s.v(), h);
				
				final double[] vs = calculateVelocityVector(s.a, v1);
				s.v = vs;
				s.x += vs[0] * dt;
				s.y += vs[1] * dt;
				s. w0 = calculateStoneOmega(s.v());
			} else {
				/* reset if ball flew to far */
				if(s.y <= MAX_FALLING_DISTANCE) {
					reset(); return;
				}
				
				s.a = 0; /* make sure always critical */
				final double[] d = flightDynamics.euler(new double[] {s.x, s.y, s.v[0], s.v[1]}, dt);
				s.x = d[0];
				s.y = d[1];
				s.v[0] = d[2];
				s.v[1] = d[3];
			}
		}
		
		/* Eigenrotation */
		s.w = s.w + (s.w0*180/Math.PI) * dt;
	}
	
	private double calculateNewV(double v0, double h) {
		return Math.sqrt(v0*v0 + 2 * G * h);
	}
	
	private double fallenHeight(Stone s) {
		return CYLINDER_RADIUS - (CYLINDER_RADIUS * Math.sin(s.a));
	}
	
	private double[] calculateVelocityVector(double angle, double v) {
		return new double [] {
				Math.sin(angle) * v,
				-Math.cos(angle) * v 
		};
	}
	
	private boolean isCritical(Stone s) {
		return s.a <= 		(s.v()*s.v())
						/	(G * (CYLINDER_RADIUS+STONE_RADIUS));
	}
	
	private double calculateStoneOmega(double velocity) {
		return velocity/STONE_RADIUS;
	}
	
	private double calculateOmega(double velocity) {
		return velocity/(CYLINDER_RADIUS+STONE_RADIUS);
	}
	
	private void drawSurroundingScene(GL2 gl) {
		gl.glPushMatrix();
			gl.glTranslated(-SPEEDWAY_LENGTH/2, CYLINDER_RADIUS-SPEEDWAY_HEIGHT/2, 0);
			drawBox(gl, SPEEDWAY_LENGTH, SPEEDWAY_HEIGHT, SPEEDWAY_WIDTH, new double[] { 0, 0, 1});
		gl.glPopMatrix();
		gl.glPushMatrix();
			gl.glTranslated(0, 0, -CYLINDER_RADIUS/2);
			new GLUT().glutSolidCylinder(CYLINDER_RADIUS, CYLINDER_RADIUS, 50, 50);
		gl.glPopMatrix();
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
		gl.glVertex2d(0,1);
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

	@Override
	public void keyPressed(KeyEvent e) {
		if(e.getKeyChar() == 'c') {
			showCoordinates = !showCoordinates;
		}
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
	        gl.glNormal3f(0.0f,0.0f,1.0f);
	        gl.glVertex3d(-length/2,		-width/2,		height/2);
	        gl.glVertex3d(length/2,			-width/2,		height/2);
	        gl.glVertex3d(length/2,			width/2,		height/2);
	        gl.glVertex3d(-length/2,		width/2,		height/2);
	      gl.glEnd();
	      gl.glBegin(GL2.GL_POLYGON);/* f3:back */
	      	gl.glColor3d(color[0], color[1], color[2]);
	      	gl.glNormal3f(1.0f,0.0f,0.0f);
	      	gl.glVertex3d(length/2,			width/2,		height/2);
	      	gl.glVertex3d(length/2,			width/2,		-height/2);
	      	gl.glVertex3d(-length/2,		width/2,		-height/2);
	      	gl.glVertex3d(-length/2,		width/2,		height/2);
	      gl.glEnd();
	      gl.glBegin(GL2.GL_POLYGON);/* f4: top */
    		gl.glColor3d(color[0], color[1], color[2]);
	        gl.glNormal3f(0.0f,0.0f,-1.0f);
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
		this.animator = new FPSAnimator(canvas, FPS, true);
	}

	@Override
	public String getName() {
		return "pfcs - Rolling Stone";
	}

	@Override
	public double getInitialDistance() {
		return 150;
	}

	@Override
	public String getHelpText() {
		return "Click and move mouse - Rotate camera";
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
	
	private static final class FlightDynamics extends Dynamics {

		@Override
		public double[] f(double[] x) {
			return new double [] {
				x[2],
				x[3],
				0,
				-G
			};
		}
	}
	
	private static final class Stone {
		public double x;
		public double y;
		public double z;
		
		public double v[] = new double[2];
		public double v() {
			return Math.sqrt(v[0]*v[0]+v[1]*v[1]);
		}
		public double a;
		public double w;
		public double w0;
		
		public Stone(double startx, double starty, double startz) {
			x = startx; y = starty; z = startz;
			a = Math.PI/2;
			w = 0;
		}
	}

	@Override
	public JPanel getParameterPanel() {
		return parameterPanel;
	}

	@Override
	public double getYOffset() {
		return 0;
	}
}
