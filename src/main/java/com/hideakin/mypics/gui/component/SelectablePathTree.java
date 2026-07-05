package com.hideakin.mypics.gui.component;

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
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import com.hideakin.mypics.gui.model.DirectorySelectionTreeModel;
import com.hideakin.mypics.gui.model.SelectablePathTreeNode;
import com.hideakin.mypics.gui.renderer.SelectablePathTreeCellEditor;
import com.hideakin.mypics.gui.renderer.SelectablePathTreeCellRenderer;
import com.hideakin.mypics.model.SelectablePath;
import com.hideakin.mypics.util.function.ConsumerList;

import static com.hideakin.mypics.Application.debug;

public class SelectablePathTree extends JTree {

	private static final long serialVersionUID = -6326954866653109798L;

	private final DefaultTreeModel _model;
	private final DefaultMutableTreeNode _root;
	private final ConsumerList<DefaultMutableTreeNode> _onSelected = new ConsumerList<>();
	private final ConsumerList<SelectablePath> _onChanged = new ConsumerList<>();
	private final Map<Path, Icon> _icons = new HashMap<>(8192);

	public SelectablePathTree(DefaultTreeModel model) {
		super(model);
		setRootVisible(false);
		setCellRenderer(new SelectablePathTreeCellRenderer(_icons));
		setCellEditor(new SelectablePathTreeCellEditor(_icons, x -> _onChanged.invoke(x)));
		setEditable(true);
		_model = (DefaultTreeModel)super.getModel();
		_root = (DefaultMutableTreeNode)_model.getRoot();
		InputMap im = getInputMap(JComponent.WHEN_FOCUSED);
		im.put(KeyStroke.getKeyStroke("SPACE"), "toggle-check");
		ActionMap am = getActionMap();
		am.put("toggle-check", new AbstractAction() {
		    @Override
		    public void actionPerformed(ActionEvent e) {
		    	SelectablePath sp = SelectablePathTree.this.selected();
		    	if (sp != null) {
		    		sp.toggleSelected();
		    		SelectablePathTree.this.repaint();
		    	}
		    }
		});
		addTreeSelectionListener(e -> {
			if (_model instanceof DirectorySelectionTreeModel) {
			    TreePath path = e.getPath();
			    if (path.getLastPathComponent() instanceof SelectablePathTreeNode sptn) {
				    debug(3, "SelectablePathTree::Selection: %s", sptn.path());
				    if (!sptn.loaded()) {
				    	loadDirectory(sptn.path());
				    }
			    }
			} else {
				TreePath path = e.getNewLeadSelectionPath();
			    if (path != null) {
		        	_onSelected.invoke((DefaultMutableTreeNode)path.getLastPathComponent());
			    }
			}
		});
	}

	public DefaultTreeModel model() {
		return _model;
	}

	public DefaultMutableTreeNode root() {
		return _root;
	}

	public void onSelected(Consumer<DefaultMutableTreeNode> callback) {
		_onSelected.add(callback);
	}

	public void onChanged(Consumer<SelectablePath> callback) {
		_onChanged.add(callback);
	}

	public void expandNode(DefaultMutableTreeNode node) {
		if (node != null) {
			_model.reload(node);
			expandPath(new TreePath(node.getPath()));
		}
	}

	public void loadDirectory(Path directory) {
		if (_model instanceof DirectorySelectionTreeModel model) {
			model.loadDirectory(directory);
			SelectablePathTreeNode node = model.find(directory);
			_model.reload(node);
			expandPath(new TreePath(node.getPath()));
		} else {
			throw new RuntimeException("SelectablePathTree::loadDirectory: Bad model.");
		}
	}

	public boolean setSelected(Path path, boolean selected, boolean cascaded) {
		if (_model instanceof DirectorySelectionTreeModel model) {
			return model.setSelected(path, selected, cascaded);
		} else {
			throw new RuntimeException("SelectablePathTree::loadDirectory: Bad model.");
		}
	}

	public boolean setEnabled(Path path, boolean enabled, boolean cascaded) {
		if (_model instanceof DirectorySelectionTreeModel model) {
			return model.setEnabled(path, enabled, cascaded);
		} else {
			throw new RuntimeException("SelectablePathTree::loadDirectory: Bad model.");
		}
	}

	public Path[] selectedPaths() {
		if (_model instanceof DirectorySelectionTreeModel model) {
			return model.selectedPaths();
		} else {
			throw new RuntimeException("SelectablePathTree::loadDirectory: Bad model.");
		}
	}

	public SelectablePath selected() {
		TreePath path = getSelectionPath();
		if (path != null) {
	    	Object obj = ((DefaultMutableTreeNode)path.getLastPathComponent()).getUserObject();
	    	if (obj instanceof SelectablePath sp) {
	    		return sp;
	    	}
		}
		return null;
	}

}
