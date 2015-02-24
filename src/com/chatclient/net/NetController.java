package com.chatclient.net;

import com.chatclient.ui.ChatApplication;

import android.bluetooth.BluetoothAdapter;
import android.util.Log;

public class NetController {
	private static final String TAG = "jasmine NetController";
	
	public static final int NETWORK_BT = 0;
	public static final int NETWORK_WIFI = 1;
	public static final int NETWORK_INET = 2;

	private ChatApplication theApp = null;
    public static int network = -1;

    private BluetoothController btController;
    private WifiController wifiController;
    private InetController inetController;
    
    private BluetoothAdapter btAdapter = null;
    
    public NetController(ChatApplication app) {
    	Log.i(TAG, "contruction");
    	theApp = app;

    	try {
    		btAdapter = BluetoothAdapter.getDefaultAdapter();
	        if (btAdapter == null) {
	        }        
	        else if (!btAdapter.isEnabled()) {
	        	btAdapter.enable();
	        }
    	}
    	catch (Exception e) {
    		System.out.println(e.toString());
    	}
    }
    
    public void start() {
    	if (network == NETWORK_BT) {
    		btController = new BluetoothController(theApp);
    		btController.start();
	    }
	    else if (network == NETWORK_WIFI) {
	    	wifiController = new WifiController(theApp);
	    	wifiController.start();
	    }
	    else if (network == NETWORK_INET) {
	    	inetController = new InetController(theApp);
	    	inetController.start();
	    }
    }
    
    public void stop() {
        if (network == NETWORK_BT) {
            if (btController != null) {
            	btController.stop();  
            }
        }
        else if (network == NETWORK_WIFI) {
            if (wifiController != null) {
            	wifiController.stop();  
            }        	
        }
        else if (network == NETWORK_INET) {
        	if (inetController != null) {
        		inetController.stop();
        	}
        }
    }
    
    public void startDiscovery() {
    	if (network == NETWORK_BT) {
    		btController.startDiscovery();
    	}
    	else if (network == NETWORK_WIFI) {
    		wifiController.startDiscovery();
    	}
    	else if (network == NETWORK_INET) {
    		//
    	}
    }
    
    public void stopDiscovery() {
    	if (network == NETWORK_BT) {
    		btController.stopDiscovery();
    	}
    	else if (network == NETWORK_WIFI) {
    		wifiController.stopDiscovery();
    	}
    	else if (network == NETWORK_INET) {
    		//
    	}
    }
}
