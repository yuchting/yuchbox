package com.yuchting.yuchberry.client;

import java.util.Calendar;
import java.util.Date;

import local.localResource;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.component.TextField;

public class WeiboItemField extends Manager{
	
	final static int		fsm_maxWeiboTextLength		= 140;
	final static int		fsm_headImageWidth 			= fetchWeibo.fsm_headImageSize;
	final static int		fsm_headImageTextInterval	= 3;
	
	final static int		fsm_weiboSignImageSize		= 16;
	
	final static int		fsm_weiboVIPImageSize		= 12;
	final static int		fsm_weiboBBerImageSize		= 12; 
	
	final static int		fsm_textWidth				= recvMain.fsm_display_width - fsm_headImageWidth - fsm_headImageTextInterval;
	final static int		fsm_editTextWidth			= recvMain.fsm_display_width;
	
	final static int		fsm_commentTextWidth		= recvMain.fsm_display_width - fsm_headImageTextInterval;
	
	final static int		fsm_darkColor				= 0xdfdfdf;
	final static int		fsm_selectedColor			= 0x2020ff;
	final static int		fsm_spaceLineColor			= 0x8f8f8f;
	
	static HyperlinkButtonField	 sm_atBut				= new HyperlinkButtonField(recvMain.sm_local.getString(localResource.AT_WEIBO_BUTTON_LABLE));
	static HyperlinkButtonField	 sm_forwardBut			= new HyperlinkButtonField(recvMain.sm_local.getString(localResource.FORWARD_WEIBO_BUTTTON_LABLE));
	static HyperlinkButtonField	 sm_favoriteBut			= new HyperlinkButtonField(recvMain.sm_local.getString(localResource.FAVORITE_WEIBO_BUTTON_LABLE));
	
	// BasicEditField for 4.2os
	static TextField 			sm_textArea				= new TextField(Field.READONLY);
	static TextField 			sm_commentTextArea		= new TextField(Field.READONLY);
	
	static TextField 			sm_editTextArea			= new TextField(EditField.FILTER_DEFAULT){
		public void setText(String _text){
			super.setText(_text);
			layout(recvMain.fsm_display_width,1000);
		}
	};
	
	static TextField 			sm_testTextArea			= new TextField(Field.READONLY){
		public void setText(String _text){
			super.setText(_text);
			layout(fsm_textWidth,1000);
		}
	};
	
	static TextField 			sm_testCommentTextArea			= new TextField(Field.READONLY){
		public void setText(String _text){
			super.setText(_text);
			layout(fsm_commentTextWidth,1000);
		}
	};
			
	static {
		sm_editTextArea.setMaxSize(fsm_maxWeiboTextLength);
	}
	
	
	
	static Font		sm_defaultFont						= sm_atBut.getFont();
	static int		sm_fontHeight						= sm_defaultFont.getHeight() + 2;
	
	static int		sm_editTextAreaHeight				= 0;
		
	static int		sm_atBut_x							= sm_defaultFont.getAdvance(sm_forwardBut.getText()) + fsm_headImageTextInterval;
	static int		sm_forvoriteBut_x					= sm_atBut_x + sm_defaultFont.getAdvance(sm_atBut.getText()) + fsm_headImageTextInterval;
	
	static int		sm_imageAreaMinHeight				= fsm_weiboSignImageSize + fsm_headImageWidth + fsm_headImageTextInterval;

	static WeiboItemField			sm_extendWeiboItem	= null;
	static WeiboItemField			sm_selectWeiboItem	= null;
	static WeiboItemField			sm_editWeiboItem	= null;
	
	static int						sm_currentSendType	= 0;
	
	static int	sm_closeHeight		= sm_fontHeight * 2 + 1;
	
	boolean[]				m_hasControlField = {false,false,false,false,false,false};
	
	fetchWeibo				m_weibo			= null;
	
	String					m_commentText	= null;
		
	int						m_extendHeight 	= sm_fontHeight;
	
	int						m_functionButton_y	= sm_fontHeight;
	int						m_commentText_y		= sm_fontHeight;
	int						m_textHeight		= sm_fontHeight;
	
	HeadImage				m_headImage = null;
	
	public WeiboItemField(){
		super(Manager.NO_VERTICAL_SCROLL );
	}
	
	public WeiboItemField(fetchWeibo _weibo,HeadImage _headImage){
		super(Manager.NO_VERTICAL_SCROLL);
		
		m_weibo 			= _weibo;
						
		m_headImage 		= _headImage;
				
		sm_testTextArea.setText(m_weibo.GetText());
		
		m_textHeight			= sm_testTextArea.getHeight();
		
		m_functionButton_y		= Math.max(m_textHeight,sm_imageAreaMinHeight) + fsm_headImageTextInterval - 2;
				
		if(m_weibo.GetCommentWeibo() != null){
			fetchWeibo t_comment = m_weibo.GetCommentWeibo();
			
			m_commentText = t_comment.GetUserName() + ":" + t_comment.GetText();
			sm_testCommentTextArea.setText(m_commentText);
			
			m_commentText_y = m_functionButton_y + fsm_headImageTextInterval;
			
			m_functionButton_y = m_commentText_y + sm_testCommentTextArea.getHeight() + fsm_headImageTextInterval;
		}
		
		m_extendHeight 			= m_functionButton_y + sm_fontHeight;
	}
	
	public void AddDelControlField(boolean _add){
		if(_add){
			
			if(!m_hasControlField[0]){
				m_hasControlField[0] = true;
				sm_textArea.setText(m_weibo.GetText());
				add(sm_textArea);
			}
			
			if(!m_hasControlField[1]){
				m_hasControlField[1] = true;
				add(sm_forwardBut);
			}
			
			if(!m_hasControlField[2]){
				m_hasControlField[2] = true;
				add(sm_atBut);
			}
			
			if(!m_hasControlField[3]){
				m_hasControlField[3] = true;
				add(sm_favoriteBut);
			}
			
			if(m_commentText != null){
				if(!m_hasControlField[5]){
					m_hasControlField[5] = true;
					sm_commentTextArea.setText(m_commentText);
					add(sm_commentTextArea);
				}
			}
			
			
		}else{
			
			if(m_hasControlField[0]){
				m_hasControlField[0] = false;
				delete(sm_textArea);
			}
			
			
			if(m_hasControlField[1]){
				m_hasControlField[1] = false;
				delete(sm_forwardBut);
			}
			
			if(m_hasControlField[2]){
				m_hasControlField[2] = false;
				delete(sm_atBut);
			}
			
			if(m_hasControlField[3]){
				m_hasControlField[3] = false;
				delete(sm_favoriteBut);
			}
			
			if(m_hasControlField[5]){
				m_hasControlField[5] = false;
				delete(sm_commentTextArea);
			}
		}							
	}
	
	public void AddDelEditTextArea(boolean _add,String _text){
		if(_add){
			
			WeiboItemField.sm_editTextArea.setText(_text);
			WeiboItemField.RefreshEditTextAreHeight();
			
			WeiboItemField.sm_editWeiboItem = this;
			
			if(!m_hasControlField[4]){
				m_hasControlField[4] = true;
				add(sm_editTextArea);				
			}
			
			WeiboItemField.sm_editTextArea.setFocus();
			
			
		}else{
			
			WeiboItemField.sm_editWeiboItem = null;
			WeiboItemField.sm_editTextAreaHeight = 0;
			
			if(m_hasControlField[4]){
				m_hasControlField[4] = false;
				delete(sm_editTextArea);
			}
		}
	}
	
	static public void RefreshEditTextAreHeight(){
		sm_editTextAreaHeight = sm_editTextArea.getHeight() + fsm_headImageTextInterval;
	}
	
	
	public int getPreferredWidth() {
		return recvMain.fsm_display_width;
	}
	
	public int getPreferredHeight() {
		if(sm_extendWeiboItem == this){
			
			if(sm_editWeiboItem == this){
				return m_extendHeight + sm_editTextAreaHeight;
			}
			
			return m_extendHeight;
		}else{
			return sm_closeHeight;
		}
	}
	
	public void sublayout(int width, int height){
		
		if(sm_extendWeiboItem == this){
			
			// forward button
			//
			setPositionChild(sm_forwardBut,0,m_functionButton_y);
			layoutChild(sm_atBut,sm_atBut.getPreferredWidth(),sm_atBut.getPreferredHeight());
			
			// at button
			//
			setPositionChild(sm_atBut,sm_atBut_x,m_functionButton_y);
			layoutChild(sm_forwardBut,sm_forwardBut.getPreferredWidth(),sm_forwardBut.getPreferredHeight());
			
			// favorite button
			//
			setPositionChild(sm_favoriteBut,sm_forvoriteBut_x,m_functionButton_y);
			layoutChild(sm_favoriteBut,sm_favoriteBut.getPreferredWidth(),sm_favoriteBut.getPreferredHeight());
			
			// text area
			//
			setPositionChild(sm_textArea,fsm_headImageWidth + fsm_headImageTextInterval,2);
			layoutChild(sm_textArea,fsm_textWidth,m_textHeight);
			
			if(m_commentText != null){
				// comment area
				//
				setPositionChild(sm_commentTextArea,fsm_headImageTextInterval,m_commentText_y);
				layoutChild(sm_commentTextArea,fsm_commentTextWidth,m_functionButton_y - m_commentText_y);
			}
			
			if(sm_editWeiboItem == this){
				
				setPositionChild(sm_editTextArea,0,m_functionButton_y + fsm_headImageTextInterval + sm_fontHeight);
				layoutChild(sm_editTextArea,recvMain.fsm_display_width,sm_editTextAreaHeight);
				
				height = m_extendHeight + sm_editTextAreaHeight;
				
			}else{
				height = m_extendHeight;
			}
						
		}else{		
			height = sm_closeHeight;
		}
		
		setExtent(recvMain.fsm_display_width,height);
	}
	
	static Calendar sm_calendar = Calendar.getInstance();
	static Date		sm_timeDate = new Date();
	
	
	protected synchronized String getTimeString(){
		
		sm_timeDate.setTime(m_weibo.GetDateTime());
		sm_calendar.setTime(sm_timeDate);		
		
		return sm_calendar.get(Calendar.DAY_OF_MONTH) + "Day " 
				+ sm_calendar.get(Calendar.HOUR_OF_DAY) + ":" 
				+ sm_calendar.get(Calendar.MINUTE);
	}
		
	public void subpaint(Graphics _g){
		
		if(sm_extendWeiboItem == this){
			
			_g.drawBitmap(0, 0, fsm_weiboSignImageSize, fsm_weiboSignImageSize, 
						weiboTimeLineScreen.GetWeiboSign(m_weibo), 0, 0);
			
			_g.drawBitmap(0, fsm_weiboSignImageSize + fsm_headImageTextInterval, 
						fsm_headImageWidth, fsm_headImageWidth, m_headImage.m_headImage, 0, 0);
			
			int t_startSign_x = fsm_weiboSignImageSize;
			
			// name VIP sign
			//
			if(m_weibo.IsSinaVIP()){
				_g.drawBitmap(t_startSign_x,0,fsm_weiboVIPImageSize, fsm_weiboVIPImageSize, 
						weiboTimeLineScreen.GetSinaVIPSignBitmap(), 0, 0);
				
				t_startSign_x += fsm_weiboVIPImageSize;

			}
			
			// name BBer sign
			//
			if(m_weibo.IsBBer()){
				_g.drawBitmap(t_startSign_x,0,fsm_weiboBBerImageSize, fsm_weiboBBerImageSize, 
								weiboTimeLineScreen.GetBBerSignBitmap(), 0, 0);
			}
			
			
			paintChild(_g,sm_atBut);
			paintChild(_g,sm_forwardBut);
			paintChild(_g,sm_favoriteBut);
			
			paintChild(_g,sm_textArea);
			
			if(m_commentText != null){
				// comment area
				//
				// draw a round rectangle of text area
				int color		= _g.getColor();
				try{
					_g.setColor(0xefefef);
					_g.fillRoundRect(1,m_commentText_y,fsm_commentTextWidth,m_functionButton_y - m_commentText_y,10,10);				
					
				}finally{
					_g.setColor(color);
				}
				paintChild(_g,sm_commentTextArea);
			}
			
			if(sm_editWeiboItem == this){
				paintChild(_g,sm_editTextArea);
			}
			
			// draw the finally line
			//
			int t_y = getPreferredHeight() - 1;
			_g.drawLine(0,t_y, recvMain.fsm_display_width,t_y);
			
			
			// draw a round rectangle of text area
			int color		= _g.getColor();
			try{
				_g.setColor(0x8f8f8f);
				_g.drawRoundRect(fsm_headImageWidth + fsm_headImageTextInterval - 1 ,1,fsm_textWidth,m_textHeight,10,10);				
				
			}finally{
				_g.setColor(color);
			}
			
		}else{
			
			final int t_firstLineHeight = 2;
			final int t_leadingSpace = 2;
			
			int color		= _g.getColor();
			try{
				
				if(sm_extendWeiboItem != null){
					// 
					//
					_g.setColor(fsm_darkColor);
					_g.fillRect(0,0, recvMain.fsm_display_width,sm_closeHeight);
					_g.setColor(0);
				}
				
				if(sm_selectWeiboItem == this){
					_g.setColor(fsm_selectedColor);
					_g.fillRect(0, 0, recvMain.fsm_display_width,sm_closeHeight);
					_g.setColor(0xFFFFFF);
				}else{
					_g.setColor(fsm_spaceLineColor);
					_g.drawLine(0,sm_closeHeight - 1,recvMain.fsm_display_width,sm_closeHeight - 1);
					_g.setColor(0);
				}			
				
				// weibo sign 
				//
				_g.drawBitmap(t_leadingSpace, t_firstLineHeight, fsm_weiboSignImageSize, fsm_weiboSignImageSize, 
							weiboTimeLineScreen.GetWeiboSign(m_weibo), 0, 0);

				// name 
				//
				int t_nameLength = _g.drawText(m_weibo.GetUserName().substring(0,Math.min(m_weibo.GetUserName().length(),16)),
										fsm_weiboSignImageSize + 5,t_firstLineHeight,Graphics.ELLIPSIS);
				
				// add the weibo sign size
				t_nameLength += fsm_weiboSignImageSize + t_leadingSpace;
				
				// name VIP sign
				//
				if(m_weibo.IsSinaVIP()){
					_g.drawBitmap(t_nameLength + t_leadingSpace, t_firstLineHeight, fsm_weiboVIPImageSize, fsm_weiboVIPImageSize, 
							weiboTimeLineScreen.GetSinaVIPSignBitmap(), 0, 0);
					
					t_nameLength += fsm_weiboVIPImageSize;

				}
				
				// name BBer sign
				//
				if(m_weibo.IsBBer()){
					_g.drawBitmap(t_nameLength + t_leadingSpace, t_firstLineHeight, 
									fsm_weiboBBerImageSize, fsm_weiboBBerImageSize, 
									weiboTimeLineScreen.GetBBerSignBitmap(), 0, 0);
				}
				
				
				// contain abstract
				//
		        _g.drawText(m_weibo.GetText(),t_leadingSpace,sm_fontHeight ,Graphics.ELLIPSIS);
		        
		        // time string
		        //
		        _g.setColor(0xafafaf);
		        String t_dateString = getTimeString();		        
		        _g.drawText(t_dateString,recvMain.fsm_display_width - _g.getFont().getAdvance(t_dateString)
		        				,t_firstLineHeight,Graphics.ELLIPSIS);
		       

			}finally{
				_g.setColor(color);
			}
			
		}		
	}
	
	protected boolean navigationMovement(int dx, int dy, int status, int time){
		super.navigationMovement(dx, dy, status, time);
		return false;
	}
	
}

class WeiboUpdateField extends WeiboItemField{
	
	String m_sendUpdateText = "";
	
	public void AddDelControlField(boolean _add){
		AddDelEditTextArea(_add,m_sendUpdateText);
	}
	
	public int getPreferredHeight() {
		if(sm_extendWeiboItem == this){
			
			if(sm_editWeiboItem == this){
				return m_extendHeight + sm_editTextAreaHeight;
			}
			
			return m_extendHeight;
		}else{
			return sm_fontHeight;
		}
	}
	
	public void sublayout(int width, int height){
		
		if(sm_extendWeiboItem == this){
					
			setPositionChild(sm_editTextArea,0,0);
			layoutChild(sm_editTextArea,recvMain.fsm_display_width,sm_editTextAreaHeight);
			
			height = sm_editTextAreaHeight;			
						
		}else{		
			height = sm_fontHeight;
		}
		
		setExtent(recvMain.fsm_display_width,height);
	}
	
	public void subpaint(Graphics _g){
		
		if(sm_extendWeiboItem == this){
			
			paintChild(_g,sm_editTextArea);
			
		}else{
			
			if(sm_extendWeiboItem != null){
				_g.setColor(fsm_darkColor);
				_g.fillRect(0,0, recvMain.fsm_display_width,sm_closeHeight);
				_g.setColor(0);
			}
			
			
			int oldColour = _g.getColor();
	        try{
	        	_g.setColor(0x8f8f8f);            
	        	_g.drawText(recvMain.sm_local.getString(localResource.UPDATE_WEIBO_LABEL),2,2,Graphics.ELLIPSIS);
	        }finally{
	        	_g.setColor( oldColour );
	        }		
			
			if(sm_selectWeiboItem == this){
				_g.drawRoundRect(1,1,recvMain.fsm_display_width - 1,sm_fontHeight - 1,1,1);
			}else{
				_g.drawLine(0,sm_fontHeight - 1,recvMain.fsm_display_width,sm_fontHeight - 1);
			}
		}
	}
}
