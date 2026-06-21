package com.hideakin.mypics.gui;

import java.awt.BorderLayout;

import java.nio.file.Files;
import java.nio.file.Path;
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

import static com.hideakin.mypics.Application.configuration;
import static com.hideakin.mypics.Application.inProcessing;

import com.hideakin.mypics.Application;
import com.hideakin.mypics.Configuration;
import com.hideakin.mypics.gui.model.DirectoryListModel;
import com.hideakin.mypics.gui.model.FileListModel;
import com.hideakin.mypics.util.function.ConsumerList;

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
	private final ConsumerList<Path> _onDirectoryChanged = new ConsumerList<>();
	private final ConsumerList<Path> _onFileSelected = new ConsumerList<>();
	private final Map<Path, Integer> _directoryListFirstLines = new HashMap<>();
	private final Map<Path, Path> _selectedFiles = new HashMap<>();
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
            	selectDirectory();
            }
        });
		_fileList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
            	selectFile();
            }
		});
		_fileListModel.onClear(() -> Application.fileManager.clear());
		_directoryFilterTextField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				filterDirectories();
			}
			@Override
			public void removeUpdate(DocumentEvent e) {
				filterDirectories();
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				filterDirectories();
			}
		});
		setDividerLocation(configuration.getListVerticalDividerLocation());
		addPropertyChangeListener("dividerLocation", e -> configuration.setListVerticalDividerLocation((int)e.getNewValue()));
		setDirectoryFilterTextFieldVisibility(configuration.getDirectoryFilterVisibility());
	}

	public void onDirectoryChanged(Consumer<Path> callback) {
		_onDirectoryChanged.add(callback);
	}

	public void onFileSelected(Consumer<Path> callback) {
		_onFileSelected.add(callback);
	}

	public FileList fileList() {
		return _fileList;
	}

	private void selectDirectory() {
    	inProcessing.runExclusively(() -> {
    		Application.debug(3, "directoryList.Selection");
    		Path selected = _directoryList.getSelectedValue();
    		if (selected != null) {
    			if (_directoryListModel.isParentDirectory(selected)) {
    				selected = configuration.getDirectory().getParent();
    			}
   				_onDirectoryChanged.invoke(selected);
    		}
    	});
	}

	private void selectFile() {
    	inProcessing.runExclusively(() -> {
    		Application.debug(3, "fileList.Selection");
    		Path selected = _fileList.getSelectedValue();
    		_selectedFiles.put(configuration.getDirectory(), selected);
    		_onFileSelected.invoke(selected);
    	});
	}

	private void filterDirectories() {
		inProcessing.run(() -> {
			try {
				String text = _directoryFilterTextField.getText().trim().toLowerCase();
				if (_directoryFilterText == null && text.length() == 0) return;
				Path directory = configuration.getDirectory();
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
			}
		});
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
				_directoryFilterTextField.setText("");
				_directoryFilterText = null;
				saveDirectoryListState(configuration.getDirectory());
				List<Path> entries = Files.list(directory).toList();
				List<Path> dd = entries.stream().filter(e -> Files.isDirectory(e)).collect(Collectors.toList());
				List<Path> ff = entries.stream().filter(e -> Files.isRegularFile(e)).collect(Collectors.toList());
				dd.sort(Comparator.comparing(e -> e.getFileName().toString()));
				ff.sort(Comparator.comparing(e -> e.getFileName().toString()));
				configuration.setDirectory(directory);
				_fileListModel.clear();
				_directoryListModel.clear();
				_directoryListModel.addParentDirectory(directory);
				_directoryListModel.addAll(dd);
				_fileListModel.addAll(ff);
				restoreDirectoryListState(directory);
				if (selection instanceof Integer index) {
					_fileList.select(index);
				} else if (selection instanceof Path filePath) {
					_fileList.select(filePath);
				} else {
					restoreFileListState(directory);
				}
				_onFileSelected.invoke(_fileList.getSelectedValue());
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
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
			_fileList.select(selected);
		} else if (_fileListModel.getSize() > 0) {
			_fileList.select(0);
		}
	}

	public boolean getDirectoryFilterTextFieldVisibility() {
		return _directoryFilterTextField.isVisible();
	}

	public void setDirectoryFilterTextFieldVisibility(boolean show) {
		_directoryFilterTextField.setVisible(show);
		getTopComponent().revalidate();
		getTopComponent().repaint();
		configuration.setDirectoryFilterVisibility(show);
	}

	public boolean toggleDirectoryFilterTextFieldVisibility() {
		setDirectoryFilterTextFieldVisibility(!getDirectoryFilterTextFieldVisibility());
		return getDirectoryFilterTextFieldVisibility();
	}

	public boolean toggleFileListCellRenderer() {
		_fileList.setCellRenderer(configuration.getFileListCellRenderer() ^ 1);
		return configuration.getFileListCellRenderer() != 0;
	}

	public Path getSelectedFile() {
		return _fileList.getSelectedValue();
	}

	public List<Path> getSelectedFiles() {
		return _fileList.getSelectedValuesList();
	}

	public void removeFiles(List<Path> paths) {
		Path selected = _fileList.getSelectedValue();
		if (paths.contains(selected)) {
			int index = _fileList.getSelectedIndex();
			_fileList.clearSelection();
			_fileListModel.removeElements(paths);
			_fileList.select(index);
			_onFileSelected.invoke(_fileList.getSelectedValue());
		} else {
			_fileListModel.removeElements(paths);
		}
	}

	public void addFiles(List<Path> paths) {
		Path selected = _fileList.getSelectedValue();
		_fileList.clearSelection();
		_fileListModel.addElements(paths);
		_fileList.select(selected);
	}

	public void setDefaultSize() {
		configuration.setListVerticalDividerLocation(Configuration.DEFAULT_LIST_VERTICAL_DIVIDER_LOCATION);
		setDividerLocation(configuration.getListVerticalDividerLocation());
	}

}
