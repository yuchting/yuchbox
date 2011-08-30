package com.yuchting.yuchberry.client.im;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.component.TextField;
import net.rim.device.api.ui.container.MainScreen;

import com.yuchting.yuchberry.client.recvMain;


public class MainIMScreen extends MainScreen{

	// BasicEditField for 4.2os
	public static TextField 	sm_testTextArea	= new TextField(Field.READONLY){
		public void setText(String _text){
			super.setText(_text);
			layout(RosterItemField.fsm_rosterItemFieldWidth,1000);
		}
	};
	
	public final static Font		sm_defaultFont	= sm_testTextArea.getFont(); 
	
		
	recvMain	m_mainApp = null;
	
	public MainIMScreen(recvMain _mainApp){
		m_mainApp = _mainApp;
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
