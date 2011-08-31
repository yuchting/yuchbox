package com.yuchting.yuchberry.client.weibo;

import net.rim.device.api.ui.Graphics;

import com.yuchting.yuchberry.client.recvMain;
import com.yuchting.yuchberry.client.ui.SliderHeader;

public class WeiboHeader extends SliderHeader{
	
	public final static int STATE_TIMELINE = 0;
	public final static int STATE_AT_ME = 1;
	public final static int STATE_COMMENT_ME = 2;
	public final static int STATE_DIRECT_MESSAGE = 3;	
	public final static int STATE_WEIBO_USER = 4;
		
	private final static String[] fsm_stateBitmapString = 
	{
		"home",
		"atMe",
		"commentMe",
		"directMsg",
		"weiboUser",
	};
	
	private final static String[] fsm_stateBitmapString_hover = 
	{
		"home_hover",
		"atMe_hover",
		"commentMe_hover",
		"directMsg_hover",
		"weiboUser_hover",
	};
	
	weiboTimeLineScreen m_parentScreen;	
	
	public WeiboHeader(weiboTimeLineScreen _screen){
		super(_screen.m_mainApp,fsm_stateBitmapString,fsm_stateBitmapString_hover,
			new int[][]
		    {
				{0x68,0xff,0x59,0xff,0x00},
				{0xd7,0xa8,0xea,0xe4,0x94},
				{0x03,0x06,0xfb,0x00,0xf2},
			});
		
		m_parentScreen = _screen;
	}	
	
	
	protected void paint( Graphics g ){
		super.paint(g);
			
		int t_x = fsm_linkedStateSize;
		boolean t_drawNewMsgSign = false;
		
		for(int i = 0 ;i < fsm_stateBitmapString.length;i++){
			
			t_drawNewMsgSign = false;
			
			switch(i){
			case STATE_TIMELINE:
				t_drawNewMsgSign = m_parentScreen.m_mainMgr.hasNewWeibo();
				break;
			case STATE_AT_ME:
				t_drawNewMsgSign = m_parentScreen.m_mainAtMeMgr.hasNewWeibo();
				break;
			case STATE_COMMENT_ME:
				t_drawNewMsgSign = m_parentScreen.m_mainCommitMeMgr.hasNewWeibo();
				break;
			case STATE_DIRECT_MESSAGE:
				t_drawNewMsgSign = m_parentScreen.m_mainDMMgr.hasNewWeibo();
				break;
			case STATE_WEIBO_USER:
				break;
			}
			
			if(t_drawNewMsgSign){
				// draw a new message sign
				//
				recvMain.sm_weiboUIImage.drawImage(g,weiboTimeLineScreen.GetBBerSignBitmap(),t_x,fsm_stateBitmapTop);
			}
			
			t_x += recvMain.fsm_display_width / m_stateBitmap.length;
		}
    }
}
