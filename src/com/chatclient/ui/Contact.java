package com.chatclient.ui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

import com.chatclient.net.BluetoothHandler;
import com.chatclient.net.NetController;
import com.chatclient.net.WifiHandler;
import com.google.gson.annotations.Expose;

import android.graphics.Bitmap;

public class Contact implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private ChatApplication theApp = null;
	
	@Expose
	public int id = -1;
	@Expose
	public String account = null;
	@Expose
	public String nickName = null;
	@Expose
	public String email = null;
	@Expose
	public int gender = -1;
	@Expose
	boolean isConnected = false;
	
	boolean isTemp = false;
		
	public Bitmap bmpPicture = null;

	public BluetoothHandler btHandler = null;
	public WifiHandler wifiHandler = null;
	
	public ArrayList<Conversation> conversationReadList;
	public ArrayList<Conversation> conversationUnReadList;
	
	public Contact(ChatApplication app, int contactId) {
		theApp = app;		
		id = contactId;		
	}
	
	public void init() {
		conversationReadList = new ArrayList<Conversation>();
		conversationUnReadList = new ArrayList<Conversation>();
		
		if (NetController.network == NetController.NETWORK_BT) {
			btHandler = new BluetoothHandler(theApp, id);
			btHandler.start();
		}
		
		if (NetController.network == NetController.NETWORK_WIFI) {
			wifiHandler = new WifiHandler(theApp, id);
			wifiHandler.start();
		}
	}
	
	public void onDestroy() {
		if (NetController.network == NetController.NETWORK_BT && btHandler != null) {
			btHandler.stop();
		}
		if (NetController.network == NetController.NETWORK_WIFI && wifiHandler != null) {
			wifiHandler.stop();
		}

		if (conversationReadList != null) {
			conversationReadList.clear();
		}
		if (conversationUnReadList != null) {
			conversationUnReadList.clear();
		}
	}
	
	@Override
	public boolean equals(Object o) {
	    if (o instanceof Contact) {
	    	Contact c = (Contact) o;
	   		if (this.account.equals(c.account)) {
    			return true;
    		}
	    }
	    return false;
	}
	
	public static boolean containsAccount(ArrayList<Contact> contactList, String account) {
    	Iterator<Contact> it = contactList.iterator();
    	while(it.hasNext()) {
    		Contact ct = it.next();
    		if (ct.account.equals(account)) {
    			return true;
    		}
    	}
    	return false;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int i) {
		id = i;
	}

	public String getAccount() {
		return account;
	}
	
	public void setAccount(String a) {
		account = a;
	}
	
	public String getNickName() {
		return nickName;
	}
	
	public void setNickName(String n) {
		nickName = n;
	}
	
	public String getEmail() {
		return email;
	}
	
	public void setEmail(String e) {
		email = e;
	}
	
	public int getGender() {
		return gender;
	}
	
	public void setGender(int g) {
		gender = g;
	}
	
}
