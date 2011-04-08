package com.yuchting.yuchberry.yuchsign.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>GreetingService</code>.
 */
public interface GreetingServiceAsync {
	
	void logonServer(String name,String password,AsyncCallback<String> callback)throws Exception;
	
	void signinAccount(String name,String password,String verifyCode,AsyncCallback<String> callback)throws Exception;
	
	void findPassword(String _signinName,String _verifyCode,AsyncCallback<String> callback)throws Exception;
	
	void changePassword(String _signinName,String _verifyCode,String _origPass,String _pass,AsyncCallback<String> callback)throws Exception;
	
	void syncAccount(String _xmlData,String verifyCode,AsyncCallback<String> callback)throws Exception;
	
	void syncAccount_check(String _signinName,String _pass,AsyncCallback<String> callback)throws Exception;
	
	void checkAccountLog(String _signinName,String _pass,AsyncCallback<String> callback)throws Exception;
	
	void payTime(String _signinName,int _payType,int _fee,AsyncCallback<String> callback)throws Exception;
	
	void getdownLev(String _signinName,AsyncCallback<String> callback)throws Exception;
	
	
	// administrator function
	//
	void queryAlipay(AsyncCallback<String> callback)throws Exception;
	
	void modifyAlipay(String _partnerID,String _key,AsyncCallback<String> callback)throws Exception;
	
	void getHostList(AsyncCallback<String> callback)throws Exception;
	
	void addHost(String _hostXMLData,AsyncCallback<String> callback)throws Exception;
	
	void delHost(String _hostName,AsyncCallback<String> callback)throws Exception;
	
	void modifyHost(String _hostName,String _hostXMLData,AsyncCallback<String> callback)throws Exception;

}
