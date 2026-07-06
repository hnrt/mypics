package com.hideakin.mypics.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.nio.file.Path;
import java.util.function.Consumer;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class RenameDirectoryDialog extends ModalDialog {

	private static final long serialVersionUID = 6669855121701668646L;

	public static RenameDirectoryDialog create(Path path, Consumer<Path> callback) {
		return new RenameDirectoryDialog(path, callback);
	}

	private final JPanel _basePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
	private final JTextField _textField = new JTextField("");
	private final Path _path;
	private final Consumer<Path> _callback;

	private RenameDirectoryDialog(Path path, Consumer<Path> callback) {
		super(String.format("Rename the directory in %s", path.getParent()));
		getContentPane().setLayout(new BorderLayout());
		add(_basePanel, BorderLayout.NORTH);
		_basePanel.add(new JLabel("Enter new directory name"));
		_basePanel.add(_textField);
		_textField.setPreferredSize(new Dimension(200, _textField.getPreferredSize().height));
		_textField.setText(path.getFileName().toString());
		_path = path;
		_callback = callback;
	}

	@Override
	public void apply() {
		String text = _textField.getText().trim();
		if (text.length() > 0) {
			Path path = _path.getParent().resolve(text);
			_callback.accept(path);
			super.apply();
		}
	}

}
