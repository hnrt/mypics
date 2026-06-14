package com.hideakin.mypics.util.function;

import java.util.ArrayList;

public class RunnableList extends ArrayList<Runnable> {

	private static final long serialVersionUID = 9158050183169005553L;

	public RunnableList() {
		super();
	}

	public void invoke() {
		for (Runnable cb : this) {
			cb.run();
		}
	}

}
