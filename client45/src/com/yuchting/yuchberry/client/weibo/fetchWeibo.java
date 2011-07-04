package com.yuchting.yuchberry.client.weibo;

import java.io.InputStream;
import java.io.OutputStream;

import com.yuchting.yuchberry.client.GPSInfo;
import com.yuchting.yuchberry.client.sendReceive;

public class fetchWeibo {
	
	final static int	VERSION = 1;
	
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
	
	int			m_forwardWeiboNum = 0;
	int			m_commentWeiboNum = 0;
	
	String		m_source	= "";
	
	boolean 	m_hasLocationInfo		= false;
	GPSInfo		m_gpsInfo = new GPSInfo();
		
	public fetchWeibo(){
		
	}
	
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
		
		if(m_hasLocationInfo){
			m_gpsInfo.OutputData(_stream);
		}
		
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
		
		if(m_hasLocationInfo){
			m_gpsInfo.InputData(_stream);
		}
		
		if(m_commentWeiboId != -1){
			if(sendReceive.ReadBoolean(_stream)){
				m_commentWeibo = new fetchWeibo();
				m_commentWeibo.InputWeibo(_stream);
			}			
		}
		
		m_replyWeiboId	= sendReceive.ReadLong(_stream);
		if(m_replyWeiboId != -1){
			if(sendReceive.ReadBoolean(_stream)){
				m_replyWeibo = new fetchWeibo();
				m_replyWeibo.InputWeibo(_stream);
			}			
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
}