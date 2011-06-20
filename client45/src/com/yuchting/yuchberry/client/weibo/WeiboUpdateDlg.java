package com.yuchting.yuchberry.client.weibo;


import local.localResource;
import net.rim.device.api.system.Characters;
import net.rim.device.api.system.KeyListener;
import net.rim.device.api.system.KeypadListener;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.component.AutoTextEditField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.yuchting.yuchberry.client.recvMain;


public class WeiboUpdateDlg extends PopupScreen{

	private final static int			fsm_width = recvMain.fsm_display_width - 10;
	private final static int			fsm_height = recvMain.fsm_display_height - 30;
	
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
		super(new VerticalFieldManager(Manager.VERTICAL_SCROLL),Field.FOCUSABLE);
		
		m_timelineScreen = _screen; 
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
	
	protected void sublayout(int width, int height){
		super.sublayout(getPreferredWidth(), getPreferredHeight());
						
		setPosition((recvMain.fsm_display_width - getPreferredWidth()) / 2 ,
				(recvMain.fsm_display_height - getPreferredHeight()) / 2);
	}
	
	public void close(){
		super.close();
		m_timelineScreen.m_currUpdateDlg = null;
	}
	
	protected boolean keyChar(char c,int status,int time){
		if(c == Characters.ESCAPE){
			close();
		}else if(c == Characters.ENTER && ((status & KeypadListener.STATUS_SHIFT) != 0)){
			if(m_editTextArea.getText().length() != 0){
				close();
				m_timelineScreen.UpdateNewWeibo(m_editTextArea.getText());
			}			
		}
		
		return super.keyChar(c,status,time);
	}
	
	protected  void	onUnfocus() {
		super.onUnfocus();
		close();	
	}	
}
