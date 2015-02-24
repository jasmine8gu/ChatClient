package com.chatclient.net;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import com.chatclient.ui.ChatApplication;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Message;
import android.util.Log;

public class BluetoothController {
    private static final String TAG = "jasmine BluetoothController";

    private ChatApplication theApp = null;
    private ServerThread serverThread;

    private final BluetoothAdapter btAdapter;    
    
    public final BroadcastReceiver btReceiver = new BroadcastReceiver() {
        @Override  
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
  
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            	Message msg = new Message();
            	msg.what = ChatApplication.ADD_CONTACT;
            	msg.obj = device.getAddress();
            	theApp.sendMessage(msg);
            } 
        }  
    };  
    
    @SuppressLint("NewApi")
	public BluetoothController(ChatApplication app) {
    	theApp = app;

    	btAdapter = BluetoothAdapter.getDefaultAdapter();
        theApp.contactFrom.account = btAdapter.getAddress();
        theApp.contactFrom.nickName = btAdapter.getAddress();
        
    	Log.i(TAG, "constructor");
    }  
  
    public synchronized void start() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        theApp.getApplicationContext().registerReceiver( btReceiver, filter);

        serverThread = new ServerThread();
    	serverThread.start();  
    }  
  
    public synchronized void stop() {
        if (serverThread != null) {
        	serverThread.cancel(); 
        	serverThread = null;
        }
        
        theApp.getApplicationContext().unregisterReceiver(btReceiver);
    }  
  
    public void startDiscovery() {
        if (btAdapter.isDiscovering()) {
            btAdapter.cancelDiscovery();
        }  
  
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
            	Message msg = new Message();
            	msg.what = ChatApplication.ADD_CONTACT;
            	msg.obj = device.getAddress();
            	theApp.sendMessage(msg);
            }  
        }
        
        btAdapter.startDiscovery();
    }

    public void stopDiscovery() {
        if (btAdapter.isDiscovering()) {
            btAdapter.cancelDiscovery();
        }  
    }
    
    public synchronized void connected(BluetoothSocket socket) {
    	Message msg = new Message();
    	msg.what = ChatApplication.CONTACT_CONNECTED;
    	msg.arg1 = -1;
    	msg.obj = socket;
    	theApp.sendMessage(msg);
    }  
    
    private class ServerThread extends Thread {
        private BluetoothServerSocket serverSocket;
  
        public ServerThread() {
        	Log.i(TAG, "serverthread contruction");
        	
        	BluetoothServerSocket tmp = null;
  
            try {  
            	UUID theUUID = UUID.nameUUIDFromBytes(btAdapter.getAddress().getBytes());
                tmp = btAdapter.listenUsingRfcommWithServiceRecord("Sparkii", theUUID);
                serverSocket = tmp;
            } 
            catch (Exception e) {  
                Log.e(TAG, "listen() failed", e);
                serverSocket = null;
            }  
        }  
  
        public void run() {  
        	Log.i(TAG, "serverthread run");
        	
        	if (serverSocket == null) {
        		Log.i(TAG, "serverSocket null");
        		return;
        	}
        	
        	while (true) {
	            try {  
	            	BluetoothSocket socket = null;
	            	
	            	Log.i(TAG, "serverSocket waiting for socket accept ... ...");
	                socket = serverSocket.accept();
	                if (socket != null) {
	                	Log.i(TAG, "serverSocket accept a socket" + socket.getRemoteDevice().getName());
	            		connected(socket);
	                }  
	            }
	            catch (IOException e) {  
	                Log.e(TAG, "accept() failed", e);  
	                break;
	            }    
        	}
        }  
  
        public void cancel() {
        	Log.e(TAG, "ServerThread cancel");
        	try {
            	serverSocket.close();
            } 
            catch (IOException e) {
                Log.e(TAG, "close() of server failed", e);
            }  
        }  
    }
}
