package gui.screens;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

import assets.ImageLoader;
import assets.ResourceRoot;
import game.GameFonts;
import game.GameRoot;
import game.Guybrush;
import game.Screens;
import game.WalkBounds;
import gui.GamePanel;

/** Playable area: WASD / arrows; movement clamped to the red walk bounds. */
public class GameScreen extends GamePanel {
	private final WalkBounds walkBounds;
	private final Guybrush guybrush;
	private boolean left, right, up, down;

	public GameScreen(Dimension dimension) {
		super(dimension);
		setBackground(Color.BLACK);
		setOpaque(true);
		setFocusable(true);
		setBackgroundImage(ImageLoader.byFile(ResourceRoot.picture("Background_closed.jpg")));
		setForegroundImage(ImageLoader.byFile(ResourceRoot.picture("Foreground.png")));

		walkBounds = new WalkBounds();
		double[] spawn = walkBounds.defaultSpawn(dimension.width, dimension.height);
		guybrush = new Guybrush(walkBounds, spawn[0], spawn[1]);

		JLabel back = new JLabel("Back", SwingConstants.CENTER);
		back.setFont(GameFonts.display(Math.max(28, dimension.height / 16f)));
		back.setForeground(Color.WHITE);
		FontMetrics fm = back.getFontMetrics(back.getFont());
		back.setSize(fm.stringWidth("Back") + 40, fm.getHeight());
		back.setLocation(20, 20);
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
		});
		add(back);

		JLabel hint = new JLabel("WASD / arrows — walk in 4 directions", SwingConstants.CENTER);
		hint.setFont(GameFonts.display(Math.max(22, dimension.height / 20f)));
		hint.setForeground(new Color(255, 255, 255, 200));
		FontMetrics hfm = hint.getFontMetrics(hint.getFont());
		hint.setSize(hfm.stringWidth(hint.getText()) + 20, hfm.getHeight());
		hint.setLocation(dimension.width / 2 - hint.getWidth() / 2, 20);
		add(hint);

		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_A || e.getKeyCode() == KeyEvent.VK_LEFT) {
					left = true;
				} else if (e.getKeyCode() == KeyEvent.VK_D || e.getKeyCode() == KeyEvent.VK_RIGHT) {
					right = true;
				} else if (e.getKeyCode() == KeyEvent.VK_W || e.getKeyCode() == KeyEvent.VK_UP) {
					up = true;
				} else if (e.getKeyCode() == KeyEvent.VK_S || e.getKeyCode() == KeyEvent.VK_DOWN) {
					down = true;
				} else {
					return;
				}
				guybrush.setInput(left, right, up, down);
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_A || e.getKeyCode() == KeyEvent.VK_LEFT) {
					left = false;
				} else if (e.getKeyCode() == KeyEvent.VK_D || e.getKeyCode() == KeyEvent.VK_RIGHT) {
					right = false;
				} else if (e.getKeyCode() == KeyEvent.VK_W || e.getKeyCode() == KeyEvent.VK_UP) {
					up = false;
				} else if (e.getKeyCode() == KeyEvent.VK_S || e.getKeyCode() == KeyEvent.VK_DOWN) {
					down = false;
				} else {
					return;
				}
				guybrush.setInput(left, right, up, down);
			}
		});
	}

	@Override
	public void addNotify() {
		super.addNotify();
		requestFocusInWindow();
	}

	@Override
	protected void paintComponent(Graphics g) {
		paintBackground(g);

		walkBounds.drawDebug(g, getWidth(), getHeight());

		guybrush.update(getWidth(), getHeight());
		guybrush.draw(g, getWidth(), getHeight());

		paintForeground(g);
	}
}
