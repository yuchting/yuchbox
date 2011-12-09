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

import net.rim.device.api.ui.FieldChangeListener;

import com.yuchting.yuchberry.client.recvMain;
import com.yuchting.yuchberry.client.ui.ImageButton;

public class ChatVoiceField extends ImageButton {
	fetchChatMsg m_msg = null;
	IChatFieldOpen m_open = null;
	
	public ChatVoiceField(){
		super("",recvMain.sm_weiboUIImage.getImageUnit("voice_button"),
										recvMain.sm_weiboUIImage.getImageUnit("voice_button_focus"),recvMain.sm_weiboUIImage);
	}
	
	public void init(fetchChatMsg msg,IChatFieldOpen _open){
		m_msg 	= msg;
		m_open = _open;
	}
	
	protected void fieldChangeNotify(int context){
		super.fieldChangeNotify(context);
		if(context != FieldChangeListener.PROGRAMMATIC){
			m_open.open(m_msg);
		}
	}
	
	public int getPreferredWidth(){
		return getImageWidth();
	}
	
	public int getPreferredHeight(){
		return getImageHeight();
	}
}
