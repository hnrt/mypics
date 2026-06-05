package com.hideakin.mypics;

import java.awt.BorderLayout;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JViewport;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class ListPane extends JSplitPane {

	private static final long serialVersionUID = -8809183505899944015L;

	public static ListPane create() {
		return new ListPane();
	}

	private final TextField _directoryFilterTextField = new TextField("filter directory");
	private final DirectoryListModel _directoryListModel = DirectoryListModel.create();
	private final DirectoryList _directoryList = DirectoryList.of(_directoryListModel);
	private final JScrollPane _directoryScrollPane = new JScrollPane(_directoryList);
	private final FileListModel _fileListModel = FileListModel.create();
	private final FileList _fileList = FileList.of(_fileListModel);
	private final List<Consumer<Path>> _onChanged = new ArrayList<>();
	private final List<Consumer<Path>> _onSelected = new ArrayList<>();
	private final Map<Path, Integer> _directoryListFirstLines = new HashMap<>();
	private final Map<Path, Path> _selectedFiles = new HashMap<>();
	private boolean _internallyProcessing = false;
	private String _directoryFilterText = null;

	private ListPane() {
		super(JSplitPane.VERTICAL_SPLIT);
		JPanel upperPanel = new JPanel(new BorderLayout());
		upperPanel.add(_directoryFilterTextField, BorderLayout.NORTH);
		upperPanel.add(_directoryScrollPane, BorderLayout.CENTER);
		setTopComponent(upperPanel);
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
		_directoryFilterTextField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				if (!_internallyProcessing) onTextChanged();
			}
			@Override
			public void removeUpdate(DocumentEvent e) {
				if (!_internallyProcessing) onTextChanged();
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				if (!_internallyProcessing) onTextChanged();
			}
			private void onTextChanged() {
				String text = _directoryFilterTextField.getText().trim().toLowerCase();
				if (_directoryFilterText == null && text.length() == 0) return;
				try {
					_internallyProcessing = true;
					Path directory = Application.configuration.getDirectory();
					if (_directoryFilterText == null) {
						saveDirectoryListState(directory);
					}
					List<Path> entries = Files.list(directory).filter(e -> Files.isDirectory(e)).collect(Collectors.toList());
					if (text.length() == 0) {
						List<Path> dd = entries.stream().filter(e -> Files.isDirectory(e)).collect(Collectors.toList());
						dd.sort(Comparator.comparing(e -> e.getFileName().toString()));
						_directoryListModel.clear();
						_directoryListModel.addParentDirectory(directory);
						_directoryListModel.addAll(dd);
						_directoryFilterText = null;
						restoreDirectoryListState(directory);
					} else {
						List<Path> dd = entries.stream().filter(e -> Files.isDirectory(e) && e.getFileName().toString().toLowerCase().contains(text)).collect(Collectors.toList());
						dd.sort(Comparator.comparing(e -> e.getFileName().toString()));
						_directoryListModel.clear();
						_directoryListModel.addAll(dd);
						_directoryFilterText = text;
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					_internallyProcessing = false;
				}
			}
		});
		setDividerLocation(Application.configuration.getListVerticalDividerLocation());
		addPropertyChangeListener("dividerLocation", e -> Application.configuration.setListVerticalDividerLocation((int)e.getNewValue()));
		setDirectoryFilterTextFieldVisibility(Application.configuration.getDirectoryFilterVisibility());
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
			_directoryFilterTextField.setText("");
			_directoryFilterText = null;
			saveDirectoryListState(Application.configuration.getDirectory());
			List<Path> entries = Files.list(directory).toList();
			List<Path> dd = entries.stream().filter(e -> Files.isDirectory(e)).collect(Collectors.toList());
			List<Path> ff = entries.stream().filter(e -> Files.isRegularFile(e)).collect(Collectors.toList());
			dd.sort(Comparator.comparing(e -> e.getFileName().toString()));
			ff.sort(Comparator.comparing(e -> e.getFileName().toString()));
			Application.configuration.setDirectory(directory);
			_fileListModel.clear();
			_directoryListModel.clear();
			_directoryListModel.addParentDirectory(directory);
			_directoryListModel.addAll(dd);
			_fileListModel.addAll(ff);
			restoreDirectoryListState(directory);
			invokeOnChanged(directory);
			restoreFileListState(directory);
			invokeOnSelected(_fileList.getSelectedValue());
		} catch (Exception e) {
			e.printStackTrace();
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
				JViewport viewport = _directoryScrollPane.getViewport();
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

	public boolean getDirectoryFilterTextFieldVisibility() {
		return _directoryFilterTextField.isVisible();
	}

	public void setDirectoryFilterTextFieldVisibility(boolean show) {
		_directoryFilterTextField.setVisible(show);
		getTopComponent().revalidate();
		getTopComponent().repaint();
		Application.configuration.setDirectoryFilterVisibility(show);
	}

	public boolean toggleDirectoryFilterTextFieldVisibility() {
		setDirectoryFilterTextFieldVisibility(!getDirectoryFilterTextFieldVisibility());
		return getDirectoryFilterTextFieldVisibility();
	}

	public boolean toggleFileListCellRenderer() {
		_fileList.setCellRenderer(Application.configuration.getFileListCellRenderer() ^ 1);
		return Application.configuration.getFileListCellRenderer() != 0;
	}

	public void setDefaultSize() {
		Application.configuration.setListVerticalDividerLocation(Configuration.DEFAULT_LIST_VERTICAL_DIVIDER_LOCATION);
		setDividerLocation(Application.configuration.getListVerticalDividerLocation());
	}

}
