package com.yuchting.yuchdroid.client;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.method.PasswordTransformationMethod;

import com.yuchting.yuchdroid.client.ui.TimePickerPreference;

public class ConnectPrefActivity extends PreferenceActivity {
	
	CheckBoxPreference	m_useSSL	= null;
	CheckBoxPreference	m_autoRun	= null;
	EditTextPreference	m_cryptKey	= null;
	ListPreference		m_pushInterval = null;
	
	CheckBoxPreference	m_promptWholeDay = null;
	TimePickerPreference m_promptStart	= null;
	TimePickerPreference m_promptEnd	= null;
	
	CheckBoxPreference	m_promptWhenDisconnect = null;	
	CheckBoxPreference	m_promptWhenDisconnect_vibrate = null;
	RingtonePreference	m_promptWhenDisconnect_sound = null;
	
	CheckBoxPreference	m_alwaysDisplayState = null;
	
	Preference			m_statistics	= null;
	
	ConfigInit			m_config		= null;
	YuchDroidApp		m_mainApp		= null;
	
	boolean			m_cryptKeyChanged = false;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.login_preference);
		
		m_mainApp	= ((YuchDroidApp)getApplicationContext());
		m_config	= m_mainApp.m_config;
		PreferenceManager t_prefMgr = getPreferenceManager();
		
		m_useSSL	= (CheckBoxPreference)t_prefMgr.findPreference("config_use_ssl");
		m_autoRun	= (CheckBoxPreference)t_prefMgr.findPreference("config_auto_run");
		m_cryptKey	= (EditTextPreference)t_prefMgr.findPreference("config_pass_key");
		m_cryptKey.getEditText().setTransformationMethod(new PasswordTransformationMethod());
		m_pushInterval = (ListPreference)t_prefMgr.findPreference("config_pulse_interval");
		
		m_promptWholeDay		= (CheckBoxPreference)t_prefMgr.findPreference("config_prompt_all_day");
		m_promptStart			= (TimePickerPreference)t_prefMgr.findPreference("config_prompt_start");
		m_promptEnd				= (TimePickerPreference)t_prefMgr.findPreference("config_prompt_end");
		m_promptWhenDisconnect	= (CheckBoxPreference)t_prefMgr.findPreference("config_prompt_disconnect");
		
		m_promptWhenDisconnect_vibrate	= (CheckBoxPreference)t_prefMgr.findPreference("config_prompt_disconnect_vibrate");
		m_promptWhenDisconnect_sound	= (RingtonePreference)t_prefMgr.findPreference("config_prompt_disconnect_sound");
		
		m_alwaysDisplayState	= (CheckBoxPreference)t_prefMgr.findPreference("config_prompt_always_display_state");
		
		m_statistics			= t_prefMgr.findPreference("config_network_stat");
		
		updateData(true);
		
		m_cryptKey.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				m_cryptKeyChanged = true;
				return true;
			}
		});
				
		m_promptWholeDay.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if(((Boolean)newValue).booleanValue()){
					m_promptStart.setEnabled(false);
					m_promptEnd.setEnabled(false);
				}else{
					m_promptStart.setEnabled(true);
					m_promptEnd.setEnabled(true);
				}
				return true;
			}
		});
			
		m_statistics.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				GlobalDialog.showYesNoDialog(getString(R.string.login_pref_statistics_clear_prompt), 
									ConnectPrefActivity.this,
									new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if(which == DialogInterface.BUTTON_POSITIVE){
							m_config.m_uploadByte = 0;
							m_config.m_downloadByte = 0;
							
							updateData(true);
						}
					}
				});
				return false;
			}
		});
		
		m_promptWhenDisconnect.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				
				m_promptWhenDisconnect_vibrate.setEnabled(((Boolean)newValue).booleanValue());
				m_promptWhenDisconnect_sound.setEnabled(((Boolean)newValue).booleanValue());
				
				return true;
			}
		});
		
		
	}
	
	private void updateData(boolean _setOrGet){
		
		if(_setOrGet){
			
			// set the values to preference control 
			//
			m_useSSL.setChecked(m_config.m_useSSL);
			m_autoRun.setChecked(m_config.m_autoRun);
			
			if(m_config.m_passwordKey.length() != 0){
				m_cryptKey.setText("000");
			}else{
				m_cryptKey.setText("");
			}			
						
			m_pushInterval.setValueIndex(m_config.m_pulseIntervalIndex);
			
			m_promptWholeDay.setChecked(m_config.m_fulldayPrompt);
			m_promptStart.setDefaultValue(Integer.toString(m_config.m_startPromptHour & 0x0000ffff) + ":" + (m_config.m_startPromptHour >>> 16));
			m_promptEnd.setDefaultValue(Integer.toString(m_config.m_endPromptHour & 0x0000ffff) + ":" + (m_config.m_endPromptHour >>> 16));
			m_promptStart.setEnabled(!m_config.m_fulldayPrompt);
			m_promptEnd.setEnabled(!m_config.m_fulldayPrompt);
			
			m_promptWhenDisconnect.setChecked(m_config.m_connectDisconnectPrompt);
						
			m_promptWhenDisconnect_vibrate.setChecked(m_config.m_connectDisconnectPrompt_vibrate);
			m_promptWhenDisconnect_sound.setDefaultValue(m_config.m_connectDisconnectPrompt_sound);
			
			m_promptWhenDisconnect_vibrate.setEnabled(m_config.m_connectDisconnectPrompt);
			m_promptWhenDisconnect_sound.setEnabled(m_config.m_connectDisconnectPrompt);
			
			m_alwaysDisplayState.setChecked(m_config.m_alwaysDisplayState);
			
			StringBuffer t_statString = new StringBuffer();
			t_statString.append(getString(R.string.login_pref_statistics_up)).append(YuchDroidApp.GetByteStr(m_config.m_uploadByte)).append(" ")
						.append(getString(R.string.login_pref_statistics_down)).append(YuchDroidApp.GetByteStr(m_config.m_downloadByte)).append(" ")
						.append(getString(R.string.login_pref_statistics_total)).append(YuchDroidApp.GetByteStr(m_config.m_uploadByte + m_config.m_downloadByte));
			m_statistics.setSummary(t_statString.toString());
			
		}else{
			
			m_config.m_useSSL 		= m_useSSL.isChecked();
			m_config.m_autoRun		= m_autoRun.isChecked();
			if(m_cryptKeyChanged){
				m_config.m_passwordKey	= YuchDroidApp.md5(m_cryptKey.getText());
				m_cryptKeyChanged = false;
			}			
			m_config.m_pulseIntervalIndex = m_pushInterval.findIndexOfValue(m_pushInterval.getValue());
			
			m_config.m_fulldayPrompt	= m_promptWholeDay.isChecked();
			m_config.m_startPromptHour	= m_promptStart.getHour() | (m_promptStart.getMinute() << 16);
			m_config.m_endPromptHour	= m_promptEnd.getHour() | (m_promptEnd.getMinute() << 16);
			m_config.m_connectDisconnectPrompt = m_promptWhenDisconnect.isChecked();
			m_config.m_connectDisconnectPrompt_vibrate = m_promptWhenDisconnect_vibrate.isChecked();
			
			SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
			String sound = prefs.getString(m_promptWhenDisconnect_sound.getKey(), ""); 
			m_config.m_connectDisconnectPrompt_sound = sound != null?sound:"";
			
			boolean t_formerState = m_config.m_alwaysDisplayState;
			m_config.m_alwaysDisplayState	= m_alwaysDisplayState.isChecked();
			if(t_formerState != m_config.m_alwaysDisplayState){
				m_mainApp.startConnectNotification(m_mainApp.getConnectState(),false);
			}
			
			m_config.WriteReadIni(false);
		}
	}
	
	public void onStop(){
		super.onStop();
		updateData(false);
	}
}
