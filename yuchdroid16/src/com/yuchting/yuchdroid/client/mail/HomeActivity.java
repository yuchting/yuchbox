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
import android.view.View;
import android.widget.RelativeLayout;

import com.yuchting.yuchdroid.client.R;
import com.yuchting.yuchdroid.client.YuchDroidApp;

public class HomeActivity extends Activity {
	
	public final static int	STATUS_BAR_MAIL		= 0;
	public final static int	STATUS_BAR_WEIBO	= 1;
	public final static int	STATUS_BAR_IM		= 2;
	
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
                                
        // initialize module view
        //
        initModuleView();
                
        registerReceiver(m_recvMailRecv, new IntentFilter(YuchDroidApp.FILTER_RECV_MAIL));
        registerReceiver(m_markReadRecv, new IntentFilter(YuchDroidApp.FILTER_MARK_MAIL_READ));
        registerReceiver(m_sendMailRecv, new IntentFilter(YuchDroidApp.FILTER_SEND_MAIL_VIEW));
    }
	
	public void onResume(){
		super.onResume();
		
		m_mainApp.StopMailNotification();
	}
	
	public void onDestroy(){
		super.onDestroy();
		
		m_groupCursor.close();
		
		unregisterReceiver(m_recvMailRecv);
		unregisterReceiver(m_markReadRecv);
		unregisterReceiver(m_sendMailRecv);
	}
		
	private void initModuleView(){
		m_mailListView = new MailListView(this,m_mainApp);
		
		// set the status bar select
		//
		int status = 0;
		Intent t_intent = getIntent();		
		if(t_intent != null && t_intent.getExtras() != null){
			t_intent.getExtras().getInt("status",0);
		}
	
		((RelativeLayout)findViewById(R.id.home_activity)).addView(m_mailListView,0);
		m_mailListView.setVisibility(View.GONE);
		
		setMainListView(status);
	}
	
	private void setMainListView(int _status){
		switch (_status) {
		case STATUS_BAR_MAIL:			
			m_mailListView.setVisibility(View.VISIBLE);
			break;
		case STATUS_BAR_WEIBO:
			break;
		case STATUS_BAR_IM:
			break;
		default:
			break;
		}
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
