package com.yuchting.yuchberry.client;

import net.rim.device.api.ui.component.RichTextField;
import net.rim.device.api.ui.container.MainScreen;

public class aboutScreen extends MainScreen{
	
	RichTextField 	m_editText	= null;
	recvMain		m_mainApp	= null;
	
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
