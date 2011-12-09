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
package com.yuchting.yuchdroid.client.mail;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;

import com.yuchting.yuchdroid.client.ConfigInit;
import com.yuchting.yuchdroid.client.GlobalDialog;
import com.yuchting.yuchdroid.client.R;
import com.yuchting.yuchdroid.client.YuchDroidApp;

public class MailPrefActivity extends PreferenceActivity {

	ListPreference			m_defaultAccount;
	CheckBoxPreference		m_discardOrgMail;
	CheckBoxPreference		m_delRemoteMail;
	CheckBoxPreference		m_copyToFolder;
	CheckBoxPreference		m_displayTextWhenHTML;
	
	CheckBoxPreference		m_vibrate;
	RingtonePreference		m_sound;
	
	Preference				m_statistics;
	ListPreference			m_mailFontSize;
			
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
		m_displayTextWhenHTML = (CheckBoxPreference)t_prefMgr.findPreference("config_mail_display_plain_text_when_html");
		
		m_vibrate			= (CheckBoxPreference)t_prefMgr.findPreference("config_mail_vibrate");
		m_sound				= (RingtonePreference)t_prefMgr.findPreference("config_mail_sound");
		
		m_statistics		= t_prefMgr.findPreference("config_mail_statistics");
		m_mailFontSize		= (ListPreference)t_prefMgr.findPreference("config_mail_font_size");
		
		updateData(true);
		
		m_statistics.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				GlobalDialog.showYesNoDialog(getString(R.string.login_pref_statistics_clear_prompt),MailPrefActivity.this,
					new GlobalDialog.YesNoListener(){
			
					@Override
					public void click() {
						
						m_config.m_sendMailNum = 0;
						m_config.m_recvMailNum = 0;
						
						updateData(true);
					
					}
				},null);
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
			m_displayTextWhenHTML.setChecked(m_config.m_displayTextWhenHTML);
			
			m_vibrate.setChecked(m_config.m_mailPrompt_vibrate);
			m_sound.setDefaultValue(m_config.m_mailPrompt_sound);
			
			StringBuffer t_stat = new StringBuffer();
			t_stat.append(getString(R.string.mail_pref_statistics_recv)).append(m_config.m_recvMailNum).append(" ")
					.append(getString(R.string.mail_pref_statistics_send)).append(m_config.m_sendMailNum);
			
			m_statistics.setSummary(t_stat.toString());
			
			m_mailFontSize.setValueIndex(m_config.m_mailFontSizeIndex);
			
		}else{
			if(!m_config.m_sendMailAccountList.isEmpty()){
				m_config.m_defaultSendMailAccountIndex = m_defaultAccount.findIndexOfValue(m_defaultAccount.getValue());
			}
			m_config.m_discardOrgText	= m_discardOrgMail.isChecked();
			m_config.m_delRemoteMail	= m_delRemoteMail.isChecked();
			m_config.m_copyMailToSentFolder = m_copyToFolder.isChecked();
			m_config.m_mailPrompt_vibrate = m_vibrate.isChecked();
			m_config.m_displayTextWhenHTML = m_displayTextWhenHTML.isChecked();
			m_config.m_mailFontSizeIndex = m_mailFontSize.findIndexOfValue(m_mailFontSize.getValue());
			
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
