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
package com.yuchting.yuchberry.client.weibo;

import java.io.InputStream;
import java.io.OutputStream;

import local.yblocalResource;

import com.yuchting.yuchberry.client.GPSInfo;
import com.yuchting.yuchberry.client.recvMain;
import com.yuchting.yuchberry.client.sendReceive;

public class fetchWeibo {
	
	final static int	VERSION = 4;
	
	final public static byte	SINA_WEIBO_STYLE 		= 0;
	final public static byte	TWITTER_WEIBO_STYLE 	= 1;
	final public static byte	QQ_WEIBO_STYLE 			= 2;
	final public static byte	N163_WEIBO_STYLE 		= 3;
	final public static byte	SOHU_WEIBO_STYLE 		= 4;
	final public static byte	FANFOU_WEIBO_STYLE 		= 5;
	
	final public static byte	TIMELINE_CLASS 			= 0;
	final public static byte	DIRECT_MESSAGE_CLASS 	= 1;
	final public static byte	AT_ME_CLASS 			= 2;
	final public static byte	COMMENT_ME_CLASS 		= 3;
	
	final public static byte	SEND_NEW_UPDATE_TYPE	= 0;
	final public static byte	SEND_FORWARD_TYPE		= 1;
	final public static byte	SEND_REPLY_TYPE			= 2;
	final public static byte	SEND_DIRECT_MSG_TYPE	= 3;
	
	final public static byte	IMAGE_TYPE_JPG			= 0;
	final public static byte	IMAGE_TYPE_GIF			= 1;
	final public static byte	IMAGE_TYPE_PNG			= 2;
	final public static byte	IMAGE_TYPE_BMP			= 3;
		
	final public static int	fsm_headImageSize		= 32;
	final public static int	fsm_headImageSize_l		= 50;
	
	byte		m_WeiboStyle;
	byte		m_WeiboClass;
	
	boolean	m_isOwnWeibo = false;
	
	long		m_id;
	long		m_userId;
	
	boolean 	m_isSinaVIP;
	boolean 	m_isBBer;
	
	int			m_userHeadImageHashCode = 0;
	
	String		m_userName	= "";
	String		m_screenName = "";
	String		m_text		= "";
	
	String		m_original_pic = "";
	
	long		m_dateTime 	= 0;
	
	long 		m_commentWeiboId = -1;
	fetchWeibo	m_commentWeibo = null;
	
	long 		m_replyWeiboId = -1;
	fetchWeibo	m_replyWeibo = null;
	
	String		m_replyName = ""; // add for QQ direct weibo  ...Orz...
	
	int			m_forwardWeiboNum = 0;
	int			m_commentWeiboNum = 0;
	
	String		m_source	= "";
	
	boolean 	m_hasLocationInfo		= false;
	GPSInfo		m_gpsInfo = new GPSInfo();

	boolean	m_userFollowing = false;
	boolean	m_userFollow_me = false;
		
	public fetchWeibo(){}
	
	public boolean equals(fetchWeibo _weibo){
		
		if(m_id != 0){
			
			// client receive compare
			//
			return m_WeiboStyle == _weibo.m_WeiboStyle 
					&& m_WeiboClass == _weibo.m_WeiboClass 
					&& m_id == _weibo.m_id;	
		}else{
			
			// client send compare
			//
			return m_userName.equals(_weibo.m_userName) 
					&& m_text.equals(_weibo.m_text) 
					&& m_dateTime == _weibo.m_dateTime;
		}
	}
	
	public String GetHeadImageId(){
		return m_WeiboStyle == fetchWeibo.QQ_WEIBO_STYLE ? 
				GetUserScreenName():Long.toString(GetUserId());
	}
	
	public byte GetWeiboStyle(){return m_WeiboStyle;}
	public void SetWeiboStyle(byte _style){m_WeiboStyle = _style;}
	
	public byte GetWeiboClass(){return m_WeiboClass;}
	public void SetWeiboClass(byte _style){m_WeiboClass = _style;}
	
	public boolean IsOwnWeibo(){return m_isOwnWeibo;}
	public void SetOwnWeibo(boolean _own){m_isOwnWeibo = _own;}
	
	public long GetId(){return m_id;}
	public void SetId(final long _id){m_id = _id;}
	
	public long GetUserId(){return m_userId;}
	public void SetUserId(final long _id){m_userId = _id;}
	
	public int GetUserHeadImageHashCode(){return m_userHeadImageHashCode;}
	public void SetUserHeadImageHashCode(final int _hashCode){m_userHeadImageHashCode = _hashCode;}	
	
	public String GetUserName(){return m_userName;}
	public void SetUserName(final String _name){m_userName = _name;}
	
	public String GetUserScreenName(){return m_screenName;}
	public void SetUserScreenName(final String _name){m_screenName = _name;}
	
	public boolean IsSinaVIP(){return m_isSinaVIP;}
	public void SetSinaVIP(boolean _isVIP){m_isSinaVIP = _isVIP;}
	
	public boolean IsBBer(){return m_isBBer;}
	public void SetBBer(boolean _bber){m_isBBer = _bber;}
	
	public String GetText(){return m_text;}
	public void SetText(final String _text){m_text = _text;}
	
	public long GetDateTime(){return m_dateTime;}
	public void SetDateLong(final long _dateTime){m_dateTime = _dateTime;}
	
	public long GetCommentWeiboId(){return m_commentWeiboId;}
	public void SetCommectWeiboId(final long _id){m_commentWeiboId = _id;}
	
	public fetchWeibo GetCommentWeibo(){return m_commentWeibo;}
	public void SetCommectWeibo(fetchWeibo _weibo){m_commentWeibo = _weibo;}
	
	public long GetReplyWeiboId(){return m_replyWeiboId;}
	public void SetReplyWeiboId(final long _id){m_replyWeiboId = _id;}
	
	public fetchWeibo GetReplyWeibo(){return m_replyWeibo;}
	public void SetReplyWeibo(fetchWeibo _weibo){m_replyWeibo = _weibo;}
	
	public String getReplyName(){return m_replyName;}
	public void setReplyName(String _name){m_replyName = _name;}
	
	public String GetSource(){return m_source;}
	public void SetSource(String _source){m_source = _source;}
	
	public int GetForwardWeiboNum(){return m_forwardWeiboNum;}
	public void SetForwardWeiboNum(int _num){m_forwardWeiboNum = _num;}
	
	public int GetCommentWeiboNum(){return m_commentWeiboNum;}
	public void SetCommentWeiboNum(int _num){m_commentWeiboNum = _num;}
	
	public String GetOriginalPic(){return m_original_pic;}
	public void SetOriginalPic(String _pic){m_original_pic = _pic;}
	
	public GPSInfo GetGPSInfo(){return m_gpsInfo;}
	
	public void EnableGPSInfo(boolean _enable){m_hasLocationInfo = _enable;}
	
	public void setUserFollowing(boolean _following){m_userFollowing = _following;}
	public boolean isUserFollowing(){return m_userFollowing;}
	
	public void setUserFollowMe(boolean _following){m_userFollow_me = _following;}
	public boolean isUserFollowMe(){return m_userFollow_me;}
		
	public void OutputWeibo(OutputStream _stream)throws Exception{
		
		_stream.write(VERSION);
		
		_stream.write(m_WeiboStyle);
		_stream.write(m_WeiboClass);
		
		sendReceive.WriteBoolean(_stream,m_isOwnWeibo);
		sendReceive.WriteBoolean(_stream,m_isSinaVIP);
		sendReceive.WriteBoolean(_stream,m_isBBer);
		
		sendReceive.WriteLong(_stream,m_id);
		sendReceive.WriteLong(_stream,m_userId);
		sendReceive.WriteString(_stream,m_userName);
		sendReceive.WriteString(_stream,m_screenName);
		sendReceive.WriteString(_stream,m_text);
		
		sendReceive.WriteInt(_stream,m_userHeadImageHashCode);
		
		sendReceive.WriteLong(_stream,m_dateTime);
		sendReceive.WriteLong(_stream,m_commentWeiboId);
		
		sendReceive.WriteString(_stream,m_source);
		sendReceive.WriteString(_stream,m_original_pic);
		
		sendReceive.WriteInt(_stream,m_forwardWeiboNum);
		sendReceive.WriteInt(_stream,m_commentWeiboNum);
		
		if(m_commentWeiboId != -1){
			_stream.write(m_commentWeibo != null?1:0);
			if(m_commentWeibo != null){				
				m_commentWeibo.OutputWeibo(_stream);
			}			
		}
		
		sendReceive.WriteLong(_stream,m_replyWeiboId);
		if(m_replyWeiboId != -1){
			_stream.write(m_replyWeibo != null?1:0);
			if(m_replyWeibo != null){
				m_replyWeibo.OutputWeibo(_stream);
			}			
		}
		
		sendReceive.WriteString(_stream,m_replyName);
		sendReceive.WriteBoolean(_stream, m_hasLocationInfo);
		if(m_hasLocationInfo){
			m_gpsInfo.OutputData(_stream);
		}
		
		sendReceive.WriteBoolean(_stream,m_userFollowing);
		sendReceive.WriteBoolean(_stream,m_userFollow_me);
	}
	
	public void InputWeibo(InputStream _stream)throws Exception{
		
		final int t_version = _stream.read();
		
		m_WeiboStyle= (byte)_stream.read();
		m_WeiboClass= (byte)_stream.read();
		
		m_isOwnWeibo= sendReceive.ReadBoolean(_stream);
		m_isSinaVIP = sendReceive.ReadBoolean(_stream);
		m_isBBer	= sendReceive.ReadBoolean(_stream);
		
		m_id		= sendReceive.ReadLong(_stream);
		m_userId	= sendReceive.ReadLong(_stream);
		m_userName	= sendReceive.ReadString(_stream);
		m_screenName= sendReceive.ReadString(_stream);
		m_text		= sendReceive.ReadString(_stream);
		
		m_userHeadImageHashCode = sendReceive.ReadInt(_stream);
		m_dateTime				= sendReceive.ReadLong(_stream);
		m_commentWeiboId		= sendReceive.ReadLong(_stream);
		
		m_source				= sendReceive.ReadString(_stream);
		m_original_pic			= sendReceive.ReadString(_stream);
		
		m_forwardWeiboNum		= sendReceive.ReadInt(_stream);
		m_commentWeiboNum		= sendReceive.ReadInt(_stream);
				
		if(m_commentWeiboId != -1){
			if(sendReceive.ReadBoolean(_stream)){
				m_commentWeibo = new fetchWeibo();
				m_commentWeibo.InputWeibo(_stream);
			}			
		}else{
			m_commentWeibo = null;
		}
		
		m_replyWeiboId	= sendReceive.ReadLong(_stream);
		if(m_replyWeiboId != -1){
			if(sendReceive.ReadBoolean(_stream)){
				m_replyWeibo = new fetchWeibo();
				m_replyWeibo.InputWeibo(_stream);
			}			
		}else{
			m_replyWeibo = null;
		}
		
		if(t_version >= 2){
			m_replyName = sendReceive.ReadString(_stream);
		}
		
		if(t_version >= 3){
			m_hasLocationInfo = sendReceive.ReadBoolean(_stream);
			if(m_hasLocationInfo){
				m_gpsInfo.InputData(_stream);
			}
		}
		
		if(t_version >= 4){
			m_userFollowing = sendReceive.ReadBoolean(_stream);
			m_userFollow_me = sendReceive.ReadBoolean(_stream);
		}		
	}
	
	public String getForwardPrefix(){
		switch(GetWeiboStyle()){
		case TWITTER_WEIBO_STYLE:
			return " RT ";
		case QQ_WEIBO_STYLE:
			return " || ";
		}
		
		return " // ";
	}
	
	public String getLocalStyleName(){
		return getLocalStyleName(GetWeiboStyle());
	}
	
	public static String getLocalStyleName(int _style){
		
		String t_style = "Weibo";
		
		switch(_style){
		case fetchWeibo.QQ_WEIBO_STYLE:
			t_style = recvMain.sm_local.getString(yblocalResource.WEIBO_QQ_STYLE);
			break;
		case fetchWeibo.SINA_WEIBO_STYLE:
			t_style = recvMain.sm_local.getString(yblocalResource.WEIBO_SINA_STYLE);
			break;
		case fetchWeibo.TWITTER_WEIBO_STYLE:
			t_style = recvMain.sm_local.getString(yblocalResource.WEIBO_TWITTER_STYLE);
			break;
		}
		
		return t_style;
	}
	
	public String getShareEmailContain(String _spaceLine){
		
		StringBuffer t_content = new StringBuffer(_spaceLine);
		String t_name;
		
		if(!GetUserScreenName().equals(GetUserName())){
			t_name = GetUserScreenName() + " [" + GetUserName() + "]";
		}else{
			t_name = GetUserScreenName();
		}
				
		t_content.append("@").append(t_name).append(" ").append("(").append(getLocalStyleName()).append(")").append("\n").append(_spaceLine)
				.append(GetText()).append("\n").append(_spaceLine);
				
		if(GetOriginalPic().length() != 0){
			t_content.append(GetOriginalPic()).append("\n").append(_spaceLine);
		}
				
		t_content.append(recvMain.sm_local.getString(yblocalResource.WEIBO_SOURCE_PREFIX))
				.append(GetSource());
		
		if(m_commentWeibo != null){
			t_content.append("\n\n").append(_spaceLine).append(m_commentWeibo.getShareEmailContain(_spaceLine + " "));
		}
		
		return t_content.toString();
	}
		
	public String getShareEmailSubject(){
		return recvMain.sm_local.getString(yblocalResource.WEIBO_EMAIL_SHARE) + getLocalStyleName();
	}
}
