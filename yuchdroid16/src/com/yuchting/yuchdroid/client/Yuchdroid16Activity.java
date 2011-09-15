package com.yuchting.yuchdroid.client;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class Yuchdroid16Activity extends Activity {
	
	public final static String TAG = "Yuchdroid16Activity";
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.i(TAG,"onCreate " + this);
        
        setContentView(R.layout.main);
        
        initButtons();
    }
    
    private void initButtons() { 

    	
        Button buttonStart = (Button) findViewById(R.id.start_svr);  
        buttonStart.setOnClickListener(new OnClickListener() {  
            public void onClick(View arg0) {  
                startService();  
            }  
        });  
  
        Button buttonStop = (Button) findViewById(R.id.stop_svr);  
        buttonStop.setOnClickListener(new OnClickListener() {  
            public void onClick(View arg0) {
                stopService();  
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