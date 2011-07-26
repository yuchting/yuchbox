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
		
	private int	m_currState = STATE_TIMELINE;
	
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
	
	private final static int fsm_moveSliderHeight = 4;
	private final static int	fsm_moveTime = 50;
	private final static int[][]	fsm_moveColor = 
	{
		{0x68,0xff,0x59,0xff},
		{0xd7,0xa8,0xea,0xe4},
		{0x03,0x06,0xfb,0x00},
	};
	
	int					m_curr_x = fsm_linkedStateSize;
	int					m_curr_color = (fsm_moveColor[0][0] << 16) | (fsm_moveColor[1][0] << 8) | (fsm_moveColor[2][0]);
	int					m_former_x = fsm_linkedStateSize;
	int					m_former_state = 0;
	int					m_dest_x = 0;
	int					m_animationRunId = -1;
	int					m_deaccel_delta = 0; 
	
	public void setCurrState(int _state){
		if(_state >= STATE_TIMELINE && _state <= STATE_DIRECT_MESSAGE){
			
			if(m_currState != _state){
				
				m_former_state = m_currState;
				m_currState = _state;
									
				m_deaccel_delta = 0;
				m_former_x = m_curr_x;
				m_dest_x = m_currState * (recvMain.fsm_display_width / fsm_stateBitmap.length) + fsm_linkedStateSize;
				
				synchronized (this) {
					
					if(m_animationRunId == -1){
					
						m_animationRunId = m_parentScreen.m_mainApp.invokeLater(new Runnable() {
							public void run() {
								int t_distance = Math.abs(m_former_x - m_dest_x);
								
								if(m_former_x < m_dest_x){
									m_curr_x += t_distance / 3 - m_deaccel_delta;
									
									if(m_curr_x >= m_dest_x){
										m_curr_x = m_dest_x;
									}
								}else{
									m_curr_x -= t_distance / 3 - m_deaccel_delta;
									
									if(m_curr_x <= m_dest_x){
										m_curr_x = m_dest_x;
									}
								}
								m_deaccel_delta += 5;
								
								int r,g,b;
								if(m_curr_x == m_dest_x){
									
									r = (fsm_moveColor[0][m_currState]);
									g = (fsm_moveColor[1][m_currState]);
									b = (fsm_moveColor[2][m_currState]);
																		
									synchronized (WeiboHeader.this) {
										WeiboHeader.this.m_parentScreen.m_mainApp.cancelInvokeLater(m_animationRunId);
										m_animationRunId = -1;
									}
									
								}else{
									r = (fsm_moveColor[0][m_currState] - fsm_moveColor[0][m_former_state]);
									r = (m_curr_x - m_former_x) * r / t_distance  + fsm_moveColor[0][m_former_state];
									
									g = (fsm_moveColor[1][m_currState] - fsm_moveColor[1][m_former_state]);
									g = (m_curr_x - m_former_x) * g / t_distance  + fsm_moveColor[1][m_former_state];
									
									b = (fsm_moveColor[2][m_currState] - fsm_moveColor[2][m_former_state]);
									b = (m_curr_x - m_former_x) * b / t_distance  + fsm_moveColor[2][m_former_state];
								}
								
								m_curr_color = (r << 16)| (g << 8) | b;
								
								WeiboHeader.this.invalidate(0,1,recvMain.fsm_display_width,fsm_moveSliderHeight);
							}
						},fsm_moveTime,true);
						
						// cant be allocated a usefull runnable id
						//
						if(m_animationRunId == -1){
							m_curr_x = m_dest_x;
							int r,g,b;
							r = (fsm_moveColor[0][m_currState]);
							g = (fsm_moveColor[1][m_currState]);
							b = (fsm_moveColor[2][m_currState]);
							m_curr_color = (r << 16)| (g << 8) | b;	
						}
					}
				}
			}
			
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
			
			if(m_parentScreen.GetOnlineState()){
				g.setColor(m_curr_color);
			}else{
				g.setColor(WeiboItemField.fsm_darkColor);
			}
			
			g.fillRoundRect(m_curr_x,1,fsm_stateBitmap[0].getWidth(),fsm_moveSliderHeight,2,2);			
			
		}finally{
			g.setColor(color);
		}
		
    }
}
