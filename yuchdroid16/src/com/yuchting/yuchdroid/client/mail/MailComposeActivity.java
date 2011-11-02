package com.yuchting.yuchdroid.client.mail;

import java.util.Date;
import java.util.Vector;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

import com.yuchting.yuchdroid.client.GlobalDialog;
import com.yuchting.yuchdroid.client.R;
import com.yuchting.yuchdroid.client.YuchDroidApp;

public class MailComposeActivity extends Activity implements View.OnClickListener{

	public final static String	COMPOSE_MAIL_STYLE = "style";
	public final static String	COMPOSE_MAIL_GROUP_ID = "groupId";
	public final static String	COMPOSE_MAIL_DRAFT = "draft";
	
	AutoCompleteTextView	m_to	= null;
	AutoCompleteTextView	m_cc	= null;
	AutoCompleteTextView	m_bcc	= null;
	
	EditText	m_subject			= null;
	EditText	m_body				= null;
	
	TextView	m_discardRefView	= null;
	
	Button		m_sendBtn			= null;
	Button		m_saveBtn			= null;
	Button		m_discardBtn		= null;
	Spinner		m_ownAccountSpinner	= null;
	
	LinearLayout	m_mainView		= null;
	
	boolean		m_modified		= false;
	
	MailOpenActivity.Envelope	m_referenceMail	= null;
	YuchDroidApp	m_mainApp		= null;
	
	fetchMail		m_draftMail		= null;
	
	int			m_referenceMailStyle = fetchMail.NOTHING_STYLE;
	long		m_referenceGroupId 	= -1;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mail_compose);
		
		m_mainApp = (YuchDroidApp)getApplicationContext();
		
		m_to		= (AutoCompleteTextView)findViewById(R.id.mail_compose_to);
		m_cc		= (AutoCompleteTextView)findViewById(R.id.mail_compose_cc);
		m_bcc		= (AutoCompleteTextView)findViewById(R.id.mail_compose_bcc);
		
		m_subject	= (EditText)findViewById(R.id.mail_compose_subject);
		m_body		= (EditText)findViewById(R.id.mail_compose_body);
		
		m_mainView 	= (LinearLayout)findViewById(R.id.mail_compose_main_view);
		
		m_discardRefView = (TextView)findViewById(R.id.mail_compose_ref_label);
		m_discardRefView.setOnClickListener(this);
		
		m_sendBtn 	= (Button)findViewById(R.id.mail_compose_send_btn);
		m_sendBtn.setOnClickListener(this);
		
		m_saveBtn	= (Button)findViewById(R.id.mail_compose_save_btn);
		m_saveBtn.setOnClickListener(this);
		m_saveBtn.setEnabled(false);
		
		m_discardBtn = (Button)findViewById(R.id.mail_compose_discard_btn);
		m_discardBtn.setOnClickListener(this);
		
		
						
		// fetch the reference mail
		//
		if(m_mainApp.m_composeRefMail != null){
			
			Intent in = getIntent();
			m_referenceMailStyle 	= in.getIntExtra(COMPOSE_MAIL_STYLE,fetchMail.NOTHING_STYLE);
			m_referenceGroupId		= in.getLongExtra(COMPOSE_MAIL_GROUP_ID,-1);
			
			boolean t_draft		= in.getBooleanExtra(COMPOSE_MAIL_DRAFT, false);
			if(t_draft){
				m_draftMail = m_mainApp.m_composeRefMail;
				if(m_draftMail.getSendRefMailIndex() != -1){
					fetchMail t_mail = m_mainApp.m_dba.fetchMail(m_draftMail.getSendRefMailIndex());
					if(t_mail != null){
						m_referenceMail = new MailOpenActivity.Envelope(t_mail, this);
					}
				}
								
			}else{
				m_referenceMail = new MailOpenActivity.Envelope(m_mainApp.m_composeRefMail, this);
			}
			
			m_mainApp.m_composeRefMail = null;			
			
			// add to main view
			//
			if(m_referenceMail != null){
				m_referenceMail.openBody();
				m_mainView.addView(m_referenceMail.m_mainView);
				m_discardRefView.setVisibility(View.VISIBLE);
			}else{
				m_discardRefView.setVisibility(View.GONE);
			}
			
			// set the title and subject
			//
			String t_sub;
			if(m_referenceMailStyle == fetchMail.REPLY_STYLE
			|| m_referenceMailStyle == fetchMail.REPLY_ALL_STYLE){
				
				Vector<String> t_toVect = null;
				if(t_draft && m_draftMail != null){
					// draft
					//
					t_sub = m_draftMail.GetSubject();
					m_body.setText(m_draftMail.GetContain());
					// draft
					//
					t_toVect = m_draftMail.GetSendToVect();
				}else{
					// reply/forward 
					//
					t_sub = MailDbAdapter.getReplySubject(m_referenceMail.m_mail.GetSubject(), getString(R.string.mail_compose_reply_prefix));
					
					if(m_referenceMail != null){

						if(m_referenceMail.m_mail.isOwnSendMail()){
							t_toVect = m_referenceMail.m_mail.GetSendToVect();
						}else{
							t_toVect = m_referenceMail.m_mail.GetReplyToVect().isEmpty()?
											m_referenceMail.m_mail.GetFromVect():
											m_referenceMail.m_mail.GetReplyToVect();
						}
					}
				}
				
				if(t_toVect != null){
					if(m_referenceMailStyle == fetchMail.REPLY_ALL_STYLE){
						StringBuffer t_to = new StringBuffer();
						for(String addr:t_toVect){
							// get rid of comma with blank
							//
							t_to.append(addr.replace(',', ' ')).append(",");
						}
						m_to.setText(t_to.toString());
					}else{
						if(!t_toVect.isEmpty()){
							m_to.setText(t_toVect.get(0));
						}					
					}
					m_body.requestFocus();
				}else{
					m_to.requestFocus();
				}				
				
			}else{
				// forward style
				//
				if(m_draftMail != null){
					t_sub = m_draftMail.GetSubject();
				}else if(m_referenceMail != null){
					t_sub = getString(R.string.mail_compose_forward_prefix) + m_referenceMail.m_mail.GetSubject();
				}else{
					t_sub = getString(R.string.mail_compose_forward_prefix);
				}
				m_to.requestFocus();
			}
			
			setTitle(t_sub);
			m_subject.setText(t_sub);
		}else{
			// compose a new mail
			//
			m_discardRefView.setVisibility(View.GONE);
			m_to.requestFocus();
			
			if(!m_mainApp.m_config.m_sendMailAccountList.isEmpty()){
			
				// load the spinner widget 
				//
				m_ownAccountSpinner = (Spinner)findViewById(R.id.mail_compose_own_account);
				m_ownAccountSpinner.setOnItemSelectedListener(new OnItemSelectedListener(){
					public void onItemSelected(AdapterView<?> parent,View view, int pos, long id) {
						m_modified = true;
						m_saveBtn.setEnabled(true);
				    }

				    public void onNothingSelected(AdapterView<?> parent){}
				});
				m_ownAccountSpinner.setVisibility(View.VISIBLE);
				
				// load the sender Mail account list
				//
				String[] t_ownAccountList = new String[m_mainApp.m_config.m_sendMailAccountList.size()];
				for(int i = 0;i < m_mainApp.m_config.m_sendMailAccountList.size();i++){
					t_ownAccountList[i] = m_mainApp.m_config.m_sendMailAccountList.get(i);
				}				
				ArrayAdapter<String> adapter = 
					new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,
							t_ownAccountList);
				adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				m_ownAccountSpinner.setAdapter(adapter);
				
				if(m_mainApp.m_config.m_defaultSendMailAccountIndex >= m_mainApp.m_config.m_sendMailAccountList.size()){
					m_mainApp.m_config.m_defaultSendMailAccountIndex = 0;
				}
				m_ownAccountSpinner.setSelection(m_mainApp.m_config.m_defaultSendMailAccountIndex);
			}
		}
		
		// set the modified flag
		//
		TextWatcher t_watcher = new TextWatcher(){
			public void afterTextChanged (Editable s){}
			public void beforeTextChanged (CharSequence s, int start, int count, int after){}
			public void onTextChanged(CharSequence s, int start, int before, int count){
				m_modified = true;
				m_saveBtn.setEnabled(true);
			}
		};
		m_body.addTextChangedListener(t_watcher);
		m_to.addTextChangedListener(t_watcher);
		m_subject.addTextChangedListener(t_watcher);
		m_bcc.addTextChangedListener(t_watcher);
		m_cc.addTextChangedListener(t_watcher);
	}
	
	public void onClick(View v){
		if(v == m_discardRefView){
			
			GlobalDialog.showYesNoDialog(getString(R.string.mail_compose_delete_ref_mail), this, 
			new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if(which == DialogInterface.BUTTON_POSITIVE){
						m_discardRefView.setVisibility(View.GONE);
						m_mainView.removeView(m_referenceMail.m_mainView);
						m_referenceMail = null;
						
						if(m_draftMail != null){
							m_draftMail.setSendRefMailIndex(-1);
						}
					}					
				}
			});
			
		}else if(v == m_sendBtn){
			if(!m_modified){
				// without modified confirm
				//
				GlobalDialog.showYesNoDialog(getString(R.string.mail_compose_send_without_modified), this, 
				new DialogInterface.OnClickListener(){
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if(which == DialogInterface.BUTTON_POSITIVE){
							send();
						}					
					}
				});
				
			}else{
				
				if(m_body.getText().length() == 0){
					// body is empty confirm
					//
					GlobalDialog.showYesNoDialog(getString(R.string.mail_compose_empty_body_question), this, 
					new DialogInterface.OnClickListener(){
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if(which == DialogInterface.BUTTON_POSITIVE){
								send();
							}					
						}
					});
				}else{
					send();
				}
				
			}
			
		}else if(v == m_saveBtn){
			saveDraft();
		}else if(v == m_discardBtn){
			onClose();		
		}
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if((keyCode == KeyEvent.KEYCODE_BACK)) {
	    	onClose();
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}
	
	private void onClose(){
		if(!m_modified){
			finish();
			return;
		}
		
		GlobalDialog.showYesNoDialog(getString(R.string.mail_compose_discard_mail), this, 
		new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(which == DialogInterface.BUTTON_POSITIVE){
					finish();
				}					
			}
		});
	}
	
	private void send(){
		
		if(!m_mainApp.m_connectDeamonRun){
			Toast.makeText(this, getString(R.string.mail_compose_connectdeamon_not_run), Toast.LENGTH_SHORT).show();
			return ;
		}
		
		// send progress:
		//
		//    MailComposeActivity.send()
		//			|
		//		write database (fetchMail)
		//			|
		//			| Broadcast( YuchDroidApp.FILTER_SEND_MAIL )
		//			|
		//		ConnectDeamon  ----> SendMailDeamon
		//									|																	
		//								write database (fetchMail's group flag)									
		//									|																	
		//									| Broadcast (YuchDroidApp.FILTER_SEND_MAIL_VIEW)				
		//									|
		//							-----------------
		//							|				|
		//						MailListView	MailOpenActivity
		//
		
		fetchMail t_mail = m_draftMail != null?m_draftMail:storeDB(fetchMail.GROUP_FLAG_SEND_PADDING);
		if(t_mail != null){

			// send mail
			//
			m_mainApp.sendMail(t_mail,
					m_referenceMail != null?m_referenceMail.m_mail:null,
					m_referenceMailStyle);
			
			// close the activity
			//
			finish();
		}		
	}
	
	private fetchMail storeDB(int _flag){
		
		String[] t_toAddrList 	= m_to.getText().toString().replaceAll("\n", ",").split(",");
		String[] t_ccAddrList 	= m_cc.getText().toString().replaceAll("\n", ",").split(",");
		String[] t_bccAddrList	= m_bcc.getText().toString().replaceAll("\n", ",").split(",");
		
		// check the address
		//
		if(!checkSendToAddr(t_toAddrList)
		|| !checkSendToAddr(t_ccAddrList)
		|| !checkSendToAddr(t_bccAddrList)){
			Toast.makeText(this, getString(R.string.mail_compose_address_error), Toast.LENGTH_SHORT).show();
			return null;
		}		
		
		fetchMail t_sendMail = new fetchMail();
		updateMail(t_sendMail,_flag);
		
		t_sendMail.setGroupFlag(_flag);		
		
		int t_id = (int)m_mainApp.m_dba.createMail(t_sendMail,m_referenceGroupId,true);
		t_sendMail.setDbIndex(t_id);
		
		return t_sendMail;
	}
	
	private void saveDraft(){		
		if(m_draftMail == null){
			m_draftMail = storeDB(fetchMail.GROUP_FLAG_SEND_DRAFT);
			if(m_draftMail == null){
				return;
			}
		}else{
			updateMail(m_draftMail,fetchMail.GROUP_FLAG_SEND_DRAFT);			
			m_mainApp.m_dba.updateMail(m_draftMail);
		}
		
		m_mainApp.sendBroadcastUpdateFlag(m_draftMail,true);
		
		m_modified = false;
		m_saveBtn.setEnabled(false);
	}
	
	private void updateMail(fetchMail _mail,int _groupFlag){
		
		String[] t_toAddrList 	= m_to.getText().toString().replaceAll("\n", ",").split(",");
		String[] t_ccAddrList 	= m_cc.getText().toString().replaceAll("\n", ",").split(",");
		String[] t_bccAddrList	= m_bcc.getText().toString().replaceAll("\n", ",").split(",");
		
		_mail.setGroupIndex(m_referenceGroupId);
		_mail.setGroupFlag(_groupFlag);
		_mail.SetSendToVect(t_toAddrList);
		_mail.SetCCToVect(t_ccAddrList);
		_mail.SetBCCToVect(t_bccAddrList);
		_mail.SetSendDate(new Date());
		_mail.SetSubject(m_subject.getText().toString());
		_mail.SetContain(m_body.getText().toString());
		
		if(m_referenceGroupId != -1 && m_referenceMail != null){
			_mail.SetFromVect(new String[]
            {
				"\"" + getString(R.string.mail_me_address) + "\" <" + m_referenceMail.m_mail.getOwnAccount() + ">"
			});
			
			_mail.setOwnAccount(m_referenceMail.m_mail.getOwnAccount());
		}else if(m_ownAccountSpinner != null){
			
			// compose a new mail and get the own account
			//
			int t_select = (int)m_ownAccountSpinner.getSelectedItemId();
			
			if(t_select < 0 || t_select >= m_mainApp.m_config.m_sendMailAccountList.size()){
				t_select = 0;
			}
			
			m_mainApp.m_config.m_defaultSendMailAccountIndex = t_select;
			_mail.setOwnAccount(m_mainApp.m_config.m_sendMailAccountList.get(
								m_mainApp.m_config.m_defaultSendMailAccountIndex));
			
			_mail.SetFromVect(new String[]
            {
 				"\"" + getString(R.string.mail_me_address) + "\" <" + _mail.getOwnAccount() + ">"
 			});
		}
		
		_mail.setSendRefMailStyle(m_referenceMailStyle);
		if(m_referenceMail != null){
			_mail.setSendRefMailIndex(m_referenceMail.m_mail.getDbIndex());
		}
	}
		
	private boolean checkSendToAddr(String[] _toAddrList){
		if(_toAddrList.length == 0){
			return false;
		}
		
		for(String addr:_toAddrList){
			if(addr.length() != 0){
				if(!addr.matches("^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$")
				&& !addr.matches("(.)*<\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*>")){
					return false;
				}
			}
		}
		
		return true;
	}
}
