package com.yuchting.yuchdroid.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.Socket;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Service;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.Intent;
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
	
	private boolean m_disconnect 			= true;
	private int	m_ipConnectCounter 		= 0;
	private int	m_connectCounter 		= 0;
	
	private boolean m_recvAboutText		= false;
	private String m_latestVersion			= "";
	
	private String m_host					= null;
	private int m_port						= 0;
	private String m_userPass				= null;
	private String m_passwordKey			= "";
	
	public Socket m_conn 					= null;
	
	public sendReceive	m_connect			= null;
	
	private int m_uploadByte 				= 0;
	private int m_downloadByte				= 0;
	
	public final static	int				DISCONNECT_STATE = 0;
	public final static	int				CONNECTING_STATE = 1;
	public final static	int				CONNECTED_STATE = 2;
	
	private int m_connectState				= DISCONNECT_STATE; 
	
	private boolean m_enableWeiboModule	= false;
	private boolean m_enableIMModule		= false;
	
	public void onCreate() {
		Log.d(TAG,"onCreate");
		
		Thread t_thread = new Thread(){
			public void run(){
				ConnectDeamon.this.run();
			}
		};
		
		t_thread.start();
	
		fsm_PIN = android.provider.Settings.System.getString(getContentResolver(), "android_id");
		
		// get the display screen size
		//
		Display display = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		
		fsm_display_width = display.getWidth();
		fsm_display_height = display.getHeight();
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
	
	private void onStart_impl(Intent _intent){
		//TODO start connect 
		//
	}

	public IBinder onBind(Intent intent) {
		return null;
	}
		
	public void onDestroy(){
		Log.d(TAG,"onDestory");
		// TODO end connect
		//
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
	
	public void TriggerDisconnectNotification(){
		//TODO trigger disconnect notification if sets
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
	
	private void DialogAlert(String _text){
		//TODO popup a dialog to prompt
	}
	
	private void SetAboutInfo(String _aboutInfo){
		//TODO popup the about activity 
	}
	
	public void run(){
		
		while(true){
	
			m_sendAuthMsg = false;
			
			while(CanNotConnectSvr() || m_disconnect == true ){
	
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
						SetErrorString("M: " + _e.getMessage() + " "+ _e.getClass().getName());
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
	
	private void ProcessRecvMail(InputStream in)throws Exception{
		//TODO receive a mail
	}
	
	private void ProcessSentMail(InputStream in)throws Exception{
		//TODO sent mail confirm
	}
	
	private void ProcessFileAttach(InputStream in)throws Exception{
		//TODO recevice file attach
	}
	
}
