package com.hideakin.mypics;

import java.nio.file.Path;
import java.nio.file.Paths;
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

	public Path move(Path source, Path targetDirectory) throws Exception {
		Path target = Paths.get(targetDirectory.toString(), source.getFileName().toString());
		Path processed = _undoManager.move(source, target);
		if (processed != null) {
			removeElement(processed);
		}
		return processed;
	}

	public Path remove(Path source) throws Exception {
		Path processed = _undoManager.remove(source);
		if (processed != null) {
			removeElement(processed);
		}
		return processed;
	}

	public Path undo() throws Exception {
		Path processed = _undoManager.undo();
		if (processed != null) {
			String fileName = processed.getFileName().toString();
			boolean inserted = false;
			int n = getSize();
			for (int i = 0; i < n; i++) {
				Path next = getElementAt(i);
				if (next != null && next.getFileName().toString().compareTo(fileName) > 0) {
					insertElementAt(processed, i);
					inserted = true;
					break;
				}
			}
			if (!inserted) {
				insertElementAt(processed, n);
			}
		}
		return processed;
	}

}
