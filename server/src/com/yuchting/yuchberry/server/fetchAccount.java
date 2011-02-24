package com.yuchting.yuchberry.server;

import java.io.InputStream;

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
	 * prepare the re-push unconfirm msg
	 */
	abstract public void PrepareRepushUnconfirmMsg(long _currTime);
	
	/**
	 * push the message to client
	 */
	abstract public void PushMsg(sendReceive _sendReceive)throws Exception;
	
	
	
		
	/**
	 * read String attribute from xml
	 */
	static public String ReadStringAttr(Element _elem,String _attrName)throws Exception{
		String attr = _elem.attributeValue(_attrName);
		if(attr == null){
			throw new Exception("Element without attribute:" + _attrName);
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
	
	
}
