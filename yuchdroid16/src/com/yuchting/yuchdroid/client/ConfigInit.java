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
package com.yuchting.yuchdroid.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import android.content.Context;
import android.util.Log;

import com.yuchting.yuchdroid.client.im.IMStatus;

public final class ConfigInit {
	
	public Context	m_ctx			= null;
	public final static String TAG = "ConfigInit";
	
	public String m_host					= null;
	public String m_account					= null;
	public int m_port						= 0;
	public String m_userPass				= null;
	
	public String m_passwordKey				= "";
	public boolean m_useSSL				= false;
	public boolean m_autoRun				= false;
	
	public int[]	m_pulseIntervalValues	= {1 * 60000,3 * 60000,5 * 60000,10 * 60000,30 * 60000};
	public int	m_pulseIntervalIndex		= 2;
	
	public boolean m_fulldayPrompt		= true;
	public int	m_startPromptHour		= 8;
	public int	m_endPromptHour			= 22;
	public boolean m_connectDisconnectPrompt = false;
	public boolean m_connectDisconnectPrompt_vibrate = true;
	public String	m_connectDisconnectPrompt_sound = "";
					
	public long m_uploadByte 				= 0;
	public long m_downloadByte				= 0;
	
	public int[]	m_fontSizeValues		= {14,16,18,20,22};
	
	public int[]	m_mailClearBeforeDay = {15,30,60,90,120,150,180,-1};
	public int		m_mailClearBeforeDayIndex = 1;
	
	// always display connect state
	//
	public boolean m_alwaysDisplayState	= false;
		
	// mail system variables
	//
	public Vector<String>		m_sendMailAccountList = new Vector<String>();
	public int					m_defaultSendMailAccountIndex = 0;
	
	public boolean				m_copyMailToSentFolder = false;
	public boolean 			m_enableMailModule	= true;
	public boolean				m_displayTextWhenHTML = false;
	
	public int	m_sendMailNum			= 0;
	public int	m_recvMailNum			= 0;
	public boolean m_discardOrgText	= false;
	public boolean m_delRemoteMail		= false;
	
	public boolean m_mailPrompt_vibrate = false;
	public String	m_mailPrompt_sound = "";
	
	public int					m_mailFontSizeIndex = 1;
		
	// weibo system
	//
	public boolean m_enableWeiboModule	= false;
	
	public boolean m_updateOwnListWhenFw = true;
	public boolean m_updateOwnListWhenRe = false;
	public boolean m_commentFirst		= false;
	public boolean m_publicForward		= false;
	
	public static final String[]	fsm_maxWeiboNumList = {"64","128","256","512","1024"};
	public static final int[]	fsm_maxWeiboNum		= {64,128,256,512,1024};
	public int m_maxWeiboNumIndex			= 0;
	
	
	public int m_receivedWeiboNum 		= 0;
	public int m_sentWeiboNum 			= 0;
	
	public boolean m_displayHeadImage 	= true;
	public boolean m_weiboSimpleMode		= false;
	public boolean m_dontDownloadWeiboHeadImage = false;
			
	public boolean 	m_hasPromptToCheckImg = true;
	public int		m_checkImgIndex 		= 0;
	
	public static final String[]	fsm_refreshWeiboIntervalList = {"0","10","20","30","40"};
	public static final int[]		fsm_refreshWeiboInterval		= {0,10,20,30,40};
	public int						m_refreshWeiboIntervalIndex = 0;
	
	public int m_weiboUploadImageSizeIndex	= 0;
	
	public boolean m_weiboPrompt_vibrate = false;
	public String	m_weiboPrompt_sound = "";
	public int		m_weiboFontSizeIndex = 0;
	
	// IM system
	//
	public boolean m_enableIMModule		= false;
	public boolean m_enableChatChecked	= true;
	public boolean m_enableChatState		= true;
	public boolean m_hideUnvailiableRoster = true;
	
	public int	m_imCurrUseStatusIndex	= 0;
	public static Vector<IMStatus>		sm_imStatusList			= new Vector<IMStatus>();
	
	public static final String[]	fsm_imChatMsgHistoryList 	= {"32","64","128","256"};
	public static final int[]		fsm_imChatMsgHistory		= {32,64,128,256};
	public int	m_imChatMsgHistory = 0;
	
	public boolean m_imChatScreenReceiveReturn = false;
	
	public boolean m_imDisplayTime = true;
	public boolean m_imReturnSend = false;
	public boolean m_imPopupPrompt = true;
	
	public boolean m_autoLoadNewTimelineWeibo = false;
			
	public int 	m_imSendImageQuality = 0;
	public boolean m_standardUI	= false;
	public boolean m_imVoiceImmMode = false;
	
	public boolean m_imPrompt_vibrate = false;
	public String	m_imPrompt_sound = "";
	public int		m_imFontSizeIndex = 0;
	
	public ConfigInit(Context _ctx){
		m_ctx = _ctx;
				
		m_pulseIntervalValues	= reinitArray(R.array.login_pref_pulse_values,60000);
		m_fontSizeValues		= reinitArray(R.array.mail_font_size_values,1);
		m_mailClearBeforeDay	= reinitArray(R.array.mail_clear_before_day_values,1);
		
	}
	
	
	
	private int[] reinitArray(int _resId,int _rate){
		
		String[] t_str = m_ctx.getResources().getStringArray(_resId);
		assert t_str != null;
		
		int[] t_arr = new int[t_str.length];
		for(int i = 0;i < t_arr.length;i++){
			t_arr[i] = Integer.valueOf(t_str[i]).intValue() * _rate;
		}
		
		return t_arr;
	}
	
	public int getClearMailBeforeDays(){
		if(m_mailClearBeforeDayIndex < 0 || m_mailClearBeforeDayIndex >= m_mailClearBeforeDay.length){
			return m_mailClearBeforeDay[0];
		}
		
		return m_mailClearBeforeDay[m_mailClearBeforeDayIndex];
	}
	
	public int getPulseInterval(){
		if(m_pulseIntervalIndex < m_pulseIntervalValues.length
			&& m_pulseIntervalIndex >= 0){
			return m_pulseIntervalValues[m_pulseIntervalIndex];
		}
		m_pulseIntervalIndex = 0;
		return m_pulseIntervalValues[0];
	}
	
	public int getMailFontSize(){
		if(m_mailFontSizeIndex < m_fontSizeValues.length
		&& m_mailFontSizeIndex >= 0){
			return m_fontSizeValues[m_mailFontSizeIndex];
		}
		m_mailFontSizeIndex = 0;
		return m_fontSizeValues[0];
	}
	
	static Calendar sm_calendar = Calendar.getInstance();
	static Date		sm_timeDate = new Date();
	
	public boolean isPromptTime(){
		if(m_fulldayPrompt){
			return true;
		}
		
		int t_startHour		= m_startPromptHour & 0x0000ffff;
		int t_startMinutes	= m_startPromptHour  >>> 16;
		
		int t_endHour		= m_endPromptHour & 0x0000ffff;
		int t_endMinutes	= m_endPromptHour >>> 16;
		
		sm_timeDate.setTime(System.currentTimeMillis());
		sm_calendar.setTime(sm_timeDate);
		
		int t_hour = sm_calendar.get(Calendar.HOUR_OF_DAY);
		int t_minutes = sm_calendar.get(Calendar.MINUTE);
		
		if( ( (t_startHour == t_hour && t_startMinutes <= t_minutes) || t_startHour < t_hour )
		&& ((t_endHour == t_hour && t_endMinutes > t_minutes) ||  t_endHour > t_hour)){
			return true;
		}
		
		return false;
	}
	
	public void SetErrorString(String _error){
		Log.e(TAG,_error);
	}
	
	public void SetErrorString(String _error,Exception e){
		SetErrorString(_error+" msg:"+e.getMessage()+" cls:"+e.getClass().getName());
		e.printStackTrace();
	}
	
	public void PreWriteReadIni(boolean _read,
			String _backPathFilename,String _orgPathFilename){
		
		try{
			if(_read){
				File t_back = m_ctx.getFileStreamPath(_backPathFilename);

				if(t_back.exists()){
					File t_ini = m_ctx.getFileStreamPath(_orgPathFilename);
					
					if(t_ini.exists()){
						t_ini.delete();
					}	
					
					
					t_back.renameTo(m_ctx.getFileStreamPath(_orgPathFilename));
				}
								
				
			}else{
				File t_ini = m_ctx.getFileStreamPath(_orgPathFilename);

				if(t_ini.exists()){
					t_ini.renameTo(m_ctx.getFileStreamPath(_backPathFilename));
				}				
				
				// needn't copy ,the normal WriteReadIni method will re-create the init.data file
				//
				//Copyfile(fsm_backInitFilename,fsm_initFilename);
			}
			
		}catch(Exception e){
			SetErrorString("write/read PreWriteReadIni file from SDCard error :" + e.getMessage() + e.getClass().getName());
		}
	}
	
	final static int		fsm_configVersion = 6;
	
	static final String fsm_initFilename_init_data = "Init.data";
	static final String fsm_initFilename_back_init_data = "~Init.data";
	
	public synchronized void WriteReadIni(boolean _read){		
		// process the ~Init.data file to restore the destroy original file
		// that writing when device is down  
		//
		// check the issue 85 
		// http://code.google.com/p/yuchberry/issues/detail?id=85&colspec=ID%20Type%20Status%20Priority%20Stars%20Summary
		//
		PreWriteReadIni(_read,fsm_initFilename_back_init_data,fsm_initFilename_init_data);
		
		try{
						
			if(_read){
				
				File t_file = m_ctx.getFileStreamPath(fsm_initFilename_init_data);
				if(t_file.exists()){
					FileInputStream t_readFile = m_ctx.openFileInput(fsm_initFilename_init_data);
					try{
						int t_version = sendReceive.ReadInt(t_readFile);
						
						m_host 			= sendReceive.ReadString(t_readFile);
						m_port 			= sendReceive.ReadInt(t_readFile);
						m_userPass 		= sendReceive.ReadString(t_readFile);
											
						m_useSSL 		= sendReceive.ReadBoolean(t_readFile);
						m_autoRun 		= sendReceive.ReadBoolean(t_readFile);
						
						m_uploadByte 	= sendReceive.ReadLong(t_readFile);
						m_downloadByte 	= sendReceive.ReadLong(t_readFile);
						
						m_pulseIntervalIndex = sendReceive.ReadInt(t_readFile);
											
						m_fulldayPrompt 	= sendReceive.ReadBoolean(t_readFile);
						m_startPromptHour	= sendReceive.ReadInt(t_readFile);
						m_endPromptHour		= sendReceive.ReadInt(t_readFile);
						m_passwordKey		= sendReceive.ReadString(t_readFile);
						m_connectDisconnectPrompt = sendReceive.ReadBoolean(t_readFile);
						
						// mail system 
						//
						m_enableMailModule	= sendReceive.ReadBoolean(t_readFile);
						m_sendMailNum		= sendReceive.ReadInt(t_readFile);
						m_recvMailNum		= sendReceive.ReadInt(t_readFile);
						
						m_discardOrgText	= sendReceive.ReadBoolean(t_readFile);
						m_delRemoteMail		= sendReceive.ReadBoolean(t_readFile);
											
						m_copyMailToSentFolder			= sendReceive.ReadBoolean(t_readFile);
						m_defaultSendMailAccountIndex	= sendReceive.ReadInt(t_readFile);
						sendReceive.ReadStringVector(t_readFile,m_sendMailAccountList);
											
						// weibo system
						//
						m_enableWeiboModule		= sendReceive.ReadBoolean(t_readFile);
						m_updateOwnListWhenFw	= sendReceive.ReadBoolean(t_readFile);
						m_updateOwnListWhenRe	= sendReceive.ReadBoolean(t_readFile);
						m_commentFirst			= sendReceive.ReadBoolean(t_readFile);
						m_publicForward			= sendReceive.ReadBoolean(t_readFile);
						
						m_maxWeiboNumIndex		= sendReceive.ReadInt(t_readFile);
						m_receivedWeiboNum		= sendReceive.ReadInt(t_readFile);
						m_sentWeiboNum			= sendReceive.ReadInt(t_readFile);
						
						m_displayHeadImage		= sendReceive.ReadBoolean(t_readFile);
						m_weiboSimpleMode		= sendReceive.ReadBoolean(t_readFile);
						m_dontDownloadWeiboHeadImage = sendReceive.ReadBoolean(t_readFile);
											
						m_hasPromptToCheckImg	= sendReceive.ReadBoolean(t_readFile);
						m_checkImgIndex 		= t_readFile.read();
						
						m_refreshWeiboIntervalIndex = sendReceive.ReadInt(t_readFile);
						
						m_weiboUploadImageSizeIndex	= t_readFile.read();

						// IM system
						//
						m_enableIMModule		= sendReceive.ReadBoolean(t_readFile);
						m_enableChatChecked		= sendReceive.ReadBoolean(t_readFile);
						m_enableChatState		= sendReceive.ReadBoolean(t_readFile);
						m_hideUnvailiableRoster	= sendReceive.ReadBoolean(t_readFile);
		    			
						m_imCurrUseStatusIndex	= sendReceive.ReadInt(t_readFile);		    			
		    			int t_size = sendReceive.ReadInt(t_readFile);
		    			sm_imStatusList.removeAllElements();
		    			for(int i = 0;i < t_size;i++){
		    				IMStatus status = new IMStatus();
		    				status.Import(t_readFile);	
		    				sm_imStatusList.add(status);
		    			}
		    			
		    			m_imChatMsgHistory 			= sendReceive.ReadInt(t_readFile);
		    			m_imChatScreenReceiveReturn	= sendReceive.ReadBoolean(t_readFile);
		    			
		    			m_imDisplayTime				= sendReceive.ReadBoolean(t_readFile);
		    			m_imReturnSend				= sendReceive.ReadBoolean(t_readFile);
		    			m_imPopupPrompt				= sendReceive.ReadBoolean(t_readFile);
		    			
		    			m_autoLoadNewTimelineWeibo	= sendReceive.ReadBoolean(t_readFile);
		    			m_imVoiceImmMode			= sendReceive.ReadBoolean(t_readFile);
		    			m_imSendImageQuality		= sendReceive.ReadInt(t_readFile);
		    			
		    			m_standardUI				= sendReceive.ReadBoolean(t_readFile);
		    			
		    			if(t_version >= 1){
		    				m_connectDisconnectPrompt_vibrate = sendReceive.ReadBoolean(t_readFile);
		    				m_connectDisconnectPrompt_sound	= sendReceive.ReadString(t_readFile);
		    				
		    				m_mailPrompt_vibrate			= sendReceive.ReadBoolean(t_readFile);
		    				m_mailPrompt_sound				= sendReceive.ReadString(t_readFile);
		    				
		    				m_weiboPrompt_vibrate			= sendReceive.ReadBoolean(t_readFile);
		    				m_weiboPrompt_sound				= sendReceive.ReadString(t_readFile);
		    				
		    				m_imPrompt_vibrate				= sendReceive.ReadBoolean(t_readFile);
		    				m_imPrompt_sound				= sendReceive.ReadString(t_readFile);
		    			}
		    			
		    			if(t_version >= 2){
		    				m_displayTextWhenHTML			= sendReceive.ReadBoolean(t_readFile);
		    			}
		    			
		    			if(t_version >= 3){
		    				m_mailFontSizeIndex				= t_readFile.read();
		    				m_weiboFontSizeIndex			= t_readFile.read();
		    				m_imFontSizeIndex				= t_readFile.read();
		    			}
		    			
		    			if(t_version >= 4){
		    				m_alwaysDisplayState			= sendReceive.ReadBoolean(t_readFile);
		    			}
		    			
		    			if(t_version >= 5){
		    				m_mailClearBeforeDayIndex		= t_readFile.read();
		    			}
		    			
		    			if(t_version >= 6){
		    				m_account						= sendReceive.ReadString(t_readFile);
		    				
		    			}
		    			
					}finally{
						t_readFile.close();
					}
					
				}
				
			}else{
				
				File t_file = m_ctx.getFileStreamPath(fsm_initFilename_init_data);
				if(!t_file.exists()){
					t_file.createNewFile();
				}
				
				FileOutputStream t_writeFile = m_ctx.openFileOutput(fsm_initFilename_init_data, Context.MODE_PRIVATE);
				try{
					sendReceive.WriteInt(t_writeFile,fsm_configVersion);
					
					sendReceive.WriteString(t_writeFile, m_host);
					sendReceive.WriteInt(t_writeFile,m_port);
					sendReceive.WriteString(t_writeFile, m_userPass);
										
					sendReceive.WriteBoolean(t_writeFile,m_useSSL);
					sendReceive.WriteBoolean(t_writeFile,m_autoRun);
					
					sendReceive.WriteLong(t_writeFile,m_uploadByte);
					sendReceive.WriteLong(t_writeFile, m_downloadByte);
					
					sendReceive.WriteInt(t_writeFile,m_pulseIntervalIndex);
										
					sendReceive.WriteBoolean(t_writeFile,m_fulldayPrompt);
					sendReceive.WriteInt(t_writeFile,m_startPromptHour);
					sendReceive.WriteInt(t_writeFile,m_endPromptHour);
					sendReceive.WriteString(t_writeFile,m_passwordKey);
					sendReceive.WriteBoolean(t_writeFile,m_connectDisconnectPrompt);
					
					// mail system 
					//
					sendReceive.WriteBoolean(t_writeFile,m_enableMailModule);
					sendReceive.WriteInt(t_writeFile,m_sendMailNum);
					sendReceive.WriteInt(t_writeFile,m_recvMailNum);
					
					sendReceive.WriteBoolean(t_writeFile,m_discardOrgText);
					sendReceive.WriteBoolean(t_writeFile,m_delRemoteMail);
										
					sendReceive.WriteBoolean(t_writeFile,m_copyMailToSentFolder);
					sendReceive.WriteInt(t_writeFile,m_defaultSendMailAccountIndex);
					sendReceive.WriteStringVector(t_writeFile, m_sendMailAccountList);
										
					// weibo system
					//
					sendReceive.WriteBoolean(t_writeFile,m_enableWeiboModule);
					sendReceive.WriteBoolean(t_writeFile,m_updateOwnListWhenFw);
					sendReceive.WriteBoolean(t_writeFile,m_updateOwnListWhenRe);
					sendReceive.WriteBoolean(t_writeFile,m_commentFirst);
					sendReceive.WriteBoolean(t_writeFile,m_publicForward);
					
					sendReceive.WriteInt(t_writeFile,m_maxWeiboNumIndex);
					sendReceive.WriteInt(t_writeFile,m_receivedWeiboNum);
					sendReceive.WriteInt(t_writeFile,m_sentWeiboNum);
					
					sendReceive.WriteBoolean(t_writeFile,m_displayHeadImage);
					sendReceive.WriteBoolean(t_writeFile,m_weiboSimpleMode);
					sendReceive.WriteBoolean(t_writeFile,m_dontDownloadWeiboHeadImage);
										
					sendReceive.WriteBoolean(t_writeFile, m_hasPromptToCheckImg);
					t_writeFile.write(m_checkImgIndex);
					
					sendReceive.WriteInt(t_writeFile,m_refreshWeiboIntervalIndex);
					
					t_writeFile.write(m_weiboUploadImageSizeIndex);

					
					// IM system
					//
					sendReceive.WriteBoolean(t_writeFile,m_enableIMModule);
	    			sendReceive.WriteBoolean(t_writeFile,m_enableChatChecked);
	    			sendReceive.WriteBoolean(t_writeFile,m_enableChatState);
	    			sendReceive.WriteBoolean(t_writeFile,m_hideUnvailiableRoster);
	    			
	    			sendReceive.WriteInt(t_writeFile,m_imCurrUseStatusIndex);		    			
	    			sendReceive.WriteInt(t_writeFile,sm_imStatusList.size());
	    			for(int i = 0;i < sm_imStatusList.size();i++){
	    				IMStatus status = (IMStatus)sm_imStatusList.elementAt(i);
	    				status.Ouput(t_writeFile);
	    			}
	    			sendReceive.WriteInt(t_writeFile,m_imChatMsgHistory);
	    			sendReceive.WriteBoolean(t_writeFile,m_imChatScreenReceiveReturn);
	    			
	    			sendReceive.WriteBoolean(t_writeFile,m_imDisplayTime);
	    			sendReceive.WriteBoolean(t_writeFile, m_imReturnSend);
	    			sendReceive.WriteBoolean(t_writeFile, m_imPopupPrompt);
	    			
	    			sendReceive.WriteBoolean(t_writeFile,m_autoLoadNewTimelineWeibo);
	    			
	    			sendReceive.WriteBoolean(t_writeFile,m_imVoiceImmMode);
	    			
	    			sendReceive.WriteInt(t_writeFile,m_imSendImageQuality);
	    			sendReceive.WriteBoolean(t_writeFile,m_standardUI);
	    			
	    			// version 1
	    			sendReceive.WriteBoolean(t_writeFile,m_connectDisconnectPrompt_vibrate);
	    			sendReceive.WriteString(t_writeFile,m_connectDisconnectPrompt_sound);
    				
    				sendReceive.WriteBoolean(t_writeFile,m_mailPrompt_vibrate);
    				sendReceive.WriteString(t_writeFile,m_mailPrompt_sound);
    				
    				sendReceive.WriteBoolean(t_writeFile,m_weiboPrompt_vibrate);
    				sendReceive.WriteString(t_writeFile,m_weiboPrompt_sound);
    				
    				sendReceive.WriteBoolean(t_writeFile,m_imPrompt_vibrate);
    				sendReceive.WriteString(t_writeFile,m_imPrompt_sound);
    				
    				sendReceive.WriteBoolean(t_writeFile,m_displayTextWhenHTML);
    				
    				t_writeFile.write(m_mailFontSizeIndex);
    				t_writeFile.write(m_weiboFontSizeIndex);
    				t_writeFile.write(m_imFontSizeIndex);
    				
    				sendReceive.WriteBoolean(t_writeFile,m_alwaysDisplayState);
    				
    				t_writeFile.write(m_mailClearBeforeDayIndex);
    				sendReceive.WriteString(t_writeFile,m_account);
					
				}finally{
					t_writeFile.close();
				}
				

    			// delete backup ~init.data 
    			//
    			File t_backIni = m_ctx.getFileStreamPath(fsm_initFilename_back_init_data);
				if(t_backIni.exists()){
					t_backIni.delete();
				}
			}
			
		}catch(Exception e){
			SetErrorString("WriteReadIni:",e);
		}
	}
}
