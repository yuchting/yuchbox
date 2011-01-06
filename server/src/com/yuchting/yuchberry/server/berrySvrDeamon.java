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


class berrySvrPush extends Thread{
	
	berrySvrDeamon		m_serverDeamon;
	sendReceive			m_sendReceive;
	
	boolean			m_closed = false;
	
	public berrySvrPush(berrySvrDeamon _svrDeamon)throws Exception{
		m_serverDeamon = _svrDeamon;
		m_sendReceive = new sendReceive(m_serverDeamon.m_socket.getOutputStream(),
										m_serverDeamon.m_socket.getInputStream());
		
		start();
	}
	
	public void run(){
				
		while(!m_closed){
						
			try{
			
				m_serverDeamon.m_fetchMgr.CheckAccountFolders();
				m_serverDeamon.m_fetchMgr.Push(m_sendReceive);
				
				sleep(m_serverDeamon.m_fetchMgr.GetPushInterval() * 1000);				
							
			}catch(Exception _e){
				m_serverDeamon.m_fetchMgr.m_logger.PrinterException(_e);
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
					
		if(!ValidateClient(_s)){
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
				sleep(50);
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
	
	public boolean ValidateClient(Socket _s){
		
		sendReceive t_tmp = null;
		try{
			
			t_tmp = new sendReceive(_s.getOutputStream(),_s.getInputStream());
			
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
			
			ByteArrayInputStream in = new ByteArrayInputStream(t_tmp.RecvBufferFromSvr());
									
			final int t_msg_head = in.read();
		
			if(msg_head.msgConfirm != t_msg_head 
			|| !sendReceive.ReadString(in).equals(m_fetchMgr.m_userPassword)
			|| (m_clientVer = sendReceive.ReadInt(in)) != 2){
				
				/* useless
				 * 
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				os.write(msg_head.msgNote);
				sendReceive.WriteString(os, msg_head.noteErrorUserPassword,m_fetchMgr.m_convertToSimpleChar);
				
				_s.getOutputStream().write(os.toByteArray());
				*/
				
				throw new Exception("illeagel client<"+ _s.getInetAddress().getHostAddress() +"> connected.");				
			}
						
			t_tmp.CloseSendReceive();
			
		}catch(Exception _e){
			// time out or other problem
			//
			try{
				_s.close();
			}catch(Exception e){}			
			
			m_fetchMgr.m_logger.PrinterException(_e);
			t_tmp.CloseSendReceive();
			
			return false;
		}
		
		return true;
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
				
				m_fetchMgr.ProcessPackage(t_package);
				
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
				
				m_pushDeamon.m_closed = true;
				m_pushDeamon.interrupt();				
				
				m_fetchMgr.m_logger.PrinterException(_e);
								
				break;
			}
		}

	}	
}
