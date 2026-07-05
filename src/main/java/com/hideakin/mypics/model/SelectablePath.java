package com.hideakin.mypics.model;

import java.nio.file.Files;
import java.nio.file.Path;

public class SelectablePath {

	public static final int UNKNOWN = 0;
	public static final int REGULAR_FILE = 1;
	public static final int DIRECTORY = 2;
	public static final int ROOT = 3;
	public static final int DETECT = -1;

	public static SelectablePath ofRoot() {
		return new SelectablePath(null, ROOT);
	}

	public static SelectablePath ofRegularFile(Path path, boolean selected) {
		return new SelectablePath(path, REGULAR_FILE, selected);
	}

	public static SelectablePath ofDirectory(Path path, boolean selected) {
		return new SelectablePath(path, DIRECTORY, selected);
	}

	protected final Path _path;
	protected int _type;
	protected boolean _selected;
	protected boolean _enabled;
	protected boolean _loaded;

	public SelectablePath(Path path, int type) {
		_path = path;
		_type = resolve(type, path);
		_selected = false;
		_enabled = true;
		_loaded = false;
	}

	public SelectablePath(Path path, int type, boolean selected) {
		_path = path;
		_type = resolve(type, path);
		_selected = selected;
		_enabled = true;
		_loaded = false;
	}

	public SelectablePath(Path path, int type, boolean selected, boolean enabled) {
		_path = path;
		_type = resolve(type, path);
		_selected = selected;
		_enabled = enabled;
		_loaded = false;
	}

	public SelectablePath(Path path, int type, boolean selected, boolean enabled, boolean loaded) {
		_path = path;
		_type = resolve(type, path);
		_selected = selected;
		_enabled = enabled;
		_loaded = loaded;
	}

	private static int resolve(int type, Path path) {
		return (type != DETECT) ? type : !Files.exists(path) ? UNKNOWN : Files.isRegularFile(path) ? REGULAR_FILE : Files.isDirectory(path) ? DIRECTORY : UNKNOWN;
	}

	public Path path() {
		return _path;
	}

	public int type() {
		return _type;
	}

	public void setType(int value) {
		_type = resolve(value, _path);
	}

	public boolean isRoot() {
		return _type == ROOT;
	}

	public boolean isRegularFile() {
		return _type == REGULAR_FILE;
	}

	public boolean isDirectory() {
		return _type == DIRECTORY;
	}

	public boolean selected() {
		return _selected;
	}

	public boolean enabled() {
		return _enabled;
	}

	public void setSelected(boolean value) {
		_selected = value;
	}

	public void toggleSelected() {
		_selected = !_selected;
	}

	public void select() {
		_selected = true;
	}

	public void unselect() {
		_selected = false;
	}

	public void setEnabled(boolean value) {
		_enabled = value;
	}

	public void toggleEnabled() {
		_enabled = !_enabled;
	}

	public void enable() {
		_enabled = true;
	}

	public void disable() {
		_enabled = false;
	}

	public boolean loaded() {
		return _loaded;
	}

	public void setLoaded(boolean value) {
		_loaded = value;
	}

	public static int countSelected(SelectablePath[] paths) {
		int selected = 0;
		for (SelectablePath element : paths) {
			if (element._selected) {
				selected++;
			}
		}
		return selected;
	}

}
