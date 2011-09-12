package com.yuchting.yuchberry.yuchsign.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.yuchting.yuchberry.yuchsign.server.weibo.WeiboAuth;
import com.yuchting.yuchberry.yuchsign.shared.FieldVerifier;

public class WeiboAuthServlet extends HttpServlet {

	public void doPost(HttpServletRequest request,HttpServletResponse response)throws ServletException,IOException{
		this.doGet(request,response	);
	}
	
	public void doGet(HttpServletRequest request,HttpServletResponse response)throws ServletException,IOException{
		
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html;charset=utf-8");
		
		String t_accountName = (String)request.getParameter("bber");
		String t_verify = (String)request.getParameter("oauth_verifier");
		
		PrintWriter out = response.getWriter();
		out.println("<html>");		
		
		try{
			
			if(t_verify != null){
				
				if(t_accountName == null || t_verify == null){
					out.println("你为啥会访问这个网址呢？");
					return;
				}
				
				t_accountName = URLDecoder.decode(t_accountName,"UTF-8");
				
				try{
					WeiboAuth t_auth = YuchsignCache.getWeiboAuth(t_accountName);
					if(t_auth == null){
						out.println("请刷新YuchSign界面再次授权。");
						return;
					}
					
					t_auth.genAccessToken(t_verify);
					
					YuchsignCache.makeCacheWeiboAuth(t_auth);
					
					out.println("授权成功，请关闭这个页面，等待YuchSign自动获取已保存在服务器上的授权访问码。<br />");
					out.println("Auth Successfully , please close this and return the YuchSign page to operate.");
					
				}catch(Exception e){
					out.println("授权出现问题："+e.getMessage());
				}
				
			}else{
				
				String t_type = (String)request.getParameter("type");
				
				if(t_type != null){			
					try{
						
						// redirect the URL to auth
						//
						WeiboAuth t_auth = new WeiboAuth(t_accountName,t_type);
						
						String t_res = t_auth.getRequestURL(FieldVerifier.fsm_mainURL+"/auth/?bber=" + URLEncoder.encode(t_accountName,"UTF-8"));
						//String t_res = t_auth.getRequestURL("http://127.0.0.1:8888/auth/?bber=" + URLEncoder.encode(t_accountName,"UTF-8"));
	
						YuchsignCache.makeCacheWeiboAuth(t_auth);					
						
						response.sendRedirect(t_res);					
						
					}catch(Exception e){
						out.println("<Error>请求出现错误 " + e.getMessage() +"</Error>");
						out.flush();
					}
				}
			}
		
		}finally{
			out.println("<br /> <br /> 有任何问题，请及时联系 <a href=\"mailto:yuchberry@gmail.com\">yuchberry@gmail.com</a>  </html>");
			out.println("</html>");
			out.flush();
		}
		
	}
	

}