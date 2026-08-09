package com.hideakin.mypics.gui.model;

import javax.swing.tree.DefaultMutableTreeNode;

public class FileGroupTreeNode extends DefaultMutableTreeNode {

	private static final long serialVersionUID = 4470022503374370235L;

	public FileGroupTreeNode(String key) {
		super(key);
		add(new MatchedFileTreeNode());
		add(new TargetDirectoryTreeNode());
	}

	public String key() {
		return (String)getUserObject();
	}

	public MatchedFileTreeNode matchedFileNode() {
		return (MatchedFileTreeNode)getChildAt(0);
	}

	public TargetDirectoryTreeNode targetDirectoryNode() {
		return (TargetDirectoryTreeNode)getChildAt(1);
	}

}
