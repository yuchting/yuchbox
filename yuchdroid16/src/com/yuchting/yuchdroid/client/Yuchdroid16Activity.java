package com.yuchting.yuchdroid.client;

import java.util.Date;
import java.util.Random;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class Yuchdroid16Activity extends Activity {
	
	public final static String TAG = "Yuchdroid16Activity";
	
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
               
        ConnectDeamon.fsm_clientVersion = getVersionName(this,Yuchdroid16Activity.class);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);       
        
        initButtons();
    }
    
    public static String getVersionName(Context context, Class cls){
		try{
			ComponentName comp = new ComponentName(context, cls);
			PackageInfo pinfo = context.getPackageManager().getPackageInfo(comp.getPackageName(), 0);
			return pinfo.versionName;
		}catch(android.content.pm.PackageManager.NameNotFoundException e) {
			return "1.0";
		}
	}	
    
    private void initButtons() { 
    	
        Button buttonStart = (Button) findViewById(R.id.start_svr);
        buttonStart.setOnClickListener(new OnClickListener() {  
            public void onClick(View arg0) {  
            	MailListActivity.show(Yuchdroid16Activity.this);
            }  
        });  
  
        Button buttonStop = (Button) findViewById(R.id.stop_svr);  
        buttonStop.setOnClickListener(new OnClickListener() {  
            public void onClick(View arg0) {
            	// add a test mail
            	//
            	Random t_rand = new Random();
            	fetchMail t_newMail = new fetchMail();
            	t_newMail.SetContain("This is a test mail + "+t_rand.nextInt());
            	t_newMail.SetSubject("Subject " + t_rand.nextInt());
            	t_newMail.SetFromVect(new String[]{"From@gmail.com"});
            	t_newMail.SetSendToVect(new String[]{"SendTo@gmail.com"});
            	t_newMail.SetSendDate(new Date());
            	
            	MailDbAdapter t_ad = new MailDbAdapter(Yuchdroid16Activity.this);
            	t_ad.open();
            	t_ad.createMail(t_newMail,null);
            	
            	Toast.makeText(getApplicationContext(), t_newMail.GetSubject() + " add OK!",
            	          Toast.LENGTH_SHORT).show();
            }  
        });  
   
    }
    
    private void startService() {  
    	Intent intent = new Intent(this,ConnectDeamon.class);
        startService(intent);
    }  
  
    private void stopService() {  
    	Intent intent = new Intent(this,ConnectDeamon.class);
        stopService(intent);
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        
        Log.i(TAG,"onStart " + this);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // The activity has become visible (it is now "resumed").
        Log.i(TAG,"onResume " + this);
    }
    @Override
    protected void onPause() {
        super.onPause();
        // Another activity is taking focus (this activity is about to be "paused").
        Log.i(TAG,"onPause " + this);
    }
    @Override
    protected void onStop() {
        super.onStop();
        // The activity is no longer visible (it is now "stopped")
        
        Log.i(TAG,"onStop " + this);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // The activity is about to be destroyed.
        
        Log.i(TAG,"onDestroy " + this);
    }
}