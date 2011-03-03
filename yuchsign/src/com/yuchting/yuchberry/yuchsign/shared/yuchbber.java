package com.yuchting.yuchberry.yuchsign.shared;

import java.util.Date;
import java.util.Vector;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public final class yuchbber {
	
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private String m_signinName = "";
	
	@Persistent
	private String m_password = "";
	
	@Persistent
	private long m_usingHours = 72;
	
	@Persistent
	private long m_createTime = 0;
	
	@Persistent
	private int m_serverPort = 0;
	
	@Persistent
	private int m_pushInterval = 30;
	
	@Persistent
	private boolean m_usingSSL = false;
	
	@Persistent
	private boolean m_convertSimpleChar = false;
	
	@Persistent(mappedBy = "m_yuchbber")
	@javax.jdo.annotations.Element(dependent = "true")
	private Vector<yuchEmail>	m_emailList = new Vector<yuchEmail>();
	
	public yuchbber(final String _name,final String _pass){
		m_signinName	= _name;
		m_password		= _pass;
		
		m_createTime 	= (new Date()).getTime();
	}
	
	public void SetSigninName(final String _name){m_signinName = _name;}
	public String GetSigninName(){return m_signinName;}
	
	public void SetPassword(final String _pass){m_password = _pass;}
	public String GetPassword(){return m_password;}
	
	
	public String OuputXMLData(){
		StringBuffer t_output = new StringBuffer();
		t_output.append("<yuchbber ").append("name=\"").append(m_signinName).
									append("\" pass=\"").append(m_password).
									append("\" hour=\"").append(m_usingHours).
									append("\" time=\"").append(m_createTime).
									append("\" port=\"").append(m_serverPort).
									append("\" interval=\"").append(m_pushInterval).
									append("\" SSL=\"").append(m_usingSSL?1:0).
									append("\" T2S=\"").append(m_convertSimpleChar?1:0).
									append("\">\n");
		
		for(yuchEmail email : m_emailList){
			email.OuputXMLData(t_output);										
		}	
		
		t_output.append("</yuchbber>");
		
		return t_output.toString();
	}
	
	public void InputXMLData(final String _data)throws Exception{
		Document t_doc = XMLParser.parse(_data);
		com.google.gwt.xml.client.Element t_elem = t_doc.getDocumentElement();
		
		m_signinName	= ReadStringAttr(t_elem,"name");
		m_password		= ReadStringAttr(t_elem,"pass");
		m_usingHours	= ReadLongAttr(t_elem,"hour");
		m_createTime	= ReadLongAttr(t_elem, "time");
		m_serverPort	= ReadIntegerAttr(t_elem, "port");
		m_pushInterval	= ReadIntegerAttr(t_elem, "interval");
		m_usingSSL		= ReadBooleanAttr(t_elem, "SSL");
		m_convertSimpleChar = ReadBooleanAttr(t_elem, "T2S");
		
		
		m_emailList.removeAllElements();
		
		NodeList t_nodeElem = t_elem.getChildNodes();
		for(int i = 0;i < t_nodeElem.getLength();i++){
			Element t_element = (Element)t_nodeElem.item(i);
			if(t_element.getTagName().equals("email")){
				yuchEmail t_email = new yuchEmail();
				
				t_email.InputXMLData(t_element);
				
				m_emailList.add(t_email);
			}
		}		
	}
	
	/**
	 * read String attribute from xml
	 */
	static public String ReadStringAttr(Element _elem,String _attrName)throws Exception{
		String attr = _elem.getAttribute(_attrName);
		if(attr == null){
			throw new Exception("Element without attribute:" + _attrName);
		}
		
		return attr;
	}
	
	/**
	 * read boolean attribute from xml
	 */
	static public boolean ReadBooleanAttr(Element _elem,String _attrName)throws Exception{
		return Integer.valueOf(ReadStringAttr(_elem,_attrName)).intValue() == 1;
	}
	
	/**
	 * read the integer value from xml
	 */
	static public int ReadIntegerAttr(Element _elem,String _attrName)throws Exception{
		return Integer.valueOf(ReadStringAttr(_elem,_attrName)).intValue();
	}
	
	/**
	 * read the integer value from xml
	 */
	static public long ReadLongAttr(Element _elem,String _attrName)throws Exception{
		return Long.valueOf(ReadStringAttr(_elem,_attrName)).longValue();
	}
}
