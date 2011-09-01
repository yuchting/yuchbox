package com.yuchting.yuchberry.client.im;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;

import com.yuchting.yuchberry.client.recvMain;
import com.yuchting.yuchberry.client.ui.BubbleImage;
import com.yuchting.yuchberry.client.ui.WeiboTextField;

public class ChatField extends Manager{
	
	public final static int	fsm_offsetWidth = 10;
	public final static int	fsm_bubblePointWidth = 8;
	public final static int	fsm_border		= 3;
	
	public final static int	fsm_textWidth 	= recvMain.fsm_display_width - fsm_offsetWidth - fsm_border * 2 - fsm_bubblePointWidth;
	
	public final static int	fsm_ownChatTextBGColor		= 0xd8d8d8;
	public final static int	fsm_otherChatTextBGColor	= 0xadadad;	
	
	fetchChatMsg			m_msg 			= null;
	int						m_msgTextHeight = 0;
	
	WeiboTextField			m_textfield 	= null;
	
	public ChatField(fetchChatMsg _msg){
		super(Field.FOCUSABLE | Manager.NO_VERTICAL_SCROLL);
		m_msg = _msg;
		
		MainIMScreen.sm_testTextArea.setText(_msg.getMsg());
		m_msgTextHeight = MainIMScreen.sm_testTextArea.getHeight();
						
		if(_msg.isOwnMsg()){
			m_textfield = new WeiboTextField(0,fsm_ownChatTextBGColor);
		}else{
			m_textfield = new WeiboTextField(0,fsm_otherChatTextBGColor);
		}
		
		m_textfield.setText(_msg.getMsg());
		
		add(m_textfield);		
	}
	
	public int getPreferredWidth(){
		return recvMain.fsm_display_width;
	}
	
	public int getPreferredHeight(){
		return m_msgTextHeight + fsm_border * 2;
	}
	
	protected void sublayout(int _width, int _height){
		
		int t_x = 0;
		
		if(m_msg.isOwnMsg()){
			t_x = fsm_offsetWidth + fsm_border;
		}else{
			t_x = fsm_border + fsm_bubblePointWidth;
		}
		
		setPositionChild(m_textfield,t_x,fsm_border);
		layoutChild(m_textfield,fsm_textWidth,m_msgTextHeight);

		setExtent(recvMain.fsm_display_width,getPreferredHeight());
	}
	
	protected void subpaint(Graphics _g){

		if(m_msg.isOwnMsg()){
						
			recvMain.sm_bubbleImage.draw(_g, fsm_offsetWidth, 0, 
					recvMain.fsm_display_width - fsm_offsetWidth - fsm_bubblePointWidth, getPreferredHeight(), 
					BubbleImage.RIGHT_POINT_STYLE);
		}else{
						
			recvMain.sm_bubbleImage_black.draw(_g,fsm_bubblePointWidth, 0, 
					recvMain.fsm_display_width - fsm_offsetWidth - fsm_bubblePointWidth, getPreferredHeight(), 
					BubbleImage.LEFT_POINT_STYLE);
		}
		
		super.subpaint(_g);
	}
}
