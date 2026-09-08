package gui.widgets;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.FontMetrics;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

import game.GameFonts;
import input.MenuMouseHandler;

public class AppearingButton extends JLabel {
	private int counter = 0;
	private int alpha = 0;
	private final int refreshRate = 1;
	private final int screenId;
	private final boolean decorative;
	private boolean listenerAttached;

	public AppearingButton(int height, String text, int screenId) {
		this(height, text, screenId, false);
	}

	public AppearingButton(int height, String text, int screenId, boolean decorative) {
		this.screenId = screenId;
		this.decorative = decorative;

		setBackground(new Color(0, 0, 0, 0));
		setForeground(new Color(255, 255, 255, 0));
		setText(text);
		setFont(GameFonts.display(height));
		FontMetrics fm = getFontMetrics(getFont());
		setSize(fm.stringWidth(text) + 50, height);
		setHorizontalAlignment(SwingConstants.CENTER);
		setBorder(null);
		setFocusable(false);
		setOpaque(false);
	}

	public void centerHorizontal(int screenWidth) {
		setLocation(screenWidth / 2 - getWidth() / 2, getY());
	}

	public void startAnimation() {
		if (counter == refreshRate && alpha <= 252) {
			alpha += 3;
			setForeground(new Color(0, 0, 0, alpha));
			counter %= refreshRate;
		}
		if (alpha >= 255 && !listenerAttached && !decorative) {
			alpha = 255;
			addMouseListener(new MenuMouseHandler(screenId, this));
			listenerAttached = true;
		}
		counter++;
	}

	public void setHovered(boolean underline) {
		if (decorative) {
			return;
		}
		if (underline) {
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			setForeground(new Color(0, 0, 0, 150));
		} else {
			setCursor(Cursor.getDefaultCursor());
			setForeground(new Color(0, 0, 0));
		}
	}

	public int getAlpha() {
		return alpha;
	}
}
