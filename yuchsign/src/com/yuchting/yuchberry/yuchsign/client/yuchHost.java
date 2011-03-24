package com.yuchting.yuchberry.yuchsign.client;

import com.google.gwt.xml.client.Element;

public class yuchHost {

	public String m_hostName = "";
	
	public String m_connectHost = "";
	
	public int m_httpPort = 4929;
	
	public String m_httpPassword = "";
	
	public String m_recommendHost = "";
	
	public void OutputXMLData(StringBuffer _buffer){
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
	
	public int compareTo(yuchHost o) {
      return (o == null || o.m_hostName == null) ? -1 
    		  : -o.m_hostName.compareTo(m_hostName);
    }

    @Override
    public boolean equals(Object o) {
      if (o instanceof yuchHost) {
        return m_hostName.equals(((yuchHost) o).m_hostName);
      }
      return false;
    }
}
