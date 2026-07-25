package com.hideakin.mypics.gui;

import static com.hideakin.mypics.Application.inProcessing;

import java.awt.BorderLayout;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.hideakin.mypics.Application;
import com.hideakin.mypics.gui.component.TextField;
import com.hideakin.mypics.gui.model.FileListModel;
import com.hideakin.mypics.util.function.ConsumerList;

public class FileListPane extends JPanel {

	private static final long serialVersionUID = 2644936868786761623L;

	public static FileListPane create() {
		return new FileListPane();
	}

	protected final TextField _filteringTextField = new TextField("filter files");
	protected final FileListModel _fileListModel = FileListModel.create();
	protected final FileList _fileList = FileList.of(_fileListModel);
	protected final JScrollPane _scrollPane = new JScrollPane(_fileList);
	protected final ConsumerList<Path> _onSelected = new ConsumerList<>();
	protected final Map<Path, Path> _selectedFiles = new HashMap<>();
	protected Path _directory = null;
	protected final FileListModel _filteredFileListModel = FileListModel.create();
	protected String _filterBy = null;
	protected int _firstLine = -1;

	protected FileListPane() {
		super(new BorderLayout());
		add(_filteringTextField, BorderLayout.NORTH);
		add(_scrollPane, BorderLayout.CENTER);
		_fileList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
            	select(_fileList.getSelectedValue());
            }
		});
		_fileListModel.onClear(() -> Application.fileManager.clear());
		_filteringTextField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				filter();
			}
			@Override
			public void removeUpdate(DocumentEvent e) {
				filter();
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				filter();
			}
		});
	}

	public FileList fileList() {
		return _fileList;
	}

	public void onFileSelected(Consumer<Path> callback) {
		_onSelected.add(callback);
	}

	public void loadFrom(Path directory, Object selection) {
		inProcessing.run(() -> {
			_directory = directory;
			_fileListModel.loadFrom(directory);
			_fileList.adjustSize();
			if (selection instanceof Integer index) {
				_fileList.select(index);
			} else if (selection instanceof Path filePath) {
				_fileList.select(filePath);
			} else {
				Path previouslySelected = _selectedFiles.get(directory);
				if (previouslySelected != null) {
					_fileList.select(previouslySelected);
				}
			}
			_onSelected.invoke(_fileList.getSelectedValue());
		});
	}

	public void addFiles(List<Path> paths) {
		Path selected = _fileList.getSelectedValue();
		_fileList.clearSelection();
		_fileListModel.add(paths);
		_fileList.select(selected);
	}

	public void removeFiles(List<Path> paths) {
		Path selected = _fileList.getSelectedValue();
		if (paths.contains(selected)) {
			int index = _fileList.getSelectedIndex();
			_fileList.clearSelection();
			_fileListModel.remove(paths);
			_fileList.select(index);
			_onSelected.invoke(_fileList.getSelectedValue());
		} else {
			_fileListModel.remove(paths);
		}
	}

	public boolean getFilterTextFieldVisibility() {
		return _filteringTextField.isVisible();
	}

	public void setFilterTextFieldVisibility(boolean show) {
		_filteringTextField.setVisible(show);
		revalidate();
		repaint();
	}

	public boolean toggleFilterTextFieldVisibility() {
		setFilterTextFieldVisibility(!getFilterTextFieldVisibility());
		return getFilterTextFieldVisibility();
	}

	public void enableThumbnail(boolean enabled) {
		_fileList.enableThumbnail(enabled);
	}

	public Path getSelectedFile() {
		return _fileList.getSelectedValue();
	}

	public List<Path> getSelectedFiles() {
		return _fileList.getSelectedValuesList();
	}

	protected void select(Path path) {
    	inProcessing.runExclusively(() -> {
    		Application.debug(3, "fileList.Selection");
    		_selectedFiles.put(_directory, path);
    		_onSelected.invoke(path);
    	});
	}

	protected void filter() {
		inProcessing.run(() -> {
			String text = _filteringTextField.getText().trim().toLowerCase();
			if (_filterBy == null && text.length() == 0) {
				return;
			}
			Path selected = _fileList.getSelectedValue();
			if (_filterBy == null) {
				int n = _fileListModel.getSize();
				if (n > 0) {
					_firstLine = _fileList.getFirstVisibleIndex();
				} else {
					_firstLine = -1;
				}
				_filteredFileListModel.clear();
				_fileList.setModel(_filteredFileListModel);
			}
			if (text.length() > 0) {
				_filteredFileListModel.copyFrom(_fileListModel, text);
				_fileList.adjustSize();
				_filterBy = text;
			} else {
				_fileList.setModel(_fileListModel);
				_fileList.adjustSize();
				_filterBy = null;
				if (_firstLine > -1) {
					java.awt.Rectangle rect = _fileList.getCellBounds(_firstLine, _firstLine);
					if (rect != null) {
						JViewport viewport = _scrollPane.getViewport();
						java.awt.Point position = new java.awt.Point(0, rect.y);
						viewport.setViewPosition(position);
					}
				}
			}
			if (selected != null) {
				_fileList.setSelectedValue(selected, true);
				if (_fileList.getSelectedValue() != selected) {
					_fileList.clearSelection();
					_onSelected.invoke(null);
				}
			}
		});
	}

}
