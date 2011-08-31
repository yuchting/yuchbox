package com.yuchting.yuchberry.client.im;

import java.util.Vector;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.component.TextField;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.yuchting.yuchberry.client.recvMain;
import com.yuchting.yuchberry.client.ui.WeiboHeadImage;


public class MainIMScreen extends MainScreen{

	// BasicEditField for 4.2os
	public static TextField 	sm_testTextArea	= new TextField(Field.READONLY){
		public void setText(String _text){
			super.setText(_text);
			layout(RosterItemField.fsm_rosterItemFieldWidth,1000);
		}
	};
	
	public final static Font		sm_defaultFont	= sm_testTextArea.getFont();
	public final static int		sm_defaultFontHeight	= sm_defaultFont.getHeight(); 
	
	public final static Font		sm_boldFont		= sm_defaultFont.derive(sm_defaultFont.getStyle() | Font.BOLD);
		
	Vector					m_headImageList = new Vector();
	
	MainIMScreenHeader		m_header = null;
	recvMain				m_mainApp = null;
	
	VerticalFieldManager	m_historyChatMgr = new VerticalFieldManager(Manager.VERTICAL_SCROLL);
	
	public MainIMScreen(recvMain _mainApp){
		m_mainApp = _mainApp;
		
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
		}
		
		add(m_historyChatMgr);
		
		//@}
		m_header = new MainIMScreenHeader(this);
		setTitle(m_header);
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
	
	

}
