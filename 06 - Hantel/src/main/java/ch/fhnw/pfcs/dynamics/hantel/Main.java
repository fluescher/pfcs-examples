package ch.fhnw.pfcs.dynamics.hantel;

import ch.fhnw.pfcs.util.MainFrame;

public class Main {
	public static void main(String args[]) {
		MainFrame f = new MainFrame(new HantelView());
		f.setVisible(true);
		f.startAnimation();
	}
}
