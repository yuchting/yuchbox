package com.yuchting.yuchberry.client;

import java.util.Hashtable;

import net.rim.device.api.io.IOUtilities;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.xml.jaxp.RIMSAXParser;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

class ImageUnit{
	public int m_x;
	public int m_y;
	public int m_width;
	public int m_height;
	
	
}
public class ImageSets {

	Hashtable	m_mapImageUnits = new Hashtable();
	
	String		m_name		= "";
	Bitmap		m_fullImage = null;
	
	public ImageSets(final String _imageSets)throws Exception{
		
		RIMSAXParser t_xml = new RIMSAXParser();
		t_xml.parse(UiApplication.getUiApplication().getClass().getResourceAsStream(_imageSets),new DefaultHandler(){
			 public void startElement(String uri, String localName, String qName, Attributes attributes){
				 
				 if(localName.equals("Imageset")){
					 m_name = attributes.getValue("Name");
					 
					 try{
						 byte[] bytes = IOUtilities.streamToBytes(UiApplication.getUiApplication().getClass()
								 .getResourceAsStream(attributes.getValue("Imagefile")));		
						 m_fullImage =  EncodedImage.createEncodedImage(bytes, 0, bytes.length).getBitmap();
					 }catch(Exception e){
						 ((recvMain)UiApplication.getUiApplication()).DialogAlertAndExit("inner load image error " + _imageSets);
					 }
				 }
				 
				 if(localName.equals("Image")){
					 ImageUnit t_unit = new ImageUnit();
					 //t_unit.m_x = Integer.valueOf(attributes.getValue("Name"))
					 m_mapImageUnits.put(attributes.getValue("Name"),t_unit);
				 }							 
			 }
		});
		
	}
}
