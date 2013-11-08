package ch.fhnw.pfcs.util;

import java.awt.BorderLayout;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.media.opengl.awt.GLCanvas;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;


public class MainFrame extends JFrame implements KeyListener {
	
	private static final long serialVersionUID = 6005431145072069672L;

	private GLView view;
	private DynamicGLCanvas glCanvas;
	private boolean isFullScreen = false;
	
	public MainFrame(GLView view) {
		super();
		this.view = view;
		initCanvas();
		initView(view);
		initFrame();
		initGL();
		setHelpMenu();
		
		glCanvas.getCanvas().addKeyListener(this);
		addWindowListener(new ClosingListener(this));
	}

	private void initView(GLView view) {
		this.view.setGLCanvas(glCanvas.getCanvas());
		final JPanel params = this.view.getParameterPanel();
		
		if(params != null) {
			add(params, BorderLayout.PAGE_START);
		}
	}
	
	private void initFrame() {
		this.setTitle(view.getName());
		this.setSize(800, 800);
		this.setLocationRelativeTo(null);
	}

	private void initCanvas() {
		GLCanvas canvas = new GLCanvas();
		add(canvas);
		glCanvas = new DynamicGLCanvas(canvas, view.getInitialDistance(), view.getYOffset());
	}
	
	private void initGL() {
		startAnimation();
	}
	
	private void setHelpMenu() {
		JMenuBar bar = new JMenuBar();
		this.setJMenuBar(bar);
		JMenu helpMenu = new JMenu("Help");
		bar.add(helpMenu);
		JMenuItem showKeys = new JMenuItem("Show keys");
		showKeys.addActionListener(showHelp);
		helpMenu.add(showKeys);
	}
	
	public void startAnimation() {
		this.view.startAnimation();
	}

	public void stopAnimation() {
		this.view.stopAnimation();
	}
	
	public static interface GLView {
		public void setGLCanvas(GLCanvas canvas);
		public void startAnimation();
		public void stopAnimation();
		public String getName();
		public double getInitialDistance();
		public String getHelpText();
		public JPanel getParameterPanel();
		public double getYOffset();
	}
	
	private final ActionListener showHelp = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			JOptionPane.showMessageDialog(MainFrame.this, view.getHelpText(), "Key shortcuts", JOptionPane.INFORMATION_MESSAGE);
		}
	};

	@Override
	public void keyPressed(KeyEvent keyEvent) {
		if(keyEvent.getKeyCode() == KeyEvent.VK_F1) {
			showHelp.actionPerformed(null);
		} else if(keyEvent.getKeyCode() == KeyEvent.VK_F11) {
			toggleFullScreen();
		}
	}

	private void toggleFullScreen() {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice device =ge.getDefaultScreenDevice();
	    if(!device.isFullScreenSupported()) return;
	    
		if(isFullScreen) {
			device.setFullScreenWindow(null);
		} else {
			device.setFullScreenWindow(this);
		}
		isFullScreen = !isFullScreen;
	}
	
	@Override
	public void keyReleased(KeyEvent arg0) {
		
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		
	}
	
	private static final class ClosingListener implements WindowListener {

		private final MainFrame frame;
		
		public ClosingListener(MainFrame frame) {
			this.frame = frame;
		}

		@Override
		public void windowActivated(WindowEvent e) {

		}

		@Override
		public void windowClosed(WindowEvent e) {
			frame.stopAnimation();
		}

		@Override
		public void windowClosing(WindowEvent e) {
			e.getWindow().setVisible(false);
			e.getWindow().dispose();
		}

		@Override
		public void windowDeactivated(WindowEvent e) {

		}

		@Override
		public void windowDeiconified(WindowEvent e) {

		}

		@Override
		public void windowIconified(WindowEvent e) {

		}

		@Override
		public void windowOpened(WindowEvent e) {

		}

	}
}
