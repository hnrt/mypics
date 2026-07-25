package com.hideakin.mypics.gui;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JSplitPane;

import static com.hideakin.mypics.Application.configuration;
import static com.hideakin.mypics.Application.inProcessing;

import com.hideakin.mypics.Configuration;

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
		_directoryListPane.setFilteringTextFieldVisibility(configuration.getDirectoryFilterVisibility());
		_fileListPane.setFilterTextFieldVisibility(true);
		_fileListPane.enableThumbnail(configuration.getFileListCellRenderer() != 0);
		addPropertyChangeListener("dividerLocation", e -> configuration.setListVerticalDividerLocation((int)e.getNewValue()));
	}

	public void onDirectoryChanged(Consumer<Path> callback) {
		_directoryListPane.onSelected(callback);
	}

	public void onFileSelected(Consumer<Path> callback) {
		_fileListPane.onFileSelected(callback);
	}

	public FileList fileList() {
		return _fileListPane.fileList();
	}

	public void loadFrom(Path directory) {
		loadFrom(directory, null);
	}

	public void loadFrom(Path directory, int selection) {
		loadFrom(directory, Integer.valueOf(selection));
	}

	public void loadFrom(Path directory, Object selection) {
		inProcessing.run(() -> {
			try {
				configuration.setDirectory(directory);
				_directoryListPane.loadFrom(directory);
				_fileListPane.loadFrom(directory, selection);
			} catch (Exception e) {
				e.printStackTrace();
			}
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

	public boolean getDirectoryFilterTextFieldVisibility() {
		return _directoryListPane.getFilteringTextFieldVisibility();
	}

	public void setDirectoryFilterTextFieldVisibility(boolean show) {
		_directoryListPane.setFilteringTextFieldVisibility(show);
		configuration.setDirectoryFilterVisibility(show);
	}

	public boolean toggleDirectoryFilterTextFieldVisibility() {
		setDirectoryFilterTextFieldVisibility(!getDirectoryFilterTextFieldVisibility());
		return getDirectoryFilterTextFieldVisibility();
	}

	public boolean toggleFileListThumbnail() {
		int value = configuration.getFileListCellRenderer() ^ 1;
		configuration.setFileListCellRenderer(value);
		boolean enabled = value != 0;
		_fileListPane.enableThumbnail(enabled);
		return enabled;
	}

	public void setDefaultSize() {
		configuration.setListVerticalDividerLocation(Configuration.DEFAULT_LIST_VERTICAL_DIVIDER_LOCATION);
		setDividerLocation(configuration.getListVerticalDividerLocation());
	}

}
