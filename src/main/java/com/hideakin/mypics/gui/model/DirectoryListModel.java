package com.hideakin.mypics.gui.model;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;

public class DirectoryListModel extends AbstractListModel<Path> {

	private static final long serialVersionUID = 4129619052128521865L;

	public static DirectoryListModel create() {
		return new DirectoryListModel();
	}

	protected static final Path DOTDOT = Path.of("..");

	protected final List<Path> _list = new ArrayList<>();
	protected int _indexStart = 0;

	protected DirectoryListModel() {
		super();
	}

	@Override
	public int getSize() {
		return _list.size();
	}

	@Override
	public Path getElementAt(int index) {
		return _list.get(index);
	}

	public boolean isParentDirectory(Path directory) {
		return DOTDOT.equals(directory);
	}

	public void clear() {
		int end = _list.size() - 1;
		if (end < 0) {
			return;
		}
		_list.clear();
		_indexStart = 0;
		fireIntervalRemoved(this, 0, end);
	}

	public void loadFrom(Path directory) {
		try {
			int sizeBefore = _list.size();
			setPreamble(directory);
			Files.list(directory).filter(path -> Files.isDirectory(path)).forEach(path -> addPath(path));
			notify(sizeBefore);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void add(Path path) {
		int i = addPath(path);
		if (i >= 0) {
			fireIntervalAdded(this, i, i);
		}
	}

	public void copyFrom(DirectoryListModel source) {
		copyFrom(source._list);
	}

	public void copyFrom(DirectoryListModel source, String filterBy) {
		copyFrom(source._list.stream().filter(e -> e.getFileName() != null && e.getFileName().toString().toLowerCase().contains(filterBy)).toList());
	}

	protected void copyFrom(List<Path> source) {
		int sizeBefore = _list.size();
		_list.clear();
		_indexStart = 0;
		_list.addAll(source);
		notify(sizeBefore);
	}

	protected void setPreamble(Path directory) {
		_list.clear();
		if (directory.getParent() == null) {
			for (Path root : FileSystems.getDefault().getRootDirectories()) {
				if (root.equals(directory)) continue;
				_list.add(root);
			}
		} else {
			_list.add(DOTDOT);
		}
		_indexStart = _list.size();
	}

	protected int addPath(Path path) {
		int n = _list.size();
		int i = _indexStart;
		int j = n - 1;
		while (i <= j) {
			int m = (i + j) / 2;
			int d = _list.get(m).compareTo(path);
			if (d < 0) {
				i = m + 1;
			} else if (d > 0) {
				j = m - 1;
			} else {
				return -1;
			}
		}
		_list.add(i, path);
		return i;
	}

	protected void notify(int sizeBefore) {
		int sizeAfter = _list.size();
		if (sizeAfter > 0) {
			if (sizeBefore > 0) {
				if (sizeBefore < sizeAfter) {
					fireIntervalRemoved(this, sizeBefore, sizeAfter - 1);
					fireContentsChanged(this, 0, sizeBefore - 1);
				} else if (sizeAfter < sizeBefore) {
					fireIntervalRemoved(this, sizeAfter, sizeBefore - 1);
					fireContentsChanged(this, 0, sizeAfter - 1);
				} else {
					fireContentsChanged(this, 0, sizeAfter - 1);
				}
			} else {
				fireIntervalAdded(this, 0, sizeAfter - 1);
			}
		} else if (sizeBefore > 0) {
			fireIntervalRemoved(this, 0, sizeBefore - 1);
		}
	}

}
