package com.jaradgray.wally;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Wally {
	private final WallpaperManager mWallpaperManager = new WallpaperManager();
	private final Random mRandom = new Random();
	private final ScheduledExecutorService mScheduler = Executors.newScheduledThreadPool(1);
	private final Runnable mRunnable = new Runnable() {
		@Override
		public void run() {
//			System.out.println("Hello from Runnable");
			SettingsManager manager = new SettingsManager();
			
			// Get a random unshown path
			String[] paths = manager.getUnshownPaths();
			
			String log = "Unshown paths:\n";
			for (String s : paths) log += s + "\n";
			
			int index = mRandom.nextInt(paths.length);
			String path = paths[index];
			
			log += "\nSelected path:\n" + path;
			System.out.println(log);
			
			// Set wallpaper to the selected path
			mWallpaperManager.setWallpaper(path);
			
			// Add selected path to the list of paths we've already shown
			manager.addShownPath(path);
			
			// Pick a random time between min and max intervals
			long min = manager.getMinSeconds();
			long max = manager.getMaxSeconds();
			int i = mRandom.nextInt((int)(max - min) + 1);
			long seconds = i + min;
			
			String logg = "Min:\t" + min + "\nMax:\t" + max + "\nRandom:\t" + seconds + "\n";
			System.out.println(logg);
			
			// Schedule this Runnable to be run again at the selected time
			mScheduler.schedule(mRunnable, seconds, TimeUnit.SECONDS);
			
			
			
			
			// Get data from the settings obj
			
			// Remove any paths of images we've already shown from the list of file paths in the wally dir
			
			// Pick a random path from the resulting list
			
			// Set wallpaper to the image at the selected path
			
			// Add selected path to the list of paths we've already shown
			
			// Persist updated list to settings file
			
			// Pick a random time between the min and max intervals
			
			// Schedule this Runnable to be run again at the selected time

//			mScheduler.schedule(mRunnable, 1, TimeUnit.SECONDS);
		}
	};
	
	public void start() {
		// Pick a random time between persisted min and max intervals
		SettingsManager manager = new SettingsManager();
		long min = manager.getMinSeconds();
		long max = manager.getMaxSeconds();
		int i = mRandom.nextInt((int)(max - min) + 1);
		long seconds = i + min;
		
		String log = "Min:\t" + min + "\nMax:\t" + max + "\nRandom:\t" + seconds + "\n";
		System.out.println(log);
		
		// Schedule mRunnable to run at the selected time
		mScheduler.schedule(mRunnable, seconds, TimeUnit.SECONDS);
	}
}
