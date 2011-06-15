package com.yuchting.yuchberry.server;


import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyStore;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import weibo4j.http.OAuth;
import weibo4j.http.OAuthToken;
import weibo4j.http.PostParameter;


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
	public static void main(String arg[])throws Exception{

		//berryRecvTest();
		
		getOAuthRequest();
	
	}
	
	static public void getOAuthRequest(){
		
		try{
		
			
			OAuth t_auth = new OAuth(fetchSinaWeibo.fsm_consumer_key,fetchSinaWeibo.fsm_consumer_serect);
			
			
			String t_request_url = "http://api.t.sina.com.cn/oauth/request_token"; 
			URL url = new URL(t_request_url);
			URLConnection con = url.openConnection();
			con.setAllowUserInteraction(false);
			
			con.setRequestProperty("Authorization",t_auth.generateAuthorizationHeader("GET", t_request_url, null, null));
			
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
			SinaRequestToken t_requestToken = new SinaRequestToken(t_arr[0].split("=")[1],t_arr[1].split("=")[1]);
			
			
			System.out.println("Open URL:" + "http://api.t.sina.com.cn/oauth/authorize?oauth_token="+t_requestToken.getToken());
			
			System.out.print("input PIN:");
			BufferedReader bufin = new BufferedReader(new   InputStreamReader(System.in)); 
			String PIN = bufin.readLine();			
						
			
			String t_token_url = "http://api.t.sina.com.cn/oauth/access_token"; 
			url = new URL(t_token_url);
			HttpURLConnection tcon = (HttpURLConnection)url.openConnection();
			
			tcon.setDoInput(true);
			tcon.setRequestMethod("POST");
			tcon.setDoOutput(true);
			
			String authString = t_auth.generateAuthorizationHeader("POST", t_token_url, 
										new PostParameter[]
										{
											new PostParameter("oauth_verifier", PIN),
											new PostParameter("source",fetchSinaWeibo.fsm_consumer_key)
										},
										t_requestToken);
			
			tcon.setRequestProperty("Authorization",authString);
			System.out.println("AuthString: " + authString);
			
			String t_params = "oauth_verifier=" + PIN + "&source=" + fetchSinaWeibo.fsm_consumer_key;
			byte[] bytes = t_params.getBytes("UTF-8");
			
			tcon.setRequestProperty("Content-Type",
            "application/x-www-form-urlencoded");
			
			tcon.setRequestProperty("Content-Length",
                    Integer.toString(bytes.length));
			
			System.out.println("param: " + t_params);
			
			OutputStream t_os = tcon.getOutputStream();		
					
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
	
		}catch(Exception e){
			prt(e.getMessage());
			e.printStackTrace();
		}
				
	}
	
	
	static public void berrySendTest(){
		
		try{
			
			Socket t_socket = GetSocketServer("111111","localhost",9716,false);
			sendReceive t_receive = new sendReceive(t_socket.getOutputStream(),t_socket.getInputStream());
			
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			os.write(msg_head.msgConfirm);
			sendReceive.WriteString(os, "111111",false);
			sendReceive.WriteInt(os,4);
			os.write(0);
			sendReceive.WriteString(os,"1.1.715",false);
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
			sendReceive t_receive = new sendReceive(t_socket.getOutputStream(),t_socket.getInputStream());
			
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
			
			t_weibo.OutputWeibo(t_stream);
			
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
			sendReceive t_receive = new sendReceive(t_socket.getOutputStream(),t_socket.getInputStream());
			
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
