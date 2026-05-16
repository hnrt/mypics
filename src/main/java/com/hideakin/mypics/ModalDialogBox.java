package com.hideakin.mypics;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

public class ModalDialogBox extends JDialog {

	private static final long serialVersionUID = 4039196230449427057L;

	private static class ButtonPanel extends JPanel {

		private static final long serialVersionUID = -4661981655248372963L;

		public static ButtonPanel of(ModalDialogBox owner) {
			return new ButtonPanel(owner);
		}

		private ButtonPanel(ModalDialogBox owner) {
			super();
			JButton applyButton = new JButton("Apply");
			applyButton.addActionListener(e -> {
				owner.apply();
				owner.dispose();
			});
			applyButton.setMnemonic(KeyEvent.VK_A);
			JButton cancelButton = new JButton("Cancel");
			cancelButton.addActionListener(e -> {
				owner.dispose();
			});
			cancelButton.setMnemonic(KeyEvent.VK_C);
	        add(applyButton);
	        add(cancelButton);
		}
		
	}

	protected final ImageViewer _viewer;

	protected ModalDialogBox(ImageViewer viewer, String title) {
		super(viewer, title, true);
		_viewer = viewer;
        setLayout(new BorderLayout());
	}

	public void showDialog() {
        add(ButtonPanel.of(this), BorderLayout.SOUTH);
        Dimension dim = getSize();
        if (dim.width == 0 && dim.height == 0) {
        	pack();
        }
        setLocationRelativeTo(_viewer);
		setVisible(true);
	}

	public void apply() {
		_viewer.imagePane().redraw();
	}

}
