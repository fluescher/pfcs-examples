package ch.fhnw.pfcs.satellites;

import java.awt.Frame;

import ch.fhnw.pfcs.util.MainFrame;

public class Main {
	public static void main(String args[]) {
		Frame f = new MainFrame(new SatelliteView());
		f.setVisible(true);
	}
}
