package com.hideakin.mypics;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.DefaultListModel;

public class DirectoryListModel extends DefaultListModel<Path> {

	private static final long serialVersionUID = 4129619052128521865L;

	public static DirectoryListModel create() {
		return new DirectoryListModel();
	}

	private static final Path DOTDOT = Paths.get("..");

	private DirectoryListModel() {
		super();
	}

	public void addParentDirectory(Path directory) {
		if (directory.getParent() == null) {
			for (char c = 'A'; c <= 'Z'; c++) {
				Path path = Paths.get(String.format("%c:\\", c));
				if (Files.exists(path) && !path.equals(directory)) {
					addElement(path);
				}
			}
		} else {
			addElement(DOTDOT);
		}
	}

	public boolean isParentDirectory(Path directory) {
		return DOTDOT.equals(directory);
	}

}
