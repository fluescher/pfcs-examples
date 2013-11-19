package ch.fhnw.pfcs;

import javax.swing.UIManager;


public class Main {
	public static void main(String args[]) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception ex) {
			System.err.println("Could not load system look and feel");
		}
		
		MainFrame f = new MainFrame();
		f.setLocationRelativeTo(null);
		f.setVisible(true);
	}
}
