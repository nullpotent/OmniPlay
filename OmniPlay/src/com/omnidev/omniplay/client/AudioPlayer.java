package com.omnidev.omniplay.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.io.FileUtils;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

public class AudioPlayer extends Thread {
	private static final String TAG = AudioPlayer.class.getName();
	
	private static final int 		MAX_BUFFER_LEN	= 5 * 96 * 1024; //5 sekundi playbacka AKO JE 96 bitrate
	private static final String 	TEMP_FILE_NAME 	= "tempFile%s";
	
	private File 				outputDirectory;
	private Queue<File>			tempFiles;
	private final Context 		ctx;
	private MediaPlayer			mediaPlayer;
	private int 				counter;
	private File				currentStreamFile;
	private FileOutputStream 	outputMedia;
	private int					streamedBytes;
	private boolean				toPlay;
	
	public AudioPlayer(final Context ctx) {
		this.ctx = ctx;
		tempFiles = new LinkedBlockingQueue<File>();		
		outputDirectory = ctx.getCacheDir();
		streamedBytes = 0;
		counter = 0;
		Log.d(TAG, "Output directory is at " + outputDirectory.getAbsolutePath());
	}
	
	private File createNextFile() {
		String filename = String.format(TEMP_FILE_NAME, counter++);
		
		Log.d(TAG, "Creating file " + filename);
		
		File retFile = null;
		
		try {
			retFile = File.createTempFile(filename, "dat", outputDirectory);
		} catch (IOException e) {
			Log.d(TAG, Log.getStackTraceString(e));
		}
		
		return retFile;
	}
	
	private void prepareNextStream() {
		currentStreamFile = createNextFile();
		tempFiles.add(currentStreamFile);
		
		toPlay = counter > 1;
		
		try {
			outputMedia = new FileOutputStream(currentStreamFile);
		} catch (FileNotFoundException e) {
			Log.d(TAG, Log.getStackTraceString(e));
		}
	}
	
	/**
	 * Glavni metod, poziva se posle dobijanja mp3 headera
	 */
	public void preparePlayer() {
		prepareNextStream();
	}

	public synchronized void fillCurrentBuffer(byte[] arr) {
		try {
			checkIfBufferFull(); 		//TODO: has side effects
			outputMedia.write(arr);
		} catch (IOException e) {
			Log.d(TAG, Log.getStackTraceString(e));
		}
		streamedBytes += arr.length;
	}
	
	public void checkIfBufferFull() {
		if(streamedBytes >= MAX_BUFFER_LEN) {
			prepareNextStream();
		}
	}
	
	public boolean canIPlay() {
		return toPlay;
	}
	
	@Override 
	public void start() {
		
	}
	
	@Override
	public void run() {
		while(true) {
			if(canIPlay()) {
				prepareMediaPlayer();
			}
		}
	}
	
	public MediaPlayer createMediaPlayer() {
		
	}
	
	public void prepareMediaPlayer() {
		FileUtils.copyFile(srcFile, destFile);
	}
	
}
