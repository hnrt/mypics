package com.hideakin.mypics;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Path;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;

public class TrashDialogBox extends ModalDialogBox {

	private static final long serialVersionUID = 3096770394969390057L;

	public static TrashDialogBox of(ImageViewer viewer) {
		return new TrashDialogBox(viewer);
	}

	private static class Item {

		public boolean selected;
		public Path path;

		public Item(Path path) {
			this.path = path;
		}

		@Override
		public String toString() {
			return path.toString();
		}

	}

	private static class TrashListCellRenderer extends JPanel implements ListCellRenderer<Item> {

		private static final long serialVersionUID = -3000736668631280570L;

		private final JCheckBox _check = new JCheckBox();
		private final JLabel _label = new JLabel();

		public TrashListCellRenderer() {
			setLayout(new BorderLayout());
			add(_check, BorderLayout.WEST);
			add(_label, BorderLayout.CENTER);
		}

		@Override
		public Component getListCellRendererComponent(JList<? extends Item> list, Item value, int index, boolean isSelected, boolean cellHasFocus) {
			_check.setSelected(value.selected);
			_label.setText(value.path.toString());
			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}
			setOpaque(true);
			return this;
		}

	}

	private final Configuration _configuration = Configuration.getInstance();
	private final DefaultListModel<Item> _model = new DefaultListModel<>();

	private TrashDialogBox(ImageViewer viewer) {
		super(viewer, "Trash (Select items to restore)");
		UndoManager.getInstance()
			.trash()
			.stream()
			.sorted()
			.forEach(path -> {
				_model.addElement(new Item(path));
			});
		JList<Item> list = new JList<>(_model);
        list.setCellRenderer(new TrashListCellRenderer());
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = list.locationToIndex(e.getPoint());
                if (index >= 0) {
                    Item item = _model.get(index);
                    item.selected = !item.selected;
                    list.repaint(list.getCellBounds(index, index));
                }
            }
        });
        getContentPane().setLayout(new BorderLayout());
        add(new JScrollPane(list), BorderLayout.CENTER);
        setSize(400, 200);
	}

	@Override
	public void apply() {
		int restored = 0;
		UndoManager um = UndoManager.getInstance();
		int n = _model.getSize();
		for (int i = 0; i < n; i++) {
			Item item = _model.getElementAt(i);
			if (item.selected) {
				try {
					if (um.restore(item.path) != null) {
						restored++;
					}
				} catch (Exception e) {
					JOptionPane.showMessageDialog(this, e.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		if (restored > 0) {
			Path selected = _viewer.listPane().fileList().getSelectedValue();
			_viewer.loadDirectoryFrom(_configuration.getDirectory());
			_viewer.listPane().fileList().select(selected);
		}
		super.apply();
	}
	
}
