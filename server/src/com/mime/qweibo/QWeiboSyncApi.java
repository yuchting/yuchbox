package com.mime.qweibo;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import weibo4j.org.json.JSONObject;

import com.mime.qweibo.QWeiboType.PageFlag;
import com.mime.qweibo.QWeiboType.ResultType;

public class QWeiboSyncApi {

	final static String fsm_requestTokenURL 			= "https://open.t.qq.com/cgi-bin/request_token";
	final static String fsm_requestTokenURL_callback	= "";
	final static String fsm_accessTokenURL				= "https://open.t.qq.com/cgi-bin/access_token";
	
	final static String fsm_verifyURL					= "http://open.t.qq.com/api/user/info";
	
	OauthKey 			m_oauthKey = new OauthKey();
	QWeiboRequest 		m_request = new QWeiboRequest();
	
	List<QParameter> 	m_parameters = new ArrayList<QParameter>();	
	
	public void setCostomerKey(String customKey, String customSecret){
		m_oauthKey.customKey = customKey;
		m_oauthKey.customSecrect = customSecret;
	}
	
	public void setAccessToken(String tokenKey, String tokenSecret){
		m_oauthKey.tokenKey = tokenKey;
		m_oauthKey.tokenSecrect = tokenSecret;
	}
	
	private void checkAllKeys()throws Exception{
		if(m_oauthKey.customKey == null || m_oauthKey.customSecrect == null){
			throw new Exception("customKey or customSecrect is null");
		}
		
		if(m_oauthKey.tokenKey == null || m_oauthKey.tokenSecrect == null){
			throw new Exception("access tokenKey or access tokenSecrect is null");
		}
	}
	
	// return user id if verify ok
	public long verifyCredentials()throws Exception{
		checkAllKeys();
		
		m_parameters.clear();
		m_parameters.add(new QParameter("format", "json"));
		
		JSONObject t_json = new JSONObject(m_request.syncRequest(fsm_verifyURL, "GET", m_oauthKey, m_parameters, null));
		if(t_json.getInt("ret") != 0){
			throw new Exception("verify Credentials failed." + t_json.toString());
		}
		
		return t_json.getLong("Uid");
	}
	
	public String getRequestToken()throws Exception{
		
		if(m_oauthKey.customKey == null || m_oauthKey.customSecrect == null){
			throw new Exception("customKey or customSecrect is null");
		}
		
		m_parameters.clear();
		m_oauthKey.reset();
				
		//The OAuth Call back URL(You should encode this url if it
		//contains some unreserved characters).
		//
		m_oauthKey.callbackUrl = fsm_requestTokenURL_callback;

		return m_request.syncRequest(fsm_requestTokenURL, "GET", m_oauthKey, m_parameters, null);
	}

	public String getAccessToken(String verify)throws Exception{

		checkAllKeys();
				
		m_parameters.clear();
		m_oauthKey.verify = verify;

		return m_request.syncRequest(fsm_accessTokenURL, "GET", m_oauthKey, m_parameters, null);		
	}

	/**
	 * Get home page messages.
	 * 
	 * @param customKey
	 *            Your AppKey
	 * @param customSecret
	 *            Your AppSecret
	 * @param requestToken
	 *            The access token
	 * @param requestTokenSecret
	 *            The access token secret
	 * @param format
	 *            Response format, xml or json
	 * @param pageFlag
	 *            Page number.
	 * @param nReqNum
	 *            Number of messages you want.
	 * @return Response messages based on the specified format.
	 */
	public String getHomeMsg(String customKey, String customSecret,
			String requestToken, String requestTokenSecrect, ResultType format,
			PageFlag pageFlag, int nReqNum) {

		String url = "http://open.t.qq.com/api/statuses/home_timeline";
		List<QParameter> parameters = new ArrayList<QParameter>();
		OauthKey oauthKey = new OauthKey();
		oauthKey.customKey = customKey;
		oauthKey.customSecrect = customSecret;
		oauthKey.tokenKey = requestToken;
		oauthKey.tokenSecrect = requestTokenSecrect;

		String strFormat = null;
		if (format == ResultType.ResultType_Xml) {
			strFormat = "xml";
		} else if (format == ResultType.ResultType_Json) {
			strFormat = "json";
		} else {
			return "";
		}

		parameters.add(new QParameter("format", strFormat));
		parameters.add(new QParameter("pageflag", String.valueOf(pageFlag
				.ordinal())));
		parameters.add(new QParameter("reqnum", String.valueOf(nReqNum)));

		QWeiboRequest request = new QWeiboRequest();
		String res = null;
		try {
			res = request.syncRequest(url, "GET", oauthKey, parameters, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}

	/**
	 * Publish a Weibo message.
	 * 
	 * @param customKey
	 *            Your AppKey
	 * @param customSecret
	 *            Your AppSecret
	 * @param requestToken
	 *            The access token
	 * @param requestTokenSecrect
	 *            The access token secret
	 * @param content
	 *            The content of your message
	 * @param pic
	 *            The files of your images.
	 * @param format
	 *            Response format, xml or json(Default).
	 * @return Result info based on the specified format.
	 */
	public String publishMsg(String customKey, String customSecret,
			String requestToken, String requestTokenSecrect, String content,
			String pic, ResultType format) {

		List<QParameter> files = new ArrayList<QParameter>();
		String url = null;
		String httpMethod = "POST";

		if (pic == null || pic.trim().equals("")) {
			url = "http://open.t.qq.com/api/t/add";
		} else {
			url = "http://open.t.qq.com/api/t/add_pic";
			files.add(new QParameter("pic", pic));
		}

		OauthKey oauthKey = new OauthKey();
		oauthKey.customKey = customKey;
		oauthKey.customSecrect = customSecret;
		oauthKey.tokenKey = requestToken;
		oauthKey.tokenSecrect = requestTokenSecrect;

		List<QParameter> parameters = new ArrayList<QParameter>();

		String strFormat = null;
		if (format == ResultType.ResultType_Xml) {
			strFormat = "xml";
		} else if (format == ResultType.ResultType_Json) {
			strFormat = "json";
		} else {
			return "";
		}

		parameters.add(new QParameter("format", strFormat));
		try {
			parameters.add(new QParameter("content", new String(content
					.getBytes("UTF-8"))));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
			return "";
		}
		parameters.add(new QParameter("clientip", "127.0.0.1"));

		QWeiboRequest request = new QWeiboRequest();
		String res = null;
		try {
			res = request.syncRequest(url, httpMethod, oauthKey, parameters,
					files);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}
}
