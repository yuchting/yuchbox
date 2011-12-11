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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Random;

class CheckVersion extends Thread{
	
	public fetchMgr		m_fetchMain = null;
	
	public void run(){
		
		boolean firstTime = true;
		while(true){
			
			try{
				URL is_gd = new URL("http://yuchberry.googlecode.com/files/latest_version?a="+(new Random()).nextInt());
				
		        URLConnection yc = is_gd.openConnection();
		        yc.setConnectTimeout(10000);
		        yc.setReadTimeout(50000);
		        BufferedReader in = new BufferedReader(
		                                new InputStreamReader(yc.getInputStream()));
		        		        
				m_fetchMain.SetLatestVersion(in.readLine());
				
				in.close();
												
			}catch(Exception e){
				
			}
			
			if(!firstTime){
				m_fetchMain.sendStatictiscInfo();
			}
			
			firstTime = false;
			
			try{ 
				sleep(12 * 3600 * 1000);
			}catch(Exception e){}
		}
	}
}

public class fetchMain{
	
	public fetchMain(){
		fetchMgr t_manger = new fetchMgr();
		Logger t_logger = new Logger("");
		
		t_logger.EnabelSystemOut(true);
		
		CheckVersion t_check = new CheckVersion();
		t_check.m_fetchMain = t_manger;
		t_check.start();
		
		new fakeMDSSvr();
		
		while(true){

			try{
				t_manger.InitConnect("",t_logger);
				
				t_manger.StartListening(true);
				
			}catch(Exception e){
				t_logger.PrinterException(e);
			}			
			
		    try{
		    	Thread.sleep(10000);
		    }catch(InterruptedException e){
		    	System.exit(0);
		    }
			
			try{
				t_manger.EndListening();
			}catch(Exception _e){
				System.exit(0);
			}
		}
	}
		
	public static void main(String[] _arg){
		new fetchMain();
	}
}