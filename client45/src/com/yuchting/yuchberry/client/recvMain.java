package com.yuchting.yuchberry.client;

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
import net.rim.device.api.ui.Ui;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.UiEngine;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.DialogClosedListener;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.MainScreen;

class ErrorLabelText extends Field{
	Vector m_stringList;
	static final int		fsm_space = 1;
	static final int		fsm_fontHeight = 14;
	
	public ErrorLabelText(Vector _stringList){
		super(Field.READONLY | Field.NON_FOCUSABLE | Field.USE_ALL_WIDTH);
		
		m_stringList = _stringList;
		try{
			Font myFont = FontFamily.forName("BBMillbankTall").getFont(Font.PLAIN,8,Ui.UNITS_pt);
			setFont(myFont);
		}catch(Exception _e){}
	}
	
	public void layout(int _width,int _height){
		final int t_width = Display.getWidth();
			
		final int t_size 	= m_stringList.size();
		final int t_height = Math.max(0, (t_size - 1)) * fsm_space +  t_size * fsm_fontHeight;
		
		setExtent(t_width, t_height);
	}
	
	public void paint(Graphics _g){
		int t_y = 0;
		final int t_fontHeight = fsm_fontHeight;
		
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
    EditField			m_APN			= null;
    
    ButtonField         m_connectBut    = null;
    LabelField          m_stateText     = null;
    ErrorLabelText      m_errorText     = null;
    LabelField			m_uploadingText = null;
        
    recvMain			m_mainApp		= null;
    
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
        
        m_APN			= new EditField(recvMain.sm_local.getString(localResource.APN_LABEL),
        			m_mainApp.m_APN,128,EditField.FILTER_DEFAULT);
        
        add(m_APN);
        
        m_connectBut = new ButtonField(m_mainApp.m_connectDeamon.IsConnected()?"disconnect":"connect",
        								ButtonField.CONSUME_CLICK| ButtonField.NEVER_DIRTY);
        
        m_connectBut.setChangeListener(this);
        
        add(m_connectBut);              
        
        m_stateText = new LabelField(m_mainApp.GetStateString(), LabelField.ELLIPSIS | LabelField.USE_ALL_WIDTH);
        add(m_stateText);
        
        m_errorText = new ErrorLabelText(m_mainApp.GetErrorString());
        add(m_errorText);
        
        m_uploadingText = new LabelField("", LabelField.ELLIPSIS | LabelField.USE_ALL_WIDTH);
        add(m_uploadingText);
        
        RefreshUploadState(_app.m_uploadingDesc);
               
    }
    
    public final boolean onClose(){
    	if(m_mainApp.m_connectDeamon.IsConnected()){
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
        		
        		t_total = t_total + "Subject: "+ t_desc.m_mail.GetSubject() + "(" +
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
												
				if(m_mainApp.m_connectDeamon.IsConnected()){
					
					try{
						m_mainApp.m_connectDeamon.Disconnect();
					}catch(Exception _e){}
					
					m_connectBut.setLabel("connect");
					m_mainApp.SetStateString("disconnect");
					
				}else{
										
					
					try{
						m_mainApp.m_hostname 		= m_hostName.getText();
						m_mainApp.m_port 			= Integer.valueOf(m_hostport.getText()).intValue();
						m_mainApp.m_userPassword 	= m_userPassword.getText();
						m_mainApp.m_APN				= m_APN.getText();
						
						m_mainApp.m_connectDeamon.Connect(m_mainApp.m_hostname,
															m_mainApp.m_port,
															m_mainApp.m_APN,
															m_mainApp.m_userPassword);
						
//						m_mainApp.m_connectDeamon.Connect("192.168.10.20",9716,"111111");
						
						m_mainApp.SetStateString("connecting...");
						m_connectBut.setLabel("disconnect");
						
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
	
	String m_attachmentDir = null;
	
	stateScreen 		m_stateScreen 		= null;
	uploadFileScreen 	m_uploadFileScreen	= null;
	connectDeamon 		m_connectDeamon		= new connectDeamon(this);
	
	String				m_stateString		= new String("disconnect");
	
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
	
	String				m_hostname = new String();
	int					m_port = 0;
	String				m_userPassword = new String();
	String				m_APN = new String();
	
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
			return "Add YuchBerry File";
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
			return "Del YuchBerry File";
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
		
		
	}
	
	public void Start(){
		
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
	
        
		ApplicationMenuItemRepository.getInstance().addMenuItem(ApplicationMenuItemRepository.MENUITEM_EMAIL_EDIT,m_addItem);
		ApplicationMenuItemRepository.getInstance().addMenuItem(ApplicationMenuItemRepository.MENUITEM_EMAIL_EDIT ,m_delItem);
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
		
		invokeLater(new Runnable()
		{
		    public void run()
			{
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
	
	public void PopupDlgToOpenAttach(final connectDeamon.FetchAttachment _att){
				
		// prompt by the background thread
		//
		synchronized(getEventLock()){
			
			Dialog t_dlg = new Dialog(Dialog.D_OK_CANCEL,_att.m_realName + "is Downloaded \nOpened?",
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
		
		if(m_stateScreen != null){
			m_stateScreen.m_stateText.setText(_state);
		}
		
		m_stateString = _state;
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
		return m_stateString;
	}
	
	public void SetErrorString(final String _error){
		m_errorString.addElement(new ErrorInfo(_error));
		if(m_errorString.size() > 8){
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

