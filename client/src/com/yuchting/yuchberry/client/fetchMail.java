package com.yuchting.yuchberry.client;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Vector;

import javax.microedition.io.file.FileConnection;

import net.rim.blackberry.api.mail.Address;
import net.rim.blackberry.api.mail.Message;


public class  fetchMail{
	
	final static int	VERSION = 1;
	    	
	final static int	ANSWERED 	= 1 << 0;
	final static int	DELETED 	= 1 << 1;
	final static int	DRAFT 		= 1 << 2;
	final static int	FLAGGED 	= 1 << 3;
	final static int	RECENT 		= 1 << 4;
	final static int	SEEN 		= 1 << 5;
	
	final static int	NOTHING_STYLE = 0;
	final static int	FORWORD_STYLE = 1;
	final static int	REPLY_STYLE = 2;
	
	private int 		m_mailIndex = 0;
	
	private Vector		m_vectFrom 		= new Vector();
	private Vector		m_vectReplyTo	= new Vector();
	private Vector		m_vectCCTo		= new Vector();
	private Vector		m_vectBCCTo		= new Vector();
	private Vector		m_vectTo		= new Vector();
	private Vector		m_vectGroup		= new Vector();
	
	private String			m_subject 		= new String();
	private Date			m_sendDate 		= new Date();
	private int			m_flags 		= 0;
	private String			m_XMailName 	= new String();
	
	private String			m_contain		= new String();
	private String			m_contain_html	= new String();
		
	class Attachment{
		int 		m_size;
		String 		m_name;
		String		m_type;
	}
	
	private Vector	m_vectAttachment	 	= new Vector();
	
	private Message m_attachMessage		= null; 
			
	
	public void SetMailIndex(int _index)throws Exception{
		if(_index <= 0){
			throw new Exception("SetMailIndex Negative");
		}
		m_mailIndex =_index;		
	}
	
	public int GetMailIndex(){
		return m_mailIndex;
	}
	
	public void SetAttchMessage(Message m){ m_attachMessage = m;}
	public Message GetAttachMessage(){return m_attachMessage;}
		
	public static Address[] parseAddressList(Vector _list)throws Exception{
		Address[] 	t_addressList = new Address[_list.size()];
		
		for(int i = 0;i < _list.size();i++){
			String fullAdd = (String)_list.elementAt(i);
			String add;
			String t_name = null;
			
			final int t_start = fullAdd.indexOf('<');
			final int t_end = fullAdd.indexOf('>');
			
			final int t_start_quotation = fullAdd.indexOf('"');
			final int t_end_quotation = fullAdd.indexOf('"',t_start_quotation + 1);
			
			if(t_start_quotation != -1 && t_end_quotation != -1 ){			
				t_name = fullAdd.substring(t_start_quotation + 1, t_end_quotation);
			}else{
				if(t_start != -1 && t_start > 0){
					t_name = fullAdd.substring(0,t_start);
				}else{
					t_name = "";
				}				
			}
			
			if(t_start != -1 && t_end != -1 ){			
				add = fullAdd.substring(t_start + 1, t_end);
			}else{
				add = fullAdd;
			}
			
			t_addressList[i] = new Address(add,t_name);
		}
		
		return t_addressList;
	}
	
	
	public void OutputMail(OutputStream _stream)throws Exception{
		
		_stream.write(VERSION);
		
		sendReceive.WriteInt(_stream,GetMailIndex());
		
		sendReceive.WriteStringVector(_stream,m_vectFrom);
		sendReceive.WriteStringVector(_stream,m_vectReplyTo);
		sendReceive.WriteStringVector(_stream,m_vectCCTo);
		sendReceive.WriteStringVector(_stream,m_vectBCCTo);
		sendReceive.WriteStringVector(_stream,m_vectTo);
		sendReceive.WriteStringVector(_stream,m_vectGroup);
		
		sendReceive.WriteString(_stream,m_subject);
		sendReceive.WriteLong(_stream,m_sendDate.getTime());

		sendReceive.WriteInt(_stream,m_flags);
		
		sendReceive.WriteString(_stream,m_XMailName);
		sendReceive.WriteString(_stream,m_contain);
		sendReceive.WriteString(_stream,m_contain_html);
		
		// write the Attachment
		//
		sendReceive.WriteInt(_stream, m_vectAttachment.size());
		for(int i = 0;i < m_vectAttachment.size();i++){
			Attachment t_attachment = (Attachment)m_vectAttachment.elementAt(i);
			sendReceive.WriteInt(_stream,t_attachment.m_size);
			sendReceive.WriteString(_stream,t_attachment.m_name);
			sendReceive.WriteString(_stream,t_attachment.m_type);
		}
		
	}
		
	public void InputMail(InputStream _stream)throws Exception{
		
		final int t_version = _stream.read();
		
		m_mailIndex = sendReceive.ReadInt(_stream);
		
		sendReceive.ReadStringVector(_stream,m_vectFrom);
		sendReceive.ReadStringVector(_stream,m_vectReplyTo);
		sendReceive.ReadStringVector(_stream,m_vectCCTo);
		sendReceive.ReadStringVector(_stream,m_vectBCCTo);
		sendReceive.ReadStringVector(_stream,m_vectTo);
		sendReceive.ReadStringVector(_stream,m_vectGroup);
		
		m_subject = sendReceive.ReadString(_stream);
		m_sendDate.setTime(sendReceive.ReadLong(_stream));
		
		m_flags = sendReceive.ReadInt(_stream);
		
		m_XMailName = sendReceive.ReadString(_stream);
		m_contain = sendReceive.ReadString(_stream);
		m_contain_html = sendReceive.ReadString(_stream);
		
		m_vectAttachment.removeAllElements();
		final int t_attachmentNum = sendReceive.ReadInt(_stream);
		for(int i = 0;i < t_attachmentNum;i++){
			Attachment t_attachment = new Attachment(); 
			
			t_attachment.m_size = sendReceive.ReadInt(_stream);
			t_attachment.m_name = sendReceive.ReadString(_stream);
			t_attachment.m_type = sendReceive.ReadString(_stream);
			
			m_vectAttachment.addElement(t_attachment);
		}
		
	}
	
	//set and gets function
	//
	public String GetSubject(){	return m_subject;}
	public void SetSubject(String _subject){m_subject = _subject;}
	
	public String GetContain(){return m_contain;}
	public void SetContain(String _contain){m_contain = _contain;}
	
	public String GetContain_html(){return m_contain_html;}
	public void SetContain_html(String _contain_html){m_contain_html = _contain_html;}
	
	public String GetXMailer(){return m_XMailName;}
	public void SetXMailer(String _str){m_XMailName = _str;}
	
	public Date GetSendDate(){return m_sendDate;}
	public void SetSendDate(Date _d){m_sendDate = _d;}
	
	public int GetFlags(){return m_flags;}
	public void SetFlags(int _flags){m_flags = _flags;}
	
	public void SetSendToVect(String[] _to){
		m_vectTo.removeAllElements();
		for(int i = 0;i < _to.length;i++){
			m_vectTo.addElement(_to[i]);
		}		
	}
	public Vector GetSendToVect(){return m_vectTo;}
	
	public void SetReplyToVect(String[] _replyTo){
		m_vectReplyTo.removeAllElements();
		for(int i = 0;i < _replyTo.length;i++){
			m_vectReplyTo.addElement(_replyTo[i]);
		}		
	}
	public Vector GetReplyToVect(){return m_vectReplyTo;}
	
	public void SetCCToVect(String[] _CCTo){
		m_vectCCTo.removeAllElements();
		for(int i = 0;i < _CCTo.length;i++){
			m_vectCCTo.addElement(_CCTo[i]);
		}		
	}
	public Vector GetCCToVect(){return m_vectCCTo;}
	
	public void SetBCCToVect(String[] _BCCTo){
		m_vectBCCTo.removeAllElements();
		for(int i = 0;i < _BCCTo.length;i++){
			m_vectBCCTo.addElement(_BCCTo[i]);
		}		
	}
	public Vector GetBCCToVect(){return m_vectBCCTo;}
	
	
	public Vector GetFromVect(){return m_vectFrom;}
	public void SetFromVect(String[] _from){
		m_vectFrom.removeAllElements();
		for(int i = 0;i < _from.length;i++){
			m_vectFrom.addElement(_from[i]);
		}		
	}
	
	public Vector GetGroupVect(){return m_vectGroup;}
	public void SetGroupVect(String[] _group){
		m_vectGroup.removeAllElements();
		for(int i = 0;i < _group.length;i++){
			m_vectGroup.addElement(_group[i]);
		}
	}
	
	public void AddAttachment(String _name,String _type,int _size)throws Exception{
		if(_name == null || _name.length() <= 0){
			throw new Exception("Error Attachment format!");
		}
		
		Attachment t_attach = new Attachment();
		t_attach.m_name = _name;
		t_attach.m_size = _size;
		t_attach.m_type = _type;
		
		m_vectAttachment.addElement(t_attach);
		
	}
	public void ClearAttachment(){
		m_vectAttachment.removeAllElements();
	}
	
	public Vector GetAttachment(){
		return m_vectAttachment;
	}	
	
}

class sendMailAttachmentDeamon extends Thread{
	
	connectDeamon		m_connect 	= null;
	fetchMail			m_sendMail 	= null;
	fetchMail			m_forwardReply 	= null;
	
	InputStream 		m_fileIn 	= null;
	FileConnection		m_fileConnection = null;
	
	int					m_sendStyle = fetchMail.NOTHING_STYLE;
	
	int 				m_beginIndex = 0;
	
	int 				m_totalSize = 0;
	int					m_uploadedSize = 0;
	
	int					m_attachmentIndex = 0;
	Vector				m_vFileConnection = null;
		
	final static private int fsm_segmentSize = 512;
	
	byte[] 				m_bufferBytes 		= new byte[fsm_segmentSize];
	ByteArrayOutputStream m_os = new ByteArrayOutputStream();
		
	public sendMailAttachmentDeamon(connectDeamon _connect,
									fetchMail _mail,
									Vector _vFileConnection,
									fetchMail _forwardReply,int _sendStyle)throws Exception{
		m_connect	= _connect;
		m_sendMail	= _mail;
		m_forwardReply	= _forwardReply;
		m_sendStyle = _sendStyle;

		
		m_vFileConnection  = _vFileConnection;
		
		if(!m_vFileConnection.isEmpty()){
			
			for(int i = 0;i < m_vFileConnection.size();i++){
				FileConnection t_file = (FileConnection)m_vFileConnection.elementAt(i);
				m_totalSize += (int)t_file.fileSize();
			}
					
			m_fileConnection = (FileConnection)m_vFileConnection.elementAt(m_attachmentIndex);
			m_fileIn = m_fileConnection.openInputStream();
		}	
								
		start();
	}
	
	private void RefreshMessageStatus(){
							
		
		try{
			
			// sleep little to wait system set the mail status error
			// and set it back
			//
			sleep(500);
			
			m_connect.m_mainApp.UpdateMessageStatus(m_sendMail.GetAttachMessage(), Message.Status.TX_SENDING);
			
		}catch(Exception _e){}		

	}
	
	private void ReleaseAttachFile(){
		try{
			for(int i = 0;i < m_vFileConnection.size();i++){
				FileConnection t_file = (FileConnection)m_vFileConnection.elementAt(i);
				t_file.close();
			}
		}catch(Exception e){}
	}
	
	private boolean SendFileSegment(final boolean _send)throws Exception{
			
		final int t_size = (m_beginIndex + fsm_segmentSize) > (int)m_fileConnection.fileSize()?
							((int)m_fileConnection.fileSize() - m_beginIndex) : fsm_segmentSize;
					
		sendReceive.ForceReadByte(m_fileIn, m_bufferBytes, t_size);
		
		m_os.write(msg_head.msgMailAttach);
		sendReceive.WriteLong(m_os,m_sendMail.GetSendDate().getTime());
		sendReceive.WriteInt(m_os, m_attachmentIndex);
		sendReceive.WriteInt(m_os, m_beginIndex);
		sendReceive.WriteInt(m_os, t_size);
		m_os.write(m_bufferBytes,0,t_size);
		
		m_connect.m_connect.SendBufferToSvr(m_os.toByteArray(), _send);
		
		//System.out.println("send msgMailAttach time:"+ m_sendMail.GetSendDate().getTime() + " beginIndex:" + m_beginIndex + " size:" + t_size);
		
		m_connect.m_mainApp.SetUploadingDesc(m_sendMail,m_attachmentIndex,
											m_uploadedSize,m_totalSize);
		
		
		if((m_beginIndex + t_size) >= (int)m_fileConnection.fileSize()){
			
			m_beginIndex = 0;
			m_attachmentIndex++;
			
			m_fileIn.close();
			m_fileConnection.close();
			
			m_fileIn = null;
			
			if(m_attachmentIndex >= m_vFileConnection.size()){
				// send over
				//
				m_connect.m_mainApp.SetUploadingDesc(m_sendMail,-2,0,0);
				return true;
			}else{
				m_fileConnection = (FileConnection)m_vFileConnection.elementAt(m_attachmentIndex);
				m_fileIn = m_fileConnection.openInputStream();
			}
			
		}else{
			m_beginIndex += t_size;
		}
		
		m_uploadedSize += t_size;
		m_os.reset();
		
		return false;
	}
	
	public void run(){		
		
		boolean t_sendContain = false;
		
		while(true){
			
			while(m_connect.m_conn == null || !m_connect.m_sendAuthMsg){
				try{
					sleep(10000);
				}catch(Exception _e){
					break;
				}
				
				if(!m_connect.IsConnectState()){
					ReleaseAttachFile();
					return;
				}
			}			
			
			
			try{
				
				if(!t_sendContain){
				
					RefreshMessageStatus();
					
					// send mail once if has not attachment 
					//
					ByteArrayOutputStream os = new ByteArrayOutputStream();
					os.write(msg_head.msgMail);
					m_sendMail.OutputMail(os);
					
					// send the Mail of forward or reply
					//
					if(m_forwardReply != null && m_sendStyle != fetchMail.NOTHING_STYLE){
						os.write(m_sendStyle);
						m_forwardReply.OutputMail(os);
					}else{
						os.write(fetchMail.NOTHING_STYLE);
					}
					
					m_connect.m_connect.SendBufferToSvr(os.toByteArray(), false);
					
					if(m_vFileConnection.isEmpty()){
						break;
					}
					
					t_sendContain = true;
				}
				
				int t_sendSegmentNum = 0;
				while(t_sendSegmentNum++ < 4){
					if(SendFileSegment(false)){
						ReleaseAttachFile();
						return;
					}					
				}
				
				if(SendFileSegment(true)){
					break;
				}
				
				
			}catch(Exception _e){
				
				m_connect.m_mainApp.SetErrorString("S: " + _e.getMessage());
				m_connect.m_mainApp.SetUploadingDesc(m_sendMail,-1,0,0);
				
			}		
		}
		
		ReleaseAttachFile();
	}	
}

