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
package com.yuchting.yuchberry.client.weibo;

import java.io.ByteArrayOutputStream;

import com.yuchting.yuchberry.client.GPSInfo;
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
	boolean	m_onlyComment	= false;
	boolean	m_onlyForward	= false;
	
	fetchWeibo		m_origWeibo		= null;
	fetchWeibo		m_commentWeibo	= null;
	
	String		m_directMsgName = null; 
	
	int			m_hashCode		= (int)((System.currentTimeMillis()) / 1000);
	
	public  boolean	m_closeState	= false;
	
	GPSInfo		m_gpsInfo		= null;
	
	SendAttachmentDeamon m_sendFileDaemon = null;
	
	// update new weibo
	//
	public WeiboSendDaemon(String _text,byte[] _file,int _fileType,recvMain _mainApp,GPSInfo _gps)throws Exception{
		
		if(_mainApp == null){
			throw new IllegalArgumentException("WeiboSendDeamon _mainApp == null");
		}
		
		m_mainApp		= _mainApp;
		m_gpsInfo		= _gps;
		m_updateText	= _text;
		m_fileBuffer	= _file;
		m_fileType		= _fileType;
		
		// disable
		//
		m_mainApp.m_weiboUseLocation = false;
		
		if(m_fileBuffer != null){
			m_sendFileDaemon = new SendAttachmentDeamon(m_mainApp.m_connectDeamon,m_fileBuffer,m_hashCode,this);
		}else{
			start();
		}
	}
	
	// reply/comment weibo
	//
	public WeiboSendDaemon(String _text,byte _weiboStyle,byte _sendType,fetchWeibo _origWeibo,fetchWeibo _commentWeibo,boolean _onlyComment,boolean _onlyForward,
							recvMain _mainApp)throws Exception{
		
		if(_mainApp == null){
			throw new IllegalArgumentException("WeiboSendDeamon _mainApp == null");
		}
		
		m_mainApp		= _mainApp;
		m_onlyComment	= _onlyComment;
		m_onlyForward	= _onlyForward;
		m_updateText	= _text;
		m_weiboStyle	= _weiboStyle;
		m_sendType		= _sendType;
		
		m_origWeibo		= _origWeibo;
		m_commentWeibo	= _commentWeibo;
			
		start();		
	}
	
	// send the direct message
	//
	public WeiboSendDaemon(String _text,byte _weiboStyle,String _directMsgName,recvMain _mainApp)throws Exception{
		m_mainApp		= _mainApp;
				
		m_updateText	= _text;
		m_weiboStyle	= _weiboStyle;
		m_directMsgName = _directMsgName;
		m_sendType		= fetchWeibo.SEND_DIRECT_MSG_TYPE;
				
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
			
			while(m_mainApp.m_connectDeamon.m_conn == null 
				|| !m_mainApp.m_connectDeamon.m_sendAuthMsg){
				
				if(!m_mainApp.m_connectDeamon.IsConnectState()){
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
				sleep(3 * 60000);
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
			
			if(m_gpsInfo != null /*m_mainApp.canUseLocation() && m_mainApp.m_weiboUseLocation*/){
				t_os.write(1);
				m_mainApp.getGPSInfo().OutputData(t_os);
			}else{
				t_os.write(0);
			}
			
			sendReceive.WriteInt(t_os, m_hashCode);
			t_os.write(m_fileType);
			
			StringBuffer t_updateAccount = new StringBuffer();
			if(m_mainApp.m_weiboAccountList.size() >= 2){
				for(int i = 0;i < m_mainApp.m_weiboAccountList.size();i++){
					WeiboAccount acc = (WeiboAccount)m_mainApp.m_weiboAccountList.elementAt(i);
					if(acc.needUpdate){
						t_updateAccount.append(acc.id).append(" ");
					}
				}
			}
			
			sendReceive.WriteString(t_os,t_updateAccount.toString());
								
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
			sendReceive.WriteBoolean(t_os,m_onlyComment?false:m_mainApp.m_publicForward);
			
			sendReceive.WriteLong(t_os,m_origWeibo != null?m_origWeibo.GetId():0);
			sendReceive.WriteLong(t_os,m_commentWeibo != null?m_commentWeibo.GetId():0);
			
			if(m_gpsInfo != null /*m_mainApp.canUseLocation()*/){
				t_os.write(1);
				m_mainApp.getGPSInfo().OutputData(t_os);
			}else{
				t_os.write(0);
			}
			
			if(m_sendType == fetchWeibo.SEND_FORWARD_TYPE){
				if(m_onlyComment){
					t_os.write(0);
				}else if(m_onlyForward){
					t_os.write(2);
					
					// prepare the append text for forward to other style weibo
					//
					String t_append = "";
					if(m_commentWeibo != null){
						t_append += m_commentWeibo.getForwardPrefix() + "@" + m_commentWeibo.GetUserScreenName() + " :" + m_commentWeibo.GetText();
					}
					
					if(m_origWeibo != null){
						t_append += m_origWeibo.getForwardPrefix() + "@" + m_origWeibo.GetUserScreenName() + " :" + m_origWeibo.GetText();
					}
					
					sendReceive.WriteString(t_os, t_append);
				}else{
					t_os.write(m_mainApp.m_updateOwnListWhenFw?1:0);
				}
				
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
