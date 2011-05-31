package com.yuchting.yuchberry.client.weibo;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import local.localResource;
import net.rim.device.api.io.IOUtilities;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.system.KeypadListener;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.container.MainScreen;

import com.yuchting.yuchberry.client.msg_head;
import com.yuchting.yuchberry.client.recvMain;
import com.yuchting.yuchberry.client.sendReceive;

public class weiboTimeLineScreen extends MainScreen{
	
	static recvMain				sm_mainApp = (recvMain)UiApplication.getUiApplication();
	
	WeiboMainManager			m_mainMgr;
	WeiboMainManager			m_mainAtMeMgr;
	WeiboMainManager			m_mainCommitMeMgr;
	
	WeiboMainManager			m_currMgr = null;
	
	static Bitmap		sm_sinaWeiboSign = null;
	static Bitmap		sm_sinaVIPSign = null;
	
	static Bitmap		sm_tWeiboSign = null;
	
	static Bitmap		sm_isBBerSign = null;
	
	
	Vector				m_headImageList = new Vector();
	
	static Bitmap		sm_defaultHeadImage = null;
		
	WeiboHeader 		m_weiboHeader		= new WeiboHeader(this);
	
	boolean			m_onlineState = false;
	
	
	public weiboTimeLineScreen(recvMain _mainApp){
		sm_mainApp = _mainApp;
		
		m_mainMgr = new WeiboMainManager(_mainApp,this,true);
		add(m_mainMgr);
		
		m_mainAtMeMgr = new WeiboMainManager(_mainApp,this,false);
		m_mainCommitMeMgr = new WeiboMainManager(_mainApp,this,false);
		
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
	
	public void SetOnlineState(boolean _online){
		m_onlineState = _online;
		m_weiboHeader.invalidate();
	}
	
	public boolean GetOnlineState(){
		return m_onlineState;
	}
	
	public boolean AddWeibo(fetchWeibo _weibo,boolean _resetSelectIdx)throws Exception{
		
		WeiboHeadImage t_headImage = SearchHeadImage(_weibo);
		
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
	
	public void DelWeibo(fetchWeibo _weibo){
		if(m_mainMgr.DelWeibo(_weibo)){
			return;
		}
		
		if(m_mainCommitMeMgr.DelWeibo(_weibo)){
			return ;
		}
		
		if(m_mainAtMeMgr.DelWeibo(_weibo)){
			return ;
		}
	}
	
	public void AddWeiboHeadImage(int _style,long _id,byte[] _dataArray){
		
		for(int i = 0 ;i < m_headImageList.size();i++){
			WeiboHeadImage t_image = (WeiboHeadImage)m_headImageList.elementAt(i);
			
			if(t_image.m_userID == _id && _style == t_image.m_weiboStyle){
				try{
					t_image.m_headImage = EncodedImage.createEncodedImage(_dataArray, 0, _dataArray.length).getBitmap();
					t_image.m_dataHash 	= _dataArray.length;
					
					sm_mainApp.SetErrorString("recv weibo head image " + _id + " dataHash " + t_image.m_dataHash);					
							
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
		
		sm_mainApp.m_connectDeamon.addSendingData(msg_head.msgWeiboHeadImage, t_os.toByteArray(),true);
	
	}
	
	private WeiboHeadImage SearchHeadImage(fetchWeibo _weibo)throws Exception{
		for(int i = 0 ;i < m_headImageList.size();i++){
			WeiboHeadImage t_image = (WeiboHeadImage)m_headImageList.elementAt(i);
			
			if(t_image.m_userID == _weibo.GetUserId() && _weibo.GetWeiboStyle() == t_image.m_weiboStyle){
				
				if(t_image.m_dataHash != _weibo.GetUserHeadImageHashCode()){
					SendHeadImageQueryMsg(_weibo);
				}
				
				return t_image;
			}
		}
		
		// find/load from the local ROM
		//
		WeiboHeadImage t_image = LoadWeiboImage(_weibo);
		if(t_image != null){
			if(t_image.m_dataHash != _weibo.GetUserHeadImageHashCode()){
				SendHeadImageQueryMsg(_weibo);
			}
			
			m_headImageList.addElement(t_image);
			return t_image;
		}
		
		// load the default image and send head image query message
		//
		SendHeadImageQueryMsg(_weibo);
		
		if(sm_defaultHeadImage == null){
			byte[] bytes = IOUtilities.streamToBytes(sm_mainApp.getClass().getResourceAsStream("/defaultHeadImage.png"));		
			sm_defaultHeadImage =  EncodedImage.createEncodedImage(bytes, 0, bytes.length).getBitmap();
		}
		
		t_image = new WeiboHeadImage();
		
		t_image.m_userID = _weibo.GetUserId();
		t_image.m_headImage = sm_defaultHeadImage;
		t_image.m_dataHash = _weibo.GetUserHeadImageHashCode();
		t_image.m_weiboStyle = _weibo.GetWeiboStyle();
		
		m_headImageList.addElement(t_image);
		
		return t_image;
	}
	
	private WeiboHeadImage LoadWeiboImage(fetchWeibo _weibo){
		try{

			FileConnection t_fc = (FileConnection)Connector.open(sm_mainApp.GetWeiboHeadImageDir(_weibo.GetWeiboStyle()) + _weibo.GetUserId() + ".png",
																Connector.READ_WRITE);
			try{
				if(t_fc.exists()){
					
					InputStream t_fileIn = t_fc.openInputStream();
					try{
																	
						byte[] t_data = new byte[(int)t_fc.fileSize()];
						
						sendReceive.ForceReadByte(t_fileIn, t_data, t_data.length);
						
						WeiboHeadImage t_image = new WeiboHeadImage();
						t_image.m_headImage =  EncodedImage.createEncodedImage(t_data, 0, t_data.length).getBitmap();
						
						t_image.m_dataHash = t_data.length;
						t_image.m_userID = _weibo.GetUserId();
						t_image.m_weiboStyle = _weibo.GetWeiboStyle();
																		
						return t_image;
						
					}finally{
						
						t_fileIn.close();
						t_fileIn = null;
					}
				}
				
			}finally{
				t_fc.close();
				t_fc = null;
			}	
		}catch(Exception e){
			sm_mainApp.SetErrorString("LWI:"+ e.getMessage() + e.getClass().getName());
		}
		
		return null;
	}
	
	private void UpdateNewWeibo(String _weiboText){
		
		try{
			// update a single weibo
			//
			ByteArrayOutputStream t_os = new ByteArrayOutputStream();
			t_os.write(msg_head.msgWeibo);
			
			t_os.write(fetchWeibo.SINA_WEIBO_STYLE);
			t_os.write(0);
			
			sendReceive.WriteString(t_os,_weiboText);
			
			if(sm_mainApp.canUseLocation()){
				t_os.write(1);
				sm_mainApp.getGPSInfo().OutputData(t_os);
			}else{
				t_os.write(0);
			}
			
			m_currMgr.EscapeKey();
					
			sm_mainApp.m_connectDeamon.addSendingData(msg_head.msgWeibo,t_os.toByteArray(),true);
			sm_mainApp.m_sentWeiboNum++;
			
		}catch(Exception e){
			sm_mainApp.SetErrorString("UNW:" + e.getMessage() + e.getClass().getName());
		}
	}
	public void SendMenuItemClick(){
		if(WeiboItemField.sm_extendWeiboItem == m_mainMgr.m_updateWeiboField){
			
			UpdateNewWeibo(m_mainMgr.m_updateWeiboField.m_sendUpdateText);
			
			
			
		}else{
			
			try{
				
				String t_text = WeiboItemField.sm_editTextArea.getText();
				
				fetchWeibo t_referenceWeibo = WeiboItemField.sm_editWeiboItem.m_weibo;
				
				if(t_referenceWeibo.GetWeiboClass() == fetchWeibo.COMMENT_ME_CLASS){
					if(t_referenceWeibo.GetCommentWeibo() != null){
						t_referenceWeibo = t_referenceWeibo.GetCommentWeibo();
					}else{
						UpdateNewWeibo(t_text);
						return ;
					}
				}			
				
				ByteArrayOutputStream t_os = new ByteArrayOutputStream();
				t_os.write(msg_head.msgWeibo);
			
				t_os.write(t_referenceWeibo.GetWeiboStyle());
				t_os.write(WeiboItemField.sm_currentSendType);
				
				sendReceive.WriteString(t_os,t_text);
				
				t_os.write(sm_mainApp.m_publicForward?1:0);
				sendReceive.WriteLong(t_os,t_referenceWeibo.GetId());
				
				if(sm_mainApp.canUseLocation()){
					t_os.write(1);
					sm_mainApp.getGPSInfo().OutputData(t_os);
				}else{
					t_os.write(0);
				}
				
				if(WeiboItemField.sm_currentSendType == 1){
					t_os.write(sm_mainApp.m_updateOwnListWhenFw?1:0);
				}else{
					t_os.write(sm_mainApp.m_updateOwnListWhenRe?1:0);
				}
				
				m_currMgr.EscapeKey();
				sm_mainApp.m_connectDeamon.addSendingData(msg_head.msgWeibo,t_os.toByteArray(),true);
				
				sm_mainApp.m_sentWeiboNum++;
				
			}catch(Exception e){
				sm_mainApp.SetErrorString("SMIC:" + e.getMessage() + e.getClass().getName());
			}
			
		}
	}
	
	int m_menuIndex = 0;
	
	MenuItem m_homeManagerItem = new MenuItem(recvMain.sm_local.getString(localResource.WEIBO_HOME_MANAGER_MENU_LABEL),m_menuIndex++,0){
        public void run() {
        	m_weiboHeader.setCurrState(WeiboHeader.STATE_TIMELINE);
    		refreshWeiboHeader();
        }
    }; 
    
    MenuItem m_atMeManagerItem = new MenuItem(recvMain.sm_local.getString(localResource.WEIBO_AT_ME_MANAGER_MENU_LABEL),m_menuIndex++,0){
        public void run() {
        	m_weiboHeader.setCurrState(WeiboHeader.STATE_AT_ME);
    		refreshWeiboHeader();
        }
    }; 
    
    MenuItem m_commentMeItem = new MenuItem(recvMain.sm_local.getString(localResource.WEIBO_COMMENT_ME_MANAGER_MENU_LABEL),m_menuIndex++,0){
        public void run() {
        	m_weiboHeader.setCurrState(WeiboHeader.STATE_COMMENT_ME);
    		refreshWeiboHeader();
        }
    }; 
    
    MenuItem m_directMsgItem = new MenuItem(recvMain.sm_local.getString(localResource.WEIBO_DM_MANAGER_MENU_LABEL),m_menuIndex++,0){
        public void run() {
        	m_weiboHeader.setCurrState(WeiboHeader.STATE_DIRECT_MESSAGE);
    		refreshWeiboHeader();
        }
    };    
   
    int m_menuIndex_op = 20;    
    
	MenuItem m_sendItem = new MenuItem(recvMain.sm_local.getString(localResource.WEIBO_SEND_LABEL),m_menuIndex_op++,0){
        public void run() {
        	SendMenuItemClick();
        }
    };
    
    MenuItem m_refreshItem = new MenuItem(recvMain.sm_local.getString(localResource.WEIBO_REFRESH_MENU_LABEL),m_menuIndex_op++,0){
        public void run() {
        	SendRefreshMsg();
        }
    };    
     
    
    MenuItem m_topItem = new MenuItem(recvMain.sm_local.getString(localResource.WEIBO_RETURN_TOP_MENU_LABEL),m_menuIndex_op++,0){
        public void run() {
        	m_currMgr.ScrollToTop();
        }
    };
    
    MenuItem m_bottomItem = new MenuItem(recvMain.sm_local.getString(localResource.WEIBO_BACK_BOTTOM_MENU_LABEL),m_menuIndex_op++,0){
        public void run() {
        	m_currMgr.ScrollToBottom();
        }
    };
    
    MenuItem m_preWeiboItem = new MenuItem(recvMain.sm_local.getString(localResource.WEIBO_PRE_ITEM_MENU_LABEL),m_menuIndex_op++,0){
        public void run() {
        	m_currMgr.OpenNextWeiboItem(false);
        }
    };
    MenuItem m_nextWeiboItem = new MenuItem(recvMain.sm_local.getString(localResource.WEIBO_NEXT_WEIBO_MENU_LABEL),m_menuIndex_op++,0){
        public void run() {
        	m_currMgr.OpenNextWeiboItem(true);
        }
    };
    
    MenuItem m_deleteItem = new MenuItem(recvMain.sm_local.getString(localResource.WEIBO_DELETE_MENU_LABEL),m_menuIndex_op++,0){
        public void run() {
        	deleteWeiboItem();
        }
    }; 
    
    MenuItem m_helpItem = new MenuItem(recvMain.sm_local.getString(localResource.WEIBO_HELP_MENU_LABEL),99,0){
        public void run() {
        	recvMain.openURL("http://code.google.com/p/yuchberry/wiki/YuchBerry_Weibo");
        }
    }; 
    
    MenuItem m_stateItem = new MenuItem(recvMain.sm_local.getString(localResource.WEIBO_STATE_SCREEN_MENU_LABEL),100,0){
        public void run() {
        	recvMain t_recv = (recvMain)UiApplication.getUiApplication();
        	t_recv.pushStateScreen();
        }
    };       
    
    public void deleteWeiboItem(){
    	
    	if(WeiboItemField.sm_selectWeiboItem != null 
    	&& WeiboItemField.sm_selectWeiboItem.m_weibo != null
    	&& WeiboItemField.sm_selectWeiboItem.m_weibo.IsOwnWeibo()){
    		if(Dialog.ask(Dialog.D_YES_NO,recvMain.sm_local.getString(localResource.WEIBO_DELETE_ASK_PROMPT),Dialog.NO) == Dialog.YES){
    			try{
    				ByteArrayOutputStream t_os = new ByteArrayOutputStream();
        			t_os.write(msg_head.msgWeiboDelete);
        			t_os.write(WeiboItemField.sm_selectWeiboItem.m_weibo.GetWeiboStyle());
        			sendReceive.WriteLong(t_os,WeiboItemField.sm_selectWeiboItem.m_weibo.GetId());
        			
        			sm_mainApp.m_connectDeamon.addSendingData(msg_head.msgWeiboDelete,t_os.toByteArray(),true);
        			
        			m_currMgr.DelWeibo(WeiboItemField.sm_selectWeiboItem.m_weibo);
    			}catch(Exception e){
    				sm_mainApp.SetErrorString("DWI:" + e.getMessage() + e.getClass().getName());
    			}
    		}
    	}
    	
    }
    
	protected void makeMenu(Menu _menu,int instance){
		
		if(WeiboItemField.sm_extendWeiboItem == null){
			_menu.add(m_homeManagerItem);
			_menu.add(m_atMeManagerItem);
			_menu.add(m_commentMeItem);
			_menu.add(m_directMsgItem);
			
			_menu.add(MenuItem.separator(m_menuIndex));
		}
		
		if(WeiboItemField.sm_editWeiboItem != null && WeiboItemField.sm_extendWeiboItem != null){
			_menu.add(m_sendItem);
		}		
		
		_menu.add(m_refreshItem);
		
		if(WeiboItemField.sm_extendWeiboItem == null){
			
			_menu.add(m_topItem);
			_menu.add(m_bottomItem);	
			
		}else{
			if(WeiboItemField.sm_editWeiboItem == null){
				_menu.add(m_preWeiboItem);
				_menu.add(m_nextWeiboItem);
			}
		}
		
		if(WeiboItemField.sm_selectWeiboItem.m_weibo != null
		&& WeiboItemField.sm_selectWeiboItem.m_weibo.IsOwnWeibo()){
			_menu.add(m_deleteItem);
		}
		
		_menu.add(MenuItem.separator(50));
		_menu.add(m_helpItem);
		_menu.add(m_stateItem);		
		
		super.makeMenu(_menu,instance);
    }
	
	private void SendRefreshMsg(){
		try{
			ByteArrayOutputStream t_os = new ByteArrayOutputStream();
			t_os.write(msg_head.msgWeiboRefresh);
			sm_mainApp.m_connectDeamon.addSendingData(msg_head.msgWeiboRefresh, t_os.toByteArray(),true);
		}catch(Exception e){
			sm_mainApp.SetErrorString("SRM:"+e.getMessage() + e.getClass().getName());
		}
		
	}
	
	boolean m_shiftKeyIsDown = false;
	
	protected boolean keyDown(int keycode,int time){
		
		final int key = Keypad.key(keycode);
				
		if(WeiboItemField.sm_extendWeiboItem != null && WeiboItemField.sm_editWeiboItem == null){
			switch(key){
	    	case ' ':
	    		boolean t_shiftDown = (Keypad.status(keycode) & KeypadListener.STATUS_SHIFT) != 0;
	    		m_currMgr.OpenNextWeiboItem(!t_shiftDown);
	    		return true;
	    	case 'F':
	    		m_currMgr.ForwardWeibo(WeiboItemField.sm_extendWeiboItem);
	    		return true;
	    	case 'V':
	    		m_currMgr.FavoriteWeibo(WeiboItemField.sm_extendWeiboItem);
	    		return true;
	    	case 'E':
	    		m_currMgr.AtWeibo(WeiboItemField.sm_extendWeiboItem);
	    		return true;
	    	case 'P':
	    		m_currMgr.OpenOriginalPic(WeiboItemField.sm_extendWeiboItem);
	    		return true;
	    	case 'D':
	    		deleteWeiboItem();
	    		return true;
			}
		}else{
			
			if(WeiboItemField.sm_extendWeiboItem == null){
				switch(key){
		    	case 'S':
		    		m_stateItem.run();
		    		return true;
		    	case 'T':		    		
	    			m_currMgr.ScrollToTop();
	    			return true;
		    	case 'B':
	    			m_currMgr.ScrollToBottom();
	    			return true;
		    	case 'R':
		    		SendRefreshMsg();
		    		break;
		    	case 10: // enter key
		    		m_currMgr.Clicked(0, 0);
		    		break;
		    	case 'H':
		    		m_homeManagerItem.run();
		    		return true;
		    	case 'M':
		    		m_atMeManagerItem.run();
		    		return true;
		    	case 'C':
		    		m_commentMeItem.run();
		    		return true;
		    	case 'D':
		    		m_directMsgItem.run();
		    		return true;
		    	}
			}
		}

		return super.keyDown(keycode,time);    	
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
				refreshWeiboHeader();
				
				t_processed = true;
			}
		}else{
			t_processed = m_currMgr.IncreaseRenderSize(dx,dy);
		}
		
		if(!t_processed /*&& WeiboItemField.sm_editTextArea.isFocus()*/){
			t_processed = super.navigationMovement(dx, dy, status, time);
		}
		return 	t_processed;
		
	}
	
	private void refreshWeiboHeader(){
		
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
		m_weiboHeader.invalidate();
		sm_mainApp.StopWeiboNotification();
		
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
			case fetchWeibo.TWITTER_WEIBO_STYLE:
				if(sm_tWeiboSign == null){
					byte[] bytes = IOUtilities.streamToBytes(sm_mainApp.getClass().getResourceAsStream("/tWeibo.png"));		
					sm_tWeiboSign =  EncodedImage.createEncodedImage(bytes, 0, bytes.length).getBitmap();					
				}
				
				return sm_tWeiboSign;
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
