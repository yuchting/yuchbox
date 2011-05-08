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
	
	public Logger(){
		// empty logger
	}
	
	public Logger(String _prefix){
		m_prefix = _prefix;
		
		RestartLogging();
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
	
	public synchronized void LogOut(String _log){
		
		if(m_logFileStream != null){
			SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm:ss");
			
			String t_out = format.format(new Date()) + ": " + _log + "\n";
			
			if(m_systemOut){
				System.out.print(t_out);
			}			
			
			try{
				
				m_logFileStream.write(t_out.getBytes("UTF-8"));
				m_logFileStream.flush();
							
			}catch(Exception _e){
				System.out.println("seriously error : cant write log file.");
			}
		}
	}
	
	public synchronized void PrinterException(Exception _e){
		
		if(m_printStack != null){
			SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm:ss :");
			
			String t_out = format.format(new Date());
			
			if(m_systemOut){
				System.out.print(t_out);
			}
			
			try{
				m_printStack.write(t_out.getBytes());
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
					
			SimpleDateFormat format = new SimpleDateFormat("MM-dd-HH_mm_ss");
			
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
