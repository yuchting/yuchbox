package com.yuchting.yuchberry.client.im;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Graphics;

import com.yuchting.yuchberry.client.recvMain;
import com.yuchting.yuchberry.client.weibo.fetchWeibo;

public class RosterItemField extends Field{

	public final static int		fsm_rosterItemFieldWidth	= recvMain.fsm_display_width;
	public final static boolean	fsm_largeHeadImage			= recvMain.fsm_display_width > 320;
	
	public final static int		fsm_headImageWidth 			= recvMain.fsm_display_width>320?fetchWeibo.fsm_headImageSize_l:fetchWeibo.fsm_headImageSize;
	
	fetchChatRoster		m_currRoster;
	
	public RosterItemField(fetchChatRoster _roster){
		super(Field.FOCUSABLE);
		
		m_currRoster = _roster;
	}
	
	protected void layout(int _width,int _height){
		
		
	}
	
	protected void paint(Graphics _g){
		
	}
}
