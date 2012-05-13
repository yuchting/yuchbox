/**
 *  Dear developer:
 *  
 *   If you want to modify this file of project and re-publish this please visit:
 *  
 *     http://code.google.com/p/yuchberry/wiki/Project_files_header
 *     
 *   to check your responsibility and my humble proposal. Thanks!
 *   
 *  -- 
 *  Yuchs' Developer    
 *  
 *  
 *  
 *  
 *  尊敬的开发者：
 *   
 *    如果你想要修改这个项目中的文件，同时重新发布项目程序，请访问一下：
 *    
 *      http://code.google.com/p/yuchberry/wiki/Project_files_header
 *      
 *    了解你的责任，还有我卑微的建议。 谢谢！
 *   
 *  -- 
 *  语盒开发者
 *  
 */
package com.yuchting.yuchberry.server.frame;

import java.io.StringReader;
import java.util.Iterator;
import java.util.Vector;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.InputSource;

import com.yuchting.yuchberry.server.fetchAccount;

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
	
	private Vector<yuchIM>		m_imList	= new Vector<yuchIM>();
	
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
	public Vector<yuchIM> GetIMList(){return m_imList;}
	
	public String GetSignature(){return m_signature;}
	public void SetSignature(final String _signature){m_signature = _signature;}
	
	public String OuputXMLData(){
				
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
									append("\" signature=\"").append(mainFrame.prepareXmlAttr(m_signature).replace("\n","#r")).
									append("\" lev=\"").append(m_bberLev).
									append("\" sync=\"").append(m_latestSyncTime).
									append("\">\n");
				
		for(yuchEmail email : m_emailList){
			email.OuputXMLData(t_output);										
		}
		
		for(yuchWeibo weibo : m_weiboList){
			weibo.OuputXMLData(t_output);
		}
		
		for(yuchIM im:m_imList){
			im.OuputXMLData(t_output);
		}
		
		t_output.append("</yuchbber>");
		
		return t_output.toString();
	}
	
	public void InputXMLData(final String _data)throws Exception{
		SAXReader t_xmlReader = new SAXReader();
		Document t_doc = t_xmlReader.read(new InputSource(
											new StringReader(_data))); 
		
		Element t_elem = t_doc.getRootElement();
		
		m_signinName	= fetchAccount.ReadStringAttr(t_elem,"name");
		m_connectHost	= fetchAccount.ReadStringAttr(t_elem,"connect");		
		m_password		= fetchAccount.ReadStringAttr(t_elem,"pass");
		m_usingHours	= fetchAccount.ReadLongAttr(t_elem,"hour");
		m_createTime	= fetchAccount.ReadLongAttr(t_elem, "time");
		m_serverPort	= fetchAccount.ReadIntegerAttr(t_elem, "port");
		m_pushInterval	= fetchAccount.ReadIntegerAttr(t_elem, "interval");
		m_usingSSL		= fetchAccount.ReadBooleanAttr(t_elem, "SSL");
		m_convertSimpleChar = fetchAccount.ReadBooleanAttr(t_elem, "T2S");
		
		m_signature = fetchAccount.ReadStringAttr(t_elem,"signature");
		m_bberLev	= fetchAccount.ReadIntegerAttr(t_elem, "lev");
		
		// validate the bber level
		//
		if(m_bberLev < 0){
			m_bberLev = 0;
		}
		if(m_bberLev >= fsm_weekMoney.length){
			m_bberLev = fsm_weekMoney.length - 1;
		}
		m_latestSyncTime = fetchAccount.ReadLongAttr(t_elem,"sync");
		m_signature = m_signature.replace("#r", "\n");
		
		m_emailList.removeAllElements();
		m_weiboList.removeAllElements();
		m_imList.removeAllElements();
		
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
		
		for( Iterator i = t_elem.elementIterator("IMAccount"); i.hasNext();){
            Element element = (Element) i.next();
            yuchIM t_im = new yuchIM();
			
            t_im.InputXMLData(element);
			
			m_imList.add(t_im);
        }
			
	}
}
