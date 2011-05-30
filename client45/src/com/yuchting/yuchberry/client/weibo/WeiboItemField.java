package com.yuchting.yuchberry.client.weibo;

import java.util.Calendar;
import java.util.Date;

import local.localResource;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.component.AutoTextEditField;
import net.rim.device.api.ui.component.TextField;

import com.yuchting.yuchberry.client.HyperlinkButtonField;
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
	public final static int		fsm_ownWeiboColor			= 0xffffee;
	
	public static HyperlinkButtonField	 sm_atBut				= new HyperlinkButtonField(recvMain.sm_local.getString(localResource.WEIBO_AT_WEIBO_BUTTON_LABEL));
	public static HyperlinkButtonField	 sm_forwardBut			= new HyperlinkButtonField(recvMain.sm_local.getString(localResource.WEIBO_FORWARD_WEIBO_BUTTON_LABEL));
	public static HyperlinkButtonField	 sm_favoriteBut			= new HyperlinkButtonField(recvMain.sm_local.getString(localResource.WEIBO_FAVORITE_WEIBO_BUTTON_LABEL));
	public static HyperlinkButtonField	 sm_picBut				= new HyperlinkButtonField(recvMain.sm_local.getString(localResource.WEIBO_CHECK_PICTURE_LABEL));
	public static HyperlinkButtonField	 sm_followCommentUser	= new HyperlinkButtonField(recvMain.sm_local.getString(localResource.WEIBO_FOLLOW_USER_BUTTON_LABEL));
	
	
	// BasicEditField for 4.2os
	public static TextField 			sm_textArea				= new TextField(Field.READONLY);
	public static TextField 			sm_commentTextArea		= new TextField(Field.READONLY);
	
	public static AutoTextEditField 	sm_editTextArea			= new AutoTextEditField(){
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
	static int		sm_favoriteBut_x					= sm_atBut_x + sm_defaultFont.getAdvance(sm_atBut.getText()) + fsm_headImageTextInterval;
	static int		sm_picBut_x							= sm_favoriteBut_x + sm_defaultFont.getAdvance(sm_favoriteBut.getText()) + fsm_headImageTextInterval;
	
	static int		sm_imageAreaMinHeight				= fsm_weiboSignImageSize + fsm_headImageWidth + fsm_headImageTextInterval;

	static WeiboItemField			sm_extendWeiboItem	= null;
	static WeiboItemField			sm_selectWeiboItem	= null;
	static WeiboItemField			sm_editWeiboItem	= null;
		
	static int						sm_currentSendType	= 0;
	public static	boolean		sm_commentFirst		= false;
	
	public final static int			fsm_closeHeight		= sm_fontHeight * 2 + 1;
	
	public final static int			fsm_maxDisplayRows = (recvMain.fsm_display_height - WeiboHeader.fsm_headHeight) / fsm_closeHeight + 2; 
	
	static public final int fsm_controlField_text 		= 0;
	static public final int fsm_controlField_comment 		= 1;
	static public final int fsm_controlField_forwardBtn 	= 2;
	static public final int fsm_controlField_atBtn 		= 3;
	static public final int fsm_controlField_favorBtn		= 4;
	static public final int fsm_controlField_picBtn		= 5;
	static public final int fsm_controlField_followBtn	= 6;
	static public final int fsm_controlField_editText		= 7;
	
	boolean[]				m_hasControlField = {false,false,false,false,false,false,false,false};
	
	fetchWeibo				m_weibo			= null;
		
	String					m_simpleAbstract = null;
	String					m_weiboText		= null;
	String					m_commentText	= null;
	
	String					m_weiboPic		= null;
	
	int						m_extendHeight 	= sm_fontHeight;
	
	int						m_functionButton_y	= sm_fontHeight;
	int						m_commentText_y		= sm_fontHeight;
	int						m_commentText_height= sm_fontHeight;
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
		
		t_weiboTextBuffer.append("@").append(m_weibo.GetUserName()).append(" :").append(m_weibo.GetText())
						.append("\n         --").append(recvMain.sm_local.getString(localResource.WEIBO_SOURCE_PREFIX))
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
			t_commentText.append("@").append(t_comment.GetUserName()).append(":").append(t_comment.GetText())
						.append("\n         --").append(recvMain.sm_local.getString(localResource.WEIBO_SOURCE_PREFIX))
						.append(parseSource(t_comment.GetSource()));
			
			m_commentText = t_commentText.toString();
			
			sm_testCommentTextArea.setText(m_commentText);
			
			m_commentText_height = sm_testCommentTextArea.getHeight() + fsm_headImageTextInterval;
			
			m_commentText_y = m_functionButton_y + fsm_headImageTextInterval;
			
			m_functionButton_y = m_commentText_y + m_commentText_height + fsm_headImageTextInterval;
			
			if(m_weibo.GetCommentWeibo().GetOriginalPic().length() != 0 ){
				m_weiboPic = m_weibo.GetCommentWeibo().GetOriginalPic();
			}
			
		}else{
			
			m_commentText_height = 0;
			if(m_weibo.GetOriginalPic().length() != 0){
				m_weiboPic = m_weibo.GetOriginalPic(); 
			}
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
			
			if(WeiboItemField.sm_commentFirst){
				
				if(m_commentText != null){
					if(!m_hasControlField[fsm_controlField_comment]){
						m_hasControlField[fsm_controlField_comment] = true;
						sm_commentTextArea.setText(m_commentText);
						add(sm_commentTextArea);
					}
					
					if(!m_hasControlField[fsm_controlField_followBtn]){
						m_hasControlField[fsm_controlField_followBtn] = true;
						add(sm_followCommentUser);
					}
				}
				
				if(!m_hasControlField[fsm_controlField_text]){
					m_hasControlField[fsm_controlField_text] = true;
					sm_textArea.setText(m_weiboText);
					add(sm_textArea);
				}
				
				
			}else{
				
				if(!m_hasControlField[fsm_controlField_text]){
					m_hasControlField[fsm_controlField_text] = true;
					sm_textArea.setText(m_weiboText);
					add(sm_textArea);
				}
				
				if(m_commentText != null){
					if(!m_hasControlField[fsm_controlField_comment]){
						m_hasControlField[fsm_controlField_comment] = true;
						sm_commentTextArea.setText(m_commentText);
						add(sm_commentTextArea);
					}
					
					if(!m_hasControlField[fsm_controlField_followBtn]){
						m_hasControlField[fsm_controlField_followBtn] = true;
						add(sm_followCommentUser);
					}
				}
			}		
			
			if(!m_hasControlField[fsm_controlField_forwardBtn]){
				m_hasControlField[fsm_controlField_forwardBtn] = true;
				add(sm_forwardBut);
			}
			
			if(!m_hasControlField[fsm_controlField_atBtn]){
				m_hasControlField[fsm_controlField_atBtn] = true;
				add(sm_atBut);
			}
			
			if(!m_hasControlField[fsm_controlField_favorBtn]){
				m_hasControlField[fsm_controlField_favorBtn] = true;
				add(sm_favoriteBut);
			}
			
			if(!m_hasControlField[fsm_controlField_picBtn]){
				m_hasControlField[fsm_controlField_picBtn] = true;
				add(sm_picBut);
			}
			
		}else{
			
			
			if(m_hasControlField[fsm_controlField_comment]){
				m_hasControlField[fsm_controlField_comment] = false;
				delete(sm_commentTextArea);
			}
			
			if(m_hasControlField[fsm_controlField_followBtn]){
				m_hasControlField[fsm_controlField_followBtn] = false;
				delete(sm_followCommentUser);
			}
			
			if(m_hasControlField[fsm_controlField_text]){
				m_hasControlField[fsm_controlField_text] = false;
				delete(sm_textArea);
			}
								
			
			if(m_hasControlField[fsm_controlField_forwardBtn]){
				m_hasControlField[fsm_controlField_forwardBtn] = false;
				delete(sm_forwardBut);
			}
			
			if(m_hasControlField[fsm_controlField_atBtn]){
				m_hasControlField[fsm_controlField_atBtn] = false;
				delete(sm_atBut);
			}
			
			if(m_hasControlField[fsm_controlField_favorBtn]){
				m_hasControlField[fsm_controlField_favorBtn] = false;
				delete(sm_favoriteBut);
			}	
			
			if(m_hasControlField[fsm_controlField_picBtn]){
				m_hasControlField[fsm_controlField_picBtn] = false;
				delete(sm_picBut);
			}
		}							
	}
	
	public void AddDelEditTextArea(boolean _add,String _text){
		if(_add){
			
			WeiboItemField.sm_editTextArea.setText(_text);
			WeiboItemField.RefreshEditTextAreHeight();
			
			WeiboItemField.sm_editWeiboItem = this;
			
			if(!m_hasControlField[fsm_controlField_editText]){
				m_hasControlField[fsm_controlField_editText] = true;
				add(sm_editTextArea);				
			}
			
			WeiboItemField.sm_editTextArea.setFocus();
			
			
		}else{
			
			WeiboItemField.sm_editWeiboItem = null;
			WeiboItemField.sm_editTextAreaHeight = 0;
			
			if(m_hasControlField[fsm_controlField_editText]){
				m_hasControlField[fsm_controlField_editText] = false;
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
			
			int t_commentText_y = WeiboItemField.sm_commentFirst?0:m_commentText_y;
			
			int t_text_y = WeiboItemField.sm_commentFirst?m_commentText_height:0;
			
			// forward button
			//
			setPositionChild(sm_forwardBut,0,m_functionButton_y);
			layoutChild(sm_atBut,sm_atBut.getPreferredWidth(),sm_atBut.getPreferredHeight());
			
			if(m_commentText != null){
				// comment area
				//
				setPositionChild(sm_commentTextArea,fsm_headImageTextInterval,t_commentText_y);
				layoutChild(sm_commentTextArea,fsm_commentTextWidth,m_functionButton_y - m_commentText_y);
				
				// follow button
				//
				setPositionChild(sm_followCommentUser,3,t_commentText_y + m_commentText_height - sm_fontHeight - 5);
				layoutChild(sm_followCommentUser,sm_followCommentUser.getPreferredWidth(),
												sm_followCommentUser.getPreferredHeight());
			}
			
			// at button
			//
			setPositionChild(sm_atBut,sm_atBut_x,m_functionButton_y);
			layoutChild(sm_forwardBut,sm_forwardBut.getPreferredWidth(),sm_forwardBut.getPreferredHeight());
			
			// favorite button
			//
			setPositionChild(sm_favoriteBut,sm_favoriteBut_x,m_functionButton_y);
			layoutChild(sm_favoriteBut,sm_favoriteBut.getPreferredWidth(),sm_favoriteBut.getPreferredHeight());
			
			if(m_weiboPic != null){
				// open the browser to check the picture button 
				//
				setPositionChild(sm_picBut,sm_picBut_x,m_functionButton_y);
				layoutChild(sm_picBut,sm_picBut.getPreferredWidth(),sm_picBut.getPreferredHeight());
			}
			
			// text area
			//
			setPositionChild(sm_textArea,fsm_headImageWidth + fsm_headImageTextInterval,t_text_y + 2);
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
		
		int t_minutes = sm_calendar.get(Calendar.MINUTE);
		
		if(t_minutes > 9){
			return sm_calendar.get(Calendar.DAY_OF_MONTH) + "d " 
					+ sm_calendar.get(Calendar.HOUR_OF_DAY) + ":" 
					+ t_minutes;
		}else{
			return sm_calendar.get(Calendar.DAY_OF_MONTH) + "d " 
					+ sm_calendar.get(Calendar.HOUR_OF_DAY) + ":" 
					+ "0" + t_minutes;
		}		
	}
		
	public void subpaint(Graphics _g){
		
		if(sm_extendWeiboItem == this){
			
			int t_textStart_y = WeiboItemField.sm_commentFirst?m_commentText_height : 0;
			
			_g.drawBitmap(0, t_textStart_y, fsm_weiboSignImageSize, fsm_weiboSignImageSize, 
						weiboTimeLineScreen.GetWeiboSign(m_weibo), 0, 0);
			
			_g.drawBitmap(0, t_textStart_y + fsm_weiboSignImageSize + fsm_headImageTextInterval, 
						fsm_headImageWidth, fsm_headImageWidth, m_headImage.m_headImage, 0, 0);
			
			int t_startSign_x = fsm_weiboSignImageSize;
			
			// name VIP sign
			//
			if(m_weibo.IsSinaVIP()){
				_g.drawBitmap(t_startSign_x,t_textStart_y,fsm_weiboVIPImageSize, fsm_weiboVIPImageSize, 
						weiboTimeLineScreen.GetSinaVIPSignBitmap(), 0, 0);
				
				t_startSign_x += fsm_weiboVIPImageSize;

			}
			
			// name BBer sign
			//
			if(m_weibo.IsBBer()){
				_g.drawBitmap(t_startSign_x,t_textStart_y,fsm_weiboBBerImageSize, fsm_weiboBBerImageSize, 
								weiboTimeLineScreen.GetBBerSignBitmap(), 0, 0);
			}
			
			
			paintChild(_g,sm_atBut);
			paintChild(_g,sm_forwardBut);
			paintChild(_g,sm_favoriteBut);
			
			if(m_weiboPic != null){
				paintChild(_g,sm_picBut);
			}
			
			paintChild(_g,sm_textArea);
			
			if(m_commentText != null){
				
				int t_commentText_y = WeiboItemField.sm_commentFirst?0:m_commentText_y;
				
				// comment area
				//
				// draw a round rectangle of text area
				int color		= _g.getColor();
				try{
					_g.setColor(0xefefef);
					_g.fillRoundRect(1,t_commentText_y,fsm_commentTextWidth,m_commentText_height,10,10);				
					
				}finally{
					_g.setColor(color);
				}
				paintChild(_g,sm_commentTextArea);
				
				paintChild(_g,sm_followCommentUser);
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
				_g.drawRoundRect(fsm_headImageWidth + fsm_headImageTextInterval - 1 ,t_textStart_y + 1,fsm_textWidth,m_textHeight,10,10);				
				
			}finally{
				_g.setColor(color);
			}
			
		}else{
			
			final int t_firstLineHeight = 2;
			final int t_leadingSpace = 2;
			
			int color		= _g.getColor();
			try{
				
				if(sm_extendWeiboItem != null){
					_g.setColor(fsm_darkColor);
					_g.fillRect(0,0, recvMain.fsm_display_width,fsm_closeHeight);
				}
				
				if(sm_selectWeiboItem == this){
					_g.setColor(fsm_selectedColor);
					_g.fillRect(0, 0, recvMain.fsm_display_width,fsm_closeHeight);
				}else{
					if(m_weibo.IsOwnWeibo() && sm_extendWeiboItem == null){
						_g.setColor(fsm_ownWeiboColor);
						_g.fillRect(0, 0, recvMain.fsm_display_width,fsm_closeHeight);
					}
					_g.setColor(fsm_spaceLineColor);
					_g.drawLine(0,fsm_closeHeight - 1,recvMain.fsm_display_width,fsm_closeHeight - 1);
				}			
				
				// weibo sign 
				//
				_g.drawBitmap(t_leadingSpace, t_firstLineHeight, fsm_weiboSignImageSize, fsm_weiboSignImageSize, 
							weiboTimeLineScreen.GetWeiboSign(m_weibo), 0, 0);

				// name 
				//
				if(sm_selectWeiboItem == this){
					_g.setColor(0xffffff);
				}else{
					_g.setColor(0);
				}
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
				if(sm_selectWeiboItem == this){
					_g.setColor(0xffffff);
				}else{
					_g.setColor(0x8f8f8f);					
				}
		        _g.drawText(m_simpleAbstract,t_leadingSpace + fsm_weiboSignImageSize,sm_fontHeight + fsm_headImageTextInterval ,Graphics.ELLIPSIS);
		        
		        // time string
		        //
		        if(sm_selectWeiboItem == this){
		        	_g.setColor(0xffffff);
		        }else{
		        	_g.setColor(0);
		        }		        
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
