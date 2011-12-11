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

import java.io.File;

import com.yuchting.yuchberry.server.Logger;

public class ClearAccount {
	
	Logger m_logger = new Logger("");	
	
	long m_dataTime = System.currentTimeMillis();
	
	public ClearAccount(){
		
		File t_file = new File(".");
		File[] t_files = t_file.listFiles();
	
		m_logger.EnabelSystemOut(true);
			
		for(File acclist : t_files){
			if(acclist.isDirectory()){
				
				String name = acclist.getName();
				if(name.endsWith("_tmpCreate")){
					
					m_logger.LogOut("Delete tmpCreate dir: " + name);
					YuchServer.DelDirectory(name);					
					
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
			}else{
				if(push.isFile() && push.getName().endsWith(".satt")){
					// delete the send attachment
					//
					push.delete();
				}
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
				YuchServer.DelDirectory(_pushFile.getAbsolutePath());
				
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
