package com.yuchting.yuchberry.client.im;

import local.localResource;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.yuchting.yuchberry.client.recvMain;

public class IMAliasDlg extends PopupScreen {
	
	RosterItemField m_currRosterField = null;
	EditField		m_name			= null; 
	
	public IMAliasDlg(RosterItemField _roster) {
		super(new VerticalFieldManager());
		 
		m_currRosterField = _roster;
		
		m_name = new EditField(recvMain.sm_local.getString(localResource.IM_ALIAS_ROSTER_DLG_NAME),
							m_currRosterField.m_currRoster.m_roster.getName(),128, EditField.FILTER_DEFAULT);
		m_name.select(true);
		m_name.setCursorPosition(m_name.getTextLength());
		m_name.select(false);
		
	}

}
