package com.yuchting.yuchdroid.client;

import java.util.Vector;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Toast;

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
	
	protected void onResume(){
		super.onResume();
		
		registerReceiver(new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context paramContext, Intent paramIntent) {
				fillInfo();				
			}
		}, new IntentFilter(ConnectDeamon.FILTER_DEBUG_INFO));
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
            	ClipboardManager clipboard = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
            	clipboard.setText(m_mainApp.getErrorString());
            	
            	Toast.makeText(this, getString(R.string.debug_info_menu_copy_ok),Toast.LENGTH_SHORT);
            	return true;
            case R.id.debug_info_menu_help:
            	Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://code.google.com/p/yuchberry/wiki/Connect_Error_info"));
            	startActivity(browserIntent);
            	return true;
        }

        return super.onMenuItemSelected(featureId, item);
    }
	
}
