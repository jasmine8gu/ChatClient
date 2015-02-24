package com.chatclient.ui;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.chatclient.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ContactArrayAdapter extends ArrayAdapter<Contact> {
	private ConcurrentHashMap<Integer, Contact> contactList;
    private Context context;
    
    Bitmap bmpProfile = null;
    
    private ImageView ivStatus;
    private TextView tvNickName;
    
    public ContactArrayAdapter(Context c, int resource, int textViewResourceId, ConcurrentHashMap<Integer, Contact> hashMap) {
    	super(c, resource, textViewResourceId);
    	context = c;
    	
    	bmpProfile = BitmapFactory.decodeResource(context.getResources(), R.drawable.profile);
    	contactList = hashMap;
    }
    
    public int getCount() {
    	if (contactList == null) {
    		return 0;
    	}

		return contactList.size();
    }     
    
    public Contact getItem(int index) {
    	if (contactList == null) {
    		return null;
    	}
    	
    	int idx = 0;
        Iterator<Entry<Integer, Contact>> it = contactList.entrySet().iterator();
    	while(it.hasNext()) {
    		Entry<Integer, Contact> entry = it.next();
    		if (idx == index) {
    			return entry.getValue();
    		}
    		idx++;
    	}   

    	return null;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (rowView == null) {
	        rowView = inflater.inflate(R.layout.item_contact, parent, false);
        }
        
        Contact contact = getItem(position);
        if (contact != null) {
	        tvNickName = (TextView) rowView.findViewById(R.id.tvNickName);
	        ivStatus = (ImageView)rowView.findViewById(R.id.ivStatus);
	        
	    	if (contact.bmpPicture == null) {
	    		ivStatus.setImageBitmap(bmpProfile);
	    	}
	    	else {
	    		ivStatus.setImageBitmap(contact.bmpPicture);
	    	}
	    	
	    	String title = contact.nickName;
	    	if (title == null || title.length() == 0) {
	    		title = contact.account;
	    	}
	    	if (contact.conversationUnReadList.isEmpty() == false) {
	    		title = title + " (" + contact.conversationUnReadList.size() + "messages)";
	    	}
	        tvNickName.setText(title);
	    
	        if (contact.isConnected == true) {
	        	tvNickName.setTextColor(Color.BLACK);
	        }
	        else {
	        	tvNickName.setTextColor(0xff999999);
	        }
        }
        return rowView;
    }
}
