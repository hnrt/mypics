package com.hideakin.mypics.gui.model;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import com.hideakin.mypics.model.PathNode;

public class FileGroupSearchTreeModel extends DefaultTreeModel {

	private static final long serialVersionUID = 2725084169062715262L;

	private final DefaultMutableTreeNode _root;
	private final Map<String, FileGroupTreeNode> _keyNodes = new HashMap<>(1024);

	public FileGroupSearchTreeModel() {
		super(new DefaultMutableTreeNode("ROOT"));
		_root = (DefaultMutableTreeNode)super.getRoot();
	}

	public DefaultMutableTreeNode root() {
		return _root;
	}

	public FileGroupTreeNode keyNode(String key) {
		int n = _root.getChildCount();
		if (n == 0) {
			_keyNodes.clear();
			return null;
		}
		return _keyNodes.get(key);
	}

	public MatchedFileTreeNode matchedFileNode(String key) {
		FileGroupTreeNode node = keyNode(key);
		if (node == null) {
			node = add(new FileGroupTreeNode(key));
		}
		return node.matchedFileNode();
	}

	public TargetDirectoryTreeNode targetDirectoryNode(String key) {
		FileGroupTreeNode node = keyNode(key);
		if (node == null) {
			node = add(new FileGroupTreeNode(key));
		}
		return node.targetDirectoryNode();
	}

	public List<Path> from(String key) {
		FileGroupTreeNode node = keyNode(key);
		if (node == null) {
			node = add(new FileGroupTreeNode(key));
		}
		return node.matchedFileNode().list();
	}

	public List<Path> to(String key) {
		FileGroupTreeNode node = keyNode(key);
		if (node == null) {
			node = add(new FileGroupTreeNode(key));
		}
		return node.targetDirectoryNode().list();
	}

	public void addKey(String key) {
		FileGroupTreeNode node = keyNode(key);
		if (node == null) {
			add(new FileGroupTreeNode(key));
		}
	}

	private FileGroupTreeNode add(FileGroupTreeNode node) {
		String key = node.key();
		_keyNodes.put(key, node);
		int n = _root.getChildCount();
		for (int i = 0; i < n; i++) {
			FileGroupTreeNode next = (FileGroupTreeNode)_root.getChildAt(i);
			int d = next.key().compareTo(key); 
			if (d > 0) {
				_root.insert(node, i);
				return node;
			} else if (d == 0) {
				_root.remove(i);
				_root.insert(node, i);
				return node;
			}
		}
		_root.add(node);
		return node;
	}

	public void removeKey(String key) {
		FileGroupTreeNode node = keyNode(key);
		if (node != null) {
			_keyNodes.remove(key);
			_root.remove(node);
		}
	}

	public void addMatchedFile(String key, Path path) {
		FileGroupTreeNode node = keyNode(key);
		if (node == null) {
			node = add(new FileGroupTreeNode(key));
		}
		node.matchedFileNode().add(path, true);
	}

	public void addMatchedFiles(String key, PathNode ff) {
		FileGroupTreeNode node = keyNode(key);
		if (node == null) {
			node = add(new FileGroupTreeNode(key));
		}
		node.matchedFileNode().add(ff, true);
	}

	public void addTargetDirectory(String key, Path path) {
		FileGroupTreeNode node = keyNode(key);
		if (node == null) {
			node = add(new FileGroupTreeNode(key));
		}
		node.targetDirectoryNode().add(path, false);
	}

	public void addTargetDirectories(String key, PathNode dd) {
		FileGroupTreeNode node = keyNode(key);
		if (node == null) {
			node = add(new FileGroupTreeNode(key));
		}
		node.targetDirectoryNode().add(dd, false);
	}

	public void reloadRoot() {
		reload(_root);
	}

	public void reloadKey(String key) {
		FileGroupTreeNode node = keyNode(key);
		if (node != null) {
			reload(node);
		}
	}

	public String[] keys() {
		int n = _root.getChildCount();
		String[] kk = new String[n];
		for (int i = 0; i < n; i++) {
			FileGroupTreeNode keyNode = (FileGroupTreeNode)_root.getChildAt(i);
			kk[i] = keyNode.key();
		}
		return kk;
	}

}
