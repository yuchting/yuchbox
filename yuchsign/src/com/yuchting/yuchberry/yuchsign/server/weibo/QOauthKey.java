package com.yuchting.yuchberry.yuchsign.server.weibo;

import java.io.Serializable;

public class QOauthKey implements Serializable{
	private static final long serialVersionUID = 419062864823392L;
	
	public String customKey;
	public String customSecrect;
	public String tokenKey;
	public String tokenSecrect;
	public String verify;
	public String callbackUrl;
	
	public String accessToken;
	public String accessSecrect;

	
	public void reset() {		
		tokenKey = null;
		tokenSecrect = null;
		verify = null;
		callbackUrl = null;
		accessToken = null;
		accessSecrect = null;
	}
	
	
}
