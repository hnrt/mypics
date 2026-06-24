package com.hideakin.mypics.gui.util;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.UIManager;

import static com.hideakin.mypics.Application.debug;

public class Thumbnail {

	public static int size = 50;

	public static Icon DEFAULT = UIManager.getIcon("FileView.fileIcon");

	public static Icon of(Path path) {
		return of(path, null);
	}

	public static Icon of(Path path, Map<Path, Icon> cache) {
		Icon icon = cache != null ? cache.get(path) : null;
		if (icon != null) {
			return icon;
		}
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
				icon = new ImageIcon(rgb);
			} else {
				icon = DEFAULT;
			}
		} catch (NoSuchElementException nsee) {
			icon = DEFAULT;
		} catch (Exception e) {
			e.printStackTrace();
			icon = DEFAULT;
		}
		if (cache != null) {
			cache.put(path, icon);
		}
		return icon;
	}

}
