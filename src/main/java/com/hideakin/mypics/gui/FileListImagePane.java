package com.hideakin.mypics.gui;

import static com.hideakin.mypics.Application.configuration;

import java.awt.BorderLayout;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import com.hideakin.mypics.util.function.ConsumerList;

public class FileListImagePane extends JPanel {

	private static final long serialVersionUID = 6080536333060614921L;

	private static final int SINGLE = 1;
	private static final int MULTI = 2;

	public static FileListImagePane create() {
		return new FileListImagePane();
	}

	protected final JLabel _title = new JLabel("?");
	protected final JSplitPane _splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
	protected final FileListPane _fileListPane = FileListPane.create();
	protected final MultiImagePane _multiImagePane = MultiImagePane.create();
	protected final ImagePane _imagePane = ImagePane.create();
	protected final ConsumerList<Path> _onSelected = new ConsumerList<>();
	protected int _right = MULTI;

	protected FileListImagePane() {
		super(new BorderLayout());
		add(_title, BorderLayout.NORTH);
		add(_splitPane, BorderLayout.CENTER);
		_splitPane.setLeftComponent(_fileListPane);
		_splitPane.setRightComponent(_multiImagePane);
		_splitPane.setDividerLocation(400);
		_fileListPane.setFilteringTextFieldVisibility(configuration.getFileFilteringVisibility());
		_fileListPane.enableThumbnail(configuration.getThumbnailEnabled());
		_fileListPane.onSelected(path -> _onSelected.invoke(path));
	}

	public void onSelected(Consumer<Path> callback) {
		_onSelected.add(callback);
	}

	public void loadFrom(Path directory) {
		_fileListPane.loadFrom(directory, null);
		select(null);
	}

	public void setFiles(List<Path> paths) {
		_fileListPane.setFiles(paths);
		select(null);
	}

	public void deselect() {
		select(null);
	}

	public void select(Path path) {
		if (path != null) {
			if (_right != SINGLE) {
				_right = SINGLE;
				_splitPane.setRightComponent(_imagePane);
			}
			_fileListPane.select(path);
			SwingUtilities.invokeLater(() -> _imagePane.loadFrom(path));
		} else {
			if (_right != MULTI) {
				_right = MULTI;
				_splitPane.setRightComponent(_multiImagePane);
			}
			_fileListPane.deselect();
			SwingUtilities.invokeLater(() -> _multiImagePane.loadFrom(_fileListPane.get()));
		}
	}

	public void setText(String format, Object... params) {
		_title.setText(String.format(format, params));
	}

	public int getDividerLocation() {
		return _splitPane.getDividerLocation();
	}

	public void setDividerLocation(int location) {
		_splitPane.setDividerLocation(location);
	}

}
