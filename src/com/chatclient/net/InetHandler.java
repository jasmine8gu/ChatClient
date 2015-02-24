package com.chatclient.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.os.AsyncTask;
import android.os.Message;
import android.util.Log;

import com.chatclient.ui.ChatApplication;
import com.chatclient.utils.ByteCache;

public class InetHandler {
	private static final String TAG = "jasmine InetHandler";
	
	private ChatApplication theApp = null;

    private CommunicateThread communicateThread;
    private int state = 0;
	
    public void sendCommand(byte[] outBytes) {
    	if (state != 1) {
    		return;
    	}
    	
    	AsyncTask<byte[], Void, Void> task = new AsyncTask<byte[], Void, Void>() {

			@Override
			protected Void doInBackground(byte[]... params) {
		        communicateThread.send(params[0]);
				return null;
			}};
			
		task.execute(outBytes);
    }
    
    public void receiveCommand(byte[] inBytes) {
    	Message msg = new Message();
    	msg.what = ChatApplication.RECEIVE_COMMAND;
    	msg.obj = inBytes;
    	theApp.sendMessage(msg);
    }
    
	public InetHandler(ChatApplication app) {
    	theApp = app;
    	state = 0;
		
	}
	
	public void start() {
        communicateThread = new CommunicateThread(this);
        communicateThread.start();
	}
	
	public void stop() {
        if (communicateThread != null) {
        	communicateThread.cancel(); 
        	communicateThread = null;
        }
        
        state = 0;
	}
	
    public synchronized void connected() {
        Message msg = new Message();
    	msg.what = ChatApplication.SERVER_CONNECTED;
    	theApp.sendMessage(msg);
    	state = 1;
    }  
    
    //TODO
    private void connectFail() {
        state = 0;
        
        if (communicateThread != null) {
        	communicateThread.cancel();
        	communicateThread = null;
        }

        Message msg = new Message();
    	msg.what = ChatApplication.SERVER_CONNECTFAIL;
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
    	msg.what = ChatApplication.SERVER_DISCONNECTED;
    	theApp.sendMessage(msg);
    }  

    private class CommunicateThread extends Thread {
    	SocketChannel schannel = null;
        private Selector selector;
        private List<ByteBuffer> sndData;
        
        ByteCache rcvData = new ByteCache();
        private boolean _run = true;
        
        public CommunicateThread(InetHandler h) {
        	sndData = new ArrayList<ByteBuffer>();
        }  
  
        public void run() {
            try {  
	        	schannel = SocketChannel.open();
	        	schannel.configureBlocking(false);  
	            selector = Selector.open();

	            //boolean ret = schannel.connect(new InetSocketAddress("192.168.1.144", 8989));
	            boolean ret = schannel.connect(new InetSocketAddress("54.234.181.176", 8989));
            	schannel.register(selector, SelectionKey.OP_CONNECT);
	            if (!ret) {
	            	int cnt = 0;
            	    while (!schannel.finishConnect() && cnt < 6) {  
            	        Thread.sleep(500);
            	        cnt++;
            	    }
	            }
	            if (schannel.isConnected()) {
	                Socket socket = schannel.socket();
	                InetAddress inetAddress = socket.getInetAddress();
	                SocketAddress remoteAddr = socket.getRemoteSocketAddress();
	                Log.i(TAG, "remote: " + remoteAddr + " inet: " + inetAddress);
	                
	            	schannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
	            	connected();
	            }
	            else {
	            	connectFail();
	            }
            }
            catch (Exception e) {
            	connectFail();
            }
            
            try {
                while (_run) {
                    int cnt = selector.select();
                    if (cnt < 1) {
                    	continue;
                    }

                    Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                    while (keys.hasNext()) {
                        SelectionKey key = (SelectionKey) keys.next();
                        keys.remove();

                        if (!key.isValid()) {
                            continue;
                        }

                        if (key.isReadable()) {
                            read(key);
                        }
                        else if (key.isWritable()) {
                            write(key);
                        }
                        
                    }
                }
            } 
            catch(Exception ex) {  
                ex.printStackTrace();  
            }
        }
  
        private void read(SelectionKey key) throws IOException {
        	Log.i(TAG, "read(key)");
            SocketChannel sc = (SocketChannel) key.channel();
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            
            try {
            	int len = sc.read(buffer);

    	        if (len > -1) {
    		        rcvData.append(buffer.array(), len);    		    	
    		    	Log.i(TAG, "sc read len: " + len + "rcvData length: " + rcvData.length());

    		        while (rcvData.length() >= 4) {
    		        	int commandLen = ByteCache.bytesToInt(rcvData.getBytes(0, 4));
    		        	
    		        	if (commandLen > 0 && rcvData.length() >= commandLen + 4) {
    		        		receiveCommand(rcvData.getBytes(0, 4 + commandLen));
    		        		rcvData.truncHead(commandLen + 4);
    	    		    	Log.i(TAG, "rcvData truncated: " + rcvData.length());
    		        	}
    		        	else {
    		        		break;
    		        	}
    		    	}
    	        }
    	    	else {
    	            sc.close();
    	            key.cancel();
	                connectionLost();
    	            return;
    	    	}
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void write(SelectionKey key) throws IOException {
        	Log.i(TAG, "write(key)");
            SocketChannel sc = (SocketChannel)key.channel();
            
            Iterator<ByteBuffer> it = sndData.iterator();
            while (it.hasNext()) {
            	ByteBuffer sendBuffer = it.next();
            	
                int ret = sc.write(sendBuffer);
                Log.i(TAG, "Write buffer: " + ret);
                
                if (ret > 0) {
                	if (0 == sendBuffer.remaining()) {
                		it.remove();
                	}
                }
                else {
                	break;
                }
            }
            
            if (sndData.isEmpty()) {
            	key.interestOps(SelectionKey.OP_READ);
            }
        }  
                  
        public void send(byte[] outBytes) {
        	sndData.add(ByteBuffer.wrap(outBytes));
        	
        	SelectionKey key = schannel.keyFor(selector);
        	key.interestOps(SelectionKey.OP_WRITE);        	
        	selector.wakeup();
        }  

        public void cancel() {
    		try {
    			_run = false;
    			sndData.clear();
    			
    			if (selector != null) {
    				selector.close();
    			}
    			
	        	if (schannel != null) {
	        			schannel.close();
	            }
            } 
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }    
}
