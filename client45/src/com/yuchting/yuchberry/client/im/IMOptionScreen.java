package com.yuchting.yuchberry.client.im;

import local.localResource;
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
	
	CheckboxField			m_imPopupPrompt		= null;
	ObjectChoiceField		m_imChatMsgHistory	= null;
	
	public IMOptionScreen(MainIMScreen _mainScreen){
		m_mainScreen = _mainScreen;
		
		//@{ IM operating
		LabelField t_label = new LabelField(recvMain.sm_local.getString(localResource.IM_OPTION_OP_LABEL));
		t_label.setFont(MainIMScreen.fsm_boldFont);
		add(t_label);
		
		m_enableChatChecked = new CheckboxField(recvMain.sm_local.getString(localResource.IM_OPTION_ENABLE_CHECKED),
											m_mainScreen.m_mainApp.m_enableChatChecked);
		add(m_enableChatChecked);
		
		m_enableChatState	= new CheckboxField(recvMain.sm_local.getString(localResource.IM_OPTION_ENABLE_CHAT_STATE),
											m_mainScreen.m_mainApp.m_enableChatState);
		add(m_enableChatState);
		
		m_imReturnSend		= new CheckboxField(recvMain.sm_local.getString(localResource.IM_OPTION_RETURN_SEND),
											m_mainScreen.m_mainApp.m_imReturnSend);
		add(m_imReturnSend);
		
		m_imChatScreenReceiveReturn	= new CheckboxField(recvMain.sm_local.getString(localResource.IM_OPTION_RETURN_RECV),
											m_mainScreen.m_mainApp.m_imChatScreenReceiveReturn);
		add(m_imChatScreenReceiveReturn);
			
		//@}
		
		add(new SeparatorField());
		
		//@{ IM display
		t_label = new LabelField(recvMain.sm_local.getString(localResource.IM_OPTION_DISPLAY_LABEL));
		t_label.setFont(MainIMScreen.fsm_boldFont);
		add(t_label);
		
		m_imPopupPrompt		= new CheckboxField(recvMain.sm_local.getString(localResource.IM_OPTION_POPUP_PROMPT),
									m_mainScreen.m_mainApp.m_imPopupPrompt);
		
		add(m_imPopupPrompt);
		
		m_imChatMsgHistory		= new ObjectChoiceField(recvMain.sm_local.getString(localResource.IM_OPTION_CHAT_HISTORY_NUM),
									recvMain.fsm_imChatMsgHistoryList,m_mainScreen.m_mainApp.m_imChatMsgHistory);
		
		add(m_imChatMsgHistory);
		
		//@}
		
		
		setTitle(recvMain.sm_local.getString(localResource.IM_OPTION_SCREEN));
	}

	 protected boolean onSave(){
		 boolean t_ret = super.onSave();
		 if(t_ret){
			 m_mainScreen.m_mainApp.m_enableChatChecked = m_enableChatChecked.getChecked();
			 m_mainScreen.m_mainApp.m_enableChatState 	= m_enableChatState.getChecked();
			 m_mainScreen.m_mainApp.m_imReturnSend 	= m_imReturnSend.getChecked();
			 
			 m_mainScreen.m_mainApp.m_imChatScreenReceiveReturn = m_imChatScreenReceiveReturn.getChecked();
			 m_mainScreen.m_mainApp.m_imPopupPrompt = m_imPopupPrompt.getChecked();
			 
			 m_mainScreen.m_mainApp.m_imChatMsgHistory = m_imChatMsgHistory.getSelectedIndex();
			 
			 m_mainScreen.m_mainApp.WriteReadIni(false);
		 }
		 return t_ret;
	 }
	 
	public void close(){
		m_mainScreen.m_optionScreen = null;
		super.close();
	}
}
