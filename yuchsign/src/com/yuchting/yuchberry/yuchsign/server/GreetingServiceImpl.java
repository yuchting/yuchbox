package com.yuchting.yuchberry.yuchsign.server;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Vector;
import java.util.logging.Logger;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
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

	private final static Logger fsm_logger = Logger.getLogger("ServerLogger");
	
	yuchHost m_currSyncHost = null;
		
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

				if(!t_bber.GetPassword().equals(t_syncbber.GetPassword())){
					return "<Error>密码错误！</Error>";
				}				
								
				if(!t_syncbber.GetEmailList().isEmpty()){
					
					t_bber.InputXMLData(_xmlData);
					
					String query = "select from " + yuchHost.class.getName();
					List<yuchHost> t_hostList = (List<yuchHost>)t_pm.newQuery(query).execute();
					
					Vector<yuchHost> t_exceptList = new Vector<yuchHost>();
					
					if(t_bber.GetConnectHost().isEmpty()){		
						m_currSyncHost =  FindProperHost(t_hostList,t_syncbber.GetEmailList(),t_exceptList);	
					}else{
						for(yuchHost host : t_hostList){
							if(host.m_hostName.equalsIgnoreCase(t_bber.GetConnectHost())){
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
							
							t_bber.SetConnetHost(m_currSyncHost.m_hostName);
							
							// query the account
							//
							StringBuffer t_URL = new StringBuffer();
							t_URL.append("http://").append(m_currSyncHost.m_hostName).append(":")
								.append(m_currSyncHost.m_httpPort);
							
							Properties t_header = new Properties();
							t_header.put("pass",m_currSyncHost.m_httpPassword);
							
							Properties t_param = new Properties();
							t_param.put("bber",_xmlData);
							
							String t_result =  RequestURL(t_URL.toString(), t_header, t_param);
							if(t_result.equals("<Max />")){
								// find the other host if the host is full
								//
								m_currSyncHost =  FindProperHost(t_hostList,t_syncbber.GetEmailList(),t_exceptList);
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
				
				Properties t_param = new Properties();
				t_param.put("bber",_signinName);
				
				String t_result = RequestURL(t_URL.toString(), t_header, t_param);
				
				// read the information
				//
//				DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance(); 
//				DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder(); 
//				Document t_doc = docBuilder.parse(new InputSource(
//													new StringReader(t_result)));
//				
//				Element t_elem = t_doc.getDocumentElement();
				
				if(t_result.indexOf("<yuchberry") != -1){
					
					t_bber.InputXMLData(t_result);
					t_bber.SetConnetHost(m_currSyncHost.m_hostName);
					
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
	
	private String RequestURL(String _string,Properties header, Properties parms)throws Exception{
		
		StringBuffer t_final = new StringBuffer();
		t_final.append(_string);
		
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
				con.setRequestProperty(name,value);
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
		for(int i = 1;i < _hostList.size();i++){
			yuchHost host = _hostList.get(i);
			
			if(host.m_recommendHost.length() != 0){
				String[] t_string = host.m_recommendHost.split(" ");
				
				for(yuchEmail email:_emailList){
					
					for(String addr:t_string){
						if(email.m_emailAddr.indexOf(addr) !=-1){
							t_listHost.add(host);
						}
					}								
				}
			}
		}	
		
		if(t_listHost.isEmpty()){
			t_resultHost = _hostList.get(0);
		}
		
		t_resultHost = t_listHost.get((new Random()).nextInt(t_listHost.size()));
		
		// find if 
		//
		for(yuchHost host : _exceptList){
			if(host == t_resultHost){
				return null;
			}
		}
		
		return t_resultHost;
	}
	
	public String getHostList()throws Exception{
		
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
