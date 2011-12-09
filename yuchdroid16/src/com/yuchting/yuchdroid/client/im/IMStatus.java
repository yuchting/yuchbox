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
package com.yuchting.yuchdroid.client.im;

import java.io.InputStream;
import java.io.OutputStream;

import com.yuchting.yuchdroid.client.sendReceive;

public final class IMStatus{
	
	static public IMStatus		sm_currUseStatus = null;
	
	public int 	m_presence = fetchChatRoster.PRESENCE_AVAIL;
	public String	m_status		= "";
	
	public IMStatus(){}
	public IMStatus(int _presence,String _status){
		m_presence	= _presence;
		m_status	= _status;
	}
	
	public void Import(InputStream in)throws Exception{
		m_presence = in.read();
		m_status	= sendReceive.ReadString(in);
	}
	
	public void Ouput(OutputStream os)throws Exception{
		os.write(m_presence);
		sendReceive.WriteString(os,m_status);
	}
	

	public String toString(){
		return m_status;
	}
}

