package com.yuchting.yuchberry.server.frame;

import java.util.Date;

import com.yuchting.yuchberry.server.Logger;
import com.yuchting.yuchberry.server.fetchMgr;

public class fetchThread extends Thread{
	
	fetchMgr 	m_fetchMgr = new fetchMgr();
	Logger 		m_logger = null;
	
	boolean	m_pauseState = false;
	boolean	m_close		= false;
		
	long		m_expiredTime	= 0;
	
	long		m_expiredTimer	= 0;
	long		m_formerTimer	= 0;
		
	public fetchThread(String _prefix,String _configFile,long _expiredTime)throws Exception{
		m_expiredTime = _expiredTime * 1000 * 3600;
		
		m_logger = new Logger(_prefix);
		m_fetchMgr.InitConnect(_prefix, _configFile, m_logger);
		
		m_fetchMgr.ResetSession();
		
		start();
	}
	
	public void run(){
		
		m_formerTimer = (new Date()).getTime();
		
		while(!m_close){
			
			try{
				while(m_pauseState){
					sleep(2000);
				}
				
				m_fetchMgr.StartListening();
				
			}catch(Exception e){
				m_logger.PrinterException(e);
			}
			
			try{
				sleep(1000);
			}catch(Exception ex){}
		}
	}
	
	
	public long GetLastTime(){
		if(m_expiredTime > 0){
			return m_expiredTime - ((new Date()).getTime() - m_formerTimer);
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
	
	public void Reuse(){
		
		if(m_close == true){
			return;
		}
		
		if(m_pauseState = true){
			m_pauseState = false;
			
			try{
				sleep(100);
			}catch(Exception e){}
			
			interrupt();
		}
	}
	
	public synchronized void Destroy(){
		
		m_fetchMgr.EndListening();
		
		try{
			sleep(100);
		}catch(Exception e){}
		
		m_close = true;
		interrupt();
	}
}
