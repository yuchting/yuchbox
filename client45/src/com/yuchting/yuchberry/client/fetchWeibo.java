package com.yuchting.yuchberry.client;

import java.io.InputStream;
import java.io.OutputStream;


public class fetchWeibo {
	
	final static int	VERSION = 1;
	final public static int	SINA_WEIBO = 0;
	
	int		m_WeiboStyle;
	
	long	m_id;
	long	m_userId;
	
	String	m_userName	= new String();
	String	m_text		= new String();
	
	long	m_dateTime 	= 0;
	
	long 	m_commentWeiboId = 0;
	fetchWeibo	m_commentWeibo = null;
	
	long 	m_replyWeiboId = 0;
	fetchWeibo	m_replyWeibo = null;
	
	private boolean m_convertoSimpleChar = false;
	
	public fetchWeibo(boolean _convertToSimple){
		m_convertoSimpleChar = _convertToSimple;
	}
	
	public int GetWeiboStyle(){return m_WeiboStyle;}
	public void SetWeiboStyle(int _style){m_WeiboStyle = _style;}
	
	public long GetId(){return m_id;}
	public void SetId(final long _id){m_id = _id;}
	
	public long GetUserId(){return m_userId;}
	public void SetUserId(final long _id){m_userId = _id;}
	
	public String GetUserName(){return m_userName;}
	public void SetUserName(final String _name){m_userName = _name;}
	
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
		
		sendReceive.WriteLong(_stream,m_id);
		sendReceive.WriteLong(_stream,m_userId);
		sendReceive.WriteString(_stream,m_userName);
		sendReceive.WriteString(_stream,m_text);
		
		sendReceive.WriteLong(_stream,m_dateTime);
		sendReceive.WriteLong(_stream,m_commentWeiboId);
		if(m_commentWeiboId != 0){
			if(m_commentWeibo == null){
				throw new Exception("Comment Weibo can't be null");
			}
			
			m_commentWeibo.OutputWeibo(_stream);
		}
		
		sendReceive.WriteLong(_stream,m_replyWeiboId);
		if(m_replyWeiboId != 0){
			if(m_replyWeibo == null){
				throw new Exception("Reply Weibo can't be null");
			}
			
			m_replyWeibo.OutputWeibo(_stream);
		}
		
	}
	
	public void InputWeibo(InputStream _stream)throws Exception{
		
		final int t_version = _stream.read();
		
		m_id		= sendReceive.ReadLong(_stream);
		m_userId	= sendReceive.ReadLong(_stream);	
		m_userName	= sendReceive.ReadString(_stream);
		m_text		= sendReceive.ReadString(_stream);
	
		m_dateTime	= sendReceive.ReadLong(_stream);
		
		m_commentWeiboId	= sendReceive.ReadLong(_stream);
		if(m_commentWeiboId != 0){
			m_commentWeibo = new fetchWeibo(m_convertoSimpleChar);
			m_commentWeibo.InputWeibo(_stream);
		}
		
		m_replyWeiboId	= sendReceive.ReadLong(_stream);
		if(m_replyWeiboId != 0){
			m_replyWeibo = new fetchWeibo(m_convertoSimpleChar);
			m_replyWeibo.InputWeibo(_stream);
		}
	}
}
