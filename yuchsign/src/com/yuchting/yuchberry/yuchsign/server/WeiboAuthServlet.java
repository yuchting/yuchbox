package com.yuchting.yuchberry.yuchsign.server;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URLDecoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.yuchting.yuchberry.yuchsign.server.weibo.WeiboAuth;

public class WeiboAuthServlet extends HttpServlet {
	
	public void doPost(HttpServletRequest request,HttpServletResponse response)throws ServletException,IOException{
		this.doGet(request,response	);
	}
	
	public void doGet(HttpServletRequest request,HttpServletResponse response)throws ServletException,IOException{
		String t_accountName = (String)request.getParameter("bber");
		String t_verify = (String)request.getParameter("oauth_verifier");
		
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html");
        
		PrintWriter out = response.getWriter();
		out.println("<html>");
		try{
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
				
				out.println("授权成功，请关闭这个页面，等待YuchSign自动获取已保存在服务器上的授权访问码。");
				
			}catch(Exception e){
				out.println("授权出现问题："+e.getMessage());
			}
		}finally{
			out.println("<br /> <br /> 有任何问题，请及时联系 <a href=\"mailto:yuchberry@gmail.com\">yuchberry@gmail.com</a>  </html>");
			out.println("</html>");
			out.flush();
		}
		
		
	}

}