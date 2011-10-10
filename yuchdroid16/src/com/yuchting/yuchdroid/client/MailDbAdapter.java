package com.yuchting.yuchdroid.client;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MailDbAdapter {
	
	private final static String DATABASE_TABLE	= "yuch_mail";
	
	private static final String DATABASE_NAME 	= "yuch_mail_data";
	private static final int DATABASE_VERSION 	= 1;
	
	public final static String KEY_ID		 		= "_id";
	
	public final static String KEY_INDEX			= "mail_index";
	public final static String KEY_SUBJECT 		= "mail_sub";  
	public final static String KEY_BODY 			= "mail_body";
	public final static String KEY_BODY_HTML 		= "mail_body_html";
	public final static String KEY_BODY_HTML_TYPE	= "mail_body_html_type";
	
	public final static String KEY_TO				= "mail_to";
	public final static String KEY_CC				= "mail_cc";
	public final static String KEY_BCC			= "mail_bcc";
	public final static String KEY_FROM			= "mail_from";
	public final static String KEY_REPLY			= "mail_reply";
	public final static String KEY_GROUP			= "mail_group";
	
	public final static String KEY_DATE			= "mail_date";
	public final static String KEY_FLAG			= "mail_flag";
	
	private static final String TAG 				= "MailDbAdapter";
	
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;

	private static final String DATABASE_CREATE =
	        "create table " + DATABASE_TABLE + 
	        " ("+KEY_ID+" integer primary key, " +
	        KEY_INDEX + " integer, " +
	        KEY_SUBJECT + " text, " +
	        KEY_BODY +  " text, " +
	        KEY_BODY_HTML + " text, " + 
	        KEY_BODY_HTML_TYPE + " text, " + 
	        KEY_TO + " text not null, " + 
	        KEY_CC + " text, " +
	        KEY_BCC + " text, " +
	        KEY_FROM + " text not null, " + 
	        KEY_REPLY + " text, " +
	        KEY_GROUP + " text, " +
	        KEY_DATE + " text, " +
	        KEY_FLAG + " integer )" ;
	
	private final Context mCtx;

	private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS notes");
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

    /**
     * Open the notes database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public MailDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }

    public long createMail(fetchMail _mail) {
        
    	ContentValues values = new ContentValues();
        
    	values.put(KEY_INDEX,_mail.GetMailIndex());
        values.put(KEY_SUBJECT,_mail.GetSubject());
        values.put(KEY_BODY,_mail.GetContain());
        values.put(KEY_BODY_HTML,_mail.GetContain_html());
        values.put(KEY_BODY_HTML_TYPE,_mail.GetContain_html_type());
        
        values.put(KEY_TO,_mail.getSendToString());
        values.put(KEY_CC,_mail.getCCToString());
        values.put(KEY_BCC,_mail.getBCCToString());
        values.put(KEY_FROM,_mail.GetFromString());
        values.put(KEY_REPLY,_mail.getReplyString());
        values.put(KEY_GROUP,_mail.getGroupString());
        
        values.put(KEY_DATE,Long.toString(_mail.GetSendDate().getTime()));
        values.put(KEY_FLAG,_mail.GetFlags());
        
        return mDb.insert(DATABASE_TABLE, null, values);
    }

    public boolean deleteMail(long id) {
        return mDb.delete(DATABASE_TABLE, KEY_ID + "=" + id, null) > 0;
    }
	
    private static String[] getColumns(){
    	return new String[] {
        		KEY_ID, 
        		
        		KEY_INDEX,
        		KEY_SUBJECT,
                KEY_BODY,
                KEY_BODY_HTML,
                KEY_BODY_HTML_TYPE,
                
                KEY_TO,
                KEY_CC,
                KEY_BCC,
                KEY_FROM,
                KEY_REPLY,
                KEY_GROUP,
                
                KEY_DATE,
                KEY_FLAG};
    }
    
    public Cursor fetchAllNotes() {
        return mDb.query(DATABASE_TABLE,getColumns(), null, null, null, null, null);
    }

    public Cursor fetchNote(long rowId) throws SQLException {

        Cursor mCursor =

            mDb.query(true, DATABASE_TABLE, getColumns(), KEY_ID + "=" + rowId, null,
                    null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        
        return mCursor;

    }

}
