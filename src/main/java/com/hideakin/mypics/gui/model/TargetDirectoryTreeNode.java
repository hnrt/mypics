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
		add(SelectablePathTreeNode.ofDirectory(path, selected));
		set("%s: %d", HEADER, getChildCount());
	}

	public void add(PathNode dd, boolean selected) {
		while (dd != null) {
			add(SelectablePathTreeNode.ofDirectory(dd.path(), selected));
			dd = dd.next();
		}
		set("%s: %d", HEADER, getChildCount());
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

}
