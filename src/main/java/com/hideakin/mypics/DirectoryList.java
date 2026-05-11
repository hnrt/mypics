package com.hideakin.mypics;

import java.nio.file.Path;
import javax.swing.JList;

public class DirectoryList extends JList<Path> {

	private static final long serialVersionUID = 2549289714657515837L;

	public static DirectoryList of(DirectoryListModel model) {
		return new DirectoryList(model);
	}

	private final Configuration _configuration = Configuration.getInstance();
	private final DirectoryListModel _model;

	private DirectoryList(DirectoryListModel model) {
		super(model);
		_model = model;
		setCellRenderer(new FileNameRenderer());
		addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Path selected = getSelectedValue();
                if (selected != null) {
                	if (selected.toString().equals("..")) {
                		Path parent = _configuration.getDirectory().getParent();
                		if (parent != null) {
                			_model.loadFrom(parent);
                		} else {
                			clearSelection();
                		}
                	} else {
            			_model.loadFrom(selected);
                	}
                }
            }
        });
	}

}
