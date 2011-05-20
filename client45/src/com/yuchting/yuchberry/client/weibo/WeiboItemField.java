package com.yuchting.yuchberry.client.weibo;

import java.util.Calendar;
import java.util.Date;

import local.localResource;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.component.TextField;

import com.yuchting.yuchberry.client.HyperlinkButtonField;
import com.yuchting.yuchberry.client.fetchWeibo;
import com.yuchting.yuchberry.client.recvMain;

public class WeiboItemField extends Manager{
	
	public final static int		fsm_maxWeiboTextLength		= 140;
	public final static int		fsm_headImageWidth 			= fetchWeibo.fsm_headImageSize;
	public final static int		fsm_headImageTextInterval	= 3;
	
	public final static int		fsm_weiboSignImageSize		= 16;
	
	public final static int		fsm_weiboVIPImageSize		= 12;
	public final static int		fsm_weiboBBerImageSize		= 12; 
	
	public final static int		fsm_maxWeiboAbstractLength	= 20;
	
	public final static int		fsm_textWidth				= recvMain.fsm_display_width - fsm_headImageWidth - fsm_headImageTextInterval;
	public final static int		fsm_editTextWidth			= recvMain.fsm_display_width;
	
	public final static int		fsm_commentTextWidth		= recvMain.fsm_display_width - fsm_headImageTextInterval;
	
	public final static int		fsm_darkColor				= 0xdfdfdf;
	public final static int		fsm_selectedColor			= 0x2020ff;
	public final static int		fsm_spaceLineColor			= 0x8f8f8f;
	
	public static HyperlinkButtonField	 sm_atBut				= new HyperlinkButtonField(recvMain.sm_local.getString(localResource.WEIBO_AT_WEIBO_BUTTON_LABLE));
	public static HyperlinkButtonField	 sm_forwardBut			= new HyperlinkButtonField(recvMain.sm_local.getString(localResource.WEIBO_FORWARD_WEIBO_BUTTTON_LABLE));
	public static HyperlinkButtonField	 sm_favoriteBut			= new HyperlinkButtonField(recvMain.sm_local.getString(localResource.WEIBO_FAVORITE_WEIBO_BUTTON_LABLE));
	
	// BasicEditField for 4.2os
	public static TextField 			sm_textArea				= new TextField(Field.READONLY);
	public static TextField 			sm_commentTextArea		= new TextField(Field.READONLY);
	
	public static TextField 			sm_editTextArea			= new TextField(EditField.FILTER_DEFAULT){
		public void setText(String _text){
			super.setText(_text);
			layout(recvMain.fsm_display_width,1000);
		}
	};
	
	public static TextField 			sm_testTextArea			= new TextField(Field.READONLY){
		public void setText(String _text){
			super.setText(_text);
			layout(fsm_textWidth,1000);
		}
	};
	
	public static TextField 			sm_testCommentTextArea	= new TextField(Field.READONLY){
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
	
	final static int	fsm_closeHeight		= sm_fontHeight * 2 + 1;
	
	boolean[]				m_hasControlField = {false,false,false,false,false,false};
	
	fetchWeibo				m_weibo			= null;
		
	String					m_simpleAbstract = null;
	String					m_weiboText		= null;
	String					m_commentText	= null;
	
	int						m_extendHeight 	= sm_fontHeight;
	
	int						m_functionButton_y	= sm_fontHeight;
	int						m_commentText_y		= sm_fontHeight;
	int						m_textHeight		= sm_fontHeight;
	
	WeiboHeadImage				m_headImage = null;
	
	public WeiboItemField(){
		super(Manager.NO_VERTICAL_SCROLL );
	}

	public WeiboItemField(fetchWeibo _weibo,WeiboHeadImage _headImage){
		super(Manager.NO_VERTICAL_SCROLL);
		
		m_weibo 			= _weibo;
						
		m_headImage 		= _headImage;
		
		StringBuffer t_weiboTextBuffer = new StringBuffer();
		
		t_weiboTextBuffer.append("@").append(m_weibo.GetUserName()).append(":").append(m_weibo.GetText())
						.append("\n   --").append(recvMain.sm_local.getString(localResource.WEIBO_SOURCE_PREFIX))
						.append(parseSource(m_weibo.GetSource()));
		
		m_weiboText 		= t_weiboTextBuffer.toString();
		
		sm_testTextArea.setText(m_weiboText);
		
		m_simpleAbstract		= m_weibo.GetText().length() > fsm_maxWeiboAbstractLength ? 
									(m_weibo.GetText().substring(0,fsm_maxWeiboAbstractLength) + "...") :  m_weibo.GetText();
									
		m_textHeight			= sm_testTextArea.getHeight();
		
		m_functionButton_y		= Math.max(m_textHeight,sm_imageAreaMinHeight) + fsm_headImageTextInterval;
				
		if(m_weibo.GetCommentWeibo() != null){
			fetchWeibo t_comment = m_weibo.GetCommentWeibo();
			
			StringBuffer t_commentText = new StringBuffer();
			t_commentText.append("@").append(t_comment.GetUserName()).append(":").append(t_comment.GetText());
			
			m_commentText = t_commentText.toString();
			
			sm_testCommentTextArea.setText(m_commentText);
			
			m_commentText_y = m_functionButton_y + fsm_headImageTextInterval;
			
			m_functionButton_y = m_commentText_y + sm_testCommentTextArea.getHeight() + fsm_headImageTextInterval;
		}
		
		m_extendHeight 			= m_functionButton_y + sm_fontHeight + fsm_headImageTextInterval;
	}
	
	private String parseSource(String _source){
		int t_start = _source.indexOf('>');
		int t_end = _source.lastIndexOf('<');
		if(t_start != -1 && t_end != -1){
			return _source.substring(t_start + 1,t_end);
		}
		
		return _source;
	}
	
	public void AddDelControlField(boolean _add){
		if(_add){
			
			if(!m_hasControlField[0]){
				m_hasControlField[0] = true;
				sm_textArea.setText(m_weiboText);
				add(sm_textArea);
			}
			
			if(m_commentText != null){
				if(!m_hasControlField[1]){
					m_hasControlField[1] = true;
					sm_commentTextArea.setText(m_commentText);
					add(sm_commentTextArea);
				}
			}
			
			
			if(!m_hasControlField[2]){
				m_hasControlField[2] = true;
				add(sm_forwardBut);
			}
			
			if(!m_hasControlField[3]){
				m_hasControlField[3] = true;
				add(sm_atBut);
			}
			
			if(!m_hasControlField[4]){
				m_hasControlField[4] = true;
				add(sm_favoriteBut);
			}			
			
		}else{
			
			if(m_hasControlField[0]){
				m_hasControlField[0] = false;
				delete(sm_textArea);
			}
			
			if(m_hasControlField[1]){
				m_hasControlField[1] = false;
				delete(sm_commentTextArea);
			}			
			
			if(m_hasControlField[2]){
				m_hasControlField[2] = false;
				delete(sm_forwardBut);
			}
			
			if(m_hasControlField[3]){
				m_hasControlField[3] = false;
				delete(sm_atBut);
			}
			
			if(m_hasControlField[4]){
				m_hasControlField[4] = false;
				delete(sm_favoriteBut);
			}		
		}							
	}
	
	public void AddDelEditTextArea(boolean _add,String _text){
		if(_add){
			
			WeiboItemField.sm_editTextArea.setText(_text);
			WeiboItemField.RefreshEditTextAreHeight();
			
			WeiboItemField.sm_editWeiboItem = this;
			
			if(!m_hasControlField[5]){
				m_hasControlField[5] = true;
				add(sm_editTextArea);				
			}
			
			WeiboItemField.sm_editTextArea.setFocus();
			
			
		}else{
			
			WeiboItemField.sm_editWeiboItem = null;
			WeiboItemField.sm_editTextAreaHeight = 0;
			
			if(m_hasControlField[5]){
				m_hasControlField[5] = false;
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
			return fsm_closeHeight;
		}
	}
	
	public void sublayout(int width, int height){
		
		if(sm_extendWeiboItem == this){
			
			// forward button
			//
			setPositionChild(sm_forwardBut,0,m_functionButton_y);
			layoutChild(sm_atBut,sm_atBut.getPreferredWidth(),sm_atBut.getPreferredHeight());
			
			if(m_commentText != null){
				// comment area
				//
				setPositionChild(sm_commentTextArea,fsm_headImageTextInterval,m_commentText_y);
				layoutChild(sm_commentTextArea,fsm_commentTextWidth,m_functionButton_y - m_commentText_y);
			}
			
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
						
			if(sm_editWeiboItem == this){
				
				setPositionChild(sm_editTextArea,0,m_functionButton_y + fsm_headImageTextInterval + sm_fontHeight);
				layoutChild(sm_editTextArea,recvMain.fsm_display_width,sm_editTextAreaHeight);
				
				height = m_extendHeight + sm_editTextAreaHeight;
				
			}else{
				height = m_extendHeight;
			}
						
		}else{		
			height = fsm_closeHeight;
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
					_g.fillRect(0,0, recvMain.fsm_display_width,fsm_closeHeight);
					_g.setColor(0);
				}
				
				if(sm_selectWeiboItem == this){
					_g.setColor(fsm_selectedColor);
					_g.fillRect(0, 0, recvMain.fsm_display_width,fsm_closeHeight);
					_g.setColor(0xFFFFFF);
				}else{
					_g.setColor(fsm_spaceLineColor);
					_g.drawLine(0,fsm_closeHeight - 1,recvMain.fsm_display_width,fsm_closeHeight - 1);
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
		        _g.drawText(m_simpleAbstract,t_leadingSpace,sm_fontHeight ,Graphics.ELLIPSIS);
		        
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
