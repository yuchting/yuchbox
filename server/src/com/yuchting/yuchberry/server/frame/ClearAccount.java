package com.yuchting.yuchberry.server.frame;

import java.io.File;
import java.util.Date;

import com.yuchting.yuchberry.server.Logger;

public class ClearAccount {
	
	Logger m_logger = new Logger("");	
	
	long m_dataTime = (new Date()).getTime();
	
	public ClearAccount(){
		
		File t_file = new File(".");
		File[] t_files = t_file.listFiles();
	
		m_logger.EnabelSystemOut(true);
			
		for(File acclist : t_files){
			if(acclist.isDirectory()){
				
				String name = acclist.getName();
				if(name.endsWith("_tmpCreate")){
					
					m_logger.LogOut("Delete tmpCreate dir: " + name);
					mainFrame.DelDirectory(name);					
					
				}else if(name.indexOf("@") != -1){
					ProcessAccountFile(acclist);					
				}
			}
		}
	}
	
	private void ProcessAccountFile(File _accFile){
		
		m_logger.LogOut("Processing Account: " + _accFile.getName());
		
		File[] pushFiles = (new File(_accFile.getAbsolutePath())).listFiles();
		
		for(File push:pushFiles){
			
			if(push.isDirectory()){
				
				ProcessAccountPushFile(push);
			}
		}
	}
	
	private void ProcessAccountPushFile(File _pushFile){
		
		if(_pushFile.isDirectory()){
			
			String pushName = _pushFile.getName();
			
			if(pushName.equals("log") || pushName.equals("WeiboAccount")){
				// delete the log directory
				//
				m_logger.LogOut("Delete log/WeiboAccount dir");
				mainFrame.DelDirectory(_pushFile.getAbsolutePath());
				
			}else if(pushName.indexOf("@") != -1){
				// process the email account
				// delete the former attachment\
				//
				File[] pushSubFiles = (new File(_pushFile.getAbsolutePath())).listFiles();
				
				for(File sub: pushSubFiles){
					String attName = sub.getAbsolutePath();
					
					if(attName.endsWith(".att") 
					|| attName.endsWith(".satt")){
						
						if(Math.abs(m_dataTime - sub.lastModified()) > 5 * 24 * 3600000){
							m_logger.LogOut("Delete Att: " + attName);
							sub.delete();
						}
					}
				}
			}
		}
	}
	
	public static void main(String[] _arg){
		new ClearAccount();
	}
}
