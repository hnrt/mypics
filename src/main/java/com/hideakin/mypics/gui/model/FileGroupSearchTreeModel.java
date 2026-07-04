package com.hideakin.mypics.gui.model;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import com.hideakin.mypics.model.PathNode;

public class FileGroupSearchTreeModel extends DefaultTreeModel {

	private static final long serialVersionUID = 2725084169062715262L;

	private final DefaultMutableTreeNode _root;
	private final Map<String, DefaultMutableTreeNode> _keyNodes = new HashMap<>(1024);

	public FileGroupSearchTreeModel() {
		super(new DefaultMutableTreeNode("ROOT"));
		_root = (DefaultMutableTreeNode)super.getRoot();
	}

	public DefaultMutableTreeNode root() {
		return _root;
	}

	public DefaultMutableTreeNode keyNode(String key) {
		return _keyNodes.get(key);
	}

	public List<Path> from(String key) {
		List<Path> list = new ArrayList<>();
		DefaultMutableTreeNode keyNode = _keyNodes.get(key);
		if (keyNode != null) {
			if (keyNode.getChildAt(0) instanceof MatchedFileTreeNode mfNode) {
				int n = mfNode.getChildCount();
				for (int i = 0; i < n; i++) {
					if (mfNode.getChildAt(i) instanceof SelectablePathTreeNode spNode) {
						if (spNode.selectablePath().selected()) {
							list.add(spNode.selectablePath().path());
						}
					} else {
						throw new RuntimeException("FileGroupTreeModel::from: Corrupted.");
					}
				}
			} else {
				throw new RuntimeException("FileGroupTreeModel::from: Corrupted.");
			}
		}
		return list;
	}

	public List<Path> to(String key) {
		List<Path> list = new ArrayList<>();
		DefaultMutableTreeNode keyNode = _keyNodes.get(key);
		if (keyNode != null) {
			if (keyNode.getChildAt(1) instanceof TargetDirectoryTreeNode tdNode) {
				int n = tdNode.getChildCount();
				for (int i = 0; i < n; i++) {
					if (tdNode.getChildAt(i) instanceof SelectablePathTreeNode spNode) {
						if (spNode.selectablePath().selected()) {
							list.add(spNode.selectablePath().path());
						}
					} else {
						throw new RuntimeException("FileGroupTreeModel::to: Corrupted.");
					}
				}
			} else {
				throw new RuntimeException("FileGroupTreeModel::to: Corrupted.");
			}
		}
		return list;
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
		keyNode.add(new MatchedFileTreeNode());
		keyNode.add(new TargetDirectoryTreeNode());
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

	public void addMatchedFile(String key, Path path) {
		DefaultMutableTreeNode keyNode =_keyNodes.get(key);
		if (keyNode == null) {
			throw new RuntimeException("FileGroupTreeModel::addMatchedFile: No such key: " + key);
		}
		if (keyNode.getChildAt(0) instanceof MatchedFileTreeNode fNode) {
			fNode.add(path, true);
		} else {
			throw new RuntimeException("FileGroupTreeModel::addMatchedFile: Corrupted.");
		}
	}

	public void addMatchedFiles(String key, PathNode ff) {
		DefaultMutableTreeNode keyNode =_keyNodes.get(key);
		if (keyNode == null) {
			throw new RuntimeException("FileGroupTreeModel::addMatchedFile: No such key: " + key);
		}
		if (keyNode.getChildAt(0) instanceof MatchedFileTreeNode fNode) {
			fNode.add(ff, true);
		} else {
			throw new RuntimeException("FileGroupTreeModel::addMatchedFile: Corrupted.");
		}
	}

	public void addTargetDirectory(String key, Path path) {
		DefaultMutableTreeNode keyNode =_keyNodes.get(key);
		if (keyNode.getChildAt(1) instanceof TargetDirectoryTreeNode tNode) {
			tNode.add(path, false);
		} else {
			throw new RuntimeException("FileGroupTreeModel::addMatchedFile: Corrupted.");
		}
	}

	public void addTargetDirectories(String key, PathNode dd) {
		DefaultMutableTreeNode keyNode =_keyNodes.get(key);
		if (keyNode.getChildAt(1) instanceof TargetDirectoryTreeNode tNode) {
			tNode.add(dd, false);
		} else {
			throw new RuntimeException("FileGroupTreeModel::addMatchedFile: Corrupted.");
		}
	}

	public void reloadKey(String key) {
		DefaultMutableTreeNode keyNode =_keyNodes.get(key);
		reload(keyNode);
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

}
