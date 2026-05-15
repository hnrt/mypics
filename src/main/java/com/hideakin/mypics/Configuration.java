package com.hideakin.mypics;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Configuration {

	public static final int DEFAULT_WIDTH = 800;
	public static final int DEFAULT_HEIGHT = 600;
	public static final int DEFAULT_HORIZONTAL_DIVIDER_LOCATION = 250;
	public static final int DEFAULT_LIST_VERTICAL_DIVIDER_LOCATION = 200;

	public static final int NUMBER_OF_RECENT_DIRECTORIES = 10;
	public static final int NUMBER_OF_DESTINATIONS = 10;
	public static final Path DEFAULT_PATH = Paths.get(System.getProperty("user.home"), ".mypics");

	private static Configuration _singleton;

	public static synchronized Configuration getInstance() {
		if (_singleton == null) {
			_singleton = loadFrom(DEFAULT_PATH);
		}
		return _singleton;
	}

	public static synchronized Configuration of(Path path) {
		if (_singleton == null || !_singleton.path().equals(path)) {
			_singleton = loadFrom(path);
		}
		return _singleton;
	}

	private static Configuration loadFrom(Path path) {
		if (Files.exists(path)) {
			try {
				ObjectMapper mapper = new ObjectMapper()
				        .registerModule(new Jdk8Module());
				Configuration configuration = mapper.readValue(path.toFile(), Configuration.class);
				configuration.setPath(path);
				return configuration;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		Configuration configuration = new Configuration();
		configuration.setPath(path);
		configuration.setDirectory(Path.of("").toAbsolutePath());
		return configuration;
	}

	@JsonProperty("width")
	private int _width = DEFAULT_WIDTH;

	@JsonProperty("height")
	private int _height = DEFAULT_HEIGHT;

	@JsonProperty("recent")
	private final Path[] _recent = new Path[NUMBER_OF_RECENT_DIRECTORIES];

	@JsonProperty("destinations")
	private final Path[] _destinations = new Path[NUMBER_OF_DESTINATIONS];

	@JsonProperty("scalingMode")
	private ScalingMode _scalingMode = ScalingMode.FIT_TO_WINDOW;

	@JsonProperty("scale")
	private double _scale = 1.0;

	@JsonProperty("moveFileInterval")
	private long _moveFileInterval = 300L;

	@JsonProperty("horizontalDividerLocation")
	private int _horizontalDividerLocation = DEFAULT_HORIZONTAL_DIVIDER_LOCATION;

	@JsonProperty("listVerticalDividerLocation")
	private int _listVerticalDividerLocation = DEFAULT_LIST_VERTICAL_DIVIDER_LOCATION;

	@JsonIgnore
	private Path _path;

	private Configuration() {
	}

	public Path path() {
		return _path;
	}

	private void setPath(Path value) {
		_path = value;
	}

	public void save() {
		try {
			ObjectMapper mapper = new ObjectMapper()
			        .registerModule(new Jdk8Module())
					.enable(SerializationFeature.INDENT_OUTPUT);
			String json = mapper.writeValueAsString(this);
			if (Files.exists(_path)) {
				Path pathOld = Path.of(_path.toString() + ".old");
				Path pathNew = Path.of(_path.toString() + ".new");
				if (Files.exists(pathOld)) {
					Files.delete(pathOld);
				}
				if (Files.exists(pathNew)) {
					Files.delete(pathNew);
				}
				Files.writeString(pathNew, json);
				Files.move(_path, pathOld);
				Files.move(pathNew, _path);
				Files.delete(pathOld);
			} else {
				Files.writeString(_path, json);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int getWidth() {
		return _width;
	}

	public void setWidth(int value) {
		_width = value;
	}

	public int getHeight() {
		return _height;
	}

	public void setHeight(int value) {
		_height = value;
	}

	@JsonIgnore
	public void setWindowSize(int width, int height) {
		_width = width;
		_height = height;
	}

	@JsonIgnore
	public Path getDirectory() {
		return _recent[0];
	}

	@JsonIgnore
	public void setDirectory(Path value) {
		if (value == null) return;
		Path previous = value;
		for (int i = 0; i < _recent.length; i++) {
			previous = setRecentAt(i, previous);
			if (value.equals(previous)) {
				break;
			}
		}
	}

	private Path setRecentAt(int index, Path value) {
		if (0 <= index && index < _recent.length) {
			Path previous = _recent[index];
			_recent[index] = value;
			return previous;
		} else {
			return null;
		}
	}

	public Path[] getRecent() {
		return _recent;
	}

	public void setRecent(Path[] value) {
		Arrays.fill(_recent, null);
		if (value != null) {
			int n = Math.min(value.length, _recent.length);
			System.arraycopy(value, 0, _recent, 0, n);
		}
		if (_recent[0] == null) {
			_recent[0] = Path.of("").toAbsolutePath();
		}
	}

	public Path[] getDestinations() {
		return _destinations;
	}

	public void setDestinations(Path[] value) {
		Arrays.fill(_destinations, null);
		if (value != null) {
			int n = Math.min(value.length, _destinations.length);
			System.arraycopy(value, 0, _destinations, 0, n);
		}
	}
	
	public Path getDestination(int index) {
		return 0 <= index && index < _destinations.length ? _destinations[index] : null;
	}

	public void setDestination(int index, Path value) {
		if (0 <= index && index < _destinations.length) {
			_destinations[index] = value;
		}
	}

	public ScalingMode getScalingMode() {
		return _scalingMode;
	}

	public void setScalingMode(ScalingMode value) {
		_scalingMode = value;
	}

	public double getScale() {
		return _scale;
	}

	public void setScale(double value) {
		_scale = value;
	}

	public long getMoveFileInterval() {
		return _moveFileInterval;
	}

	public int getHorizontalDividerLocation() {
		return _horizontalDividerLocation;
	}

	public void setHorizontalDividerLocation(int value) {
		_horizontalDividerLocation = value;
	}

	public int getListVerticalDividerLocation() {
		return _listVerticalDividerLocation;
	}

	public void setListVerticalDividerLocation(int value) {
		_listVerticalDividerLocation = value;
	}

}
