package com.yuchting.yuchdroid.client;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

import com.yuchting.yuchdroid.client.mail.MailComposeActivity;
import com.yuchting.yuchdroid.client.mail.MailListView;
import com.yuchting.yuchdroid.client.mail.fetchMail;

public class HomeActivity extends Activity {
	
	public final static int	STATUS_BAR_MAIL		= 0;
	public final static int	STATUS_BAR_WEIBO	= 1;
	public final static int	STATUS_BAR_IM		= 2;
	
	private YuchDroidApp	m_mainApp;
	private MailListView	m_mailListView;
	
	private RadioButton[]	m_statusBarBut = {null,null,null};
	private RadioGroup		m_statusBarGroup;
				
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
                
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.home);
                
        m_mainApp = (YuchDroidApp)getApplicationContext();
        
        // initialize home status bar 
        //
        initHomeStatusBar();
                        
        // initialize module view
        //
        initModuleView();
        
        m_mainApp.StopMailNotification();
    }
	
	private void initHomeStatusBar(){
		
//		m_statusBarBut[S TATUS_BAR_MAIL] 	= (RadioButton)findViewById(R.id.home_status_bar_mail);
//		m_statusBarBut[STATUS_BAR_WEIBO] 	= (RadioButton)findViewById(R.id.home_status_bar_weibo);
//		m_statusBarBut[STATUS_BAR_IM] 		= (RadioButton)findViewById(R.id.home_status_bar_im);
//		
//		m_statusBarGroup 					= (RadioGroup)findViewById(R.id.home_status_bar);
//		
//		m_statusBarGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//			
//			@Override
//			public void onCheckedChanged(RadioGroup paramRadioGroup, int paramInt) {
//				setMainListView(paramInt);
//			}
//		});
	}
		
	private void initModuleView(){
		m_mailListView = new MailListView(this,m_mainApp);
		
		// set the status bar select
		//
		int status = 0;
		Intent t_intent = getIntent();		
		if(t_intent != null && t_intent.getExtras() != null){
			t_intent.getExtras().getInt("status",0);
		}
	
		((RelativeLayout)findViewById(R.id.home_activity)).addView(m_mailListView,0);
		m_mailListView.setVisibility(View.GONE);
		
		setMainListView(status);
	}
	
	private void setMainListView(int _status){
		switch (_status) {
		case STATUS_BAR_MAIL:			
			m_mailListView.setVisibility(View.VISIBLE);
			break;
		case STATUS_BAR_WEIBO:
			break;
		case STATUS_BAR_IM:
			break;
		default:
			break;
		}
	}
		
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mail_list_menu,menu);
        return true;
    }
	
	@Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
            case R.id.mail_list_compose_new_mail:
            	Intent in = new Intent(this,MailComposeActivity.class);
            	in.putExtra(MailComposeActivity.COMPOSE_MAIL_STYLE, fetchMail.NOTHING_STYLE);
            	m_mainApp.m_composeRefMail = null;
            	
            	startActivity(in);
                return true;
           
        }

        return super.onMenuItemSelected(featureId, item);
	}
}
