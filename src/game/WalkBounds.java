package game;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import assets.ImageLoader;
import assets.ResourceRoot;

/**
 * Walkable floor from {@code Background_bounding.jpg}. Bright red (#FE0000-style)
 * marks the back/side edge; feet may stand on or below that line.
 */
public final class WalkBounds {
	private static final int MASK_WIDTH = 980;

	private final int maskWidth;
	private final int maskHeight;
	/** Per mask column: back edge of the red stroke (smaller Y = farther into the room). */
	private final int[] boundaryY;
	private final boolean[] hasRed;
	private final int minRedX;
	private final int maxRedX;
	/** Smallest boundary Y in mask space (farthest into the room). Used for depth scale. */
	private int farthestBoundaryY;

	public WalkBounds() {
		BufferedImage full = ImageLoader.byFile(ResourceRoot.picture("Background_bounding.jpg"));
		if (full == null) {
			throw new IllegalStateException("Missing Background_bounding.jpg");
		}
		maskWidth = MASK_WIDTH;
		maskHeight = Math.max(1, full.getHeight() * MASK_WIDTH / full.getWidth());
		BufferedImage img = new BufferedImage(maskWidth, maskHeight, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = img.createGraphics();
		g.drawImage(full, 0, 0, maskWidth, maskHeight, null);
		g.dispose();

		boundaryY = new int[maskWidth];
		hasRed = new boolean[maskWidth];
		int first = maskWidth;
		int last = -1;

		for (int x = 0; x < maskWidth; x++) {
			int minY = Integer.MAX_VALUE;
			for (int y = 0; y < maskHeight; y++) {
				if (isMarkerRed(img.getRGB(x, y))) {
					if (y < minY) {
						minY = y;
					}
				}
			}
			if (minY != Integer.MAX_VALUE) {
				boundaryY[x] = minY;
				hasRed[x] = true;
				if (x < first) {
					first = x;
				}
				if (x > last) {
					last = x;
				}
			}
		}

		minRedX = first < maskWidth ? first : 0;
		maxRedX = last >= 0 ? last : maskWidth - 1;
		fillGaps();
		int far = maskHeight - 1;
		for (int x = minRedX; x <= maxRedX; x++) {
			if (boundaryY[x] < far) {
				far = boundaryY[x];
			}
		}
		farthestBoundaryY = far;
	}

	/** Pure marker red from the paint stroke — not warm wood / fire. */
	private static boolean isMarkerRed(int argb) {
		int r = (argb >> 16) & 0xFF;
		int g = (argb >> 8) & 0xFF;
		int b = argb & 0xFF;
		return r >= 220 && g <= 60 && b <= 60 && (r - g) >= 150 && (r - b) >= 150;
	}

	private void fillGaps() {
		for (int x = 0; x < maskWidth; x++) {
			if (hasRed[x]) {
				continue;
			}
			int left = -1;
			int right = -1;
			for (int i = x; i >= 0; i--) {
				if (hasRed[i]) {
					left = i;
					break;
				}
			}
			for (int i = x; i < maskWidth; i++) {
				if (hasRed[i]) {
					right = i;
					break;
				}
			}
			if (left >= 0 && right >= 0 && right != left) {
				double t = (double) (x - left) / (right - left);
				boundaryY[x] = (int) Math.round(boundaryY[left] * (1.0 - t) + boundaryY[right] * t);
			} else if (left >= 0) {
				boundaryY[x] = boundaryY[left];
			} else if (right >= 0) {
				boundaryY[x] = boundaryY[right];
			} else {
				boundaryY[x] = maskHeight - 1;
			}
		}
	}

	public double boundaryScreenY(double screenX, int screenW, int screenH) {
		int sx = toMaskX(screenX, screenW);
		return boundaryY[sx] * (double) screenH / maskHeight;
	}

	/**
	 * Global far edge of the floor (smallest boundary Y), independent of X — so depth
	 * scale does not jump when walking sideways past the stove bump.
	 */
	public double farthestScreenY(int screenH) {
		return farthestBoundaryY * (double) screenH / maskHeight;
	}

	public boolean isWalkable(double footScreenX, double footScreenY, int screenW, int screenH) {
		if (screenW <= 0 || screenH <= 0) {
			return false;
		}
		if (footScreenY < 0 || footScreenY > screenH || footScreenX < 0 || footScreenX > screenW) {
			return false;
		}
		int sx = toMaskX(footScreenX, screenW);
		if (sx < minRedX || sx > maxRedX) {
			return false;
		}
		double by = boundaryY[sx] * (double) screenH / maskHeight;
		return footScreenY >= by;
	}

	public double[] defaultSpawn(int screenW, int screenH) {
		double x = screenW * 0.5;
		double top = boundaryScreenY(x, screenW, screenH);
		double y = top + (screenH - top) * 0.55;
		if (isWalkable(x, y, screenW, screenH)) {
			return new double[] { x, y };
		}
		for (double ty = screenH * 0.98; ty >= screenH * 0.4; ty -= 3) {
			for (double tx = screenW * 0.15; tx <= screenW * 0.85; tx += 6) {
				if (isWalkable(tx, ty, screenW, screenH)) {
					return new double[] { tx, ty };
				}
			}
		}
		return new double[] { screenW * 0.5, screenH * 0.9 };
	}

	/** Debug: green walkable fill + red boundary polyline. */
	public void drawDebug(Graphics g, int screenW, int screenH) {
		Graphics2D g2 = (Graphics2D) g.create();
		try {
			g2.setColor(new Color(40, 220, 80, 55));
			for (int x = 0; x < screenW; x++) {
				int sx = toMaskX(x + 0.5, screenW);
				if (sx < minRedX || sx > maxRedX) {
					continue;
				}
				int by = (int) Math.round(boundaryY[sx] * (double) screenH / maskHeight);
				if (by < screenH) {
					g2.drawLine(x, by, x, screenH - 1);
				}
			}
			g2.setColor(new Color(255, 30, 30, 220));
			int prevX = -1;
			int prevY = -1;
			for (int x = 0; x < screenW; x++) {
				int sx = toMaskX(x + 0.5, screenW);
				if (sx < minRedX || sx > maxRedX) {
					prevX = -1;
					continue;
				}
				int by = (int) Math.round(boundaryY[sx] * (double) screenH / maskHeight);
				if (prevX >= 0) {
					g2.drawLine(prevX, prevY, x, by);
				}
				prevX = x;
				prevY = by;
			}
		} finally {
			g2.dispose();
		}
	}

	private int toMaskX(double screenX, int screenW) {
		int sx = (int) Math.floor(screenX * maskWidth / screenW);
		return Math.max(0, Math.min(maskWidth - 1, sx));
	}
}
