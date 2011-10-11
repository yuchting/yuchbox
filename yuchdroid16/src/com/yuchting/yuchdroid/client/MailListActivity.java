package com.yuchting.yuchdroid.client;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class MailListActivity extends ListActivity {
	
	private MailDbAdapter 	m_mailDbAdapter = new MailDbAdapter(this);
	private Cursor			m_mailCursor = null;
	
	
	public static void show(Context ctx){
		Intent t_in = new Intent().setClass(ctx, MailListActivity.class);
		ctx.startActivity(t_in);
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        setContentView(R.layout.mail_list);
        
        m_mailDbAdapter.open();
        fillMail();
        
        registerForContextMenu(getListView());
    }
    
    private static final String[] fsm_fromCursor = 
    {
    	MailDbAdapter.KEY_SUBJECT,
    	MailDbAdapter.KEY_BODY
    };
    
    private static final int[] fsm_toCursor = 
    {
    	R.id.mail_subject,
    	R.id.mail_body,
    };
    
    public void fillMail(){
    	 // Get all of the rows from the database and create the item list
    	m_mailCursor = m_mailDbAdapter.fetchAllNotes();
        startManagingCursor(m_mailCursor);


        // Now create a simple cursor adapter and set it to display
        SimpleCursorAdapter notes = 
            new SimpleCursorAdapter(this, 
            						R.layout.mail_list_item,
            						m_mailCursor, fsm_fromCursor, fsm_toCursor);
        
        notes.setViewBinder(new SimpleCursorAdapter.ViewBinder(){
        	public boolean setViewValue(View view, Cursor cursor, int columnIndex){
        		// custom the value to display
        		//
        		switch(columnIndex){
        		case 0:
        			String t_value = cursor.getString(columnIndex);
            		((TextView)view).setText(t_value);
        			break;
        		case 1:
        		}
        		
        		
        		return true;
        	}
        });
        
        setListAdapter(notes);
    }
}
