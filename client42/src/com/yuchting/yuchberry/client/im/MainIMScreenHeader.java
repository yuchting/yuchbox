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
package com.yuchting.yuchberry.client.im;

import net.rim.device.api.ui.Graphics;

import com.yuchting.yuchberry.client.recvMain;
import com.yuchting.yuchberry.client.ui.SliderHeader;

public class MainIMScreenHeader extends SliderHeader{

	public final static int STATE_HISTORY_CHAT = 0;
	public final static int STATE_ROSTER_LIST = 1;
	public final static int STATE_STATUS_LIST = 2;
			
	private final static String[] fsm_stateBitmapString = 
	{
		"history_chat",
		"friend_list",
		"own_status",
	};	
	
	MainIMScreen	m_parentScreen = null;
	public MainIMScreenHeader(MainIMScreen _parentScreen){
		super(_parentScreen.m_mainApp,fsm_stateBitmapString);
		
		m_parentScreen = _parentScreen;
	}
	
	protected void paint( Graphics g ){
		super.paint(g);
		
		if(m_parentScreen.m_hasNewChatMsg){
			
			// draw a new message sign
			//
			recvMain.sm_weiboUIImage.drawImage(g,GetBBerSignBitmap(),0,fsm_stateBitmapTop);
		}
    }
}
