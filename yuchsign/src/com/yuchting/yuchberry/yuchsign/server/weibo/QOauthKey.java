package com.yuchting.yuchberry.yuchsign.server.weibo;

public class QOauthKey {
	public String customKey;
	public String customSecrect;
	public String tokenKey;
	public String tokenSecrect;
	public String verify;
	public String callbackUrl;

	
	public void reset() {		
		tokenKey = null;
		tokenSecrect = null;
		verify = null;
		callbackUrl = null;
	}
	
	
}
