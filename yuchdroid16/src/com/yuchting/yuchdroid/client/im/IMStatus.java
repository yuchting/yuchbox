package com.yuchting.yuchdroid.client.im;

import java.io.InputStream;
import java.io.OutputStream;

import com.yuchting.yuchdroid.client.sendReceive;

public final class IMStatus{
	
	static public IMStatus		sm_currUseStatus = null;
	
	public int 	m_presence = fetchChatRoster.PRESENCE_AVAIL;
	public String	m_status		= "";
	
	public IMStatus(){}
	public IMStatus(int _presence,String _status){
		m_presence	= _presence;
		m_status	= _status;
	}
	
	public void Import(InputStream in)throws Exception{
		m_presence = in.read();
		m_status	= sendReceive.ReadString(in);
	}
	
	public void Ouput(OutputStream os)throws Exception{
		os.write(m_presence);
		sendReceive.WriteString(os,m_status);
	}
	

	public String toString(){
		return m_status;
	}
}

