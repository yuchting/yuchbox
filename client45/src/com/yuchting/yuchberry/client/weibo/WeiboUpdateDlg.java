package com.yuchting.yuchberry.client.weibo;


import local.localResource;
import net.rim.device.api.system.Characters;
import net.rim.device.api.system.KeypadListener;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.component.AutoTextEditField;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.yuchting.yuchberry.client.recvMain;
import com.yuchting.yuchberry.client.ui.BubbleImage;
import com.yuchting.yuchberry.client.ui.ButtonSegImage;
import com.yuchting.yuchberry.client.ui.ImageButton;

final class WeiboUpdateManager extends Manager implements FieldChangeListener{
	
	public AutoTextEditField 	m_editTextArea	= new AutoTextEditField();
	
	weiboTimeLineScreen		m_timelineScreen;
	int						m_titleHeight 	= 0;
	int						m_separateLine_y = 0;
	ImageButton				m_sendButton	= null;
	ButtonSegImage			m_updateTitle		= null;
	BubbleImage				m_editBubbleImage = null;
	
	
	public VerticalFieldManager m_editTextManager = new VerticalFieldManager(Manager.VERTICAL_SCROLL){
		
		public int getPreferredHeight(){
			return WeiboUpdateDlg.fsm_height - m_titleHeight - m_sendButton.getImageHeight() - 6;
		}
		
		public int getPreferredWidth(){
			return WeiboUpdateDlg.fsm_width - 6;
		}
		
		protected void sublayout(int width, int height){
			
			setPositionChild(m_editTextArea,0,0);
			layoutChild(m_editTextArea,getPreferredWidth(),999);
			
			setExtent(getPreferredWidth(),getPreferredHeight());
		}
	};
	
	public WeiboUpdateManager(weiboTimeLineScreen _timeline){
		super(Manager.VERTICAL_SCROLL);
		
		if(recvMain.GetClientLanguage() == 0){
			m_sendButton = new ImageButton(recvMain.sm_local.getString(localResource.WEIBO_SEND_LABEL),
					weiboTimeLineScreen.sm_weiboUIImage.getImageUnit("update_button"),
					weiboTimeLineScreen.sm_weiboUIImage.getImageUnit("update_button_focus"),
					weiboTimeLineScreen.sm_weiboUIImage,
					Field.FIELD_RIGHT);
		}else{
			m_sendButton = new ImageButton(recvMain.sm_local.getString(localResource.WEIBO_SEND_LABEL),
					weiboTimeLineScreen.sm_weiboUIImage.getImageUnit("update_button_en"),
					weiboTimeLineScreen.sm_weiboUIImage.getImageUnit("update_button_focus_en"),
					weiboTimeLineScreen.sm_weiboUIImage,
					Field.FIELD_RIGHT);
		}
		
		m_updateTitle = new ButtonSegImage(
				weiboTimeLineScreen.sm_weiboUIImage.getImageUnit("composeTitle_left"),
				weiboTimeLineScreen.sm_weiboUIImage.getImageUnit("composeTitle_mid"),
				weiboTimeLineScreen.sm_weiboUIImage.getImageUnit("composeTitle_right"),
				weiboTimeLineScreen.sm_weiboUIImage);
		
		m_editBubbleImage = weiboTimeLineScreen.sm_bubbleImage;
		m_timelineScreen = _timeline;

		m_editTextArea.setMaxSize(WeiboItemField.fsm_maxWeiboTextLength);
		m_sendButton.setChangeListener(this);
		m_editTextArea.setChangeListener(this);
		
		m_editTextManager.add(m_editTextArea);
		
		add(m_editTextManager);
		add(m_sendButton);
				
		m_titleHeight = m_updateTitle.getImageHeight() + 2;
		m_separateLine_y = m_titleHeight + m_editTextManager.getPreferredHeight();
	}
	
	public int getPreferredHeight(){
		return WeiboUpdateDlg.fsm_height;
	}
	
	public int getPreferredWidth(){
		return WeiboUpdateDlg.fsm_width;
	}
	
	public void fieldChanged(Field field, int context) {
		if(field == m_sendButton){
			m_editTextArea.setFocus();
			sendUpdate();			
		}else if(field == m_editTextArea){
			
			// refresh the input number title text
			//
			getScreen().invalidate();
		}
	}
	
	public void sublayout(int width, int height){
		
		setPositionChild(m_editTextManager,2,m_titleHeight + 2);
		layoutChild(m_editTextManager,m_editTextManager.getPreferredWidth(),m_editTextManager.getPreferredHeight());
		
		int t_buttonWidth = m_sendButton.getImageWidth();
		int t_buttonHeight = m_sendButton.getImageHeight();
		
		int t_sendButton_y = (m_editTextManager.getPreferredHeight() + m_titleHeight) + 
					(WeiboUpdateDlg.fsm_height - (m_editTextManager.getPreferredHeight() + m_titleHeight) - t_buttonHeight) / 2;
		
		setPositionChild(m_sendButton,WeiboUpdateDlg.fsm_width - t_buttonWidth - 3,t_sendButton_y);
			
		layoutChild(m_sendButton,t_buttonWidth,t_buttonHeight);
		
		setExtent(getPreferredWidth(), getPreferredHeight());
	}
	

	public void sendUpdate(){
		if(m_editTextArea.getText().length() != 0){
			getScreen().close();
			m_timelineScreen.UpdateNewWeibo(m_editTextArea.getText());
			m_editTextArea.setText("");
		}
	}
	
	public void subpaint(Graphics _g){
		m_updateTitle.draw(_g,1,1,getPreferredWidth() - 2);
		
		m_editBubbleImage.draw(_g, 1, m_titleHeight,getPreferredWidth() - 3,
				m_editTextManager.getPreferredHeight() + 2,BubbleImage.NO_POINT_STYLE);
		
		int oldColor = _g.getColor();
		Font oldFont = _g.getFont();
		try{
			_g.setFont(WeiboItemField.sm_boldFont);
			_g.setColor(0xffffff);
			String t_str = recvMain.sm_local.getString(localResource.WEIBO_UPDATE_DIALOG_TITLE) 
				+ " (" + m_editTextArea.getText().length() + ")";
			
			_g.drawText(t_str,0,(m_titleHeight - WeiboItemField.sm_fontHeight) / 2 + 2);
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
		}
		
		invalidate();
		getScreen().invalidate();
				
		return super.keyChar(c,status,time);
	}
}

public class WeiboUpdateDlg extends Screen {
	
	public final static int			fsm_width = recvMain.fsm_display_width - 20;
	public final static int			fsm_height = (recvMain.fsm_display_height - 30 > 300?300:(recvMain.fsm_display_height - 30));
		
	WeiboUpdateManager		m_updateManager;
		
	public WeiboUpdateDlg(weiboTimeLineScreen _screen){
		super(new WeiboUpdateManager(_screen),Screen.DEFAULT_MENU | Manager.NO_VERTICAL_SCROLL);
		m_updateManager = (WeiboUpdateManager)getDelegate();
		
		
	}
	
	public int getPreferredHeight(){
		return fsm_height;
	}
	
	public int getPreferredWidth(){
		return fsm_width;
	}
	
	protected void sublayout(int width, int height){
		
		m_updateManager.sublayout(m_updateManager.getPreferredWidth(), m_updateManager.getPreferredHeight());
		
		setExtent(getPreferredWidth(), getPreferredHeight());		
		setPosition((recvMain.fsm_display_width - getPreferredWidth()) / 2 ,
				(recvMain.fsm_display_height - getPreferredHeight()) / 2);
		
	}
	
	protected  void	onDisplay(){
		super.onDisplay();
		
		m_updateManager.setVerticalScroll(0);
		
		if(m_updateManager.m_editTextArea.getTextLength() != 0){
			m_updateManager.m_editTextArea.setCursorPosition(m_updateManager.m_editTextArea.getTextLength());
		}
		m_updateManager.m_editTextArea.setFocus();		
	}
	
	protected void paint(Graphics _g){		

		int color = _g.getColor();
		try{
						
			_g.setColor(0x737373);
			_g.fillRect(0,0,getPreferredWidth(),getPreferredHeight());
						
			_g.setColor(0);
			_g.drawRect(0,0,getPreferredWidth(),getPreferredHeight());
			
		}finally{
			_g.setColor(color);
		}
		
		m_updateManager.subpaint(_g);
	}
	
	public void close(){
		super.close();
		m_updateManager.m_timelineScreen.m_pushUpdateDlg = false;
	}		
}
