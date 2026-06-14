package com.hideakin.mypics.io;

import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;

public class Locker {

	public static Locker of(Path path) {
		return new Locker(path);
	}

	private Path _path;
	private RandomAccessFile _raf;
	private FileChannel _channel;
	private FileLock _lock;

	private Locker(Path path) {
		_path = Path.of(path.toString() + ".lock");
	}

	public boolean hold() {
		try {
			_raf = new RandomAccessFile(_path.toFile(), "rw");
			_channel = _raf.getChannel();
			_lock = _channel.tryLock();
			if (_lock != null) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		release();
		return false;
	}

	public void release() {
		try {
			boolean isDeleteRequired = _lock != null;
			if (_lock != null) {
				_lock.close();
				_lock = null;
			}
			if (_channel != null) {
				_channel.close();
				_channel = null;
			}
			if (_raf != null) {
				_raf.close();
				_raf = null;
			}
			if (isDeleteRequired) {
				Files.deleteIfExists(_path);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean haveLocked() {
		return _lock != null;
	}

}
