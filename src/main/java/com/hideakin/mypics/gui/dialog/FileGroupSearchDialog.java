package com.hideakin.mypics.gui.dialog;

import static com.hideakin.mypics.Application.configuration;
import static com.hideakin.mypics.Application.mainFrame;
import static com.hideakin.mypics.Application.fileManager;
import static com.hideakin.mypics.Application.debug;

import java.awt.BorderLayout;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import com.hideakin.mypics.gui.ImagePane;
import com.hideakin.mypics.gui.MultiImagePane;
import com.hideakin.mypics.gui.component.SelectablePathTree;
import com.hideakin.mypics.gui.component.SelectableThumbnailedPathTree;
import com.hideakin.mypics.gui.model.FileGroupSearchTreeModel;
import com.hideakin.mypics.gui.model.MatchedFileTreeNode;
import com.hideakin.mypics.gui.model.SelectablePathTreeNode;
import com.hideakin.mypics.gui.util.WorkInProgress;
import com.hideakin.mypics.model.PathNode;
import com.hideakin.mypics.model.SelectablePath;

public class FileGroupSearchDialog extends ModalDialog {

	private static final long serialVersionUID = 2292343761958282508L;

	public static FileGroupSearchDialog create() {
		return new FileGroupSearchDialog();
	}

	private final JSplitPane _basePane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
	private final JSplitPane _leftPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
	private final JSplitPane _upperPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
	private final JPanel _patternPane = new JPanel(new BorderLayout());
	private final JLabel _patternLabel = new JLabel("Search pattern");
	private final JTextField _patternEdit = new JTextField(configuration.getFileGroupPattern());
	private final JPanel _sourcePane = new JPanel(new BorderLayout());
	private final JLabel _sourceLabel = new JLabel("Source directories");
	private final SelectablePathTree _sourceTree = new SelectablePathTree();
	private final JScrollPane _sourceScroll = new JScrollPane(_sourceTree);
	private final JPanel _psPane = new JPanel(new BorderLayout());
	private final JPanel _destinationPane = new JPanel(new BorderLayout());
	private final JLabel _destinationLabel = new JLabel("Destination directories");
	private final SelectablePathTree _destinationTree = new SelectablePathTree();
	private final JScrollPane _destinationScroll = new JScrollPane(_destinationTree);
	private final JPanel _resultPane = new JPanel(new BorderLayout());
	private final JLabel _resultLabel = new JLabel("File groups");
	private final SelectableThumbnailedPathTree _resultTree = new SelectableThumbnailedPathTree(new FileGroupSearchTreeModel());
	private final JScrollPane _resultScroll = new JScrollPane(_resultTree);
	private final ImagePane _imagePane = ImagePane.create();
	private final MultiImagePane _multiImagePane = MultiImagePane.create();
	private final AtomicInteger _state = new AtomicInteger(0);
	private final Map<String, PathNode> _sources = new HashMap<>();
	private final Map<String, PathNode> _destinations = new HashMap<>();
	private Pattern _pattern;
	private Thread _background;

	private FileGroupSearchDialog() {
		super("Select source and destination directories and click Test to begin search.");
		getContentPane().setLayout(new BorderLayout());
		add(_basePane, BorderLayout.CENTER);
		_basePane.setLeftComponent(_leftPane);
		_basePane.setRightComponent(_imagePane);
		_basePane.setDividerLocation(600);
		_leftPane.setTopComponent(_upperPane);
		_leftPane.setBottomComponent(_resultPane);
        _leftPane.setDividerLocation(400);
		_upperPane.setTopComponent(_psPane);
		_upperPane.setBottomComponent(_destinationPane);
        _upperPane.setDividerLocation(200);
		_psPane.add(_patternPane, BorderLayout.NORTH);
		_psPane.add(_sourcePane, BorderLayout.CENTER);
		_patternPane.add(_patternLabel, BorderLayout.NORTH);
		_patternPane.add(_patternEdit, BorderLayout.CENTER);
		_sourcePane.add(_sourceLabel, BorderLayout.NORTH);
		_sourcePane.add(_sourceScroll, BorderLayout.CENTER);
		_destinationPane.add(_destinationLabel, BorderLayout.NORTH);
		_destinationPane.add(_destinationScroll, BorderLayout.CENTER);
		_resultPane.add(_resultLabel, BorderLayout.NORTH);
		_resultPane.add(_resultScroll, BorderLayout.CENTER);
		_buttons.testButton.setVisible(true);
		_buttons.testButton.setEnabled(false);
		_buttons.applyButton.setEnabled(false);
		setSize(1200, 800);
		_sourceTree.onChanged(x -> {
			_sourceTree.setSelected(x.path(), x.selected(), true);
			_destinationTree.setEnabled(x.path(), !x.selected(), true);
			_buttons.testButton.setEnabled(_sourceTree.checked().length > 0 && _destinationTree.checked().length > 0);
			_buttons.applyButton.setEnabled(false);
		});
		_destinationTree.onChanged(x -> {
			_destinationTree.setSelected(x.path(), x.selected(), true);
			_sourceTree.setEnabled(x.path(), !x.selected(), true);
			_buttons.testButton.setEnabled(_sourceTree.checked().length > 0 && _destinationTree.checked().length > 0);
			_buttons.applyButton.setEnabled(false);
		});
		_resultTree.onChanged(x -> {
			FileGroupSearchTreeModel model = (FileGroupSearchTreeModel)_resultTree.model();
			_buttons.applyButton.setEnabled(model.canProcess());
		});
		_resultTree.onSelected(x -> {
			int loc = _basePane.getDividerLocation();
			if (x instanceof SelectablePathTreeNode spNode) {
				SelectablePath sp = spNode.selectablePath();
				if (sp.type() == SelectablePath.REGULAR_FILE) {
					_basePane.setRightComponent(_imagePane);
					_imagePane.loadFrom(sp.path());
				} else if (sp.type() == SelectablePath.DIRECTORY) {
					_basePane.setRightComponent(_multiImagePane);
					try {
						_multiImagePane.loadFrom(Files.list(sp.path()).filter(e -> Files.isRegularFile(e)).toList());
					} catch (Exception e) {
						e.printStackTrace();
						_multiImagePane.clear();
					}
				}
			} else if (x instanceof MatchedFileTreeNode mfNode) {
				_basePane.setRightComponent(_multiImagePane);
				try {
					_multiImagePane.loadFrom(mfNode.list());
				} catch (Exception e) {
					e.printStackTrace();
					_multiImagePane.clear();
				}
			} else {
				_basePane.setRightComponent(_imagePane);
				_imagePane.loadFrom(null);
			}
			SwingUtilities.invokeLater(() -> _basePane.setDividerLocation(loc));
		});
		_sourceTree.loadDirectory(configuration.getDirectory());
		_destinationTree.loadDirectory(configuration.getDirectory());
	}

	private void run() {
		_sources.clear();
		_destinations.clear();
		for (Path directory : _sourceTree.checked()) {
			try {
				Files.list(directory).filter(e -> Files.isRegularFile(e)).forEach(e -> {
					String fileName = e.getFileName().toString();
					Matcher m = _pattern.matcher(fileName);
					if (m.find()) {
						String key = m.group();
						if (key == null) return;
						PathNode node = _sources.get(key);
						if (node != null) {
							node.add(e);
						} else {
							_sources.put(key, PathNode.of(e));
						}
					}
				});
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		for (Path directory : _destinationTree.checked()) {
			try {
				Files.list(directory).filter(e -> Files.isRegularFile(e)).forEach(e -> {
					String fileName = e.getFileName().toString();
					Matcher m = _pattern.matcher(fileName);
					if (m.find()) {
						String key = m.group();
						if (key == null) return;
						PathNode node = _destinations.get(key);
						if (node != null) {
							node.add(directory);
						} else {
							_destinations.put(key, PathNode.of(directory));
						}
					}
				});
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		FileGroupSearchTreeModel model = (FileGroupSearchTreeModel)_resultTree.model();
		for (String key : _sources.keySet()) {
			PathNode s = _sources.get(key);
			PathNode d = _destinations.get(key);
			if (d != null) {
				model.addKey(key);
				model.addMatchedFiles(key, s);
				model.addTargetDirectories(key, d);
				invokeLater(() -> {
					model.reloadKey(key);
				});
			}
		}
		String[] keys = model.keys();
		invokeLater(() -> {
			_resultLabel.setText(String.format("File groups: %d", keys.length));
			if (model.canProcess()) {
				_buttons.applyButton.setEnabled(true);
			}
			_buttons.testButton.setEnabled(true);
		});
		for (String key : keys) {
			DefaultMutableTreeNode node = model.keyNode(key);
			invokeLater(() -> {
				_resultTree.expandPath(new TreePath(node.getPath()));
			});
		}
	}

	@Override
	public void test() {
		debug(3, "FileGroupSearchDialog::test");
		try {
			String text = _patternEdit.getText();
			_pattern = Pattern.compile(text);
			configuration.setFileGroupPattern(text);
		} catch (Exception e) {
			mainFrame.showErrorDialog("Bad search pattern: " + e.getMessage());
			return;
		}
		int loc = _basePane.getDividerLocation();
		_resultLabel.setText("File groups: Searching...");
		_resultTree.root().removeAllChildren();
		_resultTree.model().reload();
		_basePane.setRightComponent(_imagePane);
		_imagePane.loadFrom(null);
		_buttons.testButton.setEnabled(false);
		_buttons.applyButton.setEnabled(false);
		SwingUtilities.invokeLater(() -> _basePane.setDividerLocation(loc));
		_background = new Thread(() -> new WorkInProgress().run(() -> run()));
		_background.start();
	}

	@Override
	public void apply() {
		debug(3, "FileGroupSearchDialog::apply");
		new WorkInProgress().run(() -> {
			FileGroupSearchTreeModel model = (FileGroupSearchTreeModel)_resultTree.model();
			for (String key : model.keys()) {
				List<Path> from = model.from(key);
				List<Path> to = model.to(key);
				if (from.size() >= 1 && to.size() == 1) {
					fileManager.move(from, to.get(0), e -> mainFrame.showErrorDialog(e));
				}
			}
			mainFrame.reloadDirectory();
		});
		super.apply();
	}

	@Override
	public void cancel() {
		debug(3, "FileGroupSearchDialog::cancel");
		_state.set(-1);
		super.cancel();
	}

	private boolean invokeLater(Runnable x) {
		while (!_state.compareAndSet(0, 1)) {
			if (_state.get() == -1) {
				return false;
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException ie) {
			}
		}
		SwingUtilities.invokeLater(() -> {
			try {
				x.run();
			} finally {
				_state.compareAndSet(1, 0);
			}
		});
		return true;
	}

}
