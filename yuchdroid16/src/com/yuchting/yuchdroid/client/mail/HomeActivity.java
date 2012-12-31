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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicReference;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemLongClickListener;

import com.yuchting.yuchdroid.client.GlobalDialog;
import com.yuchting.yuchdroid.client.R;
import com.yuchting.yuchdroid.client.YuchDroidApp;
import com.yuchting.yuchdroid.client.Yuchdroid16Activity;


public class HomeActivity extends ListActivity implements View.OnClickListener, OnItemLongClickListener,OnCreateContextMenuListener{
			
	public static final int	MAX_GROUP_FATCH_NUM		= 35;

	public Cursor			m_groupCursor;
	public int 			m_currGroupLimit = MAX_GROUP_FATCH_NUM;
		
	private YuchDroidApp	m_mainApp;
	private MailListAdapter m_mailListAd;
	
	//! select mails
	private Vector<Long> m_selectedMailGroupList = new Vector<Long>();
	
	private ViewGroup	m_footer 			= null;
	private Button		m_batchReadBtn 		= null;
	private Button		m_batchUnreadBtn 	= null;
	private Button		m_batchDelBtn		= null;
	
	//! long click item holder var for processing
	private MailListAdapter.ItemHolder	m_longClickItemHolder = null;
				
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
        
        // add the footer
        m_footer = (ViewGroup)findViewById(R.id.mail_home_footer);
        m_batchReadBtn = (Button)m_footer.findViewById(R.id.mail_home_read_btn);
        m_batchReadBtn.setOnClickListener(this);
        m_batchUnreadBtn = (Button)m_footer.findViewById(R.id.mail_home_unread_btn);
        m_batchUnreadBtn.setOnClickListener(this);
        m_batchDelBtn = (Button)m_footer.findViewById(R.id.mail_home_delete_btn);
        m_batchDelBtn.setOnClickListener(this);
        
        // get the cursor of db
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
        
        getListView().setOnItemLongClickListener(this);
        		
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
	
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position,long id){
		
		m_longClickItemHolder = (MailListAdapter.ItemHolder)view.getTag();		
		if(m_longClickItemHolder != null){
			getListView().setOnCreateContextMenuListener(this);
		}
		
		return false;
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo){
		menu.add(0, 0, 0, getString(R.string.mail_home_item_context_menu_read));
		menu.add(0, 1, 0, getString(R.string.mail_home_item_context_menu_del));
		menu.add(0, 2, 0, getString(R.string.mail_home_item_context_menu_read_former));
		menu.add(0, 3, 0, getString(R.string.mail_home_item_context_menu_del_former));
		menu.add(0, 4, 0, getString(R.string.mail_home_item_context_menu_cancel));
	}
	
	@Override
    public boolean onContextItemSelected(MenuItem item) {
	
		switch (item.getItemId()) {
			case 0:
				clearSelectedGroup();
				addSelectedGroup(m_longClickItemHolder.groupId);
				onClick(m_batchReadBtn);
				break;
			case 1:
				clearSelectedGroup();
				addSelectedGroup(m_longClickItemHolder.groupId);
				onClick(m_batchDelBtn);
				break;
			case 2:
			case 3:
				
				final boolean t_markReadOrDelete	= item.getItemId() == 2;
				final long 	t_groupId			= m_longClickItemHolder.groupId;
				final Handler	t_refreshHandler	= new Handler();
				
				clearSelectedGroup();
				
				GlobalDialog.showWait(null, this);
				
				(new Thread(){
					public void run(){

						List<Integer> t_mailSimpleHashList	= new ArrayList<Integer>();
						List<String> t_mailMessageIdList		= new ArrayList<String>();
						
						if(m_mainApp.m_dba.markReadOrDelBatchMail(t_groupId, t_mailSimpleHashList, t_mailMessageIdList, t_markReadOrDelete) 
						&& !t_mailMessageIdList.isEmpty()){

							Intent t_intent = new Intent(YuchDroidApp.FILTER_MARK_MAIL_READ_BATCH);
							t_intent.putExtra(YuchDroidApp.DATA_FILTER_MARK_MAIL_READ_OR_DEL,t_markReadOrDelete);
							t_intent.putExtra(YuchDroidApp.DATA_FILTER_MARK_MAIL_READ_MAILID,(Serializable)t_mailMessageIdList);
							t_intent.putExtra(YuchDroidApp.DATA_FILTER_MARK_MAIL_READ_MAIL_HASH,(Serializable)t_mailSimpleHashList);
							
							sendBroadcast(t_intent);
							
							t_refreshHandler.post(new Runnable() {
								public void run() {
									refreshGroupCursor();
								}
							});
						}
						
						GlobalDialog.hideWait(HomeActivity.this);
					}
				}).start();
				
				break;
			default:
				break;
		}
		
		m_longClickItemHolder = null;
		
		return super.onContextItemSelected(item);

    }
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if(!m_selectedMailGroupList.isEmpty()){
				
				clearSelectedGroup();
				refreshGroupCursor();
				
				return true;
			}	
		}
		
		return super.onKeyDown(keyCode, event);
	}
	
	/**
	 * mark mail group read
	 */
	private void markMailGroupRead(long _groupId,StringBuffer _markReadMailString_hash,StringBuffer _markReadMailString_ID){
		
		Cursor t_mailCursor		= m_mainApp.m_dba.fetchGroup(_groupId);
		
		String t_mailIndexList	= t_mailCursor.getString(t_mailCursor.getColumnIndex(MailDbAdapter.GROUP_ATTR_MAIL_INDEX));
		String[] t_mailList 	= t_mailIndexList.split(fetchMail.fsm_vectStringSpliter);
		
		t_mailCursor.close();
		
		// fetch Mail data from the group list
		//
		
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
	    		_markReadMailString_hash.append(t_mail.GetSimpleHashCode()).append(fetchMail.fsm_vectStringSpliter);
	    		_markReadMailString_ID.append(t_mail.getMessageID()).append(fetchMail.fsm_vectStringSpliter);
			}  	
		}
		  	
		if(t_hasUnreadMail){
			
			// mark the unread group mail as read (database)
			//
			m_mainApp.m_dba.markGroupRead(_groupId);
		}
	}
	
	private void delMailGroup(long _groupId,StringBuffer _markReadMailString_hash,StringBuffer _markReadMailString_ID){
		
		Cursor t_mailCursor		= m_mainApp.m_dba.fetchGroup(_groupId);
		
		String t_mailIndexList	= t_mailCursor.getString(t_mailCursor.getColumnIndex(MailDbAdapter.GROUP_ATTR_MAIL_INDEX));
		String[] t_mailList 	= t_mailIndexList.split(fetchMail.fsm_vectStringSpliter);
		
		t_mailCursor.close();
		
		
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
				_markReadMailString_hash.append(t_mail.GetSimpleHashCode()).append(fetchMail.fsm_vectStringSpliter);
				_markReadMailString_ID.append(t_mail.getMessageID()).append(fetchMail.fsm_vectStringSpliter);
			}  	
		}
		  	
		m_mainApp.m_dba.deleteGroup(_groupId);
		
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
	
	
	/**
	 * clear the selected group item
	 */
	public void clearSelectedGroup(){
		m_selectedMailGroupList.clear();
		m_footer.setVisibility(View.GONE);
	}
	
	/**
	 * add a mail group to select
	 * @param _groupId
	 */
	public void addSelectedGroup(long _groupId){
		m_selectedMailGroupList.add(_groupId);
		m_footer.setVisibility(View.VISIBLE);
	}
	
	/**
	 * remove the selected mail group 
	 * @param _groupId
	 */
	public void removeSeletedGroup(long _groupId){
		for (Long groupId:m_selectedMailGroupList){
			if(groupId == _groupId){
				m_selectedMailGroupList.remove(groupId);
				break;
			}
		}
		
		if(m_selectedMailGroupList.isEmpty()){
			m_footer.setVisibility(View.GONE);
		}
	}
	
	/**
	 * is this group id selected mail ?
	 * @return
	 */
	public boolean isSelectedGroup(long _groupId){
		
		for (Long groupId:m_selectedMailGroupList){
			if(groupId == _groupId){	
				return true;
			}
		}
		
		return false;
	}

	@Override
	public void onClick(View v) {
		
		if(v == m_batchReadBtn){
						
			StringBuffer t_markReadMailString_hash	= new StringBuffer();
			StringBuffer t_markReadMailString_ID	= new StringBuffer();
			
			Long t_groupId = m_selectedMailGroupList.get(0);
			
			for (Long groupId:m_selectedMailGroupList){
				markMailGroupRead(groupId,t_markReadMailString_hash,t_markReadMailString_ID);
			}
			
			clearSelectedGroup();
			
			if(t_markReadMailString_hash.length() != 0){

				// send the broadcast to ConnectDeamon and MailListView (MailListActivity)
				//
				Intent t_intent = new Intent(YuchDroidApp.FILTER_MARK_MAIL_READ);
				t_intent.putExtra(YuchDroidApp.DATA_FILTER_MARK_MAIL_READ_GROUPID,t_groupId);
				t_intent.putExtra(YuchDroidApp.DATA_FILTER_MARK_MAIL_READ_MAILID,t_markReadMailString_ID.toString());
				t_intent.putExtra(YuchDroidApp.DATA_FILTER_MARK_MAIL_READ_MAIL_HASH,t_markReadMailString_hash.toString());
				
				sendBroadcast(t_intent);
			}else{
				refreshGroupCursor();
			}
			
		}else if(v == m_batchDelBtn){
			
			// delete the selected mail group
			//
			if(m_mainApp.m_config.m_forceDeleteMail){
				deleteSelectedGroup_impl();
			}else{
				
				GlobalDialog.showYesNoDialog(getString(R.string.mail_open_delete_prompt), this, 
				new GlobalDialog.YesNoListener() {
					
					@Override
					public void click() {
						deleteSelectedGroup_impl();
					}
				},null);
			}			
			
		}else if(v instanceof CheckBox || v instanceof ImageView){
						
			MailListAdapter.ItemHolder t_holder = (MailListAdapter.ItemHolder)v.getTag();
			
			if(v instanceof ImageView){
				t_holder.selected.setChecked(!t_holder.selected.isChecked());
			}
			
			if(t_holder.selected.isChecked()){
				addSelectedGroup(t_holder.groupId);
			}else{
				removeSeletedGroup(t_holder.groupId);
			}
		}
	}
	
	private void deleteSelectedGroup_impl(){
		
		if(!m_selectedMailGroupList.isEmpty()){
			StringBuffer t_markReadMailString_hash	= new StringBuffer();
			StringBuffer t_markReadMailString_ID	= new StringBuffer();
			
			Long t_groupId = m_selectedMailGroupList.get(0);
			
			for (Long groupId:m_selectedMailGroupList){
				delMailGroup(groupId,t_markReadMailString_hash,t_markReadMailString_ID);
			}
			
			if(t_markReadMailString_hash.length() != 0 && m_mainApp.m_config.m_delRemoteMail){
				
				// send broadcast to ConnectDeamon
				//
				Intent t_intent = new Intent(YuchDroidApp.FILTER_DELETE_MAIL);
				t_intent.putExtra(YuchDroidApp.DATA_FILTER_MARK_MAIL_READ_GROUPID,t_groupId);
	        	t_intent.putExtra(YuchDroidApp.DATA_FILTER_MARK_MAIL_READ_MAIL_HASH,t_markReadMailString_hash.toString());
	        	t_intent.putExtra(YuchDroidApp.DATA_FILTER_MARK_MAIL_READ_MAILID,t_markReadMailString_ID.toString());
	        	
	    		sendBroadcast(t_intent);
			}
			
			clearSelectedGroup();
		}
		
		refreshGroupCursor();		
	}

}
