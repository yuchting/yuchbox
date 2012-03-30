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
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.yuchting.yuchberry.client.recvMain;

public class IMPromptDlg extends PopupScreen implements FieldChangeListener{
	
	MainIMScreen.RosterChatData 	m_openData = null;
	MainIMScreen			m_mainScreen = null;
	
	LabelField				m_nameText = new LabelField();
	LabelField				m_msgText = new LabelField();
	ButtonField				m_replyBut	 = new ButtonField(recvMain.sm_local.getString(yblocalResource.IM_PROMPT_DLG_CONFIRM_BUT),
												Field.FIELD_HCENTER | ButtonField.CONSUME_CLICK | ButtonField.NEVER_DIRTY);
	
	ButtonField				m_laterBut	 = new ButtonField(recvMain.sm_local.getString(yblocalResource.IM_PROMPT_DLG_CANCEL_BUT),
												Field.FIELD_HCENTER | ButtonField.CONSUME_CLICK | ButtonField.NEVER_DIRTY);
	
	public IMPromptDlg(MainIMScreen _mainScreen){
		super(new VerticalFieldManager(Manager.VERTICAL_SCROLL));
		
		m_mainScreen = _mainScreen;
		
		LabelField t_title = new LabelField(recvMain.sm_local.getString(yblocalResource.IM_PROMPT_DLG_TITLE));
		t_title.setFont(MainIMScreen.fsm_boldFont);
		add(t_title);
		
		add(m_nameText);
		add(m_msgText);
		add(new SeparatorField());
		
		HorizontalFieldManager t_buttonMgr = new HorizontalFieldManager(Field.FIELD_HCENTER);
		t_buttonMgr.add(m_replyBut);
		t_buttonMgr.add(m_laterBut);
		
		add(t_buttonMgr);
		
		m_replyBut.setChangeListener(this);
		m_laterBut.setChangeListener(this);
	}
	
	public void setRosterChatData(MainIMScreen.RosterChatData _data,String _text){
		m_openData = _data;

		m_nameText.setText(m_openData.m_roster.getName() + ":");
		
		// cut the text to display
		//
		final int ft_maxLength = 20;
		
		if(_text.length() > ft_maxLength){
			_text = _text.substring(0,ft_maxLength) + "...";
		}
		m_msgText.setText(_text);
	}
	
	public void fieldChanged(Field _field,int _context){
		if(_context != FieldChangeListener.PROGRAMMATIC){
			if(m_replyBut == _field){
				
				if(!m_mainScreen.m_mainApp.isForeground()){
					m_mainScreen.m_mainApp.requestForeground();
				}
				
				if(m_mainScreen.m_mainApp.getActiveScreen() != m_mainScreen.m_chatScreen){
					m_mainScreen.m_chatScreen.m_currRoster = m_openData;
					m_mainScreen.m_mainApp.pushScreen(m_mainScreen.m_chatScreen);	
				}else{
					m_mainScreen.m_chatScreen.m_currRoster = m_openData;
					m_mainScreen.m_chatScreen.m_middleMgr.prepareChatScreen(m_openData);
				}
				
				m_mainScreen.m_chatScreen.clearAttachment();
				
				onClose();
				
			}else if(m_laterBut == _field){
				onClose();	
			}
		}
	}
	
	protected void onDisplay(){
		super.onDisplay();
		
		m_replyBut.setFocus();
	}
	
	public boolean onClose(){
		m_mainScreen.m_mainApp.StopIMNotification();
		
		if(!hasMoreChatPrompt()){
			close();
			return true;
		}		
		return false;
	}
	
	private boolean hasMoreChatPrompt(){
		if(!m_mainScreen.m_promptQueue.isEmpty()){
			MainIMScreen.RosterChatData t_promptData = (MainIMScreen.RosterChatData)m_mainScreen.m_promptQueue.elementAt(0);
			m_mainScreen.m_promptQueue.removeElementAt(0);
			
			int t_num = t_promptData.m_chatMsgList.size();
			if(t_num > 0){
				fetchChatMsg msg = (fetchChatMsg)t_promptData.m_chatMsgList.elementAt(t_num - 1);
				
				setRosterChatData(t_promptData, MainIMScreen.getChatMsgAbsText(msg));
				
				return true;
			}			
		}
	
		return false;
	}
	
	protected boolean keyDown(int keycode,int time){
		
		final int key = Keypad.key(keycode);
		switch(key){
		case 'R':
			fieldChanged(m_replyBut,~FieldChangeListener.PROGRAMMATIC);
			return true;
		case 'L':
			fieldChanged(m_laterBut,~FieldChangeListener.PROGRAMMATIC);
			return true;
		}
		
		return super.keyDown(keycode,time);
	}

}
