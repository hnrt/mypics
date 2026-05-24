package com.hideakin.mypics;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

public class ModalDialog extends JDialog {

	private static final long serialVersionUID = 4039196230449427057L;

	private static class ButtonPanel extends JPanel {

		private static final long serialVersionUID = -4661981655248372963L;

		public static ButtonPanel of(ModalDialog owner) {
			return new ButtonPanel(owner);
		}

		private ButtonPanel(ModalDialog owner) {
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

	protected ModalDialog(String title) {
		super(Application.mainFrame, title, true);
        setLayout(new BorderLayout());
	}

	public void showDialog() {
        add(ButtonPanel.of(this), BorderLayout.SOUTH);
        Dimension dim = getSize();
        if (dim.width == 0 && dim.height == 0) {
        	pack();
        }
        setLocationRelativeTo(Application.mainFrame);
		setVisible(true);
	}

	public void apply() {
		Application.mainFrame.applyChanges();
	}

}
