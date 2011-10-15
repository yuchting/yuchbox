package com.yuchting.yuchberry.client.weibo;

import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.component.TextField;

public class ContentTextField extends TextField{
	
	int		m_textWidth;
	
	public ContentTextField(){
		super(TextField.READONLY);
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
