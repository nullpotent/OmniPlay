package com.omnidev.omniplay.server;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;

import com.omnidev.omniplay.R;

public class ServerActivity extends TabActivity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_server);
		createTabs();
	}

	private void createTabs() {
		TabHost tabHost = getTabHost();
	
		//Add control tab 
		tabHost
			.addTab(tabHost
				.newTabSpec("Control")
				.setIndicator("Control", getResources().getDrawable(R.drawable.server_control_tab))
				.setContent(new Intent(this, ServerControlActivity.class)));
		
		//Add playlist tab
		tabHost
		.addTab(tabHost
			.newTabSpec("Playlist")
			.setIndicator("Playlist", getResources().getDrawable(R.drawable.server_playlist_tab))
			.setContent(new Intent(this, ServerPlaylistActivity.class)));
		
		//Add console tab
		
		tabHost
		.addTab(tabHost
			.newTabSpec("Console")
			.setIndicator("Console", getResources().getDrawable(R.drawable.server_console_tab))
			.setContent(new Intent(this, ServerPlaylistActivity.class)));		
	}
}
