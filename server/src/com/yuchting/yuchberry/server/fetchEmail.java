package com.yuchting.yuchberry.server;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import org.dom4j.Element;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.nodes.TextNode;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;

import com.sun.mail.smtp.SMTPTransport;

class EmailSendAttachment extends Thread{
	
	FileInputStream		m_file;
	fetchEmail			m_fetchEmail;
	int					m_fileLength;
	int					m_mailIndex;
	int					m_attachIndex;
	
	int					m_startIndex = 0;
	byte[] 				m_buffer = new byte[fsm_sendSize];
	ByteArrayOutputStream m_os = new ByteArrayOutputStream();
	
	final static int	fsm_sendSize = 512;
	
	EmailSendAttachment(int _mailIndex,int _attachIdx,fetchEmail _mgr){
		
		m_fetchEmail 	= _mgr;
		m_mailIndex 	= _mailIndex;
		m_attachIndex 	= _attachIdx;
		
		try{
			File t_file = new File(m_fetchEmail.GetAccountPrefix() + _mailIndex +"_"+ _attachIdx + ".att");	
			m_fileLength = (int)t_file.length();
			m_file = new FileInputStream(t_file);
			
		}catch(Exception _e){
			m_fetchEmail.m_mainMgr.m_logger.PrinterException(_e);
			return;
		}
		
		start();
	}
	
	private boolean SendAttachment(boolean _send) throws Exception{
		m_os.reset();
		
		final int t_size = (m_startIndex + fsm_sendSize) > m_fileLength ?(m_fileLength - m_startIndex):fsm_sendSize;
		m_file.read(m_buffer, 0, t_size);
		m_os.write(msg_head.msgMailAttach);
		
		sendReceive.WriteInt(m_os,m_mailIndex);
		sendReceive.WriteInt(m_os,m_attachIndex);
		sendReceive.WriteInt(m_os,m_startIndex);
		sendReceive.WriteInt(m_os,t_size);
		m_os.write(m_buffer);
		
		m_fetchEmail.m_mainMgr.m_logger.LogOut("send msgMailAttach mailIndex:" + m_mailIndex + " attachIndex:" + m_attachIndex + " startIndex:" +
									m_startIndex + " size:" + t_size + " first:" + (int)m_buffer[0]);
		
		int t_waitTimer = 0;
		while(m_fetchEmail.m_mainMgr.GetClientConnected() == null || m_fetchEmail.m_mainMgr.GetClientConnected().m_sendReceive == null){
			
			t_waitTimer++;
			
			sleep(10000);
			
			if(t_waitTimer > 5){
				throw new Exception("Client closed when send attachment!");
			}			
		}
		
		m_fetchEmail.m_mainMgr.SendData(m_os,_send);
		
		if(m_startIndex + t_size >= m_fileLength){
			return true;
		}
		
		m_startIndex += t_size;
		
		return false;
	}
	
	public void run(){

		while(true){
			try{
				
				int t_sendNum = 0;
				while(t_sendNum++ < 4){
					if(SendAttachment(false)){
						m_file.close();
						return;
					}
				}
				
				if(SendAttachment(true)){
					break;
				}
				
				
			}catch(Exception _e){
				m_fetchEmail.m_mainMgr.m_logger.PrinterException(_e);
				break;
			}			
		}
		
		try{
			m_file.close();
		}catch(Exception e){}
		
	}
}

class RecvMailAttach{
		
	fetchMail m_sendMail 			= null;
	fetchMail m_forwardReplyMail	= null;
	
	int			m_style;
	
	public RecvMailAttach(fetchMail _sendMail,fetchMail _forwardReplyMail,int _style){
		m_sendMail = _sendMail;
		m_forwardReplyMail = _forwardReplyMail;
		
		m_style = _style;
	}
	
	public void PrepareForwardReplyContain(String _signature){
				
		StringBuffer t_string = new StringBuffer();
		t_string.append(m_sendMail.GetContain());
		t_string.append("\n\n\n" + _signature);
		
		if(m_forwardReplyMail != null && m_style != fetchMail.NOTHING_STYLE){
			if(m_style == fetchMail.REPLY_STYLE){
				
				t_string.append("\n\n---------- 原始邮件 ----------\n");

				try{
					BufferedReader in = null;
					if(m_forwardReplyMail.GetContain_html().isEmpty()){
						in = new BufferedReader(new StringReader(m_forwardReplyMail.GetContain()));
					}else{
						in = new BufferedReader(new StringReader(m_forwardReplyMail.GetContain() + "\n\n---------- HTML ----------\n\n" +
									fetchMgr.ParseHTMLText(m_forwardReplyMail.GetContain_html(),true)));
					}						
	 
					String line = new String();
					while((line = in.readLine())!= null){
						t_string.append("> " + line + "\n");
					}
					
				}catch(Exception e){
					t_string.append("读取转发消息出现异常！！！");
				}
				
				
			}else if(m_style == fetchMail.FORWORD_STYLE){
				
				t_string.append("\n\n---------- 已转发邮件 ----------\n");

				Vector t_form = m_forwardReplyMail.GetFromVect();
				
				t_string.append("发件人："+ (String)t_form.elementAt(0) + "\n");
				for(int i = 1;i < t_form.size();i++){
					t_string.append((String)t_form.elementAt(i) + "\n");
				}
				t_string.append("日期："+ new SimpleDateFormat("yyyy年MM月dd日 HH:mm").format(m_forwardReplyMail.GetSendDate()) + "\n");
				t_string.append("主题："+ m_forwardReplyMail.GetSubject() + "\n");
				
				Vector t_sendto = m_forwardReplyMail.GetSendToVect();
				t_string.append("收件人："+ (String)t_sendto.elementAt(0) + "\n");
				for(int i = 1;i < t_sendto.size();i++){
					t_string.append((String)t_sendto.elementAt(i) + "\n");
				}
				
				t_string.append(m_forwardReplyMail.GetContain());
				
				if(!m_forwardReplyMail.GetContain_html().isEmpty()){					
					t_string.append("\n\n---------- HTML ----------\n\n");
					t_string.append(fetchMgr.ParseHTMLText(m_forwardReplyMail.GetContain_html(),true));
				}
			}
		}	
		
		m_sendMail.SetContain(t_string.toString());
	}
}


public class fetchEmail extends fetchAccount{
	
	final static int	CHECK_NUM 		= 20;
	
	public final static String	fsm_signatureFilename 		= "signature.txt";
	
	public final static String	fsm_mailIndexAttachFilename = "totalMailIndexAtt.data";
	
	String		m_protocol				= null;
	String		m_host					= null;
	int			m_port					= 110;
	
	String		m_protocol_send			= null;
	String		m_host_send				= null;
	int			m_port_send				= 25;
	
    
	String 	m_userName 					= null;
	String 	m_strUserNameFull			= null;
	boolean m_useFullNameSignIn		= false;
	String 	m_password 					= null;
	
	String	m_signature 				= new String();
	
	 // Get a Session object
    Session m_session 					= null;
    Store 	m_store						= null;
    
    Session m_session_send 				= null;
    SMTPTransport m_sendTransport 		= null;
        	
    Vector m_unreadMailVector 			= new Vector();
    Vector m_unreadMailVector_confirm 	= new Vector();
    Vector m_unreadMailVector_marking	= new Vector();

    // pushed mail index vector 
    Vector m_vectPushedMailIndex 		= new Vector();
    
    int		m_beginFetchIndex 			= 0;
    int		m_totalMailCount			= 0;
    
    int		m_unreadFetchIndex			= 0;
    
    boolean m_useAppendHTML			= false;
    
    private Vector	m_recvMailAttach 	= new Vector();
    
	// Get a Properties object
    Properties m_sysProps = System.getProperties();
    Properties m_sysProps_send = System.getProperties();
    
    class MailIndexAttachment{
    	int			m_mailHashCode;
    	
    	boolean	m_send;
    	long		m_mailIndexOrTime;
    	
    	Vector		m_attachmentName = null;
    	
    	public MailIndexAttachment(){
    		m_attachmentName = new Vector();
    	}
    	
    	public MailIndexAttachment(fetchMail _mail,boolean _send){
    		
    		m_mailHashCode		= _mail.GetSimpleHashCode();
    		m_send 				= _send;
    		
    		if(_send){
    			m_mailIndexOrTime	= _mail.GetSendDate().getTime();
    		}else{
    			m_mailIndexOrTime	= _mail.GetMailIndex();
    		}    		
    		
    		m_attachmentName	= _mail.GetAttachment();
    	}
    }
    
    Vector	m_mailIndexAttachName = new Vector();
    
    
    public fetchEmail(fetchMgr _mainMgr){
    	super(_mainMgr);
    }
    
    public String GetAccountPrefix(){
		return m_mainMgr.GetPrefixString() + m_strUserNameFull + "/";
	}
	
	public int GetBeginFetchIndex(){
		return m_beginFetchIndex;
	}
	
	public void SetUnreadFetchIndex(int _index){
		m_unreadFetchIndex = _index;
	}
	
	public int GetUnreadFetchIndex(){
		return m_unreadFetchIndex;
	}
	
	public int GetMailCountWhenFetched(){
		return m_totalMailCount;
	}
	
	public String GetHost(){
		return m_host;
	}
	
	public String GetSendHost(){
		return m_host_send;
	}
	
	public int GetHostPort(){
		return m_port;
	}
	
	public int GetSendPort(){
		return m_port_send;
	}
	
	public String GetAccountName(){
		return m_strUserNameFull;
	}	
	
	public void InitAccount(Element _elem)throws Exception{
		
		m_strUserNameFull					= ReadStringAttr(_elem,"account");
		if(m_strUserNameFull.indexOf('@') == -1 || m_strUserNameFull.indexOf('.') == -1){
			throw new Exception("account : xxxxx@xxx.xxx such as 1234@gmail.com");
		}
    	m_userName	= m_strUserNameFull.substring(0,m_strUserNameFull.indexOf('@'));
    	
    	m_password							= ReadStringAttr(_elem,"password");

    	m_useFullNameSignIn					= ReadBooleanAttr(_elem,"useFullNameSignIn");
    	
		m_protocol							= ReadStringAttr(_elem,"protocol");
		m_host								= ReadStringAttr(_elem,"host");
		m_port								= ReadIntegerAttr(_elem,"port");
		
		m_protocol_send						= ReadStringAttr(_elem,"protocol_send");
		m_host_send							= ReadStringAttr(_elem,"host_send");
		m_port_send							= ReadIntegerAttr(_elem,"port_send");
		
		m_useAppendHTML						= ReadBooleanAttr(_elem,"appendHTML");
		
		File t_file = new File(GetAccountPrefix());
		if(!t_file.exists() || !t_file.isDirectory()){
			t_file.mkdir();
		}
		
		ReadWriteMailIndexAttach(true);	
		ReadSignature();
	}
		
	public void CheckFolder()throws Exception{
		
		Folder folder = m_store.getDefaultFolder();
		
	    if(folder == null) {
	    	throw new Exception("Cant find default namespace");
	    }
	    
	    folder = folder.getFolder("INBOX");
	    if (folder == null) {
	    	throw new Exception("Invalid INBOX folder");
	    }
	    	    
	    folder.open(Folder.READ_ONLY);
	   
	    if(m_totalMailCount != folder.getMessageCount()){
	    	m_totalMailCount = folder.getMessageCount();	    
		    final int t_startIndex = Math.max(m_totalMailCount - Math.min(CHECK_NUM,m_totalMailCount) + 1,
		    									Math.min(m_totalMailCount,m_beginFetchIndex));
		    
		    Message[] t_msgs = folder.getMessages(t_startIndex, m_totalMailCount);
		    
		    for(int i = 0;i < t_msgs.length;i++){
		    	
		    	Message t_msg = t_msgs[i];
		    	
		    	Flags flags = t_msg.getFlags();
	        	Flags.Flag[] flag = flags.getSystemFlags();  
	        	
	        	boolean t_isNew = true;
	        	for(int j = 0; j < flag.length; j++){
	                if (flag[j] == Flags.Flag.SEEN 
	                	&& flag[j] != Flags.Flag.DELETED
	                	&& flag[j] != Flags.Flag.DRAFT) {
	                	
	                    t_isNew = false;
	                    break;      
	                }
	            }      
	        	
		    	if(t_isNew){
		    		
		    		fetchMail t_mail = new fetchMail(m_mainMgr.m_convertToSimpleChar);
		    		t_mail.SetMailIndex(i + t_startIndex);
		    		try{
		    			ImportMail(t_msg,t_mail);
		    		}catch(Exception e){
		    			t_mail.SetContain(t_mail.GetContain() + "\n\n\n" + e.getMessage() + "\nThe yuchberry ImportMail Error! Please read the Mail via another way!\n\n\n");
		    		}
		    		
		    		AddMailIndexAttach(t_mail,false);		    		
		    		
		    		m_unreadMailVector.addElement(t_mail);
		    		
		    		synchronized (m_unreadMailVector_marking) {
		    			
		    			// prepare the marking reading vector
		    			//
			    		if(m_unreadMailVector_marking.size() > 50){
			    			m_unreadMailVector_marking.removeElementAt(0);
			    		}
						m_unreadMailVector_marking.addElement(t_mail);
					}
		    	}
		    }
	    }	       
	    
	    folder.close(false);
	}
	
	public boolean ProcessNetworkPackage(byte[] _package)throws Exception{
		
		ByteArrayInputStream in = new ByteArrayInputStream(_package);
		
		final int t_msg_head = in.read();
				
		boolean t_processed = true;
		
		switch(t_msg_head){			
			case msg_head.msgMail:
				t_processed = ProcessMail(in);
				break;
			case msg_head.msgBeenRead:
				t_processed = ProcessBeenReadMail(in);
				break;
			case msg_head.msgMailAttach:
				t_processed = ProcessMailAttach(in);
				break;
			case msg_head.msgFetchAttach:
				t_processed = ProcessFetchMailAttach(in);
				break;
			case msg_head.msgMailConfirm:
				t_processed = ProcessMailConfirm(in);
				break;
			default:
				t_processed = false;
		}
		
		return t_processed;
	}
	
	private boolean ProcessMail(ByteArrayInputStream in)throws Exception{
		
		fetchMail t_mail = new fetchMail(m_mainMgr.m_convertToSimpleChar);
		t_mail.InputMail(in);
				
		fetchMail t_forwardReplyMail = null;
				
		final int t_style = in.read();
		
		if(t_style != fetchMail.NOTHING_STYLE){
			t_forwardReplyMail = new fetchMail(m_mainMgr.m_convertToSimpleChar);
			t_forwardReplyMail.InputMail(in);
			
			if(t_style == fetchMail.REPLY_STYLE){
				Vector t_replyAddr = t_forwardReplyMail.GetSendToVect();
				if(!t_replyAddr.isEmpty()){

					boolean t_found = false;
					
					for(int i= 0 ;i < t_replyAddr.size();i++){
						String t_addr = (String)t_replyAddr.elementAt(i);
						if(t_addr.toLowerCase().indexOf(GetAccountName().toLowerCase()) != -1){
							t_found = true;
							break;
						}
					}
					if(!t_found){
						return false;
					}
				}
			}
		}
		
		if(t_mail.GetAttachment().isEmpty()){
			SendMailToSvr(new RecvMailAttach(t_mail,t_forwardReplyMail,t_style));
		}else{
			CreateTmpSendMailAttachFile(new RecvMailAttach(t_mail,t_forwardReplyMail,t_style));
		}
		
		return true;
	}
	
	private boolean ProcessMailConfirm(ByteArrayInputStream in)throws Exception{
		
		final int t_mailHash = sendReceive.ReadInt(in);
		
		synchronized(m_mainMgr){
			
			for(int i = 0;i < m_unreadMailVector_confirm.size();i++){
				fetchMail t_confirmMail = (fetchMail)m_unreadMailVector_confirm.elementAt(i); 
				if(t_confirmMail.GetSimpleHashCode() == t_mailHash){
					
					m_unreadMailVector_confirm.removeElementAt(i);
					
					m_mainMgr.m_logger.LogOut(GetAccountName() + " Mail Index<" + t_confirmMail.GetMailIndex() + "> confirmed");
					
					return true;
				}
			}
			
		}
		
		return false;
	}	
	
	private boolean ProcessFetchMailAttach(InputStream in)throws Exception{
		
		final int t_mailIndex		= sendReceive.ReadInt(in);
		final int t_attachIndex	= sendReceive.ReadInt(in);
		
		File t_file = new File(GetAccountPrefix() + t_mailIndex +"_"+ t_attachIndex + ".att");
		if(t_file.exists()){
			new EmailSendAttachment(t_mailIndex,t_attachIndex,this);
			return true;
		}
		
		return false;
		
	}
		
	private boolean ProcessMailAttach(ByteArrayInputStream in)throws Exception{
		
		final int t_hashCode = sendReceive.ReadInt(in);
		
		RecvMailAttach t_mail = FindAttachMail(t_hashCode);
		if(t_mail == null){
			return false;
		}
		
		final int t_attachmentIdx = sendReceive.ReadInt(in);
		final int t_segIdx = sendReceive.ReadInt(in);
		final int t_segSize = sendReceive.ReadInt(in);
		
		String t_filename = GetAccountPrefix() + t_mail.m_sendMail.GetSendDate().getTime() + "_" + t_attachmentIdx + ".satt";
		File t_file = new File(t_filename);
		
		if(t_segIdx + t_segSize > t_file.length()){
			throw new Exception("error attach" + t_filename + " idx and size");
		}
		
		m_mainMgr.m_logger.LogOut("recv msgMailAttach time:"+ t_mail.m_sendMail.GetSendDate().getTime() + " beginIndex:" + t_segIdx + " size:" + t_segSize);
		
		byte[] t_bytes = new byte[t_segSize];
		sendReceive.ForceReadByte(in, t_bytes, t_segSize);
		
		RandomAccessFile t_fwrite = new RandomAccessFile(t_file,"rw");
		t_fwrite.seek(t_segIdx);
		t_fwrite.write(t_bytes);
		
		t_fwrite.close();
		
		if(t_segIdx + t_segSize == t_file.length()){
			
			m_recvMailAttach.removeElement(t_mail);
			
			if((t_attachmentIdx + 1) >= t_mail.m_sendMail.GetAttachment().size()){
				SendMailToSvr(t_mail);
			}		
			
		}
		
		return true;
	}
	
	public void SendMailToSvr(final RecvMailAttach _mail)throws Exception{
		
		// receive send message to berry
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		os.write(msg_head.msgSendMail);
		int t_succ = 1;
		
		try{
			
			SendMail(_mail);
			
		}catch(Exception _e){
			
			ByteArrayOutputStream error = new ByteArrayOutputStream();
			error.write(msg_head.msgNote);
			sendReceive.WriteString(error, _e.getMessage(),m_mainMgr.m_convertToSimpleChar);
			
			m_mainMgr.SendData(error, false);
			
			t_succ = 0;
		}
		
		os.write(t_succ);
		
		sendReceive.WriteInt(os,(int)_mail.m_sendMail.GetSendDate().getTime());
		sendReceive.WriteInt(os,(int)(_mail.m_sendMail.GetSendDate().getTime() >>> 32));

		m_mainMgr.SendData(os,false);
		
		m_mainMgr.m_logger.LogOut(GetAccountName() + " Mail <" +_mail.m_sendMail.GetSendDate().getTime() +  "> send " + ((t_succ == 1)?"Succ":"Failed"));
	}
	
	private  boolean ProcessBeenReadMail(ByteArrayInputStream in)throws Exception{
		
		final int t_mailHashCode	= sendReceive.ReadInt(in);

		boolean t_found = false;
		fetchMail t_mail = null;
		
		synchronized (m_unreadMailVector_marking) {
			for(int i = 0 ;i < m_unreadMailVector_marking.size();i++){
				t_mail = (fetchMail)m_unreadMailVector_marking.elementAt(i);
				if(t_mail.GetSimpleHashCode() == t_mailHashCode){
					m_unreadMailVector_marking.removeElementAt(i);
					
					t_found = true;
					
					break;					
				}
			}	
		}
		
		if(t_found){
			try{
				MarkReadMail(t_mail.GetMailIndex());
			}catch(Exception _e){
				m_mainMgr.m_logger.PrinterException(_e);
			}
		}	
		
		return t_found;
	}
	
	
	public void SendMail(RecvMailAttach _mail)throws Exception{
		
		Message msg = new MimeMessage(m_session_send);
		
		_mail.PrepareForwardReplyContain(m_signature);		
		ComposeMessage(msg,_mail.m_sendMail,
					(_mail.m_style == fetchMail.FORWORD_STYLE)?_mail.m_forwardReplyMail:null);
		
		AddMailIndexAttach(_mail.m_sendMail,true);
	    
		int t_tryTime = 0;
		while(t_tryTime++ < 5){
			try{
				m_sendTransport.connect(m_host_send,m_port_send,m_userName,m_password);
				m_sendTransport.sendMessage(msg, msg.getAllRecipients());
				m_sendTransport.close();
				break;
			}catch(Exception e){}
		}
		
		// delete the tmp files
		//
		for(int i = 0;i < _mail.m_sendMail.GetAttachment().size();i++){
			String t_fullname = GetAccountPrefix() + _mail.m_sendMail.GetSendDate().getTime() + "_" + i + ".satt";
			File t_file = new File(t_fullname);
			t_file.delete();
		}		
	}

	private MailIndexAttachment FindMailIndexAttach(int _hashCode){
		for(int i = 0;i < m_mailIndexAttachName.size();i++){
			MailIndexAttachment t_att = (MailIndexAttachment)m_mailIndexAttachName.elementAt(i);
			if(t_att.m_mailHashCode == _hashCode){
				return t_att;
			}
		}
		
		return null;
	}
	
	private void AddMailIndexAttach(fetchMail _mail,boolean _send){
		
		if(_mail.GetAttachment().isEmpty()){
			return ;
		}
		
		final int t_hashCode = _mail.GetSimpleHashCode();
		
		if(FindMailIndexAttach(t_hashCode) == null){
			m_mailIndexAttachName.addElement(new MailIndexAttachment(_mail, _send));
			
			ReadWriteMailIndexAttach(false);
		}
	}
	
	private void ReadWriteMailIndexAttach(boolean _read){
		
		final int ft_currentVersion	= 1;
		
		try{
			if(_read){
				m_mailIndexAttachName.removeAllElements();
				
				final String t_filename = GetAccountPrefix() + fsm_mailIndexAttachFilename;
				if(!(new File(t_filename)).exists()){
					return;
				}
				
				FileInputStream t_fileRead = new FileInputStream(t_filename);				
				
				final int t_version = t_fileRead.read();
				
				final int t_mainNum = sendReceive.ReadInt(t_fileRead);
				for(int i = 0;i < t_mainNum;i++){
					MailIndexAttachment t_mailAtt 		= new MailIndexAttachment();
					t_mailAtt.m_mailHashCode 			= sendReceive.ReadInt(t_fileRead);
					t_mailAtt.m_send 					= (t_fileRead.read() == 0)?false:true;
					t_mailAtt.m_mailIndexOrTime			= sendReceive.ReadLong(t_fileRead);
					
					final int t_attNum = sendReceive.ReadInt(t_fileRead);
					for(int j = 0;j < t_attNum;j++){
						MailAttachment t_att 	= new MailAttachment();
						
						t_att.m_size			= sendReceive.ReadInt(t_fileRead);
						t_att.m_name			= sendReceive.ReadString(t_fileRead);
						t_att.m_type			= sendReceive.ReadString(t_fileRead);
						
						t_mailAtt.m_attachmentName.addElement(t_att);
					}
					
					m_mailIndexAttachName.addElement(t_mailAtt);
				}
				
				t_fileRead.close();
				
			}else{
				FileOutputStream t_fileWrite = new FileOutputStream(GetAccountPrefix() + fsm_mailIndexAttachFilename);
				
				t_fileWrite.write(ft_currentVersion);
				
				sendReceive.WriteInt(t_fileWrite,m_mailIndexAttachName.size());
				for(int i = 0 ; i< m_mailIndexAttachName.size();i++){
					MailIndexAttachment t_att = (MailIndexAttachment)m_mailIndexAttachName.elementAt(i);
										
					sendReceive.WriteInt(t_fileWrite,t_att.m_mailHashCode);
					t_fileWrite.write(t_att.m_send?1:0);
					sendReceive.WriteLong(t_fileWrite,t_att.m_mailIndexOrTime);
					
					sendReceive.WriteInt(t_fileWrite,t_att.m_attachmentName.size());
					for(int j = 0 ;j < t_att.m_attachmentName.size();j++){
						MailAttachment t_attachment = (MailAttachment)t_att.m_attachmentName.elementAt(j);
						sendReceive.WriteInt(t_fileWrite,t_attachment.m_size);
						sendReceive.WriteString(t_fileWrite,t_attachment.m_name,m_mainMgr.m_convertToSimpleChar);
						sendReceive.WriteString(t_fileWrite,t_attachment.m_type,m_mainMgr.m_convertToSimpleChar);
					}
					
				}
				
				t_fileWrite.flush();
				t_fileWrite.close();
			}
		}catch(Exception e){
			m_mainMgr.m_logger.PrinterException(e);
		}
	}
		
		
	public void MarkReadMail(int _index)throws Exception{
		
		Folder folder = m_store.getDefaultFolder();
	    if(folder == null) {
	    	throw new Exception("Cant find default namespace");
	    }
	    
	    folder = folder.getFolder("INBOX");
	    if (folder == null) {
	    	throw new Exception("Invalid INBOX folder");
	    }
	    try{
			folder.open(Folder.READ_WRITE);  
			
			Message[] t_msg = folder.getMessages(_index, _index);
			
			if(t_msg.length != 0){
				m_mainMgr.m_logger.LogOut(GetAccountName() + " Set index " + _index + " read ");
				t_msg[0].setFlag(Flags.Flag.SEEN, true);
			}
	 	    
	    }finally{	    	
	    	folder.close(false);
	    }	        
	}
			
	public boolean IsConnectState(){
		return m_session != null;
	}
	
	private void ReadSignature(){
		
		try{
			m_signature = fetchMgr.ReadSimpleIniFile(m_mainMgr.GetPrefixString() + fsm_signatureFilename,"UTF-8",null);
		}catch(Exception e){
			m_mainMgr.m_logger.PrinterException(e);
		}
	}
	
	public synchronized void ResetSession(boolean _fullTest)throws Exception{
		
		DestroySession();
    	
		if(m_session != null){
			throw new Exception("has been initialize the session");
		}
				
		if(m_protocol == null){
    		m_protocol = "pop3";
    	}else{
    		
    		if(!m_protocol.equals("imap") 
    		&& !m_protocol.equals("pop3") 
    		&& !m_protocol.equals("pop3s") 
    		&& !m_protocol.equals("imaps")){
    			
    			m_protocol = "pop3";
    		}   		
	    }
		
		
		m_session = Session.getInstance(m_sysProps, null);
    	m_session.setDebug(false);
    	
    	if(m_protocol.indexOf("pop3") != -1){
    		m_sysProps.put("mail.pop3.disabletop", "true");
    	}
    	
    	m_sysProps.put("mail.imap.timeout","10000");
    	m_sysProps.put("mail.smtp.timeout","20000");
    	m_sysProps.put("mail.pop3.timeout","10000");
    	
    	m_store = m_session.getStore(m_protocol);
    	try{
    		
    		if(m_useFullNameSignIn){
    			m_store.connect(m_host,m_port,m_strUserNameFull,m_password);
    		}else{
    			m_store.connect(m_host,m_port,m_userName,m_password);
    		}
    		
    	}catch(Exception e){
    		if(e.getMessage() != null && e.getMessage().indexOf("no such domain") != -1){
    			m_store.connect(m_host,m_port,m_strUserNameFull,m_password);
    		}else{
    			throw e;
    		}
    	}
    	
    	
    	// initialize the smtp transfer
    	//
    	m_sysProps_send.put("mail.smtp.auth", "true");
    	m_sysProps_send.put("mail.smtp.port", Integer.toString(m_port_send));
    	
    	if(m_protocol.indexOf("s") != -1){
    		m_sysProps_send.put("mail.smtp.starttls.enable","true");
    	}else{
    		m_sysProps_send.put("mail.smtp.starttls.enable","false");
    	}
    	
    	m_session_send = Session.getInstance(m_sysProps_send, null);
    	m_session_send.setDebug(false);
    	
    	m_sendTransport = (SMTPTransport)m_session_send.getTransport(m_protocol_send);
    	
    	// test connected
    	//
    	if(_fullTest){
    		m_sendTransport.connect(m_host_send,m_port_send,m_userName,m_password);
        	m_sendTransport.close();        		
    	}
    	
    	m_mainMgr.m_logger.LogOut("prepare Email account <" + m_strUserNameFull + "> OK" );
    	
	}
	
	public synchronized void DestroySession(){
		
		try{
			m_session = null;
			
			if(m_store != null){
				
			    m_unreadMailVector.clear();
			    
			    // wouldn't clear confirm 
			    // the DestroyConnect function will called when the CheckFolder throw 
			    // javaMail exception 
			    // and re-send when client re-connected
			    //
			    //m_unreadMailVector_confirm.clear();
			    
			    // pushed mail index vector 
			    m_vectPushedMailIndex.clear();
			    
				m_store.close();
				m_store = null;
			}	
		}catch(Exception e){}	
	}
	
	public synchronized void SetBeginFetchIndex(int _index){
		m_beginFetchIndex = _index + 1;
//		
//		try{
//			String t_iniFile = m_mainMgr + fetchMgr.fsm_configFilename;
//			
//			BufferedReader in = new BufferedReader(
//									new InputStreamReader(
//										new FileInputStream(t_iniFile),"UTF-8"));
//				
//			StringBuffer t_contain = new StringBuffer();
//			
//			String line = new String();
//			while((line = in.readLine())!= null){
//				if(line.indexOf("userFetchIndex=") != -1){
//					line = line.replaceAll("userFetchIndex=[^\n]*", "userFetchIndex=" + m_beginFetchIndex);
//				}
//				
//				t_contain.append(line + "\r\n");
//			}
//			
//			in.close();
//			
//			FileOutputStream os = new FileOutputStream(t_iniFile);
//			os.write(t_contain.toString().getBytes("UTF-8"));
//			os.flush();
//			os.close();
//			
//		}catch(Exception _e){
//			m_logger.PrinterException(_e);
//		}
	}
	
	public synchronized void PrepareRepushUnconfirmMsg(){
		
		for(int i = m_unreadMailVector_confirm.size() - 1;i >= 0 ;i--){
			
			fetchMail t_confirmMail = (fetchMail)m_unreadMailVector_confirm.elementAt(i);
			
			boolean t_add = true;
			
			for(int j = 0;j < m_unreadMailVector.size();j++){
				
				fetchMail t_sendMail = (fetchMail)m_unreadMailVector.elementAt(j);
				
				if(t_confirmMail.GetMailIndex() == t_sendMail.GetMailIndex()){
					t_add = false;
					break;
				}
			}
			
			if(t_add){
				
				final int t_maxConfirmNum = 5;
				
				if(t_confirmMail.m_sendConfirmNum < t_maxConfirmNum){
					m_unreadMailVector.add(0,t_confirmMail);
					m_mainMgr.m_logger.LogOut("load mail<" + t_confirmMail.GetMailIndex() + "> send again,wait confirm...");	
				}else{
					m_mainMgr.m_logger.LogOut("load mail<" + t_confirmMail.GetMailIndex() + "> send " + t_maxConfirmNum + " times, give up.");
				}
			}
			
		}
		
		m_unreadMailVector_confirm.removeAllElements();	
	}
	
	public synchronized void PushMsg(sendReceive _sendReceive)throws Exception{ 
				
		while(!m_unreadMailVector.isEmpty()){
			
			fetchMail t_mail = (fetchMail)m_unreadMailVector.elementAt(0); 
			
			ByteArrayOutputStream t_output = new ByteArrayOutputStream();
			
			t_output.write(msg_head.msgMail);
			t_mail.OutputMail(t_output);
			
			m_mainMgr.SendData(t_output,false);
			
			SetBeginFetchIndex(t_mail.GetMailIndex());
			
			synchronized(this){
				m_unreadMailVector.remove(0);
				m_unreadMailVector_confirm.addElement(t_mail);
			}
			
			t_mail.m_sendConfirmNum++;
			
			m_mainMgr.m_logger.LogOut("send mail<" + t_mail.GetMailIndex() + " : " + t_mail.GetSubject() + ">,wait confirm...");
		}
	}
	
	public void CreateTmpSendMailAttachFile(final RecvMailAttach _mail)throws Exception{
		
		// create new thread to send mail
		//
		m_recvMailAttach.addElement(_mail);
		
		m_mainMgr.m_logger.LogOut("send mail with attachment " + _mail.m_sendMail.GetAttachment().size());
		
		Vector t_list = _mail.m_sendMail.GetAttachment();
		
		for(int i = 0;i < t_list.size();i++){
			MailAttachment t_attachment = (MailAttachment)t_list.elementAt(i);
			
			String t_filename = GetAccountPrefix() + _mail.m_sendMail.GetSendDate().getTime() + "_" + i + ".satt";
			FileOutputStream fos = new FileOutputStream(t_filename);
			
			for(int j = 0;j < t_attachment.m_size;j++){
				fos.write(0);
			}
			
			m_mainMgr.m_logger.LogOut("store attachment " + t_filename + " size:" + t_attachment.m_size);
			
			fos.close();
		}
	}
	
	public RecvMailAttach FindAttachMail(final int _hashCode){
		// send the file...
		//
		for(int i = 0;i < m_recvMailAttach.size();i++){
			RecvMailAttach t_mail = (RecvMailAttach)m_recvMailAttach.elementAt(i);
			
			if(t_mail.m_sendMail.GetSimpleHashCode() == _hashCode){
								
				return t_mail;
			}
		}
		
		return null;
	}
	
	public void SendImmMail(final String _subject ,final String _contain,final String _from){
		
		Message msg = new MimeMessage(m_session_send);
		
		fetchMail t_mail = new fetchMail(m_mainMgr.m_convertToSimpleChar);
		t_mail.SetSubject(_subject);
		t_mail.SetContain(_contain);
		t_mail.SetFromVect(new String[]{_from});
		t_mail.SetSendToVect(new String[]{m_strUserNameFull});
		
		try{
			
			ComposeMessage(msg,t_mail,null);
			
			int t_tryTime = 0;
			while(t_tryTime++ < 5){
				m_sendTransport.connect(m_host_send,m_port_send,m_userName,m_password);
				m_sendTransport.sendMessage(msg, msg.getAllRecipients());
				m_sendTransport.close();
				break;
			}
			
		
		}catch(Exception e){}	    
		
	}
	public void ImportMail(Message m,fetchMail _mail)throws Exception{
		
		Address[] a;
		
		// FROM 
		if ((a = m.getFrom()) != null) {
			Vector t_from = _mail.GetFromVect();
			t_from.removeAllElements();
		    for (int j = 0; j < a.length; j++){
		    	t_from.addElement(DecodeName(a[j].toString(),false));
		    }
		}

		// REPLY TO
		if ((a = m.getReplyTo()) != null) {
			Vector t_vect = _mail.GetReplyToVect();
			t_vect.removeAllElements();
		    for (int j = 0; j < a.length; j++){
		    	t_vect.addElement(DecodeName(a[j].toString(),false));
		    }
		}
		
		// CC
		if( (a = m.getRecipients(Message.RecipientType.CC)) != null){
			Vector t_vect = _mail.GetCCToVect();
			t_vect.removeAllElements();
		    for (int j = 0; j < a.length; j++){
		    	t_vect.addElement(DecodeName(a[j].toString(),false));
		    }
		}
		
		// BCC
		if( (a = m.getRecipients(Message.RecipientType.BCC)) != null){
			Vector t_vect = _mail.GetBCCToVect();
			t_vect.removeAllElements();
		    for (int j = 0; j < a.length; j++){
		    	t_vect.addElement(DecodeName(a[j].toString(),false));
		    }
		}

		// TO
		if ((a = m.getRecipients(Message.RecipientType.TO)) != null) {
			Vector t_vect = _mail.GetSendToVect();
			t_vect.removeAllElements();
			
			Vector t_vectGroup = _mail.GetGroupVect();
			t_vectGroup.removeAllElements();
			
		    for (int j = 0; j < a.length; j++) {
		    	
		    	t_vect.addElement(DecodeName(a[j].toString(),false));
			    
				InternetAddress ia = (InternetAddress)a[j];
				
				if (ia.isGroup()) {
				    InternetAddress[] aa = ia.getGroup(false);
				    for (int k = 0; k < aa.length; k++){
				    	t_vectGroup.addElement(DecodeName(aa[k].toString(),false));
				    }
				}
		    }
		}
		
		String mailTitle = ""; 
		Enumeration enumerationHeaderTmp = ((MimeMessage) m).getMatchingHeaders(new String[] { "Subject" });  
		
		while (enumerationHeaderTmp.hasMoreElements()) {  
		    Header header = (Header) enumerationHeaderTmp.nextElement();  
		    mailTitle = header.getValue();
		}		
			
		if(mailTitle.indexOf("=?") != -1 && mailTitle.indexOf("?=") != 1){
			mailTitle = DecodeName(mailTitle,false);
		}else{
			if(m.getSubject() != null){
				mailTitle = m.getSubject();
			}else{
				mailTitle = fetchMail.fsm_noSubjectTile;
			}
		}
		
		// remove all \r and \n 
		// because the Blackberry ViewListenerExtended.forward or ViewListenerExtended.reply
		// method's argument Message.getSubject without \n
		// to make yuchberry can't find right orig message by subject
		//
		_mail.SetSubject(mailTitle.replaceAll("[\r\n]", ""));		
		
		Date t_date = m.getSentDate();
		if(t_date != null){
			_mail.SetSendDate(t_date);
		}
		
		
		int t_flags = 0;
		Flags.Flag[] sf = m.getFlags().getSystemFlags(); // get the system flags

		for (int i = 0; i < sf.length; i++) {
		    Flags.Flag f = sf[i];
		    if (f == Flags.Flag.ANSWERED)
		    	t_flags |= fetchMail.ANSWERED;
		    else if (f == Flags.Flag.DELETED)
		    	t_flags |= fetchMail.DELETED;
		    else if (f == Flags.Flag.DRAFT)
		    	t_flags |= fetchMail.DRAFT;
		    else if (f == Flags.Flag.FLAGGED)
		    	t_flags |= fetchMail.FLAGGED;
		    else if (f == Flags.Flag.RECENT)
		    	t_flags |= fetchMail.RECENT;
		    else if (f == Flags.Flag.SEEN)
		    	t_flags |= fetchMail.SEEN;
		    else
		    	continue;	// skip it		
		}
		
		_mail.SetFlags(t_flags);
		
		String[] hdrs = m.getHeader("X-Mailer");
		
		if (hdrs != null){
			_mail.SetXMailer(hdrs[0]);
	    }
		
		_mail.ClearAttachment();

		ImportPart(m,_mail);
	}
	
	public void ImportPart(Part p,fetchMail _mail)throws Exception{
		
		String filename = p.getFileName();
				
		/*
		 * Using isMimeType to determine the content type avoids
		 * fetching the actual content data until we need it.
		 */
		if (p.isMimeType("text/plain")) {
			
		    try{
		    	_mail.SetContain(_mail.GetContain().concat(p.getContent().toString()));
		    }catch(Exception e){
		    	_mail.SetContain(_mail.GetContain().concat("can't decode content " + e.getMessage()));
		    }	    
		    
		} else if(p.isMimeType("text/html")){
			
			try{
				String t_contain = ChangeHTMLCharset(p.getContent().toString());
								
		    	_mail.SetContain_html(_mail.GetContain_html().concat(t_contain));
		    	
		    	if(m_useAppendHTML){
				    // parser HTML append the plain text
				    //		    	
			    	_mail.SetContain(_mail.GetContain().concat("\n\n---------- HTML part convert ----------\n\n" + 
			    						fetchMgr.ParseHTMLText(t_contain,true)));
		    	}
		    	
		    }catch(Exception e){
		    	_mail.SetContain_html(_mail.GetContain_html().concat("can't decode content " + e.getMessage()));
		    }
		    
		    
		}else if (p.isMimeType("multipart/*")) {
			
		    Multipart mp = (Multipart)p.getContent();
		    int count = mp.getCount();
		    
		    for (int i = 0; i < count; i++){
		    	ImportPart(mp.getBodyPart(i),_mail);
		    }
		    
		} else if (p.isMimeType("message/rfc822")) {

			ImportPart((Part)p.getContent(),_mail);
			
		} else if(p.isMimeType("application/*")){
			
			// attachment 
			//
			InputStream is = (InputStream)p.getContent();
			int c;
			ByteArrayOutputStream t_os = new ByteArrayOutputStream();
			while ((c = is.read()) != -1){
				t_os.write(c);
			}			
			
			byte[] t_bytes = t_os.toByteArray();
			
			StoreAttachment(_mail.GetMailIndex(), _mail.GetAttachment().size(), t_bytes);
			
			_mail.AddAttachment(DecodeName(p.getFileName(),false),p.getContentType(),t_bytes.length);
			
		}else if (p instanceof MimeBodyPart){
		
			/*
			 * If we're saving attachments, write out anything that
			 * looks like an attachment into an appropriately named
			 * file.  Don't overwrite existing files to prevent
			 * mistakes.
			 */
			
		    String disp = p.getDisposition();
		    
		    // many mailers don't include a Content-Disposition
		    if (disp != null && disp.equals("ATTACHMENT")) {
		    			    	
				Vector t_vect = _mail.GetAttachment();
								
				if (filename == null){	
				    filename = "Attachment_" + t_vect.size();
				}else{
					filename = DecodeName(filename,true);
				}

			    _mail.AddAttachment(filename, 
			    					p.getContentType(),
			    					StoreAttachment(((MimeBodyPart)p),_mail.GetMailIndex(), t_vect.size()));
			    
		    }
		    
		} else {
			/*
			 * If we actually want to see the data, and it's not a
			 * MIME type we know, fetch it and check its Java type.
			 */
			Object o = p.getContent();
			
			if (o instanceof String) {
			    
				_mail.SetContain(_mail.GetContain().concat((String)o));
			    
			} else if (o instanceof InputStream) {

			    InputStream is = (InputStream)o;
			    int c;
			    ByteArrayOutputStream t_os = new ByteArrayOutputStream();
			    while ((c = is.read()) != -1){
			    	t_os.write(c);
			    }
			    
			    byte[] t_bytes = t_os.toByteArray();
			    
			    StoreAttachment(_mail.GetMailIndex(),_mail.GetAttachment().size(),t_bytes);
			    
			    _mail.AddAttachment("unknownFromat", "application/*",t_bytes.length);
			    
			} else {
				
				_mail.SetContain(_mail.GetContain().concat(o.toString()));
			}			
		}		
	}
	
	static public String ChangeHTMLCharset(String _html){
		
		final String ft_meta = "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />";
		final int t_charsetIdx = _html.indexOf("charset");
		
		if(t_charsetIdx == -1){
			
			final int t_headIdx = _html.indexOf("<head>");
			if(t_headIdx != -1){
				//
				StringBuffer str = new StringBuffer(_html);
				str.insert(t_headIdx + 6, ft_meta);
				_html = str.toString();
			}else{
				_html = ft_meta + _html;
			}
		}else{
			_html = _html.replaceAll("charset.[^\"]*", "charset=utf-8");
		}
		
		return _html;
	}
	
	

	static public String DecodeName(String _name,boolean _convert)throws Exception{
		
		if(_name == null){
			return "No Subject";
		}
		
		int t_start = _name.indexOf("=?");
		
		if(t_start != -1){
			
			int t_count = 0;
			do{
				// find the third ? to identified "=?gb2312?Q?=deinvueHF?="
				//
				int t_endStart = _name.indexOf("?",t_start + 2);
				if(t_endStart != -1){
					t_endStart = _name.indexOf("?",t_endStart + 1);
				}
				
				int t_end = t_endStart == -1?-1:_name.indexOf("?=", t_endStart + 1);
				
				if(t_end == -1){
					_name = _name.substring(0, t_start) + MimeUtility.decodeText(_name.substring(t_start));
				}else{
					
					// replace all \r \n blank char to identified "=?gb2312?Q?=dei \n \r nvueHF?=" 
					//
					_name = _name.substring(0, t_start) + 
							MimeUtility.decodeText(_name.substring(t_start,t_end + 2).replaceAll("[\r\n ]", "")) + 
							_name.substring(t_end + 2);
				}				
				
				t_start = _name.indexOf("=?");
				
			}while(t_start != -1 && t_count++ < 10);
			
		}else{
			if(_convert){
				_name = new String(_name.getBytes("ISO8859_1"));
			}			
		}
		
		return _name;
	}
	
	public void ComposeMessage(Message msg,fetchMail _mail,fetchMail _forwardMail)throws Exception{
		
		msg.setFrom(new InternetAddress(m_strUserNameFull));
				
	    msg.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(fetchMail.parseAddressList(_mail.GetSendToVect()), false));
	    if (!_mail.GetReplyToVect().isEmpty()){
			msg.setRecipients(Message.RecipientType.CC,
						InternetAddress.parse(fetchMail.parseAddressList(_mail.GetReplyToVect()), false));
	    }
	    
	    if(!_mail.GetGroupVect().isEmpty()){
	    	msg.setRecipients(Message.RecipientType.BCC,
					InternetAddress.parse(fetchMail.parseAddressList(_mail.GetGroupVect()), false));
	    }
		

	    msg.setSubject(_mail.GetSubject());
	    
	    MailIndexAttachment t_forwardMailAttach = null;
	    if(_forwardMail != null){
	    	t_forwardMailAttach = FindMailIndexAttach(_forwardMail.GetSimpleHashCode());
	    }

	    if(!_mail.GetAttachment().isEmpty() || t_forwardMailAttach != null) {
			// Attach the specified file.
			// We need a multipart message to hold the attachment.
		    	
			MimeBodyPart t_containPart = new MimeBodyPart();
			t_containPart.setText(_mail.GetContain());
			
			MimeMultipart t_mainPart = new MimeMultipart();
			t_mainPart.addBodyPart(t_containPart);
			
			Vector t_contain = _mail.GetAttachment();
			
			try{

				for(int i = 0;i< t_contain.size();i++){

					MailAttachment t_attachment = (MailAttachment)t_contain.elementAt(i);
					
					MimeBodyPart t_filePart = new MimeBodyPart();
					t_filePart.setFileName(MimeUtility.encodeText(t_attachment.m_name));

					String t_fullname = GetAccountPrefix() + _mail.GetSendDate().getTime() + "_" + i + ".satt";
					t_filePart.setContent(ReadFileBuffer( t_fullname ), t_attachment.m_type);
					
					t_mainPart.addBodyPart(t_filePart);
				}
				
				t_contain = t_forwardMailAttach.m_attachmentName;
				
				for(int i = 0;i < t_contain.size();i++){
					MailAttachment t_attachment = (MailAttachment)t_contain.elementAt(i);
										
					String t_fullname = GetAccountPrefix() + t_forwardMailAttach.m_mailIndexOrTime + "_" + i;
					
					if(t_forwardMailAttach.m_send){
						t_fullname = t_fullname + ".satt";
					}else{
						t_fullname = t_fullname + ".att";
					}
					
					File t_file = new File(t_fullname);
					
					if(t_file.exists()){
						MimeBodyPart t_filePart = new MimeBodyPart();
						t_filePart.setFileName(MimeUtility.encodeText(t_attachment.m_name));
						t_filePart.setContent(ReadFileBuffer(t_fullname), t_attachment.m_type);
						
						t_mainPart.addBodyPart(t_filePart);
					}
				}
				
			}catch(Exception _e){
				m_mainMgr.m_logger.LogOut(_e.getMessage());
			}
			
			msg.setContent(t_mainPart);
			
	    } else {
			// If the desired charset is known, you can use
			// setText(text, charset)
			msg.setText(_mail.GetContain());
	    }

	    msg.setHeader("X-Mailer",_mail.GetXMailer());
	    msg.setSentDate(_mail.GetSendDate());
	}
	
	private static byte[] ReadFileBuffer(String _file)throws Exception{
		File t_file = new File(_file);
		byte[] t_buffer = new byte[(int)t_file.length()];
		
		FileInputStream in = new FileInputStream(_file);
		in.read(t_buffer, 0, t_buffer.length);
		in.close();
		
		return t_buffer;
	}
	
	private  void StoreAttachment(int _mailIndex,int _attachmentIndex,byte[] _contain){
		String t_filename = GetAccountPrefix() + _mailIndex + "_" + _attachmentIndex + ".att";
		
		File t_file = new File(t_filename);
		if(t_file.exists() && t_file.length() == (long) _contain.length){
			return;
		}
		
		try{

			FileOutputStream fos = new FileOutputStream(t_filename);
			fos.write(_contain);
			
			fos.close();	
		}catch(Exception _e){
			m_mainMgr.m_logger.PrinterException(_e);
		}		
	}
	
	private  int StoreAttachment(MimeBodyPart p,int _mailIndex,int _attachmentIndex){
		String t_filename = GetAccountPrefix() + _mailIndex + "_" + _attachmentIndex + ".att";
		
		File t_file = new File(t_filename);		
		try{

			p.saveFile(t_file);
			
			return (int)t_file.length();
			
		}catch(Exception _e){
			m_mainMgr.m_logger.PrinterException(_e);
		}	
		
		return 0;
	}
	
}
