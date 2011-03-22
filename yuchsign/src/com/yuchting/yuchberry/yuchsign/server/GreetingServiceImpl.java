package com.yuchting.yuchberry.yuchsign.server;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Logger;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.yuchting.yuchberry.yuchsign.client.GreetingService;



final class PMF {
	private static final PersistenceManagerFactory pmfInstance =
	        JDOHelper.getPersistenceManagerFactory("transactions-optional");

	private PMF() {}

    public static PersistenceManagerFactory get() {
        return pmfInstance;
    } 
}
/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class GreetingServiceImpl extends RemoteServiceServlet implements GreetingService {

	public final static Logger fsm_logger = Logger.getLogger("ServerLogger");
	
	yuchHost	m_currSyncHost = null;
	
	boolean 	m_isAdministrator = false;
	
	long		m_createTime = 0;
	
	long		m_checkLogTime = 0;
	
	boolean	m_foundPassword = false;
		
	public String logonServer(String name,String password) throws Exception {
		
		PersistenceManager t_pm = PMF.get().getPersistenceManager();
			
		try{
			yuchbber t_bber = null;
			
			Key k = KeyFactory.createKey(yuchbber.class.getSimpleName(), name);
			try{
				t_bber = t_pm.getObjectById(yuchbber.class, k);	
				
				if(!t_bber.GetPassword().equals(password)){
					return "<Error>密码错误！</Error>";
				}
				
				m_isAdministrator = t_bber.GetSigninName().equalsIgnoreCase("yuchting@gmail.com");
				
			}catch(javax.jdo.JDOObjectNotFoundException e){
				return "<Error>找不到用户!</Error>";
			}		
			
			return t_bber.OuputXMLData();
			
		}finally{
			t_pm.close();
		}		
	}
	
	public String signinAccount(String _name,String _password)throws Exception{
		
		PersistenceManager t_pm = PMF.get().getPersistenceManager();
		
		try{
			yuchbber t_newbber = null;
			
			Key k = KeyFactory.createKey(yuchbber.class.getSimpleName(), _name);
			try{
				yuchbber t_bber = t_pm.getObjectById(yuchbber.class, k);				

				if(t_bber != null){
					return "<Error>用户名已经存在!</Error>";
				}
				
				m_isAdministrator = t_bber.GetSigninName().equalsIgnoreCase("yuchting@gmail.com");
				
			}catch(javax.jdo.JDOObjectNotFoundException e){
								
				// create account
				//
				t_newbber = new yuchbber(_name,_password);
				
				t_pm.makePersistent(t_newbber);
			}
			
			return t_newbber.OuputXMLData();
			
		}finally{
			t_pm.close();
		}				
	}
		
	public String syncAccount(String _xmlData)throws Exception{
		
		PersistenceManager t_pm = PMF.get().getPersistenceManager();
				
		try{
			
			yuchbber t_syncbber = new yuchbber();
			t_syncbber.InputXMLData(_xmlData);
			
			Key k = KeyFactory.createKey(yuchbber.class.getSimpleName(), t_syncbber.GetSigninName());
			try{
				yuchbber t_bber = t_pm.getObjectById(yuchbber.class, k);		

				m_createTime			= t_bber.GetCreateTime();
				long t_hours			= t_bber.GetUsingHours();
				int t_lev				= t_bber.GetLevel();
				long t_latesSyncTime	= t_bber.GetLatestSyncTime();
				int t_interval			= t_bber.GetPushInterval();
				
				if(!t_bber.GetPassword().equals(t_syncbber.GetPassword())){
					return "<Error>密码错误！</Error>";
				}
				
				if(t_latesSyncTime != 0){
					// judge the sync time
					//
					if(!m_isAdministrator && Math.abs((new Date()).getTime() - t_latesSyncTime) < 10 * 60 * 1000){
						return "<Error>一个账户同步时间的最小间隔是10分钟，请不要过于频繁。</Error>";
					}
				}
								
				
				if(!t_syncbber.GetEmailList().isEmpty()){
					
					t_bber.InputXMLData(_xmlData);					

					// restore backup the time and using hours
					//
					t_bber.SetCreateTime(m_createTime);
					t_bber.SetUsingHours(t_hours);
					t_bber.SetLevel(t_lev);
					t_bber.SetLatestSyncTime(t_latesSyncTime);
					t_bber.SetPusnInterval(t_interval);
					
					//set the sync bber
					//
					if(m_createTime == 0){
						// first sync
						//
						m_createTime = (new Date()).getTime();
						t_syncbber.SetCreateTime(m_createTime);
					}else{
						t_syncbber.SetCreateTime(m_createTime);
					}
					
					t_syncbber.SetUsingHours(t_hours);
					
					
					// search the proper host to synchronize
					//
					String query = "select from " + yuchHost.class.getName();
					List<yuchHost> t_hostList = (List<yuchHost>)t_pm.newQuery(query).execute();
					
					Vector<yuchHost> t_exceptList = new Vector<yuchHost>();
					
					if(t_syncbber.GetConnectHost().isEmpty()){		
						m_currSyncHost =  FindProperHost(t_hostList,t_syncbber.GetEmailList(),t_exceptList);	
					}else{
						for(yuchHost host : t_hostList){
							if(host.m_hostName.equalsIgnoreCase(t_syncbber.GetConnectHost())){
								m_currSyncHost = host;
								break;
							}
						}
						
						if(m_currSyncHost == null){
							m_currSyncHost =  FindProperHost(t_hostList,t_syncbber.GetEmailList(),t_exceptList);	
						}
					}
					
					if(m_currSyncHost == null){
						return "<Error>没有可用的服务器主机！</Error>";
					}
					
					t_exceptList.add(m_currSyncHost);
					
					try{
						
						while(m_currSyncHost != null){
							
							t_syncbber.SetConnetHost(m_currSyncHost.m_hostName);
							
							// query the account
							//
							StringBuffer t_URL = new StringBuffer();
							t_URL.append("http://").append(m_currSyncHost.m_hostName).append(":")
								.append(m_currSyncHost.m_httpPort);
							
							Properties t_header = new Properties();
							t_header.put("pass",m_currSyncHost.m_httpPassword);
							t_header.put("bber",t_syncbber.OuputXMLData());
							
							String t_result =  RequestURL(t_URL.toString(), t_header, null);
							if(t_result.indexOf("<Max />") != -1){
								// find the other host if the host is full
								//
								m_currSyncHost =  FindProperHost(t_hostList,t_syncbber.GetEmailList(),t_exceptList);
								
								t_exceptList.add(m_currSyncHost);
								
							}else{
								return t_result;
							}
						}
						
						return "<Error>所有的主机用户已经满员！</Error>";
						
					}catch(Exception e){
						return "<Error>请求主机URL时出错:" + e.getMessage() + "</Error>";
					}
			        
										
					
				}else{
					return "<Error>没有添加推送账户，无法同步！</Error>";
				}
				
			}catch(javax.jdo.JDOObjectNotFoundException e){
				return "<Error>找不到用户!</Error>";
			}
			
		}finally{
			t_pm.close();
		}	
	}
	
	public String syncAccount_check(String _signinName,String _pass)throws Exception{
		
		if(m_currSyncHost == null){
			return "<Error>请先同步！</Error>";
		}
		
		PersistenceManager t_pm = PMF.get().getPersistenceManager();
		try{
			Key k = KeyFactory.createKey(yuchbber.class.getSimpleName(),_signinName);
			try{
				yuchbber t_bber = t_pm.getObjectById(yuchbber.class, k);				

				if(!t_bber.GetPassword().equals(_pass)){
					return "<Error>密码错误！</Error>";
				}
								
				// query the account
				//
				StringBuffer t_URL = new StringBuffer();
				t_URL.append("http://").append(m_currSyncHost.m_hostName).append(":")
					.append(m_currSyncHost.m_httpPort);
				
				Properties t_header = new Properties();
				t_header.put("pass",m_currSyncHost.m_httpPassword);
				t_header.put("check",_signinName);
				t_header.put("bber",_signinName);
				
				String t_result = RequestURL(t_URL.toString(), t_header, null);
				
				// read the information
				//
//				DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance(); 
//				DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder(); 
//				Document t_doc = docBuilder.parse(new InputSource(
//													new StringReader(t_result)));
//				
//				Element t_elem = t_doc.getDocumentElement();
				
				int t_portTag_start		= t_result.indexOf("<Port>");
				int t_portTag_end		= t_result.indexOf("</Port>");
				if(t_portTag_start != -1){
					
					t_result = t_result.substring(t_portTag_start + 6,t_portTag_end);
					int t_port = Integer.valueOf(t_result).intValue();
					
					t_bber.SetServerProt(t_port);
					t_bber.SetConnetHost(m_currSyncHost.m_hostName);
					
					t_bber.SetCreateTime(m_createTime);
					t_bber.SetLatestSyncTime((new Date()).getTime());
					
					t_result = t_bber.OuputXMLData();
					
					m_currSyncHost = null;
				}				
				
				return t_result;
				
			}catch(javax.jdo.JDOObjectNotFoundException e){
				return "<Error>找不到用户!</Error>";
			}
			
			
		}finally{
			
			t_pm.close();
		}		
	}
	public String checkAccountLog(String _signinName,String _pass)throws Exception{
		
		if(m_checkLogTime != 0){

			long t_currentTime = (new Date()).getTime();
			if(!m_isAdministrator && Math.abs(t_currentTime - m_checkLogTime) < 2 * 60 * 1000){
				return "<Error>在2分钟内不要重复提交查询</Error>";
			}	
		}
		
		PersistenceManager t_pm = PMF.get().getPersistenceManager();
		
		try{
			Key k = KeyFactory.createKey(yuchbber.class.getSimpleName(),_signinName);
			try{
				yuchbber t_bber = t_pm.getObjectById(yuchbber.class, k);				

				if(!t_bber.GetPassword().equals(_pass)){
					return "<Error>密码错误！</Error>";
				}
				
				if(t_bber.GetConnectHost().isEmpty()){
					return "<Error>没有获得主机，无法查询日志</Error>";
				}
				
				// search the proper host to synchronize
				//
				String query = "select from " + yuchHost.class.getName();
				List<yuchHost> t_hostList = (List<yuchHost>)t_pm.newQuery(query).execute();
				
				for(yuchHost host : t_hostList){
					if(host.m_hostName.equalsIgnoreCase(t_bber.GetConnectHost())){
						StringBuffer t_URL = new StringBuffer();
						t_URL.append("http://").append(host.m_hostName).append(":").append(host.m_httpPort);
						
						Properties t_header = new Properties();
						t_header.put("pass",host.m_httpPassword);
						t_header.put("bber",_signinName);
						t_header.put("log",_signinName);
						
						String t_result = RequestURL(t_URL.toString(), t_header, null);
						if(!t_result.startsWith("<Error>")){
							m_checkLogTime = (new Date()).getTime();
						}
						return t_result; 
					}
				}
				
				return "<Error>之前同步过的主机已经被删除，请先同步</Error>";				
				
			}catch(javax.jdo.JDOObjectNotFoundException e){
				return "<Error>找不到用户!</Error>";
			}
			
			
		}finally{
			
			t_pm.close();
		}	
	}
	
	public String findPassword(String _signinName)throws Exception{
		
		if(m_foundPassword){
			return "你已经提交过找回密码的信息";
		}
		
		PersistenceManager t_pm = PMF.get().getPersistenceManager();
		
		try{
			Key k = KeyFactory.createKey(yuchbber.class.getSimpleName(),_signinName);
			try{
				yuchbber t_bber = t_pm.getObjectById(yuchbber.class, k);				
				
				Properties props = new Properties();
		        Session session = Session.getDefaultInstance(props, null);

		        StringBuffer t_body = new StringBuffer();
		        t_body.append("您好！\n\n您收到这封邮件，是因为您在登录 Yuchberry 账户时忘记了密码，您使用这个邮箱地址注册时的密码为：\n    ")
		        	.append(t_bber.GetPassword()).append("\n\n   请您务必保管好，以防下次遗失。\n\n致\n  敬！\nhttp://code.google.com/p/yuchberry/");
		        		        	
	            Message msg = new MimeMessage(session);
	            
	            msg.setFrom(new InternetAddress("yuchting@yuchberry.info"));
	            msg.addRecipient(Message.RecipientType.TO,new InternetAddress(_signinName,""));
	            msg.setSubject("YuchBerry 找回密码");
	            msg.setText(t_body.toString());
	            
	            Transport.send(msg);
	            
	            m_foundPassword = true;		    
				
		        return "已经将邮件发送到<" + t_bber.GetSigninName() +"> 请及时查收\n如果没有请在垃圾邮件箱内查找一下。";
		        
			}catch(javax.jdo.JDOObjectNotFoundException e){
				return "找不到用户!";
			}
			
			
		}finally{
			
			t_pm.close();
		}	
	}
	
	private String RequestURL(String _url,Properties header, Properties parms)throws Exception{
		
		StringBuffer t_final = new StringBuffer();
		t_final.append(_url);
		
		if(parms != null){
			t_final.append("?");
			Enumeration e = parms.propertyNames();
			while(true){
				String value = (String)e.nextElement();
				t_final.append(value).append("=").append(URLEncoder.encode(parms.getProperty(value),"UTF-8"));
				if(e.hasMoreElements()){
					t_final.append("&");
				}else{
					break;
				}
			}
		}
		
		URL url = new URL(t_final.toString());
		URLConnection con = url.openConnection();
		con.setAllowUserInteraction(false);
		
		if(header != null){
			Enumeration e = header.propertyNames();
			while ( e.hasMoreElements()){
				String name = (String)e.nextElement();
				String value = header.getProperty(name);
				con.setRequestProperty(name,URLEncoder.encode(value,"UTF-8"));
			}
		}		
		con.connect();
		
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
		try{
			StringBuffer t_stringBuffer = new StringBuffer();
			
			String temp;
			while ((temp = in.readLine()) != null) {
				t_stringBuffer.append(temp+"\n");
			}
			
			return t_stringBuffer.toString();
		}finally{
			in.close();	
		}
	}

	private yuchHost FindProperHost(List<yuchHost> _hostList,
									Vector<yuchEmail> _emailList,
										Vector<yuchHost> _exceptList){
		
		
		if(_hostList == null || _hostList.size() == 0){
			return null;
		}
		
		Vector<yuchHost> t_listHost = new Vector<yuchHost>();
		
		yuchHost t_resultHost = null;
		
		for(yuchHost host : _hostList){
			
			if(host.m_recommendHost.length() != 0){
				String[] t_string = host.m_recommendHost.split(" ");
				
				boolean t_add = false;
				
				for(yuchEmail email:_emailList){
					
					for(String addr:t_string){
						if(email.m_emailAddr.indexOf(addr) !=-1){
							t_add = true;
							t_listHost.add(0,host);
						}
					}								
				}
				
				if(!t_add){
					t_listHost.add(host);
				}
			}else{
				t_listHost.add(host);
			}
		}	
		
		if(!t_listHost.isEmpty()){
			
			search_tag:
			for(yuchHost host : t_listHost){
				for(yuchHost except : _exceptList){
					if(host == except){
						continue search_tag;
					}
				}
				
				t_resultHost = host;
				break;
			}
		}

		return t_resultHost;
	}
	
	public String getHostList()throws Exception{
		
		if(!m_isAdministrator){
			return "<Error>you're not administrator</Error>";
		}
		
		PersistenceManager t_pm = PMF.get().getPersistenceManager();
				
		try{
			
			String query = "select from " + yuchHost.class.getName();
			List<yuchHost> t_hostList = (List<yuchHost>)t_pm.newQuery(query).execute();
			
			StringBuffer t_xmlData = new StringBuffer();
			t_xmlData.append("<HostList>");
			
			if(t_hostList != null){
				for(yuchHost host :t_hostList){
					host.OutputXMLData(t_xmlData);
				}	
			}
			
			t_xmlData.append("</HostList>");
			
			return t_xmlData.toString();
			
		}finally{
			
			t_pm.close();
		}	
	}
	
	public String addHost(String _hostXMLData)throws Exception{
		
		if(!m_isAdministrator){
			return "<Error>you're not administrator</Error>";
		}
		
		PersistenceManager t_pm = PMF.get().getPersistenceManager();
		
		try{
			yuchHost t_newHost = new yuchHost();
			
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance(); 
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder(); 
			Document t_doc = docBuilder.parse(new InputSource(
											new StringReader(_hostXMLData))); 
			
			t_newHost.InputXMLData(t_doc.getDocumentElement());
			
			Key k = KeyFactory.createKey(yuchHost.class.getSimpleName(), t_newHost.m_hostName);
			try{
				yuchHost t_host = t_pm.getObjectById(yuchHost.class, k);				

				if(t_host != null){
					return "<Error>主机 "+t_newHost.m_hostName+" 已经存在!</Error>";
				}				
				
			}catch(javax.jdo.JDOObjectNotFoundException e){
									
				try{
					t_pm.makePersistent(t_newHost);
				}catch(Exception ex){
					return "<Error>"+ex.getMessage()+"</Error>";
				}				
			}
			
			StringBuffer t_output = new StringBuffer();
			t_newHost.OutputXMLData(t_output);
			return t_output.toString();
			
		}finally{
			t_pm.close();
		}	
	}
	
	public String delHost(String _hostName)throws Exception{
		
		if(!m_isAdministrator){
			return "<Error>you're not administrator</Error>";
		}		
		
		PersistenceManager t_pm = PMF.get().getPersistenceManager();
		
		try{
			Key k = KeyFactory.createKey(yuchHost.class.getSimpleName(),_hostName);
			try{
				yuchHost t_host = t_pm.getObjectById(yuchHost.class, k);
				t_pm.deletePersistent(t_host);
				
			}catch(javax.jdo.JDOObjectNotFoundException e){					
				return "<Error>主机 "+_hostName+" 不存在!</Error>";
			}
			
			return "<OK />";
			
		}finally{
			t_pm.close();
		}
	}
	
	public String modifyHost(String _hostName,String _hostXMLData)throws Exception{
		
		if(!m_isAdministrator){
			return "<Error>you're not administrator</Error>";
		}
		
		PersistenceManager t_pm = PMF.get().getPersistenceManager();
		
		try{
			Key k = KeyFactory.createKey(yuchHost.class.getSimpleName(),_hostName);
			try{
				
				yuchHost t_host = t_pm.getObjectById(yuchHost.class, k);
				
				DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance(); 
				DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder(); 
				Document t_doc = docBuilder.parse(new InputSource(
													new StringReader(_hostXMLData))); 
				
				
				t_host.InputXMLData(t_doc.getDocumentElement());
				
			}catch(javax.jdo.JDOObjectNotFoundException e){					
				return "<Error>主机 "+_hostName+" 不存在!</Error>";
			}
			
			return "<OK />";
			
		}finally{
			t_pm.close();
		}
	}	
	
	
	

}
