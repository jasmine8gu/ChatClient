package com.chatclient.ui;

import com.chatclient.R;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class ContactActivity extends Activity {
	private static final String TAG = "jasmine ContactActivity";
	
	ChatApplication theApp;
	
	EditText etKeyword = null;
    private ContactArrayAdapter lvAdapter;
    private ListView lvContactResult;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contact);
		
        theApp = (ChatApplication)getApplication();
        theApp.addHandler(handler);
        
		theApp.searchResult.clear();

		etKeyword = (EditText)findViewById(R.id.etKeyword);
        
		lvAdapter = new ContactArrayAdapter(this, R.layout.item_contact, R.id.tvNickName, theApp.searchResult);
		lvContactResult = (ListView) findViewById(R.id.listView1);
		lvContactResult.setAdapter(lvAdapter);
		
		lvContactResult.setOnItemClickListener(new OnItemClickListener() {
        	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent i = new Intent(ContactActivity.this, SettingsActivity.class);
				Contact theContact = lvAdapter.getItem(position);
				i.putExtra("contactId", theContact.id);
				i.putExtra("profile", 2);
				//i.putExtra("contact", theContact);
				startActivity(i);
			}
		});	
    }

    protected void onDestroy() {
    	super.onDestroy();
    	theApp.deleteHandler(handler);
    }
    
	public void onSearch(View v) {
		String sKeyword = etKeyword.getText().toString();
		if (sKeyword != null && sKeyword.length() > 0) {
			Command command = new Command();
			command.cmd = Command.SEARCHCONTACT;
			command.arg1 = theApp.contactFrom.id;
			command.value = sKeyword.getBytes();
			
			Message msg = new Message();
			msg.what = ChatApplication.SEND_COMMAND;
			msg.obj = command.encode();
			theApp.sendMessage(msg);
		}
		else {
			AlertDialog.Builder builder = new Builder(ContactActivity.this);
			builder.setMessage("Keyword must be at least 2 characters long");
			builder.setTitle("Information");
			builder.setPositiveButton("OK", new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			
			builder.create().show();
		}
	}
	
    private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case ChatApplication.RECEIVE_COMMAND: {
					Command command = Command.decode((byte[])msg.obj);
					if (command.cmd == Command.SEARCHCONTACT) {
						lvAdapter.notifyDataSetChanged();
					}
					else if (command.cmd == Command.SYNCPICTURE) {
						Log.i(TAG, "notifyDataSetChanged");
						lvAdapter.notifyDataSetChanged();
					}
            		break;
				}
			}
		}
	};		
}
