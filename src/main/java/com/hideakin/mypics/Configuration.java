package com.hideakin.mypics;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.hideakin.mypics.gui.ScalingMode;
import com.hideakin.mypics.io.Locker;

public class Configuration {

	public static final int DEFAULT_WIDTH = 800;
	public static final int DEFAULT_HEIGHT = 600;
	public static final int DEFAULT_HORIZONTAL_DIVIDER_LOCATION = 250;
	public static final int DEFAULT_LIST_VERTICAL_DIVIDER_LOCATION = 200;

	public static final int NUMBER_OF_RECENT_DIRECTORIES = 10;
	public static final int NUMBER_OF_DESTINATIONS = 10;
	public static final Path DEFAULT_PATH = Paths.get(System.getProperty("user.home"), ".mypics");
	public static final Path DEFAULT_TRASH_PATH = Paths.get(System.getProperty("user.home"), ".mypics.trash");

	private static Configuration _singleton;

	public static synchronized Configuration getInstance() {
		if (_singleton == null) {
			_singleton = new Configuration(DEFAULT_PATH);
		}
		return _singleton;
	}

	public static synchronized Configuration of(Path path) {
		if (_singleton == null || !_singleton.path().equals(path)) {
			_singleton = new Configuration(path);
		}
		return _singleton;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	private static class ConfigurationDocument {

		public int width = DEFAULT_WIDTH;
		public int height = DEFAULT_HEIGHT;
		public final Path[] recent = new Path[NUMBER_OF_RECENT_DIRECTORIES];
		public final Path[] destinations = new Path[NUMBER_OF_DESTINATIONS];
		public ScalingMode scalingMode = ScalingMode.FIT_TO_WINDOW;
		public double scale = 1.0;
		public long moveFileInterval = 300L;
		public int horizontalDividerLocation = DEFAULT_HORIZONTAL_DIVIDER_LOCATION;
		public int listVerticalDividerLocation = DEFAULT_LIST_VERTICAL_DIVIDER_LOCATION;
		public boolean directoryFilterVisibility = true;
		public Path trashDirectory = DEFAULT_TRASH_PATH;
		public int fileListCellRenderer = 0;

		public ConfigurationDocument() {
			recent[0] = Path.of("").toAbsolutePath();
		}

		@JsonIgnore
		public void debug() {
			Application.debug(3, "ConfigurationDocument: width=%d", width);
			Application.debug(3, "ConfigurationDocument: height=%d", height);
			for (int i = 0; i < recent.length; i++)
				Application.debug(3, "ConfigurationDocument: recent[%d]=%s", i, recent[i]);
			for (int i = 0; i < destinations.length; i++)
				Application.debug(3, "ConfigurationDocument: destinations[%d]=%s", i, destinations[i]);
			Application.debug(3, "ConfigurationDocument: scalingMode=%s", scalingMode.name());
			Application.debug(3, "ConfigurationDocument: scale=%f", scale);
			Application.debug(3, "ConfigurationDocument: moveFileInterval=%d", moveFileInterval);
			Application.debug(3, "ConfigurationDocument: horizontalDividerLocation=%d", horizontalDividerLocation);
			Application.debug(3, "ConfigurationDocument: listVerticalDividerLocation=%d", listVerticalDividerLocation);
			Application.debug(3, "ConfigurationDocument: filterDirectoryVisibility=%s", directoryFilterVisibility ? "true" : "false");
			Application.debug(3, "ConfigurationDocument: trashDirectory=%s", trashDirectory);
			Application.debug(3, "ConfigurationDocument: fileListCellRenderer=%d", fileListCellRenderer);
		}

	}

	private Path _path;
	private Locker _locker;
	private ConfigurationDocument _document;

	private Configuration(Path path) {
		setPath(path);
		_document = loadFrom(path);
	}

	public void debug() {
		_document.debug();
	}

	public Path path() {
		return _path;
	}

	private void setPath(Path value) {
		_path = value;
		_locker = Locker.of(_path);
		_locker.hold();
	}

	private static ConfigurationDocument loadFrom(Path path) {
		if (Files.exists(path)) {
			try {
				ObjectMapper mapper = new ObjectMapper()
				        .registerModule(new Jdk8Module());
				return mapper.readValue(path.toFile(), ConfigurationDocument.class);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return new ConfigurationDocument();
	}

	public void save() {
		if (!_locker.haveLocked()) {
			return;
		}
		try {
			_document.debug();
			ObjectMapper mapper = new ObjectMapper()
			        .registerModule(new Jdk8Module())
					.enable(SerializationFeature.INDENT_OUTPUT);
			String json = mapper.writeValueAsString(_document);
			if (Files.exists(_path)) {
				Path pathOld = Path.of(_path.toString() + ".old");
				Path pathNew = Path.of(_path.toString() + ".new");
				if (Files.exists(pathOld)) {
					Files.delete(pathOld);
					Application.debug(3, "deleted %s", pathOld);
				}
				if (Files.exists(pathNew)) {
					Files.delete(pathNew);
					Application.debug(3, "deleted %s", pathNew);
				}
				Files.writeString(pathNew, json); Application.debug(3, "wrote configuration to %s", pathNew);
				Files.move(_path, pathOld); Application.debug(3, "renamed %s to %s", _path, pathOld);
				Files.move(pathNew, _path); Application.debug(3, "renamed %s to %s", pathNew, _path);
				Files.delete(pathOld); Application.debug(3, "deleted %s", pathOld);
			} else {
				Files.writeString(_path, json); Application.debug(3, "wrote configuration to %s", _path);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		_locker.release();
	}

	public int getWidth() {
		return _document.width;
	}

	public void setWidth(int value) {
		_document.width = value;
	}

	public int getHeight() {
		return _document.height;
	}

	public void setHeight(int value) {
		_document.height = value;
	}

	public void setWindowSize(int width, int height) {
		_document.width = width;
		_document.height = height;
	}

	public Path getDirectory() {
		return _document.recent[0];
	}

	public void setDirectory(Path value) {
		if (value == null) return;
		Path previous = value;
		for (int i = 0; i < _document.recent.length; i++) {
			previous = setRecentAt(i, previous);
			if (value.equals(previous)) {
				break;
			}
		}
	}

	private Path setRecentAt(int index, Path value) {
		if (0 <= index && index < _document.recent.length) {
			Path previous = _document.recent[index];
			_document.recent[index] = value;
			return previous;
		} else {
			return null;
		}
	}

	public Path[] getRecent() {
		return _document.recent;
	}

	public void setRecent(Path[] value) {
		Arrays.fill(_document.recent, null);
		if (value != null) {
			int n = Math.min(value.length, _document.recent.length);
			System.arraycopy(value, 0, _document.recent, 0, n);
		}
		if (_document.recent[0] == null) {
			_document.recent[0] = Path.of("").toAbsolutePath();
		}
	}

	public Path[] getDestinations() {
		return _document.destinations;
	}

	public void setDestinations(Path[] value) {
		Arrays.fill(_document.destinations, null);
		if (value != null) {
			int n = Math.min(value.length, _document.destinations.length);
			System.arraycopy(value, 0, _document.destinations, 0, n);
		}
	}
	
	public Path getDestination(int index) {
		return 0 <= index && index < _document.destinations.length ? _document.destinations[index] : null;
	}

	public void setDestination(int index, Path value) {
		if (0 <= index && index < _document.destinations.length) {
			_document.destinations[index] = value;
		}
	}

	public ScalingMode getScalingMode() {
		return _document.scalingMode;
	}

	public void setScalingMode(ScalingMode value) {
		_document.scalingMode = value;
	}

	public double getScale() {
		return _document.scale;
	}

	public void setScale(double value) {
		_document.scale = value;
	}

	public long getMoveFileInterval() {
		return _document.moveFileInterval;
	}

	public int getHorizontalDividerLocation() {
		return _document.horizontalDividerLocation;
	}

	public void setHorizontalDividerLocation(int value) {
		_document.horizontalDividerLocation = value;
	}

	public int getListVerticalDividerLocation() {
		return _document.listVerticalDividerLocation;
	}

	public void setListVerticalDividerLocation(int value) {
		_document.listVerticalDividerLocation = value;
	}

	public boolean getDirectoryFilterVisibility() {
		return _document.directoryFilterVisibility;
	}

	public void setDirectoryFilterVisibility(boolean value) {
		_document.directoryFilterVisibility = value;
	}

	public Path getTrashDirectory() {
		return _document.trashDirectory;
	}

	public void setTrashDirectory(Path value) {
		_document.trashDirectory = value;
	}

	public int getFileListCellRenderer() {
		return _document.fileListCellRenderer;
	}

	public void setFileListCellRenderer(int value) {
		_document.fileListCellRenderer = value;
	}

}
