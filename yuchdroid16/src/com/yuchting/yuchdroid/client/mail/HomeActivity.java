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
package com.yuchting.yuchdroid.client.mail;

import android.app.ListActivity;
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
import android.widget.AbsListView;
import android.widget.ListView;

import com.yuchting.yuchdroid.client.R;
import com.yuchting.yuchdroid.client.YuchDroidApp;
import com.yuchting.yuchdroid.client.Yuchdroid16Activity;


public class HomeActivity extends ListActivity{
			
	public static final int	MAX_GROUP_FATCH_NUM		= 35;

	public Cursor			m_groupCursor;
	public int 			m_currGroupLimit = MAX_GROUP_FATCH_NUM;
		
	private YuchDroidApp	m_mainApp;
	private MailListAdapter m_mailListAd;
			
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
		
		if(m_groupCursor != null){
			m_groupCursor.close();
		}
		
		m_groupCursor = m_mainApp.m_dba.fetchAllGroup(m_currGroupLimit);
		refershTitle();
		
		m_mailListAd.notifyDataSetChanged();
	}
				
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        m_mainApp 			= (YuchDroidApp)getApplicationContext();
        
        if(!m_mainApp.m_connectDeamonRun){
        	// if the connect daemon is NOT run
        	// start the YuchsBox connect daemon activity
        	//
        	startActivity(new Intent(this,Yuchdroid16Activity.class));
        }
              
        setContentView(R.layout.home);
        m_groupCursor 		= m_mainApp.m_dba.fetchAllGroup(m_currGroupLimit);
        refershTitle();
        
        m_mailListAd		= new MailListAdapter(this);
        getListView().setAdapter(m_mailListAd);
        
        getListView().setOnScrollListener(new AbsListView.OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState){}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount){
				if(m_groupCursor.getCount() >= m_currGroupLimit 
					&& (float)(firstVisibleItem + visibleItemCount)/ (float)totalItemCount > 0.85f){
					
					m_currGroupLimit += MAX_GROUP_FATCH_NUM;
					refreshGroupCursor();
				}
			}
		});
        		
        registerReceiver(m_recvMailRecv, new IntentFilter(YuchDroidApp.FILTER_RECV_MAIL));
        registerReceiver(m_markReadRecv, new IntentFilter(YuchDroidApp.FILTER_MARK_MAIL_READ));
        registerReceiver(m_sendMailRecv, new IntentFilter(YuchDroidApp.FILTER_MAIL_GROUP_FLAG));
    }
	
	public void onResume(){
		super.onResume();
		
		m_mainApp.StopMailNotification();
		m_mainApp.stopMailFailedNotification();
	}
		
	public void onDestroy(){
		super.onDestroy();
		
		if(m_groupCursor != null){
			m_groupCursor.close();
		}
				
		unregisterReceiver(m_recvMailRecv);
		unregisterReceiver(m_markReadRecv);
		unregisterReceiver(m_sendMailRecv);
		
		m_mainApp.clearHistoryImm();
	}
	
	private void refershTitle(){
		int t_unread = 0;
		
		while(m_groupCursor.moveToNext()){
			int t_groupFlag = m_groupCursor.getInt(m_groupCursor.getColumnIndex(MailDbAdapter.GROUP_ATTR_GROUP_FLAG));
			if(t_groupFlag == fetchMail.GROUP_FLAG_RECV
			|| t_groupFlag == fetchMail.GROUP_FLAG_RECV_ATTACH){
				t_unread++;
			}
		}
		
		setTitle(getString(R.string.title_mail) + " (" + t_unread + ")");
		
	}
	
	@Override
    protected void onListItemClick(ListView l, View v, int position, long id){
		
		MailListAdapter.ItemHolder t_holder = (MailListAdapter.ItemHolder)v.getTag();
		
		if(t_holder != null){
			Intent in = new Intent(this,MailOpenActivity.class);
			in.putExtra(MailOpenActivity.INTENT_CURRENT_MAIL_GROUP, t_holder.groupId);
			in.putExtra(MailOpenActivity.INTENT_PRE_MAIL_GROUP_INDEX, t_holder.preGroupId);
			in.putExtra(MailOpenActivity.INTENT_NEXT_MAIL_GROUP_INDEX, t_holder.nextGroupId);
			in.putExtra(MailOpenActivity.INTENT_NEXT_MAIL_CURSOR_POS,t_holder.cursorPos);
			in.putExtra(MailOpenActivity.INTENT_CURRENT_GROUP_LIMIT,m_currGroupLimit);
			
			startActivity(in);
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
                break;
            case R.id.mail_list_preference:
            	startActivity(new Intent(this,MailPrefActivity.class));
            	break;
            case R.id.mail_list_connect_state:
            	startActivity(new Intent(this,Yuchdroid16Activity.class));
            	break;
           
        }

        return super.onMenuItemSelected(featureId, item);
	}

//	public boolean onTrackballEvent(MotionEvent event){
//	View v = m_mailListView.getFocusedChild();
//	if(v != null){
//		v.invalidate();
//	}		
//	return super.onTrackballEvent(event);
//}

}
