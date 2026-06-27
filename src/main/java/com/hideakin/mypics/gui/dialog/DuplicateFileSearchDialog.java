package com.hideakin.mypics.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.Rectangle;
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

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import com.hideakin.mypics.gui.WorkInProgress;
import com.hideakin.mypics.gui.component.SelectableThumbnailedPathTree;
import com.hideakin.mypics.gui.renderer.SelectablePathTreeCellEditor;
import com.hideakin.mypics.gui.renderer.SelectablePathTreeCellRenderer;
import com.hideakin.mypics.gui.util.ImageLoader;
import com.hideakin.mypics.gui.util.ScalingMode;
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
	private final DefaultMutableTreeNode _dRoot = new DefaultMutableTreeNode("ROOT");
	private final JTree _dTree = new JTree(_dRoot);
	private final SelectableThumbnailedPathTree _fTree = new SelectableThumbnailedPathTree();
	private final JScrollPane _imagePane = new JScrollPane();
	private final JLabel _imageLabel = new JLabel();
	private final Map<String, PathNode> _hashes = new HashMap<>();
	private final AtomicInteger _state = new AtomicInteger(0);
	private final AtomicInteger _count = new AtomicInteger(0);
	private Thread _background = new Thread(() -> run());

	private DuplicateFileSearchDialog() {
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
		_fTree.onSelected(path -> {
        	try {
	    		BufferedImage image = ImageLoader.loadCorrectedImage(path.toFile());
	    		double scale = ImageLoader.computeScale(image, ScalingMode.FIT_TO_WINDOW, _imagePane);
	    		Rectangle rect = ImageLoader.computeSizeByScale(image, scale);
	    		_imageLabel.setIcon(new ImageIcon(image.getScaledInstance(rect.width, rect.height, Image.SCALE_SMOOTH)));
	            _imageLabel.revalidate();
	            return;
	    	} catch (Exception ex) {
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
					do {
						_fTree.add(entry.getKey(), node.path(), true);
					} while ((node = node.next()) != null);
				}
			}
			int duplicated = _fTree.tags().length;
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
					_fTree.reloadRoot();
				} finally {
					_state.compareAndSet(2, 1);
				}
			});
			String[] tags = _fTree.tags();
			for (String tag : tags) {
				while (!_state.compareAndSet(1, 2)) {
					if (_state.get() == -1) {
						return;
					}
				}
				SwingUtilities.invokeLater(() -> {
					try {
						_fTree.expandTag(tag);
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
				for (String tag : _fTree.tags()) {
					SelectablePath[] paths = _fTree.paths(tag);
					if (SelectablePath.countSelected(paths) == 0) {
						debug(1, "%s Removing all files is not allowed for safety.", tag);
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
	}

	@Override
	public void cancel() {
		_state.set(-1);
		super.cancel();
	}

}
