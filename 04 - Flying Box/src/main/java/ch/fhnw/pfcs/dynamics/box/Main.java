package ch.fhnw.pfcs.dynamics.box;

import ch.fhnw.pfcs.util.MainFrame;

public class Main {
	public static void main(String args[]) {
		MainFrame f = new MainFrame(new BoxView());
		f.setVisible(true);
		f.startAnimation();
	}
}
