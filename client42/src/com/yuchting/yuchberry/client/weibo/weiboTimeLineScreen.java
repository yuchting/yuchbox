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
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.UiEngine;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.component.NullField;
import net.rim.device.api.ui.component.BasicEditField;
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
	
	private static Bitmap[]		sm_VIPSign = 
	{
		null,
		null,
		null,
		
		null,
		null,
		null,
	};
	
	private static String[]		sm_VIPSignString = 
	{
		"/sinaVIP.png",
		"/sinaVIP.png",
		"/qqVIP.png",
		
		"/sinaVIP.png",
		"/sinaVIP.png",
		"/sinaVIP.png",
	};
	
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
	NullField			m_nullWeiboHeader	= new NullField();
	boolean			m_weiboHeaderShow	= true;
	
	boolean			m_onlineState = false;
	
	private Vector		m_delayWeiboAddList = new Vector();
	private int		m_delayWeiboAddRunnableID = -1;
	
	private Vector		m_delayWeiboDelList = new Vector();
	private int		m_delayWeiboDelRunnableID = -1;

	private WeiboMainManager[]	m_refMainManager = 
	{
		null,
		null,
		null,
		null,
	};
	
	recvMain			m_mainApp = null;
	
	public weiboTimeLineScreen(recvMain _mainApp){
		super(Manager.VERTICAL_SCROLL);
		sm_mainApp 	= _mainApp;
		m_mainApp	= _mainApp;
		
		m_mainMgr = new WeiboMainManager(_mainApp,this,true);
		add(m_mainMgr);
		
		m_mainAtMeMgr = new WeiboMainManager(_mainApp,this,false);
		m_mainCommitMeMgr = new WeiboMainManager(_mainApp,this,false);
		
		m_mainDMMgr = new WeiboDMManager(_mainApp, this, false);
		
		m_refMainManager[0] = m_mainMgr;
		m_refMainManager[1] = m_mainAtMeMgr;
		m_refMainManager[2] = m_mainCommitMeMgr;
		m_refMainManager[3] = m_mainDMMgr;		
		
		m_currMgr = m_mainMgr;
		
		setTitle(m_weiboHeader);
		
		m_currMgr.setFocus();
		
	}
	
	public void enableHeader(boolean _enable){
		if(m_mainApp.m_hideHeader){
			if(_enable){
				if(!m_weiboHeaderShow){
					m_weiboHeaderShow = true;
					setTitle(m_weiboHeader);
				}				
			}else{
				if(m_weiboHeaderShow){
					m_weiboHeaderShow = false;
					setTitle(m_nullWeiboHeader);
				}
			}
		}else{
			// show the header always
			//
			if(!m_weiboHeaderShow){
				m_weiboHeaderShow = true;
				setTitle(m_weiboHeader);
			}
		}
	}
	
	public boolean isHeaderShow(){
		if(m_mainApp.m_hideHeader){
			return m_weiboHeaderShow;
		}
		return true;
	}
		
	private final static int fsm_promptBubble_x = 30;
	private final static int fsm_promptBubble_y = 40;
	private final static int fsm_promptBubbleBorder = 5;
	private final static int fsm_promptBubbleArc = 8;
	private final static int	fsm_promptBubbleWidth = recvMain.fsm_display_width - (fsm_promptBubble_x) * 2;
	
	private final static int fsm_promptTextPos_x = fsm_promptBubble_x + fsm_promptBubbleBorder;
	private final static int fsm_promptTextPos_y = fsm_promptBubble_y + fsm_promptBubbleBorder;
	
	private final static int	fsm_promptTextWidth = fsm_promptBubbleWidth - fsm_promptBubbleBorder * 2;
	
	
	int					m_runnableTextShowID = -1;
	int					m_popupPromptTextTimer = 0;
	
	
	final class PromptBasicEditField extends BasicEditField {
		
		public PromptBasicEditField(){
			super(Field.READONLY);
			
		}
		public void setText(String _text){
			super.setText(_text);
			layout(fsm_promptTextWidth,1000);
		}
		
		public void paint(Graphics _g){
			super.paint(_g);
		}

	}
	
	public PromptBasicEditField 	m_promptTextArea	= new PromptBasicEditField();
	
	public synchronized void popupPromptText(String _prompt){
		
		if(!m_promptTextArea.getText().equals(_prompt)){
			
			m_popupPromptTextTimer = 0;
			
			m_promptTextArea.setText(_prompt);
			
			if(m_runnableTextShowID == -1){
				
				m_runnableTextShowID = m_mainApp.invokeLater(new Runnable() {
					
					public void run() {
						
						synchronized (weiboTimeLineScreen.this) {
							if(++m_popupPromptTextTimer > 10){
								m_promptTextArea.setText("");
								m_mainApp.cancelInvokeLater(m_runnableTextShowID);
								m_runnableTextShowID = -1;
								
								invalidate();
							}
						}
						
					}
				}, 200, true);
			}
			
			invalidate();
		}
	}

	protected void paint(Graphics g){
		super.paint(g);

		if(m_promptTextArea.getText().length() != 0){
			int t_height = m_promptTextArea.getHeight();
			
			int t_color = g.getColor();
			try{
				g.setColor(WeiboItemField.fsm_promptTextBGColor);
	        	g.fillRoundRect(fsm_promptBubble_x, fsm_promptBubble_y, 
	        			fsm_promptBubbleWidth, t_height + fsm_promptBubbleBorder * 2, 
	        			fsm_promptBubbleArc, fsm_promptBubbleArc);
	        	
	        	g.setColor(WeiboItemField.fsm_promptTextBorderColor);
	        	g.drawRoundRect(fsm_promptBubble_x, fsm_promptBubble_y, 
	        			fsm_promptBubbleWidth, t_height + fsm_promptBubbleBorder * 2, 
	        			fsm_promptBubbleArc, fsm_promptBubbleArc);
				
			}finally{
				g.setColor(t_color);
			}
			
	        boolean notEmpty = g.pushContext( fsm_promptTextPos_x, fsm_promptTextPos_y, 
	        		fsm_promptTextWidth , t_height, fsm_promptTextPos_x, fsm_promptTextPos_y );
	        try {
	            if( notEmpty ) {
	            	m_promptTextArea.paint(g);
	            }
	        } finally {
	            g.popContext();
	            g.setColor(t_color);
	        }
		}
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
			m_mainApp.SetErrorString("AW_i:"+e.getMessage());
		}
	}
	
	final class DelayAddWeiboData{
		public fetchWeibo	m_weibo;
		public boolean		m_initAdd;
		public DelayAddWeiboData(fetchWeibo _weibo,boolean _initAdd){
			m_weibo = _weibo;
			m_initAdd = _initAdd;
		}
	}
	
	public boolean AddWeibo(fetchWeibo _weibo,boolean _initAdd){
		
		synchronized (m_delayWeiboAddList) {
			
			m_delayWeiboAddList.addElement(new DelayAddWeiboData(_weibo,_initAdd));
			
			if(m_delayWeiboAddRunnableID == -1){
				
				m_delayWeiboAddRunnableID = m_mainApp.invokeLater(new Runnable() {
					
					public void run() {
						
						synchronized (m_delayWeiboAddList){
							
							if(!m_delayWeiboAddList.isEmpty()){
								
								DelayAddWeiboData t_data = (DelayAddWeiboData)m_delayWeiboAddList.elementAt(0);
								if(t_data.m_initAdd && m_mainApp.m_connectDeamon.CanNotConnectSvr()){
									// initAdd is system starting run, it must wait data service is available
									//
									return;
								}
								
								AddWeibo_imple(t_data.m_weibo,t_data.m_initAdd);
								m_delayWeiboAddList.removeElementAt(0);	
							}								
							
							if(m_delayWeiboAddList.isEmpty()){
								m_mainApp.cancelInvokeLater(m_delayWeiboAddRunnableID);
								m_delayWeiboAddRunnableID = -1;
							}
						}
					}
					
				},500, true);
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
				DelayAddWeiboData t_weiboData = (DelayAddWeiboData)m_delayWeiboAddList.elementAt(i);
				if(_weibo == t_weiboData.m_weibo){
					m_delayWeiboAddList.removeElementAt(i);
					
					return;
				}
			}
		}
		
		synchronized(m_delayWeiboDelList){
			
			m_delayWeiboDelList.addElement(_weibo);
			
			if(m_delayWeiboDelRunnableID == -1){
				
				m_delayWeiboDelRunnableID = m_mainApp.invokeLater(new Runnable() {
					
					public void run() {
						synchronized (m_delayWeiboDelList) {
														
							if(!m_delayWeiboDelList.isEmpty()){
								fetchWeibo t_delWeibo = (fetchWeibo)m_delayWeiboDelList.elementAt(0);
															
								if(m_currMgr.getCurrExtendedItem() != null 
								&& m_currMgr.getCurrExtendedItem().hasTheWeibo(t_delWeibo)){
									// if the user is reading this extend item
									//
									return ;
								}
								
								m_delayWeiboDelList.removeElementAt(0);
								
								for(int i = 0;i < m_refMainManager.length;i++){
									if(m_refMainManager[i].DelWeibo(t_delWeibo)){
										break;
									}
								}
								
								DelWeiboHeadImage(t_delWeibo.GetWeiboStyle(),Long.toString(t_delWeibo.GetId()));
							}
							
							if(m_delayWeiboDelList.isEmpty()){
								m_mainApp.cancelInvokeLater(m_delayWeiboDelRunnableID);
								m_delayWeiboDelRunnableID = -1;
							}
							
						}
					}
					
				}, 200, true);
			}
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
						
						m_mainApp.SetErrorString("recv weibo head image " + _id + " dataHash " + t_image.m_dataHash);					
								
					}catch(Exception ex){
						m_mainApp.SetErrorString("AWHI:"+ _id + " " + ex.getMessage() + ex.getClass().getName() );
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
		
		if(m_mainApp.m_dontDownloadWeiboHeadImage){
			return ;
		}
		
		if(!m_mainApp.isSDCardAvaible()){
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
		
		// whether large image 
		sendReceive.WriteBoolean(t_os,WeiboItemField.fsm_headImageWidth == fetchWeibo.fsm_headImageSize_l);
		
		m_mainApp.m_connectDeamon.addSendingData(msg_head.msgWeiboHeadImage, t_os.toByteArray(),true);
	
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
				byte[] bytes = IOUtilities.streamToBytes(m_mainApp.getClass()
						.getResourceAsStream(WeiboItemField.fsm_largeHeadImage?"/defaultHeadImage_l.png":"/defaultHeadImage.png"));		
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

			String t_imageFilename = null;
			if(WeiboItemField.fsm_largeHeadImage){
				t_imageFilename = m_mainApp.GetWeiboHeadImageDir(_weibo.GetWeiboStyle()) + _weibo.GetUserId() + "_l.png";
			}else{
				t_imageFilename = m_mainApp.GetWeiboHeadImageDir(_weibo.GetWeiboStyle()) + _weibo.GetUserId() + ".png";
			}
			
			FileConnection t_fc = (FileConnection)Connector.open(t_imageFilename,Connector.READ_WRITE);
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
			m_mainApp.SetErrorString("LWI:"+ e.getMessage() + e.getClass().getName());
		}
		
		return null;
	}
	
	public void UpdateNewWeibo(String _weiboText){
		
		try{
			// update a single weibo
			//
			ByteArrayOutputStream t_os = new ByteArrayOutputStream();
			t_os.write(msg_head.msgWeibo);
			
			t_os.write(fetchWeibo.SINA_WEIBO_STYLE);
			t_os.write(fetchWeibo.SEND_NEW_UPDATE_TYPE);
			
			sendReceive.WriteString(t_os,_weiboText);
			
			if(m_mainApp.canUseLocation()){
				t_os.write(1);
				m_mainApp.getGPSInfo().OutputData(t_os);
			}else{
				t_os.write(0);
			}
			
			m_currMgr.EscapeKey();
					
			m_mainApp.m_connectDeamon.addSendingData(msg_head.msgWeibo,t_os.toByteArray(),true);
			m_mainApp.m_sentWeiboNum++;
			
		}catch(Exception e){
			m_mainApp.SetErrorString("UNW:" + e.getMessage() + e.getClass().getName());
		}
	}
	
	public void SendMenuItemClick(){
		if(m_currMgr.getCurrExtendedItem() == m_mainMgr.m_updateWeiboField){
			
			UpdateNewWeibo(m_mainMgr.m_updateWeiboField.m_sendUpdateText);			
			
		}else{
			
			try{
				
				String t_text = m_currMgr.m_editTextArea.getText();
				
				m_currMgr.BackupSendWeiboText(m_currMgr.m_currentSendType,m_currMgr.getCurrEditItem().m_weibo,t_text);
				
				if(m_currMgr.getCurrEditItem().m_weibo.GetWeiboClass() == fetchWeibo.COMMENT_ME_CLASS){
					
					if(m_currMgr.getCurrEditItem().m_weibo.GetCommentWeibo() != null){
						
						long t_orgId = m_currMgr.getCurrEditItem().m_weibo.GetCommentWeiboId();
						long t_commentId = m_currMgr.getCurrEditItem().m_weibo.GetId();
						
						sendCommentReply(t_text,m_currMgr.getCurrEditItem().m_weibo.GetWeiboStyle(),
										m_currMgr.m_currentSendType,t_orgId,t_commentId);
						
					}else{
						UpdateNewWeibo(t_text);
						return ;
					}
				}else{
					long t_orgId = m_currMgr.getCurrEditItem().m_weibo.GetId();
					
					sendCommentReply(t_text,m_currMgr.getCurrEditItem().m_weibo.GetWeiboStyle(),
									m_currMgr.m_currentSendType,t_orgId,0);
				}			
				
				
			}catch(Exception e){
				m_mainApp.SetErrorString("SMIC:" + e.getMessage() + e.getClass().getName());
			}
			
		}
	}
	
	private void sendCommentReply(String _text,int _weiboStyle,int _sendType,long _orgId,long _commentId)throws Exception{
		
		ByteArrayOutputStream t_os = new ByteArrayOutputStream();
		
		t_os.write(msg_head.msgWeibo);
		t_os.write(_weiboStyle);
		t_os.write(m_currMgr.m_currentSendType);
		
		sendReceive.WriteString(t_os,_text);
		sendReceive.WriteBoolean(t_os,m_mainApp.m_publicForward);
		sendReceive.WriteLong(t_os,_orgId);
		sendReceive.WriteLong(t_os,_commentId);
		
		if(m_mainApp.canUseLocation()){
			t_os.write(1);
			m_mainApp.getGPSInfo().OutputData(t_os);
		}else{
			t_os.write(0);
		}
		
		if(_sendType == fetchWeibo.SEND_FORWARD_TYPE){
			sendReceive.WriteBoolean(t_os,m_mainApp.m_updateOwnListWhenFw);
		}else{
			sendReceive.WriteBoolean(t_os,m_mainApp.m_updateOwnListWhenRe);
		}
		
		m_currMgr.EscapeKey();
		m_mainApp.m_connectDeamon.addSendingData(msg_head.msgWeibo,t_os.toByteArray(),true);
		
		m_mainApp.m_sentWeiboNum++;
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
    
    public WeiboUpdateDlg m_currUpdateDlg = null;
    public boolean m_pushUpdateDlg = false;
    public MenuItem m_updateItem = new MenuItem(recvMain.sm_local.getString(localResource.WEIBO_UPDATE_DLG),m_menuIndex_op++,0){
        public void run() {
        	if(m_currUpdateDlg == null){
        		m_currUpdateDlg = new WeiboUpdateDlg(weiboTimeLineScreen.this);
        	}
        	
        	m_pushUpdateDlg = true;
        	UiApplication.getUiApplication().pushScreen(m_currUpdateDlg);
        }
    };
    
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
    				fetchWeibo t_weibo = m_currMgr.getCurrSelectedItem().m_weibo;
    				
    				ByteArrayOutputStream t_os = new ByteArrayOutputStream();
        			t_os.write(msg_head.msgWeiboDelete);
        			t_os.write(t_weibo.GetWeiboStyle());
        			sendReceive.WriteLong(t_os,t_weibo.GetId());
        			sendReceive.WriteBoolean(t_os,t_weibo.GetWeiboClass() == fetchWeibo.COMMENT_ME_CLASS);
        			
        			m_mainApp.m_connectDeamon.addSendingData(msg_head.msgWeiboDelete,t_os.toByteArray(),true);
        			
        			m_currMgr.DelWeibo(m_currMgr.getCurrSelectedItem().m_weibo);
    			}catch(Exception e){
    				m_mainApp.SetErrorString("DWI:" + e.getMessage() + e.getClass().getName());
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
		
		if(m_currMgr.getCurrEditItem() == null && !m_pushUpdateDlg){
			_menu.add(m_updateItem);
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
			m_mainApp.m_connectDeamon.addSendingData(msg_head.msgWeiboRefresh, t_os.toByteArray(),true);
		}catch(Exception e){
			m_mainApp.SetErrorString("SRM:"+e.getMessage() + e.getClass().getName());
		}
		
	}
	
	boolean m_shiftKeyIsDown = false;
	
	protected boolean keyDown(int keycode,int time){
		
		final int key = Keypad.key(keycode);
		
		if(m_currMgr.getCurrEditItem() == null){

			switch(key){
			case 'C':
				m_updateItem.run();
				return true;
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
			
			if(m_currMgr.getCurrExtendedItem() != null && m_currMgr != m_mainDMMgr){
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
			    		return true;
			    	case 10: // enter key
			    	case ' ':
			    	case '0':
			    		m_currMgr.Clicked(0, 0);   		
			    		return true;   		
			    	
			    	}
				}
			}	
		}
		
		if(key == 10){
			boolean t_shiftDown = (Keypad.status(keycode) & KeypadListener.STATUS_SHIFT) != 0;
    		if(t_shiftDown && m_currMgr.getCurrEditItem() != null){
    			// send the contain
    			//
    			m_sendItem.run();
    			return true;
    		}			
		}
		
		return super.keyDown(keycode,time);   	
	}
		 
	public boolean onClose(){
		
		if(!m_currMgr.EscapeKey()){
			
			if(m_mainApp.m_connectDeamon.IsConnectState()){
	    		m_mainApp.requestBackground();
	    		return false;
	    	}else{
	    		close();
	    		m_mainApp.pushStateScreen();
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
		
		if(m_runnableTextShowID != -1){
			invalidate();
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
		
		if(m_currMgr.getFieldCount() <= WeiboMainManager.fsm_maxItemInOneScreen){
			enableHeader(true);
		}
		
		m_weiboHeader.invalidate();
		m_mainApp.StopWeiboNotification();
		
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
	
	static public Bitmap GetVIPSignBitmap(fetchWeibo _weibo){
		
		if(sm_VIPSign[_weibo.GetWeiboStyle()] == null){
			try{
				byte[] bytes = IOUtilities.streamToBytes(sm_mainApp.getClass().getResourceAsStream(sm_VIPSignString[_weibo.GetWeiboStyle()]));		
				sm_VIPSign[_weibo.GetWeiboStyle()] =  EncodedImage.createEncodedImage(bytes, 0, bytes.length).getBitmap();	
			}catch(Exception e){
				sm_mainApp.SetErrorString("GSVSB:" + e.getMessage() + e.getClass().getName());
			}
					
		}
		
		return sm_VIPSign[_weibo.GetWeiboStyle()];
	}
	
	static public Bitmap GetHeadImageMaskBitmap(){
		if(sm_headImageMask == null){
			try{
				byte[] bytes = IOUtilities.streamToBytes(sm_mainApp.getClass()
							.getResourceAsStream(WeiboItemField.fsm_largeHeadImage?"/headImageMask_l.png":"/headImageMask.png"));		
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
