package com.hideakin.mypics.gui.component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import com.hideakin.mypics.gui.renderer.SelectablePathTreeCellEditor;
import com.hideakin.mypics.gui.renderer.SelectablePathTreeCellRenderer;
import com.hideakin.mypics.model.SelectablePath;

public class SelectablePathTree extends JTree {

	private static final long serialVersionUID = -6326954866653109798L;

	private final DefaultTreeModel _model;
	private final DefaultMutableTreeNode _root;

	public SelectablePathTree() {
		super(new DefaultMutableTreeNode("ROOT"));
		setRootVisible(false);
		setCellRenderer(new SelectablePathTreeCellRenderer());
		setCellEditor(new SelectablePathTreeCellEditor());
		setEditable(true);
		_model = (DefaultTreeModel)super.getModel();
		_root = (DefaultMutableTreeNode)_model.getRoot();
	}

	public void loadSubdirectories(Path directory) {
		try {
			List<Path> entries = Files.list(directory).toList();
			List<Path> dd = entries.stream().filter(e -> Files.isDirectory(e)).collect(Collectors.toList());
			dd.sort(Comparator.comparing(e -> e.getFileName().toString()));
			for (Path path : dd) {
				_root.add(new DefaultMutableTreeNode(new SelectablePath(path, false)));
			}
			_model.reload();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Path[] checked() {
		List<Path> pp = new ArrayList<>();
		int n = _root.getChildCount();
		for (int i = 0; i < n; i++) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)_root.getChildAt(i);
			SelectablePath sp = (SelectablePath)node.getUserObject();
			if (sp.selected()) {
				pp.add(sp.path());
			}
		}
		return pp.toArray(new Path[pp.size()]);
	}

}
