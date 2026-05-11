package com.hideakin.mypics;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.nio.file.Path;
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

	private FileList(FileListModel model) {
		super(model);
		_model = model;
		setCellRenderer(new FileNameRenderer());
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
            	if (e.getSource() instanceof FileList list) {
            		move(list.getSelectedValue(), 0, list.getSelectedIndex());
            	}
            }
        });
        am.put("ctrl1", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	if (e.getSource() instanceof FileList list) {
            		move(list.getSelectedValue(), 1, list.getSelectedIndex());
            	}
            }
        });
        am.put("ctrl2", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	if (e.getSource() instanceof FileList list) {
            		move(list.getSelectedValue(), 2, list.getSelectedIndex());
            	}
            }
        });
        am.put("ctrl3", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	if (e.getSource() instanceof FileList list) {
            		move(list.getSelectedValue(), 3, list.getSelectedIndex());
            	}
            }
        });
        am.put("ctrl4", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	if (e.getSource() instanceof FileList list) {
            		move(list.getSelectedValue(), 4, list.getSelectedIndex());
            	}
            }
        });
        am.put("ctrl5", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	if (e.getSource() instanceof FileList list) {
            		move(list.getSelectedValue(), 5, list.getSelectedIndex());
            	}
            }
        });
        am.put("ctrl6", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	if (e.getSource() instanceof FileList list) {
            		move(list.getSelectedValue(), 6, list.getSelectedIndex());
            	}
            }
        });
        am.put("ctrl7", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	if (e.getSource() instanceof FileList list) {
            		move(list.getSelectedValue(), 7, list.getSelectedIndex());
            	}
            }
        });
        am.put("ctrl8", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	if (e.getSource() instanceof FileList list) {
            		move(list.getSelectedValue(), 8, list.getSelectedIndex());
            	}
            }
        });
        am.put("ctrl9", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	if (e.getSource() instanceof FileList list) {
            		move(list.getSelectedValue(), 9, list.getSelectedIndex());
            	}
            }
        });
        am.put("undo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	undo();
            }
        });
	}

	private void move(Path selected, int destinationIndex, int selectedIndex) {
		try {
			clearSelection();
			_model.move(selected, _configuration.getDestination(destinationIndex), selectedIndex);
			if (0 <= selectedIndex && selectedIndex < _model.getSize()) {
				setSelectedIndex(selectedIndex);
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void undo() {
		try {
			_model.undo();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
		}
	}

}
