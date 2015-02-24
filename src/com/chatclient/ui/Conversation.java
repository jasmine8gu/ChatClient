package com.chatclient.ui;

import java.util.ArrayList;

public class Conversation {
	public static ArrayList<Conversation> conversationList = new ArrayList<Conversation>();
	
	public int id;	
	public Contact contactFrom;
	public Contact contactTo;
	public String text;
	public String datetime;
	
	public boolean isHost = false;
	
	@Override
	public boolean equals(Object o) {
	    if (o instanceof Conversation) {
	    	Conversation c = (Conversation)o;
	   		if (this.id == c.id) {
    			return true;
    		}
	    }
	    
	    return false;
	}
}
