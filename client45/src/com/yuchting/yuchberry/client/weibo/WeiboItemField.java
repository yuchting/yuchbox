package com.yuchting.yuchberry.client.weibo;

import java.util.Calendar;
import java.util.Date;

import local.localResource;
import net.rim.device.api.io.IOUtilities;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.component.TextField;

import com.yuchting.yuchberry.client.recvMain;

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
	
	public final static int		fsm_darkColor				= 0xc1c1c1;
	public final static int		fsm_promptTextBGColor		= 0xffffcc;
	public final static int		fsm_promptTextBorderColor	= 0xc0c0c0;
	
	public final static int		fsm_selectedColor			= 0x42a2de;
	public final static int		fsm_absTextColor			= 0x8f8f8f;
	
	public final static int		fsm_weiboTextBGColor		= 0xf6fdff;
	
	public final static int		fsm_weiboCommentFGColor		= 0x6f6f6f;
	public final static int		fsm_weiboCommentBGColor		= 0xd8d8d8;
	
	public final static int		fsm_spaceLineColor			= 0x5f5f5f;
	public final static int		fsm_ownWeiboColor			= 0xffffee;
	
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
	
	static private Bitmap sm_weiboSelected = null;
	static private Bitmap sm_weiboCommentSign = null;
	static private Bitmap sm_weiboPicSign = null;
	
	static {
		try{
			byte[] data = IOUtilities.streamToBytes(weiboTimeLineScreen.sm_mainApp.getClass().getResourceAsStream("/weibo/weibo_sel.png"));
			sm_weiboSelected = EncodedImage.createEncodedImage(data, 0, data.length).getBitmap();
			
			data = IOUtilities.streamToBytes(weiboTimeLineScreen.sm_mainApp.getClass().getResourceAsStream("/weibo/commentSign.png"));
			sm_weiboCommentSign = EncodedImage.createEncodedImage(data, 0, data.length).getBitmap();
			
			data = IOUtilities.streamToBytes(weiboTimeLineScreen.sm_mainApp.getClass().getResourceAsStream("/weibo/picSign.png"));
			sm_weiboPicSign = EncodedImage.createEncodedImage(data, 0, data.length).getBitmap();
			
		}catch(Exception e){
			weiboTimeLineScreen.sm_mainApp.DialogAlertAndExit("inner load error:" + e.getMessage());
		}
	}
	
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
			m_extendHeight 			= m_functionButton_y + WeiboMainManager.fsm_controlButtonHeight + fsm_headImageTextInterval;
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
				return Math.max(m_absTextHeight + 5,fsm_weiboSignImageSize + fsm_headImageWidth + 10);
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
				
				t_text_y += sm_bubble_block_size / 2;
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
				setPositionChild(m_absTextArea,getAbsTextPosX(),fsm_headImageTextInterval + 1);
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
							
				int t_textStart_y = WeiboItemField.sm_commentFirst?m_commentText_height : 0;
				
				// draw weibo style 
				//
				_g.drawBitmap(0, t_textStart_y, fsm_weiboSignImageSize, fsm_weiboSignImageSize, 
							weiboTimeLineScreen.GetWeiboSign(m_weibo.GetWeiboStyle()), 0, 0);
				
				// draw head image
				//
				displayHeadImage(_g,0, t_textStart_y + fsm_weiboSignImageSize + fsm_headImageTextInterval,true);
							
				int t_startSign_x = fsm_weiboSignImageSize;
				
				// name VIP sign
				//
				if(m_weibo.IsSinaVIP()){
					_g.drawBitmap(t_startSign_x,t_textStart_y,fsm_weiboVIPImageSize, fsm_weiboVIPImageSize, 
							weiboTimeLineScreen.GetVIPSignBitmap(m_weibo.GetWeiboStyle()), 0, 0);
					
					t_startSign_x += fsm_weiboVIPImageSize;
	
				}
				
				// name BBer sign
				//
				if(m_weibo.IsBBer()){
					_g.drawBitmap(t_startSign_x,t_textStart_y,fsm_weiboBBerImageSize, fsm_weiboBBerImageSize, 
									weiboTimeLineScreen.GetBBerSignBitmap(), 0, 0);
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
					drawBubble(_g, 0, t_commentText_y - fsm_headImageTextInterval,
							fsm_commentTextWidth + fsm_headImageTextInterval * 2, m_commentText_height + 5,
							WeiboItemField.sm_commentFirst?fsm_bubble_bottom_point : fsm_bubble_top_point);
					
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
				fillWeiboFieldBG(_g,0,0,getPreferredWidth(),getPreferredHeight());

				// draw the bubble
				//
				drawBubble(_g,getAbsTextPosX() - 5,fsm_headImageTextInterval,
						m_absTextArea.getTextWidth() + 5,m_absTextHeight,fsm_bubble_left_point);
				
				paintChild(_g,m_absTextArea);
				
				// draw the weibo style
				//
				_g.drawBitmap(2, 2, fsm_weiboSignImageSize, fsm_weiboSignImageSize, 
						weiboTimeLineScreen.GetWeiboSign(m_weibo.GetWeiboStyle()), 0, 0);
				
				if(sm_displayHeadImage){
					// display head image when closed
					//
					displayHeadImage(_g,t_leadingSpace,fsm_weiboSignImageSize + t_leadingSpace * 2 ,_on);
				}
				
				// name VIP sign
				//
				if(m_weibo.IsSinaVIP()){
					_g.drawBitmap(fsm_weiboSignImageSize + t_leadingSpace ,t_leadingSpace, fsm_weiboVIPImageSize, fsm_weiboVIPImageSize, 
							weiboTimeLineScreen.GetVIPSignBitmap(m_weibo.GetWeiboStyle()), 0, 0);
				}	
				
			}else{
				
				//draw the background
				//
				fillWeiboFieldBG(_g,0,0,getPreferredWidth(),getPreferredHeight());
				
				if(_on){
					int t_draw_y = 0;
					int t_y = sm_weiboSelected.getHeight() - getPreferredHeight();
					
					if(t_y < 0){
						t_y = 0;
						t_draw_y = -t_y;
					}
					
					// draw selected backgroud
					//
					_g.drawBitmap(0, t_draw_y, getPreferredWidth(), getPreferredHeight(), sm_weiboSelected,0, t_y);
				}
				
				// weibo sign 
				//
				_g.drawBitmap(t_leadingSpace, t_firstLineHeight, fsm_weiboSignImageSize, fsm_weiboSignImageSize, 
							weiboTimeLineScreen.GetWeiboSign(m_weibo.GetWeiboStyle()), 0, 0);			
				
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
				_g.setColor(0xffffff);
				int t_nameLength = _g.drawText(t_displayName,
											fsm_weiboSignImageSize + t_nameLeadingSpace,
											t_firstLineHeight,Graphics.ELLIPSIS);
				
				_g.setFont(oldFont);
				
				// add the weibo sign size
				t_nameLength += fsm_weiboSignImageSize + t_nameLeadingSpace;
				
				// name VIP sign
				//
				if(m_weibo.IsSinaVIP()){
					_g.drawBitmap(t_nameLength + t_leadingSpace, t_firstLineHeight, fsm_weiboVIPImageSize, fsm_weiboVIPImageSize, 
							weiboTimeLineScreen.GetVIPSignBitmap(m_weibo.GetWeiboStyle()), 0, 0);
					
					t_nameLength += fsm_weiboVIPImageSize;

				}
				
				// name BBer sign
				//
				if(m_weibo.IsBBer()){
					_g.drawBitmap(t_nameLength + t_nameLeadingSpace, t_firstLineHeight, 
									fsm_weiboBBerImageSize, fsm_weiboBBerImageSize, 
									weiboTimeLineScreen.GetBBerSignBitmap(), 0, 0);
				}
				
				// contain abstract
				//
				_g.setColor(0xc0c0c0);			
				
				int t_abs_x = t_nameLeadingSpace + fsm_weiboSignImageSize;
				int t_abs_y = sm_fontHeight + fsm_headImageTextInterval;
				_g.drawText(m_simpleAbstract,t_abs_x,t_abs_y,Graphics.ELLIPSIS);
				
	        
				// draw time string
				//
		        String t_dateString = getTimeString(m_weibo);
	        	_g.setFont(sm_timeFont);
	        	int t_time_x = _g.drawText(t_dateString,fsm_weiboItemFieldWidth - sm_timeFont.getAdvance(t_dateString)
        				,t_firstLineHeight,Graphics.ELLIPSIS);
	        	
	        	t_time_x = fsm_weiboItemFieldWidth - t_time_x;
	        	
	        	// draw weibo picture or comment sign
	        	//
	        	if(m_weiboPic != null){
	        		t_time_x -= sm_weiboPicSign.getWidth();
	        		
	        		_g.drawBitmap(t_time_x, t_firstLineHeight, 
	        				sm_weiboPicSign.getWidth(), sm_weiboPicSign.getHeight(), sm_weiboPicSign, 0, 0);
	        		
	        		t_time_x -= 3;
        		}
	        	
	        	if(m_commentText != null){
	        		
	        		t_time_x -= sm_weiboCommentSign.getWidth();
	        		
	        		_g.drawBitmap(t_time_x, t_firstLineHeight, 
	        				sm_weiboCommentSign.getWidth(), sm_weiboCommentSign.getHeight(), sm_weiboCommentSign, 0, 0);
	        	}
			}  	       

		}finally{
			_g.setColor(color);
			_g.setFont(oldFont);
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
		_g.drawBitmap(_x,_y,fsm_headImageWidth,fsm_headImageWidth,weiboTimeLineScreen.GetHeadImageMaskBitmap(),0,0);
		
//		if(m_parentManager.getCurrExtendedItem() == null){
//			if(!_focus){
//				_g.drawBitmap(_x,_y,fsm_headImageWidth,fsm_headImageWidth,weiboTimeLineScreen.GetHeadImageMaskBitmap(),0,0);
//			}
//		}else if(m_parentManager.getCurrExtendedItem() == this){
//			if(m_parentManager.getCurrEditItem() == null){
//				_g.drawBitmap(_x,_y,fsm_headImageWidth,fsm_headImageWidth,weiboTimeLineScreen.GetHeadImageMaskBitmap(),0,0);
//			}			
//		}
	}
	
	// weibo field backgroud
	//
	private static Bitmap sm_weiboFieldBG = null;
	private static Bitmap sm_weiboFieldBG_spaceLine = null;
	
	private static int sm_weiboFieldBG_block_size = 0;
	private static int sm_weiboFieldBG_spaceLine_width = 0;
	private static int sm_weiboFieldBG_spaceLine_height = 0;
	static{
		try{
			byte[] bytes = IOUtilities.streamToBytes(weiboTimeLineScreen.sm_mainApp.getClass().
					getResourceAsStream("/weibo/weibo_bg.png"));		
			sm_weiboFieldBG = EncodedImage.createEncodedImage(bytes, 0, bytes.length).getBitmap();
			
			bytes = IOUtilities.streamToBytes(weiboTimeLineScreen.sm_mainApp.getClass().
						getResourceAsStream("/weibo/space_line.png"));		
			sm_weiboFieldBG_spaceLine = EncodedImage.createEncodedImage(bytes, 0, bytes.length).getBitmap();
				
			sm_weiboFieldBG_block_size = sm_weiboFieldBG.getWidth();
			
			sm_weiboFieldBG_spaceLine_width = sm_weiboFieldBG_spaceLine.getWidth();
			sm_weiboFieldBG_spaceLine_height = sm_weiboFieldBG_spaceLine.getHeight();
			
		}catch(Exception e){
			weiboTimeLineScreen.sm_mainApp.DialogAlertAndExit("inner load error:" + e.getMessage());
		}
	}
	
	public static void fillWeiboFieldBG(Graphics _g,int _x,int _y,int _width,int _height){
		
		int t_horz_num =  _width / sm_weiboFieldBG_block_size;
		int t_vert_num =  _height / sm_weiboFieldBG_block_size;		

		for(int i = 0 ;i < t_vert_num;i++){
			for(int j = 0; j < t_horz_num;j++){
				_g.drawBitmap(_x + j * sm_weiboFieldBG_block_size,_y + i * sm_weiboFieldBG_block_size, 
						sm_weiboFieldBG_block_size, sm_weiboFieldBG_block_size, sm_weiboFieldBG, 0, 0);
			}
		}
		
		int t_horz_remain_width = _width % sm_weiboFieldBG_block_size;
		if(t_horz_remain_width > 0){
			
			int t_horz_x	= _x + t_horz_num * sm_weiboFieldBG_block_size;

			for(int i = 0 ;i < t_vert_num;i++){
				_g.drawBitmap(t_horz_x, _y + i * sm_weiboFieldBG_block_size, 
						t_horz_remain_width, sm_weiboFieldBG_block_size, sm_weiboFieldBG, 0, 0);
				
			}
		}
		
		int t_vert_remain_height = _height % sm_weiboFieldBG_block_size;
		if(t_vert_remain_height > 0){
			
			int t_vert_y	= _y + t_vert_num * sm_weiboFieldBG_block_size;

			for(int i = 0 ;i < t_horz_num;i++){
				_g.drawBitmap(_x + i * sm_weiboFieldBG_block_size, t_vert_y , 
						sm_weiboFieldBG_block_size, t_vert_remain_height, sm_weiboFieldBG, 0, 0);
			}
		}
		
		if(t_horz_remain_width > 0 && t_vert_remain_height > 0){
			_g.drawBitmap(_x + t_horz_num * sm_weiboFieldBG_block_size, _y + t_vert_num * sm_weiboFieldBG_block_size, 
					t_horz_remain_width, t_vert_remain_height, sm_weiboFieldBG, 0, 0);
		}
		
		// draw the space line
		//
		t_horz_num = _width / sm_weiboFieldBG_spaceLine_width;
		for(int i = 0 ;i < t_horz_num;i++){
			_g.drawBitmap(_x + i * sm_weiboFieldBG_spaceLine_width, _y , 
					sm_weiboFieldBG_spaceLine_width, sm_weiboFieldBG_spaceLine_height, sm_weiboFieldBG_spaceLine, 0, 0);
		}
		
		t_horz_remain_width = _width % sm_weiboFieldBG_block_size;
		if(t_horz_remain_width > 0){
			_g.drawBitmap(_x + t_horz_num * sm_weiboFieldBG_spaceLine_width, _y, 
					t_horz_remain_width, sm_weiboFieldBG_spaceLine_height, sm_weiboFieldBG_spaceLine, 0, 0);
		}
		
	}
	
	// bubble drawing data
	//
	//
	private final static int fsm_bubble_top_left 	= 0;
	private final static int fsm_bubble_top 			= 1;
	private final static int fsm_bubble_top_right 	= 2;
	private final static int fsm_bubble_right		= 3;
	
	private final static int fsm_bubble_bottom_right = 4;
	private final static int fsm_bubble_bottom 		= 5;
	private final static int fsm_bubble_bottom_left 	= 6;
	private final static int fsm_bubble_left 		= 7;
	
	public  final static int fsm_bubble_top_point		= 8;
	public final static int fsm_bubble_right_point		= 9;
	public final static int fsm_bubble_bottom_point		= 10;
	public final static int fsm_bubble_left_point		= 11;
	
	public final static int fsm_bubble_no_point			= 12;
		
	private final static String[] fsm_bubble_string	=
	{
		"/weibo/bubble_top_left.png",
		"/weibo/bubble_top.png",
		"/weibo/bubble_top_right.png",
		"/weibo/bubble_right.png",
		
		"/weibo/bubble_bottom_right.png",
		"/weibo/bubble_bottom.png",
		"/weibo/bubble_bottom_left.png",
		"/weibo/bubble_left.png",
		
		"/weibo/bubble_top_point.png",
		"/weibo/bubble_right_point.png",
		"/weibo/bubble_bottom_point.png",
		"/weibo/bubble_left_point.png",
	};
	
	private final static Bitmap[] fsm_bubble_image = 
	{
		null,null,null,null,
		null,null,null,null,
		null,null,null,null,
	};
	
	private static int sm_bubble_block_size = 0;
	
	static{
		try{
			for(int i = 0 ;i < fsm_bubble_string.length;i++){
				byte[] bytes = IOUtilities.streamToBytes(weiboTimeLineScreen.sm_mainApp.getClass().getResourceAsStream(fsm_bubble_string[i]));		
				fsm_bubble_image[i] = EncodedImage.createEncodedImage(bytes, 0, bytes.length).getBitmap();	
			}
			
			sm_bubble_block_size = fsm_bubble_image[0].getWidth();
			
		}catch(Exception e){
			weiboTimeLineScreen.sm_mainApp.DialogAlertAndExit("inner load error:" + e.getMessage());
		}
	}
	
	public static void drawBubble(Graphics _g,int _x,int _y,int _width,int _height,int _point_style){
		
		// draw the corner
		//
		_g.drawBitmap(_x, _y, sm_bubble_block_size, sm_bubble_block_size, 
				fsm_bubble_image[fsm_bubble_top_left], 0, 0);
		
		_g.drawBitmap(_x + (_width - sm_bubble_block_size), _y, sm_bubble_block_size, sm_bubble_block_size, 
				fsm_bubble_image[fsm_bubble_top_right], 0, 0);
		
		_g.drawBitmap(_x + (_width - sm_bubble_block_size), _y + (_height - sm_bubble_block_size), 
				sm_bubble_block_size, sm_bubble_block_size, 
				fsm_bubble_image[fsm_bubble_bottom_right], 0, 0);
		
		_g.drawBitmap(_x , _y + (_height - sm_bubble_block_size), 
				sm_bubble_block_size, sm_bubble_block_size, 
				fsm_bubble_image[fsm_bubble_bottom_left], 0, 0);
		
		// draw horz edge
		//
		int t_horz_num =  (_width - sm_bubble_block_size * 2) / sm_bubble_block_size;
		if(t_horz_num > 0){
			
			int t_horz_x = _x + sm_bubble_block_size;
			for(int i = 0;i < t_horz_num;i++){
				_g.drawBitmap(t_horz_x, _y,sm_bubble_block_size, sm_bubble_block_size, 
						fsm_bubble_image[fsm_bubble_top], 0, 0);
				
				_g.drawBitmap(t_horz_x, _y + (_height - sm_bubble_block_size),sm_bubble_block_size, sm_bubble_block_size, 
						fsm_bubble_image[fsm_bubble_bottom], 0, 0);
				
				t_horz_x += sm_bubble_block_size;
			}
		}
		int t_horz_remain_width = (_width - sm_bubble_block_size * 2) % sm_bubble_block_size;
		if(t_horz_remain_width > 0){
		
			int t_horz_x = _x + t_horz_num * sm_bubble_block_size + sm_bubble_block_size;
			
			_g.drawBitmap(t_horz_x, _y,t_horz_remain_width, sm_bubble_block_size, 
					fsm_bubble_image[fsm_bubble_top], 0, 0);
			
			_g.drawBitmap(t_horz_x, _y + (_height - sm_bubble_block_size),t_horz_remain_width, 
					sm_bubble_block_size, fsm_bubble_image[fsm_bubble_bottom], 0, 0);
		}
		
		// draw vert edge
		//
		int t_vert_num =  (_height - sm_bubble_block_size * 2) / sm_bubble_block_size;
		if(t_vert_num > 0){

			int t_vert_y = _y + sm_bubble_block_size;
			for(int i = 0;i < t_vert_num;i++){
				_g.drawBitmap(_x, t_vert_y,sm_bubble_block_size, sm_bubble_block_size, 
						fsm_bubble_image[fsm_bubble_left], 0, 0);
				
				_g.drawBitmap(_x + (_width - sm_bubble_block_size), t_vert_y,sm_bubble_block_size, 
						sm_bubble_block_size, fsm_bubble_image[fsm_bubble_right], 0, 0);
				
				t_vert_y += sm_bubble_block_size;
			}
		}
		
		int t_vert_remain_height = (_height - sm_bubble_block_size * 2) % sm_bubble_block_size;
		if(t_vert_remain_height > 0){
			
			int t_vert_y = _y + t_vert_num * sm_bubble_block_size + sm_bubble_block_size;
			
			_g.drawBitmap(_x, t_vert_y,sm_bubble_block_size, t_vert_remain_height, 
					fsm_bubble_image[fsm_bubble_left], 0, 0);
			
			_g.drawBitmap(_x + (_width - sm_bubble_block_size), t_vert_y,sm_bubble_block_size, 
					t_vert_remain_height, fsm_bubble_image[fsm_bubble_right], 0, 0);
		}
		
		// draw the point 
		//
		switch(_point_style){
		case fsm_bubble_left_point:
			_g.drawBitmap(_x - sm_bubble_block_size / 2, _y + sm_bubble_block_size , 
					sm_bubble_block_size, sm_bubble_block_size, fsm_bubble_image[fsm_bubble_left_point],0, 0);
			break;
		case fsm_bubble_top_point:
			_g.drawBitmap(_x + sm_bubble_block_size , _y - sm_bubble_block_size / 2, 
					sm_bubble_block_size, sm_bubble_block_size, fsm_bubble_image[fsm_bubble_top_point],0, 0);
			break;
		case fsm_bubble_right_point:
			_g.drawBitmap(_x + _width + sm_bubble_block_size / 2, _y + sm_bubble_block_size , 
					sm_bubble_block_size, sm_bubble_block_size, fsm_bubble_image[fsm_bubble_right_point],0, 0);
			break;
		case fsm_bubble_bottom_point:
			_g.drawBitmap(_x + sm_bubble_block_size * 2, _y + _height - sm_bubble_block_size / 2, 
					sm_bubble_block_size, sm_bubble_block_size, fsm_bubble_image[fsm_bubble_bottom_point],0, 0);
			break;
		}
		
		// fill the inner rectangle
		//
		int t_oldColor = _g.getColor();
		try{
			_g.setColor(fsm_weiboCommentBGColor);
			
			_g.fillRect(_x + sm_bubble_block_size ,_y + sm_bubble_block_size,
					_width - sm_bubble_block_size * 2,_height - sm_bubble_block_size * 2);
		}finally{
			_g.setColor(t_oldColor);
		}
	}
}
