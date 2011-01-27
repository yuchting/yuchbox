package com.yuchting.yuchberry.client;

import java.util.Vector;

import local.localResource;
import net.rim.device.api.io.IOUtilities;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.component.TextField;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;


class WeiboItemField extends Manager{
	
	final static int		fsm_maxWeiboTextLength		= 140;
	final static int		fsm_headImageWidth 			= fetchWeibo.fsm_headImageSize;
	final static int		fsm_headImageTextInterval	= 4;
	
	final static int		fsm_weiboSignImageWidth		= 16;
	
	final static int		fsm_textWidth				= recvMain.fsm_display_width - fsm_headImageWidth - fsm_headImageTextInterval;
	final static int		fsm_editTextWidth			= recvMain.fsm_display_width;	

	static HyperlinkButtonField	sm_atBut				= new HyperlinkButtonField(recvMain.sm_local.getString(localResource.AT_WEIBO_BUTTON_LABLE));
	static HyperlinkButtonField	sm_forwardBut			= new HyperlinkButtonField(recvMain.sm_local.getString(localResource.FORWARD_WEIBO_BUTTTON_LABLE));
	static HyperlinkButtonField	sm_favoriteBut			= new HyperlinkButtonField(recvMain.sm_local.getString(localResource.FAVORITE_WEIBO_BUTTON_LABLE));
	static TextField 			sm_textArea				= new TextField(Field.READONLY);
	
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
			
	static {
		sm_editTextArea.setMaxSize(fsm_maxWeiboTextLength);
	}
	
	
	
	static Font		sm_defaultFont						= sm_atBut.getFont();
	static int		sm_fontHeight						= sm_defaultFont.getHeight() + 2;
	
	static int		sm_editTextAreaHeight				= 0;
		
	static int		sm_forwardBut_x						= sm_defaultFont.getAdvance(sm_atBut.getText()) + fsm_headImageTextInterval;
	static int		sm_forvoriteBut_x					= sm_forwardBut_x + sm_defaultFont.getAdvance(sm_forwardBut.getText()) + fsm_headImageTextInterval;
	
	static int		sm_imageAreaMinHeight				= fsm_weiboSignImageWidth + fsm_headImageWidth + fsm_headImageTextInterval;

	static WeiboItemField			sm_extendWeiboItem	= null;
	static WeiboItemField			sm_selectWeiboItem	= null;
	static WeiboItemField			sm_editWeiboItem	= null;
	
	
	
	boolean[]				m_hasControlField = {false,false,false,false,false};
	
	fetchWeibo				m_weibo			= null;
	String					m_displayText	= null;
		
	int						m_extendHeight 	= sm_fontHeight;
	int						m_height		= sm_fontHeight;
	int						m_functionButton_y	= sm_fontHeight;
	
	int						m_textHeight	= sm_fontHeight;
	
	Bitmap					m_weiboSignImage = null;
	Bitmap					m_headImage 	= null;	
	
	public WeiboItemField(){
		super(NO_SCROLL_RESET);
	}
	
	public WeiboItemField(fetchWeibo _weibo,Bitmap _headImage,Bitmap _weiboSignImage){
		super(NO_SCROLL_RESET);
				
		m_weibo 			= _weibo;
						
		m_weiboSignImage	= _weiboSignImage;
		m_headImage 		= _headImage;
		
		m_displayText		= "@" + m_weibo.GetUserName() + ":" + m_weibo.GetText();
		
		sm_testTextArea.setText(m_displayText);
		m_textHeight			= sm_testTextArea.getHeight();
		
		m_functionButton_y		= Math.max(m_textHeight,sm_imageAreaMinHeight) + fsm_headImageTextInterval;
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
				add(sm_atBut);
			}
			if(!m_hasControlField[2]){
				m_hasControlField[2] = true;
				add(sm_forwardBut);
			}
			if(!m_hasControlField[3]){
				m_hasControlField[3] = true;
				add(sm_favoriteBut);
			}			
			
		}else{
			
			if(m_hasControlField[0]){
				m_hasControlField[0] = false;
				delete(sm_textArea);
			}
			
			if(m_hasControlField[1]){
				m_hasControlField[1] = false;
				delete(sm_atBut);
			}
			if(m_hasControlField[2]){
				m_hasControlField[2] = false;
				delete(sm_forwardBut);
			}
			if(m_hasControlField[3]){
				m_hasControlField[3] = false;
				delete(sm_favoriteBut);
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
			return m_height;
		}
	}
	
	public void sublayout(int width, int height){
		
		if(sm_extendWeiboItem == this){
						
			setPositionChild(sm_atBut,0,m_functionButton_y);
			layoutChild(sm_atBut,sm_atBut.getPreferredWidth(),sm_atBut.getPreferredHeight());
			
			setPositionChild(sm_forwardBut,sm_forwardBut_x,m_functionButton_y);
			layoutChild(sm_forwardBut,sm_forwardBut.getPreferredWidth(),sm_forwardBut.getPreferredHeight());
			
			setPositionChild(sm_favoriteBut,sm_forvoriteBut_x,m_functionButton_y);
			layoutChild(sm_favoriteBut,sm_favoriteBut.getPreferredWidth(),sm_favoriteBut.getPreferredHeight());
			
			setPositionChild(sm_textArea,fsm_headImageWidth + fsm_headImageTextInterval,0);
			layoutChild(sm_textArea,fsm_textWidth,m_textHeight);
			
			if(sm_editWeiboItem == this){
				
				setPositionChild(sm_editTextArea,0,m_functionButton_y + fsm_headImageTextInterval + sm_fontHeight);
				layoutChild(sm_editTextArea,recvMain.fsm_display_width,sm_editTextAreaHeight);
				
				height = m_extendHeight + sm_editTextAreaHeight;
				
			}else{
				height = m_extendHeight;
			}
						
		}else{		
			height = sm_fontHeight;
		}
		
		setExtent(recvMain.fsm_display_width,height);
	}
		
	protected void subpaint(Graphics _g){
		
		if(sm_extendWeiboItem == this){
			
			_g.drawBitmap(0, 0, fsm_weiboSignImageWidth, fsm_weiboSignImageWidth, m_weiboSignImage, 0, 0);
			_g.drawBitmap(0, fsm_weiboSignImageWidth + fsm_headImageTextInterval, fsm_headImageWidth, fsm_headImageWidth, m_headImage, 0, 0);
			
			paintChild(_g,sm_atBut);
			paintChild(_g,sm_forwardBut);
			paintChild(_g,sm_favoriteBut);
			
			paintChild(_g,sm_textArea);
			
			if(sm_editWeiboItem == this){
				paintChild(_g,sm_editTextArea);
			}
			
		}else{
			
			_g.drawBitmap(0, 0, fsm_weiboSignImageWidth, fsm_weiboSignImageWidth, m_weiboSignImage, 0, 0);
			_g.drawText(m_displayText.substring(0, Math.min(m_displayText.length(),40)), fsm_weiboSignImageWidth,0,Graphics.ELLIPSIS);
			
			if(sm_selectWeiboItem == this){
				_g.drawRoundRect(0,0,recvMain.fsm_display_width,sm_fontHeight,1,1);
			}
		}		
	}
	
}

class WeiboUpdateField extends WeiboItemField{
	
	String m_sendUpdateText = "";
	
	public void AddDelControlField(boolean _add){
		AddDelEditTextArea(_add,m_sendUpdateText);
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
	
	protected void subpaint(Graphics g){
		
		if(sm_extendWeiboItem == this){
			
			paintChild(g,sm_editTextArea);
			
		}else{
			int oldColour = g.getColor();
	        try{
	            g.setColor(0x8f8f8f);            
	            g.drawText(recvMain.sm_local.getString(localResource.UPDATE_WEIBO_LABEL), fsm_weiboSignImageWidth,0,Graphics.ELLIPSIS);
	        }finally{
	            g.setColor( oldColour );
	        }		
			
			if(sm_selectWeiboItem == this){
				g.drawRoundRect(0,0,recvMain.fsm_display_width,sm_fontHeight,1,1);
			}
		}		
	}
}

class MainManager extends VerticalFieldManager implements FieldChangeListener{
	
	recvMain			m_mainApp;
	int					m_selectWeiboItemIndex = 0;
	WeiboUpdateField	m_updateWeiboField = new WeiboUpdateField();
	
	int					m_formerVerticalPos = 0;
	
	public MainManager(recvMain _mainApp){
		super(Manager.VERTICAL_SCROLL);
		m_mainApp  = _mainApp;
		
		WeiboItemField.sm_atBut.setChangeListener(this);
		WeiboItemField.sm_forwardBut.setChangeListener(this);
		WeiboItemField.sm_favoriteBut.setChangeListener(this);
		WeiboItemField.sm_editTextArea.setChangeListener(this);		
		
		add(m_updateWeiboField);
		WeiboItemField.sm_selectWeiboItem = m_updateWeiboField;
	}
	
	public void fieldChanged(Field field, int context) {
		if(WeiboItemField.sm_atBut == field){
			AtWeibo(WeiboItemField.sm_extendWeiboItem);
		}else if(WeiboItemField.sm_forwardBut == field){
			ForwardWeibo(WeiboItemField.sm_extendWeiboItem);
		}else if(WeiboItemField.sm_editTextArea == field){
			
			WeiboItemField.RefreshEditTextAreHeight();
			invalidate();
			sublayout(0, 0);
			
			if(WeiboItemField.sm_editWeiboItem == m_updateWeiboField){
				m_updateWeiboField.m_sendUpdateText = WeiboItemField.sm_editTextArea.getText();
			}
		}		
	}
	
	public int getPreferredWidth(){
		return recvMain.fsm_display_width;
	}
	
	public int getPreferredHeight(){
		int t_totalHeight = m_updateWeiboField.getPreferredHeight();
		
		final int t_num = getFieldCount();
		for(int i =  1;i < t_num;i++){
			WeiboItemField t_item = (WeiboItemField)getField(i);			
			t_totalHeight += t_item.getPreferredHeight();
		}
		
		return t_totalHeight;
	}
	
	protected void sublayout(int width, int height){
		int t_totalHeight = m_updateWeiboField.getPreferredHeight();
		
		setPositionChild(m_updateWeiboField,0,0);
		layoutChild(m_updateWeiboField,recvMain.fsm_display_width,t_totalHeight);
		
		final int t_num = getFieldCount();
		for(int i =  1;i < t_num;i++){
			
			WeiboItemField t_item = (WeiboItemField)getField(i);
			
			final int t_height = t_item.getPreferredHeight();
			
			setPositionChild(t_item, 0,t_totalHeight);			
			layoutChild(t_item,recvMain.fsm_display_width,t_height);
			
			t_totalHeight += t_height;
		}
		
		setExtent(recvMain.fsm_display_width,t_totalHeight);
	}
	
	protected void subpaint(Graphics graphics){
		
		paintChild(graphics,m_updateWeiboField);
		
		final int t_num = getFieldCount();
		for(int i =  1;i < t_num;i++){
			WeiboItemField t_item = (WeiboItemField)getField(i);
			paintChild(graphics, t_item);
		}
	}
	
	public void AddWeibo(WeiboItemField _item){
		insert(_item,1);
		
		WeiboItemField.sm_selectWeiboItem = _item;
		m_selectWeiboItemIndex = 1;
		
		invalidate();
	}
	
	public boolean IncreaseRenderSize(int _dx,int _dy){
		
		if(WeiboItemField.sm_extendWeiboItem != null || WeiboItemField.sm_editWeiboItem != null){
			return false;
		}
				
		final int t_num = getFieldCount();

		if(m_selectWeiboItemIndex + _dy >= 0 && m_selectWeiboItemIndex + _dy < t_num){
			
			m_selectWeiboItemIndex += _dy;
			
			final int t_verticalScroll = getVerticalScroll();
		
			if(_dy > 0){
		
				if(m_selectWeiboItemIndex * WeiboItemField.sm_fontHeight >= t_verticalScroll + getVisibleHeight()){
					setVerticalScroll(t_verticalScroll + WeiboItemField.sm_fontHeight);
				}
				
			}else{
				if(m_selectWeiboItemIndex * WeiboItemField.sm_fontHeight < t_verticalScroll){
					setVerticalScroll(t_verticalScroll - WeiboItemField.sm_fontHeight);
				}
			}
			
			WeiboItemField.sm_selectWeiboItem = (WeiboItemField)getField(m_selectWeiboItemIndex);
			invalidate();
			
			return true;
		}
		
		return false;
	}
	
	public boolean Clicked(int status, int time){
		
		final WeiboItemField t_formerExtendItem 	= WeiboItemField.sm_extendWeiboItem; 
		final WeiboItemField t_currentExtendItem	= WeiboItemField.sm_selectWeiboItem;

		if(t_formerExtendItem != t_currentExtendItem){
			
			int t_formerPos = m_formerVerticalPos = getVerticalScroll();
		
			WeiboItemField.sm_extendWeiboItem = null;
			
			if(t_formerExtendItem != null){
				t_formerExtendItem.AddDelControlField(false);
			}
			
			t_currentExtendItem.AddDelControlField(true);
			WeiboItemField.sm_extendWeiboItem = t_currentExtendItem;
												
			final int t_extendItemHeight = m_selectWeiboItemIndex * WeiboItemField.sm_fontHeight + 
												WeiboItemField.sm_extendWeiboItem.getPreferredHeight();
			
			if(t_extendItemHeight > m_formerVerticalPos + recvMain.fsm_display_height){
				final int t_delta = (t_extendItemHeight - (m_formerVerticalPos + recvMain.fsm_display_height));
				t_formerPos += t_delta;
			}
			
			setVerticalScroll(t_formerPos);	
			
			sublayout(0, 0);
			invalidate();
						
			return true;
		}
		
		return false;
	}
	
	public boolean EscapeKey(){
		if(WeiboItemField.sm_extendWeiboItem != null){
			
			final WeiboItemField t_extendItem = WeiboItemField.sm_extendWeiboItem;
			
			invalidate();
			
			if(WeiboItemField.sm_editWeiboItem != null 
			&& WeiboItemField.sm_editWeiboItem != m_updateWeiboField){
				
				// the add/delete field operation will cause the sublayout being called
				//
				WeiboItemField.sm_editWeiboItem.AddDelEditTextArea(false,null);	
								
			}else{
				WeiboItemField.sm_extendWeiboItem = null;
				t_extendItem.AddDelControlField(false);
				
				setVerticalScroll(m_formerVerticalPos);
			}
			
			return true;
		}
		
		return false;
	}
	
	public void AtWeibo(WeiboItemField _item){
		
		if(WeiboItemField.sm_editWeiboItem != _item){	
			
			final String t_text = "@" + _item.m_weibo.GetUserName() + " ";
			_item.AddDelEditTextArea(true,t_text);
			
			WeiboItemField.sm_editTextArea.setText(t_text);			
			WeiboItemField.RefreshEditTextAreHeight();
			
			sublayout(0,0);
			invalidate();			
			
			WeiboItemField.sm_editTextArea.setCursorPosition(t_text.length());
		}
	}
	
	public void ForwardWeibo(WeiboItemField _item){
		
		if(WeiboItemField.sm_editWeiboItem != _item){
			
			final String t_text = (_item.m_weibo.GetText().length() >= WeiboItemField.fsm_maxWeiboTextLength + 3)?
									_item.m_weibo.GetText():(" //" + _item.m_weibo.GetText());
			_item.AddDelEditTextArea(true,t_text);
			
			WeiboItemField.sm_editTextArea.setText(t_text);			
			WeiboItemField.RefreshEditTextAreHeight();
			
			sublayout(0,0);
			invalidate();
			
			WeiboItemField.sm_editTextArea.setCursorPosition(0);
		}
	}
}

public class weiboTimeLineScreen extends MainScreen{
	
	recvMain			m_mainApp;
	
	MainManager			m_mainMgr;
	
	Vector				m_timelineWeibo 	= new Vector();
	Vector				m_directMessage 	= new Vector();
	Vector				m_atMeMessage 		= new Vector();
	Vector				m_commentMeMessage 	= new Vector();
	
	public weiboTimeLineScreen(recvMain _mainApp){
		m_mainApp = _mainApp;
		
		m_mainMgr = new MainManager(_mainApp);
		add(m_mainMgr);
				
		//@{ test code
		try{
			byte[] bytes = IOUtilities.streamToBytes(m_mainApp.getClass().getResourceAsStream("/Unknown_resize.jpg"));		
			Bitmap t_headImage =  EncodedImage.createEncodedImage(bytes, 0, bytes.length).getBitmap();
			
			for(int i = 0 ;i < 20;i++){
				fetchWeibo t_weibo = new fetchWeibo(false);
				t_weibo.SetId(0);
				t_weibo.SetUserName("Yuchting" + i);
				t_weibo.SetText("Just a Test Just a Test Just a Test Just a Test Just a Test Just a Test Just a Test Just a Test Just a Test ");
				
				m_mainMgr.AddWeibo(new WeiboItemField(t_weibo,t_headImage,t_headImage));
			}
			
		}catch(Exception e){}
		//@}
	}
	
	public void SendMenuItemClick(){
		
	}
	
	protected void makeMenu(Menu _menu,int instance){
		
		if(WeiboItemField.sm_editWeiboItem != null && WeiboItemField.sm_extendWeiboItem != null){
			_menu.add(new MenuItem(recvMain.sm_local.getString(localResource.WEIBO_SEND_LABEL),0,0){
	            public void run() {
	            	SendMenuItemClick();
	            }
	        });
		}
    }
	public boolean onClose(){
		if(!m_mainMgr.EscapeKey()){
			close();
			return true;
		}
		return false;
	}
	
	protected boolean navigationMovement(int dx,int dy,int status,int time){
		boolean t_processed = m_mainMgr.IncreaseRenderSize(dx,dy);
		if(!t_processed && WeiboItemField.sm_editTextArea.isFocus()){
			t_processed = super.navigationMovement(dx, dy, status, time);
		}
		return 	t_processed;
	}
	protected boolean navigationClick(int status, int time){
		return m_mainMgr.Clicked(status,time);
	}
	

}
