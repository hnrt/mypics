package com.hideakin.mypics;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;

public class ImageLoader {

	public static final int ROTATE_90_DEGREES = 6;
	public static final int ROTATE_180_DEGREES = 3;
	public static final int ROTATE_270_DEGREES = 8;

	public static BufferedImage loadCorrectedImage(File file) throws Exception {
        BufferedImage img = ImageIO.read(file);
        if (img == null) {
        	return null;
        }

        Metadata metadata = ImageMetadataReader.readMetadata(file);
        ExifIFD0Directory dir = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);

        int orientation = 1;
        if (dir != null && dir.containsTag(ExifIFD0Directory.TAG_ORIENTATION)) {
            orientation = dir.getInt(ExifIFD0Directory.TAG_ORIENTATION);
        }

        return rotateByOrientation(img, orientation);
    }

    public static BufferedImage rotateByOrientation(BufferedImage img, int orientation) {
        int w = img.getWidth();
        int h = img.getHeight();
        BufferedImage rotated = img;

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
}
