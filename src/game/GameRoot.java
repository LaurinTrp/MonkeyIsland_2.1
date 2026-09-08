package game;

import java.awt.Dimension;

import javax.swing.JPanel;

import gui.screens.GameScreen;
import gui.screens.PlaceholderScreen;
import gui.screens.StartScreen;

/**
 * Root controller that swaps screens (start / game / settings), like Label in 2.0.
 */
public class GameRoot extends JPanel {
	private final Dimension dimension;
	private final StartScreen startScreen;
	private GameScreen gameScreen;
	private PlaceholderScreen settingsScreen;
	private int screen = Screens.START;
	private int previousScreen = -1;

	public GameRoot(Dimension dimension) {
		this.dimension = dimension;
		setLayout(null);
		setSize(dimension);
		setPreferredSize(dimension);
		setOpaque(true);

		startScreen = new StartScreen(dimension);
		showScreen(Screens.START);
	}

	public void setScreen(int screen) {
		this.screen = screen;
	}

	public int getScreen() {
		return screen;
	}

	/** Called from the main loop; applies pending screen changes. */
	public void tick() {
		if (previousScreen != screen) {
			showScreen(screen);
			previousScreen = screen;
		}
	}

	private GameScreen gameScreen() {
		if (gameScreen == null) {
			gameScreen = new GameScreen(dimension);
		}
		return gameScreen;
	}

	private PlaceholderScreen settingsScreen() {
		if (settingsScreen == null) {
			settingsScreen = new PlaceholderScreen(dimension, "Settings");
		}
		return settingsScreen;
	}

	private void showScreen(int screen) {
		removeAll();
		switch (screen) {
		case Screens.START -> add(startScreen);
		case Screens.GAME -> {
			startScreen.resetColors();
			GameScreen game = gameScreen();
			add(game);
			game.requestFocusInWindow();
		}
		case Screens.SETTINGS -> {
			startScreen.resetColors();
			add(settingsScreen());
		}
		case Screens.EXIT -> System.exit(0);
		default -> throw new IllegalArgumentException("Unexpected screen: " + screen);
		}
		revalidate();
		repaint();
	}
}
