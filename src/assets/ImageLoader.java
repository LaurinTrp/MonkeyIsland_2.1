package assets;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public final class ImageLoader {
	private ImageLoader() {
	}

	/** Load from assets/pictures/ + relative path (e.g. {@code StartAnimation/foo.png}). */
	public static BufferedImage byName(String relativePath) {
		return byFile(ResourceRoot.picture(relativePath));
	}

	public static BufferedImage byFile(File file) {
		try {
			if (file != null && file.isFile()) {
				return ImageIO.read(file);
			}
			System.err.println("Image not found: " + (file == null ? "null" : file.getAbsolutePath()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static BufferedImage byPath(String absolutePath) {
		return byFile(new File(absolutePath));
	}

	public static BufferedImage resize(BufferedImage image, int width, int height) {
		if (image == null) {
			return null;
		}
		Image tmp = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
		BufferedImage dimg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = dimg.createGraphics();
		g2d.drawImage(tmp, 0, 0, null);
		g2d.dispose();
		return dimg;
	}

	public static BufferedImage flipHorizontal(BufferedImage image) {
		if (image == null) {
			return null;
		}
		BufferedImage out = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = out.createGraphics();
		g2d.drawImage(image, image.getWidth(), 0, -image.getWidth(), image.getHeight(), null);
		g2d.dispose();
		return out;
	}

	/** Make pure white / exact teal chroma (#008080) transparent. */
	public static BufferedImage keyWhiteAndTeal(BufferedImage image) {
		if (image == null) {
			return null;
		}
		int w = image.getWidth();
		int h = image.getHeight();
		BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				int argb = image.getRGB(x, y);
				int a = (argb >>> 24) & 0xFF;
				int r = (argb >>> 16) & 0xFF;
				int g = (argb >>> 8) & 0xFF;
				int b = argb & 0xFF;
				if (a == 0 || (r >= 250 && g >= 250 && b >= 250)
						|| (r <= 8 && g >= 110 && g <= 140 && b >= 110 && b <= 140)) {
					out.setRGB(x, y, 0);
				} else {
					out.setRGB(x, y, argb);
				}
			}
		}
		return out;
	}
}
