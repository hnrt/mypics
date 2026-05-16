package com.hideakin.mypics;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.DefaultListModel;

public class DirectoryListModel extends DefaultListModel<Path> {

	private static final long serialVersionUID = 4129619052128521865L;

	public static DirectoryListModel create(FileListModel fileModel) {
		return new DirectoryListModel(fileModel);
	}

	private final Configuration _configuration = Configuration.getInstance();
	private final FileListModel _fileListModel;
	private final List<Consumer<Path>> _onChanged = new ArrayList<>();

	private DirectoryListModel(FileListModel fileListModel) {
		super();
		_fileListModel = fileListModel;
	}

	public void onChanged(Consumer<Path> callback) {
		_onChanged.add(callback);
	}

	public FileListModel fileListModel() {
		return _fileListModel;
	}

	public void loadFrom(Path directory) {
		_configuration.setDirectory(directory);
		_fileListModel.clear();
		clear();
		if (directory.getParent() == null) {
			for (char c = 'A'; c <= 'Z'; c++) {
				Path path = Paths.get(String.format("%c:\\", c));
				if (Files.exists(path) && !path.equals(directory)) {
					addElement(path);
				}
			}
		} else {
			addElement(Paths.get(".."));
		}
		try {
			Files.list(directory).sorted().forEach((e) -> {
				if (Files.isDirectory(e)) {
					addElement(e);
				} else if (Files.isRegularFile(e)) {
					_fileListModel.addElement(e);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		for (Consumer<Path> cb : _onChanged) {
			cb.accept(directory);
		}
	}

}
