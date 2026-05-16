package com.hideakin.mypics;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

public class PreferencesDialog extends ModalDialogBox {

	private static final long serialVersionUID = -3682327462543253658L;

	public static PreferencesDialog of(ImageViewer viewer) {
		return new PreferencesDialog(viewer);
	}

	private static class ImageSizePanel extends JPanel {

		private static final long serialVersionUID = 5161904274383406682L;
		
		public static ImageSizePanel of(String label) {
			return new ImageSizePanel(label);
		}

		private static class ScaleItem {

			public String label;
			public double scale;

			public ScaleItem(String label, double scale) {
				this.label = label;
				this.scale = scale;
			}

			@Override
			public String toString() {
				return label;
			}

		}

		private static class ScaleComboBox extends JComboBox<ScaleItem> {

			private static final long serialVersionUID = 1137230436358484289L;

			private static ScaleItem[] _items = new ScaleItem[] {
					new ScaleItem("25%", 0.25),
					new ScaleItem("50%", 0.5),
					new ScaleItem("75%", 0.75),
					new ScaleItem("100%", 1.0),
					new ScaleItem("200%", 2.0),
					new ScaleItem("300%", 3.0),
					new ScaleItem("500%", 5.0)
			};

			private final Configuration _configuration = Configuration.getInstance();

			public ScaleComboBox() {
				super(_items);
				setSelectedIndex(getScaleItemIndex());
			}

			private int getScaleItemIndex() {
				double s = _configuration.getScale();
				for (int i = 0; i < _items.length; i++) {
					if (_items[i].scale == s) {
						return i;
					}
				}
				_configuration.setScale(_items[3].scale);
				return 3;
			}

			public void apply() {
				_configuration.setScale(((ScaleItem)getSelectedItem()).scale);
			}

		}

		private static class FixedScalePanel extends JPanel {

			private static final long serialVersionUID = 7083879944453902826L;

			private JRadioButton _rb;
			private ScaleComboBox _cb;

			public FixedScalePanel(String label) {
				super(new GridLayout(1, 0));
				_rb = new JRadioButton(label);
				_cb = new ScaleComboBox();
				add(_rb);
				add(_cb);
				setSelected(false);
				_rb.addItemListener(e -> _cb.setEnabled(e.getStateChange() == ItemEvent.SELECTED));
			}

			public JRadioButton radioButton() {
				return _rb;
			}

			public ScaleComboBox comboBox() {
				return _cb;
			}

			public void setSelected(boolean value) {
				_rb.setSelected(value);
				_cb.setEnabled(value);
			}

		}

		private final Configuration _configuration = Configuration.getInstance();
		private JRadioButton _rb1;
		private JRadioButton _rb2;
		private JRadioButton _rb3;
		private FixedScalePanel _rb4;

		private ImageSizePanel(String label) {
			super(new GridLayout(0, 1));
	        _rb1 = new JRadioButton("Fit to window");
	        _rb2 = new JRadioButton("Fit to window width");
	        _rb3 = new JRadioButton("Fit to window height");
	        _rb4 = new FixedScalePanel("Fixed scale");
	        ButtonGroup group = new ButtonGroup();
	        group.add(_rb1);
	        group.add(_rb2);
	        group.add(_rb3);
	        group.add(_rb4.radioButton());
	        ScalingMode sm = _configuration.getScalingMode();
	        if (sm == ScalingMode.FIT_TO_WINDOW) {
	        	_rb1.setSelected(true);
	        } else if (sm == ScalingMode.FIT_TO_WINDOW_WIDTH) {
	        	_rb2.setSelected(true);
	        } else if (sm == ScalingMode.FIT_TO_WINDOW_HEIGHT) {
	        	_rb3.setSelected(true);
	        } else {
	        	_rb4.setSelected(true);
	        }
	        setBorder(BorderFactory.createTitledBorder("Image Size"));
	        add(_rb1);
	        add(_rb2);
	        add(_rb3);
	        add(_rb4);
		}

		public void apply() {
			if (_rb1.isSelected()) {
				_configuration.setScalingMode(ScalingMode.FIT_TO_WINDOW);
			} else if (_rb2.isSelected()) {
				_configuration.setScalingMode(ScalingMode.FIT_TO_WINDOW_WIDTH);
			} else if (_rb3.isSelected()) {
				_configuration.setScalingMode(ScalingMode.FIT_TO_WINDOW_HEIGHT);
			} else {
				_configuration.setScalingMode(ScalingMode.RATIO);
				_rb4.comboBox().apply();
			}
		}

	}

	private final ImageSizePanel _imageSizePanel;

	private PreferencesDialog(ImageViewer viewer) {
		super(viewer, "Preferences");
        _imageSizePanel = ImageSizePanel.of("Image Size");
        add(_imageSizePanel, BorderLayout.CENTER);
	}

	@Override
	public void apply() {
		_imageSizePanel.apply();
		super.apply();
	}

}
