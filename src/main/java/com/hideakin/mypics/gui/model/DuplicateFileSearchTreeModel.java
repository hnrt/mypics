package com.hideakin.mypics.gui.model;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import com.hideakin.mypics.model.PathNode;
import com.hideakin.mypics.model.SelectablePath;

public class DuplicateFileSearchTreeModel extends DefaultTreeModel {

	private static final long serialVersionUID = -7674690994823771568L;

	private final DefaultMutableTreeNode _root;
	private final Map<String, DefaultMutableTreeNode> _keyNodes = new HashMap<>(1024);

	public DuplicateFileSearchTreeModel() {
		super(new DefaultMutableTreeNode("ROOT"));
		_root = (DefaultMutableTreeNode)super.getRoot();
	}

	public DefaultMutableTreeNode root() {
		return _root;
	}

	public DefaultMutableTreeNode keyNode(String key) {
		return _keyNodes.get(key);
	}

	public void addKey(String key) {
		int n = _root.getChildCount();
		if (n == 0) {
			_keyNodes.clear();
		}
		DefaultMutableTreeNode keyNode = _keyNodes.get(key);
		if (keyNode != null) {
			return;
		}
		keyNode = new DefaultMutableTreeNode(key);
		_keyNodes.put(key, keyNode);
		for (int i = 0; i < n; i++) {
			DefaultMutableTreeNode next = (DefaultMutableTreeNode)_root.getChildAt(i);
			String nextKey = (String)next.getUserObject();
			if (nextKey.compareTo(key) > 0) {
				_root.insert(keyNode, i);
				return;
			}
		}
		_root.add(keyNode);
	}

	public void addRegularFile(String key, Path path) {
		DefaultMutableTreeNode keyNode =_keyNodes.get(key);
		if (keyNode == null) {
			throw new RuntimeException("DuplicateFileSearchTreeModel::addRegularFile: No such key: " + key);
		}
		keyNode.add(SelectablePathTreeNode.ofRegularFile(path, true));
	}

	public void addRegularFiles(String key, PathNode pp) {
		DefaultMutableTreeNode keyNode =_keyNodes.get(key);
		if (keyNode == null) {
			throw new RuntimeException("DuplicateFileSearchTreeModel::addRegularFiles: No such key: " + key);
		}
		while (pp != null) {
			keyNode.add(SelectablePathTreeNode.ofRegularFile(pp.path(), true));
			pp = pp.next();
		}
	}

	public String[] keys() {
		int n = _root.getChildCount();
		String[] kk = new String[n];
		for (int i = 0; i < n; i++) {
			DefaultMutableTreeNode keyNode = (DefaultMutableTreeNode)_root.getChildAt(i);
			kk[i] = (String)keyNode.getUserObject();
		}
		return kk;
	}

	public SelectablePath[] paths(String key) {
		DefaultMutableTreeNode keyNode = _keyNodes.get(key);
		if (keyNode == null) return null;
		int n = keyNode.getChildCount();
		SelectablePath[] pp = new SelectablePath[n];
		for (int i = 0; i < n; i++) {
			SelectablePathTreeNode child = (SelectablePathTreeNode)keyNode.getChildAt(i);
			pp[i] = child.selectablePath();
		}
		return pp;
	}

}
