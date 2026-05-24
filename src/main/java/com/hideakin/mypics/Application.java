package com.hideakin.mypics;

import javax.swing.SwingUtilities;

public class Application {

	public static final String ABOUT = "Image Viewer version %s\nCopyright \u00a9 2026 Hideaki Narita";
	public static final String VERSION = Application.class.getPackage().getImplementationVersion();

	public static final Configuration configuration = Configuration.getInstance();
	public static final MainFrame mainFrame = MainFrame.getInstance();

	public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> mainFrame.setVisible(true));
    }

}
