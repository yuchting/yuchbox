/**
 *  Dear developer:
 *  
 *   If you want to modify this file of project and re-publish this please visit:
 *  
 *     http://code.google.com/p/yuchberry/wiki/Project_files_header
 *     
 *   to check your responsibility and my humble proposal. Thanks!
 *   
 *  -- 
 *  Yuchs' Developer    
 *  
 *  
 *  
 *  
 *  尊敬的开发者：
 *   
 *    如果你想要修改这个项目中的文件，同时重新发布项目程序，请访问一下：
 *    
 *      http://code.google.com/p/yuchberry/wiki/Project_files_header
 *      
 *    了解你的责任，还有我卑微的建议。 谢谢！
 *   
 *  -- 
 *  语盒开发者
 *  
 */
package com.yuchting.yuchberry.client.im;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import local.yblocalResource;
import net.rim.blackberry.api.invoke.Invoke;
import net.rim.blackberry.api.invoke.MessageArguments;
import net.rim.blackberry.api.mail.Address;
import net.rim.blackberry.api.mail.Message;
import net.rim.device.api.collection.Collection;
import net.rim.device.api.system.Backlight;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.UiEngine;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.component.NullField;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.yuchting.yuchberry.client.Indicator;
import com.yuchting.yuchberry.client.ObjectAllocator;
import com.yuchting.yuchberry.client.msg_head;
import com.yuchting.yuchberry.client.recvMain;
import com.yuchting.yuchberry.client.sendReceive;
import com.yuchting.yuchberry.client.ui.BubbleTextField;
import com.yuchting.yuchberry.client.ui.SliderHeader;
import com.yuchting.yuchberry.client.ui.WeiboHeadImage;


public class MainIMScreen extends MainScreen implements FieldChangeListener{
	
	public final static class RosterChatData{
		
		public fetchChatRoster		m_roster;
		public Vector				m_chatMsgList = new Vector();
		
		public String				m_lastChatText = "";
		public int					m_currChatState;
		
		public boolean				m_hasNewMessage = false;
		
		/**
		 * this data roster is Yuch client
		 */
		boolean					m_isYuch = false;
		
		public RosterChatData(fetchChatRoster _roster){
			m_roster = _roster;
			m_isYuch = m_roster.getSource().indexOf(MainIMScreen.fsm_YuchBerrySource) != -1;
		}
		
		public void copyFrom(fetchChatRoster _roster){
			m_roster.copyFrom(_roster);
			m_isYuch = m_roster.getSource().indexOf(MainIMScreen.fsm_YuchBerrySource) != -1;
		}
	}
	
	public final static int fsm_backgroundColor = 0x1f2d39;
	
	public final static int fsm_groupBackgroundColor = 0x122030;
	
	int m_menu_label = 0;
	
	MenuItem	m_historyChatMenu = new MenuItem(recvMain.sm_local.getString(yblocalResource.IM_HISTORY_MENU_LABEL),m_menu_label++,0){
		public void run(){
			m_header.setCurrState(MainIMScreenHeader.STATE_HISTORY_CHAT);
			refreshHeader();
		}
	};
	
	MenuItem	m_rosterListMenu = new MenuItem(recvMain.sm_local.getString(yblocalResource.IM_ROSTER_LIST_MENU_LABEL),m_menu_label++,0){
		public void run(){
			m_header.setCurrState(MainIMScreenHeader.STATE_ROSTER_LIST);
			refreshHeader();
		}
	};
	
	MenuItem	m_statusListMenu = new MenuItem(recvMain.sm_local.getString(yblocalResource.IM_STATUS_LIST_MENU_LABEL),m_menu_label++,0){
		public void run(){
			m_header.setCurrState(MainIMScreenHeader.STATE_STATUS_LIST);
			refreshHeader();
		}
	};
	
	int m_menu_op = 20;
	
	MenuItem	m_refreshListMenu = new MenuItem(recvMain.sm_local.getString(yblocalResource.IM_REFRESH_ROSTER_MENU_LABEL),m_menu_op++,0){
		public void run(){
			sendRequestRosterListMsg(true);
		}
	};
	
	MenuItem	m_showUnvailRosterMenu = new MenuItem(recvMain.sm_local.getString(yblocalResource.IM_SHOW_UNVAIL_ROSTER_MENU_LABEL),m_menu_op++,0){
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
	

	MenuItem	m_searchRosterMenu = new MenuItem(recvMain.sm_local.getString(yblocalResource.IM_SEARCH_ROSTER_MENU_LABEL),m_menu_op++,0){
		public void run(){
			if(m_searchStatus == null){
				m_searchStatus = new SearchStatus(MainIMScreen.this);
			}
			
			UiApplication.getUiApplication().pushScreen(m_searchStatus);
			m_searchStatus.m_editTextArea.setFocus();
		}
	};
	
	MenuItem m_topRosterMenu = new MenuItem(recvMain.sm_local.getString(yblocalResource.IM_TOP_ROSTER_MENU_LABEL),m_menu_op++,0){
		public void run(){
			if(m_currMgr != null && m_currMgr.getFieldCount() != 0){
				m_currMgr.getField(0).setFocus();
			}
		}
	};
	
	MenuItem m_bottomRosterMenu = new MenuItem(recvMain.sm_local.getString(yblocalResource.IM_BOTTOM_ROSTER_MENU_LABEL),m_menu_op++,0){
		public void run(){
			if(m_currMgr != null && m_currMgr.getFieldCount() != 0){
				m_currMgr.getField(m_currMgr.getFieldCount() - 1).setFocus();
			}
		}
	};
	
	public IMAddRosterDlg		m_addRosterDlg = null;
	MenuItem	m_addRosterMenu = new MenuItem(recvMain.sm_local.getString(yblocalResource.IM_ADD_ROSTER_MENU_LABEL),m_menu_op++,0){
		public void run(){
			if(m_addRosterDlg == null){
				m_addRosterDlg = new IMAddRosterDlg(MainIMScreen.this);
			}
			
			m_mainApp.invokeLater(new Runnable() {
				public void run() {
					m_mainApp.pushScreen(m_addRosterDlg);
				}
			});
			
		}
	};	

	public RosterInfoScreen m_checkRosterInfoScreen = null;
	MenuItem	m_checkRosterMenu = new MenuItem(recvMain.sm_local.getString(yblocalResource.IM_CHECK_ROSTER_MENU_LABEL),m_menu_op++,0){
		public void run(){
			
			MainIMScreen.RosterChatData t_data = getCurrFocusRosterData();
			
			if(t_data != null){

				if(m_checkRosterInfoScreen == null){
					m_checkRosterInfoScreen = new RosterInfoScreen(MainIMScreen.this,t_data);
				}
				
				UiApplication.getUiApplication().pushScreen(m_checkRosterInfoScreen);	
			}
		}
	};
	
	MenuItem	m_delRosterMenu = new MenuItem(recvMain.sm_local.getString(yblocalResource.IM_DEL_ROSTER_MENU_LABEL),m_menu_op++,0){
		public void run(){
			if(m_currFocusRosterItemField != null && m_currMgr == m_groupListMgr){
			
				fetchChatRoster t_roster = ((RosterItemField)m_currFocusRosterItemField).m_currRoster.m_roster;
				
				String t_prompt = recvMain.sm_local.getString(yblocalResource.IM_DEL_ROSTER_PROMPT) 
										+ t_roster.getName() + " " + t_roster.getAccount();
				
				if(Dialog.ask(Dialog.D_YES_NO,t_prompt,Dialog.NO) != Dialog.YES){
					return;
				}
				
				delRoster(t_roster);
			}
			
		}
	};
	
	public IMAliasDlg m_aliasDlg = null;
	MenuItem	m_aliasRosterMenu = new MenuItem(recvMain.sm_local.getString(yblocalResource.IM_ALIAS_ROSTER_MENU_LABEL),m_menu_op++,0){
		public void run(){
			if(m_currFocusRosterItemField != null){
			
				if(m_aliasDlg == null){
					m_aliasDlg = new IMAliasDlg(MainIMScreen.this,(RosterItemField)m_currFocusRosterItemField);
				}
				
				m_mainApp.invokeLater(new Runnable() {
					public void run() {
						m_mainApp.pushScreen(m_aliasDlg);
					}
				});
			}
			
		}
	};
	
	MenuItem m_delHistoryRoster = new MenuItem(recvMain.sm_local.getString(yblocalResource.IM_DEL_HISTORY_ROSTER_MENU_LABEL),m_menu_op++,0){
		public void run(){
			
			if(m_currMgr == m_historyChatMgr){
				RosterChatData t_data = getCurrFocusRosterData();
				if(t_data != null){
					
					fetchChatRoster t_roster = t_data.m_roster;
					
					String t_prompt = recvMain.sm_local.getString(yblocalResource.IM_DEL_HISTORY_ROSTER_PROMPT) 
											+ t_roster.getName() + " " + t_roster.getAccount();
					
					if(Dialog.ask(Dialog.D_YES_NO,t_prompt,Dialog.NO) != Dialog.YES){
						return;
					}
					
					delHistoryRoster(t_roster);
				}
			}
			
		}
	};
	
	
	MenuItem		m_sendEmailMenu = new MenuItem(recvMain.sm_local.getString(yblocalResource.IM_SEND_EMAIL),m_menu_op++,0){
		public void run(){
			RosterChatData t_data = getCurrFocusRosterData();
			
			if(t_data != null){
				Message msg = new Message();
				
				try{
					msg.addRecipients(Message.RecipientType.TO,
							new Address[]{new Address(t_data.m_roster.getAccount(),t_data.m_roster.getName())});
											
					Invoke.invokeApplication(Invoke.APP_TYPE_MESSAGES, new MessageArguments(msg));
					
				}catch(Exception e){
					m_mainApp.SetErrorString("SEM:"+e.getMessage()+e.getClass().getName());
				}
				
			}
		}
	};
	
	MenuItem	m_hideUnvailRosterMenu = new MenuItem(recvMain.sm_local.getString(yblocalResource.IM_HIDE_UNVAIL_ROSTER_MENU_LABEL),m_menu_op++,0){
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
	MenuItem	m_addStatusMenu = new MenuItem(recvMain.sm_local.getString(yblocalResource.IM_ADD_STATUS),m_menu_op++,0){
		public void run(){
			m_statusAddScreen = new IMStatusAddScreen(MainIMScreen.this, null);
			m_mainApp.invokeLater(new Runnable() {
				public void run() {
					m_mainApp.pushScreen(m_statusAddScreen);
				}
			});
			
		}
	};
	
	MenuItem	m_modifyStatusMenu = new MenuItem(recvMain.sm_local.getString(yblocalResource.IM_MODIFY_STATUS),m_menu_op++,0){
		public void run(){
			
			int num = m_statusListMgr.getFieldCount();
			for(int i = 0;i < num;i++){
				IMStatusField t_field = (IMStatusField)m_statusListMgr.getField(i);
				if(t_field.isFocus()){
					
					m_statusAddScreen = new IMStatusAddScreen(MainIMScreen.this, t_field.m_status);
					m_mainApp.invokeLater(new Runnable() {
						public void run() {
							m_mainApp.pushScreen(m_statusAddScreen);
						}
					});
					
					break;
				}
			}
		}
	};
	
	MenuItem	m_delStatusMenu = new MenuItem(recvMain.sm_local.getString(yblocalResource.IM_DELETE_STATUS),m_menu_op++,0){
		public void run(){
			
			int num = m_statusListMgr.getFieldCount();
			for(int i = 0;i < num;i++){
				IMStatusField t_field = (IMStatusField)m_statusListMgr.getField(i);
				if(t_field.isFocus()){
					
					if(Dialog.ask(Dialog.D_YES_NO,
						recvMain.sm_local.getString(yblocalResource.IM_DELETE_STATUS_PROMPT) + t_field.m_status,Dialog.NO) != Dialog.YES){
						
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
	
	MenuItem	m_optionMenu = new MenuItem(recvMain.sm_local.getString(yblocalResource.IM_OPTION_SCREEN),m_menu_op++,0){
		public void run(){
			if(m_optionScreen == null){
				m_optionScreen = new IMOptionScreen(MainIMScreen.this);
			}
			
			m_mainApp.pushScreen(m_optionScreen);
		}
	};
		
	
	MenuItem	m_stateMenu = new MenuItem(recvMain.sm_local.getString(yblocalResource.WEIBO_STATE_SCREEN_MENU_LABEL),100,0){
		public void run(){
			recvMain t_recv = (recvMain)UiApplication.getUiApplication();
        	
        	t_recv.popScreen(MainIMScreen.this);
        	t_recv.pushStateScreen();
		}
	};
	
	  
    MenuItem m_weiboScreenItem = new MenuItem(recvMain.sm_local.getString(yblocalResource.YB_WEIBO_MENU_LABEL),101,0){
        public void run() {
        	        	
        	if(m_mainApp.m_enableWeiboModule){
        		
        		recvMain t_recv = (recvMain)UiApplication.getUiApplication();
            	t_recv.popScreen(MainIMScreen.this);
            	
            	t_recv.PopupWeiboScreen();		
        	}
        }
    };

	// BasicEditField for 4.2os
	public final static BubbleTextField 	fsm_testTextArea	= new BubbleTextField();
	
	public final static Font		fsm_defaultFont			= fsm_testTextArea.getFont();
	public final static int		fsm_defaultFontHeight	= fsm_defaultFont.getHeight();
	
	public final static String	fsm_YuchBerrySource		= "YuchBerry.info";
	public final static Font		fsm_boldFont			= fsm_defaultFont.derive(fsm_defaultFont.getStyle() | Font.BOLD);
	
	public ObjectAllocator			m_chatMsgAllocator		= new ObjectAllocator("com.yuchting.yuchberry.client.im.fetchChatMsg"); 
			
	public Vector					m_headImageList = new Vector();

	MainIMScreenHeader		m_header = null;
	
	public IMOptionScreen	m_optionScreen = null;
	
	recvMain				m_mainApp = null;
	
	VerticalFieldManager	m_historyChatMgr = new VerticalFieldManager(Manager.VERTICAL_SCROLL | Manager.VERTICAL_SCROLLBAR);
	VerticalFieldManager	m_statusListMgr = new VerticalFieldManager(Manager.VERTICAL_SCROLL | Manager.VERTICAL_SCROLLBAR);
	VerticalFieldManager	m_groupListMgr = new VerticalFieldManager(Manager.VERTICAL_SCROLL | Manager.VERTICAL_SCROLLBAR){
		public Field getFieldWithFocus(){
			
			Field t_result = null;
			
			int num = this.getFieldCount();
			
			for(int i = 0;i < num;i++){
				GroupFieldManager t_mgr = (GroupFieldManager)this.getField(i);
				t_result = t_mgr.getFieldWithFocus();
				if(t_result != null){
					break;
				}
			}
			
			return t_result;
		}
	};
	
	GroupFieldManager		m_selectGroupTitleField = null;
	
	Vector					m_groupNameList = new Vector();
	Vector					m_groupRosterList = new Vector();
		
	public Vector			m_rosterChatDataList = new Vector();
	
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
	

	public	Field	m_currFocusRosterItemField_scrollBackup = null;
	public	Field	m_currFocusHistoryRosterItemField_scrollBackup = null;
	public Field	m_currFocusStatusField_scrollBackup = null;
	
	public SearchStatus		m_searchStatus	= null;
	
		
	public MainIMScreen(recvMain _mainApp){
		super(Manager.VERTICAL_SCROLL);
		
		m_mainApp = _mainApp;
		m_header = new MainIMScreenHeader(this);
		m_chatScreen = new MainChatScreen(_mainApp,this);
		
		add(m_historyChatMgr);
		setBanner(m_header);
		
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
	
	public void changeHeadImageHash(String _acc,int _style,int _headImageHash){
		synchronized(m_rosterChatDataList){
			
			for(int i = 0;i < m_rosterChatDataList.size();i++){
				
				RosterChatData t_data = (RosterChatData)m_rosterChatDataList.elementAt(i);
				
				if(t_data.m_roster.getStyle() == _style
				&& t_data.m_roster.getAccount().equals(_acc)){
					
					t_data.m_roster.setHeadImageHashCode(_headImageHash);
					
					break;
				}
			}
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
	
	private RosterChatData getCurrFocusRosterData(){
		RosterChatData t_data = null;
		
		if(m_currMgr == m_groupListMgr && m_currFocusRosterItemField != null){
			t_data = ((RosterItemField)m_currFocusRosterItemField).m_currRoster;
		}else if(m_currMgr == m_historyChatMgr && m_currFocusHistoryRosterItemField != null){
			t_data = ((RosterItemField)m_currFocusHistoryRosterItemField).m_currRoster;
		}
		
		return t_data;
	}
	
	protected void makeMenu(Menu _menu,int instance){
		_menu.add(m_historyChatMenu);
		_menu.add(m_rosterListMenu);
		_menu.add(m_statusListMenu);
		_menu.add(MenuItem.separator(m_menu_label));
		
		_menu.add(m_refreshListMenu);
		
		if(m_currMgr != null && m_currMgr.getFieldCount() != 0){
			_menu.add(m_topRosterMenu);
			_menu.add(m_bottomRosterMenu);
		}
		
		if(m_currMgr == m_groupListMgr){

			if(m_mainApp.m_hideUnvailiableRoster){
				_menu.add(m_showUnvailRosterMenu);
			}else{
				_menu.add(m_hideUnvailRosterMenu);
			}
			
			_menu.add(m_addRosterMenu);
			
			if(m_currFocusRosterItemField != null){
				_menu.add(m_delRosterMenu);
				_menu.add(m_aliasRosterMenu);
			}
		}else if(m_currMgr == m_statusListMgr){
			_menu.add(m_addStatusMenu);
			_menu.add(m_modifyStatusMenu);
			_menu.add(m_delStatusMenu);
		}else if(m_currMgr == m_historyChatMgr){
			_menu.add(m_delHistoryRoster);
			if(m_currFocusRosterItemField != null){
				_menu.add(m_aliasRosterMenu);
			}
		}
		
		if(m_currMgr == m_groupListMgr || m_currMgr == m_historyChatMgr){
			_menu.add(m_searchRosterMenu);
			
			if(getCurrFocusRosterData() != null){
				_menu.add(m_checkRosterMenu);
				_menu.add(m_sendEmailMenu);
			}
			
		}
		
		_menu.add(m_optionMenu);
		_menu.add(MenuItem.separator(m_menu_op));
		
		_menu.add(m_stateMenu);
		if(m_mainApp.m_enableWeiboModule){
			_menu.add(m_weiboScreenItem);
		}
		
		
		super.makeMenu(_menu,instance);
	}
	
	protected boolean navigationMovement(int dx,int dy,int status,int time){
		if(dx != 0){
			
			if(m_searchStatus != null){
				return true;
			}
			
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
			if(m_mainApp.m_enableWeiboModule){
				m_weiboScreenItem.run();
				return true;
			}
			break;
		case 'F':
			m_searchRosterMenu.run();
			return true;
		case Keypad.KEY_BACKSPACE:
			
			if(m_currMgr == m_statusListMgr){
				
				m_delStatusMenu.run();
				
			}else if(m_currFocusRosterItemField != null){
				
				if(m_currMgr == m_groupListMgr){
					m_delRosterMenu.run();
				}else if(m_currMgr == m_historyChatMgr){
					m_delHistoryRoster.run();
				}
				
				return true;
			}
			
			break;
		case 'T':
			m_topRosterMenu.run();
			return true;
		case 'B':
			m_bottomRosterMenu.run();
			return true;
		}
		
		if(key == 10 && click()){
			return true;
		}
		
		return super.keyDown(keycode,time);
	}
	
	protected void paint(Graphics _g){
		super.paint(_g);
		
		int t_currMgrHeight = m_currMgr.getExtent().height;
		int t_height = recvMain.fsm_display_height - m_header.getPreferredHeight() - t_currMgrHeight;
		if(t_height > 0){
			int t_color = _g.getColor();
			try{
				_g.setColor(fsm_backgroundColor);
				_g.fillRect(0,m_header.getPreferredHeight() + t_currMgrHeight,
								recvMain.fsm_display_width,t_height);
				
			}finally{
				_g.setColor(t_color);
			}
		}		
	}
	
	private boolean click(){

		if(m_currMgr == m_groupListMgr && m_selectGroupTitleField != null){
			m_selectGroupTitleField.fieldChanged(null, 0);
			return true;
		}
		
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
		}		
		
		return false;
	}
	
	public void fieldChanged(Field field,int context){
		if(context != FieldChangeListener.PROGRAMMATIC){
			RosterItemField t_field = (RosterItemField)field;
			
			m_chatScreen.popup(t_field.m_currRoster);
		}
	}
	
	public void clearNewChatSign(){
		if(m_hasNewChatMsg){
			m_hasNewChatMsg = false;
			m_header.invalidate();
		}
	}
	
	private void refreshHeader(){
		
		if(m_currMgr == m_historyChatMgr){
			m_currFocusHistoryRosterItemField_scrollBackup = m_currMgr.getFieldWithFocus();
		}else if(m_currMgr == m_groupListMgr){
			m_currFocusRosterItemField_scrollBackup = m_currMgr.getFieldWithFocus();
		}else if(m_currMgr == m_statusListMgr){
			m_currFocusStatusField_scrollBackup  = m_currMgr.getFieldWithFocus();
		}		
		
		switch(m_header.getCurrState()){
		case MainIMScreenHeader.STATE_HISTORY_CHAT:
			if(m_currMgr != m_historyChatMgr){
				
				// the replace function will make RosterItemField.drawFocus to modify
				//
				// m_currFocusHistoryRosterItemField
				// m_currFocusRosterItemField
				// m_currFocusStatusField
				//
				// so use the special variables for backup/restore selected item
				//
				replace(m_currMgr,m_historyChatMgr);
				m_currMgr = m_historyChatMgr;
				
				clearNewChatSign();
			}else{
				return;
			}
			break;
		case MainIMScreenHeader.STATE_ROSTER_LIST:
			if(m_currMgr != m_groupListMgr){
		
				replace(m_currMgr,m_groupListMgr);
				m_currMgr = m_groupListMgr;
				
				if(!m_isRequestRoster){
					m_isRequestRoster = true;
					sendRequestRosterListMsg(false);
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
		
		if(m_currMgr == m_historyChatMgr && m_currFocusHistoryRosterItemField_scrollBackup != null){
			m_currFocusHistoryRosterItemField_scrollBackup.setFocus();
		}else if(m_currMgr == m_groupListMgr && m_currFocusRosterItemField_scrollBackup != null){
			m_currFocusRosterItemField_scrollBackup.setFocus();
		}else if(m_currMgr == m_statusListMgr && m_currFocusStatusField_scrollBackup != null){
			m_currFocusStatusField_scrollBackup.setFocus();
		}	
	}
	
	Vector m_delayLoadChatMsg = new Vector();
	int	   m_delayLoadChatMsgTimer = -1;
	
	public void processChatMsg(final InputStream in)throws Exception{
		
		fetchChatMsg t_msg = null;
		try{
			t_msg = (fetchChatMsg)m_chatMsgAllocator.alloc();
		}catch(Exception e){
			t_msg = new fetchChatMsg();
			m_mainApp.SetErrorString("PCM_0:"+e.getMessage()+e.getClass().getName());
		}
		
		t_msg.Import(in);
		
		sendChatConfirmMsg(t_msg);
		
		synchronized (m_delayLoadChatMsg) {
			
			m_delayLoadChatMsg.addElement(t_msg);
			
			if(m_delayLoadChatMsgTimer == -1){
				
				m_delayLoadChatMsgTimer = m_mainApp.invokeLater(new Runnable() {
					
					public void run() {
						
						if(m_delayLoadChatMsg.isEmpty()){
							synchronized (m_delayLoadChatMsg) {
								m_mainApp.cancelInvokeLater(m_delayLoadChatMsgTimer);
								m_delayLoadChatMsgTimer = -1; 
								return;
							}
						}
						
						fetchChatMsg msg = null; 
							
						synchronized (m_delayLoadChatMsg) {
							msg = (fetchChatMsg)m_delayLoadChatMsg.elementAt(0);
							m_delayLoadChatMsg.removeElementAt(0);	
						}
						
						addChatMsg(msg);
					}
				}, recvMain.fsm_delayLoadingTime / 2, true);
			}
		}
	}
	
	public static String fsm_chatMsgPrompt_image = recvMain.sm_local.getString(yblocalResource.IM_CHAT_MSG_IMAGE_PROMPT);
	public static String fsm_chatMsgPrompt_voice = recvMain.sm_local.getString(yblocalResource.IM_CHAT_MSG_VOICE_PROMPT);
	
	public static String getChatMsgAbsText(fetchChatMsg _msg){
		
		String t_text = _msg.getMsg().length() == 0?"":_msg.getMsg();
		
		if(_msg.getFileContent() != null){
			if(_msg.getFileContentType() == fetchChatMsg.FILE_TYPE_IMG){
				t_text = t_text + fsm_chatMsgPrompt_image;
			}else{
				t_text = t_text + fsm_chatMsgPrompt_voice;
			}
		}
		
		return t_text;
	}
	
	private void addChatMsg(fetchChatMsg _msg){
				
		boolean t_notify = !Backlight.isEnabled() 
							|| !m_mainApp.isForeground() 
							|| m_chatScreen.getUiEngine() == null;
		
		boolean t_reNotify = !Backlight.isEnabled() && m_mainApp.m_imRenotifyPrompt;
		
		synchronized (m_rosterChatDataList) {
			for(int i = 0;i < m_rosterChatDataList.size();i++){
				RosterChatData data = (RosterChatData)m_rosterChatDataList.elementAt(i);
				
				if(data.m_roster.getStyle() == _msg.getStyle()
					&& data.m_roster.getAccount().equals(_msg.getOwner())
					&& data.m_roster.getOwnAccount().equals(_msg.getSendTo())){
					
					// found whether has the duplication message
					//
					for(int index = 0; index < data.m_chatMsgList.size();index++){
						fetchChatMsg msg = (fetchChatMsg)data.m_chatMsgList.elementAt(index);
						
						if(msg.getMsg().equals(_msg.getMsg())
							&& msg.getSendTime() == _msg.getSendTime()){
			
							// repeat message, ignore it
							//
							return ;
						}
					}
					
					// remove the history chat record
					//
					while(data.m_chatMsgList.size() > m_mainApp.getIMChatMsgHistory()){
						fetchChatMsg msg = (fetchChatMsg)data.m_chatMsgList.elementAt(0);
						msg.destory();
						m_chatMsgAllocator.release(msg);						
						data.m_chatMsgList.removeElementAt(0);
					}
					
					data.m_hasNewMessage = true;
					data.m_chatMsgList.addElement(_msg);
					data.m_currChatState = fetchChatMsg.CHAT_STATE_COMMON;
					
					storefetchMsg(_msg);
					
					addHistroyChatMgr(data);
										
					if(m_mainApp.m_imPopupPrompt){
						
						// popup the dialog to prompt
						//
						if(!m_mainApp.isForeground() || m_chatScreen.getUiEngine() == null
						 || (m_chatScreen.getUiEngine() != null && m_chatScreen.getCurrRoster() != data ) ){
							
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
										m_promptDlg.setRosterChatData(data,getChatMsgAbsText(_msg));
										
										// stop the notify
										//
										t_notify = false;
										
									}else{
										m_promptQueue.addElement(data);
									}								
								}
							}else{
								m_promptDlg.setRosterChatData(data,getChatMsgAbsText(_msg));
								m_mainApp.pushGlobalScreen(m_promptDlg, 0, UiEngine.GLOBAL_QUEUE);
							}						
						}else{
							if(m_hasNewChatMsg){
								// has new chat msg 
								//
								t_notify = false;
							}
						}
					}
					
					if(m_chatScreen.getUiEngine() != null
						&& m_chatScreen.getCurrRoster() == data){
						// the activate screen is chat screen
						//
						m_chatScreen.m_middleMgr.addChatMsg(_msg);
						m_chatScreen.m_header.invalidate();
						
						if(!m_chatScreen.m_isPrompted){
							m_chatScreen.m_isPrompted = true;
						}else{
							t_notify = false;
						}
												
					}else{
						
						m_hasNewChatMsg = true;
						m_header.invalidate();
					}
					
					break;
				}
			}	
		}
		
		if(t_notify || t_reNotify){
			m_mainApp.TriggerIMNotification();
		}
		
		Indicator.notifyIM();
	}
	
	static Calendar sm_calendar = Calendar.getInstance();
	static Date		sm_timeDate = new Date();

	public  void storefetchMsg(fetchChatMsg _msg){
		
		if(!m_mainApp.m_imStoreImageVoice){
			return;
		}
		
		if(_msg.getFileContent() == null){
			return;
		}
		
		try{
			
			sm_timeDate.setTime(_msg.getSendTime());
			sm_calendar.setTime(sm_timeDate);
			
			StringBuffer t_filename = new StringBuffer();
			t_filename.append(m_mainApp.getIMStoreImageVoicePath()).append(_msg.getOwner()).append("_2_").append(_msg.getSendTo())
						.append("_").append(sm_calendar.get(Calendar.YEAR)).append("-").append(sm_calendar.get(Calendar.MONTH))
						.append("-").append(sm_calendar.get(Calendar.DAY_OF_MONTH))
						.append("-").append(sm_calendar.get(Calendar.HOUR_OF_DAY)).append("h")
						.append(sm_calendar.get(Calendar.MINUTE)).append("m").append(sm_calendar.get(Calendar.SECOND)).append("s")
						.append(_msg.hashCode()).append(_msg.getFileContentType() == fetchChatMsg.FILE_TYPE_IMG?".jpg":".amr");
			
			FileConnection t_file = (FileConnection)Connector.open(t_filename.toString(),Connector.READ_WRITE);
			try{
				if(!t_file.exists()){
					t_file.create();
				}
				
				OutputStream os = t_file.openOutputStream();
				try{
					os.write(_msg.getFileContent());
				}finally{
					os.close();
					os = null;
				}
				
			}finally{
				t_file.close();
			}
			
		}catch(Exception e){
			m_mainApp.SetErrorString("SFM:"+e.getMessage()+e.getClass().getName());
		}
		
	}
	
	private void delRoster(fetchChatRoster _roster){
		synchronized (m_rosterChatDataList) {
			for(int i = 0 ;i < m_rosterChatDataList.size();i++){
				RosterChatData data = (RosterChatData)m_rosterChatDataList.elementAt(i);
				if(data.m_roster == _roster){
					
					for(int j = 0;j < data.m_chatMsgList.size();j++){
						fetchChatMsg msg = (fetchChatMsg)data.m_chatMsgList.elementAt(j);
						msg.destory();
						m_chatMsgAllocator.release(data.m_chatMsgList.elementAt(j));
					}
					
					m_rosterChatDataList.removeElementAt(i);
					
					break;
				}
			}
		}
		
		synchronized (m_groupListMgr) {
			
			Manager t_groupRosterMgr = getRosterGroupMgr(_roster);
			if(t_groupRosterMgr != null){
				
				int t_count = t_groupRosterMgr.getFieldCount();
				for(int i = 0; i < t_count;i++){
					RosterItemField field = (RosterItemField)t_groupRosterMgr.getField(i);
					if(field.m_currRoster.m_roster == _roster){
						
						t_groupRosterMgr.delete(field);
						
						break;				
					}			
				}	
			}			
		}
		
				
		synchronized (m_historyChatMgr) {

			int t_count = m_historyChatMgr.getFieldCount();
			for(int i = 0; i < t_count;i++){
				RosterItemField field = (RosterItemField)m_historyChatMgr.getField(i);
				if(field.m_currRoster.m_roster == _roster){
					
					m_historyChatMgr.delete(field);
					
					break;				
				}			
			}	
		}	
		
		// send to server to delete roster
		//
		try{
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			os.write(msg_head.msgChatDelRoster);
			os.write(_roster.getStyle());
			sendReceive.WriteString(os,_roster.getOwnAccount());
			sendReceive.WriteString(os,_roster.getAccount());
			
			m_mainApp.m_connectDeamon.addSendingData(msg_head.msgChatDelRoster, os.toByteArray(), true);	
		}catch(Exception e){
			m_mainApp.SetErrorString("DR:"+e.getMessage()+e.getClass().getName());
		}
	}
	
	private void delHistoryRoster(fetchChatRoster _roster){
		
		synchronized (m_historyChatMgr) {

			int t_count = m_historyChatMgr.getFieldCount();
			for(int i = 0; i < t_count;i++){
				RosterItemField field = (RosterItemField)m_historyChatMgr.getField(i);
				if(field.m_currRoster.m_roster == _roster){
					
					field.m_currRoster.m_chatMsgList.removeAllElements();
					
					m_historyChatMgr.delete(field);
					
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
		
		if(!m_mainApp.m_enableChatChecked){
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
	
	int m_delayLoadChatRosterTimer = -1;
	Vector m_delayLoadChatRosterList = new Vector();
	
	public void processChatRosterList(final InputStream in){
		
		try{
			int t_type = in.read();
			switch(t_type){
			case 0:
				m_isRequestRoster = true;
				
				// all list
				//
				boolean t_found = false;
				
				Vector t_tmpRosterList = new Vector();
				int t_num = sendReceive.ReadInt(in);
				
				m_mainApp.SetErrorString("PCRL: recv " + t_num + " roster");
				synchronized(m_rosterChatDataList){
					for(int i = 0 ;i < t_num;i++){
						fetchChatRoster t_roster = new fetchChatRoster();
						t_roster.Import(in);
						
						t_found = false;
												
						for(int j = 0;j < m_rosterChatDataList.size();j++){
							RosterChatData t_data = (RosterChatData)m_rosterChatDataList.elementAt(j);
							
							if(t_data.m_roster.getAccount().equals(t_roster.getAccount())){
								t_data.copyFrom(t_roster);
								t_tmpRosterList.addElement(t_data);
								
								t_found = true;
								break;
							}
						}
											
						if(!t_found){				
							RosterChatData t_data = new RosterChatData(t_roster);
							t_tmpRosterList.addElement(t_data);
						}
					}
				
					// re-add it
					m_rosterChatDataList.removeAllElements();
					
					for(int i = 0;i < t_tmpRosterList.size();i++){
						RosterChatData t_data = (RosterChatData)t_tmpRosterList.elementAt(i);
						m_rosterChatDataList.addElement(t_data);								
					}
				}
		
				refreshRosterList();
										
				break;
			case 1:
				fetchChatRoster t_roster = new fetchChatRoster();
				t_roster.Import(in);
				
				boolean t_modified = false;
				
				synchronized (m_rosterChatDataList) {
					for(int i = 0 ;i < m_rosterChatDataList.size();i++){
						RosterChatData data = (RosterChatData)m_rosterChatDataList.elementAt(i);
						
						if(data.m_roster.equals(t_roster)){
							
							data.copyFrom(t_roster);
																
							t_modified = true;
							
							m_delayLoadChatRosterList.addElement(data);
							
							if(m_delayLoadChatRosterTimer == -1){
								
								m_delayLoadChatRosterTimer = m_mainApp.invokeLater(new Runnable() {
									public void run() {
										synchronized (m_delayLoadChatRosterList) {
											if(m_delayLoadChatRosterList.isEmpty()){
												m_mainApp.cancelInvokeLater(m_delayLoadChatRosterTimer);
												m_delayLoadChatRosterTimer = -1;
												return;
											}
											
											RosterChatData data = (RosterChatData)m_delayLoadChatRosterList.elementAt(0);
											m_delayLoadChatRosterList.removeElementAt(0);
											rangeRosterItemField(data);
										}
									}
								}, recvMain.fsm_delayLoadingTime, true);
							}							
							
							if(m_mainApp.getActiveScreen() == m_chatScreen
							&& m_chatScreen.getCurrRoster() == data){
								m_chatScreen.m_header.invalidate();
							}
							
							m_currMgr.invalidate();
							
							break;
						}
					}
					
					if(!t_modified){
						m_rosterChatDataList.addElement(new RosterChatData(t_roster));						
					}
				}						
				
				if(!t_modified){
					refreshRosterList();
				}
				
				break;
			}
		}catch(Exception e){
			m_mainApp.SetErrorString("PCRL:"+e.getMessage()+e.getClass().getName());
		}
				
	}
	
	private void rangeRosterItemField(RosterChatData _rosterData){
		
		Manager t_groupRosterMgr = getRosterGroupMgr(_rosterData.m_roster);
		
		int num = t_groupRosterMgr.getFieldCount();
		
		for(int j = 0;j < num;j++){
			RosterItemField t_field = (RosterItemField)t_groupRosterMgr.getField(j);
			if(_rosterData == t_field.m_currRoster){
				// delete the changed field and re-insert it by it's presence state 
				//
				t_groupRosterMgr.delete(t_field);
				
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
								
				insertRosterItemField(new RosterItemField(this,_rosterData,t_image,false,this));	
				
			}catch(Exception e){
				m_mainApp.SetErrorString("RRIF:"+e.getMessage()+e.getClass().getName());
			}
			
		}
	}
	
	/**
	 * get the group roster manager by roster group name
	 * @param _roster
	 * @return 
	 */
	private VerticalFieldManager getRosterGroupMgr(fetchChatRoster _roster){
		
		GroupFieldManager t_groupItemMgr = null; 
		
		synchronized (m_groupListMgr) {
			
			// search former group name list
			//
			int t_groupIdx = -1;
			for(int i = 0;i < m_groupNameList.size();i++){
				String groupName = (String)m_groupNameList.elementAt(i);
				if(groupName.equals(_roster.getGroup())){
					t_groupIdx = i;
					break;
				}
			}
			
			if(t_groupIdx == -1){
				// has NOT groups yet
				//
				t_groupItemMgr = new GroupFieldManager(_roster.getGroup());
				
				m_groupNameList.addElement(_roster.getGroup());
				m_groupRosterList.addElement(t_groupItemMgr);
				m_groupListMgr.add(t_groupItemMgr);
			}else{
				// get the added manager
				//
				t_groupItemMgr = (GroupFieldManager)m_groupRosterList.elementAt(t_groupIdx);
				
			}
		}
		
		return t_groupItemMgr.m_rosterItemList;
	}
	
	/**
	 * insert a roster item field to exist list by its presence
	 * @param _field
	 */
	private void insertRosterItemField(RosterItemField _field){
		
		VerticalFieldManager t_groupItemMgr = getRosterGroupMgr(_field.m_currRoster.m_roster);
		
		int t_insertPresence 	= _field.m_currRoster.m_roster.getPresence();
		String t_insertAccount 	= _field.m_currRoster.m_roster.getAccount();
		
		int num = t_groupItemMgr.getFieldCount();
		int index = 0;
		
		for(;index < num;index++){
			RosterItemField item = (RosterItemField)t_groupItemMgr.getField(index);
			if(t_insertPresence <= item.m_currRoster.m_roster.getPresence()){
				break;
			}
		}
		
		for(;index < num;index++){
							
			RosterItemField cmp = (RosterItemField)t_groupItemMgr.getField(index);
			
			int t_cmpPresenece	= cmp.m_currRoster.m_roster.getPresence();
			String t_cmpAccount	= cmp.m_currRoster.m_roster.getAccount();
			
			int t_cmpResult = t_cmpAccount.compareTo(t_insertAccount);
			
			if((t_cmpResult > 0 && t_cmpPresenece == t_insertPresence) || t_cmpPresenece != t_insertPresence){
				
				t_groupItemMgr.insert(_field,index);
				
				return;
			}
		}
		
		// add to rear
		//
		t_groupItemMgr.add(_field);
		
	}
	
	int m_delayRefreshRosterListTimerID 	= -1;
	Vector m_delayRefreshRosterListList 	= new Vector();
		
	private void refreshRosterList()throws Exception{
		
		m_mainApp.invokeLater(new Runnable() {
			
			public void run() {
				
				synchronized(m_delayRefreshRosterListList){
					
					m_groupListMgr.deleteAll();
					m_groupNameList.removeAllElements();
					m_groupRosterList.removeAllElements();
					m_delayRefreshRosterListList.removeAllElements();
					m_currFocusRosterItemField = null;
					
					try{
						addRosterListByPresence(fetchChatRoster.PRESENCE_AVAIL);
						addRosterListByPresence(fetchChatRoster.PRESENCE_AWAY);
						addRosterListByPresence(fetchChatRoster.PRESENCE_BUSY);
						addRosterListByPresence(fetchChatRoster.PRESENCE_FAR_AWAY);
						
						if(!m_mainApp.m_hideUnvailiableRoster){
							addRosterListByPresence(fetchChatRoster.PRESENCE_UNAVAIL);
						}	
					}catch(Exception e){
						m_mainApp.SetErrorString("RRL:"+e.getMessage()+e.getClass().getName());
					}					
				
					if(m_delayRefreshRosterListTimerID == -1){
						
						m_delayRefreshRosterListTimerID = m_mainApp.invokeLater(new Runnable() {
							public void run() {
								
								synchronized (m_delayRefreshRosterListList){
									
									if(m_delayRefreshRosterListList.isEmpty()){
										m_mainApp.cancelInvokeLater(m_delayRefreshRosterListTimerID);
										m_delayRefreshRosterListTimerID = -1;
										return ;
									}
									
									if(m_mainApp.m_weiboTimeLineScreen != null
										&& m_mainApp.m_weiboTimeLineScreen.isAddingWeibo()){
										return;
									}									
								
									try{
										RosterChatData t_roster = (RosterChatData)m_delayRefreshRosterListList.elementAt(0);
										
										WeiboHeadImage t_image = WeiboHeadImage.SearchHeadImage(m_headImageList, t_roster.m_roster.getAccount(), 
																						(byte)t_roster.m_roster.getStyle(), 
																						t_roster.m_roster.getHeadImageHashCode(), false);
																		
										getRosterGroupMgr(t_roster.m_roster).add(new RosterItemField(MainIMScreen.this,t_roster, t_image, false,MainIMScreen.this));
										
									}catch(Exception e){
										m_mainApp.SetErrorString("RRL:"+e.getMessage()+e.getClass().getName());
									}
									
									m_delayRefreshRosterListList.removeElementAt(0);
									
									if(m_delayRefreshRosterListList.isEmpty()){
										m_mainApp.cancelInvokeLater(m_delayRefreshRosterListTimerID);
										m_delayRefreshRosterListTimerID = -1;
									}
								}
							}
							
						},recvMain.fsm_delayLoadingTime,true);
					}
				}
			}
		});		
	}
		
	/**
	 * get the fetch roster head image
	 * @param _roster
	 * @return
	 * @throws Exception
	 */
	public WeiboHeadImage getHeadImage(fetchChatRoster _roster)throws Exception{
		return WeiboHeadImage.SearchHeadImage(m_headImageList, _roster.getAccount(),
				(byte)_roster.getStyle(),_roster.getHeadImageHashCode(), false);
	}
	
	private void addRosterListByPresence(int _presence)throws Exception{
		
		synchronized (m_rosterChatDataList) {
			
			for(int i = 0 ;i < m_rosterChatDataList.size();i++){
				RosterChatData t_rosterData = (RosterChatData)m_rosterChatDataList.elementAt(i);
				if(t_rosterData.m_roster.getPresence() == _presence){
					m_delayRefreshRosterListList.addElement(t_rosterData);
				}
			}	
		}
		
	}
	
	long m_refreshRosterTimer = 0;
	
	public void sendRequestRosterListMsg(boolean _prompt){
		
		final long t_currTime = System.currentTimeMillis();
		if(Math.abs(m_refreshRosterTimer - t_currTime) < 5 * 6000){
			if(_prompt){
				m_mainApp.DialogAlert(recvMain.sm_local.getString(yblocalResource.IM_REFRESH_MIN_TIME_PROMPT));
			}			
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
					
					if(m_chatScreen.getCurrRoster() == t_data){
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
							&& m_chatScreen.getCurrRoster() == data){
							
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
	
	public void sendAddRosterMsg(int _style,String _addr,String _name,String _group){
		try{
			
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			os.write(msg_head.msgChatAddRoster);
			os.write(_style);
			
			sendReceive.WriteString(os,_addr);
			sendReceive.WriteString(os,_name);
			sendReceive.WriteString(os,_group);
			
			m_mainApp.m_connectDeamon.addSendingData(msg_head.msgChatAddRoster, os.toByteArray(), true);
			
		}catch(Exception e){
			m_mainApp.SetErrorString("SUP:"+e.getMessage()+e.getClass().getName());
		}
	}
	
	public void sendUseStatus(IMStatus _status){
		
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
	

	public void sendRosterAliasName(RosterChatData _roster,String _aliasName){
		try{
			
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			os.write(msg_head.msgChatAlias);
			os.write(_roster.m_roster.getStyle());
			sendReceive.WriteString(os, _roster.m_roster.getOwnAccount());
			sendReceive.WriteString(os, _roster.m_roster.getAccount());
			sendReceive.WriteString(os,_aliasName);
			
			m_mainApp.m_connectDeamon.addSendingData(msg_head.msgChatAlias, os.toByteArray(), true);
			
		}catch(Exception e){
			m_mainApp.SetErrorString("SRAN:"+e.getMessage()+e.getClass().getName());
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

					m_historyChatMgr.insert(new RosterItemField(this,_rosterData,t_image,true,this),0);
					
				}catch(Exception e){
					m_mainApp.SetErrorString("ASCM:"+e.getMessage()+e.getClass().getName());
				}
			}
		}
	}
	
	public boolean selectedByKeyword(String _keyword,int _index){
		
		synchronized (m_groupListMgr){
			
			_keyword = _keyword.toLowerCase();
			
			int t_addIndex = 0;
			
			int num = m_groupListMgr.getFieldCount();
			for(int i = 0 ;i < num;i++){
				GroupFieldManager t_groupField = (GroupFieldManager)m_groupListMgr.getField(i);
				
				// each group
				//
				int t_groupNum = t_groupField.m_rosterItemList.getFieldCount();
				
				for(int j = 0;j < t_groupNum;j++){
				
					RosterItemField t_field = (RosterItemField )t_groupField.m_rosterItemList.getField(j);
					
					if(t_field.m_currRoster.m_roster.getName().indexOf(_keyword) != -1
						|| t_field.m_currRoster.m_roster.getAccount().indexOf(_keyword) != -1){
											
						if(t_addIndex++ == _index){
							
							if(t_groupField.m_isOpen){
								t_field.setFocus();
							}else{
								t_groupField.m_titleField.setFocus();
							}
							
							return true;
						}
					}
				}
				
			}
			
			return false;			
		}
	}
	
	/**
	 * group field manager
	 * @author yuch
	 *
	 */
	public class GroupFieldManager extends VerticalFieldManager implements FieldChangeListener{
		
		final int fm_titleHeight;
				
		String m_groupName;
		
		VerticalFieldManager m_rosterItemList = new VerticalFieldManager(Manager.VERTICAL_SCROLL | Manager.VERTICAL_SCROLLBAR);
		
		boolean m_isOpen		= true; 
		
		/**
		 * replace field with roster item list manager
		 */
		NullField	m_nullField = new NullField(Field.NON_FOCUSABLE);
		
		/**
		 * title field
		 */
		Field m_titleField = new Field(Field.FOCUSABLE){

			public int getPreferredWidth(){
				return recvMain.fsm_display_width;
			}
			
			public int getPreferredHeight() {
				return MainIMScreen.fsm_defaultFontHeight + 5;
			}
			
			protected void layout(int paramInt1, int paramInt2) {
				setExtent(getPreferredWidth(),getPreferredHeight());
			}

			protected void paint(Graphics _g) {
				drawFocus(_g, isFocus());			
			}
			
			protected void onUnfocus(){
			    super.onUnfocus();
			    invalidate();
			}
			
			protected void drawFocus(Graphics _g,boolean _on){
				
				if(_on){
					m_selectGroupTitleField = GroupFieldManager.this;
					m_currFocusRosterItemField = null;
				}
				
				int color = _g.getColor();
				try{
					_g.setColor(fsm_groupBackgroundColor);
					_g.fillRect(0, 0, getPreferredWidth(), getPreferredHeight());
					
					_g.setColor(fsm_backgroundColor);
					_g.drawLine(0, getPreferredHeight() - 1, getPreferredWidth(), getPreferredHeight() - 1);
					
					if(_on){
						// draw selected backgroud
						//
						WeiboHeadImage.drawSelectedImage(_g, getPreferredWidth(), getPreferredHeight());
					}
					
					_g.setColor(RosterItemField.fsm_groupTitleTextColor);
					
					if(m_isOpen){
						_g.drawText(" - " + m_groupName,0,2);
					}else{
						_g.drawText(" + " + m_groupName,0,2);
					}
				}finally{
					_g.setColor(color);
				}
			}
			
		};
		
		public GroupFieldManager(String _groupName){
			super(Manager.VERTICAL_SCROLL | Manager.VERTICAL_SCROLLBAR);
			
			fm_titleHeight 	= getFont().getHeight() + 5;
			m_groupName 	= _groupName;
			
			add(m_titleField);
			add(m_rosterItemList);
			
			m_titleField.setChangeListener(this);
		}
		
		// m_topRosterMenu /m_bottomRosterMenu will call this function 
		//
		public void setFocus(){
			m_titleField.setFocus();
		}

		public void fieldChanged(Field paramField, int paramInt) {
			m_isOpen = !m_isOpen;
			
			if(m_isOpen){
				replace(m_nullField, m_rosterItemList);
			}else{
				replace(m_rosterItemList, m_nullField);
			}
		}
		
		public Field getFieldWithFocus(){
		
			if(m_titleField.isFocus()){
				return m_titleField;
			}
			
						
			return m_rosterItemList.getFieldWithFocus();
		}
		
	}
}
