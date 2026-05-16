package com.hideakin.mypics;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.nio.file.Path;

public class ImageViewer extends JFrame {

	private static final long serialVersionUID = -3714006055304394239L;

	private final Configuration _configuration = Configuration.getInstance();

	private final MenuBar _menuBar;
	private final ListPane _listPane;
	private final ImagePane _imagePane;
	private final JSplitPane _splitPane;

	public ImageViewer() {
		super("Image Viewer");

		_menuBar = MenuBar.of(this);
		_listPane = ListPane.create();
		_imagePane = ImagePane.create();

		_splitPane = new JSplitPane(
				JSplitPane.HORIZONTAL_SPLIT,
				_listPane,
				_imagePane
		);
		_splitPane.setDividerLocation(_configuration.getHorizontalDividerLocation());
		_splitPane.addPropertyChangeListener("dividerLocation", e -> {
			_configuration.setHorizontalDividerLocation((int)e.getNewValue());
		});
		add(_splitPane, BorderLayout.CENTER);

		setJMenuBar(_menuBar);

		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				_configuration.save();
				System.exit(0);
			}
		});

		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				int state = ImageViewer.this.getExtendedState();
				if ((state & (Frame.MAXIMIZED_BOTH | Frame.ICONIFIED)) == 0) {
					int w = ImageViewer.this.getWidth();
					int h = ImageViewer.this.getHeight();
					_configuration.setWindowSize(w, h);
				}
			}
		});

		_listPane.onChanged(path -> {
			setTitle(String.format("%s", path));
			_menuBar.update();
		});
		_listPane.onSelected(path -> {
			_imagePane.loadFrom(path);
		});

		_imagePane.onChanged(pane -> {
			if (pane.path() == null) {
				setTitle(String.format("%s", _configuration.getDirectory()));
			} else {
				setTitle(String.format("%s [%d%%]", pane.path(), (int)(pane.scale() * 100)));
			}
		});

		setSize(_configuration.getWidth(), _configuration.getHeight());
		setLocationRelativeTo(null);

		_listPane.loadDirectoryFrom(_configuration.getDirectory());
	}

	public void close() {
		dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
	}

	public MenuBar menuBar() {
		return _menuBar;
	}

	public ListPane listPane() {
		return _listPane;
	}

	public ImagePane imagePane() {
		return _imagePane;
	}

	public void loadDirectoryFrom(Path path) {
		_configuration.setDirectory(path);
		_listPane.loadDirectoryFrom(_configuration.getDirectory());
	}

	public void loadImageFrom(Path path) {
		_configuration.setDirectory(path.getParent());
		_listPane.loadDirectoryFrom(_configuration.getDirectory());
		_listPane.select(path);
	}

	public void moveFileTo(Path path) {
		_listPane.moveSelectedFileTo(path);
	}

	public void undo() {
		_listPane.undo();
	}

	public void setDefaultSize() {
		_listPane.setDefaultSize();
		_configuration.setWidth(Configuration.DEFAULT_WIDTH);
		_configuration.setHeight(Configuration.DEFAULT_HEIGHT);
		_configuration.setHorizontalDividerLocation(Configuration.DEFAULT_HORIZONTAL_DIVIDER_LOCATION);
		setSize(_configuration.getWidth(), _configuration.getHeight());
		_splitPane.setDividerLocation(_configuration.getHorizontalDividerLocation());
	}

}
