package com.yuchting.yuchberry.client;

import java.io.InputStream;
import java.io.OutputStream;

public class fetchWeibo {
	
	final static int	VERSION = 1;
	final public static int	SINA_WEIBO_STYLE 		= 0;
	
	final public static int	TIMELINE_CLASS 			= 0;
	final public static int	DIRECT_MESSAGE_CLASS 	= 1;
	final public static int	AT_ME_CLASS 			= 2;
	final public static int	COMMENT_ME_CLASS 		= 3;
	
	final public static int	fsm_headImageSize		= 32;
	
	int		m_WeiboStyle;
	int		m_WeiboClass;
	
	long	m_id;
	long	m_userId;
	
	boolean m_isSinaVIP;
	boolean m_isBBer;
	
	int		m_userHeadImageHashCode = 0;
	
	String	m_userName	= new String();
	String	m_text		= new String();
	
	long	m_dateTime 	= 0;
	
	long 	m_commentWeiboId = -1;
	fetchWeibo	m_commentWeibo = null;
	
	long 	m_replyWeiboId = -1;
	fetchWeibo	m_replyWeibo = null;
		
	public fetchWeibo(){
		
	}
	
	
	public int GetWeiboStyle(){return m_WeiboStyle;}
	public void SetWeiboStyle(int _style){m_WeiboStyle = _style;}
	
	public int GetWeiboClass(){return m_WeiboClass;}
	public void SetWeiboClass(int _style){m_WeiboClass = _style;}
	
	public long GetId(){return m_id;}
	public void SetId(final long _id){m_id = _id;}
	
	public long GetUserId(){return m_userId;}
	public void SetUserId(final long _id){m_userId = _id;}
	
	public long GetUserHeadImageHashCode(){return m_userHeadImageHashCode;}
	public void SetUserHeadImageHashCode(final int _hashCode){m_userHeadImageHashCode = _hashCode;}	
	
	public String GetUserName(){return m_userName;}
	public void SetUserName(final String _name){m_userName = _name;}
	
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
	
	
	public void OutputWeibo(OutputStream _stream)throws Exception{
		
		_stream.write(VERSION);
		
		_stream.write(m_WeiboStyle);
		sendReceive.WriteInt(_stream,m_WeiboClass);
		
		_stream.write(m_isSinaVIP?1:0);
		_stream.write(m_isBBer?1:0);
		
		sendReceive.WriteLong(_stream,m_id);
		sendReceive.WriteLong(_stream,m_userId);
		
		sendReceive.WriteString(_stream,m_userName);
		sendReceive.WriteString(_stream,m_text);
		
		sendReceive.WriteInt(_stream,m_userHeadImageHashCode);
		
		sendReceive.WriteLong(_stream,m_dateTime);
		sendReceive.WriteLong(_stream,m_commentWeiboId);
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
		
		m_WeiboStyle= _stream.read();
		m_WeiboClass= sendReceive.ReadInt(_stream);
		m_isSinaVIP = _stream.read() == 1?true:false;
		m_isBBer	= _stream.read() == 1?true:false;
		
		m_id		= sendReceive.ReadLong(_stream);
		m_userId	= sendReceive.ReadLong(_stream);
		m_userName	= sendReceive.ReadString(_stream);
		m_text		= sendReceive.ReadString(_stream);
		
		m_userHeadImageHashCode = sendReceive.ReadInt(_stream);
	
		m_dateTime	= sendReceive.ReadLong(_stream);
		
		m_commentWeiboId	= sendReceive.ReadLong(_stream);
		if(m_commentWeiboId != -1){
			if(_stream.read() == 1){
				m_commentWeibo = new fetchWeibo();
				m_commentWeibo.InputWeibo(_stream);
			}			
		}
		
		m_replyWeiboId	= sendReceive.ReadLong(_stream);
		if(m_replyWeiboId != -1){
			if(_stream.read() == 1){
				m_replyWeibo = new fetchWeibo();
				m_replyWeibo.InputWeibo(_stream);
			}			
		}
	}
}