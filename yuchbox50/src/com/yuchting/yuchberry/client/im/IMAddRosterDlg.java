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
package com.yuchting.yuchberry.client.im;

import local.yblocalResource;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.component.AutoTextEditField;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.component.RadioButtonField;
import net.rim.device.api.ui.component.RadioButtonGroup;

import com.yuchting.yuchberry.client.recvMain;
import com.yuchting.yuchberry.client.screen.SimpleOKCancelDlg;

public class IMAddRosterDlg extends SimpleOKCancelDlg{
	
	RadioButtonGroup m_addTypeGroup = new RadioButtonGroup();
	RadioButtonField[] m_addType =
	{
		new RadioButtonField("GTalk",m_addTypeGroup,true),
		new RadioButtonField("MSN",m_addTypeGroup,false,Field.READONLY),
	};
	
	AutoTextEditField		m_addr	= new AutoTextEditField(recvMain.sm_local.getString(yblocalResource.IM_ADD_ROSTER_DLG_ADDR),
									"",128, EditField.FILTER_DEFAULT);
	
	AutoTextEditField		m_name	= new AutoTextEditField(recvMain.sm_local.getString(yblocalResource.IM_ADD_ROSTER_DLG_NAME),
									"",128, EditField.FILTER_DEFAULT);
		
	MainIMScreen	m_mainScreen = null;
	
	public IMAddRosterDlg(MainIMScreen _screen){
		super(yblocalResource.IM_ADD_ROSTER_DLG_TITLE);
		m_mainScreen = _screen;
		

		m_addType[1].setEditable(false);
		for(int i = 0 ; i < m_addType.length;i++){
			m_middleMgr.add(m_addType[i]);
		}
		
		m_middleMgr.add(m_addr);
		m_middleMgr.add(m_name);	
	}
	
	protected boolean onCancel(){
		m_mainScreen.m_addRosterDlg = null;
		return true;
	}
	protected boolean onOK() {
		int style = 0;
		for(int i = 0 ; i < m_addType.length;i++){
			if(m_addType[i].isSelected()){
				style = i;
				break;
			}
		}
		
		String t_addr = m_addr.getText();
		int t_dot = t_addr.indexOf('.');
		int t_at = t_addr.indexOf('@');
		if(t_at == -1){
			t_at = t_addr.indexOf('＠');
		}
		
		if(style == 0 || style == 1){
			if(t_dot == -1 || t_at == -1 || t_addr.length() <= 5){
				m_mainScreen.m_mainApp.DialogAlert(recvMain.sm_local.getString(yblocalResource.IM_ADD_ROSTER_DLG_ERROR));
				return false;
			}
		}
		
		String t_name = m_name.getText();
		
		m_mainScreen.sendAddRosterMsg(style, t_addr, t_name,recvMain.sm_local.getString(yblocalResource.IM_ADD_ROSTER_DLG_DEFAULT_GROUP));
		
		return true;
	}
}
