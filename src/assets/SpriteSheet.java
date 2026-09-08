package assets;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * A horizontal (or grid) sprite sheet sliced into equal-sized frames.
 */
public final class SpriteSheet {
	private final BufferedImage sheet;
	private final int frameWidth;
	private final int frameHeight;
	private final int columns;
	private final int rows;
	private final BufferedImage[] frames;

	public SpriteSheet(BufferedImage sheet, int frameWidth, int frameHeight) {
		if (sheet == null) {
			throw new IllegalArgumentException("sheet is null");
		}
		this.sheet = sheet;
		this.frameWidth = frameWidth;
		this.frameHeight = frameHeight;
		this.columns = Math.max(1, sheet.getWidth() / frameWidth);
		this.rows = Math.max(1, sheet.getHeight() / frameHeight);
		this.frames = slice();
	}

	/** Convenience: one row, N equal frames across the width. */
	public static SpriteSheet horizontalStrip(BufferedImage sheet, int frameCount) {
		int fw = sheet.getWidth() / frameCount;
		return new SpriteSheet(sheet, fw, sheet.getHeight());
	}

	public static SpriteSheet fromFile(File file, int frameWidth, int frameHeight) {
		return new SpriteSheet(ImageLoader.byFile(file), frameWidth, frameHeight);
	}

	/** Load numbered PNGs ({@code walk_00.png}, …) as a virtual sheet of frames. */
	public static SpriteSheet fromFrameFiles(File folder) {
		File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".png"));
		if (files == null || files.length == 0) {
			throw new IllegalArgumentException("No frames in " + folder);
		}
		Arrays.sort(files, Comparator.comparing(File::getName));
		List<BufferedImage> images = new ArrayList<>();
		int maxW = 0;
		int maxH = 0;
		for (File file : files) {
			BufferedImage img = ImageLoader.byFile(file);
			if (img == null) {
				continue;
			}
			images.add(img);
			maxW = Math.max(maxW, img.getWidth());
			maxH = Math.max(maxH, img.getHeight());
		}
		BufferedImage sheet = new BufferedImage(maxW * images.size(), maxH, BufferedImage.TYPE_INT_ARGB);
		var g = sheet.createGraphics();
		for (int i = 0; i < images.size(); i++) {
			BufferedImage img = images.get(i);
			int x = i * maxW + (maxW - img.getWidth()) / 2;
			int y = maxH - img.getHeight();
			g.drawImage(img, x, y, null);
		}
		g.dispose();
		return new SpriteSheet(sheet, maxW, maxH);
	}

	private BufferedImage[] slice() {
		BufferedImage[] out = new BufferedImage[columns * rows];
		int i = 0;
		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < columns; col++) {
				out[i++] = sheet.getSubimage(col * frameWidth, row * frameHeight, frameWidth, frameHeight);
			}
		}
		return out;
	}

	public BufferedImage frame(int index) {
		return frames[Math.floorMod(index, frames.length)];
	}

	public int frameCount() {
		return frames.length;
	}

	public int frameWidth() {
		return frameWidth;
	}

	public int frameHeight() {
		return frameHeight;
	}

	public BufferedImage[] frames() {
		return frames.clone();
	}
}
