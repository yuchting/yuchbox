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

import java.io.InputStream;
import java.io.OutputStream;

public final class GPSInfo{
	public double m_longitude 				= 0;
	public double m_latitude				= 0;
	public float	 m_altitude				= 0;
	public float	 m_speed				= 0;
	public float	 m_heading				= 0;
	
	public void InputData(InputStream in)throws Exception{
		m_longitude = sendReceive.ReadDouble(in);
		m_latitude 	= sendReceive.ReadDouble(in);
		
		m_altitude	= sendReceive.ReadFloat(in);
		m_speed		= sendReceive.ReadFloat(in);
		m_heading	= sendReceive.ReadFloat(in);		
	}
	
	public void OutputData(OutputStream os)throws Exception{
		sendReceive.WriteDouble(os,m_longitude);
		sendReceive.WriteDouble(os,m_latitude);
		
		sendReceive.WriteFloat(os,m_altitude);
		sendReceive.WriteFloat(os,m_speed);
		sendReceive.WriteFloat(os,m_heading);
	}
}
