package com.hideakin.mypics.util;

public class StringUtils {

	public static final char[] HEX = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	public static String toHex(byte[] bb) {
		StringBuilder buf = new StringBuilder(64);
		for (byte b : bb) {
			int v = (int)b + 256;
			buf.append(HEX[(v >> 4) & 0xF]);
			buf.append(HEX[(v >> 0) & 0xF]);
		}
		return buf.toString();
	}

}
