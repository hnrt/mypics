package com.hideakin.mypics.gui;

import java.awt.BorderLayout;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.hideakin.mypics.gui.component.TextField;
import com.hideakin.mypics.gui.model.DirectoryListModel;
import com.hideakin.mypics.util.function.ConsumerList;

import static com.hideakin.mypics.Application.debug;
import static com.hideakin.mypics.Application.inProcessing;

public class DirectoryListPane extends JPanel {

	private static final long serialVersionUID = -256075743709412456L;

	public static DirectoryListPane create() {
		return new DirectoryListPane();
	}

	protected final TextField _filteringTextField = new TextField("filter directories");
	protected final DirectoryListModel _directoryListModel = DirectoryListModel.create();
	protected final DirectoryList _directoryList = DirectoryList.of(_directoryListModel);
	protected final JScrollPane _scrollPane = new JScrollPane(_directoryList);
	protected final ConsumerList<Path> _onSelected = new ConsumerList<>();
	protected final Map<Path, Integer> _firstLines = new HashMap<>();
	protected Path _directory = null;
	protected final DirectoryListModel _filteredListModel = DirectoryListModel.create();
	protected String _filterBy = null;

	protected DirectoryListPane() {
		super(new BorderLayout());
		add(_filteringTextField, BorderLayout.NORTH);
		add(_scrollPane, BorderLayout.CENTER);
		_directoryList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
            	select(_directoryList.getSelectedValue());
            }
		});
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

	public void onSelected(Consumer<Path> callback) {
		_onSelected.add(callback);
	}

	public void loadFrom(Path directory) {
		inProcessing.run(() -> {
			_filteringTextField.setText("");
			_filterBy = null;
			saveListState(_directory);
			_directory = directory;
			_directoryListModel.loadFrom(directory);
			restoreListState(directory);
			_directoryList.clearSelection();
		});
	}

	protected void select(Path directory) {
    	inProcessing.runExclusively(() -> {
    		debug(3, "directoryList.Selection");
    		if (directory != null) {
    			Path actual = _directoryListModel.isParentDirectory(directory) ? _directory.getParent() : directory;
    			_onSelected.invoke(actual);
    		}
    	});
	}

	public boolean getFilteringTextFieldVisibility() {
		return _filteringTextField.isVisible();
	}

	public void setFilteringTextFieldVisibility(boolean show) {
		_filteringTextField.setVisible(show);
		revalidate();
		repaint();
	}

	public boolean toggleFilteringTextFieldVisibility() {
		setFilteringTextFieldVisibility(!getFilteringTextFieldVisibility());
		return getFilteringTextFieldVisibility();
	}

	protected void filter() {
		inProcessing.run(() -> {
			String text = _filteringTextField.getText().trim().toLowerCase();
			if (_filterBy == null && text.length() == 0) {
				return;
			}
			if (_filterBy == null) {
				saveListState(_directory);
				_filteredListModel.clear();
				_directoryList.setModel(_filteredListModel);
			}
			if (text.length() > 0) {
				_filteredListModel.copyFrom(_directoryListModel, text);
				_filterBy = text;
			} else {
				_filterBy = null;
				_directoryList.setModel(_directoryListModel);
				restoreListState(_directory);
			}
		});
	}

	private void saveListState(Path directory) {
		if (directory != null && _directoryList.getModel().getSize() > 0) {
			int index = _directoryList.getFirstVisibleIndex();
			if (index > -1) {
				_firstLines.put(directory, Integer.valueOf(index));
			}
		}
	}

	private void restoreListState(Path directory) {
		Integer index = _firstLines.get(directory);
		if (index != null && index < _directoryList.getModel().getSize()) {
			java.awt.Rectangle rect = _directoryList.getCellBounds(index, index);
			if (rect != null) {
				JViewport viewport = _scrollPane.getViewport();
				java.awt.Point position = new java.awt.Point(0, rect.y);
				viewport.setViewPosition(position);
			}
		}
	}

}
