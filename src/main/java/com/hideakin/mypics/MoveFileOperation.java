package com.hideakin.mypics;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.UUID;

public class MoveFileOperation implements Operation {

	public static MoveFileOperation of(Path source, Path targetDirectory) {
		return new MoveFileOperation(source, targetDirectory);
	}

	private Path _source;
	private Path _target;

	private MoveFileOperation(Path source, Path targetDirectory) {
		_source = source;
		_target = targetDirectory.resolve(source.getFileName());
	}

	@Override
	public Path source() {
		return _source;
	}

	@Override
	public Path target() {
		return _target;
	}

	@Override
	public void execute() throws Exception {
		if (Files.exists(_target)) {
			if (exactMatch()) {
				String uuid = UUID.randomUUID().toString();
				_target = Application.configuration.getTrashDirectory().resolve(uuid);
			} else {
				Path parent = _target.getParent();
				String original = _target.getFileName().toString();
				int offset = original.lastIndexOf('.');
				if (offset == -1) {
					for (int i = 2; Files.exists(_target); i++) {
						_target = parent.resolve(String.format("%s-%d", original, i));
					}
				} else {
					String first = original.substring(0, offset);
					String second = original.substring(offset);
					for (int i = 2; Files.exists(_target); i++) {
						_target = parent.resolve(String.format("%s-%d%s", first, i, second));
					}
				}
			}
		}
		if (!Files.isDirectory(_target.getParent())) {
			Files.createDirectories(_target.getParent());
		}
		Files.move(_source, _target);
	}

	@Override
	public void undo() throws Exception {
		Files.move(_target, _source);
	}

	private boolean exactMatch() throws Exception {
		long sourceSize = Files.size(_source);
		long targetSize = Files.size(_target);
		if (sourceSize != targetSize) {
			return false;
		} else if (sourceSize == 0) {
			return true;
		}
		byte[] bufSource = new byte[65536];
		byte[] bufTarget = new byte[65536];
		int offSource = 0;
		int offTarget = 0;
		try (
			InputStream inSource = Files.newInputStream(_source, StandardOpenOption.READ);
			InputStream inTarget = Files.newInputStream(_target, StandardOpenOption.READ)
			) {
			while (true) {
				int lenSource = inSource.read(bufSource, offSource, bufSource.length - offSource);
				while (lenSource > -1) {
					offSource += lenSource;
					if (offSource < bufSource.length) {
						lenSource = inSource.read(bufSource, offSource, bufSource.length - offSource);
					} else if (offSource == bufSource.length) {
						break;
					} else {
						throw new RuntimeException(String.format("read malfunctioned with %s", _source));
					}
				}
				int lenTarget = inTarget.read(bufTarget, offTarget, bufTarget.length - offTarget);
				while (lenTarget > -1) {
					offTarget += lenTarget;
					if (offTarget < bufTarget.length) {
						lenTarget = inTarget.read(bufTarget, offTarget, bufTarget.length - offTarget);
					} else if (offTarget == bufTarget.length) {
						break;
					} else {
						throw new RuntimeException(String.format("read malfunctioned with %s", _target));
					}
				}
				if (Arrays.mismatch(bufSource, 0, offSource, bufTarget, 0, offTarget) != -1) {
					return false;
				} else if (lenSource == -1 && lenTarget == -1) {
					return true;
				} else if (lenSource == -1 || lenTarget == -1) {
					return false;
				}
				offSource = 0;
				offTarget = 0;
			}
		}
	}

}
