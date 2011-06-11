package com.yuchting.yuchberry.client.weibo;

import java.io.ByteArrayOutputStream;

import local.localResource;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.yuchting.yuchberry.client.msg_head;
import com.yuchting.yuchberry.client.recvMain;
import com.yuchting.yuchberry.client.sendReceive;

public class WeiboMainManager extends VerticalFieldManager implements FieldChangeListener{
	
	recvMain			m_mainApp;
	WeiboUpdateField	m_updateWeiboField = null;
	boolean			m_timelineManager;
	
	weiboTimeLineScreen	m_parentScreen = null;
	
	boolean			m_hasNewWeibo	= false;
		
	public WeiboMainManager(recvMain _mainApp,weiboTimeLineScreen _parentScreen,boolean _timelineManager){
		super(Manager.VERTICAL_SCROLL);
		
		m_mainApp  			= _mainApp;
		m_parentScreen		= _parentScreen;
		
		m_timelineManager 	= _timelineManager;
		
		if(_timelineManager){	
			
			m_updateWeiboField = new WeiboUpdateField();
			
			WeiboItemField.sm_atBut.setChangeListener(this);
			WeiboItemField.sm_forwardBut.setChangeListener(this);
			WeiboItemField.sm_favoriteBut.setChangeListener(this);
			WeiboItemField.sm_picBut.setChangeListener(this);
			
			WeiboItemField.sm_editTextArea.setChangeListener(this);
			WeiboItemField.sm_followCommentUser.setChangeListener(this);
			
			add(m_updateWeiboField.getFocusField());
		}		
	}
	
	public boolean hasNewWeibo(){
		return m_hasNewWeibo;
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
			
			if(m_timelineManager){
				if(WeiboItemField.sm_editWeiboItem == m_updateWeiboField){
					m_updateWeiboField.m_sendUpdateText = WeiboItemField.sm_editTextArea.getText();
				}	
			}			
		}else if(WeiboItemField.sm_favoriteBut == field){
			FavoriteWeibo(WeiboItemField.sm_extendWeiboItem);			
		}else if(WeiboItemField.sm_followCommentUser == field){
			FollowCommentUser(WeiboItemField.sm_extendWeiboItem);
		}else if(WeiboItemField.sm_picBut == field){
			OpenOriginalPic(WeiboItemField.sm_extendWeiboItem);
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
							final boolean _resetSelectIdx){
		
		final WeiboItemField t_field = new WeiboItemField(_weibo,_image);
		
		m_mainApp.invokeLater(new Runnable() {
			
			public void run() {
				
				if(m_timelineManager){
					insert(t_field.getFocusField(),1);
				}else{
					insert(t_field.getFocusField(),0);
				}
				
				if(_resetSelectIdx){				
					m_hasNewWeibo = true;
				}
			}
		});					
	}
	
	public boolean DelWeibo(final fetchWeibo _weibo){
		
		int t_num = getFieldCount();
		for(int i = 0 ;i < t_num;i++){
			WeiboItemFocusField t_field = (WeiboItemFocusField)getField(i);
			if(t_field.m_itemField.m_weibo == _weibo){
				
				m_mainApp.invokeLater(new Runnable() {
					
					public void run() {
						
						int t_num = getFieldCount();
						
						for(int i = 0 ;i < t_num;i++){
							WeiboItemFocusField t_field = (WeiboItemFocusField)getField(i);
							if(t_field.m_itemField.m_weibo == _weibo){
								delete(t_field);
								
								break;
							}
						}
						
					}
				});
				
				return true;
			}
		}
		
		return false;		
	}
		
	public boolean IncreaseRenderSize(int _dx,int _dy,int _status,int _time){
		
		if(WeiboItemField.sm_extendWeiboItem != null || WeiboItemField.sm_editWeiboItem != null){
			return false;
		}	
		
		super.navigationMovement(_dx, _dy, _status, _time);
				
		invalidate();
		
		return true;
	}
		
	public boolean Clicked(int status, int time){
				
		final WeiboItemField t_formerExtendItem 	= WeiboItemField.sm_extendWeiboItem; 
		final WeiboItemField t_currentExtendItem	= WeiboItemField.sm_selectWeiboItem;

		if(t_formerExtendItem == null && t_currentExtendItem != null){	
		
			WeiboItemField.sm_extendWeiboItem = null;
			
			if(t_formerExtendItem != null){
				t_formerExtendItem.AddDelControlField(false);
			}
			
			t_currentExtendItem.AddDelControlField(true);
			WeiboItemField.sm_extendWeiboItem = t_currentExtendItem;
			
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
		if(WeiboItemField.sm_extendWeiboItem != null){
			
			final WeiboItemField t_extendItem = WeiboItemField.sm_extendWeiboItem;
						
			if(WeiboItemField.sm_editWeiboItem != null 
			&& WeiboItemField.sm_editWeiboItem != m_updateWeiboField){
				
				// the add/delete field operation will cause the sublayout being called
				//
				WeiboItemField.sm_editWeiboItem.AddDelEditTextArea(false,null);
				
			}else{
				WeiboItemField.sm_extendWeiboItem = null;
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
		WeiboItemField.sm_selectWeiboItem = t_focus.m_itemField;
		
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
		
		if(WeiboItemField.sm_editWeiboItem != _item){	
			
			final String t_text = "@" + _item.m_weibo.GetUserScreenName() + " ";
			_item.AddDelEditTextArea(true,t_text);
			
			WeiboItemField.sm_editTextArea.setText(t_text);
			WeiboItemField.sm_editTextArea.setFocus();
			
			WeiboItemField.RefreshEditTextAreHeight();
			
			WeiboItemField.sm_currentSendType = 2;			
			
			sublayout(0,0);
			invalidate();
			
			WeiboItemField.sm_editTextArea.setCursorPosition(t_text.length());
		}
	}
	
	public void ForwardWeibo(WeiboItemField _item){
		
		if(WeiboItemField.sm_editWeiboItem != _item){
			
			StringBuffer t_forwardText = new StringBuffer();
			t_forwardText.append(" //@").append(_item.m_weibo.GetUserScreenName()).append(" :").append(_item.m_weibo.GetText());
			
			if(weiboTimeLineScreen.sm_mainApp.m_publicForward && _item.m_commentText != null){
				t_forwardText.append(" //").append(_item.m_commentText)
							.append(" --").append(recvMain.sm_local.getString(localResource.WEIBO_SOURCE_PREFIX))
							.append(WeiboItemField.parseSource(_item.m_weibo.GetSource()));
			}
			
			String t_text = t_forwardText.toString();
			if(t_text.length() > WeiboItemField.fsm_maxWeiboTextLength){
				t_text = t_text.substring(0,WeiboItemField.fsm_maxWeiboTextLength);
			}
			
			_item.AddDelEditTextArea(true,t_text);
			
			WeiboItemField.sm_editTextArea.setText(t_text);
			WeiboItemField.sm_editTextArea.setFocus();
			
			WeiboItemField.RefreshEditTextAreHeight();
			
			WeiboItemField.sm_currentSendType = 1;
			
			sublayout(0,0);
			invalidate();
			
			WeiboItemField.sm_editTextArea.setCursorPosition(0);
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
