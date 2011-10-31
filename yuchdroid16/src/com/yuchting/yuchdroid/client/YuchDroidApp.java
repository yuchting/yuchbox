package com.yuchting.yuchdroid.client;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.text.ClipboardManager;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import com.yuchting.yuchdroid.client.mail.HomeActivity;
import com.yuchting.yuchdroid.client.mail.MailDbAdapter;
import com.yuchting.yuchdroid.client.mail.fetchMail;

public class YuchDroidApp extends Application {
	
	public final static String	TAG = "YuchDroidApp";
	// send broadcast intent filter
	//
	public final static	String	FILTER_CONNECT_STATE 	= TAG + "_FILTER_CS";
	public final static	String	FILTER_DEBUG_INFO 		= TAG + "_FILTER_DI";
	public final static	String	FILTER_MARK_MAIL_READ	= TAG + "_FILTER_MMR";
	public final static	String	FILTER_SEND_MAIL		= TAG + "_FILTER_SM";
	public final static	String	FILTER_MAIL_GROUP_FLAG	= TAG + "_FILTER_MGF";
	public final static	String	FILTER_RECV_MAIL		= TAG + "_FILTER_RM";
	
	
	// FILTER_MARK_MAIL_READ broadcast parameters data
	//
	public final static	String	DATA_FILTER_MARK_MAIL_READ_GROUPID	= "groupId";
	public final static	String	DATA_FILTER_MARK_MAIL_READ_MAILID	= "mailId";
	
	// FILTER_SEND_MAIL broadcast parameters data
	//
	public final static	String	DATA_FILTER_SEND_MAIL_STYLE			= "style";
	
	// FILTER_MAIL_GROUP_FLAG broadcast parameters data
	//
	public final static	String	DATA_FILTER_MAIL_GROUP_FLAG_GROUP_ID	= "groupId";
	public final static	String	DATA_FILTER_MAIL_GROUP_FLAG_MAIL_ID		= "mailId";
	public final static	String	DATA_FILTER_MAIL_GROUP_FLAG_GROUP_FLAG	= "groupFlag";
	public final static	String	DATA_FILTER_MAIL_GROUP_FLAG_REFRESH_BODY= "refreshBody";
	
	
	// connect state
	//
	public final static	int				STATE_DISCONNECT	= 0;
	public final static	int				STATE_CONNECTING	= 1;
	public final static	int				STATE_CONNECTED		= 2;
				
	// notification system varaibles
	//
	public final static	int				YUCH_NOTIFICATION_MAIL			= 0;
	public final static	int				YUCH_NOTIFICATION_WEIBO			= 1;
	public final static	int				YUCH_NOTIFICATION_WEIBO_HOME	= 2;
	public final static	int				YUCH_NOTIFICATION_DISCONNECT	= 3;
	
	// notifcation intent status
	//
	public final static String			YUCH_NOTIFICATION_STATUS		= "status";
	
	private int m_mailNotificationNum		= 0;
	
	public static int sm_displyWidth		= 0;
	public static int sm_displyHeight		= 0;
	
	
	public Vector<String>	m_errorList = new Vector<String>();
	
	public MailDbAdapter	m_dba		= new MailDbAdapter(this);
	public ConfigInit		m_config 	= new ConfigInit(this);
	
	public boolean			m_connectDeamonRun = false;
	public int				m_connectState	= STATE_DISCONNECT;
	
	
	// reference fetch Mail
	//
	public fetchMail		m_composeRefMail;
	public fetchMail		m_composeStyleRefMail;
	
	
	@Override
	public void onCreate (){
		super.onCreate();
		
		m_dba.open();
		m_config.WriteReadIni(true);
		
		Display display = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		sm_displyWidth = display.getWidth();
		sm_displyHeight = display.getHeight();
	}
	
	@Override
	public void onTerminate(){
		super.onTerminate();
		
		m_dba.close();
	}
	
	public void setConnectState(int _state){
		m_connectState = _state;
		
		Intent t_intent = new Intent(FILTER_CONNECT_STATE);	
		sendBroadcast(t_intent);
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
		notificationIntent.putExtra(YUCH_NOTIFICATION_STATUS, 0);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
				PendingIntent.FLAG_UPDATE_CURRENT | Notification.FLAG_ONLY_ALERT_ONCE | Notification.FLAG_AUTO_CANCEL);

		// the next two lines initialize the Notification, using the configurations above
		Notification notification = new Notification(icon, tickerText, System.currentTimeMillis());
		notification.setLatestEventInfo(this, contentTitle, contentText, contentIntent);
		
		notification.number = ++m_mailNotificationNum;
		
		//TODO trigger mail received notification
		//
		// sound & vibrate & LED 
		//
		notification.defaults |= Notification.DEFAULT_SOUND;
				
		t_mgr.notify(YuchDroidApp.YUCH_NOTIFICATION_MAIL, notification);
		
		// send broadcast
		//
		Intent intent = new Intent(YuchDroidApp.FILTER_RECV_MAIL);
		sendBroadcast(intent);
	}
	
	public void StopMailNotification(){
		NotificationManager t_mgr = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		t_mgr.cancel(YUCH_NOTIFICATION_MAIL);
		
		m_mailNotificationNum = 0;
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
		//TODO trigger disconnect notification if sets
		//
	}
	
	public void StopDisconnectNotification(){
		//TODO stop disconnect notification if sets
		//
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
				t_ret.append(str);
			}
		}
		
		return t_ret.toString();
	}
	
	 // utility function
    //
    public static String GetByteStr(int _byte){
    	if(_byte < 1000){
			return "" + _byte + "B";
		}else if(_byte >= 1000 && _byte < 1000000){
			return "" + (_byte / 1000) + "." + (_byte % 1000 / 100)+ "KB";
		}else{
			return "" + (_byte / (1000000)) + "." + ((_byte / 1000) % 1000 / 100) + "MB";
		}
    }
}
