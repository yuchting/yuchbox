package com.yuchting.yuchberry.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.SocketConnection;
import javax.microedition.io.file.FileConnection;

import local.localResource;
import net.rim.blackberry.api.browser.Browser;
import net.rim.blackberry.api.browser.BrowserSession;
import net.rim.blackberry.api.homescreen.HomeScreen;
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
import net.rim.blackberry.api.mail.BodyPart.ContentType;
import net.rim.blackberry.api.mail.event.FolderEvent;
import net.rim.blackberry.api.mail.event.FolderListener;
import net.rim.blackberry.api.mail.event.MessageEvent;
import net.rim.blackberry.api.mail.event.MessageListener;
import net.rim.blackberry.api.mail.event.ViewListener;
import net.rim.blackberry.api.mail.event.ViewListenerExtended;
import net.rim.device.api.i18n.Locale;
import net.rim.device.api.io.Base64OutputStream;
import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.UiApplication;

public class connectDeamon extends Thread implements SendListener,
												MessageListener,
												FolderListener,
												AttachmentHandler,
												ViewListener,
												ViewListenerExtended{
		
	final static int	fsm_clientVer = 3;
	 
	sendReceive		m_connect = null;
	
	String				m_hostip = null;
	
	 
	FileConnection		m_keyfile;
	 
	SocketConnection	m_conn 					= null;
	 
	Vector				m_sendingMailAttachment = new Vector();
	
	boolean			m_systemStart			= false;
	boolean			m_disconnect 			= true;
	
	public Vector 		m_markReadVector 		= new Vector();
	 
		 
	boolean			m_sendAboutText			= false;
	boolean			m_recvAboutText			= false;
	 
	 	 
	// read the email temporary variables
	// 
	private String			m_plainTextContain 	= new String();
	private String			m_htmlTextContain 	= new String();
	 
	final class ComposingAttachment{
		String m_filename;
		int	m_fileSize;
		 
		ComposingAttachment(String _filename,int _size){
			m_filename = _filename;
			m_fileSize = _size;
		}
	}
	 
	//! current composing mail
	Message			m_composingMail 		= null;
	Message			m_forwordReplyMail 		= null;
	int				m_sendStyle				= fetchMail.NOTHING_STYLE;
	 
	Vector				m_composingAttachment 	= new Vector();
	 
	String				m_currStateString 		= new String();
	recvMain			m_mainApp 				= null;
	 
	boolean			m_sendAuthMsg			= false;
	 
	 
	int				m_ipConnectCounter 		= 10;
	 
	int				m_connectCounter		= -1;
	 
	class FetchAttachment{
		int						m_messageHashCode;
		 
		int						m_mailIndex;
		int						m_attachmentIdx;
		int						m_attachmentSize;
		 
		String						m_realName;
		int						m_completePercent;
		 
		ByteArrayOutputStream		m_fileContainBuffer = new ByteArrayOutputStream();
		 
	}
	 
	//! receive the attachment
	Vector				m_vectReceiveAttach = new Vector();
	 
	//! the listening folder to add message
	Folder				m_listeningMessageFolder = null;
	Folder				m_listeningMessageFolder_out = null;
	
	String m_currentVersion = null;
	 
	public connectDeamon(recvMain _app){
		m_mainApp = _app;
		m_currentVersion = ApplicationDescriptor.currentApplicationDescriptor().getVersion();
		start();
	}
	 
	 
	public void BeginListener()throws Exception{
		// add the send listener
		//
		Store store = Session.getDefaultInstance().getStore();
		store.addSendListener(this);
		
		Session t_session  = Session.getDefaultInstance();
		t_session.addViewListener(this);
						 
		AttachmentHandlerManager.getInstance().addAttachmentHandler(this);
		
		m_listeningMessageFolder = GetDefaultFolder();
		m_listeningMessageFolder.addFolderListener(this);
		
		m_listeningMessageFolder_out = GetDefaultOutFolder();
		m_listeningMessageFolder_out.addFolderListener(this	);
	}
	 
	 
	public void EndListener()throws Exception{
		
		if(m_listeningMessageFolder != null){
			
			// add the send listener
			//
			Store store = Session.getDefaultInstance().getStore();
			store.removeSendListener(this);
			 
			Session.getDefaultInstance().removeViewListener(this);
					         
	        AttachmentHandlerManager.getInstance().removeAttachmentHandler(this);
        
	        m_listeningMessageFolder.removeFolderListener(this);       			
			m_listeningMessageFolder = null;
			
			m_listeningMessageFolder_out.removeFolderListener(this);
			m_listeningMessageFolder_out = null;
        }        
	}
	 
	 
	//! SendListener
	public boolean sendMessage(Message message){
    	
		final Message t_msg = message;
		
		m_mainApp.m_messageApplication = UiApplication.getUiApplication();
			
		m_mainApp.invokeAndWait(new Runnable(){
			
			 public void run(){
				 
				 try{
						
					m_mainApp.SetErrorString("sendMsg:" + t_msg.getSubject());
					
					fetchMail t_mail = new fetchMail();
					ImportMail(t_msg,t_mail);
										
					fetchMail t_forwardReplyMail = null;
					if(m_sendStyle != fetchMail.NOTHING_STYLE && m_forwordReplyMail != null){
						t_forwardReplyMail = new fetchMail();
						ImportMail(m_forwordReplyMail,t_forwardReplyMail);
					}
																
					AddSendingMail(t_mail,m_composingAttachment,t_forwardReplyMail,m_sendStyle);
					m_composingAttachment.removeAllElements();
								
				}catch(Exception _e){
					m_mainApp.SetErrorString("sMsg: " + _e.getMessage() + " " + _e.getClass().getName());
				}
			 }
		});
		
		return true;
	}
	
	 
	//@{ MessageListener
	public void changed(MessageEvent e){
		
		if(e.getMessageChangeType() == MessageEvent.UPDATED
		|| e.getMessageChangeType() == MessageEvent.OPENED){
			
			try{
				
				m_mainApp.StopNotification();
				
				AddMarkReadMail(e.getMessage());
				e.getMessage().removeMessageListener(this);
				
			}catch(Exception _e){}
			
		}
		
	}
	//@}
	
	//@{ FolderListener
	public void messagesAdded(FolderEvent e){}
	
	public void messagesRemoved(FolderEvent e){
		if(e.getType() == FolderEvent.MESSAGE_REMOVED){
			if(AddMarkReadMail(e.getMessage())){
				m_mainApp.StopNotification();
			}
			
			try{
				
				for(int i = 0;i < m_sendingMailAttachment.size();i++){
					sendMailAttachmentDeamon t_mail = (sendMailAttachmentDeamon)m_sendingMailAttachment.elementAt(i);
					if(t_mail.m_sendMail.GetSendDate().equals(e.getMessage().getSentDate())
					&& ((String)t_mail.m_sendMail.GetFromVect().elementAt(0)).indexOf(e.getMessage().getFrom().getAddr()) != -1){
						
						t_mail.m_closeState = true;
						if(t_mail.isAlive()){
							t_mail.interrupt();
						}
						
						
						break;
					}
				}
				
			}catch(Exception ex){
				m_mainApp.SetErrorString("D:"+ex.getMessage());
			}
			
		}
	}
	//@}
	
	
	//@{ AttachmentHandler
	public void run(Message m, SupportedAttachmentPart p){

		String t_attName = p.getFilename();
		
		if(t_attName.equals(recvMain.sm_local.getString(localResource.HTML_PART_FILENAME))){
			
			try{
				
				final ByteArrayOutputStream output = new ByteArrayOutputStream();
	            final Base64OutputStream boutput = new Base64OutputStream( output );
	            output.write( "data:text/html;base64,".getBytes() );
	            boutput.write( (byte[])p.getContent() );
	            boutput.flush();
	            boutput.close();
	            output.flush();
	            output.close();
	            
	            BrowserSession browserSession = Browser.getDefaultSession();
				browserSession.displayPage(output.toString());	
				
			}catch(Exception e){
				m_mainApp.DialogAlert("open the attachment file failed:\n" + e.getMessage());
			}
			
			return;
		}
		
		final int t_messageCode = (m.getSentDate().toString() + m.getSubject()).hashCode();
		ByteArrayInputStream in = new ByteArrayInputStream((byte[])p.getContent());
		
		if(in.read() == 'y' && in.read() == 'u' 
			&& in.read() == 'c' && in.read() == 'h'){
		
			try{

				final int t_mailIndex 		= sendReceive.ReadInt(in);
				final int t_attachIndex	= sendReceive.ReadInt(in);
				final int t_attachSize		= sendReceive.ReadInt(in);
				final String t_realName		= sendReceive.ReadString(in);
				
				final String t_filename = m_mainApp.m_attachmentDir + t_realName;
				FileConnection t_file = (FileConnection)Connector.open(t_filename,Connector.READ_WRITE);
				
				if(t_file.exists()){
					
					m_mainApp.PushViewFileScreen(t_filename);
					
				}else{
					
					m_mainApp.PopupDownloadFileDlg(t_realName);
					
					// fetch from the server 
					//
					for(int i = 0;i < m_vectReceiveAttach.size();i++){
						FetchAttachment t_att = (FetchAttachment)m_vectReceiveAttach.elementAt(i);
												
						if(t_att.m_messageHashCode == t_messageCode){
							// found...
							//
							m_mainApp.m_downloadDlg.RefreshProgress(t_att);
							return;
						}
					}				
					
					FetchAttachment t_att = new FetchAttachment();
					
					t_att.m_mailIndex 		= t_mailIndex;
					t_att.m_attachmentIdx	= t_attachIndex;
					t_att.m_attachmentSize	= t_attachSize;
					t_att.m_messageHashCode	= t_messageCode;
					t_att.m_realName		= t_realName;
					t_att.m_completePercent	= 0;
					
					SendFetchAttachmentFile(t_att);
					
					m_vectReceiveAttach.addElement(t_att);
				}
				
				t_file.close();
				
				
			}catch(Exception e){
				m_mainApp.DialogAlert("open the attachment file failed:\n" + e.getMessage());
			}			
		}else{
			m_mainApp.DialogAlert("the attachment has been fetched from the another server.");
		}
	}
	
	public String	menuString(){
		return recvMain.sm_local.getString(localResource.OPEN_ATTACHMENT);
	}
	
	public boolean supports(String contentType){
		return true;
	}
	//@}
	
	
	
	//@{ ViewListener
	public void open(MessageEvent e){
		m_mainApp.StopNotification();
	}
	
	public void close(MessageEvent e){
		
		if(e.getMessageChangeType() == MessageEvent.CLOSED ){
			m_composingMail = null;
			m_forwordReplyMail = null;
			m_sendStyle = fetchMail.NOTHING_STYLE;
		}
		
		m_mainApp.StopNotification();
	}
	//@}
	
	//@{ ViewListenerExtended
	public void forward(MessageEvent e){
		m_composingMail = e.getMessage();
		m_composingAttachment.removeAllElements();
		
		m_forwordReplyMail = FindOrgMessage(e.getMessage(),fetchMail.FORWORD_STYLE);
		
		m_sendStyle = fetchMail.FORWORD_STYLE;
	}

	public void newMessage(MessageEvent e){
		m_composingMail = e.getMessage();
		m_composingAttachment.removeAllElements();
		
		m_sendStyle = fetchMail.NOTHING_STYLE;
	}
	public void reply(MessageEvent e){
		m_composingMail = e.getMessage();
		m_composingAttachment.removeAllElements();
	
		m_forwordReplyMail = FindOrgMessage(e.getMessage(),fetchMail.REPLY_STYLE);
		
		m_sendStyle = fetchMail.REPLY_STYLE;
	}
	//@}
		
	public Message FindOrgMessage(Message _message,int _style){
		
		Message t_org = null;
		Store store = Session.getDefaultInstance().getStore();
		try{
			String t_messageSub = _message.getSubject();			
			String t_trimString = null;
			if(_style == fetchMail.REPLY_STYLE){
				final int t_code = Locale.getDefaultForSystem().getCode();
				
				switch(t_code){
				case Locale.LOCALE_zh_CN:
				case Locale.LOCALE_zh:
				case Locale.LOCALE_zh_HK:
					t_trimString = "答复： ";
					break;
				default:
					t_trimString = "Re: ";
					break;
				}
				
				final int t_prefixIndex = t_messageSub.indexOf(t_trimString);
				if(t_prefixIndex != -1){
					t_messageSub = t_messageSub.substring(t_prefixIndex + t_trimString.length());
				}
				
			}else{
				
				t_trimString = _message.forward().getSubject();
				
				final int t_start = t_trimString.indexOf(t_messageSub);
				if(t_start != -1 && t_start != 0){
					t_trimString = t_trimString.substring(0,t_start);
					t_messageSub = t_messageSub.substring(t_trimString.length());
				}
			}
				
			Folder[] t_folders = store.list();
			for(int i = 0 ;i < t_folders.length;i++){
				Message[] t_messages = t_folders[i].getMessages();
				
				// backword search the message
				for(int j = t_messages.length - 1;j >= 0 ;j++){
					
					final String t_sub = t_messages[j].getSubject();
					if(t_messageSub.equals(t_sub)){
						
						if(_style == fetchMail.REPLY_STYLE){
							String t_from = t_messages[j].getFrom().getAddr();
							
							Address[] t_replyTo = _message.getRecipients(Message.RecipientType.TO);
							for(int index = 0;index < t_replyTo.length;index++){
								if(t_replyTo[index].getAddr().equals(t_from)){
									t_org = t_messages[j];
									break;
								}
							}
							
							if(t_org != null){
								break;
							}
						}else{
							t_org = t_messages[j];
							break;
						}
						
					}
				}
				
				if(t_org != null){
					break;
				}
			}
			
		}catch(Exception e){
			
		}
		
		return t_org;
	}
	
	public void SendFetchAttachmentFile(FetchAttachment _att)throws Exception{
		
		ByteArrayOutputStream t_os = new ByteArrayOutputStream();
		t_os.write(msg_head.msgFetchAttach);
		sendReceive.WriteInt(t_os, _att.m_mailIndex);
		sendReceive.WriteInt(t_os, _att.m_attachmentIdx);
		
		m_connect.SendBufferToSvr(t_os.toByteArray(), true);
			
	}
	
	//! the attachment file selection screen(uploadFileScreen) will call
	public void AddAttachmentFile(String _filename,int _fileSize){
		for(int i = 0;i < m_composingAttachment.size();i++){
			ComposingAttachment t_att = (ComposingAttachment)m_composingAttachment.elementAt(i);
			if(t_att.equals(_filename)){
				return;
			}
		}
		
		m_composingAttachment.addElement(new ComposingAttachment(_filename,_fileSize));
	}
	
	public void DelAttachmentFile(String _filename){
		for(int i = 0;i < m_composingAttachment.size();i++){
			
			ComposingAttachment t_att = (ComposingAttachment)m_composingAttachment.elementAt(i);
			
			if(t_att.m_filename.equals(_filename)){
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
				String t_name = ((ComposingAttachment)m_composingAttachment.elementAt(i)).m_filename;
				
				final int t_lastSplash = t_name.lastIndexOf('/');
				if(t_lastSplash == -1){
					throw new Exception("attachment file name error...");
				}
				
				t_name = t_name.substring(t_lastSplash + 1,t_name.length());
								
			} 
		}
	}
	
	public void SendAboutInfoQuery(boolean _force){
		synchronized (this) {
			try{
				
				if(m_connect != null){
					
					if(_force || !m_recvAboutText){
						
						m_sendAboutText = true;
						
						ByteArrayOutputStream t_os = new ByteArrayOutputStream();
						t_os.write(msg_head.msgSponsorList);
						m_connect.SendBufferToSvr(t_os.toByteArray(), false);
						
					}
					
				}
				
			}catch(Exception e){}			
		}		
	}
	 
	 public void run(){
		
		while(true){

			m_sendAuthMsg = false;
			
			if(m_systemStart){
				// wait 15.sec for geting GPRS if it's system start connect 
				//
				m_systemStart = false;
				try{
					sleep(15000);
				}catch(Exception _e){}
			}
			
			while(m_disconnect == true){
				try{
					sleep(100);
				}catch(Exception _e){}
			}			
			
			
			// if it is calling
			//
//			try{
//				while(true){
//					final PhoneCall t_calling = Phone.getActiveCall();
//					if(t_calling != null){
//						m_mainApp.SetErrorString("sleep 10sec when is calling.");
//						sleep(30000);
//					}else{
//						break;
//					}
//				}
//				
//			}catch(Exception _e){}
			
			m_ipConnectCounter = 10;
			
			try{

				m_conn = GetConnection(m_mainApp.IsUseSSL());
				m_connect = new sendReceive(m_conn.openOutputStream(),m_conn.openInputStream());
				m_connect.SetKeepliveInterval(m_mainApp.GetPulseIntervalMinutes());
				
				m_connect.RegisterStoreUpDownloadByte(new sendReceive.IStoreUpDownloadByte() {
					public void Store(long uploadByte, long downloadByte) {
						m_mainApp.StoreUpDownloadByte(uploadByte,downloadByte,true);
					}
				});
							
				// send the Auth info
				//
				ByteArrayOutputStream t_os = new ByteArrayOutputStream();
				t_os.write(msg_head.msgConfirm);
				sendReceive.WriteString(t_os, m_mainApp.GetUserPassword());
				sendReceive.WriteInt(t_os,fsm_clientVer);
				t_os.write(recvMain.GetClientLanguage());
				sendReceive.WriteString(t_os,m_currentVersion);	
				
				m_connect.SendBufferToSvr(t_os.toByteArray(), true);			
				
				m_sendAuthMsg = true;				
				
				// set the text connect
				//
				m_mainApp.SetStateString(recvMain.sm_local.getString(localResource.CONNECTED_LABEL));
				
				HomeScreen.updateIcon(Bitmap.getBitmapResource("Main.png"));
				
				while(true){
					ProcessMsg(m_connect.RecvBufferFromSvr());
				}
				
			}catch(Exception _e){
				
				if(m_disconnect != true){
					try{
						m_mainApp.SetStateString(recvMain.sm_local.getString(localResource.CONNECTING_RETRY_LABEL));
						m_mainApp.SetErrorString("M: " + _e.getMessage() + " "+ _e.getClass().getName());
					}catch(Exception e){}	
				}							
			}		
			
			HomeScreen.updateIcon(Bitmap.getBitmapResource("Main_offline.png"));
			
			synchronized (this) {
				try{
					if(m_connect != null){
						m_connect.CloseSendReceive();
					}	
					
					if(m_conn != null){
						m_conn.close();
					}					
					
				}catch(Exception _e){
				}finally{
					m_connect = null;
					m_conn = null;
				}
				
			}			
		}
		
	 }
	 
	 public void Connect(boolean _systemStart)throws Exception{
		 
		synchronized (this) {
			
			Disconnect();
			
			m_mainApp.SetStateString(recvMain.sm_local.getString(localResource.CONNECTING_LABEL));
			m_systemStart = _systemStart;
			m_disconnect = false;
			
			BeginListener();
			
			
		}
	 }
	 
	 public boolean IsConnectState(){
		 return !m_disconnect;
	 }
	 
	 public void Disconnect()throws Exception{
		 
		 m_disconnect = true;
		 
		 interrupt();	 
		 
		 m_connectCounter = -1;
		 	
		 synchronized (this) {
			 
			 EndListener();	

			 if(m_connect != null){
				 m_connect.CloseSendReceive();
				 m_connect = null;
			 }
			 
			 if(m_conn != null){			 
				 m_conn.close();
				 m_conn = null; 
			 }			
			 
			 for(int i = 0 ;i < m_sendingMailAttachment.size();i++){
				 sendMailAttachmentDeamon send = (sendMailAttachmentDeamon)m_sendingMailAttachment.elementAt(i);
				 if(send.isAlive()){

					 send.m_closeState = true;
					 if(send.isAlive()){
						 send.interrupt();
					 }
					 
				 }				 
			 }
			 
			 m_sendingMailAttachment.removeAllElements();
		 }
	 }
	 
	 private SocketConnection GetConnection(boolean _ssl)throws Exception{
		 
		 final int t_sleep = GetConnectInterval();
		 if(t_sleep != 0){
			 sleep(t_sleep);
		 }
		 
		 if(m_disconnect == true){
			 throw new Exception("user closed");
		 }
		 
		 String URL ;

		 // first use IP address to decrease the DNS message  
		 //
		 final String t_hostname = m_mainApp.GetHostName();
		 final int		t_hostport = m_mainApp.GetHostPort();
		 
		 if(_ssl){
			 URL =  "ssl://" + ((m_hostip != null)?m_hostip:t_hostname) + ":" + t_hostport + ";deviceside=true;EndToEndDesired";
		 }else{
			 URL =  "socket://" +((m_hostip != null)?m_hostip:t_hostname) + ":" + t_hostport + ";deviceside=true";
		 }
		 
		 String t_append = m_mainApp.GetURLAppendString();
		 URL = URL + t_append;
		 
		 SocketConnection socket = null;
		 
		 try{
			 socket = (SocketConnection)Connector.open(URL,Connector.READ_WRITE,false);
			 
			 socket.setSocketOption(SocketConnection.DELAY, 0);
			 socket.setSocketOption(SocketConnection.KEEPALIVE, 2);
			 socket.setSocketOption(SocketConnection.LINGER, 0);
			 socket.setSocketOption(SocketConnection.RCVBUF, 512);
			 socket.setSocketOption(SocketConnection.SNDBUF, 128);
			 
		 }catch(Exception _e){

			 m_mainApp.SetErrorString("M: " +_e.getMessage() + t_append + " " + _e.getClass().getName());
			 
			 if(_e.getMessage().indexOf("Peer") != -1){
				 m_connectCounter = 1000;
				 
				 throw _e;
			 }
			 
			 if(m_hostip != null){
				 
				 m_hostip = null;
				 socket = GetConnection(_ssl);
				 
			 }else if(_e.getMessage().indexOf("Tunnel") != -1 
					 || _e.getMessage().indexOf("tunnel") != -1){
				 
				 socket = GetConnection(_ssl);
				 
			 }else{
				 
//				 if(_e.getMessage().indexOf("DNS") != -1 && m_ipConnectCounter > 0){
//					 m_ipConnectCounter--;
//					 socket = GetConnection(_ssl);
//				 }else{
//					 throw _e;
//				 }
				 
				 throw new Exception(_e.getMessage() + " " + t_append + " " + _e.getClass().getName());
			 }
		 }
		 
		 // TCP connect flowing bytes statistics 
		 //
		 m_mainApp.StoreUpDownloadByte(72,40,false);
		 
		 m_hostip = socket.getAddress();
		 
		 return socket;
	 }
	 
	 private int GetConnectInterval(){
		 
		 if(m_connectCounter++ == -1){
			 return 0;
		 }
		 
		 if(m_connectCounter++ > 6){
			 m_connectCounter = 0;		 
			 return m_mainApp.GetPulseIntervalMinutes() * 60 * 1000;
		 }
		 
		 return 10000;
	 }
	 
	 private synchronized void ProcessMsg(byte[] _package)throws Exception{
		 ByteArrayInputStream in  = new ByteArrayInputStream(_package);
		 
		 final int t_msg_head = in.read();
		 
		 switch(t_msg_head){
		 	case msg_head.msgMail:
				ProcessRecvMail(in);		 		
		 		break;
		 	case msg_head.msgSendMail:
		 		ProcessSentMail(in);
		 		break;
		 	case msg_head.msgNote:
		 		String t_string = sendReceive.ReadString(in);
		 		m_mainApp.SetErrorString("Svr:" + t_string);
		 		break;
		 	case msg_head.msgMailAttach:
		 		ProcessMailAttach(in);
		 		break;
		 	case msg_head.msgSponsorList:
		 		m_mainApp.SetAboutInfo(sendReceive.ReadString(in));
		 		m_recvAboutText = true;
		 		break;
		 	case msg_head.msgLatestVersion:
		 		String t_latestVersion = sendReceive.ReadString(in);
		 		if(!m_currentVersion.equals(t_latestVersion)){
		 			m_currentVersion = t_latestVersion;
		 			m_mainApp.SetReportLatestVersion(t_latestVersion);
		 		}		 		 			
		 		break;
		 }
	 }
	
	private Folder GetDefaultFolder()throws Exception{
		
		Store store = Session.waitForDefaultSession().getStore();
		Folder folder = null; 
		Folder[] t_folders = store.list();
		for(int i = 0;i < t_folders.length;i++){
			String t_name = t_folders[i].toString();
			if((t_name.indexOf("Inbox") != -1 || t_name.indexOf("收件箱") != -1 ) 
			&& (t_name.indexOf("no service book") == -1)){
				
				folder = t_folders[i];
				break;
			}
		}
		
		if(folder == null ){
			folder = store.getFolder(Folder.INBOX);
		}
		
		if(folder == null){
			throw new Exception("Can't be retrieve the Folder!");
		}
		
		return folder;
	}
	
	private Folder GetDefaultOutFolder()throws Exception{
		
		Store store = Session.waitForDefaultSession().getStore();
		Folder folder = null; 
		Folder[] t_folders = store.list();
		for(int i = 0;i < t_folders.length;i++){
			String t_name = t_folders[i].toString();
			if((t_name.indexOf("Outbox") != -1 || t_name.indexOf("发件箱") != -1 || t_name.indexOf("發件箱") != -1) 
			&& (t_name.indexOf("no service book") == -1)){
				
				folder = t_folders[i];
				break;
			}
		}
		
		if(folder == null ){
			folder = store.getFolder(Folder.OUTBOX);
		}
		
		if(folder == null){
			throw new Exception("Can't be retrieve the Folder!");
		}
		
		return folder;
	}
	
	private void ProcessRecvMail(InputStream in)throws Exception{
		
		final Message m = new Message();
 		
 		fetchMail t_mail = new fetchMail();
 		t_mail.InputMail(in);
		
		try{
			
			ComposeMessage(m,t_mail);
			
			m.setInbound(true);
			m.setStatus(Message.Status.RX_RECEIVED,1);
			
			m_listeningMessageFolder.appendMessage(m);
											
			// add the message listener to send message to server
			// to remark the message is read
			//
			
			m.addMessageListener(this);
			m_markReadVector.addElement(t_mail);
			
			// send the msgMailConfirm to server to confirm receive this mail
			//
			ByteArrayOutputStream t_os = new ByteArrayOutputStream();
			t_os.write(msg_head.msgMailConfirm);
			int t_hashcode = t_mail.GetSimpleHashCode();
			sendReceive.WriteInt(t_os,t_hashcode);
			m_mainApp.SetErrorString("" + t_hashcode + ":" + t_mail.GetSubject() + "+" + t_mail.GetSendDate().getTime());
			
			m_connect.SendBufferToSvr(t_os.toByteArray(), false);
									
			m_mainApp.TriggerNotification();
							
		}catch(Exception _e){
			m_mainApp.SetErrorString("C:" + _e.getMessage() + " " + _e.getClass().getName());
		}
	}
	
	public synchronized void ProcessSentMail(ByteArrayInputStream in)throws Exception{
		
		boolean t_succ = (in.read() == 1);
	
		final long t_time = sendReceive.ReadLong(in);
		
		// delete the fetchMail send deamon thread
		//
		for(int i = 0;i < m_sendingMailAttachment.size();i++){
			sendMailAttachmentDeamon t_deamon = (sendMailAttachmentDeamon)m_sendingMailAttachment.elementAt(i);
			
			if(t_deamon.m_sendMail.GetSendDate().getTime() == t_time){
				
				if(t_succ){
					m_mainApp.UpdateMessageStatus(t_deamon.m_sendMail.GetAttachMessage(),Message.Status.TX_DELIVERED);
				}else{
					m_mainApp.UpdateMessageStatus(t_deamon.m_sendMail.GetAttachMessage(),Message.Status.TX_ERROR);
				}
				
				t_deamon.m_closeState = true;
				if(t_deamon.isAlive()){
					t_deamon.interrupt();
				}				
				
				m_sendingMailAttachment.removeElement(t_deamon);
				
				// delete the uploading desc string of main application
				//
				for(int j = 0 ;j < m_mainApp.m_uploadingDesc.size();j++){
					recvMain.UploadingDesc t_desc = (recvMain.UploadingDesc)m_mainApp.m_uploadingDesc.elementAt(j);
					if(t_desc.m_mail == t_deamon.m_sendMail){
					
						m_mainApp.m_uploadingDesc.removeElementAt(j);
						break;
					}
				}
								
				break;
			}
		}		
	}
	 
	public synchronized void AddSendingMail(fetchMail _mail,Vector _files,
												fetchMail _forwardReply,int _sendStyle)throws Exception{
				
		// load the attachment if has 
		//
		Vector t_vfileReader = new Vector();
		
		if(!_files.isEmpty()){
			
			
			for(int i = 0;i< _files.size();i++){
				String t_fullname = ((ComposingAttachment)_files.elementAt(i)).m_filename;
				
				FileConnection t_fileReader = (FileConnection) Connector.open(t_fullname,Connector.READ_WRITE);
				
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
					t_type = "application/*";
				}
	
				_mail.AddAttachment(t_name, t_type, t_size);
			}
			
			// reset the content of mail...
			//
			Message msg = _mail.GetAttachMessage();
			ComposeMessageContent(msg, _mail);
		}
		
		if(m_mainApp.m_useLocationInfo){
			_mail.SetLocationInfo(m_mainApp.m_longitude, m_mainApp.m_latitude, 
								m_mainApp.m_altitude, m_mainApp.m_movingSpeed, m_mainApp.m_locationHeading);
		}

		sendMailAttachmentDeamon t_mailDeamon = new sendMailAttachmentDeamon(this, _mail, t_vfileReader,_forwardReply,_sendStyle);
		m_sendingMailAttachment.addElement(t_mailDeamon);	
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
				
				t_att.m_fileContainBuffer.write(t_bytes);				
				
				//System.out.println("write msgMailAttach mailIndex:" + t_mailIndex + " attachIndex:" + t_attachIndex + " startIndex:" +
				//					t_startIndex + " size:" + t_size + " first:" + (int)t_bytes[0]);
				
				t_att.m_completePercent = (t_startIndex + t_size) * 100 / t_att.m_attachmentSize;
				
				if(m_mainApp.m_downloadDlg != null){
					m_mainApp.m_downloadDlg.RefreshProgress(t_att);
				}
				
				if(t_startIndex + t_size >= t_att.m_attachmentSize){
					
					// fetching attachment is over...
					//
					FileConnection t_file = (FileConnection)Connector.open(m_mainApp.m_attachmentDir + t_att.m_realName,Connector.READ_WRITE);
					if(t_file.exists()){
						t_file.delete();
					}
						
					t_file.create();
					
					byte[] t_writeBytes = t_att.m_fileContainBuffer.toByteArray();
					OutputStream os = t_file.openOutputStream();
					os.write(t_writeBytes);
					os.flush();
					os.close();
					t_file.close();

					m_vectReceiveAttach.removeElementAt(i);
					
					m_mainApp.PopupDlgToOpenAttach(t_att);
				}
				
				break;
			}
		}
	}
		
	public synchronized boolean AddMarkReadMail(Message m){
		
		for(int i = 0;i < m_markReadVector.size();i++){
		
			try{
				
				final fetchMail t_mail = (fetchMail)m_markReadVector.elementAt(i);
				
				if(t_mail.GetSendDate().equals(m.getSentDate())
					&& ((String)t_mail.GetFromVect().elementAt(0)).indexOf(m.getFrom().getAddr()) != -1){
					
					// start 
					//
					Thread t_sendMark = new Thread(){
						public void run(){
							fetchMail t_markMail = t_mail;
							int t_time = 0;
							try{
								
								while(m_connect == null){
									if(m_disconnect){
										return;
									}
									
									if(t_time++ > 3){
										return;
									}
									
									sleep(60000);
								}
								
								ByteArrayOutputStream t_os = new ByteArrayOutputStream();
								t_os.write(msg_head.msgBeenRead);
								sendReceive.WriteInt(t_os, t_markMail.GetSimpleHashCode());
								
								m_connect.SendBufferToSvr(t_os.toByteArray(), false);
								
							}catch(Exception e){
								m_mainApp.SetErrorString("Mark:"+ e.getMessage() + e.getClass().getName());
							}
							
						}
					};
					
					t_sendMark.start();
					
					m_markReadVector.removeElementAt(i);
					
					return true;
				}
									
			}catch(Exception _e){
				break;
			}
		}
		
		return false;
	}

	
	public void ImportMail(Message m,fetchMail _mail)throws Exception{
		
		_mail.SetAttchMessage(m);
		
		Address[] a;
		
		// FROM 
		if (m.getFrom() != null) {
			_mail.GetFromVect().removeAllElements();
			_mail.GetFromVect().addElement(composeAddress(m.getFrom()));			
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
		
		String t_sub = m.getSubject();
		if(t_sub == null){
			_mail.SetSubject(fetchMail.fsm_noSubjectTile);
		}else{
			_mail.SetSubject(t_sub);	
		}
		
		Date t_date = m.getSentDate();
		if(t_date != null){	
			_mail.SetSendDate(t_date);
		}
		
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
	
	
	
	private void findEmailBody(Object obj,fetchMail _mail)throws Exception{
	   	   
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
	        	 m_mainApp.SetErrorString("Ex: " + ex.toString() + " " + ex.getClass().getName());
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
	        	 m_mainApp.SetErrorString("Ex: " + ex.toString() + " " + ex.getClass().getName());
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
	    	  m_mainApp.SetErrorString("Ex: " + ex.toString() + " " + ex.getClass().getName());
	      }
	   }
	}
	
	static public void ComposeMessage(Message msg,fetchMail _mail)throws Exception{
		
		_mail.SetAttchMessage(msg);
		
		if(!_mail.GetFromVect().isEmpty()){
			msg.setFrom(fetchMail.parseAddressList(_mail.GetFromVect())[0]);
		}		
		
		if(!_mail.GetSendToVect().isEmpty()){
			 msg.addRecipients(Message.RecipientType.TO,
	    				fetchMail.parseAddressList(_mail.GetSendToVect()));
		}	   
	    
	    if (!_mail.GetCCToVect().isEmpty()){
	    	  msg.addRecipients(Message.RecipientType.CC,
	    				fetchMail.parseAddressList(_mail.GetCCToVect()));
	    }
	    
	    if(!_mail.GetReplyToVect().isEmpty()){
	    	msg.setReplyTo(fetchMail.parseAddressList(_mail.GetReplyToVect()));
	    }
	    
	    if(!_mail.GetBCCToVect().isEmpty()){
	    	 msg.addRecipients(Message.RecipientType.BCC,
	    				fetchMail.parseAddressList(_mail.GetBCCToVect()));
	    }
	    
	    
	    msg.setFlag(Message.Flag.REPLY_ALLOWED, true);
	    
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
	    		SupportedAttachmentPart sap;
	    		String t_filename = recvMain.sm_local.getString(localResource.HTML_PART_FILENAME);
		    	try{
		    		// if the UTF-8 decode sytem is NOT present in current system
					// will throw the exception
					//
		    		
		    		sap = new SupportedAttachmentPart(multipart,ContentType.TYPE_TEXT_HTML_STRING,
		    					t_filename,_mail.GetContain_html().getBytes("UTF-8"));
		    	}catch(Exception e){
		    		sap = new SupportedAttachmentPart(multipart,ContentType.TYPE_TEXT_HTML_STRING,
		    				t_filename,_mail.GetContain_html().getBytes());
		    	}	    		    			
	    		
		    	multipart.addBodyPart(sap);
	    	}
    	
	    	if(!_mail.GetAttachment().isEmpty()){
	    		
				Vector t_contain	= _mail.GetAttachment();
				
				ByteArrayOutputStream t_tmpContent = new ByteArrayOutputStream();
				
		    	for(int i = 0;i< t_contain.size();i++){
		    		
		    		MailAttachment t_attachment = (MailAttachment)t_contain.elementAt(i);
		    		t_tmpContent.reset();
		    				    		
		    		t_tmpContent.write('y');
		    		t_tmpContent.write('u');
		    		t_tmpContent.write('c');
		    		t_tmpContent.write('h');
		    		sendReceive.WriteInt(t_tmpContent,_mail.GetMailIndex());
		    		sendReceive.WriteInt(t_tmpContent,i);
		    		sendReceive.WriteInt(t_tmpContent, t_attachment.m_size);
		    		sendReceive.WriteString(t_tmpContent, t_attachment.m_name);		    		
		    		
		    		String t_sizeString = "(" + recvMain.GetByteStr(t_attachment.m_size) + ")";		    		

		    		SupportedAttachmentPart attach = new SupportedAttachmentPart( multipart,
		    																	(String)t_attachment.m_type,
		    																	t_sizeString + t_attachment.m_name,
		    																	t_tmpContent.toByteArray());
		    		multipart.addBodyPart(attach);
		    	}
			}
			
			msg.setContent(multipart);

		} else {
			
			msg.setContent(_mail.GetContain());
			
		}
	  
	}
	
	 
}
 
