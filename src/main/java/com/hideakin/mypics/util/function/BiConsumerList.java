package com.hideakin.mypics.util.function;

import java.util.ArrayList;
import java.util.function.BiConsumer;

public class BiConsumerList<T1,T2> extends ArrayList<BiConsumer<T1,T2>> {

	private static final long serialVersionUID = -1661456824451182986L;

	public BiConsumerList() {
		super();
	}

	public void invoke(T1 value1, T2 value2) {
		for (BiConsumer<T1, T2> cb : this) {
			cb.accept(value1, value2);
		}
	}

}
