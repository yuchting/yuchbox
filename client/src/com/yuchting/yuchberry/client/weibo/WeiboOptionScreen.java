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
package com.yuchting.yuchberry.client.weibo;

import local.yblocalResource;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.CheckboxField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ObjectChoiceField;
import net.rim.device.api.ui.component.RadioButtonField;
import net.rim.device.api.ui.component.RadioButtonGroup;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

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
	 CheckboxField		m_autoLoadNewTimelineWeibo = null;
	 ObjectChoiceField	m_refreshWeiboInterval = null;
	 ObjectChoiceField	m_maxWeiboNum	= null;
	 ObjectChoiceField	m_uploadImageSize	= null;
	 ButtonField		m_clearCheckImageSetting = null;
	 
	 CheckboxField		m_weiboDontReadHistroy = null;
	 
	 private RadioButtonGroup m_shortkeyTypeGroup = new RadioButtonGroup();
	 private RadioButtonField m_spaceDown	= new RadioButtonField(recvMain.sm_local.getString(yblocalResource.SETTING_WEIBO_SPACE_DOWN),m_shortkeyTypeGroup,true);
	 private RadioButtonField m_spaceUp	= new RadioButtonField(recvMain.sm_local.getString(yblocalResource.SETTING_WEIBO_SPACE_UP),m_shortkeyTypeGroup,false);
	
	 private RadioButtonGroup m_uiGroup = new RadioButtonGroup();
	 private RadioButtonField m_uiStandard	= new RadioButtonField(recvMain.sm_local.getString(yblocalResource.WEIBO_OPTION_STANDARD_UI),m_uiGroup,true);
	 private RadioButtonField m_uiBlack	= new RadioButtonField(recvMain.sm_local.getString(yblocalResource.WEIBO_OPTION_BLACK_UI),m_uiGroup,false);
	 
	 public VerticalFieldManager	m_weiboAccountList = new VerticalFieldManager(Manager.VERTICAL_SCROLL);
	 ButtonField		m_weiboAccountRefreshBtn = new ButtonField(recvMain.sm_local.getString(yblocalResource.WEIBO_OPTION_ACCOUNT_REFRESH_BTN_LABEL),
			 														Field.FIELD_RIGHT | ButtonField.CONSUME_CLICK | ButtonField.NEVER_DIRTY);
	 recvMain			m_mainApp = null;
	 
	 public WeiboOptionScreen(recvMain _mainApp){
		 m_mainApp = _mainApp;
		 
		 //@{{ weibo account list
		 LabelField t_label = new LabelField(recvMain.sm_local.getString(yblocalResource.WEIBO_OPTION_ACCOUNT_LABEL),Field.FOCUSABLE);
		 t_label.setFont(t_label.getFont().derive(t_label.getFont().getStyle() | Font.BOLD));
		 add(t_label);
		 
		 add(m_weiboAccountList);
		 add(m_weiboAccountRefreshBtn);
		 
		 m_weiboAccountRefreshBtn.setChangeListener(this);
		 //@}}
		 
		 add(new SeparatorField());
		 
		 //@{ weibo operation
		 t_label = new LabelField(recvMain.sm_local.getString(yblocalResource.WEIBO_OPTION_OP_LABEL));
		 t_label.setFont(t_label.getFont().derive(t_label.getFont().getStyle() | Font.BOLD));
		 add(t_label);
		 
		 m_updateOwnWhenFw = new CheckboxField(recvMain.sm_local.getString(yblocalResource.SETTING_WEIBO_OP_UPDATE_FW),m_mainApp.m_updateOwnListWhenFw);
		 add(m_updateOwnWhenFw);
		 
		 m_updateOwnWhenRe = new CheckboxField(recvMain.sm_local.getString(yblocalResource.SETTING_WEIBO_OP_UPDATE_RE),m_mainApp.m_updateOwnListWhenRe);
		 add(m_updateOwnWhenRe);
		 		 
		 m_publicForward	= new CheckboxField(recvMain.sm_local.getString(yblocalResource.SETTING_WEIBO_OP_PUBLIC_FW),m_mainApp.m_publicForward);
		 m_publicForward.setChangeListener(this);
		 add(m_publicForward);
		 	 
		 m_dontDownloadHeadImage = new CheckboxField(recvMain.sm_local.getString(yblocalResource.SETTING_WEIBO_DONT_DOWNLOAD_HEAD_IMAGE),m_mainApp.m_dontDownloadWeiboHeadImage);
		 add(m_dontDownloadHeadImage);
		 		 
		 m_weiboDontReadHistroy		= new CheckboxField(recvMain.sm_local.getString(yblocalResource.WEIBO_READ_HISTROY_LABEL),m_mainApp.m_weiboDontReadHistroy);
		 add(m_weiboDontReadHistroy);
		 		 		 
		 m_maxWeiboNum		= new ObjectChoiceField(recvMain.sm_local.getString(yblocalResource.SETTING_WEIBO_OP_MAX_WEIBO_NUM),
								recvMain.fsm_maxWeiboNumList,m_mainApp.m_maxWeiboNumIndex);
		 add(m_maxWeiboNum);
		 
		 m_refreshWeiboInterval		= new ObjectChoiceField(recvMain.sm_local.getString(yblocalResource.WEIBO_OPTION_REFRESH_INTERVAL),
								recvMain.fsm_refreshWeiboIntervalList,m_mainApp.m_refreshWeiboIntervalIndex);
		 add(m_refreshWeiboInterval);
		 
		 m_uploadImageSize			= new ObjectChoiceField(recvMain.sm_local.getString(yblocalResource.WEIBO_UPLOAD_IMAGE_SIZE),
								recvMain.fsm_weiboUploadImageSizeList,m_mainApp.m_weiboUploadImageSizeIndex);
		 add(m_uploadImageSize);
		 
		 m_clearCheckImageSetting = new ButtonField(recvMain.sm_local.getString(yblocalResource.SETTING_WEIBO_CLEAR_CHECK_IMAGE_PROMPT),
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
		 		 
		 //@}}
		 
		 add(new SeparatorField());
				 
		 //@{ weibo display
		 t_label = new LabelField(recvMain.sm_local.getString(yblocalResource.WEIBO_OPTION_DISPLAY_LABEL));
		 t_label.setFont(t_label.getFont().derive(t_label.getFont().getStyle() | Font.BOLD));
		 add(t_label);
		 
		 m_commentFirst		= new CheckboxField(recvMain.sm_local.getString(yblocalResource.SETTING_WEIBO_DISPLAY_COMMENT_FIRST),
				 							recvMain.sm_commentFirst);
		 add(m_commentFirst);
		 
		 m_displayHeadImage = new CheckboxField(recvMain.sm_local.getString(yblocalResource.SETTING_WEIBO_DISPLAY_HEAD_IMAGE),
				 							recvMain.sm_displayHeadImage);
		 add(m_displayHeadImage);
		 
		 m_simpleMode 	=  new CheckboxField(recvMain.sm_local.getString(yblocalResource.SETTING_WEIBO_SIMPLE_MODE),
				 							recvMain.sm_simpleMode);
		 add(m_simpleMode);
		 
		 m_hideWeiboHeader = new CheckboxField(recvMain.sm_local.getString(yblocalResource.SETTING_WEIBO_HIDE_HEADER),
				 							m_mainApp.m_hideHeader);
		 add(m_hideWeiboHeader);
		 
		 m_showAllInList	= new CheckboxField(recvMain.sm_local.getString(yblocalResource.SETTING_WEIBO_SHOW_ALL_IN_LIST),
				 							recvMain.sm_showAllInList);
		 add(m_showAllInList);
		 m_showAllInList.setChangeListener(this);
		 
		 m_autoLoadNewTimelineWeibo = new CheckboxField(recvMain.sm_local.getString(yblocalResource.WEIBO_OPTION_AUTO_LOAD_TIMELINE),
				 				m_mainApp.m_autoLoadNewTimelineWeibo);
		 
		 add(m_autoLoadNewTimelineWeibo);
		 		 
		 t_label = new LabelField(recvMain.sm_local.getString(yblocalResource.WEIBO_OPTION_THEME_LABEL));
// 屏蔽UI选择按钮
/*
		 add(t_label);
		 add(m_uiStandard);
	 	 add(m_uiBlack);
		 m_uiStandard.setChangeListener(this);
		 m_uiBlack.setChangeListener(this);
*/		 
		 if(!recvMain.sm_standardUI){
			 m_uiBlack.setSelected(true);
		 }
		 //@}
		 		 
		 setTitle(recvMain.sm_local.getString(yblocalResource.WEIBO_SETTING_SCREEN_TITLE));
		 
		 // refresh WeiboAccount list
		 refreshWeiboAccount();
	 }
	 
	 public void fieldChanged(Field field, int context) {
		 if(context != FieldChangeListener.PROGRAMMATIC){
			 if(field == m_publicForward){
				 if(m_publicForward.getChecked()){
					 m_mainApp.DialogAlert(recvMain.sm_local.getString(yblocalResource.SETTING_WEIBO_OP_PUBLIC_FW_PROMPT));
				 }
			 }else if(field == m_showAllInList){
				 m_mainApp.DialogAlert(recvMain.sm_local.getString(yblocalResource.SETTING_WEIBO_SHOW_ALL_IN_LIST_PROMPT));				
			 }else if(field == m_clearCheckImageSetting){
				 m_mainApp.m_hasPromptToCheckImg = true;
				 m_mainApp.DialogAlert("Clear OK!");
			 }else if(field == m_uiStandard || field == m_uiBlack){
				 if(((RadioButtonField)field).isSelected()){
					 m_mainApp.DialogAlert(recvMain.sm_local.getString(yblocalResource.WEIBO_OPTION_UI_CHANGE_PROMPT));
				 }				 
			 }else if(field == m_weiboAccountRefreshBtn){
				 m_mainApp.sendRefreshWeiboAccountList();
			 }
		 }
	 }
	 
	 public void refreshWeiboAccount(){
		 
		 UiApplication.getUiApplication().invokeLater(new Runnable() {
			
			public void run() {
				m_weiboAccountList.deleteAll();
				 
				int num = m_mainApp.m_weiboAccountList.size();
				for(int i = 0 ; i< num;i++){
					WeiboAccount acc = (WeiboAccount)m_mainApp.m_weiboAccountList.elementAt(i);
					String t_name = acc.name + "(" + fetchWeibo.getLocalStyleName(acc.weiboStyle) + ")";
					CheckboxField t_field = new CheckboxField(t_name,acc.needUpdate);
					 
					m_weiboAccountList.add(t_field);
				}
				
			}
		});		
	 }
	 
	 protected boolean onSave(){
		 boolean t_ret = super.onSave();
		 if(t_ret){
			 
			 m_mainApp.m_updateOwnListWhenFw = m_updateOwnWhenFw.getChecked();
			 m_mainApp.m_updateOwnListWhenRe = m_updateOwnWhenRe.getChecked();
			 recvMain.sm_commentFirst	= m_commentFirst.getChecked();
			 m_mainApp.m_publicForward		= m_publicForward.getChecked();		
			 m_mainApp.m_maxWeiboNumIndex	= m_maxWeiboNum.getSelectedIndex();
			 m_mainApp.m_weiboUploadImageSizeIndex = m_uploadImageSize.getSelectedIndex();
			 
			 m_mainApp.m_refreshWeiboIntervalIndex = m_refreshWeiboInterval.getSelectedIndex();
			 m_mainApp.m_weiboTimeLineScreen.startAutoRefresh();
			 m_mainApp.m_weiboDontReadHistroy = m_weiboDontReadHistroy.getChecked();
			 m_mainApp.m_hideHeader			= m_hideWeiboHeader.getChecked();
			
			 recvMain.sm_displayHeadImage	= m_displayHeadImage.getChecked();
			 recvMain.sm_simpleMode			= m_simpleMode.getChecked();
			 recvMain.sm_showAllInList		= m_showAllInList.getChecked();
			 m_mainApp.m_dontDownloadWeiboHeadImage	= m_dontDownloadHeadImage.getChecked();
			 
			 m_mainApp.m_autoLoadNewTimelineWeibo = m_autoLoadNewTimelineWeibo.getChecked();
			 
			 m_mainApp.m_spaceDownWeiboShortcutKey  = m_spaceDown.isSelected();
			
			 recvMain.sm_standardUI = m_uiStandard.isSelected();
			 
			 // don't display headimage if simple mode
			 //
			 if(recvMain.sm_simpleMode){
				 recvMain.sm_displayHeadImage = false; 
			 }
			
			 // refresh the weibo updata list
			 //
			 int num = m_mainApp.m_weiboAccountList.size();
			 for(int i = 0 ; i< num;i++){
				 WeiboAccount acc = (WeiboAccount)m_mainApp.m_weiboAccountList.elementAt(i);
				 CheckboxField t_field = (CheckboxField)m_weiboAccountList.getField(i);
				 
				 acc.needUpdate = t_field.getChecked();
			 }
			 
			 m_mainApp.WriteReadIni(false);
		 }
		 return t_ret;
	 }
	 
	 public void close(){
		 m_mainApp.m_weiboTimeLineScreen.m_optionScreen = null;
		 super.close();
	 }
}
