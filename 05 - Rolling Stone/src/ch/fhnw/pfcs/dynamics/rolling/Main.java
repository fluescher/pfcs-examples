package ch.fhnw.pfcs.dynamics.rolling;

import ch.fhnw.pfcs.util.MainFrame;

public class Main {
	public static void main(String args[]) {
		MainFrame f = new MainFrame(new RollingStoneView());
		f.setVisible(true);
		f.startAnimation();
	}
} 
