package gui;

import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import game.GameRoot;

public class GameWindow extends JFrame {
	private final GameRoot root;
	private final int width;
	private final int height;

	public GameWindow(int width, int height) {
		this.width = width;
		this.height = height;
		this.root = new GameRoot(new Dimension(width, height));
		initialize();
	}

	private void initialize() {
		setTitle("Monkey Island");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		setContentPane(root);
		setSize(width, height);
		setVisible(true);
		SwingUtilities.invokeLater(this::placeOnFocusedScreen);
	}

	public GameRoot getRoot() {
		return root;
	}

	private void placeOnFocusedScreen() {
		GraphicsConfiguration gc = getGraphicsConfiguration();
		if (gc == null) {
			gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
					.getDefaultConfiguration();
		}
		Rectangle bounds = gc.getBounds();
		Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(gc);
		int x = bounds.x + insets.left + (bounds.width - insets.left - insets.right - getWidth()) / 2;
		int y = bounds.y + insets.top + (bounds.height - insets.top - insets.bottom - getHeight()) / 2;
		setLocation(Math.max(bounds.x, x), Math.max(bounds.y, y));
	}
}
