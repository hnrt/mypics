package com.hideakin.mypics.gui;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.Rectangle;
import java.nio.file.Path;
import java.util.function.Consumer;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import com.hideakin.mypics.Application;
import com.hideakin.mypics.gui.util.ImageCache;
import com.hideakin.mypics.gui.util.ImageLoader;
import com.hideakin.mypics.util.function.ConsumerList;

import static com.hideakin.mypics.Application.configuration;

public class ImagePane extends JScrollPane {

	private static final long serialVersionUID = -3780897431146089389L;

	public static ImagePane create() {
		return new ImagePane();
	}

	private JLabel _imageLabel;
	private BufferedImage _image;
	private Path _path;
    private double _scale = 1.0;
    private ConsumerList<ImagePane> _onChanged = new ConsumerList<>();
    private ImageCache _cache = new ImageCache();

	private ImagePane() {
		super();
        _imageLabel = new JLabel();
        _imageLabel.setHorizontalAlignment(JLabel.CENTER);
		this.setViewportView(_imageLabel);
        addMouseWheelListener(e -> {
            if (_image == null) return;
            int notches = e.getWheelRotation();
            if (notches < 0) {
                _scale *= 1.1; // zoom in
            } else {
                _scale /= 1.1; // zoom out
            }
            updateImage(ImageLoader.computeSizeByScale(_image, _scale));
        });
	}

	public Path path() {
		return _path;
	}

	public double scale() {
		return _scale;
	}

	public void onChanged(Consumer<ImagePane> callback) {
		_onChanged.add(callback);
	}

	public void clear() {
		_path = null;
		_scale = 1.0;
		_imageLabel.setIcon(null);
		_onChanged.invoke(this);
    }

    public void loadFrom(Path path) {
    	Application.debug(3, "ImagePane::loadFrom(%s)", path);
    	if (path == null) {
    		clear();
    		return;
    	}
    	_image = _cache.load(path);
    	if (_image != null) {
        	_path = path;
        	updateImage(computeImageSize());
        	_imageLabel.setText(null);
    	} else {
    		clear();
    	}
    }

    public void redraw() {
    	if (_path == null) return;
		updateImage(computeImageSize());
    }

    public void rotateByOrientation(int orientation) {
    	if (_image == null) return;
    	_image = _cache.set(_path, ImageLoader.rotateByOrientation(_image, orientation));
		updateImage(computeImageSize());
    }

    private Rectangle computeImageSize() {
		_scale = ImageLoader.computeScale(_image, configuration.getScalingMode(), this);
		return ImageLoader.computeSizeByScale(_image, _scale);
    }

    private void updateImage(Rectangle rect) {
        Image scaled = _image.getScaledInstance(rect.width, rect.height, Image.SCALE_SMOOTH);
        _imageLabel.setIcon(new ImageIcon(scaled));
        _imageLabel.revalidate();
		_onChanged.invoke(this);
    }

}
