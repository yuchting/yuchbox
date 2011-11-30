package com.yuchting.yuchberry.server.frame;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Vector;

import twitter4j.internal.org.json.JSONArray;
import twitter4j.internal.org.json.JSONObject;

import com.yuchting.yuchberry.server.Logger;
import com.yuchting.yuchberry.server.fetchMgr;

class checkStateThread extends Thread{
	YuchServer	m_mainServer	= null;
		
	public checkStateThread(YuchServer _main){
		m_mainServer = _main;
		
		start();
	}
	
	public void run(){
		
		int t_versionDetectCounter = -1;
		int t_backupCounter = 0;
		
		Vector<fetchMgr> t_mgrList = new Vector<fetchMgr>();
		
		final int t_sleepInterval = 240000;
		
		final int t_versionDetect = 12 * 3600000 / t_sleepInterval;
		
		final int t_backupInterval = 48 * 3600000 / t_sleepInterval;
		
		while(true){
			
			try{
				
				try{
					sleep(t_sleepInterval);
				}catch(Exception e){}
				
				m_mainServer.RefreshState();
				
				if(t_versionDetectCounter == -1 ||  t_versionDetectCounter > t_versionDetect ){
									
					URL is_gd = new URL("http://yuchberry.googlecode.com/files/latest_version?a="+(new Random()).nextInt());
					
			        URLConnection yc = is_gd.openConnection();
			        yc.setConnectTimeout(10000);
			        yc.setReadTimeout(50000);
			        BufferedReader in = new BufferedReader(
			                                new InputStreamReader(yc.getInputStream()));
			        
			        String t_version = in.readLine();
			        in.close();
			        
			        t_mgrList.clear();
			        
			        synchronized (m_mainServer.m_accountList) {
			        	for(fetchThread t_thread:m_mainServer.m_accountList){
				        	t_mgrList.add(t_thread.m_fetchMgr);    	
				        }
					}
			        
			        for(fetchMgr mgr:t_mgrList){
			        	mgr.SetLatestVersion(t_version);
			        	
			        	if(t_versionDetectCounter != -1){
			        		mgr.sendStatictiscInfo();
			        	}			        	
			        }   
			        t_versionDetectCounter = 0;
				}
								
				t_versionDetectCounter++;
				
				if(m_mainServer.m_needbackup && t_backupCounter > t_backupInterval){
					t_backupCounter = 0;
					Runtime.getRuntime().exec(m_mainServer.m_backupShellLine);
				}
				
				t_backupCounter++;
				
			}catch(Exception ex){}
		}
	}
}

public class YuchServer {
	
	final static String fsm_accountDataFilename = "account.info";
	
	NanoHTTPD	m_httpd					= null;
	Logger		m_logger				= new Logger("");
	
	mainFrame	m_mainFrame				= null;
	
	Vector<fetchThread>		m_accountList		= new Vector<fetchThread>();
	Vector<fetchThread>	m_checkFolderStateThread = new Vector<fetchThread>();
		
	int			m_pushInterval			= 30;
	long		m_expiredTime			= 0;
		
	int			m_currUsingAccount		= 0;
	int			m_currConnectAccount	= 0;
	
	static public void main(String _arg[])throws Exception{
		new YuchServer(null);
		
		System.out.println("OK");
	}
	
	public YuchServer(mainFrame _frame){
		m_mainFrame = _frame;		
		LoadStoreAccountInfo();
		
		new checkStateThread(this);
		LoadYuchsign();
	}
	
	public void LoadStoreAccountInfo(){
		
		Thread t_load = new Thread(){
			public void run(){
				try{
					
					File t_file = new File(fsm_accountDataFilename);
					if(t_file.exists()){
						String line = null;
						Vector<String> t_lineContain = new Vector<String>();

						BufferedReader in = new BufferedReader(
												new InputStreamReader(
													new FileInputStream(t_file),"UTF-8"));
						try{
							while((line = in.readLine())!= null){
								if(!fetchMgr.IsEmptyLine(line)){
									t_lineContain.addElement(line);
								}
							}								
						}finally{
							in.close();
						}
						
						if(m_mainFrame != null){
							m_mainFrame.m_loadDialog.m_progress.setMaximum(t_lineContain.size());
						}						
						
						for(int i = 0;i < t_lineContain.size();i++){
							line = (String)t_lineContain.elementAt(i);
							String t_data[] = line.split(",");
							String t_prefix = t_data[0] + "/";
							
							try{
								
								
								fetchThread t_thread = new fetchThread(new fetchMgr(),t_prefix,
													Long.valueOf(t_data[1]).longValue(),Long.valueOf(t_data[2]).longValue(),false);
								
								if(t_data.length >= 4){
									t_thread.m_clientDisconnectTime = Long.valueOf(t_data[3]).longValue();
								}
				
								AddAccountThread(t_thread,false);
								
								if(m_mainFrame != null){
									m_mainFrame.m_loadDialog.m_state.setText("一共有" + t_lineContain.size()  + "个用户，正在载入第" + (i + 1) + "个用户：");
									m_mainFrame.m_loadDialog.m_state1.setText(t_data[0]);
									m_mainFrame.m_loadDialog.m_progress.setValue(i + 1);
								}								
																
							}catch(Exception e){
								m_logger.PrinterException(e);
							}
						}
					}					
					
					if(m_mainFrame != null){
						m_mainFrame.m_loadDialog.setVisible(false);
						m_mainFrame.m_loadDialog.dispose();
						
						m_mainFrame.m_loadDialog = null;
					}
				}catch(Exception e){
					m_logger.PrinterException(e);
				}
			}
		};
		
		t_load.start();
		
		if(m_mainFrame != null){
			m_mainFrame.m_loadDialog.setVisible(true);
		}
		
	}
	
public fetchThread SearchAccountThread(String _accountName,int _port){
		
		m_logger.LogOut("SearchAccountThread start name="+_accountName);
		
		synchronized (m_accountList) {
			for(int i = 0;i < m_accountList.size();i++){
				fetchThread t_fetch = (fetchThread)m_accountList.elementAt(i);
				if(t_fetch.m_fetchMgr.GetAccountName().equalsIgnoreCase(_accountName) 
				|| t_fetch.m_fetchMgr.GetServerPort() == _port){
					
					m_logger.LogOut("SearchAccountThread end");
					
					return t_fetch;
				}
			}
		}
		
		
		m_logger.LogOut("SearchAccountThread end name="+_accountName);
		
		return null;
	}
		
	
	public boolean AddAccountThread(fetchThread _thread,boolean _storeAccountInfo){
		
		m_logger.LogOut("AddAccountThread start");
		
		synchronized (m_accountList){
			for(int i = 0;i < m_accountList.size();i++){
				fetchThread t_fetch = (fetchThread)m_accountList.elementAt(i);
				if(t_fetch.m_fetchMgr.GetAccountName().equalsIgnoreCase(_thread.m_fetchMgr.GetAccountName())){
					
					m_logger.LogOut("AddAccountThread end");
					return false;
				}
			}
			
			m_accountList.addElement(_thread);
		}
		
		if(m_mainFrame != null){
			m_mainFrame.m_accountTable.AddAccount(_thread);
		}
				
		m_pushInterval		= _thread.m_fetchMgr.GetPushInterval();
		m_expiredTime		= _thread.m_usingHours;
		
		if(_storeAccountInfo){
			storeAccountInfo();
			if(m_mainFrame != null){
				m_mainFrame.m_accountTable.setRowSelectionInterval(m_accountList.size() - 1, m_accountList.size() - 1);
			}
		}
				
		m_logger.LogOut("AddAccountThread end");
		return true;
	}
	
	public void storeAccountInfo(){
		
		m_logger.LogOut("StoreAccountInfo start");
		
		try{
			FileOutputStream t_file = new FileOutputStream(fsm_accountDataFilename);
			
			try{
				StringBuffer t_buffer = new StringBuffer();
				
				synchronized (m_accountList) {
					for(int i = 0;i < m_accountList.size();i++){
						fetchThread t_thread = (fetchThread)m_accountList.elementAt(i);
						t_buffer.append(t_thread.m_fetchMgr.GetAccountName()).append(",")
								.append((t_thread.m_usingHours)).append(",")
								.append(t_thread.m_formerTimer).append(",")
								.append(t_thread.m_clientDisconnectTime).append(",")
								.append("\r\n");
					}
				}
				
				
				t_file.write(t_buffer.toString().getBytes("UTF-8"));
				t_file.flush();
				
			}finally{
				t_file.close();
			}			
			
		}catch(Exception e){
			m_logger.PrinterException(e);
		}
		
		m_logger.LogOut("StoreAccountInfo end");
	}
	
	public void DelAccoutThread(String _accountName,boolean _storeAccountInfo){
		
		m_logger.LogOut("DelAccoutThread start name="+_accountName);
		
		synchronized (m_accountList) {
			for(int i = 0;i < m_accountList.size();i++){
				
				fetchThread t_fetch = (fetchThread)m_accountList.elementAt(i);
				if(_accountName.equalsIgnoreCase(t_fetch.m_fetchMgr.GetAccountName())){
					
					t_fetch.Destroy();
					
					DelDirectory(t_fetch.m_fetchMgr.GetAccountName());
					
					m_accountList.remove(t_fetch);
					
					if(m_mainFrame != null){
						m_mainFrame.m_accountTable.DelAccount(t_fetch);
					}					
					
					if(_storeAccountInfo){
						storeAccountInfo();
					}				
					
					break;
				}
			}
		}		
		
		m_logger.LogOut("DelAccoutThread end name="+_accountName);
	}
	
	/*
	 * the HTTP process... 
	 */
	public int GetAvailableServerPort(){
		
		m_logger.LogOut("GetAvailableServerPort start");
		
		try{
			
			if(m_accountList.isEmpty() && m_bberRequestList.isEmpty()){
				return 9717;
			}
			
			List<Integer> t_portList = new ArrayList<Integer>();
							
			synchronized (m_accountList) {
				for(fetchThread acc : m_accountList){
					t_portList.add(new Integer(acc.m_fetchMgr.GetServerPort()));
				}
			}
			
			
			synchronized (m_bberRequestList) {
				for(BberRequestThread req : m_bberRequestList){
					t_portList.add(new Integer(req.m_serverPort));
				}
			}
			
			
			Collections.sort(t_portList);
			
			for(int i = 0 ;i < t_portList.size() - 1;i++){
				
				int port = t_portList.get(i).intValue() + 1;
				
				if(port < t_portList.get(i + 1).intValue()){
					try{
						ServerSocket t_sock = new ServerSocket(port);
						t_sock.close();
						
						return port;
						
					}catch(Exception e){
						
					}
				
				}
			}
			
			int t_port = t_portList.get(t_portList.size() - 1).intValue() + 1;
			
			while(t_port < 12000){
				try{
					ServerSocket t_sock = new ServerSocket(t_port);
					t_sock.close();
					
					break;
					
				}catch(Exception e){
					t_port++;
				}			
			}
			
			return t_port;
		}finally{
			m_logger.LogOut("GetAvailableServerPort end");
		}		
	}
	/*
	 * the process thread to create/response the GAE URL requeset
	 */
	final class BberRequestThread extends Thread{
		String		m_result		= null;
		yuchbber	m_currbber		= null;
		
		long		m_requestTime	= System.currentTimeMillis();
		int			m_serverPort	= 0;
		
		String		m_prefix		= null;
		
		fetchMgr	m_mainMgr		= null;
		
		boolean	m_prepareSucc 	= false;
		
		fetchThread	m_orgThread = null;
				
		BberRequestThread(yuchbber _bber,fetchThread _orgThread){

			m_currbber		= _bber;
			
			m_orgThread = _orgThread; 
			
			if(m_orgThread != null){
				m_serverPort	= _orgThread.m_fetchMgr.GetServerPort();
				m_prefix		= _orgThread.m_fetchMgr.GetPrefixString();
				
				
				// end listening
				//
				m_orgThread.Pause();
				
			}else{
				m_prefix = _bber.GetSigninName() + "_tmpCreate/";
				
				File t_tmpFolder = new File(m_prefix);
				if(!t_tmpFolder.exists()){
					t_tmpFolder.mkdir();
				}
				
				m_serverPort = GetAvailableServerPort();
			}
						
			if(m_currbber.GetEmailList().isEmpty()
			&& m_currbber.GetWeiboList().isEmpty()
			&& m_currbber.GetIMList().isEmpty()){
				m_result = "<Error>没有账户信息</Error>";
			}else{
				start();
			}
		}
				
		@Override
		public void run(){
			try{
				
				FileOutputStream t_config_file = new FileOutputStream(m_prefix + fetchMgr.fsm_configFilename);
				try{
					StringBuffer t_configBuffer = new StringBuffer();
					t_configBuffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
					
					t_configBuffer.append("<Yuchberry userPassword=\"").append(m_currbber.GetPassword())
											.append("\" serverPort=\"").append(m_serverPort)
											.append("\" pushInterval=\"").append(m_currbber.GetPushInterval())
											
											// caution!!
											//
											//	can't use the SSL
											//
											.append("\" userSSL=\"").append(0/*m_currbber.IsUsingSSL()?1:0*/)
											.append("\" convertoSimpleChar=\"").append(m_currbber.IsConvertSimpleChar()?1:0)
								.append("\" >\n");					
					
					for(yuchEmail email:m_currbber.GetEmailList()){
						t_configBuffer.append("<EmailAccount account=\"").append(email.m_emailAddr)
														.append("\" password=\"").append(email.m_password)
														.append("\" sendName=\"").append(email.m_username)
														.append("\" useFullNameSignIn=\"").append(email.m_fullnameSignIn?1:0)
														.append("\" protocol=\"").append(email.m_protocol)
														.append("\" host=\"").append(email.m_host)
														.append("\" port=\"").append(email.m_port)
														.append("\" protocol_send=\"").append("smtp")
														.append("\" host_send=\"").append(email.m_host_send)
														.append("\" port_send=\"").append(email.m_port_send)
														.append("\" appendHTML=\"").append(email.m_appendHTML?1:0)
										.append("\" />\n");
					}
					
					for(yuchWeibo weibo:m_currbber.GetWeiboList()){
						t_configBuffer.append("<WeiboAccount type=\"").append(weibo.m_typeName)
														.append("\" account=\"").append(weibo.m_accoutName)
														.append("\" accessToken=\"").append(weibo.m_accessToken)
														.append("\" secretToken=\"").append(weibo.m_secretToken)
														.append("\" timelineSum=\"").append(weibo.m_timelineSum)
														.append("\" directMessageSum=\"").append(weibo.m_directMsgSum)
														.append("\" atMeSum=\"").append(weibo.m_atMeSum)
														.append("\" commentSum=\"").append(weibo.m_commentMeSum)
														
										.append("\" />\n");
					}
					
					for(yuchIM im:m_currbber.GetIMList()){
						t_configBuffer.append("<IMAccount type=\"").append(im.m_typeName)
														.append("\" account=\"").append(im.m_accoutName)
														.append("\" password=\"").append(im.m_password)
														.append("\" cryptPassword=\"")
										.append("\" />\n");
					}
					
					t_configBuffer.append("</Yuchberry>");
					t_config_file.write(t_configBuffer.toString().getBytes("UTF-8"));
					
				}finally{
					t_config_file.flush();
					t_config_file.close();
				}
											
				if(m_orgThread != null){
					
					m_logger.LogOut(m_orgThread.m_fetchMgr.GetAccountName() + " start sync current fetchThread.");
					
					m_orgThread.m_fetchMgr.InitConnect(m_orgThread.m_fetchMgr.GetPrefixString(),m_orgThread.m_logger);
					m_orgThread.m_fetchMgr.ResetAllAccountSession(true);
					
					m_orgThread.Reuse();
					
					m_orgThread.m_usingHours = m_currbber.GetUsingHours();
					m_orgThread.m_formerTimer = m_currbber.GetCreateTime();
					
					createDialog.WriteSignatureAndGooglePos(m_orgThread.m_fetchMgr.GetPrefixString(), m_currbber.GetSignature());
					
				}else{
					
					m_logger.LogOut(m_currbber.GetSigninName() + " start sync new bber.");
					
					m_mainMgr = new fetchMgr();
					Logger t_logger = new Logger();
					try{
						m_mainMgr.InitConnect(m_prefix,t_logger);
						
						m_mainMgr.ResetAllAccountSession(true);	
												
						// copy the account information 
						//
						String t_copyPrefix = m_currbber.GetSigninName() + "/";
						
						File t_tmpFolder = new File(m_prefix);
						if(t_tmpFolder.exists()){
							File t_copyDir = new File(t_copyPrefix);
							if(t_copyDir.exists()){
								DelDirectory(t_copyPrefix);
							}
							
							if(!t_tmpFolder.renameTo(t_copyDir)){
								// if rename failed(the original directory can't be deleted) 
								// then write the config.xml to that directory 
								//
								createDialog.CopyFile(m_prefix + fetchMgr.fsm_configFilename,
										t_copyPrefix + fetchMgr.fsm_configFilename);
							}
							
						}else{
							
							File t_accountFolder = new File(t_copyPrefix);
							if(!t_accountFolder.exists()){
								t_accountFolder.mkdir();
							}
							
							createDialog.CopyFile(m_prefix + fetchMgr.fsm_configFilename,
									t_copyPrefix + fetchMgr.fsm_configFilename);
						}
												
						createDialog.WriteSignatureAndGooglePos(t_copyPrefix, m_currbber.GetSignature());
												
					}catch(Exception e){
						
						m_mainMgr.EndListening();
						t_logger.StopLogging();
						
						throw e;
					}
					
				}
				
				m_prepareSucc = true;			
				
			}catch(Exception e){
				m_result = "<Error>" + e.getMessage() + "</Error>";
			}
			
			m_logger.LogOut(m_prefix + " end sync result:" + m_result);
		}
		
		public void CheckStartRequest(){

			try{
				
				if(m_prepareSucc){

					if(m_orgThread == null){

						String t_copyPrefix = m_currbber.GetSigninName() + "/";
						
						fetchThread t_newThread = new fetchThread(m_mainMgr,t_copyPrefix,
																	m_currbber.GetUsingHours(),
																	m_currbber.GetCreateTime(),false);

						AddAccountThread(t_newThread, true);
						
					}else{
						m_orgThread.Reuse();
					}
					
					// to tell the web server port
					//
					m_result = "<Port>" + m_serverPort +"</Port>";
					
				}
				
			}catch(Exception e){
				
				m_result = "<Error>" + e.getMessage() +"</Error>";
			}
			
			m_logger.LogOut(m_currbber.GetSigninName() + " 同步结果: " + m_result);
		}
	}
	
	
	
	static public void DelDirectory(final String _dir){
		
		File t_file = new File(_dir);
		
		if(t_file.exists()){
			if(t_file.isFile()){
				t_file.delete();
			}else if(t_file.isDirectory()){
				File[] t_files = t_file.listFiles();
				for(int i = 0;i < t_files.length;i++){
					DelDirectory(t_files[i].getAbsolutePath());
				}
				t_file.delete();
			}
		}
	}
	
	public void RefreshState(){
		
		m_currUsingAccount		= 0;
		m_currConnectAccount	= 0;
		
		m_checkFolderStateThread.removeAllElements();
				
		final long t_currTime = System.currentTimeMillis();
		
		Vector<fetchThread> t_deadPool = new Vector<fetchThread>();
		
		synchronized (m_accountList) {
			for(int i = 0;i < m_accountList.size();i++){
				fetchThread t_thread = (fetchThread)m_accountList.elementAt(i);
							
				if(!t_thread.m_pauseState){
					
					if(t_thread.m_fetchMgr.IsCheckFolderState()){
						m_checkFolderStateThread.add(t_thread);
					}
					
					if(t_thread.GetLastTime(t_currTime) < 0){
						m_logger.LogOut("帐户暂停： " + t_thread.m_fetchMgr.GetAccountName());
						t_thread.Pause();
					}
					
					m_currUsingAccount++;
					
					final long t_lastTime = t_thread.GetLastTime(t_currTime);
					
					if(!t_thread.m_sendTimeupMail && t_lastTime > 0 && t_lastTime < 48 * 3600 * 1000){
						
						t_thread.m_sendTimeupMail = true;
						
						m_logger.LogOut("到期帐户，发送邮件： " + t_thread.m_fetchMgr.GetAccountName());
						
						SendTimeupMail(t_thread);
					}
					
					if(t_thread.m_fetchMgr.GetClientConnected() != null){
						m_currConnectAccount++;
						t_thread.m_clientDisconnectTime = 0;
					}else{
						if(t_thread.m_clientDisconnectTime == 0){
							t_thread.m_clientDisconnectTime = t_currTime;
						}
					}
					
				}else{
					if(t_thread.m_clientDisconnectTime == 0){
						t_thread.m_clientDisconnectTime = t_currTime;
					}
				}
				
				if(t_thread.m_clientDisconnectTime != 0 && t_thread.m_usingHours != 0){
					
					final long t_delTime = t_thread.m_pauseState?(12 * 3600 * 1000):(5 * 24 * 3600 * 1000);
					
					if(Math.abs(t_currTime - t_thread.m_clientDisconnectTime) >= t_delTime  ){
						t_deadPool.add(t_thread);
					}
				}
			}
		}
		
			
		// delete the disconnect time-up thread
		//
		for(fetchThread thread : t_deadPool){
			m_logger.LogOut("未连接时间超过5天，删除帐户 " + thread.m_fetchMgr.GetAccountName() + " 账户暂停状态：" + thread.m_pauseState);
			
			DelAccoutThread(thread.m_fetchMgr.GetAccountName(), false);			
		}
		
		if(m_mainFrame != null){
			m_mainFrame.m_stateLabel.setText("check/连接账户/使用帐户/总帐户：" + m_checkFolderStateThread.size() + "/" + m_currConnectAccount + "/" + m_currUsingAccount + "/" + m_accountList.size());
			m_mainFrame.m_accountTable.RefreshState();
			
			if(m_mainFrame.m_currentSelectThread != null){
				m_mainFrame.FillLogInfo(m_mainFrame.m_currentSelectThread);
			}
		}
		
		// close the timeup Bber Request
		//
		boolean t_deleteThread = false;
		
		synchronized (m_bberRequestList) {
			for(int i = 0;i < m_bberRequestList.size();i++){
				BberRequestThread bber = m_bberRequestList.get(i);
				
				if(Math.abs(t_currTime - bber.m_requestTime) > 10 * 60 * 1000){
					
					if(bber.m_orgThread != null){
						
						bber.m_orgThread.Destroy();
						DelAccoutThread(bber.m_orgThread.m_fetchMgr.GetAccountName(), false);
						
						m_logger.LogOut("同步没有响应，删除帐户 " + bber.m_orgThread.m_fetchMgr.GetAccountName());
						
						t_deleteThread = true;
					}else{
						bber.m_mainMgr.EndListening();
						
						m_logger.LogOut("同步没有响应，停止帐户 " + bber.m_mainMgr.GetAccountName());
					}
					
					bber.interrupt();
					m_bberRequestList.remove(i);
					
					i--;
				}
			}
		}		
		
		if(!t_deadPool.isEmpty() || t_deleteThread){		
			storeAccountInfo();
		}
	}
	
	public void SendTimeupMail(fetchThread _thread){
		try{
			final String t_contain = fetchMgr.ReadSimpleIniFile("timeupMail.txt","UTF-8",null);
			
			_thread.m_fetchMgr.SendImmMail("yuchberry 提示", t_contain, "\"YuchBerry\" <yuchberry@gmail.com>");
			
		}catch(Exception e){}		
	}
	
	/*
	 * current request thread
	 */
	Vector<BberRequestThread> m_bberRequestList = new Vector<BberRequestThread>();
	
	String 		m_yuchsignFramePass = null;
	String		m_backupShellLine = null;
	int 	  	m_yuchsignMaxBber = 0;
	
	public boolean m_needbackup = false;		
	
	private void LoadYuchsign(){
		
		try{
			BufferedReader in = new BufferedReader(
									new InputStreamReader(
										new FileInputStream("yuchsign"),"UTF-8"));
						
			try{
				m_yuchsignFramePass 	= in.readLine();
				String	t_port 			= in.readLine();
				m_yuchsignMaxBber 		= Integer.valueOf(in.readLine()).intValue();
				m_backupShellLine		= in.readLine();
				
				if(m_yuchsignFramePass == null || m_yuchsignFramePass.isEmpty()){
					return ;
				}
				
				m_needbackup = true;
				
				m_httpd = new NanoHTTPD(Integer.valueOf(t_port).intValue()){
					public Response serve( String uri, String method, Properties header, Properties parms, Properties files ){
								
						m_logger.LogOut("start serve");
						try{
							
							String pass = header.getProperty("pass");
							String type = parms.getProperty("type");
							
							if(pass == null){
								pass = parms.getProperty("pass");
								if(pass == null || !pass.equals(m_yuchsignFramePass)){
									return new NanoHTTPD.Response( HTTP_FORBIDDEN, MIME_PLAINTEXT, "");
								}
								
							}
						
							if(type != null){
								return new NanoHTTPD.Response( HTTP_OK, MIME_PLAINTEXT, ProcessAdminCheck(type,parms.getProperty("bber")));
							}else{
								return new NanoHTTPD.Response( HTTP_OK, MIME_PLAINTEXT, ProcessHTTPD(method,header,parms));
							}
							
							
						}finally{
							m_logger.LogOut("end serve");
						}
					}
				};
				
				m_logger.LogOut("start HTTP serve");
				
			}finally{
				in.close();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private String ProcessAdminCheck(String _type,String _bber){
		
		m_logger.LogOut("ProcessAdminCheck start fill admin data type: " + _type + " _bber:" + _bber);
				
		try{
			if(_type.equals("0")){
				
				JSONObject t_result = new JSONObject();
				
				StringBuffer t_abs = new StringBuffer();
				t_abs.append("state:").append(m_checkFolderStateThread.size()).append("/")
									.append(m_currConnectAccount).append("/").append(m_currUsingAccount)
									.append("/").append(m_accountList.size());
				
				try{
					t_result.put("abs",t_abs.toString());
					
					JSONArray t_logList = new JSONArray();
					
					long t_currTime = System.currentTimeMillis();
					
					if(!m_accountList.isEmpty()){
						
						synchronized (m_accountList) {
							for(int i = m_accountList.size() - 1; i >= 0;i--){
								fetchThread t_acc = m_accountList.elementAt(i);
								JSONObject t_accObj = new JSONObject();
								t_accObj.put("acc",t_acc.m_fetchMgr.GetAccountName());
								t_accObj.put("port",t_acc.m_fetchMgr.GetServerPort());
								t_accObj.put("PIN",t_acc.m_fetchMgr.m_IMEI + t_acc.m_fetchMgr.m_pin);
														
								if(!t_acc.m_pauseState){
									long t_remainTime = t_acc.GetLastTime(t_currTime);
									t_accObj.put("remain",t_remainTime / 3600000);
								}else{		
									t_accObj.put("remain",-1);
								}
								
								t_accObj.put("state",t_acc.getConnectState());
								t_logList.put(t_accObj);
							}
						}
					}
					
					t_result.put("log",t_logList);

				}catch(Exception e){
					m_logger.PrinterException(e);
				}
				
				return t_result.toString();
			}else if(_type.equals("1")){
				
				StringBuffer t_result = new StringBuffer();
				
				
				t_result.append("state:").append(m_checkFolderStateThread.size()).append("/")
										.append(m_currConnectAccount).append("/").append(m_currUsingAccount)
										.append("/").append(m_accountList.size()).append("\n");
				
				
				if(!m_accountList.isEmpty()){
					
					synchronized (m_accountList) {
						
						for(int i = m_accountList.size() - 1; i >= 0;i--){
							fetchThread t_acc = m_accountList.elementAt(i);
							
							if(t_acc.m_fetchMgr.GetClientConnected() == null){
								t_result.append(t_acc.m_fetchMgr.GetAccountName()).append("\t\t");
								t_result.append("\t\t").append(t_acc.GetStateString()).append("\n");
							}						
						}
					}
					
				}
							
				return t_result.toString();
				
			}else if(_type.equals("2") && _bber != null){
				
				return ProcessLogQuery(_bber,true);
				
			}else if(_type.equals("3") && _bber != null){
				return ProcessDelBberQuery(_bber);
			}else if(_type.equals("close")){
				// close the server
				//
		    	storeAccountInfo();
		    	
		    	new Thread(){
		    		public void run(){
		    			try{
		    				sleep(1000);
		    				System.exit(0);
		    			}catch(Exception e){}
		    		}
		    	}.start();		    	
		    	
		    	return "Close done!";
			}
			
			return "";
		}finally{
			m_logger.LogOut("ProcessAdminCheck end");
		}
	}
	
	private String ProcessDelBberQuery(final String _bber){
		new Thread(){
			public void run(){
				DelAccoutThread(_bber, true);
			}
		}.start();		
		return "<Processed />";
	}
	
	private String ProcessHTTPD(String method, Properties header, Properties parms){
		
		m_logger.LogOut("start ProcessHTTPD 0");
		
		String t_string = parms.getProperty("bber");
		if( t_string == null){
			return "<Error>没有bber参数的URL</Error>";
		}
		
		try{
			
			String t_bberParam = URLDecoder.decode(t_string, "UTF-8");
			
			if((parms.getProperty("log") != null)){
				return ProcessLogQuery(t_bberParam,false);
			}
			
			if((parms.getProperty("create") != null)){
				return ProcessCreateTimeQuery(parms);
			}
			
			m_logger.LogOut("start ProcessHTTPD 1");
			
			boolean t_checkState = (parms.getProperty("check") != null);		
					
			String t_signinName = null;
			yuchbber t_bber = null;
			if(t_checkState){
				t_signinName = t_bberParam;
			}else{
				t_bber = new yuchbber();
				t_bber.InputXMLData(t_bberParam);
				
				t_signinName = t_bber.GetSigninName();
			}
			
			synchronized (m_bberRequestList) {
				// find the requested bber
				//
				for(BberRequestThread bber : m_bberRequestList){
					if(bber.m_currbber.GetSigninName().equals(t_signinName)){

						if(bber.isAlive()){
							return "<Loading />";
						}else{
							
							bber.CheckStartRequest();
							
							m_bberRequestList.remove(bber);
							
							return bber.m_result;
						}									 
					}
				}
			}

			// search the former thread
			//
			fetchThread t_thread = SearchAccountThread(t_bber.GetSigninName(),0);
			
			if(t_thread == null && m_accountList.size() >= m_yuchsignMaxBber){
				// if reach the max bber
				//
				return "<Max />";
			}
			
			// the constructor of BberRequestThread has fetchThread.Pause
			// that function will be suspended by some wired reason
			//
			BberRequestThread t_request = new BberRequestThread(t_bber,t_thread);
			
			synchronized (m_bberRequestList) {
				// create new request thread
				//
				m_bberRequestList.add(t_request);
			}
			
			
			m_logger.LogOut("bber <" + t_bber.GetSigninName() + "> sync, current syncing Thread:" + m_bberRequestList.size());
			
			return "<Loading />";			
			
		}catch(Exception e){
			
			return "<Error>" + e.getMessage() + "</Error>";
		}
		
		
	}
	
	private String ProcessCreateTimeQuery(Properties header){
		
		m_logger.LogOut("ProcessCreateTimeQuery start");
		try{
			try{
				
				final String t_bber 		= URLDecoder.decode(header.getProperty("bber"),"UTF-8");
				final String t_createTime	= header.getProperty("create");
				final String t_usingHours	= header.getProperty("time");
				
				if(t_bber == null || t_createTime == null || t_usingHours == null){
					return "参数错误";
				}
				
				m_logger.LogOut("t_bber="+ t_bber+ " ProcessCreateTimeQuery");
				
				synchronized (m_accountList) {
					for(fetchThread thread:m_accountList){
						if(thread.m_fetchMgr.GetAccountName().equalsIgnoreCase(t_bber)){
							
							thread.m_usingHours = Long.valueOf(t_usingHours).longValue();
							thread.m_formerTimer = Long.valueOf(t_createTime).longValue();
											
							storeAccountInfo();
							
							if(thread.m_pauseState){
								thread.Reuse();
							}
							
							return "<OK />";
						}
					}
				}
				
				
				return "原有账户已经因为长时间没有链接而删除，需要重新同步";
				
			}catch(Exception e){
				return "服务器重新启用账户出现异常，需要手动同步";
			}
		}finally{
			m_logger.LogOut("ProcessCreateTimeQuery end");
		}
		
	}
	
	static final int fsm_maxReadLogLen = 1024 * 4;
	static final byte[] fsm_readLogBuffer = new byte[fsm_maxReadLogLen];
	
	static final int fsm_maxReadLogLen_admin = 1024 * 30;
	static final byte[] fsm_readLogBuffer_admin = new byte[fsm_maxReadLogLen_admin]; 
	
	private String ProcessLogQuery(String _bberName,boolean _admin){
		
		m_logger.LogOut(_bberName + " query log.");
		
		// search the former thread
		//
		fetchThread t_thread = SearchAccountThread(_bberName,0);
		if(t_thread == null){
			m_logger.LogOut(_bberName + " query log error.");
			return "<Error>在主机上查询不到账户，请先同步，获得有效主机。</Error>";
		}
		
		try{
			FileInputStream t_stream = new FileInputStream(t_thread.m_logger.GetLogFileName());
			try{
				
				int t_fileLen = t_stream.available();
				if(t_fileLen == 0){
					m_logger.LogOut(_bberName + " query log null.");
					return "null";
				}
				
				int t_bufferLength;
				byte[] t_buffer;
				if(_admin){
					t_bufferLength	= fsm_maxReadLogLen_admin;
					t_buffer		= fsm_readLogBuffer_admin;
				}else{
					t_bufferLength	= fsm_maxReadLogLen;
					t_buffer		= fsm_readLogBuffer;
				}
				
				t_stream.skip(Math.max(t_fileLen - t_bufferLength,0));
								
				final int t_bufferLen = Math.min(t_fileLen - 1, t_bufferLength);
				t_stream.read(t_buffer,0,t_bufferLen);
				
				return (new String(t_buffer,0,t_bufferLen,"UTF-8"));
				
			}finally{
				
				t_stream.close();
				m_logger.LogOut(_bberName + " query log done.");
			}
		}catch(Exception e){
			return "<Error>读取文件失败:"+e.getMessage()+"</Error>";
		}
		
	}
}
