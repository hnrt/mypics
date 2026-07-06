package com.hideakin.mypics.gui.model;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import com.hideakin.mypics.model.PathNode;

public class TargetDirectoryTreeNode extends DefaultMutableTreeNode {

	private static final long serialVersionUID = 359166520473102869L;

	public static final String HEADER = "Target directories";

	public TargetDirectoryTreeNode() {
		super(HEADER);
	}

	public void set(String format, Object...args) {
		setUserObject(String.format(format, args));
	}

	public void add(Path path, boolean selected) {
		if (!has(path)) {
			add(SelectablePathTreeNode.ofDirectory(path, selected));
			set("%s: %d", HEADER, getChildCount());
		}
	}

	public void add(PathNode dd, boolean selected) {
		int last = getChildCount();
		while (dd != null) {
			if (!has(dd.path())) {
				add(SelectablePathTreeNode.ofDirectory(dd.path(), selected));
			}
			dd = dd.next();
		}
		int count = getChildCount();
		if (count != last) {
			set("%s: %d", HEADER, count);
		}
	}

	private boolean has(Path path) {
		int n = getChildCount();
		for (int i = 0; i < n; i++) {
			SelectablePathTreeNode child = (SelectablePathTreeNode)getChildAt(i);
			if (child.has(path)) {
				return true;
			}
		}
		return false;
	}

	public List<Path> list() {
		List<Path> directories = new ArrayList<>();
		int n = getChildCount();
		for (int i = 0; i < n; i++) {
			SelectablePathTreeNode child = (SelectablePathTreeNode)getChildAt(i);
			directories.add(child.selectablePath().path());
		}
		return directories;
	}

	public void replaceWith(Path[] paths) {
		int last = getChildCount();
		for (int i = last - 1; i >= 0; i--) {
			SelectablePathTreeNode child = (SelectablePathTreeNode)getChildAt(i);
			boolean found = false;
			for (Path path : paths) {
				if (child.has(path)) {
					found = true;
					break;
				}
			}
			if (!found) {
				this.remove(i);
			}
		}
		for (Path path : paths) {
			if (!has(path)) {
				add(SelectablePathTreeNode.ofDirectory(path, false));
			}
		}
		int count = getChildCount();
		if (count != last) {
			set("%s: %d", HEADER, count);
		}
	}

}
