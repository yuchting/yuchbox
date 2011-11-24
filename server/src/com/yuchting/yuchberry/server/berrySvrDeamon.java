package com.yuchting.yuchberry.server;

import java.net.Socket;


class berrySvrPush extends Thread{
	
	berrySvrDeamon		m_serverDeamon;
	sendReceive			m_sendReceive;
	
	boolean			m_closed = false;
	
	public berrySvrPush(berrySvrDeamon _svrDeamon)throws Exception{
		m_serverDeamon = _svrDeamon;
		m_sendReceive = new sendReceive(m_serverDeamon.m_socket);
		
		start();
	}
	
	public void run(){
				
		while(!m_closed){
						
			try{
				
				m_serverDeamon.m_fetchMgr.SetCheckFolderState(true);
				{
					m_serverDeamon.m_fetchMgr.CheckAccountFolders();
				}				
				m_serverDeamon.m_fetchMgr.SetCheckFolderState(false);
				
				if(m_closed){
					break;
				}
				
				m_serverDeamon.m_fetchMgr.Push(m_sendReceive);				
				
				sleep(m_serverDeamon.m_fetchMgr.GetPushInterval() * 1000);
				
				if(!m_serverDeamon.m_fetchMgr.isClientConnected()){
					m_serverDeamon.m_fetchMgr.m_logger.LogOut("client closed! break berrySvrPush");
					break;
				}
							
			}catch(Exception _e){
				m_serverDeamon.m_fetchMgr.m_logger.PrinterException(_e);
			}
			
		}
		
		m_serverDeamon.m_fetchMgr.SetCheckFolderState(false);
		m_sendReceive.CloseSendReceive();
	}
	
}


public class berrySvrDeamon extends Thread{
	
	public fetchMgr		m_fetchMgr		= null;
	public Socket		m_socket		= null;
		
	sendReceive  		m_sendReceive	= null;
		
	private berrySvrPush m_pushDeamon	= null;
	
	boolean			m_quit			= true;
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
				
				m_quit = false;
				
				byte[] t_package = m_sendReceive.RecvBufferFromSvr();
				m_fetchMgr.m_logger.LogOut("receive package head<" + t_package[0] + "> length<" + t_package.length + ">");
				
				m_fetchMgr.ProcessPackage(t_package);
								
			}catch(Exception _e){
				
				//m_fetchMgr.m_logger.LogOut("berrySvrDeamon$run 0");
				
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
				
				//m_fetchMgr.m_logger.LogOut("berrySvrDeamon$run 1");
				
				try{
					
					m_sendReceive.CloseSendReceive();
					
					//m_fetchMgr.m_logger.LogOut("berrySvrDeamon$run 2");
					
					m_pushDeamon.m_closed = true;
					m_pushDeamon.interrupt();
								
					//m_fetchMgr.m_logger.LogOut("berrySvrDeamon$run 3");
					
					m_fetchMgr.m_logger.PrinterException(_e);
					
					//m_fetchMgr.m_logger.LogOut("berrySvrDeamon$run 4");
					
				}finally{
										
					m_quit = true;
					
					if(!m_isCloseByMgr){
						m_fetchMgr.SetClientConnected(null);
					}	
				}							
					
				//m_fetchMgr.m_logger.LogOut("berrySvrDeamon$run 5");
				
				break;
			}
		}

	}	
}
