package com.yuchting.yuchberry.client.im;

import java.util.Calendar;
import java.util.Date;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;

import com.yuchting.yuchberry.client.recvMain;
import com.yuchting.yuchberry.client.ui.BubbleImage;
import com.yuchting.yuchberry.client.ui.ImageUnit;
import com.yuchting.yuchberry.client.ui.WeiboTextField;

public class ChatField extends Manager{
		
	public final static int	fsm_offsetWidth = 32;
	public final static int	fsm_bubblePointWidth = 8;
	public final static int	fsm_border		= 6;
	
	public final static int	fsm_minTextWidth	= fsm_offsetWidth + fsm_bubblePointWidth;  
	public final static int	fsm_maxTextWidth 	= recvMain.fsm_display_width - fsm_offsetWidth - fsm_border * 2 - fsm_bubblePointWidth;
	
	public final static int	fsm_ownChatTextBGColor		= 0xd8d8d8;
	public final static int	fsm_otherChatTextBGColor	= 0xadadad;
	public final static int	fsm_timeTextBGColor			= 0xffffcc;
	public final static int	fsm_timeTextBorderColor		= 0xc0c0c0;
	
	fetchChatMsg			m_msg 			= null;
	int						m_msgTextHeight = 0;
	int						m_msgTextWidth	= 0;
	WeiboTextField			m_textfield 	= null;
	String					m_timeText		= null; 
	
	final static Calendar 	fsm_calendar 	= Calendar.getInstance();
	final static Date		fsm_timeDate 	= new Date();
	final static	Font	fsm_timeFont		= MainIMScreen.fsm_defaultFont.derive(MainIMScreen.fsm_defaultFont.getStyle(),14);
		
	final static String[]	fsm_stateImageStr = 
	{
		"padding_chat",
		"sending_chat",
		"sent_chat",
		"read_chat",
		"error_chat",
	};
	
	public static ImageUnit[]	sm_stateImage = new ImageUnit[fsm_stateImageStr.length];
	static {
		try{
			for(int i = 0 ;i < fsm_stateImageStr.length;i++){
				sm_stateImage[i] = recvMain.sm_weiboUIImage.getImageUnit(fsm_stateImageStr[i]);
			}
		}catch(Exception e){}
	}
	
	public ChatField(fetchChatMsg _msg){
		super(Field.FOCUSABLE | Manager.NO_VERTICAL_SCROLL);
		m_msg = _msg;
				
		String t_converText = WeiboTextField.getConvertString(_msg.getMsg());
		m_msgTextWidth = MainIMScreen.fsm_defaultFont.getAdvance(t_converText);
		
		if(m_msgTextWidth < fsm_minTextWidth){
			m_msgTextWidth = fsm_minTextWidth;
		}
		
		if(m_msgTextWidth > fsm_maxTextWidth){
			m_msgTextWidth = fsm_maxTextWidth;
		}
				
		MainIMScreen.fsm_testTextArea.setPreferredWidth(m_msgTextWidth);
		
		MainIMScreen.fsm_testTextArea.setText(t_converText);
		m_msgTextHeight = MainIMScreen.fsm_testTextArea.getHeight();
						
		if(_msg.isOwnMsg()){
			m_textfield = new WeiboTextField(0,fsm_ownChatTextBGColor);
		}else{
			m_textfield = new WeiboTextField(0,fsm_otherChatTextBGColor);
		}
		
		m_textfield.setText(_msg.getMsg());
		add(m_textfield);
		
		// generate the time string
		//
		fsm_timeDate.setTime(_msg.getSendTime());
		fsm_calendar.setTime(fsm_timeDate);
		m_timeText = Integer.toString(fsm_calendar.get(Calendar.HOUR_OF_DAY)) + ":" + fsm_calendar.get(Calendar.MINUTE);
	}
	
	public void setSendState(int _state){
		if(_state >= 0 && _state <= fetchChatMsg.SEND_STATE_READ){
			m_msg.setSendState(_state);
			
			invalidate();
		}
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
		
		int t_x = 0;
		
		int t_time_x = 0;
		int t_time_y = getPreferredHeight() - fsm_timeFont.getHeight() - 3;
		int t_time_width = fsm_timeFont.getAdvance(m_timeText);
		
		if(m_msg.isOwnMsg()){
			
			t_x = recvMain.fsm_display_width - t_bubbleWidth - fsm_bubblePointWidth;
			
			recvMain.sm_bubbleImage.draw(_g, t_x, 0, t_bubbleWidth, getPreferredHeight(), 
					BubbleImage.RIGHT_POINT_STYLE);
			
			t_time_x = t_x - t_time_width + 5;
			
		}else{
			
			t_x = fsm_bubblePointWidth;
			
			recvMain.sm_bubbleImage_black.draw(_g,t_x, 0, t_bubbleWidth, getPreferredHeight(), 
					BubbleImage.LEFT_POINT_STYLE);
			
			t_time_x = t_x + t_bubbleWidth - 5;
		}
		
		super.subpaint(_g);
		
		// draw the send state sign
		//
		if(m_msg.isOwnMsg()){
			recvMain.sm_weiboUIImage.drawImage(_g, sm_stateImage[m_msg.getSendState()], 
							t_x - sm_stateImage[m_msg.getSendState()].getWidth(), 0);
		}
		
		// draw the time string
		//
		int t_color = _g.getColor();
		Font t_font	= _g.getFont();
		try{
			_g.setColor(fsm_timeTextBGColor);
			_g.fillRoundRect(t_time_x,t_time_y, t_time_width, fsm_timeFont.getHeight(), 5, 5);
			
			_g.setColor(fsm_timeTextBorderColor);
			_g.drawRoundRect(t_time_x,t_time_y, t_time_width, fsm_timeFont.getHeight(), 5, 5);
			
			_g.setColor(0);
			
			_g.setFont(fsm_timeFont);
			_g.drawText(m_timeText,t_time_x,t_time_y);
			
		}finally{
			_g.setFont(t_font);
			_g.setColor(t_color);
		}
	}
}
