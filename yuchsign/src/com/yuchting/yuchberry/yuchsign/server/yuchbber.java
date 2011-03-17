package com.yuchting.yuchberry.yuchsign.server;

import java.io.StringReader;
import java.util.Date;
import java.util.Vector;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public final class yuchbber {
	
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private String m_signinName = "";
	
	@Persistent
	private String m_connectHost = "";
	
	@Persistent
	private String m_password = "";
	
	@Persistent
	private long m_usingHours = 168;
	
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
	
	@Persistent
	private String m_signature = "--send from my yuchberry\nhttp://code.google.com/p/yuchberry";
	
	@Persistent
	private int m_bberLev = 0;
	
	@Persistent(mappedBy = "m_yuchbber")
	@javax.jdo.annotations.Element(dependent = "true")
	private Vector<yuchEmail>	m_emailList = new Vector<yuchEmail>();
	
	public yuchbber(final String _name,final String _pass){
		m_signinName	= _name;
		m_password		= _pass;
		
		m_createTime 	= (new Date()).getTime();
	}
	public yuchbber(){}
	
	public int GetLevel(){return m_bberLev;}
	public void SetLevel(int _level){ m_bberLev = _level;}
	
	public void SetSigninName(final String _name){m_signinName = _name;}
	public String GetSigninName(){return m_signinName;}
	
	public void SetConnetHost(final String _host){m_connectHost = _host;}
	public String GetConnectHost(){return m_connectHost;}
	
	public void SetPassword(final String _pass){m_password = _pass;}
	public String GetPassword(){return m_password;}
	
	public int GetServerPort(){return m_serverPort;}
	public void SetServerProt(int _port){m_serverPort = _port;}
	
	public int GetPushInterval(){return m_pushInterval;}
	public void SetPusnInterval(int _pushInterval){m_pushInterval = _pushInterval;}
	
	public boolean IsUsingSSL(){return m_usingSSL;}
	public void SetUsingSSL(boolean _ssl){m_usingSSL = _ssl;}
	
	public boolean IsConvertSimpleChar(){return m_convertSimpleChar;}
	public void SetConvertSimpleChar(boolean _convert){m_convertSimpleChar = _convert;}
	
	public long GetUsingHours(){return m_usingHours;}
	public void SetUsingHours(long _hours){m_usingHours = _hours;}
	
	public long GetCreateTime(){return m_createTime;}
	public void SetCreateTime(long _time){m_createTime = _time;}
	
	public String GetSignature(){return m_signature;}
	public void SetSignature(final String _signature){m_signature = _signature;}
	
	public Vector<yuchEmail> GetEmailList(){return m_emailList;}
	
	public String OuputXMLData(){
				
		String t_signature = m_signature.replaceAll("<","&lt;");
		t_signature = t_signature.replaceAll(">","&gt;");
		t_signature = t_signature.replaceAll("&","&amp;");
		t_signature = t_signature.replaceAll("'","&apos;");
		t_signature = t_signature.replaceAll("\"","&quot;");
		t_signature = t_signature.replaceAll("\n","#r");
		
		StringBuffer t_output = new StringBuffer();
		t_output.append("<yuchbber ").append("name=\"").append(m_signinName).
									append("\" connect=\"").append(m_connectHost).
									append("\" pass=\"").append(m_password).
									append("\" hour=\"").append(m_usingHours).
									append("\" time=\"").append(m_createTime).
									append("\" port=\"").append(m_serverPort).
									append("\" interval=\"").append(m_pushInterval).
									append("\" SSL=\"").append(m_usingSSL?1:0).
									append("\" T2S=\"").append(m_convertSimpleChar?1:0).
									append("\" signature=\"").append(t_signature).
									append("\" lev=\"").append(m_bberLev).
									append("\">\n");
				
		for(yuchEmail email : m_emailList){
			email.OuputXMLData(t_output);										
		}	
		
		t_output.append("</yuchbber>");
		
		return t_output.toString();
	}
	
	public void InputXMLData(final String _data)throws Exception{

		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance(); 
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder(); 
		Document t_doc = docBuilder.parse(new InputSource(new StringReader(_data))); 

		Element t_elem = t_doc.getDocumentElement();
		
		m_signinName	= ReadStringAttr(t_elem,"name");
		m_connectHost	= ReadStringAttr(t_elem,"connect");		
		m_password		= ReadStringAttr(t_elem,"pass");
		m_usingHours	= ReadLongAttr(t_elem,"hour");
		m_createTime	= ReadLongAttr(t_elem, "time");
		m_serverPort	= ReadIntegerAttr(t_elem, "port");
		m_pushInterval	= ReadIntegerAttr(t_elem, "interval");
		m_usingSSL		= ReadBooleanAttr(t_elem, "SSL");
		m_convertSimpleChar = ReadBooleanAttr(t_elem, "T2S");
		
		m_signature = ReadStringAttr(t_elem,"signature");
		m_bberLev	= ReadIntegerAttr(t_elem, "lev");
		
		m_signature = m_signature.replaceAll("&lt;", "<");
		m_signature = m_signature.replaceAll("&gt;", ">");
		m_signature = m_signature.replaceAll("&amp;", "&");
		m_signature = m_signature.replaceAll("&apos;", "'");
		m_signature = m_signature.replaceAll("&quot;", "\"");
		m_signature = m_signature.replaceAll("#r", "\n");
		
		m_emailList.removeAllElements();
		
		NodeList t_nodeElem = t_elem.getChildNodes();
		
		for(int i = 0;i < t_nodeElem.getLength();i++){
			Node t_node = t_nodeElem.item(i);
			
			if(t_node instanceof Element){

				Element t_element = (Element)t_node;
				if(t_element.getTagName().equals("email")){
					yuchEmail t_email = new yuchEmail();
					
					t_email.InputXMLData(t_element);
					
					m_emailList.add(t_email);
				}
			}
		}
	}
	
	public int GetMaxPushNum(){
		switch(m_bberLev){
			case 0: return 1;
			case 1: return 2;
			case 2: return 3;
			default:
				return 4;
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
