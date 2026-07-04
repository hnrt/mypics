package com.hideakin.mypics.gui.dialog;

import java.awt.BorderLayout;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import com.hideakin.mypics.gui.ImagePane;
import com.hideakin.mypics.gui.component.SelectablePathTree;
import com.hideakin.mypics.gui.component.SelectableThumbnailedPathTree;
import com.hideakin.mypics.gui.model.DuplicateFileSearchTreeModel;
import com.hideakin.mypics.gui.model.SelectablePathTreeNode;
import com.hideakin.mypics.gui.util.WorkInProgress;
import com.hideakin.mypics.io.FileUtils;
import com.hideakin.mypics.model.PathNode;
import com.hideakin.mypics.model.SelectablePath;

import static com.hideakin.mypics.Application.configuration;
import static com.hideakin.mypics.Application.fileManager;
import static com.hideakin.mypics.Application.mainFrame;
import static com.hideakin.mypics.Application.debug;

public class DuplicateFileSearchDialog extends ModalDialog {

	private static final long serialVersionUID = -8248798840590139117L;

	public static DuplicateFileSearchDialog create() {
		return new DuplicateFileSearchDialog();
	}

	private final JSplitPane _mainPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
	private final JSplitPane _listPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
	private final SelectablePathTree _dTree = new SelectablePathTree();
	private final JPanel _dPane = new JPanel(new BorderLayout());
	private final JLabel _dLabel = new JLabel("Target directories");
	private final JScrollPane _dScroll = new JScrollPane(_dTree);
	private final SelectableThumbnailedPathTree _fTree = new SelectableThumbnailedPathTree(new DuplicateFileSearchTreeModel());
	private final JPanel _fPane = new JPanel(new BorderLayout());
	private final JLabel _fLabel = new JLabel("Duplicate files");
	private final JScrollPane _fScroll = new JScrollPane(_fTree);
	private final ImagePane _imagePane = ImagePane.create();
	private final Map<String, PathNode> _hashes = new HashMap<>();
	private final AtomicInteger _state = new AtomicInteger(0);
	private final AtomicInteger _count = new AtomicInteger(0);
	private Thread _background;

	private DuplicateFileSearchDialog() {
		super("Select directories and click Check button to start the detection.");
		getContentPane().setLayout(new BorderLayout());
		add(_mainPane, BorderLayout.CENTER);
		_mainPane.setLeftComponent(_listPane);
		_mainPane.setRightComponent(_imagePane);
		_mainPane.setDividerLocation(600);
        _listPane.setTopComponent(_dPane);
        _listPane.setBottomComponent(_fPane);
        _listPane.setDividerLocation(300);
        _dPane.add(_dLabel, BorderLayout.NORTH);
        _dPane.add(_dScroll, BorderLayout.CENTER);
        _fPane.add(_fLabel, BorderLayout.NORTH);
        _fPane.add(_fScroll, BorderLayout.CENTER);
		_buttons.testButton.setVisible(true);
		_buttons.testButton.setEnabled(false);
		_buttons.applyButton.setEnabled(false);
		setSize(1200, 800);
        _dTree.onChanged(x -> {
        	_dTree.setSelected(x.path(), x.selected(), true);
        	Path[] checked = _dTree.checked();
        	_buttons.testButton.setEnabled(checked.length > 0);
        	_dLabel.setText(checked.length > 0 ? String.format("Target directories: %d directories selected.", checked.length) : "Target directories");
        });
		_fTree.onSelected(x -> {
			debug(3, "_fTree::onSelected: %s", x != null ? x.getClass().getName() : "null");
			if (x instanceof SelectablePathTreeNode spNode) {
				SelectablePath sp = spNode.selectablePath();
				if (sp.type() == SelectablePath.REGULAR_FILE) {
					_imagePane.loadFrom(sp.path());
					return;
				}
			}
			_imagePane.loadFrom(null);
		});
		_dTree.loadDirectory(configuration.getDirectory());
	}

	private void run() {
		_hashes.clear();
		_count.set(0);
		Path[] checked = _dTree.checked();
		if (!invokeLater(() -> {
			_dLabel.setText(String.format("Target directories: %d directories selected.", checked.length));
		})) return;
		for (Path path : checked) {
			if (_state.get() == -1) {
				return;
			}
			walk(path);
		}
		DuplicateFileSearchTreeModel model = (DuplicateFileSearchTreeModel)_fTree.model();
		for (Map.Entry<String, PathNode> entry : _hashes.entrySet()) {
			if (_state.get() == -1) {
				return;
			}
			PathNode node = entry.getValue();
			if (node.next() != null) {
				String key = entry.getKey();
				model.addKey(key);
				model.addRegularFiles(key, node);
			}
		}
		if (!invokeLater(() -> {
			model.reload();
		})) return;
		String[] keys = model.keys();
		int duplicated = keys.length;
		if (!invokeLater(() -> {
			if (duplicated > 0) {
				setTitle(String.format("Read %d files. Click Apply button to start removing files.", _count.get()));
				_fLabel.setText(String.format("Duplicate files: %d %s detected. Check to leave and uncheck to remove.", duplicated, duplicated > 1 ? "duplicate sets" : "duplicate set"));
				_buttons.applyButton.setEnabled(true);
			} else {
				setTitle(String.format("Read %d files.", _count.get()));
				_fLabel.setText(String.format("Duplicate files: No duplicate sets detected."));
			}
			_buttons.testButton.setEnabled(true);
		})) return;
		if (duplicated == 0) {
			return;
		}
		for (String key : keys) {
			if (!invokeLater(() -> {
				_fTree.expandNode(model.keyNode(key));
			})) return;
		}
	}

	private void walk(Path directory) {
		try {
			if (_state.get() == -1) {
				return;
			}
			Files.list(directory).filter(e -> Files.isRegularFile(e)).forEach(e -> {
				if (!invokeLater(() -> {
					setTitle(String.format("Read %d files (now reading %s)", _count.get(), e));
				})) return;
				String hash = FileUtils.computeSHA256(e);
				if (hash != null) {
					try {
						hash = String.format("%s:%d", hash, Files.size(e));
						PathNode node = _hashes.get(hash);
						if (node != null) {
							debug(3, "DuplicationDialog::walk: %s", e);
							node.add(e);
						} else {
							_hashes.put(hash, PathNode.of(e));
						}
					} catch (Exception x) {
						x.printStackTrace();
					}
				}
				_count.incrementAndGet();
			});
			Files.list(directory).filter(e -> Files.isDirectory(e)).forEach(e -> walk(e));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void test() {
		debug(3, "DuplicateFileSearchDialog::test");
		_fTree.root().removeAllChildren();
		_fTree.model().reload();
		_buttons.testButton.setEnabled(false);
		_buttons.applyButton.setEnabled(false);
		_background = new Thread(() -> new WorkInProgress().run(() -> run()));
		_background.start();
	}

	@Override
	public void apply() {
		debug(3, "DuplicateFileSearchDialog::apply");
		new WorkInProgress().run(() -> {
			List<Path> toBeRemoved = new ArrayList<>();
			DuplicateFileSearchTreeModel model = (DuplicateFileSearchTreeModel)_fTree.model();
			for (String key : model.keys()) {
				SelectablePath[] paths = model.paths(key);
				if (SelectablePath.countSelected(paths) == 0) {
					debug(1, "%s Removing all files is not allowed for safety.", key);
					continue;
				}
				for (SelectablePath next : paths) {
					if (!next.selected()) {
						debug(1, "TO BE DELETED: %s", next.path());
						toBeRemoved.add(next.path());
					}
				}
			}
			if (toBeRemoved.size() > 0) {
				fileManager.remove(toBeRemoved, e -> mainFrame.showErrorDialog(e));
				mainFrame.reloadDirectory();
			}
		});
		super.apply();
	}

	@Override
	public void cancel() {
		debug(3, "DuplicateFileSearchDialog::cancel");
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
