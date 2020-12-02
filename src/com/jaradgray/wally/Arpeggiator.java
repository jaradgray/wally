package com.jaradgray.wally;

import java.net.URL;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

/**
 * The Arpeggiator class facilitates playing a random sequence of
 * audio clips.
 * 
 * When play() is called, a Runnable is scheduled to play a random
 * audio clip, and reschedules itself until a randomly selected
 * number of notes have been played.
 *  
 * @author Jarad
 *
 */
public class Arpeggiator {
	// Constants
	private static final int MAX_NOTES = 9;
	private static final int MIN_NOTES = 5;
	private static final long MILLIS_PER_NOTE = 50;
	
	// Instance variables
	private final Random mRandy = new Random();
	private int mNumNotes; // The number of notes to play
	private int mNotesCount = 0; // The number of notes we've already played
	private URL[] mUrls; // The array of URLs of audio resources
	private final ScheduledExecutorService mScheduler = Executors.newScheduledThreadPool(1);
	private final Runnable mRunnable = new Runnable() {
		@Override
		public void run() {
			// Get a random index for a sound
			int index = mRandy.nextInt(mUrls.length);
			try {
				// From lots of "how to play sound in Java" answers
				AudioInputStream ais = AudioSystem.getAudioInputStream(mUrls[index]);
				Clip clip = AudioSystem.getClip();
				clip.open(ais);
				clip.start();
			} catch (Exception e) {
				e.printStackTrace();
			}

			// Reschedule this Runnable until we've played the selected number of notes
			mNotesCount++;
			if (mNotesCount < mNumNotes) {
				mScheduler.schedule(mRunnable, MILLIS_PER_NOTE, TimeUnit.MILLISECONDS);
			} else {
				// Reset this Arpeggiator so play() can be called again
				mNotesCount = 0;
			}
		}
	};
	
	
	// Public methods
	
	public void play() {
		// Build the array of URLs of audio resources
		mUrls = new URL[8];
		mUrls[0] = Application.class.getResource("/res/audio/0-G#4.wav");
		mUrls[1] = Application.class.getResource("/res/audio/1-C5.wav");
		mUrls[2] = Application.class.getResource("/res/audio/2-D#5.wav");
		mUrls[3] = Application.class.getResource("/res/audio/3-G5.wav");
		mUrls[4] = Application.class.getResource("/res/audio/4-G#5.wav");
		mUrls[5] = Application.class.getResource("/res/audio/5-C6.wav");
		mUrls[6] = Application.class.getResource("/res/audio/6-D#6.wav");
		mUrls[7] = Application.class.getResource("/res/audio/7-G6.wav");
		
		// Pick a random number of notes between MIN_NOTES (inclusive) and MAX_NOTES (inclusive)
		mNumNotes = mRandy.nextInt(MAX_NOTES - MIN_NOTES + 1) + MIN_NOTES;
		System.out.println("mNumNotes: " + mNumNotes);
		
		// Run mRunnable immediately
		mScheduler.schedule(mRunnable, 0, TimeUnit.MILLISECONDS);
	}
}
