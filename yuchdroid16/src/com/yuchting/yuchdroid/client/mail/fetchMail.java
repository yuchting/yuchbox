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
package com.yuchting.yuchdroid.client.mail;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Vector;

import com.yuchting.yuchdroid.client.GPSInfo;
import com.yuchting.yuchdroid.client.sendReceive;

public class  fetchMail{
	
	public static final class MailAttachment{
		public int 		m_size;
		public String 		m_name;
		public String		m_type;
	}
	
	public static final class Address{
		public String m_name;
		public String m_addr;
		
		public Address(String _name,String _addr){
			m_name = _name;
			m_addr = _addr;
		}
	};
	
	final static int	VERSION = 4;
	
	public final static String	fsm_vectStringSpliter = "<>";
	public final static String	fsm_vectStringSpliter_sub = "@#&";
	
	public final static String	fsm_noSubjectTile = "No Subject";
	    	
	public final static int	ANSWERED 	= 1 << 0;
	public final static int	DELETED 	= 1 << 1;
	public final static int	DRAFT 		= 1 << 2;
	public final static int	FLAGGED 	= 1 << 3;
	public final static int	RECENT 		= 1 << 4;
	public final static int	SEEN 		= 1 << 5;
	
	public final static int	NOTHING_STYLE = 0;
	public final static int	FORWORD_STYLE = 1;
	public final static int	REPLY_STYLE = 2;
	public final static int	REPLY_ALL_STYLE = 2;
	
	private int 		m_mailIndex = 0;
	
	private Vector<String>			m_vectFrom 		= new Vector<String>();
	private Vector<String>			m_vectReplyTo	= new Vector<String>();
	private Vector<String>			m_vectCCTo		= new Vector<String>();
	private Vector<String>			m_vectBCCTo		= new Vector<String>();
	private Vector<String>			m_vectTo		= new Vector<String>();
	private Vector<String>			m_vectGroup		= new Vector<String>();
	
	private String			m_subject 		= "";
	private Date			m_sendDate 		= new Date();
	private int			m_flags 		= 0;
	private String			m_XMailName 	= "";
	
	private String			m_contain		= "";
	private String			m_contain_html	= "";
	private String			m_contain_html_type	= "";
	
	private Vector<MailAttachment>	m_vectAttachment	 	= new Vector<MailAttachment>();
	
	private String			m_ownAccount	= "";
	
	private String			m_message_id 	= "";
	private String			m_in_reply_to	= "";
	private String			m_reference_id	= "";
	
	// group flag to display to the user 
	// used by client
	//
	public static final int		GROUP_FLAG_RECV					= 0;
	public static final int		GROUP_FLAG_RECV_READ			= 1;
	public static final int		GROUP_FLAG_RECV_ATTACH			= 2;
	public static final int		GROUP_FLAG_RECV_READ_ATTACH		= 3;
	
	public static final int		GROUP_FLAG_SEND_PADDING			= 4;
	public static final int		GROUP_FLAG_SEND_SENDING			= 5;
	public static final int		GROUP_FLAG_SEND_SENT			= 6;
	public static final int		GROUP_FLAG_SEND_ERROR			= 7;

	public static final int		GROUP_FLAG_SEND_DRAFT			= 8;
	
	private int					m_groupFlag		= 0;
	private long					m_dbIndex		= -1;
	private long					m_groupId		= -1;
	
	private long					m_sendRefMailId		= -1;
	private int					m_sendRefMailStyle	= NOTHING_STYLE;
	
	private long					m_recvMailTime	= System.currentTimeMillis();
		
	
	// location information
	boolean m_hasLocationInfo		= false;
	GPSInfo	m_gpsInfo 				= new GPSInfo();			
	
	public void SetMailIndex(int _index){
		m_mailIndex =_index;		
	}
	
	public int GetMailIndex(){
		return m_mailIndex;
	}
	
	public int GetSimpleHashCode(){
		return (GetSubject() + GetSendDate().getTime()).hashCode();
	}
	
	public static Address[] parseAddressList(Vector<String> _list){
		String[] t_arrayList = new String[_list.size()];
		for(int i = 0 ;i < _list.size();i++){
			t_arrayList[i] = _list.get(i);
		}
		return parseAddressList(t_arrayList);
	}
	
	public static Address[] parseAddressList(String[] _list){
		Address[] 	t_addressList = new Address[_list.length];
		
		int i = 0;
		for(String fullAdd:_list){
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
			
			t_addressList[i++] = new Address(t_name,add);
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
			MailAttachment t_attachment = (MailAttachment)m_vectAttachment.elementAt(i);
			sendReceive.WriteInt(_stream,t_attachment.m_size);
			sendReceive.WriteString(_stream,t_attachment.m_name);
			sendReceive.WriteString(_stream,t_attachment.m_type);
		}
		
		sendReceive.WriteBoolean(_stream,m_hasLocationInfo);
		if(m_hasLocationInfo){
			m_gpsInfo.OutputData(_stream);
		}
		
		sendReceive.WriteString(_stream,m_contain_html_type);
		sendReceive.WriteString(_stream,m_ownAccount);
		
		sendReceive.WriteString(_stream,m_message_id);
		sendReceive.WriteString(_stream,m_in_reply_to);
		sendReceive.WriteString(_stream,m_reference_id);
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
		
		m_hasLocationInfo = sendReceive.ReadBoolean(_stream);
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
		
		
		// client data variables initialized
		//
		if(m_vectAttachment.isEmpty()){
			setGroupFlag(GROUP_FLAG_RECV);
		}else{
			setGroupFlag(GROUP_FLAG_RECV_ATTACH);
		}
		
		m_recvMailTime = System.currentTimeMillis();
	}
	
	
	
	//set and gets function
	//
	public String getOwnAccount(){return m_ownAccount;}
	public void setOwnAccount(String _acc){m_ownAccount = _acc;}
	
	public String GetSubject(){	return m_subject;}
	public void SetSubject(String _subject){m_subject = _subject;}
	
	public void setGroupFlag(int _flag){m_groupFlag = _flag;}
	public int getGroupFlag(){return m_groupFlag;}
	
	public void setDbIndex(long _id){m_dbIndex = _id;}
	public long getDbIndex(){return m_dbIndex;}
		
	public void setGroupIndex(long _id){m_groupId = _id;}
	public long getGroupIndex(){return m_groupId;}
	
	public void setSendRefMailIndex(long _id){m_sendRefMailId = _id;}
	public long getSendRefMailIndex(){return m_sendRefMailId;}
	
	public void setSendRefMailStyle(int _style){m_sendRefMailStyle = _style;}
	public int getSendRefMailStyle(){return m_sendRefMailStyle;}
	
	public void setRecvMailTime(long _time){m_recvMailTime = _time;}
	public long getRecvMailTime(){return m_recvMailTime;}
	
	public String getMessageID(){return m_message_id;}
	public void setMessageID(String _id){m_message_id = _id;}
	
	public String getInReplyTo(){return m_in_reply_to;}
	public void setInReplyTo(String _replyTo){m_in_reply_to = _replyTo;}
	
	public String getReferenceID(){return m_reference_id;}
	public void setReferenceID(String _refID){m_reference_id = _refID;}
	
	public boolean isOwnSendMail(){
		
		return m_groupFlag == GROUP_FLAG_SEND_PADDING
			|| m_groupFlag == GROUP_FLAG_SEND_SENDING
			|| m_groupFlag == GROUP_FLAG_SEND_SENT
			|| m_groupFlag == GROUP_FLAG_SEND_ERROR
			|| m_groupFlag == GROUP_FLAG_SEND_DRAFT;
	}
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
	
	public GPSInfo GetGPSInfo(){return m_gpsInfo;}
	
	private static void setVectorArray(Vector<String> _vect,String[] _arr){
		_vect.removeAllElements();
		for(int i = 0;i < _arr.length;i++){
			if(_arr[i].length() != 0){
				_vect.addElement(_arr[i]);
			}
		}
	}
	public void SetSendToVect(String[] _to){
		setVectorArray(m_vectTo,_to);	
	}
	
	private static String getVectorString(Vector<String> _vector){
		StringBuffer t_result = new StringBuffer();
		for(String str:_vector){
			t_result.append(str).append(fsm_vectStringSpliter);
		}
		
		return t_result.toString();
	}
	
	public Vector<String> GetSendToVect(){return m_vectTo;}
	public String getSendToString(){return getVectorString(m_vectTo);}
	
	public void SetReplyToVect(String[] _replyTo){
		setVectorArray(m_vectReplyTo, _replyTo);
	}
	public Vector<String> GetReplyToVect(){return m_vectReplyTo;}
	public String getReplyString(){return getVectorString(m_vectReplyTo);}
	
	public void SetCCToVect(String[] _CCTo){
		setVectorArray(m_vectCCTo, _CCTo);
	}
	public Vector<String> GetCCToVect(){return m_vectCCTo;}
	public String getCCToString(){return getVectorString(m_vectCCTo);}
	
	public void SetBCCToVect(String[] _BCCTo){
		setVectorArray(m_vectBCCTo,_BCCTo);
	}
	public Vector<String> GetBCCToVect(){return m_vectBCCTo;}
	public String getBCCToString(){return getVectorString(m_vectBCCTo);}
	
	public Vector<String> GetFromVect(){return m_vectFrom;}
	public String GetFromString(){return getVectorString(m_vectFrom);}
	public void SetFromVect(String[] _from){
		setVectorArray(m_vectFrom,_from);
	}
	
	
	public Vector<String> GetGroupVect(){return m_vectGroup;}
	public String getGroupString(){return getVectorString(m_vectGroup);}
	public void SetGroupVect(String[] _group){
		setVectorArray(m_vectGroup, _group);
	}
	
	public void AddAttachment(String _name,String _type,int _size)throws Exception{
		if(_name == null || _name.length() <= 0){
			throw new Exception("Error Attachment format!");
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
	
	public String getAttachmentString(){
		StringBuffer t_string = new StringBuffer(); 
		for(MailAttachment att:m_vectAttachment){
			t_string.append(att.m_name).append(fsm_vectStringSpliter_sub)
					.append(att.m_size).append(fsm_vectStringSpliter_sub)
					.append(att.m_type).append(fsm_vectStringSpliter);
		}
		
		return t_string.toString();
	}
	
	public void setAttachmentByString(String _attList[]){
		m_vectAttachment.clear();
		
		for(String attStr:_attList){
			
			if(attStr.length() != 0){
				
				String[] t_list = attStr.split(fsm_vectStringSpliter_sub);
				MailAttachment att = new MailAttachment();
				att.m_name = t_list[0];
				att.m_size = Integer.valueOf(t_list[1]).intValue();
				att.m_type = t_list[2];
				
				m_vectAttachment.add(att);
			}			
		}
	}
	
	public Vector<MailAttachment> GetAttachment(){
		return m_vectAttachment;
	}
	
	public void SetLocationInfo(GPSInfo _gpsInfo){
		
		if(_gpsInfo.m_longitude != 0 || _gpsInfo.m_latitude != 0){
			
			m_hasLocationInfo = true;
			
			m_gpsInfo.m_longitude 	= _gpsInfo.m_longitude;
			m_gpsInfo.m_latitude	= _gpsInfo.m_latitude;
			m_gpsInfo.m_altitude	= _gpsInfo.m_altitude;
			m_gpsInfo.m_speed		= _gpsInfo.m_speed;
			m_gpsInfo.m_heading		= _gpsInfo.m_heading;
			
		}else{
			m_hasLocationInfo = false;
		}
	}
	
}


