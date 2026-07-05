package com.hideakin.mypics.gui.component;

import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.Icon;
import javax.swing.JLabel;

public class CenteredIconLabel extends JLabel {

	private static final long serialVersionUID = 7263177567962142493L;

	public CenteredIconLabel(int size) {
		super();
		setPreferredSize(new Dimension(size, size));
		setOpaque(false);
	}

	public CenteredIconLabel(Icon icon, int size) {
		super(icon);
		setPreferredSize(new Dimension(size, size));
		setOpaque(false);
	}

	@Override
	protected void paintComponent(Graphics g) {
		Icon icon = getIcon();
		if (icon == null) return;
		int w = icon.getIconWidth();
		int h = icon.getIconHeight();
		int x = (getWidth() - w) / 2;
		int y = (getHeight() - h) / 2;
		icon.paintIcon(this, g, x, y);
	}

}
