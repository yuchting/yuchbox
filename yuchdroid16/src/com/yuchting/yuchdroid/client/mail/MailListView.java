package com.yuchting.yuchdroid.client.mail;

import android.database.Cursor;
import android.widget.ListView;

import com.yuchting.yuchdroid.client.HomeActivity;
import com.yuchting.yuchdroid.client.YuchDroidApp;

public class MailListView extends ListView {
	
	private Cursor			m_groupCursor = null;
	
	public YuchDroidApp		m_mainApp;
	public HomeActivity		m_homeActivity;
	
		
	public MailListView(HomeActivity _home,YuchDroidApp _mainApp){
		super(_home);
		
		m_mainApp = _mainApp;
		m_homeActivity = _home;
			
		fillMail();
	}
	    
    public void destroy(){
    	
    	// close the cuar
    	//
    	m_groupCursor.close();
    }
    
    public void fillMail(){
    	m_groupCursor = m_mainApp.m_dba.fetchAllGroup();
        setAdapter(new MailListAdapter(m_homeActivity, m_groupCursor));
    }
}
