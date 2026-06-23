package com.hideakin.mypics.model;

import java.nio.file.Path;

public class PathNode {

	public static PathNode of(Path path) {
		return new PathNode(path);
	}

	protected final Path _path;
	protected PathNode _next;

	protected PathNode(Path path) {
		_path = path;
		_next = null;
	}

	public Path path() {
		return _path;
	}

	public PathNode next() {
		return _next;
	}

	public PathNode add(Path path) {
		PathNode current = this;
		PathNode next;
		while ((next = current._next) != null) {
			current = next;
		}
		current._next = new PathNode(path);
		return this;
	}

	public int length() {
		int counter = 1;
		PathNode current = this;
		PathNode next;
		while ((next = current._next) != null) {
			counter++;
			current = next;
		}
		return counter;
	}

}
