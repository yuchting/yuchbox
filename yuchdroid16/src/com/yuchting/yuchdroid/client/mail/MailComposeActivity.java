package com.yuchting.yuchdroid.client.mail;

import android.app.Activity;
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

	AutoCompleteTextView	m_to;
	AutoCompleteTextView	m_cc;
	AutoCompleteTextView	m_bcc;
	
	EditText	m_subject;
	EditText	m_body;
	
	TextView	m_discardRefView;
	
	Button		m_sendBtn;
	Button		m_saveBtn;
	Button		m_discardBtn;
	
	LinearLayout	m_mainView;
	
		
	MailOpenActivity.Envelope	m_referenceMail;
	YuchDroidApp	m_mainApp;
	
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
			m_discardRefView.setVisibility(View.VISIBLE);
			m_referenceMail = MailOpenActivity.getEnvelope(m_mainApp.m_composeRefMail, this);
			m_mainApp.m_composeRefMail = null;
			
			// add to main view
			//
			m_referenceMail.setBody();
			m_mainView.addView(m_referenceMail.m_mainView);
		}
		
	}
	
	public void onClick(View v){
		if(v == m_discardRefView){
			
		}
	}
}
