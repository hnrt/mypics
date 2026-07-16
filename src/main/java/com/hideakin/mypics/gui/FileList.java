package com.hideakin.mypics.gui;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.nio.file.Path;
import java.util.function.BiFunction;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import com.hideakin.mypics.Application;
import com.hideakin.mypics.gui.model.FileListModel;
import com.hideakin.mypics.gui.renderer.PathListCellRenderer;

import static com.hideakin.mypics.Application.configuration;

public class FileList extends JList<Path> {

	public static final int FIRST = 0;
	public static final int LAST = -1;

	public static final int SIMPLE_RENDERER = 0;
	public static final int THUMBNAIL_RENDERER = 1;
	
	private static final long serialVersionUID = 5229274496231891006L;

	public static FileList of(FileListModel model) {
		return new FileList(model);
	}

	private static final String CTRL0 = "ctrl0";
	private static final String CTRL1 = "ctrl1";
	private static final String CTRL2 = "ctrl2";
	private static final String CTRL3 = "ctrl3";
	private static final String CTRL4 = "ctrl4";
	private static final String CTRL5 = "ctrl5";
	private static final String CTRL6 = "ctrl6";
	private static final String CTRL7 = "ctrl7";
	private static final String CTRL8 = "ctrl8";
	private static final String CTRL9 = "ctrl9";
	private static final String DELETE = "delete";
	private static final String UNDO = "undo";
	private static final String EDIT = "edit";

	private final PathListCellRenderer _listCellRenderer = new PathListCellRenderer();
	private final JTextField _editor = new JTextField();
	private int _editingIndex = -1;
	private BiFunction<Path, String, Path> _onCommitRenaming;
	private final FileListModel _model;

	private FileList(FileListModel model) {
		super(model);
		_model = model;
		setCellRenderer(_listCellRenderer);
		_listCellRenderer.enableThumbnail(configuration.getFileListCellRenderer() == THUMBNAIL_RENDERER);
		_model.onClear(() -> _listCellRenderer.clearCache());
		InputMap im = getInputMap(JComponent.WHEN_FOCUSED);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_0, InputEvent.CTRL_DOWN_MASK), CTRL0);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_1, InputEvent.CTRL_DOWN_MASK), CTRL1);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_2, InputEvent.CTRL_DOWN_MASK), CTRL2);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_3, InputEvent.CTRL_DOWN_MASK), CTRL3);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_4, InputEvent.CTRL_DOWN_MASK), CTRL4);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_5, InputEvent.CTRL_DOWN_MASK), CTRL5);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_6, InputEvent.CTRL_DOWN_MASK), CTRL6);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_7, InputEvent.CTRL_DOWN_MASK), CTRL7);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_8, InputEvent.CTRL_DOWN_MASK), CTRL8);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_9, InputEvent.CTRL_DOWN_MASK), CTRL9);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), DELETE);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_DOWN_MASK), UNDO);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), EDIT);
        ActionMap am = getActionMap();
        am.put(CTRL0, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	Application.moveTo(0);
            }
        });
        am.put(CTRL1, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	Application.moveTo(1);
            }
        });
        am.put(CTRL2, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	Application.moveTo(2);
            }
        });
        am.put(CTRL3, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	Application.moveTo(3);
            }
        });
        am.put(CTRL4, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	Application.moveTo(4);
            }
        });
        am.put(CTRL5, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	Application.moveTo(5);
            }
        });
        am.put(CTRL6, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	Application.moveTo(6);
            }
        });
        am.put(CTRL7, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	Application.moveTo(7);
            }
        });
        am.put(CTRL8, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	Application.moveTo(8);
            }
        });
        am.put(CTRL9, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	Application.moveTo(9);
            }
        });
        am.put(DELETE, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	Application.remove();
            }
        });
        am.put(UNDO, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	Application.undo();
            }
        });
        am.put(EDIT, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	Application.startRenaming();
            }
        });
        _editor.setVisible(false);
		_editor.addActionListener(e -> finishRenaming(true));
		_editor.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					finishRenaming(false);
				}
			}
		});
        add(_editor);
	}

	public void setCellRenderer(int renderer) {
		configuration.setFileListCellRenderer(renderer);
		_listCellRenderer.enableThumbnail(configuration.getFileListCellRenderer() == THUMBNAIL_RENDERER);
		SwingUtilities.invokeLater(() -> {
			int selected = getSelectedIndex();
			if (selected > -1) {
				ensureIndexIsVisible(selected);
			}
		});
	}

	public void select(Path path) {
		setSelectedValue(path, true);
		SwingUtilities.invokeLater(() -> requestFocusInWindow());
	}

	public void select(int index) {
		int n = _model.getSize();
		if (n < 1) {
			return;
		} else if (index < 0) {
			index = n + index;
			if (index < 0) {
				index = 0;
			}
		} else if (n <= index) {
			index = n - 1;
		}
		setSelectedIndex(index);
		ensureIndexIsVisible(index);
		SwingUtilities.invokeLater(() -> requestFocusInWindow());
	}

	public void startRenaming(BiFunction<Path, String, Path> cb) {
		_editingIndex = getSelectedIndex();
		if (_editingIndex < 0) return;
		java.awt.Rectangle cellBounds = getCellBounds(_editingIndex, _editingIndex);
		if (cellBounds == null) return;
		_editor.setText(_model.get(_editingIndex).getFileName().toString());
		_editor.setBounds(cellBounds);
		_editor.setVisible(true);
		_editor.requestFocus();
		_editor.selectAll();
		_onCommitRenaming = cb;
    }

    private void finishRenaming(boolean commit) {
    	Path target = null;
        if (_editingIndex >= 0 && commit) {
        	String fileName = _editor.getText().trim();
        	Path source = _model.get(_editingIndex);
        	target = _onCommitRenaming.apply(source, fileName);
        	if (target != null) {
                _model.set(_editingIndex, target);
        	}
        }
        _editor.setVisible(false);
        _editingIndex = -1;
        select(target);
    }

    public void adjustSize() {
    	_listCellRenderer.adjustSize(_model);
    }

}
