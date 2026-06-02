package com.hideakin.mypics;

import java.nio.file.Path;

public interface Operation {

	Path source();
	Path target();
	void execute() throws Exception;
	void undo() throws Exception;

}
