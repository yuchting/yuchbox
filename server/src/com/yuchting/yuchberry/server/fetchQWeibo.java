package com.yuchting.yuchberry.server;

import java.io.File;
import java.util.List;

import org.dom4j.Element;

import com.mime.qweibo.QWeibo;
import com.mime.qweibo.QWeiboSyncApi;

public class fetchQWeibo extends fetchAbsWeibo{
	
	final static String		fsm_consumerKey 	= "06d0a334755146d9b3dfe024e205f36b";
	final static String		fsm_consumerSecret	= "dcc64862deec1a57e98b6985c405e369";
	
	
	
	QWeiboSyncApi m_api = new QWeiboSyncApi();
		
	long m_userId		= 0;
	
	public fetchQWeibo(fetchMgr _mainMgr){
		super(_mainMgr);
		m_api.setCostomerKey(fsm_consumerKey, fsm_consumerSecret);
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

	protected void CheckTimeline()throws Exception{
		
		
	}
	
	protected void CheckDirectMessage()throws Exception{
		
	
	}
	
	protected void CheckAtMeMessage()throws Exception{
		
	}
	
	protected void CheckCommentMeMessage()throws Exception{
		
	}

	protected void DeleteWeibo(long _id)throws Exception{
		
	}
	
	
	
	/**
	 * reset the session for connection
	 * 
	 * @param _fullTest		: whether test the full configure( SMTP for email)
	 */
	public void ResetSession(boolean _fullTest)throws Exception{
		
		
	}
	
	protected void ResetCheckFolderLimit()throws Exception{
		
	}
	
	/**
	 * destroy the session connection
	 */
	public void DestroySession(){
		
	}	
	
	protected void UpdataStatus(String _text,GPSInfo _info)throws Exception{
			
	}
	
	protected void UpdataComment(int _style,String _text,long _commentWeiboId,
									GPSInfo _info,boolean _updateTimeline)throws Exception{

	
			
	}
	
	protected void FavoriteWeibo(long _id)throws Exception{
				
	}

	protected void FollowUser(long _id)throws Exception{
		
	}
	
	
	public static String				sm_requestTokenKey		= null;
	public static String				sm_requestTokenSecret	= null;
	
	public String getVerifyPinURL()throws Exception{
		
		String t_response = m_api.getRequestToken();
		
		if(!parseToken(t_response)){
			throw new Exception("error server request PIN URL response :" + t_response); 
		}
		
		return "http://open.t.qq.com/cgi-bin/authorize?" + t_response;
	}
	
	public void RequestTokenByVerfiyPIN(String _verifyPIN)throws Exception{
		
		if(sm_requestTokenKey == null || sm_requestTokenSecret == null){
			throw new Exception("call the getVerifyPinURL first plz"); 
		}
		
		m_api.setAccessToken(sm_requestTokenKey, sm_requestTokenKey);
		String t_response = m_api.getAccessToken(_verifyPIN);
		
		if(!parseToken(t_response)){
			throw new Exception("error server request token response :" + t_response); 
		}
	}
	

	
	static boolean parseToken(String response) {
		if (response == null || response.equals("")) {
			return false;
		}

		String[] tokenArray = response.split("&");

		if (tokenArray.length < 2) {
			return false;
		}

		String strTokenKey = tokenArray[0];
		String strTokenSecrect = tokenArray[1];

		String[] token1 = strTokenKey.split("=");
		if (token1.length < 2) {
			return false;
		}
		sm_requestTokenKey = token1[1];

		String[] token2 = strTokenSecrect.split("=");
		if (token2.length < 2) {
			return false;
		}
		sm_requestTokenSecret = token2[1];

		return true;
	}
	
	public static void main(String[] _arg)throws Exception{
		fetchQWeibo t_weibo = new fetchQWeibo(null);
		t_weibo.m_api.setCostomerKey(fsm_consumerKey, fsm_consumerSecret);
		t_weibo.m_api.setAccessToken("2189ff2946d3498a82e6bbb8b2b57d61", "8c160ee7358a5dc07e539429c3cbb487");
		
		List<QWeibo> t_list = t_weibo.m_api.getMentionList();
		
		for(QWeibo weibo : t_list){
			
			System.out.println("@" + weibo.getName() + " :"+ weibo.getText());
			
		}
	
	}
	
	
	

}
