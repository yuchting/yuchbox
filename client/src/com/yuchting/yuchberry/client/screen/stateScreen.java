/**
 *  Dear developer:
 *  
 *   If you want to modify this file of project and re-publish this please visit:
 *  
 *     http://code.google.com/p/yuchberry/wiki/Project_files_header
 *     
 *   to check your responsibility and my humble proposal. Thanks!
 *   
 *  -- 
 *  Yuchs' Developer    
 *  
 *  
 *  
 *  
 *  尊敬的开发者：
 *   
 *    如果你想要修改这个项目中的文件，同时重新发布项目程序，请访问一下：
 *    
 *      http://code.google.com/p/yuchberry/wiki/Project_files_header
 *      
 *    了解你的责任，还有我卑微的建议。 谢谢！
 *   
 *  -- 
 *  语盒开发者
 *  
 */
package com.yuchting.yuchberry.client.screen;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

import local.yblocalResource;
import net.rim.device.api.io.http.HttpProtocolConstants;
import net.rim.device.api.servicebook.ServiceBook;
import net.rim.device.api.servicebook.ServiceRecord;
import net.rim.device.api.system.Characters;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.FocusChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.component.PasswordEditField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.MainScreen;

import com.yuchting.yuchberry.client.debugInfo;
import com.yuchting.yuchberry.client.recvMain;
import com.yuchting.yuchberry.client.ui.BubbleImage;
import com.yuchting.yuchberry.client.ui.HyperlinkButtonField;
import com.yuchting.yuchberry.client.ui.ImageSets;
import com.yuchting.yuchberry.client.ui.ImageUnit;

class StateButton extends ButtonField{
	
	int m_width;
	public StateButton(String _label,ImageSets _imagesets,int _width){
		super(_label,ButtonField.CONSUME_CLICK | ButtonField.NEVER_DIRTY );
		
		m_width = _width;
	}
	
	public int getPreferredWidth(){
		return m_width;
	}
	
	public int getPreferredHeight(){
		return getFont().getHeight() * 8 / 5;
	}
}

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
    			m_text = recvMain.sm_local.getString(yblocalResource.DISCONNECT_BUTTON_LABEL);
    			break;
    		case recvMain.DISCONNECT_STATE:
    			m_text = recvMain.sm_local.getString(yblocalResource.CONNECT_BUTTON_LABEL);
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

public class stateScreen extends MainScreen{
    
	int m_menu_op = 100;
	MenuItem 	m_aboutMenu = new MenuItem(recvMain.sm_local.getString(yblocalResource.ABOUT_MENU_TEXT), m_menu_op++, 10) {
		public void run() {
			recvMain t_app = (recvMain)UiApplication.getUiApplication();
			t_app.PopupAboutScreen();
		}
	};
	
	MenuItem 	m_selectMenu = new MenuItem(recvMain.sm_local.getString(yblocalResource.STATE_SELECT_HOST_MENU_TEXT), m_menu_op++, 10) {
		public void run() {
			showInitManager();
		}
	};
	
	MenuItem	m_shareMenu = new MenuItem(recvMain.sm_local.getString(yblocalResource.SHARE_TO_FRIEND_MENU), m_menu_op++, 10) {
		public void run() {
			recvMain t_app = (recvMain)UiApplication.getUiApplication();
			t_app.PopupShareScreen();
		}
	};
	
	MenuItem 	m_setingMenu = new MenuItem(recvMain.sm_local.getString(yblocalResource.SETTING_MENU_TEXT), m_menu_op++, 10) {
		public void run() {
			recvMain t_app = (recvMain)UiApplication.getUiApplication();
			t_app.PopupSettingScreen();													
		}
	};
	
											
	MenuItem	m_debugInfoMenu = new MenuItem(recvMain.sm_local.getString(yblocalResource.DEBUG_MENU_TEXT), m_menu_op++, 10) {
		public void run() {
			recvMain t_app = (recvMain)UiApplication.getUiApplication();
			t_app.m_debugInfoScreen = new debugInfo(t_app);
			t_app.pushScreen(t_app.m_debugInfoScreen);
		}
	};
	
											
	MenuItem	m_weiboMenu = new MenuItem(recvMain.sm_local.getString(yblocalResource.YB_WEIBO_MENU_LABEL), m_menu_op++, 10) {
		public void run() {
			recvMain t_app = (recvMain)UiApplication.getUiApplication();
			t_app.PopupWeiboScreen();			
		}
	};
	
	MenuItem	m_imMenu = new MenuItem(recvMain.sm_local.getString(yblocalResource.IM_MENU_LABEL),m_menu_op++, 10) {
		public void run() {
			recvMain t_app = (recvMain)UiApplication.getUiApplication();
			t_app.PopupIMScreen();			
		}
	};
	
	MenuItem	m_quitMenu = new MenuItem(recvMain.sm_local.getString(yblocalResource.STATE_QUIT_MENU_LABEL), 200, 10) {
		public void run() {
			recvMain t_app = (recvMain)UiApplication.getUiApplication();
			t_app.Exit();
		}
	};
	
	
	
	
   
            
    recvMain			m_mainApp		= null;
    
    Manager				m_currManger	= null;
    
    InitManager			m_initManager	= null;
    AccMainManger		m_mainManger 	= null;
    LoginManager		m_loginManager	= null;
	
	ImageUnit			m_stateBG = null;
	ImageUnit			m_stateLogo = null;
	
	BubbleImage	m_stateInputBG 			= null;
	BubbleImage	m_stateInputBG_focus 	= null;
	
	/**
	 * login account thread first
	 */
	LoginAccThread		m_loginAccThread = null;
	
	
	
	/**
	 * the height of input 
	 */
	final int fm_inputHeight;
	
	/**
	 * the front and back border width of input
	 */
	final int fm_inputLeftBorder;
	
	/**
	 * the top and bottom border height of input
	 */
	final int fm_inputTopBorder;
	
    public stateScreen(recvMain _app){    	
    	m_mainApp	= _app;    	 
    	
    	m_stateBG		= m_mainApp.m_allImageSets.getImageUnit("state_bg");
    	m_stateLogo		= m_mainApp.m_allImageSets.getImageUnit("state_logo");  	
    	
    	m_stateInputBG = new BubbleImage(m_mainApp.m_allImageSets.getImageUnit("state_input_top_left"),
    									m_mainApp.m_allImageSets.getImageUnit("state_input_top"),
    									m_mainApp.m_allImageSets.getImageUnit("state_input_top_right"),
    									m_mainApp.m_allImageSets.getImageUnit("state_input_right"),
    									m_mainApp.m_allImageSets.getImageUnit("state_input_bottom_right"),
    									m_mainApp.m_allImageSets.getImageUnit("state_input_bottom"),
    									m_mainApp.m_allImageSets.getImageUnit("state_input_bottom_left"),
    									m_mainApp.m_allImageSets.getImageUnit("state_input_left"),
    									m_mainApp.m_allImageSets.getImageUnit("state_input_mid_block"),
    									m_mainApp.m_allImageSets);
    	
    	m_stateInputBG_focus = new BubbleImage(m_mainApp.m_allImageSets.getImageUnit("state_input_top_left_focus"),
												m_mainApp.m_allImageSets.getImageUnit("state_input_top_focus"),
												m_mainApp.m_allImageSets.getImageUnit("state_input_top_right_focus"),
												m_mainApp.m_allImageSets.getImageUnit("state_input_right_focus"),
												m_mainApp.m_allImageSets.getImageUnit("state_input_bottom_right_focus"),
												m_mainApp.m_allImageSets.getImageUnit("state_input_bottom_focus"),
												m_mainApp.m_allImageSets.getImageUnit("state_input_bottom_left_focus"),
												m_mainApp.m_allImageSets.getImageUnit("state_input_left_focus"),
												m_mainApp.m_allImageSets.getImageUnit("state_input_mid_block_focus"),
												m_mainApp.m_allImageSets);
    	
    	fm_inputHeight		= getFont().getHeight() + m_stateInputBG.getTopBorderHeight() + m_stateInputBG.getBottomBorderHeight();
    	fm_inputLeftBorder	= m_stateInputBG.getLeftBorderWdith();
    	fm_inputTopBorder	= m_stateInputBG.getTopBorderHeight();
    	
    	m_initManager		= new InitManager();
    	add(m_initManager);
    	
    	m_currManger = m_initManager;
    	
    	if(m_mainApp.m_hostname.length() != 0){
    		showAccMainManager(false);
    	}
        
        RefreshUploadState(_app.m_uploadingDesc);
        
        //setTitle(new LabelField(recvMain.sm_local.getString(yblocalResource.STATE_TITLE_LABEL),LabelField.ELLIPSIS | LabelField.USE_ALL_WIDTH));
    }
    
    private void showInitManager(){
    	if(m_currManger == m_initManager){
    		return;
    	}
    	
    	replace(m_currManger, m_initManager);
    	
    	m_currManger = m_initManager;
    }
    
    public void showAccMainManager(boolean _setConnect){

    	if(m_currManger == m_mainManger){
    		return ;
    	}
    	
    	if(m_mainManger == null){
    		m_mainManger		= new AccMainManger();
    	}
    	
    	replace(m_currManger, m_mainManger);
        
        if(m_mainApp.m_connectDeamon.IsConnectState()){
        	m_mainManger.m_connectBut.setFocus();
        }else{        	
            if(m_mainApp.m_hostname.length() != 0){
            	m_mainManger.m_connectBut.setFocus();
            }
        }
        
        m_mainManger.m_hostName.setText(m_mainApp.m_hostname);
        m_mainManger.m_hostport.setText(m_mainApp.m_port == 0?"":Integer.toString(m_mainApp.m_port));
        m_mainManger.m_userPassword.setText(m_mainApp.m_userPassword);
        
        if(_setConnect){
        	m_mainManger.fieldChanged(m_mainManger.m_connectBut,0);
        }
        
        m_currManger = m_mainManger;
    }
    
    private void showLoginManager(){
    	if(m_currManger == m_loginManager){
    		return;
    	}
    	
    	if(m_loginManager == null){
    		m_loginManager		= new LoginManager();
    	}
    	
    	replace(m_currManger,m_loginManager);
    	
    	m_loginManager.m_userPassword.setText(m_mainApp.m_userPassword);
    	
    	m_currManger = m_loginManager;
    }
    
    public void setConnectButState(int _state,recvMain _mainApp){
    	m_mainManger.m_connectBut.setConnectState(_state, _mainApp);
    }
    
    protected void makeMenu(Menu _menu,int instance){
    	_menu.add(m_aboutMenu);
    	_menu.add(m_selectMenu);
    	_menu.add(m_shareMenu);
    	_menu.add(m_setingMenu);
    	_menu.add(m_debugInfoMenu);
    	
    	if(m_mainApp.m_enableWeiboModule){
    		_menu.add(m_weiboMenu);
    	}
    	if(m_mainApp.m_enableIMModule){
    		_menu.add(m_imMenu);
    	}
    	
    	_menu.add(MenuItem.separator(199));
    	_menu.add(m_quitMenu);
    	
    	super.makeMenu(_menu, instance);
    }
    
    public final boolean onClose(){
    	
    	if(m_loginAccThread != null){
    		m_loginAccThread.m_waitDlg.close();
    		return true;
    	}
    	
    	if(m_mainApp.m_connectDeamon.IsConnectState()){
    		if(m_mainApp.getScreenCount() == 1){
    			m_mainApp.requestBackground();
        		return false;	
    		}else{
    			m_mainApp.popStateScreen();
    			return true;
    		}    		
    	}else{
    		if(m_currManger == m_mainManger && m_mainApp.m_hostname.length() == 0){
    			showInitManager();
        		return false;
        	}else if(m_currManger == m_loginManager){
        		showInitManager();
        		return false;
        	}
    	}
    	
    	m_mainApp.Exit();
    	
    	return true;
    }
    
    protected boolean keyDown(int keycode,int time){

    	if(m_mainManger != null 
    	&& m_mainManger.m_connectBut.isFocus()){
    		
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
        	case 'M':
        		if(m_mainApp.m_enableIMModule){
        			m_imMenu.run();
        			return true;
        		}
        	}
    	}
    	
    	return super.keyDown(keycode, time);    	
    }
        
    public void RefreshUploadState(final Vector _uploading){
//    	String t_total = new String();
//    	
//    	for(int i = 0;i < _uploading.size();i++){
//    		
//    		recvMain.UploadingDesc t_desc = (recvMain.UploadingDesc)_uploading.elementAt(i);
//    		
//    		if(t_desc.m_attachmentIdx == -1){
//    			
//    			t_total = t_total + "(Failed) retry again " + t_desc.m_mail.GetSubject();
//    			
//    		}else{
//    			
//    			final int t_tmp = (int)((float)t_desc.m_uploadedSize / (float)t_desc.m_totalSize * 1000);
//    			final float t_percent = (float)t_tmp / 10;
//        		
//        		t_total = t_total +"(" + t_desc.m_attachmentIdx + "/" + t_desc.m_mail.GetAttachment().size() + " " + t_percent + "%)" + t_desc.m_mail.GetSubject() ;
//    		}   		
//    	}
//    	
//    	m_uploadingText.setText(t_total);    	
    }
        
    private abstract class FullManager extends Manager implements FocusChangeListener{
    	
    	protected final static int UI_MARGIN_LEFT = 15;
    	
    	public FullManager(){
    		super(Manager.NO_VERTICAL_SCROLL);
    	}    	

		public int getPreferredWidth(){
			return recvMain.fsm_display_width;
		}
		
		public int getPreferredHeight(){
			return recvMain.fsm_display_height;
		}
		
		protected void paintBackground(Graphics _g){
			m_mainApp.m_allImageSets.fillImageBlock(_g, m_stateBG, 0, 0, getPreferredWidth(), getPreferredHeight());
			
			int t_logo_x = (recvMain.fsm_display_width - m_stateLogo.getWidth()) / 2;
			int t_logo_y = 10;
			
			m_mainApp.m_allImageSets.drawImage(_g, m_stateLogo, t_logo_x, t_logo_y);
			
			int oldColor = _g.getColor();
	    	try{	    	
	    		_g.setColor(0xffffff);
	    		_g.drawText(recvMain.fsm_client_version, 0, 0);
	    		
	    	}finally{
	    		_g.setColor(oldColor);
	    	}
	    }
		
    	public void focusChanged(Field field, int eventType) {
			if(eventType == FocusChangeListener.FOCUS_GAINED){
				invalidate();
			}					
		}
    }
    

    /**
     * initial screen when user open firstly
     * two buttons: official host or own host
     * 
     * @author yuch
     *
     */
    private class InitManager extends FullManager implements FieldChangeListener{
    	StateButton		m_officialHostBtn;
    	
    	StateButton		m_ownHostBtn;
    	
    	public InitManager(){
    		m_officialHostBtn = new StateButton(recvMain.sm_local.getString(yblocalResource.STATE_OFFICIAL_HOST_BTN), 
    											m_mainApp.m_allImageSets,getPreferredWidth() - UI_MARGIN_LEFT * 2);
    		
    		m_ownHostBtn = new StateButton(recvMain.sm_local.getString(yblocalResource.STATE_OWN_HOST_BTN), 
												m_mainApp.m_allImageSets,getPreferredWidth() - UI_MARGIN_LEFT * 2);
    		
    		add(m_officialHostBtn);
    		add(m_ownHostBtn);
    		
    		m_officialHostBtn.setChangeListener(this);
    		m_ownHostBtn.setChangeListener(this);
    	}

		protected void sublayout(int width, int height) {
			int t_start_x = UI_MARGIN_LEFT;
			
			int t_start_y = recvMain.fsm_display_height/3 + 20;
			
			setPositionChild(m_officialHostBtn,t_start_x,t_start_y);
			layoutChild(m_officialHostBtn,m_officialHostBtn.getPreferredWidth(),m_officialHostBtn.getPreferredHeight());
			
			t_start_y += m_officialHostBtn.getPreferredHeight() + 10;
			
			setPositionChild(m_ownHostBtn,t_start_x,t_start_y);
			layoutChild(m_ownHostBtn,m_ownHostBtn.getPreferredWidth(),m_ownHostBtn.getPreferredHeight());
			
			setExtent(getPreferredWidth(), getPreferredHeight());			
		}

		public void fieldChanged(Field field, int context) {
			if(context != FieldChangeListener.PROGRAMMATIC){
				if(field == m_officialHostBtn){
					showLoginManager();
				}else if(field == m_ownHostBtn){
					showAccMainManager(false);
				}
			}			
		}
    }
    
    
    /**
     * account main manager 
     * @author tzz
     *
     */
    private class AccMainManger extends FullManager implements FieldChangeListener{
    	
    	HorizontalFieldManager m_hostNameMgr = new HorizontalFieldManager(Manager.HORIZONTAL_SCROLL | Manager.NO_VERTICAL_SCROLL);
    	EditField           m_hostName      = null;
    	
    	HorizontalFieldManager m_hostportMgr = new HorizontalFieldManager(Manager.HORIZONTAL_SCROLL | Manager.NO_VERTICAL_SCROLL);
    	EditField			m_hostport		= null;
    	
    	HorizontalFieldManager m_userPasswordMgr = new HorizontalFieldManager(Manager.HORIZONTAL_SCROLL | Manager.NO_VERTICAL_SCROLL);
        PasswordEditField   m_userPassword  = null;
        
        public ConnectButton	m_connectBut    = null;
        
        HyperlinkButtonField m_getHostLink	= new HyperlinkButtonField(recvMain.sm_local.getString(yblocalResource.STATE_SCREEN_HELP_MENU),0xffffff,0x8a8a8a);
        
    	public AccMainManger(){
    		
    		m_hostName = new EditField(recvMain.sm_local.getString(yblocalResource.HOST),
     						m_mainApp.m_hostname,128, EditField.FILTER_DEFAULT);
    		
			m_hostName.setChangeListener(this);
			m_hostNameMgr.add(m_hostName);
			add(m_hostNameMgr);
			
			m_hostport = new EditField(recvMain.sm_local.getString(yblocalResource.PORT),
							m_mainApp.m_port == 0?"":Integer.toString(m_mainApp.m_port),5,EditField.FILTER_INTEGER);
			
			m_hostport.setChangeListener(this);
			m_hostportMgr.add(m_hostport);
			add(m_hostportMgr);
			
			m_userPassword = new PasswordEditField(recvMain.sm_local.getString(yblocalResource.USER_PASSWORD),
											m_mainApp.m_userPassword,128,EditField.NO_COMPLEX_INPUT);
			
			m_userPasswordMgr.add(m_userPassword);
			add(m_userPasswordMgr);
			  
			m_connectBut = new ConnectButton(recvMain.sm_local.getString(yblocalResource.CONNECT_BUTTON_LABEL),
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
			
			add(m_connectBut);
			
	        m_getHostLink.setChangeListener(this);
	        add(m_getHostLink);
	        
			
			m_hostName.setFocusListener(this);
			m_hostport.setFocusListener(this);
			m_userPassword.setFocusListener(this);
			m_connectBut.setFocusListener(this);
    	}
    	
    	    	
    	public void sublayout(int _width,int _height){
			
			int t_start_x = UI_MARGIN_LEFT;
			
			int t_width = recvMain.fsm_display_width - t_start_x * 2;
			int t_height = m_hostName.getFont().getHeight();
			
			int y = recvMain.fsm_display_height/3 + 20 + fm_inputTopBorder;
			
			setPositionChild(m_hostNameMgr,t_start_x,y);
			layoutChild(m_hostNameMgr,t_width,t_height);
			
			y += fm_inputHeight;
			
			setPositionChild(m_hostportMgr,t_start_x,y);
			layoutChild(m_hostportMgr,t_width,t_height);
			
			y += fm_inputHeight;
			
			setPositionChild(m_userPasswordMgr,t_start_x,y);
			layoutChild(m_userPasswordMgr,t_width,t_height);
			
			y += fm_inputHeight;
			
			setPositionChild(m_connectBut,t_start_x,y);
			layoutChild(m_connectBut,m_connectBut.getImageWidth(),m_connectBut.getImageHeight());
			
			t_start_x += m_connectBut.getImageWidth();
						
			setPositionChild(m_getHostLink,t_start_x,y);
			layoutChild(m_getHostLink,getPreferredWidth(),m_getHostLink.getFont().getHeight());
			
			y += m_connectBut.getImageHeight();
			
			setExtent(getPreferredWidth(), getPreferredHeight());
		}

		public int getPreferredHeight(){
			return  Math.max(super.getPreferredHeight(),
					(recvMain.fsm_display_height/3 + 20 + fm_inputTopBorder) + fm_inputHeight * 3 + m_connectBut.getImageHeight());
		}
		
		public void subpaint(Graphics _g){
			
	    	int t_delta_x = fm_inputLeftBorder;
	    	int t_delta_y = fm_inputTopBorder;	    	
	    	
	    	BubbleImage t_host = m_hostName.isFocus()?m_stateInputBG_focus:m_stateInputBG;
	    	BubbleImage t_port = m_hostport.isFocus()?m_stateInputBG_focus:m_stateInputBG;
	    	BubbleImage t_pass = m_userPassword.isFocus()?m_stateInputBG_focus:m_stateInputBG;
	    	
	    	t_host.draw(_g, m_hostNameMgr.getExtent().x - t_delta_x, 
							m_hostNameMgr.getExtent().y - t_delta_y,
							m_hostNameMgr.getExtent().width + t_delta_x * 2,
							fm_inputHeight,BubbleImage.NO_POINT_STYLE);
	    	
	    	t_port.draw(_g, m_hostportMgr.getExtent().x - t_delta_x, 
							m_hostportMgr.getExtent().y - t_delta_y, 
							m_hostportMgr.getExtent().width + t_delta_x * 2,
							fm_inputHeight,BubbleImage.NO_POINT_STYLE);
	    	
	    	t_pass.draw(_g, m_userPasswordMgr.getExtent().x - t_delta_x, 
							m_userPasswordMgr.getExtent().y - t_delta_y, 
							m_userPasswordMgr.getExtent().width + t_delta_x * 2,
							fm_inputHeight,BubbleImage.NO_POINT_STYLE);
	    	
	    	int t_num = getFieldCount();
	    	for(int i = 0 ;i < t_num;i++){
	    		paintChild(_g, getField(i));
	    	}
		}
		
		
		public void fieldChanged(Field field, int context) {
	        if(context != FieldChangeListener.PROGRAMMATIC){
				// Perform action if user changed field. 
				//
				if(field == m_connectBut){
				        
					if(m_hostName.getText().length() == 0 
						|| m_userPassword.getText().length() == 0
						|| m_hostport.getText().length() == 0){
						
						Dialog.alert(recvMain.sm_local.getString(yblocalResource.INPUT_FULL_SIGN_IN_SEG));
						
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
							m_mainApp.DialogAlert(recvMain.sm_local.getString(yblocalResource.NEED_CMIME_PROMPT));
							return;
						}
						
						if(m_hostName.getText().indexOf(" ") != -1){
							m_mainApp.DialogAlert(recvMain.sm_local.getString(yblocalResource.STATE_HOST_STRING_ILLEGAL_PROMPT));
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
    
    private class LoginManager extends FullManager implements FieldChangeListener{
    	
    	HorizontalFieldManager m_accountNameMgr = new HorizontalFieldManager(Manager.HORIZONTAL_SCROLL | Manager.NO_VERTICAL_SCROLL);
    	EditField           	m_accountName      	= null;
    	    	
    	HorizontalFieldManager m_userPasswordMgr = new HorizontalFieldManager(Manager.HORIZONTAL_SCROLL | Manager.NO_VERTICAL_SCROLL);
        PasswordEditField   	m_userPassword  	= null;
    	
                
        StateButton				m_loginButton		= null;
        StateButton				m_signButton		= null;
        
    	public LoginManager(){
    		
    		m_accountName	= new EditField(recvMain.sm_local.getString(yblocalResource.STATE_ACCOUNT_LABEL),
												m_mainApp.m_account,128, EditField.FILTER_EMAIL);
    		m_userPassword	= new PasswordEditField(recvMain.sm_local.getString(yblocalResource.USER_PASSWORD),
    											m_mainApp.m_userPassword,128, EditField.FILTER_INTEGER | EditField.FILTER_NUMERIC | EditField.NO_COMPLEX_INPUT);
    		
    		m_accountNameMgr.add(m_accountName);    		
    		m_userPasswordMgr.add(m_userPassword);
    		    		
    		add(m_accountNameMgr);
    		add(m_userPasswordMgr);

    		int t_btnWidth = recvMain.fsm_display_width * 2 / 5;
    		m_loginButton = new StateButton(recvMain.sm_local.getString(yblocalResource.STATE_LOGIN_BTN_LABEL),m_mainApp.m_allImageSets,t_btnWidth);
    		m_signButton = new StateButton(recvMain.sm_local.getString(yblocalResource.STATE_SIGN_BTN_LABEL),m_mainApp.m_allImageSets,t_btnWidth);

    		add(m_loginButton);
    		add(m_signButton);
    		
    		m_accountName.setFocusListener(this);
    		m_userPassword.setFocusListener(this);
    		m_loginButton.setFocusListener(this);
    		
    		m_loginButton.setChangeListener(this);
    		m_signButton.setChangeListener(this);
    	}
    	
		protected void sublayout(int width, int height) {
			int t_start_x = UI_MARGIN_LEFT;
			
			int t_width = recvMain.fsm_display_width - t_start_x * 2;
			int t_height = m_accountName.getFont().getHeight();
			
			int y = recvMain.fsm_display_height/3 + 20 + fm_inputTopBorder;
			
			setPositionChild(m_accountNameMgr,t_start_x,y);
			layoutChild(m_accountNameMgr,t_width,t_height);
			
			y += fm_inputHeight;
			
			setPositionChild(m_userPasswordMgr,t_start_x,y);
			layoutChild(m_userPasswordMgr,t_width,t_height);
			
			y += fm_inputHeight;
			
			setPositionChild(m_loginButton,t_start_x,y);
			layoutChild(m_loginButton,m_loginButton.getPreferredWidth(),m_loginButton.getPreferredHeight());
			
			t_start_x += m_loginButton.getWidth() + 20;
			
			setPositionChild(m_signButton,t_start_x,y);
			layoutChild(m_signButton,m_signButton.getPreferredWidth(),m_signButton.getPreferredHeight());
			
			setExtent(getPreferredWidth(), getPreferredHeight());
		}
		
		public void subpaint(Graphics _g){
			
	    	int t_delta_x = fm_inputLeftBorder;
	    	int t_delta_y = fm_inputTopBorder;	    	
	    	
	    	BubbleImage t_acc = m_accountName.isFocus()?m_stateInputBG_focus:m_stateInputBG;
	    	BubbleImage t_pass = m_userPassword.isFocus()?m_stateInputBG_focus:m_stateInputBG;
	    	
	    	t_acc.draw(_g, m_accountNameMgr.getExtent().x - t_delta_x,
	    					m_accountNameMgr.getExtent().y - t_delta_y,
	    					m_accountNameMgr.getExtent().width + t_delta_x * 2,
							fm_inputHeight,BubbleImage.NO_POINT_STYLE);
	    		    	
	    	t_pass.draw(_g, m_userPasswordMgr.getExtent().x - t_delta_x, 
							m_userPasswordMgr.getExtent().y - t_delta_y, 
							m_userPasswordMgr.getExtent().width + t_delta_x * 2,
							fm_inputHeight,BubbleImage.NO_POINT_STYLE);
	    	
	    	int t_num = getFieldCount();
	    	for(int i = 0 ;i < t_num;i++){
	    		paintChild(_g, getField(i));
	    	}
		}

		public void fieldChanged(Field field, int context) {
			if(FieldChangeListener.PROGRAMMATIC != context){
				if(field == m_loginButton){
					login();
				}else if(field == m_signButton){
					
					if(recvMain.fsm_OS_version.charAt(0) < '6' && recvMain.fsm_OS_version.charAt(1) == '.'){
						m_mainApp.DialogAlert(recvMain.sm_local.getString(yblocalResource.STATE_CLIENT_VER_LOW));
						return ;
					}
					
					String t_url;
					if(recvMain.GetClientLanguage() == 2){
						t_url = "http://www.yuchs.com/Android_en.html";
					}else{
						t_url = "http://www.yuchs.com/Android.html";
					}
					
//					try{
//						UiApplication.getUiApplication().pushScreen(new LoginWebView(stateScreen.this,t_url));
//					}catch(Exception e){
//						m_mainApp.DialogAlert("Internal Error:" + e.getMessage());
//					}					
					
					recvMain.openURL(t_url);
				}
			}
			
		}
		
		private void login(){

			if(!m_accountName.isDataValid()){
				m_mainApp.DialogAlert(recvMain.sm_local.getString(yblocalResource.STATE_LOGIN_ACC_NAME_ERROR));
				return;
			}
			
			if(m_userPassword.getTextLength() < 6){
				m_mainApp.DialogAlert(recvMain.sm_local.getString(yblocalResource.STATE_LOGIN_ACC_PASS_ERROR));
				return;
			}
			
			m_mainApp.m_account 		= m_accountName.getText().toLowerCase();
			m_mainApp.m_userPassword	= m_userPassword.getText();
			
			if(m_loginAccThread == null){
				m_loginAccThread = new LoginAccThread(m_mainApp.m_account, m_mainApp.m_userPassword);
				m_loginAccThread.start();
			}
		}
    }
    
    /**
     * login acc thread
     * @author tzz
     *
     */
    private class LoginAccThread extends Thread{
		
		Dialog m_waitDlg = new Dialog(recvMain.sm_local.getString(yblocalResource.STATE_LOGIN_WAIT),new Object[0],new int[0],0,null);
		
		byte[] m_postData;
		
		public LoginAccThread(String _acc,String _pass){
			String t_str = ("acc=" + _acc + "&" + "pass=" + _pass);
			try{
				m_postData = t_str.getBytes("UTF-8");
			}catch(Exception e){
				m_postData = t_str.getBytes();
			}			
		}
		
		public void run(){
			
			m_mainApp.invokeLater(new Runnable() {
				public void run() {
					m_waitDlg.show();
				}
			});
			
			String MAIN_URL = DeviceInfo.isSimulator()?"http://192.168.10.7:8888/f/login/":"http://www.yuchs.com/f/login/";		
			
			MAIN_URL += recvMain.getHTTPAppendString();
			
			try{
				HttpConnection conn = (HttpConnection)Connector.open(MAIN_URL);
				
				try{
					conn.setRequestMethod(HttpConnection.POST);
					conn.setRequestProperty(HttpProtocolConstants.HEADER_CONTENT_LENGTH,String.valueOf(m_postData.length));
					
					conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
					conn.setRequestProperty("User-Agent","Profile/MIDP-2.0 Configuration/CLDC-1.0");

					OutputStream out = conn.openOutputStream();
					try{
						out.write(m_postData);
						out.flush();
					}finally{
						out.close();
						out = null;
					}
					
					int rc = conn.getResponseCode();
				    if(rc != HttpConnection.HTTP_OK){
				    	throw new IOException("HTTP response code: " + rc);
				    }
				    
				    InputStream in = conn.openInputStream();
				    try{
				    	int length = (int) conn.getLength();
				    	String result;
				    	if (length != -1){
				    		byte servletData[] = new byte[length];
				    		in.read(servletData);
				    		result = new String(servletData);
				    	}else{
				    		ByteArrayOutputStream os = new ByteArrayOutputStream();
				    		int ch;
					        while ((ch = in.read()) != -1){
					        	os.write(ch);
					        }
					        result = new String(os.toByteArray(),"UTF-8");
				    	}
				    	
				    	int idx;
				    	
				    	if(result.startsWith("<Error>")){
				    		m_mainApp.DialogAlert(result.substring(7));
				    	}else if((idx = result.indexOf("|")) != -1){
				    		m_mainApp.m_hostname 	= result.substring(0,idx);
				    		m_mainApp.m_port 		= Integer.parseInt(result.substring(idx + 1));
				    		
				    		succ();
				    		
				    	}else{
				    		m_mainApp.DialogAlert("Unknow Error: "+result); 
				    	}

				    }finally{
				    	in.close();
				    	in = null;
				    }

				}finally{
					conn.close();
					conn = null;
				}
								
			}catch(Exception e){
				m_mainApp.SetErrorString("LAT:", e);
			}finally{
				m_mainApp.invokeLater(new Runnable() {
					public void run() {
						m_waitDlg.close();
						m_loginAccThread = null;
					}
				});
			}
		}
		
		private void succ(){
			m_mainApp.invokeLater(new Runnable() {
				public void run() {
				
					if(m_loginAccThread != null){

						showAccMainManager(true);
						
						m_loginAccThread = null;
					}
				}
			});
		}
	}
    
}
    
    
