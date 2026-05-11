package com.hideakin.mypics;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Deque;

import javax.swing.DefaultListModel;

public class FileListModel extends DefaultListModel<Path> {

	private static final long serialVersionUID = 6092214785805156671L;

	private static class UndoRecord {

		public final Path source;
		public final Path target;
		public final int index;

		public UndoRecord(Path source, Path target, int index) {
			this.source = source;
			this.target = target;
			this.index = index;
		}

	}

	public static FileListModel create() {
		return new FileListModel();
	}

	private final Configuration _configuration = Configuration.getInstance();
	private long _canMoveLaterThan = 0L;
	private final Deque<UndoRecord> _undoes = new ArrayDeque<>();

	private FileListModel() {
		super();
	}

	@Override
	public void clear() {
		super.clear();
		_undoes.clear();
	}

	public void move(Path source, Path targetDirectory, int index) throws Exception {
		if (source != null && targetDirectory != null && _canMoveLaterThan < System.currentTimeMillis()) {
			Path target = Paths.get(targetDirectory.toString(), source.getFileName().toString());
			Files.move(source, target);
			_undoes.push(new UndoRecord(target, source, index));
			remove(index);
			_canMoveLaterThan = System.currentTimeMillis() + _configuration.getMoveFileInterval();
		}
	}

	public void undo() throws Exception {
		if (!_undoes.isEmpty() && _canMoveLaterThan < System.currentTimeMillis()) {
			UndoRecord record = _undoes.pop();
			Files.move(record.source, record.target);
			insertElementAt(record.target, record.index);
			_canMoveLaterThan = System.currentTimeMillis() + _configuration.getMoveFileInterval();
		}
	}

	public void clearUndoStack() {
		_undoes.clear();
	}

}
