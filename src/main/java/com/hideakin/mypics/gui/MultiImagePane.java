package com.hideakin.mypics.gui;

import java.awt.GridLayout;
import java.nio.file.Path;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.hideakin.mypics.Application;
import com.hideakin.mypics.gui.component.CenteredIconLabel;
import com.hideakin.mypics.gui.util.Thumbnail;

public class MultiImagePane extends JScrollPane {

	private static final long serialVersionUID = 7168635008880816264L;

	public static MultiImagePane create() {
		return new MultiImagePane();
	}

	private final JPanel _panel = new JPanel();

	private MultiImagePane() {
		super();
		setViewportView(_panel);
	}

	public void clear() {
		_panel.removeAll();
		_panel.revalidate();
		_panel.repaint();
	}

	public void loadFrom(List<Path> paths) {
		_panel.removeAll();
		if (paths.size() > 0) {
			int size = Thumbnail.BIG_SIZE;
			int cols = 4;
			int rows = (paths.size() + cols - 1) / cols;
			_panel.setLayout(new GridLayout(rows, cols, 2, 2));
			for (Path path : paths) {
				Application.debug(3, "MultiImagePane::loadFrom: %s", path);
				JLabel label = new CenteredIconLabel(size);
				label.setIcon(Thumbnail.of(path, size, icon -> label.setIcon(icon)));
				label.setToolTipText(path.getFileName().toString());
				_panel.add(label);
			}
		}
		_panel.revalidate();
		_panel.repaint();
	}

}
