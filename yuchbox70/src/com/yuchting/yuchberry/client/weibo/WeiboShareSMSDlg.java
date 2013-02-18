package com.yuchting.yuchberry.client.weibo;

import java.util.Vector;

import javax.microedition.io.Connector;
import javax.wireless.messaging.MessageConnection;
import javax.wireless.messaging.TextMessage;

import local.yblocalResource;
import net.rim.blackberry.api.invoke.Invoke;
import net.rim.blackberry.api.invoke.MessageArguments;
import net.rim.device.api.system.Clipboard;
import net.rim.device.api.ui.component.CheckboxField;

import com.yuchting.yuchberry.client.recvMain;
import com.yuchting.yuchberry.client.screen.SimpleOKCancelDlg;
import com.yuchting.yuchberry.client.screen.shareYBScreen;

public class WeiboShareSMSDlg extends SimpleOKCancelDlg{
	
	weiboTimeLineScreen		m_mainScreen;
	fetchWeibo				m_shareWeibo;
		
	Vector m_contactList = new Vector();
	
	public shareYBScreen.sendingSMSDlg	m_sendingDlg = null;
	
	public WeiboShareSMSDlg(weiboTimeLineScreen _mainScreen,fetchWeibo _weibo) {
		super(yblocalResource.WEIBO_SHARE_SMS_PROMPT);
		
		m_mainScreen = _mainScreen;
		m_shareWeibo = _weibo;
		
		shareYBScreen.loadContactList(m_contactList,true,false);
		for(int i = 0; i < m_contactList.size();i++){
			shareYBScreen.ShareConcatData t_data = (shareYBScreen.ShareConcatData)m_contactList.elementAt(i);
			CheckboxField t_field = new CheckboxField(t_data.m_name + " " + t_data.m_phoneNumber, false);
			m_middleMgr.add(t_field);
		}		
	}
	
	protected boolean onCancel(){
		synchronized (this) {
			if(m_sendingDlg != null){
				m_sendingDlg.close();
				m_sendingDlg = null;
			}
		}		
		m_mainScreen.m_smsShareDlg = null;
		
		return true;
	}
	
	protected boolean onOK(){
		
		Vector t_list = new Vector();
		int num = m_middleMgr.getFieldCount();		
		
		for(int i = 0 ;i < num;i++){
			CheckboxField t_field = (CheckboxField)m_middleMgr.getField(i);
			if(t_field.getChecked()){
				t_list.addElement(m_contactList.elementAt(i));
			}
		}
		switch(t_list.size()){
		case 0:
			return false;
		case 1:
			shareYBScreen.ShareConcatData t_data = (shareYBScreen.ShareConcatData)t_list.elementAt(0);
			try{
				MessageConnection mc = (MessageConnection)Connector.open("sms://");
				TextMessage m = (TextMessage)mc.newMessage( MessageConnection.TEXT_MESSAGE );
				
				String t_contain = m_shareWeibo.getShareSMSContain(true);
				
				m.setAddress("sms://" + t_data.m_phoneNumber);
				m.setPayloadText(t_contain);
				
				if(!recvMain.fsm_OS_version.startsWith("4")){
					// the TextMessage::setPayloadText method is invalid from OS5
					// so copy to the clipboard and prompt user
					//
					Clipboard.getClipboard().put(t_contain);
					m_mainScreen.m_mainApp.DialogAlert(recvMain.sm_local.getString(yblocalResource.WEIBO_SHARE_SMS_COPY_PROMPT));
				}
										
				Invoke.invokeApplication( Invoke.APP_TYPE_MESSAGES, new MessageArguments(m) );
			}catch(Exception e){
				m_mainScreen.m_mainApp.SetErrorString("SMSDlg",e);
			}
			return true;
		default:
			
			m_sendingDlg = new shareYBScreen.sendingSMSDlg(m_mainScreen.m_mainApp,t_list, 
														m_shareWeibo.getShareEmailContain(""),
														new shareYBScreen.ISendOver() {
				public void over() {
					synchronized (WeiboShareSMSDlg.this) {
						m_sendingDlg = null;
					}
					WeiboShareSMSDlg.this.close();
				}
			});
			
			m_mainScreen.m_mainApp.pushScreen(m_sendingDlg);
			return true;
		}
			
	}
}
