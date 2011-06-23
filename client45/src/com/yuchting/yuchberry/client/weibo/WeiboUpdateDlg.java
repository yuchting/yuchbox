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

import com.yuchting.yuchberry.client.recvMain;

final class WeiboUpdateManager extends Manager implements FieldChangeListener{
	
	public AutoTextEditField 	m_editTextArea			= new AutoTextEditField(){
		public int getPreferredHeight(){
			return WeiboUpdateDlg.fsm_height - 
					WeiboUpdateManager.this.m_titleHeight - 
					WeiboUpdateManager.this.m_sendButton.getPreferredHeight();
		}
		
		public int getPreferredWidth(){
			return WeiboUpdateDlg.fsm_width - 2;
		}
		
		protected void layout(int width, int height){
			super.layout(getPreferredWidth(), getPreferredHeight());			
			setExtent(getPreferredWidth(),getPreferredHeight());
		}
	};
	
	weiboTimeLineScreen		m_timelineScreen;
	int						m_titleHeight 	= 0;
	WeiboButton				m_sendButton	= new WeiboButton(recvMain.sm_local.getString(localResource.WEIBO_SEND_LABEL),
																Field.FIELD_LEFT);
	
	public WeiboUpdateManager(weiboTimeLineScreen _timeline){
		super(Manager.NO_VERTICAL_SCROLL);
		
		m_timelineScreen = _timeline;

		m_editTextArea.setMaxSize(WeiboItemField.fsm_maxWeiboTextLength);
		m_sendButton.setChangeListener(this);
		
		add(m_editTextArea);
		add(m_sendButton);
		
		m_titleHeight = m_editTextArea.getFont().getHeight() + 5;
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
		}
	}
	
	public void sublayout(int width, int height){
		
		setPositionChild(m_editTextArea,0,m_titleHeight);
		layoutChild(m_editTextArea,m_editTextArea.getPreferredWidth(),m_editTextArea.getPreferredHeight());
		
		setPositionChild(m_sendButton,0,m_editTextArea.getPreferredHeight() + m_titleHeight);		
		layoutChild(m_sendButton,m_sendButton.getPreferredWidth(),m_sendButton.getPreferredHeight());
		
		setExtent(getPreferredWidth(), getPreferredHeight());
	}
	
	public void subpaint(Graphics graphics){
		super.subpaint(graphics);
	}
	
	
	public void sendUpdate(){
		if(m_editTextArea.getText().length() != 0){
			getScreen().close();
			m_timelineScreen.UpdateNewWeibo(m_editTextArea.getText());
			m_editTextArea.setText("");
		}
	}
}

public class WeiboUpdateDlg extends Screen {
	
	public final static int			fsm_width = recvMain.fsm_display_width - 40;
	public final static int			fsm_height = (recvMain.fsm_display_height - 30 > 300?300:(recvMain.fsm_display_height - 30));
		
	WeiboUpdateManager		m_updateManager;
	public WeiboUpdateDlg(weiboTimeLineScreen _screen){
		super(new WeiboUpdateManager(_screen));
		m_updateManager = (WeiboUpdateManager)getDelegate();
	}
	
	public int getPreferredHeight(){
		return fsm_height;
	}
	
	public int getPreferredWidth(){
		return fsm_width;
	}
	
	protected void sublayout(int width, int height){
		
		m_updateManager.sublayout(m_updateManager.getPreferredWidth(), m_updateManager.getPreferredWidth());
		
		setExtent(getPreferredWidth(), getPreferredHeight());		
		setPosition((recvMain.fsm_display_width - getPreferredWidth()) / 2 ,
				(recvMain.fsm_display_height - getPreferredHeight()) / 2);
	}
	
	protected void paint(Graphics _g){		
		super.paint(_g);
		
		int color = _g.getColor();
		try{
			_g.setColor(WeiboItemField.fsm_selectedColor);
			
			_g.fillRect(0,0,getPreferredWidth(),m_updateManager.m_titleHeight);
			_g.drawRect(0,0,getPreferredWidth(),getPreferredHeight());
			
			_g.setColor(0xffffff);
			_g.drawText("YB Weibo Update",0,0);
			
		}finally{
			_g.setColor(color);
		}	
	}
	
	public void close(){
		super.close();
		m_updateManager.m_timelineScreen.m_pushUpdateDlg = false;
	}	
	
	protected boolean keyChar(char c,int status,int time){
		if(c == Characters.ESCAPE){
			getScreen().close();
		}else if(c == Characters.ENTER){
			if(m_updateManager.m_editTextArea.getText().length() != 0 &&((status & KeypadListener.STATUS_SHIFT) != 0)){
				m_updateManager.sendUpdate();					
			}
			// consum the Enter key
			//
			return true;
		}
		
		return super.keyChar(c,status,time);
	}
	
}
