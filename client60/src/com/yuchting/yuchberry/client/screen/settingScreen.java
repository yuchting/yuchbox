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

import java.io.ByteArrayOutputStream;
import java.util.Vector;

import local.yblocalResource;
import net.rim.device.api.system.Clipboard;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.FocusChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.CheckboxField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.component.NullField;
import net.rim.device.api.ui.component.NumericChoiceField;
import net.rim.device.api.ui.component.ObjectChoiceField;
import net.rim.device.api.ui.component.RadioButtonField;
import net.rim.device.api.ui.component.RadioButtonGroup;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.yuchting.yuchberry.client.msg_head;
import com.yuchting.yuchberry.client.recvMain;
import com.yuchting.yuchberry.client.sendReceive;

public class settingScreen extends MainScreen implements FieldChangeListener,FocusChangeListener{
	
	 EditField			m_APN			= null;
	 EditField			m_appendString	= null;
	 EditField			m_passwordKey	= null;
	 boolean			m_hasChangePasswordKey	= false;
	 
	 CheckboxField		m_useSSLCheckbox= null;
	 CheckboxField		m_uesMDS		= null;
	 CheckboxField		m_useWifi		= null;
	 CheckboxField		m_autoRun		= null;
	 CheckboxField		m_conDisPrompt 	= null;
	 CheckboxField		m_popupDlgWhenDisconnect 	= null;
	 ObjectChoiceField	m_pulseInterval	= null;
	 
	 CheckboxField		m_hideBackgroundIcon = null;
	 
	 
	 
	 LabelField			m_uploadDownloadByte	= new LabelField();
	 LabelField			m_totalByte		= new LabelField();
	 LabelField			m_sendRecvMailNum	= new LabelField();
	 LabelField			m_sentRecvWeiboNum	= new LabelField();
	 ButtonField		m_clearByteBut	= new ButtonField(recvMain.sm_local.getString(yblocalResource.SETTING_CLEAR_STATISTICS),
			 											Field.FIELD_RIGHT | ButtonField.CONSUME_CLICK | ButtonField.NEVER_DIRTY);
	 
	 ButtonField		m_copyStatBut	= new ButtonField(recvMain.sm_local.getString(yblocalResource.SETTING_COPY_STATISTICS),
			 											Field.FIELD_RIGHT | ButtonField.CONSUME_CLICK | ButtonField.NEVER_DIRTY);
	 CheckboxField		m_fulldayPrompt = null;
	 NumericChoiceField	m_startPromptHour = null;
	 NumericChoiceField	m_endPromptHour = null;
	 
	 CheckboxField		m_useLocationInfo = null;
	 LabelField			m_longitude		= new LabelField();
	 LabelField			m_latitude		= new LabelField();
	 
	 CheckboxField		m_discardOrgText = null;
	 CheckboxField		m_delRemoteMail	= null;
	 CheckboxField		m_markReadMailSvr = null;
	 CheckboxField		m_copyToSentFolder = null;
	 CheckboxField		m_mailUseLocation = null;
	 ObjectChoiceField	m_recvMsgTextLength	= null;
	 CheckboxField		m_closeMailSendModule = null;
	 ButtonField		m_changeSignature = new ButtonField(recvMain.sm_local.getString(yblocalResource.CHANGE_SIGNATURE_BUTTON_TEXT),
			 										Field.FIELD_RIGHT | ButtonField.CONSUME_CLICK | ButtonField.NEVER_DIRTY);
	 
	 VerticalFieldManager	m_sendMailAccountList = new VerticalFieldManager(Manager.VERTICAL_SCROLL);
	 ButtonField		m_requestMailAccountBut = new ButtonField(recvMain.sm_local.getString(yblocalResource.SETTING_REQUEST_MAIL_ACCOUNT),
			 											Field.FIELD_RIGHT | ButtonField.CONSUME_CLICK | ButtonField.NEVER_DIRTY);
	 CheckboxField		m_popupDlgWhenComposeNew = null;
	 CheckboxField		m_mailHtmlShow			= null;
	 CheckboxField		m_mailHtmlShowOnlyWIFI	= null;
	 NullField			m_mailHtmlShowOnlyWIFINull	= new NullField(Field.NON_FOCUSABLE);
	 
	 CheckboxField		m_weiboModule	= null;
	 NullField			m_weiboNullField = new NullField(Field.NON_FOCUSABLE);
	 LabelField			m_weiboSettingPrompt = new LabelField(recvMain.sm_local.getString(yblocalResource.SETTING_WEIBO_SETTING_PROMPT));
	 
	 CheckboxField		m_imModule		= null;
	 NullField			m_imNullField	= new NullField(Field.NON_FOCUSABLE);
	 LabelField			m_imSettingPrompt = new LabelField(recvMain.sm_local.getString(yblocalResource.SETTING_IM_SETTING_PROMPT));
	 
	 recvMain			m_mainApp		= null;
	 
	 MenuItem	m_helpMenu = new MenuItem(recvMain.sm_local.getString(yblocalResource.SETTING_HELP_MENU_LABEL), 99, 10) {	
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
		 LabelField t_title = new LabelField(recvMain.sm_local.getString(yblocalResource.CONNECT_OPTION_LABEL));
		 t_title.setFont(t_title.getFont().derive(Font.BOLD));
		 add(t_title);
		 
		 m_APN			= new EditField(recvMain.sm_local.getString(yblocalResource.APN_LABEL),
		 									m_mainApp.GetAPNList(),128,EditField.FILTER_DEFAULT);
		 add(m_APN);
		 
		 m_appendString	= new EditField(recvMain.sm_local.getString(yblocalResource.APPEND_STRING_LABEL),
											m_mainApp.m_appendString,128,EditField.FILTER_DEFAULT);
		 add(m_appendString);
		 
		 m_passwordKey	= new EditField(recvMain.sm_local.getString(yblocalResource.SETTING_PASSWORD_KEY),
				 							(m_mainApp.m_passwordKey.length() != 0)?"***":"",128,EditField.FILTER_DEFAULT);
		 add(m_passwordKey);
		 m_passwordKey.setChangeListener(this);
		 m_passwordKey.setFocusListener(this);
		 
		 m_pulseInterval	= new ObjectChoiceField(recvMain.sm_local.getString(yblocalResource.PULSE_INTERVAL_LABEL),
				 							recvMain.fsm_pulseIntervalString,m_mainApp.m_pulseIntervalIndex);
		 add(m_pulseInterval);
		 m_pulseInterval.setChangeListener(this);

		 m_useSSLCheckbox	= new CheckboxField(recvMain.sm_local.getString(yblocalResource.USE_SSL_LABEL),m_mainApp.m_useSSL);
		 add(m_useSSLCheckbox);
		 m_useSSLCheckbox.setChangeListener(this);
		 
		 m_uesMDS			= new CheckboxField(recvMain.sm_local.getString(yblocalResource.USE_MDS),m_mainApp.UseMDS());
		 add(m_uesMDS);
		 
		 m_useWifi			= new CheckboxField(recvMain.sm_local.getString(yblocalResource.USE_WIFI_LABEL), m_mainApp.m_useWifi);
		 add(m_useWifi);
		 
		 m_autoRun			= new CheckboxField(recvMain.sm_local.getString(yblocalResource.AUTO_RUN_CHECK_BOX), m_mainApp.m_autoRun);
		 add(m_autoRun);
		 
		 m_conDisPrompt		= new CheckboxField(recvMain.sm_local.getString(yblocalResource.SETTING_CONNECT_DISCONNECT_PROMPT), m_mainApp.m_connectDisconnectPrompt);
		 add(m_conDisPrompt);
		 
		 m_popupDlgWhenDisconnect = new CheckboxField(recvMain.sm_local.getString(yblocalResource.SETTING_DISCONNECT_PROMPT), m_mainApp.m_popupDlgWhenDisconnect);
		 add(m_popupDlgWhenDisconnect);
		 
		 m_hideBackgroundIcon = new CheckboxField(recvMain.sm_local.getString(yblocalResource.SETTING_HIDE_BG_ICON), m_mainApp.m_hideBackgroundIcon);
		 add(m_hideBackgroundIcon);
		 
		 //@}
		 
		 add(new SeparatorField());
		 
		 //@{ mail operation
		 t_title = new LabelField(recvMain.sm_local.getString(yblocalResource.SETTING_MAIL_OP));
		 t_title.setFont(t_title.getFont().derive(Font.BOLD));
		 add(t_title);
		 
		 m_discardOrgText	= new CheckboxField(recvMain.sm_local.getString(yblocalResource.SETTING_DISCARD_ORG_TEXT), m_mainApp.m_discardOrgText);
		 add(m_discardOrgText);
		 
		 m_delRemoteMail	= new CheckboxField(recvMain.sm_local.getString(yblocalResource.SETTING_DELETE_REMOTE_MAIL),m_mainApp.m_delRemoteMail);
		 add(m_delRemoteMail);
		 
		 m_markReadMailSvr	= new CheckboxField(recvMain.sm_local.getString(yblocalResource.SETTING_MARK_REAE_REMOTE_MAIL),m_mainApp.m_markReadMailInSvr);
		 add(m_markReadMailSvr);
		 
		 m_copyToSentFolder	= new CheckboxField(recvMain.sm_local.getString(yblocalResource.SETTING_COPY_MAIL_TO_SENT_FOLDER),m_mainApp.m_copyMailToSentFolder);
		 add(m_copyToSentFolder);
		 
		 m_mailUseLocation = new CheckboxField(recvMain.sm_local.getString(yblocalResource.SETTING_MAIL_USER_LOCATION),m_mainApp.m_mailUseLocation);
		 add(m_mailUseLocation);
		 
		 if(!recvMain.fsm_OS_version.startsWith("4.")){
			 m_mailHtmlShow			 = new CheckboxField(recvMain.sm_local.getString(yblocalResource.SETTING_SHOW_HTML_DIRECTLY),m_mainApp.m_mailHtmlShow);
			 add(m_mailHtmlShow);
			 m_mailHtmlShow.setChangeListener(this);
			 
			 m_mailHtmlShowOnlyWIFI = new CheckboxField(recvMain.sm_local.getString(yblocalResource.SETTING_SHOW_HTML_DIRECTLY_ONLY_WIFI),m_mainApp.m_mailHtmlShowOnlyWIFI);
			 			 
			 if(m_mainApp.m_mailHtmlShow){
				 add(m_mailHtmlShowOnlyWIFI);
			 }else{
				 add(m_mailHtmlShowOnlyWIFINull);
			 }
		 }
		 
		 m_closeMailSendModule = new CheckboxField(recvMain.sm_local.getString(yblocalResource.SETTING_CLOSE_SEND_MAIL),m_mainApp.m_closeMailSendModule);
		 add(m_closeMailSendModule);
		 m_closeMailSendModule.setChangeListener(this);
		 
		 m_recvMsgTextLength = new ObjectChoiceField(recvMain.sm_local.getString(yblocalResource.MESSAGE_CONTAIN_MAX_LENGTH),
				 					recvMain.fsm_recvMaxTextLenghtString,m_mainApp.m_recvMsgTextLengthIndex);
		 add(m_recvMsgTextLength);
		 
		 add(m_changeSignature);
		 m_changeSignature.setChangeListener(this);
		 		 
		 add(new LabelField(recvMain.sm_local.getString(yblocalResource.SETTING_DEFAULT_MAIL_ACCOUNT)));
		 add(m_sendMailAccountList);
		 add(m_requestMailAccountBut);
		 m_requestMailAccountBut.setChangeListener(this);
		 
		 refreshMailAccountList();
		 
		 m_popupDlgWhenComposeNew = new CheckboxField(recvMain.sm_local.getString(yblocalResource.SETTING_POPUP_DLG_COMPOSE_NEW),m_mainApp.m_popupDlgWhenComposeNew);
		 add(m_popupDlgWhenComposeNew);
		 //@}
		 
		 
		 add(new SeparatorField());
		 
		 //@{ weibo op
		 t_title = new LabelField(recvMain.sm_local.getString(yblocalResource.SETTING_WEIBO_OP));
		 t_title.setFont(t_title.getFont().derive(Font.BOLD));
		 add(t_title);
		 
		 m_weiboModule = new CheckboxField(recvMain.sm_local.getString(yblocalResource.SETTING_WEIBO_OP_ENABLE),
				 								m_mainApp.m_enableWeiboModule);
		 add(m_weiboModule);
		 m_weiboModule.setChangeListener(this);
		 
		 add(m_weiboNullField);
		 
		 if(m_mainApp.m_enableWeiboModule){
			 enableWeiboSet(true);
		 }		 
		 //@}
		 
		 add(new SeparatorField());
		 
		 //@{ im op
		 t_title = new LabelField(recvMain.sm_local.getString(yblocalResource.SETTING_IM_OP));
		 t_title.setFont(t_title.getFont().derive(Font.BOLD));
		 add(t_title);
		 
		 m_imModule = new CheckboxField(recvMain.sm_local.getString(yblocalResource.SETTING_IM_OP_ENABLE),m_mainApp.m_enableIMModule);
		 add(m_imModule);
		 m_imModule.setChangeListener(this);
		 
		 add(m_imNullField);
		 if(m_mainApp.m_enableIMModule){
			 enableIMSet(true);
		 }
		 //
		 		 
		 add(new SeparatorField());		 
		 
		 //@{ reminder option
		 t_title = new LabelField(recvMain.sm_local.getString(yblocalResource.PROMPT_OPTION_LABEL));
		 t_title.setFont(t_title.getFont().derive(Font.BOLD));
		 add(t_title);
		 
		 m_fulldayPrompt 	= new CheckboxField(recvMain.sm_local.getString(yblocalResource.FULLDAY_PROMT_LABEL),m_mainApp.m_fulldayPrompt);
		 add(m_fulldayPrompt);
		 
		 m_startPromptHour	= new NumericChoiceField(recvMain.sm_local.getString(yblocalResource.START_HOUR_PROMPT_LABEL),0,24,1,m_mainApp.m_startPromptHour);
		 add(m_startPromptHour);
		 
		 m_endPromptHour	= new NumericChoiceField(recvMain.sm_local.getString(yblocalResource.END_HOUR_PROMPT_LABEL),0,24,1,m_mainApp.m_endPromptHour);
		 add(m_endPromptHour);
		 //@}
		 
		 add(new SeparatorField());
		 
		 //@{ other option
		 t_title = new LabelField(recvMain.sm_local.getString(yblocalResource.LOCATION_OPTIION_LABEL));
		 t_title.setFont(t_title.getFont().derive(Font.BOLD));
		 add(t_title);
		 
		 m_useLocationInfo	= new CheckboxField(recvMain.sm_local.getString(yblocalResource.USE_LOCATION_LABEL), m_mainApp.m_useLocationInfo);
		 add(m_useLocationInfo);
		 
		 add(m_longitude);
		 add(m_latitude);
		 
		 RefreshLocation();
		 
		 //@}
		 
		 add(new SeparatorField());
		 
		 //@{ upload and download bytes statistics
		 t_title = new LabelField(recvMain.sm_local.getString(yblocalResource.BYTE_STATISTICS));
		 t_title.setFont(t_title.getFont().derive(Font.BOLD));
		 add(t_title);
		 
		 add(m_sendRecvMailNum);
		 add(m_sentRecvWeiboNum);	 
		 add(m_uploadDownloadByte);
		 add(m_totalByte);
		 
		 add(m_clearByteBut);
		 add(m_copyStatBut);
		 m_clearByteBut.setChangeListener(this);
		 m_copyStatBut.setChangeListener(this);
		 
		 if(m_mainApp.m_connectDeamon.m_connect != null){
			 m_mainApp.m_connectDeamon.m_connect.StoreUpDownloadByteImm(true);
		 }
		 
		 RefreshUpDownloadByte();
		 //@}
		 
		 setTitle(new LabelField(recvMain.sm_local.getString(yblocalResource.ADVANCE_SETTING_TITEL_LABEL),LabelField.ELLIPSIS | LabelField.USE_ALL_WIDTH));
		 
	 }
	 	 
	 private void enableWeiboSet(boolean _enable){
		 if(_enable){
			 replace(m_weiboNullField,m_weiboSettingPrompt);
		 }else{
			 replace(m_weiboSettingPrompt,m_weiboNullField);
		 }
	 }
	 
	 private void enableIMSet(boolean _enable){
		 if(_enable){
			 replace(m_imNullField,m_imSettingPrompt);
		 }else{
			 replace(m_imSettingPrompt,m_imNullField);
		 }
	 }
	 
	 public void fieldChanged(Field field, int context) {
		if(context != FieldChangeListener.PROGRAMMATIC){
			// Perform action if user changed field. 
			//
			if(field == m_clearByteBut){
				if(Dialog.ask(Dialog.D_YES_NO,recvMain.sm_local.getString(yblocalResource.CLEAR_STATISTICS_PROMPT),Dialog.NO) == Dialog.YES){
					m_mainApp.ClearUpDownloadByte();
					
					m_mainApp.SetSendMailNum(0);
					m_mainApp.SetRecvMailNum(0);
					
					m_mainApp.m_receivedWeiboNum = 0;
					m_mainApp.m_sentWeiboNum = 0;
					
					RefreshUpDownloadByte();
				}
			}else if(field == m_copyStatBut){
				StringBuffer t_string = new StringBuffer();
				 
				t_string.append(recvMain.sm_local.getString(yblocalResource.BYTE_STATISTICS)).append("\n")
						.append(m_sendRecvMailNum.getText()).append("\n")
						.append(m_sentRecvWeiboNum.getText()).append("\n")
						.append(m_uploadDownloadByte.getText()).append("\n")
						.append(m_totalByte.getText()).append("\n");
						
				
				Clipboard.getClipboard().put(t_string.toString());
				m_mainApp.DialogAlert(recvMain.sm_local.getString(yblocalResource.COPY_TO_CLIPBOARD_SUCC));
				
			}else if(field == m_pulseInterval){
				if(m_pulseInterval.getSelectedIndex() == 0){
					m_mainApp.DialogAlert(recvMain.sm_local.getString(yblocalResource.PULSE_INTERVAL_TOO_SHORT_PROMPT));
				}
			}else if(field == m_changeSignature){
				m_mainApp.DialogAlert(recvMain.sm_local.getString(yblocalResource.CHANGE_SIGNATURE_PROMPT_TEXT));
			}else if(field == m_passwordKey){			
				m_hasChangePasswordKey = true;
			}else if(field == m_useSSLCheckbox){
				if(m_useSSLCheckbox.getChecked()){
					m_mainApp.DialogAlert(recvMain.sm_local.getString(yblocalResource.SETTING_ENABLE_SSL_ENABLE));
				}
			}else if(field == m_weiboModule){
				enableWeiboSet(m_weiboModule.getChecked());
			}else if(field == m_requestMailAccountBut){
				m_mainApp.m_connectDeamon.sendRequestMailAccountMsg();
			}else if(field == m_imModule){
				enableIMSet(m_imModule.getChecked());
			}else if(field == m_closeMailSendModule){
				if(m_closeMailSendModule.getChecked()){
					m_mainApp.DialogAlert(yblocalResource.SETTING_DISABLE_MAIL_PROMPT);
				}
			}else if(field == m_mailHtmlShow){
				if(m_mailHtmlShow.getChecked()){
					m_mainApp.DialogAlert(yblocalResource.SETTING_SHOW_HTML_DIRECTLY_PROMPT);
					replace(m_mailHtmlShowOnlyWIFINull, m_mailHtmlShowOnlyWIFI);
				}else{
					replace(m_mailHtmlShowOnlyWIFI, m_mailHtmlShowOnlyWIFINull);
				}
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
	 
	 private RadioButtonGroup m_sendMailAccountGroup = new RadioButtonGroup();
	 
	 public void refreshMailAccountList(){
		 
		m_mainApp.invokeLater(new Runnable() {
			public void run() {
				m_sendMailAccountList.deleteAll();
				Vector	t_list = m_mainApp.m_sendMailAccountList;
				
				if(m_mainApp.m_defaultSendMailAccountIndex >= t_list.size()){
					m_mainApp.m_defaultSendMailAccountIndex = 0;
				}
				
				for(int i = 0 ;i < t_list.size();i++){
					String t_name = (String)t_list.elementAt(i);
					 
					RadioButtonField t_acc = new RadioButtonField(t_name, m_sendMailAccountGroup, i == m_mainApp.m_defaultSendMailAccountIndex);
					 
					m_sendMailAccountList.add(t_acc);
				}		
			}
		});
		 
	 }
	 
	 public boolean onClose(){
		
		if(m_startPromptHour.getSelectedIndex() < m_endPromptHour.getSelectedIndex()){
			m_mainApp.m_startPromptHour	= m_startPromptHour.getSelectedIndex();
			m_mainApp.m_endPromptHour	= m_endPromptHour.getSelectedIndex();
		}else{
			m_mainApp.DialogAlert(recvMain.sm_local.getString(yblocalResource.START_BIGGER_THAN_END_PROMPT));
			return false;
		}
	 
		m_mainApp.m_useSSL	= m_useSSLCheckbox.getChecked();
				
		m_mainApp.SetAPNName(m_APN.getText());
		m_mainApp.m_autoRun = m_autoRun.getChecked();
		m_mainApp.m_connectDisconnectPrompt = m_conDisPrompt.getChecked();
		m_mainApp.m_popupDlgWhenDisconnect = m_popupDlgWhenDisconnect.getChecked();
		
		m_mainApp.m_appendString 		= m_appendString.getText();
		m_mainApp.m_useWifi 			= m_useWifi.getChecked();
		
		m_mainApp.m_pulseIntervalIndex	= m_pulseInterval.getSelectedIndex();
		m_mainApp.m_useLocationInfo		= m_useLocationInfo.getChecked();
		
		m_mainApp.m_useMDS				= m_uesMDS.getChecked();
		m_mainApp.m_fulldayPrompt		= m_fulldayPrompt.getChecked();				
		
		m_mainApp.m_discardOrgText 			= m_discardOrgText.getChecked();
		m_mainApp.m_delRemoteMail			= m_delRemoteMail.getChecked();
		m_mainApp.m_markReadMailInSvr		= m_markReadMailSvr.getChecked();
		m_mainApp.m_recvMsgTextLengthIndex	= m_recvMsgTextLength.getSelectedIndex();
		m_mainApp.m_copyMailToSentFolder	= m_copyToSentFolder.getChecked();
		m_mainApp.m_mailUseLocation			= m_mailUseLocation.getChecked();
		m_mainApp.m_hideBackgroundIcon		= m_hideBackgroundIcon.getChecked();
		
		if(m_mailHtmlShow != null){
			m_mainApp.m_mailHtmlShow			= m_mailHtmlShow.getChecked();
			m_mainApp.m_mailHtmlShowOnlyWIFI	= m_mailHtmlShowOnlyWIFI.getChecked();
		}		
		
		boolean t_formerClose = m_mainApp.m_closeMailSendModule;
		m_mainApp.m_closeMailSendModule = m_closeMailSendModule.getChecked();
		if(t_formerClose != m_mainApp.m_closeMailSendModule){
			
			try{

				if(m_mainApp.m_closeMailSendModule){
					m_mainApp.m_connectDeamon.EndListener();
				}else{
					m_mainApp.m_connectDeamon.BeginListener();
				}
				
			}catch(Exception e){
				m_mainApp.DialogAlert("Close Mail Module Exception:"+e.getMessage());
			}
		}
		
		// set the default mail
		//
		int t_accountNum = m_sendMailAccountList.getFieldCount();
		for(int i = 0 ;i < t_accountNum;i++){
			RadioButtonField acc = (RadioButtonField)m_sendMailAccountList.getField(i);
			if(acc.isSelected()){
				m_mainApp.m_defaultSendMailAccountIndex = i;
				break;
			}
		}
		
		m_mainApp.m_popupDlgWhenComposeNew = m_popupDlgWhenComposeNew.getChecked();
		
		if(m_mainApp.m_enableWeiboModule != m_weiboModule.getChecked()){
			
			boolean t_hasEnabled = m_mainApp.m_enableWeiboModule;
			
			m_mainApp.m_enableWeiboModule = m_weiboModule.getChecked();
			
			if(m_mainApp.m_enableWeiboModule && !t_hasEnabled){
				m_mainApp.InitWeiboModule();
			}
			
			if(m_mainApp.m_enableWeiboModule == false){
				m_mainApp.DisableWeiboModule();
			}
			
			if(m_mainApp.m_connectDeamon.IsConnectState()){
				ByteArrayOutputStream t_os = new ByteArrayOutputStream();
				try{
					t_os.write(msg_head.msgWeiboEnable);
					sendReceive.WriteBoolean(t_os,m_mainApp.m_enableWeiboModule);
					m_mainApp.m_connectDeamon.addSendingData(msg_head.msgWeiboEnable, t_os.toByteArray(), true);	
					
					t_os.close();
				}catch(Exception e){}
						
				t_os = null;
			}
		}
		
		if(m_mainApp.m_enableIMModule != m_imModule.getChecked()){
			
			boolean t_hasEnabled = m_mainApp.m_enableIMModule;
			
			m_mainApp.m_enableIMModule = m_imModule.getChecked();
			
			if(m_mainApp.m_enableIMModule && !t_hasEnabled){
				m_mainApp.initIMModule();
			}
			
			if(m_mainApp.m_connectDeamon.IsConnectState()){
				ByteArrayOutputStream t_os = new ByteArrayOutputStream();
				try{
					t_os.write(msg_head.msgChatEnable);
					sendReceive.WriteBoolean(t_os,m_mainApp.m_enableIMModule);
					m_mainApp.m_connectDeamon.addSendingData(msg_head.msgChatEnable, t_os.toByteArray(), true);	
					
					t_os.close();
					
				}catch(Exception e){}
						
				t_os = null;
			}
		}
		
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
				m_sendRecvMailNum.setText(recvMain.sm_local.getString(yblocalResource.SETTING_SEND_RECV_MAIL_NUM) + 
						m_mainApp.GetSendMailNum() + " / " + m_mainApp.GetRecvMailNum());
								
				m_sentRecvWeiboNum.setText(recvMain.sm_local.getString(yblocalResource.SETTING_WEIBO_SENT_RECV_NUM) + 
						m_mainApp.m_sentWeiboNum + " / " + m_mainApp.m_receivedWeiboNum);
				
				m_uploadDownloadByte.setText(recvMain.sm_local.getString(yblocalResource.SETTING_UPLOAD_DOWNLOAD_STATISTICS) + 
						recvMain.GetByteStr(m_mainApp.m_uploadByte) + " / " + recvMain.GetByteStr(m_mainApp.m_downloadByte));
				
				m_totalByte.setText(recvMain.sm_local.getString(yblocalResource.TOTAL_STATISTICS) + 
						recvMain.GetByteStr(m_mainApp.m_downloadByte + m_mainApp.m_uploadByte));
			}
		});
	 }
	 
	 public void RefreshLocation(){
		 m_mainApp.invokeLater(new Runnable() {
			public void run() {
				m_longitude.setText(recvMain.sm_local.getString(yblocalResource.CURRENT_LONGITUDE_LABEL) + m_mainApp.m_gpsInfo.m_longitude);
				m_latitude.setText(recvMain.sm_local.getString(yblocalResource.CURRENT_LATITUDE_LABEL) + m_mainApp.m_gpsInfo.m_latitude);
			}
		});
	 }
	 
	 protected boolean keyDown(int keycode,int time){
		 
		if(m_APN.isFocus()){
			final int key = Keypad.key(keycode);
			 
			if(key == ' ' || key == 10){
				return true;
			}
		}
		
		return super.keyDown(keycode,time);
	 }
}
