package com.yuchting.yuchberry.server.frame;

import org.dom4j.Element;

import com.yuchting.yuchberry.server.fetchMgr;

public class yuchIM {

	public String m_typeName = "gtalk";
	public String m_accoutName = "";
	public String m_password = "";
	
	public void OuputXMLData(final StringBuffer _output){
		_output.append("\t<IMAccount ").append("type=\"").append(m_typeName).
				append("\" account=\"").append(m_accoutName).
				append("\" pass=\"").append(mainFrame.prepareXmlAttr(m_password)).
				append("\" />\n");
	}
	
	public void InputXMLData(final Element _elem)throws Exception{
		m_typeName 		= yuchbber.ReadStringAttr(_elem, "type");
		m_accoutName	= yuchbber.ReadStringAttr(_elem, "account");
		m_password		= yuchbber.ReadStringAttr(_elem, "pass");
	}
	
	public String toString(){
		return m_typeName + " IM<" + m_accoutName + ">"; 
	}
}
