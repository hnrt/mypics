package com.hideakin.mypics.gui.model;

import static com.hideakin.mypics.Application.debug;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultTreeModel;

public class DirectorySelectionTreeModel extends DefaultTreeModel {

	private static final long serialVersionUID = -1057043237069700532L;

	private final SelectablePathTreeNode _root;

	public DirectorySelectionTreeModel() {
		super(SelectablePathTreeNode.ofRoot());
		_root = (SelectablePathTreeNode)super.getRoot();
	}

	public SelectablePathTreeNode root() {
		return _root;
	}

	public SelectablePathTreeNode find(Path path) {
		return path == null || path.getParent() == null ? _root : _root.find(path);
	}

	public void loadDirectory(Path directory) {
		Path parent = directory.getParent();
		if (parent != null) {
			loadDirectory(parent);
			loadSubdirectories(directory, _root.find(directory));
		} else {
			loadSubdirectories(directory, _root);
		}
	}

	private void loadSubdirectories(Path directory, SelectablePathTreeNode treeNode) {
		if (!treeNode.loaded()) {
			treeNode.setLoaded(true);
			debug(3, "DirectorySelectionTreeModel::loadSubdirectories: %s", directory);
			try {
				Files.list(directory).filter(e -> Files.isDirectory(e)).forEach(path -> {
					treeNode.addDirectory(path, false);
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public boolean setSelected(Path path, boolean selected, boolean cascaded) {
        debug(3, "DirectorySelectionTreeModel::setSelected: %s %s %s", selected ? "T" : "F", cascaded ? "T" : "F", path);
        loadDirectory(path);
		SelectablePathTreeNode found = _root.find(path);
		if (found != null) {
			return setSelected(found, selected, cascaded);
		} else {
			return false;
		}
	}

	private boolean setSelected(SelectablePathTreeNode node, boolean selected, boolean cascaded) {
		boolean changed = false;
		if (node.enabled()) {
			node.setSelected(selected);
			changed = true;
		}
		if (cascaded) {
			loadDirectory(node.path());
			int n = node.getChildCount();
			for (int i = 0; i < n; i++) {
				if (setSelected(node.child(i), selected, true)) {
					changed = true;
				}
			}
		}
		return changed;
	}

	public boolean setEnabled(Path path, boolean enabled, boolean cascaded) {
        debug(3, "DirectorySelectionTreeModel::setEnabled: %s %s %s", enabled ? "T" : "F", cascaded ? "T" : "F", path);
        loadDirectory(path);
		SelectablePathTreeNode found = _root.find(path);
		if (found != null) {
			return setEnabled(found, enabled, cascaded);
		} else {
			return false;
		}
	}

	private boolean setEnabled(SelectablePathTreeNode node, boolean enabled, boolean cascaded) {
		boolean changed = false;
		if (!node.selected()) {
			node.setEnabled(enabled);
			changed = true;
		}
		if (cascaded) {
			loadDirectory(node.path());
			int n = node.getChildCount();
			for (int i = 0; i < n; i++) {
				if (setEnabled(node.child(i), enabled, true)) {
					changed = true;
				}
			}
		}
		return changed;
	}

	public Path[] selectedPaths() {
		List<Path> pp = new ArrayList<>();
		collectSelectedPaths(_root, pp);
		return pp.toArray(new Path[pp.size()]);
	}

	private static void collectSelectedPaths(SelectablePathTreeNode node, List<Path> pp) {
		int n = node.getChildCount();
		for (int i = 0; i < n; i++) {
			SelectablePathTreeNode child = node.child(i);
			if (child.selected()) {
				pp.add(child.path());
			}
			collectSelectedPaths(child, pp);
		}
	}

}
