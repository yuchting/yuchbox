package com.yuchting.yuchdroid.client.mail;

import java.text.SimpleDateFormat;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicReference;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.yuchting.yuchdroid.client.R;
import com.yuchting.yuchdroid.client.YuchDroidApp;

public class MailOpenActivity extends Activity implements View.OnClickListener{

	public final static String			TAG = "MailOpenActivity";
	
	public final static String			INTENT_PRE_MAIL_GROUP_INDEX 	= "pre";
	public final static String			INTENT_NEXT_MAIL_GROUP_INDEX	= "next";
	public final static String			INTENT_NEXT_MAIL_CURSOR_POS		= "pos";
		
	public final static String			INTENT_CURRENT_MAIL_GROUP		= "mail";
	
	public static final class Envelope{
		
		fetchMail		m_mail		= null;
		
		Context			m_loadCtx	= null;
		
		RelativeLayout	m_mainView	= null;
		TextView		m_bodyText	= null;
		TextView		m_htmlText	= null;
		
		ImageView		m_mailFlag	= null;
		TextView		m_touchHTML	= null;
		
		Button			m_resendBtn	= null;
		
		int				m_mailDbIdx;				
		boolean		m_initOpen = false;
		
		public Envelope(fetchMail _mail,Context _ctx){
			m_mail		= _mail;
			m_loadCtx	= _ctx;
		}
		
		public void openBody(){
			m_initOpen = true;
			
			init();
			
			if(m_mail.GetContain().length() != 0){
				m_bodyText.setVisibility(View.VISIBLE);
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
		
		public void init(){
			if(m_mainView != null){
				return;
			}
			
			LayoutInflater t_inflater = LayoutInflater.from(m_loadCtx);
			m_mainView = (RelativeLayout)t_inflater.inflate(R.layout.mail_open_envelope, null);
			
	        // fill mail data
	        //
	        TextView t_fromAddr = (TextView)m_mainView.findViewById(R.id.mail_open_from_addr);
	        TextView t_date		= (TextView)m_mainView.findViewById(R.id.mail_open_date);
	        TextView t_time		= (TextView)m_mainView.findViewById(R.id.mail_open_time);
	        TextView t_toAddr	= (TextView)m_mainView.findViewById(R.id.mail_open_recv_addr);        
	        TextView t_subject	= (TextView)m_mainView.findViewById(R.id.mail_open_subject);
	        
	        m_mailFlag	= (ImageView)m_mainView.findViewById(R.id.mail_open_flag);
	        m_bodyText	= (TextView)m_mainView.findViewById(R.id.mail_open_body);
	        m_htmlText	= (TextView)m_mainView.findViewById(R.id.mail_open_html);
	        m_touchHTML	= (TextView)m_mainView.findViewById(R.id.mail_open_html_switch);
	        m_resendBtn = (Button)m_mainView.findViewById(R.id.mail_open_resend_btn);
	        
	        Address[] t_fromAddrList 	= fetchMail.parseAddressList(m_mail.GetFromVect());
	        Address[] t_toAddrList		= fetchMail.parseAddressList(m_mail.GetSendToVect());
	        
	        t_fromAddr.setText(t_fromAddrList[0].m_name);
	        
	        SimpleDateFormat t_format = new SimpleDateFormat("yyyy"+m_loadCtx.getString(R.string.mail_time_year)+
															"MM"+m_loadCtx.getString(R.string.mail_time_month)+
															"dd" + m_loadCtx.getString(R.string.mail_time_day));
	        
	        t_date.setText(t_format.format(m_mail.GetSendDate()));
	        
	        t_format = new SimpleDateFormat("HH:mm");
	        t_time.setText(t_format.format(m_mail.GetSendDate()));
	        
	        t_toAddr.setText(m_loadCtx.getString(R.string.mail_open_recipient_addr_prefix) + MailListAdapter.getShortAddrList(t_toAddrList));
	        t_subject.setText(m_mail.GetSubject());
	        m_mailFlag.setImageResource(MailListAdapter.getMailFlagImageId(m_mail.getGroupFlag()));
	        
	        // set the attachment file
	        //
	        if(!m_mail.GetAttachment().isEmpty()){
	        	for(final MailAttachment att:m_mail.GetAttachment()){
	        		View attachView = t_inflater.inflate(R.layout.mail_open_attachment_item,null);
	        		TextView filename = (TextView)attachView.findViewById(R.id.mail_open_attachment_item_filename);
	        		filename.setText(att.m_name + " ("+YuchDroidApp.GetByteStr(att.m_size) + ")");
	        		
	        		
	        		RelativeLayout.LayoutParams t_lp = new RelativeLayout.LayoutParams(
	        		        RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
	        		t_lp.addRule(RelativeLayout.BELOW, m_touchHTML.getId());
	        		
	        		m_mainView.addView(attachView,t_lp);
	        	}
	        }
		}
		
		public void setOnClickOpenBodyEvent(){
			init();
			if(!m_initOpen){
				m_mainView.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						openBody();
						m_mainView.setOnClickListener(null);
					}
				});
			}
		}
		
		public void openResendBtn(View.OnClickListener _l){
			init();
			m_resendBtn.setVisibility(View.VISIBLE);
			m_resendBtn.setOnClickListener(_l);
		}
		
		public void hideResendBtn(){
			init();
			m_resendBtn.setVisibility(View.GONE);
			m_resendBtn.setOnClickListener(null);
		}
	}	
	
	YuchDroidApp			m_mainApp;
	Cursor					m_groupCursor;
		
	Vector<Envelope>		m_currMailList = new Vector<Envelope>();
	
	TextView				m_formerMailView;
	LinearLayout			m_mainMailView;
	ScrollView				m_envelopeScrollView;
	
	TextView				m_touchTop;
	
	TextView				m_titleSubject;
	TextView				m_titleOwnAccount;
	
	ImageView				m_preGroupBtn;
	ImageView				m_nextGroupBtn;
	
	Button					m_replyBtn;
	Button					m_forwardBtn;
	Button					m_deleteBtn;
	
	long					m_currGroupIdx = -1;
	long					m_preGroupIdx = -1;
	long					m_nextGroupIdx = -1;
	int						m_cursorPosition;
	
	final MailOpenActivity	m_composeActivity = this;
	BroadcastReceiver		m_sendMailRecv = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {

			long t_groupIndex 	= intent.getLongExtra(YuchDroidApp.DATA_FILTER_SEND_MAIL_VIEW_GROUP_ID, -1);
			long t_mailIndex 	= intent.getLongExtra(YuchDroidApp.DATA_FILTER_SEND_MAIL_VIEW_MAIL_ID, -1);
			int t_flag			= intent.getIntExtra(YuchDroidApp.DATA_FILTER_SEND_MAIL_VIEW_GROUP_FLAG, -1);
						
			if(t_groupIndex == m_currGroupIdx){
				boolean t_envelopeAdded = false;
				for(Envelope en:m_currMailList){
					if(en.m_mail.getDbIndex() == t_mailIndex){
						t_envelopeAdded = true;
						
						en.m_mailFlag.setImageResource(MailListAdapter.getMailFlagImageId(t_flag));
						
						if(t_flag == fetchMail.GROUP_FLAG_SEND_ERROR){
							en.openResendBtn(m_resendListener);
						}
						
						break;
					}
				}
				
				if(!t_envelopeAdded){
					Cursor t_mailCursor = m_mainApp.m_dba.fetchMail(t_mailIndex);
					try{
						fetchMail t_mail = m_mainApp.m_dba.convertMail(t_mailCursor);
												
						Envelope en = new Envelope(t_mail, m_composeActivity);
						en.openBody();
						
						m_mainMailView.addView(en.m_mainView);
						m_currMailList.add(en);
						
					}catch(Exception e){
						m_mainApp.setErrorString(TAG+" sendMailRecv",e);
					}finally{
						t_mailCursor.close();
					}
				}
			}
			
		}
	};
	
	View.OnClickListener	m_resendListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if(!m_mainApp.m_connectDeamonRun){
				Toast.makeText(MailOpenActivity.this, 
						getString(R.string.mail_open_resend_need_connectDeamon_prompt), 
						Toast.LENGTH_SHORT).show();
				
				return;
			}
			
			for(Envelope en:m_currMailList){
				if(en.m_resendBtn == v){
					en.hideResendBtn();
					
					fetchMail t_refMail = null;
					if(en.m_mail.getSendRefMailIndex() != -1){
						Cursor c = m_mainApp.m_dba.fetchMail(en.m_mail.getSendRefMailIndex());
						if(c.getCount() != 0){
							t_refMail = m_mainApp.m_dba.convertMail(c);
						}
						c.close();						
					}
					
					m_mainApp.sendMail(en.m_mail, t_refMail, en.m_mail.getSendRefMailStyle());
					
					break;
				}
			}			
		}
	};
	
	// the first child of Envelope holder View group is "Touch to Show former mail" TextView
    //
    final static int fsm_insertEnvelopeIndex = 1;
		
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.mail_open);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.mail_open_title);
        
        m_titleSubject			= (TextView)findViewById(R.id.mail_open_title_subject);
        m_titleOwnAccount		= (TextView)findViewById(R.id.mail_open_title_own_account);
        
        m_mainApp 				= (YuchDroidApp)getApplicationContext();
        m_groupCursor			= m_mainApp.m_dba.fetchAllGroup();
        m_formerMailView 		= (TextView)findViewById(R.id.mail_open_open_former_envelope);
        m_mainMailView 			= (LinearLayout)findViewById(R.id.mail_open_envelope_view);
        m_envelopeScrollView 	= (ScrollView)findViewById(R.id.mail_open_envelope_scroll_view);
        
        m_formerMailView.setOnClickListener(this);	
        
        m_preGroupBtn = (ImageView)findViewById(R.id.mail_open_pre_btn);
        m_preGroupBtn.setOnClickListener(this);
        
        m_nextGroupBtn = (ImageView)findViewById(R.id.mail_open_next_btn);
        m_nextGroupBtn.setOnClickListener(this);
               
        // scroll to top
        //
        m_touchTop = (TextView)findViewById(R.id.mail_open_scroll_top);
        m_touchTop.setOnClickListener(this);
        
        m_replyBtn	= (Button)findViewById(R.id.mail_open_mail_reply_btn);
        m_replyBtn.setOnClickListener(this);
        
        m_forwardBtn = (Button)findViewById(R.id.mail_open_mail_forward_btn);
        m_forwardBtn.setOnClickListener(this);
        
        m_deleteBtn	= (Button)findViewById(R.id.mail_open_mail_delete_btn);
        m_deleteBtn.setOnClickListener(this);
        
    	Intent in = getIntent();
        if(in == null || in.getExtras() == null){
        	finish();
        }
        
        // prepare the nav
        //
        m_preGroupIdx 	= in.getExtras().getLong(INTENT_PRE_MAIL_GROUP_INDEX);
        m_nextGroupIdx	= in.getExtras().getLong(INTENT_NEXT_MAIL_GROUP_INDEX);
        m_cursorPosition = in.getExtras().getInt(INTENT_NEXT_MAIL_CURSOR_POS);
        
        // prepare the mail data
        //
        m_currGroupIdx = in.getExtras().getLong(INTENT_CURRENT_MAIL_GROUP);    
        
        try{
        	fillMailContent();
        }catch(Exception e){
        	m_mainApp.setErrorString(TAG, e);
        	finish();
        }
        
        registerReceiver(m_sendMailRecv, new IntentFilter(YuchDroidApp.FILTER_SEND_MAIL_VIEW));
        
        
    }
	
	public void onDestroy(){
		super.onDestroy();
		
		m_groupCursor.close();
		unregisterReceiver(m_sendMailRecv);
	}
	
	private void fillMailContent()throws Exception{   

        m_preGroupBtn.setVisibility(m_preGroupIdx != -1?View.VISIBLE:View.GONE);
        m_nextGroupBtn.setVisibility(m_nextGroupIdx != -1?View.VISIBLE:View.GONE);     
    
        Cursor t_mailCursor		= m_mainApp.m_dba.fetchGroup(m_currGroupIdx);
        
        String t_title = t_mailCursor.getString(t_mailCursor.getColumnIndex(MailDbAdapter.GROUP_ATTR_SUBJECT));
        m_titleSubject.setText(t_title);

        String t_mailIndexList	= t_mailCursor.getString(t_mailCursor.getColumnIndex(MailDbAdapter.GROUP_ATTR_MAIL_INDEX));
        String[] t_mailList 	= t_mailIndexList.split(fetchMail.fsm_vectStringSpliter);
        
        t_mailCursor.close();
        
        // fetch Mail data from the group list
        //
        StringBuffer t_markReadMailString = new StringBuffer();
        boolean t_hasUnreadMail = false;
        m_currMailList.clear();
        for(String id:t_mailList){
        	
        	int t_id = Integer.valueOf(id).intValue();
        	
        	t_mailCursor 		= m_mainApp.m_dba.fetchMail(t_id);
        	fetchMail t_mail	= m_mainApp.m_dba.convertMail(t_mailCursor);
        	t_mail.setGroupIndex(m_currGroupIdx);
        	
        	Envelope en = new Envelope(t_mail,this);
        	
        	AtomicReference<Integer> t_flag = new AtomicReference<Integer>(t_mail.getGroupFlag());
        	if(MailDbAdapter.modifiedUnreadFlag(t_flag)){
        		t_mail.setGroupFlag(t_flag.get());
        		t_hasUnreadMail = true;
        		
        		en.openBody();
        		
        		// mark the mail as read
        		//
        		m_mainApp.m_dba.markMailRead(t_id);
        		
        		t_markReadMailString.append(t_mail.GetSimpleHashCode()).append(fetchMail.fsm_vectStringSpliter);
        	}
     	
        	m_currMailList.add(en);
        	t_mailCursor.close();
        }
          	
        if(t_hasUnreadMail){
        	
        	// mark the unread group mail as read (database)
        	//
        	m_mainApp.m_dba.markGroupRead(m_currGroupIdx);
        	
        	// send the broadcast to ConnectDeamon and MailListView (MailListActivity)
        	//
        	Intent t_intent = new Intent(YuchDroidApp.FILTER_MARK_MAIL_READ);
        	t_intent.putExtra(YuchDroidApp.DATA_FILTER_MARK_MAIL_READ_GROUPID,m_currGroupIdx);
        	t_intent.putExtra(YuchDroidApp.DATA_FILTER_MARK_MAIL_READ_MAILID,t_markReadMailString.toString());
    		sendBroadcast(t_intent);
        }
                
        // remove all envelope
        //
        while(m_mainMailView.getChildCount() > fsm_insertEnvelopeIndex){
        	m_mainMailView.removeViewAt(fsm_insertEnvelopeIndex);
        }
        
        int t_formerMailView = 0;
        boolean t_showTouchFormerMailView = false;
        for(int i = 0;i < m_currMailList.size();i++){
        	Envelope en = m_currMailList.get(i);
        	if(en.m_initOpen || i == m_currMailList.size() - 1){        		
        		
        		en.openBody();
        		
        		if(en.m_mail.getGroupFlag() == fetchMail.GROUP_FLAG_SEND_ERROR){
        			en.openResendBtn(m_resendListener);
        		}
        		
        		m_mainMailView.addView(en.m_mainView);
        		m_titleOwnAccount.setText(en.m_mail.getOwnAccount());
        	}else{
        		t_formerMailView++;
        		t_showTouchFormerMailView = true;
        	}
        }
        
        if(t_showTouchFormerMailView){

            // set the former mail view touch 
            //
        	m_formerMailView.setVisibility(View.VISIBLE);
            m_formerMailView.setText(getString(R.string.mail_open_show_former_mail) + " (" + t_formerMailView + ")");    
        }else{
        	m_formerMailView.setVisibility(View.GONE);
        }
        
	}
	
	public void onClick(View v) {
		if(v == m_formerMailView){
			m_formerMailView.setVisibility(View.GONE);
			
			for(Envelope en:m_currMailList){
				if(!en.m_initOpen){
					
					en.init();
					en.setOnClickOpenBodyEvent();
					
					if(en.m_mail.getGroupFlag() == fetchMail.GROUP_FLAG_SEND_ERROR){
	        			en.openResendBtn(m_resendListener);
	        		}
					
					m_mainMailView.addView(en.m_mainView,fsm_insertEnvelopeIndex);
				}				
			}
			
		}else if(v == m_touchTop){
			
			m_envelopeScrollView.fullScroll(ScrollView.FOCUS_UP);
			
		}else if(v == m_preGroupBtn){	
			
			try{
				int t_groupCount = m_groupCursor.getCount();
				
				m_nextGroupIdx = m_currGroupIdx;
				m_currGroupIdx = m_preGroupIdx;
				
				m_cursorPosition++;
				if(m_cursorPosition < t_groupCount - 1){
					
					m_groupCursor.moveToPosition(m_cursorPosition + 1);
					m_preGroupIdx = m_groupCursor.getInt(m_groupCursor.getColumnIndex(MailDbAdapter.KEY_ID));
				}else{
					m_preGroupIdx = -1;					
				}
				
				
				fillMailContent();
				
			}catch(Exception e){
				m_mainApp.setErrorString("preGroupBtn",e);
			}			
			
		}else if(v == m_nextGroupBtn){
			
			try{
								
				m_preGroupIdx = m_currGroupIdx;
				m_currGroupIdx = m_nextGroupIdx;
				
				m_cursorPosition--;
				if(m_cursorPosition > 0){
					m_groupCursor.moveToPosition(m_cursorPosition - 1);
					m_nextGroupIdx = m_groupCursor.getInt(m_groupCursor.getColumnIndex(MailDbAdapter.KEY_ID));
				}else{
					m_nextGroupIdx = -1;
				}
				
				fillMailContent();
				
			}catch(Exception e){
				m_mainApp.setErrorString("nextGroupBtn",e);
			}	
		}else if(v == m_replyBtn){
			startComposeMail(fetchMail.REPLY_STYLE);
		}else if(v == m_forwardBtn){
			startComposeMail(fetchMail.FORWORD_STYLE);
		}
	}
	
	private void startComposeMail(int _referenceStyle){
		Intent in = new Intent(this,MailComposeActivity.class);
		in.putExtra(MailComposeActivity.COMPOSE_MAIL_STYLE, _referenceStyle);
		in.putExtra(MailComposeActivity.COMPOSE_MAIL_GROUP_ID,m_currGroupIdx);
		
		m_mainApp.m_composeRefMail = m_currMailList.get(m_currMailList.size() -1).m_mail;	
		startActivity(in);
	}	
}
