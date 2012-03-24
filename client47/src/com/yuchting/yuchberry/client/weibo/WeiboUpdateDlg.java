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
package com.yuchting.yuchberry.client.weibo;


import local.yblocalResource;
import net.rim.blackberry.api.invoke.CameraArguments;
import net.rim.blackberry.api.invoke.Invoke;
import net.rim.device.api.system.Characters;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.system.Display;
import net.rim.device.api.system.KeypadListener;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.UiEngine;
import net.rim.device.api.ui.component.AutoTextEditField;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.yuchting.yuchberry.client.recvMain;
import com.yuchting.yuchberry.client.screen.CameraScreen;
import com.yuchting.yuchberry.client.screen.ICameraScreenCallback;
import com.yuchting.yuchberry.client.screen.IUploadFileScreenCallback;
import com.yuchting.yuchberry.client.screen.imageViewScreen;
import com.yuchting.yuchberry.client.screen.uploadFileScreen;
import com.yuchting.yuchberry.client.ui.BubbleImage;
import com.yuchting.yuchberry.client.ui.CameraFileOP;
import com.yuchting.yuchberry.client.ui.ImageButton;
import com.yuchting.yuchberry.client.ui.ImageSets;
import com.yuchting.yuchberry.client.ui.ImageUnit;
import com.yuchting.yuchberry.client.ui.PhizSelectedScreen;

final class WeiboUpdateManager extends Manager implements FieldChangeListener{
	
	public int			m_updateDlgHeaderHeight = 32;
	
	public int			m_width 	= Display.getWidth() - 20;
	public int			m_height 	= (Display.getHeight() - 30 > 300?300:(Display.getHeight() - 30));
	
	public AutoTextEditField 	m_editTextArea	= new AutoTextEditField();
	
	weiboTimeLineScreen		m_timelineScreen;
	int						m_titleHeight 	= 0;
	int						m_separateLine_y = 0;

    ImageButton				m_phizButton = null;
    ImageButton				m_photoButton = null;
    ImageButton				m_attachButton = null;
    ImageButton				m_locationButton = null;
    
	ImageButton				m_sendButton	= null;
	
	ImageUnit				m_updateTitle		= null;
	BubbleImage				m_editBubbleImage = null;
	
	boolean 				m_addLocation = false;
	
	// because the Message application will reload some method of this WeiboUpdateDlg
	// the WeiboItemField.static variables will be called/reloaded by Message application(another process) again...
	//
	ImageSets				m_weiboUIImageSets = null;
	int						m_weiboCommentFGColor = 0;
		
	public VerticalFieldManager m_editTextManager = new VerticalFieldManager(Manager.VERTICAL_SCROLL){
		
		public int getPreferredHeight(){
			return m_height - m_titleHeight - m_sendButton.getImageHeight() - 6;
		}
		
		public int getPreferredWidth(){
			return m_width - 6;
		}
		
		protected void sublayout(int width, int height){
			
			setPositionChild(m_editTextArea,0,0);
			layoutChild(m_editTextArea,getPreferredWidth(),999);
			
			setExtent(getPreferredWidth(),getPreferredHeight());
		}
		
		protected void subpaint(Graphics _g){
			int t_num = getFieldCount();
			for(int i = 0;i < t_num;i++){
				Field field = getField(i);
				
				if(field == m_editTextArea){
					int t_color = _g.getColor();
					try{
						_g.setColor(m_weiboCommentFGColor);
						paintChild(_g, field);
					}finally{
						_g.setColor(t_color);
					}
				}else{
					paintChild(_g, field);
				}
			}
		}
	};
	
	public WeiboUpdateManager(weiboTimeLineScreen _timeline){
		super(Manager.VERTICAL_SCROLL);
						
		m_weiboUIImageSets = recvMain.sm_weiboUIImage;
		m_weiboCommentFGColor = WeiboItemField.fsm_weiboCommentFGColor;
				
		if(recvMain.GetClientLanguage() == 0){
			m_sendButton = new ImageButton(recvMain.sm_local.getString(yblocalResource.WEIBO_SEND_LABEL),
					m_weiboUIImageSets.getImageUnit("update_button"),
					m_weiboUIImageSets.getImageUnit("update_button_focus"),
					m_weiboUIImageSets,
					Field.FIELD_RIGHT);
		}else{
			m_sendButton = new ImageButton(recvMain.sm_local.getString(yblocalResource.WEIBO_SEND_LABEL),
					m_weiboUIImageSets.getImageUnit("update_button_en"),
					m_weiboUIImageSets.getImageUnit("update_button_focus_en"),
					m_weiboUIImageSets,
					Field.FIELD_RIGHT);
		}
		
		m_phizButton = new ImageButton("phiz",
					m_weiboUIImageSets.getImageUnit("phiz_button"),
					m_weiboUIImageSets.getImageUnit("phiz_button_focus"),
					m_weiboUIImageSets,Field.FIELD_LEFT);
		
		m_photoButton = new ImageButton("photo",
				m_weiboUIImageSets.getImageUnit("photo_button"),
				m_weiboUIImageSets.getImageUnit("photo_button_focus"),
				m_weiboUIImageSets,Field.FIELD_LEFT);
		
		m_attachButton = new ImageButton("attachment",
				m_weiboUIImageSets.getImageUnit("attach_button"),
				m_weiboUIImageSets.getImageUnit("attach_button_focus"),
				m_weiboUIImageSets,Field.FIELD_LEFT);
		
		m_locationButton = new ImageButton("location",
				m_weiboUIImageSets.getImageUnit("location_button"),
				m_weiboUIImageSets.getImageUnit("location_button_focus"),
				m_weiboUIImageSets,Field.FIELD_LEFT);
		
		if(recvMain.sm_standardUI){
			m_updateTitle = m_weiboUIImageSets.getImageUnit("compose_nav_bar");
		}else{
			m_updateTitle = m_weiboUIImageSets.getImageUnit("nav_bar");
		}
		
		
		m_editBubbleImage = WeiboItemField.sm_bubbleImage;
		m_timelineScreen = _timeline;

		m_editTextArea.setMaxSize(WeiboItemField.fsm_maxWeiboTextLength);
		
		m_phizButton.setChangeListener(this);
		m_photoButton.setChangeListener(this);
		m_attachButton.setChangeListener(this);
		m_sendButton.setChangeListener(this);
		m_editTextArea.setChangeListener(this);
		m_locationButton.setChangeListener(this);
		
		m_editTextManager.add(m_editTextArea);
		
		add(m_editTextManager);
		
		add(m_phizButton);
		add(m_photoButton);
		add(m_attachButton);
		add(m_locationButton);
		
		add(m_sendButton);		
				
		m_titleHeight = m_updateDlgHeaderHeight + 2;
		m_separateLine_y = m_titleHeight + m_editTextManager.getPreferredHeight();
	}
	
	public int getPreferredHeight(){
		return m_height;
	}
	
	public int getPreferredWidth(){
		return m_width;
	}
	
	public void fieldChanged(Field field, int context) {
		if(field == m_sendButton){
			m_editTextArea.setFocus();
			sendUpdate();			
		}else if(field == m_editTextArea){
			
			// refresh the input number title text
			//
			getScreen().invalidate();
		}else if(field == m_phizButton){
			m_timelineScreen.m_currUpdateDlg.m_phizItem.run();
		}else if(field == m_photoButton){
			if(recvMain.fsm_snapshotAvailible){
				m_timelineScreen.m_currUpdateDlg.m_snapItem.run();
			}else{
				m_timelineScreen.m_currUpdateDlg.m_cameraItem.run();
			}
			
		}else if(field == m_attachButton){
			m_timelineScreen.m_currUpdateDlg.m_attachItem.run();
		}else if(field == m_locationButton){
			m_timelineScreen.m_currUpdateDlg.m_locationItem.run();
		}
	}
	
	public void sublayout(int width, int height){
		
		int t_buttons_line = (m_editTextManager.getPreferredHeight() + m_titleHeight);
		
		setPositionChild(m_editTextManager,2,m_titleHeight + 2);
		layoutChild(m_editTextManager,m_editTextManager.getPreferredWidth(),m_editTextManager.getPreferredHeight());
			
		int t_button_x = m_timelineScreen.m_currUpdateDlg.m_hasImageSign.getWidth() + 2;
		int t_button_y = t_buttons_line + (getPreferredHeight() - t_buttons_line - m_phizButton.getImageHeight()) / 2;
		
		setPositionChild(m_phizButton,t_button_x,t_button_y);
		layoutChild(m_phizButton,m_phizButton.getImageWidth(),m_phizButton.getImageHeight());
		
		t_button_x += m_phizButton.getWidth() + 2;
		
		setPositionChild(m_photoButton,t_button_x,t_button_y);
		layoutChild(m_photoButton, m_photoButton.getImageWidth(),m_photoButton.getImageHeight());
		
		t_button_x += m_photoButton.getWidth() + 2;
		
		setPositionChild(m_attachButton,t_button_x,t_button_y);
		layoutChild(m_attachButton, m_attachButton.getImageWidth(),m_attachButton.getImageHeight());		
		
		t_button_x += m_attachButton.getWidth() + 2;
		
		setPositionChild(m_locationButton,t_button_x,t_button_y);
		layoutChild(m_locationButton, m_locationButton.getImageWidth(),m_locationButton.getImageHeight());
		
		// layout the send update button
		//
		int t_buttonWidth = m_sendButton.getImageWidth();
		int t_buttonHeight = m_sendButton.getImageHeight();
		
		t_button_y = t_buttons_line + (getPreferredHeight() - t_buttons_line - t_buttonHeight) / 2;
		
		setPositionChild(m_sendButton,getPreferredWidth() - t_buttonWidth - 3,t_button_y);
		layoutChild(m_sendButton,t_buttonWidth,t_buttonHeight);
		
		setExtent(getPreferredWidth(), getPreferredHeight());
	}
		
	public void sendUpdate(){
		if(m_editTextArea.getText().length() != 0){
			
			byte[] t_content = null;
			
			try{
				t_content = CameraFileOP.resizePicFile(m_timelineScreen.m_currUpdateDlg.m_imagePath,
						m_timelineScreen.m_mainApp.getWeiboUploadSize());
			}catch(Exception e){
				m_timelineScreen.m_mainApp.DialogAlert("camera file process error:"+ e.getMessage()+ e.getClass());
				m_timelineScreen.m_mainApp.SetErrorString("su:"+ e.getMessage()+ e.getClass());
			}
						
			if(t_content == null){
				t_content = m_timelineScreen.m_currUpdateDlg.m_snapBuffer;				
			}
			
			m_timelineScreen.UpdateNewWeibo(m_editTextArea.getText(),
					t_content,m_timelineScreen.m_currUpdateDlg.m_imageType,
					m_addLocation);
			
			m_editTextArea.setText("");
			
			m_timelineScreen.m_currUpdateDlg.m_imagePath = null;
			m_timelineScreen.m_currUpdateDlg.close();
		}
	}
	
	public void subpaint(Graphics _g){
		
		m_weiboUIImageSets.drawBitmapLine(_g, m_updateTitle, 1, 1, 
				getPreferredWidth() - 2,m_updateDlgHeaderHeight);
		
		m_editBubbleImage.draw(_g, 1, m_titleHeight,getPreferredWidth() - 3,
				m_editTextManager.getPreferredHeight() + 2,BubbleImage.NO_POINT_STYLE);
		
		int oldColor = _g.getColor();
		Font oldFont = _g.getFont();
				
		try{
			Font t_boldFont = oldFont.derive(oldFont.getStyle() | Font.BOLD);
			
			_g.setFont(t_boldFont);
			_g.setColor(0xffffff);
			String t_str = recvMain.sm_local.getString(yblocalResource.WEIBO_UPDATE_DIALOG_TITLE) 
				+ " (" + m_editTextArea.getText().length() + ")";
			
			_g.drawText(t_str,(getPreferredWidth() - t_boldFont.getAdvance(t_str)) / 2,
					(m_titleHeight - oldFont.getHeight()) / 2 + 2);
			
		}finally{
			_g.setColor(oldColor);
			_g.setFont(oldFont);
		}
		
		super.subpaint(_g);
		
	}
	
	public boolean keyChar(char c,int status,int time){
				
		if(c == Characters.ESCAPE){
			getScreen().close();
		}else if(c == Characters.ENTER){
			if(m_editTextArea.getText().length() != 0 &&((status & KeypadListener.STATUS_SHIFT) != 0)){
				sendUpdate();					
			}
			// consum the Enter key
			//
			return true;
		}else if(c == ' '){
			
			if((status & KeypadListener.STATUS_SHIFT_LEFT) != 0){
				((WeiboUpdateDlg)getScreen()).m_phizItem.run();
				return true;
			}
		}
		
		invalidate();
		getScreen().invalidate();
				
		return super.keyChar(c,status,time);
	}
}

public class WeiboUpdateDlg extends Screen implements IUploadFileScreenCallback{
	
	int m_menuIndex_op = 0;
	
	MenuItem m_sendItem = new MenuItem(recvMain.sm_local.getString(yblocalResource.WEIBO_SEND_LABEL),m_menuIndex_op++,0){
        public void run() {
        	m_updateManager.sendUpdate();
        }
    };
        
    MenuItem m_phizItem = new MenuItem(recvMain.sm_local.getString(yblocalResource.WEIBO_PHIZ_LABEL),m_menuIndex_op++,0){
        public void run() {
        	m_phizScreen.preparePhizScreen(m_updateManager.m_editTextArea);
        	UiApplication.getUiApplication().pushScreen(m_phizScreen);
        }
    };
    
    MenuItem m_locationItem = new MenuItem(recvMain.sm_local.getString(yblocalResource.WEIBO_LOCATION_LABEL),m_menuIndex_op++,0){
        public void run() {
        	//TODO add logical for location process
        	if(!m_mainApp.getGPSInfo().isValidLocation()){
        		m_mainApp.DialogAlert(recvMain.sm_local.getString(yblocalResource.WEIBO_NEED_GPS_PROMPT));
        		return ;
        	}
        	
        	m_updateManager.m_addLocation = !m_updateManager.m_addLocation;
        	
        	invalidate();
        }
    };
    
    MenuItem m_snapItem = new MenuItem(recvMain.sm_local.getString(yblocalResource.WEIBO_OPEN_CAMERA_SNAP),m_menuIndex_op++,0){
    	public void run(){
    		try{
    			m_cameraScreen = new CameraScreen(new ICameraScreenCallback(){
        			public void snapOK(byte[] _buffer){
        				m_snapBuffer = _buffer;
        				m_imageType	= fetchWeibo.IMAGE_TYPE_JPG;
        				
        				m_imagePath = null;
        				
        				invalidate();
        			}
        		},m_mainApp.getWeiboUploadSize().x);
        		
        		m_mainApp.pushScreen(m_cameraScreen);	
    		}catch(Exception e){
    			
    			m_mainApp.SetErrorString("WUDS:" + e.getMessage());
    			m_cameraItem.run();
    		}    		
    	}
    };
    
    MenuItem m_cameraItem = new MenuItem(recvMain.sm_local.getString(yblocalResource.WEIBO_OPEN_CAMERA),m_menuIndex_op++,0){
    	public void run(){
    		Invoke.invokeApplication(Invoke.APP_TYPE_CAMERA, new CameraArguments());
    	}
    };
    
    MenuItem m_attachItem = new MenuItem(recvMain.sm_local.getString(yblocalResource.WEIBO_ADD_ATTACH_MENU_LABEL),m_menuIndex_op++,0){
    	public void run(){
    		try{
    			UiApplication.getUiApplication().pushScreen(new uploadFileScreen(m_mainApp,false,WeiboUpdateDlg.this));
    		}catch(Exception e){
    			m_mainApp.SetErrorString("WAI:"+e.getMessage()+e.getClass().getName());
    		}
    		
    	}
    };
    
    MenuItem m_checkPic		= new MenuItem(recvMain.sm_local.getString(yblocalResource.WEIBO_CHECK_UPLOADING_IMAGE),m_menuIndex_op++,0){
    	public void run(){
    		try{
    			if(m_imagePath != null){
    				if(!m_mainApp.CheckMediaNativeApps(m_imagePath)){
    					UiApplication.getUiApplication().pushGlobalScreen(new imageViewScreen(m_imagePath),0,UiEngine.GLOBAL_MODAL);
    				}        			
        		}else{
        			UiApplication.getUiApplication().pushGlobalScreen(new imageViewScreen(m_snapBuffer),0,UiEngine.GLOBAL_MODAL);
        		}	
    		}catch(Exception e){
    			m_mainApp.SetErrorString("WCP:"+e.getMessage()+e.getClass().getName());
    		}
    		    		
    	}
    };
    
    MenuItem m_deletePic	= new MenuItem(recvMain.sm_local.getString(yblocalResource.WEIBO_DELETE_PIC_MENU_LABEL),m_menuIndex_op++,0){
    	public void run(){
    		clearAttachment();
    	}
    };
    
    MenuItem m_weiboAccount	= new MenuItem(recvMain.sm_local.getString(yblocalResource.WEIBO_WEIBO_ACCOUNT_MENU_LABEL),m_menuIndex_op++,0){
    	public void run(){
    		m_mainApp.m_weiboTimeLineScreen.m_optionItem.run();
    	}
    };
    		
	WeiboUpdateManager		m_updateManager = null;
	CameraScreen			m_cameraScreen = null;
	
	// because the Message application will reload some method of this WeiboUpdateDlg
	// the WeiboItemField.static variables will be called/reloaded by Message application(another process) again...
	//
	int						m_backgroundColor;
	boolean	 			m_snapshotAvaiable;
	ImageSets				m_weiboUIImageSets = null;
	PhizSelectedScreen		m_phizScreen		= null;
		
	String					m_imagePath = null;
	int						m_imageType = 0;
	
	byte[]					m_snapBuffer = null;
	
	recvMain				m_mainApp	= null;
	
	ImageUnit				m_hasImageSign	= null;
	ImageUnit				m_hasLocation = null;
	
	CameraFileOP			m_fileSystem = new CameraFileOP() {
		
		public void onAddUploadingPic(String _file, int _type) {
			
			m_imagePath = _file;
			m_snapBuffer = null;
			m_imageType = _type;
			
			invalidate();
		}
		
		public boolean canAdded(){
			return m_imagePath == null;
		}
	};
	
	public WeiboUpdateDlg(weiboTimeLineScreen _screen){
		super(new WeiboUpdateManager(_screen),Screen.DEFAULT_MENU | Manager.NO_VERTICAL_SCROLL);
		m_updateManager = (WeiboUpdateManager)getDelegate();
		
		m_phizScreen = PhizSelectedScreen.getPhizScreen(m_updateManager.m_editTextArea);
		m_weiboUIImageSets = recvMain.sm_weiboUIImage;
		
		m_backgroundColor = WeiboItemField.fsm_extendBGColor;
		m_snapshotAvaiable = recvMain.fsm_snapshotAvailible;
		
		m_mainApp = _screen.m_mainApp;
		m_hasImageSign	= m_weiboUIImageSets.getImageUnit("picSign");
		m_hasLocation	= m_weiboUIImageSets.getImageUnit("locationSign");
	}
	
	public void clearAttachment(){
		m_imagePath = null;
		m_snapBuffer = null;
		
		invalidate();
	}
	
	public int getPreferredHeight(){
		return m_updateManager.m_height;
	}
	
	public int getPreferredWidth(){
		return m_updateManager.m_width;
	}
	
	protected void sublayout(int width, int height){
		
		int t_width = m_updateManager.getPreferredWidth();
		int t_height= m_updateManager.getPreferredHeight();
		
		m_updateManager.sublayout(t_width, t_height);
		
		t_width = getPreferredWidth();
		t_height = getPreferredHeight();
		
		setExtent(t_width, t_height);
		
		setPosition((Display.getWidth() - t_width) / 2 ,(Display.getHeight() - t_height) / 2);
		
	}
	
	protected void makeMenu(Menu _menu,int instance){
		_menu.add(m_sendItem);
		_menu.add(m_phizItem);
		_menu.add(m_locationItem);		
		
		if(DeviceInfo.hasCamera()){
			if(m_snapshotAvaiable){
				_menu.add(m_snapItem);
			}			
			_menu.add(m_cameraItem);
		}
		
		_menu.add(m_attachItem);
		if(m_imagePath != null || m_snapBuffer != null){
			_menu.add(m_deletePic);
			_menu.add(m_checkPic);
		}
		
		_menu.add(m_weiboAccount);
		
		_menu.add(MenuItem.separator(m_menuIndex_op));
		
		super.makeMenu(_menu,instance);
	}
	
	public boolean clickOK(String _filename,int _size){
		if(!m_fileSystem.addUploadingPic(_filename)){
			return false;
		}
				
		return true;
	}
	
	public void clickDel(String _filename){
		clearAttachment();
	}
	
	protected void onDisplay(){
		super.onDisplay();
		
		m_updateManager.setVerticalScroll(0);
		
		if(m_updateManager.m_editTextArea.getTextLength() != 0){
			m_updateManager.m_editTextArea.setCursorPosition(m_updateManager.m_editTextArea.getTextLength());
		}
		m_updateManager.m_editTextArea.setFocus();
		
		m_mainApp.addFileSystemJournalListener(m_fileSystem);
	}
	
	protected void paint(Graphics _g){		

		int color = _g.getColor();
		try{

			_g.setColor(m_backgroundColor);
			_g.fillRect(0,0,getPreferredWidth(),getPreferredHeight());
						
			_g.setColor(0);
			_g.drawRect(0,0,getPreferredWidth(),getPreferredHeight());
			
		}finally{
			_g.setColor(color);
		}
		
		m_updateManager.subpaint(_g);
		
		if(m_imagePath != null || m_snapBuffer != null){
			m_weiboUIImageSets.drawImage(_g, m_hasImageSign,
					2,getPreferredHeight() - m_hasImageSign.getHeight() - 10 );
		}
		
		if(m_updateManager.m_addLocation){
			m_weiboUIImageSets.drawImage(_g, m_hasLocation,2,1);
		}
	}
	
	public void close(){
		
		m_updateManager.m_timelineScreen.m_pushUpdateDlg = false;
		
		super.close();
	}		
}
