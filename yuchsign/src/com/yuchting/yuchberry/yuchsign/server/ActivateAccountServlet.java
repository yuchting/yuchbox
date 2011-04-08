package com.yuchting.yuchberry.yuchsign.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.Date;

import javax.jdo.PersistenceManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

public class ActivateAccountServlet extends HttpServlet {
	
	public void doPost(HttpServletRequest request,HttpServletResponse response)throws ServletException,IOException{
		this.doGet(request,response);
	}
	
	public void doGet(HttpServletRequest request,HttpServletResponse response)throws ServletException,IOException{
		String t_account		= (String)request.getParameter("acc");
		String t_rand			= (String)request.getParameter("rand");
		
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html");
        
		PrintWriter out = response.getWriter();
		out.println("<html>");
		try{

			if(t_account == null || t_rand == null){
				out.println("参数错误，无法激活账户");
				return;
			}
			
			try{
				t_account	= URLDecoder.decode(t_account,"UTF-8");
				t_rand		= URLDecoder.decode(t_rand,"UTF-8");
				
				PersistenceManager t_pm = PMF.get().getPersistenceManager();
				try{
					Key k = KeyFactory.createKey(yuchbber.class.getSimpleName(), t_account);
					try{
						yuchbber t_bber = t_pm.getObjectById(yuchbber.class, k);				

						if(t_bber == null){
							out.println("无法找到注册用户：" + t_account);
							return;
						}
						
						if(t_bber.GetSigninTime() > 0){
							out.println("已经激活的注册用户：" + t_account);
							return;
						}
						
						if(t_bber.GetSigninTime() == Long.valueOf(t_rand).longValue()){
							t_bber.SetSigninTime((new Date()).getTime());
							
							out.println("注册用户：" + t_account + " 激活成功，现在可以使用同步功能进行同步了。");
						}else{
							out.println("激活参数错误");
						}
												
					}catch(javax.jdo.JDOObjectNotFoundException e){
						out.println("无法找到注册用户：" + t_account);
					}
					
				}finally{
					t_pm.close();
				}
				
			}catch(Exception e){
				out.println("激活时，出现内部错误，请截图\n\n" + e.getMessage());
			}
			
		}finally{
			out.println("<br /> <br /> 有任何问题，请及时联系 <a href=\"mailto:yuchberry@gmail.com\">yuchberry@gmail.com</a>  </html>");
			out.flush();
		}
		
		
	}

}
