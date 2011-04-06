package com.yuchting.yuchberry.yuchsign.server;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



public class VerifyCodeServlet extends HttpServlet {
	
	public void doPost(HttpServletRequest request,HttpServletResponse response)throws ServletException,IOException{
		this.doGet(request,response	);
	}
	
	public void doGet(HttpServletRequest request,HttpServletResponse response)throws ServletException,IOException{
		final String t_accountName = (String)request.getParameter("acc");
		if(t_accountName == null){
			return;
		}
		
		try{
			GenVerifyCode t_code = (GenVerifyCode)YuchsignCache.queryCache().get(GenVerifyCode.fsm_numberCacheKeyPrefix + t_accountName);
			if(t_code != null){

				OutputStream t_os = response.getOutputStream();
				try{
					t_os.write(t_code.getVerfiyCodeImageData());
				}finally{
					t_os.flush();
					t_os.close();
				}	
			}
			
		}catch(Exception e){
			
		}
		
	}

}
