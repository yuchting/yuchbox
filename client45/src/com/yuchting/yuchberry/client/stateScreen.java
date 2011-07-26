package com.yuchting.yuchberry.client;

import java.util.Vector;

import local.localResource;
import net.rim.device.api.servicebook.ServiceBook;
import net.rim.device.api.servicebook.ServiceRecord;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.component.PasswordEditField;
import net.rim.device.api.ui.component.RichTextField;
import net.rim.device.api.ui.container.MainScreen;

import com.yuchting.yuchberry.client.ui.ButtonSegImage;
import com.yuchting.yuchberry.client.ui.ImageSets;
import com.yuchting.yuchberry.client.ui.ImageUnit;

public class stateScreen extends MainScreen implements FieldChangeListener{
										
  
    
	MenuItem	m_helpMenu = new MenuItem(recvMain.sm_local.getString(localResource.STATE_SCREEN_HELP_MENU), 99, 10) {
		public void run() {
			recvMain.openURL("http://code.google.com/p/yuchberry/wiki/YuchBerry_Client_Help");
		}
	};
	
    
	MenuItem 	m_aboutMenu = new MenuItem(recvMain.sm_local.getString(localResource.ABOUT_MENU_TEXT), 100, 10) {
		public void run() {
			recvMain t_app = (recvMain)UiApplication.getUiApplication();
			t_app.PopupAboutScreen();
		}
	};
	
	MenuItem	m_shareMenu = new MenuItem(recvMain.sm_local.getString(localResource.SHARE_TO_FRIEND_MENU), 101, 10) {
		public void run() {
			recvMain t_app = (recvMain)UiApplication.getUiApplication();
			t_app.PopupShareScreen();			
		}
	};
	
	MenuItem 	m_setingMenu = new MenuItem(recvMain.sm_local.getString(localResource.SETTING_MENU_TEXT), 103, 10) {
		public void run() {
			recvMain t_app = (recvMain)UiApplication.getUiApplication();
			t_app.PopupSettingScreen();													
		}
	};
	
											
	MenuItem	m_debugInfoMenu = new MenuItem(recvMain.sm_local.getString(localResource.DEBUG_MENU_TEXT), 104, 10) {
		public void run() {
			recvMain t_app = (recvMain)UiApplication.getUiApplication();
			t_app.m_debugInfoScreen = new debugInfo(t_app);
			t_app.pushScreen(t_app.m_debugInfoScreen);
		}
	};
	
											
	MenuItem	m_weiboMenu = new MenuItem(recvMain.sm_local.getString(localResource.YB_WEIBO_MENU_LABEL), 105, 10) {
		public void run() {
			recvMain t_app = (recvMain)UiApplication.getUiApplication();
			if(t_app.getScreenCount() == 2){
				t_app.popStateScreen();
			}else{
				t_app.PopupWeiboScreen();
			}
		}
	};
	
	MenuItem	m_quitMenu = new MenuItem(recvMain.sm_local.getString(localResource.STATE_QUIT_MENU_LABEL), 200, 10) {
		public void run() {
			recvMain t_app = (recvMain)UiApplication.getUiApplication();
			t_app.Exit();
		}
	};
	
	EditField           m_hostName      = null;
	EditField			m_hostport		= null;
    PasswordEditField   m_userPassword  = null;
       
    ButtonField         m_connectBut    = null;
    LabelField          m_stateText     = null;
   
    RichTextField		m_uploadingText = null;
            
    recvMain			m_mainApp		= null;
    
	Manager m_mainManger = new Manager(Manager.VERTICAL_SCROLL){
		
		public void sublayout(int _width,int _height){
			
			int t_start_x = 10;
			int t_width = recvMain.fsm_display_width - t_start_x * 2;
			int t_height = m_stateInputBG.getImageHeight();
			
			int y = recvMain.fsm_display_height/3 + 20;
			
			setPositionChild(m_hostName,t_start_x,y);
			layoutChild(m_hostName,t_width,t_height);
			
			y += m_stateInputBG.getImageHeight();
			
			setPositionChild(m_hostport,t_start_x,y);
			layoutChild(m_hostport,t_width,t_height);
			
			y += m_stateInputBG.getImageHeight();
			
			setPositionChild(m_userPassword,t_start_x,y);
			layoutChild(m_userPassword,t_width,t_height);
			
			y += m_stateInputBG.getImageHeight();
			
			setPositionChild(m_connectBut,t_start_x,y);
			layoutChild(m_connectBut,m_connectBut.getPreferredWidth(),m_connectBut.getPreferredHeight());
			
			y += m_connectBut.getPreferredHeight();
			
			setPositionChild(m_stateText,t_start_x,y);
			layoutChild(m_stateText,recvMain.fsm_display_width,m_stateText.getFont().getHeight());						
			
			setExtent(recvMain.fsm_display_width, recvMain.fsm_display_height);
		}
		
		public void subpaint(Graphics _g){
			
			int x = (recvMain.fsm_display_width - m_stateBG.getWidth()) / 2;
	    	int y = (recvMain.fsm_display_height - m_stateBG.getHeight()) / 2;
	    	
	    	m_stateImage.drawImage(_g, m_stateBG, x, y);
	    	
	    	int t_delta_x = 4;
	    	int t_delta_y = (m_stateInputBG.getImageHeight() - m_hostName.getFont().getHeight()) / 2;
	    	
	    	
	    	m_stateInputBG.draw(_g, m_hostName.getExtent().x - t_delta_x, 
	    							m_hostName.getExtent().y - t_delta_y, m_hostName.getExtent().width + t_delta_x * 2);
	    	
	    	m_stateInputBG.draw(_g, m_hostport.getExtent().x - t_delta_x, 
	    							m_hostport.getExtent().y - t_delta_y, m_hostport.getExtent().width + t_delta_x * 2);
	    	
	    	m_stateInputBG.draw(_g, m_userPassword.getExtent().x - t_delta_x, 
	    							m_userPassword.getExtent().y - t_delta_y, m_userPassword.getExtent().width + t_delta_x * 2);
	    	
			super.subpaint(_g);
		}
	};
	
	ImageSets	m_stateImage = null;
	ImageUnit	m_stateBG = null;
	ButtonSegImage m_stateInputBG = null;
	
    public stateScreen(final recvMain _app){
    	
    	try{
    		m_stateImage = new ImageSets("/state_images.imageset");
    	}catch(Exception e){
    		_app.DialogAlertAndExit("load state_images error:"+e.getMessage()+e.getClass().getName());
    		return ;
    	}
    	
    	m_stateBG = m_stateImage.getImageUnit("state_bg");
    	m_stateInputBG = new ButtonSegImage(m_stateImage.getImageUnit("state_input_left"), 
    			m_stateImage.getImageUnit("state_input_mid"), 
    			m_stateImage.getImageUnit("state_input_right"), 
    			m_stateImage);
    	
        m_mainApp	= _app;
        
        m_hostName = new EditField(recvMain.sm_local.getString(localResource.HOST),
        				m_mainApp.m_hostname,128, EditField.FILTER_DEFAULT);
               
        m_hostName.setChangeListener(this);
        m_mainManger.add(m_hostName);
        
        m_hostport = new EditField(recvMain.sm_local.getString(localResource.PORT),
        				m_mainApp.m_port == 0?"":Integer.toString(m_mainApp.m_port),5,EditField.FILTER_INTEGER);

        m_hostport.setChangeListener(this);
        m_mainManger.add(m_hostport);
        
        m_userPassword = new PasswordEditField(recvMain.sm_local.getString(localResource.USER_PASSWORD),
        								m_mainApp.m_userPassword,128,EditField.NO_COMPLEX_INPUT);
        
        m_mainManger.add(m_userPassword);
               
        m_connectBut = new ButtonField(recvMain.sm_local.getString(m_mainApp.m_connectDeamon.IsConnectState()?
        									localResource.DISCONNECT_BUTTON_LABEL:localResource.CONNECT_BUTTON_LABEL),
        									ButtonField.CONSUME_CLICK| ButtonField.NEVER_DIRTY);
        
        m_connectBut.setChangeListener(this);
        
        m_mainManger.add(m_connectBut);
        
        m_stateText = new LabelField(m_mainApp.GetStateString(), LabelField.ELLIPSIS | LabelField.USE_ALL_WIDTH);
        m_mainManger.add(m_stateText);
        
        add(m_mainManger);
        
        m_uploadingText = new RichTextField("", LabelField.ELLIPSIS | LabelField.USE_ALL_WIDTH);
        add(m_uploadingText);
            
        RefreshUploadState(_app.m_uploadingDesc);
        
        //setTitle(new LabelField(recvMain.sm_local.getString(localResource.STATE_TITLE_LABEL),LabelField.ELLIPSIS | LabelField.USE_ALL_WIDTH));
        
        if(m_mainApp.m_connectDeamon.IsConnectState()){
        	m_connectBut.setFocus();
        }else{        	
            if(m_mainApp.m_hostname.length() != 0){
            	m_connectBut.setFocus();            	
            }
        }         
    }
    
    protected void makeMenu(Menu _menu,int instance){
    	_menu.add(m_helpMenu);
    	_menu.add(m_aboutMenu);
    	_menu.add(m_shareMenu);
    	_menu.add(MenuItem.separator(102));
    	_menu.add(m_setingMenu);
    	_menu.add(m_debugInfoMenu);
    	
    	if(m_mainApp.m_enableWeiboModule){
    		_menu.add(m_weiboMenu);
    	} 
    	
    	_menu.add(MenuItem.separator(199));
    	_menu.add(m_quitMenu);
    	
    	super.makeMenu(_menu, instance);
    }
    
    public final boolean onClose(){
    	
    	if(m_mainApp.m_connectDeamon.IsConnectState()){
    		if(m_mainApp.getScreenCount() == 1){
    			m_mainApp.requestBackground();
        		return false;	
    		}else{
    			m_mainApp.popStateScreen();
    			return true;
    		}    		
    	}
    	
    	m_mainApp.Exit();
    	
    	return true;
    }
    
    protected boolean keyDown(int keycode,int time){
    	if(m_connectBut.isFocus()){
    		final int key = Keypad.key(keycode);
        	switch(key){
        	case 'A':
        		m_aboutMenu.run();
        		return true;
        	case 'S':
        		m_setingMenu.run();
        		return true;
        	case 'D':
        		m_debugInfoMenu.run();
        		return true;
        	case 'H':
        		m_helpMenu.run();
        		return true;
        	case 'F':
        		m_shareMenu.run();
        		return true;
        	case 'W':
        		if(m_mainApp.m_enableWeiboModule){
        			m_weiboMenu.run();
        			return true;
        		}
        		break;
        	}
    	}
    	
    	return false;    	
    }
    
    
   
    
    public void RefreshUploadState(final Vector _uploading){
    	String t_total = new String();
    	
    	for(int i = 0;i < _uploading.size();i++){
    		
    		recvMain.UploadingDesc t_desc = (recvMain.UploadingDesc)_uploading.elementAt(i);
    		
    		if(t_desc.m_attachmentIdx == -1){
    			
    			t_total = t_total + "(Failed) retry again " + t_desc.m_mail.GetSubject();
    			
    		}else{
    			
    			final int t_tmp = (int)((float)t_desc.m_uploadedSize / (float)t_desc.m_totalSize * 1000);
    			final float t_percent = (float)t_tmp / 10;
        		
        		t_total = t_total +"(" + t_desc.m_attachmentIdx + "/" + t_desc.m_mail.GetAttachment().size() + " " + t_percent + "%)" + t_desc.m_mail.GetSubject() ;
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
					
					ServiceBook t_sb = ServiceBook.getSB();
					ServiceRecord[] t_record = t_sb.findRecordsByCid("CMIME");
					if(t_record == null || t_record.length == 0){
						m_mainApp.DialogAlert(recvMain.sm_local.getString(localResource.NEED_CMIME_PROMPT));
						return;
					}
					
					if(m_hostName.getText().indexOf(" ") != -1){
						m_mainApp.DialogAlert(recvMain.sm_local.getString(localResource.STATE_HOST_STRING_ILLEGAL_PROMPT));
						return;
					}
					
					try{
						m_mainApp.m_hostname 		= m_hostName.getText();
						m_mainApp.m_port 			= Integer.valueOf(m_hostport.getText()).intValue();
						m_mainApp.m_userPassword 	= m_userPassword.getText();

						m_mainApp.m_connectDeamon.Connect();
						
						m_connectBut.setLabel(recvMain.sm_local.getString(localResource.DISCONNECT_BUTTON_LABEL));
						
						m_mainApp.Start();
						
					}catch(Exception e){
						m_mainApp.DialogAlert(e.getMessage() + e.getClass().getName());
					}
				}				
			}
        }else{
        	// Perform action if application changed field.
        }
    }
    
}
