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
import com.hideakin.mypics.model.TagNode;
import com.hideakin.mypics.util.function.ConsumerList;

public class SelectableThumbnailedPathTree extends JTree {

	private static final long serialVersionUID = -5173080788780336722L;

	private final DefaultMutableTreeNode _root;
	private final ConsumerList<Path> _onSelected = new ConsumerList<>();
	private final Map<String, DefaultMutableTreeNode> _first = new HashMap<>(1024);
	private final Map<Path, Icon> _icons = new HashMap<>(8192);

	public SelectableThumbnailedPathTree() {
		super(new DefaultMutableTreeNode("ROOT"));
		setRootVisible(false);
		setCellRenderer(new SelectableThumbnailedPathTreeCellRenderer(_icons));
		setCellEditor(new SelectableThumbnailedPathTreeCellEditor(_icons));
		setEditable(true);
		_root = (DefaultMutableTreeNode)super.getModel().getRoot();
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
		    	Object obj = ((DefaultMutableTreeNode)path.getLastPathComponent()).getUserObject();
		        if (obj instanceof SelectablePath sp) {
		        	_onSelected.invoke(sp.path());
		        	return;
		        }
		    }
		    _onSelected.invoke(null);
		});
	}

	public void onSelected(Consumer<Path> callback) {
		_onSelected.add(callback);
	}

	public void reloadRoot() {
		DefaultTreeModel model = (DefaultTreeModel)getModel();
		model.reload(_root);
	}

	public void expandTag(String tag) {
		DefaultMutableTreeNode node = _first.get(tag);
		if (node != null) {
			DefaultTreeModel model = (DefaultTreeModel)getModel();
			model.reload(node);
			expandPath(new TreePath(node.getPath()));
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

	public void add(String tag, Path path, boolean selected) {
		tagTreeNode(tag).add(new DefaultMutableTreeNode(new SelectablePath(path, selected)));
	}

	private DefaultMutableTreeNode tagTreeNode(String tag) {
		DefaultMutableTreeNode node = _first.get(tag);
		if (node == null) {
			node = new DefaultMutableTreeNode(new TagNode(tag));
			_root.add(node);
			_first.put(tag, node);
		}
		return node;
	}

	public String[] tags() {
		int n = _root.getChildCount();
		String[] tt = new String[n];
		for (int i = 0; i < n; i++) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)_root.getChildAt(i);
			tt[i] = ((TagNode)node.getUserObject()).toString();
		}
		return tt;
	}

	public SelectablePath[] paths(String tag) {
		DefaultMutableTreeNode parent = _first.get(tag);
		if (parent == null) return null;
		int n = parent.getChildCount();
		SelectablePath[] pp = new SelectablePath[n];
		for (int i = 0; i < n; i++) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode)_root.getChildAt(i);
			pp[i] = (SelectablePath)child.getUserObject();
		}
		return pp;
	}

}
