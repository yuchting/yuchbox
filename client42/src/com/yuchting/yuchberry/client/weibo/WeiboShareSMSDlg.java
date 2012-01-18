package com.yuchting.yuchberry.client.weibo;

import java.util.Vector;

import javax.microedition.io.Connector;
import javax.wireless.messaging.MessageConnection;
import javax.wireless.messaging.TextMessage;

import local.yblocalResource;
import net.rim.blackberry.api.invoke.Invoke;
import net.rim.blackberry.api.invoke.MessageArguments;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.CheckboxField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.yuchting.yuchberry.client.recvMain;
import com.yuchting.yuchberry.client.screen.shareYBScreen;
import com.yuchting.yuchberry.client.screen.shareYBScreen.ISendOver;
import com.yuchting.yuchberry.client.screen.shareYBScreen.sendingSMSDlg;

public class WeiboShareSMSDlg extends PopupScreen implements FieldChangeListener{
	
	ButtonField		m_ok		= new ButtonField(recvMain.sm_local.getString(yblocalResource.IM_STATUS_SCREEN_OK),
			Field.FIELD_HCENTER | ButtonField.CONSUME_CLICK | ButtonField.NEVER_DIRTY);

	ButtonField		m_cancel	= new ButtonField(recvMain.sm_local.getString(yblocalResource.IM_STATUS_SCREEN_CANCEL),
			Field.FIELD_HCENTER | ButtonField.CONSUME_CLICK | ButtonField.NEVER_DIRTY);
	
	weiboTimeLineScreen		m_mainScreen;
	fetchWeibo				m_shareWeibo;
		
	Vector m_contactList = new Vector();
	VerticalFieldManager m_contactMgr = new VerticalFieldManager(){
		public void sublayout(int width,int height){
			super.sublayout(width, height);
			
			setExtent(getExtent().width, recvMain.fsm_display_height * 1 / 2);
		}
	};
	
	public shareYBScreen.sendingSMSDlg	m_sendingDlg = null;
	
	public WeiboShareSMSDlg(weiboTimeLineScreen _mainScreen,fetchWeibo _weibo) {
		super(new VerticalFieldManager(Manager.NO_VERTICAL_SCROLL));
		
		m_mainScreen = _mainScreen;
		m_shareWeibo = _weibo;
		
		add(new LabelField(recvMain.sm_local.getString(yblocalResource.WEIBO_SHARE_SMS_PROMPT)));
		add(new SeparatorField());

		shareYBScreen.loadContactList(m_contactList,true,false);
		
		for(int i = 0; i < m_contactList.size();i++){
			shareYBScreen.ShareConcatData t_data = (shareYBScreen.ShareConcatData)m_contactList.elementAt(i);
			CheckboxField t_field = new CheckboxField(t_data.m_name + " " + t_data.m_phoneNumber, false);
			m_contactMgr.add(t_field);
		}
		
		add(m_contactMgr);
		add(new SeparatorField());
		
		HorizontalFieldManager t_btnMgr = new HorizontalFieldManager();
		t_btnMgr.add(m_ok);
		t_btnMgr.add(m_cancel);
		
		m_ok.setChangeListener(this);
		m_cancel.setChangeListener(this);
		
		add(t_btnMgr);
		
	}
	
	public void close(){
		super.close();
		
		synchronized (this) {
			if(m_sendingDlg != null){
				m_sendingDlg.close();
				m_sendingDlg = null;
			}
		}		
		m_mainScreen.m_smsShareDlg = null;
	}
	
	public boolean onClose(){
		close();
		return true;
	}

	public void fieldChanged(Field field, int context) {
		if(field == m_ok){
			
			Vector t_list = new Vector();
			int num = m_contactMgr.getFieldCount();		
			
			for(int i = 0 ;i < num;i++){
				CheckboxField t_field = (CheckboxField)m_contactMgr.getField(i);
				if(t_field.getChecked()){
					t_list.addElement(m_contactList.elementAt(i));
				}
			}
			switch(t_list.size()){
			case 0:
				return;
			case 1:
				shareYBScreen.ShareConcatData t_data = (shareYBScreen.ShareConcatData)t_list.elementAt(0);
				try{
					MessageConnection mc = (MessageConnection)Connector.open("sms://");
					TextMessage m = (TextMessage)mc.newMessage( MessageConnection.TEXT_MESSAGE );
					
					m.setAddress("sms://" + t_data.m_phoneNumber);
					m.setPayloadText(m_shareWeibo.getShareEmailContain(""));
											
					Invoke.invokeApplication( Invoke.APP_TYPE_MESSAGES, new MessageArguments(m) );
				}catch(Exception e){
					m_mainScreen.m_mainApp.SetErrorString("SMSDlg",e);
				}
				break;
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
				
			}
			
		}else if(field == m_cancel){
			onClose();
		}		
	}

}
