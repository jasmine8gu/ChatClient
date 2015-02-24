package com.chatclient.net;

import java.net.Socket;

import com.chatclient.ui.ChatApplication;
import com.chatclient.ui.Command;
import com.chatclient.ui.Contact;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

@SuppressLint({ "NewApi", "HandlerLeak" })
public class NetHandler {
	private static final String TAG = "jasmine NetHandler";
	
	public static final int NETWORK_BT = 0;
	public static final int NETWORK_WIFI = 1;
	public static final int NETWORK_INET = 2;
	
    private ChatApplication theApp = null;
    private InetHandler inetHandler = null;
    
    private void handleBTMessage(Message msg) {
        switch (msg.what) {
        case ChatApplication.CONNECT_CONTACT: {
        	Contact theContact = theApp.contactList.get(msg.arg1);
        	if (theContact == null) {
        		return;
        	}
        	
    		String address = theContact.account;
			theContact.btHandler.connect(address);
        	break;         
        }
        
        case ChatApplication.DISCONNECT_CONTACT:
        	break;
        
        case ChatApplication.COMMUNICATE_CONTACT: {
        	Contact theContact = theApp.contactList.get(msg.arg1);
        	if (theContact == null) {
        		return;
        	}
        	
        	theContact.btHandler.communicate((BluetoothSocket)msg.obj);
        	break;
        }
        
        case ChatApplication.CONNECT_DUPLICATE: {
        	Contact theContact = theApp.contactList.get(msg.arg1);
        	if (theContact == null) {
        		return;
        	}
        	
        	theContact.btHandler.duplicate((BluetoothSocket)msg.obj);
        	break;
        }
        
        case ChatApplication.SEND_COMMAND:
        	Command command = Command.decode((byte[])msg.obj);
        	Contact theContact = theApp.contactList.get(command.arg2);
        	if (theContact == null) {
        		return;
        	}
        	
        	theContact.btHandler.sendCommand((byte[])msg.obj);
        	break;      
        
        default:
        	break;
        }    	
    }
    
    private void handleWifiMessage(Message msg) {
    	Contact theContact = theApp.contactList.get(msg.arg1);        	
    	if (theContact == null) {
    		return;
    	}
    	
        switch (msg.what) {
        case ChatApplication.CONNECT_CONTACT: {
    		String address = theContact.account;
    		theContact.wifiHandler.connect(address);
        	break;         
        }
        
        case ChatApplication.DISCONNECT_CONTACT:
        	theContact.wifiHandler.stop();
        	break;
        
        case ChatApplication.COMMUNICATE_CONTACT: {
        	theContact.wifiHandler.communicate((Socket)msg.obj);
        	break;
        }
        
        case ChatApplication.CONNECT_DUPLICATE: {
        	theContact.wifiHandler.duplicate((Socket)msg.obj);
        	break;
        }
        
        case ChatApplication.SEND_COMMAND:
        	theContact.wifiHandler.sendCommand((byte[])msg.obj);
        	break;      
        
        default:
        	break;
        }
    }
    
    private void handleInetMessage(Message msg) {
        switch (msg.what) {
        case ChatApplication.SEND_COMMAND:
        	inetHandler.sendCommand((byte[])msg.obj);
        	break;      
        
        default:
        	break;
        }
    }
    
    private Handler serviceHandler = new Handler() {
        public void handleMessage(Message msg) {
        	if (NetController.network == NETWORK_BT) {
            	handleBTMessage(msg);
        	}
        	else if (NetController.network == NETWORK_WIFI) {
        		handleWifiMessage(msg);
        	}
        	else if (NetController.network == NetController.NETWORK_INET) {
        		handleInetMessage(msg);
        	}
        }
    };

    public NetHandler(ChatApplication app) {
    	Log.i(TAG, "contruction");
    	
    	theApp = app;
    	theApp.setServiceHandler(serviceHandler);
    }
    
    public void start() {
	    if (NetController.network == NetController.NETWORK_INET) {
	    	inetHandler = new InetHandler(theApp);
	    	inetHandler.start();
	    }
    }
    
    public void stop() {
        if (NetController.network == NetController.NETWORK_INET) {
        	if (inetHandler != null) {
        		inetHandler.stop();
        	}
        }
    }
    
}
