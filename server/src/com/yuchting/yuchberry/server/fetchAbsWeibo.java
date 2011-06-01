package com.yuchting.yuchberry.server;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.Vector;

import javax.imageio.ImageIO;

import org.dom4j.Element;

public abstract class fetchAbsWeibo extends fetchAccount{

	byte[] m_headImageBuffer	= new byte[1024 * 10];
	
	String	m_headImageDir		= null;
		
	String	m_prefix			= null;
	
	String	m_accountName 		= null;
	
	String	m_accessToken		= null;
	String	m_secretToken		= null;

	final class fetchWeiboData{
		long					m_fromIndex = -1;
		Vector<fetchWeibo>		m_historyList = null;
		Vector<fetchWeibo>		m_weiboList = new Vector<fetchWeibo>();
		int						m_sum		= 1;
		int						m_counter	= -1;
		Vector<fetchWeibo>		m_WeiboComfirm	= new Vector<fetchWeibo>();
	}
	
	fetchWeiboData				m_timeline 		= new fetchWeiboData();
	fetchWeiboData				m_directMessage	= new fetchWeiboData();
	fetchWeiboData				m_atMeMessage	= new fetchWeiboData();
	fetchWeiboData				m_commentMessage= new fetchWeiboData();
	
	//! the time of weibo check folder call
	int							m_weiboDelayTimer = -1;
	
	//! check number 
	int		m_maxCheckFolderNum = 0;
	int		m_currRemainCheckFolderNum = 0;
	
	public fetchAbsWeibo(fetchMgr _mainMgr){
		super(_mainMgr);
	}
	
	/**
	 * get the directory prefix of this account 
	 */
	public String GetAccountPrefix(){
		return m_prefix;
	}

	/**
	 * initialize the account to sign in 
	 * 
	 * @param _elem		: the xml element for read the attribute 
	 */
	public void InitAccount(Element _elem)throws Exception{
		m_accountName			= fetchAccount.ReadStringAttr(_elem,"account");
		m_accessToken			= fetchAccount.ReadStringAttr(_elem,"accessToken");
		m_secretToken			= fetchAccount.ReadStringAttr(_elem,"secretToken");
		
		m_timeline.m_sum		= fetchAccount.ReadIntegerAttr(_elem,"timelineSum");
		m_directMessage.m_sum	= fetchAccount.ReadIntegerAttr(_elem,"directMessageSum");
		m_atMeMessage.m_sum		= fetchAccount.ReadIntegerAttr(_elem,"atMeSum");
		m_commentMessage.m_sum	= fetchAccount.ReadIntegerAttr(_elem,"commentSum");
		
		m_prefix				= m_accountName + "/";
		
		// create the account directory
		//
		File t_file = new File(m_mainMgr.GetPrefixString() + m_prefix);
		if(!t_file.exists()){
			t_file.mkdir();
		}
		
		m_headImageDir			= m_mainMgr.GetPrefixString() + m_prefix + "WeiboHeadImage/";
		t_file = new File(m_headImageDir);
		if(!t_file.exists()){
			t_file.mkdir();
		}
	}
	
	public String GetHeadImageFilename(final long _id){
		return GetHeadImageDir() + _id + ".png";
	}
	
	/**
	 * get the account name (Email address)
	 */
	public String GetAccountName(){
		return m_accountName;
	}
	
	/**
	 * check the folder to find the news to push
	 */
	public synchronized void CheckFolder()throws Exception{
		
		try{
			
			if(m_weiboDelayTimer == -1 
			|| m_weiboDelayTimer >= (3600 / 100) / m_mainMgr.GetPushInterval() + 1){

				synchronized(this){
					m_weiboDelayTimer = 0;
				}
								
				m_currRemainCheckFolderNum -= 4;
				if(m_currRemainCheckFolderNum > 0){

					CheckTimeline();
					CheckAtMeMessage();
					CheckCommentMeMessage();
					
					// this message called number is limited
					//
					// un-authorith
					CheckDirectMessage();
					
				}else{
					
					ResetCheckFolderLimit();
				}
			}else{
				
				synchronized(this){
					m_weiboDelayTimer++;
				}
			}
			
		}catch(Exception e){
			
			try{
				ResetCheckFolderLimit();
			}catch(Exception ex){
				m_mainMgr.m_logger.LogOut(GetAccountName() + " ResetCheckFolderLimit Error:" + e.getMessage());
			}
			
			
			m_mainMgr.m_logger.LogOut(GetAccountName() + " current limit:" + 
							m_currRemainCheckFolderNum + "/" + m_maxCheckFolderNum + 
							" Error:" + e.getMessage());
			
			// sleep for a while
			//
			Thread.sleep(2000);
		}
	}
	
	protected void PrepareRepushUnconfirmMsg_impl(fetchWeiboData _weiboList){
		
		boolean t_repush = true;
		
		long t_currTime = (new Date()).getTime();
		
		for(int i = 0;i < _weiboList.m_WeiboComfirm.size();i++){
			
			fetchWeibo confirmOne = _weiboList.m_WeiboComfirm.get(i);
			
			t_repush = true;
			for(fetchWeibo existOne : _weiboList.m_weiboList){
				
				if(confirmOne.GetId() == existOne.GetId()){
					t_repush = false;
				}
			}
			
			if(t_repush){
				
				final int t_maxTimes = 5;
				
				if(Math.abs(t_currTime - confirmOne.m_sendConfirmTime) >= (5 * 60 * 1000) ){
					
					if(confirmOne.m_sendConfirmCount++ < t_maxTimes){
						
						confirmOne.m_sendConfirmTime = t_currTime;
						
						_weiboList.m_weiboList.add(confirmOne);
						
						m_mainMgr.m_logger.LogOut("Weibo Account<" + GetAccountName() + 
								"> prepare Weibo<" + confirmOne.GetId() + "> send again...");
						
					}else{
						m_mainMgr.m_logger.LogOut("Weibo Account<" + GetAccountName() + 
								"> prepare Weibo<" + confirmOne.GetId() + "> sent " + t_maxTimes + " Times , give up.");
					}
					
					_weiboList.m_WeiboComfirm.remove(confirmOne);
					
					i--;
				}

			}
		}
		
	}
	
	/**
	 * push the message to client
	 */
	public synchronized void PushMsg(sendReceive _sendReceive)throws Exception{
		
		PrepareRepushUnconfirmMsg_impl(m_timeline);
		PrepareRepushUnconfirmMsg_impl(m_directMessage);
		PrepareRepushUnconfirmMsg_impl(m_atMeMessage);
		PrepareRepushUnconfirmMsg_impl(m_commentMessage);
		
		PushMsg_impl(m_timeline,_sendReceive);
		PushMsg_impl(m_directMessage,_sendReceive);
		PushMsg_impl(m_atMeMessage,_sendReceive);
		PushMsg_impl(m_commentMessage,_sendReceive);		
	}
	
	protected void PushMsg_impl(fetchWeiboData _weiboList,sendReceive _sendReceive)throws Exception{
		
		ByteArrayOutputStream t_output = new ByteArrayOutputStream();
		
		long t_currTime = (new Date()).getTime();
		
		if(_weiboList.m_counter == -1 // client send the refresh cmd to refresh or the first call
		|| _weiboList.m_weiboList.size() + _weiboList.m_counter >= _weiboList.m_sum){
			
			StringBuffer t_debugString = _weiboList.m_weiboList.isEmpty()?null:(new StringBuffer());
			
			while(!_weiboList.m_weiboList.isEmpty()){				
				
				// send the fetchWeibo
				//
				fetchWeibo t_weibo = (fetchWeibo)_weiboList.m_weiboList.get(_weiboList.m_weiboList.size() - 1); 
				
				t_output.write(msg_head.msgWeibo);
				t_weibo.OutputWeibo(t_output);
				
				m_mainMgr.SendData(t_output,false);
							
				// add the confirm list
				//
				_weiboList.m_weiboList.remove(t_weibo);
				_weiboList.m_WeiboComfirm.add(t_weibo);
				
				t_weibo.m_sendConfirmTime = t_currTime;
				
				// debug.out...
				t_debugString.append(GetAccountName()).append(" send Weibo<").append(t_weibo.GetId())
							.append("+").append(t_weibo.GetWeiboClass()).append(":").append(t_weibo.GetText())
							.append(">,wait confirm...");
				
				if(!_weiboList.m_weiboList.isEmpty()){
					t_debugString.append("\n\t\t");
				}
							
				t_output.reset();
			}
			
			if(t_debugString != null){
				m_mainMgr.m_logger.LogOut(t_debugString.toString());
			}	
			
			synchronized(this){
				_weiboList.m_counter = 0;
			}
			
		}else{
			synchronized(this){
				_weiboList.m_counter++;
			}
		}
		
	}
	
	public String GetHeadImageDir(){
		return m_headImageDir;
	}
		
	protected abstract void ResetCheckFolderLimit()throws Exception;
	protected abstract void CheckTimeline()throws Exception;
	protected abstract void CheckDirectMessage()throws Exception;
	protected abstract void CheckAtMeMessage()throws Exception;
	protected abstract void CheckCommentMeMessage()throws Exception;
	
	static byte[] sm_operateWeiboFailed = null;
	static{
		ByteArrayOutputStream t_os = new ByteArrayOutputStream();
		t_os.write(msg_head.msgNote);
		try{
			sendReceive.WriteString(t_os,"Weibo operating failed, please check server log for detail.",false);
		}catch(Exception e){}
		
		sm_operateWeiboFailed = t_os.toByteArray();
	}
	
	
	/**
	 * network package process function
	 * 
	 * @return boolean		: has been processed?
	 */
	public boolean ProcessNetworkPackage(byte[] _package)throws Exception{
		ByteArrayInputStream in = new ByteArrayInputStream(_package);
		
		boolean t_processed = false;
		
		final int t_head = in.read();
		switch(t_head){
			case msg_head.msgWeibo:
				t_processed = ProcessWeiboUpdate(in);
				break;
			case msg_head.msgWeiboConfirm:
				t_processed = ProcessWeiboConfirmed(in);
				break;
			case msg_head.msgWeiboHeadImage:
				t_processed = ProcessWeiboHeadImage(in);
				break;
			case msg_head.msgWeiboFavorite:
				t_processed = ProcessWeiboFavorite(in);
				break;
			case msg_head.msgWeiboFollowUser:
				t_processed =  ProcessWeiboFollowUser(in);
				break;
			case msg_head.msgWeiboRefresh:
				ProcessWeiboRefresh(in);
				break;
			case msg_head.msgWeiboDelete:
				t_processed =  ProcessWeiboDelete(in);
				break;
		}
		
		return t_processed;
	}
	
	protected synchronized void ProcessWeiboRefresh(ByteArrayInputStream in){
		
		m_timeline.m_counter 		= -1;
		m_directMessage.m_counter 	= -1;
		m_atMeMessage.m_counter 	= -1;
		m_commentMessage.m_counter	= -1;
		
		m_weiboDelayTimer			= -1;
	}
	
	static byte[] sm_followOkPrompt = null;
	static {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		os.write(msg_head.msgNote);
		try{
			sendReceive.WriteString(os,"follow user OK!",false);
			sm_followOkPrompt = os.toByteArray();
		}catch(Exception e){}
		
	}
	
	protected boolean ProcessWeiboDelete(ByteArrayInputStream in)throws Exception{
		int t_style = in.read();
				
		if(t_style == GetCurrWeiboStyle()){
			long t_long = sendReceive.ReadLong(in);
			DeleteWeibo(t_long);
			
			return true;
		}
		
		return false;
	}
	
	protected boolean ProcessWeiboUpdate(ByteArrayInputStream in)throws Exception{
		
		int t_style = in.read();
		int t_type = in.read();	
		
		String t_text = sendReceive.ReadString(in);
		t_text = t_text.replace("＠", "@");
		
		GPSInfo t_gpsInfo = null;
		
		try{
			
			switch(t_type){
			case 0:
				
				m_mainMgr.m_logger.LogOut(GetAccountName() + " update new weibo");
				
				if(in.read() != 0){
					t_gpsInfo = new GPSInfo();
					t_gpsInfo.InputData(in);
				}
				
				UpdataStatus(t_text,t_gpsInfo);
				
				break;
			case 1:
			case 2:
				
				int t_public_fw = in.read();
				
				if(t_style == GetCurrWeiboStyle() || t_public_fw == 1){
					
					long t_commentWeiboId = sendReceive.ReadLong(in);
					
					if(in.read() != 0){
						t_gpsInfo = new GPSInfo();
						t_gpsInfo.InputData(in);
					}
					
					boolean t_updateTimeline = (in.read() == 1);
					
					m_mainMgr.m_logger.LogOut(GetAccountName() + " comment/reply weibo " + t_commentWeiboId);
					
					UpdataComment(t_style,t_text,t_commentWeiboId,t_gpsInfo,t_updateTimeline);
					
					// public the forward commect/forward
					//
					return t_public_fw != 1;
				}
				
				break;
				
			}
			
		}catch(Exception e){
			m_mainMgr.m_logger.LogOut(GetAccountName() + " Exception:" + e.getMessage());
			m_mainMgr.m_logger.PrinterException(e);
			
			m_mainMgr.SendData(sm_operateWeiboFailed, false);
		}
		
		return false;
	}

	protected abstract int GetCurrWeiboStyle(); 
	protected abstract void UpdataStatus(String _text,GPSInfo _info)throws Exception;
	protected abstract void UpdataComment(int _style,String _text,long _commentWeiboId,
											GPSInfo _info,boolean _updateTimeline)throws Exception;
	
	protected abstract void FavoriteWeibo(long _id)throws Exception;
	protected abstract void FollowUser(long _id)throws Exception;
	protected abstract void DeleteWeibo(long _id)throws Exception;
	
	protected boolean ProcessWeiboConfirmed(ByteArrayInputStream in)throws Exception{
		
		if(in.read() == GetCurrWeiboStyle()){

			final long t_id = sendReceive.ReadLong(in);

			if(ProcessWeiboConfirmed_imple(m_timeline,t_id)){
				return true;
			}
			if(ProcessWeiboConfirmed_imple(m_directMessage,t_id)){
				return true;
			}
			if(ProcessWeiboConfirmed_imple(m_atMeMessage,t_id)){
				return true;
			}
			if(ProcessWeiboConfirmed_imple(m_commentMessage,t_id)){
				return true;
			}
		}	
		
		return false;
	}
	
	protected boolean ProcessWeiboConfirmed_imple(fetchWeiboData _weiboList,final long _id){
		boolean t_found = false;
		for(fetchWeibo confirmOne : _weiboList.m_WeiboComfirm){
			
			if(confirmOne.GetId() == _id){
				t_found = true;
				_weiboList.m_WeiboComfirm.remove(confirmOne);
				
				m_mainMgr.m_logger.LogOut("Weibo Account<" + GetAccountName() + "> confirm Weibo<" + _id + ">");
				break;
			}
		}
		
		return t_found;
	}
	
	protected boolean ProcessWeiboHeadImage(ByteArrayInputStream in)throws Exception{
		
		if(in.read() == GetCurrWeiboStyle()){
			
			final long t_id = sendReceive.ReadLong(in);
			
			File t_file = new File(GetHeadImageFilename(t_id));
			if(t_file.exists()){
				ByteArrayOutputStream t_os = new ByteArrayOutputStream();
				t_os.write(msg_head.msgWeiboHeadImage);
				t_os.write(GetCurrWeiboStyle());
				sendReceive.WriteLong(t_os,t_id);
				
				BufferedInputStream t_read = new BufferedInputStream(new FileInputStream(t_file));
				int size = 0;
		        while((size = t_read.read(m_headImageBuffer))!= -1){
		        	t_os.write(m_headImageBuffer,0,size);
		        }	        
		        t_read.close();
		        
		        m_mainMgr.SendData(t_os, false);
		        
		        m_mainMgr.m_logger.LogOut(GetAccountName() + " send weibo head image " + t_id);
			}
			
			return true;
		}
		
		return false;
	}
	
	protected boolean ProcessWeiboFavorite(ByteArrayInputStream in)throws Exception{
		
		if(in.read() == GetCurrWeiboStyle()){
			FavoriteWeibo(sendReceive.ReadLong(in));
			return true;
		}
		
		return false;
	}
	protected boolean ProcessWeiboFollowUser(ByteArrayInputStream in)throws Exception{
		
		int t_style = in.read();
		if(t_style == GetCurrWeiboStyle()){
			long t_id = sendReceive.ReadLong(in);
			
			m_mainMgr.m_logger.LogOut(GetAccountName() + " Follow User " + t_id);
			
			FollowUser(t_id);
			
			m_mainMgr.SendData(sm_followOkPrompt, false);
			
			return true;
		}
		
		return false;
	}
	
	protected int StoreHeadImage(URL _url,long _id){
		
		final String t_filename 		= GetHeadImageFilename(_id);
		
		int t_hashCode = -1;
		int size = 0;
		
		try{
			
			File t_file = new File(t_filename);
			
			if(!t_file.exists()){
				
				// local file is NOT exist then download from the URL
				//
				URL t_url = _url;
				
		        URLConnection t_connect = t_url.openConnection();
		        BufferedInputStream t_read = new   BufferedInputStream(t_connect.getInputStream()); 
		   		try{
		   		  FileOutputStream fos = new FileOutputStream(t_file);
			        try{
				        while((size = t_read.read(m_headImageBuffer))!= -1){
				        	fos.write(m_headImageBuffer,0,size);
				        }
			        }finally{
			        	fos.flush();
				        fos.close();
				        fos = null;
			        }
		   		}finally{
		   			t_read.close();
		   			t_read = null;
		   		}
		   		
		        // scale the image...
		        //
		        BufferedImage bsrc = ImageIO.read(t_file);
		        BufferedImage bdest = new BufferedImage(fetchWeibo.fsm_headImageSize,fetchWeibo.fsm_headImageSize, BufferedImage.TYPE_INT_RGB);
		        Graphics2D g = bdest.createGraphics();
		        AffineTransform at = AffineTransform.getScaleInstance((double)fetchWeibo.fsm_headImageSize/bsrc.getWidth(),
		        														(double)fetchWeibo.fsm_headImageSize/bsrc.getHeight());
		        g.drawRenderedImage(bsrc,at);
		        ImageIO.write(bdest,"PNG",t_file);
			}
			
			byte[] t_byte = new byte[(int)t_file.length()];
			
			FileInputStream t_fileIn = new FileInputStream(t_file);
			try{
				sendReceive.ForceReadByte(t_fileIn, t_byte, t_byte.length);	        
		        
		        t_hashCode = t_byte.length;

			}finally{
				t_fileIn.close();
				t_fileIn = null;
			}
			
	        
		}catch(Exception e){
			m_mainMgr.m_logger.PrinterException(e);
		}
		
		return t_hashCode;
	}
	
	
}
