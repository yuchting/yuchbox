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
package com.yuchting.yuchberry.client.screen;

import net.rim.device.api.ui.component.RichTextField;

public class textViewScreen extends fileViewScreen{

	RichTextField m_editText	= null;
	
	public textViewScreen(String _filename)throws Exception{
		
		super(_filename,true);
		
		final int t_maxTextLen = 5 * 1024;
		
		byte[] t_tmpContain = null;
		
		if(sm_fileLength > t_maxTextLen){
			String t_appendPrompt = new String("\n\n\n remain contain... \n\n\n");
			
			t_tmpContain = new byte[t_maxTextLen];
			
			System.arraycopy(sm_fileContain, 0, t_tmpContain, 0, t_maxTextLen - t_appendPrompt.length());
			System.arraycopy(t_appendPrompt.getBytes(),0,t_tmpContain,t_maxTextLen - t_appendPrompt.length(),t_appendPrompt.length());
			
		}else{
			t_tmpContain = sm_fileContain;
		}
		
		try{
			// if the UTF-8 decode sytem is NOT present in current system
			// will throw the exception
			//
			m_editText = new RichTextField(new String(t_tmpContain,"UTF-8"));
		}catch(Exception e_){
			m_editText = new RichTextField(new String(t_tmpContain));
		}
		
		
		add(m_editText);
		
	}
	
}
