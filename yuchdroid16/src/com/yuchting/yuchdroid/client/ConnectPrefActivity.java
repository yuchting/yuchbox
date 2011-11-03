package com.yuchting.yuchdroid.client;

import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.text.method.PasswordTransformationMethod;

import com.yuchting.yuchdroid.client.ui.TimePickerPreference;

public class ConnectPrefActivity extends PreferenceActivity {

	PreferenceManager 	m_prefMgr		= null;
	
	CheckBoxPreference	m_useSSL	= null;
	CheckBoxPreference	m_autoRun	= null;
	EditTextPreference	m_cryptKey	= null;
	ListPreference		m_pushInterval = null;
	
	CheckBoxPreference	m_promptWholeDay = null;
	TimePickerPreference m_promptStart	= null;
	TimePickerPreference m_promptEnd	= null;
	CheckBoxPreference	m_promptWhenDisconnect = null;
	
	Preference			m_statistics	= null;
	
	ConfigInit			m_config		= null;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.login_preference);
		
		m_config	= ((YuchDroidApp)getApplicationContext()).m_config;
		m_prefMgr = getPreferenceManager();
		
		m_useSSL	= (CheckBoxPreference)m_prefMgr.findPreference("config_use_ssl");
		m_autoRun	= (CheckBoxPreference)m_prefMgr.findPreference("config_auto_run");
		m_cryptKey	= (EditTextPreference)m_prefMgr.findPreference("config_pass_key");
		m_cryptKey.getEditText().setTransformationMethod(new PasswordTransformationMethod());
		m_pushInterval = (ListPreference)m_prefMgr.findPreference("config_pulse_interval");
		
		m_promptWholeDay		= (CheckBoxPreference)m_prefMgr.findPreference("config_prompt_all_day");
		m_promptStart			= (TimePickerPreference)m_prefMgr.findPreference("config_prompt_start");
		m_promptEnd				= (TimePickerPreference)m_prefMgr.findPreference("config_prompt_end");
		m_promptWhenDisconnect	= (CheckBoxPreference)m_prefMgr.findPreference("config_prompt_disconnect");
		
		m_statistics			= m_prefMgr.findPreference("config_network_stat");
		
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
		
		updateData(true);
	}
	
	private void updateData(boolean _getOrSet){
		
		if(_getOrSet){
			// set the values to preference control 
			//
			m_useSSL.setChecked(m_config.m_useSSL);
			m_autoRun.setChecked(m_config.m_autoRun);
			m_cryptKey.getEditText().setText(m_config.m_passwordKey);
			m_pushInterval.setValueIndex(m_config.m_pulseIntervalIndex);
			
			m_promptWholeDay.setChecked(m_config.m_fulldayPrompt);
			m_promptStart.setDefaultValue(Integer.toString(m_config.m_startPromptHour & 0x0000ffff) + ":" + (m_config.m_startPromptHour >>> 16));
			m_promptEnd.setDefaultValue(Integer.toString(m_config.m_endPromptHour & 0x0000ffff) + ":" + (m_config.m_endPromptHour >>> 16));
			m_promptStart.setEnabled(!m_config.m_fulldayPrompt);
			m_promptEnd.setEnabled(!m_config.m_fulldayPrompt);
			
			m_promptWhenDisconnect.setChecked(m_config.m_connectDisconnectPrompt);
			
			StringBuffer t_statString = new StringBuffer();
			t_statString.append(getString(R.string.login_pref_statistics_up)).append(YuchDroidApp.GetByteStr(m_config.m_uploadByte)).append(" ")
						.append(getString(R.string.login_pref_statistics_down)).append(YuchDroidApp.GetByteStr(m_config.m_downloadByte)).append(" ")
						.append(getString(R.string.login_pref_statistics_total)).append(YuchDroidApp.GetByteStr(m_config.m_uploadByte + m_config.m_downloadByte));
			m_statistics.setSummary(t_statString.toString());
			
		}else{
			m_config.m_useSSL 		= m_useSSL.isChecked();
			m_config.m_autoRun		= m_autoRun.isChecked();
			m_config.m_passwordKey	= m_cryptKey.getEditor().toString();
			m_config.m_pulseIntervalIndex = m_pushInterval.findIndexOfValue(m_pushInterval.getValue());
			
			m_config.m_fulldayPrompt	= m_promptWholeDay.isChecked();
			m_config.m_startPromptHour	= m_promptStart.getHour() | (m_promptStart.getMinute() << 16);
			m_config.m_endPromptHour	= m_promptEnd.getHour() | (m_promptEnd.getMinute() << 16);
			m_config.m_connectDisconnectPrompt = m_promptWhenDisconnect.isChecked();
			
			m_config.WriteReadIni(false);
		}
	}
	
	public void onStop(){
		super.onStop();
		updateData(false);
	}
}
