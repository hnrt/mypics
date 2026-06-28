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

import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import com.hideakin.mypics.gui.ImagePane;
import com.hideakin.mypics.gui.component.SelectablePathTree;
import com.hideakin.mypics.gui.component.SelectableThumbnailedPathTree;
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
	private final SelectableThumbnailedPathTree _fTree = new SelectableThumbnailedPathTree();
	private final ImagePane _imagePane = ImagePane.create();
	private final Map<String, PathNode> _hashes = new HashMap<>();
	private final AtomicInteger _state = new AtomicInteger(0);
	private final AtomicInteger _count = new AtomicInteger(0);
	private Thread _background = new Thread(() -> run());

	private DuplicateFileSearchDialog() {
		super("Select directories to check duplicate files");
		getContentPane().setLayout(new BorderLayout());
		add(_mainPane, BorderLayout.CENTER);
		_mainPane.setLeftComponent(_listPane);
		_mainPane.setRightComponent(_imagePane);
		_mainPane.setDividerLocation(600);
        _listPane.setTopComponent(new JScrollPane(_dTree));
        _listPane.setBottomComponent(new JScrollPane(_fTree));
        _listPane.setDividerLocation(300);
		_dTree.loadSubdirectories(configuration.getDirectory());
		_fTree.onSelected(path -> _imagePane.loadFrom(path));
        _buttons.applyButton.setText("Check");
        _buttons.applyButton.setMnemonic(KeyEvent.VK_K);
        _buttons.applyButton.setEnabled(false);
        _dTree.onChanged(x -> {
        	_buttons.applyButton.setEnabled(_dTree.checked().length > 0);
        });
		setSize(1200, 800);
	}

	private void run() {
		new WorkInProgress().run(() -> {
			_hashes.clear();
			_count.set(0);
			_state.set(1);
			for (Path path : _dTree.checked()) {
				if (_state.get() == -1) {
					return;
				}
				walk(path);
			}
			for (Map.Entry<String, PathNode> entry : _hashes.entrySet()) {
				if (_state.get() == -1) {
					return;
				}
				PathNode node = entry.getValue();
				if (node.next() != null) {
					String key = entry.getKey();
					do {
						_fTree.add(key, node.path(), true);
					} while ((node = node.next()) != null);
				}
			}
			int duplicated = _fTree.tags().length;
			if (!invokeLater(() -> {
				if (duplicated > 0) {
					setTitle(String.format("Detected %d %s. Check to leave and uncheck to remove.", duplicated, duplicated > 1 ? "duplicate sets" : "duplicate set"));
					_buttons.applyButton.setText("Apply");
					_buttons.applyButton.setMnemonic(KeyEvent.VK_A);
					_buttons.applyButton.setEnabled(true);
				} else {
					setTitle(String.format("Detected no duplicate files."));
				}
			})) return;
			if (duplicated == 0) {
				if (reset()) _background = new Thread(() -> run());
				return;
			}
			if (!invokeLater(() -> {
				_dTree.setEnabled(false);
				_fTree.reloadRoot();
			})) return;
			String[] tags = _fTree.tags();
			for (String tag : tags) {
				if (!invokeLater(() -> {
					_fTree.expandTag(tag);
				})) return;
			}
		});
	}

	private void walk(Path directory) {
		try {
			if (_state.get() == -1) {
				return;
			}
			Files.list(directory).filter(e -> Files.isRegularFile(e)).forEach(e -> {
				if (!invokeLater(() -> {
					setTitle(String.format("Loaded %d files (now loading %s)", _count.get(), e));
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

	private boolean invokeLater(Runnable x) {
		while (!_state.compareAndSet(1, 2)) {
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
				_state.compareAndSet(2, 1);
			}
		});
		return true;
	}

	private boolean reset() {
		while (!_state.compareAndSet(1, 0)) {
			if (_state.get() == -1) {
				return false;
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException ie) {
			}
		}
		return true;
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
