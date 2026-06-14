package com.hideakin.mypics.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.JTextField;

public class TextField extends JTextField {

	private static final long serialVersionUID = 8740103788889358256L;

	private String _placeholder;

	public TextField() {
		this("");
	}

	public TextField(String placeholder) {
		super();
		_placeholder = placeholder;
		getCaret().setBlinkRate(500);
	    addFocusListener(new FocusAdapter() {
	        @Override
	        public void focusGained(FocusEvent e) { repaint(); }
	        @Override
	        public void focusLost(FocusEvent e) { repaint(); }
	    });
	}

	public String placeholder() {
		return _placeholder;
	}

	public void setPlaceholder(String value) {
		_placeholder = value;
		repaint();
	}

	@Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (getText().isEmpty() && !isFocusOwner()) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(Color.GRAY);
            g2.setFont(getFont().deriveFont(Font.ITALIC));
            Insets insets = getInsets();
            g2.drawString(_placeholder, insets.left + 2, getHeight() / 2 + g2.getFontMetrics().getAscent() / 2 - 2);
            g2.dispose();
        }	
	}

}
