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

import net.rim.device.api.ui.Graphics;

public class ButtonSegImage {
	ImageUnit	m_left;
	ImageUnit	m_middle;
	ImageUnit	m_right;
	
	ImageSets	m_imageSets;
	
	public ButtonSegImage(ImageUnit _left,ImageUnit _mid,ImageUnit _right,ImageSets _imageSets){
		m_left = _left;
		m_middle = _mid;
		m_right = _right;
		
		m_imageSets = _imageSets;
	}
	
	public void draw(Graphics _g,int _x,int _y,int _width){
		m_imageSets.drawImage(_g, m_left, _x, _y);
		m_imageSets.drawBitmapLine(_g, m_middle, _x + m_left.getWidth(), _y, _width - m_left.getWidth() - m_right.getWidth());
		m_imageSets.drawImage(_g,m_right,_x + (_width - m_right.getWidth()),_y);
	}
	
	public int getImageHeight(){
		return m_left.getHeight();
	}
}
