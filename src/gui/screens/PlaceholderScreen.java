package gui.screens;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

import game.GameFonts;
import game.GameRoot;
import game.Screens;
import gui.GamePanel;

/** Simple placeholder until game / settings are implemented. */
public class PlaceholderScreen extends GamePanel {
	private final String headline;

	public PlaceholderScreen(Dimension dimension, String headline) {
		super(dimension);
		this.headline = headline;
		setBackground(new Color(20, 24, 32));
		setOpaque(true);

		JLabel back = new JLabel("Back", SwingConstants.CENTER);
		back.setFont(GameFonts.display(Math.max(36, dimension.height / 12f)));
		back.setForeground(Color.WHITE);
		FontMetrics fm = back.getFontMetrics(back.getFont());
		back.setSize(fm.stringWidth("Back") + 40, fm.getHeight());
		back.setLocation(dimension.width / 2 - back.getWidth() / 2, dimension.height - back.getHeight() - 40);
		back.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		back.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				var parent = getParent();
				while (parent != null) {
					if (parent instanceof GameRoot root) {
						root.setScreen(Screens.START);
						return;
					}
					parent = parent.getParent();
				}
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				back.setForeground(new Color(200, 200, 200));
			}

			@Override
			public void mouseExited(MouseEvent e) {
				back.setForeground(Color.WHITE);
			}
		});
		add(back);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(Color.WHITE);
		g.setFont(GameFonts.display(Math.max(36, getHeight() / 12f)));
		FontMetrics fm = g.getFontMetrics();
		g.drawString(headline, (getWidth() - fm.stringWidth(headline)) / 2, getHeight() / 2);
	}
}
