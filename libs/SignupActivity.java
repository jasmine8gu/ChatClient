package com.chatclient.ui;

import com.chatclient.R;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;

public class SignupActivity extends Activity {

	ChatApplication theApp;
	
	EditText etAccount = null;  
	EditText etPassword = null;  
	EditText etRePassword = null;  
 
	TextView errAccount = null;  
	TextView errPassword = null;  
	TextView errRePassword = null;  
	
	ImageView ivAccount = null;
	ImageView ivPassword = null;
	ImageView ivRePassword = null;
	
	private boolean checkAccount() {
		boolean ret = false;
		String sAccount = etAccount.getText().toString();
		
    	if (sAccount == null || sAccount.length() < 1) {
    		errAccount.setText("Account can't be empty!");
    		errAccount.setVisibility(0x00000000);  
    		ivAccount.setVisibility(0x00000004); 
    	}
    	else if (sAccount.indexOf("@") < 0) {                  
    		errAccount.setText("Invalid email address!");
    		errAccount.setVisibility(0x00000000);      
    		ivAccount.setVisibility(0x00000004);
		}
    	else {
    		errAccount.setVisibility(0x00000004);
    		ivAccount.setVisibility(0x00000000);
    		ret = true;
		}	
		return ret;
	}
	
	private boolean checkPassword() {
		boolean ret = false;
		String sPassword = etPassword.getText().toString();
		
    	if (sPassword == null || sPassword.length() < 1) {
    		errPassword.setText("Password can't be empty!");
    		errPassword.setVisibility(0x00000000);  
    		ivPassword.setVisibility(0x00000004); 
    	}
    	else if (sPassword.length() < 2) {
    		errPassword.setText("Password must be at least 2 characters!");
    		errPassword.setVisibility(0x00000000);            
    		ivPassword.setVisibility(0x00000004);
    	}
    	else {
    		errPassword.setVisibility(0x00000004);   
    		ivPassword.setVisibility(0x00000000); 
    		ret = true;
    	}
		return ret;
	}
	
	private boolean checkRePassword() {
		boolean ret = false;
		String sRePassword = etRePassword.getText().toString();
		String sPassword = etPassword.getText().toString();
   		
		if (sRePassword == null || sRePassword.length() < 1) {
			errRePassword.setText("Please repeat password!");
			errRePassword.setVisibility(0x00000000);  
			ivRePassword.setVisibility(0x00000004); 
		}
		else if (!sPassword.equals(sRePassword)) {
			errRePassword.setText("Password not match!");
			errRePassword.setVisibility(0x00000000);            
			ivRePassword.setVisibility(0x00000004);
		}
		else {
			errRePassword.setVisibility(0x00000004);   
			ivRePassword.setVisibility(0x00000000); 
			ret = true;
		}
		return ret;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_signup);
		
        theApp = (ChatApplication)getApplication();
        theApp.addHandler(handler);
        
    	etAccount = (EditText)findViewById(R.id.etAccount);  
    	etPassword = (EditText)findViewById(R.id.etPassword);  
    	etRePassword = (EditText)findViewById(R.id.etRePassword);  
     
    	errAccount = (TextView)findViewById(R.id.errAccount);  
    	errPassword = (TextView)findViewById(R.id.errPassword);  
    	errRePassword = (TextView)findViewById(R.id.errRePassword);  
    	
    	ivAccount = (ImageView)findViewById(R.id.ivAccount);
    	ivPassword = (ImageView)findViewById(R.id.ivPassword);
    	ivRePassword = (ImageView)findViewById(R.id.ivRePassword);
    	 
    	ivAccount.setVisibility(0x00000004);
    	ivPassword.setVisibility(0x00000004);
    	ivRePassword.setVisibility(0x00000004);
    	
    	etAccount.setOnFocusChangeListener(new OnFocusChangeListener() {  
    		@Override  
    	    public void onFocusChange(View v, boolean hasFocus) {
    	    	if (v.hasFocus() == false) {
    	            if (v.getId() == R.id.etAccount) {
    	            	checkAccount();
    	            }
    	    	}  
    		}  
    	});
    	
    	etPassword.setOnFocusChangeListener(new OnFocusChangeListener() {  
	         @Override  
	         public void onFocusChange(View v, boolean hasFocus) {
	        	 if (v.hasFocus() == false) {   
	                if (v.getId() == R.id.etPassword) {
	                	checkPassword();
	                }
	        	 }  
	         }  
    	});  
	
		 etRePassword.setOnFocusChangeListener(new OnFocusChangeListener() {  
	         @Override  
	         public void onFocusChange(View v, boolean hasFocus) {
	        	 if (v.hasFocus() == false) {
	        		 if (v.getId() == R.id.etRePassword) {
	        			 checkRePassword();
	        		 }
	        	 }       
	         }  
		 });
	}

	public void onSignup(View v) {
		boolean valAccount = checkAccount();
		boolean valPassword = checkPassword();
		boolean valRePassword = checkRePassword();
		
		if (valAccount && valPassword && valRePassword) {
			Command command = new Command();
			command.cmd = Command.SIGNUP;
			String sValue = etAccount.getText().toString();
			sValue += " " + etPassword.getText().toString();
			command.value = sValue.getBytes();
			
			Message msg = new Message();
			msg.what = ChatApplication.SEND_COMMAND;
			msg.obj = command.encode();
			theApp.sendMessage(msg);
		}
	}
	 
    protected void onDestroy() {
    	super.onDestroy();
    	theApp.deleteHandler(handler);
    }
    
    private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case ChatApplication.RECEIVE_COMMAND: {
					Command command = Command.decode((byte[])msg.obj);
					if (command.cmd == Command.SIGNUP) {
						if (new String(command.value).equals("success")) {
							finish();
							Intent i = new Intent(SignupActivity.this, ContactListActivity.class);
							startActivity(i);
						}
						else {
							AlertDialog.Builder builder = new Builder(SignupActivity.this);
							builder.setMessage(new String(command.value));
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
			}
		}
	};	
}
