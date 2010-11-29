package com.yuchting.yuchberry.client;

import local.localResource;
import net.rim.device.api.ui.component.RichTextField;
import net.rim.device.api.ui.container.MainScreen;

public class aboutScreen extends MainScreen{
	
	RichTextField m_editText	= null;
	
	public aboutScreen(){
		
		m_editText = new RichTextField(new String(recvMain.sm_local.getString(localResource.ABOUT_DESC)));
		add(m_editText);
	}
	
	public boolean onClose(){
		close();
		return true;
	}
}
