package com.yuchting.yuchberry.yuchsign.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
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
	
	public void doPost(HttpServletRequest request,HttpServletResponse response)throws ServletException,IOException{
		doGet(request,response);
	}
	
	public void doGet(HttpServletRequest request,HttpServletResponse response)throws ServletException,IOException{
				
		final String t_out_trade_no	= (String)request.getParameter("out_trade_no");
		final String t_total_fee	= (String)request.getParameter("total_fee");
		final String t_notify_id	= (String)request.getParameter("notify_id");
		
		if(!VerifyURL(t_notify_id)){
			System.err.println("notify failed notify id:"+ t_notify_id);
			return ;
		}
		
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html");
        
		PrintWriter out = response.getWriter();
		
		try{
			
			if(t_out_trade_no == null || t_total_fee == null){
				System.err.println("notify failed notify id:'"+ t_notify_id +"' out trade no:'"+ t_out_trade_no +"' total fee:'" + t_total_fee + "' ");
				return ;
			}
			
			PersistenceManager t_pm = PMF.get().getPersistenceManager();
			try{
								
				Key t_order_key = KeyFactory.createKey(yuchOrder.class.getSimpleName(),t_out_trade_no);
				try{
					
					yuchOrder t_order = t_pm.getObjectById(yuchOrder.class, t_order_key);
					if(t_order == null){
						System.err.println("notify failed notify id:'"+ t_notify_id +"' out trade no:'"+t_out_trade_no +"' total fee:'" + t_total_fee + "' ");
					    return;
					}
					
					if(t_order.GetState() != 0){
						System.err.println("repeat notify,notify id:'"+ t_notify_id +"' out trade no:'"+t_out_trade_no +"' total fee:'" + t_total_fee + "' ");				
						return;
					}
					
					t_order.SetState(1);					
					
					final int t_total_fee_value = Integer.valueOf(t_total_fee).intValue();
					
					if(t_order.GetTotalFee() != t_total_fee_value){
						System.err.println("interval fee error ,notify id:'"+ t_notify_id +"' out trade no:'"+t_out_trade_no +"' total fee:'" + t_total_fee + "' ");
						return;
					}

					try{				
						Key t_yuchbber_key = KeyFactory.createKey(yuchbber.class.getSimpleName(),t_order.GetBuyerEmail());
						yuchbber t_bber = t_pm.getObjectById(yuchbber.class, t_yuchbber_key);
						if(t_bber == null){
							System.err.println(" bber null error ,notify id:'"+ t_notify_id +"' out trade no:'"+t_out_trade_no +"' total fee:'" + t_total_fee + "' ");
						    return;
						}
		
						long t_payHours = 0;
						int t_nextLev = t_bber.GetLevel();
						
						if(t_order.GetPayType() == 0){
							// pay time
							//
							final int t_weekMoney = yuchbber.fsm_weekMoney[t_bber.GetLevel()];

							t_payHours =  (t_total_fee_value / t_weekMoney) * 7 * 24;
							
							if(t_total_fee_value == t_weekMoney * 4){
								t_payHours += 2 * 24;
							}							
							
						}else if(t_order.GetPayType() == 1){
							// pay level
							//
							int t_fee = t_total_fee_value;
							
							
							while(t_fee > 0){
								
								t_fee -= yuchbber.fsm_levelMoney[t_nextLev + 1];
						
								t_nextLev++;
							}
							
							if(t_nextLev >= yuchbber.fsm_levelMoney.length){
								System.err.println(" reach ,notify id:'"+ t_notify_id +"' out trade no:'"+t_out_trade_no +"' total fee:'" + t_total_fee + "' ");
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
							List<yuchHost> t_hostList = (List<yuchHost>)GreetingServiceImpl.getCacheYuchhostList();
							if(t_hostList == null){
								t_hostList = (List<yuchHost>)t_pm.newQuery("select from " + yuchHost.class.getName()).execute();
								GreetingServiceImpl.makeCacheYuchhostList(t_hostList);
							}
							
							if(t_hostList != null && !t_hostList.isEmpty()){
								
								for(yuchHost host : t_hostList){
									if(host.GetHostName().equals(t_bber.GetConnectHost())){
										try{

											Properties t_param = new Properties();
											t_param.put("bber",t_bber.GetSigninName());
											t_param.put("create",Long.toString(t_bber.GetCreateTime()));
											t_param.put("time",Long.toString(t_bber.GetUsingHours()));
											
											GreetingServiceImpl.RequestYuchHostURL(host, null, t_param);
																						
										}catch(Exception e){
											System.err.println(" reach ,notify id:'"+ t_notify_id +"' out trade no:'"+t_out_trade_no +"' total fee:'" + t_total_fee + "' ex:" + e.getMessage());
										}

										break;
									}
								}	
							}
												
						}
						
						
					}catch(javax.jdo.JDOObjectNotFoundException e){
						System.err.println(" JDOObjectNotFoundException ,notify id:'"+ t_notify_id +"' out trade no:'"+t_out_trade_no +"' total fee:'" + t_total_fee + "' ");
					}				

				}catch(javax.jdo.JDOObjectNotFoundException e){
					System.err.println(" JDOObjectNotFoundException out_trade_id,notify id:'"+ t_notify_id +"' out trade no:'"+t_out_trade_no +"' total fee:'" + t_total_fee + "' ");
				}
							
			}finally{
				t_pm.close();
			}	
		}finally{
			out.println("success");
			out.flush();
		}
		
	}
	
	private boolean VerifyURL(String _notify_id){
		
		boolean t_result = false;
		try{
			String t_partnerID = null;
			
			yuchAlipay t_alipay = GreetingServiceImpl.getCacheAlipay();
			
			if(t_alipay == null){
				PersistenceManager t_pm = PMF.get().getPersistenceManager();
				try{
					
					Key  k = KeyFactory.createKey(yuchAlipay.class.getSimpleName(),yuchAlipay.class.getName());
					try{
						t_alipay = t_pm.getObjectById(yuchAlipay.class, k);
					}catch(Exception e){
						return false;
					}
					
					GreetingServiceImpl.makeCacheYuchAlipay(t_alipay);
					
					t_partnerID = t_alipay.GetPartnerID();
					
				}finally{
					t_pm.close();
				}				
			}			
			
			String veryfy_url = "http://notify.alipay.com/trade/notify_query.do?partner=" + t_partnerID + "&notify_id=" + _notify_id; 
			
			URL url = new URL(veryfy_url);
			URLConnection con = url.openConnection();
			con.setAllowUserInteraction(false);
			con.connect();
			
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));		
			
			veryfy_url = in.readLine();
			
			System.out.println("Verify alipay notify_id=" + _notify_id + " result=" + veryfy_url);
			
			if(veryfy_url.indexOf("true") != -1){
				t_result = true;
			}
			
		}catch(Exception e){
			System.err.println("Verify Alipay Exception:" + e.getMessage());
		}
		
		return t_result;
	}
}
