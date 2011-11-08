package com.yuchting.yuchdroid.client.mail;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.yuchting.yuchdroid.client.R;
import com.yuchting.yuchdroid.client.YuchDroidApp;

public class HomeActivity extends Activity {
		
	private YuchDroidApp	m_mainApp;
	private MailListView	m_mailListView;
	
	public Cursor			m_groupCursor;
		
	BroadcastReceiver m_recvMailRecv = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			refreshGroupCursor();
		}
	};
	BroadcastReceiver m_markReadRecv = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context paramContext, Intent paramIntent) {
			refreshGroupCursor();
		}
	};
	
	BroadcastReceiver m_sendMailRecv = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			refreshGroupCursor();
		}
	};
	
	private void refreshGroupCursor(){
		m_groupCursor.close();
		m_groupCursor = m_mainApp.m_dba.fetchAllGroup();
		m_mailListView.m_mailListAd.notifyDataSetChanged();
	}
				
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
                
        m_mainApp = (YuchDroidApp)getApplicationContext();
        m_groupCursor = m_mainApp.m_dba.fetchAllGroup();
        
        m_mailListView = new MailListView(this,m_mainApp);
		((RelativeLayout)findViewById(R.id.home_activity)).addView(m_mailListView,0);
                
        registerReceiver(m_recvMailRecv, new IntentFilter(YuchDroidApp.FILTER_RECV_MAIL));
        registerReceiver(m_markReadRecv, new IntentFilter(YuchDroidApp.FILTER_MARK_MAIL_READ));
        registerReceiver(m_sendMailRecv, new IntentFilter(YuchDroidApp.FILTER_MAIL_GROUP_FLAG));
    }
	
	public void onResume(){
		super.onResume();
		
		m_mainApp.StopMailNotification();
	}
	
//	public boolean onTrackballEvent(MotionEvent event){
//		View v = m_mailListView.getFocusedChild();
//		if(v != null){
//			v.invalidate();
//		}		
//		return super.onTrackballEvent(event);
//	}
	
	public void onDestroy(){
		super.onDestroy();
		
		m_groupCursor.close();
		
		unregisterReceiver(m_recvMailRecv);
		unregisterReceiver(m_markReadRecv);
		unregisterReceiver(m_sendMailRecv);
	}
		
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mail_list_menu,menu);
        return true;
    }
	
	@Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
            case R.id.mail_list_compose_new_mail:
            	Intent in = new Intent(this,MailComposeActivity.class);
            	in.putExtra(MailComposeActivity.COMPOSE_MAIL_STYLE, fetchMail.NOTHING_STYLE);
            	m_mainApp.m_composeRefMail = null;
            	
            	startActivity(in);
                return true;
           
        }

        return super.onMenuItemSelected(featureId, item);
	}
}
