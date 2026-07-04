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

import com.hideakin.mypics.gui.renderer.SelectableThumbnailedPathTreeCellEditor;
import com.hideakin.mypics.gui.renderer.SelectableThumbnailedPathTreeCellRenderer;
import com.hideakin.mypics.model.SelectablePath;
import com.hideakin.mypics.util.function.ConsumerList;

public class SelectableThumbnailedPathTree extends JTree {

	private static final long serialVersionUID = -5173080788780336722L;

	private final DefaultTreeModel _model;
	private final DefaultMutableTreeNode _root;
	private final ConsumerList<DefaultMutableTreeNode> _onSelected = new ConsumerList<>();
	private final Map<Path, Icon> _icons = new HashMap<>(8192);
	private final ConsumerList<SelectablePath> _onChanged = new ConsumerList<>();

	public SelectableThumbnailedPathTree() {
		this(new DefaultTreeModel(new DefaultMutableTreeNode("ROOT")));
	}

	public SelectableThumbnailedPathTree(DefaultTreeModel model) {
		super(model);
		setRootVisible(false);
		setCellRenderer(new SelectableThumbnailedPathTreeCellRenderer(_icons));
		setCellEditor(new SelectableThumbnailedPathTreeCellEditor(_icons, x -> _onChanged.invoke(x)));
		setEditable(true);
		_model = (DefaultTreeModel)super.getModel();
		_root = (DefaultMutableTreeNode)_model.getRoot();
		InputMap im = getInputMap(JComponent.WHEN_FOCUSED);
		im.put(KeyStroke.getKeyStroke("SPACE"), "toggle-check");
		ActionMap am = getActionMap();
		am.put("toggle-check", new AbstractAction() {
		    @Override
		    public void actionPerformed(ActionEvent e) {
		    	SelectablePath sp = SelectableThumbnailedPathTree.this.selected();
		    	if (sp != null) {
		    		sp.toggle();
		    		SelectableThumbnailedPathTree.this.repaint();
		    	}
		    }
		});
		addTreeSelectionListener(e -> {
			TreePath path = e.getNewLeadSelectionPath();
		    if (path != null) {
	        	_onSelected.invoke((DefaultMutableTreeNode)path.getLastPathComponent());
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

	public void expandNode(DefaultMutableTreeNode node) {
		DefaultTreeModel model = (DefaultTreeModel)getModel();
		model.reload(node);
		expandPath(new TreePath(node.getPath()));
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

	public void onChanged(Consumer<SelectablePath> callback) {
		_onChanged.add(callback);
	}

}
