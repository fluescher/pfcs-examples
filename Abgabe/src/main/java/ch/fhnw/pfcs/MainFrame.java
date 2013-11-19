package ch.fhnw.pfcs;

import java.awt.Container;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

import ch.fhnw.pfcs.dynamics.box.BoxView;
import ch.fhnw.pfcs.dynamics.cylinder.CylinderView;
import ch.fhnw.pfcs.dynamics.hantel.HantelView;
import ch.fhnw.pfcs.dynamics.rolling.RollingStoneView;
import ch.fhnw.pfcs.dynamics.stoss.StossView;
import ch.fhnw.pfcs.federpendel.FederpendelView;
import ch.fhnw.pfcs.satellites.SatelliteView;
import ch.fhnw.pfcs.util.MainFrame.GLView;

public class MainFrame extends JFrame {
	
	private static final long serialVersionUID = 1L;
	
	private JButton startPendel;
	private JButton startSatellite;
	private JButton startCylinder;
	private JButton startFallingBoxes;
	private JButton startRollingStone;
	private JButton startHantel;
	private JButton startStoss;
	private JLabel title;
	private JLabel name;
	
	public MainFrame() {
		super("pfcs - Starter");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		initButtons();
		initLabels();
		initLayout();
	}
	
	private void initLabels() {
		Font f = new Font("Arial", Font.BOLD, 25);
		
		name = new JLabel("Florian Lüscher");
		title = new JLabel("pfcs - Starter");
		title.setFont(f);
	}
	
	private void initButtons() {
		startPendel = new JButton("Federpendel");
		startSatellite = new JButton("Satelliten");
		startCylinder = new JButton("Cylinder");
		startFallingBoxes = new JButton("Falling Boxes");
		startRollingStone = new JButton("Rolling Stone");
		startHantel = new JButton("Hantel");
		startStoss = new JButton("Stoss");
		
		startPendel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				showView(new FederpendelView());
			}
		});
		
		startSatellite.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				showView(new SatelliteView());
			}
		});
		
		startCylinder.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				showView(new CylinderView());
			}
		});
		
		startFallingBoxes.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				showView(new BoxView());
			}
		});
		
		startRollingStone.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				showView(new RollingStoneView());
			}
		});
		
		startHantel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				showView(new HantelView());
			}
		});
		
		startStoss.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				showView(new StossView());
			}
		});
	}
	
	private void showView(GLView view) {
		new ch.fhnw.pfcs.util.MainFrame(view).setVisible(true);
	}
	
	private void initLayout() {
		Container content = getContentPane();
		content.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		c.weightx = 0.5;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(15, 5, 15, 5);
		
		c.gridx = 0;
		c.gridy = 0;
		content.add(title, c);
		
		c.insets = new Insets(2, 5, 2, 5);
		c.gridy = 1;
		content.add(startPendel, c);
		
		c.gridy = 2;
		content.add(startSatellite, c);
		
		c.gridy = 3;
		content.add(startCylinder, c);
		
		c.gridy = 4;
		content.add(startFallingBoxes, c);
		
		c.gridy = 5;
		content.add(startRollingStone, c);
		
		c.gridy = 6;
		content.add(startHantel, c);
		
		c.gridy = 7;
		content.add(startStoss, c);
		
		c.gridy = 8;
		c.weighty = 1;
		c.anchor = GridBagConstraints.PAGE_END;
		content.add(name, c);
		
		setSize(200,350);
	}
}
