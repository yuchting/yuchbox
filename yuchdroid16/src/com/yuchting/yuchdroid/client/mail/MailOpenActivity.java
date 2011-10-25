package com.yuchting.yuchdroid.client.mail;

import java.text.SimpleDateFormat;
import java.util.Vector;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.yuchting.yuchdroid.client.R;
import com.yuchting.yuchdroid.client.YuchDroidApp;

public class MailOpenActivity extends Activity {

	public final static String			TAG = "MailOpenActivity";
	
	public final static String			INTENT_PRE_MAIL_GROUP_INDEX 	= "pre";
	public final static String			INTENT_NEXT_MAIL_GROUP_INDEX	= "next";
	
	public final static String			INTENT_CURRENT_MAIL_GROUP		= "mail";
	
	YuchDroidApp			m_mainApp;
	
	int						m_preMail = -1;
	int						m_nextMail = -1;
	
	Vector<fetchMail>		m_currMailList = new Vector<fetchMail>();
		
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mail_open);
        
        m_mainApp = (YuchDroidApp)getApplicationContext();
        
        try{
        	fillMailContent();
        }catch(Exception e){
        	m_mainApp.setErrorString(TAG, e);
        	finish();
        }        
    }
	
	private void fillMailContent()throws Exception{
		
		Intent in = getIntent();
        if(in == null || in.getExtras() == null){
        	finish();
        }
        
        m_preMail 	= in.getExtras().getInt(INTENT_PRE_MAIL_GROUP_INDEX);
        m_nextMail	= in.getExtras().getInt(INTENT_NEXT_MAIL_GROUP_INDEX);
        
        int t_currMail = in.getExtras().getInt(INTENT_CURRENT_MAIL_GROUP);        
        Cursor t_mailCursor		= m_mainApp.m_dba.fetchGroup(t_currMail);

        String t_mailIndexList	= t_mailCursor.getString(t_mailCursor.getColumnIndex(MailDbAdapter.GROUP_ATTR_MAIL_INDEX));
        String[] t_mailList 	= t_mailIndexList.split(fetchMail.fsm_vectStringSpliter);
        
        t_mailCursor.close();
        
        // fetch Mail data from the group list
        //
        for(String id:t_mailList){
        	t_mailCursor = m_mainApp.m_dba.fetchMail(Integer.valueOf(id).intValue());
        	m_currMailList.add(m_mainApp.m_dba.convertMail(t_mailCursor));
        	t_mailCursor.close();
        }        
                
	}
	
	final class Envelope{
		fetchMail		m_mail;
		ScrollView		m_mainView;
		TextView		m_bodyText;
		TextView		m_htmlText;
		
		TextView		m_touchHTML;
		
		public void setBody(){
			if(m_mail.GetContain().length() != 0){			
				m_bodyText.setText(m_mail.GetContain());        	
            }else{
            	m_bodyText.setVisibility(View.GONE);
            }
                		
            if(m_mail.GetContain_html().length() != 0){
            	m_touchHTML.setVisibility(View.VISIBLE);     	
            	
            	m_touchHTML.setOnClickListener(new View.OnClickListener() {
    				
    				@Override
    				public void onClick(View paramView){
    					m_touchHTML.setVisibility(View.GONE);
    					m_htmlText.setVisibility(View.VISIBLE);
    					m_htmlText.setText(Html.fromHtml(m_mail.GetContain_html()));
    				}
    			});
            }
		}
	}
	
	private Envelope setEnvelope(fetchMail _mail,boolean _loadBody){
		
		Envelope t_envelope = new Envelope();
		t_envelope.m_mail 	= _mail;
		
		LayoutInflater t_inflater = LayoutInflater.from(this);
		t_envelope.m_mainView = (ScrollView)t_inflater.inflate(R.layout.mail_open_envelope, (ViewGroup)findViewById(R.id.mail_open_main));
		
        // fill mail data
        //
        TextView t_fromAddr = (TextView)t_envelope.m_mainView.findViewById(R.id.mail_open_from_addr);
        TextView t_date		= (TextView)t_envelope.m_mainView.findViewById(R.id.mail_open_date);
        TextView t_time		= (TextView)t_envelope.m_mainView.findViewById(R.id.mail_open_time);
        TextView t_toAddr	= (TextView)t_envelope.m_mainView.findViewById(R.id.mail_open_recv_addr);        
        TextView t_subject	= (TextView)t_envelope.m_mainView.findViewById(R.id.mail_open_subject);
                
        t_envelope.m_bodyText	= (TextView)t_envelope.m_mainView.findViewById(R.id.mail_open_body);
        t_envelope.m_htmlText	= (TextView)t_envelope.m_mainView.findViewById(R.id.mail_open_html);
        t_envelope.m_touchHTML	= (TextView)t_envelope.m_mainView.findViewById(R.id.mail_open_html_switch);
        
        Address[] t_fromAddrList 	= fetchMail.parseAddressList(_mail.GetFromVect());
        Address[] t_toAddrList		= fetchMail.parseAddressList(_mail.GetSendToVect());
        
        t_fromAddr.setText(t_fromAddrList[0].m_name);
        
        SimpleDateFormat t_format = new SimpleDateFormat("yyyy"+getString(R.string.mail_time_year)+
														"MM"+getString(R.string.mail_time_month)+
														"dd" + getString(R.string.mail_time_day));
        
        t_date.setText(t_format.format(_mail.GetSendDate()));
        
        t_format = new SimpleDateFormat("HH:mm");
        t_time.setText(t_format.format(_mail.GetSendDate()));
        
        t_toAddr.setText(getString(R.string.mail_open_recipient_addr_prefix) + MailListAdapter.getShortAddrList(t_toAddrList));
        t_subject.setText(_mail.GetSubject());
        
        return t_envelope;
	}
	
	
	
}
