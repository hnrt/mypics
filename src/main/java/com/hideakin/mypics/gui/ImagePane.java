package com.hideakin.mypics.gui;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.Rectangle;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import com.hideakin.mypics.Application;
import com.hideakin.mypics.gui.util.ImageLoader;
import com.hideakin.mypics.util.function.ConsumerList;

import static com.hideakin.mypics.Application.configuration;

public class ImagePane extends JScrollPane {

	private static final long serialVersionUID = -3780897431146089389L;

	private static class Record {

		public final Path path;
		public int status;
		public BufferedImage image;
		public long accessed;

		public Record(Path path) {
			this.path = path;
			this.status = 0;
			this.image = null;
			this.accessed = 0L;
		}

	}

	public static ImagePane create() {
		return new ImagePane();
	}

	private JLabel _imageLabel;
	private Image _originalImage;
	private BufferedImage _processedImage;
	private Path _imagePath;
    private double _scale = 1.0;
    private ConsumerList<ImagePane> _onChanged = new ConsumerList<>();

	private ImagePane() {
		super();
        _imageLabel = new JLabel();
        _imageLabel.setHorizontalAlignment(JLabel.CENTER);
		this.setViewportView(_imageLabel);
        addMouseWheelListener(e -> {
            if (_originalImage == null) return;
            int notches = e.getWheelRotation();
            if (notches < 0) {
                _scale *= 1.1; // zoom in
            } else {
                _scale /= 1.1; // zoom out
            }
            updateImage(ImageLoader.computeSizeByScale(_originalImage, _scale));
        });
	}

	public Path path() {
		return _imagePath;
	}

	public double scale() {
		return _scale;
	}

	public void onChanged(Consumer<ImagePane> callback) {
		_onChanged.add(callback);
	}

	public void clear() {
		_imagePath = null;
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
    	try {
    		Record record = getRecord(path);
    		_processedImage = record.image;
    		if (_processedImage == null) {
    			clear();
    			return;
    		}
    		_imagePath = path;
    		ImageIcon icon = new ImageIcon(_processedImage);
    		_originalImage = icon.getImage();
    		updateImage(computeImageSize());
    		_imageLabel.setText(null);
    	} catch (Exception e) {
    		e.printStackTrace();
    		_imageLabel.setIcon(null);
    	}
    }

    public void redraw() {
    	if (_imagePath == null) return;
		updateImage(computeImageSize());
    }

    public void rotateByOrientation(int orientation) {
    	if (_processedImage == null) return;
    	Record record = getRecord(_imagePath);
    	record.image = ImageLoader.rotateByOrientation(record.image, orientation);
    	_processedImage = record.image;
		ImageIcon icon = new ImageIcon(_processedImage);
		_originalImage = icon.getImage();
		updateImage(computeImageSize());
    }

    private Rectangle computeImageSize() {
		_scale = ImageLoader.computeScale(_originalImage, configuration.getScalingMode(), this);
		return ImageLoader.computeSizeByScale(_originalImage, _scale);
    }

    private void updateImage(Rectangle rect) {
        Image scaled = _originalImage.getScaledInstance(rect.width, rect.height, Image.SCALE_SMOOTH);
        _imageLabel.setIcon(new ImageIcon(scaled));
        _imageLabel.revalidate();
		_onChanged.invoke(this);
    }

    private static final int INITIAL_CAPACITY = 1024;
    private static final int NUMBER_OF_ACTIVE_IMAGES = 256;

    private final Map<Path, Record> _cache = new LinkedHashMap<>(INITIAL_CAPACITY);
    private int _activeImages = 0;
    private long _serialNumber = 0;

	private Record getRecord(Path path) {
		Record record = _cache.get(path);
		if (record == null) {
			record = new Record(path);
			_cache.put(path, record);
		}
		record.accessed = _serialNumber++;
		if (record.status == 0) {
			try {
				record.image = ImageLoader.loadCorrectedImage(path.toFile());
				record.status = record.image != null ? 1 : 2;
				_activeImages += record.status & 1;
				if (_activeImages > NUMBER_OF_ACTIVE_IMAGES) {
					for (Record next : _cache.values()) {
						if (next.image != null && next.accessed + NUMBER_OF_ACTIVE_IMAGES < _serialNumber) {
							_cache.remove(next.path);
							_activeImages--;
							break;
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				record.status = -1;
			}
		}
		return record;
	}

}
