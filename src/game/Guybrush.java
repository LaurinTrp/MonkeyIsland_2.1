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
 * Guybrush with front/back/left/right walks. Position is foot contact on the
 * walkable floor from {@link WalkBounds}.
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
	private static final double MOVE_SPEED = 5;
	private static final double DEPTH_SPEED = 2.5;
	/** Sprite is 64×96; keep on-screen size comparable to barrels/stove. */
	private static final double MIN_SCALE = 3;
	private static final double MAX_SCALE = 5;

	private final Map<Facing, SpriteAnimation> walks = new EnumMap<>(Facing.class);
	private final Map<Facing, BufferedImage> idles = new EnumMap<>(Facing.class);
	private final WalkBounds bounds;
	private final int baseWidth;
	private final int baseHeight;

	/** Foot position in screen pixels (bottom-center of the sprite). */
	private double footX;
	private double footY;
	private Facing facing = Facing.FRONT;
	private boolean moving;

	public Guybrush(WalkBounds bounds, double footX, double footY) {
		this.bounds = bounds;
		this.footX = footX;
		this.footY = footY;
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

	public void update(int screenW, int screenH) {
		if (!moving) {
			return;
		}
		// Screen-space speed tracks sprite scale so far/near feel like constant world speed.
		double speedScale = scale(screenW, screenH) / MIN_SCALE;
		double move = MOVE_SPEED * speedScale;
		double depth = DEPTH_SPEED * speedScale;
		double nx = footX;
		double ny = footY;
		switch (facing) {
		case LEFT -> nx -= move;
		case RIGHT -> nx += move;
		case BACK -> ny -= depth;
		case FRONT -> ny += depth;
		}

		if (tryMove(nx, ny, screenW, screenH)) {
			walks.get(facing).update();
			return;
		}
		// Slide along the boundary when blocked on one axis.
		if (tryMove(nx, footY, screenW, screenH) || tryMove(footX, ny, screenW, screenH)) {
			walks.get(facing).update();
		}
	}

	private boolean tryMove(double nx, double ny, int screenW, int screenH) {
		if (bounds.isWalkable(nx, ny, screenW, screenH)) {
			footX = nx;
			footY = ny;
			return true;
		}
		return false;
	}

	private double scale(int screenW, int screenH) {
		// Depth from foot Y only (not local boundary at X), so left/right keeps size.
		double far = bounds.farthestScreenY(screenH);
		double span = Math.max(1.0, screenH - far);
		double t = (footY - far) / span;
		t = Math.max(0.0, Math.min(1.0, t));
		return MIN_SCALE + (MAX_SCALE - MIN_SCALE) * t;
	}

	public void draw(Graphics g, int screenW, int screenH) {
		double scale = scale(screenW, screenH);
		int drawW = Math.max(1, (int) Math.round(baseWidth * scale));
		int drawH = Math.max(1, (int) Math.round(baseHeight * scale));
		int drawX = (int) Math.round(footX - drawW / 2.0);
		int drawY = (int) Math.round(footY - drawH);

		BufferedImage frame = moving ? walks.get(facing).currentFrame() : idles.get(facing);
		if (frame != null) {
			g.drawImage(frame, drawX, drawY, drawW, drawH, null);
		}
	}
}
