/**
 *  Dear developer:
 *  
 *   If you want to modify this file of project and re-publish this please visit:
 *  
 *     http://code.google.com/p/yuchberry/wiki/Project_files_header
 *     
 *   to check your responsibility and my humble proposal. Thanks!
 *   
 *  -- 
 *  Yuchs' Developer    
 *  
 *  
 *  
 *  
 *  尊敬的开发者：
 *   
 *    如果你想要修改这个项目中的文件，同时重新发布项目程序，请访问一下：
 *    
 *      http://code.google.com/p/yuchberry/wiki/Project_files_header
 *      
 *    了解你的责任，还有我卑微的建议。 谢谢！
 *   
 *  -- 
 *  语盒开发者
 *  
 */
package com.yuchting.yuchdroid.client;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.Contacts;
import android.provider.Contacts.People;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.yuchting.yuchdroid.client.mail.HomeActivity;
import com.yuchting.yuchdroid.client.mail.MailDbAdapter;
import com.yuchting.yuchdroid.client.mail.fetchMail;

public class YuchDroidApp extends Application {
	
	public final static String	TAG = "YuchsBoxApp";
	
	public 	static String fsm_PIN			= "";
	public static String fsm_IMEI			= "ad";
	public static String fsm_android_ver	= "1.6";
	public static String fsm_phone_model 	= "unknown";
	public static String fsm_appVersion 	= "unknown";
		
	// send broadcast intent filter
	//
	public final static	String	FILTER_CONNECT_STATE 	= TAG + "_FILTER_CS";
	public final static	String	FILTER_DEBUG_INFO 		= TAG + "_FILTER_DI";
	public final static	String	FILTER_MARK_MAIL_READ	= TAG + "_FILTER_MMR";
	public final static	String	FILTER_SEND_MAIL		= TAG + "_FILTER_SM";
	public final static	String	FILTER_MAIL_GROUP_FLAG	= TAG + "_FILTER_MGF";
	public final static	String	FILTER_RECV_MAIL		= TAG + "_FILTER_RM";
	public final static	String	FILTER_DELETE_MAIL		= TAG + "_FILTER_DM";
	public final static	String	FILTER_DOWNLOAD_ATT_DONE= TAG + "_FILTER_ATT";
	
	// FILTER_MARK_MAIL_READ broadcast parameters data
	//
	public final static	String	DATA_FILTER_MARK_MAIL_READ_GROUPID		= "groupId";
	public final static	String	DATA_FILTER_MARK_MAIL_READ_MAIL_HASH	= "mailHash";
	public final static	String	DATA_FILTER_MARK_MAIL_READ_MAILID	= "messageID";
	
	
	// FILTER_SEND_MAIL broadcast parameters data
	//
	public final static	String	DATA_FILTER_SEND_MAIL_STYLE			= "style";
	
	// FILTER_MAIL_GROUP_FLAG broadcast parameters data
	//
	public final static	String	DATA_FILTER_MAIL_GROUP_FLAG_GROUP_ID	= "groupId";
	public final static	String	DATA_FILTER_MAIL_GROUP_FLAG_MAIL_ID		= "mailId";
	public final static	String	DATA_FILTER_MAIL_GROUP_FLAG_GROUP_FLAG	= "groupFlag";
	public final static	String	DATA_FILTER_MAIL_GROUP_FLAG_REFRESH_BODY= "refreshBody";
	
	// FILTER_DOWNLOAD_ATT_DONE broadcast parameters data
	//
	public final static	String	DATA_FILTER_DOWNLOAD_ATT_DONE_INDEX		= "mailId";
	public final static	String	DATA_FILTER_DOWNLOAD_ATT_DONE_ATT_INDEX	= "mailAttId";
	public final static	String	DATA_FILTER_DOWNLOAD_ATT_DONE_MSG_ID	= "messageId";
		
	// connect state
	//
	public final static	int				STATE_DISCONNECT	= 0;
	public final static	int				STATE_CONNECTING	= 1;
	public final static	int				STATE_CONNECTED		= 2;
				
	// notification system varaibles
	//
	public final static	int				YUCH_NOTIFICATION_CONNECT_STATE	= 0;
	public final static	int				YUCH_NOTIFICATION_MAIL			= 1;
	public final static	int				YUCH_NOTIFICATION_MAIL_ATT		= 2;
	public final static	int				YUCH_NOTIFICATION_MAIL_ATT_SEND	= 3;
	public final static	int				YUCH_NOTIFICATION_MAIL_FAILED	= 4;
		
	private int m_mailNotificationNum		= 0;
	
	public static int sm_displyWidth		= 320;
	public static int sm_displyHeight		= 480;
	
	public boolean			m_hasUnhandledException = false;
	public Vector<String>	m_errorList = new Vector<String>();
	
	public MailDbAdapter	m_dba		= new MailDbAdapter(this);
	public ConfigInit		m_config 	= null;
	
	public boolean			m_connectDeamonRun	= false;
	public int				m_connectState		= STATE_DISCONNECT;
	
	public String[]			m_mailAddressList	= new String[]{""};
	private Vector<String>	m_mailAddrSearch	= new Vector<String>();
		
	// reference fetch Mail
	//
	public fetchMail		m_composeRefMail;
	public fetchMail		m_composeStyleRefMail;
	
	public boolean			m_isOfficalHost		= true;
		
	@Override
	public void onCreate (){
		super.onCreate();
		
		// start own unhandled exception handler
		//
		ExceptionHandler.registerHandler(this);
		
		// check the former unhandled file
		//
		checkFormerUnhandleFiles();
		
		
		m_config = new ConfigInit(this);
				
		// get the PIN string (android id)
		//
		fsm_PIN = android.provider.Settings.System.getString(getContentResolver(), "android_id");
		if(fsm_PIN == null){fsm_PIN = "unknown";}
		
		fsm_phone_model = android.os.Build.MODEL;
		if(fsm_phone_model == null){fsm_phone_model = "unknown";}
		
		fsm_android_ver = android.os.Build.VERSION.RELEASE;
		if(fsm_android_ver == null){fsm_android_ver = "unknown";}
		
		// the app version
		//
		fsm_appVersion = getVersionName(this,ConnectDeamon.class);
		
		m_dba.open();
		m_config.WriteReadIni(true);
		
		Display display = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		sm_displyWidth = display.getWidth();
		sm_displyHeight = display.getHeight();
				
		// construct mail address list
		//
		(new Thread(){
			public void run(){
				try{
					constructMailAddrList();
				}catch(Exception e){
					setErrorString(TAG, e);
				}
			}
		}).start();
		
	}
	
	
	private  void constructMailAddrList(){
	
		m_mailAddrSearch.clear();
		
		// first search from database
		//
		Cursor c = m_dba.fetchAllGroupAddrList();
		try{
														
			if(c != null && c.getCount() > 0){
				
				int t_index = c.getColumnIndex(MailDbAdapter.GROUP_ATTR_ADDR_LIST);
				while(c.moveToNext()){
					String[] t_list = c.getString(t_index).split(fetchMail.fsm_vectStringSpliter);
					
					for(String addr : t_list){
						addMailAddrSearch(addr);									
					}
				}
			}
			
		}finally{
			c.close();
		}
		
		// second search from the contact
		// the People class is Deprecated after 2.1
		//
		// check follow website for detail
		// http://www.coderanch.com/t/512048/Android/Mobile/Contact-API
		//
		
		int sdk = new Integer(android.os.Build.VERSION.SDK).intValue();  
        if(sdk >= 5){  
            try {  
                Class<?> clazz = Class.forName("android.provider.ContactsContract$Contacts");  
                Uri CONTENT_URI = (Uri) clazz.getField("CONTENT_URI").get(clazz);
                
                // find the magic string from the source of android 2.1 OS
                // http://618119.com/archives/2011/01/01/201.html
                //
                String Contacts_ID = "_id";
                String Contacts_NAME = "display_name";
                
                c = getContentResolver().query(CONTENT_URI,new String[]{Contacts_ID,Contacts_NAME},null,null,null);
                try{
                	
                	clazz = Class.forName("android.provider.ContactsContract$CommonDataKinds$Email");  
	                CONTENT_URI = (Uri) clazz.getField("CONTENT_URI").get(clazz);
	                
	                String Contact_ID = "contact_id";
	                String Email_DATA = "data1"; 
	                String id;
	                String name;
	                Cursor emailCur;
	                String email;
	                while(c.moveToNext()){  
	                    id = c.getString(c.getColumnIndex(Contacts_ID));
	                    name = c.getString(c.getColumnIndex(Contacts_NAME));
	                    
	                    emailCur = getContentResolver().query(CONTENT_URI,  
	                            null, Contact_ID+ " = ?", new String[] { id }, null);
	                    if(emailCur != null){
		                    try{
		                    	while(emailCur.moveToNext()){
		                    		email = emailCur.getString(emailCur.getColumnIndex(Email_DATA));
		                    		addMailAddrSearch(name,email);				                    													
			                    }
		                    	
		                    }finally{
		                    	emailCur.close();
		                    }	
	                    }
	                }
                }finally{
                	c.close();
                }
                
            }catch(Throwable t){  
                Log.e(TAG, "Exception when determining CONTENT_URI", t); 
            }
             
        } else {  
        	
            c = getContentResolver().query(Contacts.People.CONTENT_URI, new String[]{People.NAME,People.PRIMARY_EMAIL_ID},
            						null,null,null);
			try{
				String name;
				String emailId;
				String email;
				Cursor emailCur;
				while(c.moveToNext()){
					name = c.getString(c.getColumnIndex(People.NAME));
					emailId = c.getString(c.getColumnIndex(People.PRIMARY_EMAIL_ID));
					
					if (emailId != null) {  
		                emailCur = getContentResolver().query(Contacts.ContactMethods.CONTENT_EMAIL_URI, null,  
		                        							People.PRIMARY_EMAIL_ID + " = ?",new String[] { emailId }, null);
		                if(emailCur != null){
		                	try{
			                	while(emailCur.moveToNext()){  
			                		email = emailCur.getString(emailCur.getColumnIndex(Contacts.ContactMethods.DATA));
			                		addMailAddrSearch(name,email);
			                	}
			                }finally{
			                	emailCur.close();
			                }
		                }
		               
		            } 
				}
			}finally{
				c.close();
			}
            
        } 

        if(m_mailAddrSearch.isEmpty()){
        	m_mailAddressList = new String[]{};
        }else{
        	
        	synchronized (m_mailAddrSearch) {
	        	m_mailAddressList = new String[m_mailAddrSearch.size()];
				int t_count = m_mailAddrSearch.size();
				for(int i = 0;i < t_count;i++){
					m_mailAddressList[i] = m_mailAddrSearch.get(i);
				}
			}
        }
	}
	
	private Thread m_checkOfficalHostThread = null;
	public void checkOfficalHost(){
		
		synchronized (this) {
			if(m_checkOfficalHostThread == null){
				m_checkOfficalHostThread = new Thread(){
					public void run(){
						
						while(true){
							try{
								
								while(ConnectDeamon.CanNotConnectSvr(YuchDroidApp.this)){
									try{
										sleep(10000);
									}catch(Exception e){}
								}								
																
								final String t_mainHost = "http://www.yuchs.com/verOffical/";
								
								StringBuffer t_url = new StringBuffer(t_mainHost);
								t_url.append("?host=").append(URLEncoder.encode(m_config.m_host,"UTF-8"))
									 .append("&port=").append(m_config.m_port);
								
								URL t_request = new URL(t_url.toString());
								
								URLConnection yc = t_request.openConnection();
								yc.setConnectTimeout(10000);
								yc.setReadTimeout(50000);
								BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
								try{
									m_isOfficalHost = in.readLine().equals("true");
								}finally{
									in.close();
								}
								
								break;
								
							}catch(Exception e){
								setErrorString("check offical state failed", e);
							}
						}						
						
						synchronized (YuchDroidApp.this) {
							m_checkOfficalHostThread = null;
						}
					}
				};
				
				m_checkOfficalHostThread.start();
			}
		}
		
	}
	
	private void addMailAddrSearch(String name, String email){
		
		String t_final = null;
		if(email != null && email.length() != 0){
			if(name == null || name.length() == 0){
				t_final = email;
    		}else{
    			t_final = name + " <"+ email+">";
    		}					                			
		}
		
		addMailAddrSearch(t_final);
	}
	
	private void addMailAddrSearch(String _final){
		
		if(_final == null || _final.length() == 0){
			return ;
		}
		
		_final += ",";
		
		boolean t_add = true;
		synchronized (m_mailAddrSearch) {
			for(String has:m_mailAddrSearch){
				if(has.equals(_final)){
					t_add = false;
					break;
				}
			}
		}
			
		if(t_add){
			m_mailAddrSearch.add(_final);
		}		
	}
	
	private void checkFormerUnhandleFiles(){

        // Filter for ".stacktrace" files
		//
        FilenameFilter t_filter = new FilenameFilter() { 
            public boolean accept(File dir, String name) {
                return name.endsWith(ExceptionHandler.fsm_stackstraceSuffix); 
            } 
        };
        
        String[] t_files = getFileStreamPath(".").list(t_filter);
        if(t_files != null && t_files.length != 0){
        	for(String file:t_files){
        		File f = getFileStreamPath(file);
        		if(f.exists()){
        			try{
        				FileInputStream in = openFileInput(file);
        				
        				byte[] t_readBuffer = new byte[(int)f.length()];
        				sendReceive.ForceReadByte(in, t_readBuffer, t_readBuffer.length);
        				
        				setErrorString(new String(t_readBuffer));
        				in.close();
        				
        				m_hasUnhandledException = true;
        				
        			}catch(Exception e){
        				setErrorString("checkFormerUnhandleFiles",e);
        			}        			
        		}
        	}
        }       
	}
	
	public static String getVersionName(Context context, Class<ConnectDeamon> cls){
		try{
			ComponentName comp = new ComponentName(context, cls);
			PackageInfo pinfo = context.getPackageManager().getPackageInfo(comp.getPackageName(), 0);
			return pinfo.versionName;
		}catch(android.content.pm.PackageManager.NameNotFoundException e) {
			return "1.0";
		}
	}
	
	@Override
	public void onTerminate(){
		super.onTerminate();
		
		m_dba.close();
	}
	
	public void setConnectState(int _state){
		if(m_connectState != _state){
			m_connectState = _state;
			
			Intent t_intent = new Intent(FILTER_CONNECT_STATE);	
			sendBroadcast(t_intent);
			
			startConnectNotification(_state,false);
		}
	}
	
	private void checkMainSDCardDir(){
		File t_dir = new File(Environment.getExternalStorageDirectory(),TAG);
		if(!t_dir.exists() || !t_dir.isDirectory()){
			t_dir.mkdir();
		}
	}
	public File getAttachmentDir(){
		checkMainSDCardDir();
		
		File t_dir = new File(Environment.getExternalStorageDirectory(),TAG + "/AttDir");
		if(!t_dir.exists() || !t_dir.isDirectory()){
			t_dir.mkdir();
		}
		
		return t_dir;
	}
	public boolean isSDCardAvailable(){
		// We can read and write the media
		//
		return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
	}
	
	public int getConnectState(){
		return m_connectState;
	}
	
	public void startConnectNotification(int _state,boolean _soundAndLight){
		
		if(!m_config.m_alwaysDisplayState && _state == STATE_CONNECTED){
			stopConnectNotification();
			return;
		}
		
		NotificationManager t_mgr = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		
		int icon;
		int t_titleId;
		int t_detailId = R.string.login_connect_deamon_state_detail;
		if(_state == STATE_CONNECTED){
			t_titleId = R.string.login_connect_deamon_state_connect;
			icon = R.drawable.notification_connect_state_on;        
		}else{
			icon = R.drawable.notification_connect_state_off;
			if(_state == STATE_CONNECTING){
				t_titleId = R.string.login_connect_deamon_state_connecting;
			}else{
				t_titleId = R.string.login_connect_deamon_state_disconnect;
				t_detailId = R.string.login_connect_deamon_state_disconnect_detail;
			}	
		}
		
		CharSequence contentTitle = getString(t_titleId);
		
		Intent notificationIntent = new Intent(Intent.ACTION_MAIN);
		notificationIntent.setClass(this, Yuchdroid16Activity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
				PendingIntent.FLAG_UPDATE_CURRENT );

		// the next two lines initialize the Notification, using the configurations above
		//
		Notification notification = new Notification(icon, getString(R.string.login_connect_deamon_state), System.currentTimeMillis());
		notification.setLatestEventInfo(this, contentTitle, getString(t_detailId), contentIntent);
		
		if(_soundAndLight && m_config.isPromptTime()){
			
			if(m_config.m_connectDisconnectPrompt_sound.length() != 0){
				notification.sound = Uri.parse(m_config.m_connectDisconnectPrompt_sound);
			}
			if(m_config.m_connectDisconnectPrompt_vibrate){
				notification.defaults |= Notification.DEFAULT_VIBRATE;
			}
			
			notification.ledARGB = 0xffff0000;
			notification.ledOnMS = 300;
			notification.ledOffMS = 3000;
			notification.flags |= Notification.FLAG_SHOW_LIGHTS;
		}
		
		// can't not clear
		//
		notification.flags |= Notification.FLAG_NO_CLEAR; 
						
		t_mgr.notify(YuchDroidApp.YUCH_NOTIFICATION_CONNECT_STATE, notification);
	}
	
	public void stopConnectNotification(){
		NotificationManager t_mgr = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		t_mgr.cancel(YUCH_NOTIFICATION_CONNECT_STATE);
	}
	
	/**
	 * trigger mail notification to the user
	 * @param _ctx		notification context
	 * @param _mail		notification mail (set the notification's text)
	 */
	public void TriggerMailNotification(fetchMail _mail){
		
		NotificationManager t_mgr = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		
		int icon = R.drawable.ic_notification_mail;        
		
		CharSequence tickerText = getString(R.string.mail_notification_ticker);
		CharSequence contentTitle = _mail.GetSubject(); 
		CharSequence contentText = MailDbAdapter.getDisplayMailBody(_mail);

		Intent notificationIntent = new Intent(Intent.ACTION_MAIN);
		notificationIntent.setClass(this, HomeActivity.class);		
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
				PendingIntent.FLAG_UPDATE_CURRENT | Notification.FLAG_ONLY_ALERT_ONCE | Notification.FLAG_AUTO_CANCEL);

		// the next two lines initialize the Notification, using the configurations above
		Notification notification = new Notification(icon, tickerText, System.currentTimeMillis());
		notification.setLatestEventInfo(this, contentTitle, contentText, contentIntent);
		
		notification.number = ++m_mailNotificationNum;
		
		// sound & vibrate & LED 
		//
		if(m_config.isPromptTime()){
			if(m_config.m_mailPrompt_sound.length() != 0){
				notification.sound = Uri.parse(m_config.m_mailPrompt_sound);
			}
			if(m_config.m_mailPrompt_vibrate){
				notification.defaults |= Notification.DEFAULT_VIBRATE;
			}
			
			notification.ledARGB = 0xff00ff00;
			notification.ledOnMS = 300;
			notification.ledOffMS = 5000;
			notification.flags |= Notification.FLAG_SHOW_LIGHTS;
		}		
				
		t_mgr.notify(YuchDroidApp.YUCH_NOTIFICATION_MAIL, notification);
	}
	
	public void StopMailNotification(){
		NotificationManager t_mgr = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		t_mgr.cancel(YUCH_NOTIFICATION_MAIL);
		
		m_mailNotificationNum = 0;
	}
	
	public void triggerMailFailedNotification(fetchMail _mail){
		
		NotificationManager t_mgr = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		
		int icon = R.drawable.ic_notification_mail_failed;        
		
		CharSequence tickerText = getString(R.string.mail_failed_notification_ticker);
		CharSequence contentTitle = _mail.GetSubject(); 
		CharSequence contentText = MailDbAdapter.getDisplayMailBody(_mail);

		Intent notificationIntent = new Intent(Intent.ACTION_MAIN);
		notificationIntent.setClass(this, HomeActivity.class);		
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
				PendingIntent.FLAG_UPDATE_CURRENT | Notification.FLAG_ONLY_ALERT_ONCE | Notification.FLAG_AUTO_CANCEL);

		// the next two lines initialize the Notification, using the configurations above
		Notification notification = new Notification(icon, tickerText, System.currentTimeMillis());
		notification.setLatestEventInfo(this, contentTitle, contentText, contentIntent);
				
		// sound & vibrate & LED 
		//
		if(m_config.isPromptTime()){
			if(m_config.m_mailPrompt_sound.length() != 0){
				notification.sound = Uri.parse(m_config.m_mailPrompt_sound);
			}
			if(m_config.m_mailPrompt_vibrate){
				notification.defaults |= Notification.DEFAULT_VIBRATE;
			}
			
			notification.ledARGB = 0xffff0000;
			notification.ledOnMS = 300;
			notification.ledOffMS = 5000;
			notification.flags |= Notification.FLAG_SHOW_LIGHTS;
		}		
				
		t_mgr.notify(YuchDroidApp.YUCH_NOTIFICATION_MAIL_FAILED, notification);
	}
	
	public void stopMailFailedNotification(){
		NotificationManager t_mgr = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		t_mgr.cancel(YUCH_NOTIFICATION_MAIL_FAILED);		
	}
	
	public void sendMail(fetchMail _mail,fetchMail _refMail,int _refStyle){
		// check the MailComposeActivity.send for detail
		
		assert _mail != null;
		assert _refMail != null || _refStyle == fetchMail.NOTHING_STYLE;
		
		// prepare the temporary data
		//
		m_composeRefMail 		= _mail;
		m_composeStyleRefMail 	= _refMail;
		
		// broadcast to ConnectDeamon
		//
		Intent in = new Intent(YuchDroidApp.FILTER_SEND_MAIL);
		in.putExtra(YuchDroidApp.DATA_FILTER_SEND_MAIL_STYLE, _refStyle);
		
		sendBroadcast(in);
	}
	
	public void sendBroadcastUpdateFlag(fetchMail _mail,boolean _refreshBody){
		// send the broadcast to notify MailListView and MailOpenActivity to update flag
		//
		Intent in = new Intent(YuchDroidApp.FILTER_MAIL_GROUP_FLAG);
		in.putExtra(DATA_FILTER_MAIL_GROUP_FLAG_GROUP_ID,_mail.getGroupIndex());
		in.putExtra(DATA_FILTER_MAIL_GROUP_FLAG_MAIL_ID,_mail.getDbIndex());
		in.putExtra(DATA_FILTER_MAIL_GROUP_FLAG_GROUP_FLAG,_mail.getGroupFlag());
		in.putExtra(DATA_FILTER_MAIL_GROUP_FLAG_REFRESH_BODY,_refreshBody);
		
		sendBroadcast(in);
	}
	
	public static void copyTextToClipboard(Context _ctx,String _text){
		ClipboardManager clipboard = (ClipboardManager)_ctx.getSystemService(Context.CLIPBOARD_SERVICE);
    	clipboard.setText(_text);
    	
    	Toast.makeText(_ctx, _ctx.getString(R.string.debug_info_menu_copy_ok),Toast.LENGTH_SHORT).show();
	}

	public void TriggerDisconnectNotification(){
		if(m_config.m_connectDisconnectPrompt){
			startConnectNotification(getConnectState(),true);
		}		
	}
	
	public void StopDisconnectNotification(){
		// do nothing
	}
		
	SimpleDateFormat m_errorTimeformat = new SimpleDateFormat("MM-dd HH:mm:ss");
	public void setErrorString(String _error){		
		String t_out = m_errorTimeformat.format(new Date()) + ": " + _error;
		m_errorList.add(t_out);

		Intent t_intent = new Intent(FILTER_DEBUG_INFO);
		sendBroadcast(t_intent);
	}
	
	public void setErrorString(String _error,Exception e){
		ByteArrayOutputStream t_stringBuffer = new ByteArrayOutputStream();
		PrintStream	m_printErrorStack	= new PrintStream(t_stringBuffer);
		e.printStackTrace(m_printErrorStack);
		
		setErrorString(_error + (new String(t_stringBuffer.toByteArray())));
	}
	
	public void clearAllErrorString(){
		m_errorList.clear();
		
		Intent t_intent = new Intent(FILTER_DEBUG_INFO);	
		sendBroadcast(t_intent);
		
		// clear all unhandle excpetion files 
		//
		String t_path = getFilesDir().getAbsolutePath();
		
		String[] t_files = (new File(t_path)).list(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String filename) {
				// TODO Auto-generated method stub
				return filename.endsWith(ExceptionHandler.fsm_stackstraceSuffix);
			}
		});
		
		for(String f:t_files){
			File delFile = new File(t_path,f);
			delFile.delete();
		}
	}
	
	public void copyAllErrorString(Vector<String> _to){
		_to.clear();
		
		synchronized (m_errorList) {
			for(String str:m_errorList){
				_to.insertElementAt(str, 0);
			}
		}
	}
	
	public String getErrorString(){
		
		StringBuffer t_ret = new StringBuffer();
		synchronized (m_errorList) {
			for(String str:m_errorList){
				t_ret.append(str).append("\n");
			}
		}
		
		return t_ret.toString();
	}
	
	// utility function
    //
    public static String GetByteStr(long _byte){
    	if(_byte < 1000){
			return "" + _byte + "B";
		}else if(_byte >= 1000 && _byte < 1000000){
			return "" + (_byte / 1000) + "." + (_byte % 1000 / 100)+ "KB";
		}else{
			return "" + (_byte / (1000000)) + "." + ((_byte / 1000) % 1000 / 100) + "MB";
		}
    }
    
    public static String md5(String _org){
		
		byte[] bytes = null;
		try{
			bytes = _org.getBytes("UTF-8");
		}catch(Exception e){
			bytes = _org.getBytes();
		}
		try{
			MessageDigest digest = MessageDigest.getInstance("MD5");
			digest.reset();
			digest.update(bytes, 0, bytes.length);

			return convertToHex(digest.digest());
			
		}catch(Exception e){
			Log.e(TAG,e.getMessage());
			return "";
		}		
	}
	
	public static String convertToHex(byte[] data) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            int halfbyte = (data[i] >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                if ((0 <= halfbyte) && (halfbyte <= 9))
                    buf.append((char) ('0' + halfbyte));
                else
                    buf.append((char) ('a' + (halfbyte - 10)));
                halfbyte = data[i] & 0x0F;
            } while(two_halfs++ < 1);
        }
        return buf.toString();
    }
	
	public static String getFileMIMEType(File _file){
		return getFileMIMEType(_file.getName());
	}
	
	public static String getFileMIMEType(String _filename){
		MimeTypeMap mime = MimeTypeMap.getSingleton();
        String ext= _filename.substring(_filename.lastIndexOf(".") + 1);
        ext = mime.getMimeTypeFromExtension(ext);
        if(ext.length() == 0){
        	ext = "application/octet-stream";
        }
        return ext;
	}
}
