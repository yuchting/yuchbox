package com.yuchting.yuchberry.client.im;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.component.AutoTextEditField;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.yuchting.yuchberry.client.recvMain;
import com.yuchting.yuchberry.client.ui.BubbleImage;
import com.yuchting.yuchberry.client.ui.ButtonSegImage;
import com.yuchting.yuchberry.client.ui.ImageButton;
import com.yuchting.yuchberry.client.ui.ImageUnit;

final class InputManager extends Manager implements FieldChangeListener{
	
	public final static int fsm_inputBubbleBorder = 4;
	public final static int fsm_textBorder = 2;
	
	public final static int fsm_minHeight = MainIMScreen.sm_defaultFontHeight + (fsm_textBorder + fsm_inputBubbleBorder) * 2;
	public final static int fsm_maxHeight = recvMain.fsm_display_height / 2;
	
	MainChatScreen		m_screen	= null;
	ImageButton			m_phizButton = null;
	
	int					m_textWidth	= 0;
	
	BubbleImage			m_inputBackground = null;
	ImageUnit			m_background	= null;
	ImageUnit			m_background_line = null;
	
	int					m_currHeight	= fsm_minHeight;
	
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
	};
	
	public InputManager(MainChatScreen _mainScreen){
		super(Manager.NO_VERTICAL_SCROLL);
		
		m_screen = _mainScreen;
		
		m_phizButton	= new ImageButton("Phiz", 
				recvMain.sm_weiboUIImage.getImageUnit("phiz_button"), 
				recvMain.sm_weiboUIImage.getImageUnit("phiz_button_focus"), 
				recvMain.sm_weiboUIImage);
		
		m_textWidth = getPreferredWidth() - m_phizButton.getImageWidth() - (fsm_textBorder + fsm_inputBubbleBorder)* 2;
		
		m_inputBackground = new BubbleImage(
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
		
		m_background = recvMain.sm_weiboUIImage.getImageUnit("weibo_bg");
		m_background_line = recvMain.sm_weiboUIImage.getImageUnit("space_line");
		
		m_editTextArea.setChangeListener(this);
		m_inputManager.add(m_editTextArea);
		add(m_inputManager);
		add(m_phizButton);
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
		
		setPositionChild(m_phizButton,getPreferredWidth() - m_phizButton.getImageWidth(),fsm_textBorder);		
		layoutChild(m_phizButton,m_phizButton.getImageWidth(),m_phizButton.getImageHeight());
				
		setExtent(getPreferredWidth(), getPreferredHeight());
	}
	
	protected void subpaint(Graphics _g){
		
		recvMain.sm_weiboUIImage.fillImageBlock(_g, m_background, 0, 0, getPreferredWidth(),getPreferredHeight());
		recvMain.sm_weiboUIImage.drawBitmapLine(_g, m_background_line, 0, 0, getPreferredWidth());
		
		m_inputBackground.draw(_g, fsm_inputBubbleBorder, fsm_inputBubbleBorder, 
								m_textWidth + fsm_textBorder * 2,
								m_inputManager.getPreferredHeight() - fsm_inputBubbleBorder * 2,
								BubbleImage.NO_POINT_STYLE);
		
		super.subpaint(_g);
	}
	
	public void fieldChanged(Field field, int context) {
		if(context != FieldChangeListener.PROGRAMMATIC){
			if(field == m_editTextArea){

				m_screen.m_currRoster.m_lastChatText = m_editTextArea.getText();
				
				int t_formerHeight = m_currHeight;
				
				m_currHeight = m_editTextArea.getHeight() + (fsm_textBorder + fsm_inputBubbleBorder) * 2;
				
				if(m_currHeight < fsm_minHeight){
					m_currHeight = fsm_minHeight;
				}
				
				if(m_currHeight > fsm_maxHeight){
					m_currHeight = fsm_maxHeight;
				}
				
				if(t_formerHeight != m_currHeight){
					sublayout(0,0);
				}
				
			}
		}
	}
}

public class MainChatScreen extends MainScreen{

	public final static int		fsm_titleBottomBorder = 4;
	
	RosterChatData	m_currRoster 	= null;
		
	recvMain		m_mainApp 		= null;
	MainIMScreen	m_mainScreen 	= null;
	
	ButtonSegImage	m_title			= null;
	Field			m_header 		= null;
	
	InputManager	m_inputMgr		= null;
	
	VerticalFieldManager	m_chatMsgMgr = null;
			
	public MainChatScreen(recvMain _mainApp,MainIMScreen _mainScreen){
		super(Manager.VERTICAL_SCROLL);
		
		m_mainApp = _mainApp;
		m_mainScreen = _mainScreen;

		m_title = new ButtonSegImage(recvMain.sm_weiboUIImage.getImageUnit("nav_bar_left"),
									recvMain.sm_weiboUIImage.getImageUnit("nav_bar_mid"),
									recvMain.sm_weiboUIImage.getImageUnit("nav_bar_right"),
									recvMain.sm_weiboUIImage);
		
		m_header = new Field(){
			
			public int getPreferredWidth() {
				return recvMain.fsm_display_width;
			}
			
			public int getPreferredHeight() {
				return m_title.getImageHeight();
			}
			
			protected void layout(int _width,int _height){
				setExtent(recvMain.fsm_display_width,m_title.getImageHeight());
			}
			
			protected void paint(Graphics _g){
				m_title.draw(_g,0,0,getPreferredWidth());
				
				// draw roster state
				//
				int t_x = RosterItemField.drawRosterState(_g,3,3,m_currRoster.m_roster);
				
				int color = _g.getColor();
				Font font = _g.getFont();
				try{
					
					_g.setColor(RosterItemField.fsm_nameTextColor);
					_g.setFont(MainIMScreen.sm_boldFont);
					
					_g.drawText(m_currRoster.m_roster.getName(),t_x,1);
					
				}finally{
					_g.setColor(color);
					_g.setFont(font);
				}
				
				RosterItemField.drawChatSign(_g,getPreferredWidth(),getPreferredHeight(),m_currRoster.m_roster.getStyle());
			}
		};
		
		setTitle(m_header);
		
		m_chatMsgMgr = new VerticalFieldManager(Manager.VERTICAL_SCROLL);		
		add(m_chatMsgMgr);
		
		m_inputMgr = new InputManager(this);
		setStatus(m_inputMgr);
	}
	
	public void prepareChatScreen(RosterChatData _chatData){
		m_currRoster = _chatData;
		m_chatMsgMgr.deleteAll();
		
		for(int i = 0 ;i < _chatData.m_chatList.size();i++){
			ChatField t_field = new ChatField((fetchChatMsg)_chatData.m_chatList.elementAt(i));
			
			m_chatMsgMgr.add(t_field);
		}
		
		m_inputMgr.m_editTextArea.setText(_chatData.m_lastChatText);
		
		invalidate();
	}
	
	protected void onDisplay(){
		super.onDisplay();	
		
		m_inputMgr.m_editTextArea.setFocus();
		
		if(m_inputMgr.m_editTextArea.getTextLength() > 0){
			m_inputMgr.m_editTextArea.setCursorPosition(m_inputMgr.m_editTextArea.getTextLength() - 1);
		}		
	}
	
	public boolean onClose(){
		close();
		return true;
	}
}
