package com.hideakin.mypics.gui.model;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import com.hideakin.mypics.model.SelectablePath;

public class SelectablePathTreeNode extends DefaultMutableTreeNode {

	private static final long serialVersionUID = 2304548236329990350L;

	public static SelectablePathTreeNode ofRoot() {
		return new SelectablePathTreeNode(SelectablePath.ofRoot());
	}

	public static SelectablePathTreeNode ofDirectory(Path path, boolean selected) {
		return new SelectablePathTreeNode(SelectablePath.ofDirectory(path, selected));
	}

	public static SelectablePathTreeNode ofRegularFile(Path path, boolean selected) {
		return new SelectablePathTreeNode(SelectablePath.ofRegularFile(path, selected));
	}

	protected SelectablePathTreeNode(SelectablePath userData) {
		super(userData);
	}

	public SelectablePathTreeNode child(int index) {
		int n = getChildCount();
		if (index < 0) {
			index += n;
		}
		return (0 <= index) && (index < n) ? (SelectablePathTreeNode)getChildAt(index) : null;
	}

	public SelectablePath selectablePath() {
		return (SelectablePath)getUserObject();
	}

	public Path path() {
		return ((SelectablePath)getUserObject()).path();
	}

	public boolean has(Path path) {
		return ((SelectablePath)getUserObject()).path().equals(path);
	}

	public boolean selected() {
		return ((SelectablePath)getUserObject()).selected();
	}

	public void setSelected(boolean value) {
		((SelectablePath)getUserObject()).setSelected(value);
	}

	public boolean enabled() {
		return ((SelectablePath)getUserObject()).enabled();
	}

	public void setEnabled(boolean value) {
		((SelectablePath)getUserObject()).setEnabled(value);
	}

	public boolean loaded() {
		return ((SelectablePath)getUserObject()).loaded();
	}

	public void setLoaded(boolean value) {
		((SelectablePath)getUserObject()).setLoaded(value);
	}

	public void addRegularFile(Path path, boolean selected) {
		add(ofRegularFile(path, selected));
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
		int n = getChildCount();
		for (int i = 0; i < n; i++) {
			SelectablePathTreeNode child = (SelectablePathTreeNode)getChildAt(i);
			if (child.has(path)) {
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
