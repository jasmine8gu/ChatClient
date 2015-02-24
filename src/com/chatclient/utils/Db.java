package com.chatclient.utils;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class Db {
	private DbHelper dbHelper;  
	private SQLiteDatabase database;  
	
	public final static String TABLE_NABLE = "Conversation"; 
	public final static String CONVERSATION_ID = "_id";
	public final static String CONVERSATION_NAME = "name";

	public Db(Context context) {
	    dbHelper = new DbHelper(context);  
	    database = dbHelper.getWritableDatabase();  
	}
	
	
	public long createRecords(String id, String name) {  
		ContentValues values = new ContentValues();  
		values.put(CONVERSATION_ID, id);  
		values.put(CONVERSATION_NAME, name);
		
		return database.insert(TABLE_NABLE, null, values);  
	}    
	
    public Cursor selectRecords() {
    	String[] cols = new String[] {CONVERSATION_ID, CONVERSATION_NAME};  
    	Cursor mCursor = database.query(true, TABLE_NABLE,cols,null , null, null, null, null, null);  
    	if (mCursor != null) {  
    		mCursor.moveToFirst();  
    	}
    	
    	return mCursor;
    }
}