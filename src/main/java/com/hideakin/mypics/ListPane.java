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
	private final Map<Path, Integer> _directoryListFirstLines = new HashMap<>();
	private final Map<Path, Path> _selectedFiles = new HashMap<>();
	private boolean _internallyProcessing = false;

	private ListPane() {
		super(JSplitPane.VERTICAL_SPLIT);
		setTopComponent(new JScrollPane(_directoryList));
		setBottomComponent(new JScrollPane(_fileList));
		_directoryList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
            	if (_internallyProcessing) return;
    	    	Application.debug(3, "directoryList.Selection");
            	Path selected = _directoryList.getSelectedValue();
            	if (selected != null) {
            		if (_directoryListModel.isParentDirectory(selected)) {
            			Path parent = Application.configuration.getDirectory().getParent();
            			if (parent != null) {
            				loadFrom(parent);
            			} else {
            				try {
            					_internallyProcessing = true;
            					_directoryList.clearSelection();
            				} finally {
            					_internallyProcessing = false;
            				}
            			}
            		} else {
            			loadFrom(selected);
            		}
            	}
            }
        });
		_fileList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
            	if (_internallyProcessing) return;
    	    	Application.debug(3, "fileList.Selection");
            	try {
        			_internallyProcessing = true;
        			Path selected = _fileList.getSelectedValue();
        			_selectedFiles.put(Application.configuration.getDirectory(), selected);
        			invokeOnSelected(selected);
            	} finally {
        			_internallyProcessing = false;
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

	public FileList fileList() {
		return _fileList;
	}

	public void loadFrom(Path directory) {
		try {
			_internallyProcessing = true;
			saveDirectoryListState(Application.configuration.getDirectory());
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
			restoreDirectoryListState(directory);
			invokeOnChanged(directory);
			restoreFileListState(directory);
			invokeOnSelected(_fileList.getSelectedValue());
		} finally {
			_internallyProcessing = false;
		}
	}

	private void saveDirectoryListState(Path directory) {
		if (directory != null && _directoryListModel.getSize() > 0) {
			int index = _directoryList.getFirstVisibleIndex();
			if (index > -1) {
				_directoryListFirstLines.put(directory, Integer.valueOf(index));
			}
		}
	}

	private void restoreDirectoryListState(Path directory) {
		Integer index = _directoryListFirstLines.get(directory);
		if (index != null && index < _directoryListModel.getSize()) {
			java.awt.Rectangle rect = _directoryList.getCellBounds(index, index);
			if (rect != null) {
				JViewport viewport = ((JScrollPane)getTopComponent()).getViewport();
				java.awt.Point position = new java.awt.Point(0, rect.y);
				viewport.setViewPosition(position);
			}
		}
	}

	private void restoreFileListState(Path directory) {
		Path selected = _selectedFiles.get(directory);
		if (selected != null) {
			_fileList.setSelectedValue(selected, true);
		}
	}

	private void invokeOnChanged(Path directory) {
		for (Consumer<Path> cb : _onChanged) {
			cb.accept(directory);
		}
	}

	private void invokeOnSelected(Path selected) {
		for (Consumer<Path> cb : _onSelected) {
			cb.accept(selected);
		}
	}

	public void setDefaultSize() {
		Application.configuration.setListVerticalDividerLocation(Configuration.DEFAULT_LIST_VERTICAL_DIVIDER_LOCATION);
		setDividerLocation(Application.configuration.getListVerticalDividerLocation());
	}

}
