package com.chatclient.ui;

import com.chatclient.R;
import com.chatclient.net.NetController;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class NetworkActivity extends Activity {
	ChatApplication theApp;
	private SharedPreferences settingsPrefs;
	boolean isSwitch = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_network);
		
        theApp = (ChatApplication)getApplication();
        theApp.addHandler(handler);
        
		settingsPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			
		Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.getInt("isSwitch") ==  1) {
        	isSwitch = true;
        }	
	}

    protected void onDestroy() {
    	super.onDestroy();
    	theApp.deleteHandler(handler);
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.network, menu);
		return true;
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
      
	      	case R.id.action_quit: {
	      		onDestroy();
	      		finish();
	      		break;
	      	}
	      	default:
	      		break;
    	}

    	return true;
    }
    
    public void onStart() {
		super.onStart();
		
		if (isSwitch == false) {
	    	if (settingsPrefs != null) {
	    		int network = settingsPrefs.getInt("network", -1);
	    		//network = NetController.NETWORK_INET;
	    		switch(network) {
		    		case NetController.NETWORK_BT: {
		    			doBt();
		    			break;
		    		}
	    		
		    		case NetController.NETWORK_WIFI: {
		    			doWifi();
		    			break;
		    		}
		    		
		    		case NetController.NETWORK_INET: {
		    			doInet();
		    			break;
		    		}
	    		}
	    	}		
		}
	}
	
	public void doBt() {
		settingsPrefs.edit().putInt("network", NetController.NETWORK_BT).commit();
		
		if (ChatApplication.activate == false) {
			NetController.network = NetController.NETWORK_BT;
			theApp.activate();
		}
		else if (NetController.network != NetController.NETWORK_BT) {
			theApp.deActivated();
			
			NetController.network = NetController.NETWORK_BT;
			theApp.activate();
		}
		
		finish();
		Intent i = new Intent(this, ContactListActivity.class);
		startActivity(i);
	}
	
	public void doWifi() {
		settingsPrefs.edit().putInt("network", NetController.NETWORK_WIFI).commit();
		
		if (ChatApplication.activate == false) {
			NetController.network = NetController.NETWORK_WIFI;
			theApp.activate();
		}
		else if (NetController.network != NetController.NETWORK_WIFI) {
			theApp.deActivated();
			
			NetController.network = NetController.NETWORK_WIFI;
			theApp.activate();
		}
		
		finish();
		Intent i = new Intent(this, ContactListActivity.class);
		startActivity(i);
	}
	
	public void doInet() {
		settingsPrefs.edit().putInt("network", NetController.NETWORK_INET).commit();

		if (ChatApplication.activate == false) {
			NetController.network = NetController.NETWORK_INET;
			theApp.activate();
		}
		else if (NetController.network != NetController.NETWORK_INET) {
			theApp.deActivated();
			
			NetController.network = NetController.NETWORK_INET;
			theApp.activate();
		}
		else {
			if (theApp.isConnected == true) {
				if (theApp.isLogin == true) {
					finish();
					Intent i = new Intent(this, ContactListActivity.class);
					startActivity(i);
				}
				else {
					finish();
					Intent i = new Intent(this, LoginActivity.class);
					startActivity(i);
				}
			}
			else {
				theApp.deActivated();
				theApp.activate();
			}
		}
	}
	
	public void onBt(View v) {
		doBt();
	}
	
	public void onWifi(View v) {
		doWifi();
	}
	
	public void onInet(View v) {
		doInet();
	}
    	
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case ChatApplication.SERVER_CONNECTED: {
					theApp.isConnected = true;
					finish();
					Intent i = new Intent(NetworkActivity.this, LoginActivity.class);
					startActivity(i);
            		break;
				}
				
				case ChatApplication.SERVER_CONNECTFAIL: {
					theApp.deActivated();
					
					AlertDialog.Builder builder = new Builder(NetworkActivity.this);
					builder.setMessage("Server is busy now! Please try it later or you can search bluetooth or wifi to see any friends online!");
					builder.setTitle("Information");
					builder.setPositiveButton("OK", new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
					
					builder.create().show();
			        break;
				}
				
				case ChatApplication.SERVER_DISCONNECTED: {
					theApp.deActivated();
					
					AlertDialog.Builder builder = new Builder(NetworkActivity.this);
					builder.setMessage("Server is busy now! Please try it later or you can search bluetooth or wifi to see any friends online!");
					builder.setTitle("Information");
					builder.setPositiveButton("OK", new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
					
					builder.create().show();
			        break;
				}				
			}
		}
	};	
}
