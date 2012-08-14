package com.omnidev.omniplay.client;

import java.io.FileOutputStream;

public class AudioPlayer extends Thread {
	private static final String TAG = AudioPlayer.class.getName();

	private static final int INITIAL_BUFFER_LEN	= 10 * 96 * 100;

	private FileOutputStream outputMedia;
	
}
