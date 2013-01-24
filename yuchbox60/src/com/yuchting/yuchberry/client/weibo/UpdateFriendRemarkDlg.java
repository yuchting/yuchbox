package com.yuchting.yuchberry.client.weibo;

import java.io.ByteArrayOutputStream;

import local.yblocalResource;
import net.rim.device.api.ui.component.EditField;

import com.yuchting.yuchberry.client.msg_head;
import com.yuchting.yuchberry.client.recvMain;
import com.yuchting.yuchberry.client.sendReceive;
import com.yuchting.yuchberry.client.screen.SimpleOKCancelDlg;

public class UpdateFriendRemarkDlg extends SimpleOKCancelDlg{

	EditField		m_remark			= null;
	
	fetchWeibo		m_selectWeibo	= null;
	
	weiboTimeLineScreen 	m_weiboScreen = null;
	
	public UpdateFriendRemarkDlg(weiboTimeLineScreen _weiboScreen,fetchWeibo _selectWeibo) {
		super(recvMain.sprintf(yblocalResource.WEIBO_UPDATE_FRIEND_REMARK_TITLE,new String[]{_selectWeibo.GetUserScreenName()}));
		
		m_weiboScreen	= _weiboScreen;
		m_selectWeibo	= _selectWeibo;
				
		m_remark = new EditField("",_selectWeibo.GetUserScreenName(),20, EditField.FILTER_DEFAULT);
		m_remark.select(true);
		m_remark.setCursorPosition(m_remark.getTextLength());
		m_remark.select(false);
		
		m_middleMgr.add(m_remark);
	}

	protected boolean onOK() {
		if(m_remark.getText().length() == 0){
			return false;
		}
		
		try{
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			os.write(msg_head.msgWeiboRemark);
			os.write(m_selectWeibo.GetWeiboStyle());
			sendReceive.WriteString(os,Long.toString(m_selectWeibo.GetUserId()));
			sendReceive.WriteString(os,m_remark.getText());
			
			m_weiboScreen.m_mainApp.m_connectDeamon.addSendingData(msg_head.msgWeiboRemark,os.toByteArray(),true);
			
		}catch(Exception e){
			m_weiboScreen.m_mainApp.SetErrorString("UFRD_OK",e);
		}
		
		return true;
	}



}
