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

import org.dom4j.Element;

import com.yuchting.yuchberry.server.fetchAccount;

public class yuchWeibo {


	public yuchbber m_yuchbber; 

	public String m_typeName = "sina";
	

	public String m_accoutName = "";
	

	public String m_accessToken = "";
	

	public String m_secretToken = "";	
	

	public int m_timelineSum = 20;
	

	public int m_directMsgSum = 2;
	

	public int m_atMeSum = 2;
	

	public int m_commentMeSum = 2;
	
	public void OuputXMLData(final StringBuffer _output){
		_output.append("\t<WeiboAccount ").append("type=\"").append(m_typeName).
				append("\" account=\"").append(m_accoutName).
				append("\" accessToken=\"").append(m_accessToken).
				append("\" secretToken=\"").append(m_secretToken).
				append("\" timelineSum=\"").append(m_timelineSum).
				append("\" directMessageSum=\"").append(m_directMsgSum).
				append("\" atMeSum=\"").append(m_atMeSum).
				append("\" commentSum=\"").append(m_atMeSum).
				append("\" />\n");
	}
	
	public void InputXMLData(final Element _elem)throws Exception{
		m_typeName 		= fetchAccount.ReadStringAttr(_elem, "type");
		m_accoutName	= fetchAccount.ReadStringAttr(_elem, "account");
		m_accessToken	= fetchAccount.ReadStringAttr(_elem, "accessToken");
		m_secretToken	= fetchAccount.ReadStringAttr(_elem, "secretToken");
		
		m_timelineSum	= fetchAccount.ReadIntegerAttr(_elem, "timelineSum");
		m_directMsgSum	= fetchAccount.ReadIntegerAttr(_elem, "directMessageSum");
		m_atMeSum		= fetchAccount.ReadIntegerAttr(_elem, "atMeSum");
		m_commentMeSum	= fetchAccount.ReadIntegerAttr(_elem, "commentSum");
	}
	
	public String toString(){
		return m_typeName + " Weibo<" + m_accoutName + ">"; 
	}
	
}
