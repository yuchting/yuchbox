package com.yuchting.yuchberry.client.im;

import net.rim.device.api.ui.Graphics;

import com.yuchting.yuchberry.client.recvMain;
import com.yuchting.yuchberry.client.ui.SliderHeader;

public class MainIMScreenHeader extends SliderHeader{

	public final static int STATE_HISTORY_CHAT = 0;
	public final static int STATE_ROSTER_LIST = 1;
			
	private final static String[] fsm_stateBitmapString = 
	{
		"commentMe",
		"weiboUser",
	};
	
	private final static String[] fsm_stateBitmapString_hover = 
	{
		"commentMe_hover",
		"weiboUser_hover",
	};
	
	MainIMScreen	m_parentScreen = null;
	public MainIMScreenHeader(MainIMScreen _parentScreen){
		super(_parentScreen.m_mainApp,fsm_stateBitmapString,fsm_stateBitmapString_hover,
				new int[][]
			    {
					{0x59,0x00},
					{0xea,0x94},
					{0xfb,0xf2},
				});
		
		m_parentScreen = _parentScreen;
	}
	
	protected void paint( Graphics g ){
		super.paint(g);
		
		if(m_parentScreen.m_hasNewChatMsg){
			
			// draw a new message sign
			//
			recvMain.sm_weiboUIImage.drawImage(g,GetBBerSignBitmap(),fsm_linkedStateSize,fsm_stateBitmapTop);
		}
    }
}
