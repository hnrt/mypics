package com.hideakin.mypics.gui.model;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import com.hideakin.mypics.model.SelectablePath;

public class SelectablePathTreeNode extends DefaultMutableTreeNode {

	private static final long serialVersionUID = 2304548236329990350L;

	public static SelectablePathTreeNode ofRoot() {
		return new SelectablePathTreeNode(SelectablePath.ofDirectory(Path.of("[ROOT]"), false));
	}

	public static SelectablePathTreeNode ofDirectory(Path path, boolean selected) {
		return new SelectablePathTreeNode(SelectablePath.ofDirectory(path, selected));
	}

	public static SelectablePathTreeNode ofRegularFile(Path path, boolean selected) {
		return new SelectablePathTreeNode(SelectablePath.ofRegularFile(path, selected));
	}

	protected SelectablePathTreeNode(Object userData) {
		super(userData);
	}

	public SelectablePath selectablePath() {
		return (SelectablePath)getUserObject();
	}

	public void addDirectory(Path path, boolean selected) {
		add(ofDirectory(path, selected));
	}

	public List<SelectablePathTreeNode> getChildList() {
		List<SelectablePathTreeNode> list = new ArrayList<>();
		int n = getChildCount();
		for (int i = 0; i < n; i++) {
			list.add((SelectablePathTreeNode)getChildAt(i));
		}
		return list;
	}

	public List<SelectablePath> getChildSelectablePathList() {
		List<SelectablePath> list = new ArrayList<>();
		int n = getChildCount();
		for (int i = 0; i < n; i++) {
			list.add(((SelectablePathTreeNode)getChildAt(i)).selectablePath());
		}
		return list;
	}

	public SelectablePathTreeNode find(Path path) {
		for (SelectablePathTreeNode child : getChildList()) {
			if (child.selectablePath().path().equals(path)) {
				return child;
			}
			SelectablePathTreeNode found = child.find(path);
			if (found != null) {
				return found;
			}
		}
		return null;
	}

}
