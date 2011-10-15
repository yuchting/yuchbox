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
