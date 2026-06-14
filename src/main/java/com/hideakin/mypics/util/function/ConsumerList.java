package com.hideakin.mypics.util.function;

import java.util.ArrayList;
import java.util.function.Consumer;

public class ConsumerList<T> extends ArrayList<Consumer<T>> {

	private static final long serialVersionUID = -5542712915329364746L;

	public ConsumerList() {
		super();
	}

	public void invoke(T value) {
		for (Consumer<T> cb : this) {
			cb.accept(value);
		}
	}
	
}
