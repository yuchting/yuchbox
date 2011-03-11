package com.yuchting.yuchberry.yuchsign.server;


import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.XMLParser;
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
					String query = "select from " + yuchHost.class.getName() + " where *";
					
					yuchHost t_recommendhost =  FindProperHost((List<yuchHost>)t_pm.newQuery(query).execute(),
																t_syncbber.GetEmailList());
					
					if(t_recommendhost == null){
						return "<Error>没有可用的服务器主机！</Error>";
					}
					
					
				}else{
					
					return "<Error>没有添加推送账户，无法同步！</Error>";
				}
				
				
			}catch(javax.jdo.JDOObjectNotFoundException e){
				return "<Error>找不到用户!</Error>";
			}
			
			return t_syncbber.OuputXMLData();
			
		}finally{
			t_pm.close();
		}	
	}
	
	private yuchHost FindProperHost(List<yuchHost> _hostList, Vector<yuchEmail> _emailList){
		
		if(_hostList == null || _hostList.size() == 0){
			return null;
		}
		
		for(int i = 1;i < _hostList.size();i++){
			yuchHost host = _hostList.get(i);
			
			if(host.m_recommendHost.length() != 0){
				String[] t_string = host.m_recommendHost.split(" ");
				
				for(yuchEmail email:_emailList){
					
					for(String addr:t_string){
						if(email.m_emailAddr.indexOf(addr) !=-1){
							return host;
						}
					}								
				}
			}
		}
		
		return _hostList.get(0);
	}
	
	public String getHostList()throws Exception{
		
		PersistenceManager t_pm = PMF.get().getPersistenceManager();
				
		try{
			
			String query = "select from " + yuchHost.class.getName() + " where *";
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
			t_newHost.InputXMLData(XMLParser.parse(_hostXMLData).getDocumentElement());
			
			Key k = KeyFactory.createKey(yuchHost.class.getSimpleName(), t_newHost.m_hostName);
			try{
				yuchHost t_host = t_pm.getObjectById(yuchHost.class, k);				

				if(t_host != null){
					return "<Error>主机 "+t_newHost.m_hostName+" 已经存在!</Error>";
				}				
				
			}catch(javax.jdo.JDOObjectNotFoundException e){
												
				t_pm.makePersistent(t_newHost);
			}
			
			return "<OK />";
			
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
				t_host.InputXMLData(XMLParser.parse(_hostXMLData).getDocumentElement());
				
			}catch(javax.jdo.JDOObjectNotFoundException e){					
				return "<Error>主机 "+_hostName+" 不存在!</Error>";
			}
			
			return "<OK />";
			
		}finally{
			t_pm.close();
		}
	}	
	
	
	

}
