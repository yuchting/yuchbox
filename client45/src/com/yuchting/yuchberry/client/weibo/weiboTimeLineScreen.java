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
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.component.NullField;
import net.rim.device.api.ui.component.TextField;
import net.rim.device.api.ui.container.MainScreen;

import com.yuchting.yuchberry.client.msg_head;
import com.yuchting.yuchberry.client.recvMain;
import com.yuchting.yuchberry.client.sendReceive;
import com.yuchting.yuchberry.client.ui.BubbleImage;
import com.yuchting.yuchberry.client.ui.ImageSets;
import com.yuchting.yuchberry.client.ui.ImageUnit;

abstract class WeiboUserMenu extends MenuItem{
	
	String m_userName = "@";
	int	m_strRes;
	
	public WeiboUserMenu(int _strRes,int _ordinal ,int _priority){
		super("",_ordinal,_priority);
		
		m_strRes = _strRes;
	}
	
	public void setUserName(String _userName){
		m_userName = _userName;
		setText(recvMain.sm_local.getString(m_strRes) + " " + m_userName);
	}
}

public class weiboTimeLineScreen extends MainScreen{
	
	public WeiboUserMenu m_userGetInfoMenu = new WeiboUserMenu(localResource.WEIBO_CHECK_USER_MENU_LABEL,5,5){
		public void run(){
			checkUserInfo(m_userName);
		}
	};
	
	public WeiboUserMenu m_userFollowMenu = new WeiboUserMenu(localResource.WEIBO_FOLLOW_USER_MENU_LABEL,5,6){
		public void run(){
			followUser(m_userName);
		}
	};
	
	public WeiboUserMenu m_userAtUserMenu = new WeiboUserMenu(localResource.WEIBO_AT_USER_MENU_LABEL,5,7){
		public void run(){
			m_updateItem.run();
			m_currUpdateDlg.m_updateManager.m_editTextArea.setText("@" + this.m_userName + " ");
		}
	};
	
	public WeiboUserMenu m_userSendMessageMenu = new WeiboUserMenu(localResource.WEIBO_SEND_MESSAGE_USER_MENU_LABEL,5,8){
		public void run(){
			
		}
	};
	public static recvMain				sm_mainApp = (recvMain)UiApplication.getUiApplication();
	
	public static ImageSets			sm_weiboUIImage = null;
	public static BubbleImage 			sm_bubbleImage = null;
	public static BubbleImage 			sm_bubbleImage_black = null;
	static {

		try{
			sm_weiboUIImage = new ImageSets("/weibo_full_image.imageset");
		}catch(Exception e){
			sm_mainApp.DialogAlertAndExit("weibo UI load Error:"+ e.getMessage() + e.getClass().getName());
		}
		
		if(sm_bubbleImage == null){
			sm_bubbleImage = new BubbleImage(
					weiboTimeLineScreen.sm_weiboUIImage.getImageUnit("bubble_top_left"),
					weiboTimeLineScreen.sm_weiboUIImage.getImageUnit("bubble_top"),
					weiboTimeLineScreen.sm_weiboUIImage.getImageUnit("bubble_top_right"),
					weiboTimeLineScreen.sm_weiboUIImage.getImageUnit("bubble_right"),
					
					weiboTimeLineScreen.sm_weiboUIImage.getImageUnit("bubble_bottom_right"),
					weiboTimeLineScreen.sm_weiboUIImage.getImageUnit("bubble_bottom"),
					weiboTimeLineScreen.sm_weiboUIImage.getImageUnit("bubble_bottom_left"),
					weiboTimeLineScreen.sm_weiboUIImage.getImageUnit("bubble_left"),
					
					weiboTimeLineScreen.sm_weiboUIImage.getImageUnit("bubble_inner_block"),
					new ImageUnit[]{
						weiboTimeLineScreen.sm_weiboUIImage.getImageUnit("bubble_left_point"),
						weiboTimeLineScreen.sm_weiboUIImage.getImageUnit("bubble_top_point"),
						weiboTimeLineScreen.sm_weiboUIImage.getImageUnit("bubble_right_point"),
						weiboTimeLineScreen.sm_weiboUIImage.getImageUnit("bubble_bottom_point"),
					},
					weiboTimeLineScreen.sm_weiboUIImage);
			
			sm_bubbleImage_black = new BubbleImage(
					weiboTimeLineScreen.sm_weiboUIImage.getImageUnit("bubble_black_top_left"),
					weiboTimeLineScreen.sm_weiboUIImage.getImageUnit("bubble_black_top"),
					weiboTimeLineScreen.sm_weiboUIImage.getImageUnit("bubble_black_top_right"),
					weiboTimeLineScreen.sm_weiboUIImage.getImageUnit("bubble_black_right"),
					
					weiboTimeLineScreen.sm_weiboUIImage.getImageUnit("bubble_black_bottom_right"),
					weiboTimeLineScreen.sm_weiboUIImage.getImageUnit("bubble_black_bottom"),
					weiboTimeLineScreen.sm_weiboUIImage.getImageUnit("bubble_black_bottom_left"),
					weiboTimeLineScreen.sm_weiboUIImage.getImageUnit("bubble_black_left"),
					
					weiboTimeLineScreen.sm_weiboUIImage.getImageUnit("bubble_black_inner_block"),
					new ImageUnit[]{
						weiboTimeLineScreen.sm_weiboUIImage.getImageUnit("bubble_black_left_point"),
						weiboTimeLineScreen.sm_weiboUIImage.getImageUnit("bubble_black_top_point"),
						weiboTimeLineScreen.sm_weiboUIImage.getImageUnit("bubble_black_right_point"),
						weiboTimeLineScreen.sm_weiboUIImage.getImageUnit("bubble_black_bottom_point"),
					},
					weiboTimeLineScreen.sm_weiboUIImage);
		}
	}
	
	private WeiboUserFindFactory	m_userfactory = new WeiboUserFindFactory(this);
	
	WeiboMainManager			m_mainMgr;
	WeiboMainManager			m_mainAtMeMgr;
	WeiboMainManager			m_mainCommitMeMgr;
	WeiboDMManager				m_mainDMMgr;
	
	WeiboMainManager			m_currMgr = null;
		
	private static ImageUnit[]	sm_VIPSign = 
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
		"sinaVIP",
		"sinaVIP",
		"qqVIP",
		
		"sinaVIP",
		"sinaVIP",
		"sinaVIP",
	};
	
	static ImageUnit		sm_headImageMask = null;
	
	static ImageUnit[]		sm_WeiboSign =
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
		"sinaWeibo",
		"tWeibo",
		"qqWeibo",
		
		"163Weibo",
		"sohuWeibo",
		"fanWeibo",
	};
	
	static ImageUnit		sm_isBBerSign = null;
	
	
	private Vector		m_headImageList = new Vector();
	
	static Bitmap		sm_defaultHeadImage = null;
		
	WeiboHeader 		m_weiboHeader		= new WeiboHeader(this);
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
					setTitle((Field)null);
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
	
	private final static int	fsm_promptTextWidth = fsm_promptBubbleWidth - fsm_promptBubbleBorder * 2;
	
	
	int					m_runnableTextShowID = -1;
	int					m_popupPromptTextTimer = 0;
	
	
	final class PromptTextField extends TextField {
		
		int m_width;
		
		public PromptTextField(boolean _small){
			super(Field.READONLY);
			if(_small){
				setFont(getFont().derive(getFont().getStyle(),getFont().getHeight() - 4));
				m_width = getFont().getAdvance("000/000 ");
			}else{
				m_width = fsm_promptTextWidth;
			}
		}
		public void setText(String _text){
			super.setText(_text);
			layout(m_width,1000);
		}
		
		public void paint(Graphics _g){
			super.paint(_g);
		}

	}
	
	public PromptTextField 	m_promptTextArea	= new PromptTextField(false);
	public PromptTextField m_inputTextNum		= new PromptTextField(true);

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

	public void setInputPromptText(String _prompt){
		m_inputTextNum.setText(_prompt);
		
		if(m_weiboHeaderShow){
			m_weiboHeader.invalidate();
		}
		
	}
	
	protected void paint(Graphics g){
		super.paint(g);

		if(m_promptTextArea.getText().length() != 0){
			
			paintPromptText(g,fsm_promptBubble_x,fsm_promptBubble_y,
					fsm_promptBubbleWidth,m_promptTextArea.getHeight() + fsm_promptBubbleBorder * 2,m_promptTextArea);
		}
		
		
		if(m_inputTextNum.getText().length() != 0){
			
			paintPromptText(g,recvMain.fsm_display_width - m_inputTextNum.getWidth() / 2 - fsm_promptBubbleBorder * 2 - 30,
					0,
					m_inputTextNum.getWidth() + fsm_promptBubbleBorder * 2 ,
					m_inputTextNum.getHeight() + fsm_promptBubbleBorder * 2,m_inputTextNum);
		}
	}
	
	public static void paintPromptText(Graphics g,int _x,int _y,int _width,int _height,PromptTextField _text){
		
		int t_color = g.getColor();
		try{
			g.setColor(WeiboItemField.fsm_promptTextBGColor);
        	g.fillRoundRect(_x, _y, _width, _height, fsm_promptBubbleArc, fsm_promptBubbleArc);
        	
        	g.setColor(WeiboItemField.fsm_promptTextBorderColor);
        	g.drawRoundRect(_x, _y, _width, _height, fsm_promptBubbleArc, fsm_promptBubbleArc);
			
		}finally{
			g.setColor(t_color);
		}
		
		boolean notEmpty = g.pushContext( _x + fsm_promptBubbleBorder , _y + fsm_promptBubbleBorder ,
										_width , _height, _x + fsm_promptBubbleBorder, _y + fsm_promptBubbleBorder);
        try {
            if( notEmpty ) {
            	_text.paint(g);
            }
        } finally {
            g.popContext();
            g.setColor(t_color);
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
			
			_initAdd = _initAdd || _weibo.IsOwnWeibo(); // don't prompt by own weibo...
			
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
				
				if(_weibo.GetWeiboClass() == fetchWeibo.TIMELINE_CLASS){
					m_mainApp.TriggerWeiboHomeNotification();					
				}else{
					m_mainApp.TriggerWeiboNotification();
				}
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
	
	public void AddWeibo(fetchWeibo _weibo,boolean _initAdd){
		
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
					
				},WeiboItemField.fsm_largeHeadImage?800:1000, true);
			}
		}
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
			String t_id = null;
			
			if(_weibo.GetWeiboStyle() == fetchWeibo.QQ_WEIBO_STYLE){
				t_id = _weibo.GetUserScreenName();
			}else{
				t_id = Long.toString(_weibo.GetUserId());
			}
			
			if(WeiboItemField.fsm_largeHeadImage){
				t_imageFilename = m_mainApp.GetWeiboHeadImageDir(_weibo.GetWeiboStyle()) + t_id + "_l.png";
			}else{
				t_imageFilename = m_mainApp.GetWeiboHeadImageDir(_weibo.GetWeiboStyle()) + t_id + ".png";
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
			
		}else if(m_currMgr == m_mainDMMgr && m_currMgr.getCurrExtendedItem() != null){
			
			// send direct message
			//
			String t_text = m_currMgr.m_editTextArea.getText();
			WeiboDMItemField t_field = (WeiboDMItemField)m_currMgr.getCurrExtendedItem();
					
			try{
				
				ByteArrayOutputStream t_os = new ByteArrayOutputStream();
				
				fetchWeibo t_weibo = t_field.getReplyWeibo();
				if(t_weibo != null){

					t_os.write(msg_head.msgWeibo);
					t_os.write(t_weibo.GetWeiboStyle());
					t_os.write(fetchWeibo.SEND_DIRECT_MSG_TYPE);
					sendReceive.WriteString(t_os,t_text);
					sendReceive.WriteString(t_os,t_weibo.GetUserScreenName());					
				}
				
				m_mainApp.m_connectDeamon.addSendingData(msg_head.msgWeibo,t_os.toByteArray(),true);
				m_mainApp.m_sentWeiboNum++;
				
			}catch(Exception e){
				m_mainApp.SetErrorString("SMIC0:" + e.getMessage() + e.getClass().getName());
			}
			
			m_currMgr.EscapeKey();
			
		}else{
			
			// send forward/reply message
			//
			try{
				
				String t_text = m_currMgr.m_editTextArea.getText();
				if(t_text.length() > WeiboItemField.fsm_maxWeiboTextLength){
					if(Dialog.ask(Dialog.D_YES_NO,recvMain.sm_local.getString(localResource.WEIBO_SEND_TEXT_CUTTED_PROMPT),Dialog.NO) != Dialog.YES){
						return;
					}
					
					t_text = t_text.substring(0,WeiboItemField.fsm_maxWeiboTextLength);
				}
				
				m_currMgr.BackupSendWeiboText(m_currMgr.m_currentSendType,m_currMgr.getCurrEditItem().m_weibo,t_text);
				
				if(m_currMgr.getCurrEditItem().m_weibo.GetWeiboClass() == fetchWeibo.COMMENT_ME_CLASS){
					
					if(m_currMgr.getCurrEditItem().m_weibo.GetCommentWeibo() != null){
						
						long t_orgId = 0;
						
						if(m_currMgr.getCurrEditItem().m_weibo.GetReplyWeiboId() != -1){
							t_orgId = m_currMgr.getCurrEditItem().m_weibo.GetReplyWeiboId();
						}else{
							t_orgId = m_currMgr.getCurrEditItem().m_weibo.GetCommentWeiboId();
						}
						
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
				m_mainApp.SetErrorString("SMIC1:" + e.getMessage() + e.getClass().getName());
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
    
    public WeiboUpdateDlg m_currUpdateDlg = new WeiboUpdateDlg(weiboTimeLineScreen.this);
    public boolean m_pushUpdateDlg = false;
    public MenuItem m_updateItem = new MenuItem(recvMain.sm_local.getString(localResource.WEIBO_UPDATE_DLG),m_menuIndex_op++,0){
        public void run() {        	
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
    
	
    MenuItem m_forwardWeiboItem = new MenuItem(recvMain.sm_local.getString(localResource.WEIBO_FORWARD_WEIBO_BUTTON_LABEL),m_menuIndex_op++,0){
		public void run(){
			m_currMgr.ForwardWeibo(m_currMgr.getCurrSelectedItem());
		}
	};
        
	MenuItem m_atWeiboItem	= new MenuItem(recvMain.sm_local.getString(localResource.WEIBO_AT_WEIBO_BUTTON_LABEL),m_menuIndex_op++,0){
		public void run(){
			m_currMgr.AtWeibo(m_currMgr.getCurrSelectedItem());
		}
	};
	
	MenuItem m_favWeiboItem = new MenuItem(recvMain.sm_local.getString(localResource.WEIBO_FAVORITE_WEIBO_BUTTON_LABEL),m_menuIndex_op++,0){
    	public void run(){
    		m_currMgr.FavoriteWeibo(m_currMgr.getCurrSelectedItem());
    	}
    };
    
	MenuItem m_picWeiboItem = new MenuItem(recvMain.sm_local.getString(localResource.WEIBO_CHECK_PICTURE_LABEL),m_menuIndex_op++,0){
		public void run(){
			m_currMgr.OpenOriginalPic(m_currMgr.getCurrSelectedItem());
		}
	};   
    
    MenuItem m_deleteItem = new MenuItem(recvMain.sm_local.getString(localResource.WEIBO_DELETE_MENU_LABEL),m_menuIndex_op++,0){
        public void run() {
        	deleteWeiboItem();
        }
    };
    
    public WeiboOptionScreen m_optionScreen = null;
    MenuItem m_optionItem = new MenuItem(recvMain.sm_local.getString(localResource.WEIBO_OPTION_MENU_LABEL),98,0){
    	public void run() {
        	if(m_optionScreen == null){
        		m_optionScreen = new WeiboOptionScreen(m_mainApp);
        	}
        	m_mainApp.pushScreen(m_optionScreen);
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
		
		if(m_currMgr != m_mainDMMgr){
			_menu.add(m_topItem);
			_menu.add(m_bottomItem);
		}	
		
		if(m_currMgr.getCurrExtendedItem() != null && m_currMgr != m_mainDMMgr){
			if(m_currMgr.getCurrEditItem() == null){
				_menu.add(m_preWeiboItem);
				_menu.add(m_nextWeiboItem);
			}
		}
		
		if(m_currMgr.getCurrEditItem() == null && !m_pushUpdateDlg){
			_menu.add(m_updateItem);
		}
		
		if(m_currMgr.getCurrSelectedItem() != null && m_currMgr != m_mainDMMgr){
			
			if(m_currMgr.getCurrEditItem() == null){
				_menu.add(m_forwardWeiboItem);
				_menu.add(m_atWeiboItem);
				
				_menu.setDefault(m_forwardWeiboItem);
			}
			
			_menu.add(m_favWeiboItem);

			if(m_currMgr.getCurrSelectedItem().m_weiboPic != null){
				_menu.add(m_picWeiboItem);
			}
		}		
		
		if(m_currMgr.getCurrSelectedItem() != null				// has selected
		&& m_currMgr.getCurrSelectedItem().m_weibo != null		// is not update weibo
		&& m_currMgr.getCurrSelectedItem().m_weibo.IsOwnWeibo()
		&& m_currMgr != m_mainDMMgr){ // is own weibo
			_menu.add(m_deleteItem);
		}
		
		_menu.add(MenuItem.separator(50));
		_menu.add(m_optionItem);
		_menu.add(m_helpItem);
		_menu.add(m_stateItem);
		
		super.makeMenu(_menu,instance);
    }

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
    
    public void checkUserInfo(String _screenName){
    	
    	WeiboItemField t_field = m_currMgr.getCurrExtendedItem();
    	
    	if(t_field != null && _screenName.length() != 0){
    		
	    	try{
				
				ByteArrayOutputStream t_os = new ByteArrayOutputStream();
				t_os.write(msg_head.msgWeiboUser);
				t_os.write(t_field.m_weibo.GetWeiboStyle());
	
				sendReceive.WriteString(t_os,_screenName);
				
				weiboTimeLineScreen.sm_mainApp.m_connectDeamon.addSendingData(
									msg_head.msgWeiboUser, t_os.toByteArray(),true);
				
			}catch(Exception e){
				weiboTimeLineScreen.sm_mainApp.SetErrorString("CUI:" + e.getMessage() + e.getClass().getName());
			}
    	}
    }
    
    public void followUser(String _screenName){
		
    	WeiboItemField t_field = m_currMgr.getCurrExtendedItem();
    	
		if(t_field != null && _screenName.length() != 0){
			
			try{
				
				ByteArrayOutputStream t_os = new ByteArrayOutputStream();
				t_os.write(msg_head.msgWeiboFollowUser);
				t_os.write(t_field.m_weibo.GetWeiboStyle());

				sendReceive.WriteString(t_os,_screenName);
				
				weiboTimeLineScreen.sm_mainApp.m_connectDeamon.addSendingData(
						msg_head.msgWeiboFollowUser, t_os.toByteArray(),true);
				
			}catch(Exception e){
				weiboTimeLineScreen.sm_mainApp.SetErrorString("FCU:" + e.getMessage() + e.getClass().getName());
			}
		}
	}

    private WeiboUserInfoScreen m_userInfoScreen = null;
    public void displayWeiboUser(final fetchWeiboUser _user){
    	
    	if(m_userInfoScreen == null){
    		m_userInfoScreen = new WeiboUserInfoScreen(m_mainApp);
    	}
    	
    	m_mainApp.invokeLater(new Runnable() {
			public void run() {
				
		    	m_userInfoScreen.setWeiboUser(_user);
		    	
		    	if(m_mainApp.getActiveScreen() != m_userInfoScreen){
	    			
					m_mainApp.pushScreen(m_userInfoScreen);
					m_userInfoScreen.updateDisplay();
				}   		
	    	}				
    	}); 
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
	    	case 'T':		    		
    			m_topItem.run();
    			return true;
	    	case 'B':
    			m_bottomItem.run();	
    			return true;
    			
	    	case 'F':
	    		m_forwardWeiboItem.run();
	    		return true;
	    	case 'V':
	    		m_favWeiboItem.run();
	    		return true;
	    	case 'E':
	    		m_atWeiboItem.run();
	    		return true;
	    	case 'G':
	    		m_picWeiboItem.run();
	    		return true;
	    	case 'D':
	    		m_deleteItem.run();
	    		return true;
			}
			
			if(m_currMgr.getCurrExtendedItem() != null && m_currMgr != m_mainDMMgr){
				switch(key){
				case '0':
		    		//boolean t_shiftDown = (Keypad.status(keycode) & KeypadListener.STATUS_SHIFT) != 0;
		    		m_currMgr.OpenNextWeiboItem(true);
		    		return true;
				case ' ':
		    		m_currMgr.OpenNextWeiboItem(false);
		    		return true;
				}
			}else{
				
				if(m_currMgr.getCurrExtendedItem() == null){
					switch(key){
			    	case 'S':
			    		m_stateItem.run();
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
		m_mainApp.StopWeiboHomeNotification();
		
	}
	
	protected boolean navigationClick(int status, int time){
		return m_currMgr.Clicked(status,time);		
	}
	
	static public ImageUnit GetWeiboSign(int _weiboStyle){
		if(sm_WeiboSign[_weiboStyle] == null){
			sm_WeiboSign[_weiboStyle] = sm_weiboUIImage.getImageUnit(sm_weiboSignFilename[_weiboStyle]);
		}
		
		return sm_WeiboSign[_weiboStyle];
	}
	
	static public ImageUnit GetVIPSignBitmap(int _weiboStyle){
		
		if(sm_VIPSign[_weiboStyle] == null){
			sm_VIPSign[_weiboStyle] = sm_weiboUIImage.getImageUnit(sm_VIPSignString[_weiboStyle]);
		}
	
		return sm_VIPSign[_weiboStyle];		
	}
	
	static public ImageUnit GetHeadImageMaskBitmap(){
		if(sm_headImageMask == null){
			sm_headImageMask = sm_weiboUIImage.getImageUnit(WeiboItemField.fsm_largeHeadImage?"headImageMask_l":"headImageMask");				
		}
		
		return sm_headImageMask;
	}
	
	static public ImageUnit GetBBerSignBitmap(){
		if(sm_isBBerSign == null){
			sm_isBBerSign = sm_weiboUIImage.getImageUnit("BBSign");		
		}
		
		return sm_isBBerSign;
	}
	
	private static ImageUnit sm_weiboSelectedImage = null;
	private static ImageUnit sm_weiboPicSignImage = null;
	private static ImageUnit sm_weiboCommentSignImage = null;
	
	static public ImageUnit getWeiboSelectedImage(){
		if(sm_weiboSelectedImage == null){
			sm_weiboSelectedImage = sm_weiboUIImage.getImageUnit("weibo_sel");
		}
		return sm_weiboSelectedImage;
	}
	static public ImageUnit getWeiboPicSignImage(){
		if(sm_weiboPicSignImage == null){
			sm_weiboPicSignImage = sm_weiboUIImage.getImageUnit("picSign");
		}
		
		return sm_weiboPicSignImage;
	}
	static public ImageUnit getWeiboCommentSignImage(){
		if(sm_weiboCommentSignImage == null){
			sm_weiboCommentSignImage = sm_weiboUIImage.getImageUnit("commentSign");
		}
		
		return sm_weiboCommentSignImage;
	}
	
	// 
	//

}
