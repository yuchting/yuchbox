package com.yuchting.yuchdroid.client;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class ConnectDeamon extends Service{
	
	public final static String TAG = "ConnectDeamon";
	
	public void onCreate() {
		Log.d(TAG,"onCreate");
	}
	
	// This is the old onStart method that will be called on the pre-2.0
	// platform.  On 2.0 or later we override onStartCommand() so this
	// method will not be called.
	public void onStart(Intent intent, int startId) {
		Log.d(TAG,"onStart " + this);
	}

	// 2.0 later callback function
	//
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		Log.d(TAG,"onStartCommand");
		
	    // We want this service to continue running until it is explicitly
	    // stopped, so return sticky.
	    return 0;
	}

	public IBinder onBind(Intent intent) {
		return null;
	}
		
	public void onDestroy(){
		Log.d(TAG,"onDestory");	
	}
}
