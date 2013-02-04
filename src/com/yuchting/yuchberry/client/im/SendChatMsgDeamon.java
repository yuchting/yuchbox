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
package com.yuchting.yuchberry.client.im;

import java.io.ByteArrayOutputStream;
import java.util.Vector;

import com.yuchting.yuchberry.client.ISendAttachmentCallback;
import com.yuchting.yuchberry.client.SendAttachmentDeamon;
import com.yuchting.yuchberry.client.connectDeamon;
import com.yuchting.yuchberry.client.msg_head;
import com.yuchting.yuchberry.client.sendReceive;

public class SendChatMsgDeamon extends Thread implements ISendAttachmentCallback{

	fetchChatMsg		m_sendMsg	= null;
	MainIMScreen.RosterChatData		m_sendTo	= null;

	MainChatScreen		m_chatScreen = null;
	
	connectDeamon		m_connect	= null;
	
	public	boolean	m_closeState = false;
	public SendAttachmentDeamon m_sendFileDaemon = null;
	
	public SendChatMsgDeamon(fetchChatMsg _msg,MainIMScreen.RosterChatData _sendTo,MainChatScreen _chatScreen){
		m_sendMsg		= _msg;
		m_chatScreen	= _chatScreen;
		m_connect		= _chatScreen.m_mainApp.m_connectDeamon;
		m_sendTo		= _sendTo;
		

		start();
		
		
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
		
		setChatState(fetchChatMsg.SEND_STATE_SENT);
	}
	
	private void setChatState(int _state){
		m_sendMsg.setSendState(_state);
		
		if(m_chatScreen.m_middleMgr.findChatField(m_sendMsg) != null){
			m_chatScreen.m_middleMgr.m_chatMsgMgr.invalidate();
		}
	
		m_chatScreen.m_mainScreen.m_historyChatMgr.invalidate();
	}
	
	public void sendStart(){
		setChatState(fetchChatMsg.SEND_STATE_SENDING);
	}
	
	public void sendProgress(int _fileIndex,int _uploaded,int _totalSize){
		//m_connect.m_mainApp.SetUploadingDesc(m_sendMail,_fileIndex,
		//									_uploaded,_totalSize);
	}
	
	public void sendPause(){
		setChatState(fetchChatMsg.SEND_STATE_PADDING);
	}
	
	public void sendError(){
		setChatState(fetchChatMsg.SEND_STATE_ERROR);
		
		Vector t_deamonList = m_chatScreen.m_mainScreen.m_sendChatDeamon;
		
		synchronized (t_deamonList) {
			for(int i = 0 ;i < t_deamonList.size();i++){
				SendChatMsgDeamon t_deamon = (SendChatMsgDeamon)t_deamonList.elementAt(i);
				if(t_deamon == this){
					t_deamonList.removeElementAt(i);
					break;
				}
			}	
		}		
	}
	
	public void sendFinish(){
		
		try{
			// send mail once if has not attachment 
			//
			ByteArrayOutputStream t_os = new ByteArrayOutputStream();
			t_os.write(msg_head.msgChat);
			t_os.write(m_sendMsg.getStyle());
			sendReceive.WriteLong(t_os,m_sendMsg.getSendTime());
			sendReceive.WriteString(t_os,m_sendMsg.getOwner());
			sendReceive.WriteString(t_os,m_sendTo.m_roster.getAccount());
			sendReceive.WriteString(t_os,m_sendMsg.getMsg());
			sendReceive.WriteInt(t_os,m_sendMsg.hashCode());
			
			// file length
			//
			if(m_sendMsg.getFileContent() != null){
				sendReceive.WriteInt(t_os,m_sendMsg.getFileContent().length);
				sendReceive.WriteBoolean(t_os, m_sendTo.m_isYuch);
				t_os.write(m_sendMsg.getFileContentType());
				t_os.write(m_sendMsg.getFileContent());
			}else{
				sendReceive.WriteInt(t_os,0);
			}			
			
			m_connect.m_connect.SendBufferToSvr(t_os.toByteArray(), false,false);
					
			t_os.close();
						
		}catch(Exception e){
			m_connect.m_mainApp.SetErrorString("SSFC:" + e.getMessage() + e.getClass().getName());
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
						
						if(m_sendMsg.getFileContent() != null){
							sleep(3*60000);
						}else{
							sleep(30000);
						}
						
					}else{
						sendError();
						m_connect.m_mainApp.SetErrorString("SC:resend 3 time,give up.");
						break;
					}

				}catch(Exception _e){
					break;
				}				
				
			}catch(Exception _e){
				
				sendError();
				
				m_connect.m_mainApp.SetErrorString("SC: " + _e.getMessage() + " " + _e.getClass().getName());			
			}		
		}
	}
}
