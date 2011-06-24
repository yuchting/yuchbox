package com.yuchting.yuchberry.yuchsign.server;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.TimeZone;
import java.util.Vector;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.yuchting.yuchberry.yuchsign.client.GreetingService;
import com.yuchting.yuchberry.yuchsign.shared.FieldVerifier;



final class PMF {
	private static final PersistenceManagerFactory pmfInstance =
	        JDOHelper.getPersistenceManagerFactory("transactions-optional");

	private PMF() {}

    public static PersistenceManagerFactory get() {
        return pmfInstance;
    } 
}
/**
 * The server side implementation of the RPC service.
 */
public class GreetingServiceImpl extends RemoteServiceServlet implements GreetingService {
	
	// get yuch host list first from cache 
	//
	yuchHost	m_currSyncHost = null;
	
	boolean 	m_isAdministrator = false;
	
	long		m_createTime = 0;
	long		m_checkLogTime = 0;
	
	boolean	m_foundPassword = false;
	
	String		m_currVerfiyCode = null;
	
	GenVerifyCode	m_currVerifyCode = new GenVerifyCode();
		
	public String logonServer(String name,String password) throws Exception {
		
		PersistenceManager t_pm = PMF.get().getPersistenceManager();
			
		try{
			yuchbber t_bber = null;
			
			Key k = KeyFactory.createKey(yuchbber.class.getSimpleName(), name);
			try{
				t_bber = t_pm.getObjectById(yuchbber.class, k);	
				
				if(!t_bber.GetPassword().equals(password)){
					return "<Error>密码错误！</Error>";
				}
				
				m_isAdministrator = t_bber.GetSigninName().equalsIgnoreCase(FieldVerifier.fsm_admin);
				
			}catch(javax.jdo.JDOObjectNotFoundException e){
				return "<Error>找不到用户!</Error>";
			}		
			
			return t_bber.OuputXMLData();
			
		}finally{
			t_pm.close();
		}		
	}
	
	public String signinAccount(String _name,String _password,String _verifyCode)throws Exception{
		
		if(!GenVerifyCode.compareCode(_name,_verifyCode)){
			return GenVerifyCode.generate(_name,getThreadLocalRequest());
		}
		
		PersistenceManager t_pm = PMF.get().getPersistenceManager();
		
		try{
			yuchbber t_newbber = null;
			
			Key k = KeyFactory.createKey(yuchbber.class.getSimpleName(), _name);
			try{
				yuchbber t_bber = t_pm.getObjectById(yuchbber.class, k);				

				if(t_bber != null){
					return "<Error>用户名已经存在!</Error>";
				}
				
				m_isAdministrator = t_bber.GetSigninName().equalsIgnoreCase(FieldVerifier.fsm_admin);
				
			}catch(javax.jdo.JDOObjectNotFoundException e){
				
				try{
				
					long t_activateRand = -Math.abs((new Random()).nextLong());
					
					StringBuffer t_body = new StringBuffer();
					t_body.append("欢迎注册YuchSign！\n\n您在YuchBerry的网站上的YuchSign邮件中转账户已经注册完成，需要您通过下面的链接完成激活：\n\nhttp://yuchberrysign.yuchberry.info/act/?acc=")
							.append(URLEncoder.encode(_name,"UTF-8")).append("&rand=").append(URLEncoder.encode(Long.toString(t_activateRand),"UTF-8"))
							.append("\n\n如果无法点击这个链接，请复制到网络浏览器的地址栏上进行访问\n\n致\n  敬!\nhttp://code.google.com/p/yuchberry/");
					
					// send the activate email
					//
					sendEmail(_name,"YuchSign 激活邮件",t_body.toString());
					
					// create account
					//
					t_newbber = new yuchbber(_name,_password);
					
					t_newbber.SetSigninTime(t_activateRand);					
					
					t_pm.makePersistent(t_newbber);	
				}catch(Exception ex){
					return "<Error>发送激活账户邮件出错：" + ex.getMessage() + "</Error>";
				}
				
			}
			
			return t_newbber.OuputXMLData();
			
		}finally{
			t_pm.close();
		}				
	}
	
	public String syncAccount(String _xmlData,String verifyCode)throws Exception{
		
		yuchbber t_syncbber = new yuchbber();
		t_syncbber.InputXMLData(_xmlData);
				
		if(!m_currVerifyCode.compareCode(t_syncbber.GetSigninName(),verifyCode)){
			return m_currVerifyCode.generate(t_syncbber.GetSigninName(),getThreadLocalRequest());
		}
		
		PersistenceManager t_pm = PMF.get().getPersistenceManager();
				
		try{			
			
			Key k = KeyFactory.createKey(yuchbber.class.getSimpleName(), t_syncbber.GetSigninName());
			try{
				yuchbber t_bber = t_pm.getObjectById(yuchbber.class, k);
				
				if(t_bber.GetSigninTime() <= 0 && !m_isAdministrator){
					return "<Error>账户尚未激活，请到注册账户的对应邮箱里面\n查询YuchBerry注册邮件（可能在垃圾邮件箱里），激活此账户。</Error>";
				}

				m_createTime			= t_bber.GetCreateTime();
				long t_hours			= t_bber.GetUsingHours();
				int t_lev				= t_bber.GetLevel();
				long t_latesSyncTime	= t_bber.GetLatestSyncTime();
				int t_interval			= t_bber.GetPushInterval();
								
				if(!t_bber.GetPassword().equals(t_syncbber.GetPassword())){
					return "<Error>密码错误！</Error>";
				}
				
				if(t_latesSyncTime != 0){
					// judge the sync time
					//
					if(!m_isAdministrator && Math.abs((new Date()).getTime() - t_latesSyncTime) < 10 * 60 * 1000){
						return "<Error>一个账户同步时间的最小间隔是10分钟，请不要过于频繁。</Error>";
					}
				}
				
				t_bber.NewWeiboList();
								
				if(!t_syncbber.GetEmailList().isEmpty() || !t_syncbber.GetWeiboList().isEmpty()){
					
					// delete original email if not in current sync bber
					//
					for(yuchEmail orig:t_bber.GetEmailList()){
						boolean t_delete = true;
						for(yuchEmail curr:t_syncbber.GetEmailList()){
							
							if(curr.m_host.indexOf(".live.com") != -1){
								//
								// hotmail's pop3 is dangerous
								//
								t_interval = 90;
							}
							
							if(curr.m_emailAddr.equalsIgnoreCase(orig.m_emailAddr)){
								t_delete = false;
								
								break;
							}
						}

						if(t_delete){
							t_pm.deletePersistent(orig);
						}
					}
					
					// delete the yuchWeibo duplication 
					//
					for(yuchWeibo orig:t_bber.GetWeiboList()){
						boolean t_delete = true;
						for(yuchWeibo curr:t_syncbber.GetWeiboList()){
														
							if(curr.m_typeName.equalsIgnoreCase(orig.m_typeName)){
								t_delete = false;
								break;
							}
						}

						if(t_delete){
							t_pm.deletePersistent(orig);
						}
					}
					
					// load the new sync data
					//
					t_bber.InputXMLData(_xmlData);

					// restore backup the time and using hours
					//
					t_bber.SetCreateTime(m_createTime);
					t_bber.SetUsingHours(t_hours);
					t_bber.SetLevel(t_lev);
					t_bber.SetLatestSyncTime(t_latesSyncTime);
					t_bber.SetPusnInterval(t_interval);
					
					long t_currTime = (new Date()).getTime(); 
					//set the sync bber
					//
					if(m_createTime == 0){
						// first sync
						//
						m_createTime = t_currTime; 
						t_syncbber.SetCreateTime(m_createTime);
					}else{
						t_syncbber.SetCreateTime(m_createTime);
						
						//System.err.println("m_createTime + t_hours * 3600000 == "+(m_createTime + t_hours * 3600000) + " current Time:"+t_currTime);
						
						if(m_createTime + t_hours * 3600000 < t_currTime){
							return "<Error>你的推送时间已经到期，请充值时间</Error>";
						}
					}					
					
					t_syncbber.SetUsingHours(t_hours);
					
					// set the curr sync host null
					//	
					m_currSyncHost = null;
					
					// search the proper host to synchronize
					//
					List<yuchHost> t_hostList = (List<yuchHost>)YuchsignCache.getCacheYuchhostList();
					if(t_hostList == null){
						t_hostList = (List<yuchHost>)t_pm.newQuery("select from " + yuchHost.class.getName()).execute();
						YuchsignCache.makeCacheYuchhostList(t_hostList);
					}
					
					Vector<yuchHost> t_exceptList = new Vector<yuchHost>();
					
					if(t_syncbber.GetConnectHost().isEmpty()){		
						m_currSyncHost =  FindProperHost(t_hostList,t_syncbber.GetEmailList(),t_exceptList);	
					}else{						
						for(yuchHost host : t_hostList){
							if(host.GetHostName().equalsIgnoreCase(t_syncbber.GetConnectHost())){
								m_currSyncHost = host;
								break;
							}
						}
						
						if(m_currSyncHost == null){
							m_currSyncHost =  FindProperHost(t_hostList,t_syncbber.GetEmailList(),t_exceptList);	
						}
					}
					
					if(m_currSyncHost == null){
						return "<Error>没有可用的服务器主机！</Error>";
					}
					
					t_exceptList.add(m_currSyncHost);
					
					try{
						
						while(m_currSyncHost != null){
							
							t_syncbber.SetConnetHost(m_currSyncHost.GetHostName());
							
							// query the account
							//
							Properties t_param = new Properties();
							t_param.put("bber",t_syncbber.OuputXMLData());
														
							String t_result =  RequestYuchHostURL(m_currSyncHost, null, t_param);
							
							if(t_result.indexOf("<Max />") != -1){
								// find the other host if the host is full
								//
								m_currSyncHost =  FindProperHost(t_hostList,t_syncbber.GetEmailList(),t_exceptList);
								
								t_exceptList.add(m_currSyncHost);
								
							}else{
								
								if(t_result.indexOf("<Port>") != -1){
									return ProcessSyncSucc(t_result,t_bber);
								}
								
								return t_result;
							}
						}
						
						return "<Error>所有的主机用户已经满员！</Error>";
						
					}catch(Exception e){
						return "<Error>请求主机URL时出错:" + e.getMessage() + "</Error>";
					}
			        
										
					
				}else{
					return "<Error>没有添加推送账户，无法同步！</Error>";
				}
				
			}catch(javax.jdo.JDOObjectNotFoundException e){
				return "<Error>找不到用户!</Error>";
			}
			
		}finally{
			t_pm.close();
		}	
	}
	
	public String syncAccount_check(String _signinName,String _pass)throws Exception{
		
		if(m_currSyncHost == null){
			return "<Error>网络不通畅，请再次同步！</Error>";
		}
		
		PersistenceManager t_pm = PMF.get().getPersistenceManager();
		try{
			Key k = KeyFactory.createKey(yuchbber.class.getSimpleName(),_signinName);
			try{
				yuchbber t_bber = t_pm.getObjectById(yuchbber.class, k);				

				if(!t_bber.GetPassword().equals(_pass)){
					return "<Error>密码错误！</Error>";
				}
								
				// query the account
				//
				Properties t_param = new Properties();
				t_param.put("check",_signinName);
				t_param.put("bber",_signinName);
				
				String t_result = RequestYuchHostURL(m_currSyncHost,null,t_param);
				
				// read the information
				//
//				DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance(); 
//				DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder(); 
//				Document t_doc = docBuilder.parse(new InputSource(
//													new StringReader(t_result)));
//				
//				Element t_elem = t_doc.getDocumentElement();
				
				if(t_result.indexOf("<Port>") != -1){
					return ProcessSyncSucc(t_result,t_bber);
				}				
				
				return t_result;
				
			}catch(javax.jdo.JDOObjectNotFoundException e){
				return "<Error>找不到用户!</Error>";
			}
			
			
		}finally{
			
			t_pm.close();
		}		
	}
	
	private String ProcessSyncSucc(String _result,yuchbber _bber){
				
		int t_portTag_start		= _result.indexOf("<Port>");
		int t_portTag_end		= _result.indexOf("</Port>");
		
		assert  t_portTag_start != -1;
					
		_result = _result.substring(t_portTag_start + 6,t_portTag_end);
		int t_port = Integer.valueOf(_result).intValue();
		
		_bber.SetServerProt(t_port);
		_bber.SetConnetHost(m_currSyncHost.GetHostName());
		
		_bber.SetCreateTime(m_createTime);
		_bber.SetLatestSyncTime((new Date()).getTime());
		
		_result = _bber.OuputXMLData();
		
		m_currSyncHost = null;
		m_checkLogTime = 0;
		
		return _result;		
	}
	
	public String checkAccountLog(String _signinName,String _pass)throws Exception{
		
		if(m_checkLogTime != 0){

			long t_currentTime = (new Date()).getTime();
			if(!m_isAdministrator && Math.abs(t_currentTime - m_checkLogTime) < 2 * 60 * 1000){
				return "<Error>在2分钟内不要重复提交查询</Error>";
			}	
		}
		
		PersistenceManager t_pm = PMF.get().getPersistenceManager();
		
		try{
			Key k = KeyFactory.createKey(yuchbber.class.getSimpleName(),_signinName);
			try{
				yuchbber t_bber = t_pm.getObjectById(yuchbber.class, k);				

				if(!t_bber.GetPassword().equals(_pass)){
					return "<Error>密码错误！</Error>";
				}
				
				if(t_bber.GetConnectHost().isEmpty()){
					return "<Error>没有获得主机，无法查询日志</Error>";
				}
				
				// search the proper host to synchronize
				//
				List<yuchHost> t_hostList = (List<yuchHost>)YuchsignCache.getCacheYuchhostList();
				if(t_hostList == null){
					t_hostList = (List<yuchHost>)t_pm.newQuery("select from " + yuchHost.class.getName()).execute();
					YuchsignCache.makeCacheYuchhostList(t_hostList);
				}
				
				for(yuchHost host : t_hostList){
					if(host.GetHostName().equalsIgnoreCase(t_bber.GetConnectHost())){
						
						Properties t_param = new Properties();
					
						t_param.put("bber",_signinName);
						t_param.put("log",_signinName);
						
						String t_result = RequestYuchHostURL(host,null, t_param);
						
						if(!t_result.startsWith("<Error>")){
							m_checkLogTime = (new Date()).getTime();
						}
						return t_result; 
					}
				}
				
				return "<Error>之前同步过的主机已经被删除，请先同步</Error>";				
				
			}catch(javax.jdo.JDOObjectNotFoundException e){
				return "<Error>找不到用户!</Error>";
			}
			
			
		}finally{
			
			t_pm.close();
		}	
	}
	
	public String findPassword(String _signinName,String _verifyCode)throws Exception{
		
		if(m_foundPassword){
			throw new Exception("你已经提交过找回密码的信息");
		}
		
		if(!GenVerifyCode.compareCode(_signinName,_verifyCode)){
			return GenVerifyCode.generate(_signinName,getThreadLocalRequest());
		}
		
		PersistenceManager t_pm = PMF.get().getPersistenceManager();
		
		try{
						
			Key k = KeyFactory.createKey(yuchbber.class.getSimpleName(),_signinName);
			try{
				yuchbber t_bber = t_pm.getObjectById(yuchbber.class, k);
				if(t_bber == null){
					throw new Exception("内部错误");
				}
				
		        StringBuffer t_body = new StringBuffer();
		        t_body.append("您好！\n\n您收到这封邮件，是因为您在登录 YuchSign 账户时忘记了密码，您使用这个邮箱地址注册时的密码为(去除两边单引号)：\n\n    '")
		        	.append(t_bber.GetPassword()).append("'\n\n   请您务必保管好，以防下次遗失。\n\n致\n  敬！\nhttp://code.google.com/p/yuchberry/");
		        		        	
		        sendEmail(_signinName,"YuchSign 找回密码",t_body.toString());
	            
	            m_foundPassword = true;		    
				
		        return "已经将邮件发送到 " + t_bber.GetSigninName() +" 请及时查收\n如果没有，请在 【垃圾邮件箱】 内查找一下。";
		        
			}catch(javax.jdo.JDOObjectNotFoundException e){
				throw new Exception("找不到用户!");
			}			
			
		}finally{
			
			t_pm.close();
		}	
	}
	
	public String changePassword(String _signinName,String _verifyCode,String _origPass,String _pass)throws Exception{
		
//		if(!GenVerifyCode.compareCode(_signinName,_verifyCode)){
//			return GenVerifyCode.generate(_signinName,getThreadLocalRequest());
//		}
		
		PersistenceManager t_pm = PMF.get().getPersistenceManager();
		
		try{
						
			Key k = KeyFactory.createKey(yuchbber.class.getSimpleName(),_signinName);
			try{
				yuchbber t_bber = t_pm.getObjectById(yuchbber.class, k);				
				if(t_bber == null){
					throw new Exception( "内部错误。");
				}
				
				if(!_origPass.equals(t_bber.GetPassword())){
					throw new Exception("原始密码错误，请仔细检查一下。");
				}
				
				t_bber.SetPassword(_pass);				
				
		        return "密码修改成功！\n注意：新的密码作为客户端用户密码使用的话，需要再次同步一下。";
		        
			}catch(javax.jdo.JDOObjectNotFoundException e){
				throw new Exception("找不到用户!");
			}			
			
		}finally{
			
			t_pm.close();
		}	
	}
	
	private void sendEmail(String _addr,String _subject,String _text)throws Exception{
		
		Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        		        	
        MimeMessage msg = new MimeMessage(session);
        
        msg.setFrom(new InternetAddress("yuchting@yuchberry.info","YuchBerry Sign"));
        msg.addRecipient(Message.RecipientType.TO,new InternetAddress(_addr));
        msg.setSubject(_subject,"UTF-8");
        msg.setText(_text,"UTF-8");
        
        Transport.send(msg);
        
        System.err.println("send message<" + _subject + ">to user:" + _addr);
	}
	
	
	public String payTime(String _signinName,int _payType,int _fee)throws Exception{
		
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
					String t_subject = "yuchberry充值时间";
					
					switch(_payType){
					case 1: t_subject = "yuchberry用户类型升级";break;
					case 2: t_subject = "yuchberry推送间隔升级";break;
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
	
	public String getdownLev(String _signinName)throws Exception{
		
		PersistenceManager t_pm = PMF.get().getPersistenceManager();
		
		try{
						
			Key k = KeyFactory.createKey(yuchbber.class.getSimpleName(),_signinName);
			try{
				yuchbber t_bber = t_pm.getObjectById(yuchbber.class, k);
				if(t_bber == null){
					throw new Exception("内部错误");
				}
				
				if(t_bber.GetLevel() <= 0){
					throw new Exception("无法再降级了，已经是最低等级!");
				}
				
				t_bber.NewWeiboList();				
				
				int t_nextLev = t_bber.GetLevel() - 1;
				
				PayServiceImpl.RecalculateTime(t_pm,t_bber,0,t_nextLev);
				
				t_bber.GetEmailList().removeAllElements();
				t_bber.GetWeiboList().removeAllElements();
				
		        return t_bber.OuputXMLData();
		        
			}catch(javax.jdo.JDOObjectNotFoundException e){
				throw new Exception("找不到用户!");
			}		
			
		}finally{
			
			t_pm.close();
		}	
	}
		
	public static String RequestYuchHostURL(yuchHost _host,Properties header, Properties parms)throws Exception{
		
		StringBuffer t_final = new StringBuffer();
		t_final.append("http://").append(_host.GetConnectHost()).append(":").append(_host.GetHTTPPort());
					
		if(parms != null){
			t_final.append("/?");
			
			Enumeration e = parms.propertyNames();
			while(true){
				String name = (String)e.nextElement();
				String value = (String)parms.getProperty(name);
				if(value != null){
					t_final.append(name).append("=").append(URLEncoder.encode(value,"UTF-8"));
					if(e.hasMoreElements()){
						t_final.append("&");
					}else{
						break;
					}	
				}
				
			}
		}
		
		
		URL url = new URL(t_final.toString());
		URLConnection con = url.openConnection();
		con.setAllowUserInteraction(false);
		
		if(header != null){
			Enumeration e = header.propertyNames();
			while ( e.hasMoreElements()){
				String name = (String)e.nextElement();
				String value = header.getProperty(name);
				if(value != null){
					con.setRequestProperty(name,URLEncoder.encode(value,"UTF-8"));
				}				
			}
			
			if(header.getProperty("pass") == null){
				con.setRequestProperty("pass",URLEncoder.encode(_host.GetHTTPPass(),"UTF-8"));
			}
			
		}else{
			con.setRequestProperty("pass",URLEncoder.encode(_host.GetHTTPPass(),"UTF-8"));
		}
		
		con.connect();
		
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
		try{
			StringBuffer t_stringBuffer = new StringBuffer();
			
			String temp;
			while ((temp = in.readLine()) != null) {
				t_stringBuffer.append(temp+"\n");
			}
			
			return t_stringBuffer.toString();
		}finally{
			in.close();	
		}
	}

	private yuchHost FindProperHost(List<yuchHost> _hostList,
									Vector<yuchEmail> _emailList,
										Vector<yuchHost> _exceptList){
		
		
		if(_hostList == null || _hostList.size() == 0){
			return null;
		}
		
		Vector<yuchHost> t_listHost = new Vector<yuchHost>();
		
		yuchHost t_resultHost = null;
		
		for(yuchHost host : _hostList){
			
			if(host.GetRecommendHost().length() != 0){
				String[] t_string = host.GetRecommendHost().split(" ");
				
				boolean t_add = false;
				
				for(yuchEmail email:_emailList){
					
					for(String addr:t_string){
						if(email.m_emailAddr.indexOf(addr) !=-1){
							t_add = true;
							t_listHost.add(0,host);
						}
					}								
				}
				
				if(!t_add){
					t_listHost.add(host);
				}
			}else{
				t_listHost.add(host);
			}
		}	
		
		if(!t_listHost.isEmpty()){
			
			search_tag:
			for(yuchHost host : t_listHost){
				for(yuchHost except : _exceptList){
					if(host == except){
						continue search_tag;
					}
				}
				
				t_resultHost = host;
				break;
			}
		}

		return t_resultHost;
	}
		
	
	
	public String queryAlipay()throws Exception{
		
		if(!m_isAdministrator){
			return "<Error>you're not administrator</Error>";
		}
		
		String t_result = "";
		
		yuchAlipay t_pay = YuchsignCache.getCacheAlipay();
		
		if(t_pay == null){
			PersistenceManager t_pm = PMF.get().getPersistenceManager();
			
			try{
				Key k = KeyFactory.createKey(yuchAlipay.class.getSimpleName(),yuchAlipay.class.getName());
				try{
					t_pay = t_pm.getObjectById(yuchAlipay.class, k);
					if(t_pay != null){

						YuchsignCache.makeCacheYuchAlipay(t_pay);
						
						t_pay = YuchsignCache.getCacheAlipay();
						
						t_result = t_pay.GetPartnerID() + ":" + t_pay.GetKey();
					}
					
				}catch(Exception ex){}
			}finally{
				t_pm.close();
			}
		}else{
			t_result = t_pay.GetPartnerID() + ":" + t_pay.GetKey();
		}		
		
		return t_result;
	}
	
	public String modifyAlipay(String _partnerID,String _key)throws Exception{
		
		if(!m_isAdministrator){
			return "<Error>you're not administrator</Error>";
		}
		
		PersistenceManager t_pm = PMF.get().getPersistenceManager();
				
		try{
			yuchAlipay t_pay = null;
			
			Key k = KeyFactory.createKey(yuchAlipay.class.getSimpleName(),yuchAlipay.class.getName());
			try{
				
				t_pay = t_pm.getObjectById(yuchAlipay.class, k);
				
			}catch(Exception ex){
				t_pay = new yuchAlipay();
				
			}
		
			t_pay.SetKey(_key);
			t_pay.SetPartnerID(_partnerID);
						
			try{
				t_pm.makePersistent(t_pay);
			}catch(Exception ex){
				return "<Error>"+ex.getMessage()+"</Error>";
			}
			
			YuchsignCache.makeCacheYuchAlipay(t_pay);
			
			return "<OK />";
			
		}finally{
			
			t_pm.close();
		}	
	}
	
	
	
	public String getHostList()throws Exception{
		
		if(!m_isAdministrator){
			return "<Error>you're not administrator</Error>";
		}
		
		List<yuchHost> t_hostList = (List<yuchHost>)YuchsignCache.getCacheYuchhostList();
		if(t_hostList == null){
			PersistenceManager t_pm = PMF.get().getPersistenceManager();
			try{
				t_hostList = (List<yuchHost>)t_pm.newQuery("select from " + yuchHost.class.getName()).execute();
				
				YuchsignCache.makeCacheYuchhostList(t_hostList);
				
				t_hostList = (List<yuchHost>)YuchsignCache.getCacheYuchhostList();
				
			}finally{
				t_pm.close();
			}			
		}
		
		StringBuffer t_xmlData = new StringBuffer();
		t_xmlData.append("<HostList>");
		
		if(t_hostList != null){
			for(yuchHost host :t_hostList){
				host.OutputXMLData(t_xmlData);
			}	
		}
		
		t_xmlData.append("</HostList>");
		
		return t_xmlData.toString();		
	}
	
	public String addHost(String _hostXMLData)throws Exception{
		
		if(!m_isAdministrator){
			return "<Error>you're not administrator</Error>";
		}
		
		PersistenceManager t_pm = PMF.get().getPersistenceManager();
		
		try{
			yuchHost t_newHost = new yuchHost();
			
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance(); 
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder(); 
			Document t_doc = docBuilder.parse(new InputSource(
											new StringReader(_hostXMLData))); 
			
			t_newHost.InputXMLData(t_doc.getDocumentElement());
			
			Key k = KeyFactory.createKey(yuchHost.class.getSimpleName(), t_newHost.GetHostName());
			try{
				yuchHost t_host = t_pm.getObjectById(yuchHost.class, k);				

				if(t_host != null){
					return "<Error>主机 "+t_newHost.GetHostName()+" 已经存在!</Error>";
				}				
				
			}catch(javax.jdo.JDOObjectNotFoundException e){
									
				try{
					t_pm.makePersistent(t_newHost);
				}catch(Exception ex){
					return "<Error>"+ex.getMessage()+"</Error>";
				}				
			}
			
			StringBuffer t_output = new StringBuffer();
			t_newHost.OutputXMLData(t_output);
			
			// invalid cache
			//
			YuchsignCache.invalidCache(yuchHost.class.getName());
			
			return t_output.toString();
			
		}finally{
			t_pm.close();
		}	
	}
	
	public String delHost(String _hostName)throws Exception{
		
		if(!m_isAdministrator){
			return "<Error>you're not administrator</Error>";
		}		
		
		PersistenceManager t_pm = PMF.get().getPersistenceManager();
		
		try{
			Key k = KeyFactory.createKey(yuchHost.class.getSimpleName(),_hostName);
			try{
				yuchHost t_host = t_pm.getObjectById(yuchHost.class, k);
				t_pm.deletePersistent(t_host);
				
			}catch(javax.jdo.JDOObjectNotFoundException e){					
				return "<Error>主机 "+_hostName+" 不存在!</Error>";
			}
			
			// invalid cache
			//
			YuchsignCache.invalidCache(yuchHost.class.getName());	
			
			return "<OK />";
			
		}finally{
			t_pm.close();
		}
	}
	
	public String modifyHost(String _hostName,String _hostXMLData)throws Exception{
		
		if(!m_isAdministrator){
			return "<Error>you're not administrator</Error>";
		}
		
		PersistenceManager t_pm = PMF.get().getPersistenceManager();
		
		try{
			Key k = KeyFactory.createKey(yuchHost.class.getSimpleName(),_hostName);
			try{
				
				yuchHost t_host = t_pm.getObjectById(yuchHost.class, k);
				
				DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance(); 
				DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder(); 
				Document t_doc = docBuilder.parse(new InputSource(
													new StringReader(_hostXMLData))); 
				
				
				t_host.InputXMLData(t_doc.getDocumentElement());
				
				// invalid cache
				//
				YuchsignCache.invalidCache(yuchHost.class.getName());
								
			}catch(javax.jdo.JDOObjectNotFoundException e){					
				return "<Error>主机 " + _hostName + " 不存在!</Error>";
			}
			
			return "<OK />";
			
		}finally{
			t_pm.close();
		}
	}
	
	
	public String beginWeiboAuth(String _bber,String _type){
		
	}
	
	
	

}
