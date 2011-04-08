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

public class PayOKServiceImpl extends HttpServlet{

	public void doPost(HttpServletRequest request,HttpServletResponse response)throws ServletException,IOException{
		this.doGet(request,response	);
	}
	
	public void doGet(HttpServletRequest request,HttpServletResponse response)throws ServletException,IOException{

		final String t_out_trade_no	= (String)request.getParameter("out_trade_no");
		final String t_is_success = (String)request.getParameter("is_success");
		
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html");
        
		PrintWriter out = response.getWriter();
		out.println("<html>");
		
		try{
			
			if(t_out_trade_no == null || t_is_success == null){
				out.println("你是怎么找到这个地方的呢？");
				return ;
			}
			
			PersistenceManager t_pm = PMF.get().getPersistenceManager();
			try{
								
				Key t_order_key = KeyFactory.createKey(yuchOrder.class.getSimpleName(),t_out_trade_no);
				try{
					
					yuchOrder t_order = t_pm.getObjectById(yuchOrder.class, t_order_key);
					if(t_order == null){
					    out.println( "内部订单错误，请重新操作。");
					    return;
					}
					
					if(t_is_success.indexOf("T") != -1){

						if(!t_order.GetAlipayTradeNO().isEmpty()){
							out.println( "支付成功，已经收到支付宝的到帐信息，需要手动同步一下，或者退出重新登录一下，查看时间、等级信息。");
						}else{
							out.println( "支付成功，但尚未收到支付宝的到帐信息。<br />请稍等片刻，手动同步一下，或者退出重新登录一下，再次查看时间、等级信息。");
						}	
					}else{
						out.println( "支付失败！请确认支付宝的支付流程。");
					}
					
				}catch(javax.jdo.JDOObjectNotFoundException e){
					 out.println("找不到下订单的注册账户");
				}	
							
			}finally{
				t_pm.close();
			}	
		}finally{

			out.println("<br /> <br /> 有任何问题，请及时联系 <a href=\"mailto:yuchberry@gmail.com\">yuchberry@gmail.com</a>  </html>");
			out.flush();
		}
		
	}
}
