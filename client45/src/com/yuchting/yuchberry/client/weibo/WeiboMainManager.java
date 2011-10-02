package com.yuchting.yuchberry.client.weibo;

import java.util.Date;
import java.util.Vector;

import local.localResource;
import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.component.AutoTextEditField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.CheckboxField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.NullField;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.yuchting.yuchberry.client.ObjectAllocator;
import com.yuchting.yuchberry.client.recvMain;
import com.yuchting.yuchberry.client.ui.ImageButton;
import com.yuchting.yuchberry.client.ui.WeiboHeadImage;
import com.yuchting.yuchberry.client.ui.WeiboTextField;


public class WeiboMainManager extends VerticalFieldManager implements FieldChangeListener{	
	public static int		sm_forwardBut_x				= 3;
	public static int		sm_atBut_x					= 0;
	public static int		sm_favoriteBut_x			= 0;
	public static int		sm_picBut_x					= 0;
	
	public ImageButton	 m_forwardBut			= null;
	public ImageButton	 m_atBut				= null;
	public ImageButton	 m_favoriteBut			= null;
	public ImageButton	 m_picBut				= null; 
		

	public WeiboTextField 	m_textArea				= new WeiboTextField(WeiboItemField.fsm_extendTextColor,WeiboItemField.fsm_extendBGColor);
	public WeiboTextField	m_commentTextArea		= new WeiboTextField(WeiboItemField.fsm_weiboCommentFGColor,WeiboItemField.fsm_weiboCommentBGColor);
	
	public int					m_currentSendType		= 0;
	
	public AutoTextEditField 	m_editTextArea			= new AutoTextEditField(){
		public void setText(String _text){
			super.setText(_text);
			this.layout(WeiboItemField.fsm_weiboItemFieldWidth,1000);
		}
	};
	
	public final static int		fsm_scrollbarSize	= 3;	
	public final static int		fsm_maxItemInOneScreen = recvMain.fsm_display_height / WeiboItemField.fsm_closeHeight;
	
	static ObjectAllocator		sm_weiboItemFieldAllocator = new ObjectAllocator("com.yuchting.yuchberry.client.weibo.WeiboItemField");
	
	WeiboItemField		m_selectedItem = null;
	WeiboItemField		m_extendedItem = null;
	WeiboItemField		m_editItem	= null;
	
	public int			m_editTextAreaHeight = 0;
	
	recvMain			m_mainApp;
	boolean			m_timelineManager;
	
	weiboTimeLineScreen	m_parentScreen = null;
	
	boolean			m_hasNewWeibo	= false;
	
	WeiboItemFocusField	m_focusFieldBeforeReplace;
	
	int					m_bufferedTotalHeight = 0;
	
	byte				m_forwardStyleBackup = 0;
	long				m_forwardIdBackup = 0;
	String				m_forwardText	= "";
	
	byte				m_replyStyleBackup = 0;
	long				m_replyIdBackup	= 0;
	String				m_replyText		= "";
	
	
	Vector 				m_bufferedWeiboList = null;
	int					m_bufferedAddInvokeId = -1;
	WeiboUpdateField	m_updateWeiboField = null;
	NullField			m_updateWeiboFieldNull = null;
		
	public WeiboMainManager(recvMain _mainApp,weiboTimeLineScreen _parentScreen,boolean _timelineManager){
		super(Manager.VERTICAL_SCROLL | Manager.VERTICAL_SCROLLBAR);
		
		
		m_forwardBut	= new ImageButton(recvMain.sm_local.getString(localResource.WEIBO_FORWARD_WEIBO_BUTTON_LABEL),
				recvMain.sm_weiboUIImage.getImageUnit("forward_comment_button"),
				recvMain.sm_weiboUIImage.getImageUnit("forward_comment_button_focus"),
				recvMain.sm_weiboUIImage);
	
		m_atBut	= new ImageButton(recvMain.sm_local.getString(localResource.WEIBO_AT_WEIBO_BUTTON_LABEL),
				recvMain.sm_weiboUIImage.getImageUnit("at_reply_button"),
				recvMain.sm_weiboUIImage.getImageUnit("at_reply_button_focus"),
				recvMain.sm_weiboUIImage);
		
		if(recvMain.GetClientLanguage() == 0){

			m_favoriteBut = new ImageButton(recvMain.sm_local.getString(localResource.WEIBO_FAVORITE_WEIBO_BUTTON_LABEL),
					recvMain.sm_weiboUIImage.getImageUnit("favorite_button"),
					recvMain.sm_weiboUIImage.getImageUnit("favorite_button_focus"),
					recvMain.sm_weiboUIImage);
			
			m_picBut = new ImageButton(recvMain.sm_local.getString(localResource.WEIBO_CHECK_PICTURE_LABEL),
					recvMain.sm_weiboUIImage.getImageUnit("picture_button"),
					recvMain.sm_weiboUIImage.getImageUnit("picture_button_focus"),
					recvMain.sm_weiboUIImage);
		}else{
			
			m_favoriteBut = new ImageButton(recvMain.sm_local.getString(localResource.WEIBO_FAVORITE_WEIBO_BUTTON_LABEL),
					recvMain.sm_weiboUIImage.getImageUnit("favorite_button_en"),
					recvMain.sm_weiboUIImage.getImageUnit("favorite_button_focus_en"),
					recvMain.sm_weiboUIImage);
			
			m_picBut = new ImageButton(recvMain.sm_local.getString(localResource.WEIBO_CHECK_PICTURE_LABEL),
					recvMain.sm_weiboUIImage.getImageUnit("picture_button_en"),
					recvMain.sm_weiboUIImage.getImageUnit("picture_button_focus_en"),
					recvMain.sm_weiboUIImage);
		}
		
		if(sm_atBut_x == 0){
			sm_forwardBut_x		= 3;
			sm_atBut_x			= sm_forwardBut_x + m_forwardBut.getImageWidth() + WeiboItemField.fsm_headImageTextInterval;
			sm_favoriteBut_x	= sm_atBut_x + m_atBut.getImageWidth() + WeiboItemField.fsm_headImageTextInterval;
			sm_picBut_x			= sm_favoriteBut_x + m_favoriteBut.getImageWidth() + WeiboItemField.fsm_headImageTextInterval;
		}
				
		m_editTextArea.setMaxSize(WeiboItemField.fsm_maxWeiboTextLength * 2);
		
		m_mainApp  			= _mainApp;
		m_parentScreen		= _parentScreen;
		m_timelineManager 	= _timelineManager;
		
		if(_timelineManager){
			m_bufferedWeiboList	= new Vector();
			m_updateWeiboField = new WeiboUpdateField(this);
			m_updateWeiboFieldNull = new NullField(Field.NON_FOCUSABLE);
			add(m_updateWeiboFieldNull);
		}
		
		m_atBut.setChangeListener(this);
		m_forwardBut.setChangeListener(this);
		m_favoriteBut.setChangeListener(this);
		m_picBut.setChangeListener(this);
		
		m_editTextArea.setChangeListener(this);
	
	}

	public void setCurrSelectedItem(WeiboItemField _field){
		m_selectedItem = _field;
	}
	public WeiboItemField getCurrSelectedItem(){
		return m_selectedItem;
	}
	
	public void setCurrExtendedItem(WeiboItemField _field){
		m_extendedItem = _field;
	}
	public WeiboItemField getCurrExtendedItem(){
		return m_extendedItem;
	}
	
	public void setCurrEditItem(WeiboItemField _field){
		m_editItem = _field; 
	}
	
	public WeiboItemField getCurrEditItem(){
		return m_editItem;
	}
	
	public boolean hasNewWeibo(){
		return m_hasNewWeibo;
	}
	
	public void backupFocusField(){
		
		m_focusFieldBeforeReplace = null;
		
		if(getCurrExtendedItem() == null && getCurrSelectedItem() != null){
			m_focusFieldBeforeReplace = getCurrSelectedItem().getFocusField();
		}else{
			if(getCurrExtendedItem() != null){
				m_focusFieldBeforeReplace = getCurrExtendedItem().getFocusField();
			}
		}
	}
	
	public void restoreFocusField(){
		if(m_focusFieldBeforeReplace != null){
			if(getCurrExtendedItem() == null && getCurrSelectedItem() != null){
				
				if(m_focusFieldBeforeReplace.getManager() != null){
					m_focusFieldBeforeReplace.setFocus();
				}
				
			}else{
				if(getCurrExtendedItem() != null){
					replace(getCurrExtendedItem(),m_focusFieldBeforeReplace);
					m_focusFieldBeforeReplace.setFocus();
					replace(m_focusFieldBeforeReplace,getCurrExtendedItem());
				}
			}
			
			m_focusFieldBeforeReplace = null;
		}
	}
	
	public void fieldChanged(Field field, int context) {
		if(m_atBut == field){
			AtWeibo(getCurrExtendedItem());
		}else if(m_forwardBut == field){
			ForwardWeibo(getCurrExtendedItem());
		}else if(m_editTextArea == field){
			
			if(m_editTextAreaHeight != m_editTextArea.getHeight()){
				RefreshEditTextAreHeight();
				
				invalidate();
				sublayout(0, 0);
			}
			
			m_parentScreen.setInputPromptText(Integer.toString(m_editTextArea.getText().length()) + 
					"/" + m_editTextArea.getMaxSize());
			
		}else if(m_favoriteBut == field){
			FavoriteWeibo(getCurrExtendedItem());			
		}else if(m_picBut == field){
			OpenOriginalPic(getCurrExtendedItem());
		}
	}
		
	public int getPreferredWidth(){
		return recvMain.fsm_display_width;
	}
	
	public int getPreferredHeight(){
		
		m_bufferedTotalHeight = 0;
		
		final int t_num = getFieldCount();
		for(int i =  0;i < t_num;i++){			
			m_bufferedTotalHeight += getField(i).getPreferredHeight();
		}
		
		m_bufferedTotalHeight += WeiboItemField.sm_fontHeight;
		
		return m_bufferedTotalHeight;
	}
	
	protected void sublayout(int width, int height){
		
		WeiboItemField.sm_currTime = System.currentTimeMillis();
		
		m_bufferedTotalHeight = 0;
				
		final int t_num = getFieldCount();
		for(int i =  0;i < t_num;i++){
			
			Field t_item = getField(i);
			
			final int t_height = t_item.getPreferredHeight();

			setPositionChild(t_item,0,m_bufferedTotalHeight);		
			layoutChild(t_item,t_item.getPreferredWidth(),t_height);		
			
			m_bufferedTotalHeight += t_height; 
		}
		
		m_bufferedTotalHeight += WeiboItemField.sm_fontHeight;
		
		setExtent(recvMain.fsm_display_width,m_bufferedTotalHeight);
	}
	
	protected void subpaint(Graphics graphics){
		super.subpaint(graphics);
		
		int oldColour = graphics.getColor();
		try{
			
			graphics.setColor(WeiboItemField.fsm_darkColor);
			graphics.drawText(recvMain.sm_local.getString(localResource.WEIBO_REACH_MAX_WEIBO_NUM_PROMPT),
								0,m_bufferedTotalHeight - WeiboItemField.sm_fontHeight);
			
			if(getFieldCount() >= fsm_maxItemInOneScreen){
				// draw the scroll bar
				// must call the invalidateScroll in WeiboItemFocusField.drawFocus 
				//
				int t_visibleHeight = getVisibleHeight();
				
				int t_start_y = getManager().getVerticalScroll();
				int t_start_x = recvMain.fsm_display_width - fsm_scrollbarSize;
						
				graphics.setColor(0xe0e0e0);
				graphics.fillRect(t_start_x, t_start_y, fsm_scrollbarSize, t_visibleHeight);
				
				int t_scroll_height = t_visibleHeight * t_visibleHeight / m_bufferedTotalHeight;
				int t_scroll_y = t_start_y + t_start_y * t_visibleHeight / m_bufferedTotalHeight;
				
				if(t_scroll_height < 4){
					t_scroll_height = 4;
				}
				
				graphics.setColor(0x606060);
				graphics.fillRect(t_start_x,t_scroll_y,fsm_scrollbarSize,t_scroll_height);
								
				m_parentScreen.enableHeader(t_start_y == 0);

			}
			
		}finally{
			graphics.setColor(oldColour);
		}
	}
	
	public int getVisibleHeight(){
		return recvMain.fsm_display_height - 
				(m_parentScreen.isHeaderShow()?WeiboHeader.fsm_headHeight:0);
	}
	
	public void invalidateScroll(){
		int t_visibleHeight = getVisibleHeight();		
		int t_start_y = getManager().getVerticalScroll();
		int t_start_x = recvMain.fsm_display_width - fsm_scrollbarSize;
		
		invalidate(t_start_x,t_start_y,fsm_scrollbarSize, t_visibleHeight);
	}
	
	final class BufferedWeibo{
		fetchWeibo		m_weibo;
		WeiboHeadImage	m_image;
		
		public BufferedWeibo(fetchWeibo _weibo,WeiboHeadImage _image){
			m_weibo = _weibo;
			m_image = _image;
		}
	}
	
	public void AddWeibo(final fetchWeibo _weibo,final WeiboHeadImage _image,
							final boolean _initAdd){	

		if(m_timelineManager // is timeline 
		&& !_initAdd ){// this weibo is history one  
			
			// add it into buffer list 
			//
			synchronized (m_bufferedWeiboList) {
				m_bufferedWeiboList.addElement(new BufferedWeibo(_weibo,_image));
			}
			
			if(m_bufferedAddInvokeId == -1){
				
				if(m_mainApp.isBackground() && m_mainApp.m_autoLoadNewTimelineWeibo){
					
					clickUpdateField();
					
				}else{

					if(getField(0) == m_updateWeiboFieldNull){
						replace(m_updateWeiboFieldNull,m_updateWeiboField);
						
						if(!_weibo.IsOwnWeibo()){
							m_mainApp.TriggerWeiboHomeNotification();
						}
					}
					
					m_updateWeiboField.invalidate();
				}
			}
								
		}else{		
			AddWeibo_impl(_weibo, _image);
		}

		if(!_initAdd && !_weibo.IsOwnWeibo()){				
			m_hasNewWeibo = true;
		}
	}
	
	protected void AddWeibo_impl(fetchWeibo _weibo,WeiboHeadImage _image){
		WeiboItemField t_field = null;
		
		try{
			t_field = (WeiboItemField)sm_weiboItemFieldAllocator.alloc();
		}catch(Exception e){
			t_field = new WeiboItemField();
			m_mainApp.SetErrorString("AW_I:"+e.getMessage()+e.getClass().getName());
		}
		
		t_field.init(_weibo,_image,this);
		
		if(m_timelineManager){
			insert(t_field.getFocusField(),1);
		}else{
			insert(t_field.getFocusField(),0);
		}
	}
	
	public boolean DelWeibo(final fetchWeibo _weibo){
		
		int t_num = getFieldCount();
		for(int i = t_num - 1;i >= 0;i--){
			
			Field t_field = getField(i);
						
			if(t_field instanceof WeiboItemFocusField){
				
				final WeiboItemFocusField t_focusField = (WeiboItemFocusField)t_field;
				
				if(t_focusField.m_itemField.m_weibo == _weibo){
					
					m_mainApp.invokeLater(new Runnable() {
						
						public void run() {
							delete(t_focusField);
							sm_weiboItemFieldAllocator.release(t_focusField.m_itemField);
						}
					});
					
					return true;
				}
				
			}else if(t_field instanceof WeiboItemField){
				
				final WeiboItemField t_weiboField = (WeiboItemField)t_field;
				
				if(t_weiboField.m_weibo == _weibo){
					
					m_mainApp.invokeLater(new Runnable() {
						
						public void run() {
							
							WeiboMainManager.this.EscapeKey(); //escape edit field 
							WeiboMainManager.this.EscapeKey(); //escape control field					
							
							delete(t_weiboField.getFocusField());
							
							sm_weiboItemFieldAllocator.release(t_weiboField);
						}
					});
					
					return true;
				}
			}			
		}
		
		return false;		
	}
		
	public boolean IncreaseRenderSize(int _dx,int _dy,int _status,int _time){
		
		if(getCurrExtendedItem() != null || getCurrEditItem() != null){
			return false;
		}	
		
		super.navigationMovement(_dx, _dy, _status, _time);
				
		invalidate();
		
		return true;
	}
	
	public void clickUpdateField(){
		
		if(m_timelineManager){
			
			if(getField(0) == m_updateWeiboField){
				
				if(m_parentScreen.m_mainApp.hasEventThread()){
					replace(m_updateWeiboField,m_updateWeiboFieldNull);
				}else{
					m_parentScreen.m_mainApp.invokeLater(new Runnable(){
						public void run(){
							replace(m_updateWeiboField,m_updateWeiboFieldNull);
						}
					});
				}
			}		
			
			synchronized (m_bufferedWeiboList) {
				if(!m_bufferedWeiboList.isEmpty() && m_bufferedAddInvokeId == -1){
					m_bufferedAddInvokeId = m_parentScreen.m_mainApp.invokeLater(new Runnable(){
						public void run(){
							
							synchronized (m_bufferedWeiboList) {
								BufferedWeibo t_weibo = (BufferedWeibo)m_bufferedWeiboList.elementAt(0);
								AddWeibo_impl(t_weibo.m_weibo, t_weibo.m_image);
								
								m_bufferedWeiboList.removeElementAt(0);
								
								if(m_bufferedWeiboList.isEmpty()){
									m_parentScreen.m_mainApp.cancelInvokeLater(m_bufferedAddInvokeId);
									m_bufferedAddInvokeId = -1;
								}
							}
							
						}
					}, recvMain.fsm_delayLoadingTime, true);
				}
			}
		}
		
	}
	public boolean Clicked(int status, int time){
		
		if(m_timelineManager 
		&& getField(0) == m_updateWeiboField 
		&& m_updateWeiboField.isFocus()){
			
			clickUpdateField();
			
			return true;
			
		}else{

			final WeiboItemField t_formerExtendItem 	= getCurrExtendedItem(); 
			final WeiboItemField t_currentExtendItem	= getCurrSelectedItem();

			if(t_formerExtendItem == null && t_currentExtendItem != null){	
			
				setCurrExtendedItem(null);
				
				if(t_formerExtendItem != null){
					t_formerExtendItem.AddDelControlField(false);
				}
				
				setCurrExtendedItem(t_currentExtendItem);
				t_currentExtendItem.AddDelControlField(true);			
				
				replace(t_currentExtendItem.getFocusField(),t_currentExtendItem);
				
				sublayout(0, 0);
				invalidate();			
						
				if(m_hasNewWeibo){
					// hide the new Weibo sign 
					//
					m_hasNewWeibo = false;
					m_parentScreen.m_weiboHeader.invalidate();
				}
				
				m_mainApp.StopWeiboNotification();
	    		m_mainApp.StopWeiboHomeNotification();
							
				return true;
			}
	
		}
		
		return false;
	}
	
	public boolean EscapeKey(){
		if(getCurrExtendedItem() != null){
			
			final WeiboItemField t_extendItem = getCurrExtendedItem();
						
			if(getCurrEditItem() != null 
			&& m_parentScreen.m_currMgr != m_parentScreen.m_mainDMMgr){
				
				// the add/delete field operation will cause the sublayout being called
				//
				getCurrEditItem().AddDelEditTextArea(false,null);
				
				try{
					if(m_parentScreen.m_currMgr == this && getCurrExtendedItem() != null){
						// make the text area is visible
						//
						// some times it will appear follow exception
						// setFocus called on a field that is not attached to a screen.
						//
						m_textArea.setFocus();						
					}
				}catch(Exception e){}
				
			}else{
				
				setCurrExtendedItem(null);
				t_extendItem.AddDelControlField(false);
				
				replace(t_extendItem,t_extendItem.getFocusField());
				try{
					if(m_parentScreen.m_currMgr == this){
						// some times it will appear follow exception
						// setFocus called on a field that is not attached to a screen.
						//
						t_extendItem.getFocusField().setFocus();						
					}
				}catch(Exception e){}
			}		
			
			m_parentScreen.setInputPromptText("");
			
			sublayout(0, 0);
			invalidate();
			
			return true;
		}
		
		return false;
	}
	
	public int getFieldWithFocusIndex(){
		
		if(getCurrExtendedItem() != null){
			
			int t_num = getFieldCount();
			
			for(int i = 0;i < t_num;i++){
				Field t_field = getField(i);
				if(t_field == getCurrExtendedItem()){
					return i;
				}
			}
		}
		
		return super.getFieldWithFocusIndex();
	}
	
	public void OpenNextWeiboItem(boolean _next){

		int t_current = getFieldWithFocusIndex();
		int t_next = t_current;
		
		if(_next){
			t_next++;
			if(t_next >= getFieldCount()){
				return;
			}
		}else{			
			t_next--;
			if(m_timelineManager){
				if(t_next <= 0){
					return;
				}
			}else{
				if(t_next < 0){
					return;
				}
			}	
		}
		
		EscapeKey();
		
		WeiboItemFocusField t_focus = (WeiboItemFocusField)getField(t_next);
		t_focus.setFocus();
		setCurrSelectedItem(t_focus.m_itemField);
		
		Clicked(0, 0);		
	}
	
	public void ScrollToTop(){
		if(getCurrExtendedItem() == null){
			
			if(m_timelineManager){
				if(getField(0) == m_updateWeiboField){
					getField(0).setFocus();
				}else{
					if(getFieldCount() >= 2){
						getField(1).setFocus();
					}
				}
			}else{
				if(getFieldCount() > 0){
					getField(0).setFocus();
				}
			}
			
			
		}else{
			EscapeKey();
			
			WeiboItemFocusField t_field = null;
			
			if(m_timelineManager){
				if(getFieldCount() >= 2){
					t_field = (WeiboItemFocusField)getField(1);
				}
			}else{
				if(getFieldCount() > 0){
					t_field = (WeiboItemFocusField)getField(0);
				}
			}
			
			if(t_field != null){
				t_field.setFocus();
				setCurrSelectedItem(t_field.m_itemField);
			}
		
			Clicked(0, 0);
		}		
		
		invalidate();
	}
	
	public void ScrollToBottom(){
		
		if(getCurrExtendedItem() == null){
					
			if(getFieldCount() != 0){
				getField(getFieldCount() - 1).setFocus();
			}
			
		}else{
			
			if(getFieldCount() != 0){
				EscapeKey();
				
				WeiboItemFocusField t_field = (WeiboItemFocusField)getField(getFieldCount() - 1);
			
				t_field.setFocus();
				setCurrSelectedItem(t_field.m_itemField);
				
				Clicked(0, 0);
			}
		}		
		
		invalidate();
	}
	
	public void BackupSendWeiboText(int _sendType,fetchWeibo _refWeibo,String _sentText){
		
		if(_sendType == fetchWeibo.SEND_FORWARD_TYPE){
			
			m_forwardIdBackup 		= _refWeibo.GetId();
			m_forwardStyleBackup 	= _refWeibo.GetWeiboStyle();
			m_forwardText			= _sentText;
			
		}else if(_sendType == fetchWeibo.SEND_REPLY_TYPE){
			
			m_replyIdBackup 	= _refWeibo.GetId();
			m_replyStyleBackup 	= _refWeibo.GetWeiboStyle();
			m_replyText			= _sentText;
			
		}
		
	}
	
	public void AtWeibo(WeiboItemField _item){
		
		if(getCurrEditItem() != _item){	
			
			if(getCurrExtendedItem() == null){
				Clicked(0, 0);
			}
			
			String t_finalText = null;
			if(m_replyIdBackup != _item.m_weibo.GetId() 
			|| m_replyStyleBackup != _item.m_weibo.GetWeiboStyle()){
								
				StringBuffer t_text = new StringBuffer();
				t_text.append("@").append(_item.m_weibo.GetUserScreenName()).append(" ");

				t_finalText = t_text.toString();			
			}else{
				t_finalText = m_replyText;
			}
			
			_item.AddDelEditTextArea(true,t_finalText);
			
			m_editTextArea.setText(t_finalText);
			m_editTextArea.setFocus();
			
			RefreshEditTextAreHeight();
			
			m_currentSendType =  fetchWeibo.SEND_REPLY_TYPE;
			
			sublayout(0,0);
			invalidate();
			
			m_editTextArea.setCursorPosition(t_finalText.length());
		}
	}
	
	public int RefreshEditTextAreHeight(){
		m_editTextAreaHeight = m_editTextArea.getHeight() /*+ WeiboItemField.fsm_headImageTextInterval*/;
		return m_editTextAreaHeight;
	}
	
	public void ForwardWeibo(WeiboItemField _item){
		
		if(getCurrEditItem() != _item){
			
			if(getCurrExtendedItem() == null){
				Clicked(0, 0);
			}
			
			String t_text =null;
			if(m_forwardIdBackup != _item.m_weibo.GetId()
			|| m_forwardStyleBackup != _item.m_weibo.GetWeiboStyle()){
								
				String t_forwardSign = _item.m_weibo.getForwardPrefix();
				
				StringBuffer t_forwardText = new StringBuffer();
				t_forwardText.append(t_forwardSign).append("@").append(_item.m_weibo.GetUserScreenName()).
								append(" :").append(_item.m_weibo.GetText());
				
				if(weiboTimeLineScreen.sm_mainApp.m_publicForward && _item.m_commentText != null){
					t_forwardText.append(t_forwardSign).append(_item.m_commentText.replace('\n', ' '));
				}
				
				t_text = t_forwardText.toString();
				if(t_text.length() > m_editTextArea.getMaxSize()){
					t_text = t_text.substring(0,m_editTextArea.getMaxSize());
				}
				
			}else{
				
				t_text = m_forwardText;
			}			
			
			_item.AddDelEditTextArea(true,t_text);
			
			m_editTextArea.setText(t_text);
			m_editTextArea.setFocus();
			
			RefreshEditTextAreHeight();
			
			m_currentSendType = fetchWeibo.SEND_FORWARD_TYPE;
			
			sublayout(0,0);
			invalidate();
			
			m_editTextArea.setCursorPosition(0);
		}
	}
	
	public void FavoriteWeibo(WeiboItemField _item){
		if(_item != null){
			m_mainApp.m_connectDeamon.SendCreateFavoriteWeibo(_item.m_weibo);
		}
	}
	
	public void OpenOriginalPic(final WeiboItemField _item){
		if(_item != null && _item.m_weiboPic != null){
			
			if(m_mainApp.m_hasPromptToCheckImg){
				
				VerticalFieldManager t_manager = new VerticalFieldManager(Manager.VERTICAL_SCROLL){
					public boolean keyChar(char c,int status,int time){
						if(c == Characters.ESCAPE){
							getScreen().close();
						}
						
						return super.keyChar(c,status,time);
					}
				};
				
				final PopupScreen t_popDlg = new PopupScreen(t_manager,Manager.VERTICAL_SCROLL);
				
				final CheckboxField t_samePrompt = new CheckboxField(recvMain.sm_local.getString(localResource.WEIBO_SAVE_CHECK_IMAGE_PROMPT),
						false,Field.FIELD_HCENTER);
				
				final ButtonField[] t_buttons = new ButtonField[]
				{
					new ButtonField(recvMain.sm_local.getString(localResource.WEIBO_OPEN_IMAGE_URL_STYLE_0),Field.FIELD_HCENTER),
					new ButtonField(recvMain.sm_local.getString(localResource.WEIBO_OPEN_IMAGE_URL_STYLE_1),Field.FIELD_HCENTER),
					new ButtonField(recvMain.sm_local.getString(localResource.WEIBO_OPEN_IMAGE_URL_STYLE_2),Field.FIELD_HCENTER),
				};

				FieldChangeListener t_listener = new FieldChangeListener() {
					
					public void fieldChanged(Field field, int context) {
						if(context != FieldChangeListener.PROGRAMMATIC){
							
							if(t_samePrompt == field){
								
								if(t_samePrompt.getChecked()){
									m_mainApp.m_hasPromptToCheckImg = false;
								}
								
							}else{
								for(int i = 0;i < t_buttons.length;i++){
									if(t_buttons[i] == field){
										m_mainApp.m_checkImgIndex = i;
										recvMain.openURL(_item.getImageURL(i));
										
										t_popDlg.close();
										
										break;
									}
								}
							}
						}
					}
				};
				
				t_manager.add(new LabelField(recvMain.sm_local.getString(localResource.WEIBO_OPEN_IMAGE_URL_QUESTION),Field.FIELD_HCENTER));
				t_manager.add(t_samePrompt);
				
				t_samePrompt.setChangeListener(t_listener);
								
				for(int i = 0;i < t_buttons.length;i++){
					t_manager.add(t_buttons[i]);
					t_buttons[i].setChangeListener(t_listener);
					
					if(i == m_mainApp.m_checkImgIndex){
						t_buttons[i].setFocus();
					}
				}
				
				m_mainApp.pushScreen(t_popDlg);

			}else{
				
				recvMain.openURL(_item.getImageURL(m_mainApp.m_checkImgIndex));
			}
			
		}
	}  
	
}
