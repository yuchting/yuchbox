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
package com.yuchting.yuchberry.client;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

public class SendAttachmentDeamon extends Thread{
	
	connectDeamon		m_connect 	= null;
	int					m_sendHashCode = 0;
	
	int					m_currOpenFileSize = 0;
	
	ISendAttachmentCallback	m_sendCallback = null;
		
	int 				m_beginIndex = 0;
	
	int 				m_totalSize = 0;
	int					m_uploadedSize = 0;
	
	int					m_attachmentIndex = 0;
	Vector				m_vFileConnection = null;
	
	byte[]				m_uploadingBuffer = null;
		
	final static private int fsm_segmentSize = 512;
	
	byte[] 				m_bufferBytes 		= new byte[fsm_segmentSize];
	
	/**
	 * the read file buffer size
	 */
	final static private int fsm_fileBufferSize = 8192 * 8;
	
	private byte[]		m_fileBuffer		= null;
	
	private int		m_fileBufferReadIdx = 0;
	
	public	boolean	m_closeState = false;
		
	public SendAttachmentDeamon(connectDeamon _connect,
								Vector _vFileConnection,int _sendHashCode,
								ISendAttachmentCallback _sendCallback)throws Exception{
		
		if(_sendCallback == null){
			throw new IllegalArgumentException("SendAttachmentDeamon _sendCallback == null");
		}
		
		if(_vFileConnection == null){
			throw new IllegalArgumentException("SendAttachmentDeamon _vFileConnection == null"); 
		}
		
		m_connect			= _connect;
		m_vFileConnection	= _vFileConnection;
		m_sendHashCode		= _sendHashCode;
		m_sendCallback		= _sendCallback;
		
		start();
	}
	
	public SendAttachmentDeamon(connectDeamon _connect,
					byte[] _buffer,int _sendHashCode,
					ISendAttachmentCallback _sendCallback)throws Exception{
		
		if(_buffer == null){
			throw new IllegalArgumentException("SendAttachmentDeamon _buffer == null"); 
		}
		
		if(_sendCallback == null){
			throw new IllegalArgumentException("SendAttachmentDeamon _sendCallback == null");
		}
		
		m_connect			= _connect;
		m_uploadingBuffer	= _buffer;
		m_sendHashCode		= _sendHashCode;
		m_sendCallback		= _sendCallback;
		m_totalSize			= _buffer.length;
									
		start();
	}
	
	private void ReleaseAttachFile(){
		m_uploadingBuffer	= null;
		m_fileBuffer		= null;
	}
	
	private void openFileToRead()throws Exception{
		
		if(m_fileBuffer == null){
			m_fileBuffer = new byte[fsm_fileBufferSize];
		}
		
		m_fileBufferReadIdx = 0;
		
		FileConnection t_file = (FileConnection)Connector.open((String)m_vFileConnection.elementAt(m_attachmentIndex),Connector.READ);
		try{
			m_currOpenFileSize = (int)t_file.fileSize();
			int t_readSize = (m_beginIndex + fsm_fileBufferSize) > (int)m_currOpenFileSize? ((int)m_currOpenFileSize - m_beginIndex) : fsm_fileBufferSize;
			
			InputStream in = t_file.openInputStream();
			try{
				in.skip(m_beginIndex);
				sendReceive.ForceReadByte(in, m_fileBuffer, t_readSize);
			}finally{
				in.close();
			}
			
		}finally{
			t_file.close();
		}
	}
	
	private synchronized boolean SendFileSegment(final boolean _send)throws Exception{
		
		if(m_closeState){
			return true;
		}
		
		try{
			
			if(m_currOpenFileSize == 0 || m_fileBufferReadIdx >= fsm_fileBufferSize){
				openFileToRead();
			}
			
		}catch(Exception _e){
			
			// try again...
			//
			m_connect.m_mainApp.SetErrorString("SA",_e);
			
			try{
				sleep(5000);					
			}catch(Exception ex){
				if(m_closeState){
					throw new Exception("SFS:closed");
				}
			}
			
			// try it again
			openFileToRead();
		}
		
		// current upload buffer total size
		int t_currSize = (m_vFileConnection != null)?(int)m_currOpenFileSize:m_uploadingBuffer.length;
		
		// send buffer segment size
		int t_size = (m_beginIndex + fsm_segmentSize) > (int)t_currSize?((int)t_currSize - m_beginIndex) : fsm_segmentSize;
				
		if(m_vFileConnection != null){

			for(int i = 0;i < t_size;i++,m_fileBufferReadIdx++){
				m_bufferBytes[i] = m_fileBuffer[m_fileBufferReadIdx];
			}
			
		}else{
			
			int t_index = m_beginIndex;
			for(int i = 0;i < t_size;i++,t_index++){
				m_bufferBytes[i] = m_uploadingBuffer[t_index];
			}
		}
		
		ByteArrayOutputStream t_os = new ByteArrayOutputStream();
		try{
			t_os.write(msg_head.msgFileAttachSeg);
			
			sendReceive.WriteInt(t_os, m_sendHashCode);
			sendReceive.WriteInt(t_os, m_attachmentIndex);
			sendReceive.WriteInt(t_os, m_beginIndex);
			sendReceive.WriteInt(t_os, t_size);
			t_os.write(m_bufferBytes,0,t_size);
					
			m_connect.m_connect.SendBufferToSvr(t_os.toByteArray(), _send,false);
			
		}finally{
			t_os.close();
		}
				
		//System.out.println("send msgMailAttach time:"+ m_sendHashCode + " attIdx<" +m_attachmentIndex + "> beginIndex<" + m_beginIndex + "> size:" + t_size);
		m_sendCallback.sendProgress(m_attachmentIndex, m_uploadedSize, m_totalSize);
				
		if((m_beginIndex + t_size) >= t_currSize){
			
			m_beginIndex 		= 0;
			m_currOpenFileSize	= 0;
			
			return true;			
		}else{
			m_beginIndex += t_size;
		}
		
		m_uploadedSize += t_size;				
		return false;
	}
	
	public synchronized void sendNextFile(int _attachIndex){
		
		try{
			
			if(m_vFileConnection != null){
				
				if(_attachIndex != m_attachmentIndex){
					
					m_closeState = true;
					
				}else{
					
					m_beginIndex 		= 0;
					m_currOpenFileSize	= 0;
					m_fileBufferReadIdx	= 0;
					m_attachmentIndex++;
					
					if(m_attachmentIndex >= m_vFileConnection.size()){
						
						// send over
						//
						//m_sendCallback.sendProgress(m_sendHashCode, -2, 0, 0);
						//m_connect.m_mainApp.SetUploadingDesc(m_sendMail,-2,0,0);
						
						m_sendCallback.sendFinish();	
						m_closeState = true;
					}
				}	
			}else{
				
				m_sendCallback.sendFinish();
				m_closeState = true;
			}
					
			
			if(isAlive()){
				interrupt();
			}
			
		}catch(Exception _e){
			m_connect.m_mainApp.SetErrorString("SASNF",_e);
		}		
		
	}
	
	private void sendFileCreateMsg()throws Exception{
		
		if(m_vFileConnection != null){
			
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			os.write(msg_head.msgFileAttach);
			sendReceive.WriteInt(os,m_sendHashCode);
			sendReceive.WriteInt(os,m_vFileConnection.size());
			
			for(int i = 0;i < m_vFileConnection.size();i++){
				
				String t_filename = (String)m_vFileConnection.elementAt(i);
				FileConnection t_file = (FileConnection)Connector.open(t_filename,Connector.READ);
				
				try{
					int t_fileSize =(int)t_file.fileSize(); 
					m_totalSize += t_fileSize;
					
					sendReceive.WriteInt(os, t_fileSize);
					
				}finally{
					t_file.close();
				}			
			}
			
			m_connect.m_connect.SendBufferToSvr(os.toByteArray(), true, false);
		}else{
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			os.write(msg_head.msgFileAttach);
			sendReceive.WriteInt(os,m_sendHashCode);
			sendReceive.WriteInt(os,1);
			sendReceive.WriteInt(os, m_uploadingBuffer.length);			
			
			m_connect.m_connect.SendBufferToSvr(os.toByteArray(), true, false);
		}	
	}
	
	public void run(){		

		boolean t_setPaddingState = false;
		boolean t_sendFileCreate = false;
		
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

				if(!t_sendFileCreate){
					t_sendFileCreate = true;
					sendFileCreateMsg();
				}
				
				
				m_sendCallback.sendStart();
				
				boolean t_sendOver = false;
				int t_sendSegmentNum = 0;
				while(t_sendSegmentNum++ < 6){
					
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
						
						t_sendFileCreate = false;
						
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
