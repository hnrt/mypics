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
	private final RunnableList _onClear = new RunnableList();

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

	public void onClear(Runnable cb) {
		_onClear.add(cb);
	}

	public void clear() {
		int end = _list.size() - 1;
		if (end < 0) return;
		_list.clear();
		fireIntervalRemoved(this, 0, end);
		_onClear.invoke();
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
			clear();
			Files.list(directory).filter(path -> Files.isRegularFile(path)).forEach(path -> add(path));
			if (_list.size() > 0) fireIntervalAdded(this, 0, _list.size() - 1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private int add(Path path) {
		int n = _list.size();
		int i = 0;
		int j = n - 1;
		while (i <= j) {
			int m = (i + j) / 2;
			int d = _list.get(m).compareTo(path);
			if (d < 0) {
				i = m + 1;
			} else if (d < 0) {
				j = m - 1;
			} else {
				return -1;
			}
		}
		_list.add(i, path);
		return i;
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
		if (toBeRemoved.size() == 0) return;
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

	private int find(Path path) {
		int n = _list.size();
		int i = 0;
		int j = n - 1;
		while (i <= j) {
			int m = (i + j) / 2;
			int d = _list.get(m).compareTo(path);
			if (d < 0) {
				i = m + 1;
			} else if (d < 0) {
				j = m - 1;
			} else {
				return m;
			}
		}
		return -1;
	}

}
