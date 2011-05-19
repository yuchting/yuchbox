package com.yuchting.yuchberry.client;

import local.localResource;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.FocusChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.CheckboxField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.component.NumericChoiceField;
import net.rim.device.api.ui.component.ObjectChoiceField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.MainScreen;

public class settingScreen extends MainScreen implements FieldChangeListener,FocusChangeListener{
	
	 EditField			m_APN			= null;
	 EditField			m_appendString	= null;
	 EditField			m_passwordKey	= null;
	 boolean			m_hasChangePasswordKey	= false;
	 
	 CheckboxField		m_useSSLCheckbox= null;
	 CheckboxField		m_uesMDS		= null;
	 CheckboxField		m_useWifi		= null;
	 CheckboxField		m_autoRun		= null;
	 ObjectChoiceField	m_pulseInterval	= null;
	 
	 LabelField			m_uploadByte	= new LabelField();
	 LabelField			m_downloadByte	= new LabelField();
	 LabelField			m_totalByte		= new LabelField();
	 LabelField			m_sendMailNum	= new LabelField();
	 LabelField			m_recvMailNum	= new LabelField();
	 ButtonField		m_clearByteBut	= new ButtonField(recvMain.sm_local.getString(localResource.CLEAR_STATISTICS),Field.FIELD_RIGHT);
	 
	 CheckboxField		m_fulldayPrompt = null;
	 NumericChoiceField	m_startPromptHour = null;
	 NumericChoiceField	m_endPromptHour = null;
	 
	 CheckboxField		m_useLocationInfo = null;
	 LabelField			m_longitude		= new LabelField();
	 LabelField			m_latitude		= new LabelField();
	 
	 CheckboxField		m_discardOrgText = null;
	 CheckboxField		m_delRemoteMail	= null;
	 CheckboxField		m_copyToSentFolder = null;
	 ObjectChoiceField	m_recvMsgTextLength	= null;
	 ButtonField		m_changeSignature = new ButtonField(recvMain.sm_local.getString(localResource.CHANGE_SIGNATURE_BUTTON_TEXT),Field.FIELD_RIGHT);
	 
	 CheckboxField		m_weiboModule	= null;
	 CheckboxField		m_updateOwnWhenFw = null;
	 CheckboxField		m_updateOwnWhenRe = null;	 
	 
	 recvMain			m_mainApp		= null;
	 
	 MenuItem	m_helpMenu = new MenuItem(recvMain.sm_local.getString(localResource.STATE_SCREEN_HELP_MENU), 99, 10) {	
		 public void run() {
			 recvMain.openURL("http://code.google.com/p/yuchberry/wiki/Use_introduction#高级设置");
		 }
	 };
	 
	 protected void makeMenu(Menu _menu,int instance){
		_menu.add(m_helpMenu);
		
		super.makeMenu(_menu, instance);
	}
	 
	 public settingScreen(recvMain _app){
		 m_mainApp = _app;
		 
		 
		 //@{ connection option
		 LabelField t_title = new LabelField(recvMain.sm_local.getString(localResource.CONNECT_OPTION_LABEL));
		 t_title.setFont(t_title.getFont().derive(Font.BOLD));
		 add(t_title);
		 
		 m_APN			= new EditField(recvMain.sm_local.getString(localResource.APN_LABEL),
		 									m_mainApp.GetAPNList(),128,EditField.FILTER_DEFAULT);
		 add(m_APN);
		 
		 m_appendString	= new EditField(recvMain.sm_local.getString(localResource.APPEND_STRING_LABEL),
											m_mainApp.m_appendString,128,EditField.FILTER_DEFAULT);
		 add(m_appendString);
		 
		 m_passwordKey	= new EditField(recvMain.sm_local.getString(localResource.SETTTING_PASSWORD_KEY),
				 							(m_mainApp.m_passwordKey.length() != 0)?"***":"",128,EditField.FILTER_DEFAULT);
		 add(m_passwordKey);
		 m_passwordKey.setChangeListener(this);
		 m_passwordKey.setFocusListener(this);
		 
		 m_pulseInterval	= new ObjectChoiceField(recvMain.sm_local.getString(localResource.PULSE_INTERVAL_LABEL),
				 							recvMain.fsm_pulseIntervalString,m_mainApp.m_pulseIntervalIndex);
		 add(m_pulseInterval);
		 m_pulseInterval.setChangeListener(this);

		 m_useSSLCheckbox	= new CheckboxField(recvMain.sm_local.getString(localResource.USE_SSL_LABEL),m_mainApp.m_useSSL);
		 add(m_useSSLCheckbox);
		 
		 m_uesMDS			= new CheckboxField(recvMain.sm_local.getString(localResource.USE_MDS),m_mainApp.UseMDS());
		 add(m_uesMDS);
		 
		 m_useWifi			= new CheckboxField(recvMain.sm_local.getString(localResource.USE_WIFI_LABEL), m_mainApp.m_useWifi);
		 add(m_useWifi);
		 
		 m_autoRun			= new CheckboxField(recvMain.sm_local.getString(localResource.AUTO_RUN_CHECK_BOX), m_mainApp.m_autoRun);
		 add(m_autoRun);
		 
		 //@}
		 
		 add(new SeparatorField());
		 
		 //@{
		 t_title = new LabelField(recvMain.sm_local.getString(localResource.SETTING_MAIL_OP));
		 t_title.setFont(t_title.getFont().derive(Font.BOLD));
		 add(t_title);
		 
		 m_discardOrgText	= new CheckboxField(recvMain.sm_local.getString(localResource.DISCARD_ORG_TEXT), m_mainApp.m_discardOrgText);
		 add(m_discardOrgText);
		 
		 m_delRemoteMail	= new CheckboxField(recvMain.sm_local.getString(localResource.DELETE_REMOTE_MAIL),m_mainApp.m_delRemoteMail);
		 add(m_delRemoteMail);
		 
		 m_copyToSentFolder	= new CheckboxField(recvMain.sm_local.getString(localResource.COPY_MAIL_TO_SENT_FOLDER),m_mainApp.m_copyMailToSentFolder);
		 add(m_copyToSentFolder);
		 
		 m_recvMsgTextLength = new ObjectChoiceField(recvMain.sm_local.getString(localResource.MESSAGE_CONTAIN_MAX_LENGTH),
				 					recvMain.fsm_recvMaxTextLenghtString,m_mainApp.m_recvMsgTextLengthIndex);
		 add(m_recvMsgTextLength);
		 
		 add(m_changeSignature);
		 m_changeSignature.setChangeListener(this);
		 
		 //@}
		 
		 add(new SeparatorField());
		 
		 //@{ weibo op
		 t_title = new LabelField(recvMain.sm_local.getString(localResource.SETTING_WEIBO_OP));
		 t_title.setFont(t_title.getFont().derive(Font.BOLD));
		 add(t_title);
		 
		 m_weiboModule = new CheckboxField(recvMain.sm_local.getString(localResource.SETTING_WEIBO_OP_ENABLE),m_mainApp.m_enableWeiboModule);
		 add(m_weiboModule);
		 
		 m_updateOwnWhenFw = new CheckboxField(recvMain.sm_local.getString(localResource.SETTING_WEIBO_OP_UPDATE_FW),m_mainApp.m_updateOwnListWhenFw);
		 add(m_updateOwnWhenFw);
		 
		 m_updateOwnWhenRe = new CheckboxField(recvMain.sm_local.getString(localResource.SETTING_WEIBO_OP_UPDATE_RE),m_mainApp.m_updateOwnListWhenRe);
		 add(m_updateOwnWhenRe);
		 
		 //@}
		 
		 add(new SeparatorField());
		 
		 //@{ upload and download bytes statistics
		 t_title = new LabelField(recvMain.sm_local.getString(localResource.BYTE_STATISTICS));
		 t_title.setFont(t_title.getFont().derive(Font.BOLD));
		 add(t_title);
		 
		 add(m_uploadByte);
		 add(m_downloadByte);
		 add(m_totalByte);
		 add(m_sendMailNum);
		 add(m_recvMailNum);
		 add(m_clearByteBut);
		 m_clearByteBut.setChangeListener(this);
		 
		 if(m_mainApp.m_connectDeamon.m_connect != null){
			 m_mainApp.m_connectDeamon.m_connect.StoreUpDownloadByteImm(true);
		 }
		 
		 RefreshUpDownloadByte();
		 //@}
		 
		 add(new SeparatorField());
		 
		 
		 //@{ reminder option
		 t_title = new LabelField(recvMain.sm_local.getString(localResource.PROMPT_OPTION_LABEL));
		 t_title.setFont(t_title.getFont().derive(Font.BOLD));
		 add(t_title);
		 
		 m_fulldayPrompt 	= new CheckboxField(recvMain.sm_local.getString(localResource.FULLDAY_PROMT_LABEL),m_mainApp.m_fulldayPrompt);
		 add(m_fulldayPrompt);
		 
		 m_startPromptHour	= new NumericChoiceField(recvMain.sm_local.getString(localResource.START_HOUR_PROMPT_LABEL),0,24,1,m_mainApp.m_startPromptHour);
		 add(m_startPromptHour);
		 
		 m_endPromptHour	= new NumericChoiceField(recvMain.sm_local.getString(localResource.END_HOUR_PROMPT_LABEL),0,24,1,m_mainApp.m_endPromptHour);
		 add(m_endPromptHour);
		 //@}
		 
		 add(new SeparatorField());
		 
		 //@{ other option
		 t_title = new LabelField(recvMain.sm_local.getString(localResource.LOCATION_OPTIION_LABEL));
		 t_title.setFont(t_title.getFont().derive(Font.BOLD));
		 add(t_title);
		 
		 m_useLocationInfo	= new CheckboxField(recvMain.sm_local.getString(localResource.USE_LOCATION_LABEL), m_mainApp.m_useLocationInfo);
		 add(m_useLocationInfo);
		 
		 add(m_longitude);
		 add(m_latitude);
		 
		 RefreshLocation();
		 
		 //@}		 
		 
		 setTitle(new LabelField(recvMain.sm_local.getString(localResource.ADVANCE_SETTING_TITEL_LABEL),LabelField.ELLIPSIS | LabelField.USE_ALL_WIDTH));
		 
	 }
	 
	 public void fieldChanged(Field field, int context) {
		if(context != FieldChangeListener.PROGRAMMATIC){
			// Perform action if user changed field. 
			//
			if(field == m_clearByteBut){
				if(Dialog.ask(Dialog.D_YES_NO,recvMain.sm_local.getString(localResource.CLEAR_STATISTICS_PROMPT),Dialog.NO) == Dialog.YES){
					m_mainApp.ClearUpDownloadByte();
					
					m_mainApp.SetSendMailNum(0);
					m_mainApp.SetRecvMailNum(0);
					
					RefreshUpDownloadByte();
				}
			}else if(field == m_pulseInterval){
				if(m_pulseInterval.getSelectedIndex() == 0){
					m_mainApp.DialogAlert(recvMain.sm_local.getString(localResource.PULSE_INTERVAL_TOO_SHORT_PROMPT));
				}
			}else if(field == m_changeSignature){
				m_mainApp.DialogAlert(recvMain.sm_local.getString(localResource.CHANGE_SIGNATURE_PROMPT_TEXT));
			}else if(field == m_passwordKey){			
				m_hasChangePasswordKey = true;
			}
		}else{
			// Perform action if application changed field.
		}
	 }
	 
	 public void focusChanged(Field field, int eventType){
		 if(field == m_passwordKey){
			 if(FocusChangeListener.FOCUS_GAINED == eventType){
				 if(!m_hasChangePasswordKey && m_mainApp.m_passwordKey.length() != 0){
					 m_passwordKey.setText("");
				 }
			 }else if(FocusChangeListener.FOCUS_LOST == eventType){
				 if(!m_hasChangePasswordKey && m_mainApp.m_passwordKey.length() != 0){
					 m_passwordKey.setText("***");
				 }
			 }
		 }
	 }
	 
	 public boolean onClose(){
		
		if(m_startPromptHour.getSelectedIndex() < m_endPromptHour.getSelectedIndex()){
			m_mainApp.m_startPromptHour	= m_startPromptHour.getSelectedIndex();
			m_mainApp.m_endPromptHour	= m_endPromptHour.getSelectedIndex();
		}else{
			m_mainApp.DialogAlert(recvMain.sm_local.getString(localResource.START_BIGGER_THAN_END_PROMPT));
			return false;
		}
	 
		m_mainApp.m_useSSL	= m_useSSLCheckbox.getChecked();
		m_mainApp.SetAPNName(m_APN.getText());
		m_mainApp.m_autoRun = m_autoRun.getChecked();
		
		m_mainApp.m_appendString = m_appendString.getText();
		m_mainApp.m_useWifi = m_useWifi.getChecked();
		
		m_mainApp.m_pulseIntervalIndex = m_pulseInterval.getSelectedIndex();
		m_mainApp.m_useLocationInfo = m_useLocationInfo.getChecked();
		
		m_mainApp.m_useMDS = m_uesMDS.getChecked();
		m_mainApp.m_fulldayPrompt	= m_fulldayPrompt.getChecked();				
		
		m_mainApp.m_discardOrgText = m_discardOrgText.getChecked();
		m_mainApp.m_delRemoteMail	= m_delRemoteMail.getChecked();
		m_mainApp.m_recvMsgTextLengthIndex = m_recvMsgTextLength.getSelectedIndex();
		m_mainApp.m_copyMailToSentFolder = m_copyToSentFolder.getChecked();
		
		if(m_mainApp.m_enableWeiboModule != m_weiboModule.getChecked()){
			
			m_mainApp.m_enableWeiboModule = m_weiboModule.getChecked();
			
			if(m_mainApp.m_enableWeiboModule){
				m_mainApp.InitWeiboModule();
			}
		}
		
		m_mainApp.m_updateOwnListWhenFw = m_updateOwnWhenFw.getChecked();
		m_mainApp.m_updateOwnListWhenRe = m_updateOwnWhenRe.getChecked();
		
		
		m_mainApp.WriteReadIni(false);
		
		m_mainApp.m_settingScreen = null;
		
		if(m_passwordKey.getText().length() != 0 && m_hasChangePasswordKey){
			m_mainApp.m_passwordKey = recvMain.md5(m_passwordKey.getText());
		}
		
		close();
		return true;
	 }
	 
	 public void RefreshUpDownloadByte(){
		 m_mainApp.invokeLater(new Runnable() {
			public void run() {
				m_uploadByte.setText(recvMain.sm_local.getString(localResource.UPLOAD_STATISTICS) + recvMain.GetByteStr(m_mainApp.m_uploadByte));
				m_downloadByte.setText(recvMain.sm_local.getString(localResource.DOWNLOAD_STATISTICS) + recvMain.GetByteStr(m_mainApp.m_downloadByte));
				m_totalByte.setText(recvMain.sm_local.getString(localResource.TOTAL_STATISTICS) + recvMain.GetByteStr(m_mainApp.m_downloadByte + m_mainApp.m_uploadByte));
				
				m_sendMailNum.setText(recvMain.sm_local.getString(localResource.SEND_MAIL_NUM) + m_mainApp.GetSendMailNum());
				m_recvMailNum.setText(recvMain.sm_local.getString(localResource.RECV_MAIL_NUM) + m_mainApp.GetRecvMailNum());
			}
		});
	 }
	 
	 public void RefreshLocation(){
		 m_mainApp.invokeLater(new Runnable() {
			public void run() {
				m_longitude.setText(recvMain.sm_local.getString(localResource.CURRENT_LONGITUDE_LABEL) + m_mainApp.m_gpsInfo.m_longitude);
				m_latitude.setText(recvMain.sm_local.getString(localResource.CURRENT_LATITUDE_LABEL) + m_mainApp.m_gpsInfo.m_latitude);
			}
		});
	 }
}
