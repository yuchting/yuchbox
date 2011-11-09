package com.yuchting.yuchdroid.client.mail;

import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
		
		m_discardBtn = (Button)findViewById(R.id.mail_compose_discard_btn);
		m_discardBtn.setOnClickListener(this);	
						
		prepareData();
		
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
		m_to.addTextChangedListener(t_watcher);
		m_bcc.addTextChangedListener(t_watcher);
		m_cc.addTextChangedListener(t_watcher);
		m_body.addTextChangedListener(t_watcher);
		m_subject.addTextChangedListener(t_watcher);

		setAutoCompleteEditText(m_cc);
		setAutoCompleteEditText(m_bcc);
		setAutoCompleteEditText(m_to);
		
		m_modified = false;
		m_saveBtn.setEnabled(false);
		
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
			if(t_draft && m_draftMail != null){
				// draft
				//
				t_sub = m_draftMail.GetSubject();
				m_body.setText(m_draftMail.GetContain());
				// draft
				//
				t_toVect = m_draftMail.GetSendToVect();
			}else{
				
				
				if(m_referenceMailStyle == fetchMail.REPLY_STYLE
				|| m_referenceMailStyle == fetchMail.REPLY_ALL_STYLE){
					// reply
					//
					if(m_referenceMail != null){
						t_sub = MailDbAdapter.getReplySubject(m_referenceMail.m_mail.GetSubject(), getString(R.string.mail_compose_reply_prefix));
					}else{
						t_sub =getString(R.string.mail_compose_reply_prefix);
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
				
				
				if(m_referenceMail != null){

					if(m_referenceMail.m_mail.isOwnSendMail()){
						t_toVect = m_referenceMail.m_mail.GetSendToVect();
					}else{
						
						if(m_referenceMailStyle != fetchMail.FORWORD_STYLE){
							// if NOT forward style
							//
							t_toVect = m_referenceMail.m_mail.GetReplyToVect().isEmpty()?
									m_referenceMail.m_mail.GetFromVect():
									m_referenceMail.m_mail.GetReplyToVect();
						}
						
					}
				}
			}
			
			if(t_toVect != null){
				
				StringBuffer t_to = new StringBuffer();
				for(String addr:t_toVect){
					// get rid of comma with blank
					//
					t_to.append(addr.replace(',', ' ')).append(",");
				}
				m_to.setText(t_to.toString());
				m_body.requestFocus();
			}else{
				m_to.requestFocus();
			}				
			
			setTitle(t_sub);
			m_subject.setText(t_sub);
			
		}else{
			
			// compose a new mail
			//
			m_discardRefView.setVisibility(View.GONE);
			m_to.requestFocus();
			
			Intent in = getIntent();
			if(in.getAction() != null && in.getAction().equals(Intent.ACTION_SEND)){
				
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
			
			loadSendMailAccountList();
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
			
			// set the listener
			//
			m_ownAccountSpinner.setOnItemSelectedListener(new OnItemSelectedListener(){
				public void onItemSelected(AdapterView<?> parent,View view, int pos, long id) {
					m_modified = true;
					m_saveBtn.setEnabled(true);
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
			                
			                final ArrayList<String> newValues = new ArrayList<String>(m_addrList.length);

			                String t_textValue;
			                String t_lowTextValue;
			                
			                for (int i = 0; i < m_addrList.length; i++) {

			                	t_textValue		= m_addrList[i];
			                	t_lowTextValue	= m_addrList[i].toLowerCase();

			                    // First match against the whole, non-splitted value
			                    if (t_lowTextValue.startsWith(prefixString)) {
			                        newValues.add(t_textValue);
			                    }else{			                    	
			                    	final String[] words = t_lowTextValue.split(" ");
			                        final int wordCount = words.length;

			                        for (int k = 0; k < wordCount; k++) {
			                            if (words[k].startsWith(prefixString)) {
			                                newValues.add(t_textValue);
			                                break;
			                            }
			                        }
			                    }
			                }
			                
			                for (int i = 0; i < m_addrList.length; i++) {

			                	t_textValue		= m_addrList[i];
			                	t_lowTextValue	= m_addrList[i].toLowerCase();

			                    // First match against the whole, non-splitted value
			                    if (t_lowTextValue.indexOf(prefixString) != -1) {
			                        newValues.add(t_textValue);
			                    }else{			                    	
			                    	final String[] words = t_lowTextValue.split(" ");
			                        final int wordCount = words.length;

			                        for (int k = 0; k < wordCount; k++) {
			                            if (words[k].indexOf(prefixString) != -1) {
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
			new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if(which == DialogInterface.BUTTON_POSITIVE){
						discardReferenceMail();
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
	
	private static boolean setEmailAddr(AutoCompleteTextView _view,String[] _addr){
		if(_addr != null){
			StringBuffer t_toString = new StringBuffer();
			for(String to:_addr){
				t_toString.append(to).append(",");
			}
			_view.setText(t_toString.toString());
			
			return _addr.length > 0;
		}		
		return false;
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
	
	private void showCc_Bcc(){
		m_cc.setVisibility(View.VISIBLE);
		m_bcc.setVisibility(View.VISIBLE);
	}
	
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
            	return true;
           
        }

        return super.onMenuItemSelected(featureId, item);
	}
}
