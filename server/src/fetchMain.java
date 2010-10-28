import java.util.*;
import java.io.*;

import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.search.*;
import javax.activation.*;

class fetchMail{
	
	final static int	VERSION = 1;
	    	
	final static int	ANSWERED 	= 1 << 0;
	final static int	DELETED 	= 1 << 1;
	final static int	DRAFT 		= 1 << 2;
	final static int	FLAGGED 	= 1 << 3;
	final static int	RECENT 		= 1 << 4;
	final static int	SEEN 		= 1 << 5;
	
	private int m_mailIndex = 0;
	
	private Vector<String>	m_vectFrom 		= new Vector<String>();
	private Vector<String>	m_vectReplyTo	= new Vector<String>();
	private Vector<String>	m_vectTo		= new Vector<String>();
	private Vector<String>	m_vectGroup		= new Vector<String>();
	
	private String			m_subject 		= null;
	private Date			m_sendDate 		= new Date();
	private Flags			m_flags 		= null;
	private String			m_XMailName 	= null;
	
	private String			m_contain		= null;
	
	private Vector<String>	m_vectAttachmentName = new Vector<String>();
	private Vector<byte[]>	m_vectAttachment= new Vector<byte[]>();
	
	
	
	public void SetMailIndex(int _index)throws Exception{
		if(_index <= 0){
			throw new Exception("SetMailIndex Negative");
		}
		m_mailIndex =_index;		
	}
	
	public int GetMailIndex(){
		return m_mailIndex;
	}
	
	public void ImportMail(Message m)throws Exception{
		
		Address[] a;
		
		// FROM 
		if ((a = m.getFrom()) != null) {
		    for (int j = 0; j < a.length; j++){
		    	m_vectFrom.addElement(a[j].toString());
		    }
		}

		// REPLY TO
		if ((a = m.getReplyTo()) != null) {
		    for (int j = 0; j < a.length; j++){
		    	m_vectReplyTo.addElement(a[j].toString());
		    }
		}

		// TO
		if ((a = m.getRecipients(Message.RecipientType.TO)) != null) {
		    for (int j = 0; j < a.length; j++) {
		    	
		    	m_vectTo.addElement(a[j].toString());
			    
				InternetAddress ia = (InternetAddress)a[j];
				
				if (ia.isGroup()) {
				    InternetAddress[] aa = ia.getGroup(false);
				    for (int k = 0; k < aa.length; k++){
				    	m_vectGroup.addElement(aa[k].toString());
				    }
				}
		    }
		}
		
		m_subject 	= m.getSubject();
		m_sendDate	= m.getSentDate();
		m_flags		= m.getFlags();
		
		String[] hdrs = m.getHeader("X-Mailer");
		
		if (hdrs != null){
			m_XMailName = hdrs[0];
	    }
		
		ImportPart(m);	
	}
	
	private void ImportPart(Part p)throws Exception{
		
		String filename = p.getFileName();
		/*
		 * Using isMimeType to determine the content type avoids
		 * fetching the actual content data until we need it.
		 */
		if (p.isMimeType("text/plain")) {
			
		    try{
		    	m_contain += (String)p.getContent();
		    }catch(Exception e){
		    	m_contain += "cant decode content " + e.getMessage();
		    }	    
		    
		} else if (p.isMimeType("multipart/*")) {
			
		    Multipart mp = (Multipart)p.getContent();
		    int count = mp.getCount();
		    
		    for (int i = 0; i < count; i++){
		    	ImportPart(mp.getBodyPart(i));
		    }
		    
		} else if (p.isMimeType("message/rfc822")) {

			ImportPart((Part)p.getContent());
		} else {
			/*
			 * If we actually want to see the data, and it's not a
			 * MIME type we know, fetch it and check its Java type.
			 */
			Object o = p.getContent();
			
			if (o instanceof String) {
			    
			    m_contain += (String)o;
			    
			} else if (o instanceof InputStream) {

			    InputStream is = (InputStream)o;
			    int c;
			    while ((c = is.read()) != -1){
			    	//System.out.write(c);
			    	m_contain += c;
			    }
			} else {
			    m_contain += o.toString();
			}			
		}

		/*
		 * If we're saving attachments, write out anything that
		 * looks like an attachment into an appropriately named
		 * file.  Don't overwrite existing files to prevent
		 * mistakes.
		 */
		if (!p.isMimeType("multipart") 
			&& p instanceof MimeBodyPart){
			
		    String disp = p.getDisposition();
		    
		    // many mailers don't include a Content-Disposition
		    if (disp != null && disp.equals("ATTACHMENT")) {
				if (filename == null){	
				    filename = "Attachment_" + m_vectAttachmentName.size();
				}
				
				m_vectAttachmentName.addElement(filename);
								
				
				ByteArrayOutputStream os = new ByteArrayOutputStream();
			    ((MimeBodyPart)p).writeTo(os);
			    
			    m_vectAttachment.add(os.toByteArray());				
		    }
		}
	}
	
	public void OutputMail(OutputStream _stream)throws Exception{
		
		_stream.write(VERSION);
		_stream.write(GetMailIndex());
		
		WriteStringVector(_stream,m_vectFrom);
		WriteStringVector(_stream,m_vectReplyTo);
		WriteStringVector(_stream,m_vectTo);
		WriteStringVector(_stream,m_vectGroup);
		
		WriteString(_stream,m_subject);
		_stream.write((int)m_sendDate.getTime());
		_stream.write((int)(m_sendDate.getTime() >>> 32));
		
		int t_flags = 0;
		
		Flags.Flag[] sf = m_flags.getSystemFlags(); // get the system flags

		for (int i = 0; i < sf.length; i++) {
		    Flags.Flag f = sf[i];
		    if (f == Flags.Flag.ANSWERED)
		    	t_flags |= ANSWERED;
		    else if (f == Flags.Flag.DELETED)
		    	t_flags |= DELETED;
		    else if (f == Flags.Flag.DRAFT)
		    	t_flags |= DRAFT;
		    else if (f == Flags.Flag.FLAGGED)
		    	t_flags |= FLAGGED;
		    else if (f == Flags.Flag.RECENT)
		    	t_flags |= RECENT;
		    else if (f == Flags.Flag.SEEN)
		    	t_flags |= SEEN;
		    else
		    	continue;	// skip it		
		}
		
		_stream.write(t_flags);
		
		WriteString(_stream,m_XMailName);
		WriteString(_stream,m_contain);
		WriteStringVector(_stream,m_vectAttachmentName);
		
	}
		
	public void InputMail(InputStream _stream)throws Exception{
		
		final int t_version = _stream.read();
		
		ReadStringVector(_stream,m_vectFrom);
		ReadStringVector(_stream,m_vectReplyTo);
		ReadStringVector(_stream,m_vectTo);
		ReadStringVector(_stream,m_vectGroup);
		
		m_subject = ReadString(_stream);
		long t_time = _stream.read();
		t_time |= ((long)_stream.read()) << 32;
		
		m_sendDate.setTime(t_time);
		
		m_contain = ReadString(_stream);
		
		ReadStringVector(_stream, m_vectAttachmentName);		
	}
	
	private void WriteStringVector(OutputStream _stream,Vector<String> _vect)throws Exception{
		
		final int t_size = _vect.size();
		_stream.write(t_size);
		
		for(int i = 0;i < t_size;i++){
			WriteString(_stream,_vect.get(i));
		}
	}
	
	private void WriteString(OutputStream _stream,String _string)throws Exception{
		_stream.write(_string.length());
		_stream.write(_string.getBytes());
	}
		
	private void ReadStringVector(InputStream _stream,Vector<String> _vect)throws Exception{
		
		_vect.clear();
		
		int t_size = 0;
		t_size = _stream.read();
		
		for(int i = 0;i < t_size;i++){
			_vect.addElement(ReadString(_stream));
		}
	}
	
	private String ReadString(InputStream _stream)throws Exception{
		
		byte[] t_buffer = new byte[_stream.read()];
		
		_stream.read(t_buffer);	
		return new String(t_buffer);		
	}
}


class fetchMgr{
	
	String 	m_protocol 	= null;
    String 	m_host 		= null;
    int		m_port		= 0;
    
    String 	m_inBox 	= "INBOX";
       
	String 	m_userName 	= null;
	String 	m_password 	= null;
	
	// Get a Properties object
    Properties m_sysProps = System.getProperties();

    // Get a Session object
    Session m_session 	= null;
    Store 	m_store		= null;
    	
    Vector<fetchMail> m_unreadMailVector = new Vector<fetchMail>();
    
    // pushed mail index vector 
    Vector m_pushedMailIndex = new Vector();
    
    
    int		m_beginFetchIndex 	= 0;
    int		m_totalMailCount	= 0;
    
    int		m_unreadFetchIndex	= 0;
        
	public void InitConnect(String _protocol,
							String _host,
							int _port,
							String _username,
							String _password) throws Exception{
		
		if(m_session != null){
			throw new Exception("has been initialize the session");
		}
		
    	m_session = Session.getInstance(m_sysProps, null);
    	m_session.setDebug(false);
		
    	m_protocol	= _protocol;
    	m_host		= _host;
    	m_port		= _port;
    	m_userName	= _username;
    	m_password	= _password;
    	
    	if(m_protocol == null
	    || (m_protocol != "imap" && m_protocol != "pop3" && m_protocol != "pop3s" && m_protocol != "imaps")){
	    	
    		m_protocol = "pop3";
	    }
    	
    	m_store = m_session.getStore(m_protocol);
    	m_store.connect(m_host,m_port,m_userName,m_password);
	}
	
	public int GetMailCountWhenFetched(){
		return m_totalMailCount;
	}
	
	public void SetBeginFetchIndex(int _index){
		m_beginFetchIndex = _index;
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
	
	public void CheckFolder()throws Exception{
		
		if(m_store == null){
			InitConnect(m_protocol,m_host,m_port,m_userName,m_password);
		}
		
		Folder folder = m_store.getDefaultFolder();
	    if(folder == null) {
	    	throw new Exception("Cant find default namespace");
	    }
	    
	    folder = folder.getFolder("INBOX");
	    if (folder == null) {
	    	throw new Exception("Invalid INBOX folder");
	    }
	    
	    m_unreadMailVector.clear();
	    
	    folder.open(Folder.READ_ONLY);
	   
	    if(m_totalMailCount != folder.getMessageCount()){
	    	m_totalMailCount = folder.getMessageCount();	    
		    final int t_startIndex = Math.max(m_totalMailCount - Math.min(50,m_totalMailCount) + 1,m_unreadFetchIndex);
		    
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
		    		
		    		fetchMail t_mail = new fetchMail();
		    		t_mail.SetMailIndex(i + t_startIndex);
		    		t_mail.ImportMail(t_msg);
		    		
		    		m_unreadMailVector.addElement(t_mail);
		    	}
		    }
		    		    
		    m_beginFetchIndex = t_startIndex;
		    
	    }	       
	    
	    folder.close(false);
	}
	
	public void DestroyConnect()throws Exception{
		m_session = null;
		
		if(m_store != null){
			m_store.close();
			m_store = null;
		}
	}
	
}

public class fetchMain{
	
	static String sm_protocol;
    static String sm_host;
    static int		sm_port	;
    static String sm_inBox;
    
    static boolean sm_debug 		= false;
    
	static String sm_strUserName ;
	static String sm_strPassword ;
	static String sm_strUserPassword;
	
	public static void main(String[] _arg){
		
		
		
		Properties p = new Properties(); 
		fetchMgr t_manger = new fetchMgr();
		
		while(true){

			try{
				
				FileInputStream fs = new FileInputStream("config.ini");
				
				p.load(fs);
				
				sm_protocol			= p.getProperty("protocol");
				sm_host				= p.getProperty("host");
				sm_port				= Integer.valueOf(p.getProperty("port")).intValue();
				sm_strUserName		= p.getProperty("account");
				sm_strPassword		= p.getProperty("password");
				sm_strUserPassword	= p.getProperty("userPassword");
				
				fs.close();
				p = null;
				
				t_manger.InitConnect(sm_protocol, sm_host, sm_port, 
									sm_strUserName, sm_strPassword);
				
				
				
			}catch(Exception ex){
				
				try{
					t_manger.DestroyConnect();
				}catch(Exception _e){
					System.exit(0);
				}
								
				System.out.println("Oops, got exception! " + ex.getMessage());
			    ex.printStackTrace();
			    
			    if(ex.getMessage().indexOf("Invalid credentials") != -1){
					// the password or user name is invalid..
					//
					System.out.println("the password or user name is invalid");
				}
			    
			    try{
			    	Thread.sleep(10000);
			    }catch(Exception _e){
			    	System.exit(0);
			    }
			}
		}
	}
}