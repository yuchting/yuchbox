package com.yuchting.yuchberry.yuchsign.server;

import java.io.StringReader;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import javax.jdo.PersistenceManager;
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

import com.yuchting.yuchberry.yuchsign.shared.FieldVerifier;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public final class yuchbber {
	
	public static final int[] fsm_weekMoney = {2,2,3,3};
	public static final int[] fsm_levelMoney = {2,3,4,5};
	public static final int[] fsm_intervalMoney = {5,5,5,5};
	
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private String m_signinName = "";
	
	@Persistent
	private long m_signinTime = 0;
	
	@Persistent
	private String m_connectHost = "";
		
	@Persistent
	private String m_password = "";
	
	@Persistent
	private long m_usingHours = FieldVerifier.fsm_freeDays * 24;
	
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
	private String m_signature = "--\nsent from my yuchberry\n" + FieldVerifier.fsm_mainURL;
	
	@Persistent
	private int m_bberLev = 0;
	
	@Persistent
	private long m_latestSyncTime = 0;	
	
	@Persistent
	private int m_total_pay		= 0;
	
	@Persistent(mappedBy = "m_yuchbber")
	@javax.jdo.annotations.Element(dependent = "true")
	private Vector<yuchEmail>	m_emailList = new Vector<yuchEmail>();
	
	@Persistent(mappedBy = "m_yuchbber")
	@javax.jdo.annotations.Element(dependent = "true")
	private Vector<yuchWeibo>	m_weiboList = new Vector<yuchWeibo>();
	
	@Persistent
	private String m_inviteCode = null;
	
	@Persistent
	private Integer m_inviteNum = new Integer(0);
	
	@Persistent
	private String m_inviter =  "";
		
	public yuchbber(final String _name,final String _pass,final String _inviter){
		m_signinName	= _name;
		m_password		= _pass;
		m_inviter		= _inviter;
	}
	
	public yuchbber(){}
	
	public long GetSigninTime(){return m_signinTime;}
	public void SetSigninTime(long _time){m_signinTime = _time;}
	
	public int GetLevel(){return m_bberLev;}
	public void SetLevel(int _level){ m_bberLev = _level;}
	
	public long GetLatestSyncTime(){return m_latestSyncTime;}
	public void SetLatestSyncTime(long _time){m_latestSyncTime = _time;}
	
	public int GetTotalPayFee(){return m_total_pay;}
	public void SetTotalPayFee(int _fee){m_total_pay = _fee;}
	
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
	
	public String getInviteCode(){return m_inviteCode;}
	public void setInviteCode(String _code){m_inviteCode = _code;}
	
	public String getInviter(){return m_inviter;}
	public void setInviter(String _inviter){m_inviter = _inviter;}
	
	public Vector<yuchEmail> GetEmailList(){return m_emailList;}
	
	public Vector<yuchWeibo> GetWeiboList(){return m_weiboList;}
	
	public void setInviteNum(int _num){
		m_inviteNum = new Integer(_num);
	}
	public int getInviteNum(){
		if(m_inviteNum == null){
			m_inviteNum = new Integer(0);
			return 0;
		}else{
			return m_inviteNum.intValue();
		}
	}
	
	public void NewWeiboList(){
		if(m_weiboList == null){
			m_weiboList = new Vector<yuchWeibo>();
		}
		
		if(m_inviteCode == null){
			m_inviteCode = genInviteCode();
		}
		
		if(m_inviteNum == null){
			m_inviteNum = new Integer(0);
		}
		
		if(m_inviter == null){
			m_inviter = "";
		}
	}
	
	public String OuputXMLData(){
		
		NewWeiboList();
		
		String t_signature = m_signature.replace("<","&lt;");
		t_signature = t_signature.replace(">","&gt;");
		t_signature = t_signature.replace("&","&amp;");
		t_signature = t_signature.replace("'","&apos;");
		t_signature = t_signature.replace("\"","&quot;");
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
									append("\" invite=\"").append(m_inviteCode).
									append("\" inviteNum=\"").append(m_inviteNum.intValue()).
									append("\">\n");
				
		for(yuchEmail email : m_emailList){
			email.OuputXMLData(t_output);										
		}		
		
		for(yuchWeibo weibo:m_weiboList){
			weibo.OuputXMLData(t_output);
		}
		
		t_output.append("</yuchbber>");
		
		return t_output.toString();
	}
	
	public void InputXMLData(final String _data)throws Exception{
		
		NewWeiboList();

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
		m_inviteCode = ReadStringAttr(t_elem, "invite");
		m_inviteNum =  new Integer(ReadIntegerAttr(t_elem, "inviteNum"));
		
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
		
		NodeList t_nodeElem = t_elem.getChildNodes();
		
		int t_pushNum = 0;
		
		for(int i = 0;i < t_nodeElem.getLength();i++){
			Node t_node = t_nodeElem.item(i);
			
			if(t_node instanceof Element){

				Element t_element = (Element)t_node;
				if(t_element.getTagName().equals("email")){
					yuchEmail t_email = new yuchEmail();
					t_email.InputXMLData(t_element);
					
					m_emailList.add(t_email);
				}else if(t_element.getTagName().equalsIgnoreCase("WeiboAccount")){
					
					yuchWeibo t_weibo = new yuchWeibo();
					t_weibo.InputXMLData(t_element);
					
					m_weiboList.add(t_weibo);
				}
				
				t_pushNum++;
				
				if(t_pushNum >= GetMaxPushNum()){
					break;
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
	
	/**
	 * generate the invite code
	 */
	static public String genInviteCode(){
		PersistenceManager t_pm = PMF.get().getPersistenceManager();
		
		int t_genTime = 0;
		String t_inviteCode = getRandomString();
		try{
			while(t_genTime++ < 3){
				
				List<yuchbber> t_bberList = 
					(List<yuchbber>)t_pm.newQuery("select from " + yuchbber.class.getName() + 
													" where m_inviteCode == " + "\"" + t_inviteCode + "\"").execute();
				if(t_bberList == null || t_bberList.isEmpty()){
					break;
				}
				
				t_inviteCode = getRandomString();
			}
			
		}finally{
			t_pm.close();
		}
		
		return t_inviteCode;
	}
	
	static public String getRandomString(){
		StringBuffer t_str = new StringBuffer();
		
		Random t_rand = new Random();
		for(int i = 0; i < FieldVerifier.fsm_inviteCodeBits;i++){
			if(t_rand.nextBoolean()){
				char a = 'a';
				if(t_rand.nextBoolean()){
					a = 'A';
				}
				
				a = (char) (a + Math.abs(t_rand.nextInt() % 26));

				t_str.append(a);

			}else{
				t_str.append(Math.abs(t_rand.nextInt() % 10));
			}
		}
		
		return t_str.toString();
	}
}
