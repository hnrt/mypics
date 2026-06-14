package com.hideakin.mypics.gui;

public class Rectangle {

	private int _width;
	private int _height;

	public Rectangle() {
		_width = 0;
		_height = 0;
	}

	public Rectangle(int width, int height) {
		_width = width;
		_height = height;
	}

	public Rectangle(int width, int height, double scale) {
		_width = (int)(width * scale);
		_height = (int)(height * scale);
	}

	public int width() {
		return _width;
	}

	public int height() {
		return _height;
	}

}
