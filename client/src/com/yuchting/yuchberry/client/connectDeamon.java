package com.yuchting.yuchberry.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.SocketConnection;
import javax.microedition.io.file.FileConnection;

import net.rim.blackberry.api.mail.Address;
import net.rim.blackberry.api.mail.AttachmentHandler;
import net.rim.blackberry.api.mail.AttachmentHandlerManager;
import net.rim.blackberry.api.mail.BodyPart;
import net.rim.blackberry.api.mail.Folder;
import net.rim.blackberry.api.mail.Message;
import net.rim.blackberry.api.mail.MimeBodyPart;
import net.rim.blackberry.api.mail.Multipart;
import net.rim.blackberry.api.mail.SendListener;
import net.rim.blackberry.api.mail.Session;
import net.rim.blackberry.api.mail.Store;
import net.rim.blackberry.api.mail.SupportedAttachmentPart;
import net.rim.blackberry.api.mail.TextBodyPart;
import net.rim.blackberry.api.mail.Transport;
import net.rim.blackberry.api.mail.UnsupportedAttachmentPart;
import net.rim.blackberry.api.mail.BodyPart.ContentType;
import net.rim.blackberry.api.mail.event.MessageEvent;
import net.rim.blackberry.api.mail.event.MessageListener;
import net.rim.blackberry.api.mail.event.ViewListener;
import net.rim.device.api.compress.GZIPInputStream;
import net.rim.device.api.compress.GZIPOutputStream;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;


class msg_head{
	
	final public static byte msgMail = 0;
	final public static byte msgSendMail = 1;

	final public static byte msgConfirm = 2;
	final public static byte msgNote = 3;
	
	final public static byte msgBeenRead = 4;
	final public static byte msgMailAttach = 5;
	final public static byte msgFetchAttach = 6;
}

public class connectDeamon extends Thread implements SendListener,
												MessageListener,
												AttachmentHandler,
												ViewListener{
	
	class AppendMessage{
		int		m_mailIndex;
		Date	m_date;
		String	m_from;
	}
	
	 sendReceive		m_connect = null;
	 
	 String				m_hostname;
	 String				m_hostip = null;
	 
	 int				m_hostport;
	 String				m_userPassword;
	 
	 FileConnection		m_keyfile;
	 
	 SocketConnection	m_conn = null;
	 
	 Vector				m_sendingMail = new Vector();
	 Vector				m_sendingMailAttachment = new Vector();
	 
	 boolean			m_disconnect = true;
	
	 public Vector 		m_markReadVector = new Vector();
	 
	 // read the email temporary variables
	 //
	 private boolean			m_hasSupportedAttachment = false;
	 private boolean			m_hasUnsupportedAttachment = false;
	 private String			m_plainTextContain = new String();
	 private String			m_htmlTextContain = new String();
	 
	 //! current composing mail
	 Message			m_composingMail = null;
	 Vector				m_composingAttachment = new Vector();
	 
	 String				m_currStateString = new String();
	 recvMain			m_mainApp = null;
	 
	 
	 class FetchAttachment{
		 Message					m_message;
		 SupportedAttachmentPart	m_attachPart;
		 
		 int						m_mailIndex;
		 int						m_attachmentIdx;
		 int						m_size;
		 
		 int						m_completePercent;
		 
		 FileConnection				m_fileConnect;
		 
	 }
	 //! receive the attachment
	 Vector				m_vectReceiveAttach = new Vector();
	 
	 
	 public connectDeamon(recvMain _app){
		 m_mainApp = _app;
		 start();	 
	 }
	 
	 public void BeginListener()throws Exception{
		// add the send listener
		//

		Store store = Session.getDefaultInstance().getStore();
		store.addSendListener(this);
		 
		Session.getDefaultInstance().addViewListener(this);
		 
		AttachmentHandlerManager.getInstance().addAttachmentHandler(this);	      		 
	 }
	 
	 
	 public void EndListener()throws Exception{
		 
		 // add the send listener
         //
		 Store store = Session.getDefaultInstance().getStore();
         store.removeSendListener(this);
         
         Session.getDefaultInstance().removeViewListener(this);
         
         AttachmentHandlerManager.getInstance().removeAttachmentHandler(this);	      
	 }
	 
	 
	 //! SendListener
	 public boolean sendMessage(Message message){
    	
		try{
			fetchMail t_mail = new fetchMail();
			ImportMail(message,t_mail);
			
			t_mail.SetSendDate(new Date());
			
			message.setStatus(Message.Status.TX_SENDING,1);
						
			AddSendingMail(t_mail,m_composingAttachment);
			m_composingAttachment.removeAllElements();
			
		}catch(Exception _e){
			return false;
		}
		
		return true;
		
	}
	
	 
	//! MessageListener
	public void changed(MessageEvent e){
		
		if(e.getMessageChangeType() == MessageEvent.UPDATED
		|| e.getMessageChangeType() == MessageEvent.OPENED){
			
			try{
	
				AddMarkReadMail(e.getMessage());
				e.getMessage().removeMessageListener(this);
			}catch(Exception _e){}
		}
	}
	//
	
	
	//@{ AttachmentHandler
	public void run(Message m, SupportedAttachmentPart p){
		
		//m_screen.DialogAlert("hahah no reaction");
		String str = p.getContent().toString();
		int t_slpashIndex = -1;
		if(str.length() < 32 && (t_slpashIndex = str.indexOf('_')) != -1){
			final int t_split = str.indexOf(' ');
			if(t_split != -1){

				//  search the fetching attachment list
				//
				final String t_filename = str.substring(0, t_split);
				final String t_filesize = str.substring(t_split + 1, str.length());
				
				final int size = Integer.parseInt(t_filesize);

				for(int i = 0;i < m_vectReceiveAttach.size();i++){
					FetchAttachment t_att = (FetchAttachment)m_vectReceiveAttach.elementAt(i);
											
					if(t_att.m_message == m && t_att.m_attachPart == p){
						
						final String t_progress = p.getFilename() +" (" + t_att.m_completePercent + "%d) wait a moment";
						
						m_mainApp.DialogAlert(t_progress);
						
						return;
					}
				}			
				
				final int t_mailIdx = Integer.parseInt(str.substring(0,t_slpashIndex));
				final int t_attachmentIdx = Integer.parseInt(str.substring(t_slpashIndex + 1,t_split));
				
				FetchAttachment t_att = new FetchAttachment();
				
				t_att.m_mailIndex 		= t_mailIdx;
				t_att.m_attachmentIdx	= t_attachmentIdx;
				t_att.m_size			= size;
				t_att.m_attachPart		= p;
				t_att.m_message			= m;
				t_att.m_completePercent	= 0;
				
				try{
					t_att.m_fileConnect		= SendFetchAttachmentFile(t_att);
					m_vectReceiveAttach.addElement(t_att);
					
				}catch(Exception e){
					m_mainApp.DialogAlert("create the attachment file failed:\n" + e.getMessage());
				}		
			}		
		}
	}
	
	public String	menuString(){
		return "Get YuchBerry Attachment";
	}
	
	public boolean supports(String contentType){
		return true;
	}
	//@}
	
	
	
	//@{ folder listener
	public void open(MessageEvent e){
		if(e.getMessageChangeType() == MessageEvent.NEW){
			m_composingMail = e.getMessage();
			m_composingAttachment.removeAllElements();
		}
	}
	
	public void close(MessageEvent e){
		if(e.getMessageChangeType() == MessageEvent.CLOSED ){
			m_composingMail = null;
		}
	}
	
	//@}
	
	public FileConnection SendFetchAttachmentFile(FetchAttachment _att)throws Exception{
		
		ByteArrayOutputStream t_os = new ByteArrayOutputStream();
		t_os.write(msg_head.msgFetchAttach);
		sendReceive.WriteInt(t_os, _att.m_mailIndex);
		sendReceive.WriteInt(t_os, _att.m_attachmentIdx);
		
		m_connect.SendBufferToSvr(t_os.toByteArray(), true);
		
		final String t_filename = "" + _att.m_mailIndex + "_" + _att.m_attachmentIdx + ".att";
		
		FileConnection t_file = (FileConnection)Connector.open(recvMain.fsm_attachmentDir + t_filename,Connector.READ_WRITE);
		if(!t_file.exists()){
			t_file.create();
		}
		
		int t_writeIdx = 0;
		DataOutputStream out = t_file.openDataOutputStream();
		while(t_writeIdx < _att.m_size){
			out.write(0);
		}
		out.close();		
		
		return t_file;		
	}
	
	//! the attachment file selection screen(uploadFileScreen) will call
	public void AddAttachmentFile(String _filename){
		if(m_composingAttachment.indexOf(_filename) == -1){
			m_composingAttachment.addElement(_filename);
		}		
	}
	
	public void DelAttachmentFile(String _filename){
		for(int i = 0;i < m_composingAttachment.size();i++){
			
			String t_filename = (String)m_composingAttachment.elementAt(i);
			
			if(t_filename.equals(_filename)){
				m_composingAttachment.removeElementAt(i);
			}			
		}		
	}
	public final Vector GetAttachmentFile(){
		return m_composingAttachment;
	}
		
	//! refresh the attachment file html
	public void RefreshAttachmentMailContain()throws Exception {
		
		if(m_composingMail != null && !m_composingAttachment.isEmpty()){
			
			for(int i = 0;i < m_composingAttachment.size();i++){
				String t_name = (String)m_composingAttachment.elementAt(i);
				
				final int t_lastSplash = t_name.lastIndexOf('/');
				if(t_lastSplash == -1){
					throw new Exception("attachment file name error...");
				}
				
				t_name = t_name.substring(t_lastSplash + 1,t_name.length());
								
			} 
		}
	}
	 
	 public void run(){
		 		 
		while(true){

			while(m_disconnect == true){
				try{
					sleep(100);
				}catch(Exception _e){}
			}
			
			try{

				m_conn = GetConnection(false);
				m_connect = new sendReceive(m_conn.openOutputStream(),m_conn.openInputStream());
							
				// send the Auth info
				//
				ByteArrayOutputStream t_os = new ByteArrayOutputStream();
				t_os.write(msg_head.msgConfirm);
				sendReceive.WriteString(t_os, m_userPassword);
				
				m_connect.SendBufferToSvr(t_os.toByteArray(), true);
				
				// set the text connect
				//
				m_mainApp.SetStateString("connected.");
				
				while(true){
					ProcessMsg(m_connect.RecvBufferFromSvr());
				}
				
			}catch(Exception _e){
				
				try{
					m_mainApp.SetStateString("disconnected retry later...");
					m_mainApp.SetErrorString(_e.getMessage());
				}catch(Exception e){}				
			}		
						
			synchronized (this) {
				try{
					if(m_conn != null){
						m_conn.close();
						m_conn = null;
					}
					
					if(m_connect == null){
						m_connect.CloseSendReceive();
						m_connect = null;
					}				
					
				}catch(Exception _e){}
			}		
			
			try{
				sleep(10000);
			}catch(Exception _e){}
			
		}
		
	 }
	 
	 public void Connect(String _host,int _port,String _userPassword)throws Exception{

		 m_hostname		= _host;
		 m_hostport		= _port;
		 m_userPassword = _userPassword;
		 
		 BeginListener();
		 
		synchronized (this) {
			m_disconnect = false;
		}
	 }
	 
	 public boolean IsConnected(){
		 return !m_disconnect;
	 }
	 
	 public void Disconnect()throws Exception{
		 
		 m_disconnect = true;
		 
		 EndListener();
		 
		 synchronized (this) {

			 if(m_conn != null){			 
				 m_conn.close();
				 m_conn = null; 
			 }
			 
			 if(m_connect != null){
				 m_connect.CloseSendReceive();
				 m_connect = null;
			 }	 
		 }
	 }
	 
	 private SocketConnection GetConnection(boolean _ssl)throws Exception{
		 
		 String URL ;

		 // first use IP address to decrease the DNS message  
		 //		 
		 
		 if(_ssl){
			 URL =  "ssl://" + ((m_hostip != null)?m_hostip:m_hostname) + ":" + m_hostport + ";deviceside=true";
		 }else{
			 URL =  "socket://" +((m_hostip != null)?m_hostip:m_hostname) + ":" + m_hostport + ";deviceside=true";
		 }
		 
		 SocketConnection socket = null;
		 
		 try{
			 socket = (SocketConnection)Connector.open(URL,Connector.READ_WRITE,false);
			 
			 socket.setSocketOption(SocketConnection.DELAY, 0);
			 socket.setSocketOption(SocketConnection.LINGER, 0);
			 socket.setSocketOption(SocketConnection.KEEPALIVE, 2);
			 socket.setSocketOption(SocketConnection.RCVBUF, 128);
			 socket.setSocketOption(SocketConnection.SNDBUF, 128);
			 
		 }catch(Exception _e){
			 if(m_hostip != null){
				 m_hostip = null;
				 socket = GetConnection(_ssl);
			 }else{
				 throw _e;
			 }
		 } 

		 m_hostip = socket.getAddress();
		 
		 return socket;
	 }
	 
	 private synchronized void ProcessMsg(byte[] _package)throws Exception{
		 ByteArrayInputStream in  = new ByteArrayInputStream(_package);
		 
		 final int t_msg_head = in.read();
		 
		 switch(t_msg_head){
		 	case msg_head.msgMail:
		 		final Message m = new Message();
		 		
		 		fetchMail t_mail = new fetchMail();
		 		t_mail.InputMail(in);
				
				try{
					
					ComposeMessage(m,t_mail);
					
					Store store = Session.waitForDefaultSession().getStore();
					Folder folder = store.getFolder(Folder.INBOX);
					m.setInbound(true);
					m.setStatus(Message.Status.RX_RECEIVED,1);
					folder.appendMessage(m);			
					
					
					// add the message listener to send message to server
					// to remark the message is read
					//
					AppendMessage t_app = new AppendMessage();
					t_app.m_date = m.getSentDate();
					t_app.m_from = m.getFrom().getAddr();
					t_app.m_mailIndex = t_mail.GetMailIndex();
					
					m.addMessageListener(this);
					m_markReadVector.addElement(t_app);
							
				}catch(Exception _e){
					m_mainApp.SetErrorString("ComposeMessage error :\n" + _e.getMessage());
				}			
						 		
		 		break;
		 	case msg_head.msgSendMail:
		 		ProcessSentMail(in);
		 		break;
		 	case msg_head.msgNote:
		 		String t_string = sendReceive.ReadString(in);
		 		m_mainApp.SetErrorString(t_string);
		 		break;
		 	case msg_head.msgMailAttach:
		 		ProcessMailAttach(in);
		 		break;
		 }
	 }
	 
	public synchronized void ProcessSentMail(ByteArrayInputStream in)throws Exception{
		
		boolean t_succ = (in.read() == 1);
	
		long t_time = sendReceive.ReadInt(in);
		t_time |= ((long)sendReceive.ReadInt(in) << 32);
			
		
		for(int i = 0;i< m_sendingMail.size();i++){
			fetchMail t_sending = (fetchMail)m_sendingMail.elementAt(i);
			
			if(t_sending.GetSendDate().getTime() == t_time){
				if(t_succ){
					t_sending.GetAttchMessage().setStatus(Message.Status.TX_DELIVERED, 1);
				}else{
					t_sending.GetAttchMessage().setStatus(Message.Status.TX_ERROR, 1);
				}
				
				m_sendingMail.removeElementAt(i);
		
				// delete the fetchMail send deamon thread
				//
				for(int j = 0;j < m_sendingMailAttachment.size();j++){
					sendMailAttachmentDeamon t_deamon = (sendMailAttachmentDeamon)m_sendingMailAttachment.elementAt(i);
					if(t_deamon.m_sendMail == t_sending){
						m_sendingMailAttachment.removeElement(t_deamon);
						break;
					}
				}
				
				
				// delete the uploading desc string of main application
				//
				for(int j = 0 ;j < m_mainApp.m_uploadingDesc.size();j++){
					recvMain.UploadingDesc t_desc = (recvMain.UploadingDesc)m_mainApp.m_uploadingDesc.elementAt(j);
					if(t_desc.m_mail == t_sending){
					
						m_mainApp.m_uploadingDesc.removeElementAt(j);
						break;
					}
				}
				
				break;
			}
		}
		
		
	}
	 
	public synchronized void AddSendingMail(fetchMail _mail,Vector _files)throws Exception{
		
		for(int i = 0;i < m_sendingMail.size();i++){
			fetchMail t_sending = (fetchMail)m_sendingMail.elementAt(i);
			if(t_sending.GetSendDate().equals(_mail.GetSendDate())){
				return;				 
			}
		}
		 
		m_sendingMail.addElement(_mail);
		
		// load the attachment if has 
		//
		Vector t_vfileReader = null;
		
		if(!_files.isEmpty()){

			t_vfileReader = new Vector();
			
			for(int i = 0;i< _files.size();i++){
				String t_fullname = (String)_files.elementAt(i);
				
				FileConnection t_fileReader = (FileConnection) Connector.open(t_fullname,Connector.READ);
		    	if(!t_fileReader.exists()){
		    		throw new Exception("attachment file <" + t_fullname + "> not exsit!"); 
		    	}
		    	
		    	t_vfileReader.addElement(t_fileReader);
		    					
				final int t_slash_rear = t_fullname.lastIndexOf('/', t_fullname.length());
				String t_name = t_fullname.substring( t_slash_rear + 1, t_fullname.length());
				int t_size = (int)t_fileReader.fileSize();
				String t_type = null;
				
				if(uploadFileScreen.IsAudioFile(t_name)){
					
					t_type = BodyPart.ContentType.TYPE_AUDIO + "*";
					
				}else if(uploadFileScreen.IsImageFile(t_name)){
					
					t_type = BodyPart.ContentType.TYPE_IMAGE + "*";
					
				}else if(uploadFileScreen.IsTxtFile(t_name)){
					
					t_type = BodyPart.ContentType.TYPE_TEXT + "*";
				}else {
					t_type = "application/";
				}
	
				_mail.AddAttachment(t_name, t_type, t_size);
			}
			
			// reset the content of mail...
			//
			Message msg = _mail.GetAttchMessage();
			ComposeMessageContent(msg, _mail);
		}
		
		// send mail once if has not attachment 
		//
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		os.write(msg_head.msgMail);
		_mail.OutputMail(os);
		
		m_connect.SendBufferToSvr(os.toByteArray(), false);
		
		if(!_files.isEmpty()){
			m_sendingMailAttachment.addElement(new sendMailAttachmentDeamon(this, _mail, t_vfileReader));			
		}
	}
	
	public void ProcessMailAttach(InputStream in)throws Exception{
		
		final int t_mailIndex		= sendReceive.ReadInt(in);
		final int t_attachIndex	= sendReceive.ReadInt(in);
		final int t_startIndex		= sendReceive.ReadInt(in);
		final int t_size			= sendReceive.ReadInt(in);
		
		for(int i = 0;i < m_vectReceiveAttach.size();i++){
			FetchAttachment t_att = (FetchAttachment)m_vectReceiveAttach.elementAt(i);
			if(t_att.m_mailIndex == t_mailIndex && t_attachIndex == t_attachIndex){
				
				byte[] t_bytes = new byte[t_size];
				sendReceive.ForceReadByte(in, t_bytes, t_size);
				
				OutputStream t_os = t_att.m_fileConnect.openOutputStream(t_startIndex);
				t_os.write(t_bytes);
				t_os.close();
				
				if(t_startIndex + t_size >= t_att.m_size){
					// fetching attachment is over...
					//
					
					InputStream t_in = t_att.m_fileConnect.openInputStream();
					t_bytes = new byte[t_att.m_size];
					t_in.read(t_bytes);
					
					t_in.close();
					t_att.m_fileConnect.close();
					
					t_att.m_attachPart.setContent(t_bytes);
					
					m_vectReceiveAttach.removeElementAt(i);
					m_mainApp.DialogAlert("attachment fetchment over");
				}
				
				break;
			}
		}
	}
	
	public synchronized void AddMarkReadMail(Message m){
		
		for(int i = 0;i < m_markReadVector.size();i++){
		
			try{
				
				AppendMessage t_mail = (AppendMessage)m_markReadVector.elementAt(i);
				
				if(t_mail.m_date.equals(m.getSentDate())
					&& t_mail.m_from.equals(m.getFrom().getAddr())){
					
					ByteArrayOutputStream t_os = new ByteArrayOutputStream();
					t_os.write(msg_head.msgBeenRead);
					sendReceive.WriteInt(t_os, t_mail.m_mailIndex);
					
					m_connect.SendBufferToSvr(t_os.toByteArray(), false);
					
					m_markReadVector.removeElementAt(i);
				}
									
			}catch(Exception _e){
				break;
			}
		}
	}
	

	
	public void ImportMail(Message m,fetchMail _mail)throws Exception{
		
		_mail.SetAttchMessage(m);
		
		Address[] a;
		
		// FROM 
		if (m.getFrom() != null) {
			_mail.GetFromVect().removeAllElements();
			_mail.GetFromVect().addElement(m.getFrom().getAddr());
		}

		// REPLY TO
		if ((a = m.getReplyTo()) != null) {
			_mail.GetFromVect().removeAllElements();
		    for (int j = 0; j < a.length; j++){
		    	_mail.GetFromVect().addElement(composeAddress(a[j]));
		    }
		}

		// TO
		if ((a = m.getRecipients(Message.RecipientType.TO)) != null) {
			_mail.GetSendToVect().removeAllElements();
			_mail.GetGroupVect().removeAllElements();
			
		    for (int j = 0; j < a.length; j++) {
		    	_mail.GetSendToVect().addElement(composeAddress(a[j]));
		    }
		}		
		
		_mail.SetSubject(m.getSubject());
		_mail.SetSendDate(m.getSentDate());
		
		final int t_flags = m.getFlags(); // get the system flags

		int t_setFlags = 0;
		if((t_flags & Message.Flag.DELETED) != 0){
			t_setFlags |= fetchMail.DELETED;
		}
		
		if((t_flags & Message.Flag.SAVED) != 0){
			t_setFlags |= fetchMail.SEEN;
		}
		
		String[] hdrs = m.getHeader("X-Mailer");
		
		if (hdrs != null){
			_mail.SetXMailer(hdrs[0]);
	    }
		
		_mail.ClearAttachment();
		
		m_plainTextContain = "";
		m_htmlTextContain	= "";
		
		findEmailBody(m.getContent(),_mail);
		
		_mail.SetContain(m_plainTextContain);
		_mail.SetContain_html(m_htmlTextContain);
		
	}
	
	private String composeAddress(Address a){
		if(a.getName() != null){
    		return "\"" + a.getName() +"\" <" + a.getAddr() + ">";
    	}else{
    		return a.getAddr();
    	}
	}
	
	
	
	void findEmailBody(Object obj,fetchMail _mail)throws Exception{

	   //Reset the attachment flags.
		m_hasSupportedAttachment = false;
		m_hasUnsupportedAttachment = false;
	   	   
	   if(obj instanceof Multipart)
	   {
	      Multipart mp = (Multipart)obj;
	    
	      for(int count=0; count < mp.getCount(); ++count)
	      {
	        findEmailBody(mp.getBodyPart(count),_mail);
	      }
	   }
	   else if (obj instanceof TextBodyPart)
	   {
	      TextBodyPart tbp = (TextBodyPart) obj;
	      readEmailBody(tbp,_mail);
	   }
	   else if (obj instanceof MimeBodyPart)
	   {
		   MimeBodyPart mbp = (MimeBodyPart)obj;
		   
	      if (mbp.getContentType().indexOf(ContentType.TYPE_TEXT_HTML_STRING) != -1)
	      {
	        readEmailBody(mbp,_mail);
	      }
	      else if (mbp.getContentType().equals(ContentType.TYPE_MULTIPART_MIXED_STRING) ||
				   mbp.getContentType().equals(ContentType.TYPE_MULTIPART_ALTERNATIVE_STRING))
		   {    
		      //The message has attachments or we are at the top level of the message.
			   //Extract all of the parts within the MimeBodyPart message.
		      findEmailBody((MimeBodyPart)(mbp.getContent()),_mail);
		   }
	   }	   
	   else if (obj instanceof SupportedAttachmentPart)  
	   {
	      m_hasSupportedAttachment = true; 
	   }
	
	   else if (obj instanceof UnsupportedAttachmentPart) 
	   {
	      m_hasUnsupportedAttachment = true;
	   }
		
	}
	
	private void readEmailBody(MimeBodyPart mbp,fetchMail _mail)throws Exception
	{
	   //Extract the content of the message.
	   Object obj = mbp.getContent();
	   String mimeType = mbp.getContentType();
	   String body = null;
   
	   if (obj instanceof String)
	   {
	      body = (String)obj;
	   }
	   else if (obj instanceof byte[])
	   {
	      body = new String((byte[])obj);
	   }else{
		   
		   throw new Exception("error MimeBodyPart Contain type");
	   }

	   if (mimeType.indexOf(ContentType.TYPE_TEXT_PLAIN_STRING) != -1)
	   {
		   
		  m_plainTextContain = m_plainTextContain.concat(body);

	      //Determine if all of the text body part is present.
	      if (mbp.hasMore() && !mbp.moreRequestSent())
	      {
	         try
	         {
	            Transport.more((BodyPart)mbp, true);
	         }
	         catch (Exception ex)
	         {
	        	 m_mainApp.SetErrorString("Exception: " + ex.toString());
	         }
	      }
	   }
	  
	   else if (mimeType.indexOf(ContentType.TYPE_TEXT_HTML_STRING) != -1)
	   {
		   m_plainTextContain = m_htmlTextContain.concat(body);

	      //Determine if all of the HTML body part is present.
	      if (mbp.hasMore() && !mbp.moreRequestSent())
	      {
	         try
	         {
	            Transport.more((BodyPart)mbp, true);
	         }
	         catch (Exception ex)
	         { 
	        	 m_mainApp.SetErrorString("Exception: " + ex.toString());
	         }
	      }
	   }
	}
	
	private void readEmailBody(TextBodyPart tbp,fetchMail _mail)
	{
		m_plainTextContain = m_plainTextContain.concat((String)tbp.getContent());

	   if (tbp.hasMore() && !tbp.moreRequestSent())
	   {
	      try
	      {
	         Transport.more((BodyPart)tbp, true);
	      }
	      catch (Exception ex)
	      {
	    	  m_mainApp.SetErrorString("Exception: " + ex.toString());
	      }
	   }
	}
	
	static public void ComposeMessage(Message msg,fetchMail _mail)throws Exception{
		
		_mail.SetAttchMessage(msg);
		
		msg.setFrom(fetchMail.parseAddressList(_mail.GetFromVect())[0]);
				
	    msg.addRecipients(Message.RecipientType.TO,
	    				fetchMail.parseAddressList(_mail.GetSendToVect()));
	    
	    if (!_mail.GetReplyToVect().isEmpty()){
	    	  msg.addRecipients(Message.RecipientType.CC,
	    				fetchMail.parseAddressList(_mail.GetReplyToVect()));
	    }
	    
	    if(!_mail.GetGroupVect().isEmpty()){
	    	 msg.addRecipients(Message.RecipientType.BCC,
	    				fetchMail.parseAddressList(_mail.GetGroupVect()));
	    }
		

	    msg.setSubject(_mail.GetSubject());
	    msg.setHeader("X-Mailer",_mail.GetXMailer());
	    msg.setSentDate(_mail.GetSendDate());	      

	    ComposeMessageContent(msg,_mail);
	}
	
	static private void ComposeMessageContent(Message msg,fetchMail _mail)throws Exception{
		
		 if(_mail.GetContain_html().length() != 0
			|| !_mail.GetAttachment().isEmpty()) {
		
			Multipart multipart = new Multipart();
		    	
	    	TextBodyPart t_text = new TextBodyPart(multipart,_mail.GetContain());
	    	multipart.addBodyPart(t_text);
	    	
	    	if(_mail.GetContain_html().length() != 0){
	
	    		TextBodyPart t_text1 = new TextBodyPart(multipart,_mail.GetContain_html());
		    	t_text1.setContentType(ContentType.TYPE_TEXT_HTML_STRING);
		    	
	    	}
	    	
	    	if(!_mail.GetAttachment().isEmpty()){
	    		
				Vector t_contain	= _mail.GetAttachment();
				
		    	for(int i = 0;i< t_contain.size();i++){
		    		
		    		fetchMail.Attachment t_attachment = (fetchMail.Attachment)t_contain.elementAt(i);
		    		
		    		SupportedAttachmentPart attach = new SupportedAttachmentPart( multipart,
		    																	(String)t_attachment.m_type,
															    				(String)t_attachment.m_name + " (" + t_attachment.m_size / 1024 +"KB)",
															    				("" + _mail.GetMailIndex() + "_" + i + ".att " + t_attachment.m_size).getBytes());
	    		
		    		multipart.addBodyPart(attach);
		    	}
			}
			
			msg.setContent(multipart);
				
		} else {
			
			msg.setContent(_mail.GetContain());
		}
	  
	}
	
	 
}
 
