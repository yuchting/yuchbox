package com.yuchting.yuchdroid.client;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Window;
import android.widget.SimpleCursorAdapter;

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
    
    public void fillMail(){
    	 // Get all of the rows from the database and create the item list
    	m_mailCursor = m_mailDbAdapter.fetchAllNotes();
        startManagingCursor(m_mailCursor);

        // Create an array to specify the fields we want to display in the list (only TITLE)
        String[] from = new String[]
        {
        	MailDbAdapter.KEY_SUBJECT,
        	MailDbAdapter.KEY_BODY
        };

        // and an array of the fields we want to bind those fields to (in this case just text1)
        int[] to = new int[]
        {
        	R.id.mail_subject,
        	R.id.mail_content,
        };

        // Now create a simple cursor adapter and set it to display
        SimpleCursorAdapter notes = 
            new SimpleCursorAdapter(this, 
            						R.layout.mail_list_item,
            						m_mailCursor, from, to){
        	
        	public CharSequence convertToString (Cursor cursor){
        		CharSequence t_set = super.convertToString(cursor);
        		return t_set;
        	}
        };
        
        setListAdapter(notes);
    }
}
