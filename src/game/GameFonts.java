package game;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.IOException;

import assets.ResourceRoot;

public final class GameFonts {
	private static Font base;

	private GameFonts() {
	}

	public static Font display(float height) {
		ensureLoaded();
		// Match Monkey Island 2.0 Linux sizing for the Blackadder display face.
		float size = height;
		if (System.getProperty("os.name", "").startsWith("Linux") && size > 80) {
			size -= 80;
		}
		if (base == null) {
			return new Font(Font.SERIF, Font.PLAIN, Math.max(12, Math.round(size)));
		}
		return base.deriveFont(Math.max(12f, size));
	}

	private static void ensureLoaded() {
		if (base != null) {
			return;
		}
		try {
			base = Font.createFont(Font.TRUETYPE_FONT, ResourceRoot.font("BITCBLKAD.ttf"));
			GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(base);
		} catch (IOException | FontFormatException e) {
			e.printStackTrace();
		}
	}
}
