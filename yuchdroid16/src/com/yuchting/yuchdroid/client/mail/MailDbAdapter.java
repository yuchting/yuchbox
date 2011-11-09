package com.yuchting.yuchdroid.client.mail;

import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

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
	
	// mail sent flag
	//	
	public final static int	MAIL_SENT_FLAG_ERROR	= 0x0001;
	public final static int	MAIL_SENT_FLAG_PADDING	= 0x0002;
	public final static int	MAIL_SENT_FLAG_SENDING	= 0x0004;
	public final static int	MAIL_SENT_FLAG_OK		= 0x0008;
	
	private static final int DATABASE_VERSION 		= 1;
	
	public final static String KEY_ID		 			= "_id";

	// mail group attribute
	//
	public final static String GROUP_ATTR_MARK			= "group_mark";
		
	public final static String GROUP_ATTR_GROUP_FLAG		= "group_flag";
	
	public final static String GROUP_ATTR_SUBJECT			= "group_subject";
	public final static String GROUP_ATTR_LEATEST_BODY	= "group_body";
	public final static String GROUP_ATTR_LEATEST_TIME	= "group_time";
	public final static String GROUP_ATTR_ADDR_LIST		= "group_addr";
	public final static String GROUP_ATTR_GROUP			= "group_group"; // reverse attribute 
	public final static String GROUP_ATTR_MAIL_INDEX		= "group_mail";
		
	
	// mail entry attribute
	//
	public final static String ATTR_MARK				= "mail_mark";
	
	public final static String ATTR_GROUP_FLAG		= "mail_group_flag";
	
	public final static String ATTR_INDEX				= "mail_index";
		
	public final static String ATTR_SUBJECT 			= "mail_sub";  
	public final static String ATTR_BODY 				= "mail_body";
	public final static String ATTR_BODY_HTML 		= "mail_body_html";
	public final static String ATTR_BODY_HTML_TYPE	= "mail_body_html_type";
	
	public final static String ATTR_TO				= "mail_to";
	public final static String ATTR_CC				= "mail_cc";
	public final static String ATTR_BCC				= "mail_bcc";
	public final static String ATTR_FROM				= "mail_from";
	public final static String ATTR_REPLY				= "mail_reply";
	public final static String ATTR_GROUP				= "mail_group";
	
	public final static String ATTR_DATE				= "mail_date";
	public final static String ATTR_RECV_DATE			= "mail_recv_date";
	public final static String ATTR_FLAG				= "mail_flag";
	
	public final static String ATTR_ATTACHMENT		= "mail_attach";
	public final static String ATTR_MAIL_OWN_ACCOUNT	= "mail_own_account";
	
	public final static String ATTR_MAIL_GROUP_INDEX	= "mail_group_index";
	
	public final static String ATTR_MAIL_SEND_REF_MAIL_ID = "mail_send_ref_id";
	public final static String ATTR_MAIL_SEND_REF_MAIL_STYLE = "mail_send_ref_style";
	
	private static final String TAG 					= "MailDbAdapter";
	
	public static final String[] fsm_groupfullColoumns = 
    {
		KEY_ID, 
		
		GROUP_ATTR_MARK,
				
		GROUP_ATTR_GROUP_FLAG,
		
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
		
		ATTR_MARK,
		
		ATTR_GROUP_FLAG,
		
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
        ATTR_RECV_DATE,
        ATTR_MAIL_GROUP_INDEX,
        ATTR_ATTACHMENT,
        ATTR_MAIL_OWN_ACCOUNT,
        ATTR_MAIL_SEND_REF_MAIL_ID,
        ATTR_MAIL_SEND_REF_MAIL_STYLE,
        ATTR_FLAG,
    };
	
	private final static String fsm_createTable_group = 
							"create table " + DATABASE_TABLE_GROUP + 
						    " ("+KEY_ID+" integer primary key, " +
						    
						    GROUP_ATTR_MARK + " smallint," +
						    
						    GROUP_ATTR_GROUP_FLAG + " integer," +
						    
						    GROUP_ATTR_SUBJECT + " text, " +
						    
						    GROUP_ATTR_LEATEST_BODY + " text, " +
						    GROUP_ATTR_LEATEST_TIME +  " integer(64), " +
						    
						    GROUP_ATTR_ADDR_LIST + " text, " +
						    GROUP_ATTR_GROUP + " text, " +
						    GROUP_ATTR_MAIL_INDEX + " text )";
	
	private final static String fsm_createTable = 
		
							"create table " + DATABASE_TABLE + 
					        " ("+KEY_ID+" integer primary key, " +
					        
					        ATTR_MARK + " smallint," +
					        
					        ATTR_GROUP_FLAG + " smallint," +
					        
					        ATTR_INDEX + " integer, " +
					        
					        ATTR_SUBJECT + " text, " +
					        ATTR_BODY +  " text, " +
					        ATTR_BODY_HTML + " text, " + 
					        ATTR_BODY_HTML_TYPE + " text, " + 
					        
					        ATTR_TO + " text, " + 
					        ATTR_CC + " text, " +
					        ATTR_BCC + " text, " +
					        ATTR_FROM + " text, " + 
					        ATTR_REPLY + " text, " +
					        ATTR_GROUP + " text, " +
					        
					        ATTR_DATE + " integer(64), " +
					        ATTR_RECV_DATE + " integer(64), " +
					        ATTR_MAIL_GROUP_INDEX + " integer(64), " +
					        ATTR_ATTACHMENT + " text, " + 
					        ATTR_MAIL_OWN_ACCOUNT + " text, " +
					        ATTR_MAIL_SEND_REF_MAIL_ID + " integer(64), "+
					        ATTR_MAIL_SEND_REF_MAIL_STYLE + " smallint, " +
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

    public final static String[] fsm_groupSubjectPrefix = 
    {
    	"Re: ",
    	"Re:",
    	"Re： ",
    	"Re：",
    	"RE: ",
    	"RE:",
    	"RE： ",
    	"RE：",
    	"回复: ",
    	"回复:",
    	"回复： ",
    	"回复：",
    	"答复: ",
    	"答复:",
    	"答复： ",
    	"答复：",
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
    			_orgSub = _orgSub.substring(t_index + pre.length());
    			break;
    		}
    	}
    	
    	return _orgSub.replace('\'', ' ');
    }
    
    public static String getReplySubject(String _orgSub,String _prefix){
    	for(String pre:fsm_groupSubjectPrefix){
    		if(_orgSub.startsWith(pre)){
    			return _orgSub;
    		}
    	}
    	
    	return _prefix + _orgSub;
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
    
    
    public long createMail(fetchMail _mail,long _replyGroupId,boolean _compose){
        
    	if(mDbHelper == null){
    		open();
    	}
    	
    	// create the values data of this mail
    	//
    	ContentValues values = getMailContentValues(_mail);
        
        long t_mailID;
        
    	if(_replyGroupId != -1){
    		
    		values.put(ATTR_MAIL_GROUP_INDEX,_replyGroupId);
    		
    		t_mailID = mDb.insert(DATABASE_TABLE, null, values);
    		
    		_mail.setGroupIndex(_replyGroupId);
    		_mail.setDbIndex(t_mailID);
    		
    		// update the former group
			//
    		Cursor t_cursor = mDb.query(DATABASE_TABLE_GROUP,fsm_groupfullColoumns,KEY_ID + "=" + _replyGroupId + "",
    											null,null,null,null);
    		try{
        		updateGroup(t_cursor,_mail,t_mailID);
    		}finally{
    			t_cursor.close();
    		}
    		
    	}else{
    				  
    		t_mailID = mDb.insert(DATABASE_TABLE, null, values);
    		
    		if(_compose){
    			//compose Mail
    			//
    			_replyGroupId = insertGroup(_mail.GetSubject(),t_mailID,_mail);
    		}else{
        		// receive mail
        		//    
    			String t_subject = groupSubject(_mail.GetSubject());
        		
        		Cursor t_cursor = mDb.query(DATABASE_TABLE_GROUP,fsm_groupfullColoumns,GROUP_ATTR_SUBJECT + "='" + t_subject + "'",
        											null,null,null,null);
        		try{
        			if(t_cursor.getCount() != 0){

            			// update the former group
            			//
            			updateGroup(t_cursor,_mail,t_mailID);
            			_replyGroupId = t_cursor.getLong(t_cursor.getColumnIndex(KEY_ID));    			
            		}else{
            			
            			// can't find the old group
            			// create a insert one
            			//    			
            			_replyGroupId = insertGroup(t_subject,t_mailID,_mail);
            		}
        		}finally{
        			t_cursor.close();
        		}
    		}
    		_mail.setDbIndex(t_mailID);
    		_mail.setGroupIndex(_replyGroupId);
    		
    		// update the mail group id
    		//
    		values.clear();
    		values.put(ATTR_MAIL_GROUP_INDEX,_replyGroupId);
    		mDb.update(DATABASE_TABLE, values, KEY_ID + "=" + t_mailID, null);
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
    
    private boolean updateGroup(Cursor _groupCursor,fetchMail _mail,long _mailIndex){

    	if(!_groupCursor.moveToFirst()){
    		// move to the frist and retrieve the data correctly 
			//
    		return false;
    	}
    	
    	long t_id 			= _groupCursor.getLong(_groupCursor.getColumnIndex(KEY_ID));
    	int t_mark 			= _groupCursor.getInt(_groupCursor.getColumnIndex(GROUP_ATTR_MARK));
    	String t_sub 		= _groupCursor.getString(_groupCursor.getColumnIndex(GROUP_ATTR_SUBJECT));
    	
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
    										_mailIndex + fetchMail.fsm_vectStringSpliter;
    	
    	ContentValues group = new ContentValues();
    	
		group.put(GROUP_ATTR_MARK,t_mark);
		group.put(GROUP_ATTR_GROUP_FLAG,_mail.getGroupFlag());
				
		group.put(GROUP_ATTR_SUBJECT,t_sub);
		group.put(GROUP_ATTR_LEATEST_BODY,getDisplayMailBody(_mail));
		
		group.put(GROUP_ATTR_LEATEST_TIME,_mail.getRecvMailTime());
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
    private long insertGroup(String _subject,long _mailID,fetchMail _mail){
    	
		ContentValues group = new ContentValues();
		group.put(GROUP_ATTR_MARK,0);
		
		group.put(GROUP_ATTR_GROUP_FLAG,_mail.getGroupFlag());
		
		group.put(GROUP_ATTR_SUBJECT,_subject);
		group.put(GROUP_ATTR_LEATEST_BODY,getDisplayMailBody(_mail));
		
		group.put(GROUP_ATTR_LEATEST_TIME,_mail.getRecvMailTime());
		group.put(GROUP_ATTR_ADDR_LIST,_mail.GetFromString());
		group.put(GROUP_ATTR_GROUP,""); // reverse attribute
		group.put(GROUP_ATTR_MAIL_INDEX,Long.toString(_mailID) + fetchMail.fsm_vectStringSpliter);    			
		
		return mDb.insert(DATABASE_TABLE_GROUP, null, group);
    }

    public boolean deleteGroup(long _groupId){
    	if(mDbHelper == null){
    		open();
    	}
    	
    	Cursor c = fetchGroup(_groupId); 
    	try{
    		if(c.getCount() > 0){
        		String mailListIndex = c.getString(c.getColumnIndex(GROUP_ATTR_MAIL_INDEX));
        		final String[] list = mailListIndex.split(fetchMail.fsm_vectStringSpliter);
        		if(list.length >= 0){

            		new Thread(){
            			public void run(){
            				for(String id:list){
            					deleteMail(Long.valueOf(id).longValue());
            				}
            			}
            		}.start();
        		}
        	}
    	}finally{
    		c.close();
    	}  	

        return mDb.delete(DATABASE_TABLE_GROUP, KEY_ID + "=" + _groupId, null) > 0;
    }
    
    public boolean deleteMail(long id) {
    	if(mDbHelper == null){
    		open();
    	}

        return mDb.delete(DATABASE_TABLE, KEY_ID + "=" + id, null) > 0;
    }
            
    public Cursor fetchAllGroup(int _limit){
    	if(mDbHelper == null){
    		open();
    	}
    	
        return mDb.query(DATABASE_TABLE_GROUP,fsm_groupfullColoumns, 
        					null, null, null, null, GROUP_ATTR_LEATEST_TIME + " DESC",Integer.toString(_limit));
    }
    
    public Cursor fetchAllGroupAddrList(){
    	if(mDbHelper == null){
    		open();
    	}
    	
        return mDb.query(DATABASE_TABLE_GROUP,new String[]{GROUP_ATTR_ADDR_LIST}, 
        					null, null, null, null, GROUP_ATTR_LEATEST_TIME + " DESC");
    }
    
    public Cursor fetchGroup(long _groupId) throws SQLException{

    	if(mDbHelper == null){
    		open();
    	}
    	
        Cursor t_cursor = mDb.query(true, DATABASE_TABLE_GROUP, fsm_groupfullColoumns, KEY_ID + "=" + _groupId, null,
                    				null, null, null, null);
        if (t_cursor != null) {
        	t_cursor.moveToFirst();
        }
        
        return t_cursor;
    }
    
    public Cursor fetchMailCursor(long _mailId)throws SQLException{
    	if(mDbHelper == null){
    		open();
    	}
    	
    	Cursor t_cursor = mDb.query(true, DATABASE_TABLE, fsm_mailfullColoumns, KEY_ID + "=" + _mailId, null,
										null, null, null, null);
		if (t_cursor != null) {
			t_cursor.moveToFirst();
		}
		
		return t_cursor;
    }
    
    public fetchMail fetchMail(long _mailId)throws SQLException{
    	Cursor c = fetchMailCursor(_mailId);
		try{
			if(c.getCount() != 0){
				return convertMail(c);
			}else{
				return null;
			}
		}finally{
			c.close();
		}
    }
    
    public void updateMail(fetchMail _mail){
    	ContentValues values = getMailContentValues(_mail);
    	
    	mDb.update(DATABASE_TABLE,values,KEY_ID + "=" + _mail.getDbIndex(),null);
    }
    

    public static boolean modifiedUnreadFlag(AtomicReference<Integer> _flag){
    	
    	if(_flag.get() == fetchMail.GROUP_FLAG_RECV){
    		_flag.set(fetchMail.GROUP_FLAG_RECV_READ);
    		return true;
    	}else if(_flag.get() == fetchMail.GROUP_FLAG_RECV_ATTACH){
    		_flag.set(fetchMail.GROUP_FLAG_RECV_READ_ATTACH);
    		return true;
    	}
    	
    	return false;
    }
    
    public void markGroupRead(long _groupId){
    	
    	Cursor t_cursor = fetchGroup(_groupId);
    	
    	if(t_cursor != null && t_cursor.getCount() > 0){
    		t_cursor.moveToFirst();
    	}else{
    		t_cursor.close();
    		return;
    	}
    	
    	AtomicReference<Integer> t_flag = new AtomicReference<Integer>(t_cursor.getInt(t_cursor.getColumnIndex(GROUP_ATTR_GROUP_FLAG)));
    	t_cursor.close();
    	
    	if(modifiedUnreadFlag(t_flag)){
    		ContentValues values = new ContentValues();    		
    		values.put(GROUP_ATTR_GROUP_FLAG,t_flag.get());
    		
    		mDb.update(DATABASE_TABLE_GROUP, values, KEY_ID + "=" + _groupId, null);
    	}
    }
    
    public void markMailRead(long _mailId){
    	
    	Cursor t_cursor = fetchMailCursor(_mailId);
    	
    	if(t_cursor != null && t_cursor.getCount() > 0){
    		t_cursor.moveToFirst();
    	}else{
    		t_cursor.close();
    		return;
    	}
    	
    	AtomicReference<Integer> t_flag = new AtomicReference<Integer>(t_cursor.getInt(t_cursor.getColumnIndex(ATTR_GROUP_FLAG)));
    	t_cursor.close();
    	
    	if(modifiedUnreadFlag(t_flag)){
    		ContentValues values = new ContentValues();    		
    		values.put(ATTR_GROUP_FLAG,t_flag.get());
    		
    		mDb.update(DATABASE_TABLE, values, KEY_ID + "=" + _mailId,null);
    	}
    }
    
    public void setMailGroupFlag(long _mailId,long _groupId,int _flag){
    	
    	ContentValues values = new ContentValues();    		
		values.put(ATTR_GROUP_FLAG,_flag);
		mDb.update(DATABASE_TABLE, values, KEY_ID + "=" + _mailId,null);
		
		// fetch the group and change the flag if the latest mail is that mailId
		//
		Cursor t_cursor = fetchGroup(_groupId);
    	if(t_cursor != null && t_cursor.getCount() > 0){
    		t_cursor.moveToFirst();
    	}else{
    		t_cursor.close();
    		return;
    	}
    	
    	String t_mailIndex = t_cursor.getString(t_cursor.getColumnIndex(GROUP_ATTR_MAIL_INDEX));
    	String[] t_mailIndexList = t_mailIndex.split(fetchMail.fsm_vectStringSpliter);
    	
    	t_cursor.close();
    	
    	if(t_mailIndexList.length != 0 
    	&& Integer.valueOf(t_mailIndexList[t_mailIndexList.length - 1]).intValue() == _mailId){
    		ContentValues v = new ContentValues();    		
    		v.put(GROUP_ATTR_GROUP_FLAG,_flag);
    		
    		mDb.update(DATABASE_TABLE_GROUP, v, KEY_ID + "=" + _groupId, null);
    	}
    	
    }
    
    private ContentValues getMailContentValues(fetchMail _mail){
    	ContentValues values = new ContentValues();
        
    	values.put(ATTR_MARK,0);
    	
    	values.put(ATTR_GROUP_FLAG,_mail.getGroupFlag());
    	    	
    	values.put(ATTR_INDEX,_mail.GetMailIndex());
        values.put(ATTR_SUBJECT,_mail.GetSubject());
        values.put(ATTR_BODY,_mail.GetContain().replaceAll("\r\n", "\n"));
        values.put(ATTR_BODY_HTML,_mail.GetContain_html());
        values.put(ATTR_BODY_HTML_TYPE,_mail.GetContain_html_type());
        
        values.put(ATTR_TO,_mail.getSendToString());
        values.put(ATTR_CC,_mail.getCCToString());
        values.put(ATTR_BCC,_mail.getBCCToString());
        values.put(ATTR_FROM,_mail.GetFromString());
        values.put(ATTR_REPLY,_mail.getReplyString());
        values.put(ATTR_GROUP,_mail.getGroupString());
        
        values.put(ATTR_DATE,_mail.GetSendDate().getTime());
        values.put(ATTR_RECV_DATE,_mail.getRecvMailTime());
        values.put(ATTR_FLAG,_mail.GetFlags());
        values.put(ATTR_ATTACHMENT,_mail.getAttachmentString());
        values.put(ATTR_MAIL_OWN_ACCOUNT,_mail.getOwnAccount());
        values.put(ATTR_MAIL_SEND_REF_MAIL_ID,_mail.getSendRefMailIndex());
        values.put(ATTR_MAIL_SEND_REF_MAIL_STYLE,_mail.getSendRefMailStyle());
                
        return values;
    }
    
    public fetchMail convertMail(Cursor _mailCursor){
    	_mailCursor.moveToFirst();
    	
    	fetchMail t_mail = new fetchMail();
    	        
    	t_mail.setDbIndex(_mailCursor.getLong(_mailCursor.getColumnIndex(KEY_ID)));
    	t_mail.setGroupIndex(_mailCursor.getLong(_mailCursor.getColumnIndex(ATTR_MAIL_GROUP_INDEX)));
    	t_mail.SetMailIndex(_mailCursor.getInt(_mailCursor.getColumnIndex(ATTR_INDEX)));
    	t_mail.setGroupFlag(_mailCursor.getInt(_mailCursor.getColumnIndex(ATTR_GROUP_FLAG)));
    	t_mail.SetFlags(_mailCursor.getInt(_mailCursor.getColumnIndex(ATTR_FLAG)));
    	
    	t_mail.SetSubject(_mailCursor.getString(_mailCursor.getColumnIndex(ATTR_SUBJECT)));
    	t_mail.SetContain(_mailCursor.getString(_mailCursor.getColumnIndex(ATTR_BODY)));
    	t_mail.SetContain_html(_mailCursor.getString(_mailCursor.getColumnIndex(ATTR_BODY_HTML)),
    			_mailCursor.getString(_mailCursor.getColumnIndex(ATTR_BODY_HTML_TYPE)));
    	
    	t_mail.SetSendDate(new Date(_mailCursor.getLong(_mailCursor.getColumnIndex(ATTR_DATE))));
    	
    	t_mail.SetSendToVect(_mailCursor.getString(_mailCursor.getColumnIndex(ATTR_TO)).split(fetchMail.fsm_vectStringSpliter));
    	t_mail.SetCCToVect(_mailCursor.getString(_mailCursor.getColumnIndex(ATTR_CC)).split(fetchMail.fsm_vectStringSpliter));
    	t_mail.SetBCCToVect(_mailCursor.getString(_mailCursor.getColumnIndex(ATTR_BCC)).split(fetchMail.fsm_vectStringSpliter));
    	t_mail.SetFromVect(_mailCursor.getString(_mailCursor.getColumnIndex(ATTR_FROM)).split(fetchMail.fsm_vectStringSpliter));
    	t_mail.SetReplyToVect(_mailCursor.getString(_mailCursor.getColumnIndex(ATTR_REPLY)).split(fetchMail.fsm_vectStringSpliter));
    	t_mail.SetGroupVect(_mailCursor.getString(_mailCursor.getColumnIndex(ATTR_GROUP)).split(fetchMail.fsm_vectStringSpliter));
    	t_mail.setAttachmentByString(_mailCursor.getString(_mailCursor.getColumnIndex(ATTR_ATTACHMENT)).split(fetchMail.fsm_vectStringSpliter));
    	t_mail.setOwnAccount(_mailCursor.getString(_mailCursor.getColumnIndex(ATTR_MAIL_OWN_ACCOUNT)));
    
    	t_mail.setSendRefMailIndex(_mailCursor.getLong(_mailCursor.getColumnIndex(ATTR_MAIL_SEND_REF_MAIL_ID)));
    	t_mail.setSendRefMailStyle(_mailCursor.getInt(_mailCursor.getColumnIndex(ATTR_MAIL_SEND_REF_MAIL_STYLE)));
    	t_mail.setRecvMailTime(_mailCursor.getLong(_mailCursor.getColumnIndex(ATTR_RECV_DATE)));
    	
    	return t_mail;
    }

}
