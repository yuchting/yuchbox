package com.yuchting.yuchberry.server;

import java.io.File;
import java.util.List;

import org.dom4j.Element;

import twitter4j.DirectMessage;
import twitter4j.GeoLocation;
import twitter4j.Paging;
import twitter4j.RateLimitStatus;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;



public class fetchTWeibo extends fetchAbsWeibo{

	static String sm_consumerKey 	= "YdIUCx8guEJm9h46HiG1w";
	static String sm_consumerSecret = "nihtObnPRottjwJaQf6Y1iXlOKdojhvve6A1jh3aV6w";
	
	Twitter	m_twitter	= new TwitterFactory().getInstance();
	User	m_userself	= null;
	
	public fetchTWeibo(fetchMgr _main){
		super(_main);
	}
	
	public void InitAccount(Element _elem)throws Exception{
		super.InitAccount(_elem);
		
		m_twitter.setOAuthConsumer(sm_consumerKey, sm_consumerSecret);
		
		m_accountName = m_accountName + "[TWeibo]";
		
		m_headImageDir			= m_headImageDir + "Twitter/";
		File t_file = new File(m_headImageDir);
		if(!t_file.exists()){
			t_file.mkdir();
		}
		
	}
	
	protected int GetCurrWeiboStyle(){
		return fetchWeibo.TWITTER_WEIBO_STYLE;
	}
	
	/**
	 * reset the session for connection
	 * 
	 * @param _fullTest		: whether test the full configure( SMTP for email)
	 */
	public void ResetSession(boolean _fullTest)throws Exception{
		
		m_twitter.setOAuthAccessToken(new AccessToken(m_accessToken, m_secretToken));

		m_userself = m_twitter.verifyCredentials();
		
		ResetCheckFolderLimit();
		
		m_mainMgr.m_logger.LogOut("Weibo Account<" + GetAccountName() + "> Prepare OK!");
	}
	
	protected void ResetCheckFolderLimit()throws Exception{
		RateLimitStatus limitStatus = m_twitter.getRateLimitStatus();
		m_currRemainCheckFolderNum = limitStatus.getRemainingHits();
		m_maxCheckFolderNum			= limitStatus.getHourlyLimit();
	}
	
	/**
	 * destroy the session connection
	 */
	public void DestroySession(){
		
	}
	
	protected void CheckTimeline()throws Exception{
		List<Status> t_fetch = null;
		if(m_timeline.m_fromIndex > 1){
			t_fetch = m_twitter.getHomeTimeline(new Paging(m_timeline.m_fromIndex));
		}else{
			t_fetch = m_twitter.getHomeTimeline();
		}		 
		
		AddWeibo(t_fetch,m_timeline,fetchWeibo.TIMELINE_CLASS);
	}
	
	private void AddWeibo(List<Status> _from,fetchWeiboData _to,byte _class){
		
		boolean t_insert;
		for(Status fetchOne : _from){
			t_insert = true;
			for(fetchWeibo weibo : _to.m_weiboList){
				if(weibo.GetId() == fetchOne.getId()){
					t_insert = false;
					break;
				}
			}
			
			if(t_insert){
				for(fetchWeibo weibo : _to.m_WeiboComfirm){
					if(weibo.GetId() == fetchOne.getId()){
						t_insert = false;
						break;
					}
				}
			}
			
			if(t_insert){
				fetchWeibo t_weibo = new fetchWeibo(m_mainMgr.m_convertToSimpleChar);
				ImportWeibo(t_weibo, fetchOne,_class);
				
				_to.m_weiboList.add(t_weibo);
			}
		}
		
		if(!_from.isEmpty()){
			Status t_lashOne = _from.get(0);
			_to.m_fromIndex = t_lashOne.getId() + 1;
		}
	}
	
	protected void CheckDirectMessage()throws Exception{
		List<DirectMessage> t_fetch = null;
		if(m_directMessage.m_fromIndex > 1){
			t_fetch = m_twitter.getDirectMessages(new Paging(m_directMessage.m_fromIndex));
		}else{
			t_fetch = m_twitter.getDirectMessages();
		}	
		
		boolean t_insert;
		for(DirectMessage fetchOne : t_fetch){
			t_insert = true;
			for(fetchWeibo weibo : m_directMessage.m_weiboList){
				if(weibo.GetId() == fetchOne.getId()){
					t_insert = false;
					break;
				}
			}
			
			if(t_insert){
				for(fetchWeibo weibo : m_directMessage.m_WeiboComfirm){
					if(weibo.GetId() == fetchOne.getId()){
						t_insert = false;
						break;
					}
				}
			}
			
			if(t_insert){
				fetchWeibo t_weibo = new fetchWeibo(m_mainMgr.m_convertToSimpleChar);
				ImportWeibo(t_weibo, fetchOne);
				
				m_directMessage.m_weiboList.add(t_weibo);
			}
		}
		
		if(!t_fetch.isEmpty()){
			DirectMessage t_lashOne = t_fetch.get(0);
			m_directMessage.m_fromIndex = t_lashOne.getId() + 1;
		}
	}
	protected void CheckAtMeMessage()throws Exception{
		List<Status> t_fetch = null;
		if(m_timeline.m_fromIndex > 1){
			t_fetch = m_twitter.getMentions(new Paging(m_timeline.m_fromIndex));
		}else{
			t_fetch = m_twitter.getMentions();
		}		 
		
		AddWeibo(t_fetch,m_timeline,fetchWeibo.AT_ME_CLASS);
	}
	
	protected void CheckCommentMeMessage()throws Exception{
		
	}
	
	protected void UpdataStatus(String _text,GPSInfo _info)throws Exception{
		if(_info != null && _info.m_latitude != 0 && _info.m_longitude != 0){
			
			GeoLocation t_geo = new GeoLocation(_info.m_latitude,_info.m_longitude);
			StatusUpdate t_status = new StatusUpdate(_text);
			t_status.setLocation(t_geo);
			
			m_twitter.updateStatus(t_status);
			
		}else{
			m_twitter.updateStatus(_text);
		}	
	}
	
	protected void UpdataComment(int _style,String _text,long _commentWeiboId,
									GPSInfo _info,boolean _updateTimeline)throws Exception{
					
		
		StatusUpdate t_status = new StatusUpdate(_text);
		
		if(_style == GetCurrWeiboStyle()){
			t_status.setInReplyToStatusId(_commentWeiboId);
		}		
		
		if(_info != null && _info.m_longitude != 0 && _info.m_latitude != 0){
			GeoLocation t_geo = new GeoLocation(_info.m_latitude,_info.m_longitude);
			t_status.setLocation(t_geo);
		}
		
		m_twitter.updateStatus(t_status);
			
	}
	
	protected void FavoriteWeibo(long _id)throws Exception{
		m_twitter.createFavorite(_id);			
	}

	protected void FollowUser(long _id)throws Exception{
		m_twitter.createFriendship(Long.toString(_id));
	}
	
	public void ImportWeibo(fetchWeibo _weibo,Status _stat,byte _weiboClass){
		_weibo.SetId(_stat.getId());
		_weibo.SetDateLong(_stat.getCreatedAt().getTime());
		_weibo.SetText(_stat.getText());
		_weibo.SetSource(_stat.getSource());
		
		_weibo.SetWeiboStyle(fetchWeibo.TWITTER_WEIBO_STYLE);
		_weibo.SetWeiboClass(_weiboClass);
		
		User t_user = _stat.getUser();

		_weibo.SetOwnWeibo(t_user.getId() == m_userself.getId());
		
		_weibo.SetUserId(t_user.getId());
		_weibo.SetUserName(t_user.getName());
		_weibo.SetSinaVIP(t_user.isVerified());
				
		
		_weibo.SetUserHeadImageHashCode(StoreHeadImage(t_user.getProfileImageURL(),t_user.getId()));		

		try{
			
			if(_stat.getInReplyToStatusId() != -1){
				
				Status t_commentStatus = m_twitter.showStatus(_weibo.GetReplyWeiboId());
				fetchWeibo t_replayWeibo = new fetchWeibo(m_mainMgr.m_convertToSimpleChar);
				
				ImportWeibo(t_replayWeibo,t_commentStatus,fetchWeibo.TIMELINE_CLASS);
				
				_weibo.SetCommectWeiboId(_stat.getInReplyToStatusId());
				_weibo.SetCommectWeibo(t_replayWeibo);
								
			}
			
			if(_stat.getInReplyToUserId() != -1){

				Status t_replyStatus = m_twitter.showStatus(_weibo.GetReplyWeiboId());
				fetchWeibo t_replayWeibo = new fetchWeibo(m_mainMgr.m_convertToSimpleChar);
				
				ImportWeibo(t_replayWeibo,t_replyStatus,fetchWeibo.TIMELINE_CLASS);
				
				_weibo.SetReplyWeiboId(_stat.getInReplyToStatusId());
				_weibo.SetReplyWeibo(t_replayWeibo);
					
			}
		
		}catch(Exception e){
			m_mainMgr.m_logger.LogOut(GetAccountName() + " Exception:" + e.getMessage());
			m_mainMgr.m_logger.PrinterException(e);
		}
	}
	
	public void ImportWeibo(fetchWeibo _weibo,DirectMessage _dm){
		_weibo.SetId(_dm.getId());
		_weibo.SetDateLong(_dm.getCreatedAt().getTime());
		_weibo.SetText(_dm.getText());
		
		_weibo.SetOwnWeibo(_dm.getSenderId() == m_userself.getId());
		_weibo.SetWeiboStyle(fetchWeibo.TWITTER_WEIBO_STYLE);
		_weibo.SetWeiboClass(fetchWeibo.DIRECT_MESSAGE_CLASS);
		
		User t_user = _dm.getSender();
		if(t_user != null){
			_weibo.SetUserId(t_user.getId());
			_weibo.SetUserName(t_user.getName());
			_weibo.SetSinaVIP(t_user.isVerified());	
		}
				
		_weibo.SetUserHeadImageHashCode(StoreHeadImage(t_user.getProfileImageURL(),t_user.getId()));
	}
	
	
	public RequestToken getRequestToken()throws Exception{	
		m_twitter.setOAuthConsumer(sm_consumerKey, sm_consumerSecret);
		return m_twitter.getOAuthRequestToken();
	}
	
	public Twitter getTwitter(){
		m_twitter.setOAuthConsumer(sm_consumerKey, sm_consumerSecret);
		return m_twitter;
	}
	
	static public void main(String[] _arg)throws Exception{
		fetchMgr t_manger = new fetchMgr();
		Logger t_logger = new Logger("");
		
		t_logger.EnabelSystemOut(true);
		t_manger.InitConnect("",t_logger);
		
		fetchTWeibo t_weibo = new fetchTWeibo(t_manger);		
		
		t_weibo.m_twitter.setOAuthConsumer(sm_consumerKey, sm_consumerSecret);
		
		t_weibo.m_accessToken = "123158821-96vShVD9oXGZmj6usNAz4vVyLzL1fJVGxKZABa1C";
		t_weibo.m_secretToken = "s9zpyuKVpOTksVr0tBC1md6Rge3SXvnQDzNkm8vg";
		
		t_weibo.ResetSession(true);
		
		t_weibo.CheckAtMeMessage();
	}
	
	
}
