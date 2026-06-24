package com.hideakin.mypics.gui.util;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.JComponent;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Iterator;

import static com.hideakin.mypics.Application.configuration;

public class ImageLoader {

	public static final int ROTATE_90_DEGREES = 6;
	public static final int ROTATE_180_DEGREES = 3;
	public static final int ROTATE_270_DEGREES = 8;

	public static BufferedImage loadCorrectedImage(File file) throws Exception {
		BufferedImage img = ImageIO.read(file);
        if (img == null) {
        	return null;
        }
        return loadCorrectedImage(file, img);
	}

	public static BufferedImage loadCorrectedImageBySubsampling(File file, int sourceSubsampling) throws Exception {
		try (ImageInputStream iis = ImageIO.createImageInputStream(file)) {
			Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
			ImageReader reader = readers.next();
			reader.setInput(iis);
			ImageReadParam param = reader.getDefaultReadParam();
			param.setSourceSubsampling(sourceSubsampling, sourceSubsampling, 0, 0);
			BufferedImage img = reader.read(0, param);
	        if (img == null) {
	        	return null;
	        }
	        return loadCorrectedImage(file, img);
		}
	}

	public static BufferedImage loadCorrectedImage(File file, BufferedImage img) throws Exception {
        Metadata metadata = ImageMetadataReader.readMetadata(file);
        ExifIFD0Directory dir = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
        int orientation = 1;
        if (dir != null && dir.containsTag(ExifIFD0Directory.TAG_ORIENTATION)) {
            orientation = dir.getInt(ExifIFD0Directory.TAG_ORIENTATION);
        }
        return rotateByOrientation(img, orientation);
    }

    public static BufferedImage rotateByOrientation(BufferedImage img, int orientation) {
        BufferedImage rotated;
        int w = img.getWidth();
        int h = img.getHeight();
        AffineTransform transform = new AffineTransform();
        switch (orientation) {
            case 6: // 90 degrees CW
                rotated = new BufferedImage(h, w, img.getType());
                transform.translate(h, 0);
                transform.rotate(Math.toRadians(90));
                break;
            case 3: // 180 degrees
                rotated = new BufferedImage(w, h, img.getType());
                transform.translate(w, h);
                transform.rotate(Math.toRadians(180));
                break;
            case 8: // 90 degrees CCW
                rotated = new BufferedImage(h, w, img.getType());
                transform.translate(0, w);
                transform.rotate(Math.toRadians(270));
                break;
            default:
                return img;
        }
        Graphics2D g = rotated.createGraphics();
        g.setTransform(transform);
        g.drawImage(img, 0, 0, null);
        g.dispose();
        return rotated;
    }

    public static double computeScale(Image image, ScalingMode mode, JComponent component) {
    	double scale;
		int ow = image.getWidth(null);
		int oh = image.getHeight(null);
    	if (mode == ScalingMode.FIT_TO_WINDOW) {
    		double scaleW = 1.0, scaleH = 1.0;
    		int pw = component.getWidth();
    		int ph = component.getHeight();
    		if (pw < ow) {
    			scaleW = 0.97 * pw / ow;
    		}
    		if (ph < oh) {
    			scaleH = 0.97 * ph / oh;
    		}
    		scale = Math.min(scaleW, scaleH);
    	} else if (mode == ScalingMode.FIT_TO_WINDOW_WIDTH) {
    		int pw = component.getWidth();
    		if (pw < ow) {
    			scale = 0.97 * pw / ow;
    		} else {
    			scale = 1.0;
    		}
    	} else if (mode == ScalingMode.FIT_TO_WINDOW_HEIGHT) {
    		int ph = component.getHeight();
    		if (ph < oh) {
    			scale = 0.97 * ph / oh;
    		} else {
    			scale = 1.0;
    		}
    	} else if (mode == ScalingMode.RATIO) {
    		scale = configuration.getScale();
    	} else {
    		scale = 1.0;
    	}
    	return scale;
    }

    public static Rectangle computeSizeByScale(Image image, double scale) {
    	return new Rectangle((int)(image.getWidth(null) * scale), (int)(image.getHeight(null) * scale));
    }

}
