package input;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import game.GameRoot;
import gui.widgets.AppearingButton;

/** Handles start-menu AppearingButton clicks and hover. */
public class MenuMouseHandler extends MouseAdapter {
	private final int screenId;
	private final AppearingButton button;

	public MenuMouseHandler(int screenId, AppearingButton button) {
		this.screenId = screenId;
		this.button = button;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getSource() != button) {
			return;
		}
		GameRoot root = findRoot(button);
		if (root != null) {
			root.setScreen(screenId);
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		button.setHovered(true);
	}

	@Override
	public void mouseExited(MouseEvent e) {
		button.setHovered(false);
	}

	private static GameRoot findRoot(AppearingButton button) {
		var parent = button.getParent();
		while (parent != null) {
			if (parent instanceof GameRoot root) {
				return root;
			}
			parent = parent.getParent();
		}
		return null;
	}
}
