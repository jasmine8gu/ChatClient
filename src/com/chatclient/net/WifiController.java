package com.chatclient.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Message;
import android.text.format.Formatter;
import android.util.Log;

import com.chatclient.ui.ChatApplication;

public class WifiController {
	private static final String TAG = "jasmine WifiController";
	
    private ChatApplication theApp = null;
    private ServerThread serverThread;
    
    private UdpBroadcaster udpBroadcaster;
    private UdpReceiver udpReceiver = null;
    
    String ipAddress = null;
    
	@SuppressWarnings("deprecation")
	public WifiController(ChatApplication app) {
    	theApp = app;
		WifiManager wifiMgr = (WifiManager)theApp.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
		int ip = wifiInfo.getIpAddress();
		ipAddress = Formatter.formatIpAddress(ip);
        Log.i(TAG, "ipAddress: " + ipAddress);
		
        theApp.contactFrom.account = ipAddress;
        theApp.contactFrom.nickName = ipAddress;
	}
	
	public void start() {
		udpBroadcaster = new UdpBroadcaster();
		udpBroadcaster.start();    	

		serverThread = new ServerThread();
    	serverThread.start();    	
	}
	
    public synchronized void stop() {
        if (serverThread != null) {
        	serverThread.cancel();
        	serverThread = null;
        }
        
    	udpBroadcaster.quit();
    	udpBroadcaster = null;
    }  
  
    public void startDiscovery() {
    	udpReceiver = new UdpReceiver();
    	udpReceiver.start();
    }  

    public void stopDiscovery() {
    	udpReceiver.quit();
    	udpReceiver = null;
    }
  
    public synchronized void connected(Socket socket) {
    	Message msg = new Message();
    	msg.what = ChatApplication.CONTACT_CONNECTED;
    	msg.arg1 = -1;
    	msg.obj = socket;
    	theApp.sendMessage(msg);
    }  
 
    class UdpBroadcaster extends Thread {
        private static final String TAG = "jasmine UdpBroadcaster";

        private DatagramSocket udpSocket;
        
        public static final int DEFAULT_PORT = 43708;
        private static final int MAX_DATA_PACKET_LENGTH = 40;
        private byte[] buffer = new byte[MAX_DATA_PACKET_LENGTH];
        private boolean _run = true;

		public void run() {
        	DatagramPacket dataPacket = null;
         
        	try {
        		dataPacket = new DatagramPacket(buffer, MAX_DATA_PACKET_LENGTH);
        		byte[] data = ipAddress.getBytes();
        		dataPacket.setData(data);
        		dataPacket.setLength(data.length);
        		dataPacket.setPort(DEFAULT_PORT);
        		dataPacket.setAddress(InetAddress.getByName("255.255.255.255"));
        		
        		udpSocket = new DatagramSocket();                
            	while( _run ){
        			udpSocket.send(dataPacket);
                    Log.i(TAG, "udpSocket send");
        			sleep(2000);
            	}
          
        	} 
        	catch (Exception e) {
        		Log.e(TAG, e.toString());
        	}

        	udpSocket.close();
       	}
        
        public void quit() {
        	_run = false;
        }
    } 
        
	class UdpReceiver extends Thread {
		private static final String TAG = "jasmine UdpReceiver";
		
		private DatagramSocket udpSocket;
		private byte[] data = new byte[256];
		private DatagramPacket udpPacket = new DatagramPacket(data, 256);
	    public final int DEFAULT_PORT = 43708;
		 
	    private boolean _run = true;
	    
		public void run() {
			try {
				udpSocket = new DatagramSocket(DEFAULT_PORT);
				while(_run) {
					udpSocket.receive(udpPacket);
			   
					if (udpPacket.getLength() != 0 && udpPacket.getLength() < 256) {
						String codeString = new String(data, 0, udpPacket.getLength());
						if (!codeString.equals(ipAddress)) {
		                	Message msg = new Message();
		                	msg.what = ChatApplication.ADD_CONTACT;
		                	msg.obj = codeString;
		                	theApp.sendMessage(msg);
						}
					}
					sleep(1000);
				}				
			} 
			catch (Exception e) {
				Log.e(TAG, e.toString());
			}
		  
			udpSocket.close();
		}
		
	    public void quit() {
	    	udpSocket.close();
	    	_run = false;
	    }
	}

    private class ServerThread extends Thread {
        private ServerSocket serverSocket = null;
        public static final int DEFAULT_PORT = 43709;
        
        public ServerThread() {  
            ServerSocket tmp = null;
  
            try {  
                tmp = new ServerSocket(DEFAULT_PORT);
            } 
            catch (IOException e) {  
                Log.e(TAG, "listen() failed", e);
            }
            serverSocket = tmp;
        }  
  
        public void run() {  
            setName("AcceptThread");
        	if (serverSocket == null) {
        		Log.i(TAG, "serverSocket null");
        		return;
        	}
        	
        	while (true) {
	            try {  
	                Socket socket = null;
	                
	                Log.i(TAG, "serverSocket accept");
	                socket = serverSocket.accept();
	                if (socket != null) {
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
            try {  
            	serverSocket.close();
            } 
            catch (IOException e) {  
                Log.e(TAG, "close() of server failed", e);
            }  
        }  
    }      
}
