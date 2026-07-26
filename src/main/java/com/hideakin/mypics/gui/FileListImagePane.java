package com.hideakin.mypics.gui;

import static com.hideakin.mypics.Application.configuration;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import com.hideakin.mypics.util.function.ConsumerList;

public class FileListImagePane extends JSplitPane {

	private static final long serialVersionUID = 6080536333060614921L;

	public static FileListImagePane create() {
		return new FileListImagePane();
	}

	protected final FileListPane _fileListPane = FileListPane.create();
	protected final MultiImagePane _multiImagePane = MultiImagePane.create();
	protected final ImagePane _imagePane = ImagePane.create();
	protected final ConsumerList<Path> _onSelected = new ConsumerList<>();

	protected FileListImagePane() {
		super(JSplitPane.HORIZONTAL_SPLIT);
		setLeftComponent(_fileListPane);
		setRightComponent(_multiImagePane);
		setDividerLocation(400);
		_fileListPane.setFilteringTextFieldVisibility(configuration.getFileFilteringVisibility());
		_fileListPane.enableThumbnail(configuration.getThumbnailEnabled());
		_fileListPane.onSelected(path -> _onSelected.invoke(path));
	}

	public void onSelected(Consumer<Path> callback) {
		_onSelected.add(callback);
	}

	public void loadFrom(Path directory) {
		_fileListPane.loadFrom(directory, null);
		select(null);
	}

	public void setFiles(List<Path> paths) {
		_fileListPane.setFiles(paths);
		select(null);
	}

	public void deselect() {
		select(null);
	}

	public void select(Path path) {
		if (path != null) {
			_imagePane.setVisible(true);
			setRightComponent(_imagePane);
			_multiImagePane.setVisible(false);
			_fileListPane.select(path);
			SwingUtilities.invokeLater(() -> _imagePane.loadFrom(path));
		} else {
			_multiImagePane.setVisible(true);
			setRightComponent(_multiImagePane);
			_imagePane.setVisible(false);
			_fileListPane.deselect();
			SwingUtilities.invokeLater(() -> _multiImagePane.loadFrom(_fileListPane.get()));
		}
	}

}
