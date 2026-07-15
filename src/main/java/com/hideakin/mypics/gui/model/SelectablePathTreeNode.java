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
		return (SelectablePathTreeNode)getChildAt(index < 0 ? index + getChildCount() : index);
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
		addChild(ofRegularFile(path, selected));
	}

	public void addDirectory(Path path, boolean selected) {
		addChild(ofDirectory(path, selected));
	}

	private void addChild(SelectablePathTreeNode node) {
		int n = getChildCount();
		int i = 0;
		int j = n - 1;
		while (i <= j) {
			int m = (i + j) / 2;
			int d = child(m).path().compareTo(node.path());
			if (d < 0) {
				i = m + 1;
			} else if (d > 0) {
				j = m - 1;
			} else {
				child(m).setEnabled(node.enabled());
				return;
			}
		}
		if (i < n) {
			insert(node, i);
		} else {
			add(node);
		}
	}

	public void remove(Path path) {
		int n = getChildCount();
		int i = 0;
		int j = n - 1;
		while (i <= j) {
			int m = (i + j) / 2;
			int d = child(m).path().compareTo(path);
			if (d < 0) {
				i = m + 1;
			} else if (d > 0) {
				j = m - 1;
			} else {
				remove(m);
				return;
			}
		}
	}

	public List<SelectablePathTreeNode> getChildList() {
		List<SelectablePathTreeNode> list = new ArrayList<>();
		int n = getChildCount();
		for (int i = 0; i < n; i++) {
			list.add(child(i));
		}
		return list;
	}

	public List<SelectablePath> getChildSelectablePathList() {
		List<SelectablePath> list = new ArrayList<>();
		int n = getChildCount();
		for (int i = 0; i < n; i++) {
			list.add(child(i).selectablePath());
		}
		return list;
	}

	public SelectablePathTreeNode find(Path path) {
		int n = getChildCount();
		for (int i = 0; i < n; i++) {
			if (child(i).has(path)) {
				return child(i);
			}
		}
		for (int i = 0; i < n; i++) {
			SelectablePathTreeNode found = child(i).find(path);
			if (found != null) {
				return found;
			}
		}
		return null;
	}

}
