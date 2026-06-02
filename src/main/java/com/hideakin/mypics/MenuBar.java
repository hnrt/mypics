package com.hideakin.mypics;

import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.nio.file.Path;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.MenuElement;

public class MenuBar extends JMenuBar {

	private static final long serialVersionUID = -8982333765550751710L;

	public static MenuBar create() {
		return new MenuBar();
	}

	private static class Menu extends JMenu {

		private static final long serialVersionUID = -8503390986339420365L;

		public Menu(String label, int mnemonic) {
			super(label);
			setMnemonic(mnemonic);
			setName(createName());
		}

		public JMenuItem addMenuItem(String label, int mnemonic, ActionListener al) {
			JMenuItem menuItem = new JMenuItem(label);
			menuItem.setMnemonic(mnemonic);
			menuItem.addActionListener(al);
			add(menuItem);
			return menuItem;
		}

		public JMenuItem addMenuItem(String label, int mnemonic, KeyStroke ks, ActionListener al) {
			JMenuItem menuItem = new JMenuItem(label);
			menuItem.setMnemonic(mnemonic);
			menuItem.addActionListener(al);
			menuItem.setAccelerator(ks);
			add(menuItem);
			return menuItem;
		}

		public JCheckBoxMenuItem addCheckBoxMenuItem(String label, int mnemonic, ActionListener al) {
			JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(label);
			menuItem.setMnemonic(mnemonic);
			menuItem.addActionListener(al);
			add(menuItem);
			return menuItem;
		}

		@SuppressWarnings("unused")
		public JCheckBoxMenuItem addCheckBoxMenuItem(String label, int mnemonic, KeyStroke ks, ActionListener al) {
			JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(label);
			menuItem.setMnemonic(mnemonic);
			menuItem.addActionListener(al);
			menuItem.setAccelerator(ks);
			add(menuItem);
			return menuItem;
		}

		@Override
		public JMenuItem add(JMenuItem menuItem) {
			menuItem.setName(createName(menuItem));
			return super.add(menuItem);
		}

		private String createName() {
			return createName(getText(), new StringBuilder());
		}

		private String createName(JMenuItem menuItem) {
			return createName(menuItem.getText(), new StringBuilder(getName()));
		}

		private static String createName(String text, StringBuilder buf) {
			int j = 0;
			for (int i = 0; i < text.length(); i++) {
				char c = text.charAt(i);
				if (Character.isWhitespace(c)) {
					j = 0;
				} else if (j++ == 0) {
					buf.append(Character.toUpperCase(c));
				} else {
					buf.append(c);
				}
			}
			return buf.toString();
		}

	}

	private static class OpenDirectoryMenu extends Menu {

		private static final long serialVersionUID = 2501143286206061265L;

		public OpenDirectoryMenu(String label, int mnemonic) {
			super(label, mnemonic);
			build();
		}

		private void build() {
			removeAll();
			Path[] directories = Application.configuration.getRecent();
			if (directories[1] != null) {
				addMenuItem(label(directories, 1), KeyEvent.VK_1, e -> changeTo(1));
			}
			if (directories[2] != null) {
				addMenuItem(label(directories, 2), KeyEvent.VK_2, e -> changeTo(2));
			}
			if (directories[3] != null) {
				addMenuItem(label(directories, 3), KeyEvent.VK_3, e -> changeTo(3));
			}
			if (directories[4] != null) {
				addMenuItem(label(directories, 4), KeyEvent.VK_4, e -> changeTo(4));
			}
			if (directories[5] != null) {
				addMenuItem(label(directories, 5), KeyEvent.VK_5, e -> changeTo(5));
			}
			if (directories[6] != null) {
				addMenuItem(label(directories, 6), KeyEvent.VK_6, e -> changeTo(6));
			}
			if (directories[7] != null) {
				addMenuItem(label(directories, 7), KeyEvent.VK_7, e -> changeTo(7));
			}
			if (directories[8] != null) {
				addMenuItem(label(directories, 8), KeyEvent.VK_8, e -> changeTo(8));
			}
			if (directories[9] != null) {
				addMenuItem(label(directories, 9), KeyEvent.VK_9, e -> changeTo(9));
			}
			addMenuItem("Browse...", KeyEvent.VK_B, e -> browse());
		}

		private String label(Path[] directories, int index) {
			return String.format("%d %s", index, directories[index]);
		}

		private void changeTo(int index) {
			Application.mainFrame.loadDirectoryFrom(Application.configuration.getRecent()[index]);
		}

		private void browse() {
			JFileChooser chooser = new JFileChooser();
			chooser.setDialogTitle("Open directory...");
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooser.setAcceptAllFileFilterUsed(false);
			chooser.setSelectedFile(Application.configuration.getDirectory().toFile());
			int result = chooser.showOpenDialog(Application.mainFrame);
			if (result == JFileChooser.APPROVE_OPTION) {
				Application.mainFrame.loadDirectoryFrom(chooser.getSelectedFile().toPath());
			}
		}

	}

	private static class MoveFileMenu extends Menu {

		private static final long serialVersionUID = 6868167166919666541L;

		public MoveFileMenu(String label, int mnemonic) {
			super(label, mnemonic);
			build();
		}

		private void build() {
			removeAll();
			Path[] directories = Application.configuration.getDestinations();
			if (directories[0] != null) {
				addMenuItem(label(directories, 0), KeyEvent.VK_0, KeyStroke.getKeyStroke(KeyEvent.VK_0, InputEvent.CTRL_DOWN_MASK), e -> moveTo(0));
			}
			if (directories[1] != null) {
				addMenuItem(label(directories, 1), KeyEvent.VK_1, KeyStroke.getKeyStroke(KeyEvent.VK_1, InputEvent.CTRL_DOWN_MASK), e -> moveTo(1));
			}
			if (directories[2] != null) {
				addMenuItem(label(directories, 2), KeyEvent.VK_2, KeyStroke.getKeyStroke(KeyEvent.VK_2, InputEvent.CTRL_DOWN_MASK), e -> moveTo(2));
			}
			if (directories[3] != null) {
				addMenuItem(label(directories, 3), KeyEvent.VK_3, KeyStroke.getKeyStroke(KeyEvent.VK_3, InputEvent.CTRL_DOWN_MASK), e -> moveTo(3));
			}
			if (directories[4] != null) {
				addMenuItem(label(directories, 4), KeyEvent.VK_4, KeyStroke.getKeyStroke(KeyEvent.VK_4, InputEvent.CTRL_DOWN_MASK), e -> moveTo(4));
			}
			if (directories[5] != null) {
				addMenuItem(label(directories, 5), KeyEvent.VK_5, KeyStroke.getKeyStroke(KeyEvent.VK_5, InputEvent.CTRL_DOWN_MASK), e -> moveTo(5));
			}
			if (directories[6] != null) {
				addMenuItem(label(directories, 6), KeyEvent.VK_6, KeyStroke.getKeyStroke(KeyEvent.VK_6, InputEvent.CTRL_DOWN_MASK), e -> moveTo(6));
			}
			if (directories[7] != null) {
				addMenuItem(label(directories, 7), KeyEvent.VK_7, KeyStroke.getKeyStroke(KeyEvent.VK_7, InputEvent.CTRL_DOWN_MASK), e -> moveTo(7));
			}
			if (directories[8] != null) {
				addMenuItem(label(directories, 8), KeyEvent.VK_8, KeyStroke.getKeyStroke(KeyEvent.VK_8, InputEvent.CTRL_DOWN_MASK), e -> moveTo(8));
			}
			if (directories[9] != null) {
				addMenuItem(label(directories, 9), KeyEvent.VK_9, KeyStroke.getKeyStroke(KeyEvent.VK_9, InputEvent.CTRL_DOWN_MASK), e -> moveTo(9));
			}
		}

		private String label(Path[] directories, int index) {
			return String.format("%d %s", index, directories[index]);
		}

		private void moveTo(int index) {
			Application.mainFrame.moveSelectedFileTo(Application.configuration.getDestination(index));
		}

	}

	private MenuBar() {
		super();
		buildFileMenu();
		buildEditMenu();
		buildViewMenu();
		buildOptionsMenu();
		buildHelpMenu();
	}

	private void buildFileMenu() {
		Menu menu = createMenu("File", KeyEvent.VK_F);
		menu.add(new OpenDirectoryMenu("Open directory", KeyEvent.VK_D));
		menu.addMenuItem("Open file...", KeyEvent.VK_F, e -> openFile());
		menu.addSeparator();
		menu.addMenuItem("Reload directory", KeyEvent.VK_R,
				KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0),
				e -> Application.mainFrame.reloadDirectory());
		menu.addMenuItem("Load previous sibling directory", KeyEvent.VK_P,
				KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.ALT_DOWN_MASK),
				e -> Application.mainFrame.loadPreviousSiblingDirectory());
		menu.addMenuItem("Load next sibling directory", KeyEvent.VK_N,
				KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.ALT_DOWN_MASK),
				e -> Application.mainFrame.loadNextSiblingDirectory());
		menu.addMenuItem("Load parent directory", KeyEvent.VK_A,
				KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.SHIFT_DOWN_MASK | InputEvent.ALT_DOWN_MASK),
				e -> Application.mainFrame.loadParentDirectory());
		menu.addMenuItem("Load first subdirectory", KeyEvent.VK_S,
				KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.SHIFT_DOWN_MASK | InputEvent.ALT_DOWN_MASK),
				e -> Application.mainFrame.loadFirstSubdirectory());
		menu.addSeparator();
		menu.addMenuItem("Exit", KeyEvent.VK_X, e -> Application.mainFrame.close());
	}

	private void buildEditMenu() {
		Menu menu = createMenu("Edit", KeyEvent.VK_E);
		menu.addMenuItem("Copy path", KeyEvent.VK_C, e -> Application.mainFrame.copyPathToClipboard());
		menu.addSeparator();
		menu.add(new MoveFileMenu("Move file", KeyEvent.VK_M));
		menu.addMenuItem("Delete file", KeyEvent.VK_D, e -> Application.mainFrame.removeSelectedFile());
		menu.addSeparator();
		menu.addMenuItem("Undo", KeyEvent.VK_U, e -> Application.mainFrame.undoEditOperation());
		menu.addSeparator();
		menu.addMenuItem("Trash...", KeyEvent.VK_T, e -> TrashDialog.create().showDialog());
	}

	private void buildViewMenu() {
		Menu menu = createMenu("View", KeyEvent.VK_V);
		menu.addMenuItem("Rotate right", KeyEvent.VK_R, e -> Application.mainFrame.rotateImageByOrientation(ImageLoader.ROTATE_90_DEGREES));
		menu.addMenuItem("Rotate left", KeyEvent.VK_L, e -> Application.mainFrame.rotateImageByOrientation(ImageLoader.ROTATE_270_DEGREES));
	}

	private void buildOptionsMenu() {
		Menu menu = createMenu("Options", KeyEvent.VK_O);
		menu.addMenuItem("Move destination...", KeyEvent.VK_M, e -> MoveDestinationDialog.create().showDialog());
		menu.addMenuItem("Preferences...", KeyEvent.VK_P, e -> PreferencesDialog.create().showDialog());
		menu.addSeparator();
		menu.addCheckBoxMenuItem("Filter directory", KeyEvent.VK_F, e -> Application.mainFrame.listPane().toggleDirectoryFilterTextFieldVisibility());
		menu.addSeparator();
		menu.addMenuItem("Default size", KeyEvent.VK_D, e -> Application.mainFrame.setDefaultSize());
	}

	private void buildHelpMenu() {
		Menu menu = createMenu("Help", KeyEvent.VK_H);
		menu.addMenuItem("About...", KeyEvent.VK_A, e -> Application.mainFrame.showAboutDialog());
	}

	private Menu createMenu(String label, int mnemonic) {
		Menu menu = new Menu(label, mnemonic);
		add(menu);
		return menu;
	}

	private void openFile() {
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle("Open image file...");
	   	chooser.setCurrentDirectory(Application.configuration.getDirectory().toFile());
		int result = chooser.showOpenDialog(Application.mainFrame);
		if (result == JFileChooser.APPROVE_OPTION) {
			Application.mainFrame.loadImageFrom(chooser.getSelectedFile().toPath());
		}
	}

	public void update() {
		((OpenDirectoryMenu)findMenuByName("FileOpenDirectory")).build();
		((MoveFileMenu)findMenuByName("EditMoveFile")).build();
		findMenuItemByName("OptionsFilterDirectory").setSelected(Application.mainFrame.listPane().getDirectoryFilterTextFieldVisibility());
	}

	public void enablePath(boolean enabled) {
		findMenuByName("EditMoveFile").setEnabled(enabled);
		findMenuItemByName("EditCopyPath").setEnabled(enabled);
		findMenuItemByName("EditMoveFile").setEnabled(enabled);
		findMenuItemByName("EditDeleteFile").setEnabled(enabled);
		findMenuItemByName("EditUndo").setEnabled(FileManager.getInstance().numberOfUndoes() > 0);
	}

	public void enableImage(boolean enabled) {
		findMenuItemByName("ViewRotateRight").setEnabled(enabled);
		findMenuItemByName("ViewRotateLeft").setEnabled(enabled);
	}

	private JMenuItem findMenuItemByName(String name) {
		return findMenuItemByName(this, name);
	}

	private static JMenuItem findMenuItemByName(MenuElement root, String name) {
		if (root instanceof JMenuItem menuItem) {
			if (name.equals(menuItem.getName())) {
				return menuItem;
			}
		}
		for (MenuElement child : root.getSubElements()) {
			JMenuItem found = findMenuItemByName(child, name);
			if (found != null) {
				return found;
			}
		}
		return null;
	}

	private Menu findMenuByName(String name) {
		return findMenuByName(this, name);
	}

	private static Menu findMenuByName(MenuElement root, String name) {
		if (root instanceof Menu menu) {
			if (name.equals(menu.getName())) {
				return menu;
			}
		}
		for (MenuElement child : root.getSubElements()) {
			Menu found = findMenuByName(child, name);
			if (found != null) {
				return found;
			}
		}
		return null;
	}

}
