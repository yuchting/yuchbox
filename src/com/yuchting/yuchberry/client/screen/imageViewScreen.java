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

import net.rim.device.api.math.Fixed32;
import net.rim.device.api.system.Display;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.ui.component.BitmapField;

public class imageViewScreen extends fileViewScreen{

	BitmapField	m_image	= null;
	
	public imageViewScreen(String _name)throws Exception{
		
		super(_name,true);
		
		init();
	}
	
	public imageViewScreen(byte[] _buffer)throws Exception{
		
		super(_buffer);
		
		init();
	}
	
	private void init(){
		m_image = new BitmapField();
		EncodedImage image = EncodedImage.createEncodedImage(sm_fileContain, 0, sm_fileLength);
		
		final int scale = Fixed32.div(Fixed32.toFP(image.getWidth()),Fixed32.toFP(Display.getWidth()));
		
		m_image.setImage(image.scaleImage32(scale,scale));
		add(m_image);
	}
}
