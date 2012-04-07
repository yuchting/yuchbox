/**
 *  Dear developer:
 *  
 *   If you want to modify this file of project and re-publish this please visit:
 *  
 *     http://code.google.com/p/yuchberry/wiki/Project_files_header
 *     
 *   to check your responsibility and my humble proposal. Thanks!
 *   
 *  -- 
 *  Yuchs' Developer    
 *  
 *  
 *  
 *  
 *  尊敬的开发者：
 *   
 *    如果你想要修改这个项目中的文件，同时重新发布项目程序，请访问一下：
 *    
 *      http://code.google.com/p/yuchberry/wiki/Project_files_header
 *      
 *    了解你的责任，还有我卑微的建议。 谢谢！
 *   
 *  -- 
 *  语盒开发者
 *  
 */
package com.yuchting.yuchberry.client.im;

import java.util.Calendar;
import java.util.Date;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;

import com.yuchting.yuchberry.client.ObjectAllocator;
import com.yuchting.yuchberry.client.recvMain;
import com.yuchting.yuchberry.client.ui.BubbleImage;
import com.yuchting.yuchberry.client.ui.ImageUnit;
import com.yuchting.yuchberry.client.ui.WeiboTextField;



public class ChatField extends Manager{
	
	public interface IChatFieldOpen{
		public void open(fetchChatMsg msg);
	}
		
	public final static int	fsm_offsetWidth = 32;
	public final static int	fsm_bubblePointWidth = 8;
	public final static int	fsm_border		= 6;
	
	public final static int	fsm_minTextWidth		= fsm_offsetWidth + fsm_bubblePointWidth;
	public final static int	fsm_maxTextWidth 		= recvMain.fsm_display_width - fsm_offsetWidth - fsm_border * 2 - fsm_bubblePointWidth - 12;//修正时间出界//

	
	public final static int	fsm_ownChatTextBGColor		= 0xd6efff; 
	public final static int	fsm_otherChatTextBGColor	= 0xe7ebf7; 
	
	public final static int	fsm_ownChatTextFGColor		= 0; 
	public final static int	fsm_otherChatTextFGColor	= 0; 

	public final static int	fsm_timeTextBGColor			= 0xffffff; 
//	public final static int	fsm_timeTextBorderColor		= 0xc0c0c0;  					//

	
	private static ObjectAllocator sm_textFieldAllocator = new ObjectAllocator("com.yuchting.yuchberry.client.ui.WeiboTextField"){
		protected Object newInstance()throws Exception{
			return new WeiboTextField(0,0,true);
		}
	};
	
	private static ObjectAllocator sm_imageFieldAllocator = new ObjectAllocator("com.yuchting.yuchberry.client.im.ChatImageField");
	private static ObjectAllocator sm_voiceFieldAllocator = new ObjectAllocator("com.yuchting.yuchberry.client.im.ChatVoiceField");
			
	fetchChatMsg			m_msg 			= null;
	int						m_msgTextHeight = 0;
	int						m_msgTextWidth	= 0;
	private WeiboTextField			m_textfield 	= null;
	private ChatImageField			m_imagefield 	= null;
	private ChatVoiceField			m_voiceField	= null;
	
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
	
	public static BubbleImage sm_otherChatBubble = new BubbleImage(
			recvMain.sm_weiboUIImage.getImageUnit("other_top_left"),
			recvMain.sm_weiboUIImage.getImageUnit("other_top"),
			recvMain.sm_weiboUIImage.getImageUnit("other_top_right"),
			recvMain.sm_weiboUIImage.getImageUnit("other_right"),
			
			recvMain.sm_weiboUIImage.getImageUnit("other_bottom_right"),
			recvMain.sm_weiboUIImage.getImageUnit("other_bottom"),
			recvMain.sm_weiboUIImage.getImageUnit("other_bottom_left"),
			recvMain.sm_weiboUIImage.getImageUnit("other_left"),
			
			recvMain.sm_weiboUIImage.getImageUnit("other_inner_block"),
			new ImageUnit[]{
				recvMain.sm_weiboUIImage.getImageUnit("other_point"),
				null,
				null,
				null,
			},
			recvMain.sm_weiboUIImage);
	
	public static BubbleImage sm_ownChatBubble = new BubbleImage(
			recvMain.sm_weiboUIImage.getImageUnit("own_top_left"),
			recvMain.sm_weiboUIImage.getImageUnit("own_top"),
			recvMain.sm_weiboUIImage.getImageUnit("own_top_right"),
			recvMain.sm_weiboUIImage.getImageUnit("own_right"),
			
			recvMain.sm_weiboUIImage.getImageUnit("own_bottom_right"),
			recvMain.sm_weiboUIImage.getImageUnit("own_bottom"),
			recvMain.sm_weiboUIImage.getImageUnit("own_bottom_left"),
			recvMain.sm_weiboUIImage.getImageUnit("own_left"),
			
			recvMain.sm_weiboUIImage.getImageUnit("own_inner_block"),
			new ImageUnit[]{
				null,
				null,
				recvMain.sm_weiboUIImage.getImageUnit("own_point"),
				null,
			},
			recvMain.sm_weiboUIImage);
	
	static{
		sm_otherChatBubble.setPointDown(true);
		sm_ownChatBubble.setPointDown(true);
	}
	
	public ChatField(){
		super(Field.FOCUSABLE | Manager.NO_VERTICAL_SCROLL);
	}
	
	public void destory(){
		int t_num = getFieldCount();
		for(int i = 0 ;i < t_num;i++){
			Field field = getField(i);
			if(field instanceof WeiboTextField){
				sm_textFieldAllocator.release(field);
			}else if(field instanceof ChatImageField){
				sm_imageFieldAllocator.release(field);
			}else if(field instanceof ChatVoiceField){
				sm_voiceFieldAllocator.release(field);
			}
		}
		
		deleteAll();
		
		m_textfield	= null;
		m_imagefield = null;
		m_voiceField = null;		
		
		m_msgTextWidth = fsm_minTextWidth;
	}
	
	public void setFocus(){
		super.setFocus();
		
		if(m_imagefield != null){			
			m_imagefield.setFocus();
		}else if(m_voiceField != null){
			m_voiceField.setFocus();
		}else if(m_textfield != null){
			m_textfield.setFocus();
			m_textfield.setCursorPosition(m_textfield.getTextLength());
		}
	}
	
	public void init(fetchChatMsg _msg,IChatFieldOpen _open){
		destory();
		
		m_msg = _msg;
		
		// text 
		//
		String t_converText = null;
		
		if(_msg.getMsg().length() != 0){
			t_converText = WeiboTextField.getConvertString(_msg.getMsg(),WeiboTextField.CONVERT_DISABLE_AT_SIGN,null);
			m_msgTextWidth = MainIMScreen.fsm_defaultFont.getAdvance(t_converText)  ; 
			
			if(m_msgTextWidth < fsm_minTextWidth){
				m_msgTextWidth = fsm_minTextWidth;
			}
			
			if(m_msgTextWidth > fsm_maxTextWidth){
				m_msgTextWidth = fsm_maxTextWidth;
			}
		}
		
		
		// set the image 
		//
		if(_msg.getFileContent() != null){
			
			if(_msg.getFileContentType() == fetchChatMsg.FILE_TYPE_IMG){
				
				try{
					m_imagefield = (ChatImageField)sm_imageFieldAllocator.alloc();
				}catch(Exception e){
					m_imagefield = new ChatImageField();
				}
				m_imagefield.init(_msg,_open);
				
				if(m_msgTextWidth < m_imagefield.getPreferredWidth()){
					m_msgTextWidth = m_imagefield.getPreferredWidth();
				}
				
			}else{
				
				try{
					m_voiceField = (ChatVoiceField)sm_voiceFieldAllocator.alloc();
				}catch(Exception e){
					m_voiceField = new ChatVoiceField();
				}
				m_voiceField.init(_msg,_open);
				
				if(m_msgTextWidth < m_voiceField.getImageWidth()){
					m_msgTextWidth = m_voiceField.getImageWidth();
				}	
			}
		}
		
		if(t_converText != null){
			// set the text field
			//
			MainIMScreen.fsm_testTextArea.setPreferredWidth(m_msgTextWidth);
			
			MainIMScreen.fsm_testTextArea.setText(t_converText);
			m_msgTextHeight = MainIMScreen.fsm_testTextArea.getHeight();
		
			try{
				m_textfield = (WeiboTextField)sm_textFieldAllocator.alloc();	
			}catch(Exception e){
				m_textfield = new WeiboTextField(0,fsm_ownChatTextBGColor);
			}
			
			if(_msg.isOwnMsg()){
				m_textfield.setColor(fsm_ownChatTextFGColor,fsm_ownChatTextBGColor);
			}else{
				m_textfield.setColor(fsm_otherChatTextFGColor,fsm_otherChatTextBGColor);
			}
			
			m_textfield.setText(_msg.getMsg());
			add(m_textfield);
		}else{
			m_msgTextHeight = 0;
		}
		
		
		if(m_imagefield != null){
			add(m_imagefield);
		}
		
		if(m_voiceField != null){
			add(m_voiceField);
		}
		
		// generate the time string
		//
		fsm_timeDate.setTime(_msg.getSendTime());
		fsm_calendar.setTime(fsm_timeDate);
		int minute = fsm_calendar.get(Calendar.MINUTE); 
		if(minute < 10){
			m_timeText = Integer.toString(fsm_calendar.get(Calendar.HOUR_OF_DAY)) + ":0" + minute;
		}else{
			m_timeText = Integer.toString(fsm_calendar.get(Calendar.HOUR_OF_DAY)) + ":" + minute;
		}
		
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
		int extra = 0;
		if(m_imagefield != null){
			extra = m_imagefield.getPreferredHeight();
		}else if(m_voiceField != null){
			extra = m_voiceField.getImageHeight();
		}
		return m_msgTextHeight + fsm_border * 2 + extra;
	}
	
	protected void sublayout(int _width, int _height){
		
		int t_x = 0;
		
		if(m_msg.isOwnMsg()){
			t_x = recvMain.fsm_display_width - m_msgTextWidth - fsm_border - fsm_bubblePointWidth;
		}else{
			t_x = fsm_border + fsm_bubblePointWidth;
		}
		
		if(m_textfield != null){
			setPositionChild(m_textfield,t_x,fsm_border);
			layoutChild(m_textfield,m_msgTextWidth,m_msgTextHeight);
		}
		
		
		if(m_imagefield != null){
			setPositionChild(m_imagefield,t_x, m_msgTextHeight + fsm_border);
			layoutChild(m_imagefield,m_imagefield.getPreferredWidth(),m_imagefield.getPreferredHeight());	
		}else if(m_voiceField != null){
			setPositionChild(m_voiceField,t_x, m_msgTextHeight + fsm_border);
			layoutChild(m_voiceField,m_voiceField.getPreferredWidth(),m_voiceField.getPreferredHeight());
		}
		
		setExtent(recvMain.fsm_display_width,getPreferredHeight());
	}
	
	protected void subpaint(Graphics _g){

		int t_bubbleWidth = m_msgTextWidth + fsm_border * 2 + 4; //Bubble加宽//
		
		int t_x = 0;
		
		int t_time_x = 0;
		int t_time_y = getPreferredHeight() - fsm_timeFont.getHeight() - 3;
		int t_time_width = fsm_timeFont.getAdvance(m_timeText);
		
		if(m_msg.isOwnMsg()){
			
			t_x = recvMain.fsm_display_width - t_bubbleWidth - fsm_bubblePointWidth;
			
			sm_ownChatBubble.draw(_g, t_x, 0, t_bubbleWidth, getPreferredHeight(), 
					BubbleImage.RIGHT_POINT_STYLE);
			
			t_time_x = t_x - t_time_width ;
			
		}else{
			
			t_x = fsm_bubblePointWidth;
			
			sm_otherChatBubble.draw(_g,t_x, 0, t_bubbleWidth, getPreferredHeight(), 
					BubbleImage.LEFT_POINT_STYLE);
			
			t_time_x = t_x + t_bubbleWidth ;
		}
		
		super.subpaint(_g);
		
		// draw the send state sign
		//
		if(m_msg.isOwnMsg()){
			recvMain.sm_weiboUIImage.drawImage(_g, sm_stateImage[m_msg.getSendState()], 
							t_x - sm_stateImage[m_msg.getSendState()].getWidth(), 0);
		}
		
		if(recvMain.sm_imDisplayTime){
			// draw the time string
			//
			int t_color = _g.getColor();
			Font t_font	= _g.getFont();
			try{
				_g.setColor(MainChatScreen.fsm_background);
				_g.fillRoundRect(t_time_x,t_time_y, t_time_width, fsm_timeFont.getHeight(), 5, 5);
				
//				_g.setColor(fsm_timeTextBorderColor); //
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
}
