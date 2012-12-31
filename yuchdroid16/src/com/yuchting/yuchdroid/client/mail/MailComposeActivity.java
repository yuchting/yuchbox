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

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

import com.yuchting.yuchdroid.client.ConnectDeamon;
import com.yuchting.yuchdroid.client.GlobalDialog;
import com.yuchting.yuchdroid.client.R;
import com.yuchting.yuchdroid.client.YuchDroidApp;

public class MailComposeActivity extends Activity implements View.OnClickListener{

	public final static String	TAG = MailComposeActivity.class.getName();
	
	public final static String	COMPOSE_MAIL_STYLE = "style";
	public final static String	COMPOSE_MAIL_GROUP_ID = "groupId";
	public final static String	COMPOSE_MAIL_DRAFT = "draft";
	
	AutoCompleteTextView	m_to	= null;
	AutoCompleteTextView	m_cc	= null;
	AutoCompleteTextView	m_bcc	= null;
	
	EditText	m_subject			= null;
	EditText	m_body				= null;
	
	TextView	m_titleSubject		= null;
	TextView	m_titleOwnAccount	= null;
		
	TextView	m_discardRefView	= null;
	
	ViewGroup	m_attachmentParent	= null;
	Vector<fetchMail.MailAttachment>	m_attachmentList = new Vector<fetchMail.MailAttachment>();
	
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
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.mail_compose);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.mail_open_title);
		
		m_mainApp = (YuchDroidApp)getApplicationContext();
		
		m_titleSubject			= (TextView)findViewById(R.id.mail_open_title_subject);
	    m_titleOwnAccount		= (TextView)findViewById(R.id.mail_open_title_own_account);
		
		m_to		= (AutoCompleteTextView)findViewById(R.id.mail_compose_to);
		m_cc		= (AutoCompleteTextView)findViewById(R.id.mail_compose_cc);
		m_bcc		= (AutoCompleteTextView)findViewById(R.id.mail_compose_bcc);
		
		m_subject	= (EditText)findViewById(R.id.mail_compose_subject);
		m_body		= (EditText)findViewById(R.id.mail_compose_body);
		m_body.setTextSize(TypedValue.COMPLEX_UNIT_DIP, m_mainApp.m_config.getMailFontSize());
		
		m_mainView 	= (LinearLayout)findViewById(R.id.mail_compose_main_view);
						
		m_discardRefView = (TextView)findViewById(R.id.mail_compose_ref_label);
		m_discardRefView.setOnClickListener(this);		
		
		m_sendBtn 	= (Button)findViewById(R.id.mail_compose_send_btn);
		m_sendBtn.setOnClickListener(this);
		
		m_saveBtn	= (Button)findViewById(R.id.mail_compose_save_btn);
		m_saveBtn.setOnClickListener(this);
		
		m_discardBtn = (Button)findViewById(R.id.mail_compose_discard_btn);
		m_discardBtn.setOnClickListener(this);
		
		m_attachmentParent	= (ViewGroup)findViewById(R.id.mail_compose_attachment_parent);
								
		prepareData();
		
		// set the modified flag
		//
		final TextWatcher t_watcher = new TextWatcher(){
			public void afterTextChanged (Editable s){}
			public void beforeTextChanged (CharSequence s, int start, int count, int after){}
			public void onTextChanged(CharSequence s, int start, int before, int count){
				m_modified = true;
				m_saveBtn.setEnabled(true);
			}
		};
		
		final TextWatcher t_subjectWatcher = new TextWatcher() {
			
			public void afterTextChanged (Editable s){}
			public void beforeTextChanged (CharSequence s, int start, int count, int after){}
			public void onTextChanged(CharSequence s, int start, int before, int count){
				t_watcher.onTextChanged(s, start, before, count);
				m_titleSubject.setText(s);
			}
		};
		m_to.addTextChangedListener(t_watcher);
		m_bcc.addTextChangedListener(t_watcher);
		m_cc.addTextChangedListener(t_watcher);
		m_body.addTextChangedListener(t_watcher);
		m_subject.addTextChangedListener(t_subjectWatcher);

		setAutoCompleteEditText(m_cc);
		setAutoCompleteEditText(m_bcc);
		setAutoCompleteEditText(m_to);
		
		m_modified = false;
		m_saveBtn.setEnabled(false);
		
//		processAds();		
	}
	
	private void prepareData(){
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
					// has reference mailindex
					//
					fetchMail t_mail = m_mainApp.m_dba.fetchMail(m_draftMail.getSendRefMailIndex());
					if(t_mail != null){
						m_referenceMail = new MailOpenActivity.Envelope(t_mail, this);
					}
				}else{
					// the new compose mail
					//
					loadSendMailAccountList();
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
			Vector<String> t_toVect = null;
			Vector<String> t_ccVect = null;
			Vector<String> t_bccVect = null;
			if(t_draft && m_draftMail != null){
				
				// draft
				//
				t_sub = m_draftMail.GetSubject();
				m_body.setText(m_draftMail.GetContain());
				t_toVect = m_draftMail.GetSendToVect();
				t_ccVect = m_draftMail.GetCCToVect();
				t_bccVect = m_draftMail.GetBCCToVect();
								
				refreshAttachment(m_draftMail);
								
			}else{
								
				if(m_referenceMailStyle == fetchMail.REPLY_STYLE
				|| m_referenceMailStyle == fetchMail.REPLY_ALL_STYLE){
					// reply
					//
					if(m_referenceMail != null){
						t_sub = MailDbAdapter.getReplySubject(m_referenceMail.m_mail.GetSubject(), getString(R.string.mail_compose_reply_prefix));
					}else{
						t_sub = getString(R.string.mail_compose_reply_prefix);
					}
					
				}else{
					// forward style
					//
					if(m_referenceMail != null){
						t_sub = getString(R.string.mail_compose_forward_prefix) + m_referenceMail.m_mail.GetSubject();
					}else{
						t_sub = getString(R.string.mail_compose_forward_prefix);
					}
				}
								
				if(m_referenceMail != null 
				&& m_referenceMailStyle != fetchMail.FORWORD_STYLE){

					if(m_referenceMail.m_mail.isOwnSendMail()){
						t_toVect = m_referenceMail.m_mail.GetSendToVect();
					}else{
						
						if(m_referenceMailStyle == fetchMail.REPLY_ALL_STYLE){
							
							t_toVect = new Vector<String>();
							
							for(String add : m_referenceMail.m_mail.GetReplyToVect()){
								t_toVect.add(add);
							}

							for(String add : m_referenceMail.m_mail.GetFromVect()){
								boolean t_added = true;
								
								for(String to : t_toVect){
									if(to.indexOf(add) != -1 || add.indexOf(to) != -1){
										t_added = false;
										break;
									}
								}
								
								if(t_added){
									t_toVect.add(add);
								}
							}
							
							for(String add : m_referenceMail.m_mail.GetSendToVect()){
								
								boolean t_added = true;
								
								for(String to : t_toVect){
									if(to.indexOf(add) != -1 || add.indexOf(to) != -1){
										t_added = false;
										break;
									}
								}
								
								if(t_added){
									for(String acc : m_mainApp.m_config.m_sendMailAccountList){
										if(acc.indexOf(add) != -1 || add.indexOf(acc) != -1){
											t_added = false;
											break;
										}
									}
									
									if(t_added){
										t_toVect.add(add);
									}
								}								
							}
						}else{
							t_toVect = m_referenceMail.m_mail.GetReplyToVect().isEmpty()?
									m_referenceMail.m_mail.GetFromVect():
									m_referenceMail.m_mail.GetReplyToVect();
						}
					}
					
					if(m_referenceMailStyle == fetchMail.REPLY_ALL_STYLE){
						t_ccVect = m_referenceMail.m_mail.GetCCToVect();
						t_bccVect = m_referenceMail.m_mail.GetBCCToVect();
					}
				}
			}
			

			if(t_ccVect != null && !t_ccVect.isEmpty()){
				showCc_Bcc();
				setEmailAddr(m_cc, t_ccVect);
			}
			
			if(t_bccVect != null && !t_bccVect.isEmpty()){
				showCc_Bcc();
				setEmailAddr(m_bcc, t_bccVect);
			}
			
			if(t_toVect != null){
				setEmailAddr(m_to,t_toVect);
				m_body.requestFocus();
			}else{
				m_to.requestFocus();
			}				
			
			m_titleSubject.setText(t_sub);		
			m_subject.setText(t_sub);
			
			if(m_referenceMail != null){
				m_titleOwnAccount.setText(m_referenceMail.m_mail.getOwnAccount());
			}
			
		}else{
			
			// compose a new mail
			//
			m_discardRefView.setVisibility(View.GONE);
			m_to.requestFocus();
			
			m_titleSubject.setText("");
			
			loadSendMailIntentData();			
			loadSendMailAccountList();
		}
	}
	
	
	private void loadSendMailIntentData(){
		// load the intent data of android.content.action.ACTION_SEND
		//
		Intent in = getIntent();			
		if(in.getAction() != null 
			&& in.getAction().equals(Intent.ACTION_SEND)){
			
			if(in.getExtras() != null){
				Object t_streamObject = in.getExtras().get(Intent.EXTRA_STREAM);
				if(t_streamObject != null && t_streamObject instanceof Uri){
					Uri t_uri = (Uri)t_streamObject;
					try{
							
						AssetFileDescriptor afd = getContentResolver().openAssetFileDescriptor(t_uri, "r");
						FileInputStream input = afd.createInputStream();
						try{

						    int ch;
						    StringBuffer vcfString = new StringBuffer("");
						    while ((ch = input.read()) != -1){
						    	vcfString.append((char) ch);
						    }
						    
						    // the vcfString is vcf file to store the 
						    // contact information 
						    // TODO : add attachment file
						    //
						    //System.out.println(vcfString.toString());
						    
						}finally{
							input.close();
						}
						
					}catch(Exception e){
						m_mainApp.setErrorString(TAG,e);
					}
				}
			}
			
			setEmailAddr(m_to,in.getStringArrayExtra(Intent.EXTRA_EMAIL));
			
			boolean t_cc_bcc = setEmailAddr(m_cc,in.getStringArrayExtra(Intent.EXTRA_CC));
			t_cc_bcc = t_cc_bcc || setEmailAddr(m_bcc,in.getStringArrayExtra(Intent.EXTRA_BCC));
			
			if(t_cc_bcc){
				showCc_Bcc();
			}
			
			String t_subject = in.getStringExtra(Intent.EXTRA_SUBJECT);
			if(t_subject != null){
				m_subject.setText(t_subject);
			}else{
				m_subject.requestFocus();
			}
			
			String t_body = in.getStringExtra(Intent.EXTRA_TEXT);
			if(t_body != null){
				m_body.setText(t_body);
			}else{
				if(t_subject != null){
					m_body.requestFocus();
				}
			}
		}
	}
	
	private void loadSendMailAccountList(){
		
		if(!m_mainApp.m_config.m_sendMailAccountList.isEmpty()){
			
			// load the spinner widget 
			//
			m_ownAccountSpinner = (Spinner)findViewById(R.id.mail_compose_own_account);
			m_ownAccountSpinner.setVisibility(View.VISIBLE);
			
			// load the sender Mail account list
			//
			String[] t_ownAccountList = new String[m_mainApp.m_config.m_sendMailAccountList.size()];
			for(int i = 0;i < m_mainApp.m_config.m_sendMailAccountList.size();i++){
				t_ownAccountList[i] = m_mainApp.m_config.m_sendMailAccountList.get(i);
			}

			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,t_ownAccountList);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			m_ownAccountSpinner.setAdapter(adapter);
			
			if(m_mainApp.m_config.m_defaultSendMailAccountIndex >= m_mainApp.m_config.m_sendMailAccountList.size()){
				m_mainApp.m_config.m_defaultSendMailAccountIndex = 0;
			}
			
			m_ownAccountSpinner.setSelection(m_mainApp.m_config.m_defaultSendMailAccountIndex,true);
			m_titleOwnAccount.setText(m_mainApp.m_config.m_sendMailAccountList.get(m_mainApp.m_config.m_defaultSendMailAccountIndex));
			
			// set the listener
			//
			m_ownAccountSpinner.setOnItemSelectedListener(new OnItemSelectedListener(){
				public void onItemSelected(AdapterView<?> parent,View view, int pos, long id) {
					m_modified = true;
					m_saveBtn.setEnabled(true);
					
					m_titleOwnAccount.setText(prepareOwnAccount());
			    }

			    public void onNothingSelected(AdapterView<?> parent){}
			});
		}
	}
	
	final class EmailAddrAdapter extends BaseAdapter implements Filterable {

		private LayoutInflater mInflater;
		private String[]			m_addrList;
		private ArrayList<String> m_displayList;
		
		public EmailAddrAdapter(Context _ctx,String[] _addrList){
			mInflater = LayoutInflater.from(_ctx);
			m_addrList = _addrList;
			
			m_displayList = new ArrayList<String>(_addrList.length);
			for(String addr:_addrList){
				m_displayList.add(addr);
			}
		}
		
		@Override
		public int getCount() {
			return m_displayList.size();
		}

		@Override
		public Object getItem(int position) {
			return m_displayList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			return createViewFromResource(position,convertView,parent,android.R.layout.simple_dropdown_item_1line);
		}
		
		// copy from the android source code
		//
		private View createViewFromResource(int position, View convertView, ViewGroup parent,
	            int resource) {
	        View view;
	        TextView text;

	        if (convertView == null) {
	            view = mInflater.inflate(resource, parent, false);
	        }else{
	            view = convertView;
	        }

	        try{
	        	text = (TextView) view;
	        	text.setText((String)getItem(position));
	        }catch(ClassCastException e) {
	            Log.e("ArrayAdapter", "You must supply a resource ID for a TextView");
	            throw new IllegalStateException(
	                    "ArrayAdapter requires the resource ID to be a TextView", e);
	        }           

	        return view;
	    }
		
		Filter 	m_filter = null;
		public Filter getFilter(){
			if(m_filter == null){
				m_filter = new Filter(){
					@Override
			        protected FilterResults performFiltering(CharSequence prefix) {
			            FilterResults results = new FilterResults();


			            if(prefix == null || prefix.length() == 0) {
 
		                    ArrayList<String> list = new ArrayList<String>(m_addrList.length);
		                    for(int i = 0;i < m_addrList.length;i++){
		                    	list.add(m_addrList[i]);
		                    }
		                    
		                    results.values = list;
		                    results.count = list.size();
			                
			            }else{
			                String prefixString = prefix.toString().toLowerCase();
			                
			                int t_lastComma = prefixString.lastIndexOf(',');
			                if(t_lastComma != -1){
			                	prefixString = prefixString.substring(t_lastComma + 1);
			                }
			                
			                final ArrayList<String> newValues = new ArrayList<String>();

			                String t_textValue;
			                String t_lowTextValue;
			                
			                for (int i = 0; i < m_addrList.length; i++) {

			                	t_textValue		= m_addrList[i];
			                	t_lowTextValue	= m_addrList[i].toLowerCase();

			                    // First match against the whole, non-splitted value
			                	//
			                    if (t_lowTextValue.startsWith(prefixString)) {
			                        newValues.add(t_textValue);
			                    }else{			                    	
			                    	final String[] words = t_lowTextValue.split(" ");
			                        final int wordCount = words.length;

			                        for (int k = 0; k < wordCount; k++) {
			                            if (words[k].startsWith(prefixString) || words[k].indexOf(prefixString) != -1) {
			                                newValues.add(t_textValue);
			                                break;
			                            }
			                        }
			                    }
			                }

			                results.values = newValues;
			                results.count = newValues.size();
			            }

			            return results;
			        }

			        @Override
			        protected void publishResults(CharSequence constraint, FilterResults results) {
			            //noinspection unchecked
			        	m_displayList = (ArrayList<String>)results.values;
			            if (results.count > 0) {
			                notifyDataSetChanged();
			            } else {
			                notifyDataSetInvalidated();
			            }
			        }
				};
			}
			
			return m_filter;
		}
	}
	private EmailAddrAdapter m_emailAdapter;
	private void setAutoCompleteEditText(AutoCompleteTextView _edit){
		if(m_emailAdapter == null){
			m_emailAdapter = new EmailAddrAdapter(this,m_mainApp.m_mailAddressList);
		}
		        
		_edit.setThreshold(1);
		_edit.setAdapter(m_emailAdapter);
	}
	
	public void onClick(View v){
		if(v == m_discardRefView){
			
			GlobalDialog.showYesNoDialog(getString(R.string.mail_compose_delete_ref_mail), this, 
			new GlobalDialog.YesNoListener() {
				
				@Override
				public void click() {
					discardReferenceMail();				
				}
			},null);
			
		}else if(v == m_sendBtn){
			if(!m_modified){
				// without modified confirm
				//
				GlobalDialog.showYesNoDialog(getString(R.string.mail_compose_send_without_modified), this, 
				new GlobalDialog.YesNoListener(){
					
					@Override
					public void click() {
						send();			
					}
				},null);
				
			}else{
				
				if(m_body.getText().length() == 0){
					// body is empty confirm
					//
					GlobalDialog.showYesNoDialog(getString(R.string.mail_compose_empty_body_question), this, 
					new GlobalDialog.YesNoListener(){
						
						@Override
						public void click() {
							send();			
						}
					},null);
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
	
	private void discardReferenceMail(){
		
		if(m_discardRefView.getVisibility() == View.VISIBLE
		&& m_referenceMail != null){
			m_discardRefView.setVisibility(View.GONE);
			m_mainView.removeView(m_referenceMail.m_mainView);
			m_referenceMail = null;
			
			if(m_draftMail != null){
				m_draftMail.setSendRefMailIndex(-1);
			}
		}		
	}
	private void onClose(){
		if(!m_modified){
			finish();
			return;
		}
		
		GlobalDialog.showYesNoDialog(getString(R.string.mail_compose_discard_mail), this, 
		new GlobalDialog.YesNoListener() {
			
			@Override
			public void click() {
				finish();			
			}
		},null);
	}
	
	private void send(){
		
		if(!m_mainApp.m_connectDeamonRun){
			Toast.makeText(this, getString(R.string.mail_compose_connectdeamon_not_run), Toast.LENGTH_SHORT).show();
			return ;
		}
		
		// send to address judge
		//
		String[] t_toAddrList 	= m_to.getText().toString().replaceAll("\n", ",").split(",");
		if(!checkSendToAddr(t_toAddrList,false)){
			m_to.requestFocus();
			Toast.makeText(this, getString(R.string.mail_compose_address_error), Toast.LENGTH_SHORT).show();
			return;
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
		
		fetchMail t_mail; 
		
		if(m_draftMail != null){
			saveDraft();
			t_mail = m_draftMail;
		}else{
			t_mail = storeDB(fetchMail.GROUP_FLAG_SEND_PADDING);
		}		
		
		if(t_mail != null){

			if(!t_mail.GetAttachment().isEmpty() && ConnectDeamon.hasAttachmentSending()){
				Toast.makeText(this, getString(R.string.mail_compose_cant_send_att), Toast.LENGTH_SHORT).show();
				return ;
			}	
		
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
		if(!checkSendToAddr(t_toAddrList,true)
		|| !checkSendToAddr(t_ccAddrList,true)
		|| !checkSendToAddr(t_bccAddrList,true)){
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
		if(m_modified == false){
			return;
		}
		
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
		_mail.setSendRefMailStyle(m_referenceMailStyle);
		_mail.SetXMailer("Yuchs'Box(Android)");
		
		_mail.GetAttachment().clear();
		_mail.GetAttachment().setSize(m_attachmentList.size());
		Collections.copy(_mail.GetAttachment(), m_attachmentList);
				
		if(m_referenceMail != null){
			_mail.SetFromVect(new String[]
            {
				"\"" + getString(R.string.mail_me_address) + "\" <" + m_referenceMail.m_mail.getOwnAccount() + ">"
			});
			
			_mail.setOwnAccount(m_referenceMail.m_mail.getOwnAccount());
			
			
			
		}else if(m_ownAccountSpinner != null){
			
			// compose a new mail and get the own account
			//
			_mail.setOwnAccount(prepareOwnAccount());
			
			_mail.SetFromVect(new String[]
            {
 				"\"" + getString(R.string.mail_me_address) + "\" <" + _mail.getOwnAccount() + ">"
 			});
		}
		
		// generate the Message-ID
		//
		if(_mail.getMessageID() == null || _mail.getMessageID().length() == 0){
			SimpleDateFormat t_format = new SimpleDateFormat("HHmmss");
			_mail.setMessageID("<" + t_format.format(new Date()) + "." + (new Random()).nextInt(1000) + "-yuchs.com-"+_mail.getOwnAccount()+">");
		}
		
		if(m_referenceMail != null){
			_mail.setSendRefMailIndex(m_referenceMail.m_mail.getDbIndex());
			_mail.setInReplyTo(m_referenceMail.m_mail.getMessageID());
			_mail.setReferenceID(m_referenceMail.m_mail.getMessageID() + " " + m_referenceMail.m_mail.getReferenceID());
		}
	}
	
	private String prepareOwnAccount(){
		// compose a new mail and get the own account
		//
		int t_select = (int)m_ownAccountSpinner.getSelectedItemId();
		
		if(t_select < 0 || t_select >= m_mainApp.m_config.m_sendMailAccountList.size()){
			t_select = 0;
		}
		
		m_mainApp.m_config.m_defaultSendMailAccountIndex = t_select;
		
		return m_mainApp.m_config.m_sendMailAccountList.get(
				m_mainApp.m_config.m_defaultSendMailAccountIndex);
	}
	
	private static void setEmailAddr(AutoCompleteTextView _view,List<String> _addrList){
		if(_addrList != null){

			StringBuffer t_to = new StringBuffer();
			for(String addr:_addrList){
				// get rid of comma with blank
				//
				t_to.append(addr.replace(',', ' ')).append(",");
			}
			_view.setText(t_to.toString());	
		}
	}
	
	private static boolean setEmailAddr(AutoCompleteTextView _view,String[] _addr){
		if(_addr != null){
			setEmailAddr(_view, Arrays.asList(_addr));			
			return _addr.length > 0;
		}		
		return false;
	}
		
	private boolean checkSendToAddr(String[] _toAddrList,boolean _acceptEmtpy){
		if(_toAddrList.length == 0){
			return false;
		}
		
		for(String addr:_toAddrList){
			if(addr.length() != 0){
				if(!YuchDroidApp.isValidEmail(addr)
				&& !addr.matches("(.)*<\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*>")){
					return false;
				}
			}else{
				if(!_acceptEmtpy){
					return false;
				}
			}
		}
		
		return true;
	}
	
	private void showCc_Bcc(){
		m_cc.setVisibility(View.VISIBLE);
		m_bcc.setVisibility(View.VISIBLE);
	}
	
//	private void processAds(){
//		AdView t_adsView	= (AdView)findViewById(R.id.mail_compose_ads);
//		if(m_mainApp.m_isOfficalHost){
//			t_adsView.setVisibility(View.GONE);
//		}else{
//			
//			AdRequest t_request = new AdRequest();
//			if(m_referenceMail != null){
//				String t_subject = MailDbAdapter.groupSubject(m_referenceMail.m_mail.GetSubject());
//				t_request.addKeyword(t_subject);
//			}
//			
//			t_adsView(t_request);
//		}
//	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mail_compose_menu,menu);
        return true;
    }
	
	@Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
            case R.id.mail_compose_add_cc_bcc:
            	showCc_Bcc();
                return true;
            case R.id.mail_compose_attachment:
            	openFile();
            	return true;
           
        }

        return super.onMenuItemSelected(featureId, item);
	}
	
	// attachment item click event
	//
	View.OnClickListener m_attachItemclick = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
						
			int t_childNum = m_attachmentParent.getChildCount();
			for(int i = 0;i < t_childNum;i++){
				if(m_attachmentParent.getChildAt(i) == v){
					
					final int t_selected = i; 
					
					GlobalDialog.showYesNoDialog(R.string.mail_compose_remove_attach_prompt, MailComposeActivity.this, 
					new GlobalDialog.YesNoListener() {
						
						@Override
						public void click() {
							m_attachmentParent.removeViewAt(t_selected);
							m_attachmentList.remove(t_selected);								
						}
					},null);
					
					break;
				}
			}
		}
	};
	
	private void refreshAttachment(fetchMail _mail){
		
		m_attachmentParent.removeAllViews();
		m_attachmentList.setSize(_mail.GetAttachment().size());
		Collections.copy(m_attachmentList,_mail.GetAttachment());
		
		for(fetchMail.MailAttachment att:m_attachmentList){
			File t_file = new File(att.m_name);
			if(t_file.exists()){
				addAttachmentItem(att);
			}
		}
	}
	
	private void addAttachmentItem(fetchMail.MailAttachment _att){
		LayoutInflater t_inflater = LayoutInflater.from(this);
		
		ViewGroup attachView = (ViewGroup)t_inflater.inflate(R.layout.mail_open_attachment_item,null);
		TextView filename = (TextView)attachView.findViewById(R.id.mail_open_attachment_item_filename);
		filename.setText("("+YuchDroidApp.GetByteStr(_att.m_size) + ") " + _att.m_name);
			        		
		LinearLayout.LayoutParams t_lp = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		
		m_attachmentParent.addView(attachView,t_lp);
		
		attachView.setOnClickListener(m_attachItemclick);
	}
	
	private void openFile(){
		// To open up a gallery browser
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		startActivityForResult(Intent.createChooser(intent, getString(R.string.mail_open_attach_select_prompt)),1);
	}
	
	static final String[]	MediaDataName = 
	{
		MediaStore.Images.Media.DATA,
		"dat",
		"data"
	};
	
	static final String[] MediaDataType = 
	{
		MediaStore.Images.Media.MIME_TYPE,
		"typ",
		"type",
	};
	
	// To handle when an image is selected from the browser, add the following to your Activity
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) { 

		if (resultCode == RESULT_OK) {

			if (requestCode == 1) {

				try{
					// currImageURI is the global variable I'm using to hold the content:// URI of the image
					Uri contentUri = data.getData();
					
					String path = null;
					String type = null; 
						
					if(contentUri.toString().startsWith("file://")){
						
						path = contentUri.toString();
						type = YuchDroidApp.getFileMIMEType(path);
						
				    }else{
				    	Cursor cursor	= null;
					    int t_tryIdx	= 0;
					    
					    do{
					    	cursor = managedQuery(contentUri, new String[]{ MediaDataName[t_tryIdx],MediaDataType[t_tryIdx]}, null, null, null);
					    	t_tryIdx++;				    	
					    }while(cursor == null && t_tryIdx < MediaDataName.length);
					    
					    if(cursor != null){
					    	t_tryIdx--;				    	

						    cursor.moveToFirst();
						    
						    path = cursor.getString(cursor.getColumnIndexOrThrow(MediaDataName[t_tryIdx]));
						    type = cursor.getString(cursor.getColumnIndexOrThrow(MediaDataType[t_tryIdx]));
					    }
				    }
					
					if(path != null){
						
						File t_file = new File(path);
						if(t_file.exists()){
							
							for(fetchMail.MailAttachment att :m_attachmentList){
								if(att.m_name.equals(path)){
									Toast.makeText(this, getString(R.string.mail_compose_cant_add_att), Toast.LENGTH_SHORT).show();
									return ;
								}
							}
							
							fetchMail.MailAttachment t_att = new fetchMail.MailAttachment();
							t_att.m_name = path;
							t_att.m_size = (int)t_file.length();
							
							if(type == null || type.length() == 0){
								t_att.m_type = YuchDroidApp.getFileMIMEType(t_file);
							}else{
								t_att.m_type = type;
							}
							
							m_attachmentList.add(t_att);
							addAttachmentItem(t_att);
						}
						
					}else{
				    	GlobalDialog.showInfo(R.string.mail_compose_add_att_failed, this);
				    }
				}catch(Exception e){
					m_mainApp.setErrorString("Attachment",e);
					GlobalDialog.showInfo(R.string.mail_compose_add_att_failed, this);
				}
								 
			}
		}
	}


}
