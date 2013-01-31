/**
 *  Dear developer:
 *  
 *   If you want to modify this file of project and re-publish this please visit:
 *  
 *     http://code.google.com/p/yuchberry/wiki/Project_files_header
 *     
 *   to check your responsibility and my humble proposal. Thanks!
 *   
 *  -- 
 *  Yuchs' Developer    
 *  
 *  
 *  
 *  
 *  尊敬的开发者：
 *   
 *    如果你想要修改这个项目中的文件，同时重新发布项目程序，请访问一下：
 *    
 *      http://code.google.com/p/yuchberry/wiki/Project_files_header
 *      
 *    了解你的责任，还有我卑微的建议。 谢谢！
 *   
 *  -- 
 *  语盒开发者
 *  
 */
package com.yuchting.yuchberry.server;


import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyStore;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import weibo4j.http.OAuth;
import weibo4j.http.OAuthToken;
import weibo4j.http.PostParameter;

import com.mime.qweibo.OauthKey;
import com.mime.qweibo.QOAuth;
import com.mime.qweibo.QParameter;


/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 * representing unauthorized Request Token which is passed to the service provider when acquiring the authorized Access Token
 */
class SinaRequestToken extends OAuthToken {
  
    private static final long serialVersionUID = -821436584546957952L;

    SinaRequestToken(String token, String tokenSecret) {
        super(token, tokenSecret);
    }

    public String getAuthorizationURL() {
        return "http://api.t.sina.com.cn/oauth/authorize" + "?oauth_token=" + getToken();
    }

    /**
     * since Weibo4J 2.0.10
     */
    public String getAuthenticationURL() {
        return "api.t.sina.com.cn/oauth/authenticate" + "?oauth_token=" + getToken();
    }
}

class QQRequestToken extends OAuthToken {
	  
    private static final long serialVersionUID = -45365845469512122L;

    QQRequestToken(String token, String tokenSecret) {
        super(token, tokenSecret);
    }

    public String getAuthorizationURL() {
        return "https://open.t.qq.com/cgi-bin/authorize" + "?oauth_token=" + getToken();
    }

    /**
     * since Weibo4J 2.0.10
     */
    public String getAuthenticationURL() {
        return "open.t.qq.com/cgi-bin/authenticate" + "?oauth_token=" + getToken();
    }
}

final class EncodeFormat{
	public int width = 0;
	public String encode = "";
}

/*!
 *  @brief note
 *  @author tzz
 *  @version 0.1
 */
public class HelloWorld {
	/*!
	 *  @brief main function
	 *  @param arg  parameters
	 */
	private static abstract class TaskTest<Params, Result> implements Callable<Result> {
        Params[] mParams;
    }
	
	private Vector mWeibo2smsList = null;
	
	private Vector mWeibo2SMSBufferList = null;
	
	class Weibo2SMS{
		
		String	weiboId;
		String	smsPhone;
		
		public Weibo2SMS(String _weiboId,String _smsPhone){
			weiboId		= _weiboId;
			smsPhone	= _smsPhone;
		}
	}
	
	private void loadWeibo2smsFile(){
		try{
			File fc = new File("weibo2sms.txt");
			try{
				if(fc.exists()){
					if(mWeibo2smsList == null){
						mWeibo2smsList 			= new Vector();
						mWeibo2SMSBufferList	= new Vector();
					}else{
						mWeibo2smsList.removeAllElements();
					}					
					
					InputStream tReadFile = new FileInputStream(fc);
		    		try{
		    			byte[] tBytes = new byte[(int)fc.length()];
		    			sendReceive.ForceReadByte(tReadFile, tBytes, tBytes.length);
		    			
		    			String tFileContent = new String(tBytes,"UTF-8");
		    			
		    			String tWeiboId		= null;
		    			String tSMSPhone	= null;
		    			
		    			int tIdx = 0;
		    			while(tIdx < tFileContent.length()){
		    				char c = tFileContent.charAt(tIdx);
		    				
		    				if(c == ':'){
		    					
		    					tWeiboId = tFileContent.substring(0,tIdx);
		    					tFileContent = tFileContent.substring(tIdx + 1);
		    					
		    					tIdx = 0;
		    					
		    					continue;
		    					
		    				}else if(c == '\r' || c == '\n'){			
		    					
		    					tSMSPhone 		= tFileContent.substring(0,tIdx);
		    					
		    					while(tIdx + 1 < tFileContent.length()){
		    						char next = tFileContent.charAt(tIdx + 1);
		    						if(next == '\r' || next == '\n'){
		    							tIdx++;
		    						}else{
		    							break;
		    						}
		    					}
		    					
		    					tFileContent 	= tFileContent.substring(tIdx + 1);		    					
		    					tIdx = 0;
		    					
		    					if(tWeiboId != null && tWeiboId.length() > 0 && tSMSPhone.length() > 0){
		    						
		    						mWeibo2smsList.addElement(new Weibo2SMS(tWeiboId, tSMSPhone));
		    						
		    						//m_mainApp.SetErrorString("w2s:" + tWeiboId + "->" + tSMSPhone);
		    						System.out.println("w2s:" + tWeiboId + "->" + tSMSPhone);
		    					}
		    					
		    					tWeiboId	= null;
		    					tSMSPhone	= null;
		    					
		    					continue;
		    				}
		    				
		    				tIdx++;
		    			}
		    			
		    			tBytes			= null;
		    			tFileContent	= null;
		    			
		    		}finally{
		    			tReadFile.close();
		    		}
				}
			}finally{
				//fc.close();
			}
		}catch(Exception e){
			
		}
	}
	public static void main(String arg[])throws Exception{
		
		
//		String pass = "ZKX8kPEYfjtVMhs9G";
//		
//		if(!pass.equals("dJtdxiIrGMRYF1X") && !pass.equals("ZKX8kPEYfjtVMhs9G")){
//			System.out.print("fun");
//			return;
//		}
//		
//		System.out.print("haha");
		
//		String t_test = "  =?UTF-8?B?UmU6IOWwmumCruiHquWKqOWbnuWkje+8muetlOWkje+8miBb6K+t55uSXSDogIHlpKc=?=" +
//						"=?UTF-8?B?77+95o6S5p+l5a6Y77+95Z+f5ZCN6Kej5p6Q?=";
//		System.out.println(fetchEmail.DecodeName(t_test, false));
//		
		
//		String t_test		= "=?gb2312?B?t7Hzd5x51Ic=?=";
//		String t_convert	= MimeUtility.decodeText(t_test);
//		
//		System.out.println(MimeUtility.decodeText(t_test));
//		System.out.println(fetchEmail.DecodeName(t_test, false));
//		

		
//		System.setProperty("proxySet", "true");
//		System.setProperty("proxyHost", "127.0.0.1");
//		System.setProperty("proxyPort", "8088");
//		
//		System.out.println(fetchTWeibo.replaceGFWVerified_URL("http://t.co/HPjdqFCN sdfsdfs http://bit.ly/PU7zFy sdfskljfeind sdfjsdkfs "));
		//berryRecvTest();
		//berrySendTest();
		
		requestPOSTHTTP("http://localhost:8888",(new String("{\"return_code\":0,\"return_message\":\"success\",\"data\":{\"data\":[{\"id\":\"1\",\"question\":\"公主令牌在哪交？\"},{\"id\":\"2\",\"question\":\"公主护使有什么用？\"},{\"id\":\"3\",\"question\":\"角斗场在哪？\"},{\"id\":\"4\",\"question\":\"北部断层在哪？\"},{\"id\":\"5\",\"question\":\"欢乐令有什么用？\"},{\"id\":\"6\",\"question\":\"令牌积分有什么用？\"},{\"id\":\"7\",\"question\":\"南部断层在哪？\"},{\"id\":\"8\",\"question\":\"大妖魔令牌交给谁？\"},{\"id\":\"9\",\"question\":\"神工坊在哪？\"},{\"id\":\"10\",\"question\":\"警戒妖珠有什么用？\"}]}}")).getBytes("UTF-8"),true);
	}
	
	/**
	 * request the url via POST
	 * @param _url
	 * @param _paramsName
	 * @param _paramsValue
	 * @param _gzip
	 * @return
	 * @throws Exception
	 */
	private static String requestPOSTHTTP(String _url,String[] _paramsName,String[] _paramsValue)throws Exception{
		
		if(_paramsName == null || _paramsValue == null || _paramsName.length != _paramsValue.length){
			throw new IllegalArgumentException("_paramsName == null || _paramsValue == null || _paramsName.length != _paramsValue.length");
		}
		
		StringBuffer tParam = new StringBuffer();
		for(int i = 0;i < _paramsName.length;i++){
			if(tParam.length() != 0){
				tParam.append('&');
			}
			
			tParam.append(_paramsName[i]).append('=').append(_paramsValue);
		}
		
		return requestPOSTHTTP(_url,tParam.toString().getBytes("UTF-8"),false);
		
	}
	
	/**
	 * post the http request directly by content
	 * @param _url
	 * @param _content
	 * @param _gzip
	 * @return
	 * @throws Exception
	 */
	private static String requestPOSTHTTP(String _url,byte[] _content,boolean _gzip)throws Exception{
		
		byte[] tParamByte = _content;
		
		// Attempt to gzip the data
		if(_gzip){
			ByteArrayOutputStream zos = new ByteArrayOutputStream();
			try{
				GZIPOutputStream zo = new GZIPOutputStream(zos,6);
				try{
					zo.write(tParamByte);
				}finally{
					zo.close();
					zo = null;
				}
				
				byte[] tZipByte = zos.toByteArray();
				if(tZipByte.length < tParamByte.length){
					tParamByte = tZipByte;
				}else{
					_gzip = false;
				}
			}finally{
				zos.close();
				zos = null;
			}
		}
		
		HttpURLConnection conn = (HttpURLConnection)(new URL(_url)).openConnection();
		try{
			
			conn.setRequestMethod("POST");
			//conn.setRequestProperty(HttpProtocolConstants.HEADER_CONTENT_LENGTH,String.valueOf(tParamByte.length));
			
			conn.setDoOutput(true);
			
			OutputStream out = conn.getOutputStream();
			try{
				out.write(tParamByte);
				out.flush();
			}finally{
				out.close();
				out = null;
			}
			
			int rc = conn.getResponseCode();
		    if(rc != 200){
		    	throw new IOException("HTTP response code: " + rc);
		    }
		    
		    InputStream in = conn.getInputStream();
		    try{
		    	int length = -1;
		    	int ch;
		    	byte[] result;
		    	
		    	if (length != -1){
		    		
		    		result = new byte[length];
		    		in.read(result);
		    		
		    	}else{
		    		
		    		ByteArrayOutputStream os = new ByteArrayOutputStream();
		    		try{

				        while ((ch = in.read()) != -1){
				        	os.write(ch);
				        }
				        
				        result = os.toByteArray();
				        
		    		}finally{
		    			os.close();
		    			os = null;
		    		}
			    }
		    	
		    	if(_gzip){
		    		ByteArrayInputStream gin = new ByteArrayInputStream(result);
					try{
						GZIPInputStream zi	= new GZIPInputStream(gin);
						try{
							ByteArrayOutputStream os = new ByteArrayOutputStream();
				    		try{

								while((ch = zi.read()) != -1){
									os.write(ch);
								}
								result = os.toByteArray();
								
				    		}finally{
				    			os.close();
				    			os = null;
				    		}
						}finally{
							zi.close();
						}
					}finally{
						gin.close();
					}
		        }
		    	
		    	return new String(result,"UTF-8");

		    }finally{
		    	in.close();
		    	in = null;
		    }

		}finally{
			conn.disconnect();
			conn = null;
		}
	}
		
	public final static String	fsm_vectStringSpliter = "<>";
	public final static String	fsm_vectStringSpliter_sub = "@#&";
	
	public final static String[] fsm_groupSubjectPrefix = 
    {
    	"Re: ",
    	"Re:",
    	"Re： ",
    	"Re：",
    	"RE: ",
    	"RE:",
    	"RE： ",
    	"RE：",
    	"回复: ",
    	"回复:",
    	"回复： ",
    	"回复：",
    	"答复: ",
    	"答复:",
    	"答复： ",
    	"答复：",
    };
	
	private static String groupSubject(String _orgSub){
		int t_index = -1;
    	int t_length = 0;
    	for(String pre:fsm_groupSubjectPrefix){
    		int last = _orgSub.lastIndexOf(pre); 
    		if(last != -1){
    			if(last > t_index){
    				t_length = pre.length();
    				t_index = last;
    			}
    		}
    	}
    	
    	if(t_index != -1){
    		_orgSub = _orgSub.substring(t_index + t_length);
    	} 	
    	
    	return _orgSub.replace('\'', ' ');
    }
	
	static public void testBoolean(Object bool){
		if(((Boolean)bool).booleanValue()){
			System.out.print("aa");
		}
		
	}
		
	static public void testMDS()throws Exception{
		String host = "45562.yuchberry.info";
		int port	= 19781;
		byte[] bytes = {0x10,0x08,0,0,0,0,0,0,0,0,0,0,0,0,0};
		
		DatagramSocket ds = new DatagramSocket();
		DatagramPacket dp = new DatagramPacket(bytes, bytes.length, InetAddress
                .getByName(host), port);
		ds.send(dp);
		
		bytes = new byte[64];
		dp = new DatagramPacket(bytes, bytes.length);
		ds.receive(dp);
		
		System.out.print(bytes);
	}
	
	static public void qgetOAuthRequest(){

		try{
//			String t_consumer_key = fetchSinaWeibo.fsm_consumer_key;
//			String t_consumer_secret = fetchSinaWeibo.fsm_consumer_serect;
//			
//			String t_request_token_api = "http://api.t.sina.com.cn/oauth/request_token";
//			String t_auth_api = "http://api.t.sina.com.cn/oauth/authorize";
//			String t_access_api = "http://api.t.sina.com.cn/oauth/access_token";
			
			String t_consumer_key = fetchQWeibo.fsm_consumerKey;
			String t_consumer_secret = fetchQWeibo.fsm_consumerSecret;
			
			String t_request_token_api = "https://open.t.qq.com/cgi-bin/request_token";
			String t_auth_api = "http://open.t.qq.com/cgi-bin/authorize";
			String t_access_api = "https://open.t.qq.com/cgi-bin/access_token";
			
//			String t_consumer_key = "4f362fc10f797de70f6d78e18246d2ae04dfaae00";
//			String t_consumer_secret = "8ed9d6ede362a23dc38981bd71c333fc";
//			
//			String t_request_token_api = "http://api.imgur.com/oauth/request_token";
//			String t_auth_api = "http://api.imgur.com/oauth/authorize";
//			String t_access_api = "http://api.imgur.com/oauth/access_token";
			
			OauthKey key = new OauthKey();
			key.customKey = t_consumer_key;
			key.customSecrect = t_consumer_secret;
			key.reset();
			key.callbackUrl = "http://127.0.0.1:8888/?bber=aaa.@gg.com";
			String t_response = null;
			
			QOAuth oauth = new QOAuth();
			
//			QWeiboRequest t_request = new QWeiboRequest();
//			t_response = t_request.syncRequest(t_request_token_api, "GET", key, new ArrayList<QParameter>(), null);

			StringBuffer sbQueryString = new StringBuffer();
			oauth.getOauthUrl(t_request_token_api, "GET", key.customKey,
					key.customSecrect, key.tokenKey, key.tokenSecrect, key.verify,
					key.callbackUrl, new ArrayList<QParameter>(), sbQueryString);
			
			String queryString = sbQueryString.toString();
						
//			java.util.logging.Logger.getLogger("org.apache.http.wire").setLevel(java.util.logging.Level.ALL);
//			java.util.logging.Logger.getLogger("org.apache.http.headers").setLevel(java.util.logging.Level.ALL);
//
//			System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
//			System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
//			System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire", "debug");
//			System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http", "debug");
//			System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.headers", "debug");
			
			//QHttpClient http = new QHttpClient();
			//t_response = http.httpGet(t_request_token_api,queryString);
			
			
			URL url = new URL(t_request_token_api + "?" + queryString);
			URLConnection con = url.openConnection();


			con.setAllowUserInteraction(false);
			//con.connect();
			
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
			try{
				StringBuffer t_stringBuffer = new StringBuffer();
				
				String temp;
				while ((temp = in.readLine()) != null) {
					t_stringBuffer.append(temp+"\n");
				}
				
				t_response = t_stringBuffer.toString();
			}finally{
				in.close();
			}
			
			String[] t_arr = t_response.split("&");
			//SinaRequestToken t_requestToken = new SinaRequestToken(t_arr[0].split("=")[1],t_arr[1].split("=")[1].replace("\n", ""));
			//SinaRequestToken t_requestToken = new SinaRequestToken(t_arr[1].split("=")[1],t_arr[2].split("=")[1].replace("\n", ""));
			QQRequestToken t_requestToken = new QQRequestToken(t_arr[0].split("=")[1],t_arr[1].split("=")[1].replace("\n", ""));
						
			System.out.println("Open URL:" + t_auth_api+"?oauth_token="+t_requestToken.getToken());
			
			System.out.print("input PIN:");
			BufferedReader bufin = new BufferedReader(new InputStreamReader(System.in)); 
			String PIN = bufin.readLine();			
			
			key.tokenKey = t_requestToken.getToken();
			key.tokenSecrect = t_requestToken.getTokenSecret();
			key.verify = PIN;
			key.callbackUrl = null;
			
			sbQueryString = new StringBuffer();
			oauth.getOauthUrl(t_access_api, "GET", key.customKey,
					key.customSecrect, key.tokenKey, key.tokenSecrect, key.verify,
					key.callbackUrl, new ArrayList<QParameter>(), sbQueryString);
			
			url = new URL(t_access_api + "?" + sbQueryString.toString());
			con = url.openConnection();
			
			con.setAllowUserInteraction(false);
			con.connect();
						
			in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
			try{
				StringBuffer t_stringBuffer = new StringBuffer();
				
				String temp;
				while ((temp = in.readLine()) != null) {
					t_stringBuffer.append(temp+"\n");
				}
				
				t_response = t_stringBuffer.toString();
			}finally{
				in.close();	
			}
			
			System.out.println("access response: " + t_response);
	
		}catch(Exception e){
			prt(e.getMessage());
			e.printStackTrace();
		}
	}
	
	
	static public void getOAuthRequest(){
		
		try{
			String t_consumer_key = fetchSinaWeibo.fsm_consumer_key;
			String t_consumer_secret = fetchSinaWeibo.fsm_consumer_serect;
			
			String t_request_token_api = "http://api.t.sina.com.cn/oauth/request_token";
			String t_auth_api = "http://api.t.sina.com.cn/oauth/authorize";
			String t_access_api = "http://api.t.sina.com.cn/oauth/access_token";
			
//			String t_consumer_key = fetchQWeibo.fsm_consumerKey;
//			String t_consumer_secret = fetchQWeibo.fsm_consumerSecret;
//			
//			String t_request_token_api = "http://open.t.qq.com/cgi-bin/request_token";
//			String t_auth_api = "https://open.t.qq.com/cgi-bin/authorize";
//			String t_access_api = "https://open.t.qq.com/cgi-bin/access_token";
			
//			String t_consumer_key = "4f362fc10f797de70f6d78e18246d2ae04dfaae00";
//			String t_consumer_secret = "8ed9d6ede362a23dc38981bd71c333fc";
//			
//			String t_request_token_api = "http://api.imgur.com/oauth/request_token";
//			String t_auth_api = "http://api.imgur.com/oauth/authorize";
//			String t_access_api = "http://api.imgur.com/oauth/access_token";
			
			OAuth t_auth = new OAuth(t_consumer_key,t_consumer_secret);
						
			URL url = new URL(t_request_token_api);
			HttpURLConnection con = (HttpURLConnection)url.openConnection();

			con.setDoInput(true);
			con.setRequestMethod("POST");
			con.setDoOutput(true);
			
			PostParameter[] t_param = 
			{
					new PostParameter("oauth_callback","http://127.0.0.1:8888"),
					new PostParameter("source",t_consumer_key)
			};
			
			String t_authHeader = t_auth.generateAuthorizationHeader("POST", t_request_token_api, t_param, null);
			con.setRequestProperty("Authorization",t_authHeader);
			
			String t_params = "oauth_callback=http://127.0.0.1:8888" + "&source=" + t_consumer_key;
			byte[] bytes = t_params.getBytes("UTF-8");
			
			con.setRequestProperty("Content-Type",
            "application/x-www-form-urlencoded");
			
			con.setRequestProperty("Content-Length",
                    Integer.toString(bytes.length));
			
			System.out.println("param: " + t_params);
			
			OutputStream t_os = con.getOutputStream();		
					
			t_os.write(bytes);
			t_os.flush();
			t_os.close();			
			
			String t_response = null;
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
			try{
				StringBuffer t_stringBuffer = new StringBuffer();
				
				String temp;
				while ((temp = in.readLine()) != null) {
					t_stringBuffer.append(temp+"\n");
				}
				
				t_response = t_stringBuffer.toString();
			}finally{
				in.close();	
			}
			
			String[] t_arr = t_response.split("&");
			SinaRequestToken t_requestToken = new SinaRequestToken(t_arr[0].split("=")[1],t_arr[1].split("=")[1].replace("\n", ""));
			//SinaRequestToken t_requestToken = new SinaRequestToken(t_arr[1].split("=")[1],t_arr[2].split("=")[1].replace("\n", ""));
			
			
			System.out.println("Open URL:" + t_auth_api+"?oauth_token="+t_requestToken.getToken());
			
			System.out.print("input PIN:");
			BufferedReader bufin = new BufferedReader(new   InputStreamReader(System.in)); 
			String PIN = bufin.readLine();			
						
			
			url = new URL(t_access_api);
			HttpURLConnection tcon = (HttpURLConnection)url.openConnection();
			
			tcon.setDoInput(true);
			tcon.setRequestMethod("POST");
			tcon.setDoOutput(true);
			
			String authString = t_auth.generateAuthorizationHeader("POST", t_access_api, 
										new PostParameter[]
										{
											new PostParameter("oauth_verifier", PIN),
											new PostParameter("source",t_consumer_key)
										},
										t_requestToken);
			
			tcon.setRequestProperty("Authorization",authString);
			System.out.println("AuthString: " + authString);
			
			t_params = "oauth_verifier=" + PIN + "&source=" + t_consumer_key;
			bytes = t_params.getBytes("UTF-8");
			
			tcon.setRequestProperty("Content-Type",
            "application/x-www-form-urlencoded");
			
			tcon.setRequestProperty("Content-Length",
                    Integer.toString(bytes.length));
			
			System.out.println("param: " + t_params);
			
			t_os = tcon.getOutputStream();		
					
			t_os.write(bytes);
			t_os.flush();
			t_os.close();
			
			in = new BufferedReader(new InputStreamReader(tcon.getInputStream(), "UTF-8"));
			try{
				StringBuffer t_stringBuffer = new StringBuffer();
				
				String temp;
				while ((temp = in.readLine()) != null) {
					t_stringBuffer.append(temp+"\n");
				}
				
				t_response = t_stringBuffer.toString();
			}finally{
				in.close();	
			}
			
			System.out.println("access response: " + t_response);
	
		}catch(Exception e){
			prt(e.getMessage());
			e.printStackTrace();
		}
				
	}
	
	
	static public void berrySendTest(){
		
		try{
			
			Socket t_socket = GetSocketServer("111111","localhost",9716,false);
			sendReceive t_receive = new sendReceive(t_socket);
			
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			os.write(msg_head.msgConfirm);
			sendReceive.WriteString(os, "111111",false);
			sendReceive.WriteInt(os,4);
			os.write(0);
			sendReceive.WriteString(os,"1.10.1556",false);
			t_receive.SendBufferToSvr(os.toByteArray(), false);
			
			fetchMail t_mail = new fetchMail(false);
			
			String[] t_string = {"yuchting@gmail.com"};
			t_mail.SetSendToVect(t_string);
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			t_mail.SetContain(df.format(new Date()));
			t_mail.SetSubject(t_mail.GetContain());
			
			final int t_math = (int)(Math.random() * 100);
			t_mail.SetMailIndex(t_math);
						
			
			os = new ByteArrayOutputStream();
			os.write(msg_head.msgMail);
			
			t_mail.OutputMail(os);
			os.write(fetchMail.NOTHING_STYLE);
			
			os.write(1);
			
			t_receive.SendBufferToSvr(os.toByteArray(), true);
			
			ByteArrayInputStream in = new ByteArrayInputStream(t_receive.RecvBufferFromSvr());
			
			if(in.read() == msg_head.msgSendMail
				&& t_math == sendReceive.ReadInt(in)){
				prt(t_mail.GetSubject() + " mail deliver succ id<" + Integer.toString(t_math) + ">");
			}
						
		}catch(Exception _e){
			prt(_e.getMessage());
			_e.printStackTrace();
		}
	}
	
	static public void berrySendWeiboTest(){
		try{
			
			Socket t_socket = GetSocketServer("111111","192.168.10.20",9716,false);
			sendReceive t_receive = new sendReceive(t_socket);
			
			ByteArrayOutputStream t_stream = new ByteArrayOutputStream();
			t_stream.write(msg_head.msgConfirm);
			sendReceive.WriteString(t_stream, "111111",false);
			sendReceive.WriteInt(t_stream,5);
			t_stream.write(0);
			sendReceive.WriteString(t_stream,"1.1.715",false);
			sendReceive.WriteString(t_stream,cryptPassword.md5("111"),false);
			
			t_receive.SendBufferToSvr(t_stream.toByteArray(), false);
			
			fetchWeibo t_weibo = new fetchWeibo(false);
			t_weibo.SetText("我要发发试试,评论发不了？");
			t_weibo.SetCommectWeiboId(5572863863L);
			
			t_stream.reset();
			t_stream.write(msg_head.msgWeibo);
			
			t_weibo.OutputWeibo(t_stream,17);
			
			t_receive.SendBufferToSvr(t_stream.toByteArray(), true);
			
			Thread.sleep(10000000);
			
		}catch(Exception _e){
			prt(_e.getMessage());
			_e.printStackTrace();
		}
	}
	
	static public void berryRecvTest(){
		try{
			
			Socket t_socket = GetSocketServer("111111","127.0.0.1",9716,false);
			sendReceive t_receive = new sendReceive(t_socket);
			
			ByteArrayOutputStream t_stream = new ByteArrayOutputStream();
			t_stream.write(msg_head.msgConfirm);
			sendReceive.WriteString(t_stream, "111111",false);
			sendReceive.WriteInt(t_stream,5);
			t_stream.write(0);
			sendReceive.WriteString(t_stream,"1.1.715",false);
			sendReceive.WriteString(t_stream,cryptPassword.md5("111"),false);
			
			t_receive.SendBufferToSvr(t_stream.toByteArray(), false);
			
			while(true){

				ByteArrayInputStream in = new ByteArrayInputStream(t_receive.RecvBufferFromSvr());
				switch(in.read()){
					case msg_head.msgMail:
						fetchMail t_mail = new fetchMail(false);
						t_mail.InputMail(in);
						prt("receive idx: " + t_mail.GetMailIndex() + " subject: " + t_mail.GetSubject() + "\n" + t_mail.GetContain());
												
						// TODO display in berry
						//
						
						break;
						
					case msg_head.msgWeibo:
						fetchWeibo t_weibo = new fetchWeibo(false);
						t_weibo.InputWeibo(in);
						
						prt("receive weibo id" + t_weibo.GetId() + " text:" + t_weibo.GetText());
						break;
					case msg_head.msgSendMail:
						
						// TODO display in berry
						// the post mail has been send
						//
						
						break;
				}
			}
			
		}catch(Exception _e){
			prt(_e.getMessage());
			_e.printStackTrace();
		}
	}
	
	static public Socket GetSocketServer(String _userPassword,String _host,int _port,boolean _ssl)throws Exception{
		
		if(_ssl){

			String	key				= "YuchBerryKey";  
			
			char[] keyStorePass		= _userPassword.toCharArray();
			char[] keyPassword		= _userPassword.toCharArray();
			
			KeyStore ks				= KeyStore.getInstance(KeyStore.getDefaultType());
			
			ks.load(new FileInputStream(key),keyStorePass);
			
			KeyManagerFactory kmf	= KeyManagerFactory.getInstance("SunX509");
			kmf.init(ks,keyPassword);
			
			SSLContext sslContext = SSLContext.getInstance("SSLv3");
			sslContext.init(kmf.getKeyManagers(),null,null);
			  
			SSLSocketFactory factory=sslContext.getSocketFactory();
			
			return factory.createSocket(_host,_port);
			
		}else{
			
			return new Socket(InetAddress.getByName(_host),_port); 
		}	  
	}

	static void prt(String s) {
		System.out.println(s);
	}
	
	static void prtA(byte[] a) {
		
		for(int i = 0;i < a.length;i++){
			prt(String.valueOf(a[i]));
		}
	}
}
