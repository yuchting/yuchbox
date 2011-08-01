package com.yuchting.yuchberry.yuchsign.client.account;


import com.google.gwt.xml.client.Element;

public class yuchWeibo {


	public yuchbber m_yuchbber; 

	public String m_typeName = "sina";
	

	public String m_accoutName = "";
	

	public String m_accessToken = "";
	

	public String m_secretToken = "";	
	

	public int m_timelineSum = 20;
	

	public int m_directMsgSum = 2;
	

	public int m_atMeSum = 2;
	

	public int m_commentMeSum = 2;
	
	public void OuputXMLData(final StringBuffer _output){
		_output.append("\t<WeiboAccount ").append("type=\"").append(m_typeName).
				append("\" account=\"").append(m_accoutName).
				append("\" accessToken=\"").append(m_accessToken).
				append("\" secretToken=\"").append(m_secretToken).
				append("\" timelineSum=\"").append(m_timelineSum).
				append("\" directMessageSum=\"").append(m_directMsgSum).
				append("\" atMeSum=\"").append(m_atMeSum).
				append("\" commentSum=\"").append(m_atMeSum).
				append("\" />\n");
	}
	
	public void InputXMLData(final Element _elem)throws Exception{
		m_typeName 		= yuchbber.ReadStringAttr(_elem, "type");
		m_accoutName	= yuchbber.ReadStringAttr(_elem, "account");
		m_accessToken	= yuchbber.ReadStringAttr(_elem, "accessToken");
		m_secretToken	= yuchbber.ReadStringAttr(_elem, "secretToken");
		
		m_timelineSum	= yuchbber.ReadIntegerAttr(_elem, "timelineSum");
		m_directMsgSum	= yuchbber.ReadIntegerAttr(_elem, "directMessageSum");
		m_atMeSum		= yuchbber.ReadIntegerAttr(_elem, "atMeSum");
		m_commentMeSum	= yuchbber.ReadIntegerAttr(_elem, "commentSum");
	}
	
	public String toString(){
		return m_typeName + " Weibo<" + m_accoutName + ">"; 
	}
	
}
