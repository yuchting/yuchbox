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
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class YuchLogonActivity extends Activity {

	public class JavaScriptInterface {
	    Context m_ctx;

	    /** Instantiate the interface and set the context */
	    JavaScriptInterface(Context c) {
	    	m_ctx = c;
	    }

	    public void syncSucc(String _host,int _port,String _pass) {
	        Toast.makeText(YuchLogonActivity.this, _host + " " + _port + " " + _pass, Toast.LENGTH_LONG).show();
	    }
	}
	
	final static String LOAD_WEB_URL = "http://192.168.2.228:8888/Android.html";
	//final static String LOAD_WEB_URL = "http://http://www.yuchs.com/Android.html";
	
	WebView			m_mainWeb;
	
	ProgressBar		m_loadProgress;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.yuch_logon);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.yuch_logon_title);
        
        m_loadProgress	= (ProgressBar)findViewById(R.id.logon_title_progress);
        m_loadProgress.setMax(100);
        
        m_mainWeb = (WebView)findViewById(R.id.yuch_logon_web);
        m_mainWeb.getSettings().setJavaScriptEnabled(true);
        
        m_mainWeb.loadUrl(LOAD_WEB_URL);
        
        m_mainWeb.setWebChromeClient(new WebChromeClient(){
        	public void onProgressChanged(WebView view, int progress){
        		m_loadProgress.setProgress(progress);
        		if(progress == 100){
        			m_loadProgress.setVisibility(View.GONE);
        		}
        	}
        });
       
        m_mainWeb.addJavascriptInterface(new JavaScriptInterface(this), "YuchDroid");
    } 
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Check if the key event was the BACK key and if there's history
        if (keyCode == KeyEvent.KEYCODE_BACK) {
        	GlobalDialog.showYesNoDialog(getString(R.string.yuch_logon_quit_ask), this, new GlobalDialog.YesNoListener() {
				
				@Override
				public void click() {
					finish();					
				}
			},null);
        	
            return true;
        }
        
        // If it wasn't the BACK key or there's no web page history, bubble up to the default
        // system behavior (probably exit the activity)
        return super.onKeyDown(keyCode, event);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.yuch_logon_menu,menu);
        return true;
    }
	
	@Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
            case R.id.mail_list_compose_new_mail:
            	m_loadProgress.setVisibility(View.VISIBLE);
            	m_loadProgress.setProgress(0);
            	m_mainWeb.loadUrl(LOAD_WEB_URL);
                break;           
        }

        return super.onMenuItemSelected(featureId, item);
	}
}
