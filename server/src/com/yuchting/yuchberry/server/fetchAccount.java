package com.yuchting.yuchberry.server;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Date;

import javax.imageio.ImageIO;

import org.dom4j.Element;

/**
 * fetch account abstract to push email or ...
 * @author yuch
 *
 */
abstract public class fetchAccount {
	
	protected fetchMgr			m_mainMgr = null;
	
	public fetchAccount(fetchMgr _mainMgr){
		m_mainMgr = _mainMgr;
	}
	
	/**
	 * initialize the account to sign in 
	 * 
	 * @param _elem		: the xml element for read the attribute 
	 */
	abstract public void InitAccount(Element _elem)throws Exception;
	
	/**
	 * get the account name (Email address)
	 */
	abstract public String GetAccountName();
	
	/**
	 * check the folder to find the news to push
	 */
	abstract public void CheckFolder()throws Exception;
	
	/**
	 * get the directory prefix of this account 
	 */
	abstract public String GetAccountPrefix();
	
	/**
	 * reset the session for connection
	 * 
	 * @param _fullTest		: whether test the full configure( SMTP for email)
	 */
	abstract public void ResetSession(boolean _fullTest)throws Exception;
	
	/**
	 * destroy the session connection
	 */
	abstract public void DestroySession();
	
	/**
	 * network package process function
	 * 
	 * @return boolean		: has been processed?
	 */
	abstract public boolean ProcessNetworkPackage(byte[] _package)throws Exception;
		
	/**
	 * push the message to client
	 */
	abstract public void PushMsg(sendReceive _sendReceive)throws Exception;
	
	/**
	 * client is connected to server
	 */
	public void ClientConnected(){}
	
	/**
	 * client is disconnected from the server
	 */
	public void ClientDisconnected(){}
	
	protected static byte[] sm_cryptPasswordKeyError = null;
	static{
		ByteArrayOutputStream t_os = new ByteArrayOutputStream();
		t_os.write(msg_head.msgNote);
		try{
			sendReceive.WriteString(t_os,"crypt password key error!(加密算子错误)",false);
		}catch(Exception e){}
		
		sm_cryptPasswordKeyError = t_os.toByteArray();
	}
	
	public String decryptPassword(String _cryptPassword,String _password)throws Exception{
		
		if(!_cryptPassword.isEmpty() && _password.isEmpty()){		
			
			if(m_mainMgr.GetPasswordKey().isEmpty()){
				m_mainMgr.m_logger.LogOut(GetAccountName() + " PasswordKey is Empty, wait for client send PasswordKey.");
				
				return null;
				
			}else{
							
				try{
					cryptPassword t_crypt = new cryptPassword(m_mainMgr.GetPasswordKey());
					_password = t_crypt.decrypt(_cryptPassword);
					
				}catch(Exception e){

					m_mainMgr.SendData(sm_cryptPasswordKeyError, false);
					m_mainMgr.m_logger.LogOut(GetAccountName() + "crypt password error,please check the PasswordKey of client.");
					
					return null;
				}
				
				m_mainMgr.m_logger.LogOut(GetAccountName() +  "used crpty Password key to decode.");
			}
		}
		
		return _password;
	}
			
	/**
	 * read String attribute from xml
	 */
	static public String ReadStringAttr(Element _elem,String _attrName)throws Exception{
		String attr = _elem.attributeValue(_attrName);
		if(attr == null){
			attr = "0";
		}
		
		return attr;
	}
	
	/**
	 * read boolean attribute from xml
	 */
	static public boolean ReadBooleanAttr(Element _elem,String _attrName)throws Exception{
		return Integer.valueOf(ReadStringAttr(_elem,_attrName)).intValue() == 1;
	}
	
	/**
	 * read the integer value from xml
	 */
	static public int ReadIntegerAttr(Element _elem,String _attrName)throws Exception{
		return Integer.valueOf(ReadStringAttr(_elem,_attrName)).intValue();
	}
	
	static public String getImageType(byte[] _data){
		
		String type = null;
		
		byte b0 = _data[0];
		byte b1 = _data[1];
		byte b2 = _data[2];
		byte b3 = _data[3];
		byte b6 = _data[6];
		byte b7 = _data[7];
		byte b8 = _data[8];
		byte b9 = _data[9];
		
		// GIF
		if (b0 == (byte) 'G' && b1 == (byte) 'I' && b2 == (byte) 'F'){
			type = "GIF";
		}else if (b1 == (byte) 'P' && b2 == (byte) 'N' && b3 == (byte) 'G'){
			type = "PNG";
		}else if (b6 == (byte) 'J' && b7 == (byte) 'F' && b8 == (byte) 'I' && b9 == (byte) 'F'){
			type = "JPEG";
		}else{
			type = "JPEG";
		}
		
		return type;
	}

	static public void writeHeadImage(BufferedImage bsrc,String _convertType,
										File _headImage_l,File _headImage)throws Exception {
		
		long t_currentTime = (new Date()).getTime();
		
		if(bsrc.getWidth() != fetchWeibo.fsm_headImageSize_l){
        	// store the large file
        	//
        	BufferedImage bdest = new BufferedImage(fetchWeibo.fsm_headImageSize_l,fetchWeibo.fsm_headImageSize_l, BufferedImage.TYPE_INT_RGB);
	        Graphics2D g = bdest.createGraphics();
	        AffineTransform at = AffineTransform.getScaleInstance((double)fetchWeibo.fsm_headImageSize_l/bsrc.getWidth(),
	        														(double)fetchWeibo.fsm_headImageSize_l/bsrc.getHeight());
	        g.drawRenderedImage(bsrc,at);
	        ImageIO.write(bdest,_convertType,_headImage_l);
        }
  
    	// store to a small file
        //
        BufferedImage bdest = new BufferedImage(fetchWeibo.fsm_headImageSize,fetchWeibo.fsm_headImageSize, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bdest.createGraphics();
        AffineTransform at = AffineTransform.getScaleInstance((double)fetchWeibo.fsm_headImageSize/bsrc.getWidth(),
        														(double)fetchWeibo.fsm_headImageSize/bsrc.getHeight());
        g.drawRenderedImage(bsrc,at);		       
        ImageIO.write(bdest,_convertType,_headImage);
							
        _headImage_l.setLastModified(t_currentTime);
	}
	
	
}
