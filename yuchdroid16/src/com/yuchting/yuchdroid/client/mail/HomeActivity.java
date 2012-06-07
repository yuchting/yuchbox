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

import java.util.concurrent.atomic.AtomicReference;

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
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.ListView;

import com.yuchting.yuchdroid.client.GlobalDialog;
import com.yuchting.yuchdroid.client.R;
import com.yuchting.yuchdroid.client.YuchDroidApp;
import com.yuchting.yuchdroid.client.Yuchdroid16Activity;
import com.yuchting.yuchdroid.client.mail.MailListAdapter.ItemHolder;


public class HomeActivity extends ListActivity implements View.OnTouchListener{
			
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
        
        getListView().setOnTouchListener(this);
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
			m_groupCursor = null;
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
	
	//! touch screen down event x
    float m_touch_x 		= 0;
    
    //! touch screen down event y
    float m_touch_y			= 0;
    
    //! is touch state
    boolean m_touched			= false;
    
    //! mail item touch capture
    boolean m_touchCapture 	= false;
    
    MailListAdapter.ItemHolder	m_captureItem	= null;
    
    //! delete group id for bufferring
    long	m_deleteGroupId		= 0;
    
    AccelerateDecelerateInterpolator m_interpolator	= new AccelerateDecelerateInterpolator();
    
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		
		switch(event.getAction()){
		case MotionEvent.ACTION_DOWN:
			
			if(!(v instanceof ListView)){
				m_touch_x 		= event.getX();
				m_touch_y		= event.getY();
				m_touched 		= true;
				m_touchCapture	= false;
				
				m_captureItem = (MailListAdapter.ItemHolder)v.getTag();
			}
			
			break;
		case MotionEvent.ACTION_MOVE:
			
			if(m_touched && m_captureItem != null){
				
				float t_curr_x = event.getX();
				float t_curr_y = event.getY();
				
				float t_delta_x = t_curr_x - m_touch_x;
				
				if(!m_touchCapture){
										
					float t_delta_y = t_curr_y - m_touch_y;
					
					if(Math.abs(t_delta_x) < Math.abs(t_delta_y)){
						break;
					}
					
					m_touchCapture = true;
					return true;				
					
				}else{
					
					// capture view state
					if(t_delta_x < 0){
						
						t_delta_x = -t_delta_x;
						
						if(t_delta_x > ItemHolder.MAX_DELETE_PROMPT_WIDTH){
							t_delta_x = ItemHolder.MAX_DELETE_PROMPT_WIDTH;
						}else{
							t_delta_x = m_interpolator.getInterpolation(t_delta_x / ItemHolder.MAX_DELETE_PROMPT_WIDTH) * t_delta_x;
						}
						
						m_captureItem.deletePrompt.getLayoutParams().width = (int)t_delta_x;
						m_captureItem.deletePrompt.requestLayout();
					
					}else{
						
						if(t_delta_x > ItemHolder.MAX_READ_PROMPT_WIDTH){
							t_delta_x = ItemHolder.MAX_READ_PROMPT_WIDTH;
						}else{
							t_delta_x = m_interpolator.getInterpolation(t_delta_x / ItemHolder.MAX_READ_PROMPT_WIDTH) * t_delta_x;
						}
						
						m_captureItem.readPrompt.getLayoutParams().width = (int)t_delta_x;
						m_captureItem.readPrompt.requestLayout();
					}
					
					return true;
				}
			}			
			
			break;
			
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_OUTSIDE:
		case MotionEvent.ACTION_CANCEL:
									
			if(m_touchCapture){
				if(m_captureItem.readPrompt.getLayoutParams().width >= ItemHolder.MAX_READ_PROMPT_WIDTH){
					// mark the mail group read
					//
					markMailGroupRead(m_captureItem.groupId);
					
				}else if(m_captureItem.deletePrompt.getLayoutParams().width >= ItemHolder.MAX_DELETE_PROMPT_WIDTH){
					// delete the mail group
					//
					if(m_mainApp.m_config.m_forceDeleteMail){
						delMailGroup(m_captureItem.groupId);
					}else{
						
						m_deleteGroupId = m_captureItem.groupId;
						
						GlobalDialog.showYesNoDialog(getString(R.string.mail_open_delete_prompt), this, 
						new GlobalDialog.YesNoListener() {
							
							@Override
							public void click() {
								
								delMailGroup(m_deleteGroupId);
							}
						},null);
					}
				}
				m_captureItem.deletePrompt.getLayoutParams().width = 0;
				m_captureItem.readPrompt.getLayoutParams().width = 0;
				m_captureItem.background.requestLayout();
			}			

			m_touched 		= false;
			m_captureItem 	=  null;
			
			if(m_touchCapture){
				m_touchCapture = false;
				return true;
			}
			
			break;
		}
		
		return false;
	}
	
	/**
	 * mark mail group read
	 */
	private void markMailGroupRead(long _groupId){
		
		Cursor t_mailCursor		= m_mainApp.m_dba.fetchGroup(_groupId);
		
		String t_mailIndexList	= t_mailCursor.getString(t_mailCursor.getColumnIndex(MailDbAdapter.GROUP_ATTR_MAIL_INDEX));
		String[] t_mailList 	= t_mailIndexList.split(fetchMail.fsm_vectStringSpliter);
		
		t_mailCursor.close();
		
		// fetch Mail data from the group list
		//
		StringBuffer t_markReadMailString_hash = new StringBuffer();
		StringBuffer t_markReadMailString_ID = new StringBuffer();
		
		boolean t_hasUnreadMail = false;
		boolean t_modifiedFlag;
		long t_id;
		
		for(int i = 0;i < t_mailList.length;i++){
			
			t_id = Long.valueOf(t_mailList[i]).longValue();
			
			fetchMail t_mail	= m_mainApp.m_dba.fetchMail(t_id);
			if(t_mail == null){
				// the mail has been deleted (clear history)
				//
				continue;
			}
						
			AtomicReference<Integer> t_flag = new AtomicReference<Integer>(t_mail.getGroupFlag());
			t_modifiedFlag = MailDbAdapter.modifiedUnreadFlag(t_flag);
			
			if(t_modifiedFlag){
				// mark the mail as read
	    		//
				t_hasUnreadMail = true;
	    		m_mainApp.m_dba.markMailRead(t_id);
	    		t_markReadMailString_hash.append(t_mail.GetSimpleHashCode()).append(fetchMail.fsm_vectStringSpliter);
	    		t_markReadMailString_ID.append(t_mail.getMessageID()).append(fetchMail.fsm_vectStringSpliter);
			}  	
		}
		  	
		if(t_hasUnreadMail){
			
			// mark the unread group mail as read (database)
			//
			m_mainApp.m_dba.markGroupRead(_groupId);
			
			// send the broadcast to ConnectDeamon and MailListView (MailListActivity)
			//
			Intent t_intent = new Intent(YuchDroidApp.FILTER_MARK_MAIL_READ);
			t_intent.putExtra(YuchDroidApp.DATA_FILTER_MARK_MAIL_READ_GROUPID,_groupId);
			t_intent.putExtra(YuchDroidApp.DATA_FILTER_MARK_MAIL_READ_MAILID,t_markReadMailString_ID.toString());
			t_intent.putExtra(YuchDroidApp.DATA_FILTER_MARK_MAIL_READ_MAIL_HASH,t_markReadMailString_hash.toString());
			
			sendBroadcast(t_intent);
		}
	}
	
	private void delMailGroup(long _groupId){
		
		Cursor t_mailCursor		= m_mainApp.m_dba.fetchGroup(_groupId);
		
		String t_mailIndexList	= t_mailCursor.getString(t_mailCursor.getColumnIndex(MailDbAdapter.GROUP_ATTR_MAIL_INDEX));
		String[] t_mailList 	= t_mailIndexList.split(fetchMail.fsm_vectStringSpliter);
		
		t_mailCursor.close();
		
		// fetch Mail data from the group list
		//
		StringBuffer t_markReadMailString_hash = new StringBuffer();
		StringBuffer t_markReadMailString_ID = new StringBuffer();
		
		long t_id;
		
		for(int i = 0;i < t_mailList.length;i++){
			
			t_id = Long.valueOf(t_mailList[i]).longValue();
			
			fetchMail t_mail	= m_mainApp.m_dba.fetchMail(t_id);
			if(t_mail == null){
				// the mail has been deleted (clear history)
				//
				continue;
			}
						
			if(!t_mail.isOwnSendMail()){
				// is NOT own send Mail
				//
	    		t_markReadMailString_hash.append(t_mail.GetSimpleHashCode()).append(fetchMail.fsm_vectStringSpliter);
	    		t_markReadMailString_ID.append(t_mail.getMessageID()).append(fetchMail.fsm_vectStringSpliter);
			}  	
		}
		  	
		m_mainApp.m_dba.deleteGroup(_groupId);
		refreshGroupCursor();
		
		if(t_markReadMailString_hash.length() != 0 && m_mainApp.m_config.m_delRemoteMail){
			
			// send broadcast to ConnectDeamon
			//
			Intent t_intent = new Intent(YuchDroidApp.FILTER_DELETE_MAIL);
			t_intent.putExtra(YuchDroidApp.DATA_FILTER_MARK_MAIL_READ_GROUPID,_groupId);
        	t_intent.putExtra(YuchDroidApp.DATA_FILTER_MARK_MAIL_READ_MAIL_HASH,t_markReadMailString_hash.toString());
        	t_intent.putExtra(YuchDroidApp.DATA_FILTER_MARK_MAIL_READ_MAILID,t_markReadMailString_ID.toString());			        	
        	
    		sendBroadcast(t_intent);	
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
}
