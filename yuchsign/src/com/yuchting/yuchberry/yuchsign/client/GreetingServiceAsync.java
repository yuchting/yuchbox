package com.yuchting.yuchberry.yuchsign.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>GreetingService</code>.
 */
public interface GreetingServiceAsync {
	void logonServer(String name,String password,AsyncCallback<String> callback)
			throws Exception;
}
