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
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.Manager;
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
	WeiboDMManager				m_mainDMMgr;
	
	WeiboMainManager			m_currMgr = null;
	
	static Bitmap		sm_sinaVIPSign = null;
	
	static Bitmap		sm_headImageMask = null;
	
	static Bitmap[]		sm_WeiboSign =
	{ 
		null,
		null,
		null,
		
		null,
		null,
		null,
	};
	
	static String[]		sm_weiboSignFilename = 
	{
		"/sinaWeibo.png",
		"/tWeibo.png",
		"/qqWeibo.png",
		
		"/163Weibo.png",
		"/sohuWeibo.png",
		"/fanWeibo.png",
	};
	
	static Bitmap		sm_isBBerSign = null;	
	
	private Vector		m_headImageList = new Vector();
	
	static Bitmap		sm_defaultHeadImage = null;
		
	WeiboHeader 		m_weiboHeader		= new WeiboHeader(this);
	
	boolean			m_onlineState = false;
	
	private Vector		m_delayWeiboAddList = new Vector();
	private int		m_delayWeiboRunnableID = -1;
	
	
	public weiboTimeLineScreen(recvMain _mainApp){
		super(Manager.VERTICAL_SCROLL);
		sm_mainApp = _mainApp;
		
		m_mainMgr = new WeiboMainManager(_mainApp,this,true);
		add(m_mainMgr);
		
		m_mainAtMeMgr = new WeiboMainManager(_mainApp,this,false);
		m_mainCommitMeMgr = new WeiboMainManager(_mainApp,this,false);
		
		m_mainDMMgr = new WeiboDMManager(_mainApp, this, false);
		
		m_currMgr = m_mainMgr;
		
		setTitle(m_weiboHeader);
		
		m_currMgr.setFocus();
	}
	
	public void ClearWeibo(){
		m_mainMgr.deleteAll();
		m_mainMgr.add(m_mainMgr.m_updateWeiboField.getFocusField());
		
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
	
	private void AddWeibo_imple(fetchWeibo _weibo,boolean _initAdd){
		
		try{
			WeiboHeadImage t_headImage = SearchHeadImage(_weibo);
			
			switch(_weibo.GetWeiboClass()){
			case fetchWeibo.TIMELINE_CLASS:
				m_mainMgr.AddWeibo(_weibo,t_headImage,_initAdd);
				break;
			case fetchWeibo.COMMENT_ME_CLASS:
				m_mainCommitMeMgr.AddWeibo(_weibo,t_headImage,_initAdd);
				break;
			case fetchWeibo.AT_ME_CLASS:
				m_mainAtMeMgr.AddWeibo(_weibo,t_headImage,_initAdd);
				break;
			case fetchWeibo.DIRECT_MESSAGE_CLASS:
				m_mainDMMgr.AddWeibo(_weibo,t_headImage,_initAdd);
				break;
			}
			
			if(!_initAdd){
				m_weiboHeader.invalidate();
			}
			
		}catch(Exception e){
			sm_mainApp.SetErrorString("AW_i:"+e.getMessage());
		}
	}
	
	public boolean AddWeibo(fetchWeibo _weibo,boolean _initAdd){
		
		if(_initAdd){
			// initialize to add history weibo from file store
			//
			AddWeibo_imple(_weibo,true);		
		}else{

			synchronized (m_delayWeiboAddList) {
				m_delayWeiboAddList.addElement(_weibo);
				
				if(m_delayWeiboRunnableID == -1){
					
					m_delayWeiboRunnableID = sm_mainApp.invokeLater(new Runnable() {
						public void run() {
							
							synchronized (m_delayWeiboAddList) {
								AddWeibo_imple((fetchWeibo)m_delayWeiboAddList.elementAt(0),true);
								m_delayWeiboAddList.removeElementAt(0);
								
								if(m_delayWeiboAddList.isEmpty()){
									sm_mainApp.cancelInvokeLater(m_delayWeiboRunnableID);
									m_delayWeiboRunnableID = -1;
								}	
							}
							
						}
						
					},200, true);
				}
			}
		}	
		
		return _weibo.GetWeiboClass() == fetchWeibo.COMMENT_ME_CLASS
				|| _weibo.GetWeiboClass() == fetchWeibo.AT_ME_CLASS
				|| _weibo.GetWeiboClass() == fetchWeibo.DIRECT_MESSAGE_CLASS;		
	}
	
	public void DelWeibo(fetchWeibo _weibo){
		
		synchronized (m_delayWeiboAddList) {
			// first check in the delay add list
			//
			for(int i = 0 ;i < m_delayWeiboAddList.size();i++){
				if(_weibo == m_delayWeiboAddList.elementAt(i)){
					m_delayWeiboAddList.removeElementAt(i);
					
					return;
				}
			}
		}
		
		if(m_mainMgr.DelWeibo(_weibo)){
			return;
		}
		
		if(m_mainCommitMeMgr.DelWeibo(_weibo)){
			return ;
		}
		
		if(m_mainAtMeMgr.DelWeibo(_weibo)){
			return ;
		}
		
		if(m_mainDMMgr.DelWeibo(_weibo)){
			return ;
		}
	}
	
	public void AddWeiboHeadImage(int _style,String _id,byte[] _dataArray){
		
		synchronized (m_headImageList) {

			for(int i = 0 ;i < m_headImageList.size();i++){
				WeiboHeadImage t_image = (WeiboHeadImage)m_headImageList.elementAt(i);
				
				if(t_image.m_userID.equals(_id) && _style == t_image.m_weiboStyle){
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
	}
	
	public void DelWeiboHeadImage(int _style,String _id){
		
		synchronized (m_headImageList) {

			for(int i = 0 ;i < m_headImageList.size();i++){
				WeiboHeadImage t_image = (WeiboHeadImage)m_headImageList.elementAt(i);
				
				if(t_image.m_userID.equals(_id) && _style == t_image.m_weiboStyle){
					m_headImageList.removeElementAt(i);
					
					break;				
				}
			}
		}
	}
	
	private void SendHeadImageQueryMsg(fetchWeibo _weibo)throws Exception{
		
		if(sm_mainApp.m_dontDownloadWeiboHeadImage){
			return ;
		}
		
		if(!sm_mainApp.isSDCardAvaible()){
			return ;
		}
		
		ByteArrayOutputStream t_os = new ByteArrayOutputStream();
		t_os.write(msg_head.msgWeiboHeadImage);
		t_os.write(_weibo.GetWeiboStyle());
		
		if(_weibo.GetWeiboStyle() == fetchWeibo.QQ_WEIBO_STYLE){
			sendReceive.WriteString(t_os,_weibo.GetUserScreenName());
		}else{
			sendReceive.WriteLong(t_os,_weibo.GetUserId());
		}		
		
		sm_mainApp.m_connectDeamon.addSendingData(msg_head.msgWeiboHeadImage, t_os.toByteArray(),true);
	
	}
	
	private WeiboHeadImage SearchHeadImage(fetchWeibo _weibo)throws Exception{
		
		synchronized (m_headImageList) {
			
			for(int i = 0 ;i < m_headImageList.size();i++){
				WeiboHeadImage t_image = (WeiboHeadImage)m_headImageList.elementAt(i);
								
				if(_weibo.GetWeiboStyle() == t_image.m_weiboStyle 
					&& t_image.m_userID.equals(_weibo.GetHeadImageId()) ){
					
					if(t_image.m_dataHash != _weibo.GetUserHeadImageHashCode()){
						SendHeadImageQueryMsg(_weibo);
					}
					
					return t_image;
				}
			}
			
			// find/load from the local FileStore
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
			
			t_image.m_userID = _weibo.GetHeadImageId();
			t_image.m_headImage = sm_defaultHeadImage;
			t_image.m_dataHash = _weibo.GetUserHeadImageHashCode();
			t_image.m_weiboStyle = _weibo.GetWeiboStyle();
			
			m_headImageList.addElement(t_image);
			
			return t_image;
		}		
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
						t_image.m_userID = _weibo.GetHeadImageId();
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
		if(m_currMgr.getCurrExtendedItem() == m_mainMgr.m_updateWeiboField){
			
			UpdateNewWeibo(m_mainMgr.m_updateWeiboField.m_sendUpdateText);			
			
		}else{
			
			try{
				
				String t_text = m_currMgr.m_editTextArea.getText();
				
				fetchWeibo t_referenceWeibo = m_currMgr.getCurrEditItem().m_weibo;
				
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
				t_os.write(m_currMgr.m_currentSendType);
				
				sendReceive.WriteString(t_os,t_text);
				
				t_os.write(sm_mainApp.m_publicForward?1:0);
				sendReceive.WriteLong(t_os,t_referenceWeibo.GetId());
				
				if(sm_mainApp.canUseLocation()){
					t_os.write(1);
					sm_mainApp.getGPSInfo().OutputData(t_os);
				}else{
					t_os.write(0);
				}
				
				if(m_currMgr.m_currentSendType == 1){
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
    	
    	if(m_currMgr.getCurrSelectedItem() != null 
    	&& m_currMgr.getCurrSelectedItem().m_weibo != null
    	&& m_currMgr.getCurrSelectedItem().m_weibo.IsOwnWeibo()){
    		if(Dialog.ask(Dialog.D_YES_NO,recvMain.sm_local.getString(localResource.WEIBO_DELETE_ASK_PROMPT),Dialog.NO) == Dialog.YES){
    			try{
    				ByteArrayOutputStream t_os = new ByteArrayOutputStream();
        			t_os.write(msg_head.msgWeiboDelete);
        			t_os.write(m_currMgr.getCurrSelectedItem().m_weibo.GetWeiboStyle());
        			sendReceive.WriteLong(t_os,m_currMgr.getCurrSelectedItem().m_weibo.GetId());
        			
        			sm_mainApp.m_connectDeamon.addSendingData(msg_head.msgWeiboDelete,t_os.toByteArray(),true);
        			
        			m_currMgr.DelWeibo(m_currMgr.getCurrSelectedItem().m_weibo);
    			}catch(Exception e){
    				sm_mainApp.SetErrorString("DWI:" + e.getMessage() + e.getClass().getName());
    			}
    		}
    	}
    	
    }
    
	protected void makeMenu(Menu _menu,int instance){
		
		if(m_currMgr.getCurrEditItem() == null){
			_menu.add(m_homeManagerItem);
			_menu.add(m_atMeManagerItem);
			_menu.add(m_commentMeItem);
			_menu.add(m_directMsgItem);
			
			_menu.add(MenuItem.separator(m_menuIndex));
		}
		
		if(m_currMgr.getCurrEditItem() != null && m_currMgr.getCurrExtendedItem() != null){
			_menu.add(m_sendItem);
		}		
		
		_menu.add(m_refreshItem);
		
		if(m_currMgr.getCurrExtendedItem() == null){
			
			_menu.add(m_topItem);
			_menu.add(m_bottomItem);	
			
		}else{
			if(m_currMgr.getCurrEditItem() == null){
				_menu.add(m_preWeiboItem);
				_menu.add(m_nextWeiboItem);
			}
		}
		
		if(m_currMgr.getCurrSelectedItem() != null				// has selected
		&& m_currMgr.getCurrSelectedItem().m_weibo != null		// is not update weibo
		&& m_currMgr.getCurrSelectedItem().m_weibo.IsOwnWeibo()){ // is own weibo
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
		
		if(m_currMgr.getCurrEditItem() == null){

			switch(key){
			case 'U':
	    		m_homeManagerItem.run();
	    		return true;
	    	case 'I':
	    		m_atMeManagerItem.run();
	    		return true;
	    	case 'O':
	    		m_commentMeItem.run();
	    		return true;
	    	case 'P':
	    		m_directMsgItem.run();
	    		return true;
	    	case 'R':
	    		m_refreshItem.run();
	    		return true;
			}
			
			if(m_currMgr.getCurrExtendedItem() != null ){
				switch(key){
		    	case ' ':
		    		//boolean t_shiftDown = (Keypad.status(keycode) & KeypadListener.STATUS_SHIFT) != 0;
		    		m_currMgr.OpenNextWeiboItem(true);
		    		return true;
		    	case 'F':
		    		m_currMgr.ForwardWeibo(m_currMgr.getCurrExtendedItem());
		    		return true;
		    	case 'V':
		    		m_currMgr.FavoriteWeibo(m_currMgr.getCurrExtendedItem());
		    		return true;
		    	case 'E':
		    		m_currMgr.AtWeibo(m_currMgr.getCurrExtendedItem());
		    		return true;
		    	case 'G':
		    		m_currMgr.OpenOriginalPic(m_currMgr.getCurrExtendedItem());
		    		return true;
		    	case 'D':
		    		deleteWeiboItem();
		    		return true;
		    	case '0':
		    		m_currMgr.OpenNextWeiboItem(false);
		    		return true;
		    	case 'L':
		    		m_currMgr.FollowCommentUser(m_currMgr.getCurrExtendedItem());
		    		return true;
				}
			}else{
				
				if(m_currMgr.getCurrExtendedItem() == null){
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
			    	
			    	}
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
		if(dx != 0){
			if(m_currMgr.getCurrExtendedItem() == null && m_currMgr.getCurrEditItem() == null){
				
				m_weiboHeader.setCurrState(m_weiboHeader.getCurrState() + dx);
				refreshWeiboHeader();
				
				return true;
			}
			
		}
		return 	super.navigationMovement(dx, dy, status, time);
		
	}
	
	private void refreshWeiboHeader(){
		
		switch(m_weiboHeader.getCurrState()){
		case WeiboHeader.STATE_TIMELINE:
			if(m_currMgr != m_mainMgr){
				m_currMgr.backupFocusField();
				replace(m_currMgr,m_mainMgr);
				m_currMgr = m_mainMgr;
			}else{
				return;
			}
			break;
		case WeiboHeader.STATE_COMMENT_ME:
			if(m_currMgr != m_mainCommitMeMgr){
				m_currMgr.backupFocusField();
				replace(m_currMgr,m_mainCommitMeMgr);
				m_currMgr = m_mainCommitMeMgr;
			}else{
				return;
			}
			break;
			
		case WeiboHeader.STATE_AT_ME:
			if(m_currMgr != m_mainAtMeMgr){
				m_currMgr.backupFocusField();
				replace(m_currMgr,m_mainAtMeMgr);
				m_currMgr = m_mainAtMeMgr;
			}else{
				return;
			}
			break;
		case WeiboHeader.STATE_DIRECT_MESSAGE:
			if(m_currMgr != m_mainDMMgr){
				m_currMgr.backupFocusField();
				replace(m_currMgr, m_mainDMMgr);
				m_currMgr = m_mainDMMgr;
			}else{
				return;
			}
			break;
		}
		
		m_currMgr.restoreFocusField();
		
		m_weiboHeader.invalidate();
		sm_mainApp.StopWeiboNotification();
		
	}
	
	protected boolean navigationClick(int status, int time){
		return m_currMgr.Clicked(status,time);		
	}
	
	static public Bitmap GetWeiboSign(fetchWeibo _weibo){
		
		try{
			if(sm_WeiboSign[_weibo.GetWeiboStyle()] == null){
				byte[] bytes = IOUtilities.streamToBytes(sm_mainApp.getClass().getResourceAsStream(sm_weiboSignFilename[_weibo.GetWeiboStyle()]));		
				sm_WeiboSign[_weibo.GetWeiboStyle()] =  EncodedImage.createEncodedImage(bytes, 0, bytes.length).getBitmap();					
			}
			return sm_WeiboSign[_weibo.GetWeiboStyle()];
			
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
	
	static public Bitmap GetHeadImageMaskBitmap(){
		if(sm_headImageMask == null){
			try{
				byte[] bytes = IOUtilities.streamToBytes(sm_mainApp.getClass().getResourceAsStream("/headImageMask.png"));		
				sm_headImageMask =  EncodedImage.createEncodedImage(bytes, 0, bytes.length).getBitmap();	
			}catch(Exception e){
				sm_mainApp.SetErrorString("GHIMB" + e.getMessage() + e.getClass().getName());
			}
					
		}
		
		return sm_headImageMask;
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
