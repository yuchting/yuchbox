package com.yuchting.yuchberry.client;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import net.rim.device.api.math.Fixed32;
import net.rim.device.api.system.Display;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.MainScreen;

public class imageViewScreen extends MainScreen{
	
	String 	m_viewImageName		= null;
	
	recvMain	m_mainApp		= null;
	LabelField	m_pathText		= null;
	BitmapField	m_image	= null;
	
	public imageViewScreen(String _name,recvMain _mainApp)throws Exception{
		m_viewImageName = _name;	
		m_mainApp = _mainApp;
		
		// prepare the image
		//
		m_pathText = new LabelField(_name,LabelField.ELLIPSIS | LabelField.USE_ALL_WIDTH);
		add(m_pathText);
		
		FileConnection t_fileRead = (FileConnection)Connector.open(_name,Connector.READ);
		if(!t_fileRead.exists()){
			throw new Exception(_name + " file is not exist!");
		}
		byte[] t_fileContent = new byte[(int)t_fileRead.fileSize()];
		sendReceive.ForceReadByte(t_fileRead.openInputStream(), t_fileContent, t_fileContent.length);

		m_image = new BitmapField();
		EncodedImage image = EncodedImage.createEncodedImage(t_fileContent, 0, t_fileContent.length);
		
		final int scale = Fixed32.div(Fixed32.toFP(image.getWidth()),Fixed32.toFP(Display.getWidth()));
		
		m_image.setImage(image.scaleImage32(scale,scale));
		add(m_image);
	}
	
	public boolean onClose(){
		close();
		return true;
	}
	
}
