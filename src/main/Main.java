package main;

import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;

import javax.swing.SwingUtilities;

import game.Screens;
import gui.GameWindow;

public class Main {
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
					.getDefaultConfiguration();
			Rectangle bounds = gc.getBounds();
			Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(gc);
			int width = Math.max(800, bounds.width - insets.left - insets.right);
			int height = Math.max(600, bounds.height - insets.top - insets.bottom);

			GameWindow window = new GameWindow(width, height);
			startLoop(window);
		});
	}

	private static void startLoop(GameWindow window) {
		Thread loop = new Thread(() -> {
			while (true) {
				SwingUtilities.invokeLater(() -> {
					window.getRoot().tick();
					window.repaint();
				});
				try {
					int screen = window.getRoot().getScreen();
					Thread.sleep(screen == Screens.START ? 40 : 16);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					break;
				}
			}
		}, "game-loop");
		loop.setDaemon(false);
		loop.start();
	}
}
