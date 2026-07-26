package com.hideakin.mypics.gui.renderer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Rectangle;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import com.hideakin.mypics.gui.model.FileListModel;
import com.hideakin.mypics.gui.util.Thumbnail;

import static com.hideakin.mypics.Application.debug;

public class PathListCellRenderer extends JPanel implements ListCellRenderer<Path> {

	private static final long serialVersionUID = 3421207696253706378L;

	private final JLabel _iconLabel = new JLabel();
	private final JLabel _nameLabel = new JLabel();
	private final Map<Path, Icon> _iconCache = new HashMap<>(1024);
	private int _size = Integer.MAX_VALUE;
	private boolean _thumbnailEnabled = true;
	private Runnable _callback = null;

	public PathListCellRenderer() {
		this(true);
	}

	public PathListCellRenderer(boolean thumbnailEnabled) {
		setLayout(new BorderLayout(10, 0));
		add(_iconLabel, BorderLayout.WEST);
		add(_nameLabel, BorderLayout.CENTER);
		enableThumbnail(thumbnailEnabled);
	}

	@Override
	public Component getListCellRendererComponent(JList<? extends Path> list, Path value, int index, boolean isSelected, boolean cellHasFocus) {
		debug(3, "PathListCellRenderer::getListCellRendererComponent(%s)", value);
		Path fileName = value.getFileName(); 
		if (fileName != null) {
			_nameLabel.setText(fileName.toString());
			if (_thumbnailEnabled) {
				Icon icon = Thumbnail.of(value, _iconCache, iconLoaded -> {
					if (_iconCache.size() < _size) {
						Rectangle r = list.getCellBounds(index, index);
						list.repaint(r);
					} else if (list.getModel() instanceof FileListModel model) {
						debug(3, "PathListCellRenderer::getListCellRendererComponent: size adjustment");
						_size = Integer.MAX_VALUE;
						model.set(0, model.get(0));
						Runnable callback = _callback;
						_callback = null;
						if (callback != null) {
							callback.run();
						}
					}
				});
				_iconLabel.setIcon(icon);
			}
		} else {
			// root directory
			_nameLabel.setText(value.toString());
			if (_thumbnailEnabled) {
				_iconLabel.setIcon(null);
			}
		}
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

	public void enableThumbnail(boolean enabled) {
		_thumbnailEnabled = enabled;
		_iconLabel.setVisible(enabled);
	}

	public boolean isThumbnailEnabled() {
		return _thumbnailEnabled;
	}

	public void clearCache() {
		debug(3, "PathListCellRenderer::clearCache");
		_iconCache.clear();
		_size = Integer.MAX_VALUE;
		_callback = null;
	}

	public void adjustSize(FileListModel model, Runnable callback) {
		int size = model.getSize();
		debug(3, "PathListCellRenderer::adjustSize: cache=%d list=%d", _iconCache.size(), size);
		if (_iconCache.size() < size) {
			_size = size;
			_callback = callback;
		} else if (size > 0) {
			debug(3, "PathListCellRenderer::adjustSize: size adjustment");
			_size = Integer.MAX_VALUE;
			model.set(0, model.get(0));
			callback.run();
		}
	}

}
