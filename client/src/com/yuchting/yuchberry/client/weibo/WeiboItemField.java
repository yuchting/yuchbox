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
package com.yuchting.yuchberry.client.weibo;

import java.util.Calendar;
import java.util.Date;

import local.yblocalResource;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;

import com.yuchting.yuchberry.client.ObjectAllocator;
import com.yuchting.yuchberry.client.recvMain;
import com.yuchting.yuchberry.client.im.MainIMScreen;
import com.yuchting.yuchberry.client.ui.BubbleImage;
import com.yuchting.yuchberry.client.ui.ImageUnit;
import com.yuchting.yuchberry.client.ui.SliderHeader;
import com.yuchting.yuchberry.client.ui.WeiboHeadImage;
import com.yuchting.yuchberry.client.ui.WeiboTextField;


public class WeiboItemField extends Manager{
	
	public final static int		fsm_weiboItemFieldWidth		= recvMain.fsm_display_width - WeiboMainManager.fsm_scrollbarSize;
	public final static int		fsm_maxWeiboTextLength		= 140;

	public final static int		fsm_headImageTextInterval	= 2;
	
	public final static int		fsm_weiboSignImageSize		= 16;
	
	public final static int		fsm_weiboVIPImageSize		= 12;
	public final static int		fsm_weiboBBerImageSize		= 12; 
	
	public final static int		fsm_maxWeiboAbstractLength	= 23; //列表微博长度//
	
	public final static int		fsm_textWidth				= fsm_weiboItemFieldWidth - WeiboHeadImage.fsm_headImageWidth - fsm_headImageTextInterval;
	public final static int		fsm_editTextWidth			= fsm_weiboItemFieldWidth;
	
	public final static int		fsm_commentTextWidth		= fsm_weiboItemFieldWidth - fsm_headImageTextInterval * 2;
	
	public final static int		fsm_darkColor				= 0xb1aeae;
	public final static int		fsm_promptTextBGColor		= 0xffffcc;
	public final static int		fsm_promptTextBorderColor	= 0xc0c0c0;	
	
	public final static int		fsm_selectedColor			= 0xcbfea5;
	
	public final static int		fsm_timeTextColor			= recvMain.sm_standardUI?0xfb9620:0x8bc5f8;
	
	public final static int		fsm_extendTextColor			= recvMain.sm_standardUI?0:0xd0d0d0;
	public final static int		fsm_extendBGColor			= recvMain.sm_standardUI?0xdbf3fe:0x1f2d39;//Original: 0xc0deed:0x1f2d39;微博内容字体背景//
	
	public final static int		fsm_absTextColor			= recvMain.sm_standardUI?0x586061:0xbbc1c6;
	
	public final static int		fsm_weiboNameTextColor		= recvMain.sm_standardUI?0x1e3f5e:0xe5e3cf;

	public final static int		fsm_weiboCommentFGColor		= recvMain.sm_standardUI?0x6d6f6f:0x84c3fa;
	public final static int		fsm_weiboCommentBGColor		= recvMain.sm_standardUI?0xFFFFFF:0x2b3d4d;
	
	
	// BasicEditField for 4.2os
	public static WeiboTextField 	sm_testTextArea	= new WeiboTextField(0,0){
		public void setText(String _text){
			super.setText(_text);
			layout(WeiboItemField.fsm_textWidth,1000);
		}
	};	
	
	public static WeiboTextField 	sm_testCommentTextArea	= new WeiboTextField(0,0){
		public void setText(String _text){
			super.setText(_text);
			layout(WeiboItemField.fsm_commentTextWidth,1000);
		}
	};	
	
	public static Font		sm_defaultFont				= sm_testTextArea.getFont();
	public static Font		sm_timeFont					= sm_testTextArea.getFont().derive(sm_defaultFont.getStyle(),16);
	public static Font		sm_absFont					= sm_testTextArea.getFont().derive(sm_defaultFont.getStyle(),sm_defaultFont.getHeight() - 2); // yuchting@2012-4-3 把绝对高度20改成相对高度，不同系统的字体高度是不一样的
	public static Font		sm_boldFont					= sm_testTextArea.getFont().derive(sm_defaultFont.getStyle() | Font.BOLD,sm_defaultFont.getHeight());
	public static int		sm_fontHeight				= sm_defaultFont.getHeight()+ 3;
	public static int		sm_imageAreaMinHeight		= fsm_weiboSignImageSize + WeiboHeadImage.fsm_headImageWidth + fsm_headImageTextInterval;
	
	public final static int	fsm_closeHeight			= sm_fontHeight * 2 + 1; 
 
	static public final int fsm_controlField_text 		= 0;
	static public final int fsm_controlField_comment 	= 1;
	static public final int fsm_controlField_forwardBtn	= 2;
	static public final int fsm_controlField_atBtn 		= 3;
	static public final int fsm_controlField_favorBtn	= 4;
	static public final int fsm_controlField_picBtn		= 5;
	static public final int fsm_controlField_followBtn	= 6;
	static public final int fsm_controlField_editText	= 7;
		
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
	
	String					m_displayName	= null;
	
	static ObjectAllocator	sm_absTextAreaAllocator = new ObjectAllocator("com.yuchting.yuchberry.client.weibo.ContentTextField");
	
	public static BubbleImage		sm_selectedBackgroud = new BubbleImage(
										recvMain.sm_weiboUIImage.getImageUnit("selected_top_left"),
										recvMain.sm_weiboUIImage.getImageUnit("selected_top"),
										recvMain.sm_weiboUIImage.getImageUnit("selected_top_right"),
										recvMain.sm_weiboUIImage.getImageUnit("selected_right"),
										
										recvMain.sm_weiboUIImage.getImageUnit("selected_bottom_right"),
										recvMain.sm_weiboUIImage.getImageUnit("selected_bottom"),
										recvMain.sm_weiboUIImage.getImageUnit("selected_bottom_left"),
										recvMain.sm_weiboUIImage.getImageUnit("selected_left"),
										
										recvMain.sm_weiboUIImage.getImageUnit("selected_inner_block"),
										null,
										recvMain.sm_weiboUIImage);
	
	public static BubbleImage 			sm_bubbleImage = null;
	public static ImageUnit 			sm_bubbleSelected = null;
	static{
		if(recvMain.sm_standardUI){
			sm_bubbleImage = new BubbleImage(
					recvMain.sm_weiboUIImage.getImageUnit("bubble_top_left_1"),
					recvMain.sm_weiboUIImage.getImageUnit("bubble_top_1"),
					recvMain.sm_weiboUIImage.getImageUnit("bubble_top_right_1"),
					recvMain.sm_weiboUIImage.getImageUnit("bubble_right_1"),
					
					recvMain.sm_weiboUIImage.getImageUnit("bubble_bottom_right_1"),
					recvMain.sm_weiboUIImage.getImageUnit("bubble_bottom_1"),
					recvMain.sm_weiboUIImage.getImageUnit("bubble_bottom_left_1"),
					recvMain.sm_weiboUIImage.getImageUnit("bubble_left_1"),
					
					recvMain.sm_weiboUIImage.getImageUnit("bubble_inner_block_1"),
					new ImageUnit[]{
						recvMain.sm_weiboUIImage.getImageUnit("bubble_left_point_1"),
						recvMain.sm_weiboUIImage.getImageUnit("bubble_top_point_1"),
						recvMain.sm_weiboUIImage.getImageUnit("bubble_right_point_1"),
						recvMain.sm_weiboUIImage.getImageUnit("bubble_bottom_point_1"),
					},
					recvMain.sm_weiboUIImage);
			
		}else{
			sm_bubbleImage = new BubbleImage(
					recvMain.sm_weiboUIImage.getImageUnit("bubble_top_left"),
					recvMain.sm_weiboUIImage.getImageUnit("bubble_top"),
					recvMain.sm_weiboUIImage.getImageUnit("bubble_top_right"),
					recvMain.sm_weiboUIImage.getImageUnit("bubble_right"),
					
					recvMain.sm_weiboUIImage.getImageUnit("bubble_bottom_right"),
					recvMain.sm_weiboUIImage.getImageUnit("bubble_bottom"),
					recvMain.sm_weiboUIImage.getImageUnit("bubble_bottom_left"),
					recvMain.sm_weiboUIImage.getImageUnit("bubble_left"),
					
					recvMain.sm_weiboUIImage.getImageUnit("bubble_inner_block"),
					new ImageUnit[]{
						recvMain.sm_weiboUIImage.getImageUnit("bubble_left_point"),
						recvMain.sm_weiboUIImage.getImageUnit("bubble_top_point"),
						recvMain.sm_weiboUIImage.getImageUnit("bubble_right_point"),
						recvMain.sm_weiboUIImage.getImageUnit("bubble_bottom_point"),
					},
					recvMain.sm_weiboUIImage);
		}
		
		sm_bubbleSelected = recvMain.sm_weiboUIImage.getImageUnit("weibo_vert_sel");
	}
	
	public WeiboItemField(){
		super(Manager.NO_VERTICAL_SCROLL);
	}
		
	public void init(fetchWeibo _weibo,WeiboHeadImage _headImage,WeiboMainManager _manager){
		
		destroy();
		
		m_weibo 		= _weibo;
		m_headImage 	= _headImage;
		m_parentManager = _manager;
				
		StringBuffer t_weiboTextBuffer = new StringBuffer();
		
		t_weiboTextBuffer.append("@").append(m_weibo.GetUserScreenName()).append(" :").append(m_weibo.GetText());
		
		if(!recvMain.sm_simpleMode && m_weibo.GetSource().length() != 0){
			t_weiboTextBuffer.append("\n       --").append(recvMain.sm_local.getString(yblocalResource.WEIBO_SOURCE_PREFIX))
			.append(parseSource(m_weibo.GetSource()));
		}						
		
		m_weiboText 		= t_weiboTextBuffer.toString();
		t_weiboTextBuffer = null;
		
		sm_testTextArea.setText(m_weiboText);
					
		m_simpleAbstract		= getSimpleAbstract(_weibo);

		m_textHeight			= sm_testTextArea.getHeight() ; 

		m_functionButton_y		= Math.max(m_textHeight,sm_imageAreaMinHeight) + fsm_headImageTextInterval + fsm_weiboSignImageSize;
				
		if(m_weibo.GetCommentWeibo() != null){
			
			fetchWeibo t_comment = m_weibo.GetCommentWeibo();
			
			StringBuffer t_commentText = new StringBuffer();
			t_commentText.append("@").append(t_comment.GetUserScreenName()).append(":").append(t_comment.GetText());

			if(!recvMain.sm_simpleMode && t_comment.GetSource().length() != 0){		
				t_commentText.append("\n       --").append(recvMain.sm_local.getString(yblocalResource.WEIBO_SOURCE_PREFIX))
							.append(parseSource(t_comment.GetSource()));
			}
			
					
			m_commentText = t_commentText.toString();
			
			t_commentText = null;
			
			sm_testCommentTextArea.setText(m_commentText);
			
			m_commentText_height = sm_testCommentTextArea.getHeight() + fsm_headImageTextInterval;
			
			m_commentText_y = m_functionButton_y + fsm_headImageTextInterval * 2;
			
			m_functionButton_y = m_commentText_y + m_commentText_height + fsm_headImageTextInterval ;
			
			if(m_weibo.GetCommentWeibo().GetOriginalPic().length() != 0 ){
				m_weiboPic = m_weibo.GetCommentWeibo().GetOriginalPic();
			}
			
		}else{
			m_commentText_height = 0;
			if(m_weibo.GetOriginalPic().length() != 0){
				m_weiboPic = m_weibo.GetOriginalPic(); 
			}
		}
		
		if(!recvMain.sm_simpleMode){
			m_extendHeight 			= m_functionButton_y + _manager.m_forwardBut.getImageHeight() + fsm_headImageTextInterval;
		}else{
			m_extendHeight 			= m_functionButton_y + fsm_headImageTextInterval ;
		}
		
		// get the abstract text height
		//
		if(recvMain.sm_showAllInList && !(this instanceof WeiboDMItemField)){
			
			m_absTextAreaAdded = true;
			
			initAbsTextArea();			
			add(m_absTextArea);
		}
	}
	
	public void destroy(){
		
		try{
			AddDelControlField(false);			
		}catch(Exception e){}
		
		for(int i = 0;i < m_hasControlField.length;i++){
			m_hasControlField[i] = false;
		}		
		
		if(m_absTextArea != null){
			if(m_absTextAreaAdded){
				try{
					delete(m_absTextArea);
				}catch(Exception e){}
			}
			sm_absTextAreaAllocator.release(m_absTextArea);
			m_absTextArea = null;	
		}		
		m_absTextAreaAdded = false;
		
		m_simpleAbstract = null;
		m_weiboText		= null;
		m_commentText	= null;
		m_displayName	= null;
		
		m_weiboPic		= null;		
	}

	public WeiboItemFocusField getFocusField(){
		return m_focusField;
	}	
	
	public static String getSimpleAbstract(fetchWeibo _weibo){
		if(_weibo != null){
			return _weibo.GetText().length() > fsm_maxWeiboAbstractLength ? 
					(_weibo.GetText().substring(0,fsm_maxWeiboAbstractLength) + "...") :  _weibo.GetText();
		}
		
		return "";
	}
	
	public int getAbsTextPosX(){
		if(recvMain.sm_displayHeadImage || m_absTextAreaAdded){
			return 2 + fsm_headImageTextInterval + WeiboHeadImage.fsm_headImageWidth + 10;
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
			try{
				m_absTextArea = (ContentTextField)sm_absTextAreaAllocator.alloc();
			}catch(Exception e){
				m_absTextArea = new ContentTextField();
			}
		}
		
		m_absTextArea.setTextWidth(recvMain.fsm_display_width - getAbsTextPosX() - WeiboMainManager.fsm_scrollbarSize - 1);		
		m_absTextArea.setText(m_weiboText);
		m_absTextHeight = m_absTextArea.getHeight() ; 
	}
	
	public void AddDelControlField(boolean _add){
		if(_add){
			
			if(m_absTextAreaAdded){
				m_absTextAreaAdded = false;
				delete(m_absTextArea);
			}
			
			if(recvMain.sm_commentFirst){
				
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
			
			if(!recvMain.sm_simpleMode){
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
				
				if(!m_hasControlField[fsm_controlField_picBtn] && m_weiboPic != null){
					m_hasControlField[fsm_controlField_picBtn] = true;
					add(m_parentManager.m_picBut);
				}
			}
			
			
		}else{
			
			if(recvMain.sm_showAllInList && !m_absTextAreaAdded){
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
			m_parentManager.m_editTextAreaHeight = 0;
			
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
				return m_extendHeight + m_parentManager.m_editTextAreaHeight;
			}
			
			return m_extendHeight;
			
		}else{
			
			if(m_absTextArea != null && recvMain.sm_showAllInList){
				return Math.max(m_absTextHeight + 5 + sm_timeFont.getHeight() + fsm_headImageTextInterval * 2,
									fsm_weiboSignImageSize + WeiboHeadImage.fsm_headImageWidth + 10);
			}else{
				return sm_fontHeight * 2 + 1;
			}
			
		}
	}
	
	public void sublayout(int width, int height){
		
		if(m_parentManager.getCurrExtendedItem() == this){
			
			int t_commentText_y = recvMain.sm_commentFirst?0:m_commentText_y;
					
			if(m_commentText != null){
				// comment area
				//
				setPositionChild(m_parentManager.m_commentTextArea,fsm_headImageTextInterval,t_commentText_y);
				layoutChild(m_parentManager.m_commentTextArea,fsm_commentTextWidth,m_functionButton_y - m_commentText_y);			
			}
			
			if(!recvMain.sm_simpleMode){
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
			
			int t_text_y = recvMain.sm_commentFirst?m_commentText_height:1;
			t_text_y += fsm_weiboSignImageSize;
			
			// text area
			//
			setPositionChild(m_parentManager.m_textArea,WeiboHeadImage.fsm_headImageWidth + fsm_headImageTextInterval,t_text_y );
			layoutChild(m_parentManager.m_textArea,fsm_textWidth,m_textHeight);
						
			if(m_parentManager.getCurrEditItem() == this){
				
				setPositionChild(m_parentManager.m_editTextArea,0,m_extendHeight);				
				layoutChild(m_parentManager.m_editTextArea,fsm_weiboItemFieldWidth,m_parentManager.m_editTextAreaHeight);
				
				height = m_extendHeight + m_parentManager.m_editTextAreaHeight;
				
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
	static public long sm_currTime = sm_timeDate.getTime();
	
	static String sm_timeUnitStr = recvMain.sm_local.getString(yblocalResource.WEIBO_TIME_UNIT);
	static String sm_timeAgoStr = recvMain.sm_local.getString(yblocalResource.WEIBO_TIME_AGO);
	
	static protected synchronized String getTimeString(fetchWeibo _weibo){
		
		long t_diff = sm_currTime - _weibo.GetDateTime();
		if(t_diff < 3600000){
			t_diff = t_diff / 60000; 
			if(t_diff < 1){
				t_diff = 1;
			}
			
			return Long.toString(t_diff) + sm_timeUnitStr + sm_timeAgoStr;
			
		}else{
			sm_timeDate.setTime(_weibo.GetDateTime());
			sm_calendar.setTime(sm_timeDate);		
			
			int t_minutes = sm_calendar.get(Calendar.MINUTE);
			String t_day = recvMain.sm_simpleMode?"":((sm_calendar.get(Calendar.MONTH) + 1) + "-" +sm_calendar.get(Calendar.DAY_OF_MONTH));
			if(t_minutes > 9){
				return t_day + " " + sm_calendar.get(Calendar.HOUR_OF_DAY) + ":" 
						+ t_minutes;
			}else{
				return t_day + " " + sm_calendar.get(Calendar.HOUR_OF_DAY) + ":" 
						+ "0" + t_minutes;
			}
		}
			
	}

	
	public void subpaint(Graphics _g){
		
		if(m_parentManager.getCurrExtendedItem() == this){
			
			int color		= _g.getColor();
			try{
				
				if(recvMain.sm_standardUI){
					sm_selectedBackgroud.draw(_g, 0, 0, fsm_weiboItemFieldWidth, m_extendHeight, BubbleImage.NO_POINT_STYLE);
				}else{
					_g.setColor(fsm_extendBGColor);
					_g.fillRect(0,0,fsm_weiboItemFieldWidth,m_extendHeight);
				}								
							
				int t_textStart_y = recvMain.sm_commentFirst?m_commentText_height : 2;
				
				// draw weibo style 
				//
				recvMain.sm_weiboUIImage.drawImage(
						_g, weiboTimeLineScreen.GetWeiboSign(m_weibo.GetWeiboStyle()), 0, t_textStart_y);
				
				// draw time string , weibo pic/comment
				//
				drawWeiboTime(_g,t_textStart_y,false);				
				
				// draw head image
				//
				WeiboHeadImage.displayHeadImage(_g,0, t_textStart_y + fsm_weiboSignImageSize + fsm_headImageTextInterval,m_headImage);
							
				int t_startSign_x = fsm_weiboSignImageSize;
				
				// name VIP sign
				//
				if(m_weibo.IsSinaVIP()){
					recvMain.sm_weiboUIImage.drawImage(
							_g,weiboTimeLineScreen.GetVIPSignBitmap(m_weibo.GetWeiboStyle()),t_startSign_x,t_textStart_y);
					
					t_startSign_x += fsm_weiboVIPImageSize;
	
				}
				
				// name BBer sign
				//
				if(m_weibo.IsBBer()){
					recvMain.sm_weiboUIImage.drawImage(
							_g,SliderHeader.GetBBerSignBitmap(),t_startSign_x,t_textStart_y);
				}
				
				// draw text
				//
				paintChild(_g,m_parentManager.m_textArea);	
				
				if(!recvMain.sm_simpleMode){
					paintChild(_g,m_parentManager.m_atBut);
					paintChild(_g,m_parentManager.m_forwardBut);
					paintChild(_g,m_parentManager.m_favoriteBut);
					
					if(m_weiboPic != null){
						paintChild(_g,m_parentManager.m_picBut);
					}
				}
				
				if(m_commentText != null){
					
					int t_commentText_y = recvMain.sm_commentFirst?0:m_commentText_y;
					
					// comment area
					//
					// draw a bubble
					sm_bubbleImage.draw(_g, 0, t_commentText_y - fsm_headImageTextInterval,
							fsm_commentTextWidth + fsm_headImageTextInterval * 2, m_commentText_height + 5,
							recvMain.sm_commentFirst?BubbleImage.BOTTOM_POINT_STYLE:BubbleImage.TOP_POINT_STYLE);
					
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
				recvMain.sm_weiboUIImage.drawImage(
						_g,weiboTimeLineScreen.GetWeiboSign(m_weibo.GetWeiboStyle()),t_leadingSpace, fsm_headImageTextInterval);
				
				// name VIP sign
				//
				if(m_weibo.IsSinaVIP()){
					recvMain.sm_weiboUIImage.drawImage(_g,weiboTimeLineScreen.GetVIPSignBitmap(m_weibo.GetWeiboStyle()),
														fsm_weiboSignImageSize ,fsm_headImageTextInterval);
				}
				
				// time string
				//
				drawWeiboTime(_g,t_firstLineHeight,true);				
				t_firstLineHeight += sm_timeFont.getHeight(); 

				// draw the bubble
				//
				if(_on){
					recvMain.sm_weiboUIImage.drawBitmapLine(_g, sm_bubbleSelected, 
							0, getPreferredHeight() - sm_bubbleSelected.getHeight(), getPreferredWidth());
				}
				
				sm_bubbleImage.draw(_g,getAbsTextPosX() - 4,t_firstLineHeight - 1,
						m_absTextArea.getTextWidth() + 4,m_absTextHeight + 5 ,BubbleImage.LEFT_POINT_STYLE);
				
				
				paintChild(_g,m_absTextArea);
								
				// display head image
				//
				WeiboHeadImage.displayHeadImage(_g,t_leadingSpace,t_firstLineHeight,m_headImage);
				t_firstLineHeight += WeiboHeadImage.fsm_headImageWidth + fsm_headImageTextInterval;
				
			}else{
								
				//draw the background
				//
				fillWeiboFieldBG(_g,0,0,getPreferredWidth(),getPreferredHeight(),true);	
				
				if(_on){
					if(recvMain.sm_standardUI){
						sm_selectedBackgroud.draw(_g, 0, 0, getPreferredWidth(), getPreferredHeight(), BubbleImage.NO_POINT_STYLE);
					}else{
						WeiboHeadImage.drawSelectedImage(_g,getPreferredWidth(),getPreferredHeight());
					}
				}
				
				// weibo sign 
				//
				recvMain.sm_weiboUIImage.drawImage(
						_g,weiboTimeLineScreen.GetWeiboSign(m_weibo.GetWeiboStyle()),t_leadingSpace, t_firstLineHeight);
				
				int t_nameLeadingSpace = t_leadingSpace;
				
				if(recvMain.sm_displayHeadImage){
					// display head image when closed
					//
					WeiboHeadImage.displayHeadImage(_g,t_leadingSpace + fsm_weiboSignImageSize + fsm_headImageTextInterval,
												t_firstLineHeight,m_headImage);
					t_nameLeadingSpace += WeiboHeadImage.fsm_headImageWidth + fsm_headImageTextInterval;
				}
								
				// display name
				//
				if(m_displayName == null){
					// cut the weibo user name
					//
					m_displayName = m_weibo.GetUserName();
					
					int t_maxDisplyNameWidth = recvMain.fsm_display_width - t_nameLeadingSpace - sm_timeFont.getAdvance("00-00 00:00");
					
					if(m_weiboPic != null){
						t_maxDisplyNameWidth -= weiboTimeLineScreen.getWeiboPicSignImage().getWidth();
					}
			    	
			    	if(m_commentText != null){
			    		t_maxDisplyNameWidth -= weiboTimeLineScreen.getWeiboCommentSignImage().getWidth();
			    	}			    							
					
			    	String t_final = m_displayName;
					
					while(sm_boldFont.getAdvance(t_final) > t_maxDisplyNameWidth){
						m_displayName = m_displayName.substring(0,m_displayName.length() - 1);
						t_final = m_displayName + "...";
					}
					
					m_displayName = t_final;
				}
				
				_g.setFont(sm_boldFont);
				_g.setColor(fsm_weiboNameTextColor);
				int t_nameLength = _g.drawText(m_displayName,
											fsm_weiboSignImageSize + t_nameLeadingSpace,
											t_firstLineHeight,Graphics.ELLIPSIS);
				
				_g.setFont(oldFont);
				
				// add the weibo sign size
				t_nameLength += fsm_weiboSignImageSize + t_nameLeadingSpace;
				
				// name VIP sign
				//
				if(m_weibo.IsSinaVIP()){
					recvMain.sm_weiboUIImage.drawImage(
							_g,weiboTimeLineScreen.GetVIPSignBitmap(m_weibo.GetWeiboStyle()),
							t_nameLength + t_leadingSpace, t_firstLineHeight);
					
					t_nameLength += fsm_weiboVIPImageSize;

				}
				
				// name BBer sign
				//
				if(m_weibo.IsBBer()){
					recvMain.sm_weiboUIImage.drawImage(
							_g,SliderHeader.GetBBerSignBitmap(),t_nameLength + t_nameLeadingSpace, t_firstLineHeight);
				}
				
				// contain abstract
				//
				_g.setFont(sm_absFont); //列表微博字体//
				_g.setColor(fsm_absTextColor);			
				
				int t_abs_x = t_nameLeadingSpace + fsm_weiboSignImageSize;
				int t_abs_y = sm_fontHeight + fsm_headImageTextInterval;
				_g.drawText(m_simpleAbstract,t_abs_x,t_abs_y,Graphics.ELLIPSIS);
					        
				// draw time string , weibo pic/comment
				//
				drawWeiboTime(_g,t_firstLineHeight,true);
			}  	       

		}finally{
			_g.setColor(color);
			_g.setFont(oldFont);
		}
	}
	
	
	private void drawWeiboTime(Graphics _g,int _y,boolean _drawSign){
		
		int color = _g.getColor();
		
		try{
			_g.setColor(fsm_timeTextColor);
			
			
			// draw time string
			//
	        String t_dateString = getTimeString(m_weibo);
	    	_g.setFont(sm_timeFont);
	    	int t_time_x = _g.drawText(t_dateString,fsm_weiboItemFieldWidth - sm_timeFont.getAdvance(t_dateString)
					,_y,Graphics.ELLIPSIS);
	    	
	    	if(!_drawSign){
	    		return;
	    	}
	    	
	    	final int t_interval = 3;
	    	
	    	t_time_x = fsm_weiboItemFieldWidth - t_time_x - t_interval;
	    	
	    	// draw weibo picture or comment sign
	    	//
	    	if(m_weiboPic != null){
	    		t_time_x -= weiboTimeLineScreen.getWeiboPicSignImage().getWidth();
	    		
	    		recvMain.sm_weiboUIImage.drawImage(_g,weiboTimeLineScreen.getWeiboPicSignImage(),t_time_x, _y);
	    		
	    		t_time_x -= t_interval;
			}
	    	
	    	if(m_commentText != null){
	    		
	    		t_time_x -= weiboTimeLineScreen.getWeiboCommentSignImage().getWidth();
	    		
	    		recvMain.sm_weiboUIImage.drawImage(_g,weiboTimeLineScreen.getWeiboCommentSignImage(),t_time_x, _y);
	    	}
		}finally{
			_g.setColor(color);
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
	
	// weibo field backgroud
	//
	private static ImageUnit sm_weiboFieldBG = null;
	private static ImageUnit sm_weiboFieldBG_disable = null;
	private static ImageUnit sm_weiboFieldBG_spaceLine_1 = null;	
	
	public void fillWeiboFieldBG(Graphics _g,int _x,int _y,int _width,int _height,boolean _topLine){
		
		if(recvMain.sm_standardUI){
			if(sm_weiboFieldBG_spaceLine_1 == null){
				sm_weiboFieldBG_spaceLine_1 = recvMain.sm_weiboUIImage.getImageUnit("space_line_1");
			}
			
			int t_color = _g.getColor();
			try{
				int t_fillColor = 0xf4fcff;//Original:0xdaeaeb;微博列表背景//
				
				if(m_parentManager.getCurrExtendedItem() != null 
				&& m_parentManager.getCurrExtendedItem() != this){
					t_fillColor = 0xedefef;//Original:0xb3c8c9;打开微博后未激活时的列表背景//
				}
				
				_g.setColor(t_fillColor);
				_g.fillRect(_x, _x, _width, _height);
			}finally{
				_g.setColor(t_color);
			}
			
			if(_topLine){
				recvMain.sm_weiboUIImage.drawBitmapLine(_g, sm_weiboFieldBG_spaceLine_1, _x, _y, _width);
			}
			
		}else{
			
			if(sm_weiboFieldBG == null){
				sm_weiboFieldBG = recvMain.sm_weiboUIImage.getImageUnit("weibo_bg");
			}
			
			if(sm_weiboFieldBG_disable == null){
				sm_weiboFieldBG_disable = recvMain.sm_weiboUIImage.getImageUnit("weibo_bg_disable");
			}
			
			if(m_parentManager.getCurrExtendedItem() != null 
				&& m_parentManager.getCurrExtendedItem() != this){
				recvMain.sm_weiboUIImage.drawBitmapLine(_g, sm_weiboFieldBG_disable, _x, _y, _width);
			}else{
				recvMain.sm_weiboUIImage.drawBitmapLine(_g, sm_weiboFieldBG, _x, _y, _width);
			}
			
			if(recvMain.sm_showAllInList){
				int t_color = _g.getColor();
				try{
					
					int t_fillColor = 0x2b3d4d;
					
					if(m_parentManager.getCurrExtendedItem() != null 
					&& m_parentManager.getCurrExtendedItem() != this){
						t_fillColor = 0x141c23;
					}
					
					_g.setColor(t_fillColor);
					_g.fillRect(0,sm_weiboFieldBG.getHeight(),getPreferredWidth(),getPreferredHeight() - sm_weiboFieldBG.getHeight());
				}finally{
					_g.setColor(t_color);
				}
			}
				
		}
	}

}
