package com.yuchting.yuchdroid.client.mail;

import java.util.Vector;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yuchting.yuchdroid.client.R;
import com.yuchting.yuchdroid.client.YuchDroidApp;

public class MailComposeActivity extends Activity implements View.OnClickListener{

	public final static String	COMPOSE_MAIL_STYLE = "style";
	
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
	
	MailOpenActivity.Envelope	m_referenceMail	= null;
	YuchDroidApp	m_mainApp		= null;
	
	int			m_referenceMailStyle = fetchMail.NOTHING_STYLE;
	
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
		
		m_discardBtn = (Button)findViewById(R.id.mail_compose_discard_btn);
		m_discardBtn.setOnClickListener(this);
				
		// fetch the reference mail
		//
		if(m_mainApp.m_composeRefMail != null){
			
			Intent in = getIntent();
			m_referenceMailStyle = in.getExtras().getInt(COMPOSE_MAIL_STYLE);
			
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
				
				Vector<String> t_toVect = m_referenceMail.m_mail.GetReplyToVect().isEmpty()?
												m_referenceMail.m_mail.GetFromVect():
												m_referenceMail.m_mail.GetReplyToVect();
												
				if(m_referenceMailStyle == fetchMail.REPLY_ALL_STYLE){
					StringBuffer t_to = new StringBuffer();
					for(String addr:t_toVect){
						t_to.append(addr).append(",");
					}
					m_to.setText(t_to.toString());
				}else{
					m_to.setText(t_toVect.get(0));
				}
				
			}else{
				t_sub = getString(R.string.mail_compose_forward_prefix) + m_referenceMail.m_mail.GetSubject();
			}
			
			setTitle(t_sub);
			
			m_subject.setText(t_sub);
			
			m_body.requestFocus();
		}else{
			m_discardRefView.setVisibility(View.GONE);
			m_to.requestFocus();
		}
		
	}
	
	public void onClick(View v){
		if(v == m_discardRefView){
			m_discardRefView.setVisibility(View.GONE);
			m_mainView.removeView(m_referenceMail.m_mainView);
			m_referenceMail = null;
		}else if(v == m_sendBtn){
			
		}
	}
}
