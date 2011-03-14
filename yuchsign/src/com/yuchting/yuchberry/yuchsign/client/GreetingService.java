package com.yuchting.yuchberry.yuchsign.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("greet")
public interface GreetingService extends RemoteService {
	
	String logonServer(String name,String password) throws Exception;
	
	String signinAccount(String name,String password)throws Exception;
	
	String syncAccount(String _xmlData)throws Exception;
	
	String syncAccount_check(String _signinName,String _pass)throws Exception;
	
	String getHostList()throws Exception;
	
	String addHost(String _hostXMLData)throws Exception;
	
	String delHost(String _hostName)throws Exception;
	
	String modifyHost(String _hostName,String _hostXMLData)throws Exception;
	
	
}
