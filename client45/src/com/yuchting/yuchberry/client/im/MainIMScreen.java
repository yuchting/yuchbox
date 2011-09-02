package com.yuchting.yuchberry.client.im;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Vector;

import local.localResource;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.yuchting.yuchberry.client.msg_head;
import com.yuchting.yuchberry.client.recvMain;
import com.yuchting.yuchberry.client.sendReceive;
import com.yuchting.yuchberry.client.ui.BubbleTextField;
import com.yuchting.yuchberry.client.ui.SliderHeader;
import com.yuchting.yuchberry.client.ui.WeiboHeadImage;


final class RosterChatData{
	
	public fetchChatRoster		m_roster;
	public Vector				m_chatMsgList = new Vector();
	
	public String				m_lastChatText = "";
	
	public RosterChatData(fetchChatRoster _roster){
		m_roster = _roster;
	}
}

public class MainIMScreen extends MainScreen{
	
	int m_menu_label = 0;
	
	MenuItem	m_historyChatMenu = new MenuItem(recvMain.sm_local.getString(localResource.IM_HISTORY_MENU_LABEL),m_menu_label++,0){
		public void run(){
			m_header.setCurrState(MainIMScreenHeader.STATE_HISTORY_CHAT);
			refreshHeader();
		}
	};
	
	MenuItem	m_rosterListMenu = new MenuItem(recvMain.sm_local.getString(localResource.IM_ROSTER_LIST_MENU_LABEL),m_menu_label++,0){
		public void run(){
			m_header.setCurrState(MainIMScreenHeader.STATE_ROSTER_LIST);
			refreshHeader();
		}
	};
	
	int m_menu_op = 20;
	
	MenuItem	m_refreshListMenu = new MenuItem(recvMain.sm_local.getString(localResource.IM_REFRESH_ROSTER_MENU_LABEL),m_menu_label++,0){
		public void run(){
			sendRequestRosterListMsg();
		}
	};
	
	MenuItem	m_stateMenu = new MenuItem(recvMain.sm_local.getString(localResource.WEIBO_STATE_SCREEN_MENU_LABEL),100,0){
		public void run(){
			recvMain t_recv = (recvMain)UiApplication.getUiApplication();
        	
        	t_recv.popScreen(MainIMScreen.this);
        	t_recv.pushStateScreen();
		}
	};

	// BasicEditField for 4.2os
	public static BubbleTextField 	sm_testTextArea	= new BubbleTextField(Field.READONLY);
	
	public final static Font		sm_defaultFont			= sm_testTextArea.getFont();
	public final static int		sm_defaultFontHeight	= sm_defaultFont.getHeight(); 
	
	public final static Font		sm_boldFont				= sm_defaultFont.derive(sm_defaultFont.getStyle() | Font.BOLD);
		
	Vector					m_headImageList = new Vector();
		
	
	MainIMScreenHeader		m_header = null;
	recvMain				m_mainApp = null;
	
	VerticalFieldManager	m_historyChatMgr = new VerticalFieldManager(Manager.VERTICAL_SCROLL | Manager.VERTICAL_SCROLLBAR);
	Vector					m_historyChatMgrList = new Vector();
	
	VerticalFieldManager	m_rosterListMgr = new VerticalFieldManager(Manager.VERTICAL_SCROLL | Manager.VERTICAL_SCROLLBAR);
	Vector					m_rosterChatDataList = new Vector();
	
	VerticalFieldManager	m_currMgr	= null;	
	
	boolean				m_isRequestRoster  = false;
	
	public MainChatScreen	m_chatScreen = null;
	
		
	public MainIMScreen(recvMain _mainApp){
		super(Manager.VERTICAL_SCROLL);
		
		m_mainApp = _mainApp;
		m_header = new MainIMScreenHeader(this);
		m_chatScreen = new MainChatScreen(_mainApp,this);
		
//		//@{ test code
//		for(int i = 0 ;i < 10;i++){
//			fetchChatRoster t_roster = new fetchChatRoster();
//			
//			t_roster.setAccount("yuchting@gmail.com");
//			t_roster.setName("YuchTing");
//			t_roster.setStyle(fetchChatMsg.STYLE_GTALK);
//			
//			try{
//				WeiboHeadImage t_image = WeiboHeadImage.SearchHeadImage(m_headImageList, t_roster.getAccount(), (byte)t_roster.getStyle(), 
//						t_roster.getHeadImageHashCode(), false);
//				
//				m_historyChatMgr.add(new RosterItemField(t_roster, t_image, true));
//				
//			}catch(Exception e){
//				m_mainApp.SetErrorString("MIMS:"+e.getMessage() + e.getClass().getName());
//			}
//			
//			m_historyChatMgrList.addElement(t_roster);
//			
//			StringBuffer t_msgText = new StringBuffer("笑多了[哈哈]");
//			RosterChatData t_data = new RosterChatData(t_roster);
//			for(int j = 0 ;j < 10;j++){
//				
//				fetchChatMsg t_msg = new fetchChatMsg();
//				t_msg.setOwner(t_roster.getAccount());
//				
//				
//				t_msg.setMsg(t_msgText.toString());
//				
//				t_msgText.append("笑多了[哈哈]");				
//				
//				t_msg.setIsOwnMsg(j % 2 == 0);
//				
//				t_data.m_chatList.addElement(t_msg);
//				
//			}
//			m_rosterChatDataList.addElement(t_data);
//		}
//		//@}		
		
		add(m_historyChatMgr);
		setTitle(m_header);
		
		m_historyChatMgr.setFocus();
		m_currMgr = m_historyChatMgr;
	}
	
	 
	public SliderHeader getHeader(){
		return m_header;
	}
	
	public boolean onClose(){
		
		if(m_mainApp.m_connectDeamon.IsConnectState()){
    		
			m_mainApp.requestBackground();
			m_mainApp.m_isWeiboOrIMScreen = false;
    		return false;
    		
    	}else{
    		
    		m_stateMenu.run();
    		
    		return false;
    	}
	}
	
	protected void makeMenu(Menu _menu,int instance){
		_menu.add(m_historyChatMenu);
		_menu.add(m_rosterListMenu);
		_menu.add(MenuItem.separator(m_menu_label));
		
		_menu.add(m_refreshListMenu);
		_menu.add(MenuItem.separator(m_menu_op));
		
		_menu.add(m_stateMenu);
		
		super.makeMenu(_menu,instance);
	}
	
	protected boolean navigationMovement(int dx,int dy,int status,int time){
		if(dx != 0){
			m_header.setCurrState(m_header.getCurrState() + dx);
			refreshHeader();
			return true;
		}
				
		return 	super.navigationMovement(dx, dy, status, time);
		
	}
	
	private RosterChatData searchRosterChatData(String _acc){
		for(int i = 0 ;i < m_rosterChatDataList.size();i++){
			RosterChatData t_data = (RosterChatData)m_rosterChatDataList.elementAt(i);
			if(t_data.m_roster.getAccount().equals(_acc)){
				return t_data;
			}
		}
		
		return null;
	}
	
	protected boolean navigationClick(int status, int time){
		
		if(click()){
			return true;
		}
		
		return super.navigationClick(status, time);
	}
	
	protected boolean keyDown(int keycode,int time){
		final int key = Keypad.key(keycode);
		if(key == 10 && click()){
			return true;
		}
		return super.keyDown(keycode,time);
	}
	
	private boolean click(){

		int t_fieldCount = m_currMgr.getFieldCount();
		for(int i = 0 ;i < t_fieldCount;i++){
			RosterItemField field = (RosterItemField)m_currMgr.getField(i);
			if(field.isFocus()){
				
				RosterChatData t_data = searchRosterChatData(field.m_currRoster.getAccount());
				
				if(t_data != null){
					m_chatScreen.prepareChatScreen(t_data);
					m_mainApp.pushScreen(m_chatScreen);
					
					return true;
				}
				
				break;
				
			}
		}
		
		return false;
	}
	
	private void refreshHeader(){
		
		switch(m_header.getCurrState()){
		case MainIMScreenHeader.STATE_HISTORY_CHAT:
			if(m_currMgr != m_historyChatMgr){
				replace(m_currMgr,m_historyChatMgr);
				m_currMgr = m_historyChatMgr;
			}else{
				return;
			}
			break;
		case MainIMScreenHeader.STATE_ROSTER_LIST:
			if(m_currMgr != m_rosterListMgr){
				replace(m_currMgr,m_rosterListMgr);
				m_currMgr = m_rosterListMgr;
				
				if(!m_isRequestRoster){
					sendRequestRosterListMsg();
				}
			}else{
				return;
			}
			break;
		}
	}
	
	public void processChatMsg(InputStream in)throws Exception{
		final fetchChatMsg t_msg = new fetchChatMsg();
		t_msg.Import(in);
		
		sendChatConfirmMsg(t_msg);
	
		m_mainApp.invokeLater(new Runnable() {
			public void run() {
				addChatMsg(t_msg);
			}
		});
	}
	
	private void addChatMsg(fetchChatMsg _msg){
		
		synchronized (m_rosterChatDataList) {
			for(int i = 0;i < m_rosterChatDataList.size();i++){
				RosterChatData data = (RosterChatData)m_rosterChatDataList.elementAt(i);
				
				if(data.m_roster.getAccount().equals(_msg.getOwner())
				&& data.m_roster.getStyle() == _msg.getStyle()){
					
					data.m_chatMsgList.addElement(_msg);
					
					if(m_chatScreen.m_currRoster == data){
						m_chatScreen.m_middleMgr.addChatMsg(_msg);
					}
					
					break;
				}
			}	
		}
	}
	
	private void sendChatConfirmMsg(fetchChatMsg _msg){
		try{
			
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			os.write(msg_head.msgChatConfirm);
			os.write(_msg.getStyle());
			sendReceive.WriteInt(os,_msg.hashCode());
			
			m_mainApp.m_connectDeamon.addSendingData(msg_head.msgChatConfirm,os.toByteArray(),true);
			
			os.close();
			os = null;
			
		}catch(Exception e){
			m_mainApp.SetErrorString("SCCM:"+e.getMessage()+e.getClass().getName());
		}
		
	}
	
	public void processChatRosterList(final InputStream in){
		
		m_mainApp.invokeLater(new Runnable() {
			
			public void run() {
				try{
					int t_type = in.read();
					switch(t_type){
					case 0:
						m_isRequestRoster = true;
						
						// all list
						//
						synchronized (m_rosterChatDataList) {
							m_rosterChatDataList.removeAllElements();

							int t_num = sendReceive.ReadInt(in);
							for(int i = 0 ;i < t_num;i++){
								fetchChatRoster t_roster = new fetchChatRoster();
								t_roster.Import(in);
								
								m_rosterChatDataList.addElement(new RosterChatData(t_roster));
								
								WeiboHeadImage t_image = WeiboHeadImage.SearchHeadImage(m_headImageList, t_roster.getAccount(), 
										(byte)t_roster.getStyle(), 
										t_roster.getHeadImageHashCode(), false);

								m_rosterListMgr.add(new RosterItemField(t_roster, t_image, true));
							}
	
						}
												
						refreshRosterList();
						
						m_historyChatMgrList.removeAllElements();
						m_historyChatMgr.deleteAll();
						
						break;
					case 1:
						fetchChatRoster t_roster = new fetchChatRoster();
						t_roster.Import(in);
						
						boolean t_modified = false;
						
						synchronized (m_rosterChatDataList) {
							for(int i = 0 ;i < m_rosterChatDataList.size();i++){
								RosterChatData data = (RosterChatData)m_rosterChatDataList.elementAt(i);
								
								if(data.m_roster.equals(t_roster)){
									data.m_roster.copyFrom(t_roster);
									
									t_modified = true;
									
									break;
								}
							}	
						}
						
						
						if(t_modified){
							
							m_currMgr.invalidate();
							
						}else{
							
							synchronized (m_rosterChatDataList) {
								m_rosterChatDataList.addElement(new RosterChatData(t_roster));
							}
							
							refreshRosterList();
						}
						break;
					}
				}catch(Exception e){
					m_mainApp.SetErrorString("PCRL:"+e.getMessage()+e.getClass().getName());
				}
				
			}
		});
		
	}
	
	private void refreshRosterList()throws Exception{
		
		m_rosterListMgr.deleteAll();
		
		refreshRosterList_impl(fetchChatRoster.PRESENCE_AVAIL);
		refreshRosterList_impl(fetchChatRoster.PRESENCE_AWAY);
		refreshRosterList_impl(fetchChatRoster.PRESENCE_BUSY);
		refreshRosterList_impl(fetchChatRoster.PRESENCE_FAR_AWAY);
		refreshRosterList_impl(fetchChatRoster.PRESENCE_UNAVAIL);
		
	}
	
	private void refreshRosterList_impl(int _presence)throws Exception{
		
		synchronized (m_rosterChatDataList) {
			
			for(int i = 0 ;i < m_rosterChatDataList.size();i++){
				RosterChatData t_rosterData = (RosterChatData)m_rosterChatDataList.elementAt(i);
				if(t_rosterData.m_roster.getPresence() == _presence){

					WeiboHeadImage t_image = WeiboHeadImage.SearchHeadImage(m_headImageList, t_rosterData.m_roster.getAccount(), 
																			(byte)t_rosterData.m_roster.getStyle(), 
																			t_rosterData.m_roster.getHeadImageHashCode(), false);
					
					m_rosterListMgr.add(new RosterItemField(t_rosterData.m_roster, t_image, true));		
				}
			}	
		}
		
	}
	
	private void sendRequestRosterListMsg(){
		
		try{
			m_mainApp.m_connectDeamon.addSendingData(msg_head.msgChatRosterList, new byte[]{msg_head.msgChatRosterList},true);
		}catch(Exception e){
			m_mainApp.SetErrorString("SRRLM:"+e.getMessage()+e.getClass().getName());
		}
		
	}
	

}
