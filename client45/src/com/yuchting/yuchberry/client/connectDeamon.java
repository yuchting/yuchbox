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
import net.rim.device.api.io.Base64OutputStream;
import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.RadioInfo;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.util.Arrays;

import com.yuchting.yuchberry.client.screen.IUploadFileScreenCallback;
import com.yuchting.yuchberry.client.screen.uploadFileScreen;
import com.yuchting.yuchberry.client.ui.WeiboHeadImage;
import com.yuchting.yuchberry.client.weibo.fetchWeibo;
import com.yuchting.yuchberry.client.weibo.fetchWeiboUser;

public class connectDeamon extends Thread implements SendListener,
												MessageListener,
												FolderListener,
												AttachmentHandler,
												ViewListener,
												ViewListenerExtended,
												IUploadFileScreenCallback{
		
	final static int	fsm_clientVer = 13;
	 
	public sendReceive		m_connect = null;
		
	 
	FileConnection		m_keyfile;
	 
	public SocketConnection m_conn 					= null;
	 
	Vector				m_sendingMailAttachment = new Vector();
	
	boolean			m_disconnect 			= true;
	
	// mark read mail data
	//
	final class MarkReadMailData{
		long	m_date;
		String	m_fromAddr = "";
		int		m_simpleHashCode;
		
		public MarkReadMailData(fetchMail _mail){
			m_date 				= _mail.GetSendDate().getTime();
			m_simpleHashCode	= _mail.GetSimpleHashCode();
			
			if(!_mail.GetFromVect().isEmpty()){
				m_fromAddr	= (String)_mail.GetFromVect().elementAt(0);
			}
			
		}
	}
	
	public Vector 		m_markReadVector 		= new Vector();
	
	Vector				m_recvMailSimpleHashCodeSet = new Vector();
			 
	boolean			m_sendAboutText			= false;
	boolean			m_recvAboutText			= false;
	 
	 	 
	// read the email temporary variables
	// 
	private String			m_plainTextContain 	= "";
	private String			m_htmlTextContain 	= "";
	private String			m_htmlTextContain_type 	= "";
	 
	public final class ComposingAttachment{
		public String m_filename;
		public int	m_fileSize;
		 
		public ComposingAttachment(String _filename,int _size){
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
	
	public recvMain		m_mainApp 				= null; 
	public boolean		m_sendAuthMsg			= false;
	 
	 
	int				m_ipConnectCounter 		= 0;
	 
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
		
	final class SendingQueue extends Thread{
		
		final class SendingQueueData{
			public int msgType;
			public byte[] msgData;
			
			public SendingQueueData(int _type,byte[] _data){
				msgType = _type;
				msgData = _data;
			}
		}
		
		Vector	m_sendingData = new Vector();
		
		public boolean addSendingData(int _msgType ,byte[] _data,boolean _exceptSame)throws Exception{
			
			if(!isDisconnectState()){
				m_connect.SendBufferToSvr(_data, false, false);
			}else{
				synchronized (m_sendingData) {
					if(_exceptSame){
						for(int i = 0 ;i < m_sendingData.size();i++){
							SendingQueueData t_data = (SendingQueueData)m_sendingData.elementAt(i);
							if(t_data.msgType == _msgType && Arrays.equals(t_data.msgData,_data)){
								return false;
							}
						}
					}
					m_sendingData.addElement(new SendingQueueData(_msgType,_data));
				}				
			}
			
			return true;
		}
				
		public void run(){
			while(true){
				
				try{

					while(isDisconnectState() || m_sendingData.isEmpty()){
						try{
							sleep(5000);
						}catch(Exception e){}
					}
					
					synchronized (m_sendingData) {
						for(int i = 0 ;i < m_sendingData.size();i++){
							SendingQueueData t_data = (SendingQueueData)m_sendingData.elementAt(i);
							m_connect.SendBufferToSvr(t_data.msgData, false, false);
						}
						
						m_sendingData.removeAllElements();
					}
					
				}catch(Exception e){
					m_mainApp.SetErrorString("SQ:"+ e.getMessage() + " " + e.getClass().getName());
				}
			}
		}
	}
	
	SendingQueue	 m_sendingQueue = new SendingQueue();
	
	public boolean addSendingData(int _msgType ,byte[] _data,boolean _exceptSame)throws Exception{
		return m_sendingQueue.addSendingData(_msgType, _data, _exceptSame);
	}
	
	public boolean isDisconnectState(){
		return m_disconnect || m_connect == null || !m_sendAuthMsg;
	}
	 
	public connectDeamon(recvMain _app){
		m_mainApp = _app;
		m_currentVersion = ApplicationDescriptor.currentApplicationDescriptor().getVersion();
		
		m_sendingQueue.start();
		start();
	}
	 
	 
	public void BeginListener()throws Exception{
		
		m_listeningMessageFolder = GetDefaultFolder();
		m_listeningMessageFolder.addFolderListener(this);
		
		m_listeningMessageFolder_out = GetDefaultOutFolder();
		m_listeningMessageFolder_out.addFolderListener(this	);
		
		// add the send listener
		//
		Store store = Session.getDefaultInstance().getStore();
		store.addSendListener(this);
		
		Session.addViewListener(this);
						 
		AttachmentHandlerManager.getInstance().addAttachmentHandler(this);
		
		
	}
	 
	 
	public void EndListener()throws Exception{
		
		if(m_listeningMessageFolder != null){
			
			m_listeningMessageFolder.removeFolderListener(this);       			
			m_listeningMessageFolder = null;
			
			m_listeningMessageFolder_out.removeFolderListener(this);
			m_listeningMessageFolder_out = null;
			
			// add the send listener
			//
			Store store = Session.getDefaultInstance().getStore();
			store.removeSendListener(this);
			 
			Session.removeViewListener(this);
					         
	        AttachmentHandlerManager.getInstance().removeAttachmentHandler(this);
        
	       
        }        
	}
	 
	 
	//! SendListener
	public boolean sendMessage(Message message){
    	
		final Message t_msg = message;
		
		try{
			Class msgClass = Class.forName("net.rim.device.apps.internal.messaging.MessagingApp");
						
			if(msgClass.isInstance(UiApplication.getUiApplication())){
				m_mainApp.m_messageApplication = UiApplication.getUiApplication();
			}
			
		}catch(Exception e){
			m_mainApp.SetErrorString("sMClass:" + e.getMessage() + e.getClass().getName());
			m_mainApp.m_messageApplication = UiApplication.getUiApplication();
		}		
		
		// invokeLater maybe rise some exception in function ImportMail 
		//
		try{
			
			for(int i = 0;i < m_sendingMailAttachment.size();i++){
				SendMailDeamon t_mail = (SendMailDeamon)m_sendingMailAttachment.elementAt(i);
				
				if(t_mail.m_sendMail.GetSendDate().equals(t_msg.getSentDate())){
					
					if(t_mail.isAlive()){
						// found the re-send message
						//
						return true;								
					}else{
						t_mail.m_closeState = true;
						m_sendingMailAttachment.removeElement(t_mail);
					}
					
					break;
				}
			}			
			
			fetchMail t_mail = new fetchMail();
			ImportMail(t_msg,t_mail);
			
			fetchMail t_forwardReplyMail = null;
			if(m_sendStyle != fetchMail.NOTHING_STYLE && m_forwordReplyMail != null){
									
				t_forwardReplyMail = new fetchMail();
				
				Folder t_folder = m_forwordReplyMail.getFolder();
				if(t_folder != null && t_folder.getType() == Folder.SENT){
					
					// if reply/forward the message of OUTBOX
					//
					Message t_original = FindOrgMessage(m_forwordReplyMail);
					
					if(t_original != null && m_sendStyle == fetchMail.REPLY_STYLE){
						
						fetchMail t_originalMail = new fetchMail();
						ImportMail(m_forwordReplyMail, t_originalMail);
						
						m_forwordReplyMail = t_original;
						ImportMail(m_forwordReplyMail,t_forwardReplyMail);
						
						// append the original message text 
						//
						StringBuffer t_contain = new StringBuffer(t_originalMail.GetContain());
						t_contain.append("\r\n\r\n\r\n---\r\n\r\n");
						t_contain.append(t_forwardReplyMail.GetContain());
						
						t_forwardReplyMail.SetContain(t_contain.toString());
						
					}else{
					
						ImportMail(m_forwordReplyMail,t_forwardReplyMail);
						t_forwardReplyMail.GetSendToVect().removeAllElements();
						
						if(t_original != null){
						
							// change the forward mail account of server 
							//
							Address[] a;
							
							if((a = t_original.getRecipients(Message.RecipientType.TO)) != null) {
								
							    for (int j = 0; j < a.length; j++) {
							    	t_forwardReplyMail.GetSendToVect().addElement(composeAddress(a[j]));
							    }
							}
									
							// append the original message text 
							//
							fetchMail t_originalMail = new fetchMail();
							ImportMail(t_original, t_originalMail);
							
							StringBuffer t_contain = new StringBuffer(t_forwardReplyMail.GetContain());
							t_contain.append("\r\n\r\n\r\n---\r\n\r\n");
							t_contain.append(t_originalMail.GetContain());
							
							t_forwardReplyMail.SetContain(t_contain.toString());
						}
					}				
					
				}else{
					ImportMail(m_forwordReplyMail,t_forwardReplyMail);
				}			
				
				
				if(m_sendStyle == fetchMail.REPLY_STYLE && m_mainApp.m_discardOrgText){
					// discard the org text when reply
					//
					t_forwardReplyMail.SetContain("");
				}
				
				m_mainApp.SetErrorString("origMsg:"+ t_forwardReplyMail.GetSubject());
			}else{
				
				// select sending from mail address
				//
				if(!m_mainApp.m_sendMailAccountList.isEmpty() 
				&& m_mainApp.m_defaultSendMailAccountIndex < m_mainApp.m_sendMailAccountList.size()
				&& m_mainApp.m_defaultSendMailAccountIndex >= 0){
					
					String t_defaultAcc = (String)m_mainApp.m_sendMailAccountList.elementAt(m_mainApp.m_defaultSendMailAccountIndex);
					
					t_mail.GetFromVect().removeAllElements();
					t_mail.GetFromVect().addElement(t_defaultAcc);
					
					m_mainApp.SetErrorString("from:"+t_defaultAcc);
				}
			}
			
			m_mainApp.SetErrorString("sendMsg:" + t_msg.getSubject());
														
			AddSendingMail(t_mail,m_composingAttachment,t_forwardReplyMail,m_sendStyle);
			m_composingAttachment.removeAllElements();
						
		}catch(Exception _e){
			m_mainApp.SetErrorString("sMsg: " + _e.getMessage() + " " + _e.getClass().getName());
		}
		
		
		// return false to forbidden system to delivery this mail
		// and let the YB set header sign of mail immediately
		//
		return false;
	}
	
	 
	//@{ MessageListener
	public void changed(MessageEvent e){
		
		if(e.getMessageChangeType() == MessageEvent.UPDATED
		|| e.getMessageChangeType() == MessageEvent.OPENED){
			
			try{
				
				m_mainApp.StopNotification();
				
				AddMarkReadOrDelMail(e.getMessage(),false);

				
			}catch(Exception _e){}
			
		}
		
	}
	//@}
	
	//@{ FolderListener
	public void messagesAdded(FolderEvent e){}
	
	public void messagesRemoved(FolderEvent e){
		
		if(e.getType() == FolderEvent.MESSAGE_REMOVED){
			Message t_msg = e.getMessage();
			
			if(AddMarkReadOrDelMail(t_msg,true)){
				m_mainApp.StopNotification();
			}
			t_msg.removeMessageListener(this);
			
			// if the user select re-send menu the RIM OS will
			// call sendMessage function first and call this messagesRemoved function later
			// this order can make deleting send message...
			//			
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
	            output.write( "data:text/html;base64,".getBytes("UTF-8") );
	            boutput.write( (byte[])p.getContent() );
	            boutput.flush();
	            boutput.close();
	            output.flush();
	            output.close();
	            
	            recvMain.openURL(output.toString());
	            
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
				
				final String t_filename = m_mainApp.GetAttachmentDir() + t_realName;
				FileConnection t_file = (FileConnection)Connector.open(t_filename,Connector.READ_WRITE);
				
				if(t_file.exists()){
					
					m_mainApp.PushViewFileScreen(t_filename);
					
				}else{
									
					// fetch from the server 
					//
					for(int i = 0;i < m_vectReceiveAttach.size();i++){
						FetchAttachment t_att = (FetchAttachment)m_vectReceiveAttach.elementAt(i);
												
						if(t_att.m_messageHashCode == t_messageCode){
							
							m_mainApp.refreshDownloadFileDlg(t_att);
							
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
					
					m_mainApp.PopupDownloadFileDlg(t_att);
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
	
	public void cancelDownloadAtt(FetchAttachment _att){
		
		for(int i = 0;i < m_vectReceiveAttach.size();i++){
			FetchAttachment t_att = (FetchAttachment)m_vectReceiveAttach.elementAt(i);
									
			if(t_att == _att){
				m_vectReceiveAttach.removeElementAt(i);
				
				try{
					ByteArrayOutputStream os = new ByteArrayOutputStream();
					os.write(msg_head.msgMailAttCancel);
					sendReceive.WriteInt(os,_att.m_mailIndex);
					
					addSendingData(msg_head.msgMailAttCancel, os.toByteArray(), true);
					
				}catch(Exception e){
					m_mainApp.SetErrorString("CDA:"+e.getMessage() + e.getClass().getName());
				}
				
				
				break;
			}
		}
		
		
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
		
		m_forwordReplyMail = FindOrgMessage(e.getMessage());
		
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
		
		m_forwordReplyMail = FindOrgMessage(e.getMessage());
		
		m_sendStyle = fetchMail.REPLY_STYLE;
	}
	//@}
			
	static final String fsm_origMsgFindTag = "Message-ID:";
	
	public Message FindOrgMessage(Message _message){
		
		ByteArrayOutputStream t_os = new ByteArrayOutputStream();
		try {
			_message.writeTo(t_os);
			String t_content = t_os.toString();
			
			int t_first = t_content.indexOf(fsm_origMsgFindTag);
			if(t_first != -1){
				t_first = t_content.indexOf(fsm_origMsgFindTag,t_first + 1);
				
				if(t_first != -1){
					int t_end = t_content.indexOf('\r',t_first);
					
					if(t_end != -1){
						String ID = t_content.substring(t_first + fsm_origMsgFindTag.length(), t_end);
						
						int messageId = Integer.valueOf(ID).intValue();
						
						Store store = Session.getDefaultInstance().getStore();
						Folder[] t_folders = store.list();
						
						for(int i = 0 ;i < t_folders.length;i++){
							Message[] t_messages = t_folders[i].getMessages();
							
							for(int j = t_messages.length - 1;j >= 0 ;j--){
								if(t_messages[j].getMessageId() == messageId){
									return t_messages[j]; 
								}
							}
						}
					}
					
				}
			}
			
		}catch (Exception e1) {
			e1.printStackTrace();
		}
		
		return null;
		
	}
	
	public void SendFetchAttachmentFile(FetchAttachment _att)throws Exception{
		
		ByteArrayOutputStream t_os = new ByteArrayOutputStream();
		t_os.write(msg_head.msgFetchAttach);
		sendReceive.WriteInt(t_os, _att.m_mailIndex);
		sendReceive.WriteInt(t_os, _att.m_attachmentIdx);
		
		m_connect.SendBufferToSvr(t_os.toByteArray(), true,false);
			
	}
	
	public boolean clickOK(String _filename,int _size){
		AddAttachmentFile(_filename,_size);
		return true;
	}
	public void clickDel(String _filename){
		DelAttachmentFile(_filename);
	}
	
	//! the attachment file selection screen(uploadFileScreen) will call
	public void AddAttachmentFile(String _filename,int _fileSize){
		for(int i = 0;i < m_composingAttachment.size();i++){
			ComposingAttachment t_att = (ComposingAttachment)m_composingAttachment.elementAt(i);
			if(t_att.m_filename.equals(_filename)){
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
						m_connect.SendBufferToSvr(t_os.toByteArray(), false,false);
						
					}
					
				}
				
			}catch(Exception e){}			
		}		
	}
	
	public boolean CanNotConnectSvr(){
		boolean t_radioNotAvail = (RadioInfo.getSignalLevel() <= -110 || !RadioInfo.isDataServiceOperational());
		
//		if(t_radioNotAvail){
//			m_mainApp.SetErrorString("radio data service is not available");
//		}
		
		return t_radioNotAvail && !m_mainApp.UseWifiConnection();
	}
	 
	 public void run(){
		
		while(true){
	
			m_sendAuthMsg = false;
			
			while(CanNotConnectSvr() || m_disconnect == true ){
	
				try{
					sleep(15000);
				}catch(Exception _e){}	
			}
			
			try{
				
				synchronized (this) {
					m_ipConnectCounter++;
				}				
				
				m_conn = GetConnection(m_mainApp.IsUseSSL(),m_mainApp.UseMDS());
				
				// TCP connect flowing bytes statistics 
				//
				m_mainApp.StoreUpDownloadByte(72,40,false);
				
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
				sendReceive.WriteString(t_os,m_mainApp.m_passwordKey);
				sendReceive.WriteBoolean(t_os,m_mainApp.m_enableWeiboModule);
				sendReceive.WriteString(t_os,recvMain.fsm_OS_version);
				int t_size = (recvMain.fsm_display_width << 16) | recvMain.fsm_display_height;
				sendReceive.WriteInt(t_os,t_size);
				
				m_connect.SendBufferToSvr(t_os.toByteArray(), true,false);
				
				m_sendAuthMsg = true;
				
				synchronized (this) {
					m_ipConnectCounter = 0;
				}
								
				// set the text connect
				//
				m_mainApp.SetConnectState(recvMain.CONNECTED_STATE);
				m_mainApp.StopDisconnectNotification();
				
				m_mainApp.SetModuleOnlineState(true);
								
				while(true){
					ProcessMsg(m_connect.RecvBufferFromSvr());
				}
				
			}catch(Exception _e){
				
				if(m_disconnect != true){
					try{
						m_mainApp.SetConnectState(recvMain.CONNECTING_STATE);
						m_mainApp.SetErrorString("M: " + _e.getMessage() + " "+ _e.getClass().getName());
					}catch(Exception e){}	
				}							
			}		
					
			if(!m_mainApp.isForeground() && m_ipConnectCounter >= 5){
				m_ipConnectCounter = 0;
				m_mainApp.TriggerDisconnectNotification();
			}			
			
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
			
			m_mainApp.SetModuleOnlineState(false);									
		}
		
	 }
	 
	 public synchronized void Connect()throws Exception{
		 
		 Disconnect();
		 
		 m_mainApp.SetConnectState(recvMain.CONNECTING_STATE);
		 m_disconnect = false;	
		 
		 BeginListener();	
	 }
	 
	 public boolean IsConnectState(){
		 return !m_disconnect;
	 }
	 
	 public void Disconnect()throws Exception{
		 
		 m_disconnect = true;
		 m_mainApp.StopDisconnectNotification();
		 
		 interrupt();	 
		 
		 m_connectCounter = -1;
				 	
		 synchronized (this) {
			 
			 m_ipConnectCounter = 0;
			 
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
				 SendMailDeamon send = (SendMailDeamon)m_sendingMailAttachment.elementAt(i);
				 	
				 send.m_closeState = true; 
				 send.inter(); 
			 }
			 
			 m_sendingMailAttachment.removeAllElements();
		 }
	 }
	 
	 private SocketConnection GetConnection(boolean _ssl,boolean _useMDS)throws Exception{
		 			 
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
			 if(_useMDS){
				 URL =  "ssl://" + (t_hostname) + ":" + t_hostport;
			 }else{
				 URL =  "ssl://" + (t_hostname) + ":" + t_hostport + ";deviceside=true;EndToEndDesired";
			 }
			 
		 }else{
			 if(_useMDS){
				 URL =  "socket://" +(t_hostname) + ":" + t_hostport;
			 }else{
				 URL =  "socket://" +(t_hostname) + ":" + t_hostport + ";deviceside=true";
			 }			 
		 }
		 
		 String t_append = "";
		 
		 if(!_useMDS){
			 t_append = m_mainApp.GetURLAppendString();
			 URL = URL + t_append;
		 }
		 
		 SocketConnection socket = null;
		 
		 try{
			 
			 socket = (SocketConnection)Connector.open(URL,Connector.READ_WRITE,false);
			
			 if(socket == null){
				 throw new Exception("socket null");
			 }
			 
			 try{
				 socket.setSocketOption(SocketConnection.DELAY, 0);	 
			 }catch(Exception _e){
				 m_mainApp.SetErrorString("CM0: " +_e.getMessage() + _e.getClass().getName());
			 }
			 
			 try{
				 socket.setSocketOption(SocketConnection.KEEPALIVE,m_mainApp.GetPulseIntervalMinutes());
			 }catch(Exception _e){
				 m_mainApp.SetErrorString("CM1: " +_e.getMessage()  + _e.getClass().getName());
			 }
			 
			 try{
				 socket.setSocketOption(SocketConnection.LINGER, 0);
			 }catch(Exception _e){
				 m_mainApp.SetErrorString("CM2: " +_e.getMessage() + _e.getClass().getName());
			 }
			 
			 try{
				 socket.setSocketOption(SocketConnection.RCVBUF, 256);
			 }catch(Exception _e){
				 m_mainApp.SetErrorString("CM3: " +_e.getMessage()+ _e.getClass().getName());
			 }
			 
			 try{
				 socket.setSocketOption(SocketConnection.SNDBUF, 128);
			 }catch(Exception _e){
				 m_mainApp.SetErrorString("CM4: " +_e.getMessage() + _e.getClass().getName()); 
			 }
			 
			 return socket;
			 
		 }catch(Exception _e){
	
			 String message = _e.getMessage();

			 if(message != null){
				 if(message.indexOf("Peer") != -1){
					 m_connectCounter = 1000;
				 }
			 }
			 
			 if(t_append.length() != 0){
				 m_mainApp.SetErrorString("CM Failed: " + t_append);
			 }
			 
			 throw _e;
		 } 
		
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
		 		m_mainApp.DialogAlert("YuchBerry svr: " + t_string);
		 		m_mainApp.SetErrorString(t_string);
		 		break;
		 	case msg_head.msgMailAttach:
		 		ProcessMailAttach(in);
		 		break;
		 	case msg_head.msgFileAttach:
		 		ProcessFileAttach(in);
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
		 	case msg_head.msgWeibo:
		 		ProcessWeibo(in);
		 		break;
		 	case msg_head.msgWeiboHeadImage:
		 		ProcessWeiboHeadImage(in);
		 		break;
		 		
		 	case msg_head.msgWeiboPrompt:
		 		if(m_mainApp.m_weiboTimeLineScreen != null){
		 			m_mainApp.m_weiboTimeLineScreen.popupPromptText(sendReceive.ReadString(in));
		 		}
		 		break;
		 	case msg_head.msgWeiboUser:
		 		if(m_mainApp.m_weiboTimeLineScreen != null){
		 			fetchWeiboUser t_user = new fetchWeiboUser();
		 			t_user.InputData(in);
		 			m_mainApp.m_weiboTimeLineScreen.displayWeiboUser(t_user);
		 		}
		 		break;
		 	case msg_head.msgWeiboConfirm:
				if(m_mainApp.m_weiboTimeLineScreen != null){
					m_mainApp.m_weiboTimeLineScreen.weiboSendFileConfirm(sendReceive.ReadInt(in),0);
				}
		 		break;
		 	case msg_head.msgDeviceInfo:
		 		ByteArrayOutputStream os = new ByteArrayOutputStream();
		 		os.write(msg_head.msgDeviceInfo);
		 		sendReceive.WriteLong(os, recvMain.fsm_PIN);
		 		sendReceive.WriteString(os,recvMain.fsm_IMEI);
		 		addSendingData(msg_head.msgDeviceInfo, os.toByteArray(), true);
		 		break;
		 	case msg_head.msgMailAccountList:
		 		sendReceive.ReadStringVector(in, m_mainApp.m_sendMailAccountList);
		 		if(m_mainApp.m_settingScreen != null){
		 			m_mainApp.m_settingScreen.refreshMailAccountList();
		 		}
		 		break;
		 	case msg_head.msgChat:
		 		if(m_mainApp.m_mainIMScreen != null){
		 			m_mainApp.m_mainIMScreen.processChatMsg(in);
		 		}
		 		break;
		 	case msg_head.msgChatRosterList:
		 		if(m_mainApp.m_mainIMScreen != null){
		 			m_mainApp.m_mainIMScreen.processChatRosterList(in);
		 		}
		 		break;
		 	case msg_head.msgChatConfirm:
		 		if(m_mainApp.m_mainIMScreen != null){
		 			m_mainApp.m_mainIMScreen.processChatConfirm(in);
		 		}
		 		break;
		 	case msg_head.msgChatState:
		 		if(m_mainApp.m_mainIMScreen != null){
		 			m_mainApp.m_mainIMScreen.processChatState(in);
		 		}
		 		break;
		 	case msg_head.msgChatHeadImage:
		 		ProcessChatHeadImage(in);
		 		break;
		 }
	 }
	
	public void sendRequestMailAccountMsg(){
		try{
			addSendingData(msg_head.msgMailAccountList, new byte[]{msg_head.msgMailAccountList}, true);
		}catch(Exception e){
			m_mainApp.SetErrorString("SRMAM:"+e.getMessage()+ e.getClass().getName());
		}
		
	}
	private Folder GetDefaultFolder()throws Exception{
		
		Store store = Session.waitForDefaultSession().getStore();
		Folder folder = null; 
		Folder[] t_folders = store.list();
		for(int i = 0;i < t_folders.length;i++){
			
			int t_type = t_folders[i].getType();
			
			if(t_type == Folder.INBOX){
				folder = t_folders[i];
				break;
			}
		}
		
		if(folder == null ){
			t_folders = store.list();
			for(int i = 0;i < t_folders.length;i++){
				m_mainApp.SetErrorString( t_folders[i].toString());
			}
			folder = store.getFolder(Folder.INBOX);
		}
		
		if(folder == null){
			throw new Exception("Can't be retrieve the Folder! check Service Book with CMIME");
		}
			
		
		return folder;
	}
	
	private Folder GetDefaultOutFolder()throws Exception{
		
		Store store = Session.waitForDefaultSession().getStore();
		Folder folder = null; 
		Folder[] t_folders = store.list();
		for(int i = 0;i < t_folders.length;i++){
		
			int t_type = t_folders[i].getType();
			
			if(t_type == Folder.SENT){
				folder = t_folders[i];
				break;
			}
		}
		
		if(folder == null ){
			t_folders = store.list();
			for(int i = 0;i < t_folders.length;i++){
				m_mainApp.SetErrorString( t_folders[i].toString());
			}
			folder = store.getFolder(Folder.SENT);
		}
		
		if(folder == null){
			throw new Exception("Can't be retrieve the Folder! check Service Book with CMIME");
		}
		
		return folder;
	}
	
	private void SendMailConfirmMsg(int _hashCode)throws Exception{
		
		// send the msgMailConfirm to server to confirm receive this mail
		//
		
		ByteArrayOutputStream t_os = new ByteArrayOutputStream();
		t_os.write(msg_head.msgMailConfirm);
		sendReceive.WriteInt(t_os,_hashCode);
		
		
		m_sendingQueue.addSendingData(msg_head.msgMailConfirm, t_os.toByteArray(),true);
	}
	
	private void ProcessRecvMail(InputStream in)throws Exception{
		
		fetchMail t_mail = new fetchMail();
		t_mail.InputMail(in); 		
	
		int t_hashcode = t_mail.GetSimpleHashCode();
		
		for(int i = 0;i < m_recvMailSimpleHashCodeSet.size();i++){
			Integer t_simpleHash = (Integer)m_recvMailSimpleHashCodeSet.elementAt(i);
			if(t_simpleHash.intValue() == t_hashcode ){
				
				SendMailConfirmMsg(t_hashcode);
				
				m_mainApp.SetErrorString("" + t_hashcode + " Mail has been added! ");
				
				return;
			}
		}
				
		if(m_recvMailSimpleHashCodeSet.size() > 256){
			m_recvMailSimpleHashCodeSet.removeElementAt(0);
		}
		
		m_recvMailSimpleHashCodeSet.addElement(new Integer(t_mail.GetSimpleHashCode()));
		
		if(m_mainApp.GetRecvMsgMaxLength() != 0){
			if(t_mail.GetContain().length() > m_mainApp.GetRecvMsgMaxLength()){
				t_mail.SetContain(t_mail.GetContain().substring(0,m_mainApp.GetRecvMsgMaxLength() - 1) + 
									"\n.....\n\n" + recvMain.sm_local.getString(localResource.REACH_MAX_MESSAGE_LENGTH_PROMPT));
			}
		}
		
		if(m_mainApp.m_sendMailAccountList.isEmpty()){
			sendRequestMailAccountMsg();
		}

		try{
			
			Message m = new Message();
			
			ComposeMessage(m,t_mail,m_mainApp.m_discardOrgText);
			
			m.setInbound(true);
			m.setStatus(Message.Status.RX_RECEIVED,1);
			
			m_listeningMessageFolder.appendMessage(m);
											
			// add the message listener to send message to server
			// to remark the message is read
			//
			m.addMessageListener(this);
			
			synchronized (m_markReadVector) {
				m_markReadVector.addElement(new MarkReadMailData(t_mail));
			}			
			
			// increase the receive mail quantity
			//
			m_mainApp.SetRecvMailNum(m_mainApp.GetRecvMailNum() + 1);			
			
			SendMailConfirmMsg(t_hashcode);
			
			m_mainApp.SetErrorString("" + t_hashcode + ":" + t_mail.GetSubject() + "+" + t_mail.GetSendDate().getTime());
									
			m_mainApp.TriggerNotification();
							
		}catch(Exception _e){
			m_mainApp.SetErrorString("C:" + _e.getMessage() + " " + _e.getClass().getName());
		}
	}
	
	public synchronized void ProcessSentMail(ByteArrayInputStream in)throws Exception{
		
		boolean t_succ = sendReceive.ReadBoolean(in);
	
		final long t_time = sendReceive.ReadLong(in);
		
		// delete the fetchMail send deamon thread
		//
		for(int i = 0;i < m_sendingMailAttachment.size();i++){
			SendMailDeamon t_deamon = (SendMailDeamon)m_sendingMailAttachment.elementAt(i);
			
			if(t_deamon.m_sendMail.GetSendDate().getTime() == t_time){
			
				if(t_deamon.m_sendMail.GetAttachMessage() != null){
					
					if(t_succ){
						m_mainApp.UpdateMessageStatus(t_deamon.m_sendMail.GetAttachMessage(),Message.Status.TX_DELIVERED);
						
						// increase the send mail quantity
						//
						m_mainApp.SetSendMailNum(m_mainApp.GetSendMailNum() + 1);
						
					}else{
						m_mainApp.UpdateMessageStatus(t_deamon.m_sendMail.GetAttachMessage(),Message.Status.TX_ERROR);
					}
				}
				
				t_deamon.m_closeState = true;
				t_deamon.inter();
							
				
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
	 
	public void AddSendingMail(final fetchMail _mail,final Vector _files,
											final fetchMail _forwardReply,final int _sendStyle)throws Exception{
		// load the attachment if has 
		//
		final Vector t_vfileReader = new Vector();
		
		if(_files != null && !_files.isEmpty()){
			
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
			ComposeMessageContent(msg, _mail,true);
		}
		
		if(m_mainApp.m_useLocationInfo && m_mainApp.m_mailUseLocation){
			_mail.SetLocationInfo(m_mainApp.m_gpsInfo);
		}
	
		m_mainApp.invokeLater(new Runnable() {
			
			public void run() {
				
				try{
					
					SendMailDeamon t_mailDeamon = new SendMailDeamon(connectDeamon.this, 
													_mail, t_vfileReader,_forwardReply,_sendStyle);
					
					m_sendingMailAttachment.addElement(t_mailDeamon);
					
				}catch(Exception e){
					m_mainApp.SetErrorString("ASM:"+e.getMessage()+e.getClass().getName());
				}
								
			}
		});
			
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
					FileConnection t_file = (FileConnection)Connector.open(m_mainApp.GetAttachmentDir() + t_att.m_realName,Connector.READ_WRITE);
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
	
	public void ProcessFileAttach(InputStream in)throws Exception{
		
		int t_hashCode = sendReceive.ReadInt(in);
		int t_attachIndex = sendReceive.ReadInt(in);
		
		for(int i = 0 ;i < m_sendingMailAttachment.size();i++){
			SendMailDeamon t_mail = (SendMailDeamon)m_sendingMailAttachment.elementAt(i);
			if(t_mail.m_sendMail.GetSimpleHashCode() == t_hashCode){
				
				t_mail.m_sendFileDaemon.sendNextFile(t_attachIndex);
								
				return;
			}
		}
		
		// TODO weibo process....
		//
		if(m_mainApp.m_weiboTimeLineScreen != null){
			m_mainApp.m_weiboTimeLineScreen.weiboSendFileConfirm(t_hashCode,t_attachIndex);
		}
		
	}
		
	public boolean AddMarkReadOrDelMail(Message m,final boolean _del){
		
		synchronized (m_markReadVector) {

			for(int i = 0;i < m_markReadVector.size();i++){
			
				try{
					
					final MarkReadMailData t_mail = (MarkReadMailData)m_markReadVector.elementAt(i);
					
					if(t_mail.m_date == m.getSentDate().getTime() 
					&& t_mail.m_fromAddr.indexOf(m.getFrom().getAddr()) != -1){
						
						byte t_msgType = (_del && m_mainApp.m_delRemoteMail)?msg_head.msgMailDel:msg_head.msgBeenRead;
						
						ByteArrayOutputStream t_os = new ByteArrayOutputStream();
						t_os.write(t_msgType);
						sendReceive.WriteInt(t_os, t_mail.m_simpleHashCode);
						
						m_sendingQueue.addSendingData(t_msgType, t_os.toByteArray(),true);
											
						if(_del){
							m_markReadVector.removeElementAt(i);
						}					
						
						return true;
					}
										
				}catch(Exception _e){
					break;
				}
			}
			
			return false;	
		}
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
		
		// CC
		if ((a = m.getRecipients(Message.RecipientType.CC)) != null) {
			
			_mail.GetCCToVect().removeAllElements();
			
		    for (int j = 0; j < a.length; j++) {
		    	_mail.GetCCToVect().addElement(composeAddress(a[j]));
		    }
		}
		
		// BCC
		if((a = m.getRecipients(Message.RecipientType.BCC)) != null){
			
			_mail.GetBCCToVect().removeAllElements();
			
		    for (int j = 0; j < a.length; j++) {
		    	_mail.GetBCCToVect().addElement(composeAddress(a[j]));
		    }
		}
		
		String t_sub = m.getSubject();
		if(t_sub == null){
			_mail.SetSubject(fetchMail.fsm_noSubjectTile);
		}else{	
			
			_mail.SetSubject(t_sub);	
		}
		
		Date t_date = m.getSentDate();
		if(t_date != null && t_date.getTime() != 0){
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
		_mail.SetContain_html(m_htmlTextContain,m_htmlTextContain_type);
	}
	
	private String composeAddress(Address a){
		if(a.getName() != null){
			return "\"" + a.getName().replace(',', ' ') +"\" <" + a.getAddr() + ">";
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
		   m_htmlTextContain_type = mbp.getContentType();
		   if(m_htmlTextContain_type == null){
			   m_htmlTextContain_type = ""; 
		   }
	
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
	
	static public void ComposeMessage(Message msg,fetchMail _mail,boolean _discardOrgText)throws Exception{
		
		
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
	    
	    // replace the blackberry auto-prefix
		//
	    String t_sub = _mail.GetSubject();		
		
	    msg.setSubject(t_sub);
	    msg.setHeader("X-Mailer",_mail.GetXMailer());
	    msg.setSentDate(_mail.GetSendDate());	      
	
	    ComposeMessageContent(msg,_mail,false);
	}
	
	static private void ComposeMessageContent(Message msg,fetchMail _mail,boolean _sendCompose)throws Exception{
		
		 if(_mail.GetContain_html().length() != 0
			|| !_mail.GetAttachment().isEmpty()) {
		
			Multipart multipart = new Multipart();
		    	
	    	TextBodyPart t_text = new TextBodyPart(multipart,_sendCompose?"":_mail.GetContain());
	    	multipart.addBodyPart(t_text);
	    	
	    	if(_mail.GetContain_html().length() != 0){
	    		SupportedAttachmentPart sap = null;
	    		String t_filename = recvMain.sm_local.getString(localResource.HTML_PART_FILENAME);
	    		
	    		String t_type = "UTF-8";
	    		    		
	    		int t_charset = _mail.GetContain_html_type().indexOf("charset=");
	    		if(t_charset != -1){
	    			t_type = _mail.GetContain_html_type().substring(t_charset + 8);
	    		}
	    		
		    	try{
		    		// if the UTF-8 decode sytem is NOT present in current system
					// will throw the exception
					//
		    		
		    		sap = new SupportedAttachmentPart(multipart,_mail.GetContain_html_type(),
		    					t_filename,_mail.GetContain_html().getBytes(t_type));
		    		
		    	}catch(Exception e){
		    		
		    		try{
		    			sap = new SupportedAttachmentPart(multipart,ContentType.TYPE_TEXT_HTML_STRING,
			    				t_filename,_mail.GetContain_html().getBytes("UTF-8"));
		    					    			
		    		}catch(Exception ex){}		    		
		    	}
		    	
		    	if(sap != null){
		    		multipart.addBodyPart(sap);
		    	}
		    	
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
	
	///////////////////////////////////////////////////////////////////////////////////////////
	///// weibo module
	///////////////////////////////////////////////////////////////////////////////////////////

	private void SendWeiboConfirmMsg(fetchWeibo _weibo) throws Exception{
		
		// send the msgWeiboConfirm to server to confirm receive this weibo
		//
		ByteArrayOutputStream t_os = new ByteArrayOutputStream();
		t_os.write(msg_head.msgWeiboConfirm);
		t_os.write(_weibo.GetWeiboStyle());
		
		sendReceive.WriteLong(t_os,_weibo.GetId());		
		
		m_sendingQueue.addSendingData(msg_head.msgWeiboConfirm, t_os.toByteArray(),true);
	}
	

	public void SendCreateFavoriteWeibo(fetchWeibo _weibo){
		try{
			
			ByteArrayOutputStream t_os = new ByteArrayOutputStream();
			t_os.write(msg_head.msgWeiboFavorite);
			t_os.write(_weibo.GetWeiboStyle());
			
			sendReceive.WriteLong(t_os,_weibo.GetId());		
			
			m_sendingQueue.addSendingData(msg_head.msgWeiboFavorite, t_os.toByteArray(),true);
			
		}catch(Exception e){
			
			m_mainApp.SetErrorString("SCFW:" + e.getMessage() + e.getClass().getName());
		}
	}
	
	private void ProcessWeibo(InputStream in){
	
		if(m_mainApp.m_weiboTimeLineScreen == null){
			m_mainApp.SetErrorString("recevive weibo message,but haven't enable Weibo module");
			return;
		}
		
		fetchWeibo t_weibo = new fetchWeibo();
	
		try{
			t_weibo.InputWeibo(in);
			SendWeiboConfirmMsg(t_weibo);
			
			m_mainApp.PrepareWeiboItem(t_weibo);
			
		}catch(Exception e){
			m_mainApp.SetErrorString("PW:"+ e.getMessage() + e.getClass().getName());
		}
	}
	
	private void ProcessWeiboHeadImage(InputStream in){
		
		if(m_mainApp.m_weiboTimeLineScreen == null){
			m_mainApp.SetErrorString("recevive weibo message,but haven't enable Weibo module");
			return;
		}
		
		try{
			
			int t_style = in.read();
			boolean t_largeSize = sendReceive.ReadBoolean(in);
			
			String t_id = null;
			if(t_style == fetchWeibo.QQ_WEIBO_STYLE){
				t_id = sendReceive.ReadString(in);
			}else{
				t_id = Long.toString(sendReceive.ReadLong(in));
			}
			
			StoreHeadImage(m_mainApp.m_weiboTimeLineScreen.m_headImageList,
							true,t_largeSize,t_style,t_id,in);
			
		}catch(Exception e){
			m_mainApp.SetErrorString("PWHI:" + e.getMessage() + e.getClass().getName());
		}
	}
	
	private void ProcessChatHeadImage(InputStream in){
		
		if(m_mainApp.m_mainIMScreen == null){
			m_mainApp.SetErrorString("recevive chat IM message,but haven't enable IM module");
			return;
		}
		
		try{
			int t_style = in.read();
			boolean t_largeSize = sendReceive.ReadBoolean(in);
			
			String t_id = sendReceive.ReadString(in);
						
			StoreHeadImage(m_mainApp.m_mainIMScreen.m_headImageList,
							false,t_largeSize,t_style,t_id,in);
			
		}catch(Exception e){
			m_mainApp.SetErrorString("PCHI:" + e.getMessage() + e.getClass().getName());
		}
	}
	
	private void StoreHeadImage(Vector _imageList,boolean _isWeiboOrIM,boolean _largeSize,
							int _style,String _imageId,InputStream in)throws Exception{
		
		ByteArrayOutputStream t_os = new ByteArrayOutputStream();
		try{
			int t_data = -1;
			while((t_data = in.read()) != -1){
				t_os.write(t_data);
			}
			
			byte[] t_dataArray = t_os.toByteArray();
			

			WeiboHeadImage.AddWeiboHeadImage(_imageList,_style,_imageId,t_dataArray);
			
			if(_isWeiboOrIM){
				m_mainApp.ChangeWeiboHeadImageHash(_imageId, _style, t_dataArray.length);
			}
			
			
			String t_imageFilename = null;
			
			if(_largeSize){
				if(_isWeiboOrIM){
					t_imageFilename = m_mainApp.GetWeiboHeadImageDir(_style) + _imageId + "_l.png";
				}else{
					t_imageFilename = m_mainApp.GetIMHeadImageDir(_style) + _imageId + "_l.png";
				}
				
			}else{
				if(_isWeiboOrIM){
					t_imageFilename = m_mainApp.GetWeiboHeadImageDir(_style) + _imageId + ".png";
				}else{
					t_imageFilename = m_mainApp.GetIMHeadImageDir(_style) + _imageId + ".png";
				}
			}
			
			FileConnection t_fc = (FileConnection)Connector.open(t_imageFilename,Connector.READ_WRITE);
			try{
				if(t_fc.exists()){
					t_fc.delete();
				}
				
				t_fc.create();
				
				OutputStream t_fileOS = t_fc.openOutputStream();
				try{
					t_fileOS.write(t_dataArray);
				}finally{
					t_fileOS.flush();
					t_fileOS.close();
					t_fileOS = null;
				}
			}finally{
				t_fc.close();
				t_fc = null;
			}
		}finally{
			t_os.close();
			t_os = null;
		}
	}	 
}
 
