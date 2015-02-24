package com.chatclient.ui;

import com.chatclient.R;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;

public class LoginActivity extends Activity {

	ChatApplication theApp;
	private SharedPreferences settingsPrefs;
	
	EditText etAccount = null;
	EditText etPassword = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
        theApp = (ChatApplication)getApplication();
        theApp.addHandler(handler);
        
        settingsPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        
        etAccount = (EditText)findViewById(R.id.etAccount);
		etPassword = (EditText)findViewById(R.id.etPassword);
		
    	if (settingsPrefs != null) {
    		String sAccount = settingsPrefs.getString("account", "");
    		etAccount.setText(sAccount);
    		String sPassword = settingsPrefs.getString("password", "");
    		etPassword.setText(sPassword);
    	}
	}

	public void onLogin(View v) {
		Command command = new Command();
		command.cmd = Command.LOGIN;
		String sValue = etAccount.getText().toString();
		sValue += " " + etPassword.getText().toString();
		command.value = sValue.getBytes();
		
		Message msg = new Message();
		msg.what = ChatApplication.SEND_COMMAND;
		msg.obj = command.encode();
		theApp.sendMessage(msg);
		
		settingsPrefs.edit().putString("account", etAccount.getText().toString()).commit();
		settingsPrefs.edit().putString("password", etPassword.getText().toString()).commit();
	}
	
	public void onSignup(View v) {
		finish();
		Intent i = new Intent(this, SignupActivity.class);
		startActivity(i);
	}
	
    protected void onDestroy() {
    	super.onDestroy();
    	theApp.deleteHandler(handler);
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
      
	      	case R.id.action_network: {
	      		onDestroy();
	      		theApp.deActivated();
	      		finish();
	    	  
	      		Intent i = new Intent(this, NetworkActivity.class);
	      		i.putExtra("isSwitch", 1);
	      		startActivity(i);	    	  
	      		break;
	      	}
	      	case R.id.action_quit: {
	      		Message msg = new Message();
	      		msg.what = ChatApplication.APPLICATION_QUIT;
	      		theApp.sendMessage(msg);
	      		break;
	      	}
	      	default:
	      		break;
    	}

    	return true;
    }
        
    private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case ChatApplication.RECEIVE_COMMAND: {
					Command command = Command.decode((byte[])msg.obj);
					if (command.cmd == Command.LOGIN) {
						if (command.arg1 > 0) {
							finish();
							Intent i = new Intent(LoginActivity.this, ContactListActivity.class);
							startActivity(i);
						}
						else {
							AlertDialog.Builder builder = new Builder(LoginActivity.this);
							builder.setMessage("Invalid account or password!");
							builder.setTitle("Information");
							builder.setPositiveButton("OK", new OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									dialog.dismiss();
								}
							});
							
							builder.create().show();
						}
					}
            		break;
				}
				case ChatApplication.APPLICATION_QUIT: {
		      		onDestroy();
		      		theApp.deActivated();
		      		finish();
					break;
				}
			}
		}
	};
}
