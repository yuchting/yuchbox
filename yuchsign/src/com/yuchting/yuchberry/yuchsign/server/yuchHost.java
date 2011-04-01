package com.yuchting.yuchberry.yuchsign.server;

import java.io.Serializable;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.w3c.dom.Element;


@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class yuchHost implements Serializable,Cloneable{

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private String m_hostName = "";
	
	@Persistent
	private String m_connectHost = "";
	
	@Persistent
	private int m_httpPort = 4929;
	
	@Persistent
	private String m_httpPassword = "";
	
	@Persistent
	private String m_recommendHost = "";
	
	public String GetHostName(){return m_hostName;}
	public void SetHostName(String _name){m_hostName = _name;}
	
	public String GetConnectHost(){return m_connectHost;}
	public void SetConnectHost(String _host){m_connectHost = _host;}
	
	public int GetHTTPPort(){return m_httpPort;}
	public void SetHTTPPort(int _port){m_httpPort = _port;}
	
	public String GetHTTPPass(){return m_httpPassword;}
	public void SetHTTPPass(String _pass){m_httpPassword = _pass;}
	
	public String GetRecommendHost(){return m_recommendHost;}
	public void SetRecommendHost(String _recommendHost){m_recommendHost = _recommendHost;}
	
	
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
	
	public Object clone()throws CloneNotSupportedException{
		yuchHost t_host = (yuchHost)super.clone();
		
//		t_host.m_hostName 		= new String(m_hostName);
//		t_host.m_connectHost 	= new String(m_connectHost);
//		t_host.m_httpPassword 	= new String(m_httpPassword);
//		t_host.m_recommendHost	= new String(m_recommendHost);
		
		return t_host;
	}
}
