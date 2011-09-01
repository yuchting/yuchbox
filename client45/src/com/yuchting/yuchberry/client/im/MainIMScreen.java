package com.yuchting.yuchberry.client.im;

import java.util.Vector;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.component.TextField;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.yuchting.yuchberry.client.recvMain;
import com.yuchting.yuchberry.client.ui.SliderHeader;
import com.yuchting.yuchberry.client.ui.WeiboHeadImage;


final class RosterChatData{
	
	public fetchChatRoster		m_roster;
	public Vector				m_chatList = new Vector();
	
	public String				m_lastChatText = "";
	
	public RosterChatData(fetchChatRoster _roster){
		m_roster = _roster;
	}
}

public class MainIMScreen extends MainScreen{

	// BasicEditField for 4.2os
	public static TextField 	sm_testTextArea	= new TextField(Field.READONLY){
		public void setText(String _text){
			super.setText(_text);
			this.layout(ChatField.fsm_textWidth,1000);
		}
	};
	
	public final static Font		sm_defaultFont			= sm_testTextArea.getFont();
	public final static int		sm_defaultFontHeight	= sm_defaultFont.getHeight(); 
	
	public final static Font		sm_boldFont				= sm_defaultFont.derive(sm_defaultFont.getStyle() | Font.BOLD);
		
	Vector					m_headImageList = new Vector();
	
	MainIMScreenHeader		m_header = null;
	recvMain				m_mainApp = null;
	
	
	VerticalFieldManager	m_historyChatMgr = new VerticalFieldManager(Manager.VERTICAL_SCROLL | Manager.VERTICAL_SCROLLBAR);
	Vector					m_historyChatMgrList = new Vector();
	
	VerticalFieldManager	m_rosterListMgr = new VerticalFieldManager(Manager.VERTICAL_SCROLL | Manager.VERTICAL_SCROLLBAR);
	Vector					m_rosterMgrList = new Vector();
	
	VerticalFieldManager	m_currMgr	= null;
	
	
	Vector					m_rosterChatDataList = new Vector();	
	
	public MainChatScreen	m_chatScreen = null;
	
	public MainIMScreen(recvMain _mainApp){
		super(Manager.VERTICAL_SCROLL);
		
		m_mainApp = _mainApp;
		m_header = new MainIMScreenHeader(this);
		m_chatScreen = new MainChatScreen(_mainApp,this);
		
		//@{ test code
		for(int i = 0 ;i < 10;i++){
			fetchChatRoster t_roster = new fetchChatRoster();
			
			t_roster.setAccount("yuchting@gmail.com");
			t_roster.setName("YuchTing");
			t_roster.setStyle(fetchChatMsg.STYLE_GTALK);
			
			try{
				WeiboHeadImage t_image = WeiboHeadImage.SearchHeadImage(m_headImageList, t_roster.getAccount(), (byte)t_roster.getStyle(), 
						t_roster.getHeadImageHashCode(), false);
				
				m_historyChatMgr.add(new RosterItemField(t_roster, t_image, true));
				
			}catch(Exception e){
				m_mainApp.SetErrorString("MIMS:"+e.getMessage() + e.getClass().getName());
			}
			
			m_historyChatMgrList.addElement(t_roster);
			
			RosterChatData t_data = new RosterChatData(t_roster);
			for(int j = 0 ;j < 10;j++){
				
				fetchChatMsg t_msg = new fetchChatMsg();
				t_msg.setOwner(t_roster.getAccount());
				
				t_msg.setMsg("笑多了又不会怀孕笑多了又不会怀孕笑多了又不会怀孕笑多了又不会怀孕[哈哈]");
				
				t_msg.setIsOwnMsg(j % 2 == 0);
				
				t_data.m_chatList.addElement(t_msg);
				
			}
			m_rosterChatDataList.addElement(t_data);
		}
		
		add(m_historyChatMgr);
		//@}		
		
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
    		return false;
    		
    	}else{
    		
    		m_mainApp.popScreen(this);
    		m_mainApp.pushStateScreen();
    		
    		return false;
    	}
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
			}else{
				return;
			}
			break;
		}
	}
	
	

}
