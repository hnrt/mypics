package com.hideakin.mypics;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;

public class UndoManager {

	public static final Path GARBASE_PATH = Paths.get(System.getProperty("user.home"), ".mypics.garbage");

	private static UndoManager _singleton = new UndoManager();

	public static UndoManager getInstance() {
		return _singleton;
	}

	private static class UndoRecord {

		public final Path source;
		public final Path target;

		public UndoRecord(Path source, Path target) {
			this.source = source;
			this.target = target;
		}

	}

	private final Configuration _configuration = Configuration.getInstance();
	private long _canMoveLaterThan = 0L;
	private final Deque<UndoRecord> _records = new ArrayDeque<>();

	private UndoManager() {
		if (!Files.isDirectory(GARBASE_PATH)) {
			try {
				Files.createDirectory(GARBASE_PATH);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public synchronized void clear() {
		while (!_records.isEmpty()) {
			UndoRecord record = _records.removeLast();
			if (record.source.getParent().equals(GARBASE_PATH)) {
				try {
					Files.deleteIfExists(record.source);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public synchronized Path move(Path source, Path target) throws Exception {
		if (source != null && target != null && _canMoveLaterThan < System.currentTimeMillis()) {
			Files.move(source, target);
			_records.push(new UndoRecord(target, source));
			_canMoveLaterThan = System.currentTimeMillis() + _configuration.getMoveFileInterval();
			return source;
		} else {
			return null;
		}
	}

	public synchronized Path remove(Path source) throws Exception {
		if (source != null && _canMoveLaterThan < System.currentTimeMillis()) {
			String uuid = UUID.randomUUID().toString();
			Path target = Paths.get(GARBASE_PATH.toString(), uuid);
			Files.move(source, target);
			_records.push(new UndoRecord(target, source));
			_canMoveLaterThan = System.currentTimeMillis() + _configuration.getMoveFileInterval();
			return source;
		} else {
			return null;
		}
	}

	public synchronized Path undo() throws Exception {
		if (!_records.isEmpty() && _canMoveLaterThan < System.currentTimeMillis()) {
			UndoRecord record = _records.pop();
			Files.move(record.source, record.target);
			_canMoveLaterThan = System.currentTimeMillis() + _configuration.getMoveFileInterval();
			return record.target;
		} else {
			return null;
		}
	}

}
