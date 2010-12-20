package com.yuchting.yuchberry.client;

import local.localResource;
import net.rim.device.api.i18n.SimpleDateFormat;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.component.CheckboxField;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.NumericChoiceField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.MainScreen;

public class settingScreen extends MainScreen {
	
	 EditField			m_APN			= null;
	 EditField			m_appendString	= null;
	 CheckboxField		m_useSSLCheckbox= null;
	 CheckboxField		m_useWifi		= null;
	 CheckboxField		m_autoRun		= null;
	 
	 NumericChoiceField	m_vibrateList	= null;
	 NumericChoiceField	m_soundList		= null;
	 
	 recvMain			m_mainApp		= null;
	 
	 public settingScreen(recvMain _app){
		 m_mainApp = _app;
		 
		 //@{ connection option
		 add(new LabelField(recvMain.sm_local.getString(localResource.CONNECT_OPTION_LABEL)));
		 
		 m_APN			= new EditField(recvMain.sm_local.getString(localResource.APN_LABEL),
		 									m_mainApp.GetAPNList(),128,EditField.FILTER_DEFAULT);
		 add(m_APN);
		 
		 m_appendString	= new EditField(recvMain.sm_local.getString(localResource.APPEND_STRING_LABEL),
											m_mainApp.m_appendString,128,EditField.FILTER_DEFAULT);
		 add(m_appendString);
		 
		 m_useSSLCheckbox	= new CheckboxField(recvMain.sm_local.getString(localResource.USE_SSL_LABEL), m_mainApp.m_useSSL);
		 add(m_useSSLCheckbox);
		 
		 m_useWifi			= new CheckboxField(recvMain.sm_local.getString(localResource.USE_WIFI_LABEL), m_mainApp.m_useWifi);
		 add(m_useWifi);
		 
		 m_autoRun			= new CheckboxField(recvMain.sm_local.getString(localResource.AUTO_RUN_CHECK_BOX), m_mainApp.m_autoRun);
		 add(m_autoRun);
		 //@}
		 
		 
		 add(new SeparatorField());
		 
		 
		 //@{ reminder option
		 add(new LabelField(recvMain.sm_local.getString(localResource.PROMPT_OPTION_LABEL)));
		 m_vibrateList = new NumericChoiceField(recvMain.sm_local.getString(localResource.VIBRATE_OPTION_LABEL), 0, 3, 1);
		 m_vibrateList.setSelectedValue(m_mainApp.m_vibrateTime);
		 add(m_vibrateList);
		 
		 m_soundList	= new NumericChoiceField(recvMain.sm_local.getString(localResource.SOUND_OPTION_LABEL), 0, 5, 1);
		 m_soundList.setSelectedValue(m_mainApp.m_soundVol);
		 add(m_soundList);		 
		 //@}
		 
	 }
	 
	 public boolean onClose(){
		 
		m_mainApp.m_useSSL	= m_useSSLCheckbox.getChecked();
		m_mainApp.SetAPNName(m_APN.getText());
		m_mainApp.m_soundVol = m_soundList.getSelectedValue();
		m_mainApp.m_vibrateTime = m_vibrateList.getSelectedValue();
		m_mainApp.m_autoRun = m_autoRun.getChecked();
		
		m_mainApp.m_appendString = m_appendString.getText();
		m_mainApp.m_useWifi = m_useWifi.getChecked();
		
		m_mainApp.WriteReadIni(false);
		
		close();
		return true;
	 }
}
