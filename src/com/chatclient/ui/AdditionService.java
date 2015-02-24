package com.chatclient.ui;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class AdditionService extends Service {

	@Override
	public IBinder onBind(Intent arg0) {
	    	return mBinder;
	}

	private final IAdditionService.Stub mBinder = new IAdditionService.Stub() {
        public int add(int value1, int value2) throws RemoteException {
        	Log.d("jasmine AdditionService", String.format("AdditionService.add(%d, %d)", value1, value2));
        	return value1 + value2;
        }
	};
}
