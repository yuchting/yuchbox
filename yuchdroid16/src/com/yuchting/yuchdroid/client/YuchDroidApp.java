package com.yuchting.yuchdroid.client;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import android.app.Application;
import android.content.Intent;

import com.yuchting.yuchdroid.client.mail.MailDbAdapter;

public class YuchDroidApp extends Application {
	
	public Vector<String>	m_errorList = new Vector<String>();
	
	public MailDbAdapter	m_dba		= new MailDbAdapter(this);
	public ConfigInit		m_config 	= new ConfigInit(this);
	
	public boolean			m_connectDeamonRun = false;
	public int				m_connectState	= ConnectDeamon.STATE_DISCONNECT;
	
	@Override
	public void onCreate (){
		super.onCreate();
		
		m_dba.open();
		m_config.WriteReadIni(true);
	}
	
	@Override
	public void onTerminate(){
		super.onTerminate();
		
		m_dba.close();
	}
		
	SimpleDateFormat m_errorTimeformat = new SimpleDateFormat("MM-dd HH:mm:ss");
	public void setErrorString(String _error){		
		String t_out = m_errorTimeformat.format(new Date()) + ": " + _error;
		m_errorList.add(t_out);

		Intent t_intent = new Intent(ConnectDeamon.FILTER_DEBUG_INFO);	
		sendBroadcast(t_intent);
	}
	
	public void setErrorString(String _error,Exception e){
		ByteArrayOutputStream t_stringBuffer = new ByteArrayOutputStream();
		PrintStream	m_printErrorStack	= new PrintStream(t_stringBuffer);
		e.printStackTrace(m_printErrorStack);
		
		setErrorString(_error + (new String(t_stringBuffer.toByteArray())));
	}
	
	public void clearAllErrorString(){
		m_errorList.clear();
		
		Intent t_intent = new Intent(ConnectDeamon.FILTER_DEBUG_INFO);	
		sendBroadcast(t_intent);
	}
	
	public void copyAllErrorString(Vector<String> _to){
		_to.clear();
		
		synchronized (m_errorList) {
			for(String str:m_errorList){
				_to.insertElementAt(str, 0);
			}
		}
	}
	
	public String getErrorString(){
		
		StringBuffer t_ret = new StringBuffer();
		synchronized (m_errorList) {
			for(String str:m_errorList){
				t_ret.append(str);
			}
		}
		
		return t_ret.toString();
	}
}
