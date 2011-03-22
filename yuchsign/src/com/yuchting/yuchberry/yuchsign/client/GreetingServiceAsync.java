package com.yuchting.yuchberry.yuchsign.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>GreetingService</code>.
 */
public interface GreetingServiceAsync {
	
	void logonServer(String name,String password,AsyncCallback<String> callback)throws Exception;
	
	void signinAccount(String name,String password,AsyncCallback<String> callback)throws Exception;
	
	void findPassword(String _signinName,AsyncCallback<String> callback)throws Exception;
	
	void syncAccount(String _xmlData,AsyncCallback<String> callback)throws Exception;
	
	void syncAccount_check(String _signinName,String _pass,AsyncCallback<String> callback)throws Exception;
	
	void checkAccountLog(String _signinName,String _pass,AsyncCallback<String> callback)throws Exception;
	
	void getHostList(AsyncCallback<String> callback)throws Exception;
	
	void addHost(String _hostXMLData,AsyncCallback<String> callback)throws Exception;
	
	void delHost(String _hostName,AsyncCallback<String> callback)throws Exception;
	
	void modifyHost(String _hostName,String _hostXMLData,AsyncCallback<String> callback)throws Exception;
}
