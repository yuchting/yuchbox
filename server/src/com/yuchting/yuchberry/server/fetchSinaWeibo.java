package com.yuchting.yuchberry.server;

import java.io.ByteArrayInputStream;
import java.util.Vector;

import org.dom4j.Element;

import weibo4j.Weibo;

public class fetchSinaWeibo extends fetchAccount{
	
	Weibo	m_weibo				= new Weibo();
	
	String	m_prefix			= null;
	
	String	m_accountName 		= null;
	
	String	m_accessToken		= null;
	String	m_secretToken		= null;
	
	long	m_fetchWeiboIndex	= 0;
	
	Vector	m_weiboTimeline			= new Vector();
		
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
	public void CheckFolder()throws Exception{
		
		
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
		}
		
		return t_processed;
	}
	
	/**
	 * prepare the re-push unconfirm msg
	 */
	public void PrepareRepushUnconfirmMsg(){
		
	}
	
	/**
	 * push the message to client
	 */
	public void PushMsg(sendReceive _sendReceive)throws Exception{
		
	}
	
	
	public boolean ProcessWeiboUpdate(ByteArrayInputStream in)throws Exception{
		
		fetchWeibo t_weibo = new fetchWeibo(m_mainMgr.m_convertToSimpleChar);
		t_weibo.InputWeibo(in);

		try{
			
			if(t_weibo.GetCommentWeiboId() == 0 && t_weibo.GetReplyWeiboId() == 0){
				m_weibo.updateStatus(t_weibo.GetText());
			}else if(t_weibo.GetCommentWeiboId() != 0){
				m_weibo.updateComment(t_weibo.GetText(),Long.toString(t_weibo.GetCommentWeiboId()),null);
			}else if(t_weibo.GetReplyWeiboId() != 0){
				m_weibo.updateStatus(t_weibo.GetText(),t_weibo.GetReplyWeiboId());
			}
			
		}catch(Exception e){
			m_mainMgr.m_logger.LogOut(GetAccountName() + "[SinaWeiBo] Exception:" + e.getMessage());
		}
		
		return false;
	}

}
