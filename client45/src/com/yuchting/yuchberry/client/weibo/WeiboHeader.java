package com.yuchting.yuchberry.client.weibo;

import net.rim.device.api.io.IOUtilities;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Graphics;

import com.yuchting.yuchberry.client.recvMain;
import com.yuchting.yuchberry.client.ui.ButtonSegImage;
import com.yuchting.yuchberry.client.ui.ImageUnit;

public class WeiboHeader extends Field{
	
	public final static int STATE_TIMELINE = 0;
	public final static int STATE_AT_ME = 1;
	public final static int STATE_COMMENT_ME = 2;
	public final static int STATE_DIRECT_MESSAGE = 3;	
	
	public final static int fsm_headHeight = 45;
	
	
	private final static int fsm_linkedStateSize = 20;
	
	private static Bitmap	sm_linkedStateBitmap =  null;
	private static Bitmap	sm_unlinkedStateBitmap =  null;
	static {
		try{
			byte[] bytes = IOUtilities.streamToBytes(weiboTimeLineScreen.sm_mainApp.getClass().getResourceAsStream("/linked_state.png"));		
			sm_linkedStateBitmap = EncodedImage.createEncodedImage(bytes, 0, bytes.length).getBitmap();	
			
			bytes = IOUtilities.streamToBytes(weiboTimeLineScreen.sm_mainApp.getClass().getResourceAsStream("/unlink_state.png"));
			sm_unlinkedStateBitmap = EncodedImage.createEncodedImage(bytes, 0, bytes.length).getBitmap();
			
		}catch(Exception e){
			weiboTimeLineScreen.sm_mainApp.DialogAlertAndExit("inner load error:" + e.getMessage());
		}	
	}
	
	private final static int fsm_stateBitmapInterval = 12;
	private final static int fsm_stateBitmapSize = 32;
	
	private final static int fsm_stateBitmapTop = (fsm_headHeight - fsm_stateBitmapSize) /2;
	
	private final static ImageUnit[] fsm_stateBitmap = 
	{
		null,null,null,null
	};
	
	private final static ImageUnit[] fsm_stateBitmap_hover = 
	{
		null,null,null,null
	};
	
	private final static String[] fsm_stateBitmapString = 
	{
		"home",
		"atMe",
		"commentMe",
		"directMsg",
	};
	
	private final static String[] fsm_stateBitmapString_hover = 
	{
		"home_hover",
		"atMe_hover",
		"commentMe_hover",
		"directMsg_hover",
	};
	
	private static ButtonSegImage sm_navigateBitmap = null;
		
	int	m_currState = STATE_TIMELINE;
	
	weiboTimeLineScreen m_parentScreen;	
	
	public WeiboHeader(weiboTimeLineScreen _screen){
		m_parentScreen = _screen;
		
		if(sm_navigateBitmap == null){
			for(int i = 0;i < fsm_stateBitmap.length;i++){
				fsm_stateBitmap[i] = weiboTimeLineScreen.sm_weiboUIImage.getImageUnit(fsm_stateBitmapString[i]);
				
				fsm_stateBitmap_hover[i] = weiboTimeLineScreen.sm_weiboUIImage.getImageUnit(fsm_stateBitmapString_hover[i]);
			}
			
			sm_navigateBitmap = new ButtonSegImage(
					weiboTimeLineScreen.sm_weiboUIImage.getImageUnit("nav_bar_left"),
					weiboTimeLineScreen.sm_weiboUIImage.getImageUnit("nav_bar_mid"),
					weiboTimeLineScreen.sm_weiboUIImage.getImageUnit("nav_bar_right"),
					weiboTimeLineScreen.sm_weiboUIImage);
		}
	}

	public void invalidate(){
		super.invalidate();
	}
	
	public int getCurrState(){
		return m_currState;
	}
	
	public void setCurrState(int _state){
		if(_state >= STATE_TIMELINE && _state <= STATE_DIRECT_MESSAGE){
			m_currState = _state;
		}
	}
	
	protected void layout(int _width,int _height){
        setExtent(recvMain.fsm_display_width,fsm_headHeight);
    }
	
	protected void paint( Graphics g ){
				
		int color = g.getColor();
		try{
			
			if(m_parentScreen.GetOnlineState()){
				g.drawBitmap(0,0,recvMain.fsm_display_width,fsm_headHeight,sm_linkedStateBitmap,0,0);
			}else{
				g.drawBitmap(0,0,recvMain.fsm_display_width,fsm_headHeight,sm_unlinkedStateBitmap,0,0);
			}
			
			sm_navigateBitmap.draw(g,0,0,recvMain.fsm_display_width);
			
			int t_x = fsm_linkedStateSize;
			boolean t_drawNewMsgSign = false;
			
			for(int i = 0 ;i <= STATE_DIRECT_MESSAGE;i++){
				
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
				}
				if(m_currState == i){
					// hover
					//
					weiboTimeLineScreen.sm_weiboUIImage.drawImage(g,fsm_stateBitmap_hover[i],t_x,fsm_stateBitmapTop);
				}else{
					// normal
					//
					weiboTimeLineScreen.sm_weiboUIImage.drawImage(g,fsm_stateBitmap[i],t_x,fsm_stateBitmapTop);
				}				
				
				if(t_drawNewMsgSign){
					// draw a new message sign
					//
					weiboTimeLineScreen.sm_weiboUIImage.drawImage(g,weiboTimeLineScreen.GetBBerSignBitmap(),t_x,fsm_stateBitmapTop);
				}
				
				t_x += recvMain.fsm_display_width / fsm_stateBitmap.length;
			}
			
		}finally{
			g.setColor(color);
		}
		
    }
}
