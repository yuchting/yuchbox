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
package com.yuchting.yuchdroid.client.im;

import java.io.InputStream;
import java.io.OutputStream;

import com.yuchting.yuchdroid.client.sendReceive;

public class fetchChatMsg{
	
	public static int 				SEND_STATE_PADDING	= 0;
	public static int 				SEND_STATE_SENDING	= 1;
	public static int					SEND_STATE_SENT		= 2;
	public static int					SEND_STATE_READ		= 3;
	public static int					SEND_STATE_ERROR	= 4;
	
	
	public final static byte			STYLE_GTALK = 0;
	public final static byte			STYLE_MSN = 1;
		
	public final static byte 		CHAT_STATE_COMMON = 0;
	public final static byte 		CHAT_STATE_COMPOSING = 1;
	
	public final static byte			FILE_TYPE_IMG = 0;
	public final static byte			FILE_TYPE_SOUND = 1;
	
	
	int			m_style			= STYLE_GTALK;
	long		m_sendTime		= 0;
	String		m_msgOwner		= "";
	String		m_sendTo		= "";
	String		m_msg			= "";
	int			m_readHashCode	= 0;
	
	byte[]		m_fileContent	= null;
	int			m_contentType	= 0;
	
	// client using variables
	//
	boolean 	m_isOwnMsg		= false;
	int			m_sendState		= SEND_STATE_PADDING;
	boolean	m_sendReadMsg	= false;
		
	public fetchChatMsg(){}
		
	public int getFileContentType(){return m_contentType;}
	public byte[] getFileContent(){return m_fileContent;}
	
	public void setFileContent(byte[] _fileContent,int _type){
		m_fileContent = _fileContent;
		m_contentType = _type;
	}
	
	public void destory(){
		m_isOwnMsg = false;
		m_sendState = SEND_STATE_PADDING;
		m_sendReadMsg = false;
	}
	
	public int hashCode(){
		return (getOwner() + getStyle() + getSendTime()).hashCode();
	}
	
	public int getReadHashCode(){return m_readHashCode;}
	public void setReadHashCode(int _hashcode){m_readHashCode = _hashcode;}
	
	public int getStyle(){	return m_style;	}
	public void setStyle(int _style){m_style = _style;}
	
	public boolean hasSendMsgChatReadMsg(){return m_sendReadMsg;}
	public void setSendMsgChatReadMsg(boolean _send){m_sendReadMsg = _send;}
	
	public boolean isOwnMsg(){return m_isOwnMsg;}
	public void setIsOwnMsg(boolean _isOwnMsg){m_isOwnMsg = _isOwnMsg;}
	
	public int getSendState(){return m_sendState;}
	public void setSendState(int _state){m_sendState = _state;}
	
	public long getSendTime(){return m_sendTime;}
	public void setSendTime(long _time){m_sendTime = _time;}
	
	public String getOwner(){return m_msgOwner;}
	public void setOwner(String _owner){m_msgOwner = _owner;}
	
	public String getSendTo(){return m_sendTo;}
	public void setSendTo(String _to){m_sendTo = _to;}
	
	public String getMsg(){return m_msg;}
	public void setMsg(String _msg){m_msg = _msg;}	
	
	public void Import(InputStream in)throws Exception{
		final int version = sendReceive.ReadInt(in);
		
		m_style		= in.read();
		m_sendTime	= sendReceive.ReadLong(in);
		m_msgOwner	= sendReceive.ReadString(in);
		m_sendTo	= sendReceive.ReadString(in);
		m_msg		= sendReceive.ReadString(in);
		
		m_readHashCode = sendReceive.ReadInt(in);
		
		int t_content = sendReceive.ReadInt(in);
		if(t_content > 0){
			m_contentType = in.read();

			m_fileContent = new byte[t_content];
			sendReceive.ForceReadByte(in, m_fileContent, t_content);
		}else{
			m_fileContent = null;
		}
	}
	
	public void Output(OutputStream os)throws Exception{
		final int version = 0;
		sendReceive.WriteInt(os,version);
		
		os.write(m_style);
		sendReceive.WriteLong(os,m_sendTime);
		sendReceive.WriteString(os,m_msgOwner);
		sendReceive.WriteString(os,m_sendTo);
		sendReceive.WriteString(os,m_msg);
		sendReceive.WriteInt(os,m_readHashCode);		
		
		if(m_fileContent != null && m_fileContent.length > 0){
			sendReceive.WriteInt(os,m_fileContent.length);
			os.write(m_contentType);
			os.write(m_fileContent);
		}else{
			os.write(0);
		}
	}
}
