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
package com.yuchting.yuchberry.server.frame;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Properties;

import javax.swing.JButton;

import com.mime.qweibo.OauthKey;
import com.yuchting.yuchberry.server.fetchQWeibo;
import com.yuchting.yuchberry.server.fetchSinaWeibo;
import com.yuchting.yuchberry.server.fetchTWeibo;
import com.yuchting.yuchberry.server.fetchWeibo;

interface IWeiboAuthOk{
	public void weiboAuthOK(String _accessToken,String _secretToken);
}

public class weiboRequestTool{

	JButton		m_openRequestURL	= new JButton("请求授权");
	IWeiboAuthOk	m_weiboAuthOK	= null;

	Object		m_requestToken	= null;
	
	int			m_style				= 0;
	
	NanoHTTPD	m_httpd				= null;
	
	private String		m_subfix	= null;
	
	boolean		m_closed = false;
	
	private final static int			fsm_daemonPort		= 4928;
	private final static String	fsm_callbackURL =  "http://localhost:" + fsm_daemonPort;
		
	public weiboRequestTool(int _style,IWeiboAuthOk _callback){
		m_weiboAuthOK = _callback;
		m_style = _style;
	}
	
	public weiboRequestTool(int _style){
		m_style = _style;
	}
	
	// synchronized function 
	//	
	public void startAuth(){		
				
		switch(m_style){
		case fetchWeibo.SINA_WEIBO_STYLE:
			m_subfix = "Sina";
			break;
		case fetchWeibo.TWITTER_WEIBO_STYLE:
			m_subfix = "Tw";
			break;
		case fetchWeibo.QQ_WEIBO_STYLE:
			m_subfix = "qq";
			break;
		}
		
				
		try{
			m_httpd = new NanoHTTPD(fsm_daemonPort){
				public Response serve( String uri, String method, Properties header, Properties parms, Properties files ){				
					return new NanoHTTPD.Response( HTTP_OK, MIME_HTML, processHTTPD(method,header,parms));
				}
			};
			
			try{
				prt("语盒正在请求"+m_subfix+" Weibo 授权并打开浏览器，获得授权码之前，请不要关闭这个窗口……");
				
				if(m_style == fetchWeibo.TWITTER_WEIBO_STYLE){
					prt("对了，还有一件事情，你貌似在请求一个不存在的网站，需要打开神马通道进行连接么？（直接回车表示不需要）");
					pt("神马通道IP: ");
					BufferedReader bufin = new BufferedReader(new   InputStreamReader(System.in)); 
					String IP = bufin.readLine();
					
					pt("神马通道端口: ");
					String port = bufin.readLine();
					
					if(IP.length() != 0 && port.length() != 0){
						System.setProperty("proxySet", "true");
						System.setProperty("proxyHost", IP);
						System.setProperty("proxyPort", port);
					}					
				}
				
				if(m_style == fetchWeibo.SINA_WEIBO_STYLE){
					m_requestToken = (new fetchSinaWeibo(null)).getRequestToken(fsm_callbackURL);
				}else if(m_style == fetchWeibo.TWITTER_WEIBO_STYLE){
					m_requestToken = (new fetchTWeibo(null)).getRequestToken(fsm_callbackURL);
				}else if(m_style == fetchWeibo.QQ_WEIBO_STYLE){
					m_requestToken = new fetchQWeibo(null);
				}			
				
				
				if(m_style == fetchWeibo.SINA_WEIBO_STYLE){
					mainFrame.OpenURL(((weibo4j.http.RequestToken)m_requestToken).getAuthorizationURL());
				}else if(m_style == fetchWeibo.TWITTER_WEIBO_STYLE){
					mainFrame.OpenURL(((twitter4j.auth.RequestToken)m_requestToken).getAuthorizationURL());
				}else if(m_style == fetchWeibo.QQ_WEIBO_STYLE){
					mainFrame.OpenURL(((fetchQWeibo)m_requestToken).m_api.getVerifyPinURL(fsm_callbackURL));
				}else{
					assert false;
				}
				
			}catch(Exception ex){
				prt("出现错误:" + ex.getMessage());
				ex.printStackTrace();
			}

		}catch(Exception e){
			prt("错误，无法打开系统的"+fsm_daemonPort +"端口，请确认是否有其他程序占有这个端口。");
		}		
		
		try{
			
			while(!m_closed){
				Thread.sleep(100);
			}
			
			if(m_httpd != null){
				m_httpd.stop();
			}
			
		}catch(Exception ex){}		
		
	}
	
	public void closeTool(){
		m_closed = true;
		
	}
	
	private void prt(String _log){
		System.out.println(_log);
	}
	private void pt(String _log){
		System.out.print(_log);
	}
	
	private String processHTTPD(String method,Properties header,Properties parms){

		StringBuffer t_response = new StringBuffer();
		
		t_response.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
		t_response.append("<head>");
		t_response.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />");
		t_response.append("<title>应用授权</title>");
		t_response.append("</head>");	
		t_response.append("<html>");
		
		try{
			String token = parms.getProperty("oauth_token");
			String verifier = parms.getProperty("oauth_verifier");
			
			if(token != null && verifier != null){
				
				String t_tokenString = null;
				String t_tokenSecret = null;
				
				if(m_style == fetchWeibo.SINA_WEIBO_STYLE){

					weibo4j.http.AccessToken accessToken = ((weibo4j.http.RequestToken)m_requestToken).getAccessToken(verifier);
					
					t_tokenString = accessToken.getToken();
					t_tokenSecret = accessToken.getTokenSecret();
					
				}else if(m_style == fetchWeibo.TWITTER_WEIBO_STYLE){

					twitter4j.auth.AccessToken accessToken = (new fetchTWeibo(null)).getTwitter()
									.getOAuthAccessToken((twitter4j.auth.RequestToken)m_requestToken,verifier);
					
					t_tokenString = accessToken.getToken();
					t_tokenSecret = accessToken.getTokenSecret();
					
				}else if(m_style == fetchWeibo.QQ_WEIBO_STYLE){
					
					fetchQWeibo t_qweibo = ((fetchQWeibo)m_requestToken);
					OauthKey t_key = t_qweibo.m_api.requestTokenByVerfiyPIN(verifier);					
					
					t_tokenString = t_key.tokenKey;
					t_tokenSecret = t_key.tokenSecrect;			
					
				}else{
					assert false;
				}
				
				t_response.append("语盒申请"+m_subfix+" Weibo 授权成功！获得如下授权码，请按照 <a href=\"http://code.google.com/p/yuchberry/wiki/YuchBerry_Weibo#填写config.xml配置文件\" target=_blank>说明文档</a> 将其填写到config.xml里面。<br /><br />");
				t_response.append("accessToken=\""+t_tokenString+"\"<br />");
				t_response.append("secretToken=\""+t_tokenSecret+"\"<br />");
				
				if(m_weiboAuthOK != null){
					m_weiboAuthOK.weiboAuthOK(t_tokenString, t_tokenSecret);
				}
				
				closeTool();
				
			}else{
				t_response.append("你不要这样访问，木有用滴。");
			}
		}catch(Exception e){
			t_response.append("授权出现问题：" + e.getMessage());
			e.printStackTrace();
		}finally{
			t_response.append("<br />有任何问题请联系 <a href=\"mailto:yuchberry@gmail.com?subject=工具授权出现问题\">yuchberry@gmail.com</a>");
			t_response.append("</html>");
		}		
		
		prt("授权完毕，现在可以关闭这个窗口了，请按照浏览器里面的说明填写 config.xml");
		
		return t_response.toString();
	}	
	
	static public void main(String _arg[]){
		(new weiboRequestTool(fetchWeibo.TWITTER_WEIBO_STYLE)).startAuth();
	}
}
