package com.yuchting.yuchdroid.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import android.app.ActivityManager;
import android.app.Service;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.util.Log;

import com.yuchting.yuchdroid.client.mail.SendMailDeamon;
import com.yuchting.yuchdroid.client.mail.fetchMail;

public class ConnectDeamon extends Service implements Runnable{
	
	public final static String TAG = "ConnectDeamon";
	
	final static int	fsm_clientVer = 15;
	
	public boolean m_sendAuthMsg 			= false;
	public boolean m_destroy				= false;
	
	private int	m_ipConnectCounter 		= 0;
	private int	m_connectCounter 		= -1;
	
	private String m_latestVersion			= "";
				
	private SendingQueue	m_sendingQueue	= null;
		
	public sendReceive	m_connect			= null;
	
	public YuchDroidApp m_mainApp; 
		
	// mail system variables
	//
	private Vector<Integer>			m_recvMailSimpleHashCodeSet = new Vector<Integer>();		
	private Vector<SendMailDeamon>		m_sendingMailAttachment = new Vector<SendMailDeamon>();
		
	// the Thread.sleep will not be risen by system when android's state is depth-sleep-state
	// please check the follow URL for detail:
	// http://stackoverflow.com/questions/5546926/how-does-the-android-system-behave-with-threads-that-sleep-for-too-long
	//
	// so we choose the selector for the timer  
	//
	private Selector			m_sleepSelector = null;
	
	BroadcastReceiver m_mailMarkReadRecv = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent){
			String t_mailHashcode = intent.getExtras().getString(YuchDroidApp.DATA_FILTER_MARK_MAIL_READ_MAILID);
			String[] t_mailHashList = t_mailHashcode.split(fetchMail.fsm_vectStringSpliter);
						
			for(String id : t_mailHashList){
				
				int t_hash = Integer.valueOf(id);
				
				try{
					ByteArrayOutputStream os  = new ByteArrayOutputStream();			
					os.write(msg_head.msgBeenRead);
					sendReceive.WriteInt(os, t_hash);
					
					m_sendingQueue.addSendingData(msg_head.msgBeenRead, os.toByteArray(), true);
				}catch(Exception e){
					m_mainApp.setErrorString("ConnectDeamon MailMarkReadRecv", e);
				}
				
			}
		}
	};
	
	BroadcastReceiver m_mailSendRecv = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if(m_mainApp.m_composeRefMail != null){
				
				// please check MailComposeActivity.send() for detail
				//				
				fetchMail t_sendMail 		= m_mainApp.m_composeRefMail;
				fetchMail t_referenceMail	= m_mainApp.m_composeStyleRefMail;
				
				m_mainApp.m_composeRefMail = null;
								
				int t_style 	= intent.getExtras().getInt(YuchDroidApp.DATA_FILTER_SEND_MAIL_STYLE);
				
				synchronized(m_sendingMailAttachment) {
					for(SendMailDeamon send:m_sendingMailAttachment){
						if(send.m_sendMail.GetSimpleHashCode() == t_sendMail.GetSimpleHashCode()){
							
							if(send.isAlive()){
								// has been added
								//
								return;
							}else{
								m_sendingMailAttachment.remove(send);
								
								break;
							}
						}
					}
				}
				
				try{
					m_sendingMailAttachment.add(new SendMailDeamon(ConnectDeamon.this, 
													t_sendMail, null, t_referenceMail,t_style));
				}catch(Exception e){
					m_mainApp.setErrorString(TAG+" MailSendRecv", e);
				}
				
			}			
		}
	};
	
	public void onCreate() {
		m_mainApp = (YuchDroidApp)getApplicationContext();
						
		// initialize the sending queue class
		//
		m_sendingQueue = new SendingQueue(this);
		
		registerReceiver(m_mailMarkReadRecv, new IntentFilter(YuchDroidApp.FILTER_MARK_MAIL_READ));
		registerReceiver(m_mailSendRecv, new IntentFilter(YuchDroidApp.FILTER_SEND_MAIL));
		
		m_mainApp.m_connectDeamonRun = true;
		m_mainApp.setErrorString("ConnectDeamon onCreate");
		
		try{
			m_sleepSelector = SelectorProvider.provider().openSelector();
		}catch(Exception e){
			m_mainApp.setErrorString(TAG, e);
			
			GlobalDialog.showInfo("can't open the selector for sleep please check debug info", this);
			stopSelf();
			
			return;
		}
		
		// start the connect run thread
		//
		(new Thread(this)).start();
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
		
	private void onStart_impl(Intent _intent){
				
		try{
			Connect();			
		}catch(Exception e){
			m_mainApp.setErrorString("onStart_impl", e);
		}
	}

	public IBinder onBind(Intent intent) {
		return null;
	}
		
	public void onDestroy(){
		m_mainApp.setErrorString("ConnectDeamon onDestroy");
		
		m_destroy = true;
		
		closeConnect();
		try{
			m_sleepSelector.close();
		}catch(Exception e){}
		
	
		if(m_sendingQueue != null){
			m_sendingQueue.destory();
			m_sendingQueue = null;
		}
				
		m_mainApp.m_connectDeamonRun = false;
		m_mainApp.m_connectState = YuchDroidApp.STATE_DISCONNECT;
		
		// destroy the all sendingMailDeamon
		//
		for(SendMailDeamon de:m_sendingMailAttachment){
			de.m_closeState = true;
			de.inter();
			de.sendError();
		}
		m_sendingMailAttachment.clear();
		
		unregisterReceiver(m_mailMarkReadRecv);
		unregisterReceiver(m_mailSendRecv);
	}
	
	private boolean CanNotConnectSvr(){
		ConnectivityManager t_connect = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		
		if(t_connect == null){
			return true;
		}
		
		NetworkInfo info = t_connect.getActiveNetworkInfo();
		if(info == null){
			return true;
		}
		
		return !info.isConnected();
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
		return m_connect == null || !m_sendAuthMsg;
	}
	
	private boolean IsUseSSL(){
		return m_mainApp.m_config.m_useSSL;
	}
	
	private int GetPulseIntervalMinutes(){
		return m_mainApp.m_config.getPulseInterval();
	}
	
	private synchronized int GetConnectInterval(){
		 
		if(m_connectCounter++ == -1){
			return 0;
		}
		
		if(m_connectCounter >= 4){
			m_connectCounter = 0;		 
			return GetPulseIntervalMinutes() * 60 * 1000;
		}
		 
		return 10000;
	}
	
	public synchronized void StoreUpDownloadByte(long _uploadByte,long _downloadByte,boolean _writeIni){
		m_mainApp.m_config.m_uploadByte += _uploadByte;
		m_mainApp.m_config.m_downloadByte += _downloadByte;	
				
		if(_writeIni){
			m_mainApp.m_config.WriteReadIni(false);
		}
	}
		
	
	
	public void SetReportLatestVersion(String _latestVersion){
		//TODO popup dialog to prompt 
	}
	
	
	sendReceive.IStoreUpDownloadByte m_upDownloadByteInterface = new sendReceive.IStoreUpDownloadByte(){
		public void store(long uploadByte, long downloadByte) {
			StoreUpDownloadByte(uploadByte,downloadByte,true);
		}
		
		public int getPushInterval(){
			return GetPulseIntervalMinutes();
		}
	};
	
	private sendReceive GetConnection(boolean _ssl)throws Exception{
		 
		final int t_sleep = GetConnectInterval();
		if(t_sleep != 0){
			m_sleepSelector.select(t_sleep);
		}
		 
		if(m_destroy){
			throw new Exception("user closed");
		}

		try{
			
			return new sendReceive(m_mainApp.m_config.m_host,m_mainApp.m_config.m_port,_ssl,
									m_upDownloadByteInterface);
			
		}catch(java.net.ConnectException e){
			// connection time out exception
			//
			m_connectCounter = 100;		
			throw e;
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
		GlobalDialog.showInfo(_text, this);
	}
	
	private void SetAboutInfo(String _aboutInfo){
		//TODO popup the about activity 
	}
		
	public synchronized void Connect()throws Exception{
		 
		Disconnect();
		m_mainApp.setConnectState(YuchDroidApp.STATE_CONNECTING);
		
		m_sleepSelector.wakeup();
	}
	
	public void Disconnect()throws Exception{
		
		m_mainApp.StopDisconnectNotification();
		
		m_sleepSelector.wakeup();
						 	
		synchronized (this) {
			
			m_connectCounter = -1;
			 
			m_ipConnectCounter = 0;
			
			closeConnect();		
			 
			for(int i = 0 ;i < m_sendingMailAttachment.size();i++){
				SendMailDeamon send = (SendMailDeamon)m_sendingMailAttachment.elementAt(i);
				 	
				send.m_closeState = true;
				send.inter(); 
				send.sendError();
			}
			 
			m_sendingMailAttachment.removeAllElements();
		}
		
		m_mainApp.setConnectState(YuchDroidApp.STATE_DISCONNECT);
	}
	
	private synchronized void closeConnect(){
		
		try{
			
			if(m_connect != null){
				m_connect.CloseSendReceive();
			}	
		}catch(Exception _e){
			m_mainApp.setErrorString("closeConnect", _e);
		}	
				
		m_connect = null;
		
	}
	
	public void run(){
		
		while(!m_destroy){
	
			m_sendAuthMsg = false;
						
			while(CanNotConnectSvr()){
				
				try{
					m_sleepSelector.select(15000);
				}catch(Exception _e){}
				

				if(m_destroy){
					return ;
				}
			}
			
			try{
				
				synchronized (this) {
					m_ipConnectCounter++;
				}				
				
				m_connect = GetConnection(IsUseSSL());
								
				// TCP connect flowing bytes statistics 
				//
				StoreUpDownloadByte(72,40,false);								
											
				// send the Auth info
				//
				ByteArrayOutputStream t_os = new ByteArrayOutputStream();
				t_os.write(msg_head.msgConfirm);
				sendReceive.WriteString(t_os, m_mainApp.m_config.m_userPass);
				sendReceive.WriteInt(t_os,fsm_clientVer);
				t_os.write(GetClientLanguage());
				sendReceive.WriteString(t_os,YuchDroidApp.fsm_appVersion);
				sendReceive.WriteString(t_os,m_mainApp.m_config.m_passwordKey);
				sendReceive.WriteBoolean(t_os,m_mainApp.m_config.m_enableWeiboModule);
				sendReceive.WriteString(t_os,"6.0"); // adapt blackberry
				int t_size = (YuchDroidApp.sm_displyWidth << 16) | YuchDroidApp.sm_displyHeight;
				sendReceive.WriteInt(t_os,t_size);
				sendReceive.WriteBoolean(t_os,m_mainApp.m_config.m_enableIMModule);
				
				m_connect.SendBufferToSvr(t_os.toByteArray(), true);
				
				m_sendAuthMsg = true;
				
				synchronized (this) {
					m_ipConnectCounter = 0;
				}
								
				// set the text connect
				//
				m_mainApp.setConnectState(YuchDroidApp.STATE_CONNECTED);
				m_mainApp.StopDisconnectNotification();
				m_sendingQueue.connectNotify();
				
				SetModuleOnlineState(true);
								
				while(true){
					ProcessMsg(m_connect.RecvBufferFromSvr());
				}
				
			}catch(Exception _e){
				
				if(!m_destroy){
					m_mainApp.setConnectState(YuchDroidApp.STATE_CONNECTING);		
				}
				
				m_mainApp.setErrorString("M ",_e);
			}
					
			if(!isAppOnForeground() && m_ipConnectCounter >= 5){
				m_ipConnectCounter = 0;
				m_mainApp.TriggerDisconnectNotification();
			}			
			
			closeConnect();
			
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
		 		m_mainApp.setErrorString(t_string);
		 		break;
		 	
		 	case msg_head.msgFileAttach:
		 		ProcessFileAttach(in);
		 		break;	 		
		 	case msg_head.msgLatestVersion:
		 		String t_latestVersion = sendReceive.ReadString(in);
		 		if(!YuchDroidApp.fsm_appVersion.equals(t_latestVersion)){
		 			YuchDroidApp.fsm_appVersion = t_latestVersion;
		 			SetReportLatestVersion(t_latestVersion);
		 		}
		 		break;
		 	case msg_head.msgDeviceInfo:
		 		ByteArrayOutputStream os = new ByteArrayOutputStream();
		 		os.write(msg_head.msgDeviceInfo);
		 		sendReceive.WriteString(os, YuchDroidApp.fsm_PIN);
		 		sendReceive.WriteString(os,YuchDroidApp.fsm_IMEI);
		 		addSendingData(msg_head.msgDeviceInfo, os.toByteArray(), true);
		 		break;
		 	case msg_head.msgMailAccountList:
		 		sendReceive.ReadStringVector(in, m_mainApp.m_config.m_sendMailAccountList);
		 		m_mainApp.m_config.WriteReadIni(false);
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
			m_mainApp.setErrorString("SRMAM ",e);
		}
	}
	
	private void ProcessRecvMail(InputStream in)throws Exception{
		
		fetchMail t_mail = new fetchMail();
		t_mail.InputMail(in); 		
	
		int t_hashcode = t_mail.GetSimpleHashCode();
		
		for(int i = 0;i < m_recvMailSimpleHashCodeSet.size();i++){
			Integer t_simpleHash = (Integer)m_recvMailSimpleHashCodeSet.elementAt(i);
			if(t_simpleHash.intValue() == t_hashcode ){
				
				SendMailConfirmMsg(t_hashcode);
				
				m_mainApp.setErrorString("" + t_hashcode + " Mail has been added! ");
				
				return;
			}
		}
				
		if(m_recvMailSimpleHashCodeSet.size() > 256){
			m_recvMailSimpleHashCodeSet.removeElementAt(0);
		}
		
		m_recvMailSimpleHashCodeSet.addElement(new Integer(t_mail.GetSimpleHashCode()));
				
		if(m_mainApp.m_config.m_sendMailAccountList.isEmpty()){
			sendRequestMailAccountMsg();
		}

		try{
						
			m_mainApp.m_dba.createMail(t_mail,-1,false);
			
			// increase the receive mail quantity
			//
			m_mainApp.m_config.m_recvMailNum++;			
			
			SendMailConfirmMsg(t_hashcode);
			
			m_mainApp.setErrorString("" + t_hashcode + ":" + t_mail.GetSubject() + "+" + t_mail.GetSendDate().getTime());
									
			m_mainApp.TriggerMailNotification(t_mail);
							
		}catch(Exception _e){
			m_mainApp.setErrorString("C ",_e);
		}
	}

	
	private void ProcessSentMail(InputStream in)throws Exception{
		
		boolean t_succ = sendReceive.ReadBoolean(in);
		final long t_time = sendReceive.ReadLong(in);
		
		// delete the fetchMail send deamon thread
		//
		for(int i = 0;i < m_sendingMailAttachment.size();i++){
			SendMailDeamon t_deamon = (SendMailDeamon)m_sendingMailAttachment.elementAt(i);
			
			if(t_deamon.m_sendMail.GetSendDate().getTime() == t_time){		
				if(t_succ){
					t_deamon.sendSucc();
					
					// increase the send mail quantity
					//
					m_mainApp.m_config.m_sendMailNum++;
					
				}else{
					t_deamon.sendError();
				}			
				
				t_deamon.m_closeState = true;
				t_deamon.inter();							
				
				m_sendingMailAttachment.removeElement(t_deamon);
				
				// TODO delete the uploading desc string of main application
				//
//				for(int j = 0 ;j < m_mainApp.m_uploadingDesc.size();j++){
//					recvMain.UploadingDesc t_desc = (recvMain.UploadingDesc)m_mainApp.m_uploadingDesc.elementAt(j);
//					if(t_desc.m_mail == t_deamon.m_sendMail){
//					
//						m_mainApp.m_uploadingDesc.removeElementAt(j);
//						break;
//					}
//				}
								
				break;
			}
		}
	}
	
	private void ProcessFileAttach(InputStream in)throws Exception{
		//TODO recevice file attach
	}
	
}
