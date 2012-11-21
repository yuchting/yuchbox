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
package com.yuchting.yuchberry.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger{
	
	private FileOutputStream	m_logFileStream	= null;
	private PrintStream		m_printStack	= null;
	private String				m_prefix		= null;
	
	private String				m_logFilename = "";
	
	private boolean			m_systemOut = false;
	
	private boolean			m_disable = false;
		
	public Logger(){
		// empty logger
	}
	
	public Logger(String _prefix){
		m_prefix = _prefix;
		
		RestartLogging();
	}
	
	public void disableLog(boolean _disable){
		m_disable = _disable;
	}
	
	public void EnabelSystemOut(boolean _enabled){
		m_systemOut = _enabled;
	}
	
	public void SetPrefix(String _prefix){
		m_prefix = _prefix;
	}
	
	public String GetLogFileName(){
		return m_logFilename;
	}
	
	public PrintStream GetPrintStream(){
		return m_printStack;
	}
	
	private final static SimpleDateFormat fsm_timeFormat = new SimpleDateFormat("MM-dd HH:mm:ss : ");
	private final static Date fsm_date = new Date();
	
	public synchronized void LogOut(String _log){
		
		if(m_disable){
			return ;
		}
				
		if(m_logFileStream != null){
			
			fsm_date.setTime(System.currentTimeMillis());
			String t_finalLog = fsm_timeFormat.format(fsm_date) + _log + "\n";
			
			if(m_systemOut){
				System.out.print(t_finalLog);
			}	
			
			try{
				
				m_logFileStream.write(t_finalLog.getBytes("UTF-8"));
				m_logFileStream.flush();
							
			}catch(Exception _e){
				System.out.println("seriously error : cant write log file.");
			}
		}
	}
	
	public synchronized void PrinterException(Exception _e){
		
		if(m_disable ){
			return ;
		}
		
		fsm_date.setTime(System.currentTimeMillis());
		String timePrefix = fsm_timeFormat.format(fsm_date);
		
		if(m_systemOut){
			System.out.print(timePrefix);
		}		
				
		if(m_printStack != null){
			
			try{
				m_printStack.write(timePrefix.getBytes());
				m_logFileStream.flush();
			}catch(Exception e){}
			
			_e.printStackTrace(m_printStack);
		}
		
		if(m_systemOut){
			_e.printStackTrace();
		}
	}
	
	public void StopLogging(){
		
		try{
			if(m_logFileStream != null){
				m_logFileStream.close();
			}
			if(m_printStack != null){
				m_printStack.close();
			}
		}catch(Exception e){
			
		}
		
		m_logFileStream = null;
		m_printStack = null;
	}
	
	public void RestartLogging(){
		
		if(m_prefix != null){
			File t_logFile = new File(m_prefix);
			
			if(!t_logFile.exists() || !t_logFile.isDirectory()){
				t_logFile.mkdir();
			}
			
			t_logFile = new File(m_prefix + "log");
			
			if(!t_logFile.exists() || !t_logFile.isDirectory()){
				t_logFile.mkdir();
			}
					
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH_mm_ss");
			
			try{
				m_logFilename = m_prefix + "log/" + format.format(new Date()) + ".log";
				m_logFileStream = new FileOutputStream(m_logFilename);
				m_printStack 	= new PrintStream(m_logFileStream);			
			}catch(Exception _e){
				System.out.println(m_prefix + " seriously error : cant create log file.");
			}
		}		
	}
}
