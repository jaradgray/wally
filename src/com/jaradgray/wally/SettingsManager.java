package com.jaradgray.wally;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class SettingsManager {
	public static final String KEY_ROOT_DIR = "root_dir";
	public static final String KEY_MIN_INTERVAL = "min_interval";
	public static final String KEY_MAX_INTERVAL = "max_interval";
	public static final String KEY_MIN_INTERVAL_UNIT = "min_interval_unit";
	public static final String KEY_MAX_INTERVAL_UNIT = "max_interval_unit";
	public static final String KEY_SHOWN_PATHS_LIST = "shown_paths";
	public static final String KEY_LAST_SHOWN_PATH = "last_shown_path";
	
	public static final String DEFAULT_ROOT_DIR = "D:\\Jarad\\Pictures\\Wallpapers\\wally_dir";
	public static final long DEFAULT_MIN_INTERVAL = 2;
	public static final long DEFAULT_MAX_INTERVAL = 3;
	public static final String DEFAULT_MIN_INTERVAL_UNIT = "s";
	public static final String DEFAULT_MAX_INTERVAL_UNIT = "h";
	public static final String DEFAULT_LAST_SHOWN_PATH = DEFAULT_ROOT_DIR + File.separator + "kikuno.jpg";
	
	public static final String APP_DIR_PATH = System.getenv("AppData") + File.separator + "Wally";
	public static final String SETTINGS_FILE_PATH = APP_DIR_PATH + File.separator + "settings.json";
	
	private final JSONObject mSettingsObj;
	
	public SettingsManager() {
		// Set mSettingsObj based on app's local settings file's data
		File settingsFile = new File(SETTINGS_FILE_PATH);
		// create settings file from default values if it doesn't exist
		if (!settingsFile.exists()) {
			createDefaultSettingsFile();
		}
		String settingsFileText = FileUtils.getTextFromFile(settingsFile);
		mSettingsObj = new JSONObject(settingsFileText);
	}
	
	
	// Public methods
	
	public String[] getUnshownPaths() {
		// Get all paths in root directory
		String[] allPaths = getImagePathsInRootDir();
				
		// Get list of shown paths
		JSONArray shownPaths = mSettingsObj.getJSONArray(KEY_SHOWN_PATHS_LIST);
				
		// If shown paths is empty, return allPaths
		if (shownPaths.length() == 0) {
			return allPaths;
		}
		
		// Remove paths in shown paths from paths in root directory
		List<String> unshownPaths = new ArrayList<>();
		List<String> allPathsList = Arrays.asList(allPaths);
		List<Object> shownPathsList = shownPaths.toList();
		for (String path : allPathsList) {
			boolean doAdd = true;
			for (int i = 0; i < shownPathsList.size(); i++) {
				String s = (String) shownPathsList.get(i);
				if (s.equals(path)) {
					doAdd = false;
					break;
				}
			}
			if (doAdd) {
				unshownPaths.add(path);
			}
		}
		
		// If unshownPaths is empty, we've shown every image in the Wally
		//	directory with no repetitions, so now we can "reset" and show
        //  any path in the Wally directory, except for the last shown path
        //  (because we'll assume it's currently showing):
		//		- "reset" by clearing the "shown_paths" entry in the settings
        //          file
		//		- return an array containing all paths in the root
		//			directory, minus the last selected path
		if (unshownPaths.isEmpty()) {
			clearShownPaths();
			// Build the array of all paths minus the last selected path
			String lastPath = mSettingsObj.getString(KEY_LAST_SHOWN_PATH);
			String[] result = new String[allPaths.length - 1];
			int index = 0;
			for (int i = 0; i < allPaths.length; i++) {
				String path = allPaths[i];
				if (path.equals(lastPath)) continue;
				result[index++] = path;
			}
			return result;
		}
		
		// Return resulting list as an array
		return unshownPaths.toArray(new String[] {});
	}
	
	public void addShownPath(String path) {
		// Add given path to mSettingsObj's "shown_paths" entry
		JSONArray shownPaths = mSettingsObj.getJSONArray(KEY_SHOWN_PATHS_LIST);
		shownPaths.put(path);
		// Update mSettingsObj's "last_shown_path" entry
		mSettingsObj.put(KEY_LAST_SHOWN_PATH, path);
		// Write mSettingsObj to settings file
		writeSettingsFile(mSettingsObj);
	}
	
	/**
	 * Returns the number of seconds represented by the "min_interval" and
	 * "min_interval_unit" values in the app's local settings file.
	 * @return
	 */
	public long getMinSeconds() {
		long min = mSettingsObj.getLong(KEY_MIN_INTERVAL);
		String unit = mSettingsObj.getString(KEY_MIN_INTERVAL_UNIT);
		return getSeconds(min, unit);
	}
	
	/**
	 * Returns the number of seconds represented by the "max_interval" and
	 * "max_interval_unit" values in the app's local settings file.
	 * @return
	 */
	public long getMaxSeconds() {
		long min = mSettingsObj.getLong(KEY_MAX_INTERVAL);
		String unit = mSettingsObj.getString(KEY_MAX_INTERVAL_UNIT);
		return getSeconds(min, unit);
	}
	
	
	// Private methods
	
	private void createDefaultSettingsFile() {
		// Build the JSONObject representing the settings with default values
		JSONObject toWrite = new JSONObject();
		toWrite.put(KEY_ROOT_DIR, DEFAULT_ROOT_DIR);
		toWrite.put(KEY_MIN_INTERVAL, DEFAULT_MIN_INTERVAL);
		toWrite.put(KEY_MAX_INTERVAL, DEFAULT_MAX_INTERVAL);
		toWrite.put(KEY_MIN_INTERVAL_UNIT, DEFAULT_MIN_INTERVAL_UNIT);
		toWrite.put(KEY_MAX_INTERVAL_UNIT, DEFAULT_MAX_INTERVAL_UNIT);
		toWrite.put(KEY_SHOWN_PATHS_LIST, new JSONArray());
		toWrite.put(KEY_LAST_SHOWN_PATH, DEFAULT_LAST_SHOWN_PATH);
		
		// Create the app's local storage directory if it doesn't exist
		try {
			Files.createDirectories(Paths.get(APP_DIR_PATH));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Write the JSONObject's data to the settings file
		writeSettingsFile(toWrite);
	}
	
	/**
	 * Returns an array containing the names of all image files
	 * in the root directory. (Not recursive)
	 * @return
	 */
	private String[] getImagePathsInRootDir() {
		// Get root dir from mSettingsObj
		String rootPath = mSettingsObj.getString(KEY_ROOT_DIR);
		File rootDir = new File(rootPath);
		
		// Make the FilenameFilter we can use to filter for images
		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				// Return true if name ends with an image extension
				if (name.toLowerCase().endsWith(".jpg")
						|| name.toLowerCase().endsWith(".jpeg")
						|| name.toLowerCase().endsWith(".png")) {
					return true;
				}
				return false;
			}
		};
		
		// Return the filtered list of files as absolute path strings
		File[] files = rootDir.listFiles(filter);
		String[] result = new String[files.length];
		for (int i = 0; i < files.length; i++) {
			result[i] = files[i].getAbsolutePath();
		}
		return result;
	}
	
	/**
	 * Given a JSONObject, writes its data to the app's local
	 * settings file.
	 * @param obj
	 */
	private void writeSettingsFile(JSONObject obj) {
		Path path = Paths.get(SETTINGS_FILE_PATH);
		byte[] bytes = obj.toString().getBytes();
		try {
			Files.write(path, bytes);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Sets mSettingsObj's "shown_paths" key-value pair to and empty
	 * JSONArray and writes mSettingsObj to the app's local settings
	 * file.
	 */
	private void clearShownPaths() {
		mSettingsObj.put(KEY_SHOWN_PATHS_LIST, new JSONArray());
		writeSettingsFile(mSettingsObj);
	}
	
	/**
	 * Returns the number of seconds represented by the given value
	 * and unit.
	 * @param value
	 * @param unit
	 * 				one of "s", "m", "h", "d"
	 * @return
	 */
	private long getSeconds(long value, String unit) {
		switch (unit) {
			case "s":
				return value;
			case "m":
				return value * 60;
			case "h":
				return value * 60 * 60;
			case "d":
				return value * 60 * 60 * 24;
			default:
				System.err.println("SettingsManager.getSeconds() - unrecognized unit: \"" + unit + "\"");
				return -1;
		}
	}
}
