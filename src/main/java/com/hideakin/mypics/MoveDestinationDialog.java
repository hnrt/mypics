package com.hideakin.mypics;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.nio.file.Path;
import java.util.function.Consumer;
import static com.hideakin.mypics.Configuration.NUMBER_OF_DESTINATIONS;

public class MoveDestinationDialog extends JDialog {

	private static final long serialVersionUID = -1768514000467514333L;

	private static final String ENTER_PRESSED = "enterPressed";

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

	public static MoveDestinationDialog of(Frame owner) {
		return new MoveDestinationDialog(owner);
	}

	private final Configuration _configuration = Configuration.getInstance();
	private final DefaultListModel<Item> _model = new DefaultListModel<>();
	private boolean result = false;

	private MoveDestinationDialog(Frame owner) {
		super(owner, "Move Destinations (click to change)", true);
        for (int i = 0; i < NUMBER_OF_DESTINATIONS; i++) {
        	_model.addElement(new Item(String.format("CTRL+%d", i), _configuration.getDestination(i)));
        }
		JButton applyButton = new JButton("Apply");
		applyButton.addActionListener(e -> {
			result = true;
			dispose();
		});
		applyButton.setMnemonic(KeyEvent.VK_A);
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(e -> {
			dispose();
		});
		cancelButton.setMnemonic(KeyEvent.VK_C);
		JPanel buttonPanel = new JPanel();
        buttonPanel.add(applyButton);
        buttonPanel.add(cancelButton);
		JList<Item> list = new JList<>(_model);
		list.setCellRenderer(new ItemRenderer());
        list.addMouseListener(new ButtonClickListener(list, index -> change(index)));
        list.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), ENTER_PRESSED);
        list.getActionMap().put(ENTER_PRESSED, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = list.getSelectedIndex();
                change(index);
            }
        });
        getContentPane().setLayout(new BorderLayout());
        add(new JScrollPane(list), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        pack();
        setSize(400, 200);
        setLocationRelativeTo(owner);
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
        	chooser.setSelectedFile(_configuration.getDirectory().toFile());
        }
        int result = chooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
        	item.value = chooser.getSelectedFile().toPath();
        	_model.set(index, _model.get(index));
        }
	}

	public void showDialog() {
		setVisible(true);
		if (result) {
			for (int i = 0; i < NUMBER_OF_DESTINATIONS; i++) {
				Item item = _model.get(i);
				_configuration.setDestination(i, item.value);
			}
		}
	}

}
