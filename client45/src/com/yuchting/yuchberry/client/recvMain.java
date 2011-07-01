package com.yuchting.yuchberry.client;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Vector;

import javax.microedition.content.Invocation;
import javax.microedition.content.Registry;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.location.Criteria;
import javax.microedition.location.Location;
import javax.microedition.location.LocationListener;
import javax.microedition.location.LocationProvider;

import local.localResource;
import net.rim.blackberry.api.browser.Browser;
import net.rim.blackberry.api.browser.BrowserSession;
import net.rim.blackberry.api.homescreen.HomeScreen;
import net.rim.blackberry.api.mail.Message;
import net.rim.blackberry.api.menuitem.ApplicationMenuItem;
import net.rim.blackberry.api.menuitem.ApplicationMenuItemRepository;
import net.rim.device.api.crypto.MD5Digest;
import net.rim.device.api.i18n.Locale;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.i18n.SimpleDateFormat;
import net.rim.device.api.notification.NotificationsConstants;
import net.rim.device.api.notification.NotificationsManager;
import net.rim.device.api.system.ApplicationManager;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.CodeModuleManager;
import net.rim.device.api.system.Display;
import net.rim.device.api.system.WLANInfo;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.UiEngine;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.DialogClosedListener;

import com.yuchting.yuchberry.client.weibo.WeiboItemField;
import com.yuchting.yuchberry.client.weibo.fetchWeibo;
import com.yuchting.yuchberry.client.weibo.weiboTimeLineScreen;

public class recvMain extends UiApplication implements localResource,LocationListener {
	
	public final static int 		fsm_display_width		= Display.getWidth();
	public final static int 		fsm_display_height		= Display.getHeight();
	public final static String	fsm_OS_version			= CodeModuleManager.getModuleVersion((CodeModuleManager.getModuleHandleForObject("")));;
	
	public static ResourceBundle sm_local = ResourceBundle.getBundle(localResource.BUNDLE_ID, localResource.BUNDLE_NAME);
	
	final static long		fsm_notifyID_email = 767918509114947L;
	
	final static Object 	fsm_notifyEvent_email = new Object() {
	    public String toString() {
	       return recvMain.sm_local.getString(localResource.NOTIFY_EMAIL_LABEL);
	    }
	};
	
	final static long		fsm_notifyID_weibo = 767918509114948L;
	
	final static Object 	fsm_notifyEvent_weibo = new Object() {
	    public String toString() {
	       return recvMain.sm_local.getString(localResource.NOTIFY_WEIBO_LABEL);
	    }
	};
	
	final static long		fsm_notifyID_disconnect = 767918509114949L;
	
	final static Object 	fsm_notifyEvent_disconnect = new Object() {
	    public String toString() {
	       return recvMain.sm_local.getString(localResource.NOTIFY_DISCONNECT_LABEL);
	    }
	};
	
	public connectDeamon 		m_connectDeamon		= new connectDeamon(this);
	
    aboutScreen			m_aboutScreen		= null;
	stateScreen 		m_stateScreen 		= null;
	uploadFileScreen 	m_uploadFileScreen	= null;
	debugInfo			m_debugInfoScreen	= null;
	downloadDlg			m_downloadDlg		= null;
	settingScreen		m_settingScreen		= null;
	shareYBScreen		m_shareScreen		= null;
		
	UiApplication		m_downloadDlgParent = null;
	
	UiApplication		m_messageApplication = null;
	
	String				m_stateString		= recvMain.sm_local.getString(localResource.DISCONNECT_BUTTON_LABEL);
	String				m_aboutString		= recvMain.sm_local.getString(localResource.ABOUT_DESC);
	
	final class ErrorInfo{
		Date		m_time;
		String		m_info;
		
		ErrorInfo(String _info){
			m_info	= _info;
			m_time	= new Date();
		}
		
	}
	
	Vector				m_errorString		= new Vector();	
	Vector				m_uploadingDesc 	= new Vector();
	
	String				m_hostname 			= new String();
	int					m_port 				= 0;
	String				m_userPassword 		= new String();
	boolean			m_useSSL			= false;
	boolean			m_useWifi			= false;
	boolean			m_useMDS			= false;
		
	boolean			m_autoRun			= false;
	
	boolean			m_discardOrgText	= false;
	boolean			m_delRemoteMail		= false;
	
	final class APNSelector{
		String		m_name			= null;
		int			m_validateNum	= 0;
	}
	
	Vector				m_APNList 			= new Vector();
	int					m_currentAPNIdx 	= 0;
	int					m_changeAPNCounter 	= 0;
	String				m_appendString		= new String();
	
	long				m_uploadByte		= 0;
	long				m_downloadByte		= 0;
	
	int					m_sendMailNum		= 0;
	int					m_recvMailNum		= 0;
	String				m_passwordKey		= "";
	
	boolean			m_connectDisconnectPrompt = false;
	
	
	static final String[]	fsm_recvMaxTextLenghtString = {"∞","1KB","5KB","10KB","50KB"};
	static final int[]	fsm_recvMaxTextLenght		= {0,1024,1024*5,1024*10,1024*50};
	int						m_recvMsgTextLengthIndex = 0;
	
	
	static final String[]	fsm_pulseIntervalString = {"1","3","5","10","30"};
	static final int[]	fsm_pulseInterval		= {1,3,5,10,30};
	int						m_pulseIntervalIndex = 2;
	
	boolean			m_fulldayPrompt		= true;
	int					m_startPromptHour	= 8;
	int					m_endPromptHour		= 22;
	
	boolean			m_copyMailToSentFolder = false;
	
	final class UploadingDesc{
		
		fetchMail		m_mail = null;
		int				m_attachmentIdx;
		int				m_uploadedSize;
		int				m_totalSize;		
	}
		
	ApplicationMenuItem m_addItem	= new ApplicationMenuItem(20){
						
		public String toString(){
			return recvMain.sm_local.getString(localResource.ADD_ATTACHMENT);
		}
		
		public Object run(Object context){
			if(context instanceof Message ){
				OpenAttachmentFileScreen(false);
				return m_uploadFileScreen;
			}
			
			return context;
		}
	};
	
	ApplicationMenuItem	m_delItem	= new ApplicationMenuItem(21){
		public String toString(){
			return recvMain.sm_local.getString(localResource.CHECK_DEL_ATTACHMENT);
		}
		
		public Object run(Object context){
			if(context instanceof Message ){

				OpenAttachmentFileScreen(true);
				return m_uploadFileScreen;
			}
			
			return context;	
		}
	};
		
	String m_latestVersion			= null;
	
	//@{ location information
	LocationProvider m_locationProvider = null;
	boolean		 m_useLocationInfo = false;
	boolean		 m_setLocationListener = false;
	
	GPSInfo			m_gpsInfo = new GPSInfo();
	//@}
	
	FileConnection m_logfc				= null;
	OutputStream	m_logfcOutput		= null;
	
	
	// weibo module
	public boolean			m_enableWeiboModule			= false;
	public boolean			m_updateOwnListWhenFw		= true;
	public boolean			m_updateOwnListWhenRe		= false;
	public boolean			m_dontDownloadWeiboHeadImage= false;
		
	public String[]				m_weiboHeadImageDir_sub = 
	{
		"Sina/",
		"TW/",
		"QQ/",
		
		"163/",
		"SOHU/",
		"FAN/",
	};
		
	public static void main(String[] args) {
		recvMain t_theApp = new recvMain(ApplicationManager.getApplicationManager().inStartup());		
		t_theApp.enterEventDispatcher();
	}
	
	public boolean		canUseLocation(){
		return m_useLocationInfo;
	}
	
	public GPSInfo getGPSInfo(){
		return m_gpsInfo;
	}
	
	public int getMaxWeiboNum(){
		if(m_maxWeiboNumIndex >= 0 && m_maxWeiboNumIndex < fsm_maxWeiboNum.length){
			return fsm_maxWeiboNum[m_maxWeiboNumIndex];
		}
		
		return fsm_maxWeiboNum[0];
	}
	
	public recvMain(boolean _systemRun) {	
	
		try{
			FileConnection fc = (FileConnection) Connector.open(uploadFileScreen.fsm_rootPath_back + "YuchBerry/",Connector.READ_WRITE);
			try{
				if(!fc.exists()){
					fc.mkdir();
				}
			}finally{
				fc.close();
				fc = null;
			}			
		}catch (Exception _e) {
			DialogAlertAndExit("can't use the dev ROM to store config file!");
        	return;
		}
						
		GetAttachmentDir();
		        
        Criteria t_criteria = new Criteria();
		t_criteria.setCostAllowed(false);
		t_criteria.setHorizontalAccuracy(50);
		t_criteria.setVerticalAccuracy(50);
		
		try{
			m_locationProvider = LocationProvider.getInstance(t_criteria);
			if(m_locationProvider == null){
				SetErrorString("your device can't support location");
			}
		}catch(Exception e){
			SetErrorString("location:"+e.getMessage()+" " + e.getClass().getName());
		}     
        
		// must read the configure first
		//
		WriteReadIni(true);		
		
        if(_systemRun){
        	
        	// register the notification
        	//
        	NotificationsManager.registerSource(fsm_notifyID_email, fsm_notifyEvent_email,NotificationsConstants.CASUAL);
        	NotificationsManager.registerSource(fsm_notifyID_weibo, fsm_notifyEvent_weibo,NotificationsConstants.CASUAL);
        	NotificationsManager.registerSource(fsm_notifyID_disconnect, fsm_notifyEvent_disconnect,NotificationsConstants.CASUAL);
        	
        	if(!m_autoRun || m_hostname.length() == 0 || m_port == 0 || m_userPassword.length() == 0){
        		System.exit(0);
        	}else{
        		try{
        			m_connectDeamon.Connect();
        			Start();
        		}catch(Exception e){
        			System.exit(0);
        		}
        	}      	
        }
        
        InitWeiboModule();
	}
	
	private boolean m_initWeiboHeadImageDir = false; 
	public String GetWeiboHeadImageDir(int _style)throws Exception{
		
		if(!isSDCardAvaible()){
			throw new Exception("Can't use the sd card to store weibo head image.");
		}
		
		String t_weiboHeadImageDir = uploadFileScreen.fsm_rootPath_default + "YuchBerry/WeiboImage/";
		
		// connect the string of head image directory
		//
		if(!m_initWeiboHeadImageDir){
    		m_initWeiboHeadImageDir = true;
        	for(int i = 0;i < m_weiboHeadImageDir_sub.length;i++){
        		m_weiboHeadImageDir_sub[i] = t_weiboHeadImageDir + m_weiboHeadImageDir_sub[i];
        	}
    	}
		
		// create the sdcard path 
		//
    	FileConnection fc = (FileConnection) Connector.open(t_weiboHeadImageDir,Connector.READ_WRITE);
    	try{
    		if(!fc.exists()){
        		fc.mkdir();
        		
        		// attempt create the head image directory
        		//
        		for(int i = 0;i < m_weiboHeadImageDir_sub.length;i++){
        			FileConnection tfc = (FileConnection) Connector.open(m_weiboHeadImageDir_sub[i],Connector.READ_WRITE);
                	try{
                		if(!tfc.exists()){
                			tfc.mkdir();
                    	}	
                	}finally{
                		tfc.close();
                		tfc = null;
                	}
            	}
        	}
    	}finally{
    		fc.close();
    		fc = null;
    	}
    	    	
    	t_weiboHeadImageDir = null;
        
		return m_weiboHeadImageDir_sub[_style];
	}
	
	public boolean isSDCardAvaible(){
		boolean t_SDCardUse = false;
		
		try{
			FileConnection fc = (FileConnection) Connector.open(uploadFileScreen.fsm_rootPath_default + "YuchBerry/",Connector.READ_WRITE);
			try{
				if(!fc.exists()){
					fc.mkdir();
				}	
			}finally{
				fc.close();
				fc = null;
			}			
			t_SDCardUse = true;
		}catch(Exception e){
			SetErrorString("SDCard can't be used :(");
		}
		
		return t_SDCardUse;
	}
	
	public String GetAttachmentDir(){
		
		String t_attDir = (isSDCardAvaible()?uploadFileScreen.fsm_rootPath_default:uploadFileScreen.fsm_rootPath_back) + "YuchBerry/AttDir/";
		
		try{
			FileConnection fc = (FileConnection) Connector.open(t_attDir,Connector.READ_WRITE);
	    	try{
	        	if(!fc.exists()){
	        		fc.mkdir();
	        	}	
	    	}finally{
	    		fc.close();
	    		fc = null;
	    	}
		}catch(Exception e){
			DialogAlertAndExit("create AttDir failed: " + t_attDir);
			t_attDir = "";
		}
				
		return t_attDir;
	}
	
	public void DialogAlertAndExit(final String _msg) {

		invokeLater(new Runnable() {
			public void run() {
				synchronized(getEventLock()) {
					Dialog.alert(_msg);
					System.exit(0);
				}
			}
		});
	
	}
	
	public void SetReportLatestVersion(String _latestVersion){
		m_latestVersion = _latestVersion;
		
		if(m_latestVersion != null){
			
			if(m_stateScreen != null){
				PopupLatestVersionDlg();
			}			
		}
	}
	
	private void PopupLatestVersionDlg(){
		
		if(m_latestVersion != null){
						
			Dialog t_dlg = new Dialog(Dialog.D_OK_CANCEL,sm_local.getString(localResource.LATEST_VER_REPORT) + m_latestVersion,
					Dialog.OK,Bitmap.getPredefinedBitmap(Bitmap.EXCLAMATION),Manager.VERTICAL_SCROLL);
			
			t_dlg.setDialogClosedListener(new DialogClosedListener(){
			
				public void dialogClosed(Dialog dialog, int choice) {
					
					switch (choice) {
						case Dialog.OK:
							openURL("http://code.google.com/p/yuchberry/downloads/list");
							
							break;
						
						default:
							break;
					}
				}
			});
			
			t_dlg.setEscapeEnabled(true);
			synchronized (getEventLock()) {
				pushGlobalScreen(t_dlg,1, UiEngine.GLOBAL_QUEUE);
			}
			
			m_latestVersion = null;
		}
	}
	
	public String GetAPNName(){
		
		if(++m_changeAPNCounter > 3){
			m_changeAPNCounter = 0;
			
			if(++m_currentAPNIdx >= m_APNList.size()){
				m_currentAPNIdx = 0;
			}
		}		
		
		if(m_currentAPNIdx < m_APNList.size()){
			return ((APNSelector)m_APNList.elementAt(m_currentAPNIdx)).m_name;
		}
		
		return "";
	}
	
	public String GetAPNList(){
		
		if(!m_APNList.isEmpty()){
			String t_str = ((APNSelector)m_APNList.elementAt(0)).m_name;
			
			for(int i = 1;i < m_APNList.size();i++){
				APNSelector t_sel = (APNSelector)m_APNList.elementAt(i); 
				t_str = t_str + ";" + t_sel.m_name;
			}
			
			return t_str;
		}		
		
		return "";
	}
	
	public boolean UseWifiConnection(){
		
		// 4.2
		// return false;
		
//		int stat = WLANInfo.getWLANState();
//		boolean t_connect = WLANInfo.getWLANState() == WLANInfo.WLAN_STATE_CONNECTED;
//		t_connect = WLANInfo.getAPInfo() != null;
//		t_connect = WLANInfo.getWLANState() != WLANInfo.WLAN_STATE_DISCONNECTED; 
//		t_connect = CoverageInfo.isCoverageSufficient(CoverageInfo.COVERAGE_DIRECT,RadioInfo.WAF_WLAN, false);
//		
		if(m_useWifi && WLANInfo.getAPInfo() != null){
			SetErrorString("Using wifi to connect");
			return true;
		}
		
		return false;
	}
	
	public int GetRecvMsgMaxLength(){
		return fsm_recvMaxTextLenght[m_recvMsgTextLengthIndex];
	}
	
	public void SetAPNName(String _APNList){
		
		m_APNList.removeAllElements();
		
		int t_beginIdx = 0;
		int t_endIdx = -1;
		
		do{
			t_endIdx = _APNList.indexOf(';',t_beginIdx);
			
			if(t_endIdx != -1){
				String t_name = _APNList.substring(t_beginIdx, t_endIdx);
				if(t_name.length() != 0){
					APNSelector t_sel = new APNSelector();
					t_sel.m_name = t_name;
					m_APNList.addElement(t_sel);
				}
				
			}else{
				String t_name = _APNList.substring(t_beginIdx, _APNList.length());
				if(t_name.length() != 0){
					APNSelector t_sel = new APNSelector();
					t_sel.m_name = t_name;
					m_APNList.addElement(t_sel);
				}
				break;
			}
			
			t_beginIdx = t_endIdx + 1;
			
		}while(t_beginIdx < _APNList.length());
		
	}
	
	public String GetURLAppendString(){

		String t_result = "";
		
		if(UseWifiConnection()){
			
			t_result = ";interface=wifi";
			
		}else{
			
			String t_APN = GetAPNName();
			
			if(t_APN.length() != 0){
				t_result = ";apn=" + t_APN;			
			}
		
			if(m_appendString.length() != 0){
				
				final String t_replaceSign = "$apn$";
				
				final int t_replaceIdx = m_appendString.indexOf(t_replaceSign); 
				if( t_replaceIdx != -1 && t_APN.length() != 0){
					t_result = t_result + ";" + m_appendString.substring(0,t_replaceIdx) + t_APN + m_appendString.substring(t_replaceIdx + t_replaceSign.length());
				}else{
					t_result = t_result + ";" + m_appendString;
				}
			}
		}		
		 
		return t_result;
	}
	
	public int GetSendMailNum(){return m_sendMailNum;}
	public void SetSendMailNum(int _num){
		m_sendMailNum = _num;
		
		if(m_settingScreen != null){
			m_settingScreen.RefreshUpDownloadByte();
		}
	}
	
	public int GetRecvMailNum(){return m_recvMailNum;}
	public void SetRecvMailNum(int _num){
		m_recvMailNum = _num;
		
		if(m_settingScreen != null){
			m_settingScreen.RefreshUpDownloadByte();
		}
	}	
	
	public boolean IsPromptTime(){
		
		if(!m_fulldayPrompt){
			SimpleDateFormat t_format = new SimpleDateFormat("HH");
			final int t_hour = Integer.valueOf(t_format.format(new Date())).intValue();
			return t_hour >= m_startPromptHour && t_hour + 1 <= m_endPromptHour;
		}
		return true;
	}
	public String GetHostName(){
		return m_hostname;
	}
	
	public int GetHostPort(){
		return m_port;
	}
	
	public String GetUserPassword(){
		return m_userPassword;
	}
	
	public boolean IsUseSSL(){
		return m_useSSL;
	}
	
	public boolean UseMDS(){
		return m_useMDS;
	}
	
	public int GetPulseIntervalMinutes(){
		return fsm_pulseInterval[m_pulseIntervalIndex];
	}
	
	
//	static public synchronized void Copyfile(String _from,String _to)throws Exception{
//		
//		byte[] t_buffer = null;
//		
//		FileConnection t_fromFile = (FileConnection) Connector.open(_from,Connector.READ_WRITE);
//		try{
//			if(t_fromFile.exists()){
//
//				t_buffer = new byte[(int)t_fromFile.fileSize()];
//				InputStream t_readFile = t_fromFile.openInputStream();
//				try{
//					t_readFile.read(t_buffer);
//				}finally{
//					
//					t_readFile.close();
//					t_readFile = null;
//				}
//								
//			}else{
//				return ;
//			}
//			
//		}finally{		
//			
//			t_fromFile.close();
//			t_fromFile = null;
//		}	
//		
//		
//		FileConnection t_toFile  = (FileConnection) Connector.open(_to,Connector.READ_WRITE);
//		try{
//			if(!t_toFile.exists()){
//				t_toFile.create();
//			}
//			
//			OutputStream t_writeFile = t_toFile.openOutputStream();
//			try{
//				t_writeFile.write(t_buffer);
//			}finally{
//				t_writeFile.close();
//				t_writeFile = null;							
//			}
//		}finally{
//			t_toFile.close();
//			t_toFile = null;
//		}		
//	}
	
	
	private void PreWriteReadIni(boolean _read,
			String _backPathFilename,String _orgPathFilename,
			String _backFilename,String _orgFilename){
		
		try{

			if(_read){
							
				FileConnection t_back = (FileConnection) Connector.open(_backPathFilename,Connector.READ_WRITE);
				try{
					if(t_back.exists()){
						FileConnection t_ini	= (FileConnection) Connector.open(_orgPathFilename,Connector.READ_WRITE);
						try{
							if(t_ini.exists()){
								t_ini.delete();
							}	
						}finally{
							t_ini.close();
							t_ini = null;
						}
						
						t_back.rename(_orgFilename);
					}
				}finally{
					t_back.close();
					t_back = null;
				}				
				
			}else{
				
				FileConnection t_ini	= (FileConnection) Connector.open(_orgPathFilename,Connector.READ_WRITE);
				try{
					if(t_ini.exists()){
						t_ini.rename(_backFilename);
					}
				}finally{
					t_ini.close();
					t_ini = null;
				}
				
				// needn't copy ,the normal WriteReadIni method will re-create the init.data file
				//
				//Copyfile(fsm_backInitFilename,fsm_initFilename);
			}
			
		}catch(Exception e){
			SetErrorString("write/read PreWriteReadIni file from SDCard error :" + e.getMessage() + e.getClass().getName());
		}
		
	}
	
	final static int		fsm_clientVersion = 25;
	
	static final String fsm_initFilename_init_data = "Init.data";
	static final String fsm_initFilename_back_init_data = "~Init.data";
	
	static final String fsm_initFilename = uploadFileScreen.fsm_rootPath_back + "YuchBerry/" + fsm_initFilename_init_data;
	static final String fsm_backInitFilename = uploadFileScreen.fsm_rootPath_back + "YuchBerry/" + fsm_initFilename_back_init_data;
	
	public synchronized void WriteReadIni(boolean _read){
		
		// process the ~Init.data file to restore the destroy original file
		// that writing when device is down  
		//
		// check the issue 85 
		// http://code.google.com/p/yuchberry/issues/detail?id=85&colspec=ID%20Type%20Status%20Priority%20Stars%20Summary
		//
		PreWriteReadIni(_read,fsm_backInitFilename,fsm_initFilename,
				fsm_initFilename_back_init_data,fsm_initFilename_init_data);
		
		try{
			
			FileConnection fc = (FileConnection) Connector.open(fsm_initFilename,Connector.READ_WRITE);
			try{
				if(_read){
					
			    	if(fc.exists()){
			    		InputStream t_readFile = fc.openInputStream();
			    		try{
			    			final int t_currVer = sendReceive.ReadInt(t_readFile);
				    		
				    		m_hostname		= sendReceive.ReadString(t_readFile);
				    		m_port			= sendReceive.ReadInt(t_readFile);
				    		m_userPassword	= sendReceive.ReadString(t_readFile);
				    		
				    		// read the APN validate 
				    		//
				    		final int t_apnNum = sendReceive.ReadInt(t_readFile);
				    		for(int i = 0 ;i < t_apnNum;i++){
				    			APNSelector t_sel = new APNSelector();
				    			t_sel.m_name 		= sendReceive.ReadString(t_readFile);
				    			t_sel.m_validateNum	= sendReceive.ReadInt(t_readFile);
				    			m_APNList.addElement(t_sel);
				    		}
				    		
				    		if(t_currVer >= 2){
				    			m_useSSL = (t_readFile.read() == 0)?false:true;
				    		}
				    		
				    		
				    		if(t_currVer >= 4){
				    			m_useWifi = (t_readFile.read() == 0)?false:true;
				    			m_appendString = sendReceive.ReadString(t_readFile);		    			
				    		}
				    						    		
				    		if(t_currVer >= 5){
				    			m_autoRun = (t_readFile.read() == 0)?false:true;		    			
				    		}
				    						    		
				    		if(t_currVer >= 6){
				    			m_uploadByte = sendReceive.ReadLong(t_readFile);
				    			m_downloadByte = sendReceive.ReadLong(t_readFile);
				    		}
				    		
				    		if(t_currVer >= 7){
				    			m_pulseIntervalIndex = sendReceive.ReadInt(t_readFile);
				    		}
				    		
				    		if(t_currVer >= 8){
				    			m_fulldayPrompt = sendReceive.ReadBoolean(t_readFile);
				    			m_startPromptHour = sendReceive.ReadInt(t_readFile);
			    				m_endPromptHour = sendReceive.ReadInt(t_readFile);		    			
				    		}	
				    		
				    		if(t_currVer >= 8){
				    			m_useLocationInfo = sendReceive.ReadBoolean(t_readFile);
				    		}
				    		
				    		if(t_currVer >= 10){
				    			m_useMDS = sendReceive.ReadBoolean(t_readFile);
				    		}
				    		
				    		if(t_currVer >= 11){
				    			m_sendMailNum = sendReceive.ReadInt(t_readFile);
				    			m_recvMailNum = sendReceive.ReadInt(t_readFile);
				    		}
				    		
				    		if(t_currVer >= 12){
				    			m_discardOrgText = sendReceive.ReadBoolean(t_readFile);
				    		}
				    		
				    		if(t_currVer >= 13){
				    			m_delRemoteMail = sendReceive.ReadBoolean(t_readFile);
				    		}
				    		
				    		if(t_currVer >= 14){
				    			m_recvMsgTextLengthIndex = sendReceive.ReadInt(t_readFile);
				    		}
				    		
				    		if(t_currVer >= 15){
				    			m_copyMailToSentFolder = sendReceive.ReadBoolean(t_readFile);
				    		}
				    		
				    		if(t_currVer >= 16){
				    			m_passwordKey = sendReceive.ReadString(t_readFile); 
				    		}
				    		
				    		if(t_currVer >= 17){
				    			m_enableWeiboModule = sendReceive.ReadBoolean(t_readFile);
				    			m_updateOwnListWhenFw = sendReceive.ReadBoolean(t_readFile);
				    			m_updateOwnListWhenRe = sendReceive.ReadBoolean(t_readFile);
				    		}
				    		
				    		if(t_currVer >= 18){
				    			WeiboItemField.sm_commentFirst = sendReceive.ReadBoolean(t_readFile);
				    		}
				    		
				    		if(t_currVer >= 19){
				    			m_publicForward		= sendReceive.ReadBoolean(t_readFile);
				    			
				    		}
				    		
				    		if(t_currVer >= 20){
				    			m_maxWeiboNumIndex	= sendReceive.ReadInt(t_readFile);
				    		}
				    		
				    		if(t_currVer >= 21){
				    			m_receivedWeiboNum	= sendReceive.ReadInt(t_readFile);
				    			m_sentWeiboNum		= sendReceive.ReadInt(t_readFile);
				    		}
				    		
				    		if(t_currVer >= 22){
				    			WeiboItemField.sm_displayHeadImage = sendReceive.ReadBoolean(t_readFile);
				    			WeiboItemField.sm_simpleMode = sendReceive.ReadBoolean(t_readFile);
				    			m_dontDownloadWeiboHeadImage = sendReceive.ReadBoolean(t_readFile);
				    		}
				    		
				    		if(t_currVer >= 23){
				    			m_hideHeader	= sendReceive.ReadBoolean(t_readFile);
				    		}
				    		
				    		if(t_currVer >= 24){
				    			m_connectDisconnectPrompt = sendReceive.ReadBoolean(t_readFile);
				    		}
				    		
				    		if(t_currVer >= 25){
				    			WeiboItemField.sm_showAllInList = sendReceive.ReadBoolean(t_readFile);
				    		}
				    		
			    		}finally{
			    			t_readFile.close();
			    			t_readFile = null;
			    		}
			    	}
				}else{
					if(!fc.exists()){
						fc.create();
					}				
					
					OutputStream t_writeFile = fc.openOutputStream();
					try{
						sendReceive.WriteInt(t_writeFile,fsm_clientVersion);
						
						sendReceive.WriteString(t_writeFile, m_hostname);
						sendReceive.WriteInt(t_writeFile,m_port);
						sendReceive.WriteString(t_writeFile, m_userPassword);
						
						// write the APN name and validate number
						//
						sendReceive.WriteInt(t_writeFile,m_APNList.size());
						for(int i = 0 ;i < m_APNList.size();i++){
							APNSelector t_sel = (APNSelector)m_APNList.elementAt(i);
							sendReceive.WriteString(t_writeFile,t_sel.m_name);
							sendReceive.WriteInt(t_writeFile,t_sel.m_validateNum);
						}
						
						sendReceive.WriteBoolean(t_writeFile,m_useSSL);
						sendReceive.WriteBoolean(t_writeFile,m_useWifi);
						sendReceive.WriteString(t_writeFile,m_appendString);
						
						sendReceive.WriteBoolean(t_writeFile,m_autoRun);
						
						sendReceive.WriteLong(t_writeFile,m_uploadByte);
						sendReceive.WriteLong(t_writeFile, m_downloadByte);
						
						sendReceive.WriteInt(t_writeFile,m_pulseIntervalIndex);
						
						
						sendReceive.WriteBoolean(t_writeFile,m_fulldayPrompt);
						sendReceive.WriteInt(t_writeFile,m_startPromptHour);
						sendReceive.WriteInt(t_writeFile,m_endPromptHour);
						
						sendReceive.WriteBoolean(t_writeFile,m_useLocationInfo);
						sendReceive.WriteBoolean(t_writeFile,m_useMDS);
						
						sendReceive.WriteInt(t_writeFile,m_sendMailNum);
						sendReceive.WriteInt(t_writeFile,m_recvMailNum);
						
						sendReceive.WriteBoolean(t_writeFile,m_discardOrgText);
						sendReceive.WriteBoolean(t_writeFile,m_delRemoteMail);
						
						sendReceive.WriteInt(t_writeFile, m_recvMsgTextLengthIndex);
						
						sendReceive.WriteBoolean(t_writeFile,m_copyMailToSentFolder);
						
						sendReceive.WriteString(t_writeFile,m_passwordKey);
						
						sendReceive.WriteBoolean(t_writeFile,m_enableWeiboModule);
						sendReceive.WriteBoolean(t_writeFile,m_updateOwnListWhenFw);
						sendReceive.WriteBoolean(t_writeFile,m_updateOwnListWhenRe);
						sendReceive.WriteBoolean(t_writeFile,WeiboItemField.sm_commentFirst);
						sendReceive.WriteBoolean(t_writeFile,m_publicForward);
						
						sendReceive.WriteInt(t_writeFile,m_maxWeiboNumIndex);
						sendReceive.WriteInt(t_writeFile,m_receivedWeiboNum);
						sendReceive.WriteInt(t_writeFile,m_sentWeiboNum);
						
						sendReceive.WriteBoolean(t_writeFile,WeiboItemField.sm_displayHeadImage);
						sendReceive.WriteBoolean(t_writeFile,WeiboItemField.sm_simpleMode);
						sendReceive.WriteBoolean(t_writeFile,m_dontDownloadWeiboHeadImage);
						
						sendReceive.WriteBoolean(t_writeFile,m_hideHeader);
						
						sendReceive.WriteBoolean(t_writeFile,m_connectDisconnectPrompt);
						sendReceive.WriteBoolean(t_writeFile,WeiboItemField.sm_showAllInList);
						
						if(m_connectDeamon.m_connect != null){
							m_connectDeamon.m_connect.SetKeepliveInterval(GetPulseIntervalMinutes());
						}
						
					}finally{
						t_writeFile.close();
						t_writeFile = null;
					}
					
					// delete the back file ~Init.data
					//
					FileConnection t_backFile = (FileConnection) Connector.open(fsm_backInitFilename,Connector.READ_WRITE);
					try{
						if(t_backFile.exists()){
							t_backFile.delete();
						}
					}finally{
						t_backFile.close();
						t_backFile = null;
					}
				}
			}finally{
				fc.close();
				fc = null;
			}
						
		}catch(Exception _e){
			SetErrorString("write/read config file from SDCard error :" + _e.getMessage() + _e.getClass().getName());
		}	
		
		if(m_locationProvider != null){
			if(m_useLocationInfo){
				
				if(m_setLocationListener == false){
					m_setLocationListener = true;
					
					m_locationProvider.setLocationListener(this, -1, 1, 1);
				}
				
			}else{
				
				if(m_setLocationListener == true){
					
					m_setLocationListener = false;
					m_locationProvider.reset();
					m_locationProvider.setLocationListener(null, -1, -1, -1);
				}				
			}
		}
	
	}
	
	//@{ LocationListener
	public void locationUpdated(LocationProvider provider, Location location){
	    // Respond to the updated location.
	    // If the application registered the location listener with an interval of
	    // 0, the location provider does not provide location updates.
		if(location.isValid()){
			m_gpsInfo.m_heading 	= location.getCourse();
			m_gpsInfo.m_longitude 	= location.getQualifiedCoordinates().getLongitude();
			m_gpsInfo.m_latitude 	= location.getQualifiedCoordinates().getLatitude();
			m_gpsInfo.m_altitude 	= location.getQualifiedCoordinates().getAltitude();
			m_gpsInfo.m_speed 		= location.getSpeed();
			
			if(m_settingScreen != null){
				m_settingScreen.RefreshLocation();
			}
		}
    }
   
	public void providerStateChanged(LocationProvider provider, int newState){
	   switch (newState) {
	     case LocationProvider.AVAILABLE :
	         // The location provider is available.
	    	 break;
	     case LocationProvider.OUT_OF_SERVICE :
	    	 // The location provider is permanently unavailable.
	    	 // Consider cancelling the location listener by calling
	    	 // provider.setLocationListener() with null as the listener.
	    	 break;
	     case LocationProvider.TEMPORARILY_UNAVAILABLE :
	    	 // The location provider is temporarily unavailable.
	        break;
	   	}
       
	}
		
	public synchronized void StoreUpDownloadByte(long _uploadByte,long _downloadByte,boolean _writeIni){
		m_uploadByte += _uploadByte;
		m_downloadByte += _downloadByte;	
				
		if(_writeIni){
			WriteReadIni(false);
		}
	}
	
	public synchronized void ClearUpDownloadByte(){

		m_uploadByte = m_downloadByte = 0;
				
		WriteReadIni(false);
	}
	
	
	public void Start(){
        
		ApplicationMenuItemRepository.getInstance().addMenuItem(ApplicationMenuItemRepository.MENUITEM_EMAIL_EDIT,m_addItem);
		ApplicationMenuItemRepository.getInstance().addMenuItem(ApplicationMenuItemRepository.MENUITEM_EMAIL_EDIT,m_delItem);
				
		WriteReadIni(false);
	}
	
	public void Exit(){
		
		try{
			if(m_connectDeamon.IsConnectState()){
				m_connectDeamon.Disconnect();
			}	
		}catch(Exception e){}
		
		
		ApplicationMenuItemRepository.getInstance().removeMenuItem(ApplicationMenuItemRepository.MENUITEM_EMAIL_EDIT, m_addItem);
		ApplicationMenuItemRepository.getInstance().removeMenuItem(ApplicationMenuItemRepository.MENUITEM_EMAIL_EDIT,m_delItem);	
		
		DisableWeiboModule();
		
		StopNotification();
		StopWeiboNotification();
		StopDisconnectNotification();
		
		if(m_connectDeamon.m_connect != null){
			m_connectDeamon.m_connect.StoreUpDownloadByteImm(true);
		}
		
		if(m_locationProvider != null){
			if(m_useLocationInfo){
				m_locationProvider.reset();
				m_locationProvider.setLocationListener(null, -1, -1, -1);
			}
		}
		
		// store the weibo item list
		ReadWriteWeiboFile(false);
		
		System.exit(0);
	}
	
	public void activate(){
		
		if(m_enableWeiboModule && m_connectDeamon.IsConnectState()){
			
			if(m_weiboTimeLineScreen == null){
				InitWeiboModule();
			}
			if(getScreenCount() == 0){
				pushScreen(m_weiboTimeLineScreen);
			}			
			
		}else{
			pushStateScreen();
		}	
		
		PopupLatestVersionDlg();
		
		if(m_hasNewWeibo){
			m_hasNewWeibo = false;
			
			if(m_connectDeamon.isDisconnectState()){
				HomeScreen.updateIcon(Bitmap.getBitmapResource("Main_offline.png"));
			}else{
				HomeScreen.updateIcon(Bitmap.getBitmapResource("Main.png"));
			}			
		}
	}
	
	public void deactivate(){
		
		if(m_enableWeiboModule ){
			
			if(m_stateScreen != null){
				popScreen(m_stateScreen);
				m_stateScreen = null;
			}
			
			if(m_weiboTimeLineScreen != null){
				
				if(m_weiboTimeLineScreen.m_pushUpdateDlg){
					m_weiboTimeLineScreen.m_currUpdateDlg.close();
				}
				
				if(getScreenCount() == 1){
					popScreen(m_weiboTimeLineScreen);
				}
			}
			
		}else{
			
			if(m_stateScreen != null){
				popScreen(m_stateScreen);
				m_stateScreen = null;
			}
		}			
	}
	
	public void pushStateScreen(){
		if(m_stateScreen == null){
			m_stateScreen = new stateScreen(this);
			pushScreen(m_stateScreen);
		}
	}
	
	public void popStateScreen(){
		if(m_stateScreen != null){
			popScreen(m_stateScreen);
			m_stateScreen = null;
		}	
	}
	
	public void TriggerNotification(){
		if(IsPromptTime()){
			NotificationsManager.triggerImmediateEvent(fsm_notifyID_email, 0, this, null);
		}		
	}
	public void StopNotification(){
		NotificationsManager.cancelImmediateEvent(fsm_notifyID_email, 0, this, null);
	}
	
	public void TriggerWeiboNotification(){
		if(IsPromptTime()){
			NotificationsManager.triggerImmediateEvent(fsm_notifyID_weibo, 0, this, null);
		}		
	}
	
	public void StopWeiboNotification(){
		NotificationsManager.cancelImmediateEvent(fsm_notifyID_weibo, 0, this, null);
	}
	
	public void TriggerDisconnectNotification(){
		if(IsPromptTime() && m_connectDisconnectPrompt){
			NotificationsManager.triggerImmediateEvent(fsm_notifyID_disconnect, 0, this, null);
		}		
	}
	
	public void StopDisconnectNotification(){
		NotificationsManager.cancelImmediateEvent(fsm_notifyID_disconnect, 0, this, null);
	}
	
	
	
	public void PopupAboutScreen(){
		m_aboutScreen = new aboutScreen(this);
		pushScreen(m_aboutScreen);
	}
	
	public void PopupShareScreen(){
		try{
			m_shareScreen = new shareYBScreen(this);
			pushScreen(m_shareScreen);
		}catch(Exception e){
			DialogAlert("Read Address Error:" + e.getMessage());
		}
	}
	
	public void PopupSettingScreen(){
		m_settingScreen = new settingScreen(this);
		pushScreen(m_settingScreen);
	}
	
	public void PopupWeiboScreen(){
		if(m_weiboTimeLineScreen != null){
			pushScreen(m_weiboTimeLineScreen);
		}		
	}
	
	public void PopupDownloadFileDlg(final String _filename){
		if(m_downloadDlg == null){
			m_downloadDlgParent = UiApplication.getUiApplication();
			m_downloadDlg = new downloadDlg(this,m_downloadDlgParent, _filename);
			m_downloadDlgParent.pushScreen(m_downloadDlg);
		}
			
	}
	
	public void SetAboutInfo(String _about){
		m_aboutString = _about;
		
		// prompt by the background thread
		//
		invokeLater(new Runnable(){
			public void run(){
				if(m_aboutScreen != null){
					m_aboutScreen.RefreshText();
				}
			}
		});		
	}
	public void OpenAttachmentFileScreen(final boolean _del){
		
		try{

			m_uploadFileScreen = new uploadFileScreen(m_connectDeamon, this,_del);
			UiApplication.getUiApplication().pushScreen(m_uploadFileScreen);
			
		}catch(Exception _e){
			SetErrorString("att screen error:" + _e.getMessage());
		}
		
	}	
	
	public void ClearUploadingScreen(){
		m_uploadFileScreen = null;
	}
	
	public void PushViewFileScreen(final String _filename){
		
		invokeLater(new Runnable(){
			
		    public void run(){
		    	
		    	if(CheckMediaNativeApps(_filename)){
		    		return;
		    	}
		    	
		    	recvMain t_mainApp = (recvMain)UiApplication.getUiApplication();
		    	try{
		    		if(uploadFileScreen.IsAudioFile(_filename)){
		    			t_mainApp.pushGlobalScreen(new audioViewScreen(_filename,t_mainApp),0,UiEngine.GLOBAL_MODAL);
		    		}else if(uploadFileScreen.IsTxtFile(_filename)){
		    			t_mainApp.pushGlobalScreen(new textViewScreen(_filename,t_mainApp),0,UiEngine.GLOBAL_MODAL);
		    		}else if(uploadFileScreen.IsMovieFile(_filename)){
		    			t_mainApp.pushGlobalScreen(new videoViewScreen(_filename,t_mainApp),0,UiEngine.GLOBAL_MODAL);		    					    			
		    		}else if(uploadFileScreen.IsImageFile(_filename)){
		    			t_mainApp.pushGlobalScreen(new imageViewScreen(_filename,t_mainApp),0,UiEngine.GLOBAL_MODAL);			
		    		}else {
		    			t_mainApp.DialogAlert("yuchberry prompt:unknow format");		    					    			
		    		}
		    		
		    	}catch(Exception _e){
		    		t_mainApp.DialogAlert(_e.getMessage());
		    	}		    	
			}
		});
	}
	
	public boolean CheckMediaNativeApps(String _filename){
		
		try{
			Invocation request = new Invocation(_filename);
			Registry registry = Registry.getRegistry("com.yuchting.yuchberry.client.recvMain");
			registry.invoke(request);
			
			return true;
		}catch(Exception e){
			SetErrorString("Invoke native apps failed: "+ e.getMessage());
		}
		
		return false;		
	}
	
	public void UpdateMessageStatus(final Message m,final int _status){
		
		if(m == null){
			return;
		}
		
		if(m_messageApplication != null && m_messageApplication.isAlive()){
			
			m_messageApplication.invokeAndWait(new Runnable() {
				public void run(){
					m.setStatus(_status,0);
					m.updateUi();
					m_messageApplication.relayout();
				}
			});
		}else{
			invokeLater(new Runnable() {
				
				public void run() {
					m.setStatus(_status,0);
					m.updateUi();
				}
			});							
		}		
		
	}
	
	public void PopupDlgToOpenAttach(final connectDeamon.FetchAttachment _att){
				
		if(m_downloadDlg != null){
			m_downloadDlgParent.invokeLater(new Runnable() {
				public void run() {

					m_downloadDlgParent.popScreen(m_downloadDlg);
					m_downloadDlg = null;
					m_downloadDlgParent = null;
				}
			});			
		}
		
		// prompt by the background thread
		//
				
		Dialog t_dlg = new Dialog(Dialog.D_OK_CANCEL,_att.m_realName + sm_local.getString(localResource.DOWNLOAD_OVER_PROMPT),
	    							Dialog.OK,Bitmap.getPredefinedBitmap(Bitmap.EXCLAMATION),Manager.VERTICAL_SCROLL);
		
		t_dlg.setDialogClosedListener(new DialogClosedListener(){
			
			public void dialogClosed(Dialog dialog, int choice) {
				
				switch (choice) {
					case Dialog.OK:
						recvMain t_mainApp = (recvMain)UiApplication.getUiApplication();
						t_mainApp.PushViewFileScreen(t_mainApp.GetAttachmentDir() + _att.m_realName);
						break;
					
					default:
						break;
				}
			}
		});
		
		t_dlg.setEscapeEnabled(true);
		synchronized (getEventLock()) {
			pushGlobalScreen(t_dlg,1, UiEngine.GLOBAL_QUEUE);
		}		
	}
		
	public void SetStateString(String _state){
			
		m_stateString = _state;
		
		invokeLater(new Runnable() {
			public void run(){
				if(m_stateScreen != null){
					m_stateScreen.m_stateText.setText(GetStateString());
				}
			}
		});
	}
	
	public void DialogAlert(final String _msg){

		invokeLater(new Runnable() {
			public void run(){
				synchronized(getEventLock()){
					
					Dialog t_dlg = new Dialog(Dialog.D_OK,_msg,
							Dialog.OK,Bitmap.getPredefinedBitmap(Bitmap.EXCLAMATION),Manager.VERTICAL_SCROLL);
					
					t_dlg.setEscapeEnabled(true);			
					pushGlobalScreen(t_dlg,1, UiEngine.GLOBAL_QUEUE);
				};
			}
		});
		
		SetErrorString(_msg);		
    }
 
	public void SetUploadingDesc(final fetchMail _mail,final int _attachmentIdx,
								final int _uploadedSize,final int _totalSize){
						
		boolean t_found = false;
		for(int i = 0;i < m_uploadingDesc.size();i++){
			UploadingDesc t_desc = (UploadingDesc)m_uploadingDesc.elementAt(i);
			if(t_desc.m_mail == _mail){
				
				t_found = true;
				
				if(_attachmentIdx == -2){					
					m_uploadingDesc.removeElement(t_desc);
					
				}else{
					t_desc.m_attachmentIdx	= _attachmentIdx;
					t_desc.m_totalSize		= _totalSize;
					t_desc.m_uploadedSize	= _uploadedSize;
					
				}
				break;
			}
		}
		
		if(_attachmentIdx != -1 && !t_found){
			UploadingDesc t_desc = new UploadingDesc();
			
			t_desc.m_mail 			= _mail;
			t_desc.m_totalSize 		= _totalSize;
			t_desc.m_uploadedSize	= _uploadedSize;
			
			m_uploadingDesc.addElement(t_desc);
		}
		
		invokeLater(new Runnable() {
			public void run() {
				if(m_stateScreen != null){
					m_stateScreen.RefreshUploadState(m_uploadingDesc);
				}
			}
		});
		
	}
	
	public final Vector GetUploadingDesc(){
		return m_uploadingDesc;
	}

	public final String GetStateString(){
		return recvMain.sm_local.getString(localResource.STATE_PROMPT) + m_stateString;
	}
	
	public void LogOut(String _log){
		try{
			if(m_logfc == null){
				m_logfc = (FileConnection) Connector.open(uploadFileScreen.fsm_rootPath_back + "YuchBerry/log.txt",Connector.READ_WRITE);
				if(m_logfc.exists()){
					m_logfc.delete();
				}
				
				m_logfc.create();
				m_logfcOutput = m_logfc.openOutputStream();
			}
			
			m_logfcOutput.write(_log.getBytes());
			m_logfcOutput.write(("\n").getBytes());
			m_logfcOutput.flush();
			
		}catch(Exception e){
			SetErrorString("LogOut Error:"+e.getMessage() + e.getClass().getName());
		}
		
	}
	
	public synchronized void SetErrorString(final String _error){
		m_errorString.addElement(new ErrorInfo(_error));
		if(m_errorString.size() > 100){
			m_errorString.removeElementAt(0);
		}
		
		if(m_debugInfoScreen != null){
			m_debugInfoScreen.RefreshText();
		}			
	}
	
	public synchronized String GetAllErrorString(){
		if(!m_errorString.isEmpty()){

			SimpleDateFormat t_format = new SimpleDateFormat("HH:mm:ss");
			
			ErrorInfo t_info = (ErrorInfo)m_errorString.elementAt(0);
			
			StringBuffer t_text = new StringBuffer();
			
			for(int i = m_errorString.size() - 1;i >= 0;i--){				
				t_info = (ErrorInfo)m_errorString.elementAt(i);
				t_text.append(t_format.format(t_info.m_time)).append(":").append(t_info.m_info).append("\n");
			}
			
			return t_text.toString();
		}
		
		return "";
	}
	public void clearDebugMenu(){
		m_errorString.removeAllElements();
		
		if(m_debugInfoScreen != null){
			m_debugInfoScreen.RefreshText();
		}
	}
	public final Vector GetErrorString(){
		return m_errorString;
	}
		
	static public void openURL(String _url){
		BrowserSession browserSession = Browser.getDefaultSession();
		browserSession.displayPage(_url);
	}
	
	static public String GetByteStr(long _byte){
		if(_byte < 1000){
			return "" + _byte + "B";
		}else if(_byte >= 1000 && _byte < 1000000){
			return "" + (_byte / 1000) + "." + (_byte % 1000 / 100)+ "KB";
		}else{
			return "" + (_byte / (1000000)) + "." + ((_byte / 1000) % 1000 / 100) + "MB";
		}
	}
	
	static public int GetClientLanguage(){
		int t_code = Locale.getDefaultForSystem().getCode();
		
		switch(t_code){
			case Locale.LOCALE_zh:
			case Locale.LOCALE_zh_CN:
				t_code = 0;
				break;
			case Locale.LOCALE_zh_HK:
				t_code = 1;
				break;
			default:
				t_code = 2;
				break;
		}
		
		return t_code;
	}
	
	public static String md5(String _org){
		
		byte[] bytes = null;
		try{
			bytes = _org.getBytes("UTF-8");
		}catch(Exception e){
			bytes = _org.getBytes();
		}
		
		MD5Digest digest = new MD5Digest();
		
		digest.update(bytes, 0, bytes.length);

		byte[] md5 = new byte[digest.getDigestLength()];
		digest.getDigest(md5, 0, true);
		
		return convertToHex(md5);
		
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
	
	
	///////////////////////////////////////////////////////////////////////////////////////////
	///// weibo module
	///////////////////////////////////////////////////////////////////////////////////////////
	public weiboTimeLineScreen	m_weiboTimeLineScreen = null;
	public boolean				m_publicForward		= false;
	private Vector				m_receivedWeiboList	= new Vector();
	
	private int				m_receivedHomeWeiboNum = 0;
	private int				m_receivedAtMeWeiboNum = 0;
	private int				m_receivedCommentWeiboNum = 0;
	private int				m_receivedDirectMsgWeiboNum = 0;
	
	static final String[]	fsm_maxWeiboNumList = {"64","128","256","512","1024"};
	static final int[]	fsm_maxWeiboNum		= {64,128,256,512,1024};
	int						m_maxWeiboNumIndex = 0;
	
	public int					m_receivedWeiboNum = 0;
	public int					m_sentWeiboNum = 0;
	public boolean				m_hideHeader = false;
	public boolean				m_hasNewWeibo = false;
	
	boolean m_receiveWeiboListChanged = false;
	
	ApplicationMenuItem m_updateWeiboItem = null;
	
	public void InitWeiboModule(){
		
		if(m_enableWeiboModule){
			
			if(m_weiboTimeLineScreen == null){
				m_weiboTimeLineScreen = new weiboTimeLineScreen(this);
				
				m_updateWeiboItem = new ApplicationMenuItem(30) {
					
					public String toString() {
						return recvMain.sm_local.getString(localResource.WEIBO_UPDATE_DLG);
					}
					
					public Object run(Object context) {
						m_weiboTimeLineScreen.m_updateItem.run();
						return m_weiboTimeLineScreen.m_currUpdateDlg;
					}
				};
				
				ApplicationMenuItemRepository.getInstance()
					.addMenuItem(ApplicationMenuItemRepository.MENUITEM_MESSAGE_LIST,m_updateWeiboItem);
			}
			
									
			ReadWriteWeiboFile(true);
			
			m_weiboTimeLineScreen.ClearWeibo();
			
			m_receivedHomeWeiboNum = 0;
			m_receivedAtMeWeiboNum = 0;
			m_receivedCommentWeiboNum = 0;
			m_receivedDirectMsgWeiboNum = 0;
					
			if(!m_receivedWeiboList.isEmpty()){
				
				synchronized (m_receivedWeiboList) {
					for(int i = 0 ;i < m_receivedWeiboList.size();i++){
						fetchWeibo weibo = (fetchWeibo)m_receivedWeiboList.elementAt(i);
						switch(weibo.GetWeiboClass()){
						case fetchWeibo.TIMELINE_CLASS:
							m_receivedHomeWeiboNum++;
							break;
						case fetchWeibo.AT_ME_CLASS:
							m_receivedAtMeWeiboNum++;
							break;
						case fetchWeibo.COMMENT_ME_CLASS:
							m_receivedCommentWeiboNum++;
							break;
						case fetchWeibo.DIRECT_MESSAGE_CLASS:
							m_receivedDirectMsgWeiboNum++;
							break;
						}
					}
					
					for(int i = 0;i < m_receivedWeiboList.size();i++){
						fetchWeibo t_weibo = (fetchWeibo)m_receivedWeiboList.elementAt(i);
						m_weiboTimeLineScreen.AddWeibo(t_weibo,true);
					}
				}
			}
			
		}
	}
	
	public void DisableWeiboModule(){
		if(m_updateWeiboItem != null){
			ApplicationMenuItemRepository.getInstance()
				.removeMenuItem(ApplicationMenuItemRepository.MENUITEM_MESSAGE_LIST,m_updateWeiboItem);
			m_updateWeiboItem = null;
		}
	}
		
	public void PrepareWeiboItem(fetchWeibo _weibo){
		
		if(!isForeground()){
			m_hasNewWeibo = true;
			
			if(m_connectDeamon.isDisconnectState()){
				HomeScreen.updateIcon(Bitmap.getBitmapResource("Main_offline_new.png"));
			}else{
				HomeScreen.updateIcon(Bitmap.getBitmapResource("Main_new.png"));
			}
		}
		
		synchronized (this) {
			m_receiveWeiboListChanged = true;
		}
		
		m_receivedWeiboNum++;
		
		int t_checkWeiboClassNum = 0;
		
		switch(_weibo.GetWeiboClass()){
		case fetchWeibo.TIMELINE_CLASS:
			t_checkWeiboClassNum = ++m_receivedHomeWeiboNum;
			break;
		case fetchWeibo.AT_ME_CLASS:
			t_checkWeiboClassNum = ++m_receivedAtMeWeiboNum;
			break;
		case fetchWeibo.COMMENT_ME_CLASS:
			t_checkWeiboClassNum = ++m_receivedCommentWeiboNum;
			break;
		case fetchWeibo.DIRECT_MESSAGE_CLASS:
			t_checkWeiboClassNum = ++m_receivedDirectMsgWeiboNum;
			break;
		}
		
		synchronized (m_receivedWeiboList) {
			
			for(int i = m_receivedWeiboList.size() - 1 ;i >= 0 ;i--){
				fetchWeibo weibo = (fetchWeibo)m_receivedWeiboList.elementAt(i);
				if(weibo.equals(_weibo)){
					return;
				}
			}			
						
			if(t_checkWeiboClassNum >= getMaxWeiboNum()){
				
				for(int i = 0; i < m_receivedWeiboList.size();i++){
					
					fetchWeibo t_delWeibo = (fetchWeibo)m_receivedWeiboList.elementAt(i);
					
					if(t_delWeibo.GetWeiboClass() == _weibo.GetWeiboClass()){
						
						m_receivedWeiboList.removeElementAt(i);
						
						m_weiboTimeLineScreen.DelWeibo(t_delWeibo);
												
						break;
					}
				}
				
				switch(_weibo.GetWeiboClass()){
				case fetchWeibo.TIMELINE_CLASS:
					--m_receivedHomeWeiboNum;
					break;
				case fetchWeibo.AT_ME_CLASS:
					--m_receivedAtMeWeiboNum;
					break;
				case fetchWeibo.COMMENT_ME_CLASS:
					--m_receivedCommentWeiboNum;
					break;
				case fetchWeibo.DIRECT_MESSAGE_CLASS:
					--m_receivedDirectMsgWeiboNum;
					break;
				}
			}
			
			m_receivedWeiboList.addElement(_weibo);
			
		}		
		
		if(m_weiboTimeLineScreen.AddWeibo(_weibo,false)){
			TriggerWeiboNotification();
		}
		
	}
	
	public void ChangeWeiboHeadImageHash(String _userId,int _weiboStyle,int _headImageHash){
		
		synchronized(m_receivedWeiboList) {

			for(int i = 0 ;i < m_receivedWeiboList.size();i++){
				fetchWeibo weibo = (fetchWeibo)m_receivedWeiboList.elementAt(i);
				
				if(weibo.GetWeiboStyle() == _weiboStyle 
				&& weibo.GetHeadImageId().equals(_userId) ){
					
					weibo.SetUserHeadImageHashCode(_headImageHash);
				}
			}
		}
	}
	
	static final String fsm_weiboDataName = "weibo.data";
	static final String fsm_weiboDataBackName = "~weibo.data";
	
	private synchronized void ReadWriteWeiboFile(boolean _read){
		
		if(!m_receiveWeiboListChanged && !_read){
			return ;
		}
		
		m_receiveWeiboListChanged = false;
		
		String t_weiboDataDir = uploadFileScreen.fsm_rootPath_back + "YuchBerry/";
		
		String t_weiboDataPathName 		= t_weiboDataDir + fsm_weiboDataName;
		String t_weiboDataBackPathName	= t_weiboDataDir + fsm_weiboDataBackName;
		
		PreWriteReadIni(_read, t_weiboDataBackPathName,t_weiboDataPathName,
							fsm_weiboDataBackName, fsm_weiboDataName);
		
		try{
			FileConnection t_fc = (FileConnection)Connector.open(t_weiboDataPathName);
			try{

				if(_read){
					synchronized (m_receivedWeiboList) {
						m_receivedWeiboList.removeAllElements();
						
						if(t_fc.exists()){
							
							InputStream t_readIn = t_fc.openInputStream();
							try{
								
								int t_num = sendReceive.ReadInt(t_readIn);
								for(int i = 0 ;i < t_num;i++){
									fetchWeibo t_weibo = new fetchWeibo();
									t_weibo.InputWeibo(t_readIn);
									
									m_receivedWeiboList.addElement(t_weibo);
								}
								
							}finally{
								t_readIn.close();
								t_readIn = null;
							}
						}
					}
					
					
				}else{
					
					if(!t_fc.exists()){
						t_fc.create();
					}
					
					OutputStream t_fileos = t_fc.openOutputStream();
					try{

						ByteArrayOutputStream tmpos = new ByteArrayOutputStream();
												
						synchronized (m_receivedWeiboList) {
							sendReceive.WriteInt(tmpos,m_receivedWeiboList.size());
							
							for(int i = 0 ;i < m_receivedWeiboList.size();i++){
								fetchWeibo t_weibo = (fetchWeibo)m_receivedWeiboList.elementAt(i);
								t_weibo.OutputWeibo(tmpos);
							}
						}
						
						t_fileos.write(tmpos.toByteArray());
												
					}finally{
						t_fileos.flush();
						t_fileos.close();
						t_fileos = null;
					}
					
					// delete the back file ~weibo.data
					//
					FileConnection t_backFile = (FileConnection) Connector.open(t_weiboDataBackPathName,Connector.READ_WRITE);
					try{
						if(t_backFile.exists()){
							t_backFile.delete();
						}
					}finally{
						t_backFile.close();
						t_backFile = null;
					}
					
				}
				
			}finally{
				t_fc.close();
				t_fc = null;
			}
		}catch(Exception e){
			SetErrorString("RWWF:"+e.getMessage()+e.getClass().getName());
		}
	}
}

