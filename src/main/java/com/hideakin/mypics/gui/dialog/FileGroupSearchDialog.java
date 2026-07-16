package com.hideakin.mypics.gui.dialog;

import static com.hideakin.mypics.Application.configuration;
import static com.hideakin.mypics.Application.mainFrame;
import static com.hideakin.mypics.Application.fileManager;
import static com.hideakin.mypics.Application.debug;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;

import com.hideakin.mypics.gui.ImagePane;
import com.hideakin.mypics.gui.MultiImagePane;
import com.hideakin.mypics.gui.component.SelectablePathTree;
import com.hideakin.mypics.gui.model.DirectorySelectionTreeModel;
import com.hideakin.mypics.gui.model.FileGroupSearchTreeModel;
import com.hideakin.mypics.gui.model.MatchedFileTreeNode;
import com.hideakin.mypics.gui.model.SelectablePathTreeNode;
import com.hideakin.mypics.gui.model.TargetDirectoryTreeNode;
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
	private final ImagePane _imagePane = ImagePane.create();
	private final MultiImagePane _multiImagePane = MultiImagePane.create();
	private final JPanel _targetDirectoryPane = new JPanel(new BorderLayout());
	private final JLabel _targetDirectoryLabel = new JLabel("Target directory");
	private final JSplitPane _targetDirectorySplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
	private final SelectablePathTree _targetDirectoryTree = new SelectablePathTree(new DirectorySelectionTreeModel(), true);
	private final JScrollPane _targetDirectoryScroll = new JScrollPane(_targetDirectoryTree);
	private final MultiImagePane _targetDirectoryMultiImagePane = MultiImagePane.create();
	private final JPopupMenu _targetDirectoryPopup = new JPopupMenu();
	private final JMenuItem _targetDirectoryCreateMenuItem = new JMenuItem("Create directory");
	private final JMenuItem _targetDirectoryRenameMenuItem = new JMenuItem("Rename directory");
	private final JMenuItem _targetDirectoryRemoveMenuItem = new JMenuItem("Remove directory");
	private final Map<String, PathNode> _sources = new HashMap<>();
	private final Map<String, PathNode> _destinations = new HashMap<>();
	private Pattern _pattern;
	private Thread _background;
	private TargetDirectoryTreeNode _targetDirectoryNode;

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
		_targetDirectoryPane.add(_targetDirectoryLabel, BorderLayout.NORTH);
		_targetDirectoryPane.add(_targetDirectorySplitPane, BorderLayout.CENTER);
		_targetDirectorySplitPane.setTopComponent(_targetDirectoryScroll);
		_targetDirectorySplitPane.setBottomComponent(_targetDirectoryMultiImagePane);
		_targetDirectorySplitPane.setDividerLocation(300);
		_targetDirectoryPopup.add(_targetDirectoryCreateMenuItem);
		_targetDirectoryPopup.add(_targetDirectoryRenameMenuItem);
		_targetDirectoryPopup.add(_targetDirectoryRemoveMenuItem);
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
			int loc = _basePane.getDividerLocation();
			if (x instanceof SelectablePathTreeNode spNode) {
				SelectablePath sp = spNode.selectablePath();
				if (sp.isRegularFile()) {
					_basePane.setRightComponent(_imagePane);
					_imagePane.loadFrom(sp.path());
				} else if (sp.isDirectory()) {
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
			} else if (x instanceof TargetDirectoryTreeNode tdNode) {
				_targetDirectoryNode = tdNode;
				_basePane.setRightComponent(_targetDirectoryPane);
				for (Path path : _targetDirectoryTree.selectedPaths()) {
					_targetDirectoryTree.setSelected(path, false, true);
				}
				for (Path path : _targetDirectoryNode.list()) {
					_targetDirectoryTree.setSelected(path,  true,  false);
				}
			} else {
				_basePane.setRightComponent(_imagePane);
				_imagePane.loadFrom(null);
			}
			SwingUtilities.invokeLater(() -> _basePane.setDividerLocation(loc));
		});
		_targetDirectoryTree.onChanged(x -> {
			_targetDirectoryNode.replaceWith(_targetDirectoryTree.selectedPaths());
			_resultTree.expandNode(_targetDirectoryNode);
		});
		_targetDirectoryTree.onSelected(x -> {
			if (x instanceof SelectablePathTreeNode spNode) {
				SelectablePath sp = spNode.selectablePath();
				if (sp.isDirectory()) {
					try {
						_targetDirectoryMultiImagePane.loadFrom(Files.list(sp.path()).filter(e -> Files.isRegularFile(e)).toList());
					} catch (Exception e) {
						e.printStackTrace();
						_targetDirectoryMultiImagePane.clear();
					}
				}
			}			
		});
		_targetDirectoryTree.addMouseListener(new MouseAdapter() {
		    @Override public void mousePressed(MouseEvent e) { showMenu(e); }
		    @Override public void mouseReleased(MouseEvent e) { showMenu(e); }
		    private void showMenu(MouseEvent e) {
		        if (!e.isPopupTrigger()) return;
		        int x = e.getX();
		        int y = e.getY();
		        TreePath path = _targetDirectoryTree.getPathForLocation(x, y);
		        if (path == null) return;
		        _targetDirectoryTree.setSelectionPath(path);
		        _targetDirectoryPopup.show(_targetDirectoryTree, x, y);
		    }
		});
		_targetDirectoryCreateMenuItem.addActionListener(e -> {
			TreePath treePath = _targetDirectoryTree.getSelectionPath();
			if (treePath != null && treePath.getLastPathComponent() instanceof SelectablePathTreeNode sptn) {
				debug(3, "FileGroupSearchDialog::targetDirectoryCreateMenuItem: %s", sptn.path());
				CreateDirectoryDialog dialog = CreateDirectoryDialog.create(sptn.path(), path -> {
					debug(3, "FileGroupSearchDialog::targetDirectoryCreateMenuItem: new=%s", path);
					try {
						if (Files.exists(path)) {
							mainFrame.showErrorDialog("Already exists:\n\n" + path.toString());
						} else {
							Files.createDirectories(path);
							_targetDirectoryTree.addDirectory(path, true);
							_targetDirectoryTree.select(path);
							_targetDirectoryNode.replaceWith(_targetDirectoryTree.selectedPaths());
							_resultTree.expandNode(_targetDirectoryNode);
						}
					} catch (Exception ex) {
						mainFrame.showErrorDialog(ex);
					}
				});
				dialog.showDialog();
			}
		});
		_targetDirectoryRenameMenuItem.addActionListener(e -> {
			TreePath treePath = _targetDirectoryTree.getSelectionPath();
			if (treePath != null && treePath.getLastPathComponent() instanceof SelectablePathTreeNode sptn) {
				debug(3, "FileGroupSearchDialog::targetDirectoryRenameMenuItem: %s", sptn.path());
				RenameDirectoryDialog dialog = RenameDirectoryDialog.create(sptn.path(), path -> {
					debug(3, "FileGroupSearchDialog::targetDirectoryRenameMenuItem: new=%s", path);
					try {
						Files.move(sptn.path(), path);
						_targetDirectoryTree.remove(sptn.path());
						_targetDirectoryTree.addDirectory(path, sptn.selected());
						_targetDirectoryNode.replaceWith(_targetDirectoryTree.selectedPaths());
						_resultTree.expandNode(_targetDirectoryNode);
					} catch (Exception ex) {
						mainFrame.showErrorDialog(ex);
					}
				});
				dialog.showDialog();
			}
		});
		_targetDirectoryRemoveMenuItem.addActionListener(e -> {
			TreePath path = _targetDirectoryTree.getSelectionPath();
			if (path != null && path.getLastPathComponent() instanceof SelectablePathTreeNode sptn) {
				debug(3, "FileGroupSearchDialog::targetDirectoryRemoveMenuItem: %s", sptn.path());
				try {
					Files.delete(sptn.path());
					SelectablePathTreeNode parentNode = (SelectablePathTreeNode)sptn.getParent();
					parentNode.remove(sptn);
					_targetDirectoryTree.model().reload(parentNode);
					_targetDirectoryNode.replaceWith(_targetDirectoryTree.selectedPaths());
					_resultTree.expandNode(_targetDirectoryNode);
				} catch (Exception ex) {
					ex.printStackTrace();
					mainFrame.showErrorDialog(String.format("Failed to delete directory.\n\n%s", sptn.path()));
				}
			}
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
		_basePane.setRightComponent(_imagePane);
		_imagePane.loadFrom(null);
		_buttons.searchButton.setEnabled(false);
		_buttons.applyButton.setEnabled(false);
		SwingUtilities.invokeLater(() -> _basePane.setDividerLocation(loc));
		_background = new Thread(() -> new WorkInProgress().run(() -> doSearch()));
		_background.start();
	}

	@Override
	public void apply() {
		debug(3, "FileGroupSearchDialog::apply");
		_basePane.setRightComponent(_imagePane);
		_imagePane.loadFrom(null);
		_buttons.searchButton.setEnabled(false);
		_buttons.applyButton.setEnabled(false);
		_background = new Thread(() -> new WorkInProgress().run(() -> doApply()));
		_background.start();
	}

	private void doSearch() {
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
			loadTargetDirectoryTree();
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

	private void loadTargetDirectoryTree() {
		_targetDirectoryTree.root().removeAllChildren();
		_targetDirectoryTree.loadDirectory(configuration.getDirectory());
		for (Path path : _sourceTree.selectedPaths()) {
			((DirectorySelectionTreeModel)_targetDirectoryTree.model()).setEnabled(path, false, false);
		}
	}

}
