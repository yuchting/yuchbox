package com.yuchting.yuchberry.client;

import java.io.ByteArrayOutputStream;
import java.util.Vector;

import local.localResource;
import net.rim.device.api.io.IOUtilities;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

final class HeadImage{
	
	// the user id
	long 	m_userID;
	
	byte	m_weiboStyle;
	
	Bitmap	m_headImage;
	int		m_dataHash;
}

class MainManager extends VerticalFieldManager implements FieldChangeListener{
	
	recvMain			m_mainApp;
	int					m_selectWeiboItemIndex = 0;
	WeiboUpdateField	m_updateWeiboField = null;
	
	int					m_formerVerticalPos = 0;
	boolean			m_timelineManager;
	
	weiboTimeLineScreen	m_parentScreen = null;
	
	boolean			m_hasNewWeibo	= false;
	
	public MainManager(recvMain _mainApp,weiboTimeLineScreen _parentScreen,boolean _timelineManager){
		super(Manager.VERTICAL_SCROLL);
		
		m_mainApp  			= _mainApp;
		m_parentScreen		= _parentScreen;
		
		m_timelineManager 	= _timelineManager;
		
		if(_timelineManager){	
			
			m_updateWeiboField = new WeiboUpdateField();
			
			WeiboItemField.sm_atBut.setChangeListener(this);
			WeiboItemField.sm_forwardBut.setChangeListener(this);
			WeiboItemField.sm_favoriteBut.setChangeListener(this);
			WeiboItemField.sm_editTextArea.setChangeListener(this);		
			
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
						
			if(WeiboItemField.sm_extendWeiboItem != null){
				m_mainApp.m_connectDeamon.SendCreateFavoriteWeibo(WeiboItemField.sm_extendWeiboItem.m_weibo);
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
	
	protected void sublayout(int width, int height){
		int t_totalHeight = 0;
				
		final int t_num = getFieldCount();
		for(int i =  0;i < t_num;i++){
			
			WeiboItemField t_item = (WeiboItemField)getField(i);
			
			final int t_height = t_item.getPreferredHeight();
			
			setPositionChild(t_item, 0,t_totalHeight);			
			layoutChild(t_item,recvMain.fsm_display_width,t_height);
			
			t_totalHeight += t_height;
		}
		
		setExtent(recvMain.fsm_display_width,t_totalHeight);
	}
	
	protected void subpaint(Graphics graphics){
				
		final int t_num = getFieldCount();
		for(int i =  0;i < t_num;i++){
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
				
				if(WeiboItemField.sm_extendWeiboItem == null && _resetSelectIdx){
					
					m_selectWeiboItemIndex++;
					
					sublayout(0,0);
					
					RestoreScroll();
					
					m_hasNewWeibo = true;
				}
			}
		});					
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
					t_currentHeight = m_selectWeiboItemIndex * WeiboItemField.sm_closeHeight;
					
					if(t_verticalScroll == 0){
						t_delta = WeiboItemField.sm_closeHeight;
					}else{
						t_delta = WeiboItemField.sm_closeHeight;
					}
					
				}else{
					t_currentHeight = WeiboItemField.sm_fontHeight;
					t_delta = WeiboItemField.sm_closeHeight;
				}
				
			}else{
				
				t_currentHeight = WeiboItemField.sm_closeHeight;
				t_delta = WeiboItemField.sm_closeHeight;
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
			
			if(t_currentExtendItem != m_updateWeiboField){
				WeiboItemField.sm_textArea.setFocus();
				WeiboItemField.sm_textArea.setCursorPosition(0);
			}			
			
			if(m_formerVerticalPos != 0 ){
				setVerticalScroll(t_currentExtendItem.getPreferredHeight());
			}
			
			sublayout(0, 0);
			invalidate();
						
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
}

public class weiboTimeLineScreen extends MainScreen{
	
	static recvMain		sm_mainApp;
	
	MainManager			m_mainMgr;
	MainManager			m_mainAtMeMgr;
	MainManager			m_mainCommitMeMgr;
	
	MainManager			m_currMgr = null;
	
	static Bitmap		sm_sinaWeiboSign = null;
	static Bitmap		sm_sinaVIPSign = null;
	static Bitmap		sm_isBBerSign = null;
	
	
	Vector				m_headImageList = new Vector();
	
	static Bitmap		sm_defaultHeadImage = null;
		
	WeiboHeader 		m_weiboHeader		= new WeiboHeader(this);
	
	public weiboTimeLineScreen(recvMain _mainApp){
		sm_mainApp = _mainApp;
		
		m_mainMgr = new MainManager(_mainApp,this,true);
		add(m_mainMgr);
		
		m_mainAtMeMgr = new MainManager(_mainApp,this,false);
		m_mainCommitMeMgr = new MainManager(_mainApp,this,false);
		
		m_currMgr = m_mainMgr;
		
		setTitle(m_weiboHeader);
	}
	
	public void ClearWeibo(){
		m_mainMgr.deleteAll();
		m_mainMgr.add(m_mainMgr.m_updateWeiboField);
		
		m_mainAtMeMgr.deleteAll();		
		m_mainCommitMeMgr.deleteAll();
		
		invalidate();
	}
	
	public boolean AddWeibo(fetchWeibo _weibo,boolean _resetSelectIdx)throws Exception{
		
		HeadImage t_headImage = SearchHeadImage(_weibo);
		
		boolean t_alert = false;
		
		switch(_weibo.GetWeiboClass()){
		case fetchWeibo.TIMELINE_CLASS:
			m_mainMgr.AddWeibo(new WeiboItemField(_weibo,t_headImage),_resetSelectIdx);
			break;
		case fetchWeibo.COMMENT_ME_CLASS:
			m_mainCommitMeMgr.AddWeibo(new WeiboItemField(_weibo,t_headImage),_resetSelectIdx);
			t_alert = true;
			break;
		case fetchWeibo.AT_ME_CLASS:
			m_mainAtMeMgr.AddWeibo(new WeiboItemField(_weibo,t_headImage),_resetSelectIdx);
			t_alert = true;
			break;
		}
		
		if(_resetSelectIdx && t_alert){
			m_weiboHeader.invalidate();
		}
		
		return t_alert;
	}
	
	public void AddWeiboHeadImage(int _style,long _id,byte[] _dataArray){
		
		for(int i = 0 ;i < m_headImageList.size();i++){
			HeadImage t_image = (HeadImage)m_headImageList.elementAt(i);
			
			if(t_image.m_userID == _id && _style == t_image.m_weiboStyle){
				try{
					t_image.m_headImage = EncodedImage.createEncodedImage(_dataArray, 0, _dataArray.length).getBitmap();
					
					sm_mainApp.SetErrorString("recv weibo head image " + _id);
					
					t_image.m_dataHash = _dataArray.hashCode();
							
				}catch(Exception ex){
					sm_mainApp.SetErrorString("AWHI:"+ _id + " " + ex.getMessage() + ex.getClass().getName() );
				}
				
				break;				
			}
		}
		
	}
	
	private void SendHeadImageQueryMsg(fetchWeibo _weibo)throws Exception{
		
		ByteArrayOutputStream t_os = new ByteArrayOutputStream();
		t_os.write(msg_head.msgWeiboHeadImage);
		t_os.write(_weibo.GetWeiboStyle());
		sendReceive.WriteLong(t_os,_weibo.GetUserId());
		
		sm_mainApp.m_connectDeamon.m_sendingQueue.addSendingData(msg_head.msgWeiboHeadImage, t_os.toByteArray(),true);
	
	}
	
	private HeadImage SearchHeadImage(fetchWeibo _weibo)throws Exception{
		for(int i = 0 ;i < m_headImageList.size();i++){
			HeadImage t_image = (HeadImage)m_headImageList.elementAt(i);
			
			if(t_image.m_userID == _weibo.GetUserId()){
				if(t_image.m_dataHash != _weibo.GetUserHeadImageHashCode()){
					SendHeadImageQueryMsg(_weibo);
				}
				
				return t_image;
			}
		}
		
		SendHeadImageQueryMsg(_weibo);
		
		if(sm_defaultHeadImage == null){
			byte[] bytes = IOUtilities.streamToBytes(sm_mainApp.getClass().getResourceAsStream("/defaultHeadImage.png"));		
			sm_defaultHeadImage =  EncodedImage.createEncodedImage(bytes, 0, bytes.length).getBitmap();
		}
		
		HeadImage t_image = new HeadImage();
		
		t_image.m_userID = _weibo.GetUserId();
		t_image.m_headImage = sm_defaultHeadImage;
		t_image.m_dataHash = _weibo.GetUserHeadImageHashCode();
		t_image.m_weiboStyle = _weibo.GetWeiboStyle();
		
		m_headImageList.addElement(t_image);
		
		return t_image;
	}
	
	
	public void SendMenuItemClick(){
		if(WeiboItemField.sm_extendWeiboItem == m_mainMgr.m_updateWeiboField){
			
			try{
				// update a single weibo
				//
				String t_text = m_mainMgr.m_updateWeiboField.m_sendUpdateText;
				
				ByteArrayOutputStream t_os = new ByteArrayOutputStream();
				t_os.write(msg_head.msgWeibo);
				
				t_os.write(fetchWeibo.SINA_WEIBO_STYLE);
				t_os.write(0);
				
				sendReceive.WriteString(t_os,t_text);
				
				m_currMgr.EscapeKey();
				
				sm_mainApp.m_connectDeamon.m_sendingQueue.addSendingData(msg_head.msgWeibo,t_os.toByteArray(),true);	
			}catch(Exception e){
				sm_mainApp.SetErrorString("SMIC:" + e.getMessage() + e.getClass().getName());
			}
			
		}else{
			
			try{
				
				String t_text = WeiboItemField.sm_editTextArea.getText();
				
				ByteArrayOutputStream t_os = new ByteArrayOutputStream();
				t_os.write(msg_head.msgWeibo);
				
				t_os.write(WeiboItemField.sm_editWeiboItem.m_weibo.GetWeiboStyle());
				t_os.write(WeiboItemField.sm_currentSendType);
				
				sendReceive.WriteString(t_os,t_text);
				
				sendReceive.WriteLong(t_os,WeiboItemField.sm_editWeiboItem.m_weibo.GetId());
				
				m_currMgr.EscapeKey();
					
				sm_mainApp.m_connectDeamon.m_sendingQueue.addSendingData(msg_head.msgWeibo,t_os.toByteArray(),true);
				
			}catch(Exception e){
				sm_mainApp.SetErrorString("SMIC:" + e.getMessage() + e.getClass().getName());
			}
			
		}
	}
	
	MenuItem m_sendItem = new MenuItem(recvMain.sm_local.getString(localResource.WEIBO_SEND_LABEL),0,0){
        public void run() {
        	SendMenuItemClick();
        }
    };
    
    MenuItem m_stateItem = new MenuItem(recvMain.sm_local.getString(localResource.STATE_SCREEN_MENU_LABEL),1,0){
        public void run() {
        	recvMain t_recv = (recvMain)UiApplication.getUiApplication();
        	t_recv.pushStateScreen();
        }
    };
    
	protected void makeMenu(Menu _menu,int instance){
		
		if(WeiboItemField.sm_editWeiboItem != null && WeiboItemField.sm_extendWeiboItem != null){
			_menu.add(m_sendItem);
		}
		
		_menu.add(m_stateItem);
		
		super.makeMenu(_menu,instance);
    }
	
	protected boolean keyDown(int keycode,int time){
		
		if(WeiboItemField.sm_extendWeiboItem == null){
			
			final int key = Keypad.key(keycode);
	    	switch(key){
	    	case 'S':
	    		m_stateItem.run();
	    		return true;
	    	}
		}
		
		return false;    	
	}       
	
	 
	public boolean onClose(){
		
		if(!m_currMgr.EscapeKey()){
			
			if(sm_mainApp.m_connectDeamon.IsConnectState()){
	    		sm_mainApp.requestBackground();
	    		return false;
	    	}else{
	    		close();
	    		sm_mainApp.pushStateScreen();
	    		return true;
	    	}
		}
		
		return false;
	}
	
	protected boolean navigationMovement(int dx,int dy,int status,int time){
		
		boolean t_processed = false;
		
		if(dx != 0){
			if(WeiboItemField.sm_extendWeiboItem == null && WeiboItemField.sm_editWeiboItem == null){
				
				m_weiboHeader.setCurrState(m_weiboHeader.getCurrState() + dx);
				
				switch(m_weiboHeader.getCurrState()){
				case WeiboHeader.STATE_TIMELINE:
					if(m_currMgr != m_mainMgr){
						replace(m_currMgr,m_mainMgr);
						m_currMgr = m_mainMgr;
					}
					break;
				case WeiboHeader.STATE_COMMENT_ME:
					if(m_currMgr != m_mainCommitMeMgr){
						replace(m_currMgr,m_mainCommitMeMgr);
						m_currMgr = m_mainCommitMeMgr;
					}
					break;
					
				case WeiboHeader.STATE_AT_ME:
					if(m_currMgr != m_mainAtMeMgr){
						replace(m_currMgr,m_mainAtMeMgr);
						m_currMgr = m_mainAtMeMgr;
					}
				}
				
				m_currMgr.RestoreScroll();
				
											
				m_weiboHeader.layout(0, 0);
				m_weiboHeader.invalidate();
				
				sm_mainApp.StopWeiboNotification();
				
				t_processed = true;
			}
		}else{
			t_processed = m_currMgr.IncreaseRenderSize(dx,dy);
		}
		
		if(!t_processed && WeiboItemField.sm_editTextArea.isFocus()){
			t_processed = super.navigationMovement(dx, dy, status, time);
		}
		return 	t_processed;
		
	}
	protected boolean navigationClick(int status, int time){
		return m_currMgr.Clicked(status,time);		
	}
	
	static public Bitmap GetWeiboSign(fetchWeibo _weibo){
		
		try{
			switch(_weibo.GetWeiboStyle()){
			case fetchWeibo.SINA_WEIBO_STYLE:
				if(sm_sinaWeiboSign == null){
					byte[] bytes = IOUtilities.streamToBytes(sm_mainApp.getClass().getResourceAsStream("/sinaWeibo.png"));		
					sm_sinaWeiboSign =  EncodedImage.createEncodedImage(bytes, 0, bytes.length).getBitmap();					
				}
				
				return sm_sinaWeiboSign;
			}			
		}catch(Exception e){
			sm_mainApp.SetErrorString("GWS:" + e.getMessage() + e.getClass().getName());
		}		
		
		return null;
	}
	
	static public Bitmap GetSinaVIPSignBitmap(){
		if(sm_sinaVIPSign == null){
			try{
				byte[] bytes = IOUtilities.streamToBytes(sm_mainApp.getClass().getResourceAsStream("/sinaVIP.png"));		
				sm_sinaVIPSign =  EncodedImage.createEncodedImage(bytes, 0, bytes.length).getBitmap();	
			}catch(Exception e){
				sm_mainApp.SetErrorString("GSVSB:" + e.getMessage() + e.getClass().getName());
			}
					
		}
		
		return sm_sinaVIPSign;
	}
	
	static public Bitmap GetBBerSignBitmap(){
		if(sm_isBBerSign == null){
			try{
				byte[] bytes = IOUtilities.streamToBytes(sm_mainApp.getClass().getResourceAsStream("/BBSign.png"));		
				sm_isBBerSign =  EncodedImage.createEncodedImage(bytes, 0, bytes.length).getBitmap();	
			}catch(Exception e){
				sm_mainApp.SetErrorString("GBSB:" + e.getMessage() + e.getClass().getName());
			}
		}
		
		return sm_isBBerSign;
	}

}
