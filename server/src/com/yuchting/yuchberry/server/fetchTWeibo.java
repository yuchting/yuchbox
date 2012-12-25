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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.dom4j.Element;

import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;

import twitter4j.DirectMessage;
import twitter4j.GeoLocation;
import twitter4j.MediaEntity;
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

	final static String fsm_consumerKey 	= getTWConsumerKey();
	final static String fsm_consumerSecret = getTWSecretKey();
	
	Twitter	m_twitter	= new TwitterFactory().getInstance();
	User	m_userself	= null;
	
	public fetchTWeibo(fetchMgr _main){
		super(_main);
	}
	
	public void InitAccount(Element _elem)throws Exception{
		super.InitAccount(_elem);
		
		m_twitter.setOAuthConsumer(fsm_consumerKey, fsm_consumerSecret);
		
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
		Map<String,RateLimitStatus> t_statusMap = m_twitter.getRateLimitStatus();
		
        RateLimitStatus limitStatus_user_timeline = t_statusMap.get("/statuses/user_timeline");
        RateLimitStatus limitStatus_mentions_timeline = t_statusMap.get("/statuses/mentions_timeline");
        RateLimitStatus limitStatus_home_timeline = t_statusMap.get("/statuses/home_timeline");
        
        m_currRemainCheckFolderNum = limitStatus_user_timeline.getRemaining() + 
        							limitStatus_mentions_timeline.getRemaining() + 
        							limitStatus_home_timeline.getRemaining();
        
		m_maxCheckFolderNum		= limitStatus_user_timeline.getLimit() + 
									limitStatus_mentions_timeline.getRemaining() + 
									limitStatus_home_timeline.getRemaining();
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
		if(m_atMeMessage.m_fromIndex > 1){
			t_fetch = m_twitter.getMentionsTimeline(new Paging(m_atMeMessage.m_fromIndex));
		}else{
			t_fetch = m_twitter.getMentionsTimeline();
		}		 
		
		AddWeibo(t_fetch,m_atMeMessage,fetchWeibo.AT_ME_CLASS);
	}
	
	protected void CheckCommentMeMessage()throws Exception{
		
	}
	
	static byte[] sm_retweetOkPrompt = null;
	static {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		os.write(msg_head.msgWeiboPrompt);
		try{
			sendReceive.WriteString(os,"Retweet OK!",false);
			sm_retweetOkPrompt = os.toByteArray();
		}catch(Exception e){}
	}
	
	static byte[] sm_retweetOkPrompt_zh = null;
	static {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		os.write(msg_head.msgWeiboPrompt);
		try{
			sendReceive.WriteString(os,"Retweet 成功！",false);
			sm_retweetOkPrompt_zh = os.toByteArray();
		}catch(Exception e){}
	}
	
	protected boolean ProcessWeiboRetweet(ByteArrayInputStream in){
		
		try{

			long t_id = sendReceive.ReadLong(in);
			m_twitter.retweetStatus(t_id);
			
			if(m_mainMgr.GetClientLanguage() == fetchMgr.CLIENT_LANG_ZH_S){
				m_mainMgr.SendData(sm_retweetOkPrompt_zh,false);
			}else{
				m_mainMgr.SendData(sm_retweetOkPrompt,false);
			}
			
		}catch(Exception e){
			m_mainMgr.m_logger.PrinterException(e);
		}
		
		return true;
	}
	
	protected void UpdateStatus(String _text,GPSInfo _info,byte[] _filePic,String _fileType)throws Exception{
		StatusUpdate t_status = new StatusUpdate(_text);
		
		if(_info != null && _info.m_latitude != 0 && _info.m_longitude != 0){
			GeoLocation t_geo = new GeoLocation(_info.m_latitude,_info.m_longitude);
			t_status.setLocation(t_geo);
		}
		
		if(_filePic != null){
			t_status.setMedia("mypic", new ByteArrayInputStream(_filePic));		
		}
		
		m_twitter.updateStatus(t_status);
	}
	
	protected void UpdateComment(int _style,String _text,long _orgWeiboId,
									GPSInfo _info,int _updateStyle)throws Exception{
					
		
		StatusUpdate t_status = new StatusUpdate(_text);
		
		if(_style == GetCurrWeiboStyle()){
			t_status.setInReplyToStatusId(_orgWeiboId);
		}		
		
		if(_info != null && _info.m_longitude != 0 && _info.m_latitude != 0){
			t_status.setLocation(new GeoLocation(_info.m_latitude,_info.m_longitude));
		}
		
		m_twitter.updateStatus(t_status);
		
	}
	
	protected void UpdateReply(String _text,long _commentWeiboId,long _orgWeiboId,
			GPSInfo _info,boolean _updateTimeline)throws Exception{
		UpdateComment(GetCurrWeiboStyle(),_text,_orgWeiboId,_info,_updateTimeline?1:0);
	}
	
	protected void FavoriteWeibo(long _id)throws Exception{
		m_twitter.createFavorite(_id);			
	}

	protected void FollowUser(String _screenName)throws Exception{
		m_twitter.createFriendship(_screenName);
	}
	
	protected void UnfollowUser(String _screenName)throws Exception{
		m_twitter.destroyFriendship(_screenName);
	}
	
	protected void DeleteWeibo(long _id,boolean _isComment)throws Exception{
		m_twitter.destroyStatus(_id);
	}
	
	protected void setFriendRemark(String _id,String _remark)throws Exception{}
	
	protected fetchWeiboUser getWeiboUser(String _name)throws Exception{
		User t_user = m_twitter.showUser(_name);
		
		fetchWeiboUser t_weibo = new fetchWeiboUser(m_mainMgr.m_convertToSimpleChar);
		
		t_weibo.setStyle(fetchWeibo.TWITTER_WEIBO_STYLE);
		t_weibo.setId(t_user.getId());
		
		t_weibo.setName(t_user.getName());
		t_weibo.setScreenName(t_user.getScreenName());
		t_weibo.setHeadImage(DownloadHeadImage(new URL(t_user.getProfileImageURL()),Long.toString(t_user.getId())));
		t_weibo.setDesc(t_user.getDescription());
		t_weibo.setCity(t_user.getLocation());
		
		t_weibo.setIsMyFans(t_user.isContributorsEnabled());
		t_weibo.setHasBeenFollowed(t_user.isFollowRequestSent());
		
		t_weibo.setFollowNum(t_user.getFriendsCount());
		t_weibo.setFansNum(t_user.getFollowersCount());
		t_weibo.setWeiboNum(t_user.getStatusesCount());
		
		List<Status> t_list = m_twitter.getUserTimeline(_name, new Paging(1,10));
		for(Status s:t_list){
			fetchWeibo weibo = new fetchWeibo(m_mainMgr.m_convertToSimpleChar);
			ImportWeibo(weibo, s,fetchWeibo.TIMELINE_CLASS);
			
			t_weibo.getUpdatedWeibo().add(weibo);
		}
		
		return t_weibo;
	}
	
	protected void sendDirectMsg(String _screenName,String _text)throws Exception{
		m_twitter.sendDirectMessage(_screenName, _text);
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
		_weibo.SetText(replaceGFWVerified_URL(_stat.getText()));
		_weibo.SetSource(_stat.getSource());
		
		_weibo.SetWeiboStyle(fetchWeibo.TWITTER_WEIBO_STYLE);
		_weibo.SetWeiboClass(_weiboClass);
		
		MediaEntity[] t_mediaEntity = _stat.getMediaEntities();
		if(t_mediaEntity != null && t_mediaEntity.length != 0){
			_weibo.SetOriginalPic(t_mediaEntity[0].getMediaURL());
		}
		
		User t_user = _stat.getUser();

		_weibo.SetOwnWeibo(t_user.getId() == m_userself.getId());
		
		_weibo.SetUserId(t_user.getId());
		_weibo.SetUserName(t_user.getName());
		_weibo.SetUserScreenName(t_user.getScreenName());
		_weibo.SetSinaVIP(t_user.isVerified());

		try{
			
			String t_imageURL = t_user.getProfileImageURL();
			if(t_imageURL != null && t_imageURL.length() != 0){
				_weibo.SetUserHeadImageHashCode(StoreHeadImage(new URL(t_imageURL),Long.toString(t_user.getId())));
			}		
			
			if(_stat.getInReplyToStatusId() != -1){
				
				Status t_commentStatus = m_twitter.showStatus(_stat.getInReplyToStatusId());
				fetchWeibo t_replayWeibo = new fetchWeibo(m_mainMgr.m_convertToSimpleChar);
				
				ImportWeibo(t_replayWeibo,t_commentStatus,_weiboClass);
				
				_weibo.SetCommectWeiboId(_stat.getInReplyToStatusId());
				_weibo.SetCommectWeibo(t_replayWeibo);			
			}
					
		}catch(Exception e){
			m_mainMgr.m_logger.LogOut(GetAccountName() + " Exception:" + e.getMessage());
			m_mainMgr.m_logger.PrinterException(e);
		}
	}
	
	public void ImportWeibo(fetchWeibo _weibo,DirectMessage _dm)throws Exception{
		_weibo.SetId(_dm.getId());
		_weibo.SetDateLong(_dm.getCreatedAt().getTime());
		
		_weibo.SetText(replaceGFWVerified_URL(_dm.getText()));
		
		_weibo.SetOwnWeibo(_dm.getSenderId() == m_userself.getId());
		_weibo.SetReplyWeiboId(_dm.getRecipientId());
		
		_weibo.SetWeiboStyle(fetchWeibo.TWITTER_WEIBO_STYLE);
		_weibo.SetWeiboClass(fetchWeibo.DIRECT_MESSAGE_CLASS);
		
		User t_user = _dm.getSender();
		if(t_user != null){
			_weibo.SetUserId(t_user.getId());
			_weibo.SetUserName(t_user.getName());
			_weibo.SetUserScreenName(t_user.getScreenName());
			_weibo.SetSinaVIP(t_user.isVerified());	
		}
		
		String t_imageURL = t_user.getProfileImageURL();
		if(t_imageURL != null && t_imageURL.length() != 0){
			_weibo.SetUserHeadImageHashCode(StoreHeadImage(new URL(t_user.getProfileImageURL()),Long.toString(t_user.getId())));
		}		
	}
	
	final static String[]		fsm_GFWVerifiedShortURLSrv=
	{
		"http://t.co/",
		"http://bit.ly/",
		"http://j.mp/",
		"http://ff.im/",
	};
	
	/**
	 * replace the http://t.co/xxxx short URL to other shorter URL
	 * 
	 * @param _weiboText weibo text
	 * @return
	 */
	public String replaceGFWVerified_URL(String _weiboText){
		
		for(String srv : fsm_GFWVerifiedShortURLSrv){
			
			int t_beginIdx = 0;
			
			while(true){
				
				int t_idx;
				if((t_idx = _weiboText.indexOf(srv,t_beginIdx)) != -1){
					
					StringBuffer t_shortURL = new StringBuffer(srv);
					
					for(int i = t_idx + srv.length();i < _weiboText.length();i++){
						
						char c = _weiboText.charAt(i);
						if(Character.isLetterOrDigit(c)){
							t_shortURL.append(c);
						}else{
							break;
						}
					}
					
					if(t_shortURL.length() != srv.length()){
						
						try{
							String t_originalURL 		= expandShortURL(t_shortURL.toString());
							String t_replaceShortURL	= fetchMgr.GetShortURL(t_originalURL);
							
							// replace it
							//
							_weiboText = _weiboText.replace(t_shortURL, t_replaceShortURL);
							
							t_beginIdx = t_idx + t_shortURL.length() + (t_replaceShortURL.length() - t_shortURL.length());
							
						}catch(Exception e){
							m_mainMgr.m_logger.PrinterException(e);
							break;
						}
						
					}else{
						
						t_beginIdx = t_idx + srv.length();
					}
					
				}else{
					break;
				}
				
				
			}
		}
		
		return _weiboText;		
	}
	
	/**
	 * recovery the original shorted URL
	 * 
	 * @param address
	 * @return
	 * @throws IOException
	 */
	private static String expandShortURL(String address) throws IOException {
		URL t_url = new URL(address);

		HttpURLConnection connection = (HttpURLConnection) t_url.openConnection(); //using proxy may increase latency
		connection.setConnectTimeout(5000);
		connection.setReadTimeout(10000);
		connection.setInstanceFollowRedirects(false);
		connection.connect();
		
		String expandedURL = connection.getHeaderField("Location");
		connection.getInputStream().close();
		
		return expandedURL;
	}
	
	
	public RequestToken getRequestToken()throws Exception{	
		m_twitter.setOAuthConsumer(fsm_consumerKey, fsm_consumerSecret);
		return m_twitter.getOAuthRequestToken();
	}
	
	public RequestToken getRequestToken(String _callback)throws Exception{	
		m_twitter.setOAuthConsumer(fsm_consumerKey, fsm_consumerSecret);
		return m_twitter.getOAuthRequestToken(_callback);
	}
	
	public Twitter getTwitter(){
		m_twitter.setOAuthConsumer(fsm_consumerKey, fsm_consumerSecret);
		return m_twitter;
	}
	
	static public void main(String[] _arg)throws Exception{
		
		System.setProperty("proxySet", "true");
		System.setProperty("proxyHost", "127.0.0.1");
		System.setProperty("proxyPort", "8580");
		
		fetchMgr t_manger = new fetchMgr();
		Logger t_logger = new Logger("");
		
		t_logger.EnabelSystemOut(true);
		t_manger.InitConnect("",t_logger);
		
		fetchTWeibo t_weibo = new fetchTWeibo(t_manger);		
		
		t_weibo.m_twitter.setOAuthConsumer(fsm_consumerKey, fsm_consumerSecret);
		
		t_weibo.m_accessToken = "123158821-XD2V5L2W4ylntDS6orwfPKBEkqoJgcGHdvhMvnAq";
		t_weibo.m_secretToken = "sM9M4F14ozghV7SZ1dzrl3iFiCYCkzQMOs7RXOa8rs";
		
		t_weibo.ResetSession(true);
		
		t_weibo.m_atMeMessage.m_sum = 10;
		t_weibo.m_timeline.m_sum = 10;
		t_weibo.m_directMessage.m_sum = 5;
		
		t_weibo.CheckTimeline();
	}
	
	
}
