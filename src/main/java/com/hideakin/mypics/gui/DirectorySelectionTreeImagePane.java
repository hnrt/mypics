package com.hideakin.mypics.gui;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import com.hideakin.mypics.gui.component.SelectablePathTree;
import com.hideakin.mypics.gui.dialog.CreateDirectoryDialog;
import com.hideakin.mypics.gui.dialog.RenameDirectoryDialog;
import com.hideakin.mypics.gui.model.DirectorySelectionTreeModel;
import com.hideakin.mypics.gui.model.SelectablePathTreeNode;
import com.hideakin.mypics.model.SelectablePath;
import com.hideakin.mypics.util.function.BiConsumerList;
import com.hideakin.mypics.util.function.ConsumerList;

import static com.hideakin.mypics.Application.debug;
import static com.hideakin.mypics.Application.mainFrame;

public class DirectorySelectionTreeImagePane extends JPanel {

	private static final long serialVersionUID = 6447276821314247832L;

	public static DirectorySelectionTreeImagePane create() {
		return new DirectorySelectionTreeImagePane();
	}

	protected final JLabel _title = new JLabel("?");
	protected final JSplitPane _splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
	protected final SelectablePathTree _directoryTree = new SelectablePathTree(new DirectorySelectionTreeModel(), true);
	protected final JScrollPane _scrollPane = new JScrollPane(_directoryTree);
	protected final MultiImagePane _multiImagePane = MultiImagePane.create();
	protected final JPopupMenu _popup = new JPopupMenu();
	protected final JMenuItem _createMenuItem = new JMenuItem("Create directory");
	protected final JMenuItem _renameMenuItem = new JMenuItem("Rename directory");
	protected final JMenuItem _removeMenuItem = new JMenuItem("Remove directory");
	protected final ConsumerList<Path> _onCreated = new ConsumerList<>();
	protected final BiConsumerList<Path, Path> _onRenamed = new BiConsumerList<>();
	protected final ConsumerList<Path> _onRemoved = new ConsumerList<>();

	protected DirectorySelectionTreeImagePane() {
		super(new BorderLayout());
		add(_title, BorderLayout.NORTH);
		add(_splitPane, BorderLayout.CENTER);
		_splitPane.setLeftComponent(_scrollPane);
		_splitPane.setRightComponent(_multiImagePane);
		_splitPane.setDividerLocation(400);
		_popup.add(_createMenuItem);
		_popup.add(_renameMenuItem);
		_popup.add(_removeMenuItem);
		_directoryTree.addMouseListener(new MouseAdapter() {
		    @Override public void mousePressed(MouseEvent e) { showMenu(e); }
		    @Override public void mouseReleased(MouseEvent e) { showMenu(e); }
		    private void showMenu(MouseEvent e) {
		        if (!e.isPopupTrigger()) return;
		        int x = e.getX();
		        int y = e.getY();
		        TreePath path = _directoryTree.getPathForLocation(x, y);
		        if (path == null) return;
		        _directoryTree.setSelectionPath(path);
		        _popup.show(_directoryTree, x, y);
		    }
		});
		_createMenuItem.addActionListener(e -> {
			TreePath treePath = _directoryTree.getSelectionPath();
			if (treePath != null && treePath.getLastPathComponent() instanceof SelectablePathTreeNode sptn) {
				debug(3, "FileGroupSearchDialog::targetDirectoryCreateMenuItem: %s", sptn.path());
				CreateDirectoryDialog dialog = CreateDirectoryDialog.create(sptn.path(), path -> {
					debug(3, "FileGroupSearchDialog::targetDirectoryCreateMenuItem: new=%s", path);
					try {
						if (Files.exists(path)) {
							mainFrame.showErrorDialog("Already exists:\n\n" + path.toString());
						} else {
							Files.createDirectories(path);
							_directoryTree.addDirectory(path, true);
							_directoryTree.select(path);
							_onCreated.invoke(path);
						}
					} catch (Exception ex) {
						mainFrame.showErrorDialog(ex);
					}
				});
				dialog.showDialog();
			}
		});
		_renameMenuItem.addActionListener(e -> {
			TreePath treePath = _directoryTree.getSelectionPath();
			if (treePath != null && treePath.getLastPathComponent() instanceof SelectablePathTreeNode sptn) {
				debug(3, "FileGroupSearchDialog::targetDirectoryRenameMenuItem: %s", sptn.path());
				RenameDirectoryDialog dialog = RenameDirectoryDialog.create(sptn.path(), path -> {
					debug(3, "FileGroupSearchDialog::targetDirectoryRenameMenuItem: new=%s", path);
					try {
						Files.move(sptn.path(), path);
						_directoryTree.remove(sptn.path());
						_directoryTree.addDirectory(path, sptn.selected());
						_onRenamed.invoke(sptn.path(), path);
					} catch (Exception ex) {
						mainFrame.showErrorDialog(ex);
					}
				});
				dialog.showDialog();
			}
		});
		_removeMenuItem.addActionListener(e -> {
			TreePath path = _directoryTree.getSelectionPath();
			if (path != null && path.getLastPathComponent() instanceof SelectablePathTreeNode sptn) {
				debug(3, "FileGroupSearchDialog::targetDirectoryRemoveMenuItem: %s", sptn.path());
				try {
					Files.delete(sptn.path());
					SelectablePathTreeNode parentNode = (SelectablePathTreeNode)sptn.getParent();
					parentNode.remove(sptn);
					_directoryTree.model().reload(parentNode);
					_onRemoved.invoke(sptn.path());
				} catch (Exception ex) {
					ex.printStackTrace();
					mainFrame.showErrorDialog(String.format("Failed to delete directory.\n\n%s", sptn.path()));
				}
			}
		});
	}

	public void onChanged(Consumer<SelectablePath> callback) {
		_directoryTree.onChanged(callback);
	}

	public void onSelected(Consumer<DefaultMutableTreeNode> callback) {
		_directoryTree.onSelected(callback);
	}

	public void onCreated(Consumer<Path> callback) {
		_onCreated.add(callback);
	}

	public void onRenamed(BiConsumer<Path, Path> callback) {
		_onRenamed.add(callback);
	}

	public void onRemoved(Consumer<Path> callback) {
		_onRemoved.add(callback);
	}

	public Path[] selectedPaths() {
		return _directoryTree.selectedPaths();
	}

	public boolean setSelected(Path path, boolean selected, boolean cascaded) {
		return _directoryTree.setSelected(path, selected, cascaded);
	}

	public void loadFrom(Path directory, Path[] toBeDisabled) {
		_directoryTree.root().removeAllChildren();
		_directoryTree.loadDirectory(directory);
		for (Path path : toBeDisabled) {
			_directoryTree.setEnabled(path, false, false);
		}
	}

	public void loadImagesFrom(Path path) {
		try {
			_multiImagePane.loadFrom(Files.list(path).filter(e -> Files.isRegularFile(e)).toList());
		} catch (Exception e) {
			e.printStackTrace();
			_multiImagePane.clear();
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

	public void select(Path path) {
		_directoryTree.select(path);
	}

}
