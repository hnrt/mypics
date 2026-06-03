package com.hideakin.mypics;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.nio.file.Path;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;

public class ThumbnailRenderer extends JPanel implements ListCellRenderer<Path> {

	private static final long serialVersionUID = 3421207696253706378L;

	private final JLabel _iconLabel = new JLabel();
    private final JLabel _nameLabel = new JLabel();

	public ThumbnailRenderer() {
		setLayout(new BorderLayout(10, 0));
		add(_iconLabel, BorderLayout.WEST);
        add(_nameLabel, BorderLayout.CENTER);
	}

	@Override
	public Component getListCellRendererComponent(
			JList<? extends Path> list,
			Path value,
			int index,
			boolean isSelected,
			boolean cellHasFocus) {
        _nameLabel.setText(value.getFileName().toString());
        Icon icon = createThumbnail(value);
        _iconLabel.setIcon(icon);
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }
        setOpaque(true);
        return this;
	}

	private Icon createThumbnail(Path path) {
        try {
        	BufferedImage original = ImageLoader.loadCorrectedImage(path.toFile());
        	if (original != null) {
                Image scaled = original.getScaledInstance(50, 50, Image.SCALE_SMOOTH);
        		return new ImageIcon(scaled);
        	} else {
                return getDefaultIcon();
            }
        } catch (Exception e) {
            return getDefaultIcon();
        }
    }

    private Icon getDefaultIcon() {
        return UIManager.getIcon("FileView.fileIcon");
    }

}
