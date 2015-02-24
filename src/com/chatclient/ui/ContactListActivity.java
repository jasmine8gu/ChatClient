package com.chatclient.ui;

import java.util.Iterator;
import java.util.Map.Entry;

import com.chatclient.R;
import com.chatclient.net.NetController;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

public class ContactListActivity extends Activity {
	ChatApplication theApp;
	
    private ContactArrayAdapter lvAdapter;
    private TextView tvInfo;
    private ListView lvContactList;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contact_list);
		
        theApp = (ChatApplication)getApplication();
        theApp.addHandler(handler);

        getActionBar().setTitle(theApp.contactFrom.nickName);
        
		lvAdapter = new ContactArrayAdapter(this, R.layout.item_contact, R.id.tvNickName, theApp.contactList);
        lvContactList = (ListView) findViewById(R.id.listView1);
        lvContactList.setAdapter(lvAdapter);
        
        lvContactList.setOnItemClickListener(new OnItemClickListener() {
        	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        		int items = parent.getCount();
			    for (int i = 0; i < items; i++) {
			    	View v = parent.getChildAt(i);
			    	if (position == i) {
			    		v.setBackgroundColor(Color.rgb(153, 204, 255));
			    	} 
			    	else {
		                 v.setBackgroundColor(Color.TRANSPARENT);
			    	}
		        }
		           
				Intent i = new Intent(ContactListActivity.this, ConversationActivity.class);
				Contact theContact = lvAdapter.getItem(position);
				i.putExtra("contactId", theContact.id);
				startActivity(i);
			}
		});	        
        tvInfo = (TextView)findViewById(R.id.tvInfo);
    	tvInfo.setVisibility(View.GONE);
    }

    protected void onStart() {
    	super.onStart();

    	lvAdapter.notifyDataSetChanged();
		theApp.netController.startDiscovery();
    }
    
    protected void onStop() {
    	super.onStop();
    	theApp.netController.stopDiscovery();
    }

    protected void onResume() {
    	super.onResume();
    	
    	if (NetController.network == NetController.NETWORK_INET) {
	        if (theApp.contactList.size() == 0) {
	        	tvInfo.setVisibility(View.VISIBLE);
	        }
	        else {
	        	tvInfo.setVisibility(View.GONE);
	        }
    	}
    }

    protected void onDestroy() {
    	super.onDestroy();
    	theApp.deleteHandler(handler);
    	
    	Iterator<Entry<Integer, Contact>> it = theApp.contactList.entrySet().iterator();
    	while(it.hasNext()) {
    		Entry<Integer, Contact> entry = it.next();
    		Contact contact = entry.getValue();
    		contact.conversationReadList.clear();
    	}
    }
        
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.contact_list, menu);
		return true;
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
      
	      	case R.id.action_contact: {
	      		if (NetController.network == NetController.NETWORK_INET) {
		      		Intent i = new Intent(this, ContactActivity.class);
		      		startActivity(i);
	      		}
	      		break;
	      	}
	      
	      	case R.id.action_myprofile: {
	      		if (NetController.network == NetController.NETWORK_INET) {
		      		Intent i = new Intent(this, SettingsActivity.class);
					i.putExtra("contactId", theApp.contactFrom.id);
					i.putExtra("profile", 0);
					startActivity(i);
	      		}
				break;
	      	}
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
				case ChatApplication.ADD_CONTACT: {
	            	lvAdapter.notifyDataSetChanged();
		        	break;
				}
				
				case ChatApplication.CONTACT_CONNECTED: {
					lvAdapter.notifyDataSetChanged();
            		break;
				}
				
				case ChatApplication.CONTACT_DISCONNECTED: {
					lvAdapter.notifyDataSetChanged();
			        break;
				}
				
				case ChatApplication.CONTACT_CONNECTFAIL: {
			        break;
				}
				
				case ChatApplication.RECEIVE_COMMAND: {
					//TODO
					//refine here
			        tvInfo = (TextView)findViewById(R.id.tvInfo);
			        if (theApp.contactList.size() == 0) {
			        	tvInfo.setVisibility(View.VISIBLE);
			        }
			        else {
			        	tvInfo.setVisibility(View.GONE);
			        }
			        
					lvAdapter.notifyDataSetChanged();
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
