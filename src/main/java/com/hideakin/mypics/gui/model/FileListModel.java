package com.hideakin.mypics.gui.model;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;

import com.hideakin.mypics.util.function.RunnableList;

public class FileListModel extends AbstractListModel<Path> {

	private static final long serialVersionUID = 6092214785805156671L;

	public static FileListModel create() {
		return new FileListModel();
	}

	private final List<Path> _list = new ArrayList<>();
	private final RunnableList _onCleared = new RunnableList();

	private FileListModel() {
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

	public void onCleared(Runnable cb) {
		_onCleared.add(cb);
	}

	public void clear() {
		int end = _list.size() - 1;
		if (end >= 0) {
			_list.clear();
			fireIntervalRemoved(this, 0, end);
		}
		_onCleared.invoke();
	}

	public Path get(int index) {
		return _list.get(index);
	}

	public void set(int index, Path path) {
		if (0 <= index && index < _list.size()) {
			_list.set(index, path);
			fireContentsChanged(this, index, index);
		}
	}

	public void loadFrom(Path directory) {
		try {
			int sizeBefore = _list.size();
			if (sizeBefore > 0) {
				_list.clear();
			}
			_onCleared.invoke();
			Files.list(directory).filter(path -> Files.isRegularFile(path)).forEach(path -> add(path));
			notify(sizeBefore);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void add(List<Path> paths) {
		for (Path path : paths) {
			int i = add(path);
			if (i >= 0) fireIntervalAdded(this, i, i);
		}
	}

	public void remove(List<Path> paths) {
		List<Integer> toBeRemoved = new ArrayList<>();
		for (Path path : paths) {
			int i = find(path);
			if (i >= 0) toBeRemoved.add(i);
		}
		if (toBeRemoved.size() == 0) {
			return;
		}
		toBeRemoved.sort(Integer::compareTo);
		int start = -1;
		int end = -1;
		for (int i = toBeRemoved.size() - 1; i >= 0; i--) {
			int next = toBeRemoved.get(i);
			_list.remove(next);
			if (end < 0) {
				end = next;
				start = next;
			} else if (next == start - 1) {
				start = next;
			} else {
				fireIntervalRemoved(this, start, end);
				end = next;
				start = next;
			}
		}
		fireIntervalRemoved(this, start, end);
	}

	public void copyFrom(FileListModel source) {
		copyFrom(source._list);
	}

	public void copyFrom(FileListModel source, String filterBy) {
		copyFrom(source._list.stream().filter(e -> e.getFileName().toString().toLowerCase().contains(filterBy)).toList());
	}

	protected void copyFrom(List<Path> source) {
		int sizeBefore = _list.size();
		_list.clear();
		_onCleared.invoke();
		_list.addAll(source);
		notify(sizeBefore);
	}

	protected int add(Path path) {
		int n = _list.size();
		int i = 0;
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

	protected int find(Path path) {
		int n = _list.size();
		int i = 0;
		int j = n - 1;
		while (i <= j) {
			int m = (i + j) / 2;
			int d = _list.get(m).compareTo(path);
			if (d < 0) {
				i = m + 1;
			} else if (d > 0) {
				j = m - 1;
			} else {
				return m;
			}
		}
		return -1;
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
