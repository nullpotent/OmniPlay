package com.omnidev.omniplay.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

public class ConnectedClient {
	
	private static final String TAG = ConnectedClient.class.getName(); 
	
	public  OutputStream 		out;
	public  InputStream			in;
	public  BluetoothDevice		device;
	public  BluetoothSocket		socket;
	
	public ConnectedClient(final BluetoothSocket socket) {
		Log.d(TAG, "Client connected...");
		this.socket = socket;
		this.device = socket.getRemoteDevice();
		initStreams();
	}
	
	private void initStreams() {
		try {
			this.out = socket.getOutputStream();
			this.in  = socket.getInputStream();
		} catch (IOException e) {
			Log.d(TAG, Log.getStackTraceString(e));
		}
	}
}
