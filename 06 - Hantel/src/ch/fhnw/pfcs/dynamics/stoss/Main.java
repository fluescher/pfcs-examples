package ch.fhnw.pfcs.dynamics.stoss;

import ch.fhnw.pfcs.util.MainFrame;

public class Main {
	public static void main(String args[]) {
		MainFrame f = new MainFrame(new StossView());
		f.setVisible(true);
		f.startAnimation();
	}
}
