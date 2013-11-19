package ch.fhnw.pfcs.dynamics;
//  -------------   JOGL SampleProgram  (Pyramide) ------------

import java.awt.Color;
import java.awt.Frame;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.GLCanvas;
public class Lorenz
       implements WindowListener, GLEventListener
{

     GLCanvas canvas;                                           // OpenGl-Canvas
     double left = -80, right = 80;                           // Koordinatenbereich
     double bottom, top;                                        // werden in reshape gesetzt
     double near = -80, far = 80;                               // Clipping Bereich
     double elev = 20;                                          // Elevation Kamera-System
     double azim = 30;                                          // Azimut Kamera-System


     //  ------------------  Methoden  --------------------

     void zeichneAchsen(GL2 gl, double a)                         // Koordinatenachsen zeichnen
     {  gl.glBegin(GL2.GL_LINES);
          gl.glVertex3d(0,0,0); gl.glVertex3d(a,0,0);
          gl.glVertex3d(0,0,0); gl.glVertex3d(0,a,0);
          gl.glVertex3d(0,0,0); gl.glVertex3d(0,0,a);
        gl.glEnd();
     }

     private static class LorenzDynamics extends Dynamics {
		@Override
		public double[] f(double[] x) {
			return new double[] { 	10 * x[1] - 10 * x[0],
									28*x[0] - x[1] - x[0] * x[2],
									x[0]*x[1]-(8/3)*x[2]};
		}
	 }
     LorenzDynamics dynamics = new LorenzDynamics();
     
     void zeichneKurve(GL2 gl) {
    	 double[] cur = {1, 1, 1};
    	 final double dt = 0.01;
    	 gl.glBegin(GL.GL_LINE_STRIP);
    	 for(int i = 0; i < 1000; i++) {
//    		 cur = dynamics.euler(cur, dt);
    		 cur = dynamics.rungeKutta(cur, dt);
    		 System.out.println(cur[0]+", "+cur[1]+", "+cur[2]);
	    	 gl.glVertex3d(cur[0],cur[1],cur[2]);
    	 }
    	 gl.glEnd();
     }

     public Lorenz()                                           // Konstruktor
     {  Frame f = new Frame("Lorenz");
        canvas = new GLCanvas();                                // OpenGL-Window
        f.setSize(800, 600);
        f.setBackground(Color.gray);
        f.addWindowListener(this);
        canvas.addGLEventListener(this);
        f.add(canvas);
        f.setVisible(true);
     }


     public static void main(String[] args)                     // main-Methode der Applikation
     {  new Lorenz();
     }


     //  ---------  OpenGL-Events  -----------------------

     public void init(GLAutoDrawable drawable)
     {  GL gl = drawable.getGL();                               // OpenGL-Objekt
        gl.glClearColor(0.0f, 0.0f, 1.0f, 1.0f);                // erasing color
     }


     public void display(GLAutoDrawable drawable)
     {  GL2 gl = drawable.getGL().getGL2();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT);                     // Bild loeschen
        gl.glColor3d(0,1,1);                                    // Zeichenfarbe
        gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_LINE);     // Polygon Zeichen-Modus
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();                                    // Kamera-System positionieren:
        gl.glRotated(elev, 1, 0, 0);                            // Drehung um x-Achse
        gl.glRotated(-azim, 0, 1, 0);                           // Drehung um y-Achse
        zeichneAchsen(gl, 20);
        zeichneKurve(gl);
     }


     public void reshape(GLAutoDrawable drawable,               // Window resized
                         int x, int y,
                         int width, int height)
     {  GL2 gl = drawable.getGL().getGL2();
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


	@Override
	public void dispose(GLAutoDrawable arg0) {
		
	}

  }
