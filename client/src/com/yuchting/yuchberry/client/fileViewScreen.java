package com.yuchting.yuchberry.client;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.MainScreen;

public class fileViewScreen extends MainScreen{
	
	String 	m_viewFileName		= null;
	
	recvMain	m_mainApp		= null;
	LabelField	m_pathText		= null;
	
	byte[]		m_fileContain	= null;
	
	public fileViewScreen(String _filename,recvMain _mainApp,boolean _readFile) throws Exception{
		m_viewFileName = _filename;
		m_mainApp = _mainApp;
		
		// prepare the image
		//
		m_pathText = new LabelField(_filename,LabelField.ELLIPSIS | LabelField.USE_ALL_WIDTH);
		add(m_pathText);
		
		if(_readFile){
			FileConnection t_fileRead = (FileConnection)Connector.open(_filename,Connector.READ);
			if(!t_fileRead.exists()){
				t_fileRead.close();
				throw new Exception(_filename + " file is not exist!");
			}
			m_fileContain = new byte[(int)t_fileRead.fileSize()];
			sendReceive.ForceReadByte(t_fileRead.openInputStream(), m_fileContain, m_fileContain.length);
			t_fileRead.close();
		}

	}

	public boolean onClose(){
		close();
		return true;
	}
	
}
