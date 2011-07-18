package com.yuchting.yuchberry.server;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

public class fetchWeiboUser {
	
	public final static int VERSION = 0;
	
	byte m_weiboUserStyle = fetchWeibo.SINA_WEIBO_STYLE;
	byte m_gender = 0;
	
	long	m_id = 0;
	String m_name = "";
	String m_screenName = "";
	String m_headURL = "";
	String m_city = "";
	
	String m_desc = "";
	boolean m_isVerified = false;
	boolean m_hasBeenFollowed = false;
	boolean m_isMyFans = false;
	
	int m_followNum = 0;
	int m_fansNum = 0;
	
	int m_weiboNum = 0;	
	
	Vector	m_updateWeibo = new Vector();
		
	boolean m_convertToSimple = false;
	
	public fetchWeiboUser(byte _style,boolean _convet){
		m_weiboUserStyle = _style;
		m_convertToSimple = _convet;
	}	
	
	public long getId(){return m_id;}
	public void setId(long _id){m_id = _id;}
	
	public byte getStyle(){return m_weiboUserStyle;}
	public void setStyle(byte _style){m_weiboUserStyle = _style;}
	
	public byte getGender(){return m_gender;}
	public void setGender(byte _gender){m_gender = _gender;}
	
	public String getName(){return m_name;}
	public void setName(String _name){m_name = _name;}
	
	public String getScreenName(){return m_screenName;}
	public void setScreenName(String _screenName){m_screenName = _screenName;}
	
	public String getHeadURL(){return m_headURL;}
	public void setHeadURL(String _URL){m_headURL = _URL;}
	
	public String getCity(){return m_city;}
	public void setCity(String _city){m_city = _city;}
	
	public String getDesc(){return m_desc;}
	public void setDesc(String _desc){m_desc = _desc;}
	
	public boolean isVerified(){return m_isVerified;}
	public void setVerified(boolean _verified){m_isVerified = _verified;}
	
	public boolean hasBeenFollowed(){return m_hasBeenFollowed;}
	public void setHasBeenFollowed(boolean _followed){m_hasBeenFollowed = _followed;}
	
	public boolean isMyFans(){return m_isMyFans;}
	public void setIsMyFans(boolean _isMyFans){m_isMyFans = _isMyFans;}
	
	public int getFollowNum(){return m_followNum;}
	public void setFollowNum(int _num){m_followNum = _num;}
	
	public int getFansNum(){return m_fansNum;}
	public void setFansNum(int _num){m_fansNum = _num;}
	
	public int getWeiboNum(){return m_weiboNum;}
	public void setWeiboNum(int _num){m_weiboNum = _num;}
	
	public Vector getUpdatedWeibo(){return m_updateWeibo;}
	
	public void InputData(InputStream in)throws Exception{
		int t_version = in.read();
		
		m_weiboUserStyle	= (byte)in.read();
		m_gender			= (byte)in.read();
		
		m_id				= sendReceive.ReadLong(in);
		m_name				= sendReceive.ReadString(in);
		m_screenName		= sendReceive.ReadString(in);
		m_headURL			= sendReceive.ReadString(in);
		m_city				= sendReceive.ReadString(in);
		
		m_desc				= sendReceive.ReadString(in);
		m_isVerified		= sendReceive.ReadBoolean(in);
		m_hasBeenFollowed	= sendReceive.ReadBoolean(in);
		m_isMyFans			= sendReceive.ReadBoolean(in);
		
		m_followNum			= sendReceive.ReadInt(in);
		m_fansNum			= sendReceive.ReadInt(in);
		m_weiboNum			= sendReceive.ReadInt(in);
		
		int t_appendWeiboNum = sendReceive.ReadInt(in);
		for(int i = 0;i < t_appendWeiboNum;i++){
			fetchWeibo t_weibo = new fetchWeibo(m_convertToSimple);
			t_weibo.InputWeibo(in);
			
			m_updateWeibo.addElement(t_weibo);			
		}
	}
	
	public void OutputData(OutputStream os)throws Exception{
		os.write(VERSION);
		
		os.write(m_weiboUserStyle);
		os.write(m_gender);
		
		sendReceive.WriteLong(os,m_id);
		sendReceive.WriteString(os,m_name,m_convertToSimple);
		sendReceive.WriteString(os,m_screenName,m_convertToSimple);
		sendReceive.WriteString(os,m_headURL,m_convertToSimple);
		sendReceive.WriteString(os,m_city,m_convertToSimple);
		
		sendReceive.WriteString(os,m_desc,m_convertToSimple);
		sendReceive.WriteBoolean(os, m_isVerified);
		sendReceive.WriteBoolean(os,m_hasBeenFollowed);
		sendReceive.WriteBoolean(os, m_isMyFans);		
		
		sendReceive.WriteInt(os,m_followNum);
		sendReceive.WriteInt(os,m_fansNum);
		sendReceive.WriteInt(os,m_weiboNum);
		
		sendReceive.WriteInt(os,m_updateWeibo.size());
		for(int i = 0;i < m_updateWeibo.size();i++){
			fetchWeibo t_weibo = (fetchWeibo)m_updateWeibo.elementAt(i);
			
			t_weibo.OutputWeibo(os);
		}
	}
}
