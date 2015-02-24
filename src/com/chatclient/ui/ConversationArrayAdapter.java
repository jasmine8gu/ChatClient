package com.chatclient.ui;

import java.util.ArrayList;

import com.chatclient.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.text.TextPaint;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ConversationArrayAdapter extends ArrayAdapter<Conversation> {
	private ArrayList<Conversation> conversationList;
    private Activity context;
    
    Bitmap bmpProfile = null;
    
    private int bubbleMaxWidth;
    private int textSize = 20;
    
    public ConversationArrayAdapter(Activity c, int resource, int textViewResourceId, ArrayList<Conversation> d) {
    	super(c, resource, textViewResourceId);
    	context = c;
    	
    	bmpProfile = BitmapFactory.decodeResource(context.getResources(), R.drawable.profile);
    	conversationList = d;
    	
    	Display display = context.getWindowManager().getDefaultDisplay();
    	Point size = new Point();
    	display.getSize(size);
    	bubbleMaxWidth = (int)(size.x * 0.5);
    }
    
    public int getCount() {
    	if (conversationList == null) {
    		return 0;
    	}

		return conversationList.size();
    }     
    
    public Conversation getItem(int index) {
    	if (conversationList == null) {
    		return null;
    	}

		return conversationList.get(index);
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        Conversation conversation = getItem(position);
        
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	if (conversation.isHost == true) {
    		rowView = inflater.inflate(R.layout.item_conversation_right, parent, false);
    	}
    	else {
    		rowView = inflater.inflate(R.layout.item_conversation_left, parent, false);
    	}
                
        TextView tvText = (TextView)rowView.findViewById(R.id.right_5);
        ImageView img2 = (ImageView)rowView.findViewById(R.id.right_2);  
        ImageView img4 = (ImageView)rowView.findViewById(R.id.right_4);
        ImageView img6 = (ImageView)rowView.findViewById(R.id.right_6);  
        ImageView img8 = (ImageView)rowView.findViewById(R.id.right_8);  
         
        tvText.setTextSize(textSize);  
        tvText.setText(conversation.text);
         
        TextPaint paint = tvText.getPaint();
        int width = (int)(paint.measureText(conversation.text));
        int height = textSize * 2;
     
        if (width >= bubbleMaxWidth) {
            height = textSize * 2 * ((int)(width / bubbleMaxWidth) + 1);
            width = bubbleMaxWidth;
        }  
        
        img2.getLayoutParams().width = width;  
        img8.getLayoutParams().width = width;  
        img4.getLayoutParams().height = height;
        img6.getLayoutParams().height = height;
        
        LayoutParams lpText = tvText.getLayoutParams();
        tvText.getLayoutParams().width = width;
        tvText.getLayoutParams().height = height;
        tvText.setLayoutParams(lpText);
        
        ImageView ivPicture = (ImageView)rowView.findViewById(R.id.ivPicture);
        if (conversation.contactFrom.bmpPicture == null) {
        	ivPicture.setImageBitmap(bmpProfile);
        }
        else {
        	ivPicture.setImageBitmap(conversation.contactFrom.bmpPicture);
        }
        
        return rowView;
    }
}
