package assets;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Absolute paths to project assets/ (pictures, fonts). Resolved from env,
 * classpath, or working directory — same idea as Monkey Island 2.0 PACKAGEPATH.
 */
public final class ResourceRoot {
	public static final File ROOT = resolve();
	public static final File PICTURES = new File(ROOT, "pictures");
	public static final File FONTS = new File(ROOT, "fonts");
	public static final File START_ANIMATION = new File(PICTURES, "StartAnimation");
	/** Preferred animated start menu: assets/pictures/StartMenu.gif */
	public static final File START_MENU_GIF = new File(PICTURES, "StartMenu.gif");
	public static final File GUYBRUSH = new File(PICTURES, "guybrush");
	/** Classic painted sprites extracted from Monkey Island 2.0. */
	public static final File GUYBRUSH_CLASSIC = new File(PICTURES, "guybrush/classic");

	private ResourceRoot() {
	}

	public static File guybrushSheet(String walkDir) {
		return new File(GUYBRUSH, walkDir + "_sheet.png");
	}

	public static File guybrushIdle(String facing) {
		return new File(GUYBRUSH, "idle_" + facing + ".png");
	}

	/** Shared left/right idle pose (facing right); left uses a horizontal flip. */
	public static File guybrushSideIdle() {
		return new File(GUYBRUSH, "idle.png");
	}

	public static File picture(String relativePath) {
		return new File(PICTURES, relativePath.replace('\\', '/'));
	}

	public static File font(String name) {
		return new File(FONTS, name);
	}

	private static File resolve() {
		String fromEnv = System.getenv("MonkeyIslandAssets");
		if (isRoot(fromEnv)) {
			return new File(fromEnv).getAbsoluteFile();
		}

		File userDir = new File(System.getProperty("user.dir"));
		File fromClasspath = null;
		try {
			URL location = ResourceRoot.class.getProtectionDomain().getCodeSource().getLocation();
			if (location != null) {
				File codeLocation = new File(location.toURI());
				File outputRoot = codeLocation.isFile() ? codeLocation.getParentFile() : codeLocation;
				File projectRoot = outputRoot.getParentFile();
				fromClasspath = new File(projectRoot, "assets");
			}
		} catch (URISyntaxException | SecurityException ignored) {
		}

		File[] candidates = {
				fromClasspath,
				new File(userDir, "assets"),
				userDir,
				new File(userDir, "MonkeyIsland_2.1" + File.separator + "assets"),
		};
		for (File candidate : candidates) {
			if (isRoot(candidate)) {
				return candidate.getAbsoluteFile();
			}
		}
		throw new ExceptionInInitializerError(
				"Could not locate assets/ (expected pictures/ and fonts/). "
						+ "Set env MonkeyIslandAssets or run with MonkeyIsland_2.1 as working directory.");
	}

	private static boolean isRoot(String path) {
		return path != null && isRoot(new File(path));
	}

	private static boolean isRoot(File dir) {
		return dir != null && dir.isDirectory() && new File(dir, "pictures").isDirectory()
				&& new File(dir, "fonts").isDirectory();
	}
}
