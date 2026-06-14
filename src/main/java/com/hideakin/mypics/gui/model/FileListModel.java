package com.hideakin.mypics.gui.model;

import java.nio.file.Path;
import java.util.List;
import javax.swing.DefaultListModel;

import com.hideakin.mypics.util.function.RunnableList;

public class FileListModel extends DefaultListModel<Path> {

	private static final long serialVersionUID = 6092214785805156671L;

	public static FileListModel create() {
		return new FileListModel();
	}

	private final RunnableList _onClear = new RunnableList();

	private FileListModel() {
		super();
	}

	public void onClear(Runnable cb) {
		_onClear.add(cb);
	}

	@Override
	public void clear() {
		super.clear();
		_onClear.invoke();
	}

	public void removeElements(List<Path> paths) {
		for (Path path : paths) {
			removeElement(path);
		}
	}

	public void addElements(List<Path> paths) {
		if (paths.size() > 1) {
			paths = paths.stream().sorted().toList();
		}
		int i = 0;
		int j = 0;
		int m = paths.size();
		int n = getSize();
		Path s = i < m ? paths.get(i) : null;
		Path t = j < n ? elementAt(j) : null;
		while (s != null && t != null) {
			int d = s.compareTo(t);
			if (d < 0) {
				insertElementAt(s, j++);
				s = ++i < m ? paths.get(i) : null;
			} else if (d > 0) {
				t = ++j < n ? elementAt(j) : null;
			} else {
				s = ++i < m ? paths.get(i) : null;
				t = ++j < n ? elementAt(j) : null;
			}
		}
		while (s != null) {
			addElement(s);
			s = ++i < m ? paths.get(i) : null;
		}
	}

}
