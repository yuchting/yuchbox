package com.yuchting.yuchberry.client.weibo;


import com.yuchting.yuchberry.client.recvMain;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.component.AutoTextEditField;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

public class WeiboUpdateDlg extends PopupScreen{

	private final static int			fsm_width = recvMain.fsm_display_width - 20;
	private final static int			fsm_height = recvMain.fsm_display_height - 50;
	
	public AutoTextEditField 	m_editTextArea			= new AutoTextEditField(){
		public void setText(String _text){
			super.setText(_text);
			layout(WeiboItemField.fsm_weiboItemFieldWidth,1000);
		}
	};
	
	weiboTimeLineScreen		m_timelineScreen;
	
	public WeiboUpdateDlg(weiboTimeLineScreen _screen){
		super(new VerticalFieldManager(Manager.VERTICAL_SCROLL),Field.FOCUSABLE);
		
		m_timelineScreen = _screen; 
		add(m_editTextArea);
	}
	
	public int getPreferredHeight(){
		return fsm_height;
	}
	
	public int getPreferredWidth(){
		return fsm_width;
	}
}
