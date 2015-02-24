package com.chatclient.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.ConcurrentHashMap;

import com.chatclient.ui.ChatApplication;
import com.chatclient.ui.Contact;

import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.BitmapFactory;

public class DbHelper extends SQLiteOpenHelper {
	private ChatApplication theApp = null;
	
    private static final String DATABASE_NAME = "sparkii";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_CONTACT = "contact";

    private static final String KEY_ID = "id";
    private static final String KEY_ACCOUNT = "account";
    private static final String KEY_NICKNAME = "nickname";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_GENDER = "gender";
    private static final String KEY_HOSTID = "hostid";

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION); 
        theApp = (ChatApplication)context;
    }
 
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACT_TABLE = "CREATE TABLE contact ( " +
                "id INTEGER PRIMARY KEY, " +
                "account TEXT, "+
                "nickname TEXT, "+
                "email TEXT, "+
                "gender INTEGER, "+
                "hostid INTEGER )";
 
        db.execSQL(CREATE_CONTACT_TABLE);
    }
 
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS contact"); 
        this.onCreate(db);
    }
   
    public long addContact(Contact contact, int hostid){
    	SQLiteDatabase db = this.getWritableDatabase();

    	ContentValues values = new ContentValues();
    	values.put(KEY_ID, contact.getId());
    	values.put(KEY_ACCOUNT, contact.getAccount());
    	values.put(KEY_NICKNAME, contact.getNickName());
    	values.put(KEY_EMAIL, contact.getEmail());
    	values.put(KEY_GENDER, contact.getGender());
    	values.put(KEY_HOSTID, hostid);
    	
    	long ret = db.insert(TABLE_CONTACT,
    			null,
    			values);
    	
    	db.close();
    	return ret;
    }
    
    public void getAllContacts(ConcurrentHashMap<Integer, Contact> contactList, int hostid) {
        String query = "SELECT  * FROM " + TABLE_CONTACT + " where hostid=" + hostid;
  
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
  
        Contact contact = null;
        if (cursor.moveToFirst()) {
            do {
            	int id = cursor.getInt(0);
            	contact = new Contact(theApp, id);
            	contact.init();
            	contact.setAccount(cursor.getString(1));
            	contact.setNickName(cursor.getString(2));
            	contact.setEmail(cursor.getString(3));
            	contact.setGender(cursor.getInt(4));
            	
            	try {
        		String filePath = new ContextWrapper(theApp).getFilesDir().getAbsolutePath();
                String fileName = id + ".jpg";
                File file = new File(filePath + "/" + fileName);
	    		if (file.exists()) {
		    	    byte[] imgData = new byte[(int) file.length()];
		    	    BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
		    	    bis.read(imgData, 0, imgData.length);
		    	    contact.bmpPicture = BitmapFactory.decodeByteArray(imgData, 0, imgData.length);
			        bis.close();
	    		}
            	}
            	catch (Exception e) {
            		e.printStackTrace();
            	}
	    		contactList.put(id, contact);
            } while (cursor.moveToNext());
        }
        
    	db.close();
    }
    
    public int updateContact(Contact contact) {
        SQLiteDatabase db = this.getWritableDatabase();
     
        ContentValues values = new ContentValues();
    	values.put(KEY_ID, contact.getId());
    	values.put(KEY_ACCOUNT, contact.getAccount());
    	values.put(KEY_NICKNAME, contact.getNickName());
    	values.put(KEY_EMAIL, contact.getEmail());
    	values.put(KEY_GENDER, contact.getGender());
     
        int i = db.update(TABLE_CONTACT,
                values,
                KEY_ID+" = ?", 
                new String[] { String.valueOf(contact.getId()) });
     
        db.close();
     
        return i;
    }
    
    public void deleteContact(Contact contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CONTACT,
                KEY_ID+" = ?",
                new String[] { String.valueOf(contact.getId()) });
 
        db.close();
    }
    
    public void deleteAllContact() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CONTACT,
                null,
                null);
        db.close();
    }
}