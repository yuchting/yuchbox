package com.yuchting.yuchberry.client;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import net.rim.device.api.math.Fixed32;
import net.rim.device.api.system.Display;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.MainScreen;

public class imageViewScreen extends fileViewScreen{

	BitmapField	m_image	= null;
	
	public imageViewScreen(String _name,recvMain _mainApp)throws Exception{
		
		super(_name,_mainApp,true);
		
		m_image = new BitmapField();
		EncodedImage image = EncodedImage.createEncodedImage(m_fileContain, 0, m_fileContain.length);
		
		final int scale = Fixed32.div(Fixed32.toFP(image.getWidth()),Fixed32.toFP(Display.getWidth()));
		
		m_image.setImage(image.scaleImage32(scale,scale));
		add(m_image);
	}	
}
