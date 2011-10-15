package com.yuchting.yuchberry.client.weibo;

import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.component.BasicEditField;

public class ContentTextField extends BasicEditField{
	
	int		m_textWidth;
	
	public ContentTextField(){
		super(BasicEditField.READONLY);
	}
	
	public void setTextWidth(int _width){
		m_textWidth = _width;
	}
	
	public int getTextWidth(){
		return m_textWidth;
	}
	
	public void setText(String _text){
		super.setText(_text);
		layout(m_textWidth,1000);
	}
	
	public void paint(Graphics _g){
		super.paint(_g);
	}
};
