package com.yuchting.yuchberry.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Vector;

import org.dom4j.Element;

import weibo4j.Paging;
import weibo4j.Status;
import weibo4j.User;
import weibo4j.Weibo;

public class fetchSinaWeibo extends fetchAccount{
	
	static
	{
		Weibo.CONSUMER_KEY = "1290385296";
    	Weibo.CONSUMER_SECRET = "508aa0bfd4b1d039bdf48374f5703d2b";
    	
    	System.setProperty("weibo4j.oauth.consumerKey", Weibo.CONSUMER_KEY);
    	System.setProperty("weibo4j.oauth.consumerSecret", Weibo.CONSUMER_SECRET);
	};
	
	final static int			fsm_checkNum = 200;
	
	Weibo	m_weibo				= new Weibo();
	
	String	m_prefix			= null;
	
	String	m_accountName 		= null;
	
	String	m_accessToken		= null;
	String	m_secretToken		= null;
	
	long	m_fetchWeiboIndex	= -1;
	
	Vector<fetchWeibo>	m_weiboTimeline	= new Vector();
	Vector<fetchWeibo>	m_weiboTimeLineConfirm	= new Vector();
		
	public fetchSinaWeibo(fetchMgr _mainMgr){
		super(_mainMgr);
	}
	
	/**
	 * initialize the account to sign in 
	 * 
	 * @param _elem		: the xml element for read the attribute 
	 */
	public void InitAccount(Element _elem)throws Exception{
		m_accountName		= _elem.attributeValue("account") + "[SinaWeibo]";
		m_accessToken		= _elem.attributeValue("accessToken");
		m_secretToken		= _elem.attributeValue("secretToken");
		
		m_prefix			= m_accountName + "/";
		
	}
	
	/**
	 * get the account name (Email address)
	 */
	public String GetAccountName(){
		return m_accountName;
	}
	
	/**
	 * check the folder to find the news to push
	 */
	public synchronized void CheckFolder()throws Exception{
		
		List<Status> t_fetch = null;
		if(m_fetchWeiboIndex > 1){
			t_fetch = m_weibo.getHomeTimeline(new Paging(m_fetchWeiboIndex));
		}else{
			t_fetch = m_weibo.getHomeTimeline();
		}
		 
		
		boolean t_insert;
		for(Status fetchOne : t_fetch){
			t_insert = true;
			for(fetchWeibo weibo : m_weiboTimeline){
				if(weibo.GetId() == fetchOne.getId()){
					t_insert = false;
				}
			}
			
			if(t_insert){
				for(fetchWeibo weibo : m_weiboTimeLineConfirm){
					if(weibo.GetId() == fetchOne.getId()){
						t_insert = false;
					}
				}
			}
			
			if(t_insert){
				fetchWeibo t_weibo = new fetchWeibo(m_mainMgr.m_convertToSimpleChar);
				ImportWeibo(t_weibo, fetchOne);
				
				m_weiboTimeline.addElement(t_weibo);
			}
		}
		
		
		if(!t_fetch.isEmpty()){
			Status t_lashOne = t_fetch.get(0);
			m_fetchWeiboIndex = t_lashOne.getId() + 1;
		}		
	}
	
	/**
	 * get the directory prefix of this account 
	 */
	public String GetAccountPrefix(){
		return m_prefix;
	}
	
	/**
	 * reset the session for connection
	 * 
	 * @param _fullTest		: whether test the full configure( SMTP for email)
	 */
	public void ResetSession(boolean _fullTest)throws Exception{
		m_weibo.setToken(m_accessToken, m_secretToken);
		m_mainMgr.m_logger.LogOut("Weibo Account<" + GetAccountName() + "> Prepare OK!");
	}
	
	/**
	 * destroy the session connection
	 */
	public void DestroySession(){
		
	}
	
	/**
	 * network package process function
	 * 
	 * @return boolean		: has been processed?
	 */
	public boolean ProcessNetworkPackage(byte[] _package)throws Exception{
		ByteArrayInputStream in = new ByteArrayInputStream(_package);
		
		boolean t_processed = false;
		
		final int t_head = in.read();
		switch(t_head){
			case msg_head.msgWeibo:
				t_processed = ProcessWeiboUpdate(in);
				break;
			case msg_head.msgWeiboConfirm:
				t_processed = ProcessWeiboConfirmed(in);
				break;
		}
		
		return t_processed;
	}
	
	/**
	 * prepare the re-push unconfirm msg
	 */
	public synchronized void PrepareRepushUnconfirmMsg(){
		
		boolean t_repush = true;
		
		for(fetchWeibo confirmOne : m_weiboTimeLineConfirm){
			t_repush = true;
			for(fetchWeibo existOne : m_weiboTimeline){
				
				if(confirmOne.GetId() == existOne.GetId()){
					t_repush = false;
				}
			}
			
			if(t_repush){
				final int t_maxTimes = 5;
				
				if(confirmOne.m_sendConfirmCount++ < t_maxTimes){
					m_weiboTimeline.add(confirmOne);
					m_mainMgr.m_logger.LogOut("Weibo Account<" + GetAccountName() + 
							"> prepare Weibo<" + confirmOne.GetId() + "> send again...");
				}else{
					m_mainMgr.m_logger.LogOut("Weibo Account<" + GetAccountName() + 
							"> prepare Weibo<" + confirmOne.GetId() + "> sent " + t_maxTimes + " Times , give up.");
				}	
				
			}
		}
		
		m_weiboTimeLineConfirm.removeAllElements();
	}
	
	/**
	 * push the message to client
	 */
	public synchronized void PushMsg(sendReceive _sendReceive)throws Exception{
		
		while(!m_weiboTimeline.isEmpty()){
			
			fetchWeibo t_weibo = (fetchWeibo)m_weiboTimeline.get(0); 
			
			ByteArrayOutputStream t_output = new ByteArrayOutputStream();
			
			t_output.write(msg_head.msgWeibo);
			t_weibo.OutputWeibo(t_output);
			
			m_mainMgr.SendData(t_output,false);
						
			m_weiboTimeline.remove(0);
			m_weiboTimeLineConfirm.addElement(t_weibo);		
						
			m_mainMgr.m_logger.LogOut("Weibo Account<" + GetAccountName() + " send Weibo<" + t_weibo.GetId() + " : " + t_weibo.GetText() + ">,wait confirm...");
		}
	}
	
	
	public boolean ProcessWeiboUpdate(ByteArrayInputStream in)throws Exception{
		
		fetchWeibo t_weibo = new fetchWeibo(m_mainMgr.m_convertToSimpleChar);
		t_weibo.InputWeibo(in);

		try{
			
			if(t_weibo.GetCommentWeiboId() == -1 && t_weibo.GetReplyWeiboId() == -1){
				m_weibo.updateStatus(t_weibo.GetText());
			}else if(t_weibo.GetCommentWeiboId() != -1){
				m_weibo.updateComment(t_weibo.GetText(),Long.toString(t_weibo.GetCommentWeiboId()),null);
			}else if(t_weibo.GetReplyWeiboId() != -1){
				m_weibo.updateStatus(t_weibo.GetText(),t_weibo.GetReplyWeiboId());
			}
			
		}catch(Exception e){
			m_mainMgr.m_logger.LogOut(GetAccountName() + "[SinaWeiBo] Exception:" + e.getMessage());
			m_mainMgr.m_logger.PrinterException(e);
		}
		
		return false;
	}
	
	public boolean ProcessWeiboConfirmed(ByteArrayInputStream in)throws Exception{
		final long t_id = sendReceive.ReadLong(in);

		boolean t_found = false;
		for(fetchWeibo confirmOne : m_weiboTimeLineConfirm){
			
			if(confirmOne.GetId() == t_id){
				t_found = true;
				m_weiboTimeLineConfirm.remove(confirmOne);
				
				m_mainMgr.m_logger.LogOut("Weibo Account<" + GetAccountName() + "> confirm Weibo<" + t_id + ">");
				break;
			}
		}
		
		return t_found;
	}
	
	public void ImportWeibo(fetchWeibo _weibo,Status _stat){
		_weibo.SetId(_stat.getId());
		_weibo.SetDateLong(_stat.getCreatedAt().getTime());
		_weibo.SetText(_stat.getText());
		
		_weibo.SetWeiboStyle(fetchWeibo.SINA_WEIBO);
		
		User t_user = _stat.getUser();
		_weibo.SetUserId(t_user.getId());
		_weibo.SetUserName(t_user.getName());		
				
		if(_stat.getInReplyToStatusId() != -1){
			
			try{
				Status t_commentStatus = m_weibo.showStatus(_weibo.GetReplyWeiboId());
				fetchWeibo t_replayWeibo = new fetchWeibo(m_mainMgr.m_convertToSimpleChar);
				
				ImportWeibo(t_replayWeibo,t_commentStatus);
				
				_weibo.SetCommectWeiboId(_stat.getInReplyToStatusId());
				_weibo.SetCommectWeibo(t_replayWeibo);
				
			}catch(Exception e){
				m_mainMgr.m_logger.LogOut(GetAccountName() + "[SinaWeiBo] Exception:" + e.getMessage());
				m_mainMgr.m_logger.PrinterException(e);
			}
		}		
		
		if(_stat.getInReplyToUserId() != -1){
			
			try{
				Status t_replyStatus = m_weibo.showStatus(_weibo.GetReplyWeiboId());
				fetchWeibo t_replayWeibo = new fetchWeibo(m_mainMgr.m_convertToSimpleChar);
				
				ImportWeibo(t_replayWeibo,t_replyStatus);
				
				_weibo.SetReplyWeiboId(_stat.getInReplyToStatusId());
				_weibo.SetReplyWeibo(t_replayWeibo);
				
			}catch(Exception e){
				m_mainMgr.m_logger.LogOut(GetAccountName() + "[SinaWeiBo] Exception:" + e.getMessage());
				m_mainMgr.m_logger.PrinterException(e);
			}
		}		
		
		
	}

}
