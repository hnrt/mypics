package com.hideakin.mypics.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.nio.file.Path;
import java.util.function.Consumer;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class CreateDirectoryDialog extends ModalDialog {

	private static final long serialVersionUID = -6276973142978259898L;

	public static CreateDirectoryDialog create(Path path, Consumer<Path> callback) {
		return new CreateDirectoryDialog(path, callback);
	}

	private final JPanel _basePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
	private final JTextField _textField = new JTextField("");
	private final Path _path;
	private final Consumer<Path> _callback;

	private CreateDirectoryDialog(Path path, Consumer<Path> callback) {
		super(String.format("Create a new directory in %s", path));
		getContentPane().setLayout(new BorderLayout());
		add(_basePanel, BorderLayout.NORTH);
		_basePanel.add(new JLabel("Enter new directory name"));
		_basePanel.add(_textField);
		_textField.setPreferredSize(new Dimension(200, _textField.getPreferredSize().height));
		_path = path;
		_callback = callback;
		
	}

	@Override
	public void apply() {
		String text = _textField.getText().trim();
		if (text.length() > 0) {
			Path path = _path.resolve(text);
			_callback.accept(path);
			super.apply();
		}
	}

}
