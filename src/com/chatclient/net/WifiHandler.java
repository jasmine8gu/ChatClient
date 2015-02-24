package com.chatclient.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

import com.chatclient.ui.ChatApplication;
import com.chatclient.utils.ByteCache;

import android.os.Message;
import android.util.Log;

public class WifiHandler {
    private static final String TAG = "jasmine WifiHandler";
    
    private ChatApplication theApp = null;
    private int contactId;
    
    private ClientThread clientThread;
    private CommunicateThread communicateThread;

    private int state = 0;  
    
    public void sendCommand(byte[] outBytes) {
    	if (state != 1) {
    		return;
    	}
    	
    	int t1 = outBytes.length;
    	String t2 = new String(outBytes);
    	Log.i(TAG, "sendcommand length: " + t1 + "command: " + t2);
    	
        communicateThread.write(outBytes);
    	Log.i(TAG, "sendcommand: " + new String(outBytes));
    }
    
    public void receiveCommand(byte[] inBytes) {
    	int t1 = inBytes.length;
    	String t2 = new String(inBytes);
    	Log.i(TAG, "receiveCommand length: " + t1 + "command: " + t2);
    	
    	Message msg = new Message();
    	msg.what = ChatApplication.RECEIVE_COMMAND;
    	msg.arg1 = contactId;
    	msg.obj = inBytes;
    	theApp.sendMessage(msg);
    }
    
	public WifiHandler(ChatApplication app, int id) {  
    	theApp = app;
    	contactId = id;
    	state = 0;
	}
	
	public void start() {
		
	}
	
	public void stop() {
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
    
    public synchronized void connect(String serverIp) {
    	Log.i(TAG, "connect");
    	if (clientThread != null) {
    		return;
    	}
  
        clientThread = new ClientThread(serverIp);
        clientThread.start();  
    }  

    public synchronized void connected(Socket socket) {
    	Log.i(TAG, "connected");
        if (clientThread != null) {
        	clientThread = null;
        }  

    	Message msg = new Message();
    	msg.what = ChatApplication.CONTACT_CONNECTED;
    	msg.arg1 = contactId;
    	msg.obj = socket;
    	theApp.sendMessage(msg);
    }  

    private void connectFail(String serverIp) {
        if (clientThread != null) {
        	clientThread = null;
        }  
  
        state = 0;
        
    	Message msg = new Message();
    	msg.what = ChatApplication.CONTACT_CONNECTFAIL;
    	msg.arg1 = contactId;
    	msg.obj = serverIp;
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

    public void communicate(Socket socket) {
        communicateThread = new CommunicateThread(socket, this);
        communicateThread.start();
    }

    public void communicated() {
        state = 1;
        
    	Message msg = new Message();
    	msg.what = ChatApplication.CONTACT_COMMUNICATED;
    	msg.arg1 = contactId;
    	theApp.sendMessage(msg);    	
    }
    
    public void duplicate(Socket socket) {
    	Log.i(TAG, "duplicate");
    	try {
			socket.close();
		} 
    	catch (IOException e) {
			e.printStackTrace();
		}
    }
        
	class ClientThread extends Thread {
		private String serverIp;
	    public final int DEFAULT_PORT = 43709;
	    
	    Socket wifiSocket;

		public ClientThread(String s) {
			serverIp = s;
		}
		
        public void run() {
            try {
                InetAddress serverAddr = InetAddress.getByName(serverIp);
                wifiSocket = new Socket(serverAddr, DEFAULT_PORT);
            } 
            catch (Exception e) {
                try {  
                	wifiSocket.close();
                }
                catch (Exception e2) {
                    Log.e(TAG, "unable to close() socket during connection failure", e2);
                }  
                connectFail(serverIp);
                return;  
            }
  
            connected(wifiSocket);
        }
        
        public void cancel() {  
            try {  
            	wifiSocket.close();
            } 
            catch (Exception e) {  
                Log.e(TAG, "close() of connect socket failed", e);
            }  
        }
        
	}
    private class CommunicateThread extends Thread {  
        private final Socket wifiSocket;  
        private final InputStream inStream;  
        private final OutputStream outStream;  
        private WifiHandler wifiHandler;
        private boolean _run = true;

        public CommunicateThread(Socket socket, WifiHandler h) {
            Log.d(TAG, "create ConnectedThread");  
        	wifiHandler = h;
            wifiSocket = socket;  
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
			            		wifiHandler.receiveCommand(cache.getBytes(0, 4 + commandLen));
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
            	wifiSocket.close();
            } 
            catch (Exception e) {  
                Log.e(TAG, "close() of connect socket failed", e);  
            }
        }  
    }	
}
