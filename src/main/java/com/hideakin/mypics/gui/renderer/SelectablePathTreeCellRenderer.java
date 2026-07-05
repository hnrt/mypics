package com.hideakin.mypics.gui.renderer;

import java.awt.Component;
import java.awt.FlowLayout;
import java.nio.file.Path;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;

import com.hideakin.mypics.gui.model.DirectorySelectionTreeModel;
import com.hideakin.mypics.gui.util.Thumbnail;
import com.hideakin.mypics.model.SelectablePath;
import static com.hideakin.mypics.Application.debug;

public class SelectablePathTreeCellRenderer extends JPanel implements TreeCellRenderer {

	private static final long serialVersionUID = 7455046819692703457L;

	private final JCheckBox _checkBox = new JCheckBox();
	private final JLabel _thumbnail = new JLabel();
	private final JLabel _label = new JLabel();
	private final DefaultTreeCellRenderer _defaultRenderer = new DefaultTreeCellRenderer();
	private final Map<Path, Icon> _icons;

	public SelectablePathTreeCellRenderer() {
		this(null);
	}

	public SelectablePathTreeCellRenderer(Map<Path, Icon> icons) {
		super();
		setLayout(new FlowLayout(FlowLayout.LEFT, 2, 0));
		add(_checkBox);
		add(_thumbnail);
		add(_label);
		setOpaque(false);
		_checkBox.setOpaque(false);
		_thumbnail.setOpaque(false);
		_label.setOpaque(false);
		_icons = icons;
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
		Object obj = node.getUserObject();
		if (obj instanceof SelectablePath sp) {
			debug(4, "SelectablePathTreeCellRenderer::getTreeCellRendererComponent: %s %s", sp.selected() ? "T" : "F", sp.path());
			_checkBox.setSelected(sp.selected());
			_checkBox.setEnabled(sp.enabled());
            if (sp.isRegularFile()) {
				_thumbnail.setIcon(_icons != null
					? Thumbnail.of(sp.path(), _icons, icon -> {
						_thumbnail.setIcon(icon);
						_thumbnail.setVisible(true);
						DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
						model.nodeChanged(node);
					})
					: Thumbnail.of(sp.path()));
				_thumbnail.setVisible(true);
            } else {
				_thumbnail.setVisible(false);
            }
			_label.setText(sp.path() != null ? sp.path().toString() : "");
			if (tree.getModel() instanceof DirectorySelectionTreeModel) {
				// DO NOT CARE
			} else if (selected) {
                setOpaque(true);
                setBackground(_defaultRenderer.getBackgroundSelectionColor());
                _label.setForeground(_defaultRenderer.getTextSelectionColor());
            } else {
                setOpaque(false);
                _label.setForeground(_defaultRenderer.getTextNonSelectionColor());
            }
			return this;
		} else {
			return _defaultRenderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
		}
	}

}
