package com.chatclient.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import com.chatclient.ui.ChatApplication;
import com.chatclient.utils.ByteCache;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.os.Message;
import android.util.Log;

@SuppressLint("NewApi")
public class BluetoothHandler {
    private static final String TAG = "jasmine BluetoothHandler";

    private ChatApplication theApp = null;
    private int contactId;
    
    private ClientThread clientThread;
    private CommunicateThread communicateThread;

    private int state = 0;

    private final BluetoothAdapter btAdapter;
    
    public void sendCommand(byte[] outBytes) {
    	if (state != 1) {
    		return;
    	}
    	
        communicateThread.write(outBytes);
    	Log.i(TAG, "sendcommand: " + new String(outBytes));
    }
    
    public void receiveCommand(byte[] inBytes) {
    	Message msg = new Message();
    	msg.what = ChatApplication.RECEIVE_COMMAND;
    	msg.arg1 = contactId;
    	msg.obj = inBytes;
    	theApp.sendMessage(msg);
    }
    
    @SuppressLint("NewApi")
	public BluetoothHandler(ChatApplication app, int id) {
    	theApp = app;
    	btAdapter = BluetoothAdapter.getDefaultAdapter();
    	
    	contactId = id;
    	state = 0;
    }  
  
    public synchronized void start() {
    	
    }  
  
    public synchronized void stop() {
        if (clientThread != null) {
        	clientThread.cancel();
        	clientThread = null;
        }
          
        if (communicateThread != null) {
        	communicateThread.cancel(); 
        	communicateThread = null;
        }
        
        state = 0;
    }  
  
    public synchronized void connect(String address) {
    	Log.i(TAG, "connect");
    	if (clientThread != null) {
    		return;
    	}
  
        clientThread = new ClientThread(address);
        clientThread.start();
    }  

    public synchronized void connected(BluetoothSocket socket) {
    	Log.i(TAG, "connected");
        if (clientThread != null) {
        	clientThread = null;
        }  

        state = 0;
        
        Message msg = new Message();
    	msg.what = ChatApplication.CONTACT_CONNECTED;
    	msg.arg1 = contactId;
    	msg.obj = socket;
    	theApp.sendMessage(msg);
    }  
    
    private void connectFail(String address) {
        if (clientThread != null) {
        	clientThread = null;
        }  
  
        state = 0;
        
    	Message msg = new Message();
    	msg.what = ChatApplication.CONTACT_CONNECTFAIL;
    	msg.arg1 = contactId;
    	msg.obj = address;
    	theApp.sendMessage(msg);
    }

    private void connectionLost() {
    	Log.i(TAG, "connectionLost");
    	state = 0;
        if (communicateThread != null) {
        	communicateThread.cancel(); 
        	communicateThread = null;
        }

    	Message msg = new Message();
    	msg.what = ChatApplication.CONTACT_DISCONNECTED;
    	msg.arg1 = contactId;
    	theApp.sendMessage(msg);
    }  

    public void communicate(BluetoothSocket socket) {
        communicateThread = new CommunicateThread(socket, this);
        communicateThread.start();
        state = 1;
    }
    
    public void communicated() {
        state = 1;
        
    	Message msg = new Message();
    	msg.what = ChatApplication.CONTACT_COMMUNICATED;
    	msg.arg1 = contactId;
    	theApp.sendMessage(msg);    	
    }
    
    public void duplicate(BluetoothSocket socket) {
    	Log.i(TAG, "duplicate");
    	try {
			socket.close();
		} 
    	catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    private class ClientThread extends Thread {
    	String serverAddress = null;
        private BluetoothSocket btSocket;  
        
        public ClientThread(String adr) {
        	Log.i(TAG, "ClientThread contruction");
        	
        	serverAddress = adr;
            BluetoothSocket tmp = null;  
  
            try {  
            	UUID theUUID = UUID.nameUUIDFromBytes(serverAddress.getBytes());
                tmp = btAdapter.getRemoteDevice(serverAddress).createRfcommSocketToServiceRecord(theUUID);
            } 
            catch (Exception e) {  
                Log.e(TAG, "create() failed", e);  
            }  
            btSocket = tmp;  
        }  
  
        public void run() {
            Log.i(TAG, "ClientThread run");
            setName("ConnectThread");  
  
            try {  
            	btSocket.connect();
            } 
            catch (IOException e) {  
                try {  
                	btSocket.close();  
                }
                catch (Exception e2) {  
                    Log.e(TAG, "unable to close() socket during connection failure", e2);
                }  
            	connectFail(serverAddress);
                return;  
            }  
  
            Log.i(TAG, "client socket connected to serversocket" + btSocket.getRemoteDevice().getName());
            connected(btSocket);
        }  
  
        public void cancel() {
        	Log.e(TAG, "ClientThread cancel");
            try {  
            	btSocket.close();
            } 
            catch (IOException e) {  
                Log.e(TAG, "close() of connect socket failed", e);
            }  
        }  
    }  
  
    private class CommunicateThread extends Thread {
        private final BluetoothSocket btSocket;
        private final InputStream inStream;
        private final OutputStream outStream;
        private BluetoothHandler btHandler;
        private boolean _run = true;

        public CommunicateThread(BluetoothSocket socket, BluetoothHandler h) {
            Log.i(TAG, "CommunicateThread construction");
        	btHandler = h;
            btSocket = socket;  
            InputStream tmpIn = null;  
            OutputStream tmpOut = null;  
  
            try {  
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } 
            catch (IOException e) {  
                Log.e(TAG, "temp sockets not created", e);
            }  
  
            inStream = tmpIn;  
            outStream = tmpOut;  
        }  
  
        public void run() {  
            Log.i(TAG, "CommunicateThread run");
            
            byte[] buffer = new byte[1024];
            
            try {
            	ByteCache cache = new ByteCache();
            	int commandLen = -1;
            	int len = -1;
            	
            	communicated();
            	
            	while (_run) {
	            	len = inStream.read(buffer);
	            	if (len >= 0) {
		            	cache.append(buffer, len);
		            	
		            	while (_run) {
			            	if (commandLen == -1 && cache.length() >= 4) {
				            	commandLen = ByteCache.bytesToInt(cache.getBytes(0, 4));
			            	}
			            	
			            	if (commandLen > 0 && cache.length() >= commandLen + 4) {
			            		btHandler.receiveCommand(cache.getBytes(0, 4 + commandLen));
			            		cache.truncHead(commandLen + 4);
			            		commandLen = -1;
			            	}
			            	else {
			            		break;
			            	}
		            	}
	            	}
	            	else {
	                    _run = false;
	                    connectionLost();
	            	}
            	}
        	} 
            catch (IOException e) {
                Log.e(TAG, "instream read ioexception" + e.toString());
                _run = false;
                connectionLost();
        	}
            
        }
  
        public void write(byte[] outBytes) {
        	Log.i(TAG, "write outBytes length: " + outBytes.length);
            try {
            	outStream.write(outBytes);
            	outStream.flush();
            } 
            catch (IOException e) {  
                Log.e(TAG, "outstream write ioexception" + e.toString());
                _run = false;
                connectionLost();  
            }  
        }  
  
        public void cancel() {  
        	Log.e(TAG, "CommunicateThread cancel");
            try {  
            	btSocket.close();  
            } 
            catch (IOException e) {  
                Log.e(TAG, "close() of connect socket failed", e);  
            }
        }  
    }
}
