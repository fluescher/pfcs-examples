package ch.fhnw.pfcs.test;
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
public class MySecond
       implements WindowListener, GLEventListener
{

     GLCanvas canvas;                                           // OpenGl-Canvas
     double left = -10, right = 10 ;                            // Koordinatenbereich
     double bottom, top;                                        // werden in reshape gesetzt
     double near = -10, far = 10;                               // Clipping Bereich
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


     void zeichnePyramide(GL2 gl, double a, double h)             // Pyramide zeichnen
     {  gl.glBegin(GL2.GL_POLYGON);                               // Boden
          gl.glVertex3d(a,0,a);
          gl.glVertex3d(a,0,-a);
          gl.glVertex3d(-a,0,-a);
          gl.glVertex3d(-a,0,a);
        gl.glEnd();
       gl.glBegin(GL2.GL_POLYGON);                                // Seitenflaechen
          gl.glVertex3d(a,0,a);
          gl.glVertex3d(a,0,-a);
          gl.glVertex3d(0,h,0);
        gl.glEnd();
       gl.glBegin(GL2.GL_POLYGON);
          gl.glVertex3d(a,0,-a);
          gl.glVertex3d(-a,0,-a);
          gl.glVertex3d(0,h,0);
        gl.glEnd();
       gl.glBegin(GL2.GL_POLYGON);
          gl.glVertex3d(-a,0,-a);
          gl.glVertex3d(-a,0,a);
          gl.glVertex3d(0,h,0);
        gl.glEnd();
       gl.glBegin(GL2.GL_POLYGON);
          gl.glVertex3d(-a,0,a);
          gl.glVertex3d(a,0,a);
          gl.glVertex3d(0,h,0);
        gl.glEnd();
     }


     public MySecond()                                           // Konstruktor
     {  Frame f = new Frame("MySecond");
        canvas = new GLCanvas();                                // OpenGL-Window
        f.setSize(800, 600);
        f.setBackground(Color.gray);
        f.addWindowListener(this);
        canvas.addGLEventListener(this);
        f.add(canvas);
        f.setVisible(true);
     }


     public static void main(String[] args)                     // main-Methode der Applikation
     {  new MySecond();
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
        zeichneAchsen(gl, 1);
        zeichnePyramide(gl, 4, 4);
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
		// TODO Auto-generated method stub
		
	}

  }
