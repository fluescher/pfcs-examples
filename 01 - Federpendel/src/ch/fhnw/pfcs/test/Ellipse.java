package ch.fhnw.pfcs.test;

import java.awt.Frame;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.GLCanvas;

import com.jogamp.opengl.util.FPSAnimator;

public class Ellipse implements WindowListener, GLEventListener {
	// ------------------ Methoden --------------------

	double phi = 3.8; // Phasenkonstante
	
	void zeichneAchsen(GL2 gl) // Koordinatenachsen zeichnen
	{
		gl.glBegin(GL.GL_LINES);
		gl.glVertex2d(-1, 0); // x-Achse
		gl.glVertex2d(1, 0);
		gl.glVertex2d(0, -1); // y-Achse
		gl.glVertex2d(0, 1);
		gl.glEnd();
	}

	void zeichneKurve(GL2 gl) // Lissajous-Kurve
	{
		phi = phi + 0.005;
		
		int nPkte = 40; // Anzahl Punkte
		double dt = 2.0 * Math.PI / nPkte; // Parameter-Schrittweite
		double r = 0.5;
		gl.glBegin(GL.GL_LINE_LOOP);
		for (int i = 0; i < nPkte; i++)
			gl.glVertex2d(r * Math.cos(i * dt), // x = r*cos(i*dt)
					r * Math.sin(i * dt - phi)); // y = r*sin(i*dt-phi)
		gl.glEnd();
	}

	public Ellipse() // Konstruktor
	{
		Frame f = new Frame("MyFirst");
		f.setSize(800, 600);
		f.addWindowListener(this);
		GLCanvas canvas = new GLCanvas(); // OpenGL-Window
		canvas.addGLEventListener(this);
		f.add(canvas);
		f.setVisible(true);
		
		FPSAnimator animator = new FPSAnimator(canvas, 200, true);
		animator.start();
	}

	public static void main(String[] args) // main-Methode der Applikation
	{
		new Ellipse();
	}

	// --------- OpenGL-Events -----------------------

	public void init(GLAutoDrawable drawable) {
		GL gl = drawable.getGL(); // OpenGL-Objekt
		gl.glClearColor(0.0f, 0.0f, 1.0f, 1.0f); // erasing color
	}

	public void display(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
		gl.glClear(GL.GL_COLOR_BUFFER_BIT); // Bild loeschen
		gl.glColor3d(0, 1, 1); // Zeichenfarbe
		zeichneAchsen(gl);
		gl.glColor3d(1, 0.2, 0.1); // Zeichenfarbe
		zeichneKurve(gl);
	}

	public void reshape(GLAutoDrawable drawable, // Window resized
			int x, int y, int width, int height) {
		GL gl = drawable.getGL();
		gl.glViewport(0, 0, width, height);
	}

	public void displayChanged(GLAutoDrawable drawable, boolean modeChanged,
			boolean deviceChanged) {
	}

	// --------- Window-Events --------------------

	public void windowClosing(WindowEvent e) {
		System.exit(0);
	}

	public void windowActivated(WindowEvent e) {
	}

	public void windowClosed(WindowEvent e) {
	}

	public void windowDeactivated(WindowEvent e) {
	}

	public void windowDeiconified(WindowEvent e) {
	}

	public void windowIconified(WindowEvent e) {
	}

	public void windowOpened(WindowEvent e) {
	}

	@Override
	public void dispose(GLAutoDrawable arg0) {

	}

}