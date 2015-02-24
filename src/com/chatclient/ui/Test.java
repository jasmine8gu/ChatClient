package com.chatclient.ui;

import com.chatclient.R;

import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class Test extends Activity {
	private static final String TAG = "AIDLDemo";
	IAdditionService service;
	AdditionServiceConnection connection;
	  
	  class AdditionServiceConnection implements ServiceConnection {

		    public void onServiceConnected(ComponentName name, IBinder boundService) {
		      service = IAdditionService.Stub.asInterface((IBinder) boundService);
		      Log.d(TAG, "onServiceConnected() connected");
		      Toast.makeText(Test.this, "Service connected", Toast.LENGTH_LONG)
		          .show();
		    }

		    public void onServiceDisconnected(ComponentName name) {
		      service = null;
		      Log.d(TAG, "onServiceDisconnected() disconnected");
		      Toast.makeText(Test.this, "Service connected", Toast.LENGTH_LONG)
		          .show();
		    }
		  }

private void initService() {
    connection = new AdditionServiceConnection();
    Intent i = new Intent(this, AdditionService.class);
    //i.setClassName("com.chatclient.ui", AdditionService.class.getName());
    boolean ret = getApplicationContext().bindService(i, connection, Context.BIND_AUTO_CREATE);
    Log.d(TAG, "initService() bound with " + ret);
  }

  /** Unbinds this activity from the service. */
  private void releaseService() {
    unbindService(connection);
    connection = null;
    Log.d(TAG, "releaseService() unbound.");
  }

@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test);
        
		//Intent j = new Intent(this, AdditionService.class);
        //startService(j);

        initService();

	    // Setup the UI
	    Button buttonCalc = (Button) findViewById(R.id.button1);

	    buttonCalc.setOnClickListener(new OnClickListener() {
	      TextView result = (TextView) findViewById(R.id.textView1);

	      public void onClick(View v) {
	        int v1, v2, res = -1;
	        v1 = 10;
	        v2 = 20;

	        try {
	          res = service.add(v1, v2);
	        } catch (RemoteException e) {
	          Log.d(Test.TAG, "onClick failed with: " + e);
	          e.printStackTrace();
	        }
	        result.setText(new Integer(res).toString());
	      }
	    });	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.test, menu);
		return true;
	}
	 @Override
	  protected void onDestroy() {
	    releaseService();
	  }

}
