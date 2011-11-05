package com.yuchting.yuchdroid.client;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootConnectRecv extends BroadcastReceiver {

	final static String	TAG = BootConnectRecv.class.getName();
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(TAG,"call on!");
		
		YuchDroidApp t_mainApp = (YuchDroidApp)context.getApplicationContext();
		
		if(t_mainApp != null
		&& t_mainApp.m_config.m_autoRun
		&& t_mainApp.m_config.m_host.length() != 0 
		&& t_mainApp.m_config.m_port != 0
		&& t_mainApp.m_config.m_userPass.length() != 0){
			
			Log.i(TAG,"auto start!");
			
			intent = new Intent(context,ConnectDeamon.class);    	
	        context.startService(intent);
		}
	}
}
