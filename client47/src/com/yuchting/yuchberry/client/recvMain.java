package com.yuchting.yuchberry.client;

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
import net.rim.blackberry.api.mail.Message;
import net.rim.blackberry.api.menuitem.ApplicationMenuItem;
import net.rim.blackberry.api.menuitem.ApplicationMenuItemRepository;
import net.rim.device.api.i18n.Locale;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.i18n.SimpleDateFormat;
import net.rim.device.api.notification.NotificationsConstants;
import net.rim.device.api.notification.NotificationsManager;
import net.rim.device.api.system.ApplicationManager;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.UiEngine;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.DialogClosedListener;

public class recvMain extends UiApplication implements localResource,LocationListener {
	
	final static int 		fsm_display_width		= Display.getWidth();
	final static int 		fsm_display_height		= Display.getHeight();
	
	final static int		fsm_clientVersion = 9;
	
	final static long		fsm_notifyID_email = 767918509114947L;
	
	final static Object 	fsm_notifyEvent_email = new Object() {
	    public String toString() {
	       return recvMain.sm_local.getString(localResource.NOTIFY_EMAIL_LABEL);
	    }
	};
	
	String 				m_attachmentDir 	= null;
	String 				m_weiboHeadImageDir = null;
	
    aboutScreen			m_aboutScreen		= null;
	stateScreen 		m_stateScreen 		= null;
	uploadFileScreen 	m_uploadFileScreen	= null;
	debugInfo			m_debugInfoScreen	= null;
	downloadDlg			m_downloadDlg		= null;
	settingScreen		m_settingScreen		= null;
	weiboTimeLineScreen	m_weiboTimeLineScreen = new weiboTimeLineScreen(this);
	UiApplication		m_downloadDlgParent = null;
	
	UiApplication		m_messageApplication = null;
	
	connectDeamon 		m_connectDeamon		= new connectDeamon(this);
	
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
		
	boolean			m_autoRun			= false;
	
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
	
	static final String[]	fsm_pulseIntervalString = {"1","5","10","30"};
	static final int[]	fsm_pulseInterval		= {1,5,10,30};
	int						m_pulseIntervalIndex = 1;
	
	boolean			m_fulldayPrompt		= true;
	int					m_startPromptHour	= 8;
	int					m_endPromptHour		= 22;
	
	final class UploadingDesc{
		
		fetchMail		m_mail = null;
		int				m_attachmentIdx;
		int				m_uploadedSize;
		int				m_totalSize;		
	}
	
	public class AddAattachmentItem extends ApplicationMenuItem{
			
		AddAattachmentItem(){
			super(20);
		}
				
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
	}
	
	public class DelAattachmentItem extends ApplicationMenuItem{
		
		DelAattachmentItem(){
			super(30);
		}
				
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
	}
	
	AddAattachmentItem 	m_addItem	= new AddAattachmentItem();
	DelAattachmentItem	m_delItem	= new DelAattachmentItem();
	
	static ResourceBundle sm_local = ResourceBundle.getBundle(
								localResource.BUNDLE_ID, localResource.BUNDLE_NAME);
	
	//@{ location information
	LocationProvider m_locationProvider = null;
	boolean		 m_useLocationInfo = false;
	boolean		 m_setLocationListener = false;
	
	double			 m_longitude		= 0;
	double			 m_latitude			= 0;
	float			 m_altitude			= 0;
	float			 m_movingSpeed		= 0;
	float			 m_locationHeading = 0;
	//@}
	
	public static void main(String[] args) {
		recvMain t_theApp = new recvMain(ApplicationManager.getApplicationManager().inStartup());		
		t_theApp.enterEventDispatcher();
	}
	
   
	public recvMain(boolean _systemRun) {	
		
		boolean t_SDCardUse = false;
		
		try{
			FileConnection fc = (FileConnection) Connector.open(uploadFileScreen.fsm_rootPath_back + "YuchBerry/",Connector.READ_WRITE);
			if(!fc.exists()){
				fc.mkdir();
			}
			fc.close();
		}catch (Exception _e) {
			System.exit(0);
		}
		
		try{
			FileConnection fc = (FileConnection) Connector.open(uploadFileScreen.fsm_rootPath_default + "YuchBerry/",Connector.READ_WRITE);
			if(!fc.exists()){
				fc.mkdir();
			}
			fc.close();
			t_SDCardUse = true;
		}catch(Exception e){
			
		}
				
		m_attachmentDir = (t_SDCardUse?uploadFileScreen.fsm_rootPath_default:uploadFileScreen.fsm_rootPath_back) + "YuchBerry/AttDir/";
		
		m_weiboHeadImageDir = uploadFileScreen.fsm_rootPath_back + "YuchBerry/WeiboImage/";
		
		// create the sdcard path 
		//
        try{
        	
        	FileConnection fc = (FileConnection) Connector.open(m_attachmentDir,Connector.READ_WRITE);
        	if(!fc.exists()){
        		fc.mkdir();
        	}
        	fc.close();
        	        	
        	fc = (FileConnection) Connector.open(m_weiboHeadImageDir,Connector.READ_WRITE);
        	if(!fc.exists()){
        		fc.mkdir();
        	}
        	fc.close();
        	
        }catch(Exception _e){
        	
        	Dialog.alert("can't use the SDCard to store attachment!");
        	System.exit(0);
        }
        
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
		
		
        WriteReadIni(true);
        
        if(_systemRun){
        	
        	// register the notification
        	//
        	NotificationsManager.registerSource(fsm_notifyID_email, fsm_notifyEvent_email,NotificationsConstants.CASUAL);
        	
        	if(!m_autoRun || m_hostname.length() == 0 || m_port == 0 || m_userPassword.length() == 0){
        		System.exit(0);
        	}else{
        		try{
        			m_connectDeamon.Connect(true);
        			Start();
        		}catch(Exception e){
        			System.exit(0);
        		}   		
        		
        	}      	
        }        
	}
	
	public String GetWeiboHeadImageDir(){
		return m_weiboHeadImageDir;
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

		String t_result = new String();
		
		String t_APN = GetAPNName();
				
		if(m_useWifi){
			t_result = ";interface=wifi";
		}else{
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
	
	public int GetPulseIntervalMinutes(){
		return fsm_pulseInterval[m_pulseIntervalIndex];
	}
	
	public synchronized void WriteReadIni(boolean _read){
		
		try{
			
			FileConnection fc = (FileConnection) Connector.open(uploadFileScreen.fsm_rootPath_back + "YuchBerry/" + "Init.data",
																Connector.READ_WRITE);
			if(_read){

		    	if(fc.exists()){
		    		InputStream t_readFile = fc.openInputStream();
		    		
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
		    			m_fulldayPrompt = t_readFile.read() == 1?true:false;
		    			m_startPromptHour = sendReceive.ReadInt(t_readFile);
	    				m_endPromptHour = sendReceive.ReadInt(t_readFile);		    			
		    		}	
		    		
		    		if(t_currVer >= 8){
		    			m_useLocationInfo = t_readFile.read() == 1?true:false;
		    		}
		    		
		    		t_readFile.close();
		    		
		    	}
			}else{
				if(!fc.exists()){
					fc.create();
				}				
				
				OutputStream t_writeFile = fc.openOutputStream();
				
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
				
				t_writeFile.write(m_useSSL?1:0);
				t_writeFile.write(m_useWifi?1:0);
				sendReceive.WriteString(t_writeFile,m_appendString);
				
				t_writeFile.write(m_autoRun?1:0);
				
				sendReceive.WriteLong(t_writeFile,m_uploadByte);
				sendReceive.WriteLong(t_writeFile, m_downloadByte);
				
				sendReceive.WriteInt(t_writeFile,m_pulseIntervalIndex);
				
				
				t_writeFile.write(m_fulldayPrompt?1:0);
				sendReceive.WriteInt(t_writeFile,m_startPromptHour);
				sendReceive.WriteInt(t_writeFile,m_endPromptHour);
				
				t_writeFile.write(m_useLocationInfo?1:0);
								
				t_writeFile.close();
				
				if(m_connectDeamon.m_connect != null){
					m_connectDeamon.m_connect.SetKeepliveInterval(GetPulseIntervalMinutes());
				}
				
			}
			
			fc.close();
			
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
			m_locationHeading 	= location.getCourse();
			m_longitude 		= location.getQualifiedCoordinates().getLongitude();
			m_latitude 			= location.getQualifiedCoordinates().getLatitude();
			m_altitude 			= location.getQualifiedCoordinates().getAltitude();
			m_movingSpeed 		= location.getSpeed();
			
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
		ApplicationMenuItemRepository.getInstance().addMenuItem(ApplicationMenuItemRepository.MENUITEM_EMAIL_EDIT ,m_delItem);
				
		WriteReadIni(false);
	}
	
	public void Exit(){
		
		ApplicationMenuItemRepository.getInstance().removeMenuItem(ApplicationMenuItemRepository.MENUITEM_EMAIL_EDIT, m_addItem);
		ApplicationMenuItemRepository.getInstance().removeMenuItem(ApplicationMenuItemRepository.MENUITEM_EMAIL_EDIT ,m_delItem);	
		
		StopNotification();
		
		if(m_connectDeamon.m_connect != null){
			m_connectDeamon.m_connect.StoreUpDownloadByteImm(true);
		}
		
		if(m_locationProvider != null){
			if(m_useLocationInfo){
				m_locationProvider.reset();
				m_locationProvider.setLocationListener(null, -1, -1, -1);
			}
		}	
		
		System.exit(0);
	}
	
	public void activate(){
		if(m_stateScreen == null){
			m_stateScreen = new stateScreen(this);
			pushScreen(m_stateScreen);
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
	
	public void deactivate(){
		if(m_stateScreen != null){
			popScreen(m_stateScreen);
			m_stateScreen = null;
		}		
	}
	
	public void PopupAboutScreen(){
		m_aboutScreen = new aboutScreen(this);
		pushScreen(m_aboutScreen);
	}
	
	public void PopupSettingScreen(){
		m_settingScreen = new settingScreen(this);
		pushScreen(m_settingScreen);
	}
	
	public void PopupWeiboScreen(){
		pushScreen(m_weiboTimeLineScreen);
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
			Registry registry = Registry.getRegistry("net.rim.device.api.content.BlackBerryContentHandler");
			registry.invoke(request);
			
			return true;
		}catch(Exception e){
			SetErrorString("Invoke native apps failed:"+ e.getMessage());
		}
		
		return false;		
	}
	
	public void UpdateMessageStatus(final Message m,final int _status){
		
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
						t_mainApp.PushViewFileScreen(t_mainApp.m_attachmentDir + _att.m_realName);
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
	
	public void SetErrorString(final String _error){
		m_errorString.addElement(new ErrorInfo(_error));
		if(m_errorString.size() > 100){
			m_errorString.removeElementAt(0);
		}
		
		if(m_debugInfoScreen != null){
			m_debugInfoScreen.RefreshText();
		}			
	}
	
	public final Vector GetErrorString(){
		return m_errorString;
	}
	
	static public String GetByteStr(long _byte){
		if(_byte < 1000){
			return "" + _byte + "B";
		}else if(_byte >= 1000 && _byte < 1000000){
			return "" + (_byte / 1000) + "." + (_byte % 1000)+ "KB";
		}else{
			return "" + (_byte / (1000000)) + "." + ((_byte / 1000) % 1000) + "MB";
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
	
}

