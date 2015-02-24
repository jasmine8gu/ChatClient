package com.chatclient.ui;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.reflect.Type;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.chatclient.R;
import com.chatclient.net.NetController;
import com.chatclient.net.NetHandler;
import com.chatclient.utils.DbHelper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import android.app.Application;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

public class ChatApplication extends Application {
	private static final String TAG = "jasmine ChatApplication";
	
	public static boolean activate = false;
	public boolean isConnected = false;
	public boolean isLogin = false;
	
	private List<Handler> handlers = new ArrayList<Handler>();
	public NetController netController = null;
	public NetHandler netHandler = null;
	public Handler serviceHandler = null;
	
	public ConcurrentHashMap<Integer, Contact> contactList = new ConcurrentHashMap<Integer, Contact>();
	public ConcurrentHashMap<Integer, Contact> searchResult = new ConcurrentHashMap<Integer, Contact>();
	
	public Contact contactFrom = new Contact(this, -1);
	
	private SharedPreferences settingsPrefs;
	
	//upward message set
	public static final int ADD_CONTACT = 1101;
	public static final int REMOVE_CONTACT = 1102;
	
	public static final int CONTACT_CONNECTED = 1103;
	public static final int CONTACT_DISCONNECTED = 1104;
	public static final int CONTACT_CONNECTFAIL = 1105;
	public static final int CONTACT_COMMUNICATED = 1106;

	public static final int SERVER_CONNECTED = 1107;
	public static final int SERVER_DISCONNECTED = 1108;
	public static final int SERVER_CONNECTFAIL = 1109;

	//downward message set
	public static final int CONNECT_CONTACT = 1001;
	public static final int DISCONNECT_CONTACT = 1002;
	public static final int COMMUNICATE_CONTACT = 1003;
	public static final int CONNECT_DUPLICATE = 1004;
	
	public static final int SEND_COMMAND = 1006;
	public static final int RECEIVE_COMMAND = 1007;
	
	public static final int APPLICATION_QUIT = 9999;
	
    @Override
    public void onCreate() {
    	Log.i(TAG, "onCreate");	
    }
    
    public void setServiceHandler(Handler h) {
    	serviceHandler = h;
    }
    
    public void activate() {
		netController = new NetController(this);
		netController.start();
		netHandler = new NetHandler(this);
		netHandler.start();

		settingsPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    	
    	if (settingsPrefs == null) {
    		contactFrom.nickName = contactFrom.account;
			contactFrom.bmpPicture = BitmapFactory.decodeResource(getResources(), R.drawable.profile);
    	}
    	else {
    		contactFrom.nickName = settingsPrefs.getString("nick_name", "");

	    	if (contactFrom.nickName == null ||contactFrom.nickName.length() == 0) {
	    		contactFrom.nickName = contactFrom.account;
	    	}
	    	
			String filePath = new ContextWrapper(this).getFilesDir().getAbsolutePath();
			File imgFile = new File(filePath + "/" + contactFrom.id + ".jpg");

			if(imgFile.exists()) {
				contactFrom.bmpPicture = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
			}
			else {
				contactFrom.bmpPicture = BitmapFactory.decodeResource(getResources(), R.drawable.profile);
			}
    	} 

    	activate = true;
    }
    
    public void deActivated() {
    	activate = false;
    	isConnected = false;
    	isLogin = false;
    	
    	Iterator<Entry<Integer, Contact>> it = contactList.entrySet().iterator();
    	while(it.hasNext()) {
    		Entry<Integer, Contact> entry = it.next();
    		Contact contact = entry.getValue();
    		if (contact != null) {
    			contact.onDestroy();
    		}
    	}
    	
    	contactList.clear();
    	netController.stop();
    	netHandler.stop();
    }

	public synchronized void addHandler(Handler h) {
		if (handlers.indexOf(h) < 0) {
			handlers.add(h);
		}
	}
	
	public synchronized void deleteHandler(Handler h) {
		handlers.remove(h);
	}
	
	public void sendMessage(Message msg) {
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().serializeNulls().create();
		
		switch (msg.what) {
			//upward messages
	        case ADD_CONTACT: {
	        	String account = (String)msg.obj;
	        	Contact theContact = null;
	        	
	        	Iterator<Entry<Integer, Contact>> it = contactList.entrySet().iterator();
	        	while(it.hasNext()) {
	        		Entry<Integer, Contact> entry = it.next();
	        		Contact contact = entry.getValue();
	        		if (contact.account.equals(account)) {
	        			theContact = contact;
	        			break;
	        		}
	        	}

	        	if (theContact == null) {
		        	theContact = new Contact(this, contactList.size());
		        	theContact.init();
		        	theContact.account = account;
		        	theContact.nickName = account;
	            	contactList.put(theContact.id, theContact);
	            	
		        	for (Handler h : handlers) {
		            	h.sendMessage(h.obtainMessage(msg.what, msg.obj));
		            }
		        	
	        	}
	        	
    			if (NetController.network == NetController.NETWORK_BT) {
            		if (theContact.isConnected == false) {
            			serviceHandler.sendMessage(serviceHandler.obtainMessage(CONNECT_CONTACT, theContact.id, 0, null));
            		}
    			}
    			else if (NetController.network == NetController.NETWORK_WIFI) {
        			if (theContact.isConnected == false && contactFrom.account.compareToIgnoreCase(account) < 0) {
        				serviceHandler.sendMessage(serviceHandler.obtainMessage(CONNECT_CONTACT, theContact.id, 0, null));
        			}
    			}
	            break;
	        }
	        
	        case REMOVE_CONTACT: {
	            break;
	        }
	        
	        case CONTACT_CONNECTED: {
        		String account = null;
        		if (NetController.network == NetController.NETWORK_BT) {
		        	BluetoothSocket socket = (BluetoothSocket)msg.obj;
		        	BluetoothDevice device = socket.getRemoteDevice();
	        		account = device.getAddress();
        		}
        		else if (NetController.network == NetController.NETWORK_WIFI) {
		        	Socket socket = (Socket)msg.obj;
	        		account = socket.getInetAddress().getHostAddress();
        		}

	        	Iterator<Entry<Integer, Contact>> it = contactList.entrySet().iterator();
	        	while(it.hasNext()) {
	        		Entry<Integer, Contact> entry = it.next();
	        		Contact contact = entry.getValue();
	        		if (contact.account.equals(account)) {
            			if (contact.isConnected == false) {
	            			contact.isConnected = true;
	            			
	            			serviceHandler.sendMessage(serviceHandler.obtainMessage(COMMUNICATE_CONTACT, contact.id, 0, msg.obj));
	            			
	        	        	for (Handler h : handlers) {
	        	            	h.sendMessage(h.obtainMessage(msg.what, msg.obj));
	        	            }	        	        	
            			}
    	        		else if (NetController.network == NetController.NETWORK_BT) {
                			serviceHandler.sendMessage(serviceHandler.obtainMessage(CONNECT_DUPLICATE, contact.id, 0, null));
    	    			}
            			break;
	        		}
	        	}
	        	
	            break;
	        }
	        
	        case CONTACT_DISCONNECTED: {
	        	Contact contact = contactList.get(msg.arg1);
	        	if (contact != null) {
            		contact.isConnected = false;
	        	        		
            		for (Handler h : handlers) {
            			h.sendMessage(h.obtainMessage(msg.what, msg.obj));
            		}
	        	}
	        	break;
	        }
	        
	        case CONTACT_CONNECTFAIL: {
	        	for (Handler h : handlers) {
	            	h.sendMessage(h.obtainMessage(msg.what, msg.obj));
	            }
	            break;
	        }
	        
	        case CONTACT_COMMUNICATED: {
	        	int contactId = msg.arg1;
	        	
    			Command command = new Command(Command.NICKNAME, 0, 0, contactFrom.nickName.getBytes());
            	serviceHandler.sendMessage(serviceHandler.obtainMessage(SEND_COMMAND, contactId, 0, command.encode()));
            	
            	if (contactFrom.bmpPicture != null) {
            		ByteArrayOutputStream baos = new ByteArrayOutputStream();
        	    	contactFrom.bmpPicture.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        	    	byte[] data = baos.toByteArray();
        			
        			Command command2 = new Command(Command.SYNCPICTURE, 0, 0, data);
	            	serviceHandler.sendMessage(serviceHandler.obtainMessage(SEND_COMMAND, contactId, 0, command2.encode()));
            	}
            	break;
	        }
	        
	        case SERVER_CONNECTED: {
	        	for (Handler h : handlers) {
	            	h.sendMessage(h.obtainMessage(msg.what, msg.obj));
	            }
	        	break;
	        }
	        
	        case SERVER_CONNECTFAIL: {
	        	for (Handler h : handlers) {
	            	h.sendMessage(h.obtainMessage(msg.what, msg.obj));
	            }
	        	break;
	        }
	        
	        case SERVER_DISCONNECTED: {
	        	for (Handler h : handlers) {
	            	h.sendMessage(h.obtainMessage(msg.what, msg.obj));
	            }
	        	break;
	        }
	        
	        //downward messages
	        case DISCONNECT_CONTACT: {
	        	break;
	        }
	        
	        case SEND_COMMAND: {
	    		serviceHandler.sendMessage(serviceHandler.obtainMessage(msg.what, 0, 0, msg.obj));
				break;
			}
			
			case RECEIVE_COMMAND: {
				if (NetController.network != NetController.NETWORK_INET) {
		        	Contact contact = contactList.get(msg.arg1);
		        	if (contact != null) {
						Command command = Command.decode((byte[])msg.obj);
						if (command.cmd == Command.CONVERSATION) {
							Conversation conversation = new Conversation();
							conversation.isHost = false;
							conversation.text = new String(command.value);
							conversation.contactFrom = contact;
							contact.conversationUnReadList.add(conversation);
						}
						else if (command.cmd == Command.NICKNAME) {
			    			contact.nickName = new String(command.value);
						}
						else if (command.cmd == Command.SYNCPICTURE) {
			    			contact.bmpPicture = BitmapFactory.decodeByteArray(command.value, 0, command.value.length);
						}
						
						for (Handler h : handlers) {
			            	h.sendMessage(h.obtainMessage(msg.what, msg.obj));
			            }
		        	}
				}
				else {
					Command command = Command.decode((byte[])msg.obj);
					if (command.cmd == Command.LOGIN) {
						if (command.arg1 > 0) {
							Contact user = gson.fromJson(new String(command.value), Contact.class);
							
							contactFrom.setId(command.arg1);
							contactFrom.setAccount(user.account);
							contactFrom.setNickName(user.nickName);
							contactFrom.setEmail(user.email);
							contactFrom.setGender(user.gender);
							
							String filePath = new ContextWrapper(this).getFilesDir().getAbsolutePath();
							File imgFile = new File(filePath + "/" + contactFrom.id + ".jpg");

							if(imgFile.exists()) {
								contactFrom.bmpPicture = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
							}
							else {
								contactFrom.bmpPicture = BitmapFactory.decodeResource(getResources(), R.drawable.profile);
							}
							
							isLogin = true;
							
					    	//DbHelper db = new DbHelper(this);
					        //db.getAllContacts(contactList, contactFrom.id);
						}
					}
					else if (command.cmd == Command.SIGNUP) {
						if (command.arg1 > 0) {
							Contact user = gson.fromJson(new String(command.value), Contact.class);
							contactFrom.setId(command.arg1);
							contactFrom.setAccount(user.account);
							isLogin = true;
						}
					}
					else if (command.cmd == Command.CONTACTSIGNIN) {
						Contact ct = contactList.get(command.arg1);
						if (ct != null) {
							ct.isConnected = true;
						}
					}
					else if (command.cmd == Command.CONTACTSIGNOFF) {
						Contact ct = contactList.get(command.arg1);
						if (ct != null) {
							ct.isConnected = false;
						}
					}
					else if (command.cmd == Command.CONVERSATION) {
						int id = command.arg1;
						Contact contact = contactList.get(id);
						if (contact == null) {
							contact = new Contact(this, id);
							contact.init();
							contact.isTemp = true;
							contactList.put(id, contact);
						}
						
						Conversation conversation = new Conversation();
						conversation.isHost = false;
						conversation.text = new String(command.value);
						conversation.contactFrom = contact;
						contact.conversationUnReadList.add(conversation);
					}
					else if (command.cmd == Command.SEARCHCONTACT) {
						searchResult.clear();
						
						Type type = new TypeToken<List<Contact>>(){}.getType();
						List<Contact> ctList = gson.fromJson(new String(command.value), type);
						for (int i = 0; i < ctList.size(); i++) {
							Contact ct = ctList.get(i);
							ct.init();
							ct.isTemp = true;
							searchResult.put(ct.id, ct);
						}
					}
					else if (command.cmd == Command.SYNCPICTURE) {
						Bitmap syncPicture = BitmapFactory.decodeByteArray(command.value, 0, command.value.length);
						if (command.arg1 == contactFrom.id) {
							contactFrom.bmpPicture = syncPicture;
						}
						else {
							Contact contact = contactList.get(command.arg1);
							if (contact != null) {
								contact.bmpPicture = syncPicture;
							}
							
							Contact contact1 = searchResult.get(command.arg1);
							if (contact1 != null) {
								contact1.bmpPicture = syncPicture;
							}
						}
					}
					else if (command.cmd == Command.SYNCPROFILE) {
						Type type = new TypeToken<List<Contact>>(){}.getType();
						List<Contact> syncResult = gson.fromJson(new String(command.value), type);
						for (int i = 0; i < syncResult.size(); i++) {
							Contact ctNew = syncResult.get(i);
							Contact ctOld = contactList.get(ctNew.id);
							if (ctOld != null) {
								ctOld.nickName = ctNew.nickName;
								ctOld.email = ctNew.email;
								ctOld.gender = ctNew.gender;
								ctOld.isConnected = ctNew.isConnected;
							}
							else {
								ctNew.init();
								contactList.put(ctNew.id, ctNew);
							}
						}
					}
					
					for (Handler h : handlers) {
		            	h.sendMessage(h.obtainMessage(msg.what, msg.obj));
		            }
				}
	            break;
			}
			case APPLICATION_QUIT: {
				for (Handler h : handlers) {
	            	h.sendMessage(h.obtainMessage(APPLICATION_QUIT, null));
	            }
				break;
			}
		}
	}
}
