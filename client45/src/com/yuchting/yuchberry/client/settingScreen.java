package com.yuchting.yuchberry.client;

import local.localResource;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.CheckboxField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ObjectChoiceField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.MainScreen;

public class settingScreen extends MainScreen implements FieldChangeListener{
	
	 EditField			m_APN			= null;
	 EditField			m_appendString	= null;
	 CheckboxField		m_useSSLCheckbox= null;
	 CheckboxField		m_useWifi		= null;
	 CheckboxField		m_autoRun		= null;
	 ObjectChoiceField	m_pulseInterval	= null;
	 
	 LabelField			m_uploadByte	= new LabelField();
	 LabelField			m_downloadByte	= new LabelField();
	 ButtonField		m_clearByteBut	= new ButtonField(recvMain.sm_local.getString(localResource.CLEAR_STATISTICS),Field.FIELD_RIGHT);
	 	 
	 recvMain			m_mainApp		= null;
	 
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
		 
		 m_useSSLCheckbox	= new CheckboxField(recvMain.sm_local.getString(localResource.USE_SSL_LABEL),m_mainApp.m_useSSL);
		 add(m_useSSLCheckbox);
		 
		 m_useWifi			= new CheckboxField(recvMain.sm_local.getString(localResource.USE_WIFI_LABEL), m_mainApp.m_useWifi);
		 add(m_useWifi);
		 
		 m_autoRun			= new CheckboxField(recvMain.sm_local.getString(localResource.AUTO_RUN_CHECK_BOX), m_mainApp.m_autoRun);
		 add(m_autoRun);
		 
		 m_pulseInterval	= new ObjectChoiceField(recvMain.sm_local.getString(localResource.PULSE_INTERVAL_LABEL),
				 				recvMain.fsm_pulseIntervalString,m_mainApp.m_pulseIntervalIndex);
		 add(m_pulseInterval);
		 //@}
		 
		 add(new SeparatorField());
		 
		 //@{ upload and download bytes statistics
		 t_title = new LabelField(recvMain.sm_local.getString(localResource.BYTE_STATISTICS));
		 t_title.setFont(t_title.getFont().derive(Font.BOLD));
		 add(t_title);
		 
		 add(m_uploadByte);
		 add(m_downloadByte);
		 add(m_clearByteBut);
		 m_clearByteBut.setChangeListener(this);
		 
		 if(m_mainApp.m_connectDeamon.m_connect != null){
			 m_mainApp.m_connectDeamon.m_connect.StoreUpDownloadByteImm();
		 }
		 
		 RefreshUpDownloadByte();
		 //@}
		 
		 add(new SeparatorField());
		 
		 
		 //@{ reminder option
		 t_title = new LabelField(recvMain.sm_local.getString(localResource.PROMPT_OPTION_LABEL));
		 t_title.setFont(t_title.getFont().derive(Font.BOLD));
		 add(t_title);
		 //@}
		 
	 }
	 public void fieldChanged(Field field, int context) {
	    if(context != FieldChangeListener.PROGRAMMATIC){
			// Perform action if user changed field. 
			//
			if(field == m_clearByteBut){
				if(Dialog.ask(Dialog.D_YES_NO,recvMain.sm_local.getString(localResource.CLEAR_STATISTICS_PROMPT),Dialog.NO) == Dialog.YES){
					m_mainApp.ClearUpDownloadByte();
					RefreshUpDownloadByte();
				}
			}
	    }else{
	    	// Perform action if application changed field.
	    }
	}
	 
	 public boolean onClose(){
		 
		m_mainApp.m_useSSL	= m_useSSLCheckbox.getChecked();
		m_mainApp.SetAPNName(m_APN.getText());
		m_mainApp.m_autoRun = m_autoRun.getChecked();
		
		m_mainApp.m_appendString = m_appendString.getText();
		m_mainApp.m_useWifi = m_useWifi.getChecked();
		
		m_mainApp.m_pulseIntervalIndex = m_pulseInterval.getSelectedIndex();
		
		m_mainApp.WriteReadIni(false);
		
		close();
		return true;
	 }
	 
	 public void RefreshUpDownloadByte(){
		 m_mainApp.invokeLater(new Runnable() {
			public void run() {
				m_uploadByte.setText(recvMain.sm_local.getString(localResource.UPLOAD_STATISTICS) + recvMain.GetByteStr(m_mainApp.m_uploadByte));
				m_downloadByte.setText(recvMain.sm_local.getString(localResource.DOWNLOAD_STATISTICS) + recvMain.GetByteStr(m_mainApp.m_downloadByte));
			}
		});
	 }
}
