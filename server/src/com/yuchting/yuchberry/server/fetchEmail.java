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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import javax.mail.util.SharedByteArrayInputStream;

import org.dom4j.Element;

import twitter4j.internal.org.json.JSONArray;
import twitter4j.internal.org.json.JSONObject;

import com.sun.mail.imap.IMAPInputStream;
import com.sun.mail.smtp.SMTPTransport;

class EmailSendAttachment extends Thread{
	
	FileInputStream		m_file;
	fetchMgr			m_mainMgr;
	int					m_fileLength;
	int					m_mailIndex;
	int					m_attachIndex;
	
	int					m_startIndex = 0;
	byte[] 				m_buffer = new byte[fsm_sendSize];
	ByteArrayOutputStream m_os = new ByteArrayOutputStream();
	
	public	boolean 	m_closeState = false;
	
	final static int	fsm_sendSize = 512;
	
	EmailSendAttachment(int _mailIndex,int _attachIdx,fetchEmail _mgr){
		
		m_mainMgr		= _mgr.m_mainMgr;
		m_mailIndex 	= _mailIndex;
		m_attachIndex 	= _attachIdx;
		
		try{
			File t_file = new File(_mgr.GetAccountPrefix() + _mailIndex +"_"+ _attachIdx + ".att");	
			m_fileLength = (int)t_file.length();
			m_file = new FileInputStream(t_file);
		}catch(Exception _e){
			m_mainMgr.m_logger.PrinterException(_e);
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
		m_os.write(m_buffer,0,t_size);
				
//		m_mainMgr.m_logger.LogOut("send msgMailAttach mailIndex:" + m_mailIndex + " attachIndex:" + m_attachIndex + " startIndex:" +
//				m_startIndex + " size:" + t_size + " first:" + (int)m_buffer[0]);
//		
		int t_waitTimer = 0;
		while(m_mainMgr.GetClientConnected() == null 
			|| m_mainMgr.GetClientConnected().m_sendReceive == null){
			
			t_waitTimer++;
			
			sleep(10000);
			
			if(t_waitTimer > 5){
				throw new Exception("Client closed when send attachment!");
			}			
		}
		
		m_mainMgr.SendData(m_os,_send);
		
		if(m_startIndex + t_size >= m_fileLength){
			
			m_mainMgr.m_logger.LogOut("sent msgMailAttach mailIndex:" + m_mailIndex + " attachIndex:" + m_attachIndex + " OK");
			
			return true;
		}
		
		m_startIndex += t_size;
		
		return false;
	}
	
	public void run(){

		while(true){
			try{
				
				if(m_closeState){
					m_mainMgr.m_logger.LogOut("client cancel mail attachment download, index:" + m_mailIndex);
					break;
				}
				
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
				
				//sleep(1000);
				
			}catch(Exception _e){
				m_mainMgr.m_logger.PrinterException(_e);
				break;
			}			
		}
		
		try{
			m_file.close();
		}catch(Exception e){}
	}
}

class RecvMailAttach{
		
	fetchMail	m_sendMail 			= null;
	fetchMail	m_forwardReplyMail	= null;
	fetchMgr	m_fetchMgr			= null;
	
	int			m_style;
	boolean	m_copyToSentFolder	= false;
	
	public RecvMailAttach(fetchMgr _mainMgr,fetchMail _sendMail,fetchMail _forwardReplyMail,int _style,boolean _copyToSentFolder){
		m_fetchMgr	= _mainMgr;
		m_sendMail = _sendMail;
		m_forwardReplyMail = _forwardReplyMail;
		
		m_style = _style;
		m_copyToSentFolder = _copyToSentFolder;
	}
	
	public void PrepareForwardReplyContain(String _signature){
		
		assert m_sendMail != null;
		
		StringBuffer t_string = new StringBuffer();
		
		if(_signature.indexOf("$contain$") != -1){
			_signature = _signature.replace("$time$", new SimpleDateFormat("yyyy-MM-dd HH:mm").format(m_sendMail.GetSendDate()));
			t_string.append(_signature.replace("$contain$",m_sendMail.GetContain()));
		}else{
			t_string.append(m_sendMail.GetContain());
			_signature = _signature.replace("$time$", new SimpleDateFormat("yyyy-MM-dd HH:mm").format(m_sendMail.GetSendDate()));
			t_string.append("\n\n" + _signature);
		}
		
		String t_originalMsgLine;
		String t_forwordErrorMsg;
		String t_forwordMsgLine;
		String t_sender;
		String t_dateTile;
		String t_dateFormat;
		String t_subject;
		String t_receiver;
		
		switch (m_fetchMgr.GetClientLanguage()) {
			case fetchMgr.CLIENT_LANG_ZH_S:
				t_originalMsgLine	= "\n\n-- 原始邮件 --\n";
				t_forwordMsgLine	= "\n\n-- 已转发邮件 --\n";
				t_forwordErrorMsg	= "！！！读取转发消息出现异常！！！";
				t_sender			= "发件人：";
				t_dateTile			= "日期：";
				t_dateFormat		= "yyyy年MM月dd日 HH:mm";
				t_subject			= "主题：";
				t_receiver			= "收件人：";
				break;
			case fetchMgr.CLIENT_LANG_ZH_T:
				t_originalMsgLine	= "\n\n-- 原始郵件 --\n";
				t_forwordMsgLine	= "\n\n-- 已轉發郵件 --\n";
				t_forwordErrorMsg	= "！！！讀取轉發消息出現異常！！！";
				t_sender			= "發件人：";
				t_dateTile			= "日期：";
				t_dateFormat		= "yyyy年MM月dd日 HH:mm";
				t_subject			= "主題：";
				t_receiver			= "收件人：";
				break;
			default:
				t_originalMsgLine 	= "\n\n-- original message --\n";
				t_forwordMsgLine	= "\n\n-- forword message --\n";
				t_forwordErrorMsg	= "！！！reading forword message exception！！！";
				t_sender			= "Sender:";
				t_dateTile			= "Date:";
				t_dateFormat		= "MM-dd yyyy HH:mm";
				t_subject			= "subject:";
				t_receiver			= "receiver:";
				break;
		}
		
		if(m_forwardReplyMail != null && m_style != fetchMail.NOTHING_STYLE){
			
			if(m_style == fetchMail.REPLY_STYLE){				
				
				if(!m_forwardReplyMail.GetContain().isEmpty() 
				|| !m_forwardReplyMail.GetContain_html().isEmpty()){
					
					t_string.append(t_originalMsgLine);
					
					try{
						if(!m_forwardReplyMail.GetFromVect().isEmpty()){
							t_string.append(m_forwardReplyMail.GetFromVect().elementAt(0)).
									append(" @").
									append(new SimpleDateFormat(t_dateFormat).format(m_forwardReplyMail.GetSendDate())).
									append("\n");
						}
						
						BufferedReader in = null;
						if(m_forwardReplyMail.GetContain_html().isEmpty()){
							in = new BufferedReader(new StringReader(m_forwardReplyMail.GetContain()));
						}else{
							in = new BufferedReader(new StringReader(m_forwardReplyMail.GetContain() + "\n\n-- HTML --\n\n" +
										fetchMgr.ParseHTMLText(m_forwardReplyMail.GetContain_html(),true)));
						}			
		 
						String line = new String();
						while((line = in.readLine())!= null){
							t_string.append("> " + line + "\n");
						}
						
					}catch(Exception e){
						t_string.append(t_forwordErrorMsg);
					}
				}
								
				
			}else if(m_style == fetchMail.FORWORD_STYLE){
				
				t_string.append(t_forwordMsgLine);

				Vector<String> t_form = m_forwardReplyMail.GetFromVect();
				
				if(!t_form.isEmpty()){
					t_string.append(t_sender+ (String)t_form.elementAt(0) + "\n");
					for(int i = 1;i < t_form.size();i++){
						t_string.append((String)t_form.elementAt(i) + "\n");
					}
				}
				
				t_string.append(t_dateTile+ new SimpleDateFormat(t_dateFormat).format(m_forwardReplyMail.GetSendDate()) + "\n");
				t_string.append(t_subject+ m_forwardReplyMail.GetSubject() + "\n");
				
				Vector<String> t_sendto = m_forwardReplyMail.GetSendToVect();
				if(!t_sendto.isEmpty()){
					t_string.append(t_receiver+ (String)t_sendto.elementAt(0) + "\n");
					for(int i = 1;i < t_sendto.size();i++){
						t_string.append((String)t_sendto.elementAt(i) + "\n");
					}
				}			
				
				t_string.append(m_forwardReplyMail.GetContain());
				
				if(!m_forwardReplyMail.GetContain_html().isEmpty()){					
					t_string.append("\n\n-- HTML --\n\n");
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
	
	public final static String	fsm_googleMapInfoFilename 	= "mapInfo.html";
	
	String		m_protocol				= null;
	String		m_host					= null;
	int			m_port					= 110;
	
	String		m_protocol_send			= null;
	String		m_host_send				= null;
	int			m_port_send				= 25;
	
    
	String 	m_userName 					= null;
	String 	m_strUserNameFull			= null;
	
	String	m_signinName				= null;
	
	String	m_sendName					= "";
	
	boolean m_useFullNameSignIn		= false;
	String 	m_password 					= null;
	String 	m_cryptPassword 			= "";
	
    boolean m_pushHistoryMsg			= false;
	
	Vector<Long> m_vectHasBeenSent		= new Vector<Long>();
	
	
	 // Get a Session object
    Session m_session 					= null;
    Store 	m_store						= null;
    
    Session m_session_send 				= null;
    SMTPTransport m_sendTransport 		= null;
        	
    Vector<fetchMail> m_unreadMailVector 			= new Vector<fetchMail>();
    Vector<fetchMail> m_unreadMailVector_confirm 	= new Vector<fetchMail>();
    
    Vector<EmailSendAttachment>	m_emailSendAttachList = new Vector<EmailSendAttachment>();
    
    final class UnreadMailMarkingData{
    	int m_simpleHashCode;
    	int m_mailIndex;
    	String m_messageID;
    	
    	public UnreadMailMarkingData(fetchMail _mail){
    		m_simpleHashCode	= _mail.GetSimpleHashCode();
    		m_mailIndex			= _mail.GetMailIndex();
    		m_messageID			= _mail.getMessageID();
    	}
    }
    
    Vector<UnreadMailMarkingData> m_unreadMailVector_marking	= new Vector<UnreadMailMarkingData>();
    
    int		m_beginFetchIndex 			= 0;
    int		m_totalMailCount			= -1;
    
    int		m_unreadFetchIndex			= 0;
    
    boolean m_useAppendHTML			= false;    
   
    private Vector<RecvMailAttach>	m_recvMailAttach 	= new Vector<RecvMailAttach>();
        
    class MailIndexAttachment{
    	int			m_mailHashCode;
    	
    	boolean	m_send;
    	long		m_mailIndexOrTime;
    	
    	Vector<fetchMail.MailAttachment>		m_attachmentName = null;
    	
    	public MailIndexAttachment(){
    		m_attachmentName = new Vector<fetchMail.MailAttachment>();
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
    
    Vector<MailIndexAttachment>	m_mailIndexAttachName = new Vector<MailIndexAttachment>();
        
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
	
	public String toString(){
		return "Email <" + GetAccountName() + ">"; 
	}
	
	public boolean IsPushHistoryMsg(){
		return m_pushHistoryMsg;
	}
	
	public void InitAccount(Element _elem)throws Exception{
		
		m_strUserNameFull					= ReadStringAttr(_elem,"account");
		if(m_strUserNameFull.indexOf('@') == -1 || m_strUserNameFull.indexOf('.') == -1){
			throw new Exception("account : xxxxx@xxx.xxx such as 1234@gmail.com");
		}
		
    	m_userName							= m_strUserNameFull.substring(0,m_strUserNameFull.indexOf('@'));
    	m_signinName						= ReadStringAttr(_elem,"signinName");
    	m_password							= ReadStringAttr(_elem,"password");

    	m_useFullNameSignIn					= ReadBooleanAttr(_elem,"useFullNameSignIn");
    	
		m_protocol							= ReadStringAttr(_elem,"protocol").toLowerCase();
		m_host								= ReadStringAttr(_elem,"host");
		m_port								= ReadIntegerAttr(_elem,"port");
		
		m_protocol_send						= ReadStringAttr(_elem,"protocol_send");
		m_host_send							= ReadStringAttr(_elem,"host_send");
		m_port_send							= ReadIntegerAttr(_elem,"port_send");
		
		m_useAppendHTML						= ReadBooleanAttr(_elem,"appendHTML");

		if(_elem.attributeValue("pushHistoryMsg") != null){
			m_pushHistoryMsg					= ReadBooleanAttr(_elem,"pushHistoryMsg");
		}else{
			if(m_protocol.indexOf("pop3") != -1){
				m_pushHistoryMsg = false;
			}else{
				m_pushHistoryMsg = true;
			}
		}
		
		m_sendName		= ReadStringAttr(_elem,"sendName");
		m_cryptPassword = ReadStringAttr(_elem,"cryptPassword");		
		
		File t_file = new File(GetAccountPrefix());
		if(!t_file.exists() || !t_file.isDirectory()){
			t_file.mkdir();
		}
		
		ReadWriteMailIndexAttach(true);
		
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
    	
		// Get a Properties object
	    Properties t_sysProps 		= new Properties();
	    Properties t_sysProps_send	= new Properties();
	    
		// initialize the session by the configure
		// IMAP/POP3
		//
    	if(m_protocol.indexOf("pop3") != -1){
    		t_sysProps.put("mail.pop3.disabletop", "true");
    		t_sysProps.put("mail.pop3s.disabletop", "true");
    	}else{
    		t_sysProps.put("mail.pop3.disabletop", "false");
    		t_sysProps.put("mail.pop3s.disabletop", "false");
    	}
    	
    	if(m_host.indexOf(".qq.com") != -1){
    		t_sysProps.put("mail.imap.auth.login.disable","true");
    	}else{
    		t_sysProps.put("mail.imap.auth.login.disable","false");
    	}
    	
    	t_sysProps.put("mail.pop3s.ssl.protocols","SSLv3");
    	t_sysProps.put("mail.imaps.ssl.protocols","SSLv3");
    	
    	t_sysProps.put("mail.imap.timeout","10000");
    	t_sysProps.put("mail.imaps.timeout","10000");
    	
    	t_sysProps.put("mail.pop3.timeout","10000");
    	t_sysProps.put("mail.pop3s.timeout","10000");
    	
    	t_sysProps.put("mail.imap.connectiontimeout","10000");
    	t_sysProps.put("mail.imaps.connectiontimeout","10000");
    	
    	t_sysProps.put("mail.pop3.connectiontimeout","10000");
    	t_sysProps.put("mail.pop3s.connectiontimeout","10000");
    	
    	// to modify issue:
    	// http://code.google.com/p/yuchberry/issues/detail?id=229
    	t_sysProps.put("mail.mime.address.strict", false);
    	    			
    	m_session = Session.getInstance(t_sysProps, null);
    	m_session.setDebug(false);
    	
    	m_store = m_session.getStore(m_protocol);
    	
    	// initialize the SMTP transfer
    	//
    	t_sysProps_send.put("mail.smtp.auth", "true");
    	t_sysProps_send.put("mail.smtp.port", Integer.toString(m_port_send));
    	t_sysProps_send.put("mail.smtp.timeout","10000");
    	t_sysProps_send.put("mail.smtp.connectiontimeout","10000");
    	
    	t_sysProps_send.remove("mail.smtp.socketFactory.class");
    	
    	if(m_protocol.indexOf("s") != -1){
    		t_sysProps_send.put("mail.smtp.starttls.enable","true");
    		
    		if(m_port_send == 465){
    			t_sysProps_send.put("mail.smtp.socketFactory.class","javax.net.ssl.SSLSocketFactory");
    		}
    		
    	}else{
    		t_sysProps_send.put("mail.smtp.starttls.enable","false");	
    	}
    	
    	m_session_send = Session.getInstance(t_sysProps_send, null);
    	m_session_send.setDebug(false);
    	
    	m_sendTransport = (SMTPTransport)m_session_send.getTransport(m_protocol_send);
	}
	
	int m_loadMessageErrorTime = 0;
	public void CheckFolder()throws Exception{
		
		if(!m_store.isConnected()){
			
			m_mainMgr.m_logger.LogOut("m_store is not connected, ResetSession first");
			
			ResetSession(true);
		}		
		
		Folder folder = m_store.getDefaultFolder();
		
	    if(folder == null) {
	    	throw new Exception("Cant find default namespace");
	    }
	    
	    folder = folder.getFolder("INBOX");
	    if (folder == null) {
	    	throw new Exception("Invalid INBOX folder");
	    }
	    	    
	    folder.open(Folder.READ_ONLY);
	    try{
	    	final int t_totalMailCount = folder.getMessageCount();
	    	
	    	if(m_totalMailCount < 0 && !IsPushHistoryMsg()){
	    		m_totalMailCount = t_totalMailCount;
	    		m_beginFetchIndex = t_totalMailCount + 1;
	    	}
	    	
	    	if(m_totalMailCount > t_totalMailCount){
	    		m_totalMailCount = t_totalMailCount;
	    		return;
	    	}
	 	    
	 	    if(m_totalMailCount != t_totalMailCount){
	 	    	
	 		    final int t_startIndex = Math.max(t_totalMailCount - Math.min(CHECK_NUM,t_totalMailCount) + 1,
	 		    									Math.min(t_totalMailCount,m_beginFetchIndex));
	 		    
	 		    Message[] t_msgs = folder.getMessages(t_startIndex, t_totalMailCount);
	 		    		    
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
	 		    			
	 		    			m_loadMessageErrorTime = 0;
	 		    			
	 		    		}catch(Exception e){
	 		    			
	 		    			m_loadMessageErrorTime++;
	 		    			
	 		    			if(m_loadMessageErrorTime >= 2){

	 		    				m_loadMessageErrorTime = 0;
	 		    				
		 		    			String t_prompt = null;
		 		    			switch (m_mainMgr.GetClientLanguage()) {
				 		   		case fetchMgr.CLIENT_LANG_ZH_S:
				 		   			t_prompt = "\nYuchBerry服务器提示：由于网络，格式等问题，读取这封邮件的时候出现了错误，需要通过其它方式查看。\n\n\n"; 
				 		   			break;
				 		   		case fetchMgr.CLIENT_LANG_ZH_T:
				 		   			t_prompt = "\nYuchBerry服务器提示：由於網絡，格式等問題，讀取這封郵件的時候出現了錯誤，需要通過其他方式查看。\n\n\n";
				 		   			break;
				 		   		default:
				 		   			t_prompt = "\nYuchBerry ImportMail Error! Please read the Mail via another way!\n\n\n";
				 		   				
				 		   		}
		 		    			
		 		    			t_mail.SetContain(t_mail.GetContain() + "\n\n\n" + e.getMessage() + t_prompt);
		 		    			
		 		    			m_mainMgr.m_logger.PrinterException(e);
		 		    			
	 		    			}else{
	 		    				throw e;
	 		    			}
	 		    		}
	 		    		 
	 		    		AddMailIndexAttach(t_mail,false);
	 		    		
	 		    		if(!addPushListMsg(t_mail)){
	 		    			// duplicate mail read the next message mail
	 		    			//
	 		    			continue;
	 		    		}	 		    	
	 		    			 		    		
	 		    		synchronized (m_unreadMailVector_marking) {
	 		    			
	 		    			// prepare the marking reading vector
	 		    			//
	 			    		if(m_unreadMailVector_marking.size() > 100){
	 			    			m_unreadMailVector_marking.removeElementAt(0);
	 			    		}
	 						m_unreadMailVector_marking.addElement(new UnreadMailMarkingData(t_mail));
	 					}
	 		    		
	 		    	}

	 		    	m_beginFetchIndex = i + t_startIndex + 1;	 		    	
	 		    }
	 		    
	 		    m_totalMailCount = t_totalMailCount;
	 		    
	 	    }
	 	    
	    }finally{
	    	try{
	    		if(folder.isOpen()){
	    			folder.close(false);
	    		}
	    	}catch(Exception e){
	    		//m_mainMgr.m_logger.LogOut("folder close exception:" + e.getMessage());
	    	}
	    	 
	    }	   
	}
	
	/**
	 * add a mail to push list
	 * @param _mail
	 * @return true if added 
	 */
	public boolean addPushListMsg(fetchMail _mail){
		
		// if some client connect very fast many times
 		// it will load same mail 
 		//
 		// check the duplicating mail
 		//
 		boolean t_hasLoad = false;
 		
 		synchronized (m_unreadMailVector) {
 			
 			for(int index = 0;index < m_unreadMailVector.size();index++ ){
    			fetchMail t_loadMail = (fetchMail)m_unreadMailVector.elementAt(index); 
    			if(t_loadMail.GetMailIndex() == _mail.GetMailIndex()){
    				t_hasLoad = true;
    				break;
    			}
    		}
    		
    		if(t_hasLoad){
    			return false;
    		}
    		
    		m_unreadMailVector.addElement(_mail);
		}
 		
 		return true;
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
				t_processed = ProcessBeenReadOrDelMail(in,false);
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
			case msg_head.msgMailDel:
				t_processed = ProcessBeenReadOrDelMail(in,true);
				break;
			case msg_head.msgMailAttCancel:
				t_processed = ProcessMailAttCancel(in);
			default:
				t_processed = false;
		}
		
		return t_processed;
	}
		
	private boolean ProcessMail(ByteArrayInputStream in)throws Exception{
		
		int t_byte = in.available();
		
		fetchMail t_mail = new fetchMail(m_mainMgr.m_convertToSimpleChar);
		t_mail.InputMail(in);
		
		for(Long t_time : m_vectHasBeenSent){
			if(t_time.longValue() == t_mail.GetSendDate().getTime()){
				
				// check the has been sent mails
				//
				m_mainMgr.m_logger.LogOut("Message<" + t_time.longValue() + "> has been sent! Ignore it.");
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				
				os.write(msg_head.msgSendMail);				
				os.write(1);
				sendReceive.WriteLong(os,t_time.longValue());
				
				m_mainMgr.SendData(os,false);
				return true;
			}
		}
				
		fetchMail t_forwardReplyMail = null;
				
		final int t_style = in.read();
		
		if(t_style != fetchMail.NOTHING_STYLE){
			
			// find the reply/forward mail
			//
			t_forwardReplyMail = new fetchMail(m_mainMgr.m_convertToSimpleChar);
			
			t_forwardReplyMail.InputMail(in);
			
			if(!doesSendThisMail(t_mail.getOwnAccount(),t_forwardReplyMail.GetSendToVect())){
				return false;
			}					
			
		}else{
			
			 if(t_mail.GetSubject().startsWith("设置签名") 
				|| t_mail.GetSubject().toLowerCase().startsWith("set signature")
				|| t_mail.GetSubject().startsWith("設置簽名") ){
				
				if(!t_mail.GetSendToVect().isEmpty() 
				&& ((String)t_mail.GetSendToVect().elementAt(0)).toLowerCase().indexOf(GetAccountName()) != -1 ){
					
					final int t_maxSignatureLength = 256;
					
					final boolean t_overMaxlength = (t_mail.GetContain().length() > t_maxSignatureLength);
					
					if(t_overMaxlength){						
						t_mail.SetContain(t_mail.GetContain().substring(0,t_maxSignatureLength));
					}
					
					FileOutputStream t_out = new FileOutputStream(m_mainMgr.GetPrefixString() + fetchEmail.fsm_signatureFilename);
					try{
						t_out.write(t_mail.GetContain().getBytes("UTF-8"));
						t_out.flush();
					}finally{
						t_out.close();
					}
					
					m_mainMgr.m_logger.LogOut("Set signature OK!");
					
					switch(m_mainMgr.GetClientLanguage()){
					case fetchMgr.CLIENT_LANG_ZH_S:
						t_mail.SetContain("YuchBerry 提示：完成设置签名！"+(t_overMaxlength?("超过最大长度:" + t_maxSignatureLength):"")+"\n\n" + t_mail.GetContain());
						break;
					case fetchMgr.CLIENT_LANG_ZH_T:
						t_mail.SetContain("YuchBerry 提示: 完成設置簽名！"+(t_overMaxlength?("超過最大長度:" + t_maxSignatureLength):"")+"\n\n" + t_mail.GetContain());
						break;
					default:
						t_mail.SetContain("YuchBerry Prompt: Set signature OK!"+(t_overMaxlength?("Over Max Length:" + t_maxSignatureLength):"")+"\n\n" + t_mail.GetContain());
						break;
					}
				}else{
					return false;
				}
				
			}else{
				
				if(!doesSendThisMail(t_mail.getOwnAccount(),t_mail.GetFromVect())){
					return false;
				}
			}
		}
		
		boolean t_copyToSentFolder = false;
		
		if(m_mainMgr.GetConnectClientVersion() >= 4){
			t_copyToSentFolder = (in.read() == 1);
		}		
		
		if(t_mail.GetAttachment().isEmpty() || m_mainMgr.GetConnectClientVersion() >= 13){
			SendMailToSvr(new RecvMailAttach(m_mainMgr,t_mail,t_forwardReplyMail,t_style,t_copyToSentFolder));
		}else{
			
			m_mainMgr.m_logger.LogOut("Create Tmp Send Maill Attach file");
			CreateTmpSendMailAttachFile(new RecvMailAttach(m_mainMgr,t_mail,t_forwardReplyMail,t_style,t_copyToSentFolder));
		}
		
		// statistics
		//
		incEmailSend();
		addEmailSendByte(t_byte);
		if(t_mail.m_hasLocationInfo){
			m_mainMgr.addGPSInfo(t_mail.m_gpsInfo);
		}
		
		return true;
	}
	
	private boolean doesSendThisMail(String _ownAccount,Vector<String> _fromAddr){
		
		boolean t_send = false;
		
		if(!_ownAccount.isEmpty()){
			// has the own account
			//
			if(_ownAccount.equals(GetAccountName()) || m_mainMgr.getEmailPushAccount(_ownAccount) == null){
				t_send = true;
			}
			
		}else if(!_fromAddr.isEmpty()){
			// from address list
			//
			fetchEmail t_account = null;
			fetchEmail t_sendAccount = null;
			for(String addr:_fromAddr){
				
				t_account = m_mainMgr.getEmailPushAccount(addr);
				
				if(t_account == this){
					// this account can send this mail
					//
					t_send = true;
					break;
				}else if(t_account != null){
					// has another email push account can send this mail
					//
					t_sendAccount = t_account;
				}
			}
			
			if(!t_send && t_sendAccount == null){
				// didn't found any send account
				//
				t_send = true;
			}
		}else if(_ownAccount.isEmpty() && _fromAddr.isEmpty()){
			// send finally if both emtpy
			//
			t_send = true;
		}
		
		return t_send;
	}
		
	private boolean ProcessMailConfirm(ByteArrayInputStream in)throws Exception{
		
		final int t_mailHash = sendReceive.ReadInt(in);
		
		m_mainMgr.m_logger.LogOut(GetAccountName() + " Check Mail simpleHash code: " + t_mailHash);
		
		synchronized(m_unreadMailVector_confirm){
			
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
	private boolean ProcessMailAttCancel(InputStream in)throws Exception{
		
		int t_mailIndex		= sendReceive.ReadInt(in);
		
		synchronized (m_emailSendAttachList) {
			
			for(int i = 0 ;i < m_emailSendAttachList.size();i++){
				EmailSendAttachment att = m_emailSendAttachList.elementAt(i);
				
				if(!att.isAlive()){
					m_emailSendAttachList.remove(i);
					i--;
				}
			}
			
			for(EmailSendAttachment att: m_emailSendAttachList){
				
				if(att.m_mailIndex == t_mailIndex){
					
					att.m_closeState = true;
					m_emailSendAttachList.remove(att);
					
					return true;
				}
			}
		}
		
		return false;
	}
	private boolean ProcessFetchMailAttach(InputStream in)throws Exception{
		
		int t_mailIndex		= sendReceive.ReadInt(in);
		int t_attachIndex	= sendReceive.ReadInt(in);
		
		File t_file = new File(GetAccountPrefix() + t_mailIndex +"_"+ t_attachIndex + ".att");
		if(t_file.exists()){
			
			synchronized (m_emailSendAttachList) {
				
				for(int i = 0 ;i < m_emailSendAttachList.size();i++){
					EmailSendAttachment att = m_emailSendAttachList.elementAt(i);
					
					if(!att.isAlive()){
						m_emailSendAttachList.remove(i);
						i--;
					}
				}
				
				m_emailSendAttachList.add(new EmailSendAttachment(t_mailIndex,t_attachIndex,this));
			}
			
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
		
		m_mainMgr.m_logger.LogOut("recv msgMailAttach time<"+ t_mail.m_sendMail.GetSendDate().getTime() + "> attIndex<" + t_attachmentIdx + "> beginIndex<" + t_segIdx + "> size:" + t_segSize);
		
		byte[] t_bytes = new byte[t_segSize];
		sendReceive.ForceReadByte(in, t_bytes, t_segSize);
		
		RandomAccessFile t_fwrite = new RandomAccessFile(t_file,"rw");
		t_fwrite.seek(t_segIdx);
		t_fwrite.write(t_bytes);
		
		t_fwrite.close();
		
		if(t_segIdx + t_segSize == t_file.length()){
					
			if((t_attachmentIdx + 1) >= t_mail.m_sendMail.GetAttachment().size()){
				
				m_recvMailAttach.removeElement(t_mail);
				
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
		
		String t_exception = "";
		
		try{
			
			SendMail(_mail);
			
		}catch(Exception _e){
						
			t_exception = _e.getMessage() + " name:" + _e.getClass().getName();
			t_succ = 0;
		}
		
		os.write(t_succ);
		
		sendReceive.WriteLong(os,_mail.m_sendMail.GetSendDate().getTime());
		
		m_mainMgr.SendData(os,true);
		m_mainMgr.m_logger.LogOut(GetAccountName() + " Mail <" +_mail.m_sendMail.GetSendDate().getTime() +  "> send " + (((t_succ == 1)?"Succ":"Failed") + t_exception));
		
		if(t_succ == 1){
			m_vectHasBeenSent.add(new Long(_mail.m_sendMail.GetSendDate().getTime()));
		}
	}
	
	private  boolean ProcessBeenReadOrDelMail(ByteArrayInputStream in,boolean _del)throws Exception{
		
		final int t_mailHashCode	= sendReceive.ReadInt(in);
		String	t_messageID = "";
		
		if(m_mainMgr.m_clientVer >= 16){
			t_messageID		= sendReceive.ReadString(in);
		}		

		boolean t_found = false;
		
		int t_mailIndex = 0;
		
		synchronized (m_unreadMailVector_marking) {
			for(UnreadMailMarkingData t_mail :m_unreadMailVector_marking){
				
				if(t_mail.m_simpleHashCode == t_mailHashCode
				|| t_messageID.equals(t_mail.m_messageID)){
					
					t_mailIndex =  t_mail.m_mailIndex;
					
					if(_del){
						m_unreadMailVector_marking.remove(t_mail);
					}
					
					t_found = true;
					
					break;					
				}
			}	
		}
		
		if(t_found){
			try{
				MarkReadOrDelMail(t_mailIndex,_del);
			}catch(Exception _e){
				m_mainMgr.m_logger.PrinterException(_e);
			}
		}
		
		return t_found;
	}
	
	/**
	 * return whether is GMail
	 */
	public boolean isGmail(){
		return m_host_send.toLowerCase().indexOf("googlemail.com") != -1
				|| m_host_send.toLowerCase().indexOf("gmail.com") != -1;
	}
	
	public void SendMail(RecvMailAttach _mail)throws Exception{
		
		MimeMessage msg = new MimeMessage(m_session_send){
			protected void updateMessageID() throws MessagingException{
				// the fetchEmail.ComposeMessage will fill the header of Message-ID
				//
				String[] hdrs = getHeader("Message-ID");
				if(hdrs == null || hdrs.length == 0){
					super.updateMessageID();
				}
			}
		};
		
		String t_signature = "";
		
		try{
			t_signature = fetchMgr.ReadSimpleIniFile(m_mainMgr.GetPrefixString() + fsm_signatureFilename,"UTF-8",null);
		}catch(Exception e){
			m_mainMgr.m_logger.PrinterException(e);
		}
		
		_mail.PrepareForwardReplyContain(t_signature);
		
		ComposeMessage(msg,_mail.m_sendMail,
					(_mail.m_style == fetchMail.FORWORD_STYLE)?_mail.m_forwardReplyMail:null,m_sendName);
		
		AddMailIndexAttach(_mail.m_sendMail,true);
	    
		int t_tryTime = 0;
		
		while(t_tryTime++ < 5){
			try{
				
				if(m_signinName != null && m_signinName.length() != 0){
					m_sendTransport.connect(m_host_send,m_port_send,m_signinName,m_password);
				}else{
					if(m_useFullNameSignIn){
						m_sendTransport.connect(m_host_send,m_port_send,m_strUserNameFull,m_password);
					}else{
						m_sendTransport.connect(m_host_send,m_port_send,m_userName,m_password);
					}
				}
				
				
				try{
					m_sendTransport.sendMessage(msg, msg.getAllRecipients());
				}finally{
					try{
						m_sendTransport.close();
					}catch(Exception e){
						m_mainMgr.m_logger.PrinterException(e);
					}			
				}
				
				try{
					
					if(_mail.m_copyToSentFolder 
					&& !isGmail()
					
					// pop3 is NOT support this operating
					//
					&& m_protocol.indexOf("pop3") == -1){
						
						// open the Sent folder and copy mail to this
						//
						Folder folder = m_store.getFolder("Sent");
						try{
							if (folder == null) {
						    	throw new Exception("Invalid INBOX folder");
						    }				    	    
						    
							// create "Sent" folder if it does not exist
							if (!folder.exists()) {
								folder.create(Folder.HOLDS_MESSAGES);						
							}
							
							// add message to "Sent" folder
							folder.appendMessages(new Message[] {msg});

							m_mainMgr.m_logger.LogOut("Copy the sent message to SENT folder.");
						}finally{
							if(folder.isOpen()){
								folder.close(false);
							}
						}	
					}
				
				}catch(Exception e){
					m_mainMgr.m_logger.PrinterException(e);
				}				
						
				break;
				
			}catch(Exception e){
				m_mainMgr.m_logger.PrinterException(e);
			}
		}
		
		if(t_tryTime >= 5){
			throw new Exception("Send the Message failed");
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
			MailIndexAttachment t_att = m_mailIndexAttachName.elementAt(i);
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
						fetchMail.MailAttachment t_att 	= new fetchMail.MailAttachment();
						
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
					sendReceive.WriteBoolean(t_fileWrite,t_att.m_send);
					sendReceive.WriteLong(t_fileWrite,t_att.m_mailIndexOrTime);
					sendReceive.WriteInt(t_fileWrite,t_att.m_attachmentName.size());
					
					for(int j = 0 ;j < t_att.m_attachmentName.size();j++){
						fetchMail.MailAttachment t_attachment = (fetchMail.MailAttachment)t_att.m_attachmentName.elementAt(j);
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
		
		
	public void MarkReadOrDelMail(int _index,boolean _del)throws Exception{
		
		if(!m_store.isConnected()){
			ResetSession(false);
		}
		
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
	    	
			if(_index <= folder.getMessageCount()){
				
				if(_del){
					folder.setFlags(_index, _index, new Flags(Flags.Flag.DELETED), true);
					
					m_mainMgr.m_logger.LogOut(GetAccountName() + " delete mail Index:"+_index);
					
				}else{
					
					folder.setFlags(_index, _index, new Flags(Flags.Flag.SEEN), true);	
					
					m_mainMgr.m_logger.LogOut(GetAccountName() + " Set index " + _index + " read ");
				}
			}			
	 	    
	    }finally{
	    	
	    	try{   		
	    			
    			if(m_protocol.indexOf("pop3") != -1 && _del){
		    		
		    		// check the 
		    		// http://www.oracle.com/technetwork/java/faq-135477.html#delpop3
		    		// for detail to delete the pop3 mail
		    		//
		    		folder.close(true);
		    		
		    	}else{
		    		folder.close(false);
		    	}    		
	    		
	    	}catch(Exception e){
	    		m_mainMgr.m_logger.LogOut("MarkReadOrDelMail folder.close exception:" + e.getMessage());
	    	}
	    	
	    	
	    }	        
	}
			
	public boolean IsConnectState(){
		return m_session != null;
	}
	
	
	public synchronized void ResetSession(boolean _fullTest)throws Exception{
				
		DestroySession();
		
		String decryptPass = decryptPassword(m_cryptPassword,m_password);
		if(decryptPass != null){
			m_password = decryptPass;
		}else{
			// haven't got the client password key
			//
			return ;
		}
		   
		if(m_signinName != null && m_signinName.length() != 0){
			m_store.connect(m_host,m_port,m_signinName,m_password);
		}else{
			if(m_useFullNameSignIn){
				m_store.connect(m_host,m_port,m_strUserNameFull,m_password);
			}else{
				m_store.connect(m_host,m_port,m_userName,m_password);
			}
		}		  	
		    	
    	// test connected
    	//
    	if(_fullTest){
    		if(m_signinName != null && m_signinName.length() != 0){
    			m_sendTransport.connect(m_host_send,m_port_send,m_signinName,m_password);
    		}else{
    			if(m_useFullNameSignIn){
    				m_sendTransport.connect(m_host_send,m_port_send,m_strUserNameFull,m_password);
    			}else{
    				m_sendTransport.connect(m_host_send,m_port_send,m_userName,m_password);
    			}
    		}
    		
        	m_sendTransport.close();        		
    	}
    	
    	m_mainMgr.m_logger.LogOut("prepare Email account <" + m_strUserNameFull + "> OK" );
	}
	
	public synchronized void DestroySession(){
		
		m_mainMgr.m_logger.LogOut("start DestroySession");

		try{
			
			if(m_store != null && m_store.isConnected()){ 
			    try{
			    	m_store.close();
			    }catch(Exception e){}
			}
			
			if(m_sendTransport != null && m_sendTransport.isConnected()){
				try{
					m_sendTransport.close();
				}catch(Exception e){}
			}
			
		}catch(Exception e){
			m_mainMgr.m_logger.PrinterException(e);
		}finally{
			m_mainMgr.m_logger.LogOut("end DestroySession");
		}	
	}
			
	public void PushMsg(sendReceive _sendReceive)throws Exception{
			
		final long t_currTime = System.currentTimeMillis();
		
		synchronized(m_unreadMailVector_confirm) {

			for(int i = m_unreadMailVector_confirm.size() - 1;i >= 0;i--){
				
				fetchMail t_confirmMail = (fetchMail)m_unreadMailVector_confirm.elementAt(i);
								
				if(Math.abs(t_currTime - t_confirmMail.m_sendConfirmTime) >= (5 * 60 * 1000) ){
					
					final int t_maxConfirmNum = 5;
					
					if(t_confirmMail.m_sendConfirmNum < t_maxConfirmNum){
						
						t_confirmMail.m_sendConfirmTime = t_currTime;
						m_unreadMailVector.add(0,t_confirmMail);
						
						m_mainMgr.m_logger.LogOut(GetAccountName() + " load mail<" + t_confirmMail.GetMailIndex() + "> send again");	
					}else{
						m_mainMgr.m_logger.LogOut(GetAccountName() + " load mail<" + t_confirmMail.GetMailIndex() + "> send " + t_maxConfirmNum + " times, give up.");
					}
			
					m_unreadMailVector_confirm.removeElement(t_confirmMail);
				
				}else{
					
					//m_mainMgr.m_logger.LogOut(GetAccountName() + " mail<" + t_confirmMail.GetMailIndex() + "> has not reach re-pushTime. currentTime<" 
					//										+ t_currTime + "> sendConfirmTime<" + t_confirmMail.m_sendConfirmTime + ">");
				}
			}	
		}
		
		ByteArrayOutputStream t_output = new ByteArrayOutputStream();
		
		synchronized (m_unreadMailVector) {

			while(!m_unreadMailVector.isEmpty()){
				
				fetchMail t_mail = (fetchMail)m_unreadMailVector.elementAt(0); 

				// send protocol data first
				//
				sendMailData(t_output,t_mail,m_mainMgr);				
				
				m_unreadMailVector.remove(0);
				t_mail.m_sendConfirmTime = System.currentTimeMillis();
				
				synchronized(m_unreadMailVector_confirm){
					m_unreadMailVector_confirm.addElement(t_mail);
				}
				
				t_mail.m_sendConfirmNum++;
				
				m_mainMgr.m_logger.LogOut(GetAccountName() + " send mail<" + t_mail.GetMailIndex() + " : "
						 				+ "simpleHash<"+ t_mail.GetSimpleHashCode() + " " + t_mail.GetSubject() + "+" + t_mail.GetSendDate().getTime() + ">,wait confirm...");
				
				// statistics
				//
				incEmailRecv();
				addEmailRecvByte(t_output.size());
				
				t_output.reset();
			}	
		}
	}
	
	/**
	 * send mail data to client by one protocol message
	 * @param _os 		data stream
	 * @param _mail		mail data
	 * @param _mgr		fetchMgr instance
	 * @throws Exception
	 */
	public static void sendMailData(ByteArrayOutputStream _os,fetchMail _mail,fetchMgr _mgr)throws Exception{
		// this function would be called in fetchMgr.SendPromptFakeMail
		// to sub-function it.
		//
		_os.write(msg_head.msgMail);
		_mail.OutputMail(_os);
		
		_mgr.SendData(_os,false);
	}
	
	public void CreateTmpSendMailAttachFile(final RecvMailAttach _mail)throws Exception{
		
		// create new thread to send mail
		//
		m_recvMailAttach.addElement(_mail);
		
		m_mainMgr.m_logger.LogOut("send mail with attachment " + _mail.m_sendMail.GetAttachment().size());
		
		Vector<fetchMail.MailAttachment> t_list = _mail.m_sendMail.GetAttachment();
		
		for(int i = 0;i < t_list.size();i++){
			fetchMail.MailAttachment t_attachment = (fetchMail.MailAttachment)t_list.elementAt(i);
			
			String t_filename = GetAccountPrefix() + _mail.m_sendMail.GetSendDate().getTime() + "_" + i + ".satt";
			FileOutputStream fos = new FileOutputStream(t_filename);
			
			for(int j = 0;j < t_attachment.m_size;j++){
				fos.write(0);
			}
			
			m_mainMgr.m_logger.LogOut(GetAccountName() + " store attachment " + t_filename + " size:" + t_attachment.m_size);
			
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
	
	public void SendImmMail(String _subject ,String _contain,String _from){
				
		fetchMail t_mail = new fetchMail(m_mainMgr.m_convertToSimpleChar);
		t_mail.SetSubject(_subject);
		t_mail.SetContain(_contain);
		t_mail.SetFromVect(new String[]{_from});
		t_mail.SetSendToVect(new String[]{m_strUserNameFull});
		
		int t_tryTime = 0;
		
		while(t_tryTime++ < 2){
			
			try{
	
				MimeMessage msg = new MimeMessage(m_session_send);
				ComposeMessage(msg,t_mail,null,"");					
				
				if(m_useFullNameSignIn){
					m_sendTransport.connect(m_host_send,m_port_send,m_strUserNameFull,m_password);
				}else{
					m_sendTransport.connect(m_host_send,m_port_send,m_userName,m_password);
				}
				
				try{
					m_sendTransport.sendMessage(msg, msg.getAllRecipients());
				}finally{
					m_sendTransport.close();
				}
				
				break;			
			
			}catch(Exception e){
				m_mainMgr.m_logger.PrinterException(e);
			}   
		}
		
	}
	
	public void ImportMail(Message m,fetchMail _mail)throws Exception{
				
		Address[] a;
				
		
		// FROM 
		if ((a = m.getFrom()) != null) {
			Vector<String> t_from = _mail.GetFromVect();
			t_from.removeAllElements();
		    for (int j = 0; j < a.length; j++){
		    	t_from.addElement(DecodeName(a[j].toString(),false));
		    }
		}

		// REPLY TO
		if ((a = m.getReplyTo()) != null) {
			Vector<String> t_vect = _mail.GetReplyToVect();
			t_vect.removeAllElements();
		    for (int j = 0; j < a.length; j++){
		    	t_vect.addElement(DecodeName(a[j].toString(),false));
		    }
		}
		
		// CC
		if( (a = m.getRecipients(Message.RecipientType.CC)) != null){
			Vector<String> t_vect = _mail.GetCCToVect();
			t_vect.removeAllElements();
		    for (int j = 0; j < a.length; j++){
		    	t_vect.addElement(DecodeName(a[j].toString(),false));
		    }
		}
		
		// BCC
		if( (a = m.getRecipients(Message.RecipientType.BCC)) != null){
			Vector<String> t_vect = _mail.GetBCCToVect();
			t_vect.removeAllElements();
		    for (int j = 0; j < a.length; j++){
		    	t_vect.addElement(DecodeName(a[j].toString(),false));
		    }
		}

		// TO
		if ((a = m.getRecipients(Message.RecipientType.TO)) != null) {
			Vector<String> t_vect = _mail.GetSendToVect();
			t_vect.removeAllElements();
			
			Vector<String> t_vectGroup = _mail.GetGroupVect();
			t_vectGroup.removeAllElements();
			
			boolean t_addCurrEmail = true;
			
		    for (int j = 0; j < a.length; j++) {
		    	
		    	String t_addr = DecodeName(a[j].toString(),false);
		    	t_vect.addElement(t_addr);
		    	
		    	if(t_addr.toLowerCase().indexOf(GetAccountName()) != -1){
		    		t_addCurrEmail = false;
		    	}
			    
				InternetAddress ia = (InternetAddress)a[j];
				
				if (ia.isGroup()) {
				    InternetAddress[] aa = ia.getGroup(false);
				    for (int k = 0; k < aa.length; k++){
				    	t_vectGroup.addElement(DecodeName(aa[k].toString(),false));
				    }
				}
		    }
		    
		    if(t_addCurrEmail){
		    	t_vect.addElement(GetAccountName());
		    }
		}
		
		String mailTitle = ""; 
		Enumeration<?> enumerationHeaderTmp = ((MimeMessage) m).getMatchingHeaders(new String[] { "Subject" });  
		
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
		mailTitle = mailTitle.replaceAll("[\r\n]", "");
		
		// convert to UTF-8 byte to compute the SimpleHashCode
		//
		mailTitle = new String(mailTitle.getBytes("UTF-8"),"UTF-8");

		_mail.SetSubject(mailTitle);	
		
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
		
		hdrs = m.getHeader("Message-ID");
		if (hdrs != null){
			_mail.setMessageID(hdrs[0]);
	    }
		
		hdrs = m.getHeader("In-Reply-To");
		if(hdrs != null){
			_mail.setInReplyTo(hdrs[0]);
		}
		
		hdrs = m.getHeader("References");
		if(hdrs != null){
			_mail.setReferenceID(hdrs[0]);
		}		
		
		_mail.setOwnAccount(m_strUserNameFull);
		_mail.ClearAttachment();

		ImportPart(m,_mail);
	}
	
	static final String fsm_defaultHTML_charset_utf8 = "TEXT/HTML;charset=UTF-8";
	static final String fsm_defaultHTML_charset_GB = "TEXT/HTML;charset=GB2312";
	
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
		    	_mail.SetContain(_mail.GetContain().concat("text/plain can't decode content " + e.getMessage()));
		    }	    
		    
		} else if(p.isMimeType("text/html")){
			
			try{
				
				String t_conString = p.getContent().toString();
				String t_contentType = p.getContentType();				
			
				if(t_contentType.toLowerCase().indexOf("gbk") != -1){
					// ths RIM os can't support gbk
					//
					t_contentType = fsm_defaultHTML_charset_utf8;
				}
				
				if(m_mainMgr.GetConnectClientVersion() >= 11){  
					
					if(m_mainMgr.GetClientOSVer().startsWith("4.2") 
					|| m_mainMgr.GetClientOSVer().startsWith("4.5")){
						
						// the 4.2 and 4.5 os can parse the <meta /> tag
						//
						t_contentType = fsm_defaultHTML_charset_GB;
						
						t_conString = t_conString.replaceAll("<meta.[^>]*>", "");
						
					}else{
						t_conString = ChangeHTMLCharset(t_conString,t_contentType);
					}
					
				}else{
					t_conString = ChangeHTMLCharset(t_conString,fsm_defaultHTML_charset_utf8);
				}
												
		    	_mail.SetContain_html(_mail.GetContain_html().concat(t_conString),t_contentType);
		    	
		    	if(m_useAppendHTML){
		    		
		    		String t_prompt = "\n\n--following converting HTML part--\n\n";
		    		
		    		switch (m_mainMgr.GetClientLanguage()) {
						case fetchMgr.CLIENT_LANG_ZH_S:
							t_prompt	= "\n\n--以下为 HTML 转换部分--\n\n";							
							break;
						case fetchMgr.CLIENT_LANG_ZH_T:
							t_prompt	= "\n\n--以下為 HTML 轉換部分--\n\n";
							break;
						
					}
				    // parser HTML append the plain text
				    //		    	
			    	_mail.SetContain(_mail.GetContain() + t_prompt + fetchMgr.ParseHTMLText(t_conString,true));
		    	}
		    	
		    }catch(Exception e){
		    	_mail.SetContain_html(_mail.GetContain_html().concat("text/html can't decode content " + e.getMessage()),"");
		    }
		    
		    
		}else if (p.isMimeType("multipart/*")) {
			
			if(p.getContent() instanceof IMAPInputStream || 
			p.getContent() instanceof SharedByteArrayInputStream){
				
				// qq mail will cause this condition
				// I can't read the string correctly so read them all to generate a string to display 
				//
				InputStream in = (InputStream)p.getContent();
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				
				int read = 0;
				while((read = in.read()) > 0){
					os.write(read);
				}
				
				byte[] t_data = os.toByteArray();
				
				try{
					_mail.SetContain(new String(t_data,"GB2312"));
				}catch(Exception e){
					_mail.SetContain(new String(t_data,"UTF-8"));
				}
				
				throw new Exception("YuchBox can't read the mail content correctly");
				
			}else{
				
				Multipart mp = (Multipart)p.getContent();
			    int count = mp.getCount();
			    
			    for (int i = 0; i < count; i++){
			    	ImportPart(mp.getBodyPart(i),_mail);
			    }
			}
		    
		    
		} else if (p.isMimeType("message/rfc822")) {

			ImportPart((Part)p.getContent(),_mail);
			
		} else if(p.isMimeType("application/*")){
			
			// attachment 
			//
			m_mainMgr.m_logger.LogOut(GetAccountName() + " start download attachFile:" + _mail.GetMailIndex() + "_" + _mail.GetAttachment().size() + ".att");
			
			InputStream is = (InputStream)p.getContent();
			int c;
			ByteArrayOutputStream t_os = new ByteArrayOutputStream();
			while ((c = is.read()) != -1){
				t_os.write(c);
			}			
			
			byte[] t_bytes = t_os.toByteArray();
			
			StoreAttachment(_mail.GetMailIndex(), _mail.GetAttachment().size(), t_bytes);
			
			Vector<fetchMail.MailAttachment> t_vect = _mail.GetAttachment();
			if (filename == null){	
			    filename = "Attachment_" + t_vect.size();
			}else{
				filename = DecodeName(filename,false);
			}
			
			_mail.AddAttachment(filename,p.getContentType(),t_bytes.length);
			
			m_mainMgr.m_logger.LogOut(GetAccountName() + " download Done!");
			
		}else if (p instanceof MimeBodyPart){
		
			/*
			 * If we're saving attachments, write out anything that
			 * looks like an attachment into an appropriately named
			 * file.  Don't overwrite existing files to prevent
			 * mistakes.
			 */
			
		    String disp = p.getDisposition();
		    
		    // many mailers don't include a Content-Disposition
		    if (disp != null && (disp.equalsIgnoreCase("ATTACHMENT") || disp.equalsIgnoreCase("INLINE")) ) {

			    /*
				 * If we actually want to see the data, and it's not a
				 * MIME type we know, fetch it and check its Java type.
				 */
				Object o = p.getContent();
				
				Vector<fetchMail.MailAttachment> t_vect = _mail.GetAttachment();
				if (filename == null){	
				    filename = "Attachment_" + t_vect.size();
				}else{
					filename = DecodeName(filename,true);
				}
				
				m_mainMgr.m_logger.LogOut(GetAccountName() + " start download attachFile:" + _mail.GetMailIndex() + "_" + _mail.GetAttachment().size() + ".att");
				
				if (o instanceof InputStream) {				
					
				    InputStream is = (InputStream)o;
				    int c;
				    ByteArrayOutputStream t_os = new ByteArrayOutputStream();
				    while ((c = is.read()) != -1){
				    	t_os.write(c);
				    }
				    
				    byte[] t_bytes = t_os.toByteArray();
				    StoreAttachment(_mail.GetMailIndex(),_mail.GetAttachment().size(),t_bytes);				   

				    _mail.AddAttachment(filename,p.getContentType(),t_bytes.length);
				    
				}else{
					
					 _mail.AddAttachment(filename,
							 			p.getContentType(),
							 			StoreAttachment((MimeBodyPart)p, _mail.GetMailIndex(), t_vect.size()));
				}
				
				m_mainMgr.m_logger.LogOut(GetAccountName() + " download Done!");
			    
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

				m_mainMgr.m_logger.LogOut(GetAccountName() + " start download attachFile:" + _mail.GetMailIndex() + "_" + _mail.GetAttachment().size() + ".att");
				
			    InputStream is = (InputStream)o;
			    int c;
			    ByteArrayOutputStream t_os = new ByteArrayOutputStream();
			    while ((c = is.read()) != -1){
			    	t_os.write(c);
			    }
			    
			    byte[] t_bytes = t_os.toByteArray();
			    
			    StoreAttachment(_mail.GetMailIndex(),_mail.GetAttachment().size(),t_bytes);
			    
			    _mail.AddAttachment("unknownFromat", "application/*",t_bytes.length);
			    
			    m_mainMgr.m_logger.LogOut(GetAccountName() + " download Done!");
			    
			} else {
				
				_mail.SetContain(_mail.GetContain().concat(o.toString()));
			}			
		}		
	}
	
	static final String fsm_htmlRegEx = "content=\"(t|T)(e|E)(x|X)(t|T)/(h|H)(t|T)(m|M)(l|L);.charset.[^\"]*";
	
	static public String ChangeHTMLCharset(String _html,String _type){		
		
		if(Pattern.compile(fsm_htmlRegEx).matcher(_html).find() == false){
			
			final String ft_meta = "<meta http-equiv=\"Content-Type\" content=\""+_type+"\" />";
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
			_html = _html.replaceAll(fsm_htmlRegEx, "content=\""+_type);
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
	
	public void ComposeMessage(MimeMessage msg,fetchMail _mail,fetchMail _forwardMail,String _sendName)throws Exception{
		
		if(_sendName.length() != 0){
			msg.setFrom(new InternetAddress(m_strUserNameFull,MimeUtility.encodeText(_sendName,"UTF-8","B")));
		}else{
			msg.setFrom(new InternetAddress(m_strUserNameFull));
		}		
	
		String t_sendTo = fetchMail.parseAddressList(_mail.GetSendToVect());
		
		m_mainMgr.m_logger.LogOut(m_mainMgr.GetPrefixString() + " ComposeMessage sendTo:'"+t_sendTo+"'");
		
		addEmailSendAddr(t_sendTo);
		
	    msg.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(t_sendTo, false));
	    
	    if (!_mail.GetCCToVect().isEmpty()){
	    	t_sendTo = fetchMail.parseAddressList(_mail.GetCCToVect());
	    	m_mainMgr.m_logger.LogOut("ComposeMessage CCTo:'"+t_sendTo+"'");
	    
	    	addEmailSendAddr(t_sendTo);
	    	
			msg.setRecipients(Message.RecipientType.CC,
						InternetAddress.parse(t_sendTo, false));			
	    }
	    
	    if(!_mail.GetBCCToVect().isEmpty()){
	    	
	    	t_sendTo = fetchMail.parseAddressList(_mail.GetBCCToVect());
	    	m_mainMgr.m_logger.LogOut("ComposeMessage BCCTo:'"+t_sendTo+"'");
	    	
	    	addEmailSendAddr(t_sendTo);
	    	
	    	msg.setRecipients(Message.RecipientType.BCC,
					InternetAddress.parse(t_sendTo, false));
	    }
		

	    msg.setSubject(_mail.GetSubject(),"UTF-8");
	    
	    MailIndexAttachment t_forwardMailAttach = null;
	    if(_forwardMail != null){
	    	t_forwardMailAttach = FindMailIndexAttach(_forwardMail.GetSimpleHashCode());
	    }

	    if(!_mail.GetAttachment().isEmpty() || t_forwardMailAttach != null || _mail.m_hasLocationInfo) {
			// Attach the specified file.
			// We need a multipart message to hold the attachment.
		    
	    	MimeMultipart t_mainPart = new MimeMultipart();
	    				
	    	if(_mail.m_hasLocationInfo){
				
				BodyPart t_htmlBodyPart = new MimeBodyPart();
				t_htmlBodyPart.setContent(GetLocationHTML(_mail), "text/html;charset=utf-8");
				t_mainPart.addBodyPart(t_htmlBodyPart);
								
			}else{
				MimeBodyPart t_containPart = new MimeBodyPart();
				t_containPart.setText(_mail.GetContain(),"UTF-8");
				
				t_mainPart.addBodyPart(t_containPart);			
			}			
			
			Vector<fetchMail.MailAttachment> t_contain = _mail.GetAttachment();
			
			try{				

				for(int i = 0;i< t_contain.size();i++){

					fetchMail.MailAttachment t_attachment = (fetchMail.MailAttachment)t_contain.elementAt(i);
					
					MimeBodyPart t_filePart = new MimeBodyPart();
					t_filePart.setFileName(MimeUtility.encodeText(t_attachment.m_name));

					String t_fullname = null;
					
					if(m_mainMgr.GetConnectClientVersion() >= 13){
						t_fullname = m_mainMgr.GetPrefixString() + _mail.GetSimpleHashCode() + "_" + i + ".satt";
					}else{
						t_fullname = GetAccountPrefix() + _mail.GetSendDate().getTime() + "_" + i + ".satt";
					}
					
					t_filePart.setContent(ReadFileBuffer( t_fullname ), t_attachment.m_type);
					
					t_mainPart.addBodyPart(t_filePart);
				}
				
				if(t_forwardMailAttach != null){
					
					t_contain = t_forwardMailAttach.m_attachmentName;
					
					for(int i = 0;i < t_contain.size();i++){
						fetchMail.MailAttachment t_attachment = (fetchMail.MailAttachment)t_contain.elementAt(i);
											
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
				}				
				
			}catch(Exception _e){
				m_mainMgr.m_logger.LogOut(_e.getMessage());
			}
			
			msg.setContent(t_mainPart);
			
	    } else {
			// If the desired charset is known, you can use
			// setText(text, charset)
			msg.setText(_mail.GetContain(),"UTF-8");
	    }

	    if(_mail.getMessageID() != null && _mail.getMessageID().length() > 0){
	    	msg.setHeader("Message-ID",_mail.getMessageID());
	    }
	    
	    if(_mail.getReferenceID() != null && _mail.getReferenceID().length() > 0){
	    	msg.setHeader("References", _mail.getReferenceID());
	    }
	    
	    if(_mail.getInReplyTo() != null && _mail.getInReplyTo().length() > 0){
	    	msg.setHeader("In-Reply-To",_mail.getInReplyTo());
	    }
	    
	    msg.setHeader("X-Mailer",_mail.GetXMailer());
	    msg.setSentDate((_mail.GetSendDate().getTime() == 0)?(new Date()):_mail.GetSendDate());

	}
	
	private String GetLocationHTML(fetchMail _mail){
		
		String t_ret = _mail.GetContain();
		
		try{
			// load the location information
			//	    	
			BufferedReader in = new BufferedReader(
									new InputStreamReader(
											new FileInputStream(fsm_googleMapInfoFilename),"UTF-8"));
		
			StringBuilder t_stringBuffer = new StringBuilder();
			String t_line = null;
			
			while((t_line = in.readLine()) != null){
				
				t_line = t_line.replace("$map_y$",Double.toString(_mail.m_gpsInfo.m_longitude));
				t_line = t_line.replace("$map_x$",Double.toString(_mail.m_gpsInfo.m_latitude));
				
				t_line = t_line.replace("$mail_content$",_mail.GetContain());
				t_line = t_line.replace("$mail_sign$","YBBer:I'm Here!");
				
				t_stringBuffer.append(t_line).append("\n");
			}
			
			t_ret = t_stringBuffer.toString();
			
		}catch(Exception e){
			m_mainMgr.m_logger.LogOut(e.getMessage());
		}
		
		return t_ret;
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
			try{
				fos.write(_contain);
			}finally{
				fos.close();
			}
				
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
	
	
	// statistics
	//
	int					m_stat_emailSend = 0;
	int					m_stat_emailRecv = 0;
	int					m_stat_emailSendB = 0;
	int					m_stat_emailRecvB = 0;
	
	Vector<String>		m_stat_emailSendAddr = new Vector<String>();
		
	public void addEmailSendAddr(String _addr){
		_addr = _addr.toLowerCase();
		
		synchronized (m_stat_emailSendAddr) {
			for(String a:m_stat_emailSendAddr){
				if(a.equals(_addr)){
					return;
				}
			}
			
			m_stat_emailSendAddr.add(_addr);
		}
	}
		
	public synchronized void incEmailSend(){
		m_stat_emailSend++;
	}
	
	public synchronized void incEmailRecv(){
		m_stat_emailRecv++;
	}
	
	public synchronized void addEmailSendByte(int _add){
		m_stat_emailSendB += _add;
	}
	
	public synchronized void addEmailRecvByte(int _add){
		m_stat_emailRecvB += _add;
	}
	
	public void fillStatJSON(JSONObject _obj)throws Exception{
		
		JSONArray t_sendEmailAddr = new JSONArray();
		synchronized (m_stat_emailSendAddr) {
			for(String addr:m_stat_emailSendAddr){
				t_sendEmailAddr.put(addr);
			}
			
			m_stat_emailSendAddr.clear();
		}
		
		_obj.put("Account",GetAccountName());
		_obj.put("AccountS",t_sendEmailAddr);
		_obj.put("Send",m_stat_emailSend );
		_obj.put("Recv",m_stat_emailRecv );
		_obj.put("SendB",m_stat_emailSendB / 1024);
		_obj.put("RecvB",m_stat_emailRecvB / 1024);
		
		synchronized (this) {
			m_stat_emailSend = 0;
			m_stat_emailRecv = 0;
			m_stat_emailSendB = 0;
			m_stat_emailRecvB = 0;
		}
	}
	
}
