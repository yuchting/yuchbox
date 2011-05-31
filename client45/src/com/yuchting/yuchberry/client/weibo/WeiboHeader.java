package com.yuchting.yuchberry.client.weibo;

import net.rim.device.api.io.IOUtilities;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Graphics;

import com.yuchting.yuchberry.client.recvMain;

public class WeiboHeader extends Field{
	
	public final static int STATE_TIMELINE = 0;
	public final static int STATE_AT_ME = 1;
	public final static int STATE_COMMENT_ME = 2;
	public final static int STATE_DIRECT_MESSAGE = 3;
	
	public final static String[] fsm_stateString = 
	{
		" H\u0332ome ",
		" @M\u0332e ",
		" C\u0332omment ",
		" D\u0332M ",
	};
	
	public final static int[] fsm_stateStringWidth = 
	{
		0,0,0,0
	};
	
	final static int	fsm_headHeight = 27;
	
	int	m_currState = STATE_TIMELINE;
	
	weiboTimeLineScreen m_parentScreen;
	
	Bitmap				m_backgroundBMP	= null;
	Bitmap				m_backgroundBMP_offline	= null;
	
	public WeiboHeader(weiboTimeLineScreen _screen){
		m_parentScreen = _screen;
		
		try{
			byte[] bytes = IOUtilities.streamToBytes(weiboTimeLineScreen.sm_mainApp.getClass().getResourceAsStream("/headBg.png"));		
			m_backgroundBMP = EncodedImage.createEncodedImage(bytes, 0, bytes.length).getBitmap();	
			
			bytes = IOUtilities.streamToBytes(weiboTimeLineScreen.sm_mainApp.getClass().getResourceAsStream("/headBg_offline.png"));
			m_backgroundBMP_offline = EncodedImage.createEncodedImage(bytes, 0, bytes.length).getBitmap();
			
		}catch(Exception e){
			weiboTimeLineScreen.sm_mainApp.SetErrorString("GBSB:" + e.getMessage() + e.getClass().getName());
		}		
	}
	
	private void calculateStringWidth(Graphics g){
		
		// calculate the width of the string
		//
		for(int i = 0 ;i < fsm_stateString.length;i++){
			fsm_stateStringWidth[i] = g.drawText(fsm_stateString[i],0,recvMain.fsm_display_width);
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
		
		if(fsm_stateStringWidth[0] == 0){
			calculateStringWidth(g);
		}
		
		int color = g.getColor();
		try{
			
			if(m_parentScreen.GetOnlineState()){
				g.drawBitmap(0,0,recvMain.fsm_display_width,fsm_headHeight,m_backgroundBMP,0,0);
			}else{
				g.drawBitmap(0,0,recvMain.fsm_display_width,fsm_headHeight,m_backgroundBMP_offline,0,0);
			}		
			
			int t_x = 1;
			for(int i = 0 ;i <= STATE_DIRECT_MESSAGE;i++){
				
				boolean t_drawNewMsgSign = false;
				
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
					
					break;
				}
				
				if(m_currState == i){
					
					g.setColor(0x666666);
					g.fillRoundRect(t_x,1,fsm_stateStringWidth[i], fsm_headHeight - 2,5,5);
					g.setColor(0xefefef);
					
				}else{
					g.setColor(0);
				}
				
				g.drawText(fsm_stateString[i],t_x,2,Graphics.ELLIPSIS);
				
				if(t_drawNewMsgSign){
					// draw a new message sign
					//
					g.drawBitmap(t_x,1,WeiboItemField.fsm_weiboBBerImageSize,WeiboItemField.fsm_weiboBBerImageSize,
							weiboTimeLineScreen.GetBBerSignBitmap(),0,0);
				}
				
				t_x += fsm_stateStringWidth[i];
			}
			
		}finally{
			g.setColor(color);
		}
		
    }
}
