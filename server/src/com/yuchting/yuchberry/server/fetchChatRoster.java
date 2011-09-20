package com.yuchting.yuchberry.server;

import java.io.InputStream;
import java.io.OutputStream;

import org.jivesoftware.smack.RosterEntry;

public class fetchChatRoster {
	
	public final static int	PRESENCE_AVAIL = 0;
	public final static int	PRESENCE_AWAY = 1;
	public final static int	PRESENCE_BUSY = 2;
	public final static int	PRESENCE_UNAVAIL = 3;
	public final static int	PRESENCE_FAR_AWAY = 4;
	
	
	int m_style		= fetchChatMsg.STYLE_GTALK;
	int m_presence	= PRESENCE_AVAIL;
	int m_headImageHashCode = 0; 
	
	String m_name		= "";
	String m_account	= "";
	String m_status		= "";
	String m_source 	= "";
			
	String m_ownAccount = "";
	
	// server using data
	//
	RosterEntry	m_smackRoster = null;
	
	public fetchChatRoster(){}
	
	public boolean equals(fetchChatRoster _roster){
		return m_style == _roster.m_style 
				&& m_account.equals(_roster.m_account) 
				&& m_ownAccount.equals(_roster.m_ownAccount);
	}
	
	public void copyFrom(fetchChatRoster _roster){
		m_style 				= _roster.m_style;
		m_presence				= _roster.m_presence;
		m_headImageHashCode		= _roster.m_headImageHashCode;
		
		m_name					= _roster.m_name;
		m_account				= _roster.m_account;
		m_status				= _roster.m_status;
		m_source				= _roster.m_source;
		m_ownAccount 			= _roster.m_ownAccount;
	}
	
	public String getName(){return m_name;}
	public void setName(String _name){m_name = _name;}
	
	public int getHeadImageHashCode(){return m_headImageHashCode;}
	public void setHeadImageHashCode(int _code){m_headImageHashCode = _code;}
	
	public String getAccount(){return m_account;}
	public void setAccount(String _acc){m_account = _acc;}
	
	public int getStyle(){return m_style;}
	public void setStyle(int _style){m_style = _style;}
	
	public int getPresence(){return m_presence;}
	public void setPresence(int _presence){m_presence = _presence;}
	
	public String getStatus(){return m_status;}
	public void setStatus(String _status){m_status = _status;}
	
	public String getSource(){return m_source;}
	public void setSource(String _source){m_source = _source;}
	
	public String getOwnAccount(){return m_ownAccount;}
	public void setOwnAccount(String _own){m_ownAccount = _own;}	

	public void Import(InputStream in)throws Exception{
		final int version = sendReceive.ReadInt(in);
		
		m_style = in.read();
		m_presence = in.read();
		
		m_headImageHashCode = sendReceive.ReadInt(in);
		m_name				= sendReceive.ReadString(in);
		m_account 			= sendReceive.ReadString(in);
		m_status 			= sendReceive.ReadString(in);
		m_source 			= sendReceive.ReadString(in);
		m_ownAccount		= sendReceive.ReadString(in);
	}
	
	public void Outport(OutputStream os)throws Exception{
		
		final int version = 0;
		sendReceive.WriteInt(os,version);
		
		os.write(m_style);
		os.write(m_presence);
		
		sendReceive.WriteInt(os,m_headImageHashCode);
		
		if(m_name != null){
			sendReceive.WriteString(os,m_name,false);
		}else{
			sendReceive.WriteString(os,"",false);
		}
		
		if(m_account != null){
			sendReceive.WriteString(os,m_account,false);
		}else{
			sendReceive.WriteString(os,"",false);
		}
		
		if(m_status != null){
			sendReceive.WriteString(os,m_status,false);
		}else{
			sendReceive.WriteString(os,"",false);
		}
		
		if(m_source != null){
			sendReceive.WriteString(os,m_source,false);
		}else{
			sendReceive.WriteString(os,"",false);
		}
		
		if(m_ownAccount != null){
			sendReceive.WriteString(os,m_ownAccount,false);
		}else{
			sendReceive.WriteString(os, m_ownAccount,false);
		}
	}
}
