package com.hideakin.mypics;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

public class FileList extends JList<Path> {

	private static final long serialVersionUID = 5229274496231891006L;

	public static FileList of(FileListModel model) {
		return new FileList(model);
	}

	private final Configuration _configuration = Configuration.getInstance();
	private final FileListModel _model;
	private final List<Consumer<Path>> _onSelected = new ArrayList<>();

	private FileList(FileListModel model) {
		super(model);
		_model = model;
		setCellRenderer(new FileNameRenderer());
		addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
            	Path selected = getSelectedValue();
            	for (Consumer<Path> cb : _onSelected) {
            		cb.accept(selected);
            	}
            }
        });
		InputMap im = getInputMap(JComponent.WHEN_FOCUSED);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_0, InputEvent.CTRL_DOWN_MASK), "ctrl0");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_1, InputEvent.CTRL_DOWN_MASK), "ctrl1");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_2, InputEvent.CTRL_DOWN_MASK), "ctrl2");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_3, InputEvent.CTRL_DOWN_MASK), "ctrl3");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_4, InputEvent.CTRL_DOWN_MASK), "ctrl4");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_5, InputEvent.CTRL_DOWN_MASK), "ctrl5");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_6, InputEvent.CTRL_DOWN_MASK), "ctrl6");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_7, InputEvent.CTRL_DOWN_MASK), "ctrl7");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_8, InputEvent.CTRL_DOWN_MASK), "ctrl8");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_9, InputEvent.CTRL_DOWN_MASK), "ctrl9");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_DOWN_MASK), "undo");
        ActionMap am = getActionMap();
        am.put("ctrl0", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	moveTo(0);
            }
        });
        am.put("ctrl1", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	moveTo(1);
            }
        });
        am.put("ctrl2", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	moveTo(2);
            }
        });
        am.put("ctrl3", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	moveTo(3);
            }
        });
        am.put("ctrl4", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	moveTo(4);
            }
        });
        am.put("ctrl5", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	moveTo(5);
            }
        });
        am.put("ctrl6", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	moveTo(6);
            }
        });
        am.put("ctrl7", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	moveTo(7);
            }
        });
        am.put("ctrl8", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	moveTo(8);
            }
        });
        am.put("ctrl9", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	moveTo(9);
            }
        });
        am.put("undo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	undo();
            }
        });
	}

	public void onSelected(Consumer<Path> callback) {
		_onSelected.add(callback);
	}

	public void select(Path path) {
		setSelectedValue(path, true);
	}

	private void moveTo(int index) {
		moveTo(_configuration.getDestination(index));
	}

	public void moveTo(Path destination) {
		Path selected = getSelectedValue();
		if (selected != null) {
			int selectedIndex = getSelectedIndex();
			try {
				clearSelection();
				_model.move(selected, destination);
				if (0 <= selectedIndex && selectedIndex < _model.getSize()) {
					setSelectedIndex(selectedIndex);
				}
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this, e.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	public void remove() {
		Path selected = getSelectedValue();
		if (selected != null) {
			int selectedIndex = getSelectedIndex();
			try {
				clearSelection();
				_model.remove(selected);
				if (0 <= selectedIndex && selectedIndex < _model.getSize()) {
					setSelectedIndex(selectedIndex);
				}
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this, e.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	public void undo() {
		Path selected = getSelectedValue();
		try {
			clearSelection();
			_model.undo();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
		}
		if (selected != null) {
			setSelectedValue(selected, true);
		}
	}

	public void copyPath() {
		Path selected = getSelectedValue();
		if (selected != null) {
			StringSelection selection = new StringSelection(selected.toString());
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, null);
		}
	}

}
