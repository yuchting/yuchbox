package com.yuchting.yuchberry.client.weibo;


import local.localResource;
import net.rim.device.api.system.Characters;
import net.rim.device.api.system.KeypadListener;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.component.AutoTextEditField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.yuchting.yuchberry.client.recvMain;


public class WeiboUpdateDlg extends PopupScreen implements FieldChangeListener{

	private final static int			fsm_width = recvMain.fsm_display_width - 10;
	private final static int			fsm_height = (recvMain.fsm_display_height - 30 > 240?240:(recvMain.fsm_display_height - 30));
	
	public AutoTextEditField 	m_editTextArea			= new AutoTextEditField(){
		public int getPreferredHeight(){
			return fsm_height - 60;
		}
		
		public int getPreferredWidth(){
			return fsm_width - 35;
		}
		
		protected void layout(int width, int height){
			super.layout(getPreferredWidth(), getPreferredHeight());
							
			setExtent(getPreferredWidth(),getPreferredHeight());
		}
	};

	
	weiboTimeLineScreen		m_timelineScreen;
	
	ButtonField				m_sendButton	= new ButtonField(recvMain.sm_local.getString(localResource.WEIBO_SEND_LABEL),
																Field.FIELD_RIGHT);
	
	public WeiboUpdateDlg(weiboTimeLineScreen _screen){
		super(new VerticalFieldManager(Manager.NO_VERTICAL_SCROLL),Field.FOCUSABLE);
		
		m_timelineScreen = _screen;
		
		m_editTextArea.setMaxSize(WeiboItemField.fsm_maxWeiboTextLength);
		m_sendButton.setChangeListener(this);
		
		add(m_editTextArea);
		add(new SeparatorField());
		add(m_sendButton);
	}
		
	public int getPreferredHeight(){
		return fsm_height;
	}
	
	public int getPreferredWidth(){
		return fsm_width;
	}
	
	public void fieldChanged(Field field, int context) {
		if(field == m_sendButton){
			m_editTextArea.setFocus();
			sendUpdate();			
		}
	}
	
	protected void sublayout(int width, int height){
		super.sublayout(getPreferredWidth(), getPreferredHeight());
						
		setPosition((recvMain.fsm_display_width - getPreferredWidth()) / 2 ,
				(recvMain.fsm_display_height - getPreferredHeight()) / 2);
	}
	
	public void close(){
		super.close();
		m_timelineScreen.m_pushUpdateDlg = false;
	}
	
	protected boolean keyChar(char c,int status,int time){
		if(c == Characters.ESCAPE){
			close();
		}else if(c == Characters.ENTER){
			if(m_editTextArea.getText().length() != 0 &&((status & KeypadListener.STATUS_SHIFT) != 0)){
				sendUpdate();					
			}
			// consum the Enter key
			//
			return true;
		}
		
		return super.keyChar(c,status,time);
	}
	
	private void sendUpdate(){
		if(m_editTextArea.getText().length() != 0){
			close();
			m_timelineScreen.UpdateNewWeibo(m_editTextArea.getText());
			m_editTextArea.setText("");
		}
	}
}
