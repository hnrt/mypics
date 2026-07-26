package com.hideakin.mypics.gui;

import java.nio.file.Path;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import javax.swing.JSplitPane;

import static com.hideakin.mypics.Application.configuration;
import static com.hideakin.mypics.Application.inProcessing;
import static com.hideakin.mypics.Configuration.DEFAULT_LIST_VERTICAL_DIVIDER_LOCATION;

public class ListPane extends JSplitPane {

	private static final long serialVersionUID = -8809183505899944015L;

	public static ListPane create() {
		return new ListPane();
	}

	private final DirectoryListPane _directoryListPane = DirectoryListPane.create();
	private final FileListPane _fileListPane = FileListPane.create();

	private ListPane() {
		super(JSplitPane.VERTICAL_SPLIT);
		setTopComponent(_directoryListPane);
		setBottomComponent(_fileListPane);
		setDividerLocation(configuration.getListVerticalDividerLocation());
		_directoryListPane.setFilteringTextFieldVisibility(configuration.getDirectoryFilteringVisibility());
		_fileListPane.setFilteringTextFieldVisibility(configuration.getFileFilteringVisibility());
		_fileListPane.enableThumbnail(configuration.getThumbnailEnabled());
		addPropertyChangeListener("dividerLocation", e -> configuration.setListVerticalDividerLocation((int)e.getNewValue()));
	}

	public void onDirectorySelected(Consumer<Path> callback) {
		_directoryListPane.onSelected(callback);
	}

	public void onFileCleared(Runnable callback) {
		_fileListPane.onCleared(callback);
	}

	public void onFileSelected(Consumer<Path> callback) {
		_fileListPane.onSelected(callback);
	}

	public Path directory() {
		return _directoryListPane.directory();
	}

	public int numberOfFiles() {
		return _fileListPane.numberOfFiles();
	}

	public void loadDirectoryFrom(Path directory) {
		loadDirectoryFrom(directory, null);
	}

	public void loadDirectoryFrom(Path directory, int selection) {
		loadDirectoryFrom(directory, Integer.valueOf(selection));
	}

	public void loadDirectoryFrom(Path directory, Object selection) {
		inProcessing.run(() -> {
			configuration.setDirectory(directory);
			_directoryListPane.loadFrom(directory);
			_fileListPane.loadFrom(directory, selection);
		});
	}

	public void addFiles(List<Path> paths) {
		_fileListPane.addFiles(paths);
	}

	public void removeFiles(List<Path> paths) {
		_fileListPane.removeFiles(paths);
	}

	public Path getSelectedFile() {
		return _fileListPane.getSelectedFile();
	}

	public List<Path> getSelectedFiles() {
		return _fileListPane.getSelectedFiles();
	}

	public boolean getDirectoryFilteringTextFieldVisibility() {
		return _directoryListPane.getFilteringTextFieldVisibility();
	}

	public boolean toggleDirectoryFilteringTextFieldVisibility() {
		return _directoryListPane.toggleFilteringTextFieldVisibility();
	}

	public boolean getFileFilteringTextFieldVisibility() {
		return _fileListPane.getFilteringTextFieldVisibility();
	}

	public boolean toggleFileFilteringTextFieldVisibility() {
		return _fileListPane.toggleFilteringTextFieldVisibility();
	}

	public boolean toggleFileListThumbnail() {
		configuration.setThumbnailEnabled(!configuration.getThumbnailEnabled());
		_fileListPane.enableThumbnail(configuration.getThumbnailEnabled());
		return configuration.getThumbnailEnabled();
	}

	public void setDefaultSize() {
		configuration.setListVerticalDividerLocation(DEFAULT_LIST_VERTICAL_DIVIDER_LOCATION);
		setDividerLocation(configuration.getListVerticalDividerLocation());
	}

	public void startFileRenaming(BiFunction<Path, String, Path> callback) {
		_fileListPane.startRenaming(callback);
	}

}
