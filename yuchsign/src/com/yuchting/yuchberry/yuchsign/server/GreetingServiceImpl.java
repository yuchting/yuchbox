package com.yuchting.yuchberry.yuchsign.server;


import java.util.logging.Logger;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

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
public class GreetingServiceImpl extends RemoteServiceServlet implements
		GreetingService {

	private final static Logger fsm_logger = Logger.getLogger("ServerLogger");
	
	public String logonServer(String name,String password) throws Exception {
		
		PersistenceManager t_pm = PMF.get().getPersistenceManager();
		yuchbber t_bber = null;
		
		try{			
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
		yuchbber t_newbber = null;
		
		try{
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
	

}
