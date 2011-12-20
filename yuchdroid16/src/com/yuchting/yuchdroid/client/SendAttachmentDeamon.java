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
package com.yuchting.yuchdroid.client;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Vector;


public class SendAttachmentDeamon extends Thread{
	
	public static String		TAG			= SendAttachmentDeamon.class.getName();
	
	ConnectDeamon		m_connect 	= null;
	int					m_sendHashCode = 0;
	
	InputStream 		m_fileIn 	= null;
	File			m_fileConnection = null;
	
	ISendAttachmentCallback	m_sendCallback = null;
		
	int 				m_beginIndex = 0;
	
	int 				m_totalSize = 0;
	int					m_uploadedSize = 0;
	
	int					m_attachmentIndex = 0;
	Vector<File>	m_vFileConnection = null;
	
	byte[]				m_uploadingBuffer = null;
		
	final static private int fsm_segmentSize = 512;
	
	byte[] 				m_bufferBytes 		= new byte[fsm_segmentSize];
	
	public	boolean	m_closeState = false;
	
		
	public SendAttachmentDeamon(ConnectDeamon _connect,
								Vector<File> _vFileConnection,int _sendHashCode,
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
						
		m_fileConnection = (File)m_vFileConnection.elementAt(m_attachmentIndex);
		m_fileIn = new FileInputStream(m_fileConnection);
				
		start();
	}
	
	public void inter(){
		
		if(!m_closeState){
			m_closeState = true;
			
			if(isAlive()){
				interrupt();				
			}			
		}
	}
	
	public SendAttachmentDeamon(ConnectDeamon _connect,
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
		try{
			
			if(m_fileIn != null){
				m_fileIn.close();
			}
			
			m_uploadingBuffer = null;			
			
		}catch(Exception e){}
	}
	
	private synchronized boolean SendFileSegment(final boolean _send)throws Exception{
		
		if(m_closeState){
			return true;
		}
				
		ByteArrayOutputStream t_os = new ByteArrayOutputStream();
		
		int t_currSize = (m_vFileConnection != null)?(int)m_fileConnection.length():m_uploadingBuffer.length;
		int t_size = (m_beginIndex + fsm_segmentSize) > (int)t_currSize?
								((int)t_currSize - m_beginIndex) : fsm_segmentSize;
				
		if(m_vFileConnection != null){
			
			try{
				
				sendReceive.ForceReadByte(m_fileIn, m_bufferBytes, t_size);
				
			}catch(Exception _e){
				try{
					sleep(5000);
					m_connect.m_mainApp.setErrorString("SA: read file fail" + _e.getMessage() + _e.getClass().getName());
				}catch(Exception ex){}
				
				m_fileIn.close();
				
				m_fileIn = new FileInputStream(m_fileConnection);
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
		}else{
								
			int t_index = m_beginIndex;
			for(int i = 0;i < t_size;i++,t_index++){
				m_bufferBytes[i] = m_uploadingBuffer[t_index];
			}
		}
		
		t_os.write(msg_head.msgFileAttachSeg);
		sendReceive.WriteInt(t_os, m_sendHashCode);
		sendReceive.WriteInt(t_os, m_attachmentIndex);
		sendReceive.WriteInt(t_os, m_beginIndex);
		sendReceive.WriteInt(t_os, t_size);
		t_os.write(m_bufferBytes,0,t_size);
				
		m_connect.m_connect.SendBufferToSvr(t_os.toByteArray(), _send);
		
		//System.out.println("send msgMailAttach time:"+ m_sendHashCode + " attIdx<" +m_attachmentIndex + "> beginIndex<" + m_beginIndex + "> size:" + t_size);
		
		m_sendCallback.sendProgress(m_attachmentIndex, m_uploadedSize, m_totalSize);
		
		
		if((m_beginIndex + t_size) >= t_currSize){
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
			
			if(m_vFileConnection != null){
				
				if(_attachIndex != m_attachmentIndex){
					
					m_closeState = true;
					
				}else{
					
					m_beginIndex = 0;
					m_attachmentIndex++;
					
					m_fileIn.close();
					
					m_fileIn = null;
					
					if(m_attachmentIndex >= m_vFileConnection.size()){
						
						m_sendCallback.sendFinish();						
						m_closeState = true;
					
					}else{
						m_fileConnection = (File)m_vFileConnection.elementAt(m_attachmentIndex);
						m_fileIn = new FileInputStream(m_fileConnection);
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
			m_connect.m_mainApp.setErrorString("SA: sendNexFile " + _e.getMessage() + _e.getClass().getName());
		}		
		
	}
	
	private void sendFileCreateMsg()throws Exception{
		
		if(m_vFileConnection != null){
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			os.write(msg_head.msgFileAttach);
			sendReceive.WriteInt(os,m_sendHashCode);
			sendReceive.WriteInt(os,m_vFileConnection.size());
			
			for(int i = 0;i < m_vFileConnection.size();i++){
				File t_file = (File)m_vFileConnection.elementAt(i);
				
				int t_fileSize =(int)t_file.length(); 
				m_totalSize += t_fileSize;
				
				sendReceive.WriteInt(os, t_fileSize);			
			}
			
			m_connect.m_connect.SendBufferToSvr(os.toByteArray(), true);
		}else{
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			os.write(msg_head.msgFileAttach);
			sendReceive.WriteInt(os,m_sendHashCode);
			sendReceive.WriteInt(os,1);
			sendReceive.WriteInt(os, m_uploadingBuffer.length);			
			
			m_connect.m_connect.SendBufferToSvr(os.toByteArray(), true);
		}	
	}
	
	public void run(){		

		m_connect.acquireWakeLock();
		
		try{
			
			boolean t_setPaddingState = false;
			boolean t_sendFileCreate = false;
			
			while(!m_closeState){
				
				try{
					
					while(!m_connect.m_sendAuthMsg){
						
						if(m_connect.m_destroy){
							
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
						}catch(Exception e){}
						
					}

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
													
					if(t_sendOver){
						try{
							
							sleep(90000);				
							t_sendFileCreate = false;
							
						}catch(Exception e){
							m_connect.m_mainApp.setErrorString("SA: OK " + e.getMessage() +  e.getClass().getName());			
						}
					}										
					
				}catch(Exception _e){
					m_connect.m_mainApp.setErrorString("SA: " + _e.getMessage() + _e.getClass().getName());		
				}		
			}
			
			ReleaseAttachFile();
			
		}finally{
			
			m_connect.releaseWakeLock();
		}
		
	}	
}
