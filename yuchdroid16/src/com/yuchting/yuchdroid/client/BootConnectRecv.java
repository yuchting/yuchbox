/**
 *  Dear developer:
 *  
 *   If you want to modify this file of project and re-publish this please visit:
 *  
 *     http://code.google.com/p/yuchberry/wiki/Project_files_header
 *     
 *   to check your responsibility and my humble proposal. Thanks!
 *   
 *  -- 
 *  Yuchs' Developer    
 *  
 *  
 *  
 *  
 *  尊敬的开发者：
 *   
 *    如果你想要修改这个项目中的文件，同时重新发布项目程序，请访问一下：
 *    
 *      http://code.google.com/p/yuchberry/wiki/Project_files_header
 *      
 *    了解你的责任，还有我卑微的建议。 谢谢！
 *   
 *  -- 
 *  语盒开发者
 *  
 */
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
