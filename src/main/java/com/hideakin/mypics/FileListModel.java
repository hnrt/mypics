package com.hideakin.mypics;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.DefaultListModel;

public class FileListModel extends DefaultListModel<Path> {

	private static final long serialVersionUID = 6092214785805156671L;

	public static FileListModel create() {
		return new FileListModel();
	}

	private final UndoManager _undoManager = UndoManager.getInstance();

	private FileListModel() {
		super();
	}

	@Override
	public void clear() {
		super.clear();
		_undoManager.clear();
	}

	public Path move(Path source, Path targetDirectory, Consumer<Exception> cb) {
		Path processed = _undoManager.move(source, targetDirectory, cb);
		if (processed != null) {
			removeElement(processed);
		}
		return processed;
	}

	public List<Path> move(List<Path> sourceFiles, Path targetDirectory, Consumer<Exception> cb) {
		List<Path> processed = _undoManager.move(sourceFiles, targetDirectory, cb);
		if (processed != null) {
			for (Path source : processed) {
				removeElement(source);
			}
		}
		return processed;
	}

	public Path remove(Path source, Consumer<Exception> cb) {
		Path processed = _undoManager.remove(source, cb);
		if (processed != null) {
			removeElement(processed);
		}
		return processed;
	}

	public List<Path> remove(List<Path> sourceFiles, Consumer<Exception> cb) {
		List<Path> processed = _undoManager.remove(sourceFiles, cb);
		if (processed != null) {
			for (Path source : processed) {
				removeElement(source);
			}
		}
		return processed;
	}

	public List<Path> undo(Consumer<Exception> cb) {
		List<Path> processed = _undoManager.undo(cb);
		if (processed.size() > 1) {
			processed = processed.stream().sorted().toList();
		}
		int i = 0;
		int j = 0;
		int m = processed.size();
		int n = getSize();
		Path s = i < m ? processed.get(i) : null;
		Path t = j < n ? elementAt(j) : null;
		while (s != null && t != null) {
			int d = s.compareTo(t);
			if (d < 0) {
				insertElementAt(s, j++);
				s = ++i < m ? processed.get(i) : null;
			} else if (d > 0) {
				t = ++j < n ? elementAt(j) : null;
			} else {
				s = ++i < m ? processed.get(i) : null;
				t = ++j < n ? elementAt(j) : null;
			}
		}
		while (s != null) {
			addElement(s);
			s = ++i < m ? processed.get(i) : null;
		}
		return processed;
	}

}
