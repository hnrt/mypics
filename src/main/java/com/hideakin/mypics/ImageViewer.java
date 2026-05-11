package com.hideakin.mypics;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ImageViewer extends JFrame {

	private static final long serialVersionUID = -3714006055304394239L;

	private final Configuration _configuration = Configuration.getInstance();

	private final ListPane _listPane = ListPane.create();
	private final ImagePane _imagePane = ImagePane.create();

    public ImageViewer() {
        super("Image Viewer");

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                _listPane,
                _imagePane
        );
        splitPane.setDividerLocation(250);
        add(splitPane, BorderLayout.CENTER);

        setJMenuBar(MenuBar.of(this));

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
            	_configuration.save();
                System.exit(0);
            }
        });

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
            	int state = ImageViewer.this.getExtendedState();
            	if ((state & (Frame.MAXIMIZED_BOTH | Frame.ICONIFIED)) == 0) {
            		int w = ImageViewer.this.getWidth();
            		int h = ImageViewer.this.getHeight();
            		_configuration.setWindowSize(w, h);
            	}
            }
        });

        _listPane.onChanged(path -> {
            setTitle(String.format("%s", path));
        });
        _listPane.onSelected(path -> {
        	_imagePane.loadFrom(path);
        });

        _imagePane.onChanged(pane -> {
        	if (pane.path() == null) {
        		setTitle(String.format("%s", _configuration.getDirectory()));
        	} else {
        		setTitle(String.format("%s [%d%%]", pane.path(), (int)(pane.scale() * 100)));
        	}
        });

        setSize(_configuration.getWidth(), _configuration.getHeight());
        setLocationRelativeTo(null);

        _listPane.loadDirectoryFrom(_configuration.getDirectory());
    }

    public ListPane listPane() {
    	return _listPane;
    }

    public ImagePane imagePane() {
    	return _imagePane;
    }

}
