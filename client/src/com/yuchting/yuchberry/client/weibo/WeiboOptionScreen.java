package com.yuchting.yuchberry.client.weibo;

import local.localResource;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.CheckboxField;
import net.rim.device.api.ui.component.ObjectChoiceField;
import net.rim.device.api.ui.component.RadioButtonField;
import net.rim.device.api.ui.component.RadioButtonGroup;
import net.rim.device.api.ui.container.MainScreen;

import com.yuchting.yuchberry.client.recvMain;

public class WeiboOptionScreen extends MainScreen implements FieldChangeListener{
 
	 CheckboxField		m_updateOwnWhenFw = null;
	 CheckboxField		m_updateOwnWhenRe = null;
	 CheckboxField		m_commentFirst	= null;
	 CheckboxField		m_publicForward	= null;
	 CheckboxField		m_displayHeadImage = null;
	 CheckboxField		m_simpleMode 	= null;
	 CheckboxField		m_dontDownloadHeadImage = null;
	 CheckboxField		m_hideWeiboHeader = null;
	 CheckboxField		m_showAllInList	= null;
	 ObjectChoiceField	m_maxWeiboNum	= null;
	 ButtonField		m_clearCheckImageSetting = null;
	 
	 private RadioButtonGroup m_shortkeyTypeGroup = new RadioButtonGroup();
	 private RadioButtonField m_spaceDown	= new RadioButtonField(recvMain.sm_local.getString(localResource.SETTING_WEIBO_SPACE_DOWN),m_shortkeyTypeGroup,true);
	 private RadioButtonField m_spaceUp	= new RadioButtonField(recvMain.sm_local.getString(localResource.SETTING_WEIBO_SPACE_UP),m_shortkeyTypeGroup,false);
	
	
	 recvMain			m_mainApp = null;
	 
	 public WeiboOptionScreen(recvMain _mainApp){
		 m_mainApp = _mainApp;
		 
		 m_updateOwnWhenFw = new CheckboxField(recvMain.sm_local.getString(localResource.SETTING_WEIBO_OP_UPDATE_FW),m_mainApp.m_updateOwnListWhenFw);
		 add(m_updateOwnWhenFw);
		 
		 m_updateOwnWhenRe = new CheckboxField(recvMain.sm_local.getString(localResource.SETTING_WEIBO_OP_UPDATE_RE),m_mainApp.m_updateOwnListWhenRe);
		 add(m_updateOwnWhenRe);
		 
		 m_commentFirst		= new CheckboxField(recvMain.sm_local.getString(localResource.SETTING_WEIBO_DISPLAY_COMMENT_FIRST),WeiboItemField.sm_commentFirst);
		 add(m_commentFirst);
		 
		 m_publicForward	= new CheckboxField(recvMain.sm_local.getString(localResource.SETTING_WEIBO_OP_PUBLIC_FW),m_mainApp.m_publicForward);
		 m_publicForward.setChangeListener(this);
		 add(m_publicForward);
		 
		 m_displayHeadImage = new CheckboxField(recvMain.sm_local.getString(localResource.SETTING_WEIBO_DISPLAY_HEAD_IMAGE),WeiboItemField.sm_displayHeadImage);
		 add(m_displayHeadImage);
		 
		 m_simpleMode 	=  new CheckboxField(recvMain.sm_local.getString(localResource.SETTING_WEIBO_SIMPLE_MODE),WeiboItemField.sm_simpleMode);
		 add(m_simpleMode);
		 
		 m_dontDownloadHeadImage = new CheckboxField(recvMain.sm_local.getString(localResource.SETTING_WEIBO_DONT_DOWNLOAD_HEAD_IMAGE),m_mainApp.m_dontDownloadWeiboHeadImage);
		 add(m_dontDownloadHeadImage);
		 
		 m_hideWeiboHeader = new CheckboxField(recvMain.sm_local.getString(localResource.SETTING_WEIBO_HIDE_HEADER),m_mainApp.m_hideHeader);
		 add(m_hideWeiboHeader);
		 
		 m_showAllInList	= new CheckboxField(recvMain.sm_local.getString(localResource.SETTING_WEIBO_SHOW_ALL_IN_LIST),WeiboItemField.sm_showAllInList);
		 add(m_showAllInList);
		 m_showAllInList.setChangeListener(this);
		 		 
		 m_maxWeiboNum		= new ObjectChoiceField(recvMain.sm_local.getString(localResource.SETTING_WEIBO_OP_MAX_WEIBO_NUM),
								recvMain.fsm_maxWeiboNumList,m_mainApp.m_maxWeiboNumIndex);
		 
		 add(m_maxWeiboNum);
		 
		 m_clearCheckImageSetting = new ButtonField(recvMain.sm_local.getString(localResource.SETTING_WEIBO_CLEAR_CHECK_IMAGE_PROMPT),
				 									Field.FIELD_RIGHT | ButtonField.CONSUME_CLICK | ButtonField.NEVER_DIRTY);
		 m_clearCheckImageSetting.setChangeListener(this);
		 
		 add(m_clearCheckImageSetting);
		 
		 add(m_spaceDown);
		 add(m_spaceUp);
		 
		 if(m_mainApp.m_spaceDownWeiboShortcutKey){
			 m_spaceDown.setSelected(true);
		 }else{
			 m_spaceUp.setSelected(true);
		 }
		 
		 setTitle(recvMain.sm_local.getString(localResource.WEIBO_SETTING_SCREEN_TITLE));
	 }
	 
	 public void fieldChanged(Field field, int context) {
		 if(context != FieldChangeListener.PROGRAMMATIC){
			 if(field == m_publicForward){
				 if(m_publicForward.getChecked()){
					 m_mainApp.DialogAlert(recvMain.sm_local.getString(localResource.SETTING_WEIBO_OP_PUBLIC_FW_PROMPT));
				 }
			 }else if(field == m_showAllInList){
				 m_mainApp.DialogAlert(recvMain.sm_local.getString(localResource.SETTING_WEIBO_SHOW_ALL_IN_LIST_PROMPT));				
			 }else if(field == m_clearCheckImageSetting){
				 m_mainApp.m_hasPromptToCheckImg = true;
				 m_mainApp.DialogAlert("Clear OK!");
			 }
		 }	
	 }
	 
	 protected boolean onSave(){
		 boolean t_ret = super.onSave();
		 if(t_ret){
			 m_mainApp.m_updateOwnListWhenFw = m_updateOwnWhenFw.getChecked();
			 m_mainApp.m_updateOwnListWhenRe = m_updateOwnWhenRe.getChecked();
			 WeiboItemField.sm_commentFirst	= m_commentFirst.getChecked();
			 m_mainApp.m_publicForward		= m_publicForward.getChecked();		
			 m_mainApp.m_maxWeiboNumIndex	= m_maxWeiboNum.getSelectedIndex();
			 m_mainApp.m_hideHeader			= m_hideWeiboHeader.getChecked();
			
			 WeiboItemField.sm_displayHeadImage	= m_displayHeadImage.getChecked();
			 WeiboItemField.sm_simpleMode		= m_simpleMode.getChecked();
			 WeiboItemField.sm_showAllInList		= m_showAllInList.getChecked();
			 m_mainApp.m_dontDownloadWeiboHeadImage	= m_dontDownloadHeadImage.getChecked();
			 
			 m_mainApp.m_spaceDownWeiboShortcutKey  = m_spaceDown.isSelected();
			
			 m_mainApp.WriteReadIni(false);
		 }
		 return t_ret;
	 }
	 
	 public void close(){
		 super.close();
		 m_mainApp.m_weiboTimeLineScreen.m_optionScreen = null;
	 }
}
