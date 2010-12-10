package com.yuchting.yuchberry.client;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import local.localResource;
import net.rim.blackberry.api.mail.Message;
import net.rim.blackberry.api.menuitem.ApplicationMenuItem;
import net.rim.blackberry.api.menuitem.ApplicationMenuItemRepository;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.i18n.SimpleDateFormat;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.FontFamily;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.Ui;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.UiEngine;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.DialogClosedListener;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.container.MainScreen;

class ErrorLabelText extends Field{
	Vector m_stringList;
	static final int		fsm_space = 1;
	
	static int sm_fontHeight = 15;
	
	public ErrorLabelText(Vector _stringList){
		super(Field.READONLY | Field.NON_FOCUSABLE | Field.USE_ALL_WIDTH);
		
		m_stringList = _stringList;
		try{
			Font myFont = FontFamily.forName("BBMillbankTall").getFont(Font.PLAIN,8,Ui.UNITS_pt);
			setFont(myFont);
			
			sm_fontHeight = myFont.getHeight() - 3;
		}catch(Exception _e){}
	}
	
	public void layout(int _width,int _height){
		final int t_width = Display.getWidth();
			
		final int t_size 	= m_stringList.size();
		final int t_height = Math.max(0, (t_size - 1)) * fsm_space +  t_size * sm_fontHeight;
		
		setExtent(t_width, t_height);
	}
	
	public void paint(Graphics _g){
		int t_y = 0;
		final int t_fontHeight = sm_fontHeight;
		
		SimpleDateFormat t_format = new SimpleDateFormat("HH:mm:ss");
		
		for(int i = m_stringList.size() -1 ;i >= 0 ;i--){
			recvMain.ErrorInfo t_info = (recvMain.ErrorInfo)m_stringList.elementAt(i);
			_g.drawText(t_format.format(t_info.m_time) + ": " + t_info.m_info,0,t_y,Graphics.ELLIPSIS);
			
			t_y += t_fontHeight + fsm_space;
		}
	}
	
	public boolean isFocusable(){
		return false;
	}
}

final class stateScreen extends MainScreen implements FieldChangeListener{
										
        
    EditField           m_hostName      = null;
    EditField			m_hostport		= null;
    EditField           m_userPassword  = null;
       
    ButtonField         m_connectBut    = null;
    LabelField          m_stateText     = null;
    ErrorLabelText      m_errorText     = null;
    LabelField			m_uploadingText = null;
            
    recvMain			m_mainApp		= null;
    
    
    MenuItem 	m_aboutMenu = new MenuItem(recvMain.sm_local.getString(localResource.ABOUT_MENU_TEXT), 100, 10) {
												public void run() {
													recvMain t_app = (recvMain)UiApplication.getUiApplication();
													t_app.PopupAboutScreen();
												}
											};
	
	MenuItem 	m_setingMenu = new MenuItem(recvMain.sm_local.getString(localResource.SETTING_MENU_TEXT), 101, 10) {
												public void run() {
													recvMain t_app = (recvMain)UiApplication.getUiApplication();
													t_app.pushScreen(new settingScreen(t_app));
													
												}
											};
	

    public stateScreen(final recvMain _app) {
    	        
        super();
        
        m_mainApp	= _app;        
        
        m_hostName = new EditField(recvMain.sm_local.getString(localResource.HOST),
        				m_mainApp.m_hostname,128, EditField.FILTER_DEFAULT);
        
        m_hostName.setChangeListener(this);
        add(m_hostName);
        
        m_hostport = new EditField(recvMain.sm_local.getString(localResource.PORT),
        				m_mainApp.m_port == 0?"":Integer.toString(m_mainApp.m_port),5,EditField.FILTER_INTEGER);
        
        m_hostport.setChangeListener(this);
        add(m_hostport);
        
        m_userPassword = new EditField(recvMain.sm_local.getString(localResource.USER_PASSWORD),
        				m_mainApp.m_userPassword,128,EditField.FILTER_DEFAULT);
        
        add(m_userPassword);
        
       
        m_connectBut = new ButtonField(recvMain.sm_local.getString(m_mainApp.m_connectDeamon.IsConnectState()?
        									localResource.DISCONNECT_BUTTON_LABEL:localResource.CONNECT_BUTTON_LABEL),
        									ButtonField.CONSUME_CLICK| ButtonField.NEVER_DIRTY);
        
        m_connectBut.setChangeListener(this);
        
        add(m_connectBut);              
        
        m_stateText = new LabelField(m_mainApp.GetStateString(), LabelField.ELLIPSIS | LabelField.USE_ALL_WIDTH);
        add(m_stateText);
        
        m_uploadingText = new LabelField("", LabelField.ELLIPSIS | LabelField.USE_ALL_WIDTH);
        add(m_uploadingText);
        
        m_errorText = new ErrorLabelText(m_mainApp.GetErrorString());
        add(m_errorText);       
        
        RefreshUploadState(_app.m_uploadingDesc);
               
    }
    
    protected void makeMenu(Menu _menu,int instance){
    	_menu.add(m_aboutMenu);
    	_menu.add(m_setingMenu);
    }
    
    public final boolean onClose(){
    	if(m_mainApp.m_connectDeamon.IsConnectState()){
    		m_mainApp.requestBackground();
    		return false;
    	}
    	
    	m_mainApp.Exit();
    	
    	return true;
    }
        
   
    
    public void RefreshUploadState(final Vector _uploading){
    	String t_total = new String();
    	
    	for(int i = 0;i < _uploading.size();i++){
    		
    		recvMain.UploadingDesc t_desc = (recvMain.UploadingDesc)_uploading.elementAt(i);
    		
    		if(t_desc.m_attachmentIdx == -1){
    			
    			t_total = t_total + "Subject: " + t_desc.m_mail.GetSubject() + "(Failed) retry again\n";
    			
    		}else{
    			
    			final int t_tmp = (int)((float)t_desc.m_uploadedSize / (float)t_desc.m_totalSize * 1000);
    			final float t_percent = (float)t_tmp / 10;
        		
        		t_total = t_total + t_desc.m_mail.GetSubject() + "(" +
        				t_desc.m_attachmentIdx + "/" + t_desc.m_mail.GetAttachment().size() + " " + t_percent + "%) \n";
    		}   		
    	}
    	
    	m_uploadingText.setText(t_total);    	
    }
        
    public void fieldChanged(Field field, int context) {
        if(context != FieldChangeListener.PROGRAMMATIC){
			// Perform action if user changed field. 
			//
			if(field == m_connectBut){
				
				if(m_hostName.getText().length() == 0 
					|| m_userPassword.getText().length() == 0
					|| m_hostport.getText().length() == 0){
					
					Dialog.alert(recvMain.sm_local.getString(localResource.INPUT_FULL_SIGN_IN_SEG));
					
					return;
				}				
												
				if(m_mainApp.m_connectDeamon.IsConnectState()){
					
					try{
						m_mainApp.m_connectDeamon.Disconnect();
					}catch(Exception _e){}
					
					m_connectBut.setLabel(recvMain.sm_local.getString(localResource.CONNECT_BUTTON_LABEL));
					m_mainApp.SetStateString(recvMain.sm_local.getString(localResource.DISCONNECT_BUTTON_LABEL));
					
				}else{
										
					
					try{
						m_mainApp.m_hostname 		= m_hostName.getText();
						m_mainApp.m_port 			= Integer.valueOf(m_hostport.getText()).intValue();
						m_mainApp.m_userPassword 	= m_userPassword.getText();

						m_mainApp.m_connectDeamon.Connect();
						
						m_mainApp.SetStateString(recvMain.sm_local.getString(localResource.CONNECTING_LABEL));
						m_connectBut.setLabel(recvMain.sm_local.getString(localResource.DISCONNECT_BUTTON_LABEL));
						
						m_mainApp.Start();
						
					}catch(Exception e){
						m_mainApp.DialogAlert(e.getMessage());
					}
				}				
			}
        }else{
        	// Perform action if application changed field.
        }
    }
    
}

public class recvMain extends UiApplication implements localResource {
	
	final static int		fsm_clientVersion = 4;
	
	String 				m_attachmentDir 	= null;
	
    aboutScreen			m_aboutScreen		= null;
	stateScreen 		m_stateScreen 		= null;
	uploadFileScreen 	m_uploadFileScreen	= null;
	connectDeamon 		m_connectDeamon		= new connectDeamon(this);
	
	String				m_stateString		= recvMain.sm_local.getString(localResource.DISCONNECT_BUTTON_LABEL);
	String				m_aboutString		= recvMain.sm_local.getString(localResource.ABOUT_DESC);
	
	class ErrorInfo{
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
	
	int					m_vibrateTime		= 1;
	int					m_soundVol			= 2;
	
	class APNSelector{
		String		m_name			= null;
		int			m_validateNum	= 0;
	}
	
	Vector				m_APNList 			= new Vector();
	int					m_currentAPNIdx 	= 0;
	int					m_changeAPNCounter 	= 0;
	String				m_appendString		= new String();
	
	class UploadingDesc{
		
		fetchMail		m_mail = null;
		int				m_attachmentIdx;
		int				m_uploadedSize;
		int				m_totalSize;		
	}
	
	public class AddAattachmentItem extends ApplicationMenuItem{
		
		recvMain		m_mainApp = null;
		
		AddAattachmentItem(){
			super(20);
		}
				
		public String toString(){
			return recvMain.sm_local.getString(localResource.ADD_ATTACHMENT);
		}
		
		public Object run(Object context){
			if(context instanceof Message ){
				m_mainApp.OpenAttachmentFileScreen(false);
				return m_mainApp.m_uploadFileScreen;
			}
			
			return context;
		}
	}
	
	public class DelAattachmentItem extends ApplicationMenuItem{
		
		recvMain		m_mainApp = null;
		
		DelAattachmentItem(){
			super(30);
		}
				
		public String toString(){
			return recvMain.sm_local.getString(localResource.CHECK_DEL_ATTACHMENT);
		}
		
		public Object run(Object context){
			if(context instanceof Message ){

				m_mainApp.OpenAttachmentFileScreen(true);
				return m_mainApp.m_uploadFileScreen;
			}
			
			return context;	
		}
	}
	
	AddAattachmentItem 	m_addItem	= new AddAattachmentItem();
	DelAattachmentItem	m_delItem	= new DelAattachmentItem();
	
	static ResourceBundle sm_local = ResourceBundle.getBundle(
								localResource.BUNDLE_ID, localResource.BUNDLE_NAME);
	
	public static void main(String[] args) {
		recvMain t_theApp = new recvMain();		
		t_theApp.enterEventDispatcher();
	}
   
	public recvMain() {	
				
		m_addItem.m_mainApp = this;
		m_delItem.m_mainApp = this;
		
		try{
			FileConnection fc = (FileConnection) Connector.open(uploadFileScreen.fsm_rootPath_default + "YuchBerry/",Connector.READ_WRITE);
			m_attachmentDir = uploadFileScreen.fsm_rootPath_default + "YuchBerry/";
			
		}catch(Exception _e){
			m_attachmentDir = uploadFileScreen.fsm_rootPath_back + "YuchBerry/";
		}
		
		// create the sdcard path 
		//
        try{
        	FileConnection fc = (FileConnection) Connector.open(m_attachmentDir,Connector.READ_WRITE);
        	if(!fc.exists()){
        		fc.mkdir();
        	}
        }catch(Exception _e){
        	
        	Dialog.alert("can't use the SDCard to store attachment!");
        	System.exit(0);
        }
        
        WriteReadIni(true);
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
		 
		return t_result;
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
	
	public void WriteReadIni(boolean _read){
		try{
			FileConnection fc = (FileConnection) Connector.open(m_attachmentDir + "Init.data",Connector.READ_WRITE);
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
		    		
		    		if(t_currVer >= 3){
		    			m_vibrateTime 	= t_readFile.read();
		    			m_soundVol		= t_readFile.read();
		    		}
		    		
		    		if(t_currVer >= 4){
		    			m_useWifi = (t_readFile.read() == 0)?false:true;
		    			m_appendString = sendReceive.ReadString(t_readFile);		    			
		    		}
		    		
		    		t_readFile.close();
		    		fc.close();
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
				t_writeFile.write(m_vibrateTime);
				t_writeFile.write(m_soundVol);
				t_writeFile.write(m_useWifi?1:0);
				
				sendReceive.WriteString(t_writeFile,m_appendString);
				
				t_writeFile.close();
				
				fc.close();
			}
			
		}catch(Exception _e){
			SetErrorString("write/read config file from SDCard error :" + _e.getMessage() + _e.getClass().getName());
		}
	}
	
	
	public void Start(){
        
		ApplicationMenuItemRepository.getInstance().addMenuItem(ApplicationMenuItemRepository.MENUITEM_EMAIL_EDIT,m_addItem);
		ApplicationMenuItemRepository.getInstance().addMenuItem(ApplicationMenuItemRepository.MENUITEM_EMAIL_EDIT ,m_delItem);
		
		WriteReadIni(false);
	}
	
	public void Exit(){
		
		ApplicationMenuItemRepository.getInstance().removeMenuItem(ApplicationMenuItemRepository.MENUITEM_EMAIL_EDIT, m_addItem);
		ApplicationMenuItemRepository.getInstance().removeMenuItem(ApplicationMenuItemRepository.MENUITEM_EMAIL_EDIT ,m_delItem);	
				
		System.exit(0);
	}
	public void activate(){
		if(m_stateScreen == null){
			m_stateScreen = new stateScreen(this);
			pushScreen(m_stateScreen);
		}		
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
		
		m_connectDeamon.SendAboutInfoQuery();
	}
	
	public void SetAboutInfo(String _about){
		m_aboutString = _about;
		
		// prompt by the background thread
		//
		synchronized(getEventLock()){
			
			invokeLater(new Runnable(){
				public void run(){
					if(m_aboutScreen != null){
						m_aboutScreen.RefreshText();
					}
				}
			});
			
		}
		
	}
	public void OpenAttachmentFileScreen(final boolean _del){
		
		try{

			m_uploadFileScreen = new uploadFileScreen(m_connectDeamon, this,_del);
			
			invokeLater(new Runnable()
			{
			    public void run()
				{
			    	recvMain t_mainApp = (recvMain)UiApplication.getUiApplication();
			    	t_mainApp.PushUploadingScreen();
				}
			});	
		}catch(Exception _e){
			DialogAlert("construct attachment file screen error: " + _e.getMessage());
		}
		
	}
	
	public void PushUploadingScreen(){
		pushGlobalScreen(m_uploadFileScreen,0,UiEngine.GLOBAL_MODAL);
	}
	public void ClearUploadingScreen(){
		m_uploadFileScreen = null;
	}
	
	public void PushViewFileScreen(final String _filename){
		
		invokeLater(new Runnable(){
			
		    public void run(){
		    	
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
		    			t_mainApp.DialogAlert("unknow format");
		    		}
		    		
		    	}catch(Exception _e){
		    		t_mainApp.DialogAlert(_e.getMessage());
		    	}		    	
			}
		});
	}
	
	public void UpdateMessageStatus(final Message m,final int _status){
		
		invokeLater(new Runnable(){
			
			public void run(){
				m.setStatus(_status,0);
				m.updateUi();
				UiApplication.getUiApplication().relayout();
			}
		});
	}
	
	public void PopupDlgToOpenAttach(final connectDeamon.FetchAttachment _att){
				
		// prompt by the background thread
		//
		synchronized(getEventLock()){
			
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
			UiApplication.getUiApplication().pushGlobalScreen(t_dlg,1, UiEngine.GLOBAL_QUEUE);
		}
		
	}
	
	public void SetStateString(String _state){
			
		m_stateString = _state;
		
		if(m_stateScreen != null){
			m_stateScreen.m_stateText.setText(GetStateString());
		}
	}
	
	public void DialogAlert(final String _msg){

    	UiApplication.getUiApplication().invokeLater(new Runnable() 
		{
		    public void run(){
		       Dialog.alert(_msg);
		    }
		});
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
		
		if(m_stateScreen != null){
			m_stateScreen.RefreshUploadState(m_uploadingDesc);
		}
	}
	
	public final Vector GetUploadingDesc(){
		return m_uploadingDesc;
	}

	public final String GetStateString(){
		return recvMain.sm_local.getString(localResource.STATE_PROMPT) + m_stateString;
	}
	
	public void SetErrorString(final String _error){
		m_errorString.addElement(new ErrorInfo(_error));
		if(m_errorString.size() > 16){
			m_errorString.removeElementAt(0);
		}
		
		if(m_stateScreen != null){
			m_stateScreen.m_errorText.layout(0, 0);
			m_stateScreen.invalidate();
		}
	}
	
	public final Vector GetErrorString(){
		return m_errorString;
	}	
}

