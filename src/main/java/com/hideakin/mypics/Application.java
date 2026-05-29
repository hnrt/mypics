package com.hideakin.mypics;

import java.nio.file.Path;
import java.util.Locale;

import javax.swing.SwingUtilities;

public class Application {

	public static final String ABOUT = "Image Viewer version %s\nCopyright \u00a9 2026 Hideaki Narita";
	public static final String VERSION = Application.class.getPackage().getImplementationVersion();

	static {
		Locale.setDefault(Locale.ENGLISH);
	}

	public static final Configuration configuration = Configuration.getInstance();
	public static final MainFrame mainFrame = MainFrame.getInstance();

	public static void main(String[] args) {
		Path path = null;
		for (int i = 0; i < args.length; i++) {
			if (path == null) {
				path = Path.of(args[i]);
			} else {
				System.err.printf("Too many arguments.\n");
				System.exit(1);
			}
		}
		mainFrame.setPathToOpen(path);
		SwingUtilities.invokeLater(() -> mainFrame.setVisible(true));
    }

}
