package com.hideakin.mypics.io;

import java.nio.file.FileAlreadyExistsException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Consumer;

import com.hideakin.mypics.Configuration;
import com.hideakin.mypics.util.MoveFileOperation;
import com.hideakin.mypics.util.Operation;
import com.hideakin.mypics.util.RemoveFileOperation;
import com.hideakin.mypics.util.OperationList;

public class FileManager {

	private static FileManager _singleton = new FileManager();

	public static FileManager getInstance() {
		return _singleton;
	}

	private final Configuration _configuration = Configuration.getInstance();
	private long _canMoveLaterThan = 0L;
	private final Deque<OperationList> _operations = new ArrayDeque<>();
	private final OperationList _trash = new OperationList();

	private FileManager() {
	}

	public synchronized int numberOfUndoes() {
		return _operations.size();
	}

	public synchronized void clearTrash() {
		for (Operation operation : _trash) {
			try {
				if (operation instanceof MoveFileOperation mf) {
					Files.deleteIfExists(mf.target());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		_trash.clear();
	}

	public synchronized void clear() {
		Path td = _configuration.getTrashDirectory();
		while (!_operations.isEmpty()) {
			OperationList operationList = _operations.removeLast();
			for (Operation operation : operationList) {
				if (td.equals(operation.target().getParent())) {
					_trash.add(operation);
				}
			}
		}
	}

	public synchronized Path move(Path source, Path targetDirectory, Consumer<Exception> cb) {
		if (source != null && targetDirectory != null && _canMoveLaterThan < System.currentTimeMillis()) {
			try {
				MoveFileOperation mf = MoveFileOperation.of(source, targetDirectory);
				mf.execute();
				_operations.push(new OperationList(mf));
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
				try {
					MoveFileOperation mf = MoveFileOperation.of(sourceFiles.get(0), targetDirectory);
					mf.execute();
					_operations.push(new OperationList(mf));
					_canMoveLaterThan = System.currentTimeMillis() + _configuration.getMoveFileInterval();
					processedList.add(sourceFiles.get(0));
				} catch (Exception e) {
					cb.accept(e);
				}
			} else if (sourceFiles.size() > 1) {
				OperationList list = new OperationList();
				_operations.push(list);
				for (Path source : sourceFiles) {
					try {
						MoveFileOperation mf = MoveFileOperation.of(source, targetDirectory);
						mf.execute();
						list.add(mf);
						_canMoveLaterThan = System.currentTimeMillis() + _configuration.getMoveFileInterval();
					} catch (Exception e) {
						cb.accept(e);
					}
				}
				list.stream().forEach(x -> processedList.add(x.source()));
			}
		}
		return processedList;
	}

	public synchronized Path remove(Path source, Consumer<Exception> cb) {
		if (source != null && _canMoveLaterThan < System.currentTimeMillis()) {
			try {
				RemoveFileOperation rf = RemoveFileOperation.of(source);
				rf.execute();
				_operations.push(new OperationList(rf));
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
				OperationList list = new OperationList();
				_operations.push(list);
				for (Path source : sourceFiles) {
					try {
						RemoveFileOperation rf = RemoveFileOperation.of(source);
						rf.execute();
						list.add(rf);
						_canMoveLaterThan = System.currentTimeMillis() + _configuration.getMoveFileInterval();
					} catch (Exception e) {
						cb.accept(e);
					}
				}
				list.stream().forEach(x -> processedList.add(x.source()));
			}
		}
		return processedList;
	}

	public synchronized Path rename(Path source, String fileName, Consumer<Exception> cb) {
		if (source != null && fileName != null && !fileName.equals(source.getFileName().toString())) {
			Path directory = source.getParent();
			Path target = directory.resolve(fileName);
			if (Files.exists(target)) {
				cb.accept(new FileAlreadyExistsException(target.toString()));
			} else {
				try {
					MoveFileOperation mf = MoveFileOperation.of(source, directory, fileName);
					mf.execute();
					_operations.push(new OperationList(mf));
					_canMoveLaterThan = System.currentTimeMillis() + _configuration.getMoveFileInterval();
					return target;
				} catch (Exception e) {
					cb.accept(e);
				}
			}
		}
		return null;
	}

	public synchronized List<Path> undo(Consumer<Exception> cb) {
		List<Path> sources = new ArrayList<>();
		if (!_operations.isEmpty() && _canMoveLaterThan < System.currentTimeMillis()) {
			OperationList list = _operations.pop();
			for (Operation operation : list) {
				try {
					operation.undo();
					sources.add(operation.source());
					_canMoveLaterThan = System.currentTimeMillis() + _configuration.getMoveFileInterval();
				} catch (Exception e) {
					cb.accept(e);
				}
			}
		}
		return sources;
	}

	public synchronized List<Path> trash() {
		Path td = _configuration.getTrashDirectory();
		List<Path> t = new ArrayList<>();
		_operations
			.stream()
			.forEach(list -> {
				for (Operation operation : list) {
					if (td.equals(operation.target().getParent())) {
						t.add(operation.source());
					}
				}
			});
		_trash
			.stream()
			.forEach(operation -> {
				t.add(operation.source());
			});
		return t;
	}

	public synchronized Path restore(Path path) throws Exception {
		OperationList found = _operations.stream().filter(list -> {
			for (Operation operation : list) {
				if (operation.source().equals(path)) {
					return true;
				}
			}
			return false;
		}).findFirst().orElse(null);
		if (found == null) {
			return null;
		}
		Operation operation = found.stream().filter(x -> x.source().equals(path)).findFirst().orElse(null);
		found.remove(operation);
		if (found.size() == 0) {
			_operations.remove(found);
		}
		operation.undo();
		_canMoveLaterThan = System.currentTimeMillis() + _configuration.getMoveFileInterval();
		return operation.source();
	}

}
