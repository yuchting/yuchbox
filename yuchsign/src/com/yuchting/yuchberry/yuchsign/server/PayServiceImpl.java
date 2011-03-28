package com.yuchting.yuchberry.yuchsign.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import javax.jdo.PersistenceManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

public class PayServiceImpl extends HttpServlet {
	
	public void doGet(HttpServletRequest request,HttpServletResponse   response)throws ServletException,IOException{
				
		final String t_out_trade_no	= (String)request.getParameter("out_trade_no");
		final String t_total_fee	= (String)request.getParameter("total_fee");
		
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html");
        
		PrintWriter out = response.getWriter();
		out.println("<html>");
		
		try{
			
			if(t_out_trade_no == null){
				out.println("<body>你是怎么找到这个地方的呢？</body>");
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
					
					if(t_order.GetState() != 0){
						out.println( "订单已经处理过了，请重新操作。");
						return;
					}
					
					t_order.SetState(1);					
					
					final int t_total_fee_value = Integer.valueOf(t_total_fee).intValue();
					
					if(t_order.GetTotalFee() != t_total_fee_value){
						out.println( "内部对账金额错误，请重新操作。");
						return;
					}

					try{				
						Key t_yuchbber_key = KeyFactory.createKey(yuchbber.class.getSimpleName(),t_order.GetBuyerEmail());
						yuchbber t_bber = t_pm.getObjectById(yuchbber.class, t_yuchbber_key);
						if(t_bber == null){
						    out.println( "内部错误，请重新操作。");
						    return;
						}
		
						long t_payHours = 0;
						int t_nextLev = t_bber.GetLevel();
						
						if(t_order.GetPayType() == 0){
							// pay time
							//
							final int t_weekMoney = yuchbber.fsm_weekMoney[t_bber.GetLevel()];
							
							t_payHours =  (t_total_fee_value / t_weekMoney) * 7 * 24;						
							
						}else if(t_order.GetPayType() == 1){
							// pay level
							//
							t_nextLev = t_nextLev + 1;
							
							if(t_nextLev >= yuchbber.fsm_levelMoney.length){
								out.println("你的用户等级已经到了最高等级，这个订单出现了内部问题");
								return ;
							}						
						}
						
						// re-calculate the time
						//
						long t_createTime = 0;
						long t_remainHours = 0;
						
						if(t_bber.GetCreateTime() == 0){
							t_remainHours = t_bber.GetUsingHours();
						}else{
							t_createTime = (new Date()).getTime();
							
							long t_expireTime = t_bber.GetCreateTime() + t_bber.GetUsingHours() * 3600000;
							if(t_createTime < t_expireTime){
								t_remainHours = (t_expireTime - t_createTime) / 3600000;
							}
						}
						
						if(t_remainHours != 0){
							// change the remain hours to current hours
							//
							t_remainHours = yuchbber.fsm_weekMoney[t_bber.GetLevel()] * t_remainHours / yuchbber.fsm_weekMoney[t_nextLev]; 
						}
						
						t_bber.SetCreateTime(t_createTime);
						t_bber.SetUsingHours(t_payHours + t_remainHours);
						t_bber.SetLevel(t_nextLev);					
											
						if(!t_bber.GetConnectHost().isEmpty()){
							// haven't sync successfully 
							//
							
							// sync to yuchberry server
							// search the proper host to synchronize
							//
							String query = "select from " + yuchHost.class.getName();
							List<yuchHost> t_hostList = (List<yuchHost>)t_pm.newQuery(query).execute();
							
							if(t_hostList == null || t_hostList.isEmpty()){
								out.println("充值成功，但是找不到之前同步的主机，需要手动同步一下。");
							}else{

								for(yuchHost host : t_hostList){
									if(host.GetHostName().equals(t_bber.GetConnectHost())){
										try{

											Properties t_header = new Properties();
											t_header.put("bber",t_bber.GetSigninName());
											t_header.put("create",t_bber.GetCreateTime());
											t_header.put("time",t_bber.GetUsingHours());
											
											String t_request = GreetingServiceImpl.RequestYuchHostURL(host, t_header, null);
											
											if(t_request.equals("<OK />")){
												out.println("充值成功，需要重新登录查看。");
											}else{
												out.println("充值成功，需要手动同步一下账户才能继续连接。<br />出现问题：" + t_request);
											}
											
										}catch(Exception e){
											out.println("充值成功，需要手动同步一下账户才能继续连接。");
										}

										break;
									}
								}	
							}
												
						}else{
							out.println("<body>充值成功，需要手动同步一下账户。</body>");
						}
						
						
					}catch(javax.jdo.JDOObjectNotFoundException e){
						 out.println("找不到下订单的注册账户");
					}				

				}catch(javax.jdo.JDOObjectNotFoundException e){
					 out.println("找不到订单");
				}
							
			}finally{
				t_pm.close();
			}	
		}finally{

			out.println("<br /> <br /> 有任何问题，请联系 yuchberry@gmail.com </html>");
			out.flush();
		}
		
	}
}
