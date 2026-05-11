package com.hideakin.mypics;

import java.nio.file.Path;
import java.util.function.Consumer;

import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

public class ListPane extends JSplitPane {

	private static final long serialVersionUID = -8809183505899944015L;

	public static ListPane create() {
		return new ListPane();
	}

	private final DirectoryListModel _directoryModel = DirectoryListModel.create(FileListModel.create());
	private final DirectoryList _directoryList = DirectoryList.of(_directoryModel);
	private final FileList _fileList = FileList.of(_directoryModel.fileListModel());
	private Consumer<Path> _onSelected = path -> {};

	private ListPane() {
		super(JSplitPane.VERTICAL_SPLIT);
		setTopComponent(new JScrollPane(_directoryList));
		setBottomComponent(new JScrollPane(_fileList));
		setDividerLocation(200);
		_fileList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
            	Path selected = _fileList.getSelectedValue();
            	_onSelected.accept(selected);
            }
        });
	}

	public void onChanged(Consumer<Path> callback) {
		_directoryModel.onChanged(callback);
	}

	public void onSelected(Consumer<Path> callback) {
		_onSelected = callback;
	}

	public void loadDirectoryFrom(Path path) {
		_directoryModel.loadFrom(path);
	}

	public void select(Path path) {
		_fileList.setSelectedValue(path, true);
	}

}
