package ch.fhnw.pfcs.federpendel;

import java.awt.Frame;

import ch.fhnw.pfcs.util.MainFrame;

public class Main {
	public static void main(String args[]) {
		Frame f = new MainFrame(new FederpendelView());
		f.setVisible(true);
	}
}
