package com.yuchting.yuchdroid.client.mail;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.yuchting.yuchdroid.client.R;
import com.yuchting.yuchdroid.client.YuchDroidApp;

public class MailDbAdapter {
	
	public static final String DATABASE_NAME 			= "yuch_data";
	
	private final static String DATABASE_TABLE		= "yuch_mail";
	private final static String DATABASE_TABLE_GROUP	= "yuch_mail_group";
	private final static String DATABASE_TABLE_GROUP_SUB_INDEX	= "yuch_mail_group_sub_index";
	
	private static final int DATABASE_VERSION 		= 1;
	
	public final static String KEY_ID		 			= "_id";

	// mail group attribute
	//
	public final static String GROUP_ATTR_READ			= "group_read";
	public final static String GROUP_ATTR_MARK			= "group_mark";
	
	public final static String GROUP_ATTR_SUBJECT				= "group_subject";
	public final static String GROUP_ATTR_LEATEST_BODY			= "group_body";
	public final static String GROUP_ATTR_LEATEST_TIME			= "group_time";
	public final static String GROUP_ATTR_ADDR_LIST			= "group_addr";
	public final static String GROUP_ATTR_GROUP				= "group_group"; // reverse attribute 
	public final static String GROUP_ATTR_MAIL_INDEX			= "group_mail";
		
	
	// mail entry attribute
	//
	public final static String ATTR_READ			= "mail_read";
	public final static String ATTR_MARK			= "mail_mark";
	
	public final static String ATTR_INDEX			= "mail_index";
	public final static String ATTR_HASH_CODE		= "mail_hash";
		
	public final static String ATTR_SUBJECT 		= "mail_sub";  
	public final static String ATTR_BODY 			= "mail_body";
	public final static String ATTR_BODY_HTML 		= "mail_body_html";
	public final static String ATTR_BODY_HTML_TYPE	= "mail_body_html_type";
	
	public final static String ATTR_TO				= "mail_to";
	public final static String ATTR_CC				= "mail_cc";
	public final static String ATTR_BCC			= "mail_bcc";
	public final static String ATTR_FROM			= "mail_from";
	public final static String ATTR_REPLY			= "mail_reply";
	public final static String ATTR_GROUP			= "mail_group";
	
	public final static String ATTR_DATE			= "mail_date";
	public final static String ATTR_FLAG			= "mail_flag";
	
	public final static String ATTR_MAIL_GROUP_INDEX	= "mail_group_index";
	
	private static final String TAG 				= "MailDbAdapter";
	
	public static final String[] fsm_groupfullColoumns = 
    {
		KEY_ID, 
		
		GROUP_ATTR_READ,
		GROUP_ATTR_MARK,
		
		GROUP_ATTR_SUBJECT,
		GROUP_ATTR_LEATEST_BODY,
		GROUP_ATTR_LEATEST_TIME,
		GROUP_ATTR_ADDR_LIST,
		GROUP_ATTR_GROUP,
		GROUP_ATTR_MAIL_INDEX,
    };
	
	public static final String[] fsm_mailfullColoumns = 
    {
		KEY_ID, 
		
		ATTR_READ,
		ATTR_MARK,
		
		ATTR_INDEX,
		ATTR_HASH_CODE,
		ATTR_SUBJECT,
        ATTR_BODY,
        ATTR_BODY_HTML,
        ATTR_BODY_HTML_TYPE,
        
        ATTR_TO,
        ATTR_CC,
        ATTR_BCC,
        ATTR_FROM,
        ATTR_REPLY,
        ATTR_GROUP,
        
        ATTR_DATE,
        ATTR_MAIL_GROUP_INDEX,
        ATTR_FLAG,   
    };
	
	private final static String fsm_createTable_group = 
							"create table " + DATABASE_TABLE_GROUP + 
						    " ("+KEY_ID+" integer primary key, " +
						    
						    GROUP_ATTR_READ + " smallint," +
						    GROUP_ATTR_MARK + " smallint," +
						    
						    GROUP_ATTR_SUBJECT + " text, " +
						    
						    GROUP_ATTR_LEATEST_BODY + " text, " +
						    GROUP_ATTR_LEATEST_TIME +  " integer(64), " +
						    
						    GROUP_ATTR_ADDR_LIST + " text, " +
						    GROUP_ATTR_GROUP + " text, " +
						    GROUP_ATTR_MAIL_INDEX + " text )";
	
	private final static String fsm_createTable = 
		
							"create table " + DATABASE_TABLE + 
					        " ("+KEY_ID+" integer primary key, " +
					        
					        ATTR_READ + " smallint," +
					        ATTR_MARK + " smallint," +
					        
					        ATTR_INDEX + " integer, " +
					        ATTR_HASH_CODE + " integer, " +
					        
					        ATTR_SUBJECT + " text, " +
					        ATTR_BODY +  " text, " +
					        ATTR_BODY_HTML + " text, " + 
					        ATTR_BODY_HTML_TYPE + " text, " + 
					        
					        ATTR_TO + " text not null, " + 
					        ATTR_CC + " text, " +
					        ATTR_BCC + " text, " +
					        ATTR_FROM + " text not null, " + 
					        ATTR_REPLY + " text, " +
					        ATTR_GROUP + " text, " +
					        
					        ATTR_DATE + " integer(64), " +
					        ATTR_MAIL_GROUP_INDEX + " integer, " + 
					        ATTR_FLAG + " integer )" ;	
	
	private DatabaseHelper mDbHelper = null;
	private SQLiteDatabase mDb = null;
	
	private YuchDroidApp m_mainApp;

	private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context,boolean _groupData) {
            super(context,DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
        	db.execSQL(fsm_createTable);
            db.execSQL(fsm_createTable_group);
                        
            
        	// create the index by subject
        	//
        	db.execSQL("create index " + DATABASE_TABLE_GROUP_SUB_INDEX + " on " + DATABASE_TABLE_GROUP + " (" + GROUP_ATTR_SUBJECT + ")");
            
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            
        	// simply delete all original table and index
        	//
        	Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            
            db.execSQL("drop index if exists " + DATABASE_TABLE_GROUP_SUB_INDEX + " on " + DATABASE_TABLE_GROUP);
            db.execSQL("drop table if exists " + DATABASE_TABLE_GROUP);
            
            db.execSQL("drop table if exists " + DATABASE_TABLE);
            
            onCreate(db);
        }
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public MailDbAdapter(YuchDroidApp _mainApp) {
    	m_mainApp = _mainApp;
    }

    public MailDbAdapter open() throws SQLException {
    	if(mDbHelper != null){
    		close();
    	}
    	
        mDbHelper = new DatabaseHelper(m_mainApp,false);
        mDb = mDbHelper.getWritableDatabase();
        
        
        return this;
    }

    public void close() {
    	if(mDbHelper != null){
    		mDbHelper.close();
            mDbHelper = null;
            mDb = null;
    	}
    }

    private final static String[] fsm_groupSubjectPrefix = 
    {
    	"Re: ",
    	"Re： ",
    	"RE: ",
    	"RE： ",
    	"回复: ",
    	"回复： ",
    	"答复: ",
    	"答复： ",
    };
    
    /**
     * get the group subject by the original subject via prefix
     * @param _orgSub original subject of mail
     * @return converted subject
     */
    private static String groupSubject(String _orgSub){
    	int t_index;
    	for(String pre:fsm_groupSubjectPrefix){
    		t_index = _orgSub.lastIndexOf(pre); 
    		if(t_index != -1){
    			return _orgSub.substring(t_index + pre.length());
    		}
    	}
    	
    	return _orgSub;
    }
    
    public static String getDisplayMailBody(fetchMail _mail){
    	if(_mail.GetContain().length() == 0){
    		if(_mail.GetContain_html().length() != 0){
    			return "HTML";
    		}else{
    			return "";
    		}
    	}else{
    		return _mail.GetContain();
    	}
    }
    
    
    public long createMail(fetchMail _mail,Long _replyGroupId){
        
    	if(mDbHelper == null){
    		open();
    	}
    	
    	// create the values data of this mail
    	//
    	ContentValues values = new ContentValues();
        
    	values.put(ATTR_READ,0);
    	values.put(ATTR_MARK,0);
    	
    	values.put(ATTR_INDEX,_mail.GetMailIndex());
    	values.put(ATTR_HASH_CODE,_mail.GetSimpleHashCode());
        values.put(ATTR_SUBJECT,_mail.GetSubject());
        values.put(ATTR_BODY,_mail.GetContain());
        values.put(ATTR_BODY_HTML,_mail.GetContain_html());
        values.put(ATTR_BODY_HTML_TYPE,_mail.GetContain_html_type());
        
        values.put(ATTR_TO,_mail.getSendToString());
        values.put(ATTR_CC,_mail.getCCToString());
        values.put(ATTR_BCC,_mail.getBCCToString());
        values.put(ATTR_FROM,_mail.GetFromString());
        values.put(ATTR_REPLY,_mail.getReplyString());
        values.put(ATTR_GROUP,_mail.getGroupString());
        
        values.put(ATTR_DATE,_mail.GetSendDate().getTime());
        values.put(ATTR_FLAG,_mail.GetFlags());

        long t_mailID;
        
    	if(_replyGroupId != null){
    		// is sent mail 
    		//
    		values.put(ATTR_READ,1);
    		
    		t_mailID = mDb.insert(DATABASE_TABLE, null, values);
    		
    		// update the former group
			//
    		Cursor t_cursor = mDb.query(DATABASE_TABLE_GROUP,fsm_groupfullColoumns,KEY_ID + "=" + _replyGroupId + "",
    											null,null,null,null);
    		try{
        		updateGroup(t_cursor,_mail,true,t_mailID);
    		}finally{
    			t_cursor.close();
    		}
    		
    	}else{
    		
    		// receive mail
    		// 
    		Cursor t_cursor = mDb.query(DATABASE_TABLE,fsm_mailfullColoumns,ATTR_HASH_CODE + "='" + _mail.GetSimpleHashCode() + "'",
					null,null,null,null);
    		try{
    			if(t_cursor.getCount() != 0){
        			// repeat mail
        			//
        			return -1;
        		}
    		}finally{
    			t_cursor.close();
    		} 		
    		  
    		t_mailID = mDb.insert(DATABASE_TABLE, null, values);
    		
    		String t_subject = groupSubject(_mail.GetSubject());
    		
    		t_cursor = mDb.query(DATABASE_TABLE_GROUP,fsm_groupfullColoumns,GROUP_ATTR_SUBJECT + "='" + t_subject + "'",
    											null,null,null,null);
    		try{
    			if(t_cursor.getCount() != 0){

        			// update the former group
        			//
        			updateGroup(t_cursor,_mail,false,t_mailID);
        			    			
        		}else{
        			
        			// can't find the old group
        			// create a insert one
        			//    			
        			insertGroup(t_subject,t_mailID,_mail,false);
        		}
    		}finally{
    			t_cursor.close();
    		}
    	}
    	
    	return t_mailID;
    }
    
    private static String reRangeAddrList(String _addrList,String _addAddr){
    	
    	String[] t_list 	= _addrList.split(fetchMail.fsm_vectStringSpliter);
    	
    	StringBuffer t_ret = new StringBuffer();
    	for(String addr:t_list){
    		if(!addr.equalsIgnoreCase(_addAddr)){
    			t_ret.append(addr).append(fetchMail.fsm_vectStringSpliter);
    		}
    	}
    	
    	t_ret.append(_addAddr).append(fetchMail.fsm_vectStringSpliter);
    	
    	return t_ret.toString();
    }
    
    private boolean updateGroup(Cursor _groupCursor,fetchMail _mail,boolean _read,long _mailIndex){

    	if(!_groupCursor.moveToFirst()){
    		// move to the frist and retrieve the data correctly 
			//
    		return false;
    	}
    	
    	long t_id 			= _groupCursor.getLong(_groupCursor.getColumnIndex(KEY_ID));
    	
    	int t_read 			= _read?1:0;
    	
    	int t_mark 			= _groupCursor.getInt(_groupCursor.getColumnIndex(GROUP_ATTR_MARK));
    	String t_sub 		= _groupCursor.getString(_groupCursor.getColumnIndex(GROUP_ATTR_SUBJECT));
    	
    	String t_body 		= getDisplayMailBody(_mail);
    	
    	long t_time 		= _mail.GetSendDate().getTime();
    	String t_addr_list;
    	if(_mail.GetFromVect().isEmpty()){
    		t_addr_list = reRangeAddrList(_groupCursor.getString(_groupCursor.getColumnIndex(GROUP_ATTR_ADDR_LIST)),
    										m_mainApp.getString(R.string.mail_me_address));
    	}else{
    		t_addr_list = reRangeAddrList(_groupCursor.getString(_groupCursor.getColumnIndex(GROUP_ATTR_ADDR_LIST)),
    								_mail.GetFromVect().get(0));
    	}
    	
    	String t_group 		= _groupCursor.getString(_groupCursor.getColumnIndex(GROUP_ATTR_GROUP)); // reverse data
    	
    	String t_index		= _groupCursor.getString(_groupCursor.getColumnIndex(GROUP_ATTR_MAIL_INDEX)) + 
    								fetchMail.fsm_vectStringSpliter + _mailIndex;
    	
    	ContentValues group = new ContentValues();
    	
		group.put(GROUP_ATTR_READ,t_read);
		group.put(GROUP_ATTR_MARK,t_mark);
		
		group.put(GROUP_ATTR_SUBJECT,t_sub);
		group.put(GROUP_ATTR_LEATEST_BODY,t_body);
		
		group.put(GROUP_ATTR_LEATEST_TIME,t_time);
		group.put(GROUP_ATTR_ADDR_LIST,t_addr_list);
		group.put(GROUP_ATTR_GROUP,t_group); 
		group.put(GROUP_ATTR_MAIL_INDEX,t_index);
		
		return mDb.update(DATABASE_TABLE_GROUP, group, KEY_ID + "=" + t_id, null) > 0;
    }
    
    /**
     *  insert a new group to the group table
     * @param _subject group subject
     * @param _mailID first mail id
     * @param _mail mail data
     * @return new group inserted _id
     */
    private long insertGroup(String _subject,long _mailID,fetchMail _mail,boolean _read){
    	
		ContentValues group = new ContentValues();
		group.put(GROUP_ATTR_READ,0);
		group.put(GROUP_ATTR_MARK,0);
		
		group.put(GROUP_ATTR_SUBJECT,_subject);
		group.put(GROUP_ATTR_LEATEST_BODY,getDisplayMailBody(_mail));
		
		group.put(GROUP_ATTR_LEATEST_TIME,_mail.GetSendDate().getTime());
		group.put(GROUP_ATTR_ADDR_LIST,_mail.GetFromString());
		group.put(GROUP_ATTR_GROUP,""); // reverse attribute
		group.put(GROUP_ATTR_MAIL_INDEX,Long.toString(_mailID) + fetchMail.fsm_vectStringSpliter);    			
		
		return mDb.insert(DATABASE_TABLE_GROUP, null, group);
    }

    public boolean deleteGroup(long _groupID){
    	if(mDbHelper == null){
    		open();
    	}

        return mDb.delete(DATABASE_TABLE_GROUP, KEY_ID + "=" + _groupID, null) > 0;
    }
    
    public boolean deleteMail(long id) {
    	if(mDbHelper == null){
    		open();
    	}

        return mDb.delete(DATABASE_TABLE, KEY_ID + "=" + id, null) > 0;
    }
            
    public Cursor fetchAllGroup(){
    	if(mDbHelper == null){
    		open();
    	}
    	
        return mDb.query(DATABASE_TABLE_GROUP,fsm_groupfullColoumns, null, null, null, null, null);
    }

    public Cursor fetchGroup(long _groupId) throws SQLException {

    	if(mDbHelper == null){
    		open();
    	}
    	
        Cursor mCursor = mDb.query(true, DATABASE_TABLE_GROUP, fsm_groupfullColoumns, KEY_ID + "=" + _groupId, null,
                    				null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        
        return mCursor;

    }

}
