package com.hideakin.mypics;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.nio.file.Path;
import java.util.function.Consumer;
import static com.hideakin.mypics.Configuration.NUMBER_OF_DESTINATIONS;

public class MoveDestinationDialog extends ModalDialog {

	private static final long serialVersionUID = -1768514000467514333L;

	public static MoveDestinationDialog create() {
		return new MoveDestinationDialog();
	}

	private static final String ENTER_PRESSED = "enterPressed";
	private static final String DELETE_PRESSED = "deletePressed";

	private static class Item {

		public String name;
		public Path value;

		public Item(String name, Path value) {
			this.name = name;
			this.value = value;
		}

	}

	private static class ItemRenderer extends DefaultListCellRenderer {

		private static final long serialVersionUID = -5832370446677505061L;

		@Override
		public Component getListCellRendererComponent(
				JList<?> list,
				Object value,
				int index,
				boolean isSelected,
				boolean cellHasFocus) {
			Item item = (Item)value;
			String text = String.format("%s %s", item.name, item.value == null ? "" : item.value.toString());
			return super.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
		}
		
	}

	private static class ButtonClickListener extends MouseAdapter {

		private JList<Item> _list;
		private Consumer<Integer> _callback;

        public ButtonClickListener(JList<Item> list, Consumer<Integer> callback) {
            _list = list;
            _callback = callback;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            int index = _list.locationToIndex(e.getPoint());
            if (0 <= index && index < NUMBER_OF_DESTINATIONS) {
            	_callback.accept(index);
            }
        }

	}

	private final DefaultListModel<Item> _model = new DefaultListModel<>();

	private MoveDestinationDialog() {
		super("Move Destinations (click to change)");
        for (int i = 0; i < NUMBER_OF_DESTINATIONS; i++) {
        	_model.addElement(new Item(String.format("CTRL-%d", i), Application.configuration.getDestination(i)));
        }
		JList<Item> list = new JList<>(_model);
		list.setCellRenderer(new ItemRenderer());
        list.addMouseListener(new ButtonClickListener(list, index -> change(index)));
        InputMap im = list.getInputMap(JComponent.WHEN_FOCUSED);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), ENTER_PRESSED);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), DELETE_PRESSED);
        ActionMap am = list.getActionMap();
        am.put(ENTER_PRESSED, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = list.getSelectedIndex();
                change(index);
            }
        });
        am.put(DELETE_PRESSED, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = list.getSelectedIndex();
                Item item = _model.get(index);
                item.value = null;
                _model.set(index, item);
            }
        });
        getContentPane().setLayout(new BorderLayout());
        add(new JScrollPane(list), BorderLayout.CENTER);
        setSize(400, 200);
	}

	private void change(int index) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(String.format("Move Destination on CTRL+%d", index));
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        Item item = _model.get(index);
        if (item.value != null) {
        	chooser.setSelectedFile(item.value.toFile());
        } else {
        	chooser.setSelectedFile(Application.configuration.getDirectory().toFile());
        }
        int result = chooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
        	item.value = chooser.getSelectedFile().toPath();
        	_model.set(index, item);
        }
	}

	@Override
	public void apply() {
		for (int i = 0; i < NUMBER_OF_DESTINATIONS; i++) {
			Item item = _model.get(i);
			Application.configuration.setDestination(i, item.value);
		}
		super.apply();
	}

}
