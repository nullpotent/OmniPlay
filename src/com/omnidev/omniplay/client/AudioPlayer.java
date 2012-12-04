package com.omnidev.omniplay.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

public class AudioPlayer extends Thread {
	private static final String TAG = AudioPlayer.class.getName();

	private static final int INITIAL_BUFFER_LEN	= 10 * 96 * 100;

	private FileOutputStream outputMedia;
	private File 		 	downloadingMediaFile;
	private int 		 	totalBytesRead; 
	private boolean		 	outputClosed;
	private MediaPlayer 	mediaPlayer;
	private int				fileCounter;
	private final Context	ctx;
	private String filepath;
	
	public AudioPlayer(final Context ctx) {
		this.ctx = ctx;
		filepath = ctx.getFilesDir().getPath().toString();
		outputMedia = null;
		outputClosed = true;
	}

	public void startStreaming() {
		if(outputMedia == null) {
			Log.d(TAG, "Output media is null");
		}
		else {
			start();
		}
	}

	public synchronized boolean isBuffering() {
		return !outputClosed;
	}
	
	public void openMediaBuffer() {
		totalBytesRead 	= 0;
		fileCounter 	= 0;
		outputClosed 	= false;
		try {
			downloadingMediaFile = new File(filepath + "/tempFile.dat");
			outputMedia = new FileOutputStream(downloadingMediaFile);
		} catch (IOException e) {
			Log.d(TAG, Log.getStackTraceString(e));
		}
	}

	public synchronized void fillBuffer(byte[] arr) {
		try {
			totalBytesRead += arr.length;
			outputMedia.write(arr);
		} catch (IOException e) {
			Log.d(TAG, Log.getStackTraceString(e));
		}
	}

	public void closeMediaBuffer() {
		try {
			outputClosed = true;
			outputMedia.close();
		} catch (IOException e) {
			Log.d(TAG, Log.getStackTraceString(e));
		}
	}

	@Override
	public void run() {
		while(true) {
			if (mediaPlayer == null) {
				//  Only create the MediaPlayer once we have the minimum buffered data
				if ( totalBytesRead >= INITIAL_BUFFER_LEN) {
					try {
						startMediaPlayer();
					} catch (Exception e) {
						Log.e(getClass().getName(), "Error copying buffered conent.", e);    			
					}
				} 
			} else if ( mediaPlayer.getDuration() - mediaPlayer.getCurrentPosition() <= 10000 ){ 
				//  NOTE:  The media player has stopped at the end so transfer any existing buffered data
				//  We test for < 1second of data because the media player can stop when there is still
				//  a few milliseconds of data left to play
				transferBufferToMediaPlayer();
			}
		}
	}

    private void transferBufferToMediaPlayer() {
	    try {
	    	// First determine if we need to restart the player after transferring data...e.g. perhaps the user pressed pause
	    	boolean wasPlaying = mediaPlayer.isPlaying();
	    	int curPosition = mediaPlayer.getCurrentPosition();
	    	
	    	// Copy the currently downloaded content to a new buffered File.  Store the old File for deleting later. 
	    	File oldBufferedFile = new File(filepath + "/tempFile" + fileCounter + ".dat");
	    	File bufferedFile = new File(filepath + "/tempFile" + (fileCounter++) + ".dat");

	    	//  This may be the last buffered File so ask that it be delete on exit.  If it's already deleted, then this won't mean anything.  If you want to 
	    	// keep and track fully downloaded files for later use, write caching code and please send me a copy.
	    	bufferedFile.deleteOnExit();   
	    	moveFile(downloadingMediaFile, bufferedFile);

	    	// Pause the current player now as we are about to create and start a new one.  So far (Android v1.5),
	    	// this always happens so quickly that the user never realized we've stopped the player and started a new one
	    	mediaPlayer.pause();

	    	// Create a new MediaPlayer rather than try to re-prepare the prior one.
        	mediaPlayer = createMediaPlayer(bufferedFile);
    		mediaPlayer.seekTo(curPosition);
    		
    		//  Restart if at end of prior buffered content or mediaPlayer was previously playing.  
    		//	NOTE:  We test for < 1second of data because the media player can stop when there is still
        	//  a few milliseconds of data left to play
    		boolean atEndOfFile = mediaPlayer.getDuration() - mediaPlayer.getCurrentPosition() <= 1000;
        	if (wasPlaying || atEndOfFile){
        		mediaPlayer.start();
        	}

	    	// Lastly delete the previously playing buffered File as it's no longer needed.
	    	oldBufferedFile.delete();
	    	
	    }catch (Exception e) {
	    	Log.e(getClass().getName(), "Error updating to newly loaded content.", e);            		
		}
    }

	public void startMediaPlayer() {
		try {   
			File bufferedFile = new File(filepath + "/tempFile" + (fileCounter++) + ".dat");
			Log.d(TAG, "tempFile" + (fileCounter) + ".dat");
			// We double buffer the data to avoid potential read/write errors that could happen if the 
			// download thread attempted to write at the same time the MediaPlayer was trying to read.
			// For example, we can't guarantee that the MediaPlayer won't open a file for playing and leave it locked while 
			// the media is playing.  This would permanently deadlock the file download.  To avoid such a deadloack, 
			// we move the currently loaded data to a temporary buffer file that we start playing while the remaining 
			// data downloads.  
			moveFile(downloadingMediaFile, bufferedFile);

			Log.e(getClass().getName(),"Buffered File path: " + bufferedFile.getAbsolutePath());
			Log.e(getClass().getName(),"Buffered File length: " + bufferedFile.length()+"");

			mediaPlayer = createMediaPlayer(bufferedFile);

			// We have pre-loaded enough content and started the MediaPlayer so update the buttons & progress meters.
			mediaPlayer.start();    	
		} catch (IOException e) {
			Log.e(getClass().getName(), "Error initializing the MediaPlayer.", e);
			return;
		}   
	}

	private MediaPlayer createMediaPlayer(File mediaFile) throws IOException {
		MediaPlayer mPlayer = new MediaPlayer();
		mPlayer.setOnErrorListener(
				new MediaPlayer.OnErrorListener() {
					public boolean onError(MediaPlayer mp, int what, int extra) {
						Log.e(getClass().getName(), "Error in MediaPlayer: (" + what +") with extra (" +extra +")" );
						return false;
					}
				});

		//  It appears that for security/permission reasons, it is better to pass a FileDescriptor rather than a direct path to the File.
		//  Also I have seen errors such as "PVMFErrNotSupported" and "Prepare failed.: status=0x1" if a file path String is passed to
		//  setDataSource().  So unless otherwise noted, we use a FileDescriptor here.
		FileInputStream fis = new FileInputStream(mediaFile);
		mPlayer.setDataSource(fis.getFD());
		mPlayer.prepare();
		return mPlayer;
	}

	public void moveFile(File oldLocation, File	newLocation) throws IOException {
		if (oldLocation.exists()) {
			BufferedInputStream  reader = new BufferedInputStream( new FileInputStream(oldLocation) );
			BufferedOutputStream  writer = new BufferedOutputStream( new FileOutputStream(newLocation, false));
			try {
				byte[]  buff = new byte[8192];
				int numChars;
				while ( (numChars = reader.read(  buff, 0, buff.length ) ) != -1) {
					writer.write( buff, 0, numChars );
				}
			} catch( IOException ex ) {
				throw new IOException("IOException when transferring " + oldLocation.getPath() + " to " + newLocation.getPath());
			} finally {
				try {
					if ( reader != null ){                    	
						writer.close();
						reader.close();
					}
				} catch( IOException ex ){
					Log.e(getClass().getName(),"Error closing files when transferring " + oldLocation.getPath() + " to " + newLocation.getPath() ); 
				}
			}
		} else {
			throw new IOException("Old location does not exist when transferring " + oldLocation.getPath() + " to " + newLocation.getPath() );
		}
	}	
}
