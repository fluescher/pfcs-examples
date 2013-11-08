package ch.fhnw.pfcs.dynamics.cylinder;

import ch.fhnw.pfcs.util.MainFrame;

public class Main {
	public static void main(String args[]) {
		MainFrame f = new MainFrame(new CylinderView());
		f.setVisible(true);
		f.startAnimation();
	}
} 
