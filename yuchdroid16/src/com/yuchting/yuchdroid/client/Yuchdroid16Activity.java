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

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Yuchdroid16Activity extends Activity {
	
	public final static String TAG = "Yuchdroid16Activity";
	
	public final static String LOGON_SYNC_OK = TAG + "_sync_ok";
	
	public final static String LOGON_SYNC_OK_HOST = LOGON_SYNC_OK + "_host";
	public final static String LOGON_SYNC_OK_PORT = LOGON_SYNC_OK + "_port";
	public final static String LOGON_SYNC_OK_PASS = LOGON_SYNC_OK + "_pass";
		
	EditText	m_host	= null;
	EditText	m_port	= null;
	EditText	m_userPass = null;
	Button		m_connectBut = null;
	
	TextView	m_connectStateView = null;
	
	YuchDroidApp m_mainApp;
	
	ConfigInit m_config;
	
	
    BroadcastReceiver	m_intentRecv = new BroadcastReceiver(){
		public void onReceive(Context context, Intent intent){
			setConnectState(m_mainApp.m_connectState);
		}
	};
	
	BroadcastReceiver m_syncOKRecv = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			String t_host	= intent.getStringExtra(LOGON_SYNC_OK_HOST);
			int t_port		= intent.getIntExtra(LOGON_SYNC_OK_PORT, 0);
			String t_pass	= intent.getStringExtra(LOGON_SYNC_OK_PASS);
			String t_portStr = Integer.toString(t_port);
			
			if(validateHost(t_host,Integer.toString(t_port),t_pass)){
				
				m_mainApp.m_isOfficalHost = true;
				
				if(m_mainApp.m_config.m_host == null 
				|| m_mainApp.m_config.m_host.length() == 0){	
					
					Toast.makeText(Yuchdroid16Activity.this,getString(R.string.yuch_logon_sync_ok_prompt), Toast.LENGTH_LONG).show();
					
				}else if(t_host.equalsIgnoreCase(m_config.m_host) 
				&& t_portStr.equalsIgnoreCase(Integer.toString(m_config.m_port)) 
				&& t_pass.equalsIgnoreCase(m_config.m_userPass) 
				&& ConnectDeamon.isConnected()){
					
					return;
				}
				
				if(ConnectDeamon.isConnected()){
					ConnectDeamon.Disconnect();
				}
				
				m_host.setText(t_host);
				m_port.setText(t_portStr);
				m_userPass.setText(t_pass);
				
				clickConnectBtn();
			}
		}
	};
		
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        
        m_mainApp = (YuchDroidApp)getApplicationContext();
        m_config = m_mainApp.m_config;
        
        initLoginLayout();
        
        registerReceiver(m_intentRecv, new IntentFilter(YuchDroidApp.FILTER_CONNECT_STATE));
        registerReceiver(m_syncOKRecv, new IntentFilter(LOGON_SYNC_OK));
        
        if(m_config.m_host == null || m_config.m_host.length() == 0){
        	startLogonActivity();
        }
    }
        
    private void initLoginLayout() { 
        
    	m_connectBut = (Button) findViewById(R.id.login_start_svr);
    	m_connectBut.setOnClickListener(new OnClickListener() {  
            public void onClick(View arg0){
            	clickConnectBtn();
            	m_mainApp.checkOfficalHost();
            }
        });
        
        m_connectStateView = (TextView)findViewById(R.id.login_connect_text);
        
        m_host = (EditText)findViewById(R.id.login_host);
        m_port = (EditText)findViewById(R.id.login_port);
        m_userPass = (EditText)findViewById(R.id.login_user_pass);
        
        m_host.setText(m_config.m_host);
        m_port.setText(m_config.m_port == 0?"":Integer.toString(m_config.m_port));
        m_userPass.setText(m_config.m_userPass); 
                
        setConnectState(m_mainApp.m_connectState);   
    }
    
    private void clickConnectBtn(){
    	
    	boolean t_readConfig = false;
    	
    	String t_host = m_host.getText().toString();
    	String t_port = m_port.getText().toString();
    	String t_pass = m_userPass.getText().toString();        	
    	
    	if(validateHost(t_host,t_port,t_pass)){	
    		
        	if(!t_host.equals(m_mainApp.m_config.m_host) 
        	|| !t_port.equals(m_mainApp.m_config.m_port) 
        	|| !t_pass.equals(m_mainApp.m_config.m_userPass)){
        		t_readConfig = true;
        		
        		m_config.m_host = t_host;
        		m_config.m_port = Integer.valueOf(t_port).intValue();
        		m_config.m_userPass = t_pass;
        		
        		m_config.WriteReadIni(false);
        		
        	}            		
    	}
    	
    	if(!m_mainApp.m_connectDeamonRun){
    		startConnectDeamon(t_readConfig);                	
        }else{
        	if(m_mainApp.m_connectState == YuchDroidApp.STATE_DISCONNECT){
        		ConnectDeamon.Connect();
        	}else{
        		ConnectDeamon.Disconnect();                		
        	}
        }
    }
    
    private void startLogonActivity(){
    	Intent in = new Intent(this,YuchLogonActivity.class);
    	startActivity(in);
    }
    
    private boolean validateHost(String _host,String _port,String _userPass){
    	
    	if(_host == null || _port == null || _userPass == null){
    		return false;
    	}
    	
    	if(!_host.matches("([\\w-]+\\.)+[\\w-]+(/[\\w- ./?%%&=]*)?") 
    	&& !_host.matches("/(\\d+)\\.(\\d+)\\.(\\d+)\\.(\\d+)/g")){
    		Toast.makeText(this, R.string.login_host_error_prompt, Toast.LENGTH_SHORT).show();
    		return false;
    	}
    	
    	if(_port.length() == 0 || Integer.valueOf(_port).intValue() > 65535){
    		Toast.makeText(this, R.string.login_port_error_prompt, Toast.LENGTH_SHORT).show();
    		return false;
    	}
    	
    	if(_userPass.length() == 0){
    		Toast.makeText(this, R.string.login_user_pass_error_prompt, Toast.LENGTH_SHORT).show();
    		return false;
    	}
    	
    	return true;
    }
    
    private void startConnectDeamon(boolean _readConfig){
    	Intent intent = new Intent(this,ConnectDeamon.class);    	
        startService(intent);
    }  
  
    private void stopConnectDeamon(){  
    	Intent intent = new Intent(this,ConnectDeamon.class);
        stopService(intent);
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        
        Log.i(TAG,"onStart " + this);
    }
    

	
	private synchronized void setConnectState(int _state){
		switch(_state){
		case YuchDroidApp.STATE_CONNECTED:
			m_connectStateView.setText(getString(R.string.login_state_connected));
			m_connectBut.setText(getString(R.string.stop_svr_but));
			break;
		case YuchDroidApp.STATE_CONNECTING:
			m_connectStateView.setText(getString(R.string.login_state_connecting));
			m_connectBut.setText(getString(R.string.stop_svr_but));
			break;
		case YuchDroidApp.STATE_DISCONNECT:
			m_connectStateView.setText(getString(R.string.login_state_disconnect));
			m_connectBut.setText(getString(R.string.start_svr_but));
			break;
		}
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.login_menu,menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
            case R.id.login_menu_debug_info:
            	startActivity(new Intent(this,DebugInfoActivity.class));
                return true;
            case R.id.login_menu_setting:
            	startActivity(new Intent(this,ConnectPrefActivity.class));
            	return true;
            case R.id.login_menu_about:
            	Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://code.google.com/p/yuchberry/wiki/Thanks_sheet"));
            	startActivity(browserIntent);
            	return true;
            case R.id.login_menu_account:
            	startLogonActivity();
            	return true;
            
        }

        return super.onMenuItemSelected(featureId, item);
    }
	
    @Override
    protected void onResume() {
        super.onResume();
        
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
        
        //Debug.stopMethodTracing();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // The activity is about to be destroyed.
        
        Log.i(TAG,"onDestroy " + this);
        
        unregisterReceiver(m_intentRecv);
        unregisterReceiver(m_syncOKRecv);
    }
 
}