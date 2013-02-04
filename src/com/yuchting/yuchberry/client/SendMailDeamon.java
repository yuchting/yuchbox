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
import java.util.Vector;

import net.rim.blackberry.api.mail.Message;


public class SendMailDeamon extends Thread implements ISendAttachmentCallback{
	
	connectDeamon		m_connect 	= null;
	fetchMail			m_sendMail 	= null;
	fetchMail			m_forwardReply 	= null;

	int					m_sendStyle = fetchMail.NOTHING_STYLE;	
	public	boolean	m_closeState = false;
	
	public SendAttachmentDeamon m_sendFileDaemon = null;
		
	public SendMailDeamon(connectDeamon _connect,
									fetchMail _mail,
									Vector _vFileConnection,
									fetchMail _forwardReply,int _sendStyle)throws Exception{
		m_connect	= _connect;
		m_sendMail	= _mail;
		m_forwardReply	= _forwardReply;
		m_sendStyle = _sendStyle;


		if(!_vFileConnection.isEmpty()){
			m_sendFileDaemon = new SendAttachmentDeamon(_connect, _vFileConnection, 
														m_sendMail.GetSimpleHashCode(), this);
		}else{
			start();
		}		
	}
	
	public void inter(){
		
		if(isAlive()){
			super.interrupt();
		}
		
		if(m_sendFileDaemon != null ){
			m_sendFileDaemon.m_closeState = true;
			if(m_sendFileDaemon.isAlive()){
				m_sendFileDaemon.interrupt();
			}
			
		}
	}
		
	public void sendStart(){
		RefreshMessageStatus(Message.Status.TX_SENDING);
	}
	
	public void sendProgress(int _fileIndex,int _uploaded,int _totalSize){
		m_connect.m_mainApp.SetUploadingDesc(m_sendMail,_fileIndex,
											_uploaded,_totalSize);
	}
	
	public void sendPause(){
		RefreshMessageStatus(Message.Status.TX_PENDING);
	}
	
	public void sendError(){
		RefreshMessageStatus(Message.Status.TX_ERROR);
		m_connect.m_mainApp.TriggerEmailFailedNotifaction();
	}
	
	public void sendSucc(){
		RefreshMessageStatus(Message.Status.TX_DELIVERED);
	}
	
	public void sendFinish(){
		
		try{
			// send mail once if has not attachment 
			//
			ByteArrayOutputStream t_os = new ByteArrayOutputStream();
			t_os.write(msg_head.msgMail);
			m_sendMail.OutputMail(t_os);
			
			// send the Mail of forward or reply
			//
			if(m_forwardReply != null && m_sendStyle != fetchMail.NOTHING_STYLE){
				t_os.write(m_sendStyle);
				m_forwardReply.OutputMail(t_os);
			}else{
				t_os.write(fetchMail.NOTHING_STYLE);
			}
			
			// does want to copy tu sent folder?
			//
			sendReceive.WriteBoolean(t_os,m_connect.m_mainApp.m_copyMailToSentFolder);
			
			m_connect.m_connect.SendBufferToSvr(t_os.toByteArray(), false,false);
					
			t_os.close();
						
		}catch(Exception e){
			m_connect.m_mainApp.SetErrorString("SSF:" + e.getMessage() + e.getClass().getName());
		}
	}
	
	private void RefreshMessageStatus(int t_style){
							
		
		try{
			
			// sleep little to wait system set the mail status error
			// and set it back
			//
			// CAN NOT BE LESS THAN 500 !!!
			//
			//sleep(200);
			
			m_connect.m_mainApp.UpdateMessageStatus(m_sendMail.GetAttachMessage(),t_style);
			
		}catch(Exception _e){
			m_connect.m_mainApp.SetErrorString("S: Status " + _e.getMessage() + " "+ _e.getClass().getName());
		}		

	}
		
	public void run(){		
		
		int t_resend_time = 0;
		
		while(true){
			
			if(m_closeState){
				break;
			}
						
			try{
													
				while(m_connect.m_conn == null || !m_connect.m_sendAuthMsg){
					
					if(!m_connect.IsConnectState()){
						sendError();
						return;
					}else{
						sendPause();
					}
					
					try{
						sleep(10000);
					}catch(Exception _e){
						break;
					}
				}
				
				sendStart();
				sendFinish();
				
				try{

					// waiting for the server to confirm 
					// except mail with attachment
					//
					if(t_resend_time++ < 3){
						sleep(2 * 60000);
					}else{
						sendError();
						m_connect.m_mainApp.SetErrorString("S:resend 3 time,give up.");
						break;
					}

				}catch(Exception _e){
					break;
				}				
				
			}catch(Exception _e){
				
				sendError();
				
				m_connect.m_mainApp.SetErrorString("S: " + _e.getMessage() + " " + _e.getClass().getName());
				m_connect.m_mainApp.SetUploadingDesc(m_sendMail,-1,0,0);				
			}		
		}
	}	
}
