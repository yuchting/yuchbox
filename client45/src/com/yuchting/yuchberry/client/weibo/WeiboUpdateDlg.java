package com.yuchting.yuchberry.client.weibo;


import local.localResource;
import net.rim.device.api.system.Characters;
import net.rim.device.api.system.KeypadListener;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.component.AutoTextEditField;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.yuchting.yuchberry.client.recvMain;

final class WeiboUpdateManager extends Manager implements FieldChangeListener{
	
	public AutoTextEditField 	m_editTextArea	= new AutoTextEditField();
	
	public VerticalFieldManager m_editTextManager = new VerticalFieldManager(Manager.VERTICAL_SCROLL){
		
		public int getPreferredHeight(){
			return WeiboUpdateDlg.fsm_height - m_titleHeight - m_sendButton.getHeight() - 6;
		}
		
		public int getPreferredWidth(){
			return WeiboUpdateDlg.fsm_width - 2;
		}
		
		protected void sublayout(int width, int height){
			
			setPositionChild(m_editTextArea,0,0);
			layoutChild(m_editTextArea,getPreferredWidth(),999);
			
			setExtent(getPreferredWidth(),getPreferredHeight());
		}
	};
	
	weiboTimeLineScreen		m_timelineScreen;
	int						m_titleHeight 	= 0;
	int						m_separateLine_y = 0;
	WeiboButton				m_sendButton	= new WeiboButton(recvMain.sm_local.getString(localResource.WEIBO_SEND_LABEL),
																Field.FIELD_LEFT);
	
	public WeiboUpdateManager(weiboTimeLineScreen _timeline){
		super(Manager.VERTICAL_SCROLL);
		
		m_timelineScreen = _timeline;

		m_editTextArea.setMaxSize(WeiboItemField.fsm_maxWeiboTextLength);
		m_sendButton.setChangeListener(this);
		m_editTextArea.setChangeListener(this);
		
		m_editTextManager.add(m_editTextArea);
		
		add(m_editTextManager);
		add(m_sendButton);
		
		m_sendButton.layout(0, 0);
		
		m_titleHeight = m_editTextArea.getFont().getHeight() + 5;
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
		
		setPositionChild(m_editTextManager,0,m_titleHeight);
		layoutChild(m_editTextManager,m_editTextManager.getPreferredWidth(),m_editTextManager.getPreferredHeight());
		
		int t_buttonWidth = m_sendButton.getWidth();
		int t_buttonHeight = m_sendButton.getHeight();
		
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
	
	public final static int			fsm_width = recvMain.fsm_display_width - 40;
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
		
		m_updateManager.subpaint(_g);
		
		int color = _g.getColor();
		try{
			_g.setColor(WeiboItemField.fsm_darkColor);
			_g.drawLine(0,m_updateManager.m_separateLine_y,
						getPreferredWidth(),m_updateManager.m_separateLine_y);
			
			_g.setColor(WeiboItemField.fsm_selectedColor);
			_g.fillRect(0,0,getPreferredWidth(),m_updateManager.m_titleHeight);
						
			_g.setColor(0x02749f);
			_g.drawLine(0,m_updateManager.m_titleHeight - 1,getPreferredWidth(),m_updateManager.m_titleHeight - 1);
			_g.drawRect(0,0,getPreferredWidth(),getPreferredHeight());
			
			_g.setColor(0xffffff);
			
			String t_str = recvMain.sm_local.getString(localResource.WEIBO_UPDATE_DIALOG_TITLE) 
				+ " (" + m_updateManager.m_editTextArea.getText().length() + ")";
			
			_g.drawText(t_str,0,(m_updateManager.m_titleHeight - WeiboItemField.sm_fontHeight) / 2 + 2);
			
		}finally{
			_g.setColor(color);
		}
	}
	
	public void close(){
		super.close();
		m_updateManager.m_timelineScreen.m_pushUpdateDlg = false;
	}		
}
