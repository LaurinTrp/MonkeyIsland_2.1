package gui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

/** Base panel that can draw a scaled background image. */
public class GamePanel extends JPanel {
	private BufferedImage backgroundImage;
	private BufferedImage foregroundImage;

	public GamePanel(Dimension dimension) {
		setSize(dimension);
		setPreferredSize(dimension);
		setLocation(0, 0);
		setLayout(null);
		setOpaque(true);
	}

	protected void setBackgroundImage(BufferedImage backgroundImage) {
		this.backgroundImage = backgroundImage;
	}

	protected void setForegroundImage(BufferedImage foregroundImage) {
		this.foregroundImage = foregroundImage;
	}

	public BufferedImage getBackgroundImage() {
		return backgroundImage;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		paintBackground(g);
	}

	protected void paintBackground(Graphics g) {
		if (backgroundImage != null) {
			g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), null);
		}
	}

	protected void paintForeground(Graphics g) {
		if (foregroundImage != null) {
			g.drawImage(foregroundImage, 0, 0, getWidth(), getHeight(), null);
		}
	}
}
