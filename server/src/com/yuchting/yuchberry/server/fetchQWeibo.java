package com.yuchting.yuchberry.server;

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.dom4j.Element;

import com.mime.qweibo.QDirectMessage;
import com.mime.qweibo.QUser;
import com.mime.qweibo.QWeibo;
import com.mime.qweibo.QWeiboSyncApi;

public class fetchQWeibo extends fetchAbsWeibo{
	
	final static String		fsm_consumerKey 	= "06d0a334755146d9b3dfe024e205f36b";
	final static String		fsm_consumerSecret	= "dcc64862deec1a57e98b6985c405e369";
		
	
	public QWeiboSyncApi m_api = new QWeiboSyncApi();
		
	QUser		m_userself = null;
	
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
		List<QWeibo> t_fetch = null;
		if(m_timeline.m_fromIndex > 1){
			t_fetch = m_api.getHomeList(m_timeline.m_fromIndex, 20);
		}else{
			t_fetch = m_api.getHomeList();
		}		 
		
		AddWeibo(t_fetch,m_timeline,fetchWeibo.TIMELINE_CLASS);
	}
	
	protected void CheckDirectMessage()throws Exception{
		List<QDirectMessage> t_fetch = null;
		
		if(m_directMessage.m_fromIndex > 1){
			t_fetch = m_api.getOutboxDirectMessage(m_directMessage.m_fromIndex,10,0);
		}else{
			t_fetch = m_api.getOutboxDirectMessage();
		}
		
		AddDirectMsgWeibo(t_fetch,m_directMessage,fetchWeibo.DIRECT_MESSAGE_CLASS);
		
		for(QDirectMessage msg : t_fetch){
			if(m_directMessage.m_fromIndex < msg.getWeiboContentItem().getTime()){
				m_directMessage.m_fromIndex = msg.getWeiboContentItem().getTime();
			}
		}		

		// inbox
		//
		if(m_directMessage.m_fromIndex2 > 1){
			t_fetch = m_api.getInboxDirectMessage(m_directMessage.m_fromIndex2,10,0);
		}else{
			t_fetch = m_api.getInboxDirectMessage();
		}
		
		AddDirectMsgWeibo(t_fetch,m_directMessage,fetchWeibo.DIRECT_MESSAGE_CLASS);
		
		for(QDirectMessage msg : t_fetch){
			if(m_directMessage.m_fromIndex2 < msg.getWeiboContentItem().getTime()){
				m_directMessage.m_fromIndex2 = msg.getWeiboContentItem().getTime();
			}
		}
				
		// sort by time
		//
		Collections.sort(m_directMessage.m_weiboList,new Comparator<fetchWeibo>() {
			
			public   int   compare(fetchWeibo o1, fetchWeibo o2){
				
				long a = o1.GetDateTime();
				long b = o2.GetDateTime();
				
				if(a < b){
					return 1;
				}else if(a > b){
					return -1;
				}else{
					return 0;
				}
			}
		});
		
	}
	
	private void AddDirectMsgWeibo(List<QDirectMessage> _from,fetchWeiboData _to,byte _class){
		
		boolean t_insert;
		
		for(QDirectMessage fetchOne : _from){

			t_insert = true;
			
			for(fetchWeibo weibo : _to.m_weiboList){
				if(weibo.GetId() == fetchOne.getWeiboContentItem().getId()){
					t_insert = false;
					break;
				}
			}
			
			if(t_insert){
				for(fetchWeibo weibo : _to.m_WeiboComfirm){
					if(weibo.GetId() == fetchOne.getWeiboContentItem().getId()){
						t_insert = false;
						break;
					}
				}
			}
			
			if(t_insert){
				fetchWeibo t_weibo = new fetchWeibo(m_mainMgr.m_convertToSimpleChar);
				ImportWeibo(t_weibo, fetchOne.getWeiboContentItem(),_class);
				
				t_weibo.SetOwnWeibo(fetchOne.getDirectMessageType() == QDirectMessage.OUTBOX_TYPE);
				
				if(t_weibo.IsOwnWeibo()){
					t_weibo.setReplyName(fetchOne.getSendToScreenName());
				}else{
					t_weibo.setReplyName(m_userself.getScreenName());
				}				
				
				_to.m_weiboList.add(t_weibo);
			}
		}
	}
	
	protected void CheckAtMeMessage()throws Exception{
		
		List<QWeibo> t_fetch = null;
		if(m_atMeMessage.m_fromIndex > 1){
			t_fetch = m_api.getMentionList(m_atMeMessage.m_fromIndex, 20);
		}else{
			t_fetch = m_api.getMentionList();
		}		 
		
		AddWeibo(t_fetch,m_atMeMessage,fetchWeibo.AT_ME_CLASS);
	}
	
	protected void CheckCommentMeMessage()throws Exception{
		// nothing about this list
	}

	protected void DeleteWeibo(long _id,boolean _isComment)throws Exception{
		m_api.deleteMessage(_id);
	}
	
	
	
	/**
	 * reset the session for connection
	 * 
	 * @param _fullTest		: whether test the full configure( SMTP for email)
	 */
	public void ResetSession(boolean _fullTest)throws Exception{
				
		m_api.setAccessToken(m_accessToken, m_secretToken);		
		m_userself = m_api.verifyCredentials();
		
		ResetCheckFolderLimit();
		
		m_mainMgr.m_logger.LogOut("Weibo Account<" + GetAccountName() + "> Prepare OK!");
		
	}
	
	protected void ResetCheckFolderLimit()throws Exception{
		m_currRemainCheckFolderNum = 100;
		m_maxCheckFolderNum			= 100;
	}
	
	/**
	 * destroy the session connection
	 */
	public void DestroySession(){
		
	}	
	
	protected void UpdateStatus(String _text,GPSInfo _info)throws Exception{
		if(_info != null && _info.m_latitude != 0 && _info.m_longitude != 0){
			m_api.publishMsg(_text, _info.m_longitude, _info.m_latitude);
		}else{
			m_api.publishMsg(_text);
		}
	}
	
	protected void UpdateComment(int _style,String _text,long _orgWeiboId,
						GPSInfo _info,boolean _updateTimeline)throws Exception{
		
		if(_style == GetCurrWeiboStyle()){
			
			if(_info != null && _info.m_latitude != 0 && _info.m_longitude != 0){
				m_api.commentMsg(_text, _orgWeiboId, _info.m_longitude, _info.m_latitude);
			}else{
				m_api.commentMsg(_text, _orgWeiboId);
			}
			
			if(_updateTimeline){

				if(_info != null && _info.m_latitude != 0 && _info.m_longitude != 0){
					m_api.publishMsg(_text, _info.m_longitude, _info.m_latitude);
				}else{
					m_api.publishMsg(_text);
				}	
			}
			
		}else{
					
			if(_info != null && _info.m_latitude != 0 && _info.m_longitude != 0){
				m_api.publishMsg(_text, _info.m_longitude, _info.m_latitude);
			}else{
				m_api.publishMsg(_text);
			}
			
		}			
	}
	
	protected void UpdateReply(String _text,long _commentWeiboId,long _orgWeiboId,
			GPSInfo _info,boolean _updateTimeline)throws Exception{
		UpdateComment(GetCurrWeiboStyle(),_text,_orgWeiboId,_info,_updateTimeline);
	}
	
	protected void FavoriteWeibo(long _id)throws Exception{
		m_api.favoriteMessage(_id);
	}

	protected void FollowUser(String _screenName)throws Exception{
		m_api.followUser(_screenName);
	}
	
	protected fetchWeiboUser getWeiboUser(String _name)throws Exception{
		QUser t_user = m_api.getUserInfo(_name);
		
		fetchWeiboUser t_weibo = new fetchWeiboUser(m_mainMgr.m_convertToSimpleChar);
		
		t_weibo.setStyle(fetchWeibo.QQ_WEIBO_STYLE);
		t_weibo.setId(0);
		
		t_weibo.setName(t_user.getName());
		t_weibo.setScreenName(t_user.getScreenName());
		t_weibo.setHeadImage(DownloadHeadImage(new URL(t_user.getHeadImageURL()),t_user.getName()));
		t_weibo.setDesc(t_user.getIntroduction());
		t_weibo.setCity(t_user.getLocation());
		t_weibo.setVerified(t_user.isVerified());
		t_weibo.setHasBeenFollowed(t_user.hasBeenFollowed());
		t_weibo.setIsMyFans(t_user.isMyFans());
		t_weibo.setFollowNum(t_user.getIdolNum());
		t_weibo.setFansNum(t_user.getFansNum());
		t_weibo.setWeiboNum(t_user.getWeiboNum());
		
		List<QWeibo> t_list = m_api.getUserTimeline(_name);
		for(QWeibo s:t_list){
			fetchWeibo weibo = new fetchWeibo(m_mainMgr.m_convertToSimpleChar);
			ImportWeibo(weibo, s,fetchWeibo.TIMELINE_CLASS);
			
			t_weibo.getUpdatedWeibo().add(weibo);
		}
		
		return t_weibo;
	}
	
	
	
	private void AddWeibo(List<QWeibo> _from,fetchWeiboData _to,byte _class){
		
		boolean t_insert;
		
		for(QWeibo fetchOne : _from){
			
			if(_to.m_fromIndex >= fetchOne.getTime()){
				// directly return because of qq time fetching bug!
				//
				break;
			}
			
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
			QWeibo t_lashOne = _from.get(0);
			_to.m_fromIndex = t_lashOne.getTime() + 1;
		}
	}
	
	private void ImportWeibo(fetchWeibo _weibo,QWeibo _qweibo,byte _weiboClass){
		
		_weibo.SetId(_qweibo.getId());
		_weibo.SetDateLong(_qweibo.getTime());
		_weibo.SetText(_qweibo.getText());
		_weibo.SetSource(_qweibo.getSource());
		
		_weibo.SetWeiboStyle(fetchWeibo.QQ_WEIBO_STYLE);
		_weibo.SetWeiboClass(_weiboClass);
		
		_weibo.SetOwnWeibo(_qweibo.isOwnWeibo());
		
		_weibo.SetUserName(_qweibo.getNickName());
		_weibo.SetUserScreenName(_qweibo.getScreenName());
		
		_weibo.SetSinaVIP(_qweibo.isVIP());
		
		if(_qweibo.getImage() != null){
			_weibo.SetOriginalPic(_qweibo.getImage());
		}		
		
		if(_qweibo.getHeadImageURL().length() != 0){
			try{
				_weibo.SetUserHeadImageHashCode(StoreHeadImage(new URL(_qweibo.getHeadImageURL()),_qweibo.getNickName()));
			}catch(Exception e){
				m_mainMgr.m_logger.LogOut(GetAccountName() + " Exception:" + e.getMessage());
				m_mainMgr.m_logger.PrinterException(e);
			}				
		}
		
		if(_qweibo.getSourceWeibo() != null){
			
			fetchWeibo t_sourceWeibo = new fetchWeibo(m_mainMgr.m_convertToSimpleChar);
			ImportWeibo(t_sourceWeibo,_qweibo.getSourceWeibo(),_weiboClass);
			
			if(_qweibo.getType() == 4){
				_weibo.SetReplyWeiboId(t_sourceWeibo.GetId());
				_weibo.SetReplyWeibo(t_sourceWeibo);
				_weibo.setReplyName(t_sourceWeibo.GetUserScreenName());
			}else{
				_weibo.SetCommectWeiboId(t_sourceWeibo.GetId());
				_weibo.SetCommectWeibo(t_sourceWeibo);				
			}						
		}		
		
	}
	
	public static void main(String[] _arg)throws Exception{
		
		fetchMgr t_mgr = new fetchMgr();
		Logger t_logger = new Logger("");
		t_mgr.m_logger = t_logger;
		
		fetchQWeibo t_weibo = new fetchQWeibo(t_mgr);
		
		QWeiboSyncApi.sm_debug = true;
		
		t_weibo.m_api.setCostomerKey(fsm_consumerKey, fsm_consumerSecret);
		t_weibo.m_accessToken = "d7804a95bde0436ca9f0b62ef84de787";
		t_weibo.m_secretToken = "0a076b55a89ed75bada75b72d880a60b";
				
		t_weibo.ResetSession(true);
		
		t_weibo.m_atMeMessage.m_sum = 10;
		t_weibo.m_timeline.m_sum = 10;
		t_weibo.m_directMessage.m_sum = 5;
		
		t_weibo.CheckDirectMessage();
		
		System.out.print(t_weibo);
		
		
//		
//		List<QWeibo> t_list = t_weibo.m_api.getHomeList(1307720032001L,20);
//		
//		for(QWeibo weibo : t_list){
//			
//			System.out.println(Long.toString(weibo.getId()) + " @" + weibo.getScreenName() + "("+weibo.getNickName()+") " + weibo.getTime() +":"+ weibo.getText());
//			
//			if(weibo.getSourceWeibo() != null){
//				weibo = weibo.getSourceWeibo();
//				System.out.print("\t\tsource:");
//				System.out.print(Long.toString(weibo.getId()) + " @" + weibo.getScreenName() + "("+weibo.getNickName()+") :"+ weibo.getText());
//			}
//		}
		
//		List<QDirectMessage> t_dmInboxlist =  t_weibo.m_api.getInboxDirectMessage();
//		
//		for(QDirectMessage dm : t_dmInboxlist){
//			QWeibo weibo = dm.getWeiboContentItem();
//			
//			System.out.println(Long.toString(weibo.getId()) + " @" + weibo.getName() + "("+weibo.getNickName()+") :"+ weibo.getText());
//			
//			if(weibo.getSourceWeibo() != null){
//				weibo = weibo.getSourceWeibo();
//				System.out.print("\t\tsource:");
//				System.out.print(Long.toString(weibo.getId()) + " @" + weibo.getName() + "("+weibo.getNickName()+") :"+ weibo.getText());
//			}
//			
//		}
		
		//t_weibo.m_api.replyMsg("reply it",81005049499103L);
	
	}
	
	
	

}
