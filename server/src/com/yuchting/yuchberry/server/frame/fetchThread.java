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
package com.yuchting.yuchberry.server.frame;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.yuchting.yuchberry.server.Logger;
import com.yuchting.yuchberry.server.fetchMgr;

public class fetchThread extends Thread{
	
	fetchMgr 	m_fetchMgr = null;
	Logger 		m_logger = null;
	
	boolean		m_pauseState = false;
	boolean		m_close		= false;
	
	boolean		m_sendTimeupMail = false;
		
	long		m_usingHours	= 0;
	long		m_formerTimer	= 0;
	
	long		m_clientDisconnectTime	= 0;
		
	public fetchThread(fetchMgr _mainMgr,String _prefix,long _expiredTime,
					long _formerTimer,boolean _testConnect)throws Exception{
		m_usingHours = _expiredTime;
		
		m_fetchMgr = _mainMgr;
		m_logger = new Logger(_prefix);
		m_fetchMgr.InitConnect(_prefix,m_logger);
		
		if(_testConnect){
			m_fetchMgr.ResetAllAccountSession(true);
		}
		
		m_formerTimer = _formerTimer;
		
		start();
	}
	
	static final SimpleDateFormat fsm_disconnectTimeFormat = new SimpleDateFormat("(上次链接时间 yyyy年MM月dd日 HH:mm)");
	static final Date				fsm_dateTime = new Date();
	
	public String GetStateString(){
		
		if(m_pauseState){
			return "暂停";
		}else if(m_close){
			return "关闭";
		}else if(m_fetchMgr.GetClientConnected() != null){
			return "客户端连接中";
		}else{
			
			String t_clientDate = "(未连接过)";
			
			if(m_clientDisconnectTime != 0){	
				fsm_dateTime.setTime(m_clientDisconnectTime);
				t_clientDate = fsm_disconnectTimeFormat.format(fsm_dateTime);
			}
			
			return "监听中" + t_clientDate;
		}
	}
	
	public int getConnectState(){
		
		if(m_pauseState){
			return -1;
		}else if(m_close){
			return -2;
		}else if(m_fetchMgr.GetClientConnected() != null){
			return -3;
		}else{		
			return (int)(m_clientDisconnectTime / 1000);
		}
	}
	
	public void run(){
		
		while(!m_close){
			
			try{
				try{
					while(m_pauseState){
						sleep(2000);
					}	
				}catch(Exception e){}							
				
				m_fetchMgr.StartListening(false);
				
			}catch(Exception e){
				m_logger.PrinterException(e);
			}
			
			if(m_close){
				break;
			}else{

				try{
					sleep(5 * 60000);
				}catch(Exception ex){}	
			}
		}
	}
	
	
	public long GetLastTime(long _currTime){
		if(m_usingHours > 0){
			return m_usingHours * 3600000 - (_currTime - m_formerTimer);
		}
		
		return 0;
	}
	
	public void Pause(){
		if(m_close == true){
			return;
		}
		
		if(m_pauseState == false){
			m_fetchMgr.EndListening();
			m_pauseState = true;
			
			try{
				sleep(100);
			}catch(Exception e){}
			
			interrupt();
		}
	}
	
	public void Reuse()throws Exception{
		
		if(m_close == true){
			return;
		}
		
		if(m_pauseState == true){
			
			m_sendTimeupMail = false;
			m_pauseState = false;
			
			try{
				
				m_fetchMgr.InitConnect(m_fetchMgr.GetPrefixString(),m_logger);
				
				sleep(100);				
				
			}catch(Exception e){
				m_logger.PrinterException(e);
				
				throw e;
				
			}finally{
								
				interrupt();
			}
			
						
		}
	}
	
	public synchronized void Destroy(){
		
		m_fetchMgr.EndListening();
		m_logger.StopLogging();
		
		try{
			sleep(100);
		}catch(Exception e){}
		
		m_close = true;
		interrupt();
	}
}
