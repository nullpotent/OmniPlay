package com.omnidev.omniplay.server;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.omnidev.omniplay.R;
import com.omnidev.omniplay.client.ConnectionWrapper;
import com.omnidev.omniplay.shared.SharedConstants;


public class ServerControlActivity extends Activity {

	private ToggleButton 			onOffServer;
	private Handler 				timerHandler;
	private TextView 				timeLeft;
	private int						timeLeftSeconds;
	private ServerThread			serverThread;
	private ArrayAdapter<String>	connectedClients;
	
	private Handler msgHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			String msgRcvd = (String)msg.obj;
			switch(msg.what) {
			case Constants.CLIENT_CONNECTED: {
				connectedClients.add(msgRcvd);
				break;
			}
			}
		}
	};

	private Runnable updateTime = new Runnable() {
		public void run() {
			timeLeftSeconds--;

			if(timeLeftSeconds > 0)
				timerHandler.postDelayed(this, 1000); 
			else 
				timeLeftSeconds = 0;

			timeLeft.setText(String.valueOf(timeLeftSeconds) + " seconds");
		}
	};

	private ToggleButton.OnClickListener onOffServerListener = new ToggleButton.OnClickListener() {
		public void onClick(View button) {
			connectedClients.clear();
			if(onOffServer.isChecked()) {
				enableBluetoothDiscovery();
			}
			else {
				serverThread.pauseServer();
				timeLeftSeconds = 0;
				timeLeft.setText("0 seconds");
				timerHandler.removeCallbacks(updateTime);
			}
		}
	};

	@Override
	public void onDestroy() {
		final BluetoothAdapter bAdapter = BluetoothAdapter.getDefaultAdapter();
		if(bAdapter != null) 
			bAdapter.disable();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_server_control);

		timerHandler 		= new Handler();
		timeLeft 			= (TextView) findViewById(R.id.timeLeft);
		onOffServer 		= (ToggleButton) findViewById(R.id.onOffServer);		
		connectedClients 	= new ArrayAdapter<String>(this, 
														android.R.layout.simple_list_item_1);
		
		((ListView) findViewById(R.id.connected_clients)).setAdapter(connectedClients);
		
		timeLeftSeconds = 0;
		
		onOffServer.setOnClickListener(onOffServerListener);
		
		serverThread = new ServerThread();
		
		serverThread.addHandler(msgHandler);
	}

	private void enableBluetoothDiscovery() {
		Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, Constants.BLUETOOTH_DISCOVERY_TIME);
		startActivityForResult(discoverableIntent, Constants.BLUETOOTH_ENABLE_DISCOVERY);
	} 

	@Override
	protected void onActivityResult(int requestCode, int resCode, Intent data) {
		if(requestCode == Constants.BLUETOOTH_ENABLE_DISCOVERY) {
			if(resCode <= 0) {
				Toast.makeText(ServerControlActivity.this, "Oh okay...", Toast.LENGTH_SHORT).show();
				return;
			}
			
			timerHandler.removeCallbacks(updateTime);
			timeLeftSeconds = resCode;
			timerHandler.post(updateTime);
			
			/**
			 * TODO: Smesti logiku u thread
			 */
			if(serverThread.isPaused()) 
				serverThread.resumeServer();
			else
				serverThread.start();
		}
	}
}
