package com.yuchting.yuchberry.client.ui;


//BasicEditField for 4.2os
public class BubbleTextField extends WeiboTextField{

	int m_textWidth = 0;
	
	public BubbleTextField(){
		super(0,0);
	}
	
	public void setText(String _text){
		super.setText(_text);
		this.layout(m_textWidth,1000);
	}
	
	public void setPreferredWidth(int _width){
		m_textWidth = _width;
	}
}
