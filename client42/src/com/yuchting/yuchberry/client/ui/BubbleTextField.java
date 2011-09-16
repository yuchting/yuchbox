package com.yuchting.yuchberry.client.ui;

import net.rim.device.api.ui.component.BasicEditField;

//BasicEditField for 4.2os
public class BubbleTextField extends BasicEditField{

	int m_textWidth = 0;
	
	public BubbleTextField(long _style){
		super(_style);
	}
	
	public void setText(String _text){
		super.setText(_text);
		this.layout(m_textWidth,1000);
	}
	
	public void setPreferredWidth(int _width){
		m_textWidth = _width;
	}
}
