package assets;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;

/**
 * Loads an animated GIF into full composited frames with per-frame delay (ms).
 * Handles ImageMagick-optimized GIFs (partial frames + offsets + disposal).
 */
public final class GifAnimation {
	private final List<BufferedImage> frames;
	private final List<Integer> delaysMs;
	private final boolean loop;

	private GifAnimation(List<BufferedImage> frames, List<Integer> delaysMs, boolean loop) {
		this.frames = List.copyOf(frames);
		this.delaysMs = List.copyOf(delaysMs);
		this.loop = loop;
	}

	public static GifAnimation load(File gifFile) {
		if (gifFile == null || !gifFile.isFile()) {
			System.err.println("GIF not found: " + gifFile);
			return null;
		}
		try (ImageInputStream stream = ImageIO.createImageInputStream(gifFile)) {
			Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("gif");
			if (!readers.hasNext()) {
				System.err.println("No GIF ImageReader available");
				return null;
			}
			ImageReader reader = readers.next();
			try {
				reader.setInput(stream, false, false);
				int count = reader.getNumImages(true);
				if (count <= 0) {
					return null;
				}

				int canvasW = reader.getWidth(0);
				int canvasH = reader.getHeight(0);
				try {
					IIOMetadata streamMeta = reader.getStreamMetadata();
					if (streamMeta != null) {
						IIOMetadataNode root = (IIOMetadataNode) streamMeta
								.getAsTree(streamMeta.getNativeMetadataFormatName());
						IIOMetadataNode screen = findNode(root, "LogicalScreenDescriptor");
						if (screen != null) {
							canvasW = Integer.parseInt(screen.getAttribute("logicalScreenWidth"));
							canvasH = Integer.parseInt(screen.getAttribute("logicalScreenHeight"));
						}
					}
				} catch (Exception ignored) {
				}

				List<BufferedImage> frames = new ArrayList<>(count);
				List<Integer> delays = new ArrayList<>(count);
				boolean loop = true;

				BufferedImage canvas = new BufferedImage(canvasW, canvasH, BufferedImage.TYPE_INT_ARGB);
				BufferedImage previous = null;

				for (int i = 0; i < count; i++) {
					BufferedImage raw = reader.read(i);
					FrameMeta meta = readFrameMeta(reader, i);

					if (meta.disposal == 3) {
						previous = copyImage(canvas);
					}

					Graphics2D g = canvas.createGraphics();
					if (meta.disposal == 2 && i > 0) {
						g.setBackground(new Color(0, 0, 0, 0));
						g.clearRect(meta.left, meta.top, meta.width, meta.height);
					}
					g.drawImage(raw, meta.left, meta.top, null);
					g.dispose();

					frames.add(copyImage(canvas));
					delays.add(meta.delayMs);

					if (meta.disposal == 2) {
						Graphics2D clear = canvas.createGraphics();
						clear.setBackground(new Color(0, 0, 0, 0));
						clear.clearRect(meta.left, meta.top, Math.max(meta.width, raw.getWidth()),
								Math.max(meta.height, raw.getHeight()));
						clear.dispose();
					} else if (meta.disposal == 3 && previous != null) {
						canvas = previous;
						previous = null;
					}
				}

				IIOMetadata streamMeta = reader.getStreamMetadata();
				if (streamMeta != null) {
					loop = readLoops(streamMeta);
				}
				return new GifAnimation(frames, delays, loop);
			} finally {
				reader.dispose();
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public int frameCount() {
		return frames.size();
	}

	public BufferedImage frame(int index) {
		return frames.get(index);
	}

	public int delayMs(int index) {
		return delaysMs.get(index);
	}

	public boolean loops() {
		return loop;
	}

	private static BufferedImage copyImage(BufferedImage src) {
		BufferedImage copy = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = copy.createGraphics();
		g.drawImage(src, 0, 0, null);
		g.dispose();
		return copy;
	}

	private static FrameMeta readFrameMeta(ImageReader reader, int index) {
		FrameMeta meta = new FrameMeta();
		try {
			IIOMetadata imageMeta = reader.getImageMetadata(index);
			String format = imageMeta.getNativeMetadataFormatName();
			IIOMetadataNode root = (IIOMetadataNode) imageMeta.getAsTree(format);

			IIOMetadataNode desc = findNode(root, "ImageDescriptor");
			if (desc != null) {
				meta.left = Integer.parseInt(desc.getAttribute("imageLeftPosition"));
				meta.top = Integer.parseInt(desc.getAttribute("imageTopPosition"));
				meta.width = Integer.parseInt(desc.getAttribute("imageWidth"));
				meta.height = Integer.parseInt(desc.getAttribute("imageHeight"));
			}

			IIOMetadataNode gce = findNode(root, "GraphicControlExtension");
			if (gce != null) {
				int hundredths = Integer.parseInt(gce.getAttribute("delayTime"));
				meta.delayMs = Math.max(20, hundredths * 10);
				String disposal = gce.getAttribute("disposalMethod");
				meta.disposal = switch (disposal) {
				case "doNotDispose" -> 1;
				case "restoreToBackgroundColor" -> 2;
				case "restoreToPrevious" -> 3;
				default -> 0;
				};
			}
		} catch (Exception e) {
			meta.delayMs = 40;
		}
		return meta;
	}

	private static boolean readLoops(IIOMetadata streamMeta) {
		try {
			String format = streamMeta.getNativeMetadataFormatName();
			IIOMetadataNode root = (IIOMetadataNode) streamMeta.getAsTree(format);
			IIOMetadataNode appExt = findNode(root, "ApplicationExtensions");
			if (appExt == null) {
				return true;
			}
			for (int i = 0; i < appExt.getLength(); i++) {
				IIOMetadataNode child = (IIOMetadataNode) appExt.item(i);
				if (!"ApplicationExtension".equals(child.getNodeName())) {
					continue;
				}
				byte[] bytes = (byte[]) child.getUserObject();
				if (bytes != null && bytes.length >= 3) {
					int loops = (bytes[1] & 0xff) | ((bytes[2] & 0xff) << 8);
					return loops == 0;
				}
			}
		} catch (Exception ignored) {
		}
		return true;
	}

	private static IIOMetadataNode findNode(IIOMetadataNode root, String name) {
		if (root.getNodeName().equals(name)) {
			return root;
		}
		for (int i = 0; i < root.getLength(); i++) {
			IIOMetadataNode found = findNode((IIOMetadataNode) root.item(i), name);
			if (found != null) {
				return found;
			}
		}
		return null;
	}

	private static final class FrameMeta {
		int left;
		int top;
		int width;
		int height;
		int delayMs = 40;
		int disposal;
	}
}
