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
package com.yuchting.yuchberry.client.ui;


//BasicEditField for 4.2os
public class BubbleTextField extends WeiboTextField{

	int m_textWidth = 0;
	
	public BubbleTextField(){
		super(0,0);
	}
	
	public void setText(String _text){
		super.setText(_text);
		this.layout(m_textWidth,this.getHeight());
	}
	
	public void setPreferredWidth(int _width){
		m_textWidth = _width;
	}
}
