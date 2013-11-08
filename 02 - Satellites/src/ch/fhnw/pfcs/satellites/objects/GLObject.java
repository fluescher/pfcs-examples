package ch.fhnw.pfcs.satellites.objects;

import javax.media.opengl.GL2;

public abstract class GLObject {
	public abstract void draw(GL2 gl);
	public abstract void showHelp(GL2 gl);
}
