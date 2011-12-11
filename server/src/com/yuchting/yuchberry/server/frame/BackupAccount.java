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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Vector;

import com.yuchting.yuchberry.server.Logger;
import com.yuchting.yuchberry.server.fetchEmail;
import com.yuchting.yuchberry.server.fetchMgr;

public class BackupAccount {
	Logger m_logger = new Logger("");
	
	long m_dataTime = System.currentTimeMillis();
	
	Vector<String>	m_accountList = new Vector<String>();
	
	public BackupAccount(String _backupDir){

		m_logger.EnabelSystemOut(true);
		
		File t_accountfile = new File(YuchServer.fsm_accountDataFilename);
		if(!t_accountfile.exists()){
			m_logger.LogOut("can't find 'account.info' file!");
			return;
		}
		
		File t_backupDir = new File(_backupDir);
		if(!t_backupDir.exists() || t_backupDir.isDirectory()){
			t_backupDir.mkdir();
		}
		
		m_logger.LogOut("start backup to '"+_backupDir+"'");
		
		try{
			// copy the account file
			//
			createDialog.CopyFile(YuchServer.fsm_accountDataFilename, _backupDir + "/" + YuchServer.fsm_accountDataFilename);
			
			m_logger.LogOut("copy " + YuchServer.fsm_accountDataFilename + " OK!");
			
			BufferedReader in = new BufferedReader(
					new InputStreamReader(
						new FileInputStream(t_accountfile),"UTF-8"));
			try{
				String line = null;
				
				while((line = in.readLine())!= null){
					if(!fetchMgr.IsEmptyLine(line)){
						m_accountList.addElement(line.split(",")[0]);
					}
				}
			}finally{
				in.close();
			}
			
			m_logger.LogOut("read " + YuchServer.fsm_accountDataFilename + " OK!");
			
		}catch(Exception e){
			m_logger.PrinterException(e);
			return;
		}
		
		for(String acc : m_accountList){
			File t_file = new File(acc);
			if(t_file.exists()){	
				// backup account file to the backup directory
				//
				ProcessAccountFile(acc,_backupDir);	
			}
		}
	}
	
	private void ProcessAccountFile(String _acc,String _backupDir){
		
		String t_accSubDir	= _acc + "/";
		String t_backupSubDir = _backupDir + "/" + _acc + "/";
		
		File t_dir = new File(t_backupSubDir);
		t_dir.mkdir();
		
		m_logger.LogOut("   Processing Account: " + _acc);
				
		try{
			createDialog.CopyFile(t_accSubDir + fetchMgr.fsm_configFilename, t_backupSubDir + fetchMgr.fsm_configFilename);
		}catch(Exception e){
			m_logger.LogOut("   failed copy" + fetchMgr.fsm_configFilename + " :" +e.getMessage());
		}
		
		try{
			createDialog.CopyFile(t_accSubDir + fetchEmail.fsm_signatureFilename, t_backupSubDir + fetchEmail.fsm_signatureFilename);
		}catch(Exception e){
			m_logger.LogOut("   failed copy" + fetchEmail.fsm_signatureFilename + " :" +e.getMessage());
		}
		
		try{
			createDialog.CopyFile(t_accSubDir + fetchEmail.fsm_googleMapInfoFilename, t_backupSubDir + fetchEmail.fsm_googleMapInfoFilename);
		}catch(Exception e){
			m_logger.LogOut("   failed copy" + fetchEmail.fsm_googleMapInfoFilename + " :" +e.getMessage());
		}
	}

	public static void main(String[] _arg){
		new BackupAccount("TestBackupDir");
	}
	
}
