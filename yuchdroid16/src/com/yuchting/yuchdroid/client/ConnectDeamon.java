package com.yuchting.yuchdroid.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.Socket;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

public class ConnectDeamon extends Service{
	
	
	public static String fsm_clientVersion;
	
	public static int fsm_display_width = 320;
	public static int fsm_display_height = 480;
	
	public static String fsm_PIN	= "";
	public static String fsm_IMEI	= "ad";
	
	public final static String TAG = "ConnectDeamon";
	
	final static int	fsm_clientVer = 14;
	
	private boolean m_sendAuthMsg 		= false;
	private boolean m_destroy				= false;
	
	private boolean m_disconnect 			= true;
	private int	m_ipConnectCounter 		= 0;
	private int	m_connectCounter 		= 0;
	
	private boolean m_recvAboutText		= false;
	private String m_latestVersion			= "";
	
	private String m_host					= null;
	private int m_port						= 0;
	private String m_userPass				= null;
	private String m_passwordKey			= "";
		
	private SendingQueue	m_sendingQueue	= null;
	
	public Socket m_conn 					= null;
	
	public sendReceive	m_connect			= null;
	
	private MailDbAdapter	m_dba			= new MailDbAdapter(this);
	
	private int m_uploadByte 				= 0;
	private int m_downloadByte				= 0;
	
		
	public final static	int				DISCONNECT_STATE = 0;
	public final static	int				CONNECTING_STATE = 1;
	public final static	int				CONNECTED_STATE = 2;
	
	private int m_connectState				= DISCONNECT_STATE;
	
	private boolean m_enableWeiboModule	= false;
	private boolean m_enableIMModule		= false;
	
	// notification system varaibles
	//
	public final static	int				YUCH_NOTIFICATION_MAIL			= 0;
	public final static	int				YUCH_NOTIFICATION_WEIBO			= 1;
	public final static	int				YUCH_NOTIFICATION_WEIBO_HOME	= 2;
	public final static	int				YUCH_NOTIFICATION_DISCONNECT	= 3;	
	
	// mail system variables
	//
	private Vector<Integer>	m_recvMailSimpleHashCodeSet = new Vector<Integer>();
	private Vector<String>		m_sendMailAccountList = new Vector<String>();
	private int				m_defaultSendMailAccountIndex = 0;
	
	// share preference data
	//
	public final static String fsm_shareData_name = "YuchDroid_share_data";
	private SharedPreferences m_shareData = null;
	
	public final static String fsm_shareData_deamon_is_run = "deamon_run";
	
	public void onCreate() {
		Log.d(TAG,"onCreate");
				
		// initialize the sending queue class
		//
		m_sendingQueue = new SendingQueue(this);
	
		// get the PIN string (android id)
		//
		fsm_PIN = android.provider.Settings.System.getString(getContentResolver(), "android_id");
		
		fsm_clientVersion = getVersionName(this,ConnectDeamon.class);
				
		// get the display screen size
		//
		Display display = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		
		fsm_display_width = display.getWidth();
		fsm_display_height = display.getHeight();
		
		m_dba.open();
		
		m_shareData = getSharedPreferences(fsm_shareData_name,MODE_PRIVATE);
				
		// start the connect run thread
		//
		(new Thread(){
			public void run(){
				ConnectDeamon.this.run();
			}
		}).start();
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
	
	public boolean addSendingData(int _msgType ,byte[] _data,boolean _exceptSame)throws Exception{
		return m_sendingQueue.addSendingData(_msgType, _data, _exceptSame);
	}
	
	// This is the old onStart method that will be called on the pre-2.0
	// platform.  On 2.0 or later we override onStartCommand() so this
	// method will not be called.
	public void onStart(Intent intent, int startId) {
		Log.d(TAG,"onStart " + this);
		
		onStart_impl(intent);
	}

	// 2.0 later callback function
	//
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		Log.d(TAG,"onStartCommand");
		
		onStart_impl(intent);
		
	    // We want this service to continue running until it is explicitly
	    // stopped, so return sticky.
	    return 0;
	}
	
	public void SetErrorString(String _error){
		//TODO add debug erro
	}
	
	public void SetErrorString(String _error,Exception e){
		SetErrorString(_error+" m:"+e.getMessage()+" c:"+e.getClass().getName());
	}
	
	private void onStart_impl(Intent _intent){
		//TODO start connect 
		//
		boolean t_disconnect = !m_disconnect;
		m_shareData.edit().putBoolean(fsm_shareData_deamon_is_run,!t_disconnect);
		
		if(t_disconnect){
			
			m_disconnect = t_disconnect;
			
			if(m_conn != null){
				try{
					m_conn.close();
				}catch(Exception e){
					SetErrorString("onStrat_impl",e);
				}
			}
			
		}else{
			
			m_host = _intent.getExtras().get("login_host").toString();
			m_port = (Integer)_intent.getExtras().get("login_port");
			m_userPass = _intent.getExtras().get("login_pass").toString();
			
			m_disconnect = t_disconnect;
		}
		
		
	}

	public IBinder onBind(Intent intent) {
		return null;
	}
		
	public void onDestroy(){
		Log.d(TAG,"onDestory");
		
		synchronized (this) {
			if(m_conn != null){
				m_destroy = true;
				try{
					m_conn.close();
				}catch(Exception e){
					SetErrorString("onDestory",e);
				}
			}
			
			if(m_sendingQueue != null){
				m_sendingQueue.destory();
				m_sendingQueue = null;
			}
		}
		
		m_dba.close();
		m_shareData.edit().putBoolean(fsm_shareData_deamon_is_run, false);
	}
	
	private boolean CanNotConnectSvr(){
		ConnectivityManager t_connect = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		return t_connect.getActiveNetworkInfo().isAvailable();
	}
	
	public static int GetClientLanguage(){
		
		Locale t_locale = Locale.getDefault();
		
		if(t_locale.equals(Locale.SIMPLIFIED_CHINESE)
		|| t_locale.equals(Locale.CHINA)){
			return 0;
		}else if(t_locale.equals(Locale.TRADITIONAL_CHINESE	)){
			return 1;
		}else{
			return 2;
		}
	}
	
	public boolean isDisconnectState(){
		return m_disconnect || m_connect == null || !m_sendAuthMsg;
	}
	
	private boolean IsUseSSL(){
		//TODO read from internal file
		//
		return false;
	}
	
	private int GetPulseIntervalMinutes(){
		//TODO read from the file
		return 5;
	}
	
	private int GetConnectInterval(){
		 
		if(m_connectCounter++ == -1){
			return 0;
		}
		 
		if(m_connectCounter++ > 6){
			m_connectCounter = 0;		 
			return GetPulseIntervalMinutes() * 60 * 1000;
		}
		 
		return 10000;
	}
	
	public synchronized void StoreUpDownloadByte(long _uploadByte,long _downloadByte,boolean _writeIni){
		m_uploadByte += _uploadByte;
		m_downloadByte += _downloadByte;	
				
		if(_writeIni){
			WriteReadIni(false);
		}
	}
	
	public void WriteReadIni(boolean _read){
		//TODO write and read from the internal file 
		//
		
	}
	
	public void SetConnectState(int _state){
		m_connectState = _state;

		//TODO set the state of activity
		//
	}
	
	/**
	 * trigger mail notification to the user
	 * @param _ctx		notification context
	 * @param _mail		notification mail (set the notification's text)
	 */
	public static void TriggerMailNotification(Context _ctx,fetchMail _mail){
		
		NotificationManager t_mgr = (NotificationManager)_ctx.getSystemService(Context.NOTIFICATION_SERVICE);
		
		int icon = R.drawable.ic_notification_mail;        
		
		CharSequence tickerText = _ctx.getString(R.string.mail_notification_ticker);
		
		long when = System.currentTimeMillis();         

		CharSequence contentTitle = _mail.GetSubject(); 
		CharSequence contentText = MailDbAdapter.getDisplayMailBody(_mail);

		Intent notificationIntent = new Intent(_ctx,MailListActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(_ctx, 0, notificationIntent, 0);

		// the next two lines initialize the Notification, using the configurations above
		Notification notification = new Notification(icon, tickerText, when);
		notification.setLatestEventInfo(_ctx, contentTitle, contentText, contentIntent);
		
		//TODO trigger mail received notification
		//
		// sound & vibrate & LED 
		//
		notification.defaults |= Notification.DEFAULT_SOUND;
		
		
		t_mgr.notify(YUCH_NOTIFICATION_MAIL, notification);		
	}
	
	public static void StopMailNotification(Context _ctx){
		NotificationManager t_mgr = (NotificationManager)_ctx.getSystemService(Context.NOTIFICATION_SERVICE);
		t_mgr.cancel(YUCH_NOTIFICATION_MAIL);		
	}

	public void TriggerDisconnectNotification(){
		//TODO trigger disconnect notification if sets
		//
	}
	
	public void StopDisconnectNotification(){
		//TODO stop disconnect notification if sets
		//
	}
	
	public void SetReportLatestVersion(String _latestVersion){
		//TODO popup dialog to prompt 
	}
	
	
	private Socket GetConnection(boolean _ssl)throws Exception{
		 
		final int t_sleep = GetConnectInterval();
		if(t_sleep != 0){
			Thread.sleep(t_sleep);
		}
		 
		if(m_disconnect == true){
			throw new Exception("user closed");
		}
	
		// first use IP address to decrease the DNS message  
		//
		try{
			
			if(_ssl){
				SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();  
	            return (SSLSocket) sslsocketfactory.createSocket(m_host, m_port); 
			}else{
				return new Socket(m_host,m_port);
			}			
			 
		}catch(Exception _e){
	
			String message = _e.getMessage();

			if(message != null){
				if(message.indexOf("Peer") != -1){
					m_connectCounter = 1000;
				}
			}
			 
			throw _e;
		} 
	}
	
	
	private boolean isAppOnForeground(){
		
	    ActivityManager activityManager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
	    List<RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
	    
	    if (appProcesses == null) {
	    	return false;
	    }
	    
	    final String packageName = getPackageName();
	    
	    for(RunningAppProcessInfo appProcess : appProcesses) {
	    	if(appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
	    		return true;
	    	}
	    }
	    return false;
	}
	
	private void SetModuleOnlineState(boolean _online){
		//TODO set the status bar connected state
		//
	}
	
	private int GetRecvMsgMaxLength(){
		//TODO get the receive mail max length
		return 0;
	}
	
	private void DialogAlert(String _text){
		GlobalDialog.showInfo(_text, this);
	}
	
	private void SetAboutInfo(String _aboutInfo){
		//TODO popup the about activity 
	}
	
	public void run(){
		
		while(!m_destroy){
	
			m_sendAuthMsg = false;
						
			while(CanNotConnectSvr() || m_disconnect == true ){
	
				if(m_destroy){
					return ;
				}
				
				try{
					Thread.sleep(15000);
				}catch(Exception _e){}	
			}
			
			try{
				
				synchronized (this) {
					m_ipConnectCounter++;
				}				
				
				m_conn = GetConnection(IsUseSSL());
				
				// TCP connect flowing bytes statistics 
				//
				StoreUpDownloadByte(72,40,false);
								
				m_connect = new sendReceive(m_conn.getOutputStream(),m_conn.getInputStream());
				m_connect.SetKeepliveInterval(GetPulseIntervalMinutes());
				
				m_connect.RegisterStoreUpDownloadByte(new sendReceive.IStoreUpDownloadByte() {
					public void Store(long uploadByte, long downloadByte) {
						StoreUpDownloadByte(uploadByte,downloadByte,true);
					}
				});
							
				// send the Auth info
				//
				ByteArrayOutputStream t_os = new ByteArrayOutputStream();
				t_os.write(msg_head.msgConfirm);
				sendReceive.WriteString(t_os, m_userPass);
				sendReceive.WriteInt(t_os,fsm_clientVer);
				t_os.write(GetClientLanguage());
				sendReceive.WriteString(t_os,fsm_clientVersion);
				sendReceive.WriteString(t_os,m_passwordKey);
				sendReceive.WriteBoolean(t_os,m_enableWeiboModule);
				sendReceive.WriteString(t_os,"6.0"); // adapt blackberry
				int t_size = (fsm_display_width << 16) | fsm_display_height;
				sendReceive.WriteInt(t_os,t_size);
				sendReceive.WriteBoolean(t_os,m_enableIMModule);
				
				m_connect.SendBufferToSvr(t_os.toByteArray(), true,false);
				
				m_sendAuthMsg = true;
				
				synchronized (this) {
					m_ipConnectCounter = 0;
				}
								
				// set the text connect
				//
				//m_mainApp.SetConnectState(recvMain.CONNECTED_STATE);
				//m_mainApp.StopDisconnectNotification();
				
				SetModuleOnlineState(true);
								
				while(true){
					ProcessMsg(m_connect.RecvBufferFromSvr());
				}
				
			}catch(Exception _e){
				
				if(m_disconnect != true){
					try{
						SetConnectState(CONNECTING_STATE);
						SetErrorString("M ",_e);
					}catch(Exception e){}	
				}							
			}		
					
			if(!isAppOnForeground() && m_ipConnectCounter >= 5){
				m_ipConnectCounter = 0;
				TriggerDisconnectNotification();
			}			
			
			synchronized (this) {
				try{
					if(m_connect != null){
						m_connect.CloseSendReceive();
					}	
					
					if(m_conn != null){
						m_conn.close();
					}					
					
				}catch(Exception _e){
				}finally{
					m_connect = null;
					m_conn = null;
				}
			}
			
			SetModuleOnlineState(false);									
		}
	}
	
	private synchronized void ProcessMsg(byte[] _package)throws Exception{
		 ByteArrayInputStream in  = new ByteArrayInputStream(_package);
		 
		 final int t_msg_head = in.read();
		 
		 switch(t_msg_head){
		 	case msg_head.msgMail:
				ProcessRecvMail(in);	 		
		 		break;
		 	case msg_head.msgSendMail:
		 		ProcessSentMail(in);
		 		break;
		 	case msg_head.msgNote:
		 		String t_string = sendReceive.ReadString(in);
		 		DialogAlert("YuchBerry svr: " + t_string);
		 		SetErrorString(t_string);
		 		break;
		 	
		 	case msg_head.msgFileAttach:
		 		ProcessFileAttach(in);
		 		break;
		 	case msg_head.msgSponsorList:
		 		SetAboutInfo(sendReceive.ReadString(in));
		 		m_recvAboutText = true;
		 		break;
		 		
		 	case msg_head.msgLatestVersion:
		 		String t_latestVersion = sendReceive.ReadString(in);
		 		if(!fsm_clientVersion.equals(t_latestVersion)){
		 			fsm_clientVersion = t_latestVersion;
		 			SetReportLatestVersion(t_latestVersion);
		 		}
		 		break;
//		 	case msg_head.msgWeibo:
//		 		ProcessWeibo(in);
//		 		break;
//		 	case msg_head.msgWeiboHeadImage:
//		 		ProcessWeiboHeadImage(in);
//		 		break;
//		 		
//		 	case msg_head.msgWeiboPrompt:
//		 		if(m_mainApp.m_weiboTimeLineScreen != null){
//		 			m_mainApp.m_weiboTimeLineScreen.popupPromptText(sendReceive.ReadString(in));
//		 		}
//		 		break;
//		 	case msg_head.msgWeiboUser:
//		 		if(m_mainApp.m_weiboTimeLineScreen != null){
//		 			fetchWeiboUser t_user = new fetchWeiboUser();
//		 			t_user.InputData(in);
//		 			m_mainApp.m_weiboTimeLineScreen.displayWeiboUser(t_user);
//		 		}
//		 		break;
//		 	case msg_head.msgWeiboConfirm:
//				if(m_mainApp.m_weiboTimeLineScreen != null){
//					m_mainApp.m_weiboTimeLineScreen.weiboSendFileConfirm(sendReceive.ReadInt(in),0);
//				}
//		 		break;
//		 	case msg_head.msgDeviceInfo:
//		 		ByteArrayOutputStream os = new ByteArrayOutputStream();
//		 		os.write(msg_head.msgDeviceInfo);
//		 		sendReceive.WriteLong(os, recvMain.fsm_PIN);
//		 		sendReceive.WriteString(os,recvMain.fsm_IMEI);
//		 		addSendingData(msg_head.msgDeviceInfo, os.toByteArray(), true);
//		 		break;
//		 	case msg_head.msgMailAccountList:
//		 		sendReceive.ReadStringVector(in, m_mainApp.m_sendMailAccountList);
//		 		if(m_mainApp.m_settingScreen != null){
//		 			m_mainApp.m_settingScreen.refreshMailAccountList();
//		 		}
//		 		break;
//		 	case msg_head.msgChat:
//		 		if(m_mainApp.m_mainIMScreen != null){
//		 			m_mainApp.m_mainIMScreen.processChatMsg(in);
//		 		}
//		 		break;
//		 	case msg_head.msgChatRosterList:
//		 		if(m_mainApp.m_mainIMScreen != null){
//		 			m_mainApp.m_mainIMScreen.processChatRosterList(in);
//		 		}
//		 		break;
//		 	case msg_head.msgChatConfirm:
//		 		if(m_mainApp.m_mainIMScreen != null){
//		 			m_mainApp.m_mainIMScreen.processChatConfirm(in);
//		 		}
//		 		break;
//		 	case msg_head.msgChatState:
//		 		if(m_mainApp.m_mainIMScreen != null){
//		 			m_mainApp.m_mainIMScreen.processChatState(in);
//		 		}
//		 		break;
//		 	case msg_head.msgChatHeadImage:
//		 		ProcessChatHeadImage(in);
//		 		break;
//		 	case msg_head.msgChatPresence:
//		 		if(m_mainApp.m_mainIMScreen != null){
//		 			m_mainApp.m_mainIMScreen.processChatPresence(in);
//		 		}
//		 		break;
//		 	case msg_head.msgChatRead:
//		 		if(m_mainApp.m_mainIMScreen != null){
//		 			m_mainApp.m_mainIMScreen.processChatRead(in);
//		 		}
//		 		break;
		}
	}
	
	private void SendMailConfirmMsg(int _hashCode)throws Exception{
		
		// send the msgMailConfirm to server to confirm receive this mail
		//
		ByteArrayOutputStream t_os = new ByteArrayOutputStream();
		t_os.write(msg_head.msgMailConfirm);
		sendReceive.WriteInt(t_os,_hashCode);
				
		m_sendingQueue.addSendingData(msg_head.msgMailConfirm, t_os.toByteArray(),true);
	}
	
	private void sendRequestMailAccountMsg(){
		try{
			addSendingData(msg_head.msgMailAccountList, new byte[]{msg_head.msgMailAccountList}, true);
		}catch(Exception e){
			SetErrorString("SRMAM ",e);
		}
	}
	
	private int GetRecvMailNum(){
		//TODO get the received mail
		return 0;
	}
	private void SetRecvMailNum(int _num){
		//TODO set the received mail
	}
	
	private void ProcessRecvMail(InputStream in)throws Exception{
		
		fetchMail t_mail = new fetchMail();
		t_mail.InputMail(in); 		
	
		int t_hashcode = t_mail.GetSimpleHashCode();
		
		for(int i = 0;i < m_recvMailSimpleHashCodeSet.size();i++){
			Integer t_simpleHash = (Integer)m_recvMailSimpleHashCodeSet.elementAt(i);
			if(t_simpleHash.intValue() == t_hashcode ){
				
				SendMailConfirmMsg(t_hashcode);
				
				SetErrorString("" + t_hashcode + " Mail has been added! ");
				
				return;
			}
		}
				
		if(m_recvMailSimpleHashCodeSet.size() > 256){
			m_recvMailSimpleHashCodeSet.removeElementAt(0);
		}
		
		m_recvMailSimpleHashCodeSet.addElement(new Integer(t_mail.GetSimpleHashCode()));
		
		if(GetRecvMsgMaxLength() != 0){
			if(t_mail.GetContain().length() > GetRecvMsgMaxLength()){
				t_mail.SetContain(t_mail.GetContain().substring(0,GetRecvMsgMaxLength() - 1) + 
									"\n.....\n\n" + getString(R.string.mail_over_max_length_prompt));
			}
		}
		
		if(m_sendMailAccountList.isEmpty()){
			sendRequestMailAccountMsg();
		}

		try{
						
			m_dba.createMail(t_mail, null);
			
			// increase the receive mail quantity
			//
			SetRecvMailNum(GetRecvMailNum() + 1);			
			
			SendMailConfirmMsg(t_hashcode);
			
			SetErrorString("" + t_hashcode + ":" + t_mail.GetSubject() + "+" + t_mail.GetSendDate().getTime());
									
			TriggerMailNotification(this,t_mail);
							
		}catch(Exception _e){
			SetErrorString("C ",e);
		}
	}

	
	private void ProcessSentMail(InputStream in)throws Exception{
		//TODO sent mail confirm
	}
	
	private void ProcessFileAttach(InputStream in)throws Exception{
		//TODO recevice file attach
	}
	
}
