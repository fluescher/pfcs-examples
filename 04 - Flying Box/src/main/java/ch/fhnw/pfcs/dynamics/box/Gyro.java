package ch.fhnw.pfcs.dynamics.box;

import java.awt.Color;
import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.GLCanvas;

import ch.fhnw.pfcs.dynamics.Dynamics;

import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.gl2.GLUT;

public class Gyro
       implements WindowListener, GLEventListener, KeyListener
{

     GLCanvas canvas;
     GLUT glut = new GLUT();
     double left = -10, right = 10 ;
     double bottom, top;
     double near = -10, far = 10;
     static double r = 1, l = 8, m = 1; // Zylinderparam
     static double I1=m*(r*r/4d+l*l/12d); // Trägheitsmoment
     static double I2=I1;
     static double I3=m*l*l/2d;
     double[] q = { 8, 5, 80, 1, 0, 0, 0 };
     double dt = 0.001;
     double elev = 20;
     double azim = 30;
     double dist = 4;
     
     private static class GyroDynamics extends Dynamics {
    	 public double[] f(double[] x) {
    		 double[] result = new double[x.length];
    		 result[0] = 1d/I1*((I2-I3)*x[1]*x[2]);
    		 result[1] = 1d/I2*((I3-I1)*x[2]*x[0]);
    		 result[2] = 1d/I3*((I1-I2)*x[0]*x[1]);
    		 
    		 result[3] = -0.5*(x[4]*x[0] + x[5]*x[1] + x[6]*x[2]);
    		 result[4] = 0.5*(x[3]*x[0] + x[5]*x[2] - x[6]*x[1]);
    		 result[5] = 0.5*(x[3]*x[1] + x[6]*x[0] - x[4]*x[2]);
    		 result[6] = 0.5*(x[3]*x[2] + x[4]*x[1] - x[5]*x[0]);
    		 return result;
    	 }
     }
     
     GyroDynamics gyroDynamics = new GyroDynamics();

     //  ------------------  Methoden  --------------------

     void zeichneAchsen(GL2 gl, double a)                         // Koordinatenachsen zeichnen
     {  gl.glBegin(GL2.GL_LINES);
          gl.glVertex3d(0,0,0); gl.glVertex3d(a,0,0);
          gl.glVertex3d(0,0,0); gl.glVertex3d(0,a,0);
          gl.glVertex3d(0,0,0); gl.glVertex3d(0,0,a);
        gl.glEnd();
     }


     public Gyro()                                          // Konstruktor
     {  
    	Frame f = new Frame("Gyro");
        canvas = new GLCanvas();                                // OpenGL-Window
        f.setSize(800, 600);
        f.setBackground(Color.black);
        f.addWindowListener(this);
        canvas.addGLEventListener(this);
        f.add(canvas);
        f.setVisible(true);
        canvas.addKeyListener(this);
        f.addKeyListener(this);
     }


     public static void main(String[] args) {                    // main-Methode der Applikation
    	 new Gyro();
     }


     //  ---------  OpenGL-Events  -----------------------

     public void init(GLAutoDrawable drawable) {  
    	GL2 gl = drawable.getGL().getGL2();                               // OpenGL-Objekt
        gl.glClearColor(0.2f, 0.2f, 0.2f, 1.0f);                // erasing color
        gl.glEnable(GL.GL_DEPTH_TEST);                          // Sichtbarkeits-Test (z-Buffer)
        gl.glEnable(GL.GL_POLYGON_OFFSET_FILL);                 // Polygon OffsetFill Mode
        gl.glPolygonOffset(1,1);
        //initLighting(gl);
        //setReflectionParam(gl);
        FPSAnimator anim = new FPSAnimator(canvas, 200, true);  // Animations-Thread, 200 Frames/sek
        anim.start();
     }


     public void display(GLAutoDrawable drawable)
     {  GL2 gl = drawable.getGL().getGL2();
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();

        gl.glTranslated(0, 0, -dist);
        gl.glRotated(elev, 1, 0, 0);
        gl.glRotated(-azim, 0, 1, 0);
        
        gl.glColor3d(0,1,1);
        zeichneAchsen(gl,10);
        
        float qnorm = (float) Math.sqrt(q[3] * q[3] + q[4] * q[4] + 
        		q[5] * q[5] + q[6] * q[6]);
        q[3] = q[3]/qnorm;
        q[4] = q[4]/qnorm;
        q[5] = q[5]/qnorm;
        q[6] = q[6]/qnorm;
        double phiRad = 2*Math.acos(q[3]);
        double phiDeg = Math.toDegrees(phiRad);
        
        gl.glRotated(phiDeg, q[4], q[5], q[6]);
        q = gyroDynamics.rungeKutta(q, dt);
        gl.glTranslated(0, 0, -l/2);

        gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_FILL);
        gl.glColor3d(0,0,1);
        glut.glutSolidCylinder(r, l, 50, 50);
        
        gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_LINE);
        gl.glColor3d(1,1,1);
        glut.glutSolidCylinder(r, l, 50, 50);
     }
     
     void initLighting(GL2 gl) {
    	 gl.glEnable(GL2.GL_LIGHTING);
    	 gl.glEnable(GL2.GL_NORMALIZE);
    	 gl.glShadeModel(GL2.GL_SMOOTH);
    	 float[] amb = { 0.4f, 0.4f, 0.4f, 1.0f };
    	 gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, amb, 0);
    	 gl.glEnable(GL2.GL_LIGHT0);
     }
     
     void setLightPos(GL2 gl, float x, float y, float z) {
    	 float [] lightPos = {x, y, z, 1 };
    	 gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, lightPos, 0);
     }

     void setReflectionParam(GL2 gl) {
    	 float[] amb = {0, 0.2f, 0.4f, 1 };
    	 float[] diff = {0, 0.3f, 0.5f, 1 };
    	 float[] spec = { 0, 0.2f, 0.3f, 1 };
    	 float[] specExp = { 20 };
    	 gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT,  amb, 0);
    	 gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_DIFFUSE, diff, 0);
    	 gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, spec, 0);
    	 gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SHININESS, specExp, 0);
     }

     public void reshape(GLAutoDrawable drawable,               // Window resized
                         int x, int y,
                         int width, int height) {  
    	GL2 gl = drawable.getGL().getGL2();
        double yxRatio = (float)height/width;                   // aspect-ratio
        bottom =  yxRatio * left;
        top = yxRatio * right;
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glOrtho(left, right, bottom, top, near, far);        // Viewing-Volume (im Raum)
        gl.glViewport(0, 0, width, height);
     }


     public void displayChanged(GLAutoDrawable drawable,
                                boolean modeChanged,
                                boolean deviceChanged)
     { }


     //  ---------  Window-Events  --------------------

     public void windowClosing(WindowEvent e)
     {  System.exit(0);
     }
     public void windowActivated(WindowEvent e) {  }
     public void windowClosed(WindowEvent e) {  }
     public void windowDeactivated(WindowEvent e) {  }
     public void windowDeiconified(WindowEvent e) {  }
     public void windowIconified(WindowEvent e) {  }
     public void windowOpened(WindowEvent e) {  }


     //  --------  Keyboard-Events  -----------------

     public void keyPressed(KeyEvent e)
     { int code = e.getKeyCode();
       switch ( code )
       {
         case KeyEvent.VK_UP:    elev++;
                                 break;
         case KeyEvent.VK_DOWN:  elev--;
                                 break;
         case KeyEvent.VK_RIGHT: azim++;
                                 break;
         case KeyEvent.VK_LEFT : azim--;
                                 break;
       }
     }
     public void keyReleased(KeyEvent e) { }
     public void keyTyped(KeyEvent e) { }


	@Override
	public void dispose(GLAutoDrawable arg0) {
		// TODO Auto-generated method stub
		
	}

  }
