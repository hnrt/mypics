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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.hideakin.mypics.gui.DirectorySelectionTreeImagePane;
import com.hideakin.mypics.gui.FileListImagePane;
import com.hideakin.mypics.gui.component.SelectablePathTree;
import com.hideakin.mypics.gui.model.DirectorySelectionTreeModel;
import com.hideakin.mypics.gui.model.FileGroupSearchTreeModel;
import com.hideakin.mypics.gui.model.MatchedFileTreeNode;
import com.hideakin.mypics.gui.model.SelectablePathTreeNode;
import com.hideakin.mypics.gui.model.TargetDirectoryTreeNode;
import com.hideakin.mypics.gui.util.WorkInProgress;
import com.hideakin.mypics.model.PathNode;
import com.hideakin.mypics.model.SelectablePath;

import static com.hideakin.mypics.Application.inProcessing;

public class FileGroupSearchDialog extends ModalDialog {

	private static final long serialVersionUID = 2292343761958282508L;

	public static FileGroupSearchDialog create() {
		return new FileGroupSearchDialog();
	}

	private final JSplitPane _basePane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
	private final JSplitPane _leftPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
	private final JSplitPane _rightPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
	private final JSplitPane _upperPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
	private final JPanel _patternPane = new JPanel(new BorderLayout());
	private final JLabel _patternLabel = new JLabel("Search pattern");
	private final JTextField _patternEdit = new JTextField(configuration.getFileGroupPattern());
	private final JPanel _sourcePane = new JPanel(new BorderLayout());
	private final JLabel _sourceLabel = new JLabel("Source directories");
	private final SelectablePathTree _sourceTree = new SelectablePathTree(new DirectorySelectionTreeModel(), false);
	private final JScrollPane _sourceScroll = new JScrollPane(_sourceTree);
	private final JPanel _psPane = new JPanel(new BorderLayout());
	private final JPanel _destinationPane = new JPanel(new BorderLayout());
	private final JLabel _destinationLabel = new JLabel("Destination directories");
	private final SelectablePathTree _destinationTree = new SelectablePathTree(new DirectorySelectionTreeModel(), false);
	private final JScrollPane _destinationScroll = new JScrollPane(_destinationTree);
	private final JPanel _resultPane = new JPanel(new BorderLayout());
	private final JLabel _resultLabel = new JLabel("File groups");
	private final SelectablePathTree _resultTree = new SelectablePathTree(new FileGroupSearchTreeModel());
	private final JScrollPane _resultScroll = new JScrollPane(_resultTree);
	private final FileListImagePane _matchedPane = FileListImagePane.create();
	private final FileListImagePane _targetPane = FileListImagePane.create();
	private final DirectorySelectionTreeImagePane _targetSelectionPane = DirectorySelectionTreeImagePane.create();
	private final Map<String, PathNode> _sources = new HashMap<>();
	private final Map<String, PathNode> _destinations = new HashMap<>();
	private Pattern _pattern;
	private Thread _background;
	private TargetDirectoryTreeNode _targetDirectoryNode;
	private MatchedFileTreeNode _currentMatchedFileTreeNode = null;

	private FileGroupSearchDialog() {
		super("Select source and destination directories and click Test to begin search.");
		getContentPane().setLayout(new BorderLayout());
		add(_basePane, BorderLayout.CENTER);
		_basePane.setLeftComponent(_leftPane);
		_basePane.setRightComponent(_rightPane);
		_basePane.setDividerLocation(400);
		_leftPane.setTopComponent(_upperPane);
		_leftPane.setBottomComponent(_resultPane);
        _leftPane.setDividerLocation(400);
        _rightPane.setTopComponent(_matchedPane);
        _rightPane.setBottomComponent(_targetPane);
        _rightPane.setDividerLocation(400);
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
		_buttons.searchButton.setVisible(true);
		_buttons.searchButton.setEnabled(false);
		_buttons.applyButton.setEnabled(false);
		setSize(1200, 800);
		_sourceTree.onChanged(x -> {
			_sourceTree.setSelected(x.path(), x.selected(), true);
			_destinationTree.setEnabled(x.path(), !x.selected(), true);
			_buttons.searchButton.setEnabled(_sourceTree.selectedPaths().length > 0);
			_buttons.applyButton.setEnabled(false);
		});
		_destinationTree.onChanged(x -> {
			_destinationTree.setSelected(x.path(), x.selected(), true);
			_sourceTree.setEnabled(x.path(), !x.selected(), true);
			_buttons.searchButton.setEnabled(_sourceTree.selectedPaths().length > 0);
			_buttons.applyButton.setEnabled(false);
		});
		_resultTree.onChanged(x -> {
			//NOP
		});
		_resultTree.onSelected(x -> {
			int locBase = _basePane.getDividerLocation();
			int locRight = _rightPane.getDividerLocation();
			if (x instanceof SelectablePathTreeNode spNode) {
				SelectablePath sp = spNode.selectablePath();
				if (sp.isRegularFile()) {
					if (spNode.getParent() instanceof MatchedFileTreeNode mfNode) {
						debug(3, "FileGroupSearchDialog.resultTree.onSelected(%s) RegularFile", sp.path());
						int loc = _matchedPane.getDividerLocation();
						if (_currentMatchedFileTreeNode != mfNode) {
							_currentMatchedFileTreeNode = mfNode;
							_matchedPane.setFiles(mfNode.list());
						}
						_matchedPane.select(sp.path());
						SwingUtilities.invokeLater(() -> _matchedPane.setDividerLocation(loc));
					}
				} else if (sp.isDirectory()) {
					debug(3, "FileGroupSearchDialog.resultTree.onSelected(%s) Directory", sp.path());
					if (_rightPane.getBottomComponent() == _targetSelectionPane) {
						_rightPane.setBottomComponent(_targetPane);
					}
					int loc = _targetPane.getDividerLocation();
					_targetPane.loadFrom(sp.path());
					SwingUtilities.invokeLater(() -> _targetPane.setDividerLocation(loc));
				}
			} else if (x instanceof MatchedFileTreeNode mfNode) {
				debug(3, "FileGroupSearchDialog.resultTree.onSelected MatchedFileTreeNode");
				if (_currentMatchedFileTreeNode != mfNode) {
					_currentMatchedFileTreeNode = mfNode;
					int loc = _matchedPane.getDividerLocation();
					_matchedPane.setFiles(mfNode.list());
					SwingUtilities.invokeLater(() -> _matchedPane.setDividerLocation(loc));
				}
			} else if (x instanceof TargetDirectoryTreeNode tdNode) {
				debug(3, "FileGroupSearchDialog.resultTree.onSelected TargetDirectoryTreeNode");
				_targetDirectoryNode = tdNode;
				_rightPane.setBottomComponent(_targetSelectionPane);
				for (Path path : _targetSelectionPane.selectedPaths()) {
					_targetSelectionPane.setSelected(path, false, true);
				}
				for (Path path : _targetDirectoryNode.list()) {
					_targetSelectionPane.setSelected(path,  true,  false);
				}
			} else {
				debug(3, "FileGroupSearchDialog.resultTree.onSelected something else");
			}
			SwingUtilities.invokeLater(() -> _basePane.setDividerLocation(locBase));
			SwingUtilities.invokeLater(() -> _rightPane.setDividerLocation(locRight));
		});
		_targetSelectionPane.onChanged(x -> {
			_targetDirectoryNode.replaceWith(_targetSelectionPane.selectedPaths());
			_resultTree.expandNode(_targetDirectoryNode);
		});
		_targetSelectionPane.onSelected(x -> {
			if (x instanceof SelectablePathTreeNode spNode) {
				SelectablePath sp = spNode.selectablePath();
				if (sp.isDirectory()) {
					debug(3, "FileGroupSearchDialog.targetPane.onSelected(%s)", sp.path());
					_targetSelectionPane.loadImagesFrom(sp.path());
				}
			}
		});
		_targetSelectionPane.onCreated(x -> {
			_targetDirectoryNode.replaceWith(_targetSelectionPane.selectedPaths());
			_resultTree.expandNode(_targetDirectoryNode);
		});
		_targetSelectionPane.onRenamed((x, y) -> {
			_targetDirectoryNode.replaceWith(_targetSelectionPane.selectedPaths());
			_resultTree.expandNode(_targetDirectoryNode);
		});
		_targetSelectionPane.onRemoved(x -> {
			_targetDirectoryNode.replaceWith(_targetSelectionPane.selectedPaths());
			_resultTree.expandNode(_targetDirectoryNode);
		});
		_matchedPane.onSelected(path -> {
			debug(3, "FileGroupSearchDialog.matchedPane.onSelected(%s)", path);
			int loc = _matchedPane.getDividerLocation();
			_matchedPane.select(path);
			SwingUtilities.invokeLater(() -> _matchedPane.setDividerLocation(loc));
		});
		_targetPane.onSelected(path -> {
			debug(3, "FileGroupSearchDialog.targetPane.onSelected(%s)", path);
			int loc = _targetPane.getDividerLocation();
			_targetPane.select(path);
			SwingUtilities.invokeLater(() -> _targetPane.setDividerLocation(loc));
		});
		_sourceTree.loadDirectory(configuration.getDirectory());
		_destinationTree.loadDirectory(configuration.getDirectory());
	}

	@Override
	public void search() {
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
		_buttons.searchButton.setEnabled(false);
		_buttons.applyButton.setEnabled(false);
		SwingUtilities.invokeLater(() -> _basePane.setDividerLocation(loc));
		_background = new Thread(() -> new WorkInProgress().run(() -> doSearch()));
		_background.start();
	}

	@Override
	public void apply() {
		debug(3, "FileGroupSearchDialog::apply");
		_buttons.searchButton.setEnabled(false);
		_buttons.applyButton.setEnabled(false);
		_background = new Thread(() -> new WorkInProgress().run(() -> doApply()));
		_background.start();
	}

	private void doSearch() {
		_currentMatchedFileTreeNode = null;
		_sources.clear();
		_destinations.clear();
		for (Path directory : _sourceTree.selectedPaths()) {
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
		for (Path directory : _destinationTree.selectedPaths()) {
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
			model.addKey(key);
			model.addMatchedFiles(key, s);
			if (d != null) {
				model.addTargetDirectories(key, d);
			}
		}
		String[] keys = model.keys();
		invokeLater(() -> {
			_resultLabel.setText(String.format("File groups: %d", keys.length));
			_buttons.applyButton.setEnabled(true);
			_buttons.searchButton.setEnabled(true);
			model.reloadRoot();
		});
		for (String key : keys) {
			invokeLater(() -> {
				_resultTree.expandNode(model.keyNode(key));
			});
			TargetDirectoryTreeNode node = model.targetDirectoryNode(key);
			invokeLater(() -> {
				_resultTree.expandNode(node);
			});
		}
		invokeLater(() -> {
			_targetSelectionPane.loadFrom(configuration.getDirectory(), _sourceTree.selectedPaths());
		});
	}

	private void doApply() {
		FileGroupSearchTreeModel model = (FileGroupSearchTreeModel)_resultTree.model();
		for (String key : model.keys()) {
			List<Path> from = model.from(key);
			List<Path> to = model.to(key);
			if (from.size() >= 1 && to.size() == 1) {
				if (!invokeLater(() -> {
					fileManager.move(from, to.get(0), e -> mainFrame.showErrorDialog(e));
				})) return;
				model.removeKey(key);
				model.reload(model.root());
			}
		}
		String[] keys = model.keys();
		if (!invokeLater(() -> {
			mainFrame.reloadDirectory();
			_buttons.searchButton.setEnabled(true);
			_buttons.applyButton.setEnabled(keys.length > 0);
		})) return;
		for (String key : keys) {
			TargetDirectoryTreeNode node = model.targetDirectoryNode(key);
			invokeLater(() -> {
				_resultTree.expandNode(node);
			});
		}
	}

}
