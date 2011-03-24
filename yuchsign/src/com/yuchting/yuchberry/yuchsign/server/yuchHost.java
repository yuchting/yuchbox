package com.yuchting.yuchberry.yuchsign.server;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.w3c.dom.Element;


@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class yuchHost {

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	public String m_hostName = "";
	
	@Persistent
	public String m_connectHost = "";
	
	@Persistent
	public int m_httpPort = 4929;
	
	@Persistent
	public String m_httpPassword = "";
	
	@Persistent
	public String m_recommendHost = "";
	
	public void OutputXMLData(StringBuffer _buffer){
		if(m_connectHost == null){
			m_connectHost = "";
		}
		_buffer.append("<Host ").append("name=\"").append(m_hostName)
								.append("\" host=\"").append(m_connectHost)
								.append("\" port=\"").append(Integer.toString(m_httpPort))
								.append("\" pass=\"").append(m_httpPassword)
								.append("\" recom=\"").append(m_recommendHost)
				.append("\" />");
	}
	
	public void InputXMLData(final Element _elem)throws Exception{
		
		m_hostName		= yuchbber.ReadStringAttr(_elem,"name");
		m_connectHost	= yuchbber.ReadStringAttr(_elem,"host");
		m_httpPort		= yuchbber.ReadIntegerAttr(_elem,"port");
		m_httpPassword	= yuchbber.ReadStringAttr(_elem,"pass");
		m_recommendHost	= yuchbber.ReadStringAttr(_elem,"recom");
	}
}
