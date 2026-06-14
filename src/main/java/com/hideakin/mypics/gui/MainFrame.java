package com.hideakin.mypics.gui;

import javax.swing.*;

import com.hideakin.mypics.Application;
import com.hideakin.mypics.Configuration;
import com.hideakin.mypics.io.FileManager;

import static com.hideakin.mypics.Application.ABOUT;
import static com.hideakin.mypics.Application.VERSION;
import static com.hideakin.mypics.Application.configuration;
import static com.hideakin.mypics.Application.inProcessing;

import java.awt.*;
import java.awt.event.*;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;

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
		_splitPane.setDividerLocation(configuration.getHorizontalDividerLocation());
		_splitPane.addPropertyChangeListener("dividerLocation", e -> {
			configuration.setHorizontalDividerLocation((int)e.getNewValue());
		});
		add(_splitPane, BorderLayout.CENTER);

		setJMenuBar(_menuBar);

		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		addWindowListener(new WindowAdapter() {
		    @Override
		    public void windowOpened(WindowEvent e) {
		    	Application.debug(3, "windowOpened");
		    	if (_pathToOpen == null) {
		    		_listPane.loadFrom(configuration.getDirectory(), FileList.FIRST);
		    	} else if (Files.isDirectory(_pathToOpen)) {
		    		_listPane.loadFrom(_pathToOpen.toAbsolutePath(), FileList.FIRST);
		    	} else if (Files.isRegularFile(_pathToOpen)) {
		    		Path filePath = _pathToOpen.toAbsolutePath();
		    		_listPane.loadFrom(filePath.getParent(), filePath);
		    	} else {
		    		showErrorDialog(String.format("Unable to open\n%s", _pathToOpen));
		    		close();
		    	}
		    }
			@Override
			public void windowClosing(WindowEvent e) {
		    	Application.debug(3, "windowClosing");
				configuration.save();
				FileManager.getInstance().clear();
				FileManager.getInstance().clearTrash();
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
					configuration.setWindowSize(w, h);
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

		_listPane.onDirectoryChanged(path -> {
			Application.debug(3, "listPane.onDirectoryChanged(%s)", path);
			setTitle(String.format("%s", path));
			_listPane.loadFrom(path);
			_menuBar.update();
		});
		_listPane.onFileSelected(path -> {
			Application.debug(3, "listPane.onFileSelected(%s)", path);
			_imagePane.loadFrom(path);
			_menuBar.enablePath(path != null);
			_menuBar.enableImage(_imagePane.path() != null);
		});

		_imagePane.onChanged(pane -> {
			Application.debug(3, "imagePane.onChanged(path=%s)", pane.path());
			if (pane.path() == null) {
				setTitle(String.format("%s", configuration.getDirectory()));
			} else {
				setTitle(String.format("%s [%d%%]", pane.path(), (int)(pane.scale() * 100)));
			}
		});

		setSize(configuration.getWidth(), configuration.getHeight());
		setLocationRelativeTo(null);
	}

	public void setPathToOpen(Path path) {
		_pathToOpen = path;
	}

	public ListPane listPane() {
		return _listPane;
	}

	public void close() {
		dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
	}

	public void applyChanges() {
		_menuBar.update();
		_imagePane.redraw();
	}

	public void reloadDirectory() {
		_listPane.loadFrom(configuration.getDirectory());
	}

	public void loadDirectoryFrom(Path path) {
		_listPane.loadFrom(path);
	}

	public void loadDirectoryFrom(Path path, int index) {
		_listPane.loadFrom(path, index);
	}

	public void loadPreviousSiblingDirectory() {
		inProcessing.run(() -> {
			try {
				Path current = configuration.getDirectory();
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
		});
	}

	public void loadNextSiblingDirectory() {
		inProcessing.run(() -> {
			try {
				Path current = configuration.getDirectory();
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
		});
	}

	public void loadParentDirectory() {
		inProcessing.run(() -> {
			Path current = configuration.getDirectory();
			Path parent = current.getParent();
			if (Files.exists(parent)) {
				loadDirectoryFrom(parent);
			}
		});
	}

	public void loadFirstSubdirectory() {
		inProcessing.run(() -> {
			try {
				Path current = configuration.getDirectory();
				Path target = Files.list(current)
						.filter(x -> Files.isDirectory(x))
						.sorted()
						.findFirst()
						.orElse(null);
				if (target != null) {
					loadDirectoryFrom(target);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	public void loadImageFrom(Path path) {
		inProcessing.run(() -> {
			configuration.setDirectory(path.getParent());
			_listPane.loadFrom(configuration.getDirectory());
			_listPane.fileList().select(path);
		});
	}

	public void rotateImageByOrientation(int orientation) {
		inProcessing.run(() -> {
			_imagePane.rotateByOrientation(orientation);
		});
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

	public Path getSelectedFile() {
		return _listPane.getSelectedFile();
	}

	public List<Path> getSelectedFiles() {
		return _listPane.getSelectedFiles();
	}

	public void removeFiles(List<Path> paths) {
		_listPane.removeFiles(paths);
	}

	public void addFiles(List<Path> paths) {
		_listPane.addFiles(paths);
	}

	public void startRenaming(BiFunction<Path, String, Path> cb) {
		_listPane.fileList().startRenaming(cb);
	}

	public void setDefaultSize() {
		_listPane.setDefaultSize();
		configuration.setWidth(Configuration.DEFAULT_WIDTH);
		configuration.setHeight(Configuration.DEFAULT_HEIGHT);
		configuration.setHorizontalDividerLocation(Configuration.DEFAULT_HORIZONTAL_DIVIDER_LOCATION);
		setSize(configuration.getWidth(), configuration.getHeight());
		_splitPane.setDividerLocation(configuration.getHorizontalDividerLocation());
	}

}
