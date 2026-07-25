package com.hideakin.mypics.gui;

import java.nio.file.Path;
import javax.swing.JList;
import com.hideakin.mypics.gui.model.DirectoryListModel;
import com.hideakin.mypics.gui.renderer.PathListCellRenderer;

public class DirectoryList extends JList<Path> {

	private static final long serialVersionUID = 2549289714657515837L;

	public static DirectoryList of(DirectoryListModel model) {
		return new DirectoryList(model);
	}

	private DirectoryList(DirectoryListModel model) {
		super(model);
		setCellRenderer(new PathListCellRenderer(false));
	}

}
