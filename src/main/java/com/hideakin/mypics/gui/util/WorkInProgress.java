package com.hideakin.mypics.gui.util;

import java.awt.Cursor;

import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.SwingUtilities;

import static com.hideakin.mypics.Application.mainFrame;
import static com.hideakin.mypics.Application.debug;

public class WorkInProgress {

	private static final AtomicInteger _count = new AtomicInteger(0);

	private final AtomicInteger _entered = new AtomicInteger(0);
	private Runnable _callback;

	public WorkInProgress() {
	}

	public WorkInProgress(Runnable callback) {
		_callback = callback;
	}

	public void run() {
		try {
			enter();
			_callback.run();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			leave();
		}
	}

	public void run(Runnable callback) {
		try {
			enter();
			callback.run();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			leave();
		}
	}

	public boolean runExclusively() {
		try {
			boolean retval = enter() == 0;
			if (retval) {
				_callback.run();
			}
			return retval;
		} finally {
			leave();
		}
	}

	public boolean runExclusively(Runnable callback) {
		try {
			boolean retval = enter() == 0;
			if (retval) {
				callback.run();
			}
			return retval;
		} finally {
			leave();
		}
	}

	private int enter() {
		int pri = _entered.incrementAndGet();
		if (SwingUtilities.isEventDispatchThread()) {
			doEnter(pri);
		} else {
			SwingUtilities.invokeLater(() -> doEnter(pri));
		}
		return pri - 1;
	}

	private void leave() {
		int pri = _entered.getAndDecrement();
		if (SwingUtilities.isEventDispatchThread()) {
			doLeave(pri);
		} else {
			SwingUtilities.invokeLater(() -> doLeave(pri));
		}
	}

	private void doEnter(int pri) {
		int pub = _count.getAndIncrement();
		if (pub == 0) {
			mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		}
		debug(3, "WorkInProgress::enter %d %d", pub + 1, pri);
	}

	private void doLeave(int pri) {
		int pub = _count.decrementAndGet();
		if (pub == 0) {
			mainFrame.setCursor(Cursor.getDefaultCursor());
		}
		debug(3, "WorkInProgress::leave %d %d", pub + 1, pri);
	}

}
