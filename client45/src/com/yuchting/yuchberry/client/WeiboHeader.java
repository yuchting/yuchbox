package com.yuchting.yuchberry.client;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Graphics;

public class WeiboHeader extends Field{
	
	final static int STATE_TIMELINE = 0;
	final static int STATE_AT_ME = 1;
	final static int STATE_COMMENT_ME = 2;
	final static int STATE_DIRECT_MESSAGE = 3;
	
	final static String[] fsm_stateString = 
	{
		" Home ",
		" @Me ",
		" Comment ",
		" DM ",
	};
	
	final static int[] fsm_stateStringWidth = 
	{
		0,0,0,0
	};
	
	final static int	fsm_headHeight = 30;
	
	int	m_currState = STATE_TIMELINE;
	
	weiboTimeLineScreen m_parentScreen;
		
	public WeiboHeader(weiboTimeLineScreen _screen){
		m_parentScreen = _screen;
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