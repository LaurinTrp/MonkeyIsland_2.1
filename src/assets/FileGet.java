package assets;

import java.io.File;

public final class FileGet {
	private FileGet() {
	}

	public static File[] getContainingFiles(File folder) {
		File[] files = folder.listFiles();
		return files == null ? new File[0] : files;
	}

	public static File[] getContainingFiles(String folder) {
		return getContainingFiles(new File(folder));
	}

	public static int getFolderCount(File folder) {
		return getContainingFiles(folder).length;
	}

	public static int getFolderCount(String folder) {
		return getContainingFiles(folder).length;
	}
}
