package com.yuchting.yuchberry.client.weibo;

import java.util.Calendar;
import java.util.Date;

import local.localResource;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.component.TextField;

import com.yuchting.yuchberry.client.recvMain;
import com.yuchting.yuchberry.client.ui.BubbleImage;
import com.yuchting.yuchberry.client.ui.ImageUnit;

class ContentTextField extends TextField{
	
	int		m_textWidth;
	
	public ContentTextField(){
		super(TextField.READONLY);
	}
	
	public void setTextWidth(int _width){
		m_textWidth = _width;
	}
	
	public int getTextWidth(){
		return m_textWidth;
	}
	
	public void setText(String _text){
		super.setText(_text);
		layout(m_textWidth,1000);
	}
	
	public void paint(Graphics _g){
		super.paint(_g);
	}
};

public class WeiboItemField extends Manager{
	
	public final static int		fsm_weiboItemFieldWidth		= recvMain.fsm_display_width - WeiboMainManager.fsm_scrollbarSize;
	public final static int		fsm_maxWeiboTextLength		= 140;
	public final static boolean	fsm_largeHeadImage			= recvMain.fsm_display_width > 320;
	
	public final static int		fsm_headImageWidth 			= recvMain.fsm_display_width>320?fetchWeibo.fsm_headImageSize_l:fetchWeibo.fsm_headImageSize;

	public final static int		fsm_headImageTextInterval	= 3;
	
	public final static int		fsm_weiboSignImageSize		= 16;
	
	public final static int		fsm_weiboVIPImageSize		= 12;
	public final static int		fsm_weiboBBerImageSize		= 12; 
	
	public final static int		fsm_maxWeiboAbstractLength	= 20;
	
	public final static int		fsm_textWidth				= fsm_weiboItemFieldWidth - fsm_headImageWidth - fsm_headImageTextInterval;
	public final static int		fsm_editTextWidth			= fsm_weiboItemFieldWidth;
	
	public final static int		fsm_commentTextWidth		= fsm_weiboItemFieldWidth - fsm_headImageTextInterval * 2;
	
	public final static int		fsm_darkColor				= 0x6d6d6d;
	public final static int		fsm_promptTextBGColor		= 0xffffcc;
	public final static int		fsm_promptTextBorderColor	= 0xc0c0c0;
	
	public final static int		fsm_selectedColor			= 0x42a2de;
	public final static int		fsm_absTextColor			= 0x8f8f8f;
	
	public final static int		fsm_weiboNameTextColor		= 0xededed;
	public final static int		fsm_weiboTextBGColor		= 0xf6fdff;
	
	public final static int		fsm_weiboCommentFGColor		= 0x6f6f6f;
	public final static int		fsm_weiboCommentBGColor		= 0xd8d8d8;
	
	
	// BasicEditField for 4.2os
	public static TextField 	sm_testTextArea	= new TextField(Field.READONLY){
		public void setText(String _text){
			super.setText(_text);
			layout(WeiboItemField.fsm_textWidth,1000);
		}
	};	
	
	public static TextField 	sm_testCommentTextArea	= new TextField(Field.READONLY){
		public void setText(String _text){
			super.setText(_text);
			layout(WeiboItemField.fsm_commentTextWidth,1000);
		}
	};	
	
	public static Font		sm_defaultFont				= sm_testTextArea.getFont();
	public static Font		sm_timeFont					= sm_testTextArea.getFont().derive(sm_defaultFont.getStyle(),sm_defaultFont.getHeight() - 4);
	public static Font		sm_boldFont					= sm_testTextArea.getFont().derive(sm_defaultFont.getStyle() | Font.BOLD,sm_defaultFont.getHeight());
	public static int		sm_fontHeight				= sm_defaultFont.getHeight() + 2;
	
	public static int		sm_editTextAreaHeight		= 0;
	
	public static int		sm_imageAreaMinHeight		= fsm_weiboSignImageSize + fsm_headImageWidth + fsm_headImageTextInterval;
	
	public final static int	fsm_closeHeight			= sm_fontHeight * 2 + 1;
 
	public static	boolean		sm_commentFirst		= false;
	public static	boolean		sm_displayHeadImage	= false;
	public static boolean			sm_simpleMode		= false;
	public static boolean			sm_showAllInList	= false;
	
	static public final int fsm_controlField_text 		= 0;
	static public final int fsm_controlField_comment 		= 1;
	static public final int fsm_controlField_forwardBtn 	= 2;
	static public final int fsm_controlField_atBtn 		= 3;
	static public final int fsm_controlField_favorBtn		= 4;
	static public final int fsm_controlField_picBtn		= 5;
	static public final int fsm_controlField_followBtn	= 6;
	static public final int fsm_controlField_editText		= 7;
		
	boolean[]				m_hasControlField = 
	{
			false,false,false,false,
			false,false,false,false,
	};
	
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
	int						m_absTextHeight		= sm_fontHeight;
	
	WeiboMainManager		m_parentManager		= null;
	
	WeiboHeadImage			m_headImage = null;
	
	WeiboItemFocusField		m_focusField = new WeiboItemFocusField(this);
	
	ContentTextField 		m_absTextArea	= null;
	boolean				m_absTextAreaAdded = false;
	
	public WeiboItemFocusField getFocusField(){
		return m_focusField;
	}
	
	public WeiboItemField(WeiboMainManager _manager){
		super(Manager.NO_VERTICAL_SCROLL );
		
		m_parentManager = _manager;
	}

	public WeiboItemField(fetchWeibo _weibo,WeiboHeadImage _headImage,WeiboMainManager _manager){
		super(Manager.NO_VERTICAL_SCROLL);
		
		m_weibo 		= _weibo;
		m_headImage 	= _headImage;
		m_parentManager = _manager;
		
		StringBuffer t_weiboTextBuffer = new StringBuffer();
		
		t_weiboTextBuffer.append("@").append(m_weibo.GetUserScreenName()).append(" :").append(m_weibo.GetText());
		
		if(!sm_simpleMode && m_weibo.GetSource().length() != 0){
			t_weiboTextBuffer.append("\n       --").append(recvMain.sm_local.getString(localResource.WEIBO_SOURCE_PREFIX))
			.append(parseSource(m_weibo.GetSource()));
		}						
		
		m_weiboText 		= t_weiboTextBuffer.toString();
		t_weiboTextBuffer = null;
		
		sm_testTextArea.setText(m_weiboText);
				
		m_simpleAbstract		= m_weibo.GetText().length() > fsm_maxWeiboAbstractLength ? 
									(m_weibo.GetText().substring(0,fsm_maxWeiboAbstractLength) + "...") :  m_weibo.GetText();

		m_textHeight			= sm_testTextArea.getHeight();

		m_functionButton_y		= Math.max(m_textHeight,sm_imageAreaMinHeight) + fsm_headImageTextInterval;
				
		if(m_weibo.GetCommentWeibo() != null){
			
			fetchWeibo t_comment = m_weibo.GetCommentWeibo();
			
			StringBuffer t_commentText = new StringBuffer();
			t_commentText.append("@").append(t_comment.GetUserScreenName()).append(":").append(t_comment.GetText());
			if(!sm_simpleMode && t_comment.GetSource().length() != 0){		
				t_commentText.append("\n       --").append(recvMain.sm_local.getString(localResource.WEIBO_SOURCE_PREFIX))
							.append(parseSource(t_comment.GetSource()));
			}
						
					
			m_commentText = t_commentText.toString();
			
			t_commentText = null;
			
			sm_testCommentTextArea.setText(m_commentText);
			
			m_commentText_height = sm_testCommentTextArea.getHeight() + fsm_headImageTextInterval;
			
			m_commentText_y = m_functionButton_y + fsm_headImageTextInterval * 2;
			
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
		
		if(!sm_simpleMode){
			m_extendHeight 			= m_functionButton_y + _manager.m_forwardBut.getImageHeight() + fsm_headImageTextInterval;
		}else{
			m_extendHeight 			= m_functionButton_y + fsm_headImageTextInterval;
		}
		
		// get the abstract text height
		//
		if(sm_showAllInList){
			
			initAbsTextArea();
			
			m_absTextAreaAdded = true;
			add(m_absTextArea);			
		}
	}
	
	public static int getAbsTextPosX(){
		if(sm_displayHeadImage){
			return 2 + fsm_headImageTextInterval + fsm_headImageWidth + 10;
		}else{
			return 2 + fsm_weiboSignImageSize + 10;
		}
	}
	
	
	public boolean hasTheWeibo(fetchWeibo _weibo){
		return _weibo == m_weibo;
	}
	
	public WeiboMainManager getParentManager(){
		return m_parentManager;
	}
	
	public static  String parseSource(String _source){
		int t_start = _source.indexOf('>');
		int t_end = _source.lastIndexOf('<');
		if(t_start != -1 && t_end != -1){
			return _source.substring(t_start + 1,t_end);
		}
		
		return _source;
	}
	
	private void initAbsTextArea(){
		
		if(m_absTextArea == null){
			m_absTextArea = new ContentTextField();
			m_absTextArea.setTextWidth(recvMain.fsm_display_width - getAbsTextPosX() - WeiboMainManager.fsm_scrollbarSize - 1);		
			m_absTextArea.setText(m_weiboText);
			m_absTextHeight = m_absTextArea.getHeight();
		}
	}
	
	public void AddDelControlField(boolean _add){
		if(_add){
			
			if(m_absTextAreaAdded){
				m_absTextAreaAdded = false;
				delete(m_absTextArea);
			}
			
			if(WeiboItemField.sm_commentFirst){
				
				if(m_commentText != null){
					if(!m_hasControlField[fsm_controlField_comment]){
						m_hasControlField[fsm_controlField_comment] = true;
						m_parentManager.m_commentTextArea.setText(m_commentText);
						add(m_parentManager.m_commentTextArea);
					}					
				}
				
				if(!m_hasControlField[fsm_controlField_text]){
					m_hasControlField[fsm_controlField_text] = true;
					m_parentManager.m_textArea.setText(m_weiboText);
					add(m_parentManager.m_textArea);
				}
				
				
			}else{
				
				if(!m_hasControlField[fsm_controlField_text]){
					m_hasControlField[fsm_controlField_text] = true;
					m_parentManager.m_textArea.setText(m_weiboText);
					add(m_parentManager.m_textArea);
				}
				
				if(m_commentText != null){
					if(!m_hasControlField[fsm_controlField_comment]){
						m_hasControlField[fsm_controlField_comment] = true;
						m_parentManager.m_commentTextArea.setText(m_commentText);
						add(m_parentManager.m_commentTextArea);
					}
					
					
				}
			}		
			
			if(!sm_simpleMode){
				if(!m_hasControlField[fsm_controlField_forwardBtn]){
					m_hasControlField[fsm_controlField_forwardBtn] = true;
					add(m_parentManager.m_forwardBut);
				}
				
				if(!m_hasControlField[fsm_controlField_atBtn]){
					m_hasControlField[fsm_controlField_atBtn] = true;
					add(m_parentManager.m_atBut);
				}
				
				if(!m_hasControlField[fsm_controlField_favorBtn]){
					m_hasControlField[fsm_controlField_favorBtn] = true;
					add(m_parentManager.m_favoriteBut);
				}
				
				if(!m_hasControlField[fsm_controlField_picBtn]){
					m_hasControlField[fsm_controlField_picBtn] = true;
					add(m_parentManager.m_picBut);
				}
			}
			
			
		}else{
			
			if(sm_showAllInList && !m_absTextAreaAdded){
				initAbsTextArea();
				
				m_absTextAreaAdded = true;
				add(m_absTextArea);
			}
			
			if(m_hasControlField[fsm_controlField_comment]){
				m_hasControlField[fsm_controlField_comment] = false;
				delete(m_parentManager.m_commentTextArea);
			}
			
			
			
			if(m_hasControlField[fsm_controlField_text]){
				m_hasControlField[fsm_controlField_text] = false;
				delete(m_parentManager.m_textArea);
			}
								
			
			if(m_hasControlField[fsm_controlField_forwardBtn]){
				m_hasControlField[fsm_controlField_forwardBtn] = false;
				delete(m_parentManager.m_forwardBut);
			}
			
			if(m_hasControlField[fsm_controlField_atBtn]){
				m_hasControlField[fsm_controlField_atBtn] = false;
				delete(m_parentManager.m_atBut);
			}
			
			if(m_hasControlField[fsm_controlField_favorBtn]){
				m_hasControlField[fsm_controlField_favorBtn] = false;
				delete(m_parentManager.m_favoriteBut);
			}	
			
			if(m_hasControlField[fsm_controlField_picBtn]){
				m_hasControlField[fsm_controlField_picBtn] = false;
				delete(m_parentManager.m_picBut);
			}
		}							
	}
	
	public void AddDelEditTextArea(boolean _add,String _text){
		if(_add){
			
			m_parentManager.m_editTextArea.setText(_text);
			m_parentManager.RefreshEditTextAreHeight();
			
			m_parentManager.setCurrEditItem(this);
			
			if(!m_hasControlField[fsm_controlField_editText]){
				m_hasControlField[fsm_controlField_editText] = true;
				add(m_parentManager.m_editTextArea);
			}
						
		}else{
			
			m_parentManager.setCurrEditItem(null);
			WeiboItemField.sm_editTextAreaHeight = 0;
			
			if(m_hasControlField[fsm_controlField_editText]){
				m_hasControlField[fsm_controlField_editText] = false;
				delete(m_parentManager.m_editTextArea);
			}
		}
	}
	
	public int getPreferredWidth() {
		return fsm_weiboItemFieldWidth;
	}
	
	public int getPreferredHeight() {
		if(m_parentManager.getCurrExtendedItem() == this){
			
			if(m_parentManager.getCurrEditItem() == this){
				return m_extendHeight + sm_editTextAreaHeight;
			}
			
			return m_extendHeight;
			
		}else{
			
			if(m_absTextArea != null && sm_showAllInList){
				return Math.max(m_absTextHeight + 5 + sm_timeFont.getHeight() + fsm_headImageTextInterval,
									fsm_weiboSignImageSize + fsm_headImageWidth + 10);
			}else{
				return sm_fontHeight * 2 + 1;
			}
			
		}
	}
	
	public void sublayout(int width, int height){
		
		if(m_parentManager.getCurrExtendedItem() == this){
			
			int t_commentText_y = WeiboItemField.sm_commentFirst?0:m_commentText_y;
			
			int t_text_y = WeiboItemField.sm_commentFirst?m_commentText_height:1;
		
			if(m_commentText != null){
				// comment area
				//
				setPositionChild(m_parentManager.m_commentTextArea,fsm_headImageTextInterval,t_commentText_y);
				layoutChild(m_parentManager.m_commentTextArea,fsm_commentTextWidth,m_functionButton_y - m_commentText_y);
				
				t_text_y += weiboTimeLineScreen.sm_bubbleImage.getInnerBlockSize() / 2;
			}
			
			if(!sm_simpleMode){
				// forward button
				//
				setPositionChild(m_parentManager.m_forwardBut,WeiboMainManager.sm_forwardBut_x,m_functionButton_y);
				layoutChild(m_parentManager.m_atBut,m_parentManager.m_atBut.getPreferredWidth(),m_parentManager.m_atBut.getPreferredHeight());
				
				
				// at button
				//
				setPositionChild(m_parentManager.m_atBut,WeiboMainManager.sm_atBut_x,m_functionButton_y);
				layoutChild(m_parentManager.m_forwardBut,m_parentManager.m_forwardBut.getPreferredWidth(),m_parentManager.m_forwardBut.getPreferredHeight());
				
				// favorite button
				//
				setPositionChild(m_parentManager.m_favoriteBut,WeiboMainManager.sm_favoriteBut_x,m_functionButton_y);
				layoutChild(m_parentManager.m_favoriteBut,m_parentManager.m_favoriteBut.getPreferredWidth(),m_parentManager.m_favoriteBut.getPreferredHeight());
				
				if(m_weiboPic != null){
					// open the browser to check the picture button 
					//
					setPositionChild(m_parentManager.m_picBut,WeiboMainManager.sm_picBut_x,m_functionButton_y);
					layoutChild(m_parentManager.m_picBut,m_parentManager.m_picBut.getPreferredWidth(),m_parentManager.m_picBut.getPreferredHeight());
				}
			}
			
			// text area
			//
			setPositionChild(m_parentManager.m_textArea,fsm_headImageWidth + fsm_headImageTextInterval,t_text_y + 2);
			layoutChild(m_parentManager.m_textArea,fsm_textWidth,m_textHeight);
						
			if(m_parentManager.getCurrEditItem() == this){
				
				setPositionChild(m_parentManager.m_editTextArea,0,m_extendHeight);				
				layoutChild(m_parentManager.m_editTextArea,fsm_weiboItemFieldWidth,sm_editTextAreaHeight);
				
				height = m_extendHeight + sm_editTextAreaHeight;
				
			}else{
				height = m_extendHeight;
			}
						
		}else{
			
			if(m_absTextAreaAdded){
				setPositionChild(m_absTextArea,getAbsTextPosX(),sm_timeFont.getHeight() + fsm_headImageTextInterval + 3);
				layoutChild(m_absTextArea,m_absTextArea.getTextWidth(), m_absTextHeight);
			}
			
			height = getPreferredHeight();
		}
		
		setExtent(fsm_weiboItemFieldWidth,height);
	}
	
	static Calendar sm_calendar = Calendar.getInstance();
	static Date		sm_timeDate = new Date();
	
	static protected synchronized String getTimeString(fetchWeibo _weibo){
		
		sm_timeDate.setTime(_weibo.GetDateTime());
		sm_calendar.setTime(sm_timeDate);		
		
		int t_minutes = sm_calendar.get(Calendar.MINUTE);
		String t_day = WeiboItemField.sm_simpleMode?"":(sm_calendar.get(Calendar.MONTH) + "-" +sm_calendar.get(Calendar.DAY_OF_MONTH));
		if(t_minutes > 9){
			return t_day + " " + sm_calendar.get(Calendar.HOUR_OF_DAY) + ":" 
					+ t_minutes;
		}else{
			return t_day + " " + sm_calendar.get(Calendar.HOUR_OF_DAY) + ":" 
					+ "0" + t_minutes;
		}		
	}

	
	public void subpaint(Graphics _g){
		
		if(m_parentManager.getCurrExtendedItem() == this){
			
			int color		= _g.getColor();
			try{
								
				_g.setColor(fsm_darkColor);
				_g.fillRect(0,0,fsm_weiboItemFieldWidth,m_extendHeight);				
							
				int t_textStart_y = WeiboItemField.sm_commentFirst?m_commentText_height : 2;
				
				// draw weibo style 
				//
				weiboTimeLineScreen.sm_weiboUIImage.drawImage(
						_g, weiboTimeLineScreen.GetWeiboSign(m_weibo.GetWeiboStyle()), 0, t_textStart_y);
				
				
				// draw head image
				//
				displayHeadImage(_g,0, t_textStart_y + fsm_weiboSignImageSize + fsm_headImageTextInterval,true);
							
				int t_startSign_x = fsm_weiboSignImageSize;
				
				// name VIP sign
				//
				if(m_weibo.IsSinaVIP()){
					weiboTimeLineScreen.sm_weiboUIImage.drawImage(
							_g,weiboTimeLineScreen.GetVIPSignBitmap(m_weibo.GetWeiboStyle()),t_startSign_x,t_textStart_y);
					
					t_startSign_x += fsm_weiboVIPImageSize;
	
				}
				
				// name BBer sign
				//
				if(m_weibo.IsBBer()){
					weiboTimeLineScreen.sm_weiboUIImage.drawImage(
							_g,weiboTimeLineScreen.GetBBerSignBitmap(),t_startSign_x,t_textStart_y);
				}
				
				// draw text
				//
				_g.setColor(0);
				paintChild(_g,m_parentManager.m_textArea);	
				
				if(!sm_simpleMode){
					paintChild(_g,m_parentManager.m_atBut);
					paintChild(_g,m_parentManager.m_forwardBut);
					paintChild(_g,m_parentManager.m_favoriteBut);
					
					if(m_weiboPic != null){
						paintChild(_g,m_parentManager.m_picBut);
					}
				}
				
				if(m_commentText != null){
					
					int t_commentText_y = WeiboItemField.sm_commentFirst?0:m_commentText_y;
					
					// comment area
					//
					// draw a bubble
					weiboTimeLineScreen.sm_bubbleImage.draw(_g, 0, t_commentText_y - fsm_headImageTextInterval,
							fsm_commentTextWidth + fsm_headImageTextInterval * 2, m_commentText_height + 5,
							WeiboItemField.sm_commentFirst?BubbleImage.BOTTOM_POINT_STYLE:BubbleImage.TOP_POINT_STYLE);
					
					paintChild(_g,m_parentManager.m_commentTextArea);
										
					//_g.setColor(fsm_spaceLineColor);
					//_g.drawRoundRect(1,t_commentText_y,fsm_commentTextWidth,m_commentText_height,10,10);
				}				
				
				//draw the edit child and separate line
				//
				if(m_parentManager.getCurrEditItem() == this){
					_g.setColor(0);
					paintChild(_g,m_parentManager.m_editTextArea);
				}
				
			}finally{
				_g.setColor(color);
			}
			
		}else{		
			paintFocus(_g,isFocus());
		}		
	}
	
	public void paintFocus(Graphics _g,boolean _on){
		
		int t_firstLineHeight = 4;
		int t_leadingSpace = 2;
		
		int color		= _g.getColor();
		Font oldFont	= _g.getFont();
		try{
				
			if(m_absTextAreaAdded){
				
				//draw the background
				//
				fillWeiboFieldBG(_g,0,0,getPreferredWidth(),getPreferredHeight(),false);
				
				// draw the weibo style
				//
				weiboTimeLineScreen.sm_weiboUIImage.drawImage(
						_g,weiboTimeLineScreen.GetWeiboSign(m_weibo.GetWeiboStyle()),t_leadingSpace, 0);
				
				// time string
				//
				int t_oldColor = _g.getColor();
				_g.setColor(0xfafafa);
				drawWeiboTime(_g,t_firstLineHeight);
				_g.setColor(t_oldColor);
				
				t_firstLineHeight += sm_timeFont.getHeight(); 

				// draw the bubble
				//
				weiboTimeLineScreen.sm_bubbleImage.draw(_g,getAbsTextPosX() - 5,t_firstLineHeight - 1,
						m_absTextArea.getTextWidth() + 5,m_absTextHeight + 5,BubbleImage.LEFT_POINT_STYLE);
				
				paintChild(_g,m_absTextArea);
								
				// display head image
				//
				displayHeadImage(_g,t_leadingSpace,t_firstLineHeight,_on);
				t_firstLineHeight += fsm_headImageWidth + fsm_headImageTextInterval;
				
				// name VIP sign
				//
				if(m_weibo.IsSinaVIP()){
					weiboTimeLineScreen.sm_weiboUIImage.drawImage(
							_g,weiboTimeLineScreen.GetVIPSignBitmap(m_weibo.GetWeiboStyle()),
							fsm_weiboSignImageSize + t_leadingSpace ,t_firstLineHeight);
				}	
				
			}else{
				
				//draw the background
				//
				fillWeiboFieldBG(_g,0,0,getPreferredWidth(),getPreferredHeight(),true);
				
				if(_on){
					int t_draw_y = 0;
					int t_y = weiboTimeLineScreen.getWeiboSelectedImage().getHeight() - getPreferredHeight();
					
					if(t_y < 0){
						t_y = 0;
						t_draw_y = -t_y;
					}
					
					// draw selected backgroud
					//
					weiboTimeLineScreen.sm_weiboUIImage.drawImage(_g,weiboTimeLineScreen.getWeiboSelectedImage(),
							0, t_draw_y, getPreferredWidth(), getPreferredHeight(),0, t_y);
				}
				
				// weibo sign 
				//
				weiboTimeLineScreen.sm_weiboUIImage.drawImage(
						_g,weiboTimeLineScreen.GetWeiboSign(m_weibo.GetWeiboStyle()),t_leadingSpace, t_firstLineHeight);
				
				int t_nameLeadingSpace = t_leadingSpace;
				
				if(sm_displayHeadImage){
					// display head image when closed
					//
					displayHeadImage(_g,t_leadingSpace + fsm_weiboSignImageSize + fsm_headImageTextInterval,
									t_firstLineHeight,_on);
					t_nameLeadingSpace += fsm_headImageWidth + fsm_headImageTextInterval;
				}
								
				// display name
				//
				String t_displayName = null;
				if(m_weibo.GetWeiboStyle() == fetchWeibo.TWITTER_WEIBO_STYLE
				|| m_weibo.GetWeiboStyle() == fetchWeibo.QQ_WEIBO_STYLE ){
					t_displayName = m_weibo.GetUserName();
				}else{
					t_displayName = m_weibo.GetUserScreenName();
				}
				
				_g.setFont(sm_boldFont);
				_g.setColor(fsm_weiboNameTextColor);
				int t_nameLength = _g.drawText(t_displayName,
											fsm_weiboSignImageSize + t_nameLeadingSpace,
											t_firstLineHeight,Graphics.ELLIPSIS);
				
				_g.setFont(oldFont);
				
				// add the weibo sign size
				t_nameLength += fsm_weiboSignImageSize + t_nameLeadingSpace;
				
				// name VIP sign
				//
				if(m_weibo.IsSinaVIP()){
					weiboTimeLineScreen.sm_weiboUIImage.drawImage(
							_g,weiboTimeLineScreen.GetVIPSignBitmap(m_weibo.GetWeiboStyle()),
							t_nameLength + t_leadingSpace, t_firstLineHeight);
					
					t_nameLength += fsm_weiboVIPImageSize;

				}
				
				// name BBer sign
				//
				if(m_weibo.IsBBer()){
					weiboTimeLineScreen.sm_weiboUIImage.drawImage(
							_g,weiboTimeLineScreen.GetBBerSignBitmap(),t_nameLength + t_nameLeadingSpace, t_firstLineHeight);
				}
				
				// contain abstract
				//
				_g.setColor(0xc0c0c0);			
				
				int t_abs_x = t_nameLeadingSpace + fsm_weiboSignImageSize;
				int t_abs_y = sm_fontHeight + fsm_headImageTextInterval;
				_g.drawText(m_simpleAbstract,t_abs_x,t_abs_y,Graphics.ELLIPSIS);
					        
				// draw time string , weibo pic/comment
				//
				drawWeiboTime(_g,t_firstLineHeight);
			}  	       

		}finally{
			_g.setColor(color);
			_g.setFont(oldFont);
		}
	}
	
	private void drawWeiboTime(Graphics _g,int _y){
		
		// draw time string
		//
        String t_dateString = getTimeString(m_weibo);
    	_g.setFont(sm_timeFont);
    	int t_time_x = _g.drawText(t_dateString,fsm_weiboItemFieldWidth - sm_timeFont.getAdvance(t_dateString)
				,_y,Graphics.ELLIPSIS);
    	
    	t_time_x = fsm_weiboItemFieldWidth - t_time_x;
    	
    	// draw weibo picture or comment sign
    	//
    	if(m_weiboPic != null){
    		t_time_x -= weiboTimeLineScreen.getWeiboPicSignImage().getWidth();
    		
    		weiboTimeLineScreen.sm_weiboUIImage.drawImage(_g,weiboTimeLineScreen.getWeiboPicSignImage(),t_time_x, _y);
    		
    		t_time_x -= 3;
		}
    	
    	if(m_commentText != null){
    		
    		t_time_x -= weiboTimeLineScreen.getWeiboCommentSignImage().getWidth();
    		
    		weiboTimeLineScreen.sm_weiboUIImage.drawImage(_g,weiboTimeLineScreen.getWeiboCommentSignImage(),t_time_x, _y);
    	}
	}
	
	/**
	 * get the image url by the weibo style(full small or thumb)
	 * @param _style 0:full 1:small 2:thumb
	 * @return
	 */
	public String getImageURL(int _style){
		
		if(m_weiboPic != null){
			
			String t_ret = m_weiboPic;
			
			switch(m_weibo.GetWeiboStyle()){
			case fetchWeibo.QQ_WEIBO_STYLE:
				if(_style == 0){
					t_ret = t_ret + "/2000";
				}else if(_style == 1){
					t_ret = t_ret + "/460";
				}else{
					t_ret = t_ret + "/160";
				}
				
				break;
				
			case fetchWeibo.SINA_WEIBO_STYLE:
				
				int t_index = t_ret.indexOf("large");
				
				if(t_index != -1){
					if(_style == 1){
						t_ret = t_ret.substring(0,t_index) + "bmiddle" + t_ret.substring(t_index + 5);
					}else if(_style == 2){
						t_ret = t_ret.substring(0,t_index) + "thumbnail" + t_ret.substring(t_index + 5);
					}
				}
				
				break;
			}
			
			return t_ret;
			
		}else{
			return "";
		}
		
	}
		
	private void displayHeadImage(Graphics _g,int _x,int _y,boolean _focus){
		
		_g.drawBitmap(_x,_y,fsm_headImageWidth,fsm_headImageWidth,m_headImage.m_headImage,0,0);
		
		weiboTimeLineScreen.sm_weiboUIImage.drawImage(_g, weiboTimeLineScreen.GetHeadImageMaskBitmap(), _x, _y);
	}
	
	// weibo field backgroud
	//
	private static ImageUnit sm_weiboFieldBG = null;
	private static ImageUnit sm_weiboFieldBG_spaceLine = null;
		
	public static void fillWeiboFieldBG(Graphics _g,int _x,int _y,int _width,int _height,boolean _topLine){
		if(sm_weiboFieldBG == null){
			sm_weiboFieldBG = weiboTimeLineScreen.sm_weiboUIImage.getImageUnit("weibo_bg");
			sm_weiboFieldBG_spaceLine = weiboTimeLineScreen.sm_weiboUIImage.getImageUnit("space_line");
		}
		
		weiboTimeLineScreen.sm_weiboUIImage.fillImageBlock(_g, sm_weiboFieldBG, _x, _y, _width, _height);
		if(_topLine){
			weiboTimeLineScreen.sm_weiboUIImage.drawBitmapLine(_g, sm_weiboFieldBG_spaceLine, _x, _y, _width);
		}
	}

}
