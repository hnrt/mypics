package com.hideakin.mypics;

import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

public class ListPane extends JSplitPane {

	private static final long serialVersionUID = -8809183505899944015L;

	public static ListPane create() {
		return new ListPane();
	}

	private final Configuration _configuration = Configuration.getInstance();
	private final DirectoryListModel _directoryListModel = DirectoryListModel.create(FileListModel.create());
	private final DirectoryList _directoryList = DirectoryList.of(_directoryListModel);
	private final FileList _fileList = FileList.of(_directoryListModel.fileListModel());

	private ListPane() {
		super(JSplitPane.VERTICAL_SPLIT);
		setTopComponent(new JScrollPane(_directoryList));
		setBottomComponent(new JScrollPane(_fileList));
		setDividerLocation(_configuration.getListVerticalDividerLocation());
		addPropertyChangeListener("dividerLocation", e -> {
        	_configuration.setListVerticalDividerLocation((int)e.getNewValue());
        });
	}

	public DirectoryListModel directoryListModel() {
		return _directoryListModel;
	}

	public FileList fileList() {
		return _fileList;
	}

	public void setDefaultSize() {
		_configuration.setListVerticalDividerLocation(Configuration.DEFAULT_LIST_VERTICAL_DIVIDER_LOCATION);
		setDividerLocation(_configuration.getListVerticalDividerLocation());
	}

}
