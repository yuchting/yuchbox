package com.yuchting.yuchberry.yuchsign.shared;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

import com.google.appengine.api.datastore.Key;
import com.google.gwt.xml.client.Element;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class yuchEmail {
	
	@PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key m_key;
	
	@Persistent
	public String m_emailAddr = "";
	
	@Persistent
	public String m_password = "";
	
	@Persistent
	public yuchbber m_yuchbber; 
	
	@Persistent
	public boolean m_fullnameSignIn = false;
	
	@Persistent
	public String m_protocol = "imaps";
	
	@Persistent
	public String m_host = "";
	
	@Persistent
	public int m_port = 993;
		
	@Persistent
	public String m_host_send = "";
	
	@Persistent
	public int m_port_send = 587;
	
	@Persistent
	public boolean m_appendHTML = false;
	
	
	public void OuputXMLData(final StringBuffer _output){
		
		_output.append("\t<email ").append("account=\"").append(m_emailAddr).
									append("\" pass=\"").append(m_password).
									append("\" full=\"").append(m_fullnameSignIn?1:0).
									append("\" protocal=\"").append(m_protocol).
									append("\" host=\"").append(m_host).
									append("\" port=\"").append(m_port).
									append("\" hosts=\"").append(m_host_send).
									append("\" ports=\"").append(m_port_send).
									append("\" appHTML=\"").append(m_appendHTML?1:0).
									append("\">\n");
	}
	
	public void InputXMLData(final Element _elem)throws Exception{
				
		m_emailAddr		= yuchbber.ReadStringAttr(_elem, "account");
		m_password		= yuchbber.ReadStringAttr(_elem, "pass");
		m_fullnameSignIn= yuchbber.ReadBooleanAttr(_elem, "full");
		m_protocol		= yuchbber.ReadStringAttr(_elem, "protocal");
		
		m_host			= yuchbber.ReadStringAttr(_elem, "host");
		m_port			= yuchbber.ReadIntegerAttr(_elem, "port");
		
		m_host_send		= yuchbber.ReadStringAttr(_elem, "hosts");
		m_port_send		= yuchbber.ReadIntegerAttr(_elem, "ports");
		
		m_appendHTML	= yuchbber.ReadBooleanAttr(_elem, "appHTML");		
	}
	
}
