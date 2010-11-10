package com.yuchting.yuchberry.client;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.RichTextField;
import net.rim.device.api.ui.container.MainScreen;

public class textViewScreen extends fileViewScreen{

	RichTextField m_editText	= null;
	
	public textViewScreen(String _filename,recvMain _mainApp)throws Exception{
		
		super(_filename,_mainApp,true);
		
		m_editText = new RichTextField(new String(m_fileContain));
		add(m_editText);
		
	}

}
