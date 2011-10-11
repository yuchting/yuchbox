package com.yuchting.yuchdroid.client;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

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
	public final static String GROUP_ATTR_LEATEST_TIME			= "group_body";
	public final static String GROUP_ATTR_ADDR_LIST			= "group_addr";
	public final static String GROUP_ATTR_GROUP				= "group_group"; // reverse attribute 
	public final static String GROUP_ATTR_MAIL_INDEX			= "group_mail";
		
	
	// mail entry attribute
	//
	public final static String ATTR_READ			= "mail_read";
	public final static String ATTR_MARK			= "mail_mark";
	
	public final static String ATTR_INDEX			= "mail_index";
		
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
	
	private static String getCreateTable(boolean _group){
		
		if(_group){

			return "create table " + DATABASE_TABLE_GROUP + 
			        " ("+KEY_ID+" integer primary key, " +
			        
			        GROUP_ATTR_READ + " smallint," +
			        GROUP_ATTR_MARK + " smallint," +
			        
			        GROUP_ATTR_SUBJECT + " text, " +
			        
			        GROUP_ATTR_LEATEST_BODY + " text, " +
			        GROUP_ATTR_LEATEST_TIME +  " text, " +
			        
			        GROUP_ATTR_ADDR_LIST + " text, " +
			        GROUP_ATTR_GROUP + " text, " +
			        GROUP_ATTR_MAIL_INDEX + " text )";
			
		}else{
			
			return "create table " + DATABASE_TABLE + 
			        " ("+KEY_ID+" integer primary key, " +
			        
			        ATTR_READ + " smallint," +
			        ATTR_MARK + " smallint," +
			        
			        ATTR_INDEX + " integer, " +
			        
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
			        
			        ATTR_DATE + " text, " +
			        ATTR_MAIL_GROUP_INDEX + " integer, " + 
			        ATTR_FLAG + " integer )" ;
		}
		
	}
	
	
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;
	
	private DatabaseHelper mDbHelper_group;
	private SQLiteDatabase mDb_group;

	private final Context mCtx;

	private static class DatabaseHelper extends SQLiteOpenHelper {

		boolean m_group_data = false;
		
        DatabaseHelper(Context context,boolean _groupData) {
            super(context,DATABASE_NAME, null, DATABASE_VERSION);
            
            m_group_data = _groupData;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(getCreateTable(m_group_data));
            
            if(m_group_data){
            	// create the index by subject
            	//
            	db.execSQL("create index " + DATABASE_TABLE_GROUP_SUB_INDEX + " on " + DATABASE_TABLE_GROUP + " (" + GROUP_ATTR_SUBJECT + ")");
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            
        	// simply delete all original table and index
        	//
        	Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            
        	if(m_group_data){
            	db.execSQL("DROP INDEX IF EXISTS " + DATABASE_TABLE_GROUP_SUB_INDEX + " ON " + DATABASE_TABLE_GROUP);
            }
        	
            db.execSQL("DROP TABLE IF EXISTS " + (m_group_data?DATABASE_TABLE_GROUP:DATABASE_TABLE));
            
            onCreate(db);
        }
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public MailDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    public MailDbAdapter open() throws SQLException {
    	if(mDbHelper != null){
    		close();
    	}
        mDbHelper = new DatabaseHelper(mCtx,false);
        mDb = mDbHelper.getWritableDatabase();
        
        mDbHelper_group = new DatabaseHelper(mCtx,true);
        mDb_group = mDbHelper_group.getWritableDatabase();
        
        return this;
    }

    public void close() {
    	if(mDbHelper != null){
    		mDbHelper.close();
            mDbHelper = null;
            mDb = null;
    	}
    	
    	if(mDbHelper_group != null){
    		mDbHelper_group.close();
    		mDbHelper_group = null;
    		mDb_group = null;
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
        
        values.put(ATTR_DATE,Long.toString(_mail.GetSendDate().getTime()));
        values.put(ATTR_FLAG,_mail.GetFlags());

        long t_mailID;
        
    	if(_replyGroupId != null){
    		// is sent mail 
    		//
    		values.put(ATTR_READ,1);
    		
    		t_mailID = mDb.insert(DATABASE_TABLE, null, values);
    		
    		//TODO: update the former group
			//
    		Cursor t_cursor = mDb_group.query(DATABASE_TABLE_GROUP,fsm_groupfullColoumns,KEY_ID + "=" + _replyGroupId + "",
										null,null,null,null);
    		
    		
    		return _replyGroupId;
    		
    	}else{
    		  
    		t_mailID = mDb.insert(DATABASE_TABLE, null, values);
    		
    		String t_subject = groupSubject(_mail.GetSubject());
    		
    		Cursor t_cursor = mDb_group.query(DATABASE_TABLE_GROUP,fsm_groupfullColoumns,GROUP_ATTR_SUBJECT + "='" + t_subject + "'",
    											null,null,null,null);
    		
    		if(t_cursor != null && t_cursor.getCount() == 0){

    			//TODO: update the former group
    			//
    			
    			return t_cursor.getInt(0); 
    			
    		}else{
    			// can't find the old group
    			// create a insert one
    			//    			
    			return insertGroup(t_subject,t_mailID,_mail);
    		}
    	}
    }
    
    private boolean updateGroup(Cursor _groupCursor,fetchMail _mail){
    	long t_id = _groupCursor.getLong(0);
    }
    
    /**
     *  insert a new group to the group table
     * @param _subject group subject
     * @param _mailID first mail id
     * @param _mail mail data
     * @return new group inserted _id
     */
    private long insertGroup(String _subject,long _mailID,fetchMail _mail){
    	
		ContentValues group = new ContentValues();
		group.put(GROUP_ATTR_READ,0);
		group.put(GROUP_ATTR_MARK,0);
		
		group.put(GROUP_ATTR_SUBJECT,_subject);
		if(_mail.GetContain().length() != 0){
			group.put(GROUP_ATTR_LEATEST_BODY,_mail.GetContain());
		}else{
			group.put(GROUP_ATTR_LEATEST_BODY,"HTML");
		}
		
		group.put(GROUP_ATTR_LEATEST_TIME,Long.toString(_mail.GetSendDate().getTime()));
		group.put(GROUP_ATTR_ADDR_LIST,_mail.GetFromString());
		group.put(GROUP_ATTR_GROUP,""); // reverse attribute
		group.put(GROUP_ATTR_MAIL_INDEX,Long.toString(_mailID) + ",");    			
		
		return mDb_group.insert(DATABASE_TABLE_GROUP, null, group);
    }

    public boolean deleteMail(long id) {
    	if(mDbHelper == null){
    		open();
    	}
        return mDb.delete(DATABASE_TABLE, ATTR_ID + "=" + id, null) > 0;
    }
	
    
            
    public Cursor fetchAllNotes() {
    	if(mDbHelper == null){
    		open();
    	}
    	
        return mDb.query(DATABASE_TABLE,fsm_fullColoumns, null, null, null, null, null);
    }

    public Cursor fetchNote(long rowId) throws SQLException {

    	if(mDbHelper == null){
    		open();
    	}
    	
        Cursor mCursor = mDb.query(true, DATABASE_TABLE, fsm_fullColoumns, ATTR_ID + "=" + rowId, null,
                    				null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        
        return mCursor;

    }

}
