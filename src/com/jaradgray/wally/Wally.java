package com.jaradgray.wally;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Wally {
	private final WallpaperManager mWallpaperManager = new WallpaperManager();
	private Arpeggiator mArp = new Arpeggiator();
	private final Random mRandom = new Random();
	private final ScheduledExecutorService mScheduler = Executors.newScheduledThreadPool(1);
	private final Runnable mRunnable = new Runnable() {
		@Override
		public void run() {
			SettingsManager manager = new SettingsManager();
			
			// Get a random unshown path
			String[] paths = manager.getUnshownPaths();
			
			String log1 = "Unshown paths:\n";
			for (String s : paths) log1 += s + "\n";
			System.out.println(log1);
			
			// Handle no unshown paths found
			// If no unshown paths were found: don't change wallpaper,
			//	and reschedule this Runnable
			if (paths.length == 0) {
				scheduleRunnable();
				return;
			}
			
			int index = mRandom.nextInt(paths.length);
			String path = paths[index];
			
			String log2 = "\nSelected path:\n" + path;
			System.out.println(log2);
			
			// Set wallpaper to the selected path
			mWallpaperManager.setWallpaper(path);
			
			// Add selected path to the list of paths we've already shown
			manager.addShownPath(path);
			
			// Play a sound effect if sound is enabled
			if (manager.isSoundEnabled()) {
				mArp.play();
			}
			
			// Reschedule runnable
			scheduleRunnable();
		}
	};
	
	public void start() {
		scheduleRunnable();
	}
	
	
	// Private method
	
	private void scheduleRunnable() {
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
