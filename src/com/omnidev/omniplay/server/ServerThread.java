package com.omnidev.omniplay.server;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import com.omnidev.omniplay.shared.SharedConstants;
import com.omnidev.omniplay.shared.TMessages;
import com.omnidev.omniplay.utils.Util;

public class ServerThread extends Thread {

	private static final String	TAG = ServerThread.class.getName();

	private static ServerThread				instance = null;
	private BluetoothServerSocket 			servSocket;
	private List<WeakReference<Handler>>  	handlers;
	private Map<String, ConnectedClient> 	connectedDevices;
	private boolean							paused;

	public ServerThread() {
		this.handlers = new ArrayList<WeakReference<Handler>>();
		connectedDevices = new HashMap<String, ConnectedClient>();
		instance = this;
		Log.d(TAG, "ServerThrad initialized");
	}

	@Override
	public void start() {
		Log.d(TAG, "ServerThread starting...");
		resumeServer();
		super.start();
	}

	public static synchronized ServerThread getInstance() {
		if(instance == null) {
			Log.d(TAG, "ServerThread instance is null!!!!!");
		} 
		return instance;
	}

	public synchronized void addHandler(final Handler handler) {
		Log.d(TAG, "Added server handler");
		handlers.add(new WeakReference<Handler>(handler));
	}

	private void initServerSocket() {
		Log.d(TAG, "Initializing server socket");

		try {
			servSocket = BluetoothAdapter
					.getDefaultAdapter()
					.listenUsingRfcommWithServiceRecord("_SERVER_", 
							UUID.fromString(SharedConstants.SERVER_UUID));
		} catch (IOException e) {
			Log.d(TAG, Log.getStackTraceString(e));
		}

		if(servSocket == null) 
			Log.d(TAG, "Something went terribly wrong while " + 
					"initializing a bluetooth listening socket!");
	}

	@Override
	public void run() {
		BluetoothSocket socket = null;

		while(true) {		
			while(!paused) {
				try {
					socket = servSocket.accept();
				} catch (IOException e) {
					Log.d(TAG, "Couldn't accept the connection");
				}			
	
				if(socket != null) {
					handleSocketConnection(socket);
					try {
						if(connectedDevices.size() > Constants.MAX_ALLOWED_CLIENTS) {
							Log.d(TAG, "Number of maximum allowed clients reached.\n" + 
									"Closing the listening socket");
							servSocket.close();
							break;
						}
					} catch (IOException e) {
						Log.d(TAG, Log.getStackTraceString(e));
					}
				}
			}
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				Log.d(TAG, Log.getStackTraceString(e));
				currentThread().interrupt();
				break;
			}			
		}
	}

	public void sendString(final String msg) {
		for(ConnectedClient client : connectedDevices.values()) {
			try {
				client.out.write(TMessages.STRING_MESSAGE.getBytes());
				client.out.write(msg.getBytes());
			} catch (IOException e) {
				Log.d(TAG, Log.getStackTraceString(e));
			}
		}
	}

	public void sendMP3(final byte[] arr) throws IOException {
		for(ConnectedClient client : connectedDevices.values()) {
				client.out.write(TMessages.MP3_MESSAGE.getBytes());
				client.out.write(TMessages.MP3_HEADER.getBytes());
				client.out.write(arr);
				client.out.write(TMessages.MP3_FOOTER.getBytes());
		}
	}

	public void sendBytes(final byte[] arr) {
		for(ConnectedClient client : connectedDevices.values()) {
			try {
				client.out.write(arr);
			} catch(IOException e) {
				Log.d(TAG, Log.getStackTraceString(e));
			}
		}
	}

	private void sendMessageToHandlers(final int msgType, final String msg) {
		for(WeakReference<Handler> handler : handlers) {
			handler.get().sendMessage(Util.createHandlerMessage(msg, msgType)); 
		}
	}

	private void handleSocketConnection(final BluetoothSocket sock) {
		ConnectedClient client = new ConnectedClient(sock);

		String devAddr = client.device.getAddress();
		
		ConnectedClient tmpClient = connectedDevices.get(devAddr);
		
		int msgType = 0x00;
		
		if(tmpClient == null) {
			connectedDevices.put(devAddr, client);
			msgType = Constants.CLIENT_CONNECTED;
		} 
		else
			client = tmpClient; 
		
		
		String devName = client.device.getName();
		
		Log.d(TAG, "Connected: " + devName);
		
		sendMessageToHandlers(msgType, 
							  String.format(Constants.CLIENT_CONNECTED_STRING,
									  		devName));
	}

	public void closeConnections() {
		Log.d(TAG, "Closing connections....");
		for(ConnectedClient client : connectedDevices.values()) {
			try {
				client.socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void pauseServer() {
		paused = true;
		try {
			servSocket.close();
			closeConnections();
		} catch(IOException e) {
			Log.d(TAG, Log.getStackTraceString(e));
		}
	}
	
	public void resumeServer() {
		initServerSocket();
		paused = false;
		Log.d(TAG, "Server started listenning...");
	}
	
	public boolean isPaused() {
		return paused;
	}
}
