package com.yuchting.yuchberry.client;

import java.util.Random;
import java.util.Vector;

import local.localResource;
import net.rim.device.api.io.IOUtilities;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

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
			
			return true;
		}
		
		return false;
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
				WeiboItemField.sm_forwardBut.setFocus();
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
	
	static recvMain		sm_mainApp;
	
	MainManager			m_mainMgr;	 
	
	Vector				m_timelineWeibo 	= new Vector();
	Vector				m_directMessage 	= new Vector();
	Vector				m_atMeMessage 		= new Vector();
	Vector				m_commentMeMessage 	= new Vector();
	
	static Bitmap		sm_sinaWeiboSign = null;
	static Bitmap		sm_sinaVIPSign = null;
	static Bitmap		sm_isBBerSign = null;
	
	WeiboHeader 		m_weiboHeader		= new WeiboHeader();
	
	public weiboTimeLineScreen(recvMain _mainApp){
		sm_mainApp = _mainApp;
		
		m_mainMgr = new MainManager(_mainApp);
		add(m_mainMgr);
		
		setTitle(m_weiboHeader);
				
		//@{ test code
		try{
			byte[] bytes = IOUtilities.streamToBytes(sm_mainApp.getClass().getResourceAsStream("/Unknown_resize.jpg"));		
			Bitmap t_headImage =  EncodedImage.createEncodedImage(bytes, 0, bytes.length).getBitmap();
			
			Random t_rand = new Random();
			for(int i = 0 ;i < 200;i++){
				fetchWeibo t_weibo = new fetchWeibo();
				t_weibo.SetId(0);
				t_weibo.SetUserName("这是" + i);
				
				if(t_rand.nextInt() % 3 == 0){
					t_weibo.SetBBer(true);
				}
				
				if(t_rand.nextInt() % 3 == 0){
					t_weibo.SetSinaVIP(true);
				}
				
				if(i % 2 == 0){
					fetchWeibo t_commentWeibo = new fetchWeibo();
					t_commentWeibo.SetUserName("评论者");
					t_commentWeibo.SetText("这是一个评论这是一个评论这是一个评论这是一个评论这是一个评论这是一个评论这是一个评论");
					
					t_weibo.SetCommectWeibo(t_commentWeibo);
				}				
				
				t_weibo.SetText("这是一个测试这个这是一个测试这个这是一个测试这个这是一个测试这个");
				
				m_mainMgr.AddWeibo(new WeiboItemField(t_weibo,t_headImage));
			}
			
		}catch(Exception e){}
		//@}
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
		
		boolean t_processed = false;
		
		if(dx != 0){
			if(WeiboItemField.sm_extendWeiboItem == null && WeiboItemField.sm_editWeiboItem == null){
				m_weiboHeader.setCurrState(m_weiboHeader.getCurrState() + dx);
				m_weiboHeader.layout(0, 0);
				m_weiboHeader.invalidate();
				
				t_processed = true;
			}
		}else{
			t_processed = m_mainMgr.IncreaseRenderSize(dx,dy);			
		}
		
		if(!t_processed && WeiboItemField.sm_editTextArea.isFocus()){
			t_processed = super.navigationMovement(dx, dy, status, time);
		}
		return 	t_processed;
		
	}
	protected boolean navigationClick(int status, int time){
		return m_mainMgr.Clicked(status,time);		
	}
	

}
