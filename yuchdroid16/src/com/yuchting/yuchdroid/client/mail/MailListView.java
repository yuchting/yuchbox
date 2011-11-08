package com.yuchting.yuchdroid.client.mail;

import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.yuchting.yuchdroid.client.R;
import com.yuchting.yuchdroid.client.YuchDroidApp;

public class MailListView extends ListView {
	
	public YuchDroidApp		m_mainApp;
	public HomeActivity		m_homeActivity;
	
	public MailListAdapter m_mailListAd;
			
	public MailListView(HomeActivity _home,YuchDroidApp _mainApp){
		super(_home);
		setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
		setDivider(getResources().getDrawable(R.drawable.mail_list_divider));
		setDividerHeight(1);
		
		setCacheColorHint(0);
		//setBackgroundColor(0xffffff);	
				
		m_mainApp = _mainApp;
		m_homeActivity = _home;

		m_mailListAd = new MailListAdapter(_home);
        setAdapter(m_mailListAd);
        
        setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
        	public void onItemClick(AdapterView<?> parent, View view, int position, long id){
        		MailListAdapter.ItemHolder t_holder = (MailListAdapter.ItemHolder)view.getTag();
        		
        		if(t_holder != null){
        			Intent in = new Intent(m_homeActivity,MailOpenActivity.class);
        			in.putExtra(MailOpenActivity.INTENT_CURRENT_MAIL_GROUP, t_holder.groupId);
        			in.putExtra(MailOpenActivity.INTENT_PRE_MAIL_GROUP_INDEX, t_holder.preGroupId);
        			in.putExtra(MailOpenActivity.INTENT_NEXT_MAIL_GROUP_INDEX, t_holder.nextGroupId);
        			in.putExtra(MailOpenActivity.INTENT_NEXT_MAIL_CURSOR_POS,t_holder.cursorPos);
        			
        			m_homeActivity.startActivity(in);
        		}
        	}
		});
	}    
}
