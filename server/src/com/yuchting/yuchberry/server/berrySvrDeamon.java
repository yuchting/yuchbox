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
				
				if(m_closed){
					break;
				}
				
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
	
	public fetchMgr		m_fetchMgr		= null;
	public Socket		m_socket		= null;
		
	sendReceive  		m_sendReceive	= null;
		
	private berrySvrPush m_pushDeamon	= null;
	
	boolean			m_quit			= false;
	boolean			m_isCloseByMgr	= false;
	
	public berrySvrDeamon(fetchMgr _mgr,Socket _s,sendReceive _sendReceive)throws Exception{
		m_fetchMgr 	= _mgr;
		
		m_sendReceive = _sendReceive; 
		
		// prepare receive and push deamon
		//
		m_socket	= _s;
		
		try{
			
			m_pushDeamon = new berrySvrPush(this);
			
		}catch(Exception _e){
			
			m_sendReceive.CloseSendReceive();						
			throw _e;
		}		
		
		m_fetchMgr.SetClientConnected(this);
		
		m_fetchMgr.m_logger.LogOut("some client connect IP<" + m_socket.getInetAddress().getHostAddress() + ">");
				
		start();
				
	}
	
	
		
	public void run(){
		
		// loop
		//
		while(true){
			
			// process....
			//
			try{
												
				byte[] t_package = m_sendReceive.RecvBufferFromSvr();
				m_fetchMgr.m_logger.LogOut("receive package head<" + t_package[0] + "> length<" + t_package.length + ">");
				
				m_fetchMgr.ProcessPackage(t_package);
				
			}catch(Exception _e){
				
				try{
					synchronized (this) {
						if(m_socket != null && !m_socket.isClosed()){
							m_socket.close();
						}
						
						m_socket = null;
					}
				}catch(Exception e){
					m_fetchMgr.m_logger.PrinterException(_e);
				}			
				
				m_sendReceive.CloseSendReceive();
				
				m_pushDeamon.m_closed = true;
				m_pushDeamon.interrupt();				
				
				m_fetchMgr.m_logger.PrinterException(_e);
							
				m_quit = true;
				
				if(!m_isCloseByMgr){
					m_fetchMgr.SetClientConnected(null);
				}				
								
				break;
			}
		}

	}	
}
