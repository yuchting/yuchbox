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


public class yuchEmail {
	

	public String m_emailAddr = "";
	public String m_signinName = "";
	
	public String m_username = "";

	public String m_password = "";
	

	public yuchbber m_yuchbber; 
	

	public boolean m_fullnameSignIn = false;
	

	public String m_protocol = "imaps";
	

	public String m_host = "";
	

	public int m_port = 993;
		

	public String m_host_send = "";
	

	public int m_port_send = 587;
	
	public boolean m_appendHTML = false;
	
	
	public void OuputXMLData(final StringBuffer _output){
		
		if(m_username == null){
			m_username = "";
		}
		
		_output.append("\t<email ").append("account=\"").append(m_emailAddr).
									append("\" signin=\"").append(m_signinName).
									append("\" name=\"").append(m_username).
									append("\" pass=\"").append(mainFrame.prepareXmlAttr(m_password)).
									append("\" full=\"").append(m_fullnameSignIn?1:0).
									append("\" protocal=\"").append(m_protocol).
									append("\" host=\"").append(m_host).
									append("\" port=\"").append(m_port).
									append("\" hosts=\"").append(m_host_send).
									append("\" ports=\"").append(m_port_send).
									append("\" appHTML=\"").append(m_appendHTML?1:0).
									append("\" />\n");
	}
	
	public void InputXMLData(final Element _elem)throws Exception{
				
		m_emailAddr		= fetchAccount.ReadStringAttr(_elem, "account");
		m_signinName	= fetchAccount.ReadStringAttr(_elem, "signin");
		m_username		= fetchAccount.ReadStringAttr(_elem,"name");
		m_password		= fetchAccount.ReadStringAttr(_elem, "pass");
		m_fullnameSignIn= fetchAccount.ReadBooleanAttr(_elem, "full");
		m_protocol		= fetchAccount.ReadStringAttr(_elem, "protocal");
		
		m_host			= fetchAccount.ReadStringAttr(_elem, "host");
		m_port			= fetchAccount.ReadIntegerAttr(_elem, "port");
		
		m_host_send		= fetchAccount.ReadStringAttr(_elem, "hosts");
		m_port_send		= fetchAccount.ReadIntegerAttr(_elem, "ports");
		
		m_appendHTML	= fetchAccount.ReadBooleanAttr(_elem, "appHTML");
	}
	
	public String toString(){
		return "Email <" + m_emailAddr + ">"; 
	}
	
}
