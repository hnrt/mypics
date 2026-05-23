package com.hideakin.mypics;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.UUID;

public class UndoManager {

	public static final Path TRASH_PATH = Paths.get(System.getProperty("user.home"), ".mypics.trash");

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

		public boolean inTrash() {
			return source.getParent().equals(TRASH_PATH);
		}

	}

	private final Configuration _configuration = Configuration.getInstance();
	private long _canMoveLaterThan = 0L;
	private final Deque<UndoRecord> _records = new ArrayDeque<>();
	private final List<UndoRecord> _trash = new ArrayList<>();

	private UndoManager() {
		if (!Files.isDirectory(TRASH_PATH)) {
			try {
				Files.createDirectory(TRASH_PATH);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public synchronized void clearTrash() {
		for (UndoRecord record : _trash) {
			try {
				Files.deleteIfExists(record.source);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		_trash.clear();
	}

	public synchronized void clear() {
		while (!_records.isEmpty()) {
			UndoRecord record = _records.removeLast();
			if (record.inTrash()) {
				_trash.add(record);
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
			Path target = Paths.get(TRASH_PATH.toString(), uuid);
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

	public synchronized List<Path> trash() {
		List<Path> list = new ArrayList<>();
		_records
			.stream()
			.forEach(r -> {
				if (r.inTrash()) {
					list.add(r.target);
				}
			});
		list.addAll(_trash.stream().map(r -> r.target).toList());
		return list;
	}

	public synchronized Path restore(Path path) throws Exception {
		UndoRecord record = _records.stream().filter(r -> r.target.equals(path)).findFirst().orElse(null);
		if (record != null) {
			_records.remove(record);
		} else {
			record = _trash.stream().filter(r -> r.target.equals(path)).findFirst().orElse(null);
			if (record != null) {
				_trash.remove(record);
			} else {
				return null;
			}
		}
		Files.move(record.source, record.target);
		_canMoveLaterThan = System.currentTimeMillis() + _configuration.getMoveFileInterval();
		return record.target;
	}

}
