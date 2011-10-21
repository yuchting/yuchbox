package com.yuchting.yuchdroid.client.mail;

import java.util.Date;

import android.database.Cursor;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.yuchting.yuchdroid.client.HomeActivity;
import com.yuchting.yuchdroid.client.R;
import com.yuchting.yuchdroid.client.YuchDroidApp;

public class MailListView extends ListView {
	
	private MailDbAdapter 	m_mailDbAdapter;
	private Cursor			m_groupCursor = null;
	
	public YuchDroidApp		m_mainApp;
	public HomeActivity		m_homeActivity;
		
	public MailListView(HomeActivity _home){
		super(_home);
		
		m_homeActivity = _home;
		
	}
	    
    public void destroy(){
    	
    	// close the cuar
    	//
    	m_groupCursor.close();
    	m_mailDbAdapter.close();
    }
    
    private static final String[] fsm_fromCursor = 
    {
    	MailDbAdapter.GROUP_ATTR_READ,
    	MailDbAdapter.GROUP_ATTR_MARK,
    	
    	MailDbAdapter.GROUP_ATTR_HAS_ATTACH,
    	
    	MailDbAdapter.GROUP_ATTR_SUBJECT,
    	MailDbAdapter.GROUP_ATTR_LEATEST_BODY,
    	
    	MailDbAdapter.GROUP_ATTR_ADDR_LIST,
    	MailDbAdapter.GROUP_ATTR_LEATEST_TIME,
    };
    
    private static final int[] fsm_toCursor = 
    {    	
    	R.id.mail_item,
    	R.id.mail_mark_btn,
    	
    	R.id.mail_attach_pic,
    	
    	R.id.mail_subject,
    	R.id.mail_body,
    	
    	R.id.mail_from_to,
    	R.id.mail_time,
    };
    
    public void fillMail(){
    	
    	m_groupCursor = m_mailDbAdapter.fetchAllGroup();
    	
        // Now create a simple cursor adapter and set it to display
        SimpleCursorAdapter notes = new SimpleCursorAdapter(m_homeActivity, 
		            						R.layout.mail_list_item,
		            						m_groupCursor, fsm_fromCursor, fsm_toCursor);
        
        notes.setViewBinder(new SimpleCursorAdapter.ViewBinder(){
        	
        	public boolean setViewValue(View view, Cursor cursor, int columnIndex){
        		
        		
        		// custom the value to display
        		//
        		switch(view.getId()){
        		case R.id.mail_item:
        			if(cursor.getInt(columnIndex) == 1){
        				view.setBackgroundColor(0xf0f0f0);
        			}else{
        				view.setBackgroundColor(0xffffff);
        			}
        			break;
        		case R.id.mail_mark_btn:
        			if(cursor.getInt(columnIndex) == 1){
        				((CheckBox)view).setSelected(true);
        			}else{
        				((CheckBox)view).setSelected(false);
        			}
        			break;
        		case R.id.mail_attach_pic:
        			view.setVisibility(cursor.getInt(columnIndex) == 1?View.VISIBLE:View.INVISIBLE);
        			break;
        		case R.id.mail_subject:
        		case R.id.mail_body:
        			((TextView)view).setText(cursor.getString(columnIndex));	
        			break;
        		case R.id.mail_from_to:
        			// address
        			//
        			Address[] t_addr = fetchMail.parseAddressList(cursor.getString(columnIndex).split(";"));
        			StringBuffer t_display = new StringBuffer();
        			int t_num = 0;
        			for(int i = t_addr.length - 1;i >= 0 && t_num < 3;i--,t_num++){
        				t_display.append(t_addr[i].m_name).append(fetchMail.fsm_vectStringSpliter);
        			}
        			t_display.append("(").append(t_addr.length).append(")");
        			((TextView)view).setText(t_display.toString());
        			break;
        		case R.id.mail_time:
        			// mail least time
        			//
        			Date t_current = new Date();
        			Date t_leastDate = new Date(cursor.getLong(columnIndex));
        			String t_time;
        			if(t_leastDate.getYear() == t_current.getYear()
        			&& t_leastDate.getMonth() == t_current.getMonth()
        			&& t_leastDate.getDay() == t_current.getDay()){
        				t_time = t_leastDate.getHours() + ":" + t_leastDate.getMinutes();
        			}else if(t_leastDate.getYear() == t_current.getYear()){
        				t_time = t_leastDate.getMonth() + "-" + t_leastDate.getDay();
        			}else {
        				t_time = t_leastDate.getYear() + "-" + t_leastDate.getMonth() + "-" + t_leastDate.getDay();
        			}
        			
        			((TextView)view).setText(t_time);
        			break;
        			
        		}
        		       		
        		return true;
        	}
        });
        
        setAdapter(notes);
    }
}
