package com.hideakin.mypics;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;

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
		Icon icon = createThumbnail(value);
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
		_iconCache.clear();
	}

	private Icon createThumbnail(Path path) {
		Icon icon = _iconCache.get(path);
		if (icon != null) {
			return icon;
		}
		try {
			BufferedImage original = ImageLoader.loadCorrectedImage(path.toFile());
			if (original != null) {
				double ow = original.getWidth();
				double oh = original.getHeight();
				double sw = 50.0;
				double sh = 50.0;
				if (ow > oh) {
					sh = sw * oh / ow;
				} else if (ow < oh){
					sw = sh * ow / oh;
				}
				Application.debug(2, "createThumbnail: %dx%d %s", (int)sw, (int)sh, path);
				Image scaled = original.getScaledInstance((int)sw, (int)sh, Image.SCALE_SMOOTH);
				icon = new ImageIcon(scaled);
			} else {
				icon = getDefaultIcon();
			}
		} catch (Exception e) {
			e.printStackTrace();
			icon = getDefaultIcon();
		}
		_iconCache.put(path, icon);
		return icon;
	}

	private Icon getDefaultIcon() {
		return UIManager.getIcon("FileView.fileIcon");
	}

}
