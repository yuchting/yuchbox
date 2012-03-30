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

import java.io.ByteArrayInputStream;

import javax.microedition.media.Player;

public class audioViewScreen extends fileViewScreen{
	
	Player 		m_player		 = null;
	
	public audioViewScreen(String _filename)throws Exception{
		super(_filename,true);
	
		String t_type = null;
		if(_filename.indexOf(".mp3") != -1){
			t_type = "audio/mpeg";
		}else if(_filename.indexOf(".wav") != -1){
			t_type = "audio/wav";
		}else if(_filename.indexOf(".mid") != -1){
			t_type = "audio/midi";
		}else{
			t_type = "audio/basic";
		}
				
		m_player = javax.microedition.media.Manager.createPlayer(new ByteArrayInputStream(m_fileContain),t_type);
		
		m_player.realize();
		m_player.prefetch();
		m_player.start();
	}
	
	public boolean onClose(){
		m_player.close();
		
		return super.onClose();		
	}
}
