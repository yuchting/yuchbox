package com.yuchting.yuchberry.server.frame;

import java.awt.event.ActionEvent;
import java.util.Properties;

import javax.swing.JButton;

import com.yuchting.yuchberry.server.fetchQWeibo;
import com.yuchting.yuchberry.server.fetchSinaWeibo;
import com.yuchting.yuchberry.server.fetchTWeibo;
import com.yuchting.yuchberry.server.fetchWeibo;

public class weiboRequestTool{

	JButton		m_openRequestURL	= new JButton("请求授权");
	

	Object		m_requestToken	= null;
	
	int			m_style				= 0;
	
	NanoHTTPD	m_httpd				= null;
	
	private String		m_subfix	= null;
	
	private final static int			fsm_daemonPort		= 4928;
	private final static String	fsm_callbackURL =  "http://localhost:" + fsm_daemonPort;
	
	public weiboRequestTool(int _style){

		m_style = _style;
				
		switch(_style){
		case fetchWeibo.SINA_WEIBO_STYLE:
			m_subfix = "Sina";
			break;
		case fetchWeibo.TWITTER_WEIBO_STYLE:
			m_subfix = "Twitter";
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
				prt("YuchBerry 正在请求"+m_subfix+" Weibo 授权并打开浏览器，获得授权码之前，请不要关闭这个窗口……");
				
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
					mainFrame.OpenURL(((fetchQWeibo)m_requestToken).getVerifyPinURL(fsm_callbackURL));
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
			Thread.sleep(99999999999L);
		}catch(Exception ex){}		
	}
	
	private void prt(String _log){
		System.out.println(_log);
	}
	
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == m_openRequestURL){
			
			try{
				if(m_requestToken == null){
					if(m_style == fetchWeibo.SINA_WEIBO_STYLE){
						m_requestToken = (new fetchSinaWeibo(null)).getRequestToken(fsm_callbackURL);
					}else if(m_style == fetchWeibo.TWITTER_WEIBO_STYLE){
						m_requestToken = (new fetchTWeibo(null)).getRequestToken(fsm_callbackURL);
					}else if(m_style == fetchWeibo.QQ_WEIBO_STYLE){
						m_requestToken = new fetchQWeibo(null);
					}
					
				}
				
				if(m_style == fetchWeibo.SINA_WEIBO_STYLE){
					mainFrame.OpenURL(((weibo4j.http.RequestToken)m_requestToken).getAuthorizationURL());
				}else if(m_style == fetchWeibo.TWITTER_WEIBO_STYLE){
					mainFrame.OpenURL(((twitter4j.auth.RequestToken)m_requestToken).getAuthorizationURL());
				}else if(m_style == fetchWeibo.QQ_WEIBO_STYLE){
					mainFrame.OpenURL(((fetchQWeibo)m_requestToken).getVerifyPinURL(fsm_callbackURL));
				}else{
					assert false;
				}
				
			}catch(Exception ex){
				prt("出现错误:" + ex.getMessage());
				ex.printStackTrace();
			}			
		}
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
					t_qweibo.RequestTokenByVerfiyPIN(verifier);					
					
					t_tokenString = t_qweibo.sm_requestTokenKey;
					t_tokenSecret = t_qweibo.sm_requestTokenSecret;			
					
				}else{
					assert false;
				}
				
				t_response.append("YuchBerry 申请"+m_subfix+" Weibo 授权成功！获得如下授权码，请按照 <a href=\"http://code.google.com/p/yuchberry/wiki/YuchBerry_Weibo#填写config.xml配置文件\" target=_blank>说明文档</a> 将其填写到config.xml里面。<br /><br />");
				t_response.append("accessToken=\""+t_tokenString+"\"<br />");
				t_response.append("secretToken=\""+t_tokenSecret+"\"<br />");

				
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
		new weiboRequestTool(fetchWeibo.QQ_WEIBO_STYLE);
	}
}
