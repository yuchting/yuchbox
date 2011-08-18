package com.yuchting.yuchberry.client;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Vector;

import javax.microedition.io.file.FileConnection;

public class SendAttachmentDeamon extends Thread{
	
	connectDeamon		m_connect 	= null;
	int					m_sendHashCode = 0;
	
	InputStream 		m_fileIn 	= null;
	FileConnection		m_fileConnection = null;
	
	ISendAttachmentCallback	m_sendCallback = null;
		
	int 				m_beginIndex = 0;
	
	int 				m_totalSize = 0;
	int					m_uploadedSize = 0;
	
	int					m_attachmentIndex = 0;
	Vector				m_vFileConnection = null;
		
	final static private int fsm_segmentSize = 512;
	
	byte[] 				m_bufferBytes 		= new byte[fsm_segmentSize];
	
	public	boolean	m_closeState = false;
		
	public SendAttachmentDeamon(connectDeamon _connect,
								Vector _vFileConnection,int _sendHashCode,
								ISendAttachmentCallback _sendCallback)throws Exception{
		
		m_connect			= _connect;
		m_vFileConnection	= _vFileConnection;
		m_sendHashCode		= _sendHashCode;
		m_sendCallback		= _sendCallback;
		
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		os.write(msg_head.msgFileAttach);
		sendReceive.WriteInt(os,m_sendHashCode);
		sendReceive.WriteInt(os,m_vFileConnection.size());
		
		for(int i = 0;i < m_vFileConnection.size();i++){
			FileConnection t_file = (FileConnection)m_vFileConnection.elementAt(i);
			
			int t_fileSize =(int)t_file.fileSize(); 
			m_totalSize += t_fileSize;
			
			sendReceive.WriteInt(os, t_fileSize);			
		}
		
		_connect.m_connect.SendBufferToSvr(os.toByteArray(), true, false);
				
		m_fileConnection = (FileConnection)m_vFileConnection.elementAt(m_attachmentIndex);
		m_fileIn = m_fileConnection.openInputStream();
										
		start();
	}
	
	private void ReleaseAttachFile(){
		try{
			
			for(int i = 0;i < m_vFileConnection.size();i++){
				FileConnection t_file = (FileConnection)m_vFileConnection.elementAt(i);
				t_file.close();
			}
			
		}catch(Exception e){}
	}
	
	private synchronized boolean SendFileSegment(final boolean _send)throws Exception{
		
		if(m_closeState){
			return true;
		}
		
		ByteArrayOutputStream t_os = new ByteArrayOutputStream();
		
		final int t_size = (m_beginIndex + fsm_segmentSize) > (int)m_fileConnection.fileSize()?
							((int)m_fileConnection.fileSize() - m_beginIndex) : fsm_segmentSize;
		try{
			
			sendReceive.ForceReadByte(m_fileIn, m_bufferBytes, t_size);
			
		}catch(Exception _e){
			try{
				sleep(5000);
				m_connect.m_mainApp.SetErrorString("SA: read file fail" + _e.getMessage() + _e.getClass().getName());
			}catch(Exception ex){}
			
			m_fileIn.close();
			
			m_fileIn = m_fileConnection.openInputStream();
			m_fileIn.skip(m_beginIndex);
			
			// try again...
			//
			try{
				sendReceive.ForceReadByte(m_fileIn, m_bufferBytes, t_size);
			}catch(Exception _ex){
				// failed again...
				//
				throw new Exception("SA: read file fail again, close. " + _e.getMessage() + _e.getClass().getName());
			}
			
		}
		
		t_os.write(msg_head.msgFileAttachSeg);
		sendReceive.WriteInt(t_os, m_sendHashCode);
		sendReceive.WriteInt(t_os, m_attachmentIndex);
		sendReceive.WriteInt(t_os, m_beginIndex);
		sendReceive.WriteInt(t_os, t_size);
		t_os.write(m_bufferBytes,0,t_size);
		
		m_connect.m_connect.SendBufferToSvr(t_os.toByteArray(), _send,false);
		
		System.out.println("send msgMailAttach time:"+ m_sendHashCode + " attIdx<" +m_attachmentIndex + "> beginIndex<" + m_beginIndex + "> size:" + t_size);
		
		m_sendCallback.sendProgress(m_attachmentIndex, m_uploadedSize, m_totalSize);
		
//		m_connect.m_mainApp.SetUploadingDesc(m_sendMail,m_attachmentIndex,
//											m_uploadedSize,m_totalSize);
		
		
		if((m_beginIndex + t_size) >= (int)m_fileConnection.fileSize()){
			
			m_beginIndex = 0;
			
			return true;
			
		}else{
			m_beginIndex += t_size;
		}
		
		m_uploadedSize += t_size;
		t_os.close();
		
		return false;
	}
	
	public synchronized void sendNextFile(int _attachIndex){
		
		try{
			
			if(_attachIndex != m_attachmentIndex){
				
				m_closeState = true;
				
			}else{
				
				m_beginIndex = 0;
				m_attachmentIndex++;
				
				m_fileIn.close();
				m_fileConnection.close();
				
				m_fileIn = null;
				
				if(m_attachmentIndex >= m_vFileConnection.size()){
					
					// send over
					//
					//m_sendCallback.sendProgress(m_sendHashCode, -2, 0, 0);
					//m_connect.m_mainApp.SetUploadingDesc(m_sendMail,-2,0,0);
					
					m_sendCallback.sendFinish();
					
					m_closeState = true;
				
				}else{
					m_fileConnection = (FileConnection)m_vFileConnection.elementAt(m_attachmentIndex);
					m_fileIn = m_fileConnection.openInputStream();
				}
			}			
			
			if(isAlive()){
				interrupt();
			}
			
		}catch(Exception _e){
			m_connect.m_mainApp.SetErrorString("SA: sendNexFile " + _e.getMessage() + _e.getClass().getName());
		}		
		
	}
	
	public void run(){		

		boolean t_setPaddingState = false;
		
		while(true){
			
			if(m_closeState){
				break;
			}
			
			while(m_connect.m_conn == null || !m_connect.m_sendAuthMsg){
				
				if(!m_connect.IsConnectState()){
					ReleaseAttachFile();
					
					m_sendCallback.sendError();
										
					return;
				}else{
				
					if(!t_setPaddingState){
						t_setPaddingState = true;
						m_sendCallback.sendPause();
					}
				}
				
				try{
					sleep(10000);
				}catch(Exception _e){
					break;
				}
			}
			
			if(m_closeState){
				break;
			}
			
			try{

				m_sendCallback.sendStart();
				
				boolean t_sendOver = false;
				int t_sendSegmentNum = 0;
				while(t_sendSegmentNum++ < 4){
					
					if(SendFileSegment(false)){
						t_sendOver = true;
						break;
					}					
				}
				
				if(!t_sendOver && SendFileSegment(true)){
					t_sendOver = true;
				}
				
				if(m_closeState){
					break;
				}
								
				if(t_sendOver){
					try{
						sleep(90 * 1000);
					}catch(Exception e){
						m_connect.m_mainApp.SetErrorString("SA: OK " + e.getMessage() +  e.getClass().getName());			
					}
				}										
				
			}catch(Exception _e){
				m_connect.m_mainApp.SetErrorString("SA: " + _e.getMessage() + _e.getClass().getName());				
			}		
		}
		
		ReleaseAttachFile();
	}	
}
