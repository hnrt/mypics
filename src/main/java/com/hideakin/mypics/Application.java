package com.hideakin.mypics;

import java.nio.file.Path;
import java.util.Locale;

import javax.swing.SwingUtilities;

public class Application {

	public static final String ABOUT = "Image Viewer version %s\nCopyright \u00a9 2026 Hideaki Narita";
	public static final String VERSION = Application.class.getPackage().getImplementationVersion();

	private static int _debugLevel = 0;

	static {
		Locale.setDefault(Locale.ENGLISH);
	}

	public static final Configuration configuration = Configuration.getInstance();
	public static final MainFrame mainFrame = MainFrame.getInstance();

	public static void main(String[] args) {
		Path path = null;
		for (int i = 0; i < args.length; i++) {
			if ("-h".equals(args[i])) {
				help();
				System.exit(0);
			} else if ("-D".equals(args[i])) {
				_debugLevel++;
			} else if (args[i].startsWith("-")) {
				System.err.printf("Unknown option: %s\nTry -h for usage. Thank you.\n", args[i]);
				System.exit(1);
			} else if (path == null) {
				path = Path.of(args[i]);
			} else {
				System.err.printf("Too many paths. Only one path is taken.\n");
				System.exit(1);
			}
		}
		configuration.debug();
		mainFrame.setPathToOpen(path);
		SwingUtilities.invokeLater(() -> mainFrame.setVisible(true));
    }

	private static void help() {
		System.out.printf(ABOUT.replace("\u00a9", "(c)"), VERSION);
		System.out.printf("\n\nSyntax:\n");
		System.out.printf("  java -jar mypics.jar [options] [path]\n");
		System.out.printf("Options:\n");
		System.out.printf("  -D  increases the debugging level.\n");
		System.out.printf("  -h  prints this message.\n");
		System.out.printf("Path:\n");
		System.out.printf("  Regular file or directory you wish to open.\n");
	}

	public static void debug(int level, String format, Object... args) {
		if (level <= _debugLevel) {
			String line = String.format(format, args);
			if (line.contains("\n")) {
				line = line.replaceAll("\n", "\n# ");
			}
			System.err.printf("# %s\n", line);
		}
	}

}
