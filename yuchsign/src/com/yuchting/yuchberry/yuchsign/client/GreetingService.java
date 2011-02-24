package com.yuchting.yuchberry.yuchsign.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("greet")
public interface GreetingService extends RemoteService {
	
	String logonServer(String name,String password) throws Exception;
	
	String signinAccount(String name,String password)throws Exception;
}
