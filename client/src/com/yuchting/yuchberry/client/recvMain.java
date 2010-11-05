package com.yuchting.yuchberry.client;

import java.util.Vector;

import local.localResource;
import net.rim.blackberry.api.mail.Message;
import net.rim.blackberry.api.menuitem.ApplicationMenuItem;
import net.rim.blackberry.api.menuitem.ApplicationMenuItemRepository;
import net.rim.device.api.i18n.MessageFormat;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.MainScreen;

final class stateScreen extends MainScreen implements FieldChangeListener{
										
        
    EditField           m_hostName      = null;
    EditField			m_hostport		= null;
    EditField           m_userPassword  = null;
    
    ButtonField         m_connectBut    = null;
    LabelField          m_stateText     = null;
    LabelField          m_errorText     = null;
    LabelField			m_uploadingText = null;
        
    recvMain			m_mainApp		= null;
    
    public stateScreen(recvMain _app) {
    	        
        super();
        
        m_mainApp	= _app;        
        
        m_hostName = new EditField(recvMain.sm_local.getString(localResource.HOST),"",128, EditField.FILTER_DEFAULT);
        m_hostName.setChangeListener(this);
        add(m_hostName);
        
        m_hostport = new EditField(recvMain.sm_local.getString(localResource.PORT),"",5, EditField.FILTER_INTEGER);
        m_hostport.setChangeListener(this);
        add(m_hostport);
        
        m_userPassword = new EditField(recvMain.sm_local.getString(localResource.USER_PASSWORD),"",128, EditField.FILTER_DEFAULT);
        add(m_userPassword);
        
        m_connectBut = new ButtonField(m_mainApp.m_connectDeamon.IsConnected()?"disconnect":"connect",
        								ButtonField.CONSUME_CLICK| ButtonField.NEVER_DIRTY);
        
        m_connectBut.setChangeListener(this);
        
        add(m_connectBut);              
        
        m_stateText = new LabelField(m_mainApp.GetStateString(), LabelField.ELLIPSIS | LabelField.USE_ALL_WIDTH);
        add(m_stateText);
        
        m_errorText = new LabelField(m_mainApp.GetErrorString(), LabelField.ELLIPSIS | LabelField.USE_ALL_WIDTH);
        add(m_errorText);
        
        m_uploadingText = new LabelField("", LabelField.ELLIPSIS | LabelField.USE_ALL_WIDTH);
        add(m_uploadingText);
        
        RefreshUploadState(_app.m_uploadingDesc);
               
    }
    
    public boolean onClose(){
    	if(m_mainApp.m_connectDeamon.IsConnected()){
    		m_mainApp.requestBackground();
    		return false;
    	}
    	
    	System.exit(0);
    	
    	return true;
    }
        
    public void DialogAlert(final String _msg){
    	
    	UiApplication.getUiApplication().invokeLater(new Runnable() 
		{
		    public void run(){
		       Dialog.alert(_msg);
		    }
		});
    }
    
    public void RefreshUploadState(Vector _uploading){
    	String t_total = new String();
    	
    	for(int i = 0;i < _uploading.size();i++){
    		
    		recvMain.UploadingDesc t_desc = (recvMain.UploadingDesc)_uploading.elementAt(i);
    		
    		if(t_desc.m_attachmentIdx != -1){
    			t_total = t_total + "Subject: " + t_desc.m_mail.GetSubject() + "(Failed) \n";
    		}else{
    			Object[] t_arg = 
        		{
        			t_desc.m_mail.GetSubject(),
        			new Integer(t_desc.m_attachmentIdx),
        			new Integer(t_desc.m_mail.GetAttachment().size()),
        			new Integer(t_desc.m_uploadedSize * 100 / t_desc.m_totalSize),
        		};
        		
        		t_total = t_total + MessageFormat.format("Subject: {0} ({1,integer}/{2,integer} {3,integer}%)\n",t_arg);
    		}   		
    	}
    	
    	m_uploadingText.setText(t_total);    	
    }
        
    public void fieldChanged(Field field, int context) {
        if(context != FieldChangeListener.PROGRAMMATIC){
			// Perform action if user changed field. 
			//
			if(field == m_connectBut){
				
				if(/*m_hostName.getText().length() == 0 
					|| m_userPassword.getText().length() == 0
					|| m_hostport.getText().length() == 0*/false){
					
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
						//m_mainApp.m_connectDeamon.Connect(m_hostName.getText(),
						//									Integer.valueOf(m_hostport.getText()).intValue(),
						//									m_userPassword.getText());
						
						m_mainApp.m_connectDeamon.Connect("192.168.10.20",9716,"111111");
						
						m_mainApp.SetStateString("connecting...");
						m_connectBut.setLabel("disconnect");
						
					}catch(Exception e){
						DialogAlert(e.getMessage());
					}
				}				
			}
        }else{
        	// Perform action if application changed field.
        }
    }
    
}

public class recvMain extends UiApplication implements localResource {
	
	stateScreen 		m_stateScreen 		= null;
	uploadFileScreen 	m_uploadFileScreen	= null;
	connectDeamon 		m_connectDeamon		= new connectDeamon(this);
	
	String				m_stateString		= new String("disconnect");
	String				m_errorString		= new String();
	
	String				m_currentPath 		= new String("file:///store/");
	
	Vector				m_uploadingDesc 	= new Vector();
	
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
				
		// create the sdcard path 
		//
//        try{
//        	FileConnection fc = (FileConnection) Connector.open("file:///SDCard/YuchBerry",Connector.READ_WRITE);
//        	if(!fc.exists()){
//        		fc.mkdir();
//        	}
//        }catch(Exception _e){
//        	
//        	DialogAlert("can't use the SDCard to store attachment!");
//        	System.exit(0);
//        }
	
		m_addItem.m_mainApp = this;
		m_delItem.m_mainApp = this;
		
		ApplicationMenuItemRepository.getInstance().
			addMenuItem(ApplicationMenuItemRepository.MENUITEM_EMAIL_EDIT,m_addItem);
		
		ApplicationMenuItemRepository.getInstance().
			addMenuItem(ApplicationMenuItemRepository.MENUITEM_EMAIL_EDIT ,m_delItem);
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
	
	public void OpenAttachmentFileScreen(boolean _del){
		if(m_uploadFileScreen == null){
			m_uploadFileScreen = new uploadFileScreen(m_connectDeamon, this,_del);
			popScreen(m_uploadFileScreen);
		}
	}
	
	public void SetStateString(String _state){
		
		if(m_stateScreen != null){
			m_stateScreen.m_stateText.setText(_state);
		}
		
		m_stateString = _state;
	}
	
	public void SetUploadingDesc(fetchMail _mail,int _attachmentIdx,
									int _uploadedSize,int _totalSize){
						
		boolean t_found = false;
		for(int i = 0;i < m_uploadingDesc.size();i++){
			UploadingDesc t_desc = (UploadingDesc)m_uploadingDesc.elementAt(i);
			if(t_desc.m_mail == _mail){
				t_found = true;
				
				t_desc.m_attachmentIdx	= _attachmentIdx;
				t_desc.m_totalSize		= _totalSize;
				t_desc.m_uploadedSize	= _uploadedSize;
				
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
	
	public Vector GetUploadingDesc(){
		return m_uploadingDesc;
	}

	public String GetStateString(){
		return m_stateString;
	}
	
	public void SetErrorString(String _error){
		if(m_stateScreen != null){
			m_stateScreen.m_errorText.setText(_error);
		}
		
		m_errorString = _error;
	}
	
	public String GetErrorString(){
		return m_errorString;
	}	
}

