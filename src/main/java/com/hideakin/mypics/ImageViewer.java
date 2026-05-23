package com.hideakin.mypics;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public class ImageViewer extends JFrame {

	private static final long serialVersionUID = -3714006055304394239L;

	private static ImageViewer _singleton;

	public static ImageViewer getInstance() {
		if (_singleton == null) {
			_singleton = new ImageViewer();
		}
		return _singleton;
	}

	private static final String PREVIOUS_SIBLING_DIRECTORY = "previousSiblingDirectory";
	private static final String NEXT_SIBLING_DIRECTORY = "nextSiblingDirectory";
	private static final String PARENT_DIRECTORY = "parentDirectory";
	private static final String FIRST_SUBDIRECTORY = "firstSubdirectory";

	private final Configuration _configuration = Configuration.getInstance();

	private final MenuBar _menuBar;
	private final ListPane _listPane;
	private final ImagePane _imagePane;
	private final JSplitPane _splitPane;

	private ImageViewer() {
		super("Image Viewer");

		_menuBar = MenuBar.create();
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
		    public void windowOpened(WindowEvent e) {
				_listPane.directoryListModel().loadFrom(_configuration.getDirectory());
				_listPane.fileList().select(FileList.FIRST);
		    }
			@Override
			public void windowClosing(WindowEvent e) {
				_configuration.save();
				UndoManager.getInstance().clear();
				UndoManager.getInstance().clearTrash();
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

		InputMap im = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.ALT_DOWN_MASK), PREVIOUS_SIBLING_DIRECTORY);
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.ALT_DOWN_MASK), NEXT_SIBLING_DIRECTORY);
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.SHIFT_DOWN_MASK | InputEvent.ALT_DOWN_MASK), PARENT_DIRECTORY);
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.SHIFT_DOWN_MASK | InputEvent.ALT_DOWN_MASK), FIRST_SUBDIRECTORY);

		ActionMap am = getRootPane().getActionMap();
		am.put(PREVIOUS_SIBLING_DIRECTORY, new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				loadPreviousSiblingDirectory();
			}
		});
		am.put(NEXT_SIBLING_DIRECTORY, new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
			   	loadNextSiblingDirectory();
			}
		});
		am.put(PARENT_DIRECTORY, new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				loadParentDirectory();
			}
		});
		am.put(FIRST_SUBDIRECTORY, new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				loadFirstSubdirectory();
			}
		});

		_listPane.directoryListModel().onChanged(path -> {
			setTitle(String.format("%s", path));
			_menuBar.update();
		});
		_listPane.fileList().onSelected(path -> {
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
		loadDirectoryFrom(path, FileList.FIRST);
	}

	public void loadDirectoryFrom(Path path, int index) {
		_configuration.setDirectory(path);
		_listPane.directoryListModel().loadFrom(_configuration.getDirectory());
		_listPane.fileList().select(index);
	}

	public void loadImageFrom(Path path) {
		_configuration.setDirectory(path.getParent());
		_listPane.directoryListModel().loadFrom(_configuration.getDirectory());
		_listPane.fileList().select(path);
	}

	public void setDefaultSize() {
		_listPane.setDefaultSize();
		_configuration.setWidth(Configuration.DEFAULT_WIDTH);
		_configuration.setHeight(Configuration.DEFAULT_HEIGHT);
		_configuration.setHorizontalDividerLocation(Configuration.DEFAULT_HORIZONTAL_DIVIDER_LOCATION);
		setSize(_configuration.getWidth(), _configuration.getHeight());
		_splitPane.setDividerLocation(_configuration.getHorizontalDividerLocation());
	}

	public void loadPreviousSiblingDirectory() {
		try {
			Path current = _configuration.getDirectory();
			Path parent = current.getParent();
			Path target = Files.list(parent)
					.filter(x -> Files.isDirectory(x))
					.sorted(Comparator.reverseOrder())
					.filter(x -> x.compareTo(current) < 0)
					.findFirst()
					.orElse(null);
			if (target != null) {
				loadDirectoryFrom(target, FileList.LAST);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void loadNextSiblingDirectory() {
		try {
			Path current = _configuration.getDirectory();
			Path parent = current.getParent();
			Path target = Files.list(parent)
					.filter(x -> Files.isDirectory(x))
					.sorted()
					.filter(x -> x.compareTo(current) > 0)
					.findFirst()
					.orElse(null);
			if (target != null) {
				loadDirectoryFrom(target, FileList.FIRST);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void loadParentDirectory() {
		Path current = _configuration.getDirectory();
		Path parent = current.getParent();
		if (Files.exists(parent)) {
			loadDirectoryFrom(parent, FileList.LAST);
		}
	}

	public void loadFirstSubdirectory() {
		try {
			Path current = _configuration.getDirectory();
			Path target = Files.list(current)
					.filter(x -> Files.isDirectory(x))
					.sorted()
					.findFirst()
					.orElse(null);
			if (target != null) {
				loadDirectoryFrom(target, FileList.FIRST);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
