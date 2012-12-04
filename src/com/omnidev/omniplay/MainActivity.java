package com.omnidev.omniplay;

import com.omnidev.omniplay.client.ClientActivity;
import com.omnidev.omniplay.server.ServerActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;

public class MainActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        chooseTheRole();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    /**
     * Choose between server/client
     */
    private void chooseTheRole() {
    	AlertDialog ad = new AlertDialog.Builder(this).create();  
    	ad.setCancelable(false);
    	ad.setTitle("Hey you");
    	ad.setMessage("Choose the role");  
    	ad.setButton(AlertDialog.BUTTON1, "Client", new AlertDialog.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				startActivity(new Intent(MainActivity.this, ClientActivity.class));
				MainActivity.this.finish();
			}
    		
    	});
    	ad.setButton(AlertDialog.BUTTON2, "Server", new AlertDialog.OnClickListener() {
    		
			@Override
			public void onClick(DialogInterface dialog, int which) {
				startActivity(new Intent(MainActivity.this, ServerActivity.class));
				MainActivity.this.finish();
			}
    		
    	});
    	ad.show(); 
    }
}
