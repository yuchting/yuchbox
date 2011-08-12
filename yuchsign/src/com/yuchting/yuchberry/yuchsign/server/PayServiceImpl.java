package com.yuchting.yuchberry.yuchsign.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.TimeZone;

import javax.jdo.PersistenceManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.yuchting.yuchberry.yuchsign.shared.FieldVerifier;

public class PayServiceImpl extends HttpServlet {
	
	public void doPost(HttpServletRequest request,HttpServletResponse response)throws ServletException,IOException{
		this.doGet(request,response);
	}
	
	public void doGet(HttpServletRequest request,HttpServletResponse response)throws ServletException,IOException{
				
		String t_client_pay			= (String)request.getParameter("yname");
		
		if(t_client_pay != null){
			// client pay redirect URL
			//
			String t_type = (String)request.getParameter("type");
			String t_fee = (String)request.getParameter("fee");
			String t_lev = (String)request.getParameter("lev");
						
			try{
				int t_typeVal = Integer.valueOf(t_type).intValue();
				int t_feeVal = Integer.valueOf(t_fee).intValue();
				int t_levVal = Integer.valueOf(t_lev).intValue();

				PersistenceManager t_pm = PMF.get().getPersistenceManager();
				try{
					yuchbber t_bber = null;
					
					Key k = KeyFactory.createKey(yuchbber.class.getSimpleName(), t_client_pay);
					try{
						t_bber = t_pm.getObjectById(yuchbber.class, k);
					}catch(javax.jdo.JDOObjectNotFoundException e){
						throw new Exception("找不到用户，请重新刷新页面登录后提交。");
					}
					
					if(t_bber == null){
						throw new Exception("找不到用户，请重新刷新页面登录后提交。");	
					}
					
					if(t_bber.GetLevel() != t_levVal){
						throw new Exception("用户等级与数据库不符，请退出后重新登录，再次充值。");
					}					
										
				}finally{
					t_pm.close();
				}
				
				String t_redirectURL = payTime(t_client_pay,t_typeVal,t_feeVal);
				
				if(t_redirectURL.startsWith("http")){
					response.sendRedirect(t_redirectURL);
				}else{
					throw new Exception(t_redirectURL);
				}
				
			}catch(Exception e){
				
				response.setCharacterEncoding("utf-8");
				response.setContentType("text/html");
				
				PrintWriter out = response.getWriter();
				out.println("<html><body>遇到错误： "+e.getMessage()+"</body></html>" );
				out.flush();
			}
			
		}else{
			alipayRequest(request,response);
		}
		
	}
	
	private void alipayRequest(HttpServletRequest request,HttpServletResponse response)throws ServletException,IOException{
		String t_out_trade_no		= (String)request.getParameter("out_trade_no");
		String t_alipay_trade_no		= (String)request.getParameter("trade_no");
		String t_total_fee		= (String)request.getParameter("total_fee");
		
		String t_notify_id	= (String)request.getParameter("notify_id");
				
		if(t_notify_id == null || t_out_trade_no == null){
			System.err.println("notify_id=null or out_trade_no=null calling");
			return;
		}
		
		if(t_alipay_trade_no == null){
			t_alipay_trade_no = t_out_trade_no;
			System.err.println("t_alipay_trade_no=null ,set out_trade_no");
		}
		
		try{
			
			if(!VerifyURL(t_notify_id)){
				System.err.println("notify verify failed notify id:"+ t_notify_id);
				return ;
			}
			
			t_notify_id = URLDecoder.decode(t_notify_id,"UTF-8");
			
		}catch(Exception e){
			System.err.println("notify verify failed notify id:"+ t_notify_id + " Exception:"+ e.getMessage());
			return ;
		}
		        
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
					
					if(!t_order.GetAlipayTradeNO().isEmpty()){
						System.err.println("repeat notify,notify id:'"+ t_notify_id +"' out trade no:'"+t_out_trade_no +"' total fee:'" + t_total_fee + "' ");				
						return;
					}
					
					t_order.SetAlipayTradeNO(t_alipay_trade_no);
					
					final int t_total_fee_value = Float.valueOf(t_total_fee).intValue();
					
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
							}else if(t_total_fee_value == t_weekMoney * 10){
								t_payHours += 14 * 24;
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
								System.err.println(" reach max level ,notify id:'"+ t_notify_id +"' out trade no:'"+t_out_trade_no +"' total fee:'" + t_total_fee + "' ");
								return ;
							}						
						}
						
						t_bber.SetTotalPayFee(t_bber.GetTotalPayFee() + t_total_fee_value);
						
						RecalculateTime(t_pm,t_bber,t_payHours,t_nextLev);
						
						System.err.println("Pay OK ,notify id:'"+ t_notify_id +"' out trade no:'"+t_out_trade_no +"' total fee:'" + t_total_fee + "'");
						
					}catch(javax.jdo.JDOObjectNotFoundException e){
						System.err.println(" JDOObjectNotFoundException ,notify id:'"+ t_notify_id +"' out trade no:'"+t_out_trade_no +"' total fee:'" + t_total_fee + "' ");
					}catch(Exception ex){
						System.err.println("Exception:" + ex.getMessage() + " ,notify id:'"+ t_notify_id +"' out trade no:'"+t_out_trade_no +"' total fee:'" + t_total_fee + "' ");
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
		
	private String payTime(String _signinName,int _payType,int _fee)throws Exception{
		
		PersistenceManager t_pm = PMF.get().getPersistenceManager();
		
		try{
			Key k = KeyFactory.createKey(yuchbber.class.getSimpleName(),_signinName);
			try{
				
				yuchbber t_bber = t_pm.getObjectById(yuchbber.class, k);				
				
				StringBuffer t_payURL = new StringBuffer();
				
				yuchAlipay t_alipay = YuchsignCache.getCacheAlipay();
				
				if(t_alipay == null){
					k = KeyFactory.createKey(yuchAlipay.class.getSimpleName(),yuchAlipay.class.getName());
					t_alipay = t_pm.getObjectById(yuchAlipay.class, k);
					
					YuchsignCache.makeCacheYuchAlipay(t_alipay);
				}
												
				if(t_alipay != null){
										
					SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
					format.setTimeZone(TimeZone.getTimeZone("GMT+8"));
					String t_out_trade_no = "" + format.format(new Date()) + (new Random()).nextInt(1000);
					
					// check the out trade no
					//
					Key t_order_key = KeyFactory.createKey(yuchOrder.class.getSimpleName(),t_out_trade_no);
					try{
						yuchOrder t_order = t_pm.getObjectById(yuchOrder.class, t_order_key);
						if(t_order != null){
							return  "内部错误，请重新操作";
						}						
					}catch(Exception ex){}
					
					
					// generate the URL 
					//
					String t_subject = "yuchberryPayTime";
					
					switch(_payType){
					case 1: t_subject = "yuchberryPayLevel";break;
					case 2: t_subject = "yuchberryPayInterval";break;
					}
					
					StringBuffer t_body = new StringBuffer();

					t_body.append("_input_charset=utf-8&")
							.append("notify_url=http://yuchberrysign.yuchberry.info/pay/&")
							.append("out_trade_no=" + t_out_trade_no + "&")
							.append("partner=" + t_alipay.GetPartnerID() +"&")
							.append("payment_type=1&")
							.append("paymethod=directPay&")
							.append("return_url=http://yuchberrysign.yuchberry.info/payok/&")
							.append("seller_email="+ FieldVerifier.fsm_admin + "&")
							.append("service=create_direct_pay_by_user&")
							.append("subject="+ t_subject +"&")
							.append("total_fee=" + _fee);
					
					String t_md5 = Md5Encrypt.md5(t_body.toString() + t_alipay.GetKey());
					
					t_payURL.append("https://www.alipay.com/cooperate/gateway.do?sign=")
							.append(t_md5).append("&").append(t_body).append("&sign_type=MD5");
					
					yuchOrder t_order = new yuchOrder();
					
					t_order.SetOutTradeNO(t_out_trade_no);
					t_order.SetTotalFee(_fee);
					t_order.SetSubject(t_subject);
					t_order.SetBuyerEmail(_signinName);
					t_order.SetPayType(_payType);
					
					t_pm.makePersistent(t_order);
					
					return t_payURL.toString();
					
				}else{
					return "暂时无法付费";
				}
		        
			}catch(javax.jdo.JDOObjectNotFoundException e){
				return "找不到用户!";
			}
						
		}finally{
			
			t_pm.close();
		}	
	}
	
	static public void RecalculateTime(PersistenceManager _pm,yuchbber _bber ,long _payHours,int _nextLev)throws Exception{
		
		// re-calculate the time
		//
		long t_createTime = 0;
		long t_remainHours = 0;
		
		if(_bber.GetCreateTime() == 0){
			t_remainHours = _bber.GetUsingHours();
		}else{
			t_createTime = (new Date()).getTime();
			
			long t_expireTime = _bber.GetCreateTime() + _bber.GetUsingHours() * 3600000;
			if(t_createTime < t_expireTime){
				t_remainHours = (t_expireTime - t_createTime) / 3600000;
				
				// give a hours ...
				//
				t_remainHours += 1;
			}
		}
		
		if(t_remainHours != 0 && _nextLev != _bber.GetLevel()){
			// change the remain hours to current hours
			//
			t_remainHours = yuchbber.fsm_weekMoney[_bber.GetLevel()] * t_remainHours / yuchbber.fsm_weekMoney[_nextLev];
		}
		
		_bber.SetCreateTime(t_createTime);
		_bber.SetUsingHours(_payHours + t_remainHours);
		_bber.SetLevel(_nextLev);					
		
		// reset the latest sync time to let the bber sync
		//
		_bber.SetLatestSyncTime(0);
		
		if(!_bber.GetConnectHost().isEmpty()){
			// haven't sync successfully
			//
			
			// sync to yuchberry server
			// search the proper host to synchronize
			//
			List<yuchHost> t_hostList = (List<yuchHost>)YuchsignCache.getCacheYuchhostList();
			if(t_hostList == null){
				
				assert _pm != null;
							
				t_hostList = (List<yuchHost>)_pm.newQuery("select from " + yuchHost.class.getName()).execute();
				YuchsignCache.makeCacheYuchhostList(t_hostList);
				
			}
			
			if(t_hostList != null && !t_hostList.isEmpty()){
				
				for(yuchHost host : t_hostList){
					if(host.GetHostName().equalsIgnoreCase(_bber.GetConnectHost())){
					
						try{
							Properties t_param = new Properties();
							t_param.put("bber",_bber.GetSigninName());
							t_param.put("create",Long.toString(_bber.GetCreateTime()));
							t_param.put("time",Long.toString(_bber.GetUsingHours()));
							
							GreetingServiceImpl.RequestYuchHostURL(host, null, t_param);
						}catch(Exception e){
							System.err.println("RequestYuchHostURL error ："+ e.getMessage()); 
						}				
																		
						break;
					}
				}	
			}
								
		}
	}
	
	private boolean VerifyURL(String _notify_id){
		
		boolean t_result = true;
		
		try{
			String t_partnerID = null;
			
			yuchAlipay t_alipay = YuchsignCache.getCacheAlipay();
			
			if(t_alipay == null){
				PersistenceManager t_pm = PMF.get().getPersistenceManager();
				try{
					
					Key  k = KeyFactory.createKey(yuchAlipay.class.getSimpleName(),yuchAlipay.class.getName());
					try{
						t_alipay = t_pm.getObjectById(yuchAlipay.class, k);
					}catch(Exception e){
						return false;
					}
					
					YuchsignCache.makeCacheYuchAlipay(t_alipay);
					
					t_partnerID = t_alipay.GetPartnerID();
					
				}finally{
					t_pm.close();
				}				
			}else{
				t_partnerID =  t_alipay.GetPartnerID();
			}
			
			String veryfy_url = "https://www.alipay.com/cooperate/gateway.do?service=notify_verify&partner=" + t_partnerID + "&notify_id=" + _notify_id; 
			
			int t_count = 0;
			while(t_count++ < 2){
				
				try{
					
					URL url = new URL(veryfy_url);
					URLConnection con = url.openConnection();
					con.setAllowUserInteraction(false);
					con.connect();
					
					BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));		
					
					veryfy_url = in.readLine();
								
					if(veryfy_url.indexOf("true") != -1){
						t_result = true;
					}else{
						t_result = false;
					}
					
					break;
					
				}catch(Exception e){
					System.err.println("!Verify Alipay Exception:" + e.getMessage());
				}			
			}			
			
		}catch(Exception e){
			System.err.println("Verify Alipay Exception:" + e.getMessage());
		}
		
		return t_result;
	}
}
