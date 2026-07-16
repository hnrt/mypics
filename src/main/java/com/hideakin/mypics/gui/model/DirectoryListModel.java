package com.hideakin.mypics.gui.model;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;

public class DirectoryListModel extends DefaultListModel<Path> {

	private static final long serialVersionUID = 4129619052128521865L;

	public static DirectoryListModel create() {
		return new DirectoryListModel();
	}

	private static final Path DOTDOT = Path.of("..");

	private int _indexStart;

	private DirectoryListModel() {
		super();
	}

	public boolean isParentDirectory(Path directory) {
		return DOTDOT.equals(directory);
	}

	public void loadFrom(Path directory) {
		try {
			clear();
			List<Path> list = getPreamble(directory);
			Files.list(directory).filter(path -> Files.isDirectory(path)).forEach(path -> add(list, path));
			addAll(list);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void loadFrom(Path directory, String filterBy) {
		try {
			clear();
			List<Path> list = getPreamble(directory);
			Files.list(directory).filter(path -> Files.isDirectory(path) && contains(path, filterBy)).forEach(path -> add(list, path));
			addAll(list);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static boolean contains(Path path, String filterBy) {
		return path.getFileName().toString().toLowerCase().contains(filterBy);
	}

	private List<Path> getPreamble(Path directory) {
		List<Path> list = new ArrayList<>();
		if (directory.getParent() == null) {
			for (Path root : FileSystems.getDefault().getRootDirectories()) {
				if (root.equals(directory)) continue;
				list.add(root);
			}
		} else {
			list.add(DOTDOT);
		}
		_indexStart = list.size();
		return list;
	}

	private void add(List<Path> list, Path path) {
		int n = list.size();
		int i = _indexStart;
		int j = n - 1;
		while (i <= j) {
			int m = (i + j) / 2;
			int d = list.get(m).compareTo(path);
			if (d < 0) {
				i = m + 1;
			} else if (d > 0) {
				j = m - 1;
			} else {
				return;
			}
		}
		list.add(i, path);
	}

	public void add(Path path) {
		int n = getSize();
		int i = _indexStart;
		int j = n - 1;
		while (i <= j) {
			int m = (i + j) / 2;
			int d = get(m).compareTo(path);
			if (d < 0) {
				i = m + 1;
			} else if (d > 0) {
				j = m - 1;
			} else {
				return;
			}
		}
		add(i, path);
	}

}
