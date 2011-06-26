package com.yuchting.yuchberry.yuchsign.server.weibo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class WeiboAuth implements java.io.Serializable{
	
	static final long serialVersionUID = 82866773447549292L;
	
	final static String fsm_sinaConsumerKey = "1290385296";
	final static String fsm_sinaConsumerSecret = "508aa0bfd4b1d039bdf48374f5703d2b";
	
	final static String fsm_qqConsumerKey	= "06d0a334755146d9b3dfe024e205f36b";
	final static String fsm_qqConsumerSecret = "dcc64862deec1a57e98b6985c405e369";
	
	final static String fsm_sina_request_token_api = "http://api.t.sina.com.cn/oauth/request_token";
	final static String fsm_sina_auth_api = "http://api.t.sina.com.cn/oauth/authorize";
	final static String fsm_sina_access_api = "http://api.t.sina.com.cn/oauth/access_token";
	
	final static String fsm_qq_request_token_api = "https://open.t.qq.com/cgi-bin/request_token";
	final static String fsm_qq_auth_api = "https://open.t.qq.com/cgi-bin/authorize";
	final static String fsm_qq_access_api = "https://open.t.qq.com/cgi-bin/access_token";
	
	class SinaRequestToken extends OAuthToken {
		
		public String m_accessToken = null;
		public String m_accessTokenSecret = null;
		
	    private static final long serialVersionUID = -821436584546957952L;

	    SinaRequestToken(String token, String tokenSecret) {
	        super(token, tokenSecret);
	    }

	    public String getAuthorizationURL() {
	        return fsm_sina_auth_api + "?oauth_token=" + getToken();
	    }
	    
	    public void setAccessToken(String _token,String _secret){
	    	m_accessToken = _token;
	    	m_accessTokenSecret = _secret;
	    }

	    /**
	     * since Weibo4J 2.0.10
	     */
	    public String getAuthenticationURL() {
	        return "api.t.sina.com.cn/oauth/authenticate" + "?oauth_token=" + getToken();
	    }
	}
		
	public String m_weiboType = "";
	public String m_bber = "";
	
	Object m_requestToken = null;
	
	public WeiboAuth(String _bber,String _weiboType){
		m_bber 			= _bber;
		m_weiboType		= _weiboType;
	}
	
	public String getAccessTokenString(){
		if(m_requestToken == null){
			return null;
		}
		
		if(m_weiboType.equals("sina")){
			SinaRequestToken t_requestToken = (SinaRequestToken)m_requestToken;
			if(t_requestToken.m_accessToken == null){
				return null;
			}
			return t_requestToken.m_accessToken +"&"+t_requestToken.m_accessTokenSecret;
		}else if(m_weiboType.equals("qq")){
			
			QOauthKey key = (QOauthKey)m_requestToken;
			if(key.accessToken == null){
				return null;
			}
			return key.accessToken + "&" + key.accessSecrect;
		}

		return null;
	}
		
	public String getRequestURL(String _callback)throws Exception{
		
		if(m_weiboType.equals("sina")){
			
			OAuth t_auth = new OAuth(fsm_sinaConsumerKey,fsm_sinaConsumerSecret);
			
			PostParameter[] t_param = 
			{
				new PostParameter("oauth_callback",_callback),
				new PostParameter("source",fsm_sinaConsumerKey)
			};
			
			String t_authHeader = t_auth.generateAuthorizationHeader("POST", fsm_sina_request_token_api, t_param, null);
			
			PostParameter[] t_header= 
			{
				new PostParameter("Authorization",t_authHeader),
			};
			
			// encode the callback param
			// 
			t_param[0].value = URLEncoder.encode(t_param[0].value,"UTF-8"); 
			
			String t_response = requestURL(fsm_sina_request_token_api,"POST",t_header,t_param);
			if(t_response.indexOf("&") == -1){
				throw new Exception("授权出现问题："+t_response);
			}
			
			String[] t_arr = t_response.split("&");
			
			SinaRequestToken t_requestToken = new SinaRequestToken(t_arr[0].split("=")[1],t_arr[1].split("=")[1].replace("\n", ""));
			m_requestToken = t_requestToken;
			
			return fsm_sina_auth_api + "?oauth_token=" + t_requestToken.getToken();
			
		}else if(m_weiboType.equals("qq")){
			
			QOauthKey key = new QOauthKey();
			key.customKey = fsm_qqConsumerKey;
			key.customSecrect = fsm_qqConsumerSecret;
			key.reset();
			
			if(_callback == null){
				key.callbackUrl = "null";
			}else{
				key.callbackUrl = _callback;
			}
		
			QOAuth oauth = new QOAuth();

			StringBuffer sbQueryString = new StringBuffer();
			
			oauth.getOauthUrl(fsm_qq_request_token_api, "GET", key.customKey,
					key.customSecrect, key.tokenKey, key.tokenSecrect, key.verify,
					key.callbackUrl, new ArrayList<QParameter>(), sbQueryString);
			
			String t_response = requestURL(fsm_qq_request_token_api,"GET",null,parseURLParam(sbQueryString.toString()));
			if(t_response.indexOf("&") == -1){
				throw new Exception("访问授权URL失败："+ t_response);
			}
			String[] t_arr = t_response.split("&");
			
			key.tokenKey 		= t_arr[0].split("=")[1];
			key.tokenSecrect 	= t_arr[1].split("=")[1].replace("\n", "");
			
			m_requestToken = key;
			
			return fsm_qq_auth_api + "?oauth_token="+ key.tokenKey;
		}
		
		return null;
	}
	
	public void genAccessToken(String PIN)throws Exception{
		
		if(m_requestToken == null){
			throw new Exception("请再次授权");
		}
		
		if(m_weiboType.equals("sina")){
			
			SinaRequestToken t_requestToken = (SinaRequestToken)m_requestToken;
			PostParameter[] t_urlParam =
			{
				new PostParameter("oauth_verifier", PIN),
				new PostParameter("source",fsm_sinaConsumerKey)
			};
			OAuth t_auth = new OAuth(fsm_sinaConsumerKey,fsm_sinaConsumerSecret);
			String authString = t_auth.generateAuthorizationHeader("POST", fsm_sina_access_api,t_urlParam,
					t_requestToken);
			
			PostParameter[] t_header= 
			{
				new PostParameter("Authorization",authString),
			};
			
			String t_response = requestURL(fsm_sina_access_api,"POST",t_header,t_urlParam);
			if(t_response.indexOf("&") == -1){
				throw new Exception("授权出现问题："+t_response);
			}
			String[] t_arr = t_response.split("&");
			
			t_requestToken.setAccessToken(t_arr[0].split("=")[1], t_arr[1].split("=")[1]);
			
		}else if(m_weiboType.equals("qq")){
			
			QOauthKey key = (QOauthKey)m_requestToken;
			key.customKey = fsm_qqConsumerKey;
			key.customSecrect = fsm_qqConsumerSecret;
			key.verify = PIN;
			key.callbackUrl = null;		
		
			QOAuth oauth = new QOAuth();

			StringBuffer sbQueryString = new StringBuffer();
			
			oauth.getOauthUrl(fsm_qq_access_api, "GET", key.customKey,
					key.customSecrect, key.tokenKey, key.tokenSecrect, key.verify,
					key.callbackUrl, new ArrayList<QParameter>(), sbQueryString);
			
			String t_response = requestURL(fsm_qq_access_api,"GET",null,parseURLParam(sbQueryString.toString()));
			if(t_response.indexOf("&") == -1){
				throw new Exception("授权出现问题："+t_response);
			}
			String[] t_arr = t_response.split("&");
			
			key.accessToken = t_arr[0].split("=")[1];
			key.accessSecrect = t_arr[1].split("=")[1];
			
		}
	}

	public Object clone()throws CloneNotSupportedException{
		WeiboAuth t_new = new WeiboAuth(m_bber,m_weiboType);
		if(m_requestToken != null){
			if(m_weiboType.equals("sina")){
				SinaRequestToken t_requestToken = (SinaRequestToken)m_requestToken;
				
				SinaRequestToken t_newToken = new SinaRequestToken(
						t_requestToken.getToken(),
						t_requestToken.getTokenSecret());
				
				t_newToken.m_accessToken 		= t_requestToken.m_accessToken;
				t_newToken.m_accessTokenSecret 	= t_requestToken.m_accessTokenSecret;
				
				t_new.m_requestToken = t_newToken;
				
			}else if(m_weiboType.equals("qq")){
				QOauthKey key = (QOauthKey)m_requestToken;
				
				QOauthKey t_newKey = new QOauthKey();
				t_newKey.customKey 		= fsm_qqConsumerKey;
				t_newKey.customSecrect 	= fsm_qqConsumerSecret;
				
				t_newKey.tokenKey 		= key.tokenKey;
				t_newKey.tokenSecrect 	= key.tokenSecrect;
				t_newKey.callbackUrl	= key.callbackUrl;
				
				t_newKey.accessToken	= key.accessToken;
				t_newKey.accessSecrect	= key.accessSecrect;
				
				t_new.m_requestToken = t_newKey;
			}
		}
		
		return t_new;
	}
	
	
	
	private String requestURL(String _url,String _method,PostParameter[] _headerParams,PostParameter[] _urlParams)throws Exception{
		
		if(_method.equals("GET")){
			_url = _url + "?" + encodeURLParam(_urlParams);
		}
		
		URL url = new URL(_url);
		HttpURLConnection con = (HttpURLConnection)url.openConnection();
		try{
			con.setDoInput(true);
			if(_method.equals("POST")){
				con.setRequestMethod("POST");
				con.setDoOutput(true);
			}
			
			con.setAllowUserInteraction(false);

			if(_headerParams != null && _headerParams.length != 0){
				for(PostParameter par : _headerParams){
					con.setRequestProperty(par.getName(),par.getValue());
				}
			}
			
			if(_method.equals("POST")){
				String t_params = encodeURLParam(_urlParams);
				byte[] bytes = t_params.getBytes("UTF-8");
				
				con.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
				con.setRequestProperty("Content-Length",Integer.toString(bytes.length));
				
				OutputStream t_os = con.getOutputStream();
				try{
					t_os.write(bytes);
					t_os.flush();
				}finally{				
					t_os.close();
				}
			}
			
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
			try{				
				return in.readLine();
			}finally{
				in.close();
			}
		}finally{
			con.disconnect();
		}
	}
	
	public static PostParameter[] parseURLParam(String _urlParams){
		String[] t_arr = _urlParams.split("&");
		PostParameter[] ret = new PostParameter[t_arr.length];
		
		for(int i = 0;i < t_arr.length;i++){
			String[] parse = t_arr[i].split("=");
			ret[i] = new PostParameter(parse[0],parse[1]);
		}
		return ret;
	}
	
	public static String encodeURLParam(PostParameter[] _params){
		StringBuffer ret= new StringBuffer();
		for(int i = 0;i<_params.length;i++){
			PostParameter par = _params[i];
			ret.append(par.getName()).append("=").append(par.getValue());
			if(i + 1 < _params.length){
				ret.append("&");
			}
		}
		return ret.toString();
	}
	
	
}
