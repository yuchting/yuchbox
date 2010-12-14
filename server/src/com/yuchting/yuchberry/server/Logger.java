package com.yuchting.yuchberry.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger{
	
	private FileOutputStream	m_logFileStream;
	private PrintStream		m_printStack;
	private String				m_prefix;
	
	private String				m_logFilename = new String();
	
	public Logger(String _prefix){
		m_prefix = _prefix;
		
		if(!_prefix.isEmpty()){
			File t_logFile = new File(m_prefix);
			
			if(!t_logFile.exists() || !t_logFile.isDirectory()){
				t_logFile.mkdir();
			}
		}
				
		File t_logFile = new File(m_prefix + "log");
		
		if(!t_logFile.exists() || !t_logFile.isDirectory()){
			t_logFile.mkdir();
		}
				
		SimpleDateFormat format = new SimpleDateFormat("MM-dd-HH_mm_ss");
		
		try{
			m_logFilename = m_prefix + "log/" + format.format(new Date()) + ".log";
			m_logFileStream = new FileOutputStream(m_logFilename);
			m_printStack 	= new PrintStream(m_logFileStream);			
		}catch(Exception _e){
			System.out.println(_prefix + " seriously error : cant create log file.");
		}
	}
	
	public String GetLogFileName(){
		return m_logFilename;
	}
	
	public synchronized void LogOut(String _log){
		
		SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm:ss");
		
		String t_out = format.format(new Date()) + ": " + _log + "\n";
		System.out.print(t_out);
		
		try{
			if(m_logFileStream != null){
				m_logFileStream.write(t_out.getBytes());
				m_logFileStream.flush();
			}			
		}catch(Exception _e){
			System.out.println("seriously error : cant write log file.");
		}
	}
	
	public void ReleaseFile(){
		if(m_logFileStream != null){
			try{
				m_logFileStream.close();
			}catch(Exception _e){
				System.out.println(m_prefix + " close file error!");
			}
			
		}
	}
	
	public PrintStream GetPrintStream(){
		return m_printStack;
	}
	
	public synchronized void PrinterException(Exception _e){
		
		if(m_printStack != null){
			SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm:ss :");
			
			String t_out = format.format(new Date());
			System.out.print(t_out);
			try{
				m_printStack.write(t_out.getBytes());
				m_logFileStream.flush();
			}catch(Exception e){}
			
			_e.printStackTrace(m_printStack);
		}
		
		_e.printStackTrace();
	}
	
	public void StopLogging(){
		
		try{
			if(m_logFileStream != null){
				m_logFileStream.close();
			}
			if(m_printStack != null){
				m_printStack.close();
			}
			
			m_logFileStream = null;
			m_printStack = null;
				
		}catch(Exception e){
			
		}
		
	}
}
