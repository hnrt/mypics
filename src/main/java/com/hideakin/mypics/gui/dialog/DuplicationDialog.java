package com.hideakin.mypics.gui.dialog;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.Icon;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import com.hideakin.mypics.gui.WorkInProgress;
import com.hideakin.mypics.gui.renderer.SelectablePathTreeCellEditor;
import com.hideakin.mypics.gui.renderer.SelectablePathTreeCellRenderer;
import com.hideakin.mypics.gui.renderer.SelectableThumbnailedPathTreeCellEditor;
import com.hideakin.mypics.gui.renderer.SelectableThumbnailedPathTreeCellRenderer;
import com.hideakin.mypics.io.FileUtils;
import com.hideakin.mypics.model.PathNode;
import com.hideakin.mypics.model.SelectablePath;
import com.hideakin.mypics.model.TagNode;

import static com.hideakin.mypics.Application.configuration;
import static com.hideakin.mypics.Application.fileManager;
import static com.hideakin.mypics.Application.mainFrame;
import static com.hideakin.mypics.Application.debug;

public class DuplicationDialog extends ModalDialog {

	private static final long serialVersionUID = -8248798840590139117L;

	public static DuplicationDialog create() {
		return new DuplicationDialog();
	}

	private JSplitPane _splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
	private DefaultMutableTreeNode _dRoot = new DefaultMutableTreeNode("ROOT");
	private DefaultMutableTreeNode _fRoot = new DefaultMutableTreeNode("ROOT");
	private JTree _dTree = new JTree(_dRoot);
	private JTree _fTree = new JTree(_fRoot);
	private Thread _background = new Thread(() -> run());
	private Map<String, PathNode> _hashes = new HashMap<>();
	private AtomicInteger _state = new AtomicInteger(0);
	private AtomicInteger _count = new AtomicInteger(0);
	private Map<Path, Icon> _icons = new HashMap<>(8192);

	private DuplicationDialog() {
		super("Detect duplicate files");
		getContentPane().setLayout(new BorderLayout());
		add(_splitPane, BorderLayout.CENTER);
		try {
			Files.list(configuration.getDirectory()).filter(e -> Files.isDirectory(e)).forEach(e ->
				_dRoot.add(new DefaultMutableTreeNode(new SelectablePath(e, false))));
			DefaultTreeModel model = (DefaultTreeModel)_dTree.getModel();
			model.reload();
		} catch (Exception e) {
			e.printStackTrace();
		}
		_dTree.setRootVisible(false);
		_dTree.setCellRenderer(new SelectablePathTreeCellRenderer());
		_dTree.setCellEditor(new SelectablePathTreeCellEditor());
		_dTree.setEditable(true);
		_fTree.setRootVisible(false);
		_fTree.setCellRenderer(new SelectableThumbnailedPathTreeCellRenderer(_icons));
		_fTree.setCellEditor(new SelectableThumbnailedPathTreeCellEditor(_icons));
		_fTree.setEditable(true);
        _splitPane.setTopComponent(new JScrollPane(_dTree));
        _splitPane.setBottomComponent(new JScrollPane(_fTree));
        _splitPane.setDividerLocation(150);
        _buttons.applyButton.setText("Check");
        _buttons.applyButton.setMnemonic(KeyEvent.VK_K);
		setSize(800, 400);
	}

	private void run() {
		new WorkInProgress().run(() -> {
			_hashes.clear();
			_count.set(0);
			_state.set(1);
			int n = _dRoot.getChildCount();
			for (int i = 0; i < n; i++) {
				if (_state.get() == -1) {
					return;
				}
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)_dRoot.getChildAt(i);
				SelectablePath sp = (SelectablePath)node.getUserObject();
				if (sp.selected()) {
					walk(sp.path());
				}
			}
			for (Map.Entry<String, PathNode> entry : _hashes.entrySet()) {
				if (_state.get() == -1) {
					return;
				}
				PathNode node = entry.getValue();
				if (node.next() != null) {
					DefaultMutableTreeNode parent = new DefaultMutableTreeNode(new TagNode(entry.getKey()));
					do {
						parent.add(new DefaultMutableTreeNode(new SelectablePath(node.path(), true)));
					} while ((node = node.next()) != null);
					_fRoot.add(parent);
				}
			}
			int duplicated = _fRoot.getChildCount();
			while (!_state.compareAndSet(1, 2)) {
				if (_state.get() == -1) {
					return;
				}
			}
			SwingUtilities.invokeLater(() -> {
				try {
					if (duplicated > 0) {
						setTitle(String.format("Detected %d %s. Uncheck to remove.", duplicated, duplicated > 1 ? "duplicate sets" : "duplicate set"));
						_buttons.applyButton.setText("Apply");
						_buttons.applyButton.setMnemonic(KeyEvent.VK_A);
					} else {
						setTitle(String.format("Detected no duplicate sets."));
					}
					_buttons.applyButton.setEnabled(true);
				} finally {
					_state.compareAndSet(2, 1);
				}
			});
			if (duplicated == 0) {
				while (!_state.compareAndSet(1, 0)) {
					if (_state.get() == -1) {
						return;
					}
				}
				_background = new Thread(() -> run());
				return;
			}
			while (!_state.compareAndSet(1, 2)) {
				if (_state.get() == -1) {
					return;
				}
			}
			SwingUtilities.invokeLater(() -> {
				try {
					DefaultTreeModel model = (DefaultTreeModel)_fTree.getModel();
					model.reload(_fRoot);
				} finally {
					_state.compareAndSet(2, 1);
				}
			});
			for (int i = 0; i < duplicated; i++) {
				DefaultMutableTreeNode parent = (DefaultMutableTreeNode)_fRoot.getChildAt(i);
				while (!_state.compareAndSet(1, 2)) {
					if (_state.get() == -1) {
						return;
					}
				}
				SwingUtilities.invokeLater(() -> {
					try {
						DefaultTreeModel model = (DefaultTreeModel)_fTree.getModel();
						model.reload(parent);
						_fTree.expandPath(new TreePath(parent.getPath()));
					} finally {
						_state.compareAndSet(2, 1);
					}
				});
			}
		});
	}

	private void walk(Path directory) {
		try {
			if (_state.get() == -1) {
				return;
			}
			Files.list(directory).filter(e -> Files.isRegularFile(e)).forEach(e -> {
				while (!_state.compareAndSet(1, 2)) {
					if (_state.get() == -1) {
						return;
					}
				}
				SwingUtilities.invokeLater(() -> {
					try {
						setTitle(String.format("Loaded %d files (now loading %s)", _count.get(), e));
					} finally {
						_state.compareAndSet(2, 1);
					}
				});
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
	public void apply() {
		if (_state.compareAndExchange(0, 1) == 0) {
			_buttons.applyButton.setEnabled(false);
			_background.start();
		} else if (_state.get() > 0) {
			_state.set(-1);
			new WorkInProgress().run(() -> {
				List<Path> toBeRemoved = new ArrayList<>();
				int n = _fRoot.getChildCount();
				for (int i = 0; i < n; i++) {
					DefaultMutableTreeNode parent = (DefaultMutableTreeNode)_fRoot.getChildAt(i);
					int m = parent.getChildCount();
					int k = 0;
					for (int j = 0; j < m; j++) {
						SelectablePath sp = (SelectablePath)((DefaultMutableTreeNode)parent.getChildAt(j)).getUserObject();
						if (!sp.selected()) {
							k++;
						}
					}
					if (k == m) continue;
					for (int j = 0; j < m; j++) {
						SelectablePath sp = (SelectablePath)((DefaultMutableTreeNode)parent.getChildAt(j)).getUserObject();
						if (!sp.selected()) {
							debug(1, "TO BE DELETED: %s", sp.path());
							toBeRemoved.add(sp.path());
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
	}

	@Override
	public void cancel() {
		_state.set(-1);
		super.cancel();
	}

}
