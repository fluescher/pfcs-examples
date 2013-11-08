package ch.fhnw.pfcs.util;


import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.GLCanvas;

public final class DynamicGLCanvas {

	private final RotateHandler rotateHandler;
	private GLCanvas canvas;
	
	public DynamicGLCanvas(GLCanvas canvas, double initialDistance, double yOffset) {
		super();
		this.canvas = canvas;
		
		rotateHandler = new RotateHandler(this, initialDistance, yOffset);
		
		getCanvas().addMouseListener(rotateHandler);
		getCanvas().addMouseMotionListener(rotateHandler);
		getCanvas().addGLEventListener(rotateHandler);
		getCanvas().addKeyListener(rotateHandler);
		getCanvas().addMouseWheelListener(rotateHandler);
	}
	
	public DynamicGLCanvas(GLCanvas canvas, double initialDistance) {
		this(canvas, initialDistance, 0);
	}
	
	public final GLCanvas getCanvas() {
		return canvas;
	}
	
	public KeyListener getKeyListener() {
		return rotateHandler;
	}
	
	private static final class RotateHandler implements MouseListener, MouseMotionListener, GLEventListener, KeyListener, MouseWheelListener {
		private static final double ROTATE_SPEED = 0.003;
		private static final double KEY_DELTA = 80;
		private final DynamicGLCanvas frame;
		private final double distanceDelta;
		private double aspectRatio;
		private GL2 gl;

		private int startx;
		private int starty;
		
		private final double initialDistance;
		private final double yOffset;
		private double distance;
		private double angleX = 0;
		private double angleY = 358;
		
		public RotateHandler(DynamicGLCanvas frame, double initialDistance, double yOffset) {
			this.frame = frame;
			distance = initialDistance;
			distanceDelta = initialDistance/100;
			this.initialDistance = initialDistance;
			this.yOffset = yOffset;
		}
		
		@Override
		public void mouseClicked(MouseEvent e) {
			
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			
		}

		@Override
		public void mouseExited(MouseEvent e) {
			
		}

		@Override
		public void mousePressed(MouseEvent e) {
			startx = e.getX();
			starty = e.getY();
		}

		@Override
		public void mouseReleased(MouseEvent e) {
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			angleX = getAngle(angleX - (startx - e.getX())*ROTATE_SPEED);
			angleY = getAngle(angleY + (starty - e.getY())*ROTATE_SPEED);
		}

		@Override
		public void mouseMoved(MouseEvent arg0) {
			
		}

		@Override
		public void init(GLAutoDrawable arg0) {
			gl = frame.getCanvas().getGL().getGL2();
		}
		
		@Override
		public void display(GLAutoDrawable arg0) {
			gl.glMatrixMode(GL2.GL_MODELVIEW);
			gl.glLoadIdentity();
			gl.glTranslated(0.00, yOffset, -initialDistance);
			
			gl.glMatrixMode(GL2.GL_PROJECTION);
			gl.glLoadIdentity();
//			gl.glOrtho(-initialDistance, initialDistance, -initialDistance, initialDistance, initialDistance/100, 5*initialDistance);
			
			double DEG2RAD = 3.14159265 / 180;
		    double tangent = Math.tan(60/2 * DEG2RAD);  // tangent of half fovY
		    double height = initialDistance/100 * tangent;          	// half height of near plane
		    double width = height * aspectRatio;      
		    
		    // params: left, right, bottom, top, near, far
		    gl.glFrustum(-width, width, -height, height, initialDistance/100, 10*initialDistance);
			
			gl.glMatrixMode(GL2.GL_MODELVIEW);
			gl.glLoadIdentity();
			gl.glTranslated(0.00, 0.00, -distance);

			gl.glRotated(-angleY, 1, 0, 0); /* elevation */ 
			gl.glRotated(angleX, 0, 1, 0); /* azimuth */
			gl.glTranslated(0.00, yOffset, 0.00);
		}

		@Override
		public void reshape(GLAutoDrawable arg0, int x, int y, int width, int height) {
			gl.glViewport(0, 0, width, height);
			aspectRatio = width/(double)height;
		}
		
		@Override
		public void dispose(GLAutoDrawable arg0) {
			
		}
		
		private double getAngle(double angle) {
			if(angle > 359) return 1;
			if(angle < 1) return 359;
			
			return angle;
		}

		@Override
		public void keyPressed(KeyEvent evt) {
			
			switch(evt.getExtendedKeyCode()) {
			case KeyEvent.VK_LEFT:
				angleX = getAngle(angleX + KEY_DELTA*ROTATE_SPEED);
				break;
			case KeyEvent.VK_RIGHT:
				angleX = getAngle(angleX- KEY_DELTA*ROTATE_SPEED);
				break;
			case KeyEvent.VK_UP:
				angleY = getAngle(angleY - KEY_DELTA*ROTATE_SPEED);
				break;
			case KeyEvent.VK_DOWN:
				angleY = getAngle(angleY + KEY_DELTA*ROTATE_SPEED);
				break;
			case KeyEvent.VK_PLUS:
				distance -= distanceDelta;
				break;
			case KeyEvent.VK_MINUS:
				distance += distanceDelta; 
				break;
			}
			switch(evt.getKeyChar()) {
			case '+':
				distance -= distanceDelta;
				break;
			case '-':
				distance += distanceDelta;
				break;
			}
		}

		@Override
		public void keyReleased(KeyEvent evt) {
		}

		@Override
		public void keyTyped(KeyEvent evt) {
		}

		@Override
		public void mouseWheelMoved(MouseWheelEvent evt) {
			distance += evt.getWheelRotation() * distanceDelta;
		}
		
	}
}
