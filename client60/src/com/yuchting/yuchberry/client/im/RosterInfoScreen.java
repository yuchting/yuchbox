package com.yuchting.yuchberry.client.im;

import local.localResource;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.component.TextField;
import net.rim.device.api.ui.container.MainScreen;

import com.yuchting.yuchberry.client.recvMain;

public class RosterInfoScreen extends MainScreen {

	MainIMScreen	m_mainScreen;
	
	LabelField		m_accName = null;
	TextField		m_addr	= null;
	
	TextField		m_source = null;
	TextField		m_status = null;
	
	RosterChatData	m_currRoster = null;
	
		
	public RosterInfoScreen(MainIMScreen _screen,RosterChatData _roster){
		super(Manager.VERTICAL_SCROLL);
		m_mainScreen = _screen;
		
		m_currRoster = _roster;
		
		m_accName = new LabelField(_roster.m_roster.getName());
		m_accName.setFont(MainIMScreen.fsm_boldFont);
		add(m_accName);
		
		add(new SeparatorField());
		
		m_addr = new TextField(Field.READONLY);
		m_addr.setLabel(recvMain.sm_local.getString(localResource.IM_ROSTER_INFO_ADDR));
		m_addr.setText(_roster.m_roster.getAccount());
		add(m_addr);
		
		add(new SeparatorField());
		
		m_source = new TextField(Field.READONLY);
		m_source.setLabel(recvMain.sm_local.getString(localResource.IM_ROSTER_INFO_SOURCE));
		m_source.setText(_roster.m_roster.getOwnAccount() + " " + _roster.m_roster.getSource());
		add(m_source);
		
		add(new SeparatorField());
		
		m_status = new TextField(Field.READONLY);
		m_status.setLabel(recvMain.sm_local.getString(localResource.IM_ROSTER_INFO_STATUS));
		m_status.setText(_roster.m_roster.getStatus());
		
		add(m_status);
		
	}
	
	public void paint(Graphics _g){
		super.paint(_g);
		
		RosterItemField.drawChatSign(_g,recvMain.fsm_display_width,recvMain.fsm_display_height,
				m_currRoster.m_roster.getStyle(),m_currRoster.m_isYuch);		
	}
	
	public boolean onClose(){
		close();
		
		return true;
	}
	
	public void close(){
		m_mainScreen.m_checkRosterInfoScreen = null;
		super.close();
	}
}
