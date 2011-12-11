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

import com.yuchting.yuchberry.server.fetchMgr;

public class yuchIM {

	public String m_typeName = "gtalk";
	public String m_accoutName = "";
	public String m_password = "";
	
	public void OuputXMLData(final StringBuffer _output){
		_output.append("\t<IMAccount ").append("type=\"").append(m_typeName).
				append("\" account=\"").append(m_accoutName).
				append("\" pass=\"").append(mainFrame.prepareXmlAttr(m_password)).
				append("\" />\n");
	}
	
	public void InputXMLData(final Element _elem)throws Exception{
		m_typeName 		= yuchbber.ReadStringAttr(_elem, "type");
		m_accoutName	= yuchbber.ReadStringAttr(_elem, "account");
		m_password		= yuchbber.ReadStringAttr(_elem, "pass");
	}
	
	public String toString(){
		return m_typeName + " IM<" + m_accoutName + ">"; 
	}
}
