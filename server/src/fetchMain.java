import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyStore;
import java.util.Properties;
import java.util.Vector;

import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

import com.sun.mail.smtp.SMTPTransport;

class berrySvrPush extends Thread{
	
	berrySvrDeamon		m_serverDeamon;
	sendReceive			m_sendReceive;
	
	public berrySvrPush(berrySvrDeamon _svrDeamon)throws Exception{
		m_serverDeamon = _svrDeamon;
		m_sendReceive = new sendReceive(m_serverDeamon.m_socket.getOutputStream(),
										m_serverDeamon.m_socket.getInputStream());
		
		start();
	}
	
	public void run(){
		
		while(true){
			
			try{
				m_serverDeamon.m_fetchMgr.CheckFolder();
								
				if(!m_serverDeamon.isAlive() 
					|| !m_serverDeamon.m_socket.isConnected()){
					
					break;
				}
								
				wait(fetchMain.sm_pushInterval);

				Vector t_unreadMailVector = m_serverDeamon.m_fetchMgr.m_unreadMailVector;
				for(int i = 0;i < t_unreadMailVector.size();i++){
					fetchMail t_mail = (fetchMail)t_unreadMailVector.elementAt(i); 
					
					ByteArrayOutputStream t_output = new ByteArrayOutputStream();
					
					t_output.write(msg_head.msgMail);
					t_mail.OutputMail(t_output);
					
					berrySvrDeamon.prt("CheckFolder OK and send mail!");
					
					m_sendReceive.SendBufferToSvr(t_output.toByteArray(),false);				
				}				
				
			}catch(Exception _e){
				_e.printStackTrace();
				break;
			}
			
		}
		
		m_sendReceive.CloseSendReceive();
	}
	
}

class berrySvrDeamon extends Thread{
	
	public fetchMgr		m_fetchMgr = null;
	public Socket		m_socket = null;
	
	private berrySvrPush m_pushDeamon = null;
	private sendReceive  m_sendReceive = null;
		
	boolean m_confirmConnect	= false;
	
		
	public berrySvrDeamon(Socket _s,fetchMgr _mgr)throws Exception{
		m_fetchMgr 	= _mgr;
		m_socket	= _s;

		try{
			m_pushDeamon = new berrySvrPush(this);
			
			m_sendReceive = new sendReceive(m_socket.getOutputStream(),m_socket.getInputStream());	
		}catch(Exception _e){
			prt("construct berrySvrDeamon error " + _e.getMessage());
			_e.printStackTrace();
			throw _e;
		}finally{
			if(m_sendReceive != null){
				m_sendReceive.CloseSendReceive();
			}
		}
		
		start();
		
		prt("some client connect");
	}
	
	static void prt(String s) {
		System.out.println(s);
	}
	
	
	public void run(){
		
		// loop
		//
		while(true){
			
			if(!m_fetchMgr.IsConnected()){
				break;
			}
			
			// process....
			//
			try{
									
				byte[] t_package = m_sendReceive.RecvBufferFromSvr();
				
				ProcessPackage(t_package);
				
			}catch(Exception _e){
				try{
					m_socket.close();
				}catch(Exception e){
					prt(e.getMessage());
					e.printStackTrace();
				}
				
				m_sendReceive.CloseSendReceive();
				
				prt(_e.getMessage());
				_e.printStackTrace();				
				
				break;
			}
		}

	}
	
	private void ProcessPackage(byte[] _package)throws Exception{
		ByteArrayInputStream in = new ByteArrayInputStream(_package);
		
		final int t_msg_head = in.read();
		
		if(m_confirmConnect == false){
			if(msg_head.msgConfirm != t_msg_head 
			|| !sendReceive.ReadString(in).equals(fetchMain.sm_strUserPassword)){
				
				ByteArrayOutputStream os = new ByteArrayOutputStream ();
				os.write(msg_head.msgNote);
				sendReceive.WriteString(os, msg_head.noteErrorUserPassword);
				
				m_sendReceive.SendBufferToSvr(os.toByteArray(), true);
				
				throw new Exception( msg_head.noteErrorUserPassword);				
			}
			
			m_confirmConnect = true;
		}
		
		switch(t_msg_head){			
			case msg_head.msgMail:
				ProcessMail(in);
				break;
			case msg_head.msgSendMail:
				m_fetchMgr.SetBeginFetchIndex(sendReceive.ReadInt(in) + 1);
				break;
			default:
				throw new Exception("illegal client connect");
		}
	}
	
	private void ProcessMail(ByteArrayInputStream in)throws Exception{
		fetchMail t_mail = new fetchMail();
		t_mail.InputMail(in);
		
		m_fetchMgr.SendMail(t_mail);
		
		// receive send message to berry
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		os.write(msg_head.msgSendMail);
		sendReceive.WriteInt(os,(int)t_mail.GetSendDate().getTime());
		sendReceive.WriteInt(os,(int)(t_mail.GetSendDate().getTime() >>> 32));
		
		m_sendReceive.SendBufferToSvr(os.toByteArray(),false);
	}
}

class fetchMgr{
	
	final static int	ACCEPT_PORT = 9716;
	
	final static int	CHECK_NUM = 50;
	
	String 	m_protocol 	= null;
    String 	m_host 		= null;
    int		m_port		= 0;
    
    String 	m_protocol_send 	= null;
    String 	m_host_send 		= null;
    int		m_port_send			= 0;
    
    String 	m_inBox 	= "INBOX";
       
	String 	m_userName 	= null;
	String 	m_password 	= null;
	String	m_userPassword	= null;
	
	// Get a Properties object
    Properties m_sysProps = System.getProperties();
    Properties m_sysProps_send = System.getProperties();
    

    // Get a Session object
    Session m_session 	= null;
    Store 	m_store		= null;
    
    
    Session m_session_send 	= null;
    SMTPTransport m_sendTransport = null;
    
    	
    Vector m_unreadMailVector = new Vector();
    
    Vector	m_vectConnect = new Vector();
    
    // pushed mail index vector 
    Vector m_vectPushedMailIndex = new Vector();
    
    int		m_beginFetchIndex 	= 0;
    int		m_totalMailCount	= 0;
    
    int		m_unreadFetchIndex	= 0;
    
        
	public void InitConnect(String _protocol,
							String _host,
							int _port,
							String _protocol_send,
							String _host_send,
							int _port_send,
							String _username,
							String _password,
							String _userPassword) throws Exception{

    	DestroyConnect();
    	
		if(m_session != null){
			throw new Exception("has been initialize the session");
		}
		
    	m_session = Session.getInstance(m_sysProps, null);
    	m_session.setDebug(false);
		
    	m_protocol	= _protocol;
    	m_host		= _host;
    	m_port		= _port;
    	
    	m_protocol_send	= _protocol_send;
    	m_host_send		= _host_send;
		m_port_send		= _port_send;
    	
    	m_userName	= _username;
    	m_password	= _password;
    	m_userPassword = _userPassword;
    	
    	m_beginFetchIndex = fetchMain.sm_fetchIndex;
    	
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
    	
		
    	m_store = m_session.getStore(m_protocol);
    	m_store.connect(m_host,m_port,m_userName,m_password);
    	
    	// initialize the smtp transfer
    	//
    	m_sysProps_send.put("mail.smtp.auth", "true");
    	m_sysProps_send.put("mail.smtp.port", Integer.toString(m_port_send));
    	m_sysProps_send.put("mail.smtp.starttls.enable","true");
    	
    	m_session_send = Session.getInstance(m_sysProps_send, null);
    	m_session_send.setDebug(false);
    	
    	m_sendTransport = (SMTPTransport)m_session_send.getTransport(m_protocol_send);
    	   	
    	   	
    	//
    	//
    	ServerSocket t_svr = GetSocketServer(m_userPassword,false);
    	
    	berrySvrDeamon.prt("prepare account OK");
    	    	
		while(true){
			try{
    			m_vectConnect.addElement(new berrySvrDeamon(t_svr.accept(),this));
			}catch(Exception _e){
				
//				for(int i = 0;i < m_vectConnect.size();i++){
//					berrySvrDeamon d = m_vectConnect.get(i);
//					if(d.m_socket.isClosed()){
//						d.destroy();
//										
//						m_vectConnect.remove(i);
//						
//						i--;
//					}
//				}			
	    	}
    	}
    		
	}
	
	public int GetMailCountWhenFetched(){
		return m_totalMailCount;
	}
	
	public synchronized void SetBeginFetchIndex(int _index){
		m_beginFetchIndex = _index;
		
		try{
			
			Properties p = new Properties(); 
			p.load(new FileInputStream("config.ini"));
			p.setProperty("userFetchIndex",Integer.toString(_index));
			
			p.save(new FileOutputStream("config.ini"), "");
			p.clear();
			
		}catch(Exception _e){
			//prt(_e.getMessage());
			_e.printStackTrace();
		}
		
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
		    final int t_startIndex = Math.max(m_totalMailCount - Math.min(CHECK_NUM,m_totalMailCount) + 1,m_beginFetchIndex);
		    
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
		    		ImportMail(t_msg,t_mail);
		    		
		    		m_unreadMailVector.addElement(t_mail);
		    	}
		    }
		    		    
		    
	    }	       
	    
	    folder.close(false);
	}
	
	public void SendMail(fetchMail _mail)throws Exception{
		
		Message msg = new MimeMessage(m_session_send);
		ComposeMessage(msg,_mail);
	    
		m_sendTransport.connect(m_host_send,fetchMain.sm_strUserNameFull,m_password);
		m_sendTransport.sendMessage(msg, msg.getAllRecipients());
		m_sendTransport.close();
	}
	
	
	
	public void DestroyConnect()throws Exception{
		m_session = null;
		
		if(m_store != null){
			
		    m_unreadMailVector.clear();
		    m_vectConnect.clear();
		    
		    // pushed mail index vector 
		    m_vectPushedMailIndex.clear();
		    
			m_store.close();
			m_store = null;
		}
	}
		
	public boolean IsConnected(){
		return m_session != null;
	}
	
	public static ServerSocket GetSocketServer(String _userPassword,boolean _ssl)throws Exception{
		
		if(_ssl){
			String	key				= "YuchBerryKey";  
			
			char[] keyStorePass		= _userPassword.toCharArray();
			char[] keyPassword		= _userPassword.toCharArray();
			
			KeyStore ks				= KeyStore.getInstance(KeyStore.getDefaultType());
			
			ks.load(new FileInputStream(key),keyStorePass);
			
			KeyManagerFactory kmf	= KeyManagerFactory.getInstance("SunX509");
			kmf.init(ks,keyPassword);
			
			SSLContext sslContext = SSLContext.getInstance("SSLv3");
			sslContext.init(kmf.getKeyManagers(),null,null);
			  
			SSLServerSocketFactory factory=sslContext.getServerSocketFactory();
			
			return (ServerSocket)factory.createServerSocket(ACCEPT_PORT);
		}else{
			return new ServerSocket(ACCEPT_PORT);
		}		  
	}
	
	static public void ImportMail(Message m,fetchMail _mail)throws Exception{
		
		Address[] a;
		
		// FROM 
		if ((a = m.getFrom()) != null) {
			Vector t_from = _mail.GetFromVect();
			t_from.removeAllElements();
		    for (int j = 0; j < a.length; j++){
		    	t_from.addElement(a[j].toString());
		    }
		}

		// REPLY TO
		if ((a = m.getReplyTo()) != null) {
			Vector t_vect = _mail.GetReplyToVect();
			t_vect.removeAllElements();
		    for (int j = 0; j < a.length; j++){
		    	t_vect.addElement(a[j].toString());
		    }
		}

		// TO
		if ((a = m.getRecipients(Message.RecipientType.TO)) != null) {
			Vector t_vect = _mail.GetSendToVect();
			t_vect.removeAllElements();
			
			Vector t_vectGroup = _mail.GetGroupVect();
			t_vectGroup.removeAllElements();
			
		    for (int j = 0; j < a.length; j++) {
		    	
		    	t_vect.addElement(a[j].toString());
			    
				InternetAddress ia = (InternetAddress)a[j];
				
				if (ia.isGroup()) {
				    InternetAddress[] aa = ia.getGroup(false);
				    for (int k = 0; k < aa.length; k++){
				    	t_vectGroup.addElement(aa[k].toString());
				    }
				}
		    }
		}
		
		_mail.SetSubject(m.getSubject());
		_mail.SetSendDate(m.getSentDate());
		
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
		_mail.GetAttachment().removeAllElements();
		_mail.GetAttachmentFilename().removeAllElements();
		
		ImportPart(m,_mail);	
	}
	
	static private void ImportPart(Part p,fetchMail _mail)throws Exception{
		
		String filename = p.getFileName();
		
		String t_getContain = _mail.GetContain();
		/*
		 * Using isMimeType to determine the content type avoids
		 * fetching the actual content data until we need it.
		 */
		if (p.isMimeType("text/plain")) {
			
		    try{
		    	t_getContain += (String)p.getContent();
		    }catch(Exception e){
		    	t_getContain += "cant decode content " + e.getMessage();
		    }	    
		    
		} else if (p.isMimeType("multipart/*")) {
			
		    Multipart mp = (Multipart)p.getContent();
		    int count = mp.getCount();
		    
		    for (int i = 0; i < count; i++){
		    	ImportPart(mp.getBodyPart(i),_mail);
		    }
		    
		} else if (p.isMimeType("message/rfc822")) {

			ImportPart((Part)p.getContent(),_mail);
		} else {
			/*
			 * If we actually want to see the data, and it's not a
			 * MIME type we know, fetch it and check its Java type.
			 */
			Object o = p.getContent();
			
			if (o instanceof String) {
			    
			    t_getContain += (String)o;
			    
			} else if (o instanceof InputStream) {

			    InputStream is = (InputStream)o;
			    int c;
			    while ((c = is.read()) != -1){
			    	//System.out.write(c);
			    	t_getContain += c;
			    }
			} else {
				t_getContain += o.toString();
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
			
			Vector t_vectName = _mail.GetAttachmentFilename();
			Vector t_vectByte = _mail.GetAttachment();
			
		    String disp = p.getDisposition();
		    
		    // many mailers don't include a Content-Disposition
		    if (disp != null && disp.equals("ATTACHMENT")) {
				if (filename == null){	
				    filename = "Attachment_" + t_vectName.size();
				}
				
				t_vectName.addElement(filename);
				
				ByteArrayOutputStream os = new ByteArrayOutputStream();
			    ((MimeBodyPart)p).writeTo(os);
			    
			    t_vectByte.addElement(os.toByteArray());				
		    }
		}
	}
	
	static public void ComposeMessage(Message msg,fetchMail _mail)throws Exception{
		
		msg.setFrom(new InternetAddress(fetchMain.sm_strUserNameFull));
				
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

	    if(!_mail.GetAttachmentFilename().isEmpty()) {
			// Attach the specified file.
			// We need a multipart message to hold the attachment.
		    	
			MimeBodyPart t_containPart = new MimeBodyPart();
			t_containPart.setText(_mail.GetContain());
			
			MimeMultipart t_mainPart = new MimeMultipart();
			t_mainPart.addBodyPart(t_containPart);
			
			Vector t_filename = _mail.GetAttachmentFilename();
			Vector t_contain = _mail.GetAttachment();
			
			for(int i = 0;i< t_contain.size();i++){

				MimeBodyPart t_filePart = new MimeBodyPart();
				
				t_filePart.setFileName((String)t_filename.elementAt(i));
				
				t_filePart.getInputStream().read((byte[])t_contain.elementAt(i));
				
				t_mainPart.addBodyPart(t_filePart);
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
}

public class fetchMain{
	
	static String sm_protocol;
    static String sm_host;
    static int		sm_port	;
    
    static String sm_protocol_send;
    static String sm_host_send;
    static int		sm_port_send	;
    
    static String sm_inBox;
    
    static boolean sm_debug 		= false;
    
	static String sm_strUserName ;
	static String sm_strUserNameFull ;
	static String sm_strPassword ;
	static String sm_strUserPassword;
	static int		sm_pushInterval = 10000;
	
	static int		sm_fetchIndex = 1;
	
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
				
				sm_protocol_send	= p.getProperty("protocol_send");
				sm_host_send		= p.getProperty("host_send");
				sm_port_send		= Integer.valueOf(p.getProperty("port_send")).intValue();
				
				sm_strUserNameFull		= p.getProperty("account");
				if(sm_strUserNameFull.indexOf('@') == -1 || sm_strUserNameFull.indexOf('.') == -1){
					throw new Exception("account : xxxxx@xxx.xxx such as 1234@gmail.com");
				}
				
				sm_strUserName = sm_strUserNameFull.substring(0, sm_strUserNameFull.indexOf('@'));
				
				sm_strPassword		= p.getProperty("password");
				sm_strUserPassword	= p.getProperty("userPassword");
				
				sm_fetchIndex		= Integer.valueOf(p.getProperty("userFetchIndex")).intValue();
				
				sm_pushInterval		= Integer.valueOf(p.getProperty("pushInterval")).intValue() * 1000;
				
				fs.close();
				p.clear();
				
				p = null;
				
				t_manger.InitConnect(sm_protocol, sm_host, sm_port, 
									sm_protocol_send, sm_host_send, sm_port_send, 
									sm_strUserName, sm_strPassword,sm_strUserPassword);
				
				
			}catch(Exception ex){
								
				System.out.println("Oops, got exception! " + ex.getMessage());
			    ex.printStackTrace();
			    
			    if(ex.getMessage().indexOf("Invalid credentials") != -1){
					// the password or user name is invalid..
					//
					System.out.println("the password or user name is invalid");
				}
			    
			    try{
			    	Thread.sleep(10000);
			    }catch(InterruptedException e){
			    	System.exit(0);
			    }
			}
			
			try{
				t_manger.DestroyConnect();
			}catch(Exception _e){
				System.exit(0);
			}
		}
	}
}