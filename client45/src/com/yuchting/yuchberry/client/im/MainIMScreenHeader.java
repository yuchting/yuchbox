package com.yuchting.yuchberry.client.im;

import net.rim.device.api.ui.Graphics;

import com.yuchting.yuchberry.client.recvMain;
import com.yuchting.yuchberry.client.ui.SliderHeader;

public class MainIMScreenHeader extends SliderHeader{

	public final static int STATE_HISTORY_CHAT = 0;
	public final static int STATE_ROSTER_LIST = 1;
	public final static int STATE_STATUS_LIST = 2;
			
	private final static String[] fsm_stateBitmapString = 
	{
		"commentMe_1",
		"weiboUser_1",
		"statusIM",
	};	
	
	MainIMScreen	m_parentScreen = null;
	public MainIMScreenHeader(MainIMScreen _parentScreen){
		super(_parentScreen.m_mainApp,fsm_stateBitmapString);
		
		m_parentScreen = _parentScreen;
	}
	
	protected void paint( Graphics g ){
		super.paint(g);
		
		if(m_parentScreen.m_hasNewChatMsg){
			
			// draw a new message sign
			//
			recvMain.sm_weiboUIImage.drawImage(g,GetBBerSignBitmap(),0,fsm_stateBitmapTop);
		}
    }
}
