package com.yuchting.yuchberry.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.util.Vector;



class berrySendAttachment extends Thread{
	
	FileInputStream		m_file;
	fetchMgr			m_fetchMain;
	int					m_fileLength;
	int					m_mailIndex;
	int					m_attachIndex;
	
	
	final static int	fsm_sendSize = 512;
	
	berrySendAttachment(int _mailIndex,int _attachIdx,fetchMgr _mgr){
		
		m_fetchMain 		= _mgr;
		m_mailIndex 	= _mailIndex;
		m_attachIndex 	= _attachIdx;
		
		try{
			File t_file = new File("" + _mailIndex +"_"+ _attachIdx + ".att");
			
			m_fileLength = (int)t_file.length();
			
			m_file = new FileInputStream(t_file);
			
		}catch(Exception _e){
			Logger.PrinterException(_e);
			return;
		}
		
		start();
	}
	
	public void run(){
		
		int t_startIndex = 0;
		byte[] t_buffer = new byte[fsm_sendSize];
		
		ByteArrayOutputStream t_os = new ByteArrayOutputStream();
		
		while(true){
			try{
				t_os.reset();
				
				final int t_size = (t_startIndex + fsm_sendSize) > m_fileLength ?(m_fileLength - t_startIndex):fsm_sendSize;
				m_file.read(t_buffer, 0, t_size);
				t_os.write(msg_head.msgMailAttach);
				
				sendReceive.WriteInt(t_os,m_mailIndex);
				sendReceive.WriteInt(t_os,m_attachIndex);
				sendReceive.WriteInt(t_os,t_startIndex);
				sendReceive.WriteInt(t_os,t_size);
				t_os.write(t_buffer);
				
				Logger.LogOut("send msgMailAttach mailIndex:" + m_mailIndex + " attachIndex:" + m_attachIndex + " startIndex:" +
									t_startIndex + " size:" + t_size + " first:" + (int)t_buffer[0]);
				
				while(m_fetchMain.GetClientConnected() == null){
					sleep(200);
				}
				
				m_fetchMain.GetClientConnected().m_sendReceive.SendBufferToSvr(t_os.toByteArray(), true);
				
				if(t_startIndex + t_size >= m_fileLength){
					break;
				}
				
				t_startIndex += t_size;				
				
				
			}catch(Exception _e){
				Logger.PrinterException(_e);
			}			
		}
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
		
		while(true){
			
			try{
				if(m_serverDeamon.m_socket == null 
				|| !m_serverDeamon.m_socket.isConnected()){
					break;
				}
				
				m_serverDeamon.m_fetchMgr.CheckFolder();
				
				if(m_serverDeamon.m_socket == null 
				|| !m_serverDeamon.m_socket.isConnected()){
					
					break;
				}
				
				//Logger.LogOut("CheckFolder OK confirm Timer:" + t_confirmTimer);
				
				if(++t_confirmTimer > 10){
					// send the mail without confirm
					//
					t_confirmTimer = 0;
					
					m_serverDeamon.m_fetchMgr.PrepareRepushUnconfirmMail();
				}				

				Vector t_unreadMailVector = m_serverDeamon.m_fetchMgr.m_unreadMailVector;
				Vector t_unreadMailVector_confirm = m_serverDeamon.m_fetchMgr.m_unreadMailVector_confirm;
				
				while(!t_unreadMailVector.isEmpty()){
					
					fetchMail t_mail = (fetchMail)t_unreadMailVector.elementAt(0); 
					
					ByteArrayOutputStream t_output = new ByteArrayOutputStream();
					
					t_output.write(msg_head.msgMail);
					t_mail.OutputMail(t_output);
					
					m_sendReceive.SendBufferToSvr(t_output.toByteArray(),false);
					
					m_serverDeamon.m_fetchMgr.SetBeginFetchIndex(t_mail.GetMailIndex());
					
					synchronized(m_serverDeamon.m_fetchMgr) {
						t_unreadMailVector.remove(0);
						t_unreadMailVector_confirm.addElement(t_mail);
					}
					
					Logger.LogOut("send mail<" + t_mail.GetMailIndex() + ">,wait confirm...");
				}				
				
				sleep(m_serverDeamon.m_fetchMgr.GetPushInterval());
				
			}catch(Exception _e){
				Logger.PrinterException(_e);
				
				if(_e instanceof javax.mail.MessagingException){
					// the network is shutdown in a short time
					//
					try{
						sleep(5000);
						m_serverDeamon.m_fetchMgr.ResetSession();
					}catch(Exception e){
						Logger.PrinterException(e);
						break;
					}
				}
					
				
			}
			
		}
		
		m_sendReceive.CloseSendReceive();
	}
	
}


public class berrySvrDeamon extends Thread{
	
	public fetchMgr		m_fetchMgr = null;
	public Socket		m_socket = null;
	
	sendReceive  m_sendReceive = null;
	
	
	private berrySvrPush m_pushDeamon = null;
	
	private Vector			m_recvMailAttach = new Vector();
	
	public berrySvrDeamon(fetchMgr _mgr,Socket _s)throws Exception{
		m_fetchMgr 	= _mgr;
		
		// wait for signIn first
		//
		_s.setSoTimeout(1000);
		
		try{
			sendReceive t_tmp = new sendReceive(_s.getOutputStream(),_s.getInputStream());
			ByteArrayInputStream in = new ByteArrayInputStream(t_tmp.RecvBufferFromSvr());
						
			final int t_msg_head = in.read();
		
			if(msg_head.msgConfirm != t_msg_head 
			|| !sendReceive.ReadString(in).equals(m_fetchMgr.m_userPassword)){
				
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				os.write(msg_head.msgNote);
				sendReceive.WriteString(os, msg_head.noteErrorUserPassword);
				
				_s.getOutputStream().write(os.toByteArray());
				_s.close();
				
				t_tmp.CloseSendReceive();
				
				return;
			}
			
			t_tmp.CloseSendReceive();
			
		}catch(Exception _e){
			// time out
			//
			_s.close();
			
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
			Logger.LogOut("construct berrySvrDeamon error " + _e.getMessage());
			_e.printStackTrace(Logger.GetPrintStream());
			
			if(m_sendReceive != null){
				m_sendReceive.CloseSendReceive();
			}
						
			throw _e;
		}
				
		start();
		
		Logger.LogOut("some client connect IP<" + m_socket.getInetAddress().getHostAddress() + ">");
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
				
				Logger.LogOut("receive package length:" + t_package.length);
				
				ProcessPackage(t_package);
				
			}catch(Exception _e){
				
				try{
					m_socket.close();					
				}catch(Exception e){
					Logger.PrinterException(_e);
				}
				
				
				m_socket = null;
				m_sendReceive.CloseSendReceive();
				m_fetchMgr.SetClientConnected(null);
				
				m_pushDeamon.interrupt();
				
				Logger.PrinterException(_e);
				
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
			default:
				throw new Exception("illegal client connect");
		}
	}
	
	private void ProcessMailConfirm(ByteArrayInputStream in)throws Exception{
		
		final int t_mailIndex = sendReceive.ReadInt(in);
		
		synchronized(m_fetchMgr){
			Vector t_unreadMailVector_confirm = m_fetchMgr.m_unreadMailVector_confirm;
			
			for(int i = 0;i < t_unreadMailVector_confirm.size();i++){
				fetchMail t_confirmMail = (fetchMail)t_unreadMailVector_confirm.elementAt(i); 
				if(t_confirmMail.GetMailIndex() == t_mailIndex){
					t_unreadMailVector_confirm.removeElementAt(i);
					
					Logger.LogOut("Mail Index<" + t_mailIndex + "> confirmed");
					break;
				}
			}
			
		}
	}
	
	private void ProcessMail(ByteArrayInputStream in)throws Exception{
		
		fetchMail t_mail = new fetchMail();
		t_mail.InputMail(in);
		
		if(t_mail.GetAttachment().isEmpty()){
			SendMailToSvr(t_mail);
		}else{
			// create new thread to send mail
			//
			m_recvMailAttach.addElement(t_mail);
			
			Logger.LogOut("send mail with attachment " + t_mail.GetAttachment().size());
			
			CreateTmpSendMailAttachFile(t_mail);
		}
	}
	private void ProcessFetchMailAttach(InputStream in)throws Exception{
		final int t_mailIndex = sendReceive.ReadInt(in);
		final int t_attachIndex = sendReceive.ReadInt(in);
		
		new berrySendAttachment(t_mailIndex,t_attachIndex,m_fetchMgr);
	}
	
	private void CreateTmpSendMailAttachFile(fetchMail _mail)throws Exception{
		Vector t_list = _mail.GetAttachment();
		
		for(int i = 0;i < t_list.size();i++){
			fetchMail.Attachment t_attachment = (fetchMail.Attachment)t_list.elementAt(i);
			
			String t_filename = "" + _mail.GetSendDate().getTime() + "_" + i + ".satt";
			FileOutputStream fos = new FileOutputStream(t_filename);
			
			for(int j = 0;j < t_attachment.m_size;j++){
				fos.write(0);
			}
			
			Logger.LogOut("store attachment " + t_filename + " size:" + t_attachment.m_size);
			
			fos.close();
		}
	}
	
	private void ProcessMailAttach(ByteArrayInputStream in)throws Exception{
		
		long t_time = sendReceive.ReadInt(in);
		t_time |= ((long)sendReceive.ReadInt(in)) << 32;
		
		final int t_attachmentIdx = sendReceive.ReadInt(in);
		final int t_segIdx = sendReceive.ReadInt(in);
		final int t_segSize = sendReceive.ReadInt(in);
		
		String t_filename = "" + t_time + "_" + t_attachmentIdx + ".satt";
		File t_file = new File(t_filename);
		
		if(t_segIdx + t_segSize > t_file.length()){
			throw new Exception("error attach" + t_filename + " idx and size");
		}
		
		Logger.LogOut("recv msgMailAttach time:"+ t_time + " beginIndex:" + t_segIdx + " size:" + t_segSize);
		
		byte[] t_bytes = new byte[t_segSize];
		sendReceive.ForceReadByte(in, t_bytes, t_segSize);
		
		RandomAccessFile t_fwrite = new RandomAccessFile(t_file,"rw");
		t_fwrite.seek(t_segIdx);
		t_fwrite.write(t_bytes);
		
		t_fwrite.close();
		
		if(t_segIdx + t_segSize == t_file.length()){
			// send the file...
			//
			for(int i = 0;i < m_recvMailAttach.size();i++){
				fetchMail t_mail = (fetchMail)m_recvMailAttach.elementAt(i);
				
				if(t_mail.GetSendDate().getTime() == t_time){
					
					SendMailToSvr(t_mail);
					m_recvMailAttach.remove(i);
					break;
				}
			}
		}
	}
	
	public void SendMailToSvr(fetchMail _mail)throws Exception{
		
		// receive send message to berry
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		os.write(msg_head.msgSendMail);
		int t_succ = 1;
		
		try{
			
			m_fetchMgr.SendMail(_mail);
			
		}catch(Exception _e){
			
			ByteArrayOutputStream error = new ByteArrayOutputStream();
			error.write(msg_head.msgNote);
			sendReceive.WriteString(error, _e.getMessage());
			
			m_sendReceive.SendBufferToSvr(error.toByteArray(), false);
			
			t_succ = 0;
		}
		
		os.write(t_succ);
		
		sendReceive.WriteInt(os,(int)_mail.GetSendDate().getTime());
		sendReceive.WriteInt(os,(int)(_mail.GetSendDate().getTime() >>> 32));

		m_sendReceive.SendBufferToSvr(os.toByteArray(),false);		
	}
	
	private void ProcessBeenReadMail(ByteArrayInputStream in)throws Exception{
		final int t_mailIndex = sendReceive.ReadInt(in);		
		try{
			m_fetchMgr.MarkReadMail(t_mailIndex);
		}catch(Exception _e){}
	}
}
