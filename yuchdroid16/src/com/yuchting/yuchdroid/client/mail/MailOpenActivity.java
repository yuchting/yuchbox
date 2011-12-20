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

import java.io.BufferedReader;
import java.io.File;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicReference;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.MimeTypeMap;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.yuchting.yuchdroid.client.ConfigInit;
import com.yuchting.yuchdroid.client.ConnectDeamon;
import com.yuchting.yuchdroid.client.GlobalDialog;
import com.yuchting.yuchdroid.client.R;
import com.yuchting.yuchdroid.client.YuchDroidApp;

public class MailOpenActivity extends Activity implements View.OnClickListener{

	public final static String			TAG = "MailOpenActivity";
	
	// start onCreate Intent parameters
	//
	public final static String			INTENT_PRE_MAIL_GROUP_INDEX 	= "pre";
	public final static String			INTENT_NEXT_MAIL_GROUP_INDEX	= "next";
	public final static String			INTENT_NEXT_MAIL_CURSOR_POS		= "pos";
	public final static String			INTENT_CURRENT_GROUP_LIMIT		= "group";
		
	public final static String			INTENT_CURRENT_MAIL_GROUP		= "mail";
		
	
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
	
	public long					m_currGroupIdx = -1;
	public long					m_preGroupIdx = -1;
	public long					m_nextGroupIdx = -1;
	public int						m_cursorPosition;
	public int						m_currGroupLimit;
	
	final MailOpenActivity	m_composeActivity = this;
	BroadcastReceiver		m_sendMailRecv = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {

			long t_groupIndex 	= intent.getLongExtra(YuchDroidApp.DATA_FILTER_MAIL_GROUP_FLAG_GROUP_ID, -1);
			long t_mailIndex 	= intent.getLongExtra(YuchDroidApp.DATA_FILTER_MAIL_GROUP_FLAG_MAIL_ID, -1);
			int t_flag			= intent.getIntExtra(YuchDroidApp.DATA_FILTER_MAIL_GROUP_FLAG_GROUP_FLAG, -1);
			boolean t_refreshBody = intent.getBooleanExtra(YuchDroidApp.DATA_FILTER_MAIL_GROUP_FLAG_REFRESH_BODY, false);
						
			if(t_groupIndex == m_currGroupIdx){
				boolean t_envelopeAdded = false;
				for(Envelope en:m_currMailList){
					if(en.m_mail.getDbIndex() == t_mailIndex){
						t_envelopeAdded = true;
						
						en.m_mailFlag.setImageResource(MailListAdapter.getMailFlagImageId(t_flag));
						
						if(t_refreshBody){
							en.m_mail = m_mainApp.m_dba.fetchMail(t_mailIndex);
							en.refreshData();
							en.openBody();
						}
						
						if(t_flag == fetchMail.GROUP_FLAG_SEND_ERROR){
							en.openResendBtn(m_resendListener);
						}
						
						break;
					}
				}
				
				if(!t_envelopeAdded){
					try{
						fetchMail t_mail = m_mainApp.m_dba.fetchMail(t_mailIndex);
						if(t_mail != null){
							Envelope en = new Envelope(t_mail, m_composeActivity);
							en.openBody();
							
							m_mainMailView.addView(en.m_mainView);
							m_currMailList.add(en);
						}	
					}catch(Exception e){
						m_mainApp.setErrorString(TAG+" sendMailRecv",e);
					}
				}
			}
			
		}
	};
	
	BroadcastReceiver	m_attDownloadDone = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			int t_mailIndex = intent.getIntExtra(YuchDroidApp.DATA_FILTER_DOWNLOAD_ATT_DONE_INDEX,-1);
			int t_attIndex = intent.getIntExtra(YuchDroidApp.DATA_FILTER_DOWNLOAD_ATT_DONE_ATT_INDEX, -1);
			String t_messageID = intent.getStringExtra(YuchDroidApp.DATA_FILTER_DOWNLOAD_ATT_DONE_MSG_ID);
						
			for(Envelope en:m_currMailList){
				if(en.m_mail.GetMailIndex() == t_mailIndex
				&& en.m_mail.getMessageID().equals(t_messageID)){
					en.checkAttFile(t_attIndex);
					break;
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
						t_refMail = m_mainApp.m_dba.fetchMail(en.m_mail.getSendRefMailIndex());						
					}
					
					m_mainApp.sendMail(en.m_mail, t_refMail, en.m_mail.getSendRefMailStyle());
					
					break;
				}
			}			
		}
	};
		
	View.OnClickListener m_clickEnvelopeListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			for(int i = 0;i < m_currMailList.size();i++){
				Envelope en = m_currMailList.get(i);
				if(en.m_mainView == v){
					
					if(!en.m_opened){
						en.openBody();
					}else if(en.m_mail.getGroupFlag() == fetchMail.GROUP_FLAG_SEND_DRAFT){

						((YuchDroidApp)getApplicationContext()).m_composeRefMail = en.m_mail;
						
						Intent in = new Intent(MailOpenActivity.this,MailComposeActivity.class);
						in.putExtra(MailComposeActivity.COMPOSE_MAIL_STYLE, en.m_mail.getSendRefMailStyle());
						in.putExtra(MailComposeActivity.COMPOSE_MAIL_GROUP_ID,en.m_mail.getGroupIndex());		
						in.putExtra(MailComposeActivity.COMPOSE_MAIL_DRAFT,true);
						
						startActivity(in);
	
		            }else{
		            	showDialog(i);
		            }					
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
        m_currGroupLimit = in.getIntExtra(INTENT_CURRENT_GROUP_LIMIT, HomeActivity.MAX_GROUP_FATCH_NUM);
        
        m_groupCursor			= m_mainApp.m_dba.fetchAllGroup(m_currGroupLimit);
                
        try{
        	fillMailContent();
        }catch(Exception e){
        	m_mainApp.setErrorString(TAG, e);
        	finish();
        }
        
        registerReceiver(m_sendMailRecv, new IntentFilter(YuchDroidApp.FILTER_MAIL_GROUP_FLAG));
        registerReceiver(m_attDownloadDone,new IntentFilter(YuchDroidApp.FILTER_DOWNLOAD_ATT_DONE));
    }
	

	
	public void onDestroy(){
		super.onDestroy();
		
		m_groupCursor.close();
		
		unregisterReceiver(m_sendMailRecv);
		unregisterReceiver(m_attDownloadDone);		
	}
	
	protected Dialog onCreateDialog (int id){
		if(id < m_currMailList.size()){
			return m_currMailList.get(id).getEnvelopeDetailDlg();
		}
		return null;
	}
	
	
	private void fillMailContent()throws Exception{   

		 // remove all envelope
        //
        while(m_mainMailView.getChildCount() > fsm_insertEnvelopeIndex){
        	m_mainMailView.removeViewAt(fsm_insertEnvelopeIndex);
        }
        
        m_preGroupBtn.setVisibility(m_preGroupIdx != -1?View.VISIBLE:View.INVISIBLE);
        m_nextGroupBtn.setVisibility(m_nextGroupIdx != -1?View.VISIBLE:View.INVISIBLE);   
    
        Cursor t_mailCursor		= m_mainApp.m_dba.fetchGroup(m_currGroupIdx);
        
        String t_title = t_mailCursor.getString(t_mailCursor.getColumnIndex(MailDbAdapter.GROUP_ATTR_SUBJECT));
        m_titleSubject.setText(t_title);

        String t_mailIndexList	= t_mailCursor.getString(t_mailCursor.getColumnIndex(MailDbAdapter.GROUP_ATTR_MAIL_INDEX));
        String[] t_mailList 	= t_mailIndexList.split(fetchMail.fsm_vectStringSpliter);
        
        t_mailCursor.close();
        
        // fetch Mail data from the group list
        //
        StringBuffer t_markReadMailString_hash = new StringBuffer();
        StringBuffer t_markReadMailString_ID = new StringBuffer();
        boolean t_hasUnreadMail = false;
        boolean t_modifiedFlag;
        long t_id;
        int t_formerMailView = 0;
        boolean t_showTouchFormerMailView = false;
        
        m_currMailList.clear();
        for(int i = 0;i < t_mailList.length;i++){
        	t_id = Long.valueOf(t_mailList[i]).longValue();
        	
        	fetchMail t_mail	= m_mainApp.m_dba.fetchMail(t_id);
        	t_mail.setGroupIndex(m_currGroupIdx);
        	
        	Envelope en = new Envelope(t_mail,this);
        	
        	AtomicReference<Integer> t_flag = new AtomicReference<Integer>(t_mail.getGroupFlag());
        	t_modifiedFlag = MailDbAdapter.modifiedUnreadFlag(t_flag);
        	if(t_modifiedFlag || en.isNeedInitOpen() || i == t_mailList.length - 1){
        		
        		t_mail.setGroupFlag(t_flag.get());
        		en.openBody();
        		en.setOnClickEnvelope(m_clickEnvelopeListener);
        		
        		if(en.m_mail.getGroupFlag() == fetchMail.GROUP_FLAG_SEND_ERROR){
        			en.openResendBtn(m_resendListener);
        		}
        		
        		m_mainMailView.addView(en.m_mainView);
        		m_titleOwnAccount.setText(en.m_mail.getOwnAccount());
        		
        		if(t_modifiedFlag){
        			// mark the mail as read
            		//
        			t_hasUnreadMail = true;
            		m_mainApp.m_dba.markMailRead(t_id);
            		t_markReadMailString_hash.append(t_mail.GetSimpleHashCode()).append(fetchMail.fsm_vectStringSpliter);
            		t_markReadMailString_ID.append(t_mail.getMessageID()).append(fetchMail.fsm_vectStringSpliter);
        		}
        		
        	}else{
        		t_formerMailView++;
        		t_showTouchFormerMailView = true;
        	}
     	
        	m_currMailList.add(en);
        }
          	
        if(t_hasUnreadMail){
        	
        	// mark the unread group mail as read (database)
        	//
        	m_mainApp.m_dba.markGroupRead(m_currGroupIdx);
        	
        	// send the broadcast to ConnectDeamon and MailListView (MailListActivity)
        	//
        	Intent t_intent = new Intent(YuchDroidApp.FILTER_MARK_MAIL_READ);
        	t_intent.putExtra(YuchDroidApp.DATA_FILTER_MARK_MAIL_READ_GROUPID,m_currGroupIdx);
        	t_intent.putExtra(YuchDroidApp.DATA_FILTER_MARK_MAIL_READ_MAILID,t_markReadMailString_ID.toString());
        	t_intent.putExtra(YuchDroidApp.DATA_FILTER_MARK_MAIL_READ_MAIL_HASH,t_markReadMailString_hash.toString());
    		sendBroadcast(t_intent);
        }
        
        if(t_showTouchFormerMailView){

            // set the former mail view touch 
            //
        	m_formerMailView.setVisibility(View.VISIBLE);
            m_formerMailView.setText(getString(R.string.mail_open_show_former_mail) + " (" + t_formerMailView + ")");    
        }else{
        	m_formerMailView.setVisibility(View.GONE);
        }
		
		m_envelopeScrollView.fullScroll(ScrollView.FOCUS_UP);        
	}
	

	
	public void onClick(View v) {
		if(v == m_formerMailView){
			m_formerMailView.setVisibility(View.GONE);
			
			int t_startIdx = fsm_insertEnvelopeIndex;
			
			for(Envelope en:m_currMailList){
				
				if(!en.m_opened){
					en.setOnClickEnvelope(m_clickEnvelopeListener);
					
					if(en.m_mail.getGroupFlag() == fetchMail.GROUP_FLAG_SEND_ERROR){
	        			en.openResendBtn(m_resendListener);
	        		}
					
					m_mainMailView.addView(en.m_mainView,t_startIdx++);
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
			startReplyForwardMail(fetchMail.REPLY_STYLE);
		}else if(v == m_forwardBtn){
			startReplyForwardMail(fetchMail.FORWORD_STYLE);
		}else if(v == m_deleteBtn){
			if(m_currGroupIdx != -1){

				GlobalDialog.showYesNoDialog(getString(R.string.mail_open_delete_prompt), this, 
				new GlobalDialog.YesNoListener() {
					
					@Override
					public void click() {

						if(m_mainApp.m_config.m_delRemoteMail){
							// send deleting messge to server
							//
							StringBuffer t_hashList = new StringBuffer();
							StringBuffer t_messageList = new StringBuffer();
							for(Envelope en:m_currMailList){
								if(!en.m_mail.isOwnSendMail()){
									t_hashList.append(en.m_mail.GetSimpleHashCode()).append(fetchMail.fsm_vectStringSpliter);
									t_messageList.append(en.m_mail.getMessageID()).append(fetchMail.fsm_vectStringSpliter);
								}
							}
							
							// send broadcast to ConnectDeamon
							//
							Intent t_intent = new Intent(YuchDroidApp.FILTER_DELETE_MAIL);
							t_intent.putExtra(YuchDroidApp.DATA_FILTER_MARK_MAIL_READ_GROUPID,m_currGroupIdx);
				        	t_intent.putExtra(YuchDroidApp.DATA_FILTER_MARK_MAIL_READ_MAIL_HASH,t_hashList.toString());
				        	t_intent.putExtra(YuchDroidApp.DATA_FILTER_MARK_MAIL_READ_MAILID,t_messageList.toString());			        	
				        	
				    		sendBroadcast(t_intent);
						}
						
						m_mainApp.m_dba.deleteGroup(m_currGroupIdx);
						
						Intent in = new Intent(YuchDroidApp.FILTER_MAIL_GROUP_FLAG);
						sendBroadcast(in);
						
						finish();
													
					}
				},null);
			}
		}
	}
	
	private void startReplyForwardMail(int _referenceStyle){
		
		Envelope t_envelope = null;
		
		for(int i = m_currMailList.size() - 1;i >= 0 ;i--){
			Envelope en = m_currMailList.get(i);
			
			if(_referenceStyle == fetchMail.REPLY_STYLE && en.m_mail.isOwnSendMail()){
				continue;
			}
			
			if(en.m_mail.getGroupFlag() != fetchMail.GROUP_FLAG_SEND_DRAFT){
				t_envelope = en;
				break;
			}
		}
				
		if(t_envelope != null){
			m_mainApp.m_composeRefMail = t_envelope.m_mail;
			
			Intent in = new Intent(this,MailComposeActivity.class);
			in.putExtra(MailComposeActivity.COMPOSE_MAIL_STYLE, _referenceStyle);
			in.putExtra(MailComposeActivity.COMPOSE_MAIL_GROUP_ID,m_currGroupIdx);
			in.putExtra(MailComposeActivity.COMPOSE_MAIL_DRAFT, false);
			
			startActivity(in);	
		}		
	}
	
	public static final class Envelope{
		
		fetchMail		m_mail		= null;
		
		Activity		m_loadCtx	= null;
		
        TextView 		m_fromAddr	= null;
        TextView 		m_date		= null;
        TextView 		m_time		= null;
        TextView 		m_toAddr	= null;        
        TextView 		m_subject	= null;
		
		RelativeLayout	m_mainView	= null;
		TextView		m_bodyText	= null;
		WebView			m_htmlText	= null;
		
		LinearLayout	m_attachView	= null;
		
		ImageView		m_mailFlag	= null;
		TextView		m_touchHTML	= null;
		
		Button			m_resendBtn	= null;
						
		boolean		m_opened = false;
		
		public Envelope(fetchMail _mail,Activity _ctx){
			m_mail		= _mail;
			m_loadCtx	= _ctx;
		}
		
		public void openBody(){
			openBody_impl(true);
		}
		
		private void openBody_impl(boolean _simple){
			init();
			
			m_opened = true;
			
			final ConfigInit t_config = ((YuchDroidApp)m_loadCtx.getApplicationContext()).m_config;
			
			boolean t_displayTextWhenHTML = t_config.m_displayTextWhenHTML;
			boolean t_displayText = m_mail.GetContain().length() != 0;
			
			if(m_mail.GetContain_html().length() != 0 && !t_displayTextWhenHTML){
				t_displayText = false;
			}			
			
			if(t_displayText){
				
				m_bodyText.setVisibility(View.VISIBLE);
				CharSequence t_str;
				if(_simple){
					try{
						t_str = getProcessedContain(m_mail.GetContain());
					}catch (Exception e) {
						t_str = m_mail.GetContain();
					}	 
				}else{
					t_str = m_mail.GetContain();
				}
				m_bodyText.setTextSize(TypedValue.COMPLEX_UNIT_SP, t_config.getMailFontSize());
				m_bodyText.setText(t_str);
            }else{
            	m_bodyText.setVisibility(View.GONE);
            }
			
            if(m_mail.GetContain_html().length() != 0){
            	
            	if(t_displayText){
            		
            		m_touchHTML.setVisibility(View.VISIBLE);
                	m_touchHTML.setOnClickListener(new View.OnClickListener() {
        				
        				@Override
        				public void onClick(View paramView){
        					m_touchHTML.setVisibility(View.GONE);
        					m_htmlText.setVisibility(View.VISIBLE);
        					m_htmlText.getSettings().setDefaultFontSize(t_config.getMailFontSize());
        					m_htmlText.loadDataWithBaseURL("",m_mail.GetContain_html(),"text/html","utf-8","");
        				}
        			});
                	
            	}else{
            		m_touchHTML.setVisibility(View.GONE);
					m_htmlText.setVisibility(View.VISIBLE);
					m_htmlText.getSettings().setDefaultFontSize(t_config.getMailFontSize());
					m_htmlText.loadDataWithBaseURL("",m_mail.GetContain_html(),"text/html","utf-8","");
            	}         	     	
            	
            }else{
            	m_touchHTML.setVisibility(View.GONE);
            }
            
            
		}
		
		private static class SpanSeg{
			int start;
			int end;
		}	
		
		private CharSequence getProcessedContain(String _orgContain)throws Exception{
			
			// replace the reference mail part with click string
			// to load former mail 
			//
			BufferedReader t_read = new BufferedReader(new StringReader(_orgContain));
			boolean t_formerRef = false;
			
			String t_referenceLine = m_loadCtx.getString(R.string.mail_open_reference_line_replace);						
			Vector<SpanSeg> t_segList = new Vector<SpanSeg>();
			
			StringBuffer t_finalString = new StringBuffer();
			String line;
			while((line = t_read.readLine()) != null){
				if(line.startsWith(">")){
					// reference line
					//
					if(!t_formerRef){
						t_formerRef = true;
						
						t_finalString.append("\n   ");
						
						SpanSeg t_seg = new SpanSeg();
						t_seg.start = t_finalString.length();
						t_seg.end = t_seg.start + t_referenceLine.length();
						
						t_segList.add(t_seg);
						
						t_finalString.append(t_referenceLine).append("\n");
					}
				}else{
					t_finalString.append(line).append("\n");
				}				
			}
			
			SpannableString t_result = new SpannableString(t_finalString.toString());
			for(SpanSeg seg:t_segList){
				t_result.setSpan(new ClickableSpan() {
					
					@Override
					public void onClick(View widget) {
						openBody_impl(false);						
					}
				},seg.start, seg.end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
			}
			
			return t_result;
		}
		
		public void init(){
			if(m_mainView != null){
				return;
			}
			
			LayoutInflater t_inflater = LayoutInflater.from(m_loadCtx);
			m_mainView = (RelativeLayout)t_inflater.inflate(R.layout.mail_open_envelope, null);
			
	        // fill mail data
	        //
	        m_fromAddr = (TextView)m_mainView.findViewById(R.id.mail_open_from_addr);
	        m_date		= (TextView)m_mainView.findViewById(R.id.mail_open_date);
	        m_time		= (TextView)m_mainView.findViewById(R.id.mail_open_time);
	        m_toAddr	= (TextView)m_mainView.findViewById(R.id.mail_open_recv_addr);        
	        m_subject	= (TextView)m_mainView.findViewById(R.id.mail_open_subject);
	        
	        m_mailFlag	= (ImageView)m_mainView.findViewById(R.id.mail_open_flag);
	        m_bodyText	= (TextView)m_mainView.findViewById(R.id.mail_open_body);
	        m_htmlText	= (WebView)m_mainView.findViewById(R.id.mail_open_html);
	        m_touchHTML	= (TextView)m_mainView.findViewById(R.id.mail_open_html_switch);
	        m_resendBtn = (Button)m_mainView.findViewById(R.id.mail_open_resend_btn);
	        
	        m_attachView	= (LinearLayout)m_mainView.findViewById(R.id.mail_open_attachment_view);
	        
	        refreshData();
		}
		
		public String getDateString(){
			SimpleDateFormat t_format = new SimpleDateFormat("yyyy"+m_loadCtx.getString(R.string.mail_time_year)+
						"MM"+m_loadCtx.getString(R.string.mail_time_month)+
						"dd" + m_loadCtx.getString(R.string.mail_time_day));

			return t_format.format(m_mail.GetSendDate());
		}
		
		public String getTimeString(){
	        return (new SimpleDateFormat("HH:mm").format(m_mail.GetSendDate()));
		}
		
		public void refreshData(){
			init();
			
			fetchMail.Address[] t_fromAddrList 	= fetchMail.parseAddressList(m_mail.GetFromVect());
			fetchMail.Address[] t_toAddrList		= fetchMail.parseAddressList(m_mail.GetSendToVect());
	        
	        if(t_fromAddrList.length != 0){
	        	m_fromAddr.setText(t_fromAddrList[0].m_name.length() != 0?t_fromAddrList[0].m_name:
	        						t_fromAddrList[0].m_addr);
	        }
	        
	        m_date.setText(getDateString());
	        m_time.setText(getTimeString());
	        
	        m_toAddr.setText(m_loadCtx.getString(R.string.mail_open_recipient_addr_prefix) + MailListAdapter.getShortAddrList(t_toAddrList));
	        m_subject.setText(m_mail.GetSubject());
	        m_mailFlag.setImageResource(MailListAdapter.getMailFlagImageId(m_mail.getGroupFlag()));
	        
	        // set the attachment file
	        //
	        m_attachView.removeAllViews();
        	m_attachView.setVisibility(View.GONE);
        	
	        if(!m_mail.GetAttachment().isEmpty()){
	        	m_attachView.setVisibility(View.VISIBLE);
	        	
	        	LayoutInflater t_inflater = LayoutInflater.from(m_loadCtx);
	        	
	        	final View.OnClickListener t_cancelclick = new View.OnClickListener() {
					@Override
					public void onClick(final View v){
						int num = m_attachView.getChildCount();
						for(int i = 0;i < num;i++){
							View child = m_attachView.getChildAt(i);
							if(v == child.findViewById(R.id.mail_open_attachment_item_cancel_btn)){
								final int t_index = i;
								
								GlobalDialog.showYesNoDialog(m_loadCtx.getString(R.string.mail_open_attach_cancel_btn_prompt),m_loadCtx, 
								new GlobalDialog.YesNoListener() {
									@Override
									public void click(){
										ConnectDeamon.stopDownload(m_mail, t_index);
										v.setVisibility(View.GONE);
									}
								},null);
								
								break;
							}
						}
					}
				};
				
				final View.OnClickListener t_click = new View.OnClickListener() {
					
					@Override
					public void onClick(View v){
						// load the Attachment
						//
						if(!ConnectDeamon.isConnected()){
							Toast.makeText(m_loadCtx, m_loadCtx.getString(R.string.mail_open_attach_prompt_disconnect), Toast.LENGTH_SHORT).show();
							return ;
						}
						if(ConnectDeamon.isAttachDownload()){
							Toast.makeText(m_loadCtx, m_loadCtx.getString(R.string.mail_open_attach_prompt_has_download), Toast.LENGTH_SHORT).show();
							return ;
						}
						
						int t_num = m_attachView.getChildCount();
						for(int i = 0;i < t_num;i++){
							if(m_attachView.getChildAt(i) == v){
								startDownloadAttachment(i);
								
								Button t_cancel = (Button)v.findViewById(R.id.mail_open_attachment_item_cancel_btn);
			        			t_cancel.setVisibility(View.VISIBLE);
			        			t_cancel.setOnClickListener(t_cancelclick);
								break;
							}
						}
					}
				};
				
	        	for(int i = 0;i < m_mail.GetAttachment().size();i++){
	        		fetchMail.MailAttachment att = m_mail.GetAttachment().get(i);
	        		ViewGroup attachView = (ViewGroup)t_inflater.inflate(R.layout.mail_open_attachment_item,null);
	        		TextView filename = (TextView)attachView.findViewById(R.id.mail_open_attachment_item_filename);
	        		filename.setText(att.m_name + " ("+YuchDroidApp.GetByteStr(att.m_size) + ")");
	        			        		
	        		LinearLayout.LayoutParams t_lp = new LinearLayout.LayoutParams(
	        				LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
	        		
	        		m_attachView.addView(attachView,t_lp);
	        		
	        		if(ConnectDeamon.hasAttachmentDownload(m_mail,i)){
	        			Button t_cancel = (Button)attachView.findViewById(R.id.mail_open_attachment_item_cancel_btn);
	        			t_cancel.setVisibility(View.VISIBLE);
	        			t_cancel.setOnClickListener(t_cancelclick);
	        		}else{
	        			if(m_mail.isOwnSendMail()){
	        				// show directly the open button to open 
	        				//
	        				showOpenAttButton(new File(att.m_name), attachView);
	        			}else{
		        			// check whether has been downloaded
		        			//
		        			if(!checkAttFile(i)){
		        				attachView.setOnClickListener(t_click);
		        			}	        				
	        			}
	        		}
	        	}
	        }	        
		}
		
		final static String[]	fsm_supportImageFormat = 
		{
			".jpg",
			".png",
			".bmp",
			".tga",
			".gif",
		};
		public boolean checkAttFile(final int _attachIdx){
			assert m_attachView.getChildCount() > _attachIdx;
			assert m_mail.GetAttachment().size() > _attachIdx;
			
			fetchMail.MailAttachment t_att = m_mail.GetAttachment().get(_attachIdx);
			YuchDroidApp t_mainApp = (YuchDroidApp)m_loadCtx.getApplicationContext();
			final File t_filename = new File(t_mainApp.getAttachmentDir(),t_att.m_name);
			
			if(t_filename.exists()){
				
				if(t_filename.length() >= t_att.m_size){

					ViewGroup t_attachView = (ViewGroup)m_attachView.getChildAt(_attachIdx);
					
					String t_name = t_att.m_name.toLowerCase();
					for(String suffix:fsm_supportImageFormat){
						if(t_name.endsWith(suffix)){
							
							Bitmap t_image = BitmapFactory.decodeFile(t_filename.getPath());
							
							if(t_image != null){
								ImageView t_imageView = (ImageView)t_attachView.findViewById(R.id.mail_open_attachment_item_image);
								t_imageView.setImageBitmap(t_image);
								t_imageView.setVisibility(View.VISIBLE);
							}
							
							break;
						}
					}					
					
					Button t_cancel = (Button)t_attachView.findViewById(R.id.mail_open_attachment_item_cancel_btn);
	    			t_cancel.setVisibility(View.GONE);
					
	    			showOpenAttButton(t_filename,t_attachView);
	    								
					// disable the click listener
					//
					t_attachView.setOnClickListener(null);
					
					return true;
					
				}else{
					t_filename.delete();
				}
				
			}			
			
			return false;
		}
		
		private void showOpenAttButton(final File _file,ViewGroup _attachView){
			
			Button t_open = (Button)_attachView.findViewById(R.id.mail_open_attachment_item_open_btn);
			t_open.setVisibility(View.VISIBLE);
			t_open.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// open the attachment by its extent filename
					//
				    Intent intent = new Intent();
	                intent.setAction(android.content.Intent.ACTION_VIEW);
	                intent.setDataAndType(Uri.fromFile(_file),YuchDroidApp.getFileMIMEType(_file));
	               
	                m_loadCtx.startActivity(Intent.createChooser(intent,m_loadCtx.getString(R.string.mail_open_attach_select_prompt)));
				}
			});
		}
		
		private void startDownloadAttachment(int _idx){
			
			long t_preGroupIndex = -1;
			long t_nextGroupIndex = -1;
			long t_currCursorPos = 0;
			int t_limit			= HomeActivity.MAX_GROUP_FATCH_NUM;
			
			if(m_loadCtx instanceof MailOpenActivity){
				MailOpenActivity t_mailOpenActivity;
				t_mailOpenActivity = (MailOpenActivity)m_loadCtx;
				
				t_preGroupIndex 	= t_mailOpenActivity.m_preGroupIdx;
				t_nextGroupIndex	= t_mailOpenActivity.m_nextGroupIdx;
				t_currCursorPos		= t_mailOpenActivity.m_cursorPosition;
				t_limit				= t_mailOpenActivity.m_currGroupLimit;
			}
			
			Intent in = new Intent(Intent.ACTION_MAIN);
			in.setClass(m_loadCtx, MailOpenActivity.class);
			
			in.putExtra(MailOpenActivity.INTENT_CURRENT_MAIL_GROUP, m_mail.getGroupIndex());
			in.putExtra(MailOpenActivity.INTENT_PRE_MAIL_GROUP_INDEX, t_preGroupIndex);
			in.putExtra(MailOpenActivity.INTENT_NEXT_MAIL_GROUP_INDEX, t_nextGroupIndex);
			in.putExtra(MailOpenActivity.INTENT_NEXT_MAIL_CURSOR_POS,t_currCursorPos);
			in.putExtra(MailOpenActivity.INTENT_CURRENT_GROUP_LIMIT,t_limit);										

			PendingIntent contentIntent = PendingIntent.getActivity(m_loadCtx, 0, in,PendingIntent.FLAG_UPDATE_CURRENT);
			
			ConnectDeamon.startDownload(m_mail,_idx,contentIntent);
		}
				
		public void setOnClickEnvelope(View.OnClickListener _l){
			init();
			m_mainView.setOnClickListener(_l);
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
		
		public boolean isNeedInitOpen(){
			return m_mail.getGroupFlag() == fetchMail.GROUP_FLAG_SEND_DRAFT
			    	|| m_mail.getGroupFlag() == fetchMail.GROUP_FLAG_SEND_ERROR
			    	|| m_mail.getGroupFlag() == fetchMail.GROUP_FLAG_SEND_SENDING;
		}
		
		public Dialog getEnvelopeDetailDlg(){
			
			View.OnClickListener t_emailClick = new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					String t_to = ((TextView)v).getText().toString();
					Intent in = new Intent(Intent.ACTION_SEND);
					in.setType("text/plain");
					in.putExtra(Intent.EXTRA_EMAIL,new String[]{t_to});
					m_loadCtx.startActivity(Intent.createChooser(in, m_loadCtx.getString(R.string.mail_open_detail_email_click_prompt)));
				}
			};
			
			String t_form 		= m_mail.GetFromVect().isEmpty()?"":m_mail.GetFromVect().get(0);
			String t_subject 	= m_mail.GetSubject();

			LayoutInflater inflater = LayoutInflater.from(m_loadCtx);
			View layout = inflater.inflate(R.layout.mail_open_envelope_detail,null);
			
			AlertDialog.Builder builder = new AlertDialog.Builder(m_loadCtx);
			builder.setView(layout);
			builder.setPositiveButton(R.string.dlg_info_yesno_confirm, null);
			
			final Dialog dialog = builder.create();
			TextView t_fromView = (TextView)layout.findViewById(R.id.mail_open_detail_from);
			t_fromView.setText(t_form);
			t_fromView.setOnClickListener(t_emailClick);
			
			addDetailAddr((ViewGroup)layout.findViewById(R.id.mail_open_detail_to_list),m_mail.GetSendToVect(),t_emailClick);
			addDetailAddr((ViewGroup)layout.findViewById(R.id.mail_open_detail_cc_list),m_mail.GetCCToVect(),t_emailClick);
			addDetailAddr((ViewGroup)layout.findViewById(R.id.mail_open_detail_bcc_list),m_mail.GetBCCToVect(),t_emailClick);
			
			TextView t_subjectView 	= (TextView)layout.findViewById(R.id.mail_open_detail_subject);
			t_subjectView.setText(t_subject);
			
			TextView t_dataView		= (TextView)layout.findViewById(R.id.mail_open_detail_date);
			t_dataView.setText(getDateString() + " " + getTimeString());
			
			return dialog;
		}
		
		private void addDetailAddr(ViewGroup _view,Vector<String> _addrList,View.OnClickListener _click){

			_view.setVisibility(_addrList.isEmpty()?View.GONE:View.VISIBLE);
			
			for(String to:_addrList){
				TextView v = new TextView(m_loadCtx);
				v.setLayoutParams(
						new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
									LinearLayout.LayoutParams.WRAP_CONTENT));
				v.setPadding(10, 10, 10, 10);
				v.setText(to);
				v.setOnClickListener(_click);
				_view.addView(v);
				
			}	
		}
		
		
	}	
}
