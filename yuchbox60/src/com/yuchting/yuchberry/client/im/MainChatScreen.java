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
package com.yuchting.yuchberry.client.im;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import local.yblocalResource;
import net.rim.blackberry.api.invoke.CameraArguments;
import net.rim.blackberry.api.invoke.Invoke;
import net.rim.device.api.system.Backlight;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.system.KeypadListener;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.UiEngine;
import net.rim.device.api.ui.component.AutoTextEditField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.yuchting.yuchberry.client.ObjectAllocator;
import com.yuchting.yuchberry.client.msg_head;
import com.yuchting.yuchberry.client.recvMain;
import com.yuchting.yuchberry.client.sendReceive;
import com.yuchting.yuchberry.client.im.MainIMScreen.RosterChatData;
import com.yuchting.yuchberry.client.screen.CameraScreen;
import com.yuchting.yuchberry.client.screen.ICameraScreenCallback;
import com.yuchting.yuchberry.client.screen.IRecordAudioScreenCallback;
import com.yuchting.yuchberry.client.screen.IUploadFileScreenCallback;
import com.yuchting.yuchberry.client.screen.PhizSelectedScreen;
import com.yuchting.yuchberry.client.screen.RecordAudioScreen;
import com.yuchting.yuchberry.client.screen.imageViewScreen;
import com.yuchting.yuchberry.client.screen.uploadFileScreen;
import com.yuchting.yuchberry.client.ui.BubbleImage;
import com.yuchting.yuchberry.client.ui.CameraFileOP;
import com.yuchting.yuchberry.client.ui.ImageButton;
import com.yuchting.yuchberry.client.ui.ImageUnit;
import com.yuchting.yuchberry.client.ui.WeiboHeadImage;

final class InputManager extends Manager implements FieldChangeListener{
	
	public final static int fsm_inputBubbleBorder = 4;
	public final static int fsm_textBorder = 2;
	
	public final static int fsm_minHeight = MainIMScreen.fsm_defaultFontHeight + (fsm_textBorder + fsm_inputBubbleBorder) * 2;
	public final static int fsm_maxHeight = recvMain.fsm_display_height / 2;
	
	MiddleMgr		m_middleMgr	= null;
	ImageButton		m_phizButton = new ImageButton("",
												recvMain.sm_weiboUIImage.getImageUnit("input_phiz"),
												recvMain.sm_weiboUIImage.getImageUnit("input_phiz"),
												recvMain.sm_weiboUIImage){
		
		ImageUnit	m_selected = recvMain.sm_weiboUIImage.getImageUnit("nav_bar_block");
		public int getImageWidth(){
	    	return fsm_minHeight;
	    }
	    
	    public int getImageHeight(){
	    	return fsm_minHeight;
	    }
	    
	    protected void focusPaint(Graphics g,boolean focus){
	    	if(focus){
	    		recvMain.sm_weiboUIImage.drawBitmapLine(g, m_selected, 
	    				sm_split_line.getWidth(), 0, getImageHeight());
	    	}
	    	
	    	super.focusPaint(g,focus);
	    }
	};
	
	int					m_textWidth	= 0;
	
	public static BubbleImage	sm_inputBackground 	= new BubbleImage(
														recvMain.sm_weiboUIImage.getImageUnit("input_top_left"),
														recvMain.sm_weiboUIImage.getImageUnit("input_top"),
														recvMain.sm_weiboUIImage.getImageUnit("input_top_right"),
														recvMain.sm_weiboUIImage.getImageUnit("input_right"),
														
														recvMain.sm_weiboUIImage.getImageUnit("input_bottom_right"),
														recvMain.sm_weiboUIImage.getImageUnit("input_bottom"),
														recvMain.sm_weiboUIImage.getImageUnit("input_bottom_left"),
														recvMain.sm_weiboUIImage.getImageUnit("input_left"),
														
														recvMain.sm_weiboUIImage.getImageUnit("input_inner_block"),
														new ImageUnit[]{
															recvMain.sm_weiboUIImage.getImageUnit("bubble_left_point"),
															recvMain.sm_weiboUIImage.getImageUnit("bubble_top_point"),
															recvMain.sm_weiboUIImage.getImageUnit("bubble_right_point"),
															recvMain.sm_weiboUIImage.getImageUnit("bubble_bottom_point"),
														},
														recvMain.sm_weiboUIImage);
	
	public static ImageUnit	sm_background		= recvMain.sm_weiboUIImage.getImageUnit("nav_bar");
	public static ImageUnit	sm_split_line 		= recvMain.sm_weiboUIImage.getImageUnit("nav_bar_seg");
	
	int					m_currHeight	= fsm_minHeight;
	
	int					m_inputInvokeID	= -1;
	long				m_inputTimer	= System.currentTimeMillis();
	
	VerticalFieldManager m_inputManager = new VerticalFieldManager(Manager.VERTICAL_SCROLL){
		public int getPreferredWidth(){
			return m_textWidth;
		}
		public int getPreferredHeight(){
			return InputManager.this.getPreferredHeight() - (fsm_textBorder + fsm_inputBubbleBorder) * 2;
		}
		
		protected void sublayout(int _width,int _height){
			super.sublayout(this.getPreferredWidth(),this.getPreferredHeight());
			setExtent(this.getPreferredWidth(),this.getPreferredHeight());
		}
	};
	
	public AutoTextEditField 	m_editTextArea			= new AutoTextEditField(){
		public void setText(String _text){
			super.setText(_text);
			this.layout(m_textWidth,1000);
		}
		
		public int getPreferredWidth(){
			return m_textWidth;
		}
		
		protected boolean keyDown(int keycode,int time){
			this.layout(m_textWidth,1000);
			return super.keyDown(keycode,time);
		}
		
		protected void onUnfocus(){
			super.onUnfocus();
			// refresh all InputManager area to refresh prompt of [press R key]
			// in InputManager::subpaint
			//
			InputManager.this.invalidate();
		}
		
		protected void onFocus(int direction){
			super.onFocus(direction);
			// refresh all InputManager area to refresh prompt of [press R key]  
			// in InputManager::subpaint
			//
			InputManager.this.invalidate();
		}
	};
	
	public void enableVoiceMode(boolean _enable){
		if(_enable){
			m_editTextArea.setText(recvMain.sm_local.getString(yblocalResource.IM_VOICE_MODE_PROMPT));			
			m_editTextArea.setEditable(false);
		}else{
			m_editTextArea.setEditable(true);
			m_editTextArea.setText(m_middleMgr.m_chatScreen.getCurrRoster().m_lastChatText);
		}
	}
	
	public InputManager(MiddleMgr _mainScreen){
		super(Manager.NO_VERTICAL_SCROLL);
		
		m_middleMgr	= _mainScreen;		
		
		m_phizButton.setChangeListener(this);
		
		m_textWidth = getPreferredWidth() - m_phizButton.getImageWidth() - (fsm_textBorder + fsm_inputBubbleBorder)* 2;
				
		m_editTextArea.setChangeListener(this);
		m_inputManager.add(m_editTextArea);
		add(m_inputManager);
		add(m_phizButton);
	}
		
	public static void drawInputBackground(Graphics _g,int _textWidth,int _preferredWidth,int _preferredHeight){
		
		recvMain.sm_weiboUIImage.drawBitmapLine(_g, sm_background, 0, 0, _preferredWidth);
		
		if(_preferredHeight > sm_background.getHeight()){
			int t_color = _g.getColor();
			try{
				_g.setColor(0);
				_g.fillRect(0, sm_background.getHeight(), 
						_preferredWidth, _preferredHeight - sm_background.getHeight());
			}finally{
				_g.setColor(t_color);
			}
		}
		
		sm_inputBackground.draw(_g, fsm_inputBubbleBorder, fsm_inputBubbleBorder, 
								_textWidth + fsm_textBorder * 2,
								_preferredHeight - fsm_inputBubbleBorder * 2,
								BubbleImage.NO_POINT_STYLE);
	}
	
	public int getPreferredWidth(){
		return recvMain.fsm_display_width;
	}
	
	public int getPreferredHeight(){		
		return m_currHeight;
	}
	
	protected void sublayout(int _width,int _height){	
		
		setPositionChild(m_inputManager,fsm_textBorder + fsm_inputBubbleBorder,fsm_textBorder + fsm_inputBubbleBorder);		
		layoutChild(m_inputManager,m_inputManager.getPreferredWidth(),m_inputManager.getPreferredHeight());
		
		setPositionChild(m_phizButton,getPreferredWidth() - m_phizButton.getImageWidth(),
							(getPreferredHeight() - m_phizButton.getImageHeight()) /2);
		
		layoutChild(m_phizButton,m_phizButton.getImageWidth(),m_phizButton.getImageHeight());
				
		setExtent(getPreferredWidth(), getPreferredHeight());
	}
	
	protected void subpaint(Graphics _g){
		
		drawInputBackground(_g,m_textWidth,getPreferredWidth(),getPreferredHeight());
		
		super.subpaint(_g);
		
		int x = recvMain.fsm_display_width - m_phizButton.getImageWidth();
		int y = 1;
		
		recvMain.sm_weiboUIImage.drawBitmapLine_vert(_g, sm_split_line, x, y,getPreferredHeight());
		
		if(!m_editTextArea.isFocus() && m_editTextArea.getTextLength() == 0){
			// draw the R key prompt
			//
			int color = _g.getColor();
			try{
				_g.setColor(0x888888);
				_g.drawText(recvMain.sm_local.getString(yblocalResource.IM_CHAT_INPUT_PROMPT), fsm_inputBubbleBorder + 1, fsm_inputBubbleBorder + 1);				
			}finally{
				_g.setColor(color);
			}
			
		}
	}
	
	public void fieldChanged(Field field, int context) {
				
		if(field == m_editTextArea){
			
			if(!m_middleMgr.m_chatScreen.m_mainApp.m_imVoiceImmMode){
				m_middleMgr.m_chatScreen.getCurrRoster().m_lastChatText = m_editTextArea.getText();
			}	
			int t_formerHeight = m_currHeight;
			m_currHeight = m_editTextArea.getHeight() + (fsm_textBorder + fsm_inputBubbleBorder) * 2;
			
			if(m_currHeight < fsm_minHeight){
				m_currHeight = fsm_minHeight;
			}
			
			if(m_currHeight > fsm_maxHeight){
				m_currHeight = fsm_maxHeight;
			}
			
			if(Math.abs(t_formerHeight - m_currHeight) >= 5){
				m_middleMgr.invalidate();
				m_middleMgr.sublayout(0, 0);
			}	
			
			if(m_middleMgr.m_chatScreen.m_mainApp.m_enableChatState
				&& context != FieldChangeListener.PROGRAMMATIC){
				
				synchronized (this) {
					if(m_inputInvokeID == -1){
						
						m_inputTimer = 0;
						
						m_inputInvokeID = m_middleMgr.m_chatScreen.m_mainApp.invokeLater(new Runnable() {
							public void run() {
								
								synchronized (InputManager.this) {
									
									if(++m_inputTimer > 5){
										m_middleMgr.m_chatScreen.m_mainApp.cancelInvokeLater(m_inputInvokeID);
										m_inputInvokeID = -1;
										
										m_middleMgr.m_chatScreen.sendChatComposeState(fetchChatMsg.CHAT_STATE_COMMON);
									}
								}								
							}
						}, 5000, true);
						
						m_middleMgr.m_chatScreen.sendChatComposeState(fetchChatMsg.CHAT_STATE_COMPOSING);
					}
				}
			}
							
		}else if(field == m_phizButton){
			UiApplication.getUiApplication().pushScreen(
					PhizSelectedScreen.getPhizScreen(m_middleMgr.m_chatScreen.m_mainApp,m_editTextArea));
		}
	}	
		
	protected boolean keyDown(int keycode,int time){
		
		if(m_middleMgr.m_chatScreen.getCurrRoster().m_isYuch
		&& m_middleMgr.m_chatScreen.m_mainApp.m_enableChatChecked){
			// send the read message to the server
			//	
			Vector list = m_middleMgr.m_chatScreen.getCurrRoster().m_chatMsgList;
			int num = list.size();
			for(int i = 0 ;i < num;i++){
				fetchChatMsg msg = (fetchChatMsg)list.elementAt(i);
				if(!msg.isOwnMsg() && !msg.hasSendMsgChatReadMsg()){
					m_middleMgr.m_chatScreen.m_mainScreen.sendChatReadMsg(msg);
				}			
			}
		}
		
		int key = Keypad.key(keycode);
		
		if(m_middleMgr.m_chatScreen.m_mainApp.m_imVoiceImmMode){
			if(key == ' '){
				m_middleMgr.m_chatScreen.m_recordMenu.run();
				return true;
			}
		}
		
		
		return super.keyDown(keycode,time);
	}
	
	protected boolean keyChar(char c,int status,int time){
		
		if(m_middleMgr.m_chatScreen.m_mainApp.m_imVoiceImmMode){
			return true;
		}else{
			
			if(c == 10){
				
				boolean t_shiftDown = (status & KeypadListener.STATUS_SHIFT) != 0;
				boolean t_returnSend = m_middleMgr.m_chatScreen.m_mainApp.m_imReturnSend;
				
	    		if((m_editTextArea.getText().length() != 0 
	    			|| m_middleMgr.m_chatScreen.m_imagePath != null 
	    			|| m_middleMgr.m_chatScreen.m_snapBuffer != null
	    			|| m_middleMgr.m_chatScreen.m_recordBuffer != null)
	    		&& ( (t_returnSend && !t_shiftDown) || (!t_returnSend && t_shiftDown))){
	    			
	    			send();
	    			
	    			return true;
	    			
	    		}else{
	    			if(!m_middleMgr.m_chatScreen.m_mainApp.m_imChatScreenReceiveReturn
	    				&& m_editTextArea.isFocus()){
	    				return true;
	    			}
	    		}
			}else if(c ==' '){
				
				if((status & KeypadListener.STATUS_ALT) != 0){
					m_middleMgr.m_chatScreen.m_recordMenu.run();
					return true;
					
				}else if((status & KeypadListener.STATUS_SHIFT_LEFT) != 0){
										
					m_middleMgr.m_chatScreen.m_phizMenu.run();
					return true;					
				}
			}
		}

		return super.keyChar(c,status,time);
	}
	
	public void send(){
		String text = m_editTextArea.getText();
		
		if(text.length() != 0 
		|| m_middleMgr.m_chatScreen.m_imagePath != null 
    	|| m_middleMgr.m_chatScreen.m_snapBuffer != null
    	|| m_middleMgr.m_chatScreen.m_recordBuffer != null){
			
			m_middleMgr.m_chatScreen.sendChatMsg(text);
			m_editTextArea.setText("");
			
			cancelComposeTimer();
		}		
	}
	
	public void cancelComposeTimer(){
		// cancel the compose timer
		//
		if(m_inputInvokeID != -1){
			synchronized (this) {
				m_middleMgr.m_chatScreen.m_mainApp.cancelInvokeLater(m_inputInvokeID);
				m_inputInvokeID = -1;
				m_inputTimer = 0;
			}
		}
	}
}

final class MiddleMgr extends VerticalFieldManager{
	
	public final static int	fsm_linespace	= 6;
	VerticalFieldManager	m_chatMsgMgr = null;
	
	VerticalFieldManager	m_chatMsgMiddleMgr = new VerticalFieldManager(Manager.VERTICAL_SCROLL){
		
		public int getPreferredHeight(){
			return MiddleMgr.this.getPreferredHeight() - m_inputMgr.getPreferredHeight();
		}
		
		public int getPreferredWidth(){
			return recvMain.fsm_display_width;
		}
		
		protected void sublayout(int _width,int _height){
			super.sublayout(_width, this.getPreferredHeight());
		}
	};
	
	InputManager		m_inputMgr		= null;
	
	MainChatScreen		m_chatScreen	= null;
	
	public MiddleMgr(MainChatScreen _charScreen){
		super(Manager.NO_VERTICAL_SCROLL);
		
		m_chatScreen	= _charScreen;
		
		m_chatMsgMgr = new VerticalFieldManager(Manager.VERTICAL_SCROLL){
			protected void sublayout(int _width,int _height){
				int t_y = 0;
				int t_num = this.getFieldCount();
				int t_height = 0;
				for(int i = 0;i < t_num;i++){
					Field field = getField(i);
					t_height = field.getPreferredHeight();
					
					setPositionChild(field,0,t_y);
					layoutChild(field,field.getPreferredWidth(),t_height);
					
					t_y += t_height + fsm_linespace;
				}
				
				setExtent(recvMain.fsm_display_width, t_y);
			}
		};
		m_chatMsgMiddleMgr.add(m_chatMsgMgr);
				
		m_inputMgr = new InputManager(this);
		
		readdControl();
	}
	
	public void readdControl(){
		deleteAll();
		
		if(m_chatScreen.m_mainApp.m_imChatScreenReverse){
			add(m_inputMgr);
			add(m_chatMsgMiddleMgr);			
		}else{
			add(m_chatMsgMiddleMgr);
			add(m_inputMgr);
		}
	}
	public synchronized void deleteChat(){
		int t_num = m_chatMsgMgr.getFieldCount();
		for(int i = 0;i < t_num;i++){
			m_chatScreen.m_chatFieldAllocator.release(m_chatMsgMgr.getField(i));
		}
		
		m_chatMsgMgr.deleteAll();
	}
	public synchronized void prepareChatScreen(MainIMScreen.RosterChatData _chatData){
		
		deleteChat();
		
		ChatField t_field = null;
		for(int i = 0 ;i < _chatData.m_chatMsgList.size();i++){
			fetchChatMsg msg = (fetchChatMsg)_chatData.m_chatMsgList.elementAt(i);
			try{
				t_field = (ChatField)m_chatScreen.m_chatFieldAllocator.alloc();
			}catch(Exception e){
				t_field = new ChatField();
				m_chatScreen.m_mainApp.SetErrorString("PCS:"+e.getMessage()+e.getClass().getName());
			}
			
			t_field.init(msg,m_chatScreen);
			
			addChatField(t_field);
			
			if(_chatData.m_isYuch){
				m_chatScreen.m_mainScreen.sendChatReadMsg(msg);
			}
		}
				
		scrollToBottom(t_field);
		
		m_inputMgr.m_editTextArea.setFocus();
				
		if(m_inputMgr.m_editTextArea.getTextLength() > 0){			
			m_inputMgr.m_editTextArea.setCursorPosition(
					m_inputMgr.m_editTextArea.getTextLength());
		}
		
		m_inputMgr.enableVoiceMode(m_chatScreen.m_mainApp.m_imVoiceImmMode);
	}
	
	public int getPreferredWidth(){
		return recvMain.fsm_display_width;
	}
	
	public int getPreferredHeight(){
		return recvMain.fsm_display_height - m_chatScreen.m_header.getPreferredHeight();
	}
	
	public void sublayout(int _width,int _height){
	
		if(getFieldCount() != 2){
			// readdControl->deleteAll->sublayout
			// readdControl->add->sublayout
			//
			// will make exception
			//
			return ;
		}
		
		if(m_chatScreen.m_mainApp.m_imChatScreenReverse){

			setPositionChild(m_inputMgr,0,0);
			layoutChild(m_inputMgr,m_inputMgr.getPreferredWidth(),m_inputMgr.getPreferredHeight());
			
			int t_y = m_inputMgr.getPreferredHeight();
			
			setPositionChild(m_chatMsgMiddleMgr,0,t_y);
			layoutChild(m_chatMsgMiddleMgr,m_chatMsgMiddleMgr.getPreferredWidth(),m_chatMsgMiddleMgr.getPreferredHeight());
			
		}else{

			setPositionChild(m_chatMsgMiddleMgr,0,0);		
			layoutChild(m_chatMsgMiddleMgr,m_chatMsgMiddleMgr.getPreferredWidth(),m_chatMsgMiddleMgr.getPreferredHeight());
			
			int t_y = getPreferredHeight() - m_inputMgr.getPreferredHeight();
			
			setPositionChild(m_inputMgr,0,t_y);
			layoutChild(m_inputMgr,m_inputMgr.getPreferredWidth(),m_inputMgr.getPreferredHeight());
		}	
		
		setExtent(recvMain.fsm_display_width,getPreferredHeight());
	}
	
	protected void subpaint(Graphics g){
		
		int t_color = g.getColor();
		try{
			g.setColor(MainChatScreen.fsm_background);
			g.fillRect(0,0,getPreferredWidth(),getPreferredHeight());
			
			g.fillRect(0,0,100,100);
			
		}finally{
			g.setColor(t_color);
		}
		
		super.subpaint(g);
	}
		
	public synchronized void addChatMsg(fetchChatMsg _msg){
		ChatField t_field = null;
		
		try{
			t_field = (ChatField)m_chatScreen.m_chatFieldAllocator.alloc();
		}catch(Exception e){
			t_field = new ChatField();
			m_chatScreen.m_mainApp.SetErrorString("ACS:"+e.getMessage()+e.getClass().getName());
		}
		t_field.init(_msg,m_chatScreen);
		
		addChatField(t_field);
				
		// scroll to bottom
		//
		boolean t_editFocus = m_inputMgr.m_editTextArea.isFocus(); 
		if(t_editFocus){
			scrollToBottom(t_field);
		}
				
		if(m_chatScreen.getCurrRoster().m_isYuch
		&& Backlight.isEnabled() 
		&& m_chatScreen.m_mainApp.isForeground() 
		&& m_chatScreen.m_mainApp.getActiveScreen() == m_chatScreen){
			m_chatScreen.m_mainScreen.sendChatReadMsg(_msg);
		}
		
		if(m_chatScreen.m_mainApp.m_imVoiceImmMode
			&& !_msg.isOwnMsg()
			&& _msg.getFileContent() != null && _msg.getFileContentType() == fetchChatMsg.FILE_TYPE_SOUND){
			m_chatScreen.open(_msg);
		}
		
		// set the focus back
		//
		if(t_editFocus){
			m_inputMgr.m_editTextArea.setFocus();
		}		
	}
	
	public synchronized void addChatMsg(ChatField _field){	
		addChatField(_field);			
		scrollToBottom(_field);	
		
		// set the focus back
		//
		m_inputMgr.m_editTextArea.setFocus();
	}
	
	public synchronized ChatField findChatField(fetchChatMsg _msg){
		int t_num = m_chatMsgMgr.getFieldCount();
		for(int i = 0;i < t_num;i++){
			ChatField t_field = (ChatField)m_chatMsgMgr.getField(i);
			
			if(t_field.m_msg == _msg){
				return t_field;
			}
		}
		
		return null;
	}
	
	public void scrollToBottom(Field _field){
		
		// scroll to bottom
		//
		if(m_chatScreen.m_mainApp.m_imChatScreenReverse){				
			m_chatMsgMiddleMgr.setVerticalScroll(0);
		}else{
			int t_height = m_chatMsgMiddleMgr.getVirtualHeight() - m_chatMsgMiddleMgr.getPreferredHeight();
			if(t_height > 0){
				m_chatMsgMiddleMgr.setVerticalScroll(t_height);
			}
		}
		
		if(_field != null){
			_field.setFocus();
		}else{
			if(m_chatMsgMiddleMgr.getFieldCount() != 0){
				if(m_chatScreen.m_mainApp.m_imChatScreenReverse){				
					_field = m_chatMsgMiddleMgr.getField(0);
				}else{
					_field = m_chatMsgMiddleMgr.getField(m_chatMsgMiddleMgr.getFieldCount() - 1);
				}
				
				_field.setFocus();
			}
		}
	}
	
	private void addChatField(ChatField _field){
		if(m_chatScreen.m_mainApp.m_imChatScreenReverse){
			m_chatMsgMgr.insert(_field,0);
		}else{
			m_chatMsgMgr.add(_field);
		}
	}
}

public class MainChatScreen extends MainScreen implements ChatField.IChatFieldOpen{
	
	public final static int fsm_background = 0x2b3d4d;
	
	int m_menu_op = 0;
	MenuItem m_sendMenu = new MenuItem(recvMain.sm_local.getString(yblocalResource.WEIBO_SEND_LABEL),m_menu_op++,0){
		public void run(){
			m_middleMgr.m_inputMgr.send();
		}
	};
	
	MenuItem m_copyMenu = new MenuItem(recvMain.sm_local.getString(yblocalResource.IM_COPY_MESSAGE_LABEL),m_menu_op++,0){
		public void run(){
			
		}
	};
	
	MenuItem m_phizMenu = new MenuItem(recvMain.sm_local.getString(yblocalResource.WEIBO_PHIZ_LABEL),m_menu_op++,0){
		public void run(){
			UiApplication.getUiApplication().pushScreen(
					PhizSelectedScreen.getPhizScreen(m_mainApp,m_middleMgr.m_inputMgr.m_editTextArea));
		}
	};
	MenuItem m_snapItem = new MenuItem(recvMain.sm_local.getString(yblocalResource.WEIBO_OPEN_CAMERA_SNAP),m_menu_op++,0){
    	public void run(){
    		try{
    			m_cameraScreen = new CameraScreen(new ICameraScreenCallback(){
        			public void snapOK(byte[] _buffer){
        				clearAttachment();
        				
        				m_snapBuffer = _buffer;
        				m_imageType	= fetchChatMsg.FILE_TYPE_IMG;
        			}
        		},m_mainApp.getWeiboUploadSize());
        		
        		m_mainApp.pushScreen(m_cameraScreen);	
    		}catch(Exception e){
    			
    			m_mainApp.SetErrorString("MCS:" + e.getMessage());
    			m_cameraMenu.run();
    		}    		
    	}
    };
	    
	MenuItem m_cameraMenu = new MenuItem(recvMain.sm_local.getString(yblocalResource.WEIBO_OPEN_CAMERA),m_menu_op++,0){
		public void run(){			
			Invoke.invokeApplication(Invoke.APP_TYPE_CAMERA, new CameraArguments());
			
			m_canbeAttachImage = true;
		}
	};
	
	MenuItem m_recordMenu = new MenuItem(recvMain.sm_local.getString(yblocalResource.IM_RECORD_AUDIO_MENU_LABEL),m_menu_op++,0){
		public void run(){
			m_recordScreen.startRecord();
			m_isRecording = true;
			
			invalidate();
		}
	};
	
	MenuItem m_browseFile = new MenuItem(recvMain.sm_local.getString(yblocalResource.IM_BROWSE_FILES_MENU_LABEL),m_menu_op++,0){
		public void run(){
			try{
				uploadFileScreen t_fileScreen = new uploadFileScreen(m_mainApp, false,new IUploadFileScreenCallback(){
					
					public boolean clickOK(String _filename,int _size){
						
						if(_filename.toLowerCase().endsWith(".jpg")){
							clearAttachment();
							
							m_imagePath = _filename;
							m_imageType	= fetchChatMsg.FILE_TYPE_IMG;					
							
							return true;
							
						}else{
							
							m_mainApp.DialogAlert(recvMain.sm_local.getString(yblocalResource.IM_BROWSE_FILES_PROMPT));
							return false;	
						}				
					}
					
					public void clickDel(String _filename){}
				});
				
				UiApplication.getUiApplication().pushScreen(t_fileScreen);
				
			}catch(Exception e){
				m_mainApp.SetErrorString("MCSBF", e);
			}
		}
	};
		
	MenuItem m_checkPic		= new MenuItem(recvMain.sm_local.getString(yblocalResource.WEIBO_CHECK_UPLOADING_IMAGE),m_menu_op++,0){
    	public void run(){
    		try{
    			if(m_imagePath != null){
    				
    				if(!m_mainApp.CheckMediaNativeApps(m_imagePath)){
    					UiApplication.getUiApplication().pushGlobalScreen(new imageViewScreen(m_imagePath),0,UiEngine.GLOBAL_MODAL);
    				}
        			
        		}else if(m_snapBuffer != null){
        			UiApplication.getUiApplication().pushGlobalScreen(new imageViewScreen(m_snapBuffer),0,UiEngine.GLOBAL_MODAL);
        		}else if(m_recordBuffer != null){
        			playAudio(m_recordBuffer);
        		}
    		}catch(Exception e){
    			m_mainApp.SetErrorString("WCP:"+e.getMessage()+e.getClass().getName());
    		}  		
    	}
    };
    
    MenuItem m_deletePic	= new MenuItem(recvMain.sm_local.getString(yblocalResource.WEIBO_DELETE_PIC_MENU_LABEL),m_menu_op++,0){
    	public void run(){
    		clearAttachment();
    	}
    };
    
    MenuItem m_deleteChatMenu = new MenuItem(recvMain.sm_local.getString(yblocalResource.IM_DELETE_HISTORY_CHAT),m_menu_op++,0){
		public void run(){
			if(Dialog.ask(Dialog.D_YES_NO,
				recvMain.sm_local.getString(yblocalResource.IM_DELETE_HISTORY_CHAT_PROMPT),
				Dialog.NO) == Dialog.YES){
				
				m_currRoster.m_chatMsgList.removeAllElements();
				m_middleMgr.deleteChat();
			}
		}
	};
	
	MenuItem m_resendMenu = new MenuItem(recvMain.sm_local.getString(yblocalResource.IM_RESEND_MSG_MENU_LABEL),m_menu_op++,0){
		public void run(){
			ChatField t_field = getResendField();
			if(t_field != null){
				m_middleMgr.m_chatMsgMgr.delete(t_field);
				m_currRoster.m_chatMsgList.removeElement(t_field.m_msg);				
				
				m_currRoster.m_chatMsgList.addElement(t_field.m_msg);
				m_middleMgr.addChatMsg(t_field);
				
				// add send daemon
				//
				m_mainScreen.addSendChatMsg(t_field.m_msg,m_currRoster);
			}
			
		}
	};
	
	MenuItem m_displayTimeMenu = new MenuItem(recvMain.sm_local.getString(yblocalResource.IM_DISPLAY_CHAT_FIELD_TIME),m_menu_op++,0){
		public void run(){
			if(!recvMain.sm_imDisplayTime){
				recvMain.sm_imDisplayTime = true;
				
				m_middleMgr.m_chatMsgMgr.invalidate();
				
				m_mainApp.WriteReadIni(false);
			}
		}
	};
	
	MenuItem m_hideTimeMenu = new MenuItem(recvMain.sm_local.getString(yblocalResource.IM_HIDE_CHAT_FIELD_TIME),m_menu_op++,0){
		public void run(){
			if(recvMain.sm_imDisplayTime){
				recvMain.sm_imDisplayTime = false;
				
				m_middleMgr.m_chatMsgMgr.invalidate();
				
				m_mainApp.WriteReadIni(false);
			}
		}
	};
	
	MenuItem m_goTopMenu = new MenuItem(recvMain.sm_local.getString(yblocalResource.IM_CHAT_SCREEN_TOP_MENU),m_menu_op++,0){
		public void run(){
			if(m_middleMgr.m_chatMsgMgr.getFieldCount() != 0){
				if(m_mainApp.m_imChatScreenReverse){
					m_middleMgr.m_chatMsgMgr.getField(m_middleMgr.m_chatMsgMgr.getFieldCount() - 1).setFocus();
				}else{
					m_middleMgr.m_chatMsgMgr.getField(0).setFocus();
				}				
			}
		}
	};
	
	MenuItem m_goBottomMenu = new MenuItem(recvMain.sm_local.getString(yblocalResource.IM_CHAT_SCREEN_BOTTOM_MENU),m_menu_op++,0){
		public void run(){
			if(m_middleMgr.m_chatMsgMgr.getFieldCount() != 0){
				if(m_mainApp.m_imChatScreenReverse){
					m_middleMgr.m_chatMsgMgr.getField(0).setFocus();
				}else{
					m_middleMgr.m_chatMsgMgr.getField(m_middleMgr.m_chatMsgMgr.getFieldCount() - 1).setFocus();					
				}				
			}
		}
	};
	
	MenuItem m_enableVoiceModeMenu = new MenuItem(recvMain.sm_local.getString(yblocalResource.IM_ENABLE_VOICE_MODE),m_menu_op++,0){
		public void run(){
			m_mainApp.m_imVoiceImmMode = true;
			m_middleMgr.m_inputMgr.enableVoiceMode(true);
			m_mainApp.enableKeyUpEvents(true);
		}
	};
	
	MenuItem m_disableVoiceModeMenu = new MenuItem(recvMain.sm_local.getString(yblocalResource.IM_DISABLE_VOICE_MODE),m_menu_op++,0){
		public void run(){
			m_mainApp.m_imVoiceImmMode = false;
			m_middleMgr.m_inputMgr.enableVoiceMode(false);
			m_mainApp.enableKeyUpEvents(false);
		}
	};
	
	
	static ImageUnit sm_composing = null;
	static {
		sm_composing = recvMain.sm_weiboUIImage.getImageUnit("composing_chat_state");
	}
	
	final class ChatScreenHeader extends Field{
		
		WeiboHeadImage		m_rosterImage;
		
		public void setRosterImage(WeiboHeadImage _image){
			m_rosterImage = _image;
		}
		
		public int getPreferredWidth() {
			return recvMain.fsm_display_width;
		}
		
		public int getPreferredHeight() {
			return m_mainApp.m_imChatScreenShowHeadImg?54:30;
		}
		
		public void invalidate(){
			super.invalidate();
		}
		
		protected void layout(int _width,int _height){
			setExtent(recvMain.fsm_display_width,getPreferredHeight());
		}	
		
		protected void paint(Graphics _g){
			recvMain.sm_weiboUIImage.drawBitmapLine(_g, m_title, 0, 0, getPreferredWidth());
			
			int t_start_x = 0;
			
			if(m_mainApp.m_imChatScreenShowHeadImg){
				t_start_x += WeiboHeadImage.displayHeadImage(_g,0, 2, m_rosterImage);
			}
			
			// draw roster state
			//
			int t_x = RosterItemField.drawRosterState(_g,t_start_x,3,m_currRoster.m_roster.getPresence());
			
			int color = _g.getColor();
			Font font = _g.getFont();
			try{
				
				_g.setColor(RosterItemField.fsm_nameTextColor);
				_g.setFont(MainIMScreen.fsm_boldFont);
				
				_g.drawText(m_currRoster.m_roster.getName(),t_x,2);
				
				if(m_mainApp.m_imChatScreenShowHeadImg){
					String t_status = m_currRoster.m_roster.getStatus();
					if(t_status.length() == 0){
						t_status = m_currRoster.m_roster.getAccount();
					}
					_g.drawText(t_status,t_start_x,MainIMScreen.fsm_boldFont.getHeight());
				}
				
			}finally{
				_g.setColor(color);
				_g.setFont(font);
			}
			
			t_x = RosterItemField.drawChatSign(_g,getPreferredWidth(),getPreferredHeight(),m_currRoster.m_roster.getStyle(),m_currRoster.m_isYuch,30);
						
			if(m_currRoster.m_currChatState == fetchChatMsg.CHAT_STATE_COMPOSING){
				recvMain.sm_weiboUIImage.drawImage(_g, sm_composing, t_x - sm_composing.getWidth(), 3);
			}
		}
	}
	
	public ObjectAllocator			m_chatFieldAllocator = new ObjectAllocator("com.yuchting.yuchberry.client.im.ChatField");
	public boolean					m_isPrompted = false;
	
	private MainIMScreen.RosterChatData	m_currRoster 	= null;
		
	recvMain		m_mainApp 		= null;
	MainIMScreen	m_mainScreen 	= null;
	
	ImageUnit		m_title			= null;
	ChatScreenHeader m_header 		= null;
	
	MiddleMgr		m_middleMgr		= null;
	
	String			m_imagePath = null;
	int				m_imageType = 0;
	
	// canbe attach a image from file system
	// maybe a non-relation file would be attached sometimes
	// some chat will send this image
	boolean		m_canbeAttachImage = false;
	
	byte[]			m_snapBuffer = null;
	
	ImageUnit		m_hasImageSign	= null;
	ImageUnit		m_hasVoiceSign	= null;
	
	CameraScreen	m_cameraScreen = null;
	
	CameraFileOP	m_camerFileOp = new CameraFileOP() {
		
		public void onAddUploadingPic(String file, int type) {
			clearAttachment();
			
			m_imagePath = file;
			m_imageType = fetchChatMsg.FILE_TYPE_IMG;
		}
		
		public boolean canAdded() {
			return m_imagePath == null && m_canbeAttachImage;
		}
	};
	
	boolean m_isRecording		= false;
	RecordAudioScreen	m_recordScreen = null;
	byte[]						m_recordBuffer = null;
	
	public void clearAttachment(){
		m_imagePath = null;
		m_imageType = 0;
		
		m_recordBuffer = null;
		m_snapBuffer = null;
		
		m_isPrompted = false;

		invalidate();
	}
			
	public MainChatScreen(recvMain _mainApp,MainIMScreen _mainScreen){
		super(Manager.NO_VERTICAL_SCROLL);
		
		m_mainApp 		= _mainApp;
		m_mainScreen	= _mainScreen;
		
		m_header 		= new ChatScreenHeader();
		m_middleMgr		= new MiddleMgr(this);
		m_title 		= recvMain.sm_weiboUIImage.getImageUnit("nav_bar");
		m_hasImageSign	= recvMain.sm_weiboUIImage.getImageUnit("picSign");
		m_hasVoiceSign	= recvMain.sm_weiboUIImage.getImageUnit("voice_sign");
				
		setBanner(m_header);
		
		add(m_middleMgr);
		
		m_recordScreen = new RecordAudioScreen(m_mainApp, this,new IRecordAudioScreenCallback(){
			public void recordDone(byte[] _buffer){
				
				if(_buffer.length > 512){

					clearAttachment();
					
					m_recordBuffer = _buffer;
					m_imageType = fetchChatMsg.FILE_TYPE_SOUND;
					
					if(m_mainApp.m_imVoiceImmMode){
						sendChatMsg("");
					}
				}
				
				m_isRecording = false;
				invalidate();
			}
		});		
	}
	protected void makeMenu(Menu _menu,int instance){
		
		if(m_isRecording){
			return;
		}
		
		_menu.add(m_sendMenu);
		_menu.add(m_phizMenu);
		if(DeviceInfo.hasCamera()){
			if(recvMain.fsm_snapshotAvailible){
				_menu.add(m_snapItem);
			}
			_menu.add(m_cameraMenu);
		}
		_menu.add(m_recordMenu);
		_menu.add(m_browseFile);
				
		if(m_imagePath != null || m_snapBuffer != null || m_recordBuffer != null){
			_menu.add(m_checkPic);
			_menu.add(m_deletePic);
		}
		
		if(!m_currRoster.m_chatMsgList.isEmpty()){
			_menu.add(m_deleteChatMenu);
		}
		
		if(getResendField() != null){
			_menu.add(m_resendMenu);
		}		
		
		if(recvMain.sm_imDisplayTime){
			_menu.add(m_hideTimeMenu);
		}else{
			_menu.add(m_displayTimeMenu);
		}
		
		if(m_middleMgr.m_chatMsgMgr.getFieldCount() != 0){
			_menu.add(m_goTopMenu);
			_menu.add(m_goBottomMenu);
		}
		if(m_mainApp.m_imVoiceImmMode){
			_menu.add(m_disableVoiceModeMenu);
		}else{
			_menu.add(m_enableVoiceModeMenu);
		}
		
		super.makeMenu(_menu,instance);
	}
	
	/**
	 * pop up the roster
	 * @param _roster
	 */
	public void popup(MainIMScreen.RosterChatData _roster){
		
		setCurrRoster(_roster);
		
		clearAttachment();	
		m_mainApp.pushScreen(this);
	}
	
	/**
	 * set the current chat roster
	 * @param _roster
	 */
	public void setCurrRoster(RosterChatData _roster){	m_currRoster = _roster;}
	
	/**
	 * get the current chat roster
	 * @return
	 */
	public RosterChatData getCurrRoster(){return m_currRoster;}
	
	protected void onDisplay(){
		super.onDisplay();
		onDisplay_impl();
	}
	
	public void onDisplay_impl(){
		if(m_currRoster != null){
			
			m_currRoster.m_hasNewMessage = false;
			
			m_middleMgr.prepareChatScreen(m_currRoster);
			
			try{
				m_header.setRosterImage(m_mainScreen.getHeadImage(m_currRoster.m_roster));
			}catch(Exception e){
				m_mainApp.SetErrorString("MCS_OD", e);
			}
		}

		m_mainApp.StopIMNotification();
		m_mainScreen.clearNewChatSign();
		
		m_isPrompted = false;
		
		m_mainApp.addFileSystemJournalListener(m_camerFileOp);
		
		m_canbeAttachImage = false;
	}
		
	public ChatField getResendField(){
		if(m_middleMgr.m_inputMgr.m_editTextArea.isFocus()
		|| m_middleMgr.m_inputMgr.m_phizButton.isFocus()){
			return null;
		}
		
		int t_num = m_middleMgr.m_chatMsgMgr.getFieldCount();
		for(int i = 0 ;i < t_num;i++){
			ChatField t_chatField = (ChatField)m_middleMgr.m_chatMsgMgr.getField(i);
			if(t_chatField.isFocus() && t_chatField.m_msg.getSendState() == fetchChatMsg.SEND_STATE_ERROR){
				return t_chatField;
			}
		}
		
		return null;
	}
	private void closeRecordScreen(){
		if(m_isRecording){
			m_recordScreen.close();
			m_isRecording = false;
		}
	}
	public boolean onClose(){
		if(m_isRecording){
			closeRecordScreen();
			return false;
		}
		
		m_mainApp.m_isChatScreen = false;
		
		close();		
		return true;
	}
	
	public void close(){
		
		m_middleMgr.m_inputMgr.cancelComposeTimer();
		m_mainApp.StopIMNotification();
		
		if(m_mainApp.getScreenCount() == 1){
			m_mainApp.requestBackground();
		}else{
			super.close();
		}
		
		closeRecordScreen();
	}
	
	Dialog m_waitDlg = null;
	public void sendChatMsg(final String _text){
		if(m_imagePath == null){
			sendChatMsg_impl(_text);
		}else{
			m_waitDlg = new Dialog(recvMain.sm_local.getString(yblocalResource.WEIBO_WAIT_COMPRESS_IMAGE),new Object[0],new int[0],0,null);
			m_waitDlg.show();
			
			m_mainApp.invokeLater(new Runnable() {
				public void run() {
					sendChatMsg_impl(_text);
				}
			});
		}
	}
	
	private void sendChatMsg_impl(String _text){
		// add UI
		//
		fetchChatMsg t_msg = null;
		try{
			t_msg = (fetchChatMsg)m_mainScreen.m_chatMsgAllocator.alloc();
		}catch(Exception e){
			t_msg = new fetchChatMsg();
			m_mainApp.SetErrorString("SCM_0:"+e.getMessage()+e.getClass().getName());
		}
		
		t_msg.setOwner(m_currRoster.m_roster.getOwnAccount());
		t_msg.setSendTime(System.currentTimeMillis());
		t_msg.setMsg(_text);
		t_msg.setStyle(m_currRoster.m_roster.getStyle());
		t_msg.setIsOwnMsg(true);
		
		byte[] t_content = null;
		if(m_imagePath != null){
			try{
				t_content = CameraFileOP.resizePicFile(m_imagePath, m_mainApp.getIMSendImageQuality());			
			}catch(Exception e){
				m_mainApp.SetErrorString("SCM:"+e.getMessage()+e.getClass());
			}	
		}
		
		if(t_content == null){
			t_content = m_snapBuffer;
		}
		
		if(m_recordBuffer != null){
			t_content = m_recordBuffer;
		}
		
		t_msg.setFileContent(t_content, m_imageType);
		
		// clear the file sign
		//
		clearAttachment();
		invalidate();
		
		m_currRoster.m_chatMsgList.addElement(t_msg);
		m_middleMgr.addChatMsg(t_msg);
		
		// add send daemon
		//
		m_mainScreen.addSendChatMsg(t_msg,m_currRoster);
		
		if(m_waitDlg != null){
			m_waitDlg.close();
			m_waitDlg = null;
		}
		
		// prepare store this msg;
		//
		m_mainScreen.storefetchMsg(t_msg);
	}
	
	public void sendChatComposeState(byte _state){
		
		try{
			
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			os.write(msg_head.msgChatState);
			os.write(m_currRoster.m_roster.getStyle());
			
			sendReceive.WriteString(os,m_currRoster.m_roster.getOwnAccount());
			sendReceive.WriteString(os,m_currRoster.m_roster.getAccount());
			
			os.write(_state);
					
			m_mainApp.m_connectDeamon.addSendingData(msg_head.msgChatState, os.toByteArray(), true);
			
		}catch(Exception e){
			m_mainApp.SetErrorString("SCCS:"+e.getMessage()+e.getClass().getName());
		}
	}
	
	protected void paint(Graphics g){			
		super.paint(g);
		
		if(m_imagePath != null || m_snapBuffer != null || m_recordBuffer != null){
			
			int t_y = m_mainApp.m_imChatScreenReverse?m_middleMgr.m_inputMgr.getPreferredHeight():
					recvMain.fsm_display_height - m_middleMgr.m_inputMgr.getPreferredHeight() - m_hasImageSign.getHeight();
			
			int t_x = recvMain.fsm_display_width - m_hasImageSign.getWidth();
			
			if(m_recordBuffer != null){
				recvMain.sm_weiboUIImage.drawImage(g, m_hasVoiceSign, t_x, t_y);
			}else{
				recvMain.sm_weiboUIImage.drawImage(g, m_hasImageSign, t_x, t_y);
			}
		}
		
		if(m_isRecording){
			m_recordScreen.paint(g);
		}
	}
	
	protected boolean keyUp(int keycode,int time){
		
		if(m_isRecording){
			closeRecordScreen();
		}
		
		// the VoiceImmMode can callback onClose() method
		// when enableKeyUpEvents(true) called to dispatch escape key event
		// so must judge programmly own to close
		//
		int key = Keypad.key(keycode);
		if(key == Keypad.KEY_ESCAPE && m_mainApp.m_imVoiceImmMode){
			onClose();
			return true;
		}
		
		return super.keyUp(keycode,time);
	}
	
	protected boolean keyDown(int keycode,int time){
		
		if(m_isPrompted){
			m_mainApp.StopIMNotification();
		}
		
		m_isPrompted = false;
		
		int key = Keypad.key(keycode);
		if(key == /*Keypad.KEY_CAMERA_FOCUS*/211){ // the 4.2/4.5 sdk has no Keypad.KEY_CAMERA_FOCUS
			// camera focus shortcut key clicked
			// canbe attach image
			//
			m_canbeAttachImage = true;
		}		
		
		if(!m_middleMgr.m_inputMgr.m_editTextArea.isFocus()){
			// edit text area is NOT focus
			//
			if(key == 'R'){
				m_middleMgr.scrollToBottom(null);
				m_middleMgr.m_inputMgr.m_editTextArea.setFocus();
				return true;				
			}
			
			// hacking function for [私信] xxxx:
			if(key == 'D' && ChatField.sm_currFocusField != null){
				if(m_middleMgr.m_inputMgr.m_editTextArea.getTextLength() == 0){
					String t_message = ChatField.sm_currFocusField.m_msg.getMsg();
					int t_commaIdx = t_message.indexOf("：");
					if(t_message.startsWith("[私信]") && t_commaIdx > 4){
						t_message = "/d" + t_message.substring(4,t_commaIdx) + " ";
						
						m_middleMgr.scrollToBottom(null);
						m_middleMgr.m_inputMgr.m_editTextArea.setFocus();
						m_middleMgr.m_inputMgr.m_editTextArea.setText(t_message);
						m_middleMgr.m_inputMgr.m_editTextArea.setCursorPosition(t_message.length());
						
						return true;
					}
				}				
			}
			
			if(m_middleMgr.m_chatMsgMgr.getFieldCount() != 0){

				int t_topKey = m_mainApp.m_imChatScreenReverse?'B':'T';
				int t_bottomKey = m_mainApp.m_imChatScreenReverse?'T':'B';
				
				if(key == t_topKey){
					
					m_goTopMenu.run();
					return true;
					
				}else if(key == t_bottomKey){
					
					m_goBottomMenu.run();
					return true;
				}
			}
		}
		
		
		return super.keyDown(keycode,time);
	}
	
	Thread m_currPlayVoiceThread = null;
	
	public void open(final fetchChatMsg msg){

		if(msg.getFileContent() != null){
			
			try{
				switch(msg.getFileContentType()){
				case fetchChatMsg.FILE_TYPE_IMG:
					String t_file = m_mainApp.GetIMHeadImageDir(msg.getStyle()) + Math.abs(msg.hashCode()) +".jpg";
					
					FileConnection t_fc = (FileConnection)Connector.open(t_file,Connector.READ_WRITE);
					try{
						if(!t_fc.exists()){
							t_fc.create();
							
							OutputStream t_fileos = t_fc.openOutputStream();
							try{
								t_fileos.write(msg.getFileContent());										
							}finally{
								t_fileos.flush();
								t_fileos.close();
								t_fileos = null;								
							}
						}
						
					}finally{
						t_fc.close();
						t_fc = null;
					}
					
					if(!m_mainApp.CheckMediaNativeApps(t_file)){
						UiApplication.getUiApplication().pushGlobalScreen(new imageViewScreen(msg.getFileContent())
															,0,UiEngine.GLOBAL_MODAL);
					}
					
					break;
				case fetchChatMsg.FILE_TYPE_SOUND:
					playAudio(msg.getFileContent());		            
					break;
				}
			}catch(Exception e){
				m_mainApp.SetErrorString("MCS-O:"+e.getMessage()+e.getClass().getName());
			}
		}	
	}
	
	private void playAudio(final byte[] _audioBuffer){
		if(m_currPlayVoiceThread == null){
			
			m_currPlayVoiceThread = new Thread(){
				
				public void run(){
					try{
						javax.microedition.media.Player p = 
							javax.microedition.media.Manager.createPlayer(
									new ByteArrayInputStream(_audioBuffer),"audio/amr");
						
			            p.realize();
			            p.prefetch();
			            p.start();
			            
			            sleep(2000);
				        
					}catch(Exception e){
						m_mainApp.SetErrorString("OPENA:"+e.getMessage()+e.getClass().getName());
					}
		            
		            m_currPlayVoiceThread = null;
				}
			};
			
			m_currPlayVoiceThread.start();
		}
	}
}
