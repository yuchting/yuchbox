package com.yuchting.yuchberry.client.im;

import java.io.InputStream;
import java.io.OutputStream;

import com.yuchting.yuchberry.client.sendReceive;

public class fetchChatMsg{
	
	public final static byte			STYLE_GTALK = 0;
	public final static byte			STYLE_MSN = 1;
		
	public final static byte 		CHAT_STATE_COMMON = 0;
	public final static byte 		CHAT_STATE_COMPOSING = 1;
	
	public final static byte			FILE_TYPE_IMG = 0;
	public final static byte			FILE_TYPE_SOUND = 0;
	
	
	int			m_style			= STYLE_GTALK;
	long		m_sendTime		= 0;
	String		m_msgOwner		= "";
	String		m_msg			= "";
	
	byte[]		m_fileContent	= null;
	int			m_contentType	= 0;
	
	public fetchChatMsg(){}
	
	public int getFileContentType(){return m_contentType;}
	public byte[] getFileContent(){return m_fileContent;}
	
	public void setFileContent(byte[] _fileContent,int _type){
		m_fileContent = _fileContent;
		m_contentType = _type;
	}
	
	public int getStyle(){	return m_style;	}
	public void setStyle(int _style){m_style = _style;}
	
	public long getSendTime(){return m_sendTime;}
	public void setSendTime(long _time){m_sendTime = _time;}
	
	public String getOwner(){return m_msgOwner;}
	public void setOwner(String _owner){m_msgOwner = _owner;}
	
	public String getMsg(){return m_msg;}
	public void setMsg(String _msg){m_msg = _msg;}	
	
	public void Import(InputStream in)throws Exception{
		final int version = sendReceive.ReadInt(in);
		
		m_style		= in.read();
		m_sendTime	= sendReceive.ReadLong(in);
		m_msgOwner	= sendReceive.ReadString(in);
		
		m_msg		= sendReceive.ReadString(in);
		
		int t_content = sendReceive.ReadInt(in);
		if(t_content > 0){
			m_contentType = in.read();

			m_fileContent = new byte[t_content];
			sendReceive.ForceReadByte(in, m_fileContent, t_content);
		}
	}
	
	public void Import(OutputStream os)throws Exception{
		final int version = 0;
		sendReceive.WriteInt(os,version);
		
		os.write(m_style);
		sendReceive.WriteLong(os,m_sendTime);
		
		if(m_msgOwner != null){
			sendReceive.WriteString(os,m_msgOwner);
		}else{
			sendReceive.WriteString(os,"");
		}
		
		if(m_msg != null){
			sendReceive.WriteString(os,m_msg);
		}else{
			sendReceive.WriteString(os,"");
		}
		
		if(m_fileContent != null && m_fileContent.length > 0){
			sendReceive.WriteInt(os,m_fileContent.length);
			os.write(m_contentType);
			os.write(m_fileContent);
		}else{
			os.write(0);
		}
	}
}
