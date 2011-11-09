package com.yuchting.yuchdroid.client.mail;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;

import com.yuchting.yuchdroid.client.ConfigInit;
import com.yuchting.yuchdroid.client.ConnectPrefActivity;
import com.yuchting.yuchdroid.client.GlobalDialog;
import com.yuchting.yuchdroid.client.R;
import com.yuchting.yuchdroid.client.YuchDroidApp;

public class MailPrefActivity extends PreferenceActivity {

	ListPreference			m_defaultAccount;
	CheckBoxPreference		m_discardOrgMail;
	CheckBoxPreference		m_delRemoteMail;
	CheckBoxPreference		m_copyToFolder;
	
	CheckBoxPreference		m_vibrate;
	RingtonePreference		m_sound;
	
	Preference				m_statistics;
			
	ConfigInit				m_config;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.mail_preference);
		
		m_config	= ((YuchDroidApp)getApplicationContext()).m_config;
		PreferenceManager t_prefMgr = getPreferenceManager();
		
		m_defaultAccount	= (ListPreference)t_prefMgr.findPreference("config_mail_own_account");
		m_discardOrgMail	= (CheckBoxPreference)t_prefMgr.findPreference("config_mail_discard_org_mail");
		m_delRemoteMail		= (CheckBoxPreference)t_prefMgr.findPreference("config_mail_del_remote");
		m_copyToFolder		= (CheckBoxPreference)t_prefMgr.findPreference("config_mail_send_to_copy");
		
		m_vibrate			= (CheckBoxPreference)t_prefMgr.findPreference("config_mail_vibrate");
		m_sound				= (RingtonePreference)t_prefMgr.findPreference("config_mail_sound");
		
		m_statistics		= t_prefMgr.findPreference("config_mail_statistics");
		
		updateData(true);
		
		m_statistics.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				GlobalDialog.showYesNoDialog(getString(R.string.login_pref_statistics_clear_prompt),MailPrefActivity.this,
					new DialogInterface.OnClickListener(){
			
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if(which == DialogInterface.BUTTON_POSITIVE){
							m_config.m_sendMailNum = 0;
							m_config.m_recvMailNum = 0;
							
							updateData(true);
						}
					}
				});
				return true;
			}
		});
	}
	
	private void updateData(boolean _setOrGet){
		
		if(_setOrGet){

			if(!m_config.m_sendMailAccountList.isEmpty()){
				String[] t_entries = new String[m_config.m_sendMailAccountList.size()];
				for(int i = 0;i < m_config.m_sendMailAccountList.size();i++){
					t_entries[i] = m_config.m_sendMailAccountList.get(i);
				}
				
				m_defaultAccount.setEntries(t_entries);
				m_defaultAccount.setEntryValues(t_entries);
				m_defaultAccount.setValueIndex(m_config.m_defaultSendMailAccountIndex);
			}
			
			m_discardOrgMail.setChecked(m_config.m_discardOrgText);
			m_delRemoteMail.setChecked(m_config.m_delRemoteMail);
			m_copyToFolder.setChecked(m_config.m_copyMailToSentFolder);
			
			m_vibrate.setChecked(m_config.m_mailPrompt_vibrate);
			m_sound.setDefaultValue(m_config.m_mailPrompt_sound);
			
			StringBuffer t_stat = new StringBuffer();
			t_stat.append(getString(R.string.mail_pref_statistics_recv)).append(m_config.m_recvMailNum).append(" ")
					.append(getString(R.string.mail_pref_statistics_send)).append(m_config.m_sendMailNum);
			
			m_statistics.setSummary(t_stat.toString());
			
		}else{
			if(!m_config.m_sendMailAccountList.isEmpty()){
				m_config.m_defaultSendMailAccountIndex = m_defaultAccount.findIndexOfValue(m_defaultAccount.getValue());
			}
			m_config.m_discardOrgText	= m_discardOrgMail.isChecked();
			m_config.m_delRemoteMail	= m_delRemoteMail.isChecked();
			m_config.m_copyMailToSentFolder = m_copyToFolder.isChecked();
			m_config.m_mailPrompt_vibrate = m_vibrate.isChecked();
			
			SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
			String sound = prefs.getString(m_sound.getKey(), ""); 
			m_config.m_mailPrompt_sound = sound != null?sound:"";
			
			m_config.WriteReadIni(false);
		}
	}
	
	public void onStop(){
		super.onStop();
		updateData(false);
	}
}
