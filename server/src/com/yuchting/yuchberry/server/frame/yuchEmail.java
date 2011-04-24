package com.yuchting.yuchberry.server.frame;

import org.dom4j.Element;


public class yuchEmail {
	

	public String m_emailAddr = "";
	
	public String m_username = "";

	public String m_password = "";
	

	public yuchbber m_yuchbber; 
	

	public boolean m_fullnameSignIn = false;
	

	public String m_protocol = "imaps";
	

	public String m_host = "";
	

	public int m_port = 993;
		

	public String m_host_send = "";
	

	public int m_port_send = 587;
	
	public boolean m_appendHTML = false;
	
	
	public void OuputXMLData(final StringBuffer _output){
		
		if(m_username == null){
			m_username = "";
		}
		
		_output.append("\t<email ").append("account=\"").append(m_emailAddr).
									append("\" name=\"").append(m_username).
									append("\" pass=\"").append(m_password).
									append("\" full=\"").append(m_fullnameSignIn?1:0).
									append("\" protocal=\"").append(m_protocol).
									append("\" host=\"").append(m_host).
									append("\" port=\"").append(m_port).
									append("\" hosts=\"").append(m_host_send).
									append("\" ports=\"").append(m_port_send).
									append("\" appHTML=\"").append(m_appendHTML?1:0).
									append("\" />\n");
	}
	
	public void InputXMLData(final Element _elem)throws Exception{
				
		m_emailAddr		= yuchbber.ReadStringAttr(_elem, "account");
		m_username		= yuchbber.ReadStringAttr(_elem,"name");
		m_password		= yuchbber.ReadStringAttr(_elem, "pass");
		m_fullnameSignIn= yuchbber.ReadBooleanAttr(_elem, "full");
		m_protocol		= yuchbber.ReadStringAttr(_elem, "protocal");
		
		m_host			= yuchbber.ReadStringAttr(_elem, "host");
		m_port			= yuchbber.ReadIntegerAttr(_elem, "port");
		
		m_host_send		= yuchbber.ReadStringAttr(_elem, "hosts");
		m_port_send		= yuchbber.ReadIntegerAttr(_elem, "ports");
		
		m_appendHTML	= yuchbber.ReadBooleanAttr(_elem, "appHTML");
	}
	
	public String toString(){
		return "Email <" + m_emailAddr + ">"; 
	}
	
}
