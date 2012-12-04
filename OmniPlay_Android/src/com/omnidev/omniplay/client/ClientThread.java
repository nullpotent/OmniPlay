package com.omnidev.omniplay.client;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.omnidev.omniplay.shared.SharedConstants;
import com.omnidev.omniplay.utils.Util;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

public class ClientThread extends Thread {
	private static final String TAG = ClientThread.class.getName();
	
	private InputStream 					in;
	private BluetoothSocket 				socket;
	private List<WeakReference<Handler>> 	handlers;
	private BluetoothDevice					btDevice;
	private boolean 						streamOpen;
	
	public ClientThread(final String address) {
		btDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
		
		try {
			socket = btDevice.createRfcommSocketToServiceRecord(UUID.fromString(SharedConstants.SERVER_UUID));
		} catch (IOException e) {
			Log.d(TAG, Log.getStackTraceString(e));
			return;
		}
		
		this.handlers = new ArrayList<WeakReference<Handler>>();
	}
	
	private boolean connectToServer() {
		try {
			socket.connect();
			in = socket.getInputStream();
			return true;
		} catch (IOException e) {
			Log.d(TAG, Log.getStackTraceString(e));
			return false;
		}
	}
	
	public synchronized void addHandler(final Handler handler) {
		Log.d(TAG, "Added client handler");
		handlers.add(new WeakReference<Handler>(handler));
	}
	
	@Override
	public void run() {
		int 	byteRet 	= -1;
		streamOpen 			= connectToServer(); 
		
		while(streamOpen) {
			try {				
				byte bytes[] = new byte[in.available()];
				
				byteRet = in.read(bytes);

				if(byteRet > 0) {
					sendMessageToHandlers(bytes);
				}				
			} catch (IOException e) {
				Log.d(TAG, "Stream is closed...");
				streamOpen = false;
			} 		
		}
	}
		
	private void sendMessageToHandlers(final Object msg) {
		for(WeakReference<Handler> handler : handlers) {
			handler.get().sendMessage(Util.createHandlerMessage(msg, Constants.SIMPLE_MESSAGE)); 
		}
	}
}
