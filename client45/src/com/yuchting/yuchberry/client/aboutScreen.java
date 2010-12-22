package com.yuchting.yuchberry.client;

import local.localResource;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.RichTextField;
import net.rim.device.api.ui.container.MainScreen;

public class aboutScreen extends MainScreen{
	
	RichTextField 	m_editText	= null;
	recvMain		m_mainApp	= null;
	
	MenuItem 	m_refreshMenu = new MenuItem(recvMain.sm_local.getString(localResource.REFRESH_ABOUT_MENU_TEXT), 100, 10) {
									public void run() {
										recvMain t_app = (recvMain)UiApplication.getUiApplication();
										t_app.m_connectDeamon.SendAboutInfoQuery(true);
									}
								};
	
	public aboutScreen(recvMain _mainApp){
		m_mainApp = _mainApp;
		
		m_editText = new RichTextField(m_mainApp.m_aboutString);
		add(m_editText);
	}
	
	public boolean onClose(){
		close();
		m_mainApp.m_aboutScreen = null;
		
		return true;
	}
	
	public void RefreshText(){
		m_editText.setText(m_mainApp.m_aboutString);
		invalidate();
	}
}
