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
import com.yuchting.yuchberry.yuchsign.shared.FieldVerifier;

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
		out.println("<html><body>");
		
		String t_insertCode = "";
		
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
							
							out.println("注册用户：" + t_account + " 激活成功，现在可以使用同步功能进行同步了 <br /><br />");
							
							if(!t_bber.getInviter().isEmpty()){
								
								System.err.println(t_bber.GetSigninName() + " has been invited by "+ t_bber.getInviter());
								
								k = KeyFactory.createKey(yuchbber.class.getSimpleName(), t_bber.getInviter());
								try{
									yuchbber t_inviterbber = t_pm.getObjectById(yuchbber.class, k);
									
									t_bber.SetUsingHours(t_bber.GetUsingHours() + FieldVerifier.fsm_inviteDays * 24);
									
									if(t_inviterbber != null){
										
										int t_inviterNum = t_inviterbber.getInviteNum() + 1;
										
										t_inviterbber.setInviteNum(t_inviterNum);
										
										out.println("你是受" + t_bber.getInviter() + "之邀，<b>第"+t_inviterNum+"个</b>注册激活语盒账户的，" +
													"所以你和他两人都会获得<b>"+ FieldVerifier.fsm_inviteDays + "天的免费使用时间</b>。" +
													"<br />可能需要退出一下账户重新登录一下才能看见。" +
													"<br /><br />语盒开发者们感谢你们的支持！遇到任何问题可以回复本邮件，我们会在第一时间为你解答。");
										
										PayServiceImpl.RecalculateTime(t_pm,t_inviterbber,FieldVerifier.fsm_inviteDays * 24,t_inviterbber.GetLevel());										
									}									
								}catch(Exception e){
									out.println("无法找到邀请者！错误：" + e.getMessage());
								}
							}
							
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
			
			out.println("<br /> <br /> 有任何问题，请及时联系 <a href=\"mailto:yuchberry@gmail.com\">yuchberry@gmail.com</a> " + t_insertCode + " </body></html>");
			out.flush();
		}
		
		
	}

}
