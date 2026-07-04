package com.hideakin.mypics.model;

import java.nio.file.Files;
import java.nio.file.Path;

public class SelectablePath {

	public static final int UNKNOWN = 0;
	public static final int REGULAR_FILE = 1;
	public static final int DIRECTORY = 2;

	public static SelectablePath ofRegularFile(Path path, boolean selected) {
		return new SelectablePath(path, selected, REGULAR_FILE);
	}

	public static SelectablePath ofDirectory(Path path, boolean selected) {
		return new SelectablePath(path, selected, DIRECTORY);
	}

	protected final Path _path;
	protected boolean _selected;
	protected boolean _enabled;
	protected int _type;
	protected boolean _loaded;

	public SelectablePath(Path path) {
		_path = path;
		_selected = false;
		_enabled = true;
		_type = Files.exists(path) ? (Files.isRegularFile(path) ? REGULAR_FILE : Files.isDirectory(path) ? DIRECTORY : UNKNOWN) : UNKNOWN;
		_loaded = false;
	}

	public SelectablePath(Path path, boolean selected) {
		_path = path;
		_selected = selected;
		_enabled = true;
		_type = Files.exists(path) ? (Files.isRegularFile(path) ? REGULAR_FILE : Files.isDirectory(path) ? DIRECTORY : UNKNOWN) : UNKNOWN;
		_loaded = false;
	}

	public SelectablePath(Path path, boolean selected, boolean enabled) {
		_path = path;
		_selected = selected;
		_enabled = enabled;
		_type = Files.exists(path) ? (Files.isRegularFile(path) ? REGULAR_FILE : Files.isDirectory(path) ? DIRECTORY : UNKNOWN) : UNKNOWN;
		_loaded = false;
	}

	public SelectablePath(Path path, boolean selected, int type) {
		_path = path;
		_selected = selected;
		_enabled = true;
		_type = type;
		_loaded = false;
	}

	public Path path() {
		return _path;
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

	public void toggle() {
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

	public static int countSelected(SelectablePath[] paths) {
		int selected = 0;
		for (SelectablePath element : paths) {
			if (element._selected) {
				selected++;
			}
		}
		return selected;
	}

	public int type() {
		return _type;
	}

	public void setType(int value) {
		_type = value;
	}

	public boolean loaded() {
		return _loaded;
	}

	public void setLoaded(boolean value) {
		_loaded = value;
	}

}
