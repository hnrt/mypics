package com.hideakin.mypics;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.nio.file.Path;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

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

	private final FileNameRenderer _fileNameRenderer = new FileNameRenderer();
	private final ThumbnailRenderer _thumbnailRenderer = new ThumbnailRenderer();
	private final JTextField _editor = new JTextField();
	private int _editingIndex = -1;
	private final FileListModel _model;

	private FileList(FileListModel model) {
		super(model);
		_model = model;
		setCellRenderer(Application.configuration.getFileListCellRenderer() == THUMBNAIL_RENDERER ? _thumbnailRenderer : _fileNameRenderer);
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
            	moveTo(0);
            }
        });
        am.put(CTRL1, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	moveTo(1);
            }
        });
        am.put(CTRL2, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	moveTo(2);
            }
        });
        am.put(CTRL3, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	moveTo(3);
            }
        });
        am.put(CTRL4, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	moveTo(4);
            }
        });
        am.put(CTRL5, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	moveTo(5);
            }
        });
        am.put(CTRL6, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	moveTo(6);
            }
        });
        am.put(CTRL7, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	moveTo(7);
            }
        });
        am.put(CTRL8, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	moveTo(8);
            }
        });
        am.put(CTRL9, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	moveTo(9);
            }
        });
        am.put(DELETE, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	remove();
            }
        });
        am.put(UNDO, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	undo();
            }
        });
        am.put(EDIT, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startEditing();
            }
        });
        _editor.setVisible(false);
		_editor.addActionListener(e -> finishEditing(true));
		_editor.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					finishEditing(false);
				}
			}
		});
        add(_editor);
	}

	public void setCellRenderer(int renderer) {
		Application.configuration.setFileListCellRenderer(renderer);
		setCellRenderer(Application.configuration.getFileListCellRenderer() == THUMBNAIL_RENDERER ? _thumbnailRenderer : _fileNameRenderer);
		SwingUtilities.invokeLater(() -> {
			int selected = getSelectedIndex();
			if (selected > -1) {
				ensureIndexIsVisible(selected);
			}
		});
	}

	public void clearCache() {
		_thumbnailRenderer.clearCache();
	}

	public void select(Path path) {
		setSelectedValue(path, true);
		requestFocusInWindow();
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
		requestFocusInWindow();
	}

	private void moveTo(int index) {
		moveTo(Application.configuration.getDestination(index));
	}

	public void moveTo(Path destination) {
		List<Path> selected = getSelectedValuesList();
		if (selected != null && selected.size() > 0) {
			int selectedIndex = getSelectionModel().getMinSelectionIndex();
			clearSelection();
			_model.move(selected, destination, e -> Application.mainFrame.showErrorDialog(e));
			select(selectedIndex);
		}
	}

	public void remove() {
		List<Path> selected = getSelectedValuesList();
		if (selected != null && selected.size() > 0) {
			int selectedIndex = getSelectionModel().getMinSelectionIndex();
			clearSelection();
			_model.remove(selected, e -> Application.mainFrame.showErrorDialog(e));
			select(selectedIndex);
		}
	}

	public void undo() {
		Path selected = getSelectedValue();
		clearSelection();
		_model.undo(e -> Application.mainFrame.showErrorDialog(e));
		if (selected != null) {
			select(selected);
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

	public void startEditing() {
		_editingIndex = getSelectedIndex();
		if (_editingIndex < 0) return;
		java.awt.Rectangle cellBounds = getCellBounds(_editingIndex, _editingIndex);
		if (cellBounds == null) return;
		_editor.setText(_model.get(_editingIndex).getFileName().toString());
		_editor.setBounds(cellBounds);
		_editor.setVisible(true);
		_editor.requestFocus();
		_editor.selectAll();
    }

    private void finishEditing(boolean commit) {
    	Path target = null;
        if (_editingIndex >= 0 && commit) {
        	String fileName = _editor.getText().trim();
        	Path source = _model.get(_editingIndex);
        	FileManager fm = FileManager.getInstance();
        	target = fm.rename(source, fileName, e -> Application.mainFrame.showErrorDialog(e));
        	if (target != null) {
                _model.set(_editingIndex, target);
        	}
        }
        _editor.setVisible(false);
        _editingIndex = -1;
        requestFocusInWindow();
        select(target);
    }

}
