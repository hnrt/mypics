package com.hideakin.mypics.gui.model;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import com.hideakin.mypics.model.PathNode;

public class MatchedFileTreeNode extends DefaultMutableTreeNode {

	private static final long serialVersionUID = -2471169054491158662L;

	public static final String HEADER = "Matched files";

	public MatchedFileTreeNode() {
		super(HEADER);
	}

	public void set(String format, Object...args) {
		setUserObject(String.format(format, args));
	}

	public void add(Path path, boolean selected) {
		add(SelectablePathTreeNode.ofRegularFile(path, selected));
		set("%s: %d", HEADER, getChildCount());
	}

	public void add(PathNode ff, boolean selected) {
		while (ff != null) {
			add(SelectablePathTreeNode.ofRegularFile(ff.path(), selected));
			ff = ff.next();
		}
		set("%s: %d", HEADER, getChildCount());
	}

	public List<Path> list() {
		List<Path> files = new ArrayList<>();
		int n = getChildCount();
		for (int i = 0; i < n; i++) {
			SelectablePathTreeNode child = (SelectablePathTreeNode)getChildAt(i);
			files.add(child.selectablePath().path());
		}
		return files;
	}

}
