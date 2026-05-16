package com.hideakin.mypics;

import static com.hideakin.mypics.Application.ABOUT;
import static com.hideakin.mypics.Application.VERSION;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.nio.file.Path;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

public class MenuBar extends JMenuBar {

	private static final long serialVersionUID = -8982333765550751710L;

	public static MenuBar of(ImageViewer viewer) {
		return new MenuBar(viewer);
	}

	private static class OpenDirectoryMenu extends JMenu {

		private static final long serialVersionUID = 2501143286206061265L;

		private final Configuration _configuration = Configuration.getInstance();
		private final ImageViewer _viewer;

		public OpenDirectoryMenu(String text, ImageViewer viewer) {
			super(text);
			_viewer = viewer;
			build();
		}

		private void build() {
			removeAll();
			Path[] directories = _configuration.getRecent();
			if (directories[1] != null) {
				JMenuItem item = new JMenuItem("1 " + directories[1].toString());
				item.setMnemonic(KeyEvent.VK_1);
				item.addActionListener(e-> changeTo(1));
				add(item);
			}
			if (directories[2] != null) {
				JMenuItem item = new JMenuItem("2 " + directories[2].toString());
				item.setMnemonic(KeyEvent.VK_2);
				item.addActionListener(e-> changeTo(2));
				add(item);
			}
			if (directories[3] != null) {
				JMenuItem item = new JMenuItem("3 " + directories[3].toString());
				item.setMnemonic(KeyEvent.VK_3);
				item.addActionListener(e-> changeTo(3));
				add(item);
			}
			if (directories[4] != null) {
				JMenuItem item = new JMenuItem("4 " + directories[4].toString());
				item.setMnemonic(KeyEvent.VK_4);
				item.addActionListener(e-> changeTo(4));
				add(item);
			}
			if (directories[5] != null) {
				JMenuItem item = new JMenuItem("5 " + directories[5].toString());
				item.setMnemonic(KeyEvent.VK_5);
				item.addActionListener(e-> changeTo(5));
				add(item);
			}
			if (directories[6] != null) {
				JMenuItem item = new JMenuItem("6 " + directories[6].toString());
				item.setMnemonic(KeyEvent.VK_6);
				item.addActionListener(e-> changeTo(6));
				add(item);
			}
			if (directories[7] != null) {
				JMenuItem item = new JMenuItem("7 " + directories[7].toString());
				item.setMnemonic(KeyEvent.VK_7);
				item.addActionListener(e-> changeTo(7));
				add(item);
			}
			if (directories[8] != null) {
				JMenuItem item = new JMenuItem("8 " + directories[8].toString());
				item.setMnemonic(KeyEvent.VK_8);
				item.addActionListener(e-> changeTo(8));
				add(item);
			}
			if (directories[9] != null) {
				JMenuItem item = new JMenuItem("9 " + directories[9].toString());
				item.setMnemonic(KeyEvent.VK_9);
				item.addActionListener(e-> changeTo(9));
				add(item);
			}
			JMenuItem browseItem = new JMenuItem("Browse...");
			browseItem.setMnemonic(KeyEvent.VK_B);
			browseItem.addActionListener(e -> browse());
			add(browseItem);
		}

		private void changeTo(int index) {
			_viewer.loadDirectoryFrom(_configuration.getRecent()[index]);
		}

		private void browse() {
			JFileChooser chooser = new JFileChooser();
			chooser.setDialogTitle("Open directory...");
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooser.setAcceptAllFileFilterUsed(false);
			chooser.setSelectedFile(_configuration.getDirectory().toFile());
			int result = chooser.showOpenDialog(_viewer);
			if (result == JFileChooser.APPROVE_OPTION) {
				_viewer.loadDirectoryFrom(chooser.getSelectedFile().toPath());
			}
		}

	}

	private static class MoveFileMenu extends JMenu {

		private static final long serialVersionUID = 6868167166919666541L;

		private final Configuration _configuration = Configuration.getInstance();
		private final ImageViewer _viewer;

		public MoveFileMenu(String text, ImageViewer viewer) {
			super(text);
			_viewer = viewer;
			build();
		}

		private void build() {
			removeAll();
			Path[] directories = _configuration.getDestinations();
			if (directories[0] != null) {
				JMenuItem item = new JMenuItem("0 " + directories[0].toString());
				item.setMnemonic(KeyEvent.VK_0);
				item.addActionListener(e-> moveTo(0));
				add(item);
			}
			if (directories[1] != null) {
				JMenuItem item = new JMenuItem("1 " + directories[1].toString());
				item.setMnemonic(KeyEvent.VK_1);
				item.addActionListener(e-> moveTo(1));
				add(item);
			}
			if (directories[2] != null) {
				JMenuItem item = new JMenuItem("2 " + directories[2].toString());
				item.setMnemonic(KeyEvent.VK_2);
				item.addActionListener(e-> moveTo(2));
				add(item);
			}
			if (directories[3] != null) {
				JMenuItem item = new JMenuItem("3 " + directories[3].toString());
				item.setMnemonic(KeyEvent.VK_3);
				item.addActionListener(e-> moveTo(3));
				add(item);
			}
			if (directories[4] != null) {
				JMenuItem item = new JMenuItem("4 " + directories[4].toString());
				item.setMnemonic(KeyEvent.VK_4);
				item.addActionListener(e-> moveTo(4));
				add(item);
			}
			if (directories[5] != null) {
				JMenuItem item = new JMenuItem("5 " + directories[5].toString());
				item.setMnemonic(KeyEvent.VK_5);
				item.addActionListener(e-> moveTo(5));
				add(item);
			}
			if (directories[6] != null) {
				JMenuItem item = new JMenuItem("6 " + directories[6].toString());
				item.setMnemonic(KeyEvent.VK_6);
				item.addActionListener(e-> moveTo(6));
				add(item);
			}
			if (directories[7] != null) {
				JMenuItem item = new JMenuItem("7 " + directories[7].toString());
				item.setMnemonic(KeyEvent.VK_7);
				item.addActionListener(e-> moveTo(7));
				add(item);
			}
			if (directories[8] != null) {
				JMenuItem item = new JMenuItem("8 " + directories[8].toString());
				item.setMnemonic(KeyEvent.VK_8);
				item.addActionListener(e-> moveTo(8));
				add(item);
			}
			if (directories[9] != null) {
				JMenuItem item = new JMenuItem("9 " + directories[9].toString());
				item.setMnemonic(KeyEvent.VK_9);
				item.addActionListener(e-> moveTo(9));
				add(item);
			}
		}

		private void moveTo(int index) {
			_viewer.moveFileTo(_configuration.getDestination(index));
		}

	}

	private final Configuration _configuration = Configuration.getInstance();
	private final ImageViewer _viewer;

	private MenuBar(ImageViewer viewer) {
		super();
		_viewer = viewer;
		buildFileMenu();
		buildEditMenu();
		buildViewMenu();
		buildOptionsMenu();
		buildHelpMenu();
	}

	private void buildFileMenu() {
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		JMenu openDirectoryMenu = new OpenDirectoryMenu("Open directory", _viewer);
		openDirectoryMenu.setMnemonic(KeyEvent.VK_D);
		JMenuItem openFileItem = new JMenuItem("Open file...");
		openFileItem.setMnemonic(KeyEvent.VK_F);
		openFileItem.addActionListener(e -> openFile());
		JMenuItem exitItem = new JMenuItem("Exit");
		exitItem.setMnemonic(KeyEvent.VK_X);
		exitItem.addActionListener(e -> _viewer.close());
		fileMenu.add(openDirectoryMenu);
		fileMenu.add(openFileItem);
		fileMenu.addSeparator();
		fileMenu.add(exitItem);
		add(fileMenu);
	}

	private void buildEditMenu() {
		JMenu editMenu = new JMenu("Edit");
		editMenu.setMnemonic(KeyEvent.VK_E);
		JMenu moveFileMenu = new MoveFileMenu("Move file", _viewer);
		moveFileMenu.setMnemonic(KeyEvent.VK_M);
		JMenuItem undoItem = new JMenuItem("Undo");
		undoItem.setMnemonic(KeyEvent.VK_U);
		undoItem.addActionListener(e-> _viewer.undo());
		editMenu.add(moveFileMenu);
		editMenu.addSeparator();
		editMenu.add(undoItem);
		add(editMenu);
	}

	private void buildViewMenu() {
		JMenu viewMenu = new JMenu("View");
		viewMenu.setMnemonic(KeyEvent.VK_V);
		JMenuItem rotateRightItem = new JMenuItem("Rotate right");
		rotateRightItem.setMnemonic(KeyEvent.VK_R);
		rotateRightItem.addActionListener(e -> _viewer.imagePane().rotateByOrientation(ImageLoader.ROTATE_90_DEGREES));
		viewMenu.add(rotateRightItem);
		JMenuItem rotateLeftItem = new JMenuItem("Rotate left");
		rotateLeftItem.setMnemonic(KeyEvent.VK_L);
		rotateLeftItem.addActionListener(e -> _viewer.imagePane().rotateByOrientation(ImageLoader.ROTATE_270_DEGREES));
		viewMenu.add(rotateLeftItem);
		add(viewMenu);
	}

	private void buildOptionsMenu() {
		JMenu optionsMenu = new JMenu("Options");
		optionsMenu.setMnemonic(KeyEvent.VK_O);
		JMenuItem moveDestItem = new JMenuItem("Move destination...");
		moveDestItem.setMnemonic(KeyEvent.VK_D);
		moveDestItem.addActionListener(e -> MoveDestinationDialog.of(_viewer).showDialog());
		optionsMenu.add(moveDestItem);
		JMenuItem preferencesItem = new JMenuItem("Preferences...");
		preferencesItem.addActionListener(e -> PreferencesDialog.of(_viewer).showDialog());
		optionsMenu.add(preferencesItem);
		optionsMenu.addSeparator();
		JMenuItem defaultSizeItem = new JMenuItem("Default size");
		defaultSizeItem.setMnemonic(KeyEvent.VK_D);
		defaultSizeItem.addActionListener(e -> _viewer.setDefaultSize());
		optionsMenu.add(defaultSizeItem);
		add(optionsMenu);
	}

	private void buildHelpMenu() {
		JMenu helpMenu = new JMenu("Help");
		helpMenu.setMnemonic(KeyEvent.VK_H);
		JMenuItem aboutItem = new JMenuItem("About...");
		aboutItem.setMnemonic(KeyEvent.VK_A);
		String message = String.format(ABOUT, VERSION);
		aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(this, message, "About", JOptionPane.PLAIN_MESSAGE));
		helpMenu.add(aboutItem);
		add(helpMenu);
	}

	private void openFile() {
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle("Open image file...");
	   	chooser.setCurrentDirectory(_configuration.getDirectory().toFile());
		int result = chooser.showOpenDialog(_viewer);
		if (result == JFileChooser.APPROVE_OPTION) {
			_viewer.loadImageFrom(chooser.getSelectedFile().toPath());
		}
	}

	public void update() {
		JMenu fileMenu = getMenu(0);
		int n = fileMenu.getMenuComponentCount();
		for (int i = 0; i < n; i++) {
			Component c = fileMenu.getMenuComponent(i);
			if (c instanceof OpenDirectoryMenu menu) {
				menu.build();
				break;
			}
		}
		JMenu editMenu = getMenu(1);
		n = editMenu.getMenuComponentCount();
		for (int i = 0; i < n; i++) {
			Component c = editMenu.getMenuComponent(i);
			if (c instanceof MoveFileMenu menu) {
				menu.build();
				break;
			}
		}
	}

}
