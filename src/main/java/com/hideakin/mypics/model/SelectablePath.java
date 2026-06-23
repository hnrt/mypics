package com.hideakin.mypics.model;

import java.nio.file.Path;

public class SelectablePath {

	protected final Path _path;
	protected boolean _selected;

	public SelectablePath(Path path) {
		_path = path;
		_selected = false;
	}

	public SelectablePath(Path path, boolean selected) {
		_path = path;
		_selected = selected;
	}

	public Path path() {
		return _path;
	}

	public boolean selected() {
		return _selected;
	}

	public void set(boolean value) {
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

}
