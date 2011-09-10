package com.yuchting.yuchberry.client.im;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.Vector;

import local.localResource;
import net.rim.device.api.system.Backlight;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.UiEngine;
import net.rim.device.api.ui.component.Dialog;
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
	public int					m_currChatState;
	
	boolean					m_isYuch = false;
	
	public RosterChatData(fetchChatRoster _roster){
		m_roster = _roster;
		m_isYuch = m_roster.getSource().indexOf(MainIMScreen.fsm_YuchBerrySource) != -1;
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
	
	MenuItem	m_statusListMenu = new MenuItem(recvMain.sm_local.getString(localResource.IM_STATUS_LIST_MENU_LABEL),m_menu_label++,0){
		public void run(){
			m_header.setCurrState(MainIMScreenHeader.STATE_STATUS_LIST);
			refreshHeader();
		}
	};
	
	int m_menu_op = 20;
	
	long m_refreshRosterTimer = 0;
	MenuItem	m_refreshListMenu = new MenuItem(recvMain.sm_local.getString(localResource.IM_REFRESH_ROSTER_MENU_LABEL),m_menu_op++,0){
		public void run(){
			sendRequestRosterListMsg();
		}
	};
	
	MenuItem	m_showUnvailRosterMenu = new MenuItem(recvMain.sm_local.getString(localResource.IM_SHOW_UNVAIL_ROSTER_MENU_LABEL),m_menu_op++,0){
		public void run(){
			m_mainApp.m_hideUnvailiableRoster = false;
			
			try{
				refreshRosterList();
			}catch(Exception e){
				m_mainApp.SetErrorString("SURM:"+e.getMessage()+e.getClass().getName());
			}
			
			m_mainApp.WriteReadIni(false);
		}
	};
	
	MenuItem	m_hideUnvailRosterMenu = new MenuItem(recvMain.sm_local.getString(localResource.IM_HIDE_UNVAIL_ROSTER_MENU_LABEL),m_menu_op++,0){
		public void run(){
			m_mainApp.m_hideUnvailiableRoster = true;
			
			try{
				refreshRosterList();
			}catch(Exception e){
				m_mainApp.SetErrorString("HURM:"+e.getMessage()+e.getClass().getName());
			}
			
			m_mainApp.WriteReadIni(false);
		}
	};
	public IMStatusAddScreen m_statusAddScreen = null;
	MenuItem	m_addStatusMenu = new MenuItem(recvMain.sm_local.getString(localResource.IM_ADD_STATUS),m_menu_op++,0){
		public void run(){
			m_statusAddScreen = new IMStatusAddScreen(MainIMScreen.this, null);
			m_mainApp.pushScreen(m_statusAddScreen);
			
		}
	};
	
	MenuItem	m_modifyStatusMenu = new MenuItem(recvMain.sm_local.getString(localResource.IM_MODIFY_STATUS),m_menu_op++,0){
		public void run(){
			
			int num = m_statusListMgr.getFieldCount();
			for(int i = 0;i < num;i++){
				IMStatusField t_field = (IMStatusField)m_statusListMgr.getField(i);
				if(t_field.isFocus()){
					
					m_statusAddScreen = new IMStatusAddScreen(MainIMScreen.this, t_field.m_status);
					m_mainApp.pushScreen(m_statusAddScreen);
					
					break;
				}
			}
		}
	};
	
	MenuItem	m_delStatusMenu = new MenuItem(recvMain.sm_local.getString(localResource.IM_DELETE_STATUS),m_menu_op++,0){
		public void run(){
			
			int num = m_statusListMgr.getFieldCount();
			for(int i = 0;i < num;i++){
				IMStatusField t_field = (IMStatusField)m_statusListMgr.getField(i);
				if(t_field.isFocus()){
					
					if(Dialog.ask(Dialog.D_YES_NO,
						recvMain.sm_local.getString(localResource.IM_DELETE_STATUS_PROMPT) + t_field.m_status,Dialog.NO) != Dialog.YES){
						
						return;
					}
					
					for(int j = 0;j< recvMain.sm_imStatusList.size();j++){
						
						IMStatus t_status = (IMStatus)recvMain.sm_imStatusList.elementAt(j);
						
						if(t_field.m_status == t_status){
							
							recvMain.sm_imStatusList.removeElementAt(j);
							
							if(IMStatus.sm_currUseStatus == t_status){
								
								m_mainApp.m_imCurrUseStatusIndex = 0;
								
								if(!recvMain.sm_imStatusList.isEmpty()){
									IMStatus.sm_currUseStatus = (IMStatus)recvMain.sm_imStatusList.elementAt(0);
									
									sendUseStatus(IMStatus.sm_currUseStatus);
								}
							}
							
							m_mainApp.WriteReadIni(false);
							
							refreshStatusList();
							
							break;
							
						}
					}
					
					break;
				}
			}
		}
	};
		
	
	MenuItem	m_stateMenu = new MenuItem(recvMain.sm_local.getString(localResource.WEIBO_STATE_SCREEN_MENU_LABEL),100,0){
		public void run(){
			recvMain t_recv = (recvMain)UiApplication.getUiApplication();
        	
        	t_recv.popScreen(MainIMScreen.this);
        	t_recv.pushStateScreen();
		}
	};
	
	  
    MenuItem m_weiboScreenItem = new MenuItem(recvMain.sm_local.getString(localResource.YB_WEIBO_MENU_LABEL),101,0){
        public void run() {
        	        	
        	if(m_mainApp.m_enableIMModule){
        		
        		recvMain t_recv = (recvMain)UiApplication.getUiApplication();
            	t_recv.popScreen(MainIMScreen.this);
            	
            	t_recv.PopupWeiboScreen();		
        	}
        }
    };

	// BasicEditField for 4.2os
	public final static BubbleTextField 	fsm_testTextArea	= new BubbleTextField(Field.READONLY);
	
	public final static Font		fsm_defaultFont			= fsm_testTextArea.getFont();
	public final static int		fsm_defaultFontHeight	= fsm_defaultFont.getHeight();
	
	public final static String	fsm_YuchBerrySource		= "YuchBerry.info";
	public final static Font		fsm_boldFont			= fsm_defaultFont.derive(fsm_defaultFont.getStyle() | Font.BOLD);
		
	public Vector					m_headImageList = new Vector();

	MainIMScreenHeader		m_header = null;
	recvMain				m_mainApp = null;
	
	VerticalFieldManager	m_historyChatMgr = new VerticalFieldManager(Manager.VERTICAL_SCROLL | Manager.VERTICAL_SCROLLBAR);
	VerticalFieldManager	m_rosterListMgr = new VerticalFieldManager(Manager.VERTICAL_SCROLL | Manager.VERTICAL_SCROLLBAR);
	VerticalFieldManager	m_statusListMgr = new VerticalFieldManager(Manager.VERTICAL_SCROLL | Manager.VERTICAL_SCROLLBAR);
		
	Vector					m_rosterChatDataList = new Vector();
	
	VerticalFieldManager	m_currMgr	= null;	
	
	Vector					m_sendChatDeamon = new Vector();
	
	boolean				m_isRequestRoster  = false;
	
	public MainChatScreen	m_chatScreen = null;
	public IMPromptDlg 		m_promptDlg = new IMPromptDlg(this);
	Vector					m_promptQueue = new Vector();
	
	public boolean			m_hasNewChatMsg = false;
	
	public	Field	m_currFocusRosterItemField = null;
	public	Field	m_currFocusHistoryRosterItemField = null;
	public Field	m_currFocusStatusField = null;
	
		
	public MainIMScreen(recvMain _mainApp){
		super(Manager.VERTICAL_SCROLL);
		
		m_mainApp = _mainApp;
		m_header = new MainIMScreenHeader(this);
		m_chatScreen = new MainChatScreen(_mainApp,this);
		
		add(m_historyChatMgr);
		setTitle(m_header);
		
		m_historyChatMgr.setFocus();
		m_currMgr = m_historyChatMgr;
		
		refreshStatusList();
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
	
	private void prepareCurrUseStatus(){
		if(!recvMain.sm_imStatusList.isEmpty()){
			if(m_mainApp.m_imCurrUseStatusIndex < 0 
				|| m_mainApp.m_imCurrUseStatusIndex >= recvMain.sm_imStatusList.size()){
				
				m_mainApp.m_imCurrUseStatusIndex = 0;
			}
			
			IMStatus.sm_currUseStatus = (IMStatus)recvMain.sm_imStatusList.elementAt(m_mainApp.m_imCurrUseStatusIndex);
		}
	}
	
	public void refreshStatusList(){
		
		prepareCurrUseStatus();
		
		m_statusListMgr.deleteAll();
		m_currFocusStatusField = null;
		
		for(int i = 0 ;i < recvMain.sm_imStatusList.size();i++){
			m_statusListMgr.add(new IMStatusField((IMStatus)recvMain.sm_imStatusList.elementAt(i)));
		}
	}
	
	public void addStatus(IMStatus _status){
		
		boolean insert = false;
		
		for(int i = 0 ;i < recvMain.sm_imStatusList.size();i++){
			IMStatus t_status = (IMStatus)recvMain.sm_imStatusList.elementAt(i); 
			if(t_status.m_presence == _status.m_presence){
				
				insert = true;
				
				recvMain.sm_imStatusList.insertElementAt(_status, i);
				
				if(i > m_mainApp.m_imCurrUseStatusIndex){
					// needn't change status index;
					//
					refreshStatusList();
					m_mainApp.WriteReadIni(false);
					return ;
				}else{
					break;
				}
				
			}			
		}
		
		if(!insert){
			
			recvMain.sm_imStatusList.addElement(_status);
			
		}else{

			// find the default status index
			//
			for(int i = 0 ;i < recvMain.sm_imStatusList.size();i++){
				IMStatus t_status = (IMStatus)recvMain.sm_imStatusList.elementAt(i); 
				if(t_status == IMStatus.sm_currUseStatus){
					m_mainApp.m_imCurrUseStatusIndex = i;
					break;
				}
			}
		}
		
		refreshStatusList();
		m_mainApp.WriteReadIni(false);
	}
	
	protected void makeMenu(Menu _menu,int instance){
		_menu.add(m_historyChatMenu);
		_menu.add(m_rosterListMenu);
		_menu.add(m_statusListMenu);
		_menu.add(MenuItem.separator(m_menu_label));
		
		_menu.add(m_refreshListMenu);
		if(m_currMgr == m_rosterListMgr){

			if(m_mainApp.m_hideUnvailiableRoster){
				_menu.add(m_showUnvailRosterMenu);
			}else{
				_menu.add(m_hideUnvailRosterMenu);
			}
		}else if(m_currMgr == m_statusListMgr){
			_menu.add(m_addStatusMenu);
			_menu.add(m_modifyStatusMenu);
			_menu.add(m_delStatusMenu);
		}
		
		_menu.add(MenuItem.separator(m_menu_op));
		
		_menu.add(m_stateMenu);
		if(m_mainApp.m_enableWeiboModule){
			_menu.add(m_weiboScreenItem);
		}
		
		
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
	
	protected boolean navigationClick(int status, int time){
		
		if(click()){
			return true;
		}
		
		return super.navigationClick(status, time);
	}
	
	protected boolean keyDown(int keycode,int time){
		
		final int key = Keypad.key(keycode);
		switch(key){
		case 'H':
			m_historyChatMenu.run();
			return true;
		case 'J':
			m_rosterListMenu.run();
			return true;
		case 'K':
			m_statusListMenu.run();
			return true;
		case 'S':
			m_stateMenu.run();
			return true;
		case 'W':
			if(m_mainApp.m_enableIMModule){
				m_weiboScreenItem.run();
				return true;
			}
			break;
		}
		
		if(key == 10 && click()){
			return true;
		}
		
		return super.keyDown(keycode,time);
	}
	
	private boolean click(){

		if(m_currMgr == m_statusListMgr){
			
			int t_fieldCount = m_currMgr.getFieldCount();
			
			for(int i = 0 ;i < t_fieldCount;i++){
				
				IMStatusField field = (IMStatusField)m_currMgr.getField(i);
				
				if(field.isFocus() && m_mainApp.m_imCurrUseStatusIndex != i){
					
					m_mainApp.m_imCurrUseStatusIndex = i;
					IMStatus.sm_currUseStatus = field.m_status;
					
					sendUseStatus(IMStatus.sm_currUseStatus);
					
					refreshStatusList();
					
					return true;					
				}
			}
			
		}else{
			
			int t_fieldCount = m_currMgr.getFieldCount();
			for(int i = 0 ;i < t_fieldCount;i++){
				RosterItemField field = (RosterItemField)m_currMgr.getField(i);
				if(field.isFocus()){
								
					m_chatScreen.m_currRoster = field.m_currRoster;
					m_mainApp.pushScreen(m_chatScreen);
					
					return true;
					
				}
			}	
		}
		
		
		return false;
	}
	
	public void clearNewChatSign(){
		if(m_hasNewChatMsg){
			m_hasNewChatMsg = false;
			m_header.invalidate();
		}
	}
	
	private void refreshHeader(){
		
		if(m_currMgr == m_historyChatMgr){
			m_currFocusHistoryRosterItemField = m_currMgr.getFieldWithFocus();
		}else if(m_currMgr == m_rosterListMgr){
			m_currFocusRosterItemField = m_currMgr.getFieldWithFocus();
		}else if(m_currMgr == m_statusListMgr){
			m_currFocusStatusField = m_currMgr.getFieldWithFocus();
		}		
		
		switch(m_header.getCurrState()){
		case MainIMScreenHeader.STATE_HISTORY_CHAT:
			if(m_currMgr != m_historyChatMgr){
				replace(m_currMgr,m_historyChatMgr);
				m_currMgr = m_historyChatMgr;
				
				clearNewChatSign();
			}else{
				return;
			}
			break;
		case MainIMScreenHeader.STATE_ROSTER_LIST:
			if(m_currMgr != m_rosterListMgr){
				replace(m_currMgr,m_rosterListMgr);
				m_currMgr = m_rosterListMgr;
				
				if(!m_isRequestRoster){
					m_isRequestRoster = true;
					sendRequestRosterListMsg();
				}
								
			}else{
				return;
			}
			break;
		case MainIMScreenHeader.STATE_STATUS_LIST:
			if(m_currMgr != m_statusListMgr){
				replace(m_currMgr,m_statusListMgr);
				m_currMgr = m_statusListMgr;
			}else{
				return;
			}
			break;
			
		}
		
		if(m_currMgr == m_historyChatMgr && m_currFocusHistoryRosterItemField != null){
			m_currFocusHistoryRosterItemField.setFocus();
		}else if(m_currMgr == m_rosterListMgr && m_currFocusRosterItemField != null){
			m_currFocusRosterItemField.setFocus();
		}else if(m_currMgr == m_statusListMgr && m_currFocusStatusField != null){
			m_currFocusStatusField.setFocus();
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
		
		if(!Backlight.isEnabled() || !m_mainApp.isForeground()){
			m_mainApp.TriggerIMNotification();
		}
			
		synchronized (m_rosterChatDataList) {
			for(int i = 0;i < m_rosterChatDataList.size();i++){
				RosterChatData data = (RosterChatData)m_rosterChatDataList.elementAt(i);
				
				if(data.m_roster.getStyle() == _msg.getStyle()
					&& data.m_roster.getAccount().equals(_msg.getOwner())
					&& data.m_roster.getOwnAccount().equals(_msg.getSendTo())){
					
					// remove the history chat record
					//
					while(data.m_chatMsgList.size() > m_mainApp.getIMChatMsgHistory()){
						data.m_chatMsgList.removeElementAt(0);
					}
					
					data.m_chatMsgList.addElement(_msg);
					data.m_currChatState = fetchChatMsg.CHAT_STATE_COMMON;
					
					addHistroyChatMgr(data);
					
					// popup the dialog to prompt
					//
					if(!m_mainApp.isForeground() || m_chatScreen.getUiEngine() == null
					 || (m_chatScreen.getUiEngine() != null && m_chatScreen.m_currRoster != data ) ){
						if(m_promptDlg.isGlobal()){
							// has been popup to prompt
							//
							boolean t_added = false;
							for(int index = 0 ;index < m_promptQueue.size();index++){
								RosterChatData t_promptData = (RosterChatData)m_promptQueue.elementAt(index);
								if(t_promptData == data){
									t_added = true;
									break;
								}
							}
							
							if(!t_added){
								if(m_promptDlg.m_openData == data){
									// the same roster
									//
									m_promptDlg.setRosterChatData(data,_msg.getMsg());
								}else{
									m_promptQueue.addElement(data);
								}								
							}
						}else{
							m_promptDlg.setRosterChatData(data,_msg.getMsg());
							m_mainApp.pushGlobalScreen(m_promptDlg, 0, UiEngine.GLOBAL_QUEUE);
						}
						
						
					}
					
					
					if(m_chatScreen.getUiEngine() != null
						&& m_chatScreen.m_currRoster == data){
						// the activate screen is chat screen
						//
						m_chatScreen.m_middleMgr.addChatMsg(_msg);
						m_chatScreen.m_header.invalidate();
						
					}else{
						
						m_hasNewChatMsg = true;
						m_header.invalidate();
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
	
	public void sendChatReadMsg(fetchChatMsg _msg){
		
		if(_msg.isOwnMsg() || _msg.hasSendMsgChatReadMsg()){
			return ;
		}

		_msg.setSendMsgChatReadMsg(true);
		
		try{
			
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			os.write(msg_head.msgChatRead);
			os.write(_msg.getStyle());
			sendReceive.WriteInt(os,_msg.getReadHashCode());
			
			m_mainApp.m_connectDeamon.addSendingData(msg_head.msgChatRead,os.toByteArray(),true);
			
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
								
								RosterChatData t_data = new RosterChatData(t_roster);
								m_rosterChatDataList.addElement(t_data);
							}
						}
												
						refreshRosterList();
						
						m_historyChatMgr.deleteAll();
						m_currFocusHistoryRosterItemField = null;
						
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
									data.m_isYuch = data.m_roster.getSource().indexOf(MainIMScreen.fsm_YuchBerrySource) != -1;
									
									t_modified = true;
									
									rangeRosterItemField(data);
									
									if(m_mainApp.getActiveScreen() == m_chatScreen
									&& m_chatScreen.m_currRoster == data){
										m_chatScreen.m_header.invalidate();
									}
									
									m_currMgr.invalidate();
									
									break;
								}
							}	
						}						
						
						if(!t_modified){
							
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
	
	private void rangeRosterItemField(RosterChatData _rosterData){
		
		int num = m_rosterListMgr.getFieldCount();
		for(int j = 0;j < num;j++){
			RosterItemField t_field = (RosterItemField)m_rosterListMgr.getField(j);
			if(_rosterData == t_field.m_currRoster){
				// delete the changed field and re-insert it by it's presence state 
				//
				m_rosterListMgr.delete(t_field);
				
				if(!m_mainApp.m_hideUnvailiableRoster 
				|| t_field.m_currRoster.m_roster.getPresence() != fetchChatRoster.PRESENCE_UNAVAIL){
					
					insertRosterItemField(t_field);
				}
				
				return ;
			}
		}
		
		// insert a new field by roster data
		//
		if(!m_mainApp.m_hideUnvailiableRoster 
		|| _rosterData.m_roster.getPresence() != fetchChatRoster.PRESENCE_UNAVAIL){
			
			try{
				WeiboHeadImage t_image = WeiboHeadImage.SearchHeadImage(m_headImageList, _rosterData.m_roster.getAccount(), 
											(byte)_rosterData.m_roster.getStyle(),
											_rosterData.m_roster.getHeadImageHashCode(), false);
				
				insertRosterItemField(new RosterItemField(_rosterData,t_image,false));	
				
			}catch(Exception e){
				m_mainApp.SetErrorString("RRIF:"+e.getMessage()+e.getClass().getName());
			}
			
		}
	}
	
	private void insertRosterItemField(RosterItemField _field){
					
		int t_insertPresence = _field.m_currRoster.m_roster.getPresence();
		int num = m_rosterListMgr.getFieldCount() - 1;
		int index = 0;
		
		for(;index < num;index++){
			RosterItemField item = (RosterItemField)m_rosterListMgr.getField(index);
			if(item.m_currRoster.m_roster.getPresence() == t_insertPresence ){
				break;
			}
		}
		
		for(;index < num;index++){
							
			RosterItemField cmp = (RosterItemField)m_rosterListMgr.getField(index + 1);
			
			int t_cmpPresenece = cmp.m_currRoster.m_roster.getPresence();
			
			int t_cmpResult = cmp.m_currRoster.m_roster.getAccount().compareTo(_field.m_currRoster.m_roster.getAccount());
			
			if((t_cmpResult > 0 && t_cmpPresenece == t_insertPresence) || t_cmpPresenece != t_insertPresence){
				
				m_rosterListMgr.insert(_field,index);
				
				return;
			}
		}
		
		// add to rear
		//
		m_rosterListMgr.add(_field);
		
	}
	
	int m_delayRefreshRosterListTimerID 	= -1;
	int m_delayRefreshRosterListIndex 		= 0;
	Vector m_delayRefreshRosterListList 	= new Vector();
		
	private void refreshRosterList()throws Exception{
		
		synchronized (m_delayRefreshRosterListList){
			
			m_rosterListMgr.deleteAll();
			m_delayRefreshRosterListList.removeAllElements();
			m_currFocusRosterItemField = null;
			
			refreshRosterList_impl(fetchChatRoster.PRESENCE_AVAIL);
			refreshRosterList_impl(fetchChatRoster.PRESENCE_AWAY);
			refreshRosterList_impl(fetchChatRoster.PRESENCE_BUSY);
			refreshRosterList_impl(fetchChatRoster.PRESENCE_FAR_AWAY);
			
			if(!m_mainApp.m_hideUnvailiableRoster){
				refreshRosterList_impl(fetchChatRoster.PRESENCE_UNAVAIL);
			}
		
			if(m_delayRefreshRosterListTimerID != -1){
				
				m_delayRefreshRosterListIndex = 0;
				
			}else{
				
				m_delayRefreshRosterListTimerID = m_mainApp.invokeLater(new Runnable() {
					public void run() {
						synchronized (m_delayRefreshRosterListList){
							
							if(m_delayRefreshRosterListIndex >= m_delayRefreshRosterListList.size()){
								
								m_mainApp.cancelInvokeLater(m_delayRefreshRosterListTimerID);
								
								m_delayRefreshRosterListTimerID = -1;
								m_delayRefreshRosterListIndex = 0;
								
							}else{
								
								RosterChatData t_rosterData = (RosterChatData)m_delayRefreshRosterListList.elementAt(m_delayRefreshRosterListIndex);
								try{
									WeiboHeadImage t_image = WeiboHeadImage.SearchHeadImage(m_headImageList, t_rosterData.m_roster.getAccount(), 
															(byte)t_rosterData.m_roster.getStyle(), 
															t_rosterData.m_roster.getHeadImageHashCode(), false);

									m_rosterListMgr.add(new RosterItemField(t_rosterData, t_image, false));

								}catch(Exception e){
									m_mainApp.SetErrorString("RRL:"+e.getMessage()+e.getClass().getName());
								}
								
								m_delayRefreshRosterListIndex++;
							}
						}
					}
					
				},500,true);
			}
			
			
		}
		
	}
	
	private void refreshRosterList_impl(int _presence)throws Exception{
		
		synchronized (m_rosterChatDataList) {
			
			for(int i = 0 ;i < m_rosterChatDataList.size();i++){
				RosterChatData t_rosterData = (RosterChatData)m_rosterChatDataList.elementAt(i);
				if(t_rosterData.m_roster.getPresence() == _presence){
					m_delayRefreshRosterListList.addElement(t_rosterData);
				}
			}	
		}
		
	}
	
	private void sendRequestRosterListMsg(){
		
		long t_currTime = (new Date()).getTime();
		if(Math.abs(m_refreshRosterTimer - t_currTime) < 5 * 6000){
			m_mainApp.DialogAlert(recvMain.sm_local.getString(localResource.IM_REFRESH_MIN_TIME_PROMPT));
			return;
		}
		
		m_refreshRosterTimer = t_currTime;
		
		try{
			m_mainApp.m_connectDeamon.addSendingData(msg_head.msgChatRosterList, new byte[]{msg_head.msgChatRosterList},true);
		}catch(Exception e){
			m_mainApp.SetErrorString("SRRLM:"+e.getMessage()+e.getClass().getName());
		}
		
	}
	
	public void processChatConfirm(ByteArrayInputStream in)throws Exception{
		
		long t_sendTime = sendReceive.ReadLong(in);
		synchronized (m_sendChatDeamon) {
			for(int i = 0 ;i < m_sendChatDeamon.size();i++){
				SendChatMsgDeamon t_daemon = (SendChatMsgDeamon)m_sendChatDeamon.elementAt(i);
				if(t_daemon.m_sendMsg.getSendTime() == t_sendTime){
					
					t_daemon.inter();
					
					m_sendChatDeamon.removeElementAt(i);
					
					break;
				}
			}
		}
	}
	
	public void processChatState(ByteArrayInputStream in)throws Exception{
		
		int t_style = in.read();
		int t_state = in.read();
		String t_ownAccount = sendReceive.ReadString(in);
		String t_account	= sendReceive.ReadString(in);
		
		synchronized (m_rosterChatDataList) {
			for(int i = 0 ;i < m_rosterChatDataList.size();i++){
				RosterChatData t_data = (RosterChatData)m_rosterChatDataList.elementAt(i);
				if(t_data.m_roster.getStyle() == t_style
					&& t_data.m_roster.getOwnAccount().equals(t_ownAccount)
					&& t_data.m_roster.getAccount().equals(t_account)){
					
					t_data.m_currChatState = t_state;
					
					if(m_chatScreen.m_currRoster == t_data){
						m_chatScreen.m_header.invalidate();
					}
					
					break;
				}
			}
		}
	}
	
	public void processChatPresence(InputStream in){
		
		prepareCurrUseStatus();
		
		if(IMStatus.sm_currUseStatus != null){
			sendUseStatus(IMStatus.sm_currUseStatus);
		}
	}
	
	public void processChatRead(InputStream in)throws Exception{
		int t_style = in.read();
		String t_account = sendReceive.ReadString(in);
		int t_hashcode = sendReceive.ReadInt(in);
		
		for(int i = 0 ;i < m_rosterChatDataList.size();i++){
			RosterChatData data = (RosterChatData)m_rosterChatDataList.elementAt(i);
			if(data.m_roster.getStyle() == t_style
			&& data.m_roster.getAccount().equals(t_account)){
				// the right account has been found
				//
				for(int j = data.m_chatMsgList.size() - 1;j >= 0;j--){
					fetchChatMsg msg = (fetchChatMsg)data.m_chatMsgList.elementAt(j);
					if(msg.hashCode() == t_hashcode){
						
						// find the right msg
						//
						msg.setSendState(fetchChatMsg.SEND_STATE_READ);
						
						if(m_mainApp.getActiveScreen() == m_chatScreen
							&& m_chatScreen.m_currRoster == data){
							
							// refresh the right ChatField 
							//
							synchronized (m_chatScreen.m_middleMgr){
								Manager t_manager = m_chatScreen.m_middleMgr.m_chatMsgMgr;
								int num = t_manager.getFieldCount();
								for(int index = num - 1;index >= 0;index--){
									ChatField t_field = (ChatField)t_manager.getField(index);
									if(t_field.m_msg == msg){
										
										t_field.invalidate();
										
										break;
									}
								}
							}
						}
						
						break;
					}
				}
				break;
			}
			
		}
	}
	
	public synchronized void sendUseStatus(IMStatus _status){
		
		try{
				
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			os.write(msg_head.msgChatPresence);
			os.write(_status.m_presence);
			sendReceive.WriteString(os,_status.m_status);
			
			m_mainApp.m_connectDeamon.addSendingData(msg_head.msgChatPresence, os.toByteArray(), true);
			
		}catch(Exception e){
			m_mainApp.SetErrorString("SUP:"+e.getMessage()+e.getClass().getName());
		}
		
	}
	
	public void addSendChatMsg(fetchChatMsg _msg,RosterChatData _sendTo){
		
		SendChatMsgDeamon t_daemon = new SendChatMsgDeamon(_msg, _sendTo, m_chatScreen);
		m_sendChatDeamon.addElement(t_daemon);
		
		addHistroyChatMgr(_sendTo);
	}
	
	private void addHistroyChatMgr(RosterChatData _rosterData){
		
		synchronized (m_historyChatMgr) {
			
			// find the field inhistroy chat
			//
			int t_num = m_historyChatMgr.getFieldCount();
			
			boolean t_found = false;
			for(int i = 0 ;i < t_num;i++){
				RosterItemField t_field = (RosterItemField)m_historyChatMgr.getField(i);
				if(t_field.m_currRoster == _rosterData){
					
					m_historyChatMgr.delete(t_field);
					m_historyChatMgr.insert(t_field, 0);
					t_found = true;
					
					break;
				}
			}
			
			if(!t_found){
				try{
					
					WeiboHeadImage t_image = WeiboHeadImage.SearchHeadImage(m_headImageList, _rosterData.m_roster.getAccount(), 
							(byte)_rosterData.m_roster.getStyle(), 
							_rosterData.m_roster.getHeadImageHashCode(), false);

					m_historyChatMgr.insert(new RosterItemField(_rosterData,t_image,true),0);
					
				}catch(Exception e){
					m_mainApp.SetErrorString("ASCM:"+e.getMessage()+e.getClass().getName());
				}
			}
		}
	}
}
