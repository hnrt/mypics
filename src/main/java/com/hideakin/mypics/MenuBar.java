package com.hideakin.mypics;

import static com.hideakin.mypics.Application.ABOUT;
import static com.hideakin.mypics.Application.VERSION;

import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
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
        JMenuItem openItem = new JMenuItem("Open...");
        openItem.setMnemonic(KeyEvent.VK_O);
        openItem.addActionListener(e -> openImage());
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.setMnemonic(KeyEvent.VK_X);
        exitItem.addActionListener(e -> {
        	_viewer.dispatchEvent(new WindowEvent(_viewer, WindowEvent.WINDOW_CLOSING));
        });
        fileMenu.add(openItem);
        fileMenu.add(exitItem);
        add(fileMenu);
    }

    private void buildEditMenu() {
        JMenu editMenu = new JMenu("Edit");
        editMenu.setMnemonic(KeyEvent.VK_E);
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
        JMenuItem moveDestItem = new JMenuItem("Move Destination...");
        moveDestItem.setMnemonic(KeyEvent.VK_D);
        moveDestItem.addActionListener(e -> MoveDestinationDialog.of(_viewer).showDialog());
        optionsMenu.add(moveDestItem);
        JMenuItem preferencesItem = new JMenuItem("Preferences...");
        preferencesItem.addActionListener(e -> PreferencesDialog.of(_viewer).showDialog());
        optionsMenu.add(preferencesItem);
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

    private void openImage() {
        JFileChooser chooser = new JFileChooser();
        Path dir = _configuration.getDirectory();
        if (dir != null) {
        	chooser.setCurrentDirectory(dir.toFile());
        }
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
        	Path path = chooser.getSelectedFile().toPath();
            _configuration.setDirectory(path.getParent());
            _viewer.listPane().loadDirectoryFrom(path);
            _viewer.listPane().select(path);
        }
    }

}
