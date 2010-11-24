package com.yuchting.yuchberry.server;

import java.awt.print.PrinterException;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger{

	private static Logger		sm_instance;
	
	private FileOutputStream	m_logFileStream;
	
	private PrintStream		m_printStack;
	
	public Logger(){
		File t_logFile = new File("log");
		
		if(!t_logFile.exists() || !t_logFile.isDirectory()){
			t_logFile.mkdir();
		}
		
		SimpleDateFormat format = new SimpleDateFormat("MM-dd-HH_mm_ss");
		
		try{
			m_logFileStream = new FileOutputStream("log" + "/" + format.format(new Date()) + ".log");
			m_printStack 	= new PrintStream(m_logFileStream);			
		}catch(Exception _e){
			Logger.LogOut("seriously error : cant create log file.");
		}
	}
	
	public static void LogOut(String _log){
		if(sm_instance == null){
			sm_instance = new Logger();
		}
		
		
		SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm:ss");
		
		String t_out = format.format(new Date()) + ": " + _log + "\n";
		System.out.print(t_out);
		
		try{
			if(sm_instance.m_logFileStream != null){
				sm_instance.m_logFileStream.write(t_out.getBytes());
				sm_instance.m_logFileStream.flush();
			}			
		}catch(Exception _e){
			System.out.println("seriously error : cant write log file.");
		}
	}
	
	public static PrintStream GetPrintStream(){
		
		if(sm_instance == null){
			sm_instance = new Logger();
		}
		return sm_instance.m_printStack;
	}
	
	public static void PrinterException(Exception _e){
		
		if(sm_instance == null){
			sm_instance = new Logger();
		}
		
		SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm:ss :");
		
		String t_out = format.format(new Date());
		System.out.print(t_out);
		try{
			sm_instance.m_printStack.write(t_out.getBytes());
			sm_instance.m_logFileStream.flush();
		}catch(Exception e){}
		
		_e.printStackTrace(sm_instance.m_printStack);
		_e.printStackTrace();
	}
}
