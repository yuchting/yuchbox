package com.yuchting.yuchberry.client.weibo;

import java.io.ByteArrayOutputStream;
import java.util.Date;

import net.rim.device.api.system.EncodedImage;

import com.yuchting.yuchberry.client.ISendAttachmentCallback;
import com.yuchting.yuchberry.client.SendAttachmentDeamon;
import com.yuchting.yuchberry.client.msg_head;
import com.yuchting.yuchberry.client.recvMain;
import com.yuchting.yuchberry.client.sendReceive;

public class WeiboSendDaemon extends Thread implements ISendAttachmentCallback{

	
	recvMain	m_mainApp		= null;
	String		m_updateText	= null;
	byte[]		m_fileBuffer	= null;
	int			m_fileType		= 0;
	
	byte		m_weiboStyle	= 0;
	byte		m_sendType		= 0; 
	
	long		m_origId		= 0;
	long		m_commentId		= 0;
	
	String		m_directMsgName = null; 
	
	int			m_hashCode		= (int)(((new Date()).getTime()) / 1000);
	
	public  boolean	m_closeState	= false;
	
	SendAttachmentDeamon m_sendFileDaemon = null;
	
	// update new weibo
	//
	public WeiboSendDaemon(String _text,byte[] _file,int _fileType,recvMain _mainApp)throws Exception{
		
		m_mainApp		= _mainApp;
		
		m_updateText	= _text;
		m_fileBuffer	= _file;
		m_fileType		= _fileType;
		
		if(m_fileBuffer != null){
			m_sendFileDaemon = new SendAttachmentDeamon(m_mainApp.m_connectDeamon,m_fileBuffer,m_hashCode,this);
		}else{
			start();
		}
	}
	
	// reply/comment weibo
	//
	public WeiboSendDaemon(String _text,byte _weiboStyle,byte _sendType,long _origId,long _commentId,
							recvMain _mainApp)throws Exception{
		
		m_mainApp		= _mainApp;
		
		m_updateText	= _text;
		m_weiboStyle	= _weiboStyle;
		m_sendType		= _sendType;
		
		m_origId		= _origId;
		m_commentId		= _commentId;
			
		start();		
	}
	
	// send the direct message
	//
	public WeiboSendDaemon(String _text,byte _weiboStyle,String _directMsgName,recvMain _mainApp)throws Exception{
		m_mainApp		= _mainApp;
				
		m_updateText	= _text;
		m_weiboStyle	= _weiboStyle;
		m_directMsgName = _directMsgName;
				
		start();		
	}
	
	public void sendStart(){}
	public void sendProgress(int _fileIndex,int _uploaded,int _totalSize){}
	public void sendPause(){}
	public void sendError(){
		inter();
		m_mainApp.DialogAlert("send weibo error");
	}
	
	public void sendFinish(){
		updateNew();
	}
	
	public void inter(){
		
		m_closeState = true;
		if(isAlive()){
			interrupt();
		}
		
		if(m_sendFileDaemon != null){
			m_sendFileDaemon.m_closeState = true;
			if(m_sendFileDaemon.isAlive()){
				m_sendFileDaemon.interrupt();
			}			
		}
	}
	
	public void run(){
		
		int t_time = 0;
		while(!m_closeState){
			
			if(t_time++ > 3){
				if(m_mainApp.m_weiboTimeLineScreen != null){
					m_mainApp.m_weiboTimeLineScreen.m_sendDaemonList.removeElement(this);
				}
				
				m_mainApp.SetErrorString("weibo resend 3 time giveup");
				break;
			}

			try{
				
				switch(m_sendType){
				case fetchWeibo.SEND_NEW_UPDATE_TYPE:
					updateNew();
					break;
				case fetchWeibo.SEND_FORWARD_TYPE:
				case fetchWeibo.SEND_REPLY_TYPE:
					updateReplyComment();
					break;
				case fetchWeibo.SEND_DIRECT_MSG_TYPE:
					sendDirectMsg();
					break;
				}
				
			}catch(Exception e){
				m_mainApp.SetErrorString("WSD:"+e.getMessage()+e.getClass().getName());
				break;
			}
			
			try{
				sleep(60000);
			}catch(Exception e){}
		}
	}
	
	private void updateNew(){
		
		try{
			// update a single weibo
			//
			ByteArrayOutputStream t_os = new ByteArrayOutputStream();
			t_os.write(msg_head.msgWeibo);
			
			t_os.write(fetchWeibo.SINA_WEIBO_STYLE);
			t_os.write(m_sendType);
			
			sendReceive.WriteString(t_os,m_updateText);
			
			if(m_mainApp.canUseLocation() && m_mainApp.m_weiboUseLocation){
				t_os.write(1);
				m_mainApp.getGPSInfo().OutputData(t_os);
			}else{
				t_os.write(0);
			}
			
			sendReceive.WriteInt(t_os, m_hashCode);
			t_os.write(m_fileType);
								
			m_mainApp.m_connectDeamon.addSendingData(msg_head.msgWeibo,t_os.toByteArray(),true);
					
		}catch(Exception e){
			m_mainApp.SetErrorString("UN:" + e.getMessage() + e.getClass().getName());
		}
	}
	
	private void updateReplyComment(){
		
		try{
			
			ByteArrayOutputStream t_os = new ByteArrayOutputStream();
			
			t_os.write(msg_head.msgWeibo);
			t_os.write(m_weiboStyle);
			t_os.write(m_sendType);
			
			sendReceive.WriteString(t_os,m_updateText);
			sendReceive.WriteBoolean(t_os,m_mainApp.m_publicForward);
			sendReceive.WriteLong(t_os,m_origId);
			sendReceive.WriteLong(t_os,m_commentId);
			
			if(m_mainApp.canUseLocation()){
				t_os.write(1);
				m_mainApp.getGPSInfo().OutputData(t_os);
			}else{
				t_os.write(0);
			}
			
			if(m_sendType == fetchWeibo.SEND_FORWARD_TYPE){
				sendReceive.WriteBoolean(t_os,m_mainApp.m_updateOwnListWhenFw);
			}else{
				sendReceive.WriteBoolean(t_os,m_mainApp.m_updateOwnListWhenRe);
			}
			
			sendReceive.WriteInt(t_os, m_hashCode);
			
			m_mainApp.m_connectDeamon.addSendingData(msg_head.msgWeibo,t_os.toByteArray(),true);		
			
		}catch(Exception e){
			m_mainApp.SetErrorString("URC:" + e.getMessage() + e.getClass().getName());
		}
		
	}
	
	private void sendDirectMsg(){
		try{
			
			ByteArrayOutputStream t_os = new ByteArrayOutputStream();

			t_os.write(msg_head.msgWeibo);
			t_os.write(m_weiboStyle);
			t_os.write(fetchWeibo.SEND_DIRECT_MSG_TYPE);
			sendReceive.WriteString(t_os,m_updateText);
			sendReceive.WriteString(t_os,m_directMsgName);
			
			sendReceive.WriteInt(t_os, m_hashCode);

			m_mainApp.m_connectDeamon.addSendingData(msg_head.msgWeibo,t_os.toByteArray(),true);
			
		}catch(Exception e){
			m_mainApp.SetErrorString("SDM:" + e.getMessage() + e.getClass().getName());
		}
	}
}
