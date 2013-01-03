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

import java.util.Vector;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class DebugInfoActivity extends ListActivity{

	YuchDroidApp	m_mainApp;
	
	ArrayAdapter<String> m_listAdapter;
	
	Vector<String>	m_tmpList = new Vector<String>(); 
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.debug_list);
		
		m_mainApp = (YuchDroidApp)getApplicationContext();
		
		// can't attach the mainApp.m_errorList because:
		// java.lang.IllegalStateException: The content of the adapter has changed but ListView did not receive a notification. 
		// Make sure the content of your adapter is not modified from a background thread, but only from the UI thread.
		//
		m_listAdapter = new ArrayAdapter<String>(this, R.layout.debug_list_item,
				R.id.debug_item_text, m_tmpList);
		
		m_listAdapter.setNotifyOnChange(false);	
		getListView().setAdapter(m_listAdapter);

		fillInfo();
	}
	
	BroadcastReceiver m_debugRecv = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context paramContext, Intent paramIntent) {
			fillInfo();				
		}
	};
	
	protected void onResume(){
		super.onResume();
		
		registerReceiver(m_debugRecv,new IntentFilter(YuchDroidApp.FILTER_DEBUG_INFO));
	}
	
	protected void onDestroy(){
		super.onDestroy();
		
		unregisterReceiver(m_debugRecv);
	}
	
	@Override
	public void onStart(){
		super.onStart();
		YuchDroidApp.onFlurryStart(this);
	}
	
	@Override
	public void onStop(){
		super.onStop();
		YuchDroidApp.onFlurryStop(this);
	}
	
	private void fillInfo(){
		
		// can't attach the mainApp.m_errorList because:
		//
		// java.lang.IllegalStateException: The content of the adapter has changed but ListView did not receive a notification. 
		// Make sure the content of your adapter is not modified from a background thread, but only from the UI thread.
		//
		// copy the error list to the tmp list and notify the adapter to refresh the listIVew
		//
		m_mainApp.copyAllErrorString(m_tmpList);
		
		m_listAdapter.notifyDataSetChanged();
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.debug_info_menu,menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
            case R.id.debug_info_menu_clear:
            	m_mainApp.clearAllErrorString();
            	
                return true;
            case R.id.debug_info_menu_copy:
            	YuchDroidApp.copyTextToClipboard(this, m_mainApp.getErrorString());
            	return true;
            case R.id.debug_info_menu_help:
            	Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://code.google.com/p/yuchberry/wiki/Connect_Error_info"));
            	startActivity(browserIntent);
            	return true;
        }

        return super.onMenuItemSelected(featureId, item);
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	super.onListItemClick(l, v, position, id);

        GlobalDialog.showInfo(((TextView)v).getText().toString(),this); 
    }
	
}
