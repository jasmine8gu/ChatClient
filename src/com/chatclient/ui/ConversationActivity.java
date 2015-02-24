package com.chatclient.ui;

import com.chatclient.R;
import com.chatclient.net.NetController;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

public class ConversationActivity extends Activity {
	ChatApplication theApp;
	Contact contactTo;
	
	private ConversationArrayAdapter lvAdapter;
    private ListView lvConversationList;
    
    private EditText etText;
    
    private static final int PHOTO_REQUEST_GALLERY = 1;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_conversation);
		
        theApp = (ChatApplication)getApplication();
        theApp.addHandler(handler);
        
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
		    int contactId = extras.getInt("contactId");
		    contactTo = theApp.contactList.get(contactId);
		}
		
		getActionBar().setTitle(contactTo.nickName);
		
        etText = (EditText)findViewById(R.id.editText1);
		lvAdapter = new ConversationArrayAdapter(this, R.layout.item_conversation_right, R.id.tvNickName, contactTo.conversationReadList);
        lvConversationList = (ListView) findViewById(R.id.listView1);
        lvConversationList.setAdapter(lvAdapter);
	}
	
    protected void onDestroy() {
    	super.onDestroy();
    	theApp.deleteHandler(handler);
    }
    
    protected void onStart() {
    	super.onStart();
    	if (!contactTo.conversationUnReadList.isEmpty()) {
			contactTo.conversationReadList.addAll(contactTo.conversationUnReadList);
			contactTo.conversationUnReadList.clear();
			lvAdapter.notifyDataSetChanged();
    	}
    }
    
    protected void onStop() {
    	super.onStop();
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.conversation, menu);
		return true;
	}
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    	/*
	      	case R.id.action_sendfile: {
	      		Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
	      		startActivityForResult(i, PHOTO_REQUEST_GALLERY);
				break;
	      	}
	    */  	
	      	case R.id.action_viewprofile: {
	      		if (NetController.network == NetController.NETWORK_INET) {
		      		Intent i = new Intent(this, SettingsActivity.class);
					i.putExtra("contactId", contactTo.id);
					i.putExtra("profile", 1);
					startActivity(i);
		      		finish();
	      		}
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
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
        	return;
        }
     
        if (requestCode == PHOTO_REQUEST_GALLERY) {
            try {
                Uri selectedImage = data.getData();
                Intent sendIntent = new Intent();
                sendIntent.setType("text/plain");
				sendIntent.setAction(Intent.ACTION_SEND);
				sendIntent.putExtra(Intent.EXTRA_STREAM, selectedImage);
				startActivity(Intent.createChooser(sendIntent, "Share file via:"));
            }
    	    catch(Exception e) {
    	    	System.out.println(e.toString());
    	    }
        }
        
        super.onActivityResult(requestCode, resultCode, data);
    }
    
    public void onSend(View v) {
		String sText = etText.getText().toString();
		if (sText.length() > 0) {
			Conversation conversation = new Conversation();
			conversation.isHost = true;
			conversation.text = sText;
			conversation.contactFrom = theApp.contactFrom;
			contactTo.conversationReadList.add(conversation);
			lvAdapter.notifyDataSetChanged();
			
			Command command = new Command(Command.CONVERSATION, theApp.contactFrom.id, contactTo.id, sText.getBytes());
			
			Message msg = new Message();
			msg.what = ChatApplication.SEND_COMMAND;
			msg.obj = command.encode();
			
			theApp.sendMessage(msg);
			
			etText.setText(null);
		}
	}
	
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case ChatApplication.RECEIVE_COMMAND: {
					Command command = Command.decode((byte[])msg.obj);
					if (command.cmd == Command.CONVERSATION) {
						contactTo.conversationReadList.addAll(contactTo.conversationUnReadList);
						contactTo.conversationUnReadList.clear();
						lvAdapter.notifyDataSetChanged();
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
