package com.hideakin.mypics;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

public class PreferencesDialog extends JDialog {

	private static final long serialVersionUID = -3682327462543253658L;

	public static PreferencesDialog of(Frame owner) {
		return new PreferencesDialog(owner);
	}

	private final Configuration _configuration = Configuration.getInstance();
	private boolean result = false;
	private JRadioButton _rb1;
	private JRadioButton _rb2;
	private JRadioButton _rb3;
	private JRadioButton _rb4;
	private JRadioButton _rb5;
	private JRadioButton _rb6;

	private PreferencesDialog(Frame owner) {
		super(owner, "Preferences", true);
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
        _rb1 = new JRadioButton("Fit to window");
        _rb2 = new JRadioButton("Fit to window width");
        _rb3 = new JRadioButton("Fit to window height");
        _rb4 = new JRadioButton("Fixed ratio 100%");
        _rb5 = new JRadioButton("Fixed ratio 200%");
        _rb6 = new JRadioButton("Fixed ratio 500%");
        ScalingMode sm = _configuration.getScalingMode();
        if (sm == ScalingMode.FIT_TO_WINDOW) {
        	_rb1.setSelected(true);
        } else if (sm == ScalingMode.FIT_TO_WINDOW_WIDTH) {
        	_rb2.setSelected(true);
        } else if (sm == ScalingMode.FIT_TO_WINDOW_HEIGHT) {
        	_rb3.setSelected(true);
        } else {
        	double s = _configuration.getScale();
        	if (s >= 5.0) _rb6.setSelected(true);
        	else if (s >= 2.0) _rb5.setSelected(true);
        	else _rb4.setSelected(true);
        }
        ButtonGroup group = new ButtonGroup();
        group.add(_rb1);
        group.add(_rb2);
        group.add(_rb3);
        group.add(_rb4);
        group.add(_rb5);
        group.add(_rb6);
        JPanel radioPanel = new JPanel(new GridLayout(0, 1));
        radioPanel.add(_rb1);
        radioPanel.add(_rb2);
        radioPanel.add(_rb3);
        radioPanel.add(_rb4);
        radioPanel.add(_rb5);
        radioPanel.add(_rb6);
        setLayout(new BorderLayout());
        add(radioPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(owner);
	}

	public void showDialog() {
		setVisible(true);
		if (result) {
			if (_rb1.isSelected()) {
				_configuration.setScalingMode(ScalingMode.FIT_TO_WINDOW);
			} else if (_rb2.isSelected()) {
				_configuration.setScalingMode(ScalingMode.FIT_TO_WINDOW_WIDTH);
			} else if (_rb3.isSelected()) {
				_configuration.setScalingMode(ScalingMode.FIT_TO_WINDOW_HEIGHT);
			} else {
				_configuration.setScalingMode(ScalingMode.RATIO);
				if (_rb6.isSelected()) {
					_configuration.setScale(5.0);
				} else if (_rb5.isSelected()) {
					_configuration.setScale(2.0);
				} else {
					_configuration.setScale(1.0);
				}
			}
		}
	}

}
