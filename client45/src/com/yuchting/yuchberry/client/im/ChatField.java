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
	public final static int	fsm_border		= 6;
	
	public final static int	fsm_maxTextWidth 	= recvMain.fsm_display_width - fsm_offsetWidth - fsm_border * 2 - fsm_bubblePointWidth;
	
	public final static int	fsm_ownChatTextBGColor		= 0xd8d8d8;
	public final static int	fsm_otherChatTextBGColor	= 0xadadad;	
	
	fetchChatMsg			m_msg 			= null;
	int						m_msgTextHeight = 0;
	int						m_msgTextWidth	= 0;
	WeiboTextField			m_textfield 	= null;
	
	public ChatField(fetchChatMsg _msg){
		super(Field.FOCUSABLE | Manager.NO_VERTICAL_SCROLL);
		m_msg = _msg;
		
		String t_converText = WeiboTextField.getConvertString(_msg.getMsg());
		m_msgTextWidth = MainIMScreen.sm_defaultFont.getAdvance(t_converText);
		if(m_msgTextWidth > fsm_maxTextWidth){
			m_msgTextWidth = fsm_maxTextWidth;
		}
		
		MainIMScreen.sm_testTextArea.setPreferredWidth(m_msgTextWidth);
		
		MainIMScreen.sm_testTextArea.setText(t_converText);
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
			t_x = recvMain.fsm_display_width - m_msgTextWidth - fsm_border - fsm_bubblePointWidth;
		}else{
			t_x = fsm_border + fsm_bubblePointWidth;
		}
		
		setPositionChild(m_textfield,t_x,fsm_border);
		layoutChild(m_textfield,m_msgTextWidth,m_msgTextHeight);

		setExtent(recvMain.fsm_display_width,getPreferredHeight());
	}
	
	protected void subpaint(Graphics _g){

		int t_bubbleWidth = m_msgTextWidth + fsm_border * 2;
		
		if(m_msg.isOwnMsg()){
			
			int t_x = recvMain.fsm_display_width - t_bubbleWidth - fsm_bubblePointWidth;
			
			recvMain.sm_bubbleImage.draw(_g, t_x, 0,
					t_bubbleWidth, getPreferredHeight(), 
					BubbleImage.RIGHT_POINT_STYLE);
		}else{
						
			recvMain.sm_bubbleImage_black.draw(_g,fsm_bubblePointWidth, 0, 
					t_bubbleWidth, getPreferredHeight(), 
					BubbleImage.LEFT_POINT_STYLE);
		}
		
		super.subpaint(_g);
	}
}
