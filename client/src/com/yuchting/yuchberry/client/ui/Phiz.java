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

public class Phiz extends ImageButton{
	public final static int		fsm_phizSize = 22;
	
	public Phiz(ImageUnit _image,ImageSets _imageSets){
        super( "",_image,_image,_imageSets,0);
    }
	
	public void focusPaint(Graphics g,boolean focus){
				
		if(focus){
			int t_color = g.getColor();
			try{
				g.setColor(0x4694ea);
				g.fillRect(0,0,m_image.getWidth(),m_image.getHeight());
			}finally{
				g.setColor(t_color);
			}
		}
		
		super.focusPaint(g,focus);
	}
	
	public String getPhizName(){
		return m_image.getName();
	}
}
