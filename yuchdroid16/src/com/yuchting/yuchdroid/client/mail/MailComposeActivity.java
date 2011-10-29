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
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.yuchting.yuchdroid.client.GlobalDialog;
import com.yuchting.yuchdroid.client.R;
import com.yuchting.yuchdroid.client.YuchDroidApp;

public class MailComposeActivity extends Activity implements View.OnClickListener{

	public final static String	COMPOSE_MAIL_STYLE = "style";
	public final static String	COMPOSE_MAIL_GROUP_ID = "groupId";
	
	AutoCompleteTextView	m_to	= null;
	AutoCompleteTextView	m_cc	= null;
	AutoCompleteTextView	m_bcc	= null;
	
	EditText	m_subject			= null;
	EditText	m_body				= null;
	
	TextView	m_discardRefView	= null;
	
	Button		m_sendBtn			= null;
	Button		m_saveBtn			= null;
	Button		m_discardBtn		= null;
	
	LinearLayout	m_mainView		= null;
	
	boolean		m_modified		= false;
	
	MailOpenActivity.Envelope	m_referenceMail	= null;
	YuchDroidApp	m_mainApp		= null;
	
	int			m_referenceMailStyle = fetchMail.NOTHING_STYLE;
	int			m_referenceGroupId 	= -1;
	
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
			m_referenceMailStyle 	= in.getExtras().getInt(COMPOSE_MAIL_STYLE);
			m_referenceGroupId		= in.getExtras().getInt(COMPOSE_MAIL_GROUP_ID);
			
			m_discardRefView.setVisibility(View.VISIBLE);
			m_referenceMail = MailOpenActivity.getEnvelope(m_mainApp.m_composeRefMail, this);
			m_mainApp.m_composeRefMail = null;
			
			// add to main view
			//
			m_referenceMail.setBody();
			m_mainView.addView(m_referenceMail.m_mainView);
			
			// set the title and subject
			//
			String t_sub;
			if(m_referenceMailStyle == fetchMail.REPLY_STYLE
			|| m_referenceMailStyle == fetchMail.REPLY_ALL_STYLE){
				
				t_sub = MailDbAdapter.getReplySubject(m_referenceMail.m_mail.GetSubject(), getString(R.string.mail_compose_reply_prefix));
				
				Vector<String> t_toVect;
				if(m_referenceMail.m_mail.isOwnSendMail()){
					t_toVect = m_referenceMail.m_mail.GetSendToVect();
				}else{
					t_toVect = m_referenceMail.m_mail.GetReplyToVect().isEmpty()?
									m_referenceMail.m_mail.GetFromVect():
									m_referenceMail.m_mail.GetReplyToVect();
				}
				 
												
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
				// forward style
				//
				t_sub = getString(R.string.mail_compose_forward_prefix) + m_referenceMail.m_mail.GetSubject();
				m_to.requestFocus();
			}
			
			setTitle(t_sub);
			m_subject.setText(t_sub);						
		}else{
			m_discardRefView.setVisibility(View.GONE);
			m_to.requestFocus();
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
		
		String[] t_toAddrList 	= m_to.getText().toString().replaceAll("\n", ",").split(",");
		String[] t_ccAddrList 	= m_cc.getText().toString().replaceAll("\n", ",").split(",");
		String[] t_bccAddrList	= m_bcc.getText().toString().replaceAll("\n", ",").split(",");
		
		// check the address
		//
		if(!checkSendToAddr(t_toAddrList)
		|| !checkSendToAddr(t_ccAddrList)
		|| !checkSendToAddr(t_bccAddrList)){
			Toast.makeText(this, getString(R.string.mail_compose_address_error), Toast.LENGTH_SHORT).show();
			return;
		}		
		
		fetchMail t_sendMail = new fetchMail();
		
		t_sendMail.setGroupIndex(m_referenceGroupId);
		t_sendMail.setGroupFlag(fetchMail.GROUP_FLAG_SEND_PADDING);
		t_sendMail.SetSendToVect(t_toAddrList);
		t_sendMail.SetCCToVect(t_ccAddrList);
		t_sendMail.SetBCCToVect(t_bccAddrList);
		t_sendMail.SetSendDate(new Date());
		t_sendMail.SetSubject(m_subject.getText().toString());
		t_sendMail.SetContain(m_body.getText().toString());
		
		int t_id = (int)m_mainApp.m_dba.createMail(t_sendMail,m_referenceGroupId == -1?null:new Long(m_referenceGroupId));
		t_sendMail.setDbIndex(t_id);
		
		m_mainApp.m_composeRefMail 		= t_sendMail;
		if(m_referenceMail != null){
			m_mainApp.m_composeStyleRefMail = m_referenceMail.m_mail;
		}		
		
		// broadcast to ConnectDeamon
		//
		Intent in = new Intent(YuchDroidApp.FILTER_SEND_MAIL);
		in.putExtra(YuchDroidApp.DATA_FILTER_SEND_MAIL_STYLE, m_referenceMailStyle);
		
		sendBroadcast(in);
		
		// close the activity
		//
		finish();
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
