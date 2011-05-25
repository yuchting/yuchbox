package com.yuchting.yuchberry.client.weibo;

import java.io.ByteArrayOutputStream;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.yuchting.yuchberry.client.fetchWeibo;
import com.yuchting.yuchberry.client.msg_head;
import com.yuchting.yuchberry.client.recvMain;
import com.yuchting.yuchberry.client.sendReceive;

public class WeiboMainManager extends VerticalFieldManager implements FieldChangeListener{
	
	recvMain			m_mainApp;
	int					m_selectWeiboItemIndex = 0;
	WeiboUpdateField	m_updateWeiboField = null;
	
	int					m_formerVerticalPos = 0;
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
			
			add(m_updateWeiboField);
			WeiboItemField.sm_selectWeiboItem = m_updateWeiboField;
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
				sendReceive.WriteLong(t_os,_field.m_weibo.GetCommentWeibo().GetUserId());
				
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
			WeiboItemField t_item = (WeiboItemField)getField(i);			
			t_totalHeight += t_item.getPreferredHeight();
		}
		
		return t_totalHeight;
	}
	
	private int getLayoutStartIdx(){
		int t_start = m_selectWeiboItemIndex - WeiboItemField.fsm_maxDisplayRows;
		if(t_start < 0){
			t_start = 0;
		}
		
		return t_start;
	}
	
	private int getLayoutEndNum(){
		int t_end = m_selectWeiboItemIndex + WeiboItemField.fsm_maxDisplayRows;
		if(t_end > getFieldCount()){
			t_end = getFieldCount();
		}
		return t_end;
	}
	
	protected void sublayout(int width, int height){
		int t_totalHeight = 0;
		
		int t_start_idx = getLayoutStartIdx();
		int t_endNum = getLayoutEndNum();
		
		final int t_num = getFieldCount();
		for(int i =  0;i < t_num;i++){
			
			WeiboItemField t_item = (WeiboItemField)getField(i);
			
			final int t_height = t_item.getPreferredHeight();
			
			if(i >= t_start_idx && i < t_endNum){
				setPositionChild(t_item,0,t_totalHeight);		
				layoutChild(t_item,recvMain.fsm_display_width,t_height);
			}			
			
			t_totalHeight += t_height;
		}
		
		setExtent(recvMain.fsm_display_width,t_totalHeight);
	}
	
	
	protected void subpaint(Graphics graphics){
				
		final int t_num = getLayoutEndNum();
		for(int i =  getLayoutStartIdx();i < t_num;i++){
			WeiboItemField t_item = (WeiboItemField)getField(i);
			paintChild(graphics, t_item);
		}
	}
	
	public void AddWeibo(final WeiboItemField _item,final boolean _resetSelectIdx){
				
		m_mainApp.invokeLater(new Runnable() {
			
			public void run() {
				
				if(m_timelineManager){
					insert(_item,1);
				}else{
					insert(_item,0);
				}
				
				if(_resetSelectIdx){
					
					m_selectWeiboItemIndex++;
					
					if(WeiboItemField.sm_extendWeiboItem == null ){
						
						m_formerVerticalPos += WeiboItemField.fsm_closeHeight;
						
						RestoreScroll();
						
						m_hasNewWeibo = true;	
					}
					
				}
			}
		});					
	}
	
	public boolean DelWeibo(final fetchWeibo _weibo){
		
		int t_num = getFieldCount();
		for(int i = 0 ;i < t_num;i++){
			WeiboItemField t_field = (WeiboItemField)getField(i);
			if(t_field.m_weibo == _weibo){
				
				m_mainApp.invokeLater(new Runnable() {
					
					public void run() {
						
						int t_num = getFieldCount();
						
						for(int i = 0 ;i < t_num;i++){
							WeiboItemField t_field = (WeiboItemField)getField(i);
							if(t_field.m_weibo == _weibo){
								delete(t_field);
								
								break;
							}
						}
						
						if(WeiboItemField.sm_extendWeiboItem == null){			
							RestoreScroll();					
						}
					}
				});
				
				return true;
			}
		}
		
		return false;		
	}
	
	public boolean IncreaseRenderSize(int _dx,int _dy){
		
		if(WeiboItemField.sm_extendWeiboItem != null || WeiboItemField.sm_editWeiboItem != null){
			return false;
		}
				
		if(_dy > 1){
			_dy = 1;
		}
		
		if(_dy < -1){
			_dy = -1;
		}
				
		final int t_num = getFieldCount();

		if(m_selectWeiboItemIndex + _dy >= 0 && m_selectWeiboItemIndex + _dy < t_num){
			
			m_selectWeiboItemIndex += _dy;
			
			final int t_verticalScroll = getVerticalScroll();
		
			int t_currentHeight = 0;
			int t_delta = 0;
			
			if(m_timelineManager){

				if(m_selectWeiboItemIndex > 1){
					t_currentHeight = m_selectWeiboItemIndex * WeiboItemField.fsm_closeHeight;
					
					if(t_verticalScroll == 0){
						t_delta = WeiboItemField.fsm_closeHeight;
					}else{
						t_delta = WeiboItemField.fsm_closeHeight;
					}
					
				}else{
					t_currentHeight = WeiboItemField.sm_fontHeight;
					t_delta = WeiboItemField.fsm_closeHeight;
				}
				
			}else{
				
				t_currentHeight = m_selectWeiboItemIndex * WeiboItemField.fsm_closeHeight;
				t_delta = WeiboItemField.fsm_closeHeight;
			}
			
			if(_dy > 0){
		
				if(t_currentHeight >= t_verticalScroll + getVisibleHeight()){
					setVerticalScroll(t_verticalScroll + t_delta);
				}
				
			}else{
				if(m_selectWeiboItemIndex == 0){
					setVerticalScroll(0);
				}else if(t_currentHeight < t_verticalScroll){
					setVerticalScroll(t_verticalScroll - t_delta);
				}
			}
			
			WeiboItemField.sm_selectWeiboItem = (WeiboItemField)getField(m_selectWeiboItemIndex);
			
			sublayout(0,0);
			invalidate();
			
			if(_dy != 0){
				m_formerVerticalPos = getVerticalScroll();
			}			
			
			return true;
		}
		
		return false;
	}
	
	public void RestoreScroll(){
		
		setVerticalScroll(m_formerVerticalPos);
		
		if(m_selectWeiboItemIndex < getFieldCount()){
			WeiboItemField.sm_selectWeiboItem = (WeiboItemField)getField(m_selectWeiboItemIndex);
		}else{
			WeiboItemField.sm_selectWeiboItem = null;
		}
		
		m_hasNewWeibo = false;		
		
		sublayout(0,0);
		invalidate();
		
	}
	
	public boolean Clicked(int status, int time){
				
		final WeiboItemField t_formerExtendItem 	= WeiboItemField.sm_extendWeiboItem; 
		final WeiboItemField t_currentExtendItem	= WeiboItemField.sm_selectWeiboItem;

		if(t_formerExtendItem == null && t_currentExtendItem != null){
			
			m_formerVerticalPos = getVerticalScroll();
		
			WeiboItemField.sm_extendWeiboItem = null;
			
			if(t_formerExtendItem != null){
				t_formerExtendItem.AddDelControlField(false);
			}
			
			t_currentExtendItem.AddDelControlField(true);
			WeiboItemField.sm_extendWeiboItem = t_currentExtendItem;
			
			t_currentExtendItem.setFocus();	
			
			if(m_formerVerticalPos != 0 ){

				// scroll the extend item field to right position
				//
				int t_extendItemHeight = t_currentExtendItem.getPreferredHeight();
				int t_maxScrollHeight = getVisibleHeight() - WeiboHeader.fsm_headHeight;
				
				if(t_extendItemHeight > t_maxScrollHeight){
					setVerticalScroll(t_maxScrollHeight);
				}else{
					setVerticalScroll(t_extendItemHeight);
				}
							
			}else{
				 int t_spaceHeight = m_selectWeiboItemIndex * WeiboItemField.fsm_closeHeight;
				 if(m_timelineManager && t_spaceHeight != 0){
					 // decrease the height of update 
					 //
					 t_spaceHeight -= WeiboItemField.sm_fontHeight;
				 }
				 int t_maxScrollHeight = getVisibleHeight() - WeiboHeader.fsm_headHeight;
				 
				 int t_scrollHeight = t_spaceHeight + t_currentExtendItem.getPreferredHeight();
				 
				 if(t_scrollHeight > t_maxScrollHeight){
					 
					 if(t_scrollHeight - t_maxScrollHeight > t_spaceHeight){
						 setVerticalScroll(t_spaceHeight);
					 }else{
						 setVerticalScroll(t_scrollHeight - t_maxScrollHeight);
					 }
				 }				 
			}
			
			sublayout(0, 0);
			invalidate();
						
			return true;
		}
		
		return false;
	}
	
	public void OpenNextWeiboItem(boolean _pre){
		if(_pre){
			EscapeKey();
			IncreaseRenderSize(0, 1);
			Clicked(0, 0);
		}else{
			EscapeKey();
			IncreaseRenderSize(0, -1);
			Clicked(0, 0);
		}
	}
	
	public void ScrollToTop(){
		setVerticalScroll(0);
		m_selectWeiboItemIndex = 0;
		
		if(getFieldCount() != 0){
			WeiboItemField.sm_selectWeiboItem = (WeiboItemField)getField(m_selectWeiboItemIndex);
		}
		
		sublayout(0, 0);
		invalidate();
	}
	
	public boolean EscapeKey(){
		if(WeiboItemField.sm_extendWeiboItem != null){
			
			final WeiboItemField t_extendItem = WeiboItemField.sm_extendWeiboItem;
						
			if(WeiboItemField.sm_editWeiboItem != null 
			&& WeiboItemField.sm_editWeiboItem != m_updateWeiboField){
				
				// the add/delete field operation will cause the sublayout being called
				//
				WeiboItemField.sm_editWeiboItem.AddDelEditTextArea(false,null);
				
				WeiboItemField.sm_textArea.setFocus();
				WeiboItemField.sm_textArea.setCursorPosition(0);
				
			}else{
				WeiboItemField.sm_extendWeiboItem = null;
				t_extendItem.AddDelControlField(false);
				
				setVerticalScroll(m_formerVerticalPos);
			}
			
			invalidate();
			
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
			
			WeiboItemField.sm_currentSendType = 2;
			
			sublayout(0,0);
			invalidate();
			
			WeiboItemField.sm_editTextArea.setCursorPosition(t_text.length());
		}
	}
	
	public void ForwardWeibo(WeiboItemField _item){
		
		if(WeiboItemField.sm_editWeiboItem != _item){
			
			String t_text = " //@" + _item.m_weibo.GetUserName() + " :" + _item.m_weibo.GetText();
			if(t_text.length() > WeiboItemField.fsm_maxWeiboTextLength){
				t_text = t_text.substring(0,WeiboItemField.fsm_maxWeiboTextLength);
			}
			
			_item.AddDelEditTextArea(true,t_text);
			
			WeiboItemField.sm_editTextArea.setText(t_text);			
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
