package com.hideakin.mypics.gui.renderer;

import static com.hideakin.mypics.Application.debug;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.function.Consumer;

import javax.swing.AbstractCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreePath;

import com.hideakin.mypics.model.SelectablePath;

public class SelectablePathTreeCellEditor extends AbstractCellEditor implements TreeCellEditor {

	private static final long serialVersionUID = 1145783160399510543L;

	private final JPanel _panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
    private final JCheckBox _checkBox = new JCheckBox();
    private final JLabel _label = new JLabel();
    private SelectablePath _current;
    private Consumer<SelectablePath> _onChanged;

	public SelectablePathTreeCellEditor(Consumer<SelectablePath> onChanged) {
		super();
		_panel.add(_checkBox);
		_panel.add(_label);
		_panel.setOpaque(false);
		_checkBox.setOpaque(false);
		_label.setOpaque(false);
		_onChanged = onChanged;
        _checkBox.addActionListener(e -> {
            if (_current != null) {
                _current.setSelected(_checkBox.isSelected());
    			debug(4, "SelectablePathTreeCellEditor::SelectablePathTreeCellEditor: %s %s", _current.selected() ? "T" : "F", _current.path());
    			if (_onChanged != null) {
    				_onChanged.accept(_current);
    			}
            }
            stopCellEditing();
        });
	}

    @Override
    public Object getCellEditorValue() {
        return _current;
    }

    @Override
    public Component getTreeCellEditorComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        Object obj = node.getUserObject();
        if (obj instanceof SelectablePath sp) {
			debug(4, "SelectablePathTreeCellEditor::getTreeCellEditorComponent: %s %s", sp.selected() ? "T" : "F", sp.path());
            _current = sp;
            _checkBox.setSelected(sp.selected());
            _checkBox.setEnabled(sp.enabled());
            _label.setText(sp.path().toString());
            return _panel;
        }
        return null;
    }

	@Override
    public boolean isCellEditable(EventObject e) {
        if (!(e instanceof MouseEvent me)) return false;
        JTree tree = (JTree)e.getSource();
        TreePath path = tree.getPathForLocation(me.getX(), me.getY());
        if (path == null) return false;
        Object obj = ((DefaultMutableTreeNode)path.getLastPathComponent()).getUserObject();
        if (!(obj instanceof SelectablePath sp)) return false;
        Rectangle bounds = tree.getPathBounds(path);
        if (bounds == null) return false;
        int checkWidth = 20;
        return (me.getX() - bounds.x < checkWidth) && sp.enabled();
    }

}
