package com.hideakin.mypics.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import com.hideakin.mypics.gui.WorkInProgress;
import com.hideakin.mypics.gui.renderer.SelectablePathTreeCellEditor;
import com.hideakin.mypics.gui.renderer.SelectablePathTreeCellRenderer;
import com.hideakin.mypics.gui.renderer.SelectableThumbnailedPathTreeCellEditor;
import com.hideakin.mypics.gui.renderer.SelectableThumbnailedPathTreeCellRenderer;
import com.hideakin.mypics.gui.util.ImageLoader;
import com.hideakin.mypics.gui.util.ScalingMode;
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

	private final JSplitPane _mainPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
	private final JSplitPane _listPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
	private final DefaultMutableTreeNode _dRoot = new DefaultMutableTreeNode("ROOT");
	private final DefaultMutableTreeNode _fRoot = new DefaultMutableTreeNode("ROOT");
	private final JTree _dTree = new JTree(_dRoot);
	private final JTree _fTree = new JTree(_fRoot);
	private final JScrollPane _imagePane = new JScrollPane();
	private final JLabel _imageLabel = new JLabel();
	private final Map<String, PathNode> _hashes = new HashMap<>();
	private final AtomicInteger _state = new AtomicInteger(0);
	private final AtomicInteger _count = new AtomicInteger(0);
	private final Map<Path, Icon> _icons = new HashMap<>(8192);
	private Thread _background = new Thread(() -> run());

	private DuplicationDialog() {
		super("Detect duplicate files");
		getContentPane().setLayout(new BorderLayout());
		add(_mainPane, BorderLayout.CENTER);
		_mainPane.setLeftComponent(_listPane);
		_mainPane.setRightComponent(_imagePane);
		_mainPane.setDividerLocation(600);
        _listPane.setTopComponent(new JScrollPane(_dTree));
        _listPane.setBottomComponent(new JScrollPane(_fTree));
        _listPane.setDividerLocation(300);
		_dTree.setRootVisible(false);
		_dTree.setCellRenderer(new SelectablePathTreeCellRenderer());
		_dTree.setCellEditor(new SelectablePathTreeCellEditor());
		_dTree.setEditable(true);
		_fTree.setRootVisible(false);
		_fTree.setCellRenderer(new SelectableThumbnailedPathTreeCellRenderer(_icons));
		_fTree.setCellEditor(new SelectableThumbnailedPathTreeCellEditor(_icons));
		_fTree.setEditable(true);
		_imagePane.setViewportView(_imageLabel);
		_imageLabel.setHorizontalAlignment(JLabel.CENTER);
		try {
			List<Path> entries = Files.list(configuration.getDirectory()).toList();
			List<Path> dd = entries.stream().filter(e -> Files.isDirectory(e)).collect(Collectors.toList());
			dd.sort(Comparator.comparing(e -> e.getFileName().toString()));
			for (Path path : dd) {
				_dRoot.add(new DefaultMutableTreeNode(new SelectablePath(path, false)));
			}
			DefaultTreeModel model = (DefaultTreeModel)_dTree.getModel();
			model.reload();
		} catch (Exception e) {
			e.printStackTrace();
		}
		InputMap im = _fTree.getInputMap(JComponent.WHEN_FOCUSED);
		im.put(KeyStroke.getKeyStroke("SPACE"), "toggle-check");
		ActionMap am = _fTree.getActionMap();
		am.put("toggle-check", new AbstractAction() {
		    @Override
		    public void actionPerformed(ActionEvent e) {
		    	TreePath path = _fTree.getSelectionPath();
		    	if (path == null) return;
		    	Object obj = ((DefaultMutableTreeNode)path.getLastPathComponent()).getUserObject();
		    	if (obj instanceof SelectablePath sp) {
		    		sp.toggle();
		    		_fTree.repaint();
		    	}
		    }
		});
		_fTree.addTreeSelectionListener(e -> {
			TreePath path = e.getNewLeadSelectionPath();
		    if (path != null) {
		    	Object obj = ((DefaultMutableTreeNode)path.getLastPathComponent()).getUserObject();
		        if (obj instanceof SelectablePath sp) {
		        	try {
		        		BufferedImage image = ImageLoader.loadCorrectedImage(sp.path().toFile());
		        		double scale = ImageLoader.computeScale(image, ScalingMode.FIT_TO_WINDOW, _imagePane);
		        		Rectangle rect = ImageLoader.computeSizeByScale(image, scale);
		        		_imageLabel.setIcon(new ImageIcon(image.getScaledInstance(rect.width, rect.height, Image.SCALE_SMOOTH)));
		                _imageLabel.revalidate();
		                return;
		        	} catch (Exception ex) {
		        	}
		        }
			}
		    _imageLabel.setIcon(null);
		});
        _buttons.applyButton.setText("Check");
        _buttons.applyButton.setMnemonic(KeyEvent.VK_K);
		setSize(1200, 800);
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
					_dTree.setEnabled(false);
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
