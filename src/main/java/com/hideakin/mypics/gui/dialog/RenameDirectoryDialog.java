package com.hideakin.mypics.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.nio.file.Path;
import java.util.function.Consumer;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

public class RenameDirectoryDialog extends ModalDialog {

	private static final long serialVersionUID = 6669855121701668646L;

	public static RenameDirectoryDialog create(Path path, Consumer<Path> callback) {
		return new RenameDirectoryDialog(path, callback);
	}

	private final JPanel _basePanel = new JPanel(new GridLayout(2, 1, 2, 2));
	private final JTextField _textField = new JTextField();
	private final Consumer<Path> _callback;

	private RenameDirectoryDialog(Path path, Consumer<Path> callback) {
		super("Rename a directory");
		getContentPane().setLayout(new BorderLayout());
		add(_basePanel, BorderLayout.NORTH);
		_basePanel.add(new JLabel("Enter a new name"));
		_basePanel.add(_textField);
		_textField.setPreferredSize(new Dimension(400, _textField.getPreferredSize().height));
		_textField.setText(path.toString());
		_callback = callback;
		InputMap im = _textField.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap am = _textField.getActionMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "apply");
        am.put("apply", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	RenameDirectoryDialog.this._buttons.applyButton.doClick();
            }
        });
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
        am.put("cancel", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	RenameDirectoryDialog.this._buttons.cancelButton.doClick();
            }
        });
	}

	@Override
	public void apply() {
		String text = _textField.getText().trim();
		if (text.length() > 0) {
			Path path = Path.of(text);
			_callback.accept(path);
			super.apply();
		}
	}

}
