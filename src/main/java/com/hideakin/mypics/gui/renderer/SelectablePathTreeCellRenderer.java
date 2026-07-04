package com.hideakin.mypics.gui.renderer;

import java.awt.Component;
import java.awt.FlowLayout;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

import com.hideakin.mypics.model.SelectablePath;
import static com.hideakin.mypics.Application.debug;

public class SelectablePathTreeCellRenderer extends JPanel implements TreeCellRenderer {

	private static final long serialVersionUID = 7455046819692703457L;

	private final JCheckBox _checkBox = new JCheckBox();
	private final JLabel _label = new JLabel();
	private final DefaultTreeCellRenderer _defaultRenderer = new DefaultTreeCellRenderer();

	public SelectablePathTreeCellRenderer() {
		super();
		setLayout(new FlowLayout(FlowLayout.LEFT, 2, 0));
		add(_checkBox);
		add(_label);
		setOpaque(false);
		_checkBox.setOpaque(false);
		_label.setOpaque(false);
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
		Object obj = node.getUserObject();
		if (obj instanceof SelectablePath sp) {
			debug(4, "SelectablePathTreeCellRenderer::getTreeCellRendererComponent: %s %s", sp.selected() ? "T" : "F", sp.path());
			_checkBox.setSelected(sp.selected());
			_checkBox.setEnabled(sp.enabled());
			_label.setText(sp.path().toString());
			return this;
		} else {
			return _defaultRenderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
		}
	}

}
