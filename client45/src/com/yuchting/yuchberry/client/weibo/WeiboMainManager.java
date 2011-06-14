package com.yuchting.yuchberry.client.weibo;

import java.io.ByteArrayOutputStream;

import local.localResource;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.component.AutoTextEditField;
import net.rim.device.api.ui.component.TextField;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.yuchting.yuchberry.client.HyperlinkButtonField;
import com.yuchting.yuchberry.client.msg_head;
import com.yuchting.yuchberry.client.recvMain;
import com.yuchting.yuchberry.client.sendReceive;

public class WeiboMainManager extends VerticalFieldManager implements FieldChangeListener{
	
	public HyperlinkButtonField	 m_atBut				= new HyperlinkButtonField(recvMain.sm_local.getString(localResource.WEIBO_AT_WEIBO_BUTTON_LABEL));
	public HyperlinkButtonField	 m_forwardBut			= new HyperlinkButtonField(recvMain.sm_local.getString(localResource.WEIBO_FORWARD_WEIBO_BUTTON_LABEL));
	public HyperlinkButtonField	 m_favoriteBut			= new HyperlinkButtonField(recvMain.sm_local.getString(localResource.WEIBO_FAVORITE_WEIBO_BUTTON_LABEL));
	public HyperlinkButtonField	 m_picBut				= new HyperlinkButtonField(recvMain.sm_local.getString(localResource.WEIBO_CHECK_PICTURE_LABEL));
	public HyperlinkButtonField	 m_followCommentUser	= new HyperlinkButtonField(recvMain.sm_local.getString(localResource.WEIBO_FOLLOW_USER_BUTTON_LABEL));

	
	// BasicEditField for 4.2os
	public TextField 			m_textArea				= new TextField(Field.READONLY);
	public TextField 			m_commentTextArea		= new TextField(Field.READONLY);
	
	public int					m_currentSendType		= 0;
	
	public AutoTextEditField 	m_editTextArea			= new AutoTextEditField(){
		public void setText(String _text){
			super.setText(_text);
			layout(recvMain.fsm_display_width,1000);
		}
	};
	
	
	WeiboItemField		m_selectedItem = null;
	WeiboItemField		m_extendedItem = null;
	WeiboItemField		m_editItem	= null;
	
	recvMain			m_mainApp;
	WeiboUpdateField	m_updateWeiboField = null;
	boolean			m_timelineManager;
	
	weiboTimeLineScreen	m_parentScreen = null;
	
	boolean			m_hasNewWeibo	= false;
	
	WeiboItemFocusField	m_focusFieldBeforeReplace;
		
	public WeiboMainManager(recvMain _mainApp,weiboTimeLineScreen _parentScreen,boolean _timelineManager){
		super(Manager.VERTICAL_SCROLL);
		m_editTextArea.setMaxSize(WeiboItemField.fsm_maxWeiboTextLength);
		
		m_mainApp  			= _mainApp;
		m_parentScreen		= _parentScreen;
		
		m_timelineManager 	= _timelineManager;
		
		if(_timelineManager){
			m_updateWeiboField = new WeiboUpdateField(this);
			add(m_updateWeiboField.getFocusField());
		}
		
		m_atBut.setChangeListener(this);
		m_forwardBut.setChangeListener(this);
		m_favoriteBut.setChangeListener(this);
		m_picBut.setChangeListener(this);
		
		m_editTextArea.setChangeListener(this);
		m_followCommentUser.setChangeListener(this);
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
				m_focusFieldBeforeReplace.setFocus();
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
			
			RefreshEditTextAreHeight();
			
			invalidate();
			sublayout(0, 0);
			
			if(m_timelineManager){
				if(getCurrEditItem() == m_updateWeiboField){
					m_updateWeiboField.m_sendUpdateText = m_editTextArea.getText();
				}	
			}
		}else if(m_favoriteBut == field){
			FavoriteWeibo(getCurrExtendedItem());			
		}else if(m_followCommentUser == field){
			FollowCommentUser(getCurrExtendedItem());
		}else if(m_picBut == field){
			OpenOriginalPic(getCurrExtendedItem());
		}
	}
	
	public void FollowCommentUser(WeiboItemField _field){
		if(_field != null && _field.m_weibo.GetCommentWeibo() != null){
			
			try{
				ByteArrayOutputStream t_os = new ByteArrayOutputStream();
				t_os.write(msg_head.msgWeiboFollowUser);
				t_os.write(_field.m_weibo.GetCommentWeibo().GetWeiboStyle());
				
				if(_field.m_weibo.GetCommentWeibo().GetWeiboStyle() == fetchWeibo.QQ_WEIBO_STYLE){
					sendReceive.WriteString(t_os,_field.m_weibo.GetCommentWeibo().GetUserScreenName());
				}else{
					sendReceive.WriteLong(t_os,_field.m_weibo.GetCommentWeibo().GetUserId());
				}				
				
				weiboTimeLineScreen.sm_mainApp.m_connectDeamon.addSendingData(
						msg_head.msgWeiboFollowUser, t_os.toByteArray(),true);
				
			}catch(Exception e){
				weiboTimeLineScreen.sm_mainApp.SetErrorString("FCU:" + e.getMessage() + e.getClass().getName());
			}
		}
	}
	
	public int getPreferredWidth(){
		return recvMain.fsm_display_width;
	}
	
	public int getPreferredHeight(){
		
		int t_totalHeight = 0;
		
		final int t_num = getFieldCount();
		for(int i =  0;i < t_num;i++){			
			t_totalHeight += getField(i).getPreferredHeight();
		}
		
		t_totalHeight += WeiboItemField.sm_fontHeight;
		
		return t_totalHeight;
	}
	
	protected void sublayout(int width, int height){
		int t_totalHeight = 0;
				
		final int t_num = getFieldCount();
		for(int i =  0;i < t_num;i++){
			
			Field t_item = getField(i);
			
			final int t_height = t_item.getPreferredHeight();

			setPositionChild(t_item,0,t_totalHeight);		
			layoutChild(t_item,recvMain.fsm_display_width,t_height);		
			
			t_totalHeight += t_height; 
		}
		
		t_totalHeight += WeiboItemField.sm_fontHeight;
		
		setExtent(recvMain.fsm_display_width,t_totalHeight);
	}
	
	
	protected void subpaint(Graphics graphics){
				
		final int t_num = getFieldCount();
		for(int i =  0;i < t_num;i++){
			paintChild(graphics, getField(i));
		}
		
		int oldColour = graphics.getColor();
		try{
			graphics.setColor(WeiboItemField.fsm_spaceLineColor);
			graphics.drawText(recvMain.sm_local.getString(localResource.WEIBO_REACH_MAX_WEIBO_NUM_PROMPT),
								0,getPreferredHeight() - WeiboItemField.sm_fontHeight);
		}finally{
			graphics.setColor(oldColour);
		}
		
	}
	
	public void AddWeibo(final fetchWeibo _weibo,final WeiboHeadImage _image,
							final boolean _initAdd){
		
		final WeiboItemField t_field = new WeiboItemField(_weibo,_image,this);
		
		m_mainApp.invokeLater(new Runnable() {
			
			public void run() {
				
				if(m_timelineManager){
					insert(t_field.getFocusField(),1);
				}else{
					insert(t_field.getFocusField(),0);
				}
				
				if(!_initAdd){				
					m_hasNewWeibo = true;
				}
			}
		});					
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
		
	public boolean Clicked(int status, int time){
				
		final WeiboItemField t_formerExtendItem 	= getCurrExtendedItem(); 
		final WeiboItemField t_currentExtendItem	= getCurrSelectedItem();

		if(t_formerExtendItem == null && t_currentExtendItem != null){	
		
			setCurrExtendedItem(null);
			
			if(t_formerExtendItem != null){
				t_formerExtendItem.AddDelControlField(false);
			}
			
			t_currentExtendItem.AddDelControlField(true);
			setCurrExtendedItem(t_currentExtendItem);
			
			replace(t_currentExtendItem.getFocusField(),t_currentExtendItem);
			
			sublayout(0, 0);
			invalidate();			
					
			if(m_hasNewWeibo){
				// hide the new Weibo sign 
				//
				m_hasNewWeibo = false;
				m_parentScreen.m_weiboHeader.invalidate();
			}	
						
			return true;
		}
		
		return false;
	}
	
	public boolean EscapeKey(){
		if(getCurrExtendedItem() != null){
			
			final WeiboItemField t_extendItem = getCurrExtendedItem();
						
			if(getCurrEditItem() != null 
			&& getCurrEditItem() != m_updateWeiboField){
				
				// the add/delete field operation will cause the sublayout being called
				//
				getCurrEditItem().AddDelEditTextArea(false,null);
				
			}else{
				setCurrExtendedItem(null);
				t_extendItem.AddDelControlField(false);
				
				replace(t_extendItem,t_extendItem.getFocusField());
				t_extendItem.getFocusField().setFocus();
			}
			
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
				
		if(getFieldCount() != 0){
			getField(0).setFocus();
		}
		
		invalidate();
	}
	
	public void ScrollToBottom(){
		
		if(getFieldCount() != 0){
			getField(getFieldCount() - 1).setFocus();
		}
		
		invalidate();
	}
	
	
	
	public void AtWeibo(WeiboItemField _item){
		
		if(getCurrEditItem() != _item){	
			
			final String t_text = "@" + _item.m_weibo.GetUserScreenName() + " ";
			_item.AddDelEditTextArea(true,t_text);
			
			m_editTextArea.setText(t_text);
			m_editTextArea.setFocus();
			
			RefreshEditTextAreHeight();
			
			m_currentSendType = 2;			
			
			sublayout(0,0);
			invalidate();
			
			m_editTextArea.setCursorPosition(t_text.length());
		}
	}
	
	public int RefreshEditTextAreHeight(){
		WeiboItemField.sm_editTextAreaHeight = m_editTextArea.getHeight() + WeiboItemField.fsm_headImageTextInterval;
		return WeiboItemField.sm_editTextAreaHeight;
	}
	
	public void ForwardWeibo(WeiboItemField _item){
		
		if(getCurrEditItem() != _item){
			
			StringBuffer t_forwardText = new StringBuffer();
			t_forwardText.append(" //@").append(_item.m_weibo.GetUserScreenName()).append(" :").append(_item.m_weibo.GetText());
			
			if(weiboTimeLineScreen.sm_mainApp.m_publicForward && _item.m_commentText != null){
				t_forwardText.append(" //").append(_item.m_commentText.replace('\n', ' '));
			}
			
			String t_text = t_forwardText.toString();
			if(t_text.length() > WeiboItemField.fsm_maxWeiboTextLength){
				t_text = t_text.substring(0,WeiboItemField.fsm_maxWeiboTextLength);
			}
			
			_item.AddDelEditTextArea(true,t_text);
			
			m_editTextArea.setText(t_text);
			m_editTextArea.setFocus();
			
			RefreshEditTextAreHeight();
			
			m_currentSendType = 1;
			
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
	
	public void OpenOriginalPic(WeiboItemField _item){
		if(_item != null 
		&& _item.m_weiboPic != null){
			recvMain.openURL(_item.m_weiboPic);
		}
	}
	
}
