package com.yuchting.yuchberry.yuchsign.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class PayLevServiceImpl extends HttpServlet{
	
	public void doGet(HttpServletRequest request,HttpServletResponse   response)throws ServletException,IOException{
		
		final String t_trade_no = (String)request.getParameter("out_trade_no");
		//final String t_trade_no = (String)request.getParameter("out_trade_no");
		
		
	}
}
