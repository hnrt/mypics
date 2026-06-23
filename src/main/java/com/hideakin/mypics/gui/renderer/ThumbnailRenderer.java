package com.hideakin.mypics.gui.renderer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import com.hideakin.mypics.Application;
import com.hideakin.mypics.gui.util.Thumbnail;

public class ThumbnailRenderer extends JPanel implements ListCellRenderer<Path> {

	private static final long serialVersionUID = 3421207696253706378L;

	private final JLabel _iconLabel = new JLabel();
	private final JLabel _nameLabel = new JLabel();
	private final Map<Path, Icon> _iconCache = new HashMap<>(1024);

	public ThumbnailRenderer() {
		setLayout(new BorderLayout(10, 0));
		add(_iconLabel, BorderLayout.WEST);
		add(_nameLabel, BorderLayout.CENTER);
	}

	@Override
	public Component getListCellRendererComponent(
			JList<? extends Path> list,
			Path value,
			int index,
			boolean isSelected,
			boolean cellHasFocus) {
		_nameLabel.setText(value.getFileName().toString());
		Icon icon = Thumbnail.of(value, _iconCache);
		_iconLabel.setIcon(icon);
		if (isSelected) {
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
		} else {
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		}
		setOpaque(true);
		return this;
	}

	public void clearCache() {
		Application.debug(3, "ThumbnailRenderer::clearCache");
		_iconCache.clear();
	}

}
