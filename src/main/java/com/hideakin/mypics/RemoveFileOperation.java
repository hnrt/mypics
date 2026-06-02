package com.hideakin.mypics;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class RemoveFileOperation implements Operation {

	public static RemoveFileOperation of(Path source) {
		return new RemoveFileOperation(source);
	}

	private Path _source;
	private Path _target;

	
	private RemoveFileOperation(Path source) {
		_source = source;
		_target = Application.configuration.getTrashDirectory().resolve(UUID.randomUUID().toString());
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
		if (!Files.isDirectory(_target.getParent())) {
			Files.createDirectories(_target.getParent());
		}
		Files.move(_source, _target);
	}

	@Override
	public void undo() throws Exception {
		Files.move(_target, _source);
	}

}
