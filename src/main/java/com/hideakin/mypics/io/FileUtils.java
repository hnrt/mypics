package com.hideakin.mypics.io;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;

import com.hideakin.mypics.util.StringUtils;

public class FileUtils {

	public static String computeSHA256(Path path) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			try (InputStream in = Files.newInputStream(path)) {
				byte[] buffer = new byte[8192];
				int len;
				while ((len = in.read(buffer)) != -1) {
					md.update(buffer, 0, len);
				}
			}
			return StringUtils.toHex(md.digest());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
