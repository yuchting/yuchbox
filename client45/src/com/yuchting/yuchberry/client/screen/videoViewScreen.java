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

import javax.microedition.media.Manager;
import javax.microedition.media.Player;
import javax.microedition.media.control.VideoControl;

import com.yuchting.yuchberry.client.recvMain;

public class videoViewScreen extends fileViewScreen{
	Player 		m_player			= null;
	VideoControl m_videoControl		= null;
	
	
	public videoViewScreen(String _filename,recvMain _mainApp) throws Exception{
		super(_filename,_mainApp,false);
		
		m_player = Manager.createPlayer(_filename);
		m_player.realize();
		
        //Create a new VideoControl.
        m_videoControl = (VideoControl)m_player.getControl("VideoControl");
        
        //Initialize the video mode using a Field.
        m_videoControl.initDisplayMode(VideoControl.USE_GUI_PRIMITIVE, "net.rim.device.api.ui.Field");

        //Set the video control to be visible.
        m_videoControl.setVisible(true);
        
        m_player.start();
	}
	
	public boolean onClose(){
		m_player.close();
		m_videoControl.setVisible(false);
		
		return super.onClose();
	}
}
