package game;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.EnumMap;
import java.util.Map;

import assets.ImageLoader;
import assets.ResourceRoot;
import assets.SpriteAnimation;
import assets.SpriteSheet;

/**
 * Guybrush with front/back/left/right walks (alibi-3D): depth changes scale.
 */
public class Guybrush {
	public enum Facing {
		LEFT("left", "walk_left"),
		RIGHT("right", "walk_right"),
		FRONT("front", "walk_front"),
		BACK("back", "walk_back");

		final String idleKey;
		final String walkDir;

		Facing(String idleKey, String walkDir) {
			this.idleKey = idleKey;
			this.walkDir = walkDir;
		}
	}

	private static final int WALK_FRAME_MS = 100;
	private static final double MOVE_SPEED = 4;
	private static final double DEPTH_SPEED = 0.9;
	private static final double MIN_SCALE = 1;
	private static final double MAX_SCALE = 3;

	private final Map<Facing, SpriteAnimation> walks = new EnumMap<>(Facing.class);
	private final Map<Facing, BufferedImage> idles = new EnumMap<>(Facing.class);

	private double x;
	/** Depth on the ground plane: 0 = far (small), 1 = near (large). */
	private double depth = 0.55;
	private Facing facing = Facing.FRONT;
	private boolean moving;
	private final int baseWidth;
	private final int baseHeight;
	private final double groundY;

	public Guybrush(double startX, double groundY) {
		this.groundY = groundY;
		int refW = 0;
		int refH = 0;
		BufferedImage sideIdle = ImageLoader.keyWhiteAndTeal(ImageLoader.byFile(ResourceRoot.guybrushSideIdle()));
		if (sideIdle == null) {
			sideIdle = ImageLoader.byFile(ResourceRoot.guybrushIdle("right"));
		}
		BufferedImage sideIdleLeft = sideIdle == null ? null : ImageLoader.flipHorizontal(sideIdle);

		for (Facing f : Facing.values()) {
			File folder = new File(ResourceRoot.GUYBRUSH, f.walkDir);
			File sheetFile = ResourceRoot.guybrushSheet(f.walkDir);
			String[] names = folder.list((dir, name) -> name.endsWith(".png"));
			int count = names == null ? 0 : names.length;
			BufferedImage sheetImg = ImageLoader.byFile(sheetFile);
			SpriteSheet sheet = SpriteSheet.horizontalStrip(sheetImg, Math.max(1, count));
			walks.put(f, new SpriteAnimation(sheet, WALK_FRAME_MS));
			BufferedImage idle;
			if (f == Facing.LEFT && sideIdleLeft != null) {
				idle = sideIdleLeft;
			} else if (f == Facing.RIGHT && sideIdle != null) {
				idle = sideIdle;
			} else {
				idle = ImageLoader.byFile(ResourceRoot.guybrushIdle(f.idleKey));
			}
			idles.put(f, idle);
			refW = Math.max(refW, sheet.frameWidth());
			refH = Math.max(refH, sheet.frameHeight());
		}
		baseWidth = refW;
		baseHeight = refH;
		this.x = startX;
	}

	/**
	 * Apply held keys. Safe under key-repeat: keeps the same walk cycle playing
	 * without resetting when the direction does not change.
	 */
	public void setInput(boolean left, boolean right, boolean up, boolean down) {
		Facing next = null;
		if (up) {
			next = Facing.BACK;
		} else if (down) {
			next = Facing.FRONT;
		} else if (left) {
			next = Facing.LEFT;
		} else if (right) {
			next = Facing.RIGHT;
		}

		if (next == null) {
			if (moving) {
				moving = false;
				walks.get(facing).pause();
				walks.get(facing).reset();
			}
			return;
		}

		if (!moving || next != facing) {
			if (moving && next != facing) {
				walks.get(facing).pause();
				walks.get(facing).reset();
			}
			facing = next;
			moving = true;
			walks.get(facing).play();
		} else {
			moving = true;
			walks.get(facing).play();
		}
	}

	public void update(int minX, int maxX, double minDepth, double maxDepth) {
		if (!moving) {
			return;
		}
		switch (facing) {
		case LEFT -> x -= MOVE_SPEED;
		case RIGHT -> x += MOVE_SPEED;
		case BACK -> depth -= DEPTH_SPEED / 100.0;
		case FRONT -> depth += DEPTH_SPEED / 100.0;
		}
		depth = Math.max(minDepth, Math.min(maxDepth, depth));
		double drawW = baseWidth * scale();
		x = Math.max(minX, Math.min(maxX - drawW, x));
		walks.get(facing).update();
	}

	private double scale() {
		return MIN_SCALE + (MAX_SCALE - MIN_SCALE) * depth;
	}

	public void draw(Graphics g) {
		double scale = scale();
		int drawW = Math.max(1, (int) Math.round(baseWidth * scale));
		int drawH = Math.max(1, (int) Math.round(baseHeight * scale));
		double groundAtDepth = groundY - (1.0 - depth) * (groundY * 0.35);
		int drawX = (int) Math.round(x);
		int drawY = (int) Math.round(groundAtDepth - drawH);

		BufferedImage frame = moving ? walks.get(facing).currentFrame() : idles.get(facing);
		if (frame != null) {
			g.drawImage(frame, drawX, drawY, drawW, drawH, null);
		}
	}

	public int getDrawWidth() {
		return (int) Math.round(baseWidth * scale());
	}

	public int getDrawHeight() {
		return (int) Math.round(baseHeight * scale());
	}
}
