package com.yuchting.yuchberry.yuchsign.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class PayServiceImpl extends HttpServlet {
	
	public void doGet(HttpServletRequest request,HttpServletResponse   response)throws ServletException,IOException{
				
		final String t_out_trade_no	= request.getParameter("out_trade_no");
		final String t_subject		= (String)request.getParameter("subject");
		final String t_body			= (String)request.getParameter("body");
		final String t_total_fee	= (String)request.getParameter("total_fee");
		
		final String t_trade_status= (String)request.getParameter("trade_status");
		
		final String t_trade_no		= (String)request.getParameter("trade_no");
		final String t_buyer_email	= (String)request.getParameter("buyer_email");
		
		
	}
}
