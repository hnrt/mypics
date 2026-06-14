package com.hideakin.mypics.util;

import java.nio.file.Path;

public interface Operation {

	Path source();
	Path target();
	void execute() throws Exception;
	void undo() throws Exception;

}
