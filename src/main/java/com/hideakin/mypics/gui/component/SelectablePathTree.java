package com.hideakin.mypics.gui.component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import com.hideakin.mypics.gui.model.SelectablePathTreeNode;
import com.hideakin.mypics.gui.renderer.SelectablePathTreeCellEditor;
import com.hideakin.mypics.gui.renderer.SelectablePathTreeCellRenderer;
import com.hideakin.mypics.model.SelectablePath;
import com.hideakin.mypics.util.function.ConsumerList;

import static com.hideakin.mypics.Application.debug;

public class SelectablePathTree extends JTree {

	private static final long serialVersionUID = -6326954866653109798L;

	private final DefaultTreeModel _model;
	private final SelectablePathTreeNode _root;
	private final ConsumerList<SelectablePath> _onChanged = new ConsumerList<>();

	public SelectablePathTree() {
		super(SelectablePathTreeNode.ofRoot());
		setRootVisible(false);
		setCellRenderer(new SelectablePathTreeCellRenderer());
		setCellEditor(new SelectablePathTreeCellEditor(x -> _onChanged.invoke(x)));
		setEditable(true);
		_model = (DefaultTreeModel)super.getModel();
		_root = (SelectablePathTreeNode)_model.getRoot();
		addTreeSelectionListener(e -> {
		    TreePath path = e.getPath();
		    if (path.getLastPathComponent() instanceof SelectablePathTreeNode sptn) {
			    if (!sptn.selectablePath().loaded()) {
			    	loadDirectory(sptn.selectablePath().path());
			    }
			    debug(3, "SelectablePathTree::Selection: %s", sptn.selectablePath().path());
		    }
		});
	}

	public void setSelected(Path path, boolean selected, boolean cascaded) {
        debug(3, "SelectablePathTree::setSelected: %s %s %s", selected ? "T" : "F", cascaded ? "T" : "F", path);
        loadDirectory(path);
		SelectablePathTreeNode found = _root.find(path);
		if (found != null) {
			setSelected(found, selected, cascaded);
			repaint();
		}
	}

	private void setSelected(SelectablePathTreeNode parent, boolean selected, boolean cascaded) {
		SelectablePath sp = parent.selectablePath();
		if (sp.enabled()) {
			sp.setSelected(selected);
		}
		if (cascaded) {
			loadDirectory(sp.path());
			for (SelectablePathTreeNode child : parent.getChildList()) {
				setSelected(child, selected, true);
			}
		}
	}

	public void setEnabled(Path path, boolean enabled, boolean cascaded) {
        debug(3, "SelectablePathTree::setEnabled: %s %s %s", enabled ? "T" : "F", cascaded ? "T" : "F", path);
        loadDirectory(path);
		SelectablePathTreeNode found = _root.find(path);
		if (found != null) {
			setEnabled(found, enabled, cascaded);
			repaint();
		}
	}

	private void setEnabled(SelectablePathTreeNode parent, boolean enabled, boolean cascaded) {
		SelectablePath sp = parent.selectablePath();
		if (!sp.selected()) {
			sp.setEnabled(enabled);
		}
		if (cascaded) {
			loadDirectory(sp.path());
			for (SelectablePathTreeNode child : parent.getChildList()) {
				setEnabled(child, enabled, true);
			}
		}
	}

	public void loadDirectory(Path directory) {
		Path parent = directory.getParent();
		if (parent != null) {
			loadDirectory(parent);
			loadSubdirectories(directory, _root.find(directory));
		} else {
			loadSubdirectories(directory, _root);
		}
	}

	private void loadSubdirectories(Path directory, SelectablePathTreeNode parent) {
		if (!parent.selectablePath().loaded()) {
			parent.selectablePath().setLoaded(true);
	        debug(3, "SelectablePathTree::loadSubdirectories: %s", directory);
			try {
				List<Path> entries = Files.list(directory).toList();
				List<Path> dd = entries.stream().filter(e -> Files.isDirectory(e)).collect(Collectors.toList());
				dd.sort(Comparator.comparing(e -> e.getFileName().toString()));
				for (Path path : dd) {
					parent.addDirectory(path, false);
				}
				_model.reload(parent);
				expandPath(new TreePath(parent.getPath()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public Path[] checked() {
		List<Path> pp = new ArrayList<>();
		checked(_root, pp);
		return pp.toArray(new Path[pp.size()]);
	}

	private void checked(SelectablePathTreeNode parent, List<Path> pp) {
		for (SelectablePathTreeNode child : parent.getChildList()) {
			SelectablePath sp = child.selectablePath();
			if (sp.selected()) {
				pp.add(sp.path());
			}
			checked(child, pp);
		}
	}

	public void onChanged(Consumer<SelectablePath> callback) {
		_onChanged.add(callback);
	}

}
