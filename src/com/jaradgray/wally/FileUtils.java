package com.jaradgray.wally;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class FileUtils {
	/**
	 * Returns the given File's data as a String.
	 * @param file
	 * @return
	 */
	public static String getTextFromFile(File file) {
		String result = "";
		try {
			result = new String(Files.readAllBytes(file.toPath()));
		} catch(IOException e) {
			e.printStackTrace();
		}
		return result;
	}
}
