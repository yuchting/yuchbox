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

package com.yuchting.yuchberry.server;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Vector;

import javax.mail.Message;
import javax.mail.internet.MimeUtility;

public class  fetchMail{
	
	public final static class MailAttachment{
		int 		m_size;
		String 		m_name;
		String		m_type;
	}
	
	final static int	VERSION = 4;
	
	public final static String	fsm_noSubjectTile = "No Subject";
	    	
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
	
	private Vector<String>		m_vectFrom 		= new Vector<String>();
	private Vector<String>		m_vectReplyTo	= new Vector<String>();
	private Vector<String>		m_vectCCTo		= new Vector<String>();
	private Vector<String>		m_vectBCCTo		= new Vector<String>();
	private Vector<String>		m_vectTo		= new Vector<String>();
	private Vector<String>		m_vectGroup		= new Vector<String>();
	
	private String			m_subject 		= "";
	private Date			m_sendDate 		= new Date();
	private int			m_flags 		= 0;
	private String			m_XMailName 	= "";
	
	private String			m_contain		= "";
	private String			m_contain_html	= "";
	private String			m_contain_html_type	= "";
	
	private String			m_ownAccount	= "";
	
	private String			m_message_id 	= "";
	private String			m_in_reply_to	= "";
	private String			m_reference_id	= "";
	
	// location information
	boolean m_hasLocationInfo		= false;
	GPSInfo	m_gpsInfo 				= new GPSInfo();
		
	public	int				m_sendConfirmNum = 0;
	public long			m_sendConfirmTime = 0;
	
	private boolean m_convertoSimpleChar = false;
	
	private Vector<MailAttachment>	m_vectAttachment	 	= new Vector<MailAttachment>();
	
	private Message m_attachMessage	= null; 
	
	public fetchMail(boolean _convertoSimpleChar){
		m_convertoSimpleChar = _convertoSimpleChar;
	}
	
	public void SetMailIndex(int _index)throws Exception{
		if(_index <= 0){
			throw new Exception("SetMailIndex Negative");
		}
		m_mailIndex =_index;		
	}
	
	public int GetMailIndex(){
		return m_mailIndex;
	}
	
	public int GetSimpleHashCode(){
		return (GetSubject() + GetSendDate().getTime()).hashCode();
	}
	
	
	public void SetAttchMessage(Message m){ m_attachMessage = m;}
	public Message GetAttachMessage(){return m_attachMessage;}
		
	public static String parseAddressList(Vector<String> _list)throws Exception{
		
		StringBuffer t_addressList = new StringBuffer();
		
		for(int i = 0;i < _list.size();i++){
			String t_addr = (String)_list.get(i);
			
			final int t_start_quotation = t_addr.indexOf('\"');
			final int t_end_quotation = t_addr.lastIndexOf('\"');
			
			if(t_start_quotation != t_end_quotation){
				String t_subName = t_addr.substring(t_start_quotation + 1,t_end_quotation);
				
				try{
					t_addr = t_addr.replace("\"" + t_subName + "\"",MimeUtility.encodeText(t_subName,"UTF-8","B"));
				}catch(Exception ex){}			
			}else{
				final int t_start_tag = t_addr.lastIndexOf('<');
				
				if(t_start_tag != -1 && t_start_tag != 0){
					String t_subName = t_addr.substring(0,t_start_tag);
					
					try{
						t_addr = t_addr.replace(t_subName,MimeUtility.encodeText(t_subName));
					}catch(Exception ex){}	
				}
			}
			
			t_addressList.append(t_addr).append(",");
		}
				
		return t_addressList.toString().replace("＠", "@");
	}
	
	
	public void OutputMail(OutputStream _stream)throws Exception{
		
		_stream.write(VERSION);
		
		sendReceive.WriteInt(_stream,GetMailIndex());
		
		sendReceive.WriteStringVector(_stream,m_vectFrom,m_convertoSimpleChar);
		sendReceive.WriteStringVector(_stream,m_vectReplyTo,m_convertoSimpleChar);
		sendReceive.WriteStringVector(_stream,m_vectCCTo,m_convertoSimpleChar);
		sendReceive.WriteStringVector(_stream,m_vectBCCTo,m_convertoSimpleChar);
		sendReceive.WriteStringVector(_stream,m_vectTo,m_convertoSimpleChar);
		sendReceive.WriteStringVector(_stream,m_vectGroup,m_convertoSimpleChar);
		
		sendReceive.WriteString(_stream,m_subject,m_convertoSimpleChar);
		sendReceive.WriteLong(_stream,m_sendDate.getTime());
				
		sendReceive.WriteInt(_stream,m_flags);
		
		sendReceive.WriteString(_stream,m_XMailName,m_convertoSimpleChar);
		sendReceive.WriteString(_stream,m_contain,m_convertoSimpleChar);
		sendReceive.WriteString(_stream,m_contain_html,m_convertoSimpleChar);
		
		// write the Attachment
		//
		sendReceive.WriteInt(_stream, m_vectAttachment.size());
		for(int i = 0;i < m_vectAttachment.size();i++){
			MailAttachment t_attachment = (MailAttachment)m_vectAttachment.elementAt(i);
			sendReceive.WriteInt(_stream,t_attachment.m_size);
			sendReceive.WriteString(_stream,t_attachment.m_name,m_convertoSimpleChar);
			sendReceive.WriteString(_stream,t_attachment.m_type,m_convertoSimpleChar);
		}
		
		sendReceive.WriteBoolean(_stream,m_hasLocationInfo);
		if(m_hasLocationInfo){
			m_gpsInfo.OutputData(_stream);
		}
		
		sendReceive.WriteString(_stream,m_contain_html_type,m_convertoSimpleChar);
		sendReceive.WriteString(_stream,m_ownAccount,false);
		
		sendReceive.WriteString(_stream,m_message_id,false);
		sendReceive.WriteString(_stream,m_in_reply_to,false);
		sendReceive.WriteString(_stream,m_reference_id,false);
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
			MailAttachment t_attachment = new MailAttachment(); 
			
			t_attachment.m_size = sendReceive.ReadInt(_stream);
			t_attachment.m_name = sendReceive.ReadString(_stream);
			t_attachment.m_type = sendReceive.ReadString(_stream);
			
			m_vectAttachment.addElement(t_attachment);
		}
		
		m_hasLocationInfo = _stream.read() == 1?true:false;
		if(m_hasLocationInfo){
			m_gpsInfo.InputData(_stream);
		}
		
		if(t_version >= 2){
			m_contain_html_type = sendReceive.ReadString(_stream);
		}
		
		if(t_version >= 3){
			m_ownAccount = sendReceive.ReadString(_stream);
		}
		
		if(t_version >= 4){
			m_message_id = sendReceive.ReadString(_stream);
			m_in_reply_to = sendReceive.ReadString(_stream);
			m_reference_id = sendReceive.ReadString(_stream);
		}
	}
	
	
	
	//set and gets function
	//
	public String getOwnAccount(){return m_ownAccount;}
	public void setOwnAccount(String _acc){m_ownAccount = _acc;}
	
	public String GetSubject(){	return m_subject;}
	public void SetSubject(String _subject){
		if(m_convertoSimpleChar){
			m_subject = sendReceive.complTosimple(_subject);
		}else{
			m_subject = _subject;
		}
		
	}
	
	public String getMessageID(){return m_message_id;}
	public void setMessageID(String _id){m_message_id = _id;}
	
	public String getInReplyTo(){return m_in_reply_to;}
	public void setInReplyTo(String _replyTo){m_in_reply_to = _replyTo;}
	
	public String getReferenceID(){return m_reference_id;}
	public void setReferenceID(String _refID){m_reference_id = _refID;}
	
	public String GetContain(){return m_contain;}
	public void SetContain(String _contain){m_contain = _contain;}
	
	public String GetContain_html(){return m_contain_html;}
	public void SetContain_html(String _contain_html,String _content_type){
		m_contain_html = _contain_html;
		m_contain_html_type = _content_type;
	}
	public String GetContain_html_type(){return m_contain_html_type;}
	
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
	public Vector<String> GetSendToVect(){return m_vectTo;}
	
	public void SetReplyToVect(String[] _replyTo){
		m_vectReplyTo.removeAllElements();
		for(int i = 0;i < _replyTo.length;i++){
			m_vectReplyTo.addElement(_replyTo[i]);
		}		
	}
	public Vector<String> GetReplyToVect(){return m_vectReplyTo;}
	
	public void SetCCToVect(String[] _CCTo){
		m_vectCCTo.removeAllElements();
		for(int i = 0;i < _CCTo.length;i++){
			m_vectCCTo.addElement(_CCTo[i]);
		}		
	}
	public Vector<String> GetCCToVect(){return m_vectCCTo;}
	
	public void SetBCCToVect(String[] _BCCTo){
		m_vectBCCTo.removeAllElements();
		for(int i = 0;i < _BCCTo.length;i++){
			m_vectBCCTo.addElement(_BCCTo[i]);
		}		
	}
	public Vector<String> GetBCCToVect(){return m_vectBCCTo;}
	
	
	public Vector<String> GetFromVect(){return m_vectFrom;}
	public void SetFromVect(String[] _from){
		m_vectFrom.removeAllElements();
		for(int i = 0;i < _from.length;i++){
			m_vectFrom.addElement(_from[i]);
		}		
	}
	
	public Vector<String> GetGroupVect(){return m_vectGroup;}
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
			
		for(int i = 0;i < m_vectAttachment.size();i++){
			MailAttachment att = (MailAttachment)m_vectAttachment.elementAt(i);
			if(att.m_name.equals(_name)){
				_name = _name.concat("_");				
			}
		}
		
		MailAttachment t_attach = new MailAttachment();
		t_attach.m_name = _name;
		t_attach.m_size = _size;
		t_attach.m_type = _type;
		
		m_vectAttachment.addElement(t_attach);
		
	}
	public void ClearAttachment(){
		m_vectAttachment.removeAllElements();
	}
	
	public Vector<MailAttachment> GetAttachment(){
		return m_vectAttachment;
	}
	
	public void SetLocationInfo(final double _longitude,final double _latitude,
								final float _altitude,final float _speed,final float _heading){
		if(_longitude != 0 || _latitude != 0){
			m_hasLocationInfo = true;
			
			m_gpsInfo.m_longitude 	= _longitude;
			m_gpsInfo.m_latitude	= _latitude;
			m_gpsInfo.m_altitude	= _altitude;
			m_gpsInfo.m_speed		= _speed;
			m_gpsInfo.m_heading		= _heading;
		}else{
			m_hasLocationInfo = false;
		}
	}
	
}