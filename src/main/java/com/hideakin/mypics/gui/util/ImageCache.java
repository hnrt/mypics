package com.hideakin.mypics.gui.util;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class ImageCache {

	private static final int INITIAL_CAPACITY = 1024;
    private static final int NUMBER_OF_ACTIVE_IMAGES = 256;

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

    private final Map<Path, Record> _cache = new LinkedHashMap<>(INITIAL_CAPACITY);
    private int _activeImages = 0;
    private long _serialNumber = 0;

    public ImageCache() {
	}

    public BufferedImage load(Path path) {
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
		return record.image;
    }

    public BufferedImage set(Path path, BufferedImage image) {
		Record record = _cache.get(path);
		if (record == null) {
			record = new Record(path);
			_cache.put(path, record);
		}
		record.accessed = _serialNumber++;
		record.image = image;
    	return image;
    }

}
