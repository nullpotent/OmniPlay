package com.omnidev.omniplay.client;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.omnidev.omniplay.R;
import com.omnidev.omniplay.shared.SharedConstants;
import com.omnidev.omniplay.shared.TMessages;

public class ClientActivity extends Activity {

	private static final String TAG = ClientActivity.class.getName();

	private Set<String>						devicesSet;
	private ArrayAdapter<ConnectionWrapper>	discoveredDevices;
	private EditText						console;
	private BluetoothAdapter				btAdapter;
	private ClientThread					clientThread;
	private Set<String>						msgConstantsSet;
	private AudioPlayer						audioPlayerThread;
	
	private Handler msgHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {			
			String msgRcvd 		= new String((byte[])msg.obj);

			if(msgRcvd.contains(TMessages.MP3_HEADER)) {
//				audioPlayerThread.openMediaBuffer();
//				audioPlayerThread.startStreaming();
				console.append(msgRcvd + "\n"); 
			} else if(msgRcvd.contains(TMessages.MP3_FOOTER)) {
//				audioPlayerThread.closeMediaBuffer();
				console.append(msgRcvd + "\n");
			} else if(msgRcvd.contains(TMessages.MP3_MESSAGE)) {
//				audioPlayerThread = new AudioPlayer(ClientActivity.this);
			} else if(msgRcvd.contains(TMessages.STRING_MESSAGE)) {
				console.append(msgRcvd + "\n");
			} else if(msgRcvd.length() > 0 /** TODO: */) {
//				audioPlayerThread.fillBuffer((byte[])msg.obj);
			}
			super.handleMessage(null);
		}
	};

	/**
	 * Broadcast receiver - registers all discovered devices
	 */
	private final BroadcastReceiver	discoveryService = new BroadcastReceiver () {	
		private static final String TAG = "BroadcastReceiver.Discovery";
		@Override
		public void onReceive(Context context, Intent intent) {
			String recvIntent = intent.getAction();
			Log.d(TAG, recvIntent);

			if(intent.getAction().equals(Constants.DEVICE_FOUND)) {
				BluetoothDevice dev = (BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");

				if(dev == null) 
					return;

				if(!dev.getName().equals(Constants.ADMIN_TAG)) 
					return;

				if(devicesSet.contains(dev.getAddress()))
					return;
				else 
					devicesSet.add(dev.getAddress());		

				discoveredDevices.add(new ConnectionWrapper(dev.getName(), dev.getAddress()));
			}
		}
	};


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_client);

		console = (EditText) findViewById(R.id.consoleLogClient);
		console.setEnabled(false);
		Button scanButton = (Button) findViewById(R.id.scanButton);
		scanButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View arg0) {
				scanDevices();
			}			
		});

		msgConstantsSet = new HashSet<String>();
		fillMsgConstSet();
	}

	private void fillMsgConstSet() {
		Field[] fields = TMessages.class.getFields();
		for(Field field : fields) {
			Log.d(TAG, field.getName());
			msgConstantsSet.add(field.getName());
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		IntentFilter iff = new IntentFilter(Constants.DEVICE_FOUND);
		this.registerReceiver(discoveryService, iff);	

		devicesSet = new HashSet<String>();

		discoveredDevices = new ArrayAdapter<ConnectionWrapper>(this, android.R.layout.simple_list_item_1);

		((ListView) findViewById(R.id.listServers)).setAdapter(discoveredDevices);
		((ListView) findViewById(R.id.listServers)).setOnItemClickListener(new ListView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> array, View arg1, int pos, long arg3) {
				ConnectionWrapper conn = (ConnectionWrapper) array.getAdapter().getItem(pos);
				/**
				 * TODO: start the thread
				 */
				BluetoothAdapter.getDefaultAdapter().cancelDiscovery();

				clientThread = new ClientThread(conn.getAddress());
				clientThread.addHandler(msgHandler);
				clientThread.start();
			}			
		});
	}

	@Override
	public void onDestroy() {
		this.unregisterReceiver(discoveryService);
		btAdapter.disable();
		super.onDestroy();
	}

	private void scanDevices() {
		setupBluetoothAdapter();

		discoveredDevices.clear();
		devicesSet.clear();

		if(btAdapter.isDiscovering())
			btAdapter.cancelDiscovery();

		new Handler().post(new Runnable() {
			public void run() {
				if(btAdapter.getState() == BluetoothAdapter.STATE_TURNING_ON || 
						!btAdapter.isEnabled()) {
					Toast.makeText(ClientActivity.this, "Bluetooth radio is turning on...", Toast.LENGTH_SHORT).show();
					return;
				}
				else {
					btAdapter.startDiscovery();
				}
			}  
		});
	}

	public void setupBluetoothAdapter() {
		btAdapter = BluetoothAdapter.getDefaultAdapter();

		if(btAdapter == null) {
			Toast.makeText(ClientActivity.this, "Bluetooth adapter is not working", Toast.LENGTH_LONG).show();
			return;
		} else if(btAdapter.isEnabled())
			return;

		btAdapter.enable();
	}

}
