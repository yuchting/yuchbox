package com.yuchting.yuchberry.server.frame;

import java.io.StringReader;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.InputSource;



public final class yuchbber {
	
	public static final int[] fsm_weekMoney = {2,3,4,5};
	
	private String m_signinName = "";
	private String m_connectHost = "";
	
	private String m_password = "";
	
	private long m_usingHours = 120;

	private long m_createTime = 0;
	
	private int m_serverPort = 0;
	
	private int m_pushInterval = 30;
	
	private boolean m_usingSSL = false;
	private boolean m_convertSimpleChar = false;
	
	private String m_signature = "";
	
	private int m_bberLev = 0;

	private long m_latestSyncTime = 0;
	
	private Vector<yuchEmail>	m_emailList = new Vector<yuchEmail>();
	
	private Vector<yuchWeibo>	m_weiboList = new Vector<yuchWeibo>();
	
	public yuchbber(final String _name,final String _pass){
		m_signinName	= _name;
		m_password		= _pass;
		
		m_createTime 	= System.currentTimeMillis();
	}
	public yuchbber(){}
	
	public int GetLevel(){return m_bberLev;}
	public void SetLevel(int _level){ m_bberLev = _level;}
	
	public long GetLatestSyncTime(){return m_latestSyncTime;}
	public void SetLatestSyncTime(long _time){m_latestSyncTime = _time;}
	
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
	
	public Vector<yuchEmail> GetEmailList(){return m_emailList;}
	public Vector<yuchWeibo> GetWeiboList(){return m_weiboList;}
	
	public String GetSignature(){return m_signature;}
	public void SetSignature(final String _signature){m_signature = _signature;}
	
	public String OuputXMLData(){
		
		String t_signature = m_signature.replace("<","&lt;");
		t_signature = t_signature.replace(">","&gt;");
		t_signature = t_signature.replace("&","&amp;");
		t_signature = t_signature.replace("\"","&quot;");
		t_signature = t_signature.replace("'","&apos;");
		t_signature = t_signature.replace("\n","#r");
		
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
									append("\" sync=\"").append(m_latestSyncTime).
									append("\">\n");
				
		for(yuchEmail email : m_emailList){
			email.OuputXMLData(t_output);										
		}
		
		for(yuchWeibo weibo : m_weiboList){
			weibo.OuputXMLData(t_output);
		}
		
		t_output.append("</yuchbber>");
		
		return t_output.toString();
	}
	
	public void InputXMLData(final String _data)throws Exception{
		SAXReader t_xmlReader = new SAXReader();
		Document t_doc = t_xmlReader.read(new InputSource(
											new StringReader(_data))); 
		
		Element t_elem = t_doc.getRootElement();
		
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
		
		// validate the bber level
		//
		if(m_bberLev < 0){
			m_bberLev = 0;
		}
		if(m_bberLev >= fsm_weekMoney.length){
			m_bberLev = fsm_weekMoney.length - 1;
		}
		m_latestSyncTime = ReadLongAttr(t_elem,"sync");
		
		m_signature = m_signature.replace("&lt;", "<");
		m_signature = m_signature.replace("&gt;", ">");
		m_signature = m_signature.replace("&amp;", "&");
		m_signature = m_signature.replace("&apos;", "'");
		m_signature = m_signature.replace("&quot;", "\"");
		m_signature = m_signature.replace("#r", "\n");
		
		m_emailList.removeAllElements();
		m_weiboList.removeAllElements();
		
		for( Iterator i = t_elem.elementIterator("email"); i.hasNext();){
            Element element = (Element) i.next();
            yuchEmail t_email = new yuchEmail();
			
			t_email.InputXMLData(element);
			
			m_emailList.add(t_email);
        }
		
		for( Iterator i = t_elem.elementIterator("WeiboAccount"); i.hasNext();){
            Element element = (Element) i.next();
            yuchWeibo t_weibo = new yuchWeibo();
			
            t_weibo.InputXMLData(element);
			
			m_weiboList.add(t_weibo);
        }
			
	}
		
	/**
	 * read String attribute from xml
	 */
	static public String ReadStringAttr(Element _elem,String _attrName)throws Exception{
		String attr = _elem.attributeValue(_attrName);
		if(attr == null){
			attr = "0";
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
