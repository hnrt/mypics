package com.hideakin.mypics;

import javax.swing.*;

import static com.hideakin.mypics.Application.ABOUT;
import static com.hideakin.mypics.Application.VERSION;

import java.awt.*;
import java.awt.event.*;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public class MainFrame extends JFrame {

	private static final long serialVersionUID = -6804531118120406617L;

	private static final MainFrame _singleton = new MainFrame();

	public static MainFrame getInstance() {
		return _singleton;
	}

	private static final String RELOAD_DIRECTORY = "reloadDirectory";
	private static final String PREVIOUS_SIBLING_DIRECTORY = "previousSiblingDirectory";
	private static final String NEXT_SIBLING_DIRECTORY = "nextSiblingDirectory";
	private static final String PARENT_DIRECTORY = "parentDirectory";
	private static final String FIRST_SUBDIRECTORY = "firstSubdirectory";

	private final MenuBar _menuBar;
	private final ListPane _listPane;
	private final ImagePane _imagePane;
	private final JSplitPane _splitPane;
	private Path _pathToOpen;

	private MainFrame() {
		super("Image Viewer");

		_menuBar = MenuBar.create();
		_listPane = ListPane.create();
		_imagePane = ImagePane.create();

		_splitPane = new JSplitPane(
				JSplitPane.HORIZONTAL_SPLIT,
				_listPane,
				_imagePane
		);
		_splitPane.setDividerLocation(Application.configuration.getHorizontalDividerLocation());
		_splitPane.addPropertyChangeListener("dividerLocation", e -> {
			Application.configuration.setHorizontalDividerLocation((int)e.getNewValue());
		});
		add(_splitPane, BorderLayout.CENTER);

		setJMenuBar(_menuBar);

		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		addWindowListener(new WindowAdapter() {
		    @Override
		    public void windowOpened(WindowEvent e) {
		    	System.err.print("#windowOpened\n");
		    	if (_pathToOpen == null) {
		    		_listPane.loadFrom(Application.configuration.getDirectory());
		    		_listPane.fileList().select(FileList.FIRST);
		    	} else if (Files.isDirectory(_pathToOpen)) {
		    		_listPane.loadFrom(_pathToOpen.toAbsolutePath());
		    		_listPane.fileList().select(FileList.FIRST);
		    	} else if (Files.isRegularFile(_pathToOpen)) {
		    		Path filePath = _pathToOpen.toAbsolutePath();
		    		_listPane.loadFrom(filePath.getParent());
		    		_listPane.fileList().select(filePath);
		    	} else {
		    		showErrorDialog(String.format("Unable to open\n%s", _pathToOpen));
		    		close();
		    	}
		    }
			@Override
			public void windowClosing(WindowEvent e) {
				Application.configuration.save();
				UndoManager.getInstance().clear();
				UndoManager.getInstance().clearTrash();
				System.exit(0);
			}
		});

		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				int state = MainFrame.this.getExtendedState();
				if ((state & (Frame.MAXIMIZED_BOTH | Frame.ICONIFIED)) == 0) {
					int w = MainFrame.this.getWidth();
					int h = MainFrame.this.getHeight();
					Application.configuration.setWindowSize(w, h);
				}
			}
		});

		InputMap im = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), RELOAD_DIRECTORY);
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.ALT_DOWN_MASK), PREVIOUS_SIBLING_DIRECTORY);
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.ALT_DOWN_MASK), NEXT_SIBLING_DIRECTORY);
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.SHIFT_DOWN_MASK | InputEvent.ALT_DOWN_MASK), PARENT_DIRECTORY);
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.SHIFT_DOWN_MASK | InputEvent.ALT_DOWN_MASK), FIRST_SUBDIRECTORY);

		ActionMap am = getRootPane().getActionMap();
		am.put(RELOAD_DIRECTORY, new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				reloadDirectory();
			}
		});
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

		_listPane.onChanged(path -> {
			setTitle(String.format("%s", path));
			_menuBar.update();
		});
		_listPane.onSelected(path -> {
			_imagePane.loadFrom(path);
		});

		_imagePane.onChanged(pane -> {
			if (pane.path() == null) {
				setTitle(String.format("%s", Application.configuration.getDirectory()));
			} else {
				setTitle(String.format("%s [%d%%]", pane.path(), (int)(pane.scale() * 100)));
			}
		});

		setSize(Application.configuration.getWidth(), Application.configuration.getHeight());
		setLocationRelativeTo(null);
	}

	public void setPathToOpen(Path path) {
		_pathToOpen = path;
	}

	public void close() {
		dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
	}

	public void applyChanges() {
		_menuBar.update();
		_imagePane.redraw();
	}

	public void reloadDirectory() {
		Path selected = _listPane.fileList().getSelectedValue();
		_listPane.loadFrom(Application.configuration.getDirectory());
		_listPane.fileList().select(selected);
	}

	public void loadDirectoryFrom(Path path) {
		Path selected = _listPane.previouslySelected(path);
		_listPane.loadFrom(path);
		_listPane.fileList().select(selected);
	}

	public void loadDirectoryFrom(Path path, int index) {
		_listPane.loadFrom(path);
		_listPane.fileList().select(index);
	}

	public void loadPreviousSiblingDirectory() {
		try {
			Path current = Application.configuration.getDirectory();
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
			Path current = Application.configuration.getDirectory();
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
		Path current = Application.configuration.getDirectory();
		Path parent = current.getParent();
		if (Files.exists(parent)) {
			loadDirectoryFrom(parent, FileList.LAST);
		}
	}

	public void loadFirstSubdirectory() {
		try {
			Path current = Application.configuration.getDirectory();
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

	public void loadImageFrom(Path path) {
		Application.configuration.setDirectory(path.getParent());
		_listPane.loadFrom(Application.configuration.getDirectory());
		_listPane.fileList().select(path);
	}

	public void moveSelectedFileTo(Path path) {
		_listPane.fileList().moveTo(path);
	}

	public void copyPathToClipboard() {
		_listPane.fileList().copyPath();
	}

	public void removeSelectedFile() {
		_listPane.fileList().remove();
	}

	public void undoEditOperation() {
		_listPane.fileList().undo();
	}

	public void rotateImageByOrientation(int orientation) {
		_imagePane.rotateByOrientation(orientation);
	}

	public void showAboutDialog() {
		String message = String.format(ABOUT, VERSION);
		JOptionPane.showMessageDialog(this, message, "About", JOptionPane.PLAIN_MESSAGE);
	}

	public void showErrorDialog(String text) {
		JOptionPane.showMessageDialog(this, text, "ERROR", JOptionPane.ERROR_MESSAGE);
	}

	public void showErrorDialog(Exception e) {
		String text;
		if (e instanceof FileAlreadyExistsException ex) {
			text = String.format("File already exists:\n%s", ex.getMessage());
		} else {
			text = e.getMessage();
		}
		JOptionPane.showMessageDialog(this, text, "ERROR", JOptionPane.ERROR_MESSAGE);
	}

	public void setDefaultSize() {
		_listPane.setDefaultSize();
		Application.configuration.setWidth(Configuration.DEFAULT_WIDTH);
		Application.configuration.setHeight(Configuration.DEFAULT_HEIGHT);
		Application.configuration.setHorizontalDividerLocation(Configuration.DEFAULT_HORIZONTAL_DIVIDER_LOCATION);
		setSize(Application.configuration.getWidth(), Application.configuration.getHeight());
		_splitPane.setDividerLocation(Application.configuration.getHorizontalDividerLocation());
	}

}
