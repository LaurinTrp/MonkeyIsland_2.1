package gui.screens;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;

import assets.GifAnimation;
import assets.ResourceRoot;
import game.Screens;
import gui.GamePanel;
import gui.widgets.AppearingButton;

/**
 * Opening animation + menu. Prefers {@code assets/pictures/StartMenu.gif}.
 * GIF loops forever; menu buttons appear after the first full playthrough.
 */
public class StartScreen extends GamePanel {
	/** Extra slowdown on top of the GIF's own frame delays. */
	private static final double SPEED = 0.45;

	private GifAnimation animation;
	private int frameIndex = 0;
	private long nextFrameAtMs;
	private boolean firstLoopDone;
	private boolean buttonsRevealed;
	private boolean loaded;

	private AppearingButton title;
	private AppearingButton start;
	private AppearingButton settings;
	private AppearingButton exit;
	private final Dimension dimension;

	public StartScreen(Dimension dimension) {
		super(dimension);
		this.dimension = dimension;
		setBackground(Color.BLACK);
		loadGif();
		initButtons();
		setButtonsVisible(false);
	}

	private void loadGif() {
		File gif = ResourceRoot.START_MENU_GIF;
		if (!gif.isFile()) {
			gif = findGifIn(ResourceRoot.START_ANIMATION);
		}
		animation = GifAnimation.load(gif);
		loaded = true;
		if (animation != null && animation.frameCount() > 0) {
			setBackgroundImage(animation.frame(0));
			nextFrameAtMs = System.currentTimeMillis() + frameDelay(0);
		}
	}

	private int frameDelay(int index) {
		return Math.max(40, (int) Math.round(animation.delayMs(index) / SPEED));
	}

	private static File findGifIn(File folder) {
		File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".gif"));
		if (files == null || files.length == 0) {
			return null;
		}
		return files[0];
	}

	private void initButtons() {
		int buttonHeight = Math.max(48, dimension.height / 6);

		title = new AppearingButton(buttonHeight, "MONKEY    ISLAND", Screens.START, true);
		title.centerHorizontal(dimension.width);
		title.setLocation(title.getX(), 10);
		title.setSize(title.getWidth(), title.getHeight() + 20);
		add(title);

		start = new AppearingButton(buttonHeight, "Start", Screens.GAME);
		start.centerHorizontal(dimension.width);
		start.setLocation(start.getX(), dimension.height / 2 - start.getHeight() / 2 - 150);
		add(start);

		settings = new AppearingButton(buttonHeight, "Settings", Screens.SETTINGS);
		settings.centerHorizontal(dimension.width);
		settings.setLocation(settings.getX(), dimension.height / 2 - settings.getHeight() / 2);
		add(settings);

		exit = new AppearingButton(buttonHeight, "Exit", Screens.EXIT);
		exit.centerHorizontal(dimension.width);
		exit.setLocation(exit.getX(), dimension.height / 2 - exit.getHeight() / 2 + 150);
		add(exit);
	}

	private void setButtonsVisible(boolean visible) {
		title.setVisible(visible);
		start.setVisible(visible);
		settings.setVisible(visible);
		exit.setVisible(visible);
	}

	public void resetColors() {
		if (start.getAlpha() == 255) {
			title.setForeground(Color.BLACK);
			start.setForeground(Color.BLACK);
			settings.setForeground(Color.BLACK);
			exit.setForeground(Color.BLACK);
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		if (animation != null && animation.frameCount() > 0) {
			advanceGif();
		}

		super.paintComponent(g);

		if (animation == null || animation.frameCount() == 0) {
			g.setColor(Color.WHITE);
			g.drawString(loaded ? "Missing StartMenu.gif in assets/pictures/" : "Loading...", 20, 40);
			return;
		}

		if (firstLoopDone) {
			if (!buttonsRevealed) {
				setButtonsVisible(true);
				buttonsRevealed = true;
			}
			title.startAnimation();
			start.startAnimation();
			settings.startAnimation();
			exit.startAnimation();
		}
	}

	private void advanceGif() {
		long now = System.currentTimeMillis();
		setBackgroundImage(animation.frame(frameIndex));

		if (now < nextFrameAtMs) {
			return;
		}

		if (frameIndex < animation.frameCount() - 1) {
			frameIndex++;
		} else {
			firstLoopDone = true;
			frameIndex = 0;
		}
		setBackgroundImage(animation.frame(frameIndex));
		nextFrameAtMs = now + frameDelay(frameIndex);
	}
}
