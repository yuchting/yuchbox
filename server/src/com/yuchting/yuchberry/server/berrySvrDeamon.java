package com.yuchting.yuchberry.server;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.util.Vector;

import javax.net.ssl.SSLSocket;

class berrySendAttachment extends Thread{
	
	FileInputStream		m_file;
	fetchMgr			m_fetchMain;
	int					m_fileLength;
	int					m_mailIndex;
	int					m_attachIndex;
	
	int					m_startIndex = 0;
	byte[] 				m_buffer = new byte[fsm_sendSize];
	ByteArrayOutputStream m_os = new ByteArrayOutputStream();
	
	final static int	fsm_sendSize = 512;
	
	berrySendAttachment(int _mailIndex,int _attachIdx,fetchMgr _mgr){
		
		m_fetchMain 		= _mgr;
		m_mailIndex 	= _mailIndex;
		m_attachIndex 	= _attachIdx;
		
		try{
			File t_file = new File(m_fetchMain.m_prefix + _mailIndex +"_"+ _attachIdx + ".att");	
			m_fileLength = (int)t_file.length();
			m_file = new FileInputStream(t_file);
			
		}catch(Exception _e){
			m_fetchMain.m_logger.PrinterException(_e);
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
		
		m_fetchMain.m_logger.LogOut("send msgMailAttach mailIndex:" + m_mailIndex + " attachIndex:" + m_attachIndex + " startIndex:" +
									m_startIndex + " size:" + t_size + " first:" + (int)m_buffer[0]);
		
		int t_waitTimer = 0;
		while(m_fetchMain.GetClientConnected() == null || m_fetchMain.GetClientConnected().m_sendReceive == null){
			
			t_waitTimer++;
			
			sleep(10000);
			
			if(t_waitTimer > 5){
				throw new Exception("Client closed when send attachment!");
			}			
		}
		
		m_fetchMain.GetClientConnected().m_sendReceive.SendBufferToSvr(m_os.toByteArray(), _send);
		
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
				m_fetchMain.m_logger.PrinterException(_e);
				break;
			}			
		}
		
		try{
			m_file.close();
		}catch(Exception e){}
		
	}
}

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
		
		int t_confirmTimer = 0;
		boolean t_isCheckFolderError = false;
		
		while(true){
			
			t_isCheckFolderError = false;
			
			try{
				if(m_serverDeamon.m_socket == null 
				|| !m_serverDeamon.m_socket.isConnected()){
					break;
				}
				
				try{
					m_serverDeamon.m_fetchMgr.CheckFolder();
				}catch(Exception e){
					t_isCheckFolderError = true;
					throw e;
				}
				
								
				if(m_serverDeamon.m_socket == null 
				|| !m_serverDeamon.m_socket.isConnected()){
					
					break;
				}
				
				//m_fetchMain.m_logger.LogOut("CheckFolder OK confirm Timer:" + t_confirmTimer);
				
				if(++t_confirmTimer > 30){
					// send the mail without confirm
					//
					t_confirmTimer = 0;
					
					m_serverDeamon.m_fetchMgr.PrepareRepushUnconfirmMail();
				}				

				m_serverDeamon.m_fetchMgr.PushMail(m_sendReceive);
				
				sleep(m_serverDeamon.m_fetchMgr.GetPushInterval() * 1000);
				
			}catch(Exception _e){
				m_serverDeamon.m_fetchMgr.m_logger.PrinterException(_e);
				
				// the network is shutdown in a short time
				//
				try{
					sleep(5000);
					
					if(t_isCheckFolderError){
						m_serverDeamon.m_fetchMgr.ResetSession(false);
					}
					
				}catch(Exception e){
					m_serverDeamon.m_fetchMgr.m_logger.PrinterException(e);
					break;
				}
			}
			
		}
		
		m_sendReceive.CloseSendReceive();
	}
	
}


public class berrySvrDeamon extends Thread{
	
	public fetchMgr		m_fetchMgr = null;
	public Socket		m_socket = null;
		
	sendReceive  		m_sendReceive = null;
	
	int					m_clientVer = 0;
	
	private berrySvrPush m_pushDeamon = null;
	
	public berrySvrDeamon(fetchMgr _mgr,Socket _s)throws Exception{
		m_fetchMgr 	= _mgr;
					
		try{
			
			m_fetchMgr.m_logger.LogOut("some client<"+ _s.getInetAddress().getHostAddress() +"> connecting ,waiting for auth");
			
			// first handshake with the client via CA instead of 
			// InputStream.read function to get the information within 1sec time out
			//
			if(_s instanceof SSLSocket){
				//((SSLSocket)_s).startHandshake();
			}
			
			// wait for signIn first
			//
			_s.setSoTimeout(60000);			
			
			sendReceive t_tmp = new sendReceive(_s.getOutputStream(),_s.getInputStream());
			ByteArrayInputStream in = null;
			try{
				in = new ByteArrayInputStream(t_tmp.RecvBufferFromSvr());
			}catch(Exception e){
				t_tmp.CloseSendReceive();
			}
			
						
			final int t_msg_head = in.read();
		
			if(msg_head.msgConfirm != t_msg_head 
			|| !sendReceive.ReadString(in).equals(m_fetchMgr.m_userPassword)
			|| (m_clientVer = sendReceive.ReadInt(in)) == 0){
				
				/* useless
				 * 
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				os.write(msg_head.msgNote);
				sendReceive.WriteString(os, msg_head.noteErrorUserPassword,m_fetchMgr.m_convertToSimpleChar);
				
				_s.getOutputStream().write(os.toByteArray());
				*/
				
				m_fetchMgr.m_logger.LogOut("illeagel client<"+ _s.getInetAddress().getHostAddress() +"> connected.");
				
				_s.close();
				
				t_tmp.CloseSendReceive();
								
				return;
			}
						
			t_tmp.CloseSendReceive();
			
		}catch(Exception _e){
			// time out or other problem
			//
			_s.close();
			m_fetchMgr.m_logger.PrinterException(_e);
			
			return;
		}
		
		_s.setSoTimeout(0);
		_s.setKeepAlive(true);
		
		if(m_fetchMgr.GetClientConnected() != null 
		&& m_fetchMgr.GetClientConnected().m_socket != null){
			
			// kick the former client
			//
			m_fetchMgr.GetClientConnected().m_socket.close();
			
			while(m_fetchMgr.GetClientConnected() != null){
				sleep(10);
			}
		}		
	
		m_fetchMgr.SetClientConnected(this);
		
		// prepare receive and push deamon
		//
		m_socket	= _s;

		try{
			m_pushDeamon = new berrySvrPush(this);
			m_sendReceive = new sendReceive(m_socket.getOutputStream(),m_socket.getInputStream());	
		}catch(Exception _e){
			m_fetchMgr.m_logger.LogOut("construct berrySvrDeamon error " + _e.getMessage());
			_e.printStackTrace(m_fetchMgr.m_logger.GetPrintStream());
			
			if(m_sendReceive != null){
				m_sendReceive.CloseSendReceive();
			}
						
			throw _e;
		}
				
		start();
		
		m_fetchMgr.m_logger.LogOut("some client connect IP<" + m_socket.getInetAddress().getHostAddress() + ">");
	}
		
	public void run(){
		
		// loop
		//
		while(true){
			
			// process....
			//
			try{
				
				m_fetchMgr.SetClientConnected(this);
				
				byte[] t_package = m_sendReceive.RecvBufferFromSvr();
				
				m_fetchMgr.m_logger.LogOut("receive package length:" + t_package.length);
				
				ProcessPackage(t_package);
				
			}catch(Exception _e){
				
				try{
					if(m_socket != null){
						m_socket.close();
					}									
				}catch(Exception e){
					m_fetchMgr.m_logger.PrinterException(_e);
				}
				
				
				m_socket = null;
				m_sendReceive.CloseSendReceive();
				m_fetchMgr.SetClientConnected(null);
				
				m_pushDeamon.interrupt();
				
				m_fetchMgr.m_logger.PrinterException(_e);
				
				// prepare repush unconfirm mail vector
				//
				m_fetchMgr.PrepareRepushUnconfirmMail();
				
				break;
			}
		}

	}
	
	private void ProcessPackage(byte[] _package)throws Exception{
		ByteArrayInputStream in = new ByteArrayInputStream(_package);
		
		final int t_msg_head = in.read();
				
		switch(t_msg_head){			
			case msg_head.msgMail:
				ProcessMail(in);
				break;
			case msg_head.msgBeenRead:
				ProcessBeenReadMail(in);
				break;
			case msg_head.msgMailAttach:
				ProcessMailAttach(in);
				break;
			case msg_head.msgFetchAttach:
				ProcessFetchMailAttach(in);
				break;
			case msg_head.msgKeepLive:
				break;
			case msg_head.msgMailConfirm:
				ProcessMailConfirm(in);
				break;
			case msg_head.msgSponsorList:
				ProcessSponsorList(in);
				break;
			default:
				throw new Exception("illegal client connect");
		}
	}
	
	private void ProcessSponsorList(ByteArrayInputStream _in){
		try{
			
			// read the google code host page
			//
			final String ft_URL = new String("http://code.google.com/p/yuchberry/wiki/Thanks_sheet");
			String charSet = null;
			StringBuffer t_stringBuffer = new StringBuffer();
			
			URL url = new URL(ft_URL);
			URLConnection con = url.openConnection();
			con.setAllowUserInteraction(false);
			con.connect();
			   
			String type = URLConnection.guessContentTypeFromStream(con.getInputStream());
			
			if (type == null)
				type = con.getContentType();
			
			if (type == null || type.trim().length() == 0 || type.trim().indexOf("text/html") < 0){
				return ;
			}
			
			if(type.indexOf("charset=") > 0){
				charSet = type.substring(type.indexOf("charset=") + 8);
			}
						
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), charSet));
			
			String temp;
			while ((temp = in.readLine()) != null) {
				t_stringBuffer.append(temp+"\n");
			}
			in.close();
	        	        
	        String t_line = fetchMgr.ParseHTMLText(t_stringBuffer.toString(),false);
	        
	        final int t_start = t_line.indexOf("##@##");
	        final int t_end = t_line.indexOf("@@#@@");
	        if(t_start != -1 && t_end != -1){
	        	t_line = t_line.substring(t_start + 5 ,t_end);
	        }
	        t_line = t_line.replace("&para;","");
	        
	        ByteArrayOutputStream t_os = new ByteArrayOutputStream();
	        t_os.write(msg_head.msgSponsorList);
	        sendReceive.WriteString(t_os,t_line,m_fetchMgr.m_convertToSimpleChar);
	        
	        m_sendReceive.SendBufferToSvr(t_os.toByteArray(),true);
	        
		}catch(Exception _e){}
		
	}
	
	private void ProcessMailConfirm(ByteArrayInputStream in)throws Exception{
		
		final int t_mailIndex = sendReceive.ReadInt(in);
		
		synchronized(m_fetchMgr){
			Vector t_unreadMailVector_confirm = m_fetchMgr.m_unreadMailVector_confirm;
			
			for(int i = 0;i < t_unreadMailVector_confirm.size();i++){
				fetchMail t_confirmMail = (fetchMail)t_unreadMailVector_confirm.elementAt(i); 
				if(t_confirmMail.GetMailIndex() == t_mailIndex){
					t_unreadMailVector_confirm.removeElementAt(i);
					
					m_fetchMgr.m_logger.LogOut("Mail Index<" + t_mailIndex + "> confirmed");
					break;
				}
			}
			
		}
	}
	
	private void ProcessMail(ByteArrayInputStream in)throws Exception{
		
		fetchMail t_mail = new fetchMail(m_fetchMgr.m_convertToSimpleChar);
		t_mail.InputMail(in);
		
		fetchMail t_forwardReplyMail = null;
				
		final int t_style = in.read();
		
		if(t_style != fetchMail.NOTHING_STYLE){
			t_forwardReplyMail = new fetchMail(m_fetchMgr.m_convertToSimpleChar);
			t_forwardReplyMail.InputMail(in);
		}
		
		if(t_mail.GetAttachment().isEmpty()){
			SendMailToSvr(new RecvMailAttach(t_mail,t_forwardReplyMail,t_style));
		}else{
			m_fetchMgr.CreateTmpSendMailAttachFile(new RecvMailAttach(t_mail,t_forwardReplyMail,t_style));
		}
	}
	private void ProcessFetchMailAttach(InputStream in)throws Exception{
		final int t_mailIndex = sendReceive.ReadInt(in);
		final int t_attachIndex = sendReceive.ReadInt(in);
		
		new berrySendAttachment(t_mailIndex,t_attachIndex,m_fetchMgr);
	}
	
	
	
	private void ProcessMailAttach(ByteArrayInputStream in)throws Exception{
		
		long t_time = sendReceive.ReadLong(in);
		
		final int t_attachmentIdx = sendReceive.ReadInt(in);
		final int t_segIdx = sendReceive.ReadInt(in);
		final int t_segSize = sendReceive.ReadInt(in);
		
		String t_filename = m_fetchMgr.m_prefix + t_time + "_" + t_attachmentIdx + ".satt";
		File t_file = new File(t_filename);
		
		if(t_segIdx + t_segSize > t_file.length()){
			throw new Exception("error attach" + t_filename + " idx and size");
		}
		
		m_fetchMgr.m_logger.LogOut("recv msgMailAttach time:"+ t_time + " beginIndex:" + t_segIdx + " size:" + t_segSize);
		
		byte[] t_bytes = new byte[t_segSize];
		sendReceive.ForceReadByte(in, t_bytes, t_segSize);
		
		RandomAccessFile t_fwrite = new RandomAccessFile(t_file,"rw");
		t_fwrite.seek(t_segIdx);
		t_fwrite.write(t_bytes);
		
		t_fwrite.close();
		
		if(t_segIdx + t_segSize == t_file.length()){
			
			RecvMailAttach t_mail = m_fetchMgr.FindAttachMail(t_time);
			
			if((t_mail) != null && (t_attachmentIdx + 1) >= t_mail.m_sendMail.GetAttachment().size()){
				SendMailToSvr(t_mail);
			}
		}
	}
	
	public void SendMailToSvr(final RecvMailAttach _mail)throws Exception{
		
		// receive send message to berry
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		os.write(msg_head.msgSendMail);
		int t_succ = 1;
		
		try{
			
			m_fetchMgr.SendMail(_mail);
			
		}catch(Exception _e){
			
			ByteArrayOutputStream error = new ByteArrayOutputStream();
			error.write(msg_head.msgNote);
			sendReceive.WriteString(error, _e.getMessage(),m_fetchMgr.m_convertToSimpleChar);
			
			m_sendReceive.SendBufferToSvr(error.toByteArray(), false);
			
			t_succ = 0;
		}
		
		os.write(t_succ);
		
		sendReceive.WriteInt(os,(int)_mail.m_sendMail.GetSendDate().getTime());
		sendReceive.WriteInt(os,(int)(_mail.m_sendMail.GetSendDate().getTime() >>> 32));

		m_sendReceive.SendBufferToSvr(os.toByteArray(),false);
		
		m_fetchMgr.m_logger.LogOut("Mail <" +_mail.m_sendMail.GetSendDate().getTime() +  "> send " + ((t_succ == 1)?"Succ":"Failed"));
	}
	
	private void ProcessBeenReadMail(ByteArrayInputStream in)throws Exception{
		final int t_mailIndex = sendReceive.ReadInt(in);		
		try{
			m_fetchMgr.MarkReadMail(t_mailIndex);
		}catch(Exception _e){}
	}
}
