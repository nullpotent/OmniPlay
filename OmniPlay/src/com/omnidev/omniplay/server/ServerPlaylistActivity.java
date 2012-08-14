package com.omnidev.omniplay.server;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.omnidev.omniplay.R;

public class ServerPlaylistActivity extends Activity {

	private static final String TAG = ServerPlaylistActivity.class.getName();

	private ArrayAdapter<String> songs;

	@Override 
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_server_playlist);
		songs = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
		((ListView) findViewById(R.id.server_playlist)).setAdapter(songs);
		((ListView) findViewById(R.id.server_playlist)).setOnItemClickListener(new ListView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> array, View arg1, int pos, long arg3) {
				/**
				 * TODO: stream the song
				 */
				try {
					String file = (String)array.getAdapter().getItem(pos);
					int id = ServerPlaylistActivity.this
								.getResources()
								.getIdentifier( file, 
												"raw", 
												ServerPlaylistActivity.this.getPackageName() );
					Log.d(TAG, "ID je: " + id);
					InputStream in = getResources().openRawResource(id);
					byte arr[] = null;
					arr = new byte[in.available()];
					in.read(arr); 
					ServerThread.getInstance().sendMP3(arr);
				} catch (IOException e) {
					Log.d(TAG, Log.getStackTraceString(e));
				}
			}			
		});
		populateSongsList();
	}

	private void populateSongsList() {
		Field[] fields=R.raw.class.getFields();
		for(int i = 0; i < fields.length; i++)
			songs.add(fields[i].getName());
	}
}
