package com.hideakin.mypics.model;

public class TagNode {

	protected final String _tag;

	public TagNode(String tag) {
		_tag = tag;
	}

	@Override
	public String toString() {
		return _tag;
	}

	@Override
	public int hashCode() {
		return _tag.hashCode();
	}

	@Override
	public boolean equals(Object other) {
		return _tag != null ? _tag.equals(other) : other == null;
	}

}
