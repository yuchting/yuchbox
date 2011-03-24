package com.yuchting.yuchberry.yuchsign.server;

import java.io.IOException;
import java.io.PrintWriter;

import javax.jdo.PersistenceManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

public class PayServiceImpl extends HttpServlet {
	
	public void doGet(HttpServletRequest request,HttpServletResponse   response)throws ServletException,IOException{
				
		final String t_out_trade_no	= request.getParameter("out_trade_no");
		final String t_subject		= request.getParameter("subject");
		final String t_total_fee	= request.getParameter("total_fee");
		
		final String t_trade_status= (String)request.getParameter("trade_status");
		
		final String t_trade_no		= (String)request.getParameter("trade_no");
		final String t_buyer_email	= (String)request.getParameter("buyer_email");
		
		PersistenceManager t_pm = PMF.get().getPersistenceManager();
		PrintWriter out = response.getWriter();
		
		if(t_out_trade_no == null){
			out.println("<Error>你是怎么找到这个地方的呢？</Error>");
		}
		
		try{
			Key t_order_key = KeyFactory.createKey(yuchOrder.class.getSimpleName(),t_out_trade_no);
			try{
				
				yuchOrder t_order = t_pm.getObjectById(yuchOrder.class, t_order_key);
				if(t_order != null){
				    out.println( "<Error>内部错误，请重新操作</Error>");
				}				
		        
			}catch(javax.jdo.JDOObjectNotFoundException e){
				 out.println("<Error>找不到订单</Error>");
			}
						
		}finally{
			
			t_pm.close();
		}	
	}
}
