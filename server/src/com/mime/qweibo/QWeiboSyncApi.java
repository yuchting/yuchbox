package com.mime.qweibo;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import com.mime.qweibo.QWeiboType.PageFlag;
import com.mime.qweibo.QWeiboType.ResultType;

public class QWeiboSyncApi {

	/**
	 * Get request token.
	 * 
	 * @param customKey
	 *            Your AppKey.
	 * @param customSecret
	 *            Your AppSecret.
	 * @return The request token.
	 */
	public String getRequestToken(String customKey, String customSecret) {
		String url = "https://open.t.qq.com/cgi-bin/request_token";
		List<QParameter> parameters = new ArrayList<QParameter>();
		OauthKey oauthKey = new OauthKey();
		oauthKey.customKey = customKey;
		oauthKey.customSecrect = customSecret;
		//The OAuth Call back URL(You should encode this url if it
		//contains some unreserved characters).
		oauthKey.callbackUrl = "http://www.qq.com";

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
	 * Get access token.
	 * 
	 * @param customKey
	 *            Your AppKey.
	 * @param customSecret
	 *            Your AppSecret
	 * @param requestToken
	 *            The request token.
	 * @param requestTokenSecret
	 *            The request token Secret
	 * @param verify
	 *            The verification code.
	 * @return
	 */
	public String getAccessToken(String customKey, String customSecret,
			String requestToken, String requestTokenSecrect, String verify) {

		String url = "https://open.t.qq.com/cgi-bin/access_token";
		List<QParameter> parameters = new ArrayList<QParameter>();
		OauthKey oauthKey = new OauthKey();
		oauthKey.customKey = customKey;
		oauthKey.customSecrect = customSecret;
		oauthKey.tokenKey = requestToken;
		oauthKey.tokenSecrect = requestTokenSecrect;
		oauthKey.verify = verify;

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
