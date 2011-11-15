package com.yuchting.yuchdroid.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.yuchting.yuchdroid.client.mail.SendMailDeamon;
import com.yuchting.yuchdroid.client.mail.fetchMail;

public class ConnectDeamon extends Service implements Runnable{
	
	public final static String TAG = ConnectDeamon.class.getName();
	
	private final static String FILTER_RECONNECT = TAG+"_FILTER_RECONNECT";
	private final static String FILTER_DESTROY = TAG+"_FILTER_DESTROY";
	
	public static	ConnectDeamon sm_connectDeamon = null;
		
	final static int	fsm_clientVer = 15;
	
	public boolean m_sendAuthMsg 			= false;
	public boolean m_connectState			= true;
	public boolean m_destroy				= false;
	
	private int	m_connectFailedCounter 	= 0;	
	private String m_latestVersion			= "";
				
	private SendingQueue	m_sendingQueue	= null;
		
	public sendReceive	m_connect			= null;
	public Selector		m_tmpConnectSelector = null;
	public YuchDroidApp m_mainApp;
	
	
		
	// mail system variables
	//
	private Vector<Integer>			m_recvMailSimpleHashCodeSet = new Vector<Integer>();		
	private Vector<SendMailDeamon>		m_sendingMailAttachment = new Vector<SendMailDeamon>();
		
	private Thread m_agentThread		= new Thread(this);	
	private PowerManager.WakeLock m_powerWakeLock	= null;
	
	BroadcastReceiver m_mailMarkReadRecv = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent){
			markDelMail(intent,true);
		}
	};
	
	BroadcastReceiver m_mailSendRecv = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if(m_mainApp.m_composeRefMail != null){
				
				// please check MailComposeActivity.send() for detail
				//				
				fetchMail t_sendMail 		= m_mainApp.m_composeRefMail;
				fetchMail t_referenceMail	= m_mainApp.m_config.m_discardOrgText?null:m_mainApp.m_composeStyleRefMail;
				
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
		
	BroadcastReceiver m_delMailRecv = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			markDelMail(intent,false);
		}
	};
				
	public void onCreate() {
		m_mainApp = (YuchDroidApp)getApplicationContext();
						
		// initialize the sending queue class
		//
		m_sendingQueue = new SendingQueue(this);
		
		registerReceiver(m_mailMarkReadRecv, new IntentFilter(YuchDroidApp.FILTER_MARK_MAIL_READ));
		registerReceiver(m_mailSendRecv, new IntentFilter(YuchDroidApp.FILTER_SEND_MAIL));
		registerReceiver(m_delMailRecv, new IntentFilter(YuchDroidApp.FILTER_DELETE_MAIL));
		registerReceiver(m_reconnectRecv, new IntentFilter(FILTER_RECONNECT));
		registerReceiver(m_destoryRecv, new IntentFilter(FILTER_DESTROY));
		
		m_mainApp.m_connectDeamonRun = true;
		m_mainApp.setErrorString("ConnectDeamon onCreate");
		
		m_powerWakeLock = ((PowerManager)getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
		
		sm_connectDeamon = this;
		
		// start the connect run thread
		//
		m_agentThread.start();
	}
	
	public void acquireWakeLock(){
		if(!m_powerWakeLock.isHeld()){
			m_powerWakeLock.acquire();
		}
	}
	public void releaseWakeLock(){
		if(m_powerWakeLock.isHeld()){
			m_powerWakeLock.release();
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
	
	public IBinder onBind(Intent intent) {
		return null;
	}
		
	private void onStart_impl(Intent _intent){
				
		try{
			Connect_impl();
		}catch(Exception e){
			m_mainApp.setErrorString("onStart_impl", e);
		}
	}
	
	private void markDelMail(Intent _intent,boolean _markOrDel){
		
		String t_mailHashcode = _intent.getExtras().getString(YuchDroidApp.DATA_FILTER_MARK_MAIL_READ_MAILID);
		String[] t_mailHashList = t_mailHashcode.split(fetchMail.fsm_vectStringSpliter);
		
		for(String id : t_mailHashList){
			
			if(id.length() != 0){
				
				int t_hash = Integer.valueOf(id).intValue();
				try{
					ByteArrayOutputStream os  = new ByteArrayOutputStream();
					
					byte t_type = _markOrDel?msg_head.msgBeenRead:msg_head.msgMailDel;
					
					os.write(t_type);
					sendReceive.WriteInt(os, t_hash);
					
					m_sendingQueue.addSendingData(t_type, os.toByteArray(), true);
				}catch(Exception e){
					m_mainApp.setErrorString("ConnectDeamon MailMarkReadRecv", e);
				}
			}
		}
	}
	
	private void clearSendingAttachment(){
		// destroy the all sendingMailDeamon
		//
		synchronized (m_sendingMailAttachment) {
			for(SendMailDeamon de:m_sendingMailAttachment){
				de.m_closeState = true;
				de.inter();
				de.sendError();
			}
			m_sendingMailAttachment.clear();	
		}
	}
		
	public void onDestroy(){
		sm_connectDeamon = this;
		m_mainApp.setErrorString("ConnectDeamon onDestroy");
		
		m_connectState = false;
		m_destroy = true;
		
		closeConnect();
		
		if(m_tmpConnectSelector != null){
			m_tmpConnectSelector.wakeup();
		}		
	
		if(m_sendingQueue != null){
			m_sendingQueue.destory();
			m_sendingQueue = null;
		}
				
		m_mainApp.m_connectDeamonRun = false;
		m_mainApp.setConnectState(YuchDroidApp.STATE_DISCONNECT);
		m_mainApp.stopConnectNotification();
		
		stopReconnectAlarm();
		clearSendingAttachment();		
		releaseWakeLock();
		
		unregisterReceiver(m_mailMarkReadRecv);
		unregisterReceiver(m_mailSendRecv);
		unregisterReceiver(m_delMailRecv);
		unregisterReceiver(m_reconnectRecv);
		unregisterReceiver(m_destoryRecv);
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
		
	private void DialogAlert(String _text){
		GlobalDialog.showInfo(_text, this);
	}
	
	sendReceive.IStoreUpDownloadByte m_upDownloadByteInterface = new sendReceive.IStoreUpDownloadByte(){
		public void store(long uploadByte, long downloadByte) {
			StoreUpDownloadByte(uploadByte,downloadByte,true);
		}
		
		public int getPushInterval(){
			return m_mainApp.m_config.getPulseInterval();
		}
		
		public void logOut(String _log){
			m_mainApp.setErrorString(_log);
		}
	};
	
	// delay destroy alarm 
	// if the user disconnect for 3 * (pulse interval) , we must destroy that  
	//
	private PendingIntent m_destoryAlarm = null;
	private BroadcastReceiver m_destoryRecv = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if(!m_connectState){				
				stopSelf();
			}
		}
	};
	private synchronized void startDestoryAlarm(){
		if(m_destoryAlarm == null){
			AlarmManager t_msg = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
			Intent notificationIntent = new Intent(FILTER_DESTROY);
			m_destoryAlarm = PendingIntent.getBroadcast(this, 0, notificationIntent,0);
			
			t_msg.set(AlarmManager.RTC_WAKEUP, 
							System.currentTimeMillis() + 3 * m_mainApp.m_config.getPulseInterval(), 
							m_destoryAlarm);
		}
	}
	
	private synchronized void stopDestoryAlarm(){
		if(m_destoryAlarm != null){
			m_destoryAlarm.cancel();
			m_destoryAlarm = null;
		}
	}
	
	// reconnect Alarm receiver
	//
	PendingIntent	m_reconnectAlarm = null;
	int				m_reconnectCounter = 0;
	BroadcastReceiver m_reconnectRecv = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent){	
			synchronized(m_reconnectRecv){
				m_reconnectRecv.notify();
			}
			
			synchronized (ConnectDeamon.this) {
				m_reconnectAlarm = null;
			}
		}
	};
	
	private synchronized void startReconnectAlarm(){
		if(m_reconnectAlarm == null){
						
			AlarmManager t_msg = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
			Intent notificationIntent = new Intent(FILTER_RECONNECT);
			m_reconnectAlarm = PendingIntent.getBroadcast(this, 0, notificationIntent,0);
			
			t_msg.set(AlarmManager.RTC_WAKEUP, 
							System.currentTimeMillis() + m_mainApp.m_config.getPulseInterval(), 
							m_reconnectAlarm);			
			
		}
	}
	
	private synchronized void stopReconnectAlarm(){
		if(m_reconnectAlarm != null){
			m_reconnectAlarm.cancel();
			m_reconnectAlarm = null;
		}
	}
	
	private synchronized boolean canReconnectImm(){
	
		if(m_reconnectCounter++ < 2){
			return true;
		}
		
		m_reconnectCounter = 0;
		return false;
	}
	
	private sendReceive getConnection(boolean _ssl)throws Exception{
				
		if(_ssl){
			throw new IllegalArgumentException("Current YuchDroid can't support SSL!");
		}
		 
		if(!canReconnectImm()){
			
			startReconnectAlarm();
			synchronized (m_reconnectRecv) {
				m_reconnectRecv.wait();
			}			
		}
				 
		if(m_destroy){
			throw new Exception("user destroy");
		}
		
		if(!m_connectState){
			throw new Exception("user reset connect");
		}
		
		if(CanNotConnectSvr()){
			throw new Exception("CanNotConnectSvr ");
		}
		
		acquireWakeLock();
		
		m_tmpConnectSelector = SelectorProvider.provider().openSelector();
		try{
			SocketChannel t_chn = SocketChannel.open();
			
			t_chn.configureBlocking(false);
			t_chn.register(m_tmpConnectSelector, SelectionKey.OP_CONNECT);
			
			t_chn.connect(new InetSocketAddress(m_mainApp.m_config.m_host, m_mainApp.m_config.m_port));
			m_tmpConnectSelector.select(10000);
									
			if(!t_chn.finishConnect() || !m_connectState){
				
				try{
					t_chn.close();
				}catch(Exception e){
					m_mainApp.setErrorString("t_chn.close();",e);
				}
				
				try{
					m_tmpConnectSelector.close();
				}catch(Exception e){
					m_mainApp.setErrorString("tmpConnectSelector.close()",e);
				}
				
				if(!m_connectState){
					throw new Exception("user close connect");
				}else{
					throw new ConnectException("client socket connect time out!");
				}				
			}
			
			synchronized (this) {
				m_reconnectCounter = 0;
			}
			
			return new sendReceive(this,m_tmpConnectSelector,t_chn,_ssl,
									m_upDownloadByteInterface);
		}finally{

			if(m_sendingMailAttachment.isEmpty()){
				//TODO im and weibo send too
				//
				releaseWakeLock();
			}
			
			m_tmpConnectSelector = null;
		}			
						
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
	
	
	public static void Connect(){
		if(sm_connectDeamon != null){
			try{
				sm_connectDeamon.stopDestoryAlarm();
				sm_connectDeamon.Connect_impl();
			}catch(Exception e){
				Log.e(TAG,e.getMessage());
			}
		}
	}
	private synchronized void Connect_impl()throws Exception{
		
		Disconnect_impl();	
		m_mainApp.setConnectState(YuchDroidApp.STATE_CONNECTING);
		m_connectState = true;
		synchronized (m_reconnectRecv) {
			m_reconnectRecv.notify();
		}
	}
	public static void Disconnect(){
		if(sm_connectDeamon != null){
			try{
				sm_connectDeamon.startDestoryAlarm();				
				sm_connectDeamon.Disconnect_impl();
			}catch(Exception e){
				Log.e(TAG,e.getMessage());
			}
		}
	}
	private synchronized void Disconnect_impl()throws Exception{
			
		m_connectState = false;		
		m_reconnectCounter = 0;
		m_connectFailedCounter = 0;
		
		synchronized (m_reconnectRecv) {
			m_reconnectRecv.notify();
		}
		
		closeConnect();
		clearSendingAttachment();
		stopReconnectAlarm();
		
		m_mainApp.setConnectState(YuchDroidApp.STATE_DISCONNECT);
		m_mainApp.StopDisconnectNotification();
		
		if(m_tmpConnectSelector != null){
			m_tmpConnectSelector.wakeup();
		}		
	}
	
	public void run(){
		
		while(!m_destroy){
				
			m_sendAuthMsg = false;
			
			while(!m_connectState){				
				// wait user to start connect
				//
				synchronized (m_reconnectRecv) {
					try{
						m_reconnectRecv.wait();
					}catch(Exception e){}
				}
			}
			
			try{
				
				synchronized (this) {
					m_connectFailedCounter++;
				}				
				
				m_connect = getConnection(IsUseSSL());
								
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
				
				// adapt blackberry to mark android client
				// if the client is not same platform , it will require PIN and IMEI again
				//
				sendReceive.WriteString(t_os,"6.0.fffff");
				int t_size = (YuchDroidApp.sm_displyWidth << 16) | YuchDroidApp.sm_displyHeight;
				sendReceive.WriteInt(t_os,t_size);
				sendReceive.WriteBoolean(t_os,m_mainApp.m_config.m_enableIMModule);
				
				m_connect.SendBufferToSvr(t_os.toByteArray(), true);
				
				m_sendAuthMsg = true;
				
				synchronized (this) {
					m_connectFailedCounter = 0;
				}
								
				// set the text connect
				//
				m_mainApp.setConnectState(YuchDroidApp.STATE_CONNECTED);
				m_mainApp.StopDisconnectNotification();
				m_sendingQueue.connectNotify();
																
				while(true){
					ProcessMsg(m_connect.RecvBufferFromSvr());
				}
				
			}catch(Exception _e){
				
				if(m_connectState){
					m_mainApp.setConnectState(YuchDroidApp.STATE_CONNECTING);		
				}else{
					m_mainApp.setConnectState(YuchDroidApp.STATE_DISCONNECT);
				}
				
				m_mainApp.setErrorString("M ",_e);
			}
					
			if(!isAppOnForeground() && m_connectFailedCounter >= 2){
				m_connectFailedCounter = 0;
				m_mainApp.TriggerDisconnectNotification();
			}			
			
			closeConnect();
		}
	}
	
	
	
	private synchronized void ProcessMsg(byte[] _package)throws Exception{
		 ByteArrayInputStream in  = new ByteArrayInputStream(_package);
		 
		 final int t_msg_head = in.read();
		 
		 switch(t_msg_head){
		 	case msg_head.msgKeepLive:
		 		//m_mainApp.setErrorString("back pulse!");
		 		break;
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
		
		// check the default account
		//
		boolean t_send = true;
		for(String str:m_mainApp.m_config.m_sendMailAccountList){
			if(str.equals(t_mail.getOwnAccount())
			// the low version can't send the own account 
			//
			|| t_mail.getOwnAccount().length() == 0){
				
				t_send = false;
				break;
			}
		}
		
		if(t_send){
			sendRequestMailAccountMsg();
		}
	}

	
	private void ProcessSentMail(InputStream in)throws Exception{
		
		boolean t_succ = sendReceive.ReadBoolean(in);
		final long t_time = sendReceive.ReadLong(in);
		
		// delete the fetchMail send deamon thread
		//
		synchronized (m_sendingMailAttachment) {
			for(SendMailDeamon t_deamon : m_sendingMailAttachment){
								
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
//					for(int j = 0 ;j < m_mainApp.m_uploadingDesc.size();j++){
//						recvMain.UploadingDesc t_desc = (recvMain.UploadingDesc)m_mainApp.m_uploadingDesc.elementAt(j);
//						if(t_desc.m_mail == t_deamon.m_sendMail){
//						
//							m_mainApp.m_uploadingDesc.removeElementAt(j);
//							break;
//						}
//					}
									
					break;
				}
			}
		}
		
	}
	
	private void ProcessFileAttach(InputStream in)throws Exception{
		//TODO recevice file attach
	}
	
}
