package com.hideakin.mypics;

import java.awt.Component;
import java.nio.file.Path;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

public class FileNameRenderer extends DefaultListCellRenderer {

	private static final long serialVersionUID = 1977444262554630124L;

	@Override
    public Component getListCellRendererComponent(
            JList<?> list, Object value, int index,
            boolean isSelected, boolean cellHasFocus) {
        Path path = (Path)value;
    	return super.getListCellRendererComponent(list, path.getParent() == null ? path : path.getFileName(), index, isSelected, cellHasFocus);
    }

}
