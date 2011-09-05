package com.yuchting.yuchberry.client.im;

import java.io.ByteArrayOutputStream;

import com.yuchting.yuchberry.client.ISendAttachmentCallback;
import com.yuchting.yuchberry.client.SendAttachmentDeamon;
import com.yuchting.yuchberry.client.connectDeamon;
import com.yuchting.yuchberry.client.msg_head;
import com.yuchting.yuchberry.client.sendReceive;

public class SendChatMsgDeamon extends Thread implements ISendAttachmentCallback{

	fetchChatMsg		m_sendMsg	= null;
	RosterChatData		m_sendTo	= null;

	MainChatScreen		m_chatScreen = null;
	
	connectDeamon		m_connect	= null;
	
	public	boolean	m_closeState = false;
	public SendAttachmentDeamon m_sendFileDaemon = null;
	
	public SendChatMsgDeamon(fetchChatMsg _msg,RosterChatData _sendTo,MainChatScreen _chatScreen){
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
		setChatState(fetchChatMsg.SEND_STATE_PADDING);
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
			
			// file length
			//
			sendReceive.WriteInt(t_os,0);
			
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
						sleep(2 * 60000);
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
