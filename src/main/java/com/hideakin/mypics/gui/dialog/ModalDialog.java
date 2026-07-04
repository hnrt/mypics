package com.hideakin.mypics.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import com.hideakin.mypics.Application;

public class ModalDialog extends JDialog {

	private static final long serialVersionUID = 4039196230449427057L;

	protected static class ButtonPanel extends JPanel {

		private static final long serialVersionUID = -4661981655248372963L;

		public static ButtonPanel of(ModalDialog owner) {
			return new ButtonPanel(owner);
		}

		public final JButton testButton = new JButton("Test");
		public final JButton applyButton = new JButton("Apply");
		public final JButton cancelButton = new JButton("Cancel");

		private ButtonPanel(ModalDialog owner) {
			super();
			testButton.addActionListener(e -> {
				owner.test();
			});
			testButton.setMnemonic(KeyEvent.VK_T);
			applyButton.addActionListener(e -> {
				owner.apply();
			});
			applyButton.setMnemonic(KeyEvent.VK_A);
			cancelButton.addActionListener(e -> {
				owner.cancel();
			});
			cancelButton.setMnemonic(KeyEvent.VK_C);
			add(testButton);
	        add(applyButton);
	        add(cancelButton);
	        testButton.setVisible(false);
		}
		
	}

	protected final ButtonPanel _buttons;

	protected ModalDialog(String title) {
		super(Application.mainFrame, title, true);
        setLayout(new BorderLayout());
        _buttons = ButtonPanel.of(this);
	}

	public void showDialog() {
        add(_buttons, BorderLayout.SOUTH);
        Dimension dim = getSize();
        if (dim.width == 0 && dim.height == 0) {
        	pack();
        }
        setLocationRelativeTo(Application.mainFrame);
		setVisible(true);
	}

	public void test() {
	}

	public void apply() {
		Application.mainFrame.applyChanges();
		dispose();
	}

	public void cancel() {
		dispose();
	}

}
