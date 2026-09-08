package assets;

import java.awt.image.BufferedImage;

/** Plays frames from a {@link SpriteSheet} on a fixed delay. */
public final class SpriteAnimation {
	private final SpriteSheet sheet;
	private final int delayMs;
	private int frameIndex;
	private long nextFrameAtMs;
	private boolean playing = true;

	public SpriteAnimation(SpriteSheet sheet, int delayMs) {
		this.sheet = sheet;
		this.delayMs = Math.max(1, delayMs);
		this.nextFrameAtMs = System.currentTimeMillis() + this.delayMs;
	}

	public void play() {
		playing = true;
	}

	public void pause() {
		playing = false;
	}

	public void reset() {
		frameIndex = 0;
		nextFrameAtMs = System.currentTimeMillis() + delayMs;
	}

	public void update() {
		if (!playing || sheet.frameCount() <= 1) {
			return;
		}
		long now = System.currentTimeMillis();
		if (now < nextFrameAtMs) {
			return;
		}
		frameIndex = (frameIndex + 1) % sheet.frameCount();
		nextFrameAtMs = now + delayMs;
	}

	public BufferedImage currentFrame() {
		return sheet.frame(frameIndex);
	}

	public int frameIndex() {
		return frameIndex;
	}
}
