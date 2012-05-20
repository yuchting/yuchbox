/**
 *  Dear developer:
 *  
 *   If you want to modify this file of project and re-publish this please visit:
 *  
 *     http://code.google.com/p/yuchberry/wiki/Project_files_header
 *     
 *   to check your responsibility and my humble proposal. Thanks!
 *   
 *  -- 
 *  Yuchs' Developer    
 *  
 *  
 *  
 *  
 *  尊敬的开发者：
 *   
 *    如果你想要修改这个项目中的文件，同时重新发布项目程序，请访问一下：
 *    
 *      http://code.google.com/p/yuchberry/wiki/Project_files_header
 *      
 *    了解你的责任，还有我卑微的建议。 谢谢！
 *   
 *  -- 
 *  语盒开发者
 *  
 */
package com.yuchting.yuchberry.server;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.dom4j.Element;

import weibo4j.Comment;
import weibo4j.DirectMessage;
import weibo4j.IDs;
import weibo4j.Paging;
import weibo4j.RateLimitStatus;
import weibo4j.Status;
import weibo4j.User;
import weibo4j.Weibo;
import weibo4j.http.ImageItem;
import weibo4j.http.RequestToken;

public class fetchSinaWeibo extends fetchAbsWeibo{
	
	public static final String	fsm_consumer_key = getSinaConsumerKey();
	public static final String	fsm_consumer_serect = getSinaSecretKey();
	
	static
	{
		try{
			
			Weibo.CONSUMER_KEY = fsm_consumer_key;
	    	Weibo.CONSUMER_SECRET = fsm_consumer_serect;
	    	
	    	System.setProperty("weibo4j.oauth.consumerKey", Weibo.CONSUMER_KEY);
	    	System.setProperty("weibo4j.oauth.consumerSecret", Weibo.CONSUMER_SECRET);
		}catch(Exception e){
			
		}
		
		
	};
	
	Weibo	m_weibo				= new Weibo();
	User 	m_userself 			= null;
	
	// following and followers list
	Set<Long>					m_followingList				= new HashSet<Long>();
	Set<Long>					m_followerList				= new HashSet<Long>();
	long						m_refreshFollowingListTimer = 0;
	
	public fetchSinaWeibo(fetchMgr _mainMgr){
		super(_mainMgr);
	}
	
	public void InitAccount(Element _elem)throws Exception{
		super.InitAccount(_elem);
		
		m_accountName = m_accountName + "[SinaWeibo]";
		
		m_headImageDir			= m_headImageDir + "Sina/";
		File t_file = new File(m_headImageDir);
		if(!t_file.exists()){
			t_file.mkdir();
		}
		
		// if sync his/her account will refresh following and follower list
		//
		m_refreshFollowingListTimer = 0;
	}
	
	/**
	 * overload to refresh following and follower list interval
	 */
	public synchronized void CheckFolder()throws Exception{
		super.CheckFolder();
		
		try{
			refreshFollowingFollowerList(false);
		}catch(Exception e){
			m_mainMgr.m_logger.LogOut(GetAccountName() + " refreshFollowingFollowerList Error:"+ e.getMessage());
			// sleep for a while
			//
			Thread.sleep(2000);
		}
	}
	
	protected int GetCurrWeiboStyle(){
		return fetchWeibo.SINA_WEIBO_STYLE;
	}
	
	protected void CheckTimeline()throws Exception{
		
		List<Status> t_fetch = null;
		if(m_timeline.m_fromIndex > 1){
						
			t_fetch = m_weibo.getHomeTimeline(new Paging(m_timeline.m_fromIndex));
		}else{
						
			t_fetch = m_weibo.getHomeTimeline();
		}		 
		
		AddWeibo(t_fetch,m_timeline,fetchWeibo.TIMELINE_CLASS);
		
	}
	
	protected void CheckDirectMessage()throws Exception{
//				
//		List<DirectMessage> t_fetch = null;
//		if(m_directMessage.m_fromIndex > 1){
//			t_fetch = m_weibo.getDirectMessages(new Paging(m_directMessage.m_fromIndex));
//		}else{
//			t_fetch = m_weibo.getDirectMessages();
//		}	
//		
//		boolean t_insert;
//		for(DirectMessage fetchOne : t_fetch){
//			t_insert = true;
//			for(fetchWeibo weibo : m_directMessage.m_weiboList){
//				if(weibo.GetId() == fetchOne.getId()){
//					t_insert = false;
//					break;
//				}
//			}
//			
//			if(t_insert){
//				for(fetchWeibo weibo : m_directMessage.m_WeiboComfirm){
//					if(weibo.GetId() == fetchOne.getId()){
//						t_insert = false;
//						break;
//					}
//				}
//			}
//			
//			if(t_insert){
//				fetchWeibo t_weibo = new fetchWeibo(m_mainMgr.m_convertToSimpleChar);
//				ImportWeibo(t_weibo, fetchOne);
//				
//				m_directMessage.m_weiboList.add(t_weibo);
//			}
//		}
//		
//		if(!t_fetch.isEmpty()){
//			DirectMessage t_lashOne = t_fetch.get(0);
//			m_directMessage.m_fromIndex = t_lashOne.getId() + 1;
//		}
	}
	
	protected void CheckAtMeMessage()throws Exception{
		List<Status> t_fetch = null;
		if(m_atMeMessage.m_fromIndex > 1){
			t_fetch = m_weibo.getMentions(new Paging(m_atMeMessage.m_fromIndex));
		}else{
			t_fetch = m_weibo.getMentions();
		}
		
		AddWeibo(t_fetch,m_atMeMessage,fetchWeibo.AT_ME_CLASS);
	}
	
	protected void CheckCommentMeMessage()throws Exception{
		List<Comment> t_fetch = m_weibo.getCommentsToMe();
		
		if(m_commentMessage.m_historyList == null){
			m_commentMessage.m_historyList = new Vector<fetchWeibo>();
		}
		
		boolean t_insert;
		
		for(Comment fetchOne : t_fetch){
			t_insert = true;
			for(fetchWeibo weibo : m_commentMessage.m_historyList){
				if(weibo.GetId() == fetchOne.getId()){
					t_insert = false;
					break;
				}
			}
						
			if(t_insert){
				fetchWeibo t_weibo = new fetchWeibo(m_mainMgr.m_convertToSimpleChar);
				ImportWeibo(t_weibo, fetchOne);
				
				m_commentMessage.m_weiboList.add(t_weibo);
				
				if(m_commentMessage.m_historyList.size() > 512){
					m_commentMessage.m_historyList.remove(0);
				}
				
				m_commentMessage.m_historyList.add(t_weibo);
			}
		}
	}
	
	private void AddWeibo(List<Status> _from,fetchWeiboData _to,byte _class){
		
		boolean t_insert;
		for(Status fetchOne : _from){
			
			if(_to.m_weiboList.size() >= _to.m_sum){
				break;
			}
			
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
	
	
	
	/**
	 * reset the session for connection
	 * 
	 * @param _fullTest		: whether test the full configure( SMTP for email)
	 */
	public void ResetSession(boolean _fullTest)throws Exception{
		
		m_weibo.setToken(m_accessToken, m_secretToken);		
		m_userself = m_weibo.verifyCredentials();
		
		ResetCheckFolderLimit();
				
		if(m_followingList.isEmpty() || m_followerList.isEmpty()){
			refreshFollowingFollowerList(true);
		}
		
		m_mainMgr.m_logger.LogOut("Weibo Account<" + GetAccountName() + "> Prepare OK!");
	}
	
	/**
	 * request the following and follower list max page number
	 */
	static final int MaxPageNum = 5000;
	/**
	 * refresh the following and followers list
	 * @throws Exception
	 */
	private void refreshFollowingFollowerList(boolean _ignoreTimer)throws Exception{
		
		if(!_ignoreTimer){
			// refresh interval judge
			//
			if(System.currentTimeMillis() - m_refreshFollowingListTimer < 24 * 3600000){
				return;
			}
		}
		
		// clear the list first
		//
		m_followingList.clear();
		m_followerList.clear();
		
		// get the friends ids
		//
		if(m_userself.getFriendsCount() != 0){	
			int t_cursor = 0;
			while(true){
				IDs t_ids = m_weibo.getFriendsIDs(m_userself.getId(),t_cursor,MaxPageNum);
								
				for(long id:t_ids.getIDs()){
					m_followingList.add(id);
				}
				
				if(t_ids.getIDs().length < MaxPageNum){
					break;
				}
				
				t_cursor += t_ids.getIDs().length;
			}
		}
		
		// get the followers ids
		//
		if(m_userself.getFollowersCount() != 0){
			
			int t_cursor = 0;
			while(true){
				
				IDs t_ids = m_weibo.getFollowersIDs(m_userself.getId(),t_cursor,MaxPageNum);
				
				for(long id:t_ids.getIDs()){
					m_followerList.add(id);
				}
				
				if(t_ids.getIDs().length < MaxPageNum){
					break;
				}
				
				t_cursor += t_ids.getIDs().length;
			}
		}
		
		m_refreshFollowingListTimer = System.currentTimeMillis();
	}
	
	private boolean isUserFollowing(long _id){
		return m_followingList.contains(_id);
	}
	
	private boolean isUserFollower(long _id){
		return m_followerList.contains(_id);
	}
	
	protected void ResetCheckFolderLimit()throws Exception{
		RateLimitStatus limitStatus = m_weibo.rateLimitStatus();
		m_currRemainCheckFolderNum = limitStatus.getRemainingHits();
		m_maxCheckFolderNum			= limitStatus.getHourlyLimit();
	}
	
	
	
	protected void UpdateStatus(String _text,GPSInfo _info,byte[] _filePic,String _fileType)throws Exception{
				
		if(_info != null && _info.m_latitude != 0 && _info.m_longitude != 0){
			
			if(_filePic != null && _fileType != null){
						
				ImageItem t_image = new ImageItem("pic",_filePic,_fileType);
				m_weibo.uploadStatus(_text,t_image,_info.m_latitude,_info.m_longitude);
							
			}else{
				m_weibo.updateStatus(_text,_info.m_latitude,_info.m_longitude);
			}			
	
		}else{
			
			if(_filePic != null && _fileType != null){
				
				ImageItem t_image = new ImageItem("pic",_filePic,_fileType);
				m_weibo.uploadStatus(_text,t_image);
			
			}else{
				m_weibo.updateStatus(_text);
			}
		}	
	}
		
 
	protected void UpdateComment(int _style,String _text,long _orgWeiboId,
									GPSInfo _info,boolean _updateTimeline)throws Exception{

		if(_style == GetCurrWeiboStyle()){
			m_weibo.updateComment(_text,Long.toString(_orgWeiboId),null);
						 
			if(_updateTimeline){
				if(_info != null && _info.m_longitude != 0 && _info.m_latitude != 0){
					m_weibo.updateStatus(_text, _orgWeiboId, _info.m_latitude, _info.m_longitude);
				}else{
					m_weibo.updateStatus(_text, _orgWeiboId);
				}						
			}
						
		}else{
			
			if(_info != null && _info.m_longitude != 0 && _info.m_latitude != 0){
				m_weibo.updateStatus(_text, _info.m_latitude, _info.m_longitude);
			}else{
				m_weibo.updateStatus(_text);
			}
		}
			
	}
	
	protected void UpdateReply(String _text,long _commentWeiboId,long _orgWeiboId,
			GPSInfo _info,boolean _updateTimeline)throws Exception{
		
		if(_commentWeiboId != 0){
			
			m_weibo.reply(Long.toString(_orgWeiboId),Long.toString(_commentWeiboId), _text);
			
			if(_updateTimeline){
				if(_info != null && _info.m_longitude != 0 && _info.m_latitude != 0){
					m_weibo.updateStatus(_text, _orgWeiboId, _info.m_latitude, _info.m_longitude);
				}else{
					m_weibo.updateStatus(_text, _orgWeiboId);
				}						
			}
			
		}else{
			
			UpdateComment(GetCurrWeiboStyle(),_text,_orgWeiboId,_info,_updateTimeline);
		}
	}
	
	protected void FavoriteWeibo(long _id)throws Exception{
		m_weibo.createFavorite(_id);			
	}

	protected void FollowUser(String _screenName)throws Exception{
		m_weibo.createFriendship(_screenName);
	}
	
	protected void UnfollowUser(String _screenName)throws Exception{
		m_weibo.destroyFriendship(_screenName);
	}
	
	protected void DeleteWeibo(long _id,boolean _isComment)throws Exception{
		m_weibo.destroyStatus(_id);
	}
	
	protected void setFriendRemark(String _id,String _remark)throws Exception{
		m_weibo.updateFriendRemark(Long.parseLong(_id),_remark);
	}
	
	protected fetchWeiboUser getWeiboUser(String _name)throws Exception{
		User t_user = m_weibo.showUser(_name);
		
		fetchWeiboUser t_weibo = new fetchWeiboUser(m_mainMgr.m_convertToSimpleChar);
		
		t_weibo.setStyle(fetchWeibo.SINA_WEIBO_STYLE);
		t_weibo.setId(t_user.getId());
		
		t_weibo.setName(t_user.getName());
		t_weibo.setScreenName(t_user.getScreenName());
		t_weibo.setHeadImage(DownloadHeadImage(t_user.getProfileImageURL(),Long.toString(t_user.getId())));
		t_weibo.setDesc(t_user.getDescription());
		t_weibo.setCity(t_user.getLocation());
		t_weibo.setVerified(t_user.isVerified());		
	
		t_weibo.setFollowNum(t_user.getFriendsCount());
		t_weibo.setFansNum(t_user.getFollowersCount());
		t_weibo.setWeiboNum(t_user.getStatusesCount());
		
		t_weibo.setHasBeenFollowed(isUserFollowing(t_user.getId()));
		t_weibo.setIsMyFans(isUserFollower(t_user.getId()));
		
		List<Status> t_list = m_weibo.getUserTimeline(Long.toString(t_user.getId()),new Paging(1, 10));
		for(Status s:t_list){
			fetchWeibo weibo = new fetchWeibo(m_mainMgr.m_convertToSimpleChar);
			ImportWeibo(weibo, s,fetchWeibo.TIMELINE_CLASS);
			
			t_weibo.getUpdatedWeibo().insertElementAt(weibo, 0);
		}
		
		return t_weibo;
	}
	
	protected void sendDirectMsg(String _screenName,String _text)throws Exception{
		throw new Exception("sina direct message no support."); 
	}
	
	protected void addWeiboAccountList(Vector<WeiboAccount> _accList){
		if(m_userself != null){
			WeiboAccount acc = new WeiboAccount();
			
			acc.name 		= m_userself.getScreenName();
			acc.id			= m_userself.getId();
			acc.weiboStyle	= (byte)GetCurrWeiboStyle();
			acc.needUpdate	= true;	
			
			_accList.add(acc);
		}
	}
	
	protected long getCurrAccountId(){
		if(m_userself != null){
			return m_userself.getId();
		}else{
			return 0;
		}
	}
	
	public void ImportWeibo(fetchWeibo _weibo,Status _stat,byte _weiboClass){
		
		_weibo.SetId(_stat.getId());
		_weibo.SetDateLong(_stat.getCreatedAt().getTime());
		_weibo.SetText(_stat.getText());
		_weibo.SetSource(_stat.getSource());
		
		_weibo.SetWeiboStyle(fetchWeibo.SINA_WEIBO_STYLE);
		_weibo.SetWeiboClass(_weiboClass);
		
		User t_user = _stat.getUser();

		_weibo.SetOwnWeibo(t_user.getId() == m_userself.getId());
		
		_weibo.SetUserId(t_user.getId());
		_weibo.SetUserName(t_user.getName());
		_weibo.SetUserScreenName(t_user.getScreenName());
		_weibo.SetSinaVIP(t_user.isVerified());
		
		_weibo.setUserFollowing(isUserFollowing(t_user.getId()));
		_weibo.setUserFollowMe(isUserFollower(t_user.getId()));
		
		if(_stat.getOriginal_pic() != null){
			_weibo.SetOriginalPic(_stat.getOriginal_pic());
		}		
		
		_weibo.SetUserHeadImageHashCode(StoreHeadImage(t_user.getProfileImageURL(),Long.toString(t_user.getId())));		
		
		try{
			
			if(_stat.getInReplyToStatusId() != -1){
				
				Status t_commentStatus = m_weibo.showStatus(_weibo.GetReplyWeiboId());
				fetchWeibo t_replayWeibo = new fetchWeibo(m_mainMgr.m_convertToSimpleChar);
				
				ImportWeibo(t_replayWeibo,t_commentStatus,_weiboClass);
				
				_weibo.SetCommectWeiboId(_stat.getInReplyToStatusId());
				_weibo.SetCommectWeibo(t_replayWeibo);
								
			}else{
				
				if(_stat.getCommentStatus() != null){
	
					fetchWeibo t_replayWeibo = new fetchWeibo(m_mainMgr.m_convertToSimpleChar);
					ImportWeibo(t_replayWeibo,_stat.getCommentStatus(),_weiboClass);
					
					_weibo.SetCommectWeiboId(t_replayWeibo.GetId());
					_weibo.SetCommectWeibo(t_replayWeibo);
				}
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
				
		_weibo.SetWeiboStyle(fetchWeibo.SINA_WEIBO_STYLE);
		_weibo.SetWeiboClass(fetchWeibo.DIRECT_MESSAGE_CLASS);
		
		_weibo.SetOwnWeibo(_dm.getSenderId() == m_userself.getId());
		_weibo.SetReplyWeiboId(_dm.getRecipientId());
		
		User t_user = _dm.getSender();
		if(t_user != null){
			_weibo.SetUserId(t_user.getId());
			_weibo.SetUserName(t_user.getName());
			_weibo.SetUserScreenName(t_user.getScreenName());
			_weibo.SetSinaVIP(t_user.isVerified());	
		}
		
		_weibo.setUserFollowing(isUserFollowing(t_user.getId()));
		_weibo.setUserFollowMe(isUserFollower(t_user.getId()));
				
		_weibo.SetUserHeadImageHashCode(StoreHeadImage(t_user.getProfileImageURL(),Long.toString(t_user.getId())));
	}
	
	public void ImportWeibo(fetchWeibo _weibo,Comment _comment){
		_weibo.SetId(_comment.getId());
		_weibo.SetDateLong(_comment.getCreatedAt().getTime());
		_weibo.SetText(_comment.getText());
		_weibo.SetSource(_comment.getSource());
		
		_weibo.SetWeiboStyle(fetchWeibo.SINA_WEIBO_STYLE);
		_weibo.SetWeiboClass(fetchWeibo.COMMENT_ME_CLASS);
				
		User t_user = _comment.getUser();
		
		if(t_user != null){	
			_weibo.SetOwnWeibo(t_user.getId() == m_userself.getId());
			_weibo.SetUserId(t_user.getId());
			_weibo.SetUserName(t_user.getName());
			_weibo.SetUserScreenName(t_user.getScreenName());
			_weibo.SetSinaVIP(t_user.isVerified());	
		}
		
		_weibo.setUserFollowing(isUserFollowing(t_user.getId()));
		_weibo.setUserFollowMe(isUserFollower(t_user.getId()));
		
		try{
			
			if(_comment.getOriginalStatus() != null){
				
				fetchWeibo t_replyWeibo = new fetchWeibo(m_mainMgr.m_convertToSimpleChar);
				ImportWeibo(t_replyWeibo,_comment.getOriginalStatus(),fetchWeibo.TIMELINE_CLASS);
				
				if(_comment.getReplyCommentStates() != null){
					
					_weibo.SetReplyWeiboId(t_replyWeibo.GetId());
					_weibo.SetReplyWeibo(t_replyWeibo);
					
					fetchWeibo t_commnetWeibo = new fetchWeibo(m_mainMgr.m_convertToSimpleChar);
					ImportWeibo(t_commnetWeibo,_comment.getReplyCommentStates(),fetchWeibo.TIMELINE_CLASS);
					
					_weibo.SetCommectWeiboId(t_commnetWeibo.GetId());
					_weibo.SetCommectWeibo(t_commnetWeibo);
					
				}else{
					
					_weibo.SetCommectWeiboId(t_replyWeibo.GetId());
					_weibo.SetCommectWeibo(t_replyWeibo);					
				}
				
			}
		
		}catch(Exception e){
			m_mainMgr.m_logger.LogOut(GetAccountName() + " Exception:" + e.getMessage());
			m_mainMgr.m_logger.PrinterException(e);
		}
		
		_weibo.SetUserHeadImageHashCode(StoreHeadImage(t_user.getProfileImageURL(),Long.toString(t_user.getId())));
	}
		
	public RequestToken getRequestToken()throws Exception{		
		return m_weibo.getOAuthRequestToken();
	}
	
	public RequestToken getRequestToken(String _callback)throws Exception{		
		return m_weibo.getOAuthRequestToken(_callback);
	}
	
	static public void main(String[] _arg)throws Exception{
		
		File t_testfile = new File("yuchberry/WeiboHeadImage/QQ/yuchberry.png");
		System.out.print(t_testfile.length());
		
		fetchMgr t_manger = new fetchMgr();
		Logger t_logger = new Logger("");
		
		t_logger.EnabelSystemOut(true);
		t_manger.InitConnect("",t_logger);
		
		fetchSinaWeibo t_weibo = new fetchSinaWeibo(t_manger);		
		
		// yuchberry_test
		//t_weibo.m_accessToken = "8a2bf4e5a97194a1eb73740b448f034e";
		//t_weibo.m_secretToken = "7529265879f3c97af609c694064bbc59";
		
		// yuchberry
		t_weibo.m_accessToken = "1eef9899c847f1cd0735c6775fdcc576";
		t_weibo.m_secretToken = "5f248ea06daf600f17edd57c303cc9ce";
		
		
		t_weibo.ResetSession(true);
		
		t_weibo.m_timeline.m_sum = 20;
		
		t_weibo.CheckTimeline();
		//fetchWeiboUser t_user = t_weibo.getWeiboUser(t_weibo.m_timeline.m_weiboList.get(0).GetUserScreenName());
		
		User t_userdd = t_weibo.m_weibo.updateFriendRemark(t_weibo.m_timeline.m_weiboList.get(0).GetUserId(), "测试备注");
		
		
//		File t_file = new File("1314193031_0.satt");
//		FileInputStream t_fileIn = new FileInputStream(t_file);
//		byte[] t_fileBuffer = new byte[(int)t_file.length()];
//		
//		sendReceive.ForceReadByte(t_fileIn, t_fileBuffer, t_fileBuffer.length);
		
		//t_weibo.m_weibo.updateStatus("果我的话说长了是不是就发不出来了呢？[哈哈]");
		//t_weibo.m_weibo.uploadStatus("果我的话说长了是不是就发不出来了呢？[哈哈]",t_file);
		//t_weibo.UpdateStatus("如果我的话说长了是不是就发不出来了呢？[哈哈]", null, t_fileBuffer,"image/jpeg");
		
		long t_cursor = -1l;
		while(true){
			IDs t_ids = t_weibo.m_weibo.getFriendsIDs(t_cursor);
			
			if(t_ids.getIDs().length == 0){
				System.out.println("over " + t_ids.getNextCursor());
				break;
			}
			
			long[] t_idsIndex = t_ids.getIDs();
			for(long id : t_idsIndex){
				System.out.println(id);
			}
			
			t_cursor = t_ids.getNextCursor();
			
		}
		
	}
	
}
