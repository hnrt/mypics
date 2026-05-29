package com.hideakin.mypics;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JViewport;

public class ListPane extends JSplitPane {

	private static final long serialVersionUID = -8809183505899944015L;

	public static ListPane create() {
		return new ListPane();
	}

	private final DirectoryListModel _directoryListModel = DirectoryListModel.create();
	private final DirectoryList _directoryList = DirectoryList.of(_directoryListModel);
	private final FileListModel _fileListModel = FileListModel.create();
	private final FileList _fileList = FileList.of(_fileListModel);
	private final List<Consumer<Path>> _onChanged = new ArrayList<>();
	private final List<Consumer<Path>> _onSelected = new ArrayList<>();
	private final Map<Path, Integer> _firstIndexes = new HashMap<>();
	private final Map<Path, Path> _selectedFiles = new HashMap<>();

	private ListPane() {
		super(JSplitPane.VERTICAL_SPLIT);
		setTopComponent(new JScrollPane(_directoryList));
		setBottomComponent(new JScrollPane(_fileList));
		_directoryList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Path selected = _directoryList.getSelectedValue();
                if (selected != null) {
                	if (_directoryListModel.isParentDirectory(selected)) {
                		Path parent = Application.configuration.getDirectory().getParent();
                		if (parent != null) {
                			Path selectedFile = _selectedFiles.get(parent);
                			loadFrom(parent);
                    		_fileList.select(selectedFile);
                		} else {
                			_directoryList.clearSelection();
                		}
                	} else {
                		Path selectedFile = _selectedFiles.get(selected);
                		loadFrom(selected);
                		_fileList.select(selectedFile);
                	}
                }
            }
        });
		_fileList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
            	Path selected = _fileList.getSelectedValue();
        		_selectedFiles.put(Application.configuration.getDirectory(), selected);
            	for (Consumer<Path> cb : _onSelected) {
            		cb.accept(selected);
            	}
            }
        });
		setDividerLocation(Application.configuration.getListVerticalDividerLocation());
		addPropertyChangeListener("dividerLocation", e -> Application.configuration.setListVerticalDividerLocation((int)e.getNewValue()));
	}

	public void onChanged(Consumer<Path> callback) {
		_onChanged.add(callback);
	}

	public void onSelected(Consumer<Path> callback) {
		_onSelected.add(callback);
	}

	public DirectoryListModel directoryListModel() {
		return _directoryListModel;
	}

	public FileList fileList() {
		return _fileList;
	}

	public FileListModel fileListModel() {
		return _fileListModel;
	}

	public Path previouslySelected(Path directory) {
		return _selectedFiles.get(directory);
	}

	public void loadFrom(Path directory) {
		if (_directoryListModel.getSize() > 0) {
			Path last = Application.configuration.getDirectory();
			if (last != null) {
				int first = _directoryList.getFirstVisibleIndex();
				if (first > -1) {
					_firstIndexes.put(last, Integer.valueOf(first));
				}
			}
		}
		Application.configuration.setDirectory(directory);
		_fileListModel.clear();
		_directoryListModel.clear();
		_directoryListModel.addParentDirectory(directory);
		try {
			Files.list(directory).sorted().forEach((e) -> {
				if (Files.isDirectory(e)) {
					_directoryListModel.addElement(e);
				} else if (Files.isRegularFile(e)) {
					_fileListModel.addElement(e);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		Integer index = _firstIndexes.get(directory);
		if (index != null && index < _directoryListModel.getSize()) {
			java.awt.Rectangle rect = _directoryList.getCellBounds(index, index);
			if (rect != null) {
				JViewport viewport = ((JScrollPane)getTopComponent()).getViewport();
				java.awt.Point position = new java.awt.Point(0, rect.y);
				viewport.setViewPosition(position);
			}
		}
		for (Consumer<Path> cb : _onChanged) {
			cb.accept(directory);
		}
	}

	public void setDefaultSize() {
		Application.configuration.setListVerticalDividerLocation(Configuration.DEFAULT_LIST_VERTICAL_DIVIDER_LOCATION);
		setDividerLocation(Application.configuration.getListVerticalDividerLocation());
	}

}
