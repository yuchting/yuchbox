package com.yuchting.yuchberry.client.screen;

import local.yblocalResource;
import net.rim.device.api.ui.component.RadioButtonField;
import net.rim.device.api.ui.component.RadioButtonGroup;

import com.yuchting.yuchberry.client.recvMain;

public class ChangeMailSenderDlg extends SimpleOKCancelDlg{
	
	recvMain	m_mainApp		= null;
	
	public ChangeMailSenderDlg(recvMain _mainApp){
		super(recvMain.sm_local.getString(yblocalResource.MAIL_SELECT_SENDER_ACC));
		
		m_mainApp = _mainApp;
		
		// account list
		RadioButtonGroup t_group = new RadioButtonGroup();
		for(int i =0 ;i < m_mainApp.m_sendMailAccountList.size();i++){
			RadioButtonField t_acc = new RadioButtonField(m_mainApp.m_sendMailAccountList.elementAt(i).toString(),t_group, i == m_mainApp.m_defaultSendMailAccountIndex);
			m_middleMgr.add(t_acc);
		}
	}
	
	protected boolean onOK() {
		for(int i = 0;i < m_middleMgr.getFieldCount();i++){
			RadioButtonField t_acc = (RadioButtonField)m_middleMgr.getField(i);
			if(t_acc.isSelected()){
				m_mainApp.m_defaultSendMailAccountIndex = i ;
				return true;
			}
		}
		
		return false;
	}

}
