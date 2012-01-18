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

import local.yblocalResource;
import net.rim.device.api.ui.component.CheckboxField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ObjectChoiceField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.MainScreen;

import com.yuchting.yuchberry.client.recvMain;

public class IMOptionScreen extends MainScreen {
	
	public MainIMScreen		m_mainScreen;
	
	CheckboxField			m_enableChatChecked = null;
	CheckboxField			m_enableChatState	= null;
	
	CheckboxField			m_imReturnSend		= null;	
	CheckboxField			m_imChatScreenReceiveReturn = null;
	
	CheckboxField			m_imChatScreenReverse	= null;
	CheckboxField			m_imStoreImageVoice		= null;
	CheckboxField			m_imPopupPrompt		= null;
	CheckboxField			m_imRenotifyPrompt	= null;
	ObjectChoiceField		m_imChatMsgHistory	= null;
	ObjectChoiceField		m_imSendImageQuality	= null;
	
	public IMOptionScreen(MainIMScreen _mainScreen){
		m_mainScreen = _mainScreen;
		
		//@{ IM operating
		LabelField t_label = new LabelField(recvMain.sm_local.getString(yblocalResource.IM_OPTION_OP_LABEL));
		t_label.setFont(MainIMScreen.fsm_boldFont);
		add(t_label);
		
		m_enableChatChecked = new CheckboxField(recvMain.sm_local.getString(yblocalResource.IM_OPTION_ENABLE_CHECKED),
											m_mainScreen.m_mainApp.m_enableChatChecked);
		add(m_enableChatChecked);
		
		m_enableChatState	= new CheckboxField(recvMain.sm_local.getString(yblocalResource.IM_OPTION_ENABLE_CHAT_STATE),
											m_mainScreen.m_mainApp.m_enableChatState);
		add(m_enableChatState);
		
		m_imReturnSend		= new CheckboxField(recvMain.sm_local.getString(yblocalResource.IM_OPTION_RETURN_SEND),
											m_mainScreen.m_mainApp.m_imReturnSend);
		add(m_imReturnSend);
		
		m_imChatScreenReceiveReturn	= new CheckboxField(recvMain.sm_local.getString(yblocalResource.IM_OPTION_RETURN_RECV),
											m_mainScreen.m_mainApp.m_imChatScreenReceiveReturn);
		add(m_imChatScreenReceiveReturn);
		
		m_imStoreImageVoice = new CheckboxField(recvMain.sm_local.getString(yblocalResource.IM_OPTION_STORE_IMAGE_VOICE),
				m_mainScreen.m_mainApp.m_imStoreImageVoice);
		
		add(m_imStoreImageVoice);
			
		//@}
		
		add(new SeparatorField());
		
		//@{ IM display
		t_label = new LabelField(recvMain.sm_local.getString(yblocalResource.IM_OPTION_DISPLAY_LABEL));
		t_label.setFont(MainIMScreen.fsm_boldFont);
		add(t_label);
		
		m_imPopupPrompt		= new CheckboxField(recvMain.sm_local.getString(yblocalResource.IM_OPTION_POPUP_PROMPT),
									m_mainScreen.m_mainApp.m_imPopupPrompt);
		add(m_imPopupPrompt);
		
		m_imRenotifyPrompt	= new CheckboxField(recvMain.sm_local.getString(yblocalResource.IM_OPTION_RENOTIFY_PROMPT),
									m_mainScreen.m_mainApp.m_imRenotifyPrompt);
		add(m_imRenotifyPrompt);
		
		m_imChatScreenReverse = new CheckboxField(recvMain.sm_local.getString(yblocalResource.IM_OPTION_CHAT_SCREEN_REV),
									m_mainScreen.m_mainApp.m_imChatScreenReverse);
		add(m_imChatScreenReverse);
		
		m_imChatMsgHistory		= new ObjectChoiceField(recvMain.sm_local.getString(yblocalResource.IM_OPTION_CHAT_HISTORY_NUM),
									recvMain.fsm_imChatMsgHistoryList,m_mainScreen.m_mainApp.m_imChatMsgHistory);
		add(m_imChatMsgHistory);
		
		m_imSendImageQuality	= new ObjectChoiceField(recvMain.sm_local.getString(yblocalResource.IM_OPTION_IMAGE_QUALITY),
										recvMain.fsm_imUploadImageSizeList,m_mainScreen.m_mainApp.m_imSendImageQuality);
		add(m_imSendImageQuality);
		//@}
		
		
		setTitle(recvMain.sm_local.getString(yblocalResource.IM_OPTION_SCREEN));
	}

	protected boolean onSave(){
		boolean t_ret = super.onSave();
		if(t_ret){
			 m_mainScreen.m_mainApp.m_enableChatChecked = m_enableChatChecked.getChecked();
			 m_mainScreen.m_mainApp.m_enableChatState 	= m_enableChatState.getChecked();
			 m_mainScreen.m_mainApp.m_imReturnSend 	= m_imReturnSend.getChecked();
			 
			 m_mainScreen.m_mainApp.m_imChatScreenReceiveReturn = m_imChatScreenReceiveReturn.getChecked();
			 m_mainScreen.m_mainApp.m_imPopupPrompt = m_imPopupPrompt.getChecked();
			 
			 boolean t_formerReverse  = m_mainScreen.m_mainApp.m_imChatScreenReverse;
			 m_mainScreen.m_mainApp.m_imChatScreenReverse = m_imChatScreenReverse.getChecked();
			 
			 m_mainScreen.m_mainApp.m_imChatMsgHistory = m_imChatMsgHistory.getSelectedIndex();
			 m_mainScreen.m_mainApp.m_imSendImageQuality = m_imSendImageQuality.getSelectedIndex();
			 m_mainScreen.m_mainApp.WriteReadIni(false);
			 
			 m_mainScreen.m_mainApp.m_imStoreImageVoice = m_imStoreImageVoice.getChecked();
			 m_mainScreen.m_mainApp.m_imRenotifyPrompt = m_imRenotifyPrompt.getChecked();
			 
			 if(t_formerReverse != m_mainScreen.m_mainApp.m_imChatScreenReverse){
				 m_mainScreen.m_chatScreen.m_middleMgr.readdControl();
			 }
		}
		return t_ret;
	}
	 
	public void close(){
		m_mainScreen.m_optionScreen = null;
		super.close();
	}
}
