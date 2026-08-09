package com.hideakin.mypics.gui.component;

import static com.hideakin.mypics.Application.debug;

import java.awt.event.ActionEvent;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import com.hideakin.mypics.gui.model.FileGroupSearchTreeModel;
import com.hideakin.mypics.gui.model.MatchedFileTreeNode;
import com.hideakin.mypics.gui.model.SelectablePathTreeNode;
import com.hideakin.mypics.gui.renderer.SelectablePathTreeCellEditor;
import com.hideakin.mypics.gui.renderer.SelectablePathTreeCellRenderer;
import com.hideakin.mypics.model.SelectablePath;
import com.hideakin.mypics.util.function.ConsumerList;

public class FileGroupSearchTree extends JTree {

	private static final long serialVersionUID = 5764032431321222309L;

	private final ConsumerList<DefaultMutableTreeNode> _onSelected = new ConsumerList<>();
	private final ConsumerList<SelectablePath> _onChanged = new ConsumerList<>();
	private final Map<Path, Icon> _icons = new HashMap<>(8192);

	public FileGroupSearchTree() {
		super(new FileGroupSearchTreeModel());
		setRootVisible(false);
		setCellRenderer(new SelectablePathTreeCellRenderer(true, _icons));
		setCellEditor(new SelectablePathTreeCellEditor(_icons, x -> _onChanged.invoke(x)));
		setEditable(true);
		InputMap im = getInputMap(JComponent.WHEN_FOCUSED);
		im.put(KeyStroke.getKeyStroke("SPACE"), "toggle-check");
		ActionMap am = getActionMap();
		am.put("toggle-check", new AbstractAction() {
		    @Override
		    public void actionPerformed(ActionEvent e) {
		    	SelectablePath sp = FileGroupSearchTree.this.selected();
		    	if (sp != null) {
		    		sp.toggleSelected();
		    		FileGroupSearchTree.this.repaint();
		    	}
		    }
		});
		addTreeSelectionListener(e -> {
			if (getSelectionCount() == 0) {
	    		debug(3, "FileGroupSearchTree.Selection(null)");
				_onSelected.invoke(null);
			} else {
				TreePath path = e.getNewLeadSelectionPath();
			    if (path != null) {
			    	if (path.getLastPathComponent() instanceof DefaultMutableTreeNode treeNode) {
			    		debug(3, "FileGroupSearchTree.Selection(%s)", treeNode.getClass().getName());
			    		_onSelected.invoke(treeNode);
			    	} else {
			    		debug(0, "FileGroupSearchTree.Selection(%s)", path.getLastPathComponent().getClass().getName());
			    	}
			    } else {
		    		debug(3, "FileGroupSearchTree.Selection(NewLeadSelectionPath=null)");
			    	_onSelected.invoke(null);
			    }
			}
		});
	}

	public FileGroupSearchTreeModel model() {
		if (getModel() instanceof FileGroupSearchTreeModel _model) {
			return _model;
		} else {
			throw new RuntimeException("FileGroupSearchTree::model: Bad model.");
		}		
	}

	public DefaultMutableTreeNode root() {
		if (model().getRoot() instanceof DefaultMutableTreeNode _root) {
			return _root;
		} else {
			throw new RuntimeException("FileGroupSearchTree::root: Bad root.");
		}
	}

	public void onSelected(Consumer<DefaultMutableTreeNode> callback) {
		_onSelected.add(callback);
	}

	public void onChanged(Consumer<SelectablePath> callback) {
		_onChanged.add(callback);
	}

	public void clear() {
		root().removeAllChildren();
		model().reload();
	}

	public void expandNode(DefaultMutableTreeNode node) {
		if (node != null) {
			model().reload(node);
			expandPath(new TreePath(node.getPath()));
		}
	}

	public SelectablePath selected() {
		TreePath path = getSelectionPath();
		if (path != null) {
			if (path.getLastPathComponent() instanceof DefaultMutableTreeNode treeNode) {
				if (treeNode.getUserObject() instanceof SelectablePath sp) {
					return sp;
				}
			}
		}
		return null;
	}

	public void select(String key, Path path) {
		if (key != null) {
			if (getModel() instanceof FileGroupSearchTreeModel model) {
				MatchedFileTreeNode node = model.matchedFileNode(key);
				if (node != null) {
					int n = node.getChildCount();
					for (int i = 0; i < n; i++) {
						if (node.getChildAt(i) instanceof SelectablePathTreeNode next) {
							if (next.path().equals(path)) {
								TreePath tp = new TreePath(next.getPath());
								setSelectionPath(tp);
								scrollPathToVisible(tp);
								return;
							}
						}
					}
					throw new RuntimeException("FileGroupSearchTree::select: Path not found.");
				} else {
					throw new RuntimeException("FileGroupSearchTree::select: Key not found.");
				}
			} else {
				throw new RuntimeException("FileGroupSearchTree::select: Bad model.");
			}
		} else {
			throw new RuntimeException("FileGroupSearchTree::select: Bad key.");
		}
	}

}
