package com.yuchting.yuchberry.client.screen;

import java.util.Vector;

import local.localResource;
import net.rim.device.api.servicebook.ServiceBook;
import net.rim.device.api.servicebook.ServiceRecord;
import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.component.PasswordEditField;
import net.rim.device.api.ui.component.RichTextField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.MainScreen;

import com.yuchting.yuchberry.client.debugInfo;
import com.yuchting.yuchberry.client.recvMain;
import com.yuchting.yuchberry.client.ui.ButtonSegImage;
import com.yuchting.yuchberry.client.ui.HyperlinkButtonField;
import com.yuchting.yuchberry.client.ui.ImageSets;
import com.yuchting.yuchberry.client.ui.ImageUnit;

class ConnectButton extends Field{
	
    String 	m_text = "";
    
    ImageUnit[]	m_image = null;
    ImageUnit[]	m_image_focus = null;
    ImageSets	m_imageSets = null;
    
    int			m_connectState = 0;
    int			m_drawStateIndex = 0;

    public ConnectButton(String text,ImageUnit[] _image,ImageUnit[] _image_focus,ImageSets _imageSets){
        this( text,_image,_image_focus,_imageSets,0);
    }

    public ConnectButton( String text,ImageUnit[] _image,ImageUnit[] _image_focus,ImageSets _imageSets,long style){
        super(Field.FOCUSABLE | style );

        m_text = text;
        m_image = _image;
        m_image_focus = _image_focus;
        m_imageSets = _imageSets;
    }
    
    int m_animationId = -1;
    public void setConnectState(int _state,final recvMain _app){
    	if(m_connectState != _state){
    		
    		m_connectState = _state;
    		    		
    		switch(m_connectState){
    		case recvMain.CONNECTED_STATE:
    		case recvMain.CONNECTING_STATE:
    			m_text = recvMain.sm_local.getString(localResource.DISCONNECT_BUTTON_LABEL);
    			break;
    		case recvMain.DISCONNECT_STATE:
    			m_text = recvMain.sm_local.getString(localResource.CONNECT_BUTTON_LABEL);
    			break;
    		}
    		
    		synchronized (this) {
    			
    			m_drawStateIndex = _state;
    			
    			if(m_connectState == recvMain.CONNECTING_STATE){
    				
    				if(m_animationId == -1){
    					
    					m_animationId = _app.invokeLater(new Runnable() {
							public void run() {
								
								if(m_connectState == recvMain.CONNECTED_STATE
									|| m_connectState == recvMain.DISCONNECT_STATE){
									
									synchronized (ConnectButton.this) {
										_app.cancelInvokeLater(m_animationId);
										m_animationId = -1;
										m_drawStateIndex = m_connectState;
									}
								}else{
									
									if(m_drawStateIndex == recvMain.CONNECTING_STATE){
										m_drawStateIndex = recvMain.DISCONNECT_STATE;
									}else{
										m_drawStateIndex = recvMain.CONNECTING_STATE;
									}
									
									ConnectButton.this.invalidate();
								}
								
							}
							
						}, 1000, true);
    				}
    				
    			}else{
    				
    			}
			}
    		
        	invalidate();	
    	}    	
    }
    
    public int getImageWidth(){
    	return m_image[0].getWidth();
    }
    
    public int getImageHeight(){
    	return m_image[0].getHeight();
    }    
            
    protected void layout(int width,int height){
    
		width = m_image[0].getWidth();
		height= m_image[0].getHeight();    	
    	
    	setExtent(width,height);
    }
    
    protected void paint( Graphics g ){
    	focusPaint(g,isFocus());
    }
    
    private void focusPaint(Graphics g,boolean focus){
    	int oldColour = g.getColor();
    	Font oldFont	= g.getFont();
    	try{
  	
    		if(focus){
    			m_imageSets.drawImage(g, m_image_focus[m_drawStateIndex], 0, 0);
    		}else{
    			m_imageSets.drawImage(g, m_image[m_drawStateIndex], 0, 0);
    		}
    		
    		
    	}finally{
    		g.setColor( oldColour );
    		g.setFont(oldFont);
    	}
    }
    
    protected void drawFocus( Graphics g, boolean on ){
    	focusPaint(g,on);
    }
    
    protected void onUnfocus(){
    	super.onUnfocus();
    	invalidate();
    }
            
            
    protected boolean keyChar( char character, int status, int time ) 
    {
        if( character == Characters.ENTER ) {
            fieldChangeNotify( 0 );
            return true;
        }
        return super.keyChar( character, status, time );
    }

    protected boolean navigationClick( int status, int time ) {        
        keyChar(Characters.ENTER, status, time );            
        return true;
    }

    protected boolean invokeAction( int action ) 
    {
        switch( action ) {
            case ACTION_INVOKE: {
                fieldChangeNotify( 0 );
                return true;
            }
        }
        return super.invokeAction( action );
    }
            

    public void setDirty( boolean dirty ) 
    {
        // We never want to be dirty or muddy
    }
    
            
    public void setMuddy( boolean muddy ) 
    {
        // We never want to be dirty or muddy
    }
    
    
    public String getMenuText()
    {
        return m_text;
    }
    

    /**
     * Returns a MenuItem that could be used to invoke this link.
     */
    public MenuItem getMenuItem()
    {
        return new MenuItem( getMenuText(), 0, 0 ) {
            public void run() {
                fieldChangeNotify( 0 );
            }
        };
    }
}

public class stateScreen extends MainScreen implements FieldChangeListener{
    
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
	
	
	HorizontalFieldManager m_hostNameMgr = new HorizontalFieldManager(Manager.HORIZONTAL_SCROLL | Manager.NO_VERTICAL_SCROLL);
	EditField           m_hostName      = null;
	
	HorizontalFieldManager m_hostportMgr = new HorizontalFieldManager(Manager.HORIZONTAL_SCROLL | Manager.NO_VERTICAL_SCROLL);
	EditField			m_hostport		= null;
	
	HorizontalFieldManager m_userPasswordMgr = new HorizontalFieldManager(Manager.HORIZONTAL_SCROLL | Manager.NO_VERTICAL_SCROLL);
    PasswordEditField   m_userPassword  = null;
       
    public ConnectButton	m_connectBut    = null;
    
    HyperlinkButtonField m_getHostLink	= new HyperlinkButtonField(recvMain.sm_local.getString(localResource.STATE_SCREEN_HELP_MENU),0xffffff,0x8a8a8a);
   
    RichTextField		m_uploadingText = null;
            
    recvMain			m_mainApp		= null;
    
	Manager m_mainManger = new Manager(Manager.VERTICAL_SCROLL){
		
		public void sublayout(int _width,int _height){
			
			int t_start_x = 10;
			int t_width = recvMain.fsm_display_width - t_start_x * 2;
			int t_height = m_stateInputBG.getImageHeight();
			
			int y = recvMain.fsm_display_height/3 + 20;
			
			setPositionChild(m_hostNameMgr,t_start_x,y);
			layoutChild(m_hostNameMgr,t_width,t_height);
			
			y += m_stateInputBG.getImageHeight();
			
			setPositionChild(m_hostportMgr,t_start_x,y);
			layoutChild(m_hostportMgr,t_width,t_height);
			
			y += m_stateInputBG.getImageHeight();
			
			setPositionChild(m_userPasswordMgr,t_start_x,y);
			layoutChild(m_userPasswordMgr,t_width,t_height);
			
			y += m_stateInputBG.getImageHeight();
			
			setPositionChild(m_connectBut,t_start_x,y);
			layoutChild(m_connectBut,m_connectBut.getImageWidth(),m_connectBut.getImageHeight());
			
			t_start_x += m_connectBut.getImageWidth();
						
			setPositionChild(m_getHostLink,t_start_x,y);
			layoutChild(m_getHostLink,getPreferredWidth(),m_getHostLink.getFont().getHeight());
			
			y += m_connectBut.getImageHeight();
			t_start_x = 0;
			
			setPositionChild(m_uploadingText, t_start_x, y);
			layoutChild(m_uploadingText,getPreferredWidth(),getPreferredHeight() / 2);
			
			setExtent(getPreferredWidth(), getPreferredHeight());
		}
		
		public int getPreferredWidth(){
			return recvMain.fsm_display_width;
		}
		
		public int getPreferredHeight(){
			return recvMain.fsm_display_height * 2;
		}
		
		public void subpaint(Graphics _g){
			
			m_mainApp.m_allImageSets.fillImageBlock(_g, m_stateBG, 0, 0, getPreferredWidth(), getPreferredHeight());
			
			int t_logo_x = (getPreferredWidth() - m_stateLogo.getWidth()) / 2;
			int t_logo_y = m_hostNameMgr.getExtent().y - m_stateLogo.getHeight();
			
			m_mainApp.m_allImageSets.drawImage(_g, m_stateLogo, t_logo_x, t_logo_y);
			
	    	int t_delta_x = 4;
	    	int t_delta_y = (m_stateInputBG.getImageHeight() - m_hostName.getFont().getHeight()) / 2;	    	
	    	
	    	m_stateInputBG.draw(_g, m_hostNameMgr.getExtent().x - t_delta_x, 
	    							m_hostNameMgr.getExtent().y - t_delta_y, m_hostNameMgr.getExtent().width + t_delta_x * 2);
	    	
	    	m_stateInputBG.draw(_g, m_hostportMgr.getExtent().x - t_delta_x, 
	    							m_hostportMgr.getExtent().y - t_delta_y, m_hostportMgr.getExtent().width + t_delta_x * 2);
	    	
	    	m_stateInputBG.draw(_g, m_userPasswordMgr.getExtent().x - t_delta_x, 
	    							m_userPasswordMgr.getExtent().y - t_delta_y, m_userPasswordMgr.getExtent().width + t_delta_x * 2);

	    	int oldColor = _g.getColor();
	    	try{	    	
	    		_g.setColor(0xffffff);
	    		_g.drawText(recvMain.fsm_client_version, 0, 0);
	    		
	    	}finally{
	    		_g.setColor(oldColor);
	    	}    	
	    	
	    	int t_num = getFieldCount();
	    	for(int i = 0 ;i < t_num;i++){
	    		Field t_field = getField(i);
	    			    		
	    		if(t_field == m_uploadingText){
	    			// set the uploading text
	    			//
	    			oldColor = _g.getColor();
	    			try{
	    				_g.setColor(0xffffff);
	    				paintChild(_g, t_field);
	    			}finally{
	    				_g.setColor(oldColor);
	    			}
	    		}else{
	    			paintChild(_g, t_field);
	    		}	    		
	    	}
		}
	};
	
	ImageUnit		m_stateBG = null;
	ImageUnit		m_stateLogo = null;
	
	ButtonSegImage	m_stateInputBG = null;
	
    public stateScreen(recvMain _app){
    	m_mainApp	= _app;
    	 
    	
    	m_stateBG		= m_mainApp.m_allImageSets.getImageUnit("state_bg");
    	m_stateLogo		= m_mainApp.m_allImageSets.getImageUnit("state_logo");
    	
    	m_stateInputBG = new ButtonSegImage(m_mainApp.m_allImageSets.getImageUnit("state_input_left"), 
							    			m_mainApp.m_allImageSets.getImageUnit("state_input_mid"), 
							    			m_mainApp.m_allImageSets.getImageUnit("state_input_right"), 
							    			m_mainApp.m_allImageSets);  	
       
        
        m_hostName = new EditField(recvMain.sm_local.getString(localResource.HOST),
        				m_mainApp.m_hostname,128, EditField.FILTER_DEFAULT);
               
        m_hostName.setChangeListener(this);
        m_hostNameMgr.add(m_hostName);
        m_mainManger.add(m_hostNameMgr);
        
        m_hostport = new EditField(recvMain.sm_local.getString(localResource.PORT),
        				m_mainApp.m_port == 0?"":Integer.toString(m_mainApp.m_port),5,EditField.FILTER_INTEGER);

        m_hostport.setChangeListener(this);
        m_hostportMgr.add(m_hostport);
        m_mainManger.add(m_hostportMgr);
        
        m_userPassword = new PasswordEditField(recvMain.sm_local.getString(localResource.USER_PASSWORD),
        								m_mainApp.m_userPassword,128,EditField.NO_COMPLEX_INPUT);
        
        m_userPasswordMgr.add(m_userPassword);
        m_mainManger.add(m_userPasswordMgr);
               
        m_connectBut = new ConnectButton(recvMain.sm_local.getString(localResource.CONNECT_BUTTON_LABEL),
        								new ImageUnit[]{
        									m_mainApp.m_allImageSets.getImageUnit("button_disconnect"),
        									m_mainApp.m_allImageSets.getImageUnit("button_connecting"),
        									m_mainApp.m_allImageSets.getImageUnit("button_connected")
        								},
        								new ImageUnit[]{
											m_mainApp.m_allImageSets.getImageUnit("button_disconnect_focus"),
											m_mainApp.m_allImageSets.getImageUnit("button_connecting_focus"),
											m_mainApp.m_allImageSets.getImageUnit("button_connected_focus")
										},m_mainApp.m_allImageSets);
        
        m_connectBut.setConnectState(m_mainApp.GetConnectState(),m_mainApp);        
        m_connectBut.setChangeListener(this);
        
        m_mainManger.add(m_connectBut);
        
        m_getHostLink.setChangeListener(this);
        m_mainManger.add(m_getHostLink);
        
        m_uploadingText = new RichTextField("", LabelField.ELLIPSIS | LabelField.USE_ALL_WIDTH);
        m_mainManger.add(m_uploadingText);
        
        add(m_mainManger);      
                    
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
    
    public void setConnectButState(int _state,recvMain _mainApp){
    	m_connectBut.setConnectState(_state, _mainApp);
    }
    
    protected void makeMenu(Menu _menu,int instance){
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
					
					m_mainApp.SetConnectState(recvMain.DISCONNECT_STATE);
					
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
												
						m_mainApp.Start();
						
					}catch(Exception e){
						m_mainApp.DialogAlert(e.getMessage() + e.getClass().getName());
					}
				}				
			}else if(field == m_getHostLink){
				recvMain.openURL("http://code.google.com/p/yuchberry/wiki/YuchBerry_Client_Help");
			}
        }else{
        	// Perform action if application changed field.
        }
    }
    
}
