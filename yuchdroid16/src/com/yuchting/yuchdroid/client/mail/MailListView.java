package com.yuchting.yuchdroid.client.mail;

import android.database.Cursor;
import android.widget.ListView;

import com.yuchting.yuchdroid.client.HomeActivity;
import com.yuchting.yuchdroid.client.R;
import com.yuchting.yuchdroid.client.YuchDroidApp;

public class MailListView extends ListView {
	
	private Cursor			m_groupCursor = null;
	
	public YuchDroidApp		m_mainApp;
	public HomeActivity		m_homeActivity;
	
	private MailListAdapter m_mailListAd;
			
	public MailListView(HomeActivity _home,YuchDroidApp _mainApp){
		super(_home);
		setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
		setDivider(getResources().getDrawable(R.drawable.mail_list_divider));
		setDividerHeight(1);
		
		setCacheColorHint(0);
		setBackgroundColor(0xffffff);
		
		m_mainApp = _mainApp;
		m_homeActivity = _home;
		
		m_groupCursor = m_mainApp.m_dba.fetchAllGroup();
		m_mailListAd = new MailListAdapter(m_homeActivity, m_groupCursor);
		
        setAdapter(m_mailListAd);
	}
	    
    public void destroy(){
    	
    	// close the cuar
    	//
    	m_groupCursor.close();
    	
    }
}
