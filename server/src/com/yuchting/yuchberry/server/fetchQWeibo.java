package com.yuchting.yuchberry.server;

import java.io.File;

import org.dom4j.Element;

import twitter4j.RateLimitStatus;
import twitter4j.auth.AccessToken;

import com.mime.qweibo.QWeiboSyncApi;

public class fetchQWeibo extends fetchAbsWeibo{
	
	public final static String		fsm_consumerKey 	= "06d0a334755146d9b3dfe024e205f36b";
	public final static String		fsm_consumerSecret	= "dcc64862deec1a57e98b6985c405e369";
	
	QWeiboSyncApi m_api = new QWeiboSyncApi();
	
	long m_userId		= 0;
	
	public fetchQWeibo(fetchMgr _mainMgr){
		super(_mainMgr);
	}
	
	public void InitAccount(Element _elem)throws Exception{
		super.InitAccount(_elem);
		
		m_accountName = m_accountName + "[QWeibo]";
		
		m_headImageDir			= m_headImageDir + "QQ/";
		File t_file = new File(m_headImageDir);
		if(!t_file.exists()){
			t_file.mkdir();
		}
		
	}
	
	protected int GetCurrWeiboStyle(){
		return fetchWeibo.QQ_WEIBO_STYLE;
	}
	
	private String getVerifyPinURL(){
		return m_api.getRequestToken(fsm_consumerKey, fsm_consumerSecret);
	}
	

}
