package com.hideakin.mypics.gui.util;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.SwingWorker;
import javax.swing.UIManager;

import static com.hideakin.mypics.Application.configuration;
import static com.hideakin.mypics.Application.debug;

public class Thumbnail {

	public static final int DEFAULT_SIZE = 50;
	public static final int SMALL_SIZE = 25;
	public static final int BIG_SIZE = 100;

	public static final Icon DEFAULT_ICON = UIManager.getIcon("FileView.fileIcon");
	public static final Icon BIG_DEFAULT_ICON;

	public static final Icon DEFAULT_BLACK;
	public static final Icon SMALL_BLACK;
	public static final Icon BIG_BLACK;

	public static boolean clipping = configuration.getThumbnailClipping();

	static {
		DEFAULT_BLACK = new ImageIcon(new BufferedImage(DEFAULT_SIZE, DEFAULT_SIZE, BufferedImage.TYPE_INT_RGB));
		SMALL_BLACK = new ImageIcon(new BufferedImage(SMALL_SIZE, SMALL_SIZE, BufferedImage.TYPE_INT_RGB));
		BIG_BLACK = new ImageIcon(new BufferedImage(BIG_SIZE, BIG_SIZE, BufferedImage.TYPE_INT_RGB));
		BIG_DEFAULT_ICON = resizeImageIcon(iconToImageIcon(DEFAULT_ICON), BIG_SIZE);
	}

	public static Icon of(Path path) {
		return of(path, DEFAULT_SIZE);
	}

	public static Icon of(Path path, Map<Path, Icon> cache) {
		return of(path, DEFAULT_SIZE, cache);
	}

	public static Icon of(Path path, Map<Path, Icon> cache, Consumer<Icon> callback) {
		return of(path, DEFAULT_SIZE, cache, callback);
	}

	public static Icon of(Path path, int size) {
		return load(path, size);
	}

	public static Icon of(Path path, int size, Consumer<Icon> callback) {
		SwingWorker<Icon, Void> worker = new SwingWorker<>() {

			@Override
			protected Icon doInBackground() throws Exception {
				return load(path, size);
			}

			@Override
			protected void done() {
				try {
					callback.accept(get());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		};
		worker.execute();
		return black(size);
	}

	public static Icon of(Path path, int size, Map<Path, Icon> cache) {
		Icon icon = cache.get(path);
		if (icon != null) {
			return icon;
		}
		icon = load(path, size);
		cache.put(path, icon);
		return icon;
	}

	public static Icon of(Path path, int size, Map<Path, Icon> cache, Consumer<Icon> callback) {
		Icon icon = cache.get(path);
		if (icon != null) {
			return icon;
		}
		SwingWorker<Icon, Void> worker = new SwingWorker<>() {

			@Override
			protected Icon doInBackground() throws Exception {
				return load(path, size);
			}

			@Override
			protected void done() {
				try {
					Icon icon = get();
		   			cache.put(path, icon);
					callback.accept(icon);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		};
		worker.execute();
		return black(size);
	}

	public static Icon load(Path path, int size) {
		try {
			long fileSize = Files.size(path);
			int subsampling = fileSize <= 65536 ? 1 : fileSize <= 262144 ? 2 : fileSize <= 1048576 ? 4 : fileSize <= 4194304 ? 8 : 16;
			BufferedImage original = subsampling > 1
					? ImageLoader.loadCorrectedImageBySubsampling(path.toFile(), subsampling)
					: ImageLoader.loadCorrectedImage(path.toFile());
			if (original != null) {
				double ow = original.getWidth();
				double oh = original.getHeight();
				double sw = (double)size;
				double sh = (double)size;
				if (clipping) {
					if (ow > oh) {
						sw = sh * ow / oh;
					} else if (ow < oh){
						sh = sw * oh / ow;
					}
					int x1 = ((int)sw - size) / 2;
					int y1 = ((int)sh - size) / 2;
					int x2 = x1 + size;
					int y2 = y1 + size;
					debug(3, "createThumbnail: %d %dx%d %s", subsampling, (int)sw, (int)sh, path);
					Image scaled = original.getScaledInstance((int)sw, (int)sh, Image.SCALE_SMOOTH);
					BufferedImage rgb = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
					Graphics2D g = rgb.createGraphics();
					g.drawImage(scaled, 0, 0, size, size, x1, y1, x2, y2, null);
					g.dispose();
					return new ImageIcon(rgb);
				} else {
					if (ow > oh) {
						sh = sw * oh / ow;
					} else if (ow < oh){
						sw = sh * ow / oh;
					}
					debug(3, "createThumbnail: %d %dx%d %s", subsampling, (int)sw, (int)sh, path);
					Image scaled = original.getScaledInstance((int)sw, (int)sh, Image.SCALE_SMOOTH);
					BufferedImage rgb = new BufferedImage((int)sw, (int)sh, BufferedImage.TYPE_INT_RGB);
					Graphics2D g = rgb.createGraphics();
					g.drawImage(scaled, 0, 0, null);
					g.dispose();
					return new ImageIcon(rgb);
				}
			}
		} catch (NoSuchElementException nsee) {
			//NOP
		} catch (Exception e) {
			e.printStackTrace();
		}
		return DEFAULT_ICON;
	}

	public static Icon black(int size) {
		return size == DEFAULT_SIZE ? DEFAULT_BLACK : size == BIG_SIZE ? BIG_BLACK : size == SMALL_SIZE ? SMALL_BLACK : DEFAULT_BLACK;
	}

	public static ImageIcon iconToImageIcon(Icon icon) {
		int w = icon.getIconWidth();
		int h = icon.getIconHeight();
		BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = bi.createGraphics();
		icon.paintIcon(null, g2, 0, 0);
		g2.dispose();
		return new ImageIcon(bi);
	}

	public static ImageIcon resizeImageIcon(ImageIcon icon, int size) {
		int w = size;
		int h = size;
		int ow = icon.getIconWidth();
		int oh = icon.getIconHeight();
		if (ow > oh) {
			h = (int)(1.0 * w * oh / ow);
		} else if (ow < oh){
			w = (int)(1.0 * h * ow / oh);
		}
		return resizeImageIcon(icon, w, h);
	}

	public static ImageIcon resizeImageIcon(ImageIcon icon, int width, int height) {
		Image img = icon.getImage();
		BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = resized.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.drawImage(img, 0, 0, width, height, null);
		g.dispose();
		return new ImageIcon(resized);
	}

	public static ImageIcon centerIconInSquare(Icon icon, int squareSize) {
		int w = icon.getIconWidth();
		int h = icon.getIconHeight();
		BufferedImage canvas = new BufferedImage(squareSize, squareSize, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = canvas.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		int x = (squareSize - w) / 2;
		int y = (squareSize - h) / 2;
		icon.paintIcon(null, g, x, y);
		g.dispose();
		return new ImageIcon(canvas);
	}

}
