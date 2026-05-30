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
import java.util.function.Consumer;

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
			return TRASH_PATH.equals(source.getParent());
		}

	}

	private static class UndoList extends ArrayList<UndoRecord> {

		private static final long serialVersionUID = -7280100457159837054L;

		public UndoList() {
			super();
		}

	}

	private final Configuration _configuration = Configuration.getInstance();
	private long _canMoveLaterThan = 0L;
	private final Deque<Object> _records = new ArrayDeque<>();
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

	public synchronized int numberOfUndoes() {
		return _records.size();
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
			Object obj = _records.removeLast();
			if (obj instanceof UndoRecord record) {
				if (record.inTrash()) {
					_trash.add(record);
				}
			} else if (obj instanceof UndoList list) {
				for (UndoRecord record : list) {
					if (record.inTrash()) {
						_trash.add(record);
					}
				}
			}
		}
	}

	public synchronized Path move(Path source, Path targetDirectory, Consumer<Exception> cb) {
		if (source != null && targetDirectory != null && _canMoveLaterThan < System.currentTimeMillis()) {
			try {
				Path target = targetDirectory.resolve(source.getFileName());
				Files.move(source, target);
				_records.push(new UndoRecord(target, source));
				_canMoveLaterThan = System.currentTimeMillis() + _configuration.getMoveFileInterval();
				return source;
			} catch (Exception e) {
				cb.accept(e);
			}
		}
		return null;
	}

	public synchronized List<Path> move(List<Path> sourceFiles, Path targetDirectory, Consumer<Exception> cb) {
		List<Path> processedList = new ArrayList<>();
		if (sourceFiles != null && targetDirectory != null) {
			if (sourceFiles.size() == 1) {
				Path processed = move(sourceFiles.get(0), targetDirectory, cb);
				if (processed != null) {
					processedList.add(processed);
				}
			} else if (sourceFiles.size() > 1) {
				UndoList list = new UndoList();
				_records.push(list);
				for (Path source : sourceFiles) {
					try {
						Path target = targetDirectory.resolve(source.getFileName());
						Files.move(source, target);
						list.add(new UndoRecord(target, source));
						_canMoveLaterThan = System.currentTimeMillis() + _configuration.getMoveFileInterval();
					} catch (Exception e) {
						cb.accept(e);
					}
				}
				list.stream().forEach(r -> processedList.add(r.target));
			}
		}
		return processedList;
	}

	public synchronized Path remove(Path source, Consumer<Exception> cb) {
		if (source != null && _canMoveLaterThan < System.currentTimeMillis()) {
			try {
				String uuid = UUID.randomUUID().toString();
				Path target = TRASH_PATH.resolve(uuid);
				Files.move(source, target);
				_records.push(new UndoRecord(target, source));
				_canMoveLaterThan = System.currentTimeMillis() + _configuration.getMoveFileInterval();
				return source;
			} catch (Exception e) {
				cb.accept(e);
			}
		}
		return null;
	}

	public synchronized List<Path> remove(List<Path> sourceFiles, Consumer<Exception> cb) {
		List<Path> processedList = new ArrayList<>();
		if (sourceFiles != null) {
			if (sourceFiles.size() == 1) {
				Path processed = remove(sourceFiles.get(0), cb);
				if (processed != null) {
					processedList.add(processed);
				}
			} else if (sourceFiles.size() > 1) {
				UndoList list = new UndoList();
				_records.push(list);
				for (Path source : sourceFiles) {
					try {
						String uuid = UUID.randomUUID().toString();
						Path target = TRASH_PATH.resolve(uuid);
						Files.move(source, target);
						list.add(new UndoRecord(target, source));
						_canMoveLaterThan = System.currentTimeMillis() + _configuration.getMoveFileInterval();
					} catch (Exception e) {
						cb.accept(e);
					}
				}
				list.stream().forEach(r -> processedList.add(r.target));
			}
		}
		return processedList;
	}

	public synchronized List<Path> undo(Consumer<Exception> cb) {
		List<Path> targets = new ArrayList<>();
		if (!_records.isEmpty() && _canMoveLaterThan < System.currentTimeMillis()) {
			Object obj = _records.pop();
			if (obj instanceof UndoRecord record) {
				try {
					Files.move(record.source, record.target);
					targets.add(record.target);
					_canMoveLaterThan = System.currentTimeMillis() + _configuration.getMoveFileInterval();
				} catch (Exception e) {
					cb.accept(e);
				}
			} else if (obj instanceof UndoList list) {
				for (UndoRecord record : list) {
					try {
						Files.move(record.source, record.target);
						targets.add(record.target);
						_canMoveLaterThan = System.currentTimeMillis() + _configuration.getMoveFileInterval();
					} catch (Exception e) {
						cb.accept(e);
					}
				}
			}
		}
		return targets;
	}

	public synchronized List<Path> trash() {
		List<Path> t = new ArrayList<>();
		_records
			.stream()
			.forEach(obj -> {
				if (obj instanceof UndoRecord record) {
					if (record.inTrash()) {
						t.add(record.target);
					}
				} else if (obj instanceof UndoList list) {
					for (UndoRecord record : list) {
						if (record.inTrash()) {
							t.add(record.target);
						}
					}
				}
			});
		_trash
			.stream()
			.forEach(record -> {
				t.add(record.target);
			});
		return t;
	}

	public synchronized Path restore(Path path) throws Exception {
		Object obj = _records.stream().filter(x -> {
			if (x instanceof UndoRecord record) {
				return record.target.equals(path);
			} else if (x instanceof UndoList list) {
				for (UndoRecord record : list) {
					if (record.target.equals(path)) {
						return true;
					}
				}
			}
			return false;
		}).findFirst().orElse(null);
		if (obj instanceof UndoRecord record) {
			_records.remove(record);
			Files.move(record.source, record.target);
			_canMoveLaterThan = System.currentTimeMillis() + _configuration.getMoveFileInterval();
			return record.target;
		} else if (obj instanceof UndoList list) {
			UndoRecord record = list.stream().filter(r -> r.target.equals(path)).findFirst().orElse(null);
			list.remove(record);
			if (list.size() == 0) {
				_records.remove(list);
			}
			Files.move(record.source, record.target);
			_canMoveLaterThan = System.currentTimeMillis() + _configuration.getMoveFileInterval();
			return record.target;
		} else {
			UndoRecord record = _trash.stream().filter(r -> r.target.equals(path)).findFirst().orElse(null);
			if (record != null) {
				_trash.remove(record);
				Files.move(record.source, record.target);
				_canMoveLaterThan = System.currentTimeMillis() + _configuration.getMoveFileInterval();
				return record.target;
			}
		}
		return null;
	}

}
