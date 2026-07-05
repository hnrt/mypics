package com.hideakin.mypics.gui.dialog;

import static com.hideakin.mypics.Application.debug;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.hideakin.mypics.Application;

public class ModalDialog extends JDialog {

	private static final long serialVersionUID = 4039196230449427057L;

	protected static class ButtonPanel extends JPanel {

		private static final long serialVersionUID = -4661981655248372963L;

		public static ButtonPanel of(ModalDialog owner) {
			return new ButtonPanel(owner);
		}

		public final JButton searchButton = new JButton("Search");
		public final JButton applyButton = new JButton("Apply");
		public final JButton cancelButton = new JButton("Cancel");

		private ButtonPanel(ModalDialog owner) {
			super();
			searchButton.addActionListener(e -> {
				owner.search();
			});
			searchButton.setMnemonic(KeyEvent.VK_S);
			applyButton.addActionListener(e -> {
				owner.apply();
			});
			applyButton.setMnemonic(KeyEvent.VK_A);
			cancelButton.addActionListener(e -> {
				owner.cancel();
			});
			cancelButton.setMnemonic(KeyEvent.VK_C);
			add(searchButton);
	        add(applyButton);
	        add(cancelButton);
	        searchButton.setVisible(false);
		}
		
	}

	protected final ButtonPanel _buttons;
	protected final AtomicInteger _state = new AtomicInteger(0);

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

	public void search() {
	}

	public void apply() {
		Application.mainFrame.applyChanges();
		dispose();
	}

	public void cancel() {
		debug(3, "ModalDialog::cancel");
		_state.set(-1);
		dispose();
	}

	protected boolean invokeLater(Runnable x) {
		while (!_state.compareAndSet(0, 1)) {
			if (_state.get() == -1) {
				return false;
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException ie) {
			}
		}
		SwingUtilities.invokeLater(() -> {
			try {
				x.run();
			} finally {
				_state.compareAndSet(1, 0);
			}
		});
		return _state.get() != -1;
	}

}
