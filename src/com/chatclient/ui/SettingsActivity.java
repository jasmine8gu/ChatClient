package com.chatclient.ui;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import com.chatclient.R;
import com.chatclient.utils.DbHelper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.provider.MediaStore;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

public class SettingsActivity extends Activity {
	ChatApplication theApp;
	Contact theContact = null;
	
	private final int PROFILE_HOST = 0;
	private final int PROFILE_CONTACTLIST = 1;
	private final int PROFILE_CONTACTRESULT = 2;
	int profile = -1;
	
	ImageView ivPicture;
	EditText etNickName;
	EditText etEmail;
	RadioGroup rgGender;
	
	TextView errNickName = null;  
	TextView errEmail = null;  
	
	Button btnDone;
	Button btnAdd;
	Button btnDelete;
	
    private static final int PHOTO_REQUEST_TAKEPHOTO = 1;
    private static final int PHOTO_REQUEST_GALLERY = 2;
    private static final int PHOTO_REQUEST_CUT = 3;
    
    String filePath = null; 
    String fileName = null;
    File tempFile = null;
    Bitmap newBitmap = null;
    
	private boolean checkNickName() {
		boolean ret = false;
		String sNickName = etNickName.getText().toString();
		
    	if (sNickName != null && sNickName.indexOf(' ') >= 0) {
    		errNickName.setText("No space allowed!");
    		errNickName.setVisibility(View.VISIBLE);
    		ret = false;
    	}
    	else {
    		errNickName.setVisibility(View.GONE);    		
    		ret = true;
    	}
    	
		return ret;
	}
	
	private boolean checkEmail() {
		boolean ret = false;
		String sEmail = etEmail.getText().toString();
		
    	if (sEmail != null && sEmail.length() > 0 && sEmail.indexOf('@') < 0) {
    		errEmail.setText("Invalid email address!");
    		errEmail.setVisibility(View.VISIBLE);      
    		ret = false;
		}
    	else if (sEmail != null && sEmail.indexOf(' ') >= 0) {
    		errEmail.setText("No space allowed!");
    		errEmail.setVisibility(View.VISIBLE);
    		ret = false;
    	}
    	else {
    		errEmail.setVisibility(View.GONE);
    		ret = true;
		}
    	
		return ret;
	}
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		
		theApp = (ChatApplication)getApplication();

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
		    profile = extras.getInt("profile");
		    
		    if (profile == PROFILE_HOST) {
		    	theContact = theApp.contactFrom;
		    }
		    else if (profile == PROFILE_CONTACTLIST) {
			    int contactId = extras.getInt("contactId");
			    theContact = theApp.contactList.get(contactId);
		    }
		    else if (profile == PROFILE_CONTACTRESULT) {
			    int contactId = extras.getInt("contactId");
			    theContact = theApp.searchResult.get(contactId);
		    }
		}

	    //theContact = (Contact)getIntent().getSerializableExtra("contact");
	    
		ivPicture = (ImageView)findViewById(R.id.ivPicture);
		etNickName = (EditText)findViewById(R.id.etNickName);
		etEmail = (EditText)findViewById(R.id.etEmail);
		rgGender = (RadioGroup)findViewById(R.id.rgGender);

    	errNickName = (TextView)findViewById(R.id.errNickName);  
    	errEmail = (TextView)findViewById(R.id.errEmail);  
    	
    	errNickName.setVisibility(View.GONE);
    	errEmail.setVisibility(View.GONE);
    	
		etNickName.setText(theContact.nickName);
		etEmail.setText(theContact.email);
		int gender = theContact.gender;
		if (gender == 0) {
			rgGender.check(R.id.rbMale);
		}
		else {
			rgGender.check(R.id.rbFemale);
		}
		
		if (theContact.bmpPicture == null) {
			ivPicture.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.profile));
		}
		else {
			ivPicture.setImageBitmap(theContact.bmpPicture);
		}
		
		btnDone = (Button)findViewById(R.id.btnDone);
		btnAdd = (Button)findViewById(R.id.btnAdd);
		btnDelete = (Button)findViewById(R.id.btnDelete);
/*		
		switch (profile) {
			case PROFILE_HOST: {
				btnDone.setVisibility(View.VISIBLE);
				btnAdd.setVisibility(View.GONE);
				btnDelete.setVisibility(View.GONE);
				break;
			}
			
			case PROFILE_CONTACTLIST: {
				btnDone.setVisibility(View.GONE);
				btnAdd.setVisibility(View.GONE);
				btnDelete.setVisibility(View.VISIBLE);
				break;
			}
			
			case PROFILE_CONTACTRESULT: {
				btnDone.setVisibility(View.GONE);
				btnAdd.setVisibility(View.VISIBLE);
				btnDelete.setVisibility(View.GONE);
				break;
			}
		}
*/
		if (theContact.id == theApp.contactFrom.id) {
			btnDone.setVisibility(View.VISIBLE);
			btnAdd.setVisibility(View.GONE);
			btnDelete.setVisibility(View.GONE);
		}
		else if (theContact.isTemp == false) {
			btnDone.setVisibility(View.GONE);
			btnAdd.setVisibility(View.GONE);
			btnDelete.setVisibility(View.VISIBLE);
		}
		else {
			btnDone.setVisibility(View.GONE);
			btnAdd.setVisibility(View.VISIBLE);
			btnDelete.setVisibility(View.GONE);
		}
		
		etNickName.setOnFocusChangeListener(new OnFocusChangeListener() {
    		@Override  
    	    public void onFocusChange(View v, boolean hasFocus) {
    	    	if (v.hasFocus() == false) {
    	            if (v.getId() == R.id.etNickName) {
    	            	checkNickName();
    	            }
    	    	}  
    		}  
    	});
    	
		etEmail.setOnFocusChangeListener(new OnFocusChangeListener() {  
	         @Override  
	         public void onFocusChange(View v, boolean hasFocus) {
	        	 if (v.hasFocus() == false) {   
	                if (v.getId() == R.id.etEmail) {
	                	checkEmail();
	                }
	        	 }  
	         }  
    	});  
	}

	public void onPicture(View v) {
		filePath = new ContextWrapper(this).getFilesDir().getAbsolutePath();
        fileName = theContact.id + ".jpg";
		tempFile = new File(Environment.getExternalStorageDirectory(), fileName);

		new AlertDialog.Builder(this)
		.setTitle("Profile Picture")
		.setPositiveButton("Take Picture", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				intent.putExtra(MediaStore.EXTRA_OUTPUT,Uri.fromFile(tempFile));
				startActivityForResult(intent, PHOTO_REQUEST_TAKEPHOTO);
			}
		})
		
		.setNegativeButton("Gallery", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				Intent intent = new Intent(Intent.ACTION_PICK, null);
				intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*");
				startActivityForResult(intent, PHOTO_REQUEST_GALLERY);
			}
		}).show();
	}
	
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case PHOTO_REQUEST_TAKEPHOTO:
            startPhotoZoom(Uri.fromFile(tempFile), 100);
            break;

        case PHOTO_REQUEST_GALLERY:
            if (data != null)
                startPhotoZoom(data.getData(), 100);
            break;

        case PHOTO_REQUEST_CUT:
            if (data != null) 
                setPicToView(data);
            break;
        }
        super.onActivityResult(requestCode, resultCode, data);

    }
    
    private void startPhotoZoom(Uri uri, int size) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");

        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);

        intent.putExtra("outputX", size);
        intent.putExtra("outputY", size);
        intent.putExtra("return-data", true);

        startActivityForResult(intent, PHOTO_REQUEST_CUT);
    }

    private void setPicToView(Intent picdata) {
        Bundle bundle = picdata.getExtras();
        if (bundle != null) {
        	newBitmap = bundle.getParcelable("data");
            ivPicture.setImageBitmap(newBitmap);
            tempFile.delete();
        }
    }    
    
	public void onDone(View v) {
		boolean valNickName = checkNickName();
		boolean valEmail = checkEmail();
		if (!valNickName || !valEmail) {
			return;
		}
	
		String nickName = etNickName.getText().toString();
		String email = etEmail.getText().toString();
		int gender = 0;
		
		if (rgGender.getCheckedRadioButtonId() == R.id.rbMale) {
			gender = 0;
		}
		else {
			gender = 1;
		}
		
		if (newBitmap != null) {
			File file = new File(filePath + "/" + fileName);
			try  {
				file.createNewFile();
			    FileOutputStream ostream = new FileOutputStream(file);
			    newBitmap.compress(CompressFormat.JPEG, 100, ostream);
			    ostream.close();
			}
			catch (Exception e) {
			    e.printStackTrace();
			}
		}
		
		theApp.contactFrom.nickName = nickName;
		theApp.contactFrom.email = email;
		theApp.contactFrom.gender = gender;
		if (newBitmap != null) {
			theApp.contactFrom.bmpPicture = newBitmap;
		}	
		
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().serializeNulls().create();
    	Command command = new Command();
		command.cmd = Command.UPDATEPROFILE;
		command.value = gson.toJson(theApp.contactFrom).getBytes();

    	Message msg = new Message();
		msg.what = ChatApplication.SEND_COMMAND;
		msg.obj = command.encode();
		theApp.sendMessage(msg);
    	    	
		if (newBitmap != null) {
    		ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    	newBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
	    	byte[] data = baos.toByteArray();

	    	Command command1 = new Command();
	    	command1.cmd = Command.UPDATEPICTURE;
	    	command1.arg1 = theContact.id;
	    	command1.value = data;

        	Message msg1 = new Message();
        	msg1.what = ChatApplication.SEND_COMMAND;
        	msg1.obj = command1.encode();
			theApp.sendMessage(msg1);
		}
		
		finish();
	}
	
	public void onDelete(View v) {
    	//DbHelper db = new DbHelper(theApp);
    	//db.deleteContact(theContact);
    	theApp.contactList.remove(theContact.id);
		
		if (theContact.bmpPicture != null) {
			filePath = new ContextWrapper(this).getFilesDir().getAbsolutePath();
	        fileName = theContact.id + ".jpg";

	        File file = new File(filePath + "/" + fileName);
			try  {
				file.delete();
			} 
			catch (Exception e) {
			    e.printStackTrace();
			}
		}
		
    	Command command = new Command();
    	command.cmd = Command.DELETECONTACT;
    	command.arg1 = theApp.contactFrom.id;
    	command.arg2 = theContact.id;

    	Message msg = new Message();
    	msg.what = ChatApplication.SEND_COMMAND;
    	msg.obj = command.encode();
		theApp.sendMessage(msg);
		
		finish();
	}
	
	public void onAdd(View v) {
    	//DbHelper db = new DbHelper(theApp);
    	//long ret = db.addContact(theContact, theApp.contactFrom.id);
    	//if (ret != -1) {
    		//theApp.contactList.put(theContact.id, theContact);
    	//}
    	
    	theApp.contactList.put(theContact.id, theContact);
    	
		if (theContact.bmpPicture != null) {
			filePath = new ContextWrapper(this).getFilesDir().getAbsolutePath();
	        fileName = theContact.id + ".jpg";

	        File file = new File(filePath + "/" + fileName);
			try  {
				file.createNewFile();
			    FileOutputStream ostream = new FileOutputStream(file);
			    theContact.bmpPicture.compress(CompressFormat.JPEG, 100, ostream);
			    ostream.close();
			} 
			catch (Exception e) {
			    e.printStackTrace();
			}
		}
		
		Command command = new Command();
    	command.cmd = Command.ADDCONTACT;
    	command.arg1 = theApp.contactFrom.id;
    	command.arg2 = theContact.id;

    	Message msg = new Message();
    	msg.what = ChatApplication.SEND_COMMAND;
    	msg.obj = command.encode();
		theApp.sendMessage(msg);
		
		finish();
	}
	
    public void onCancel(View v) {
    	finish();
    }    
}
