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
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.component.AutoTextEditField;
import net.rim.device.api.ui.component.RadioButtonField;
import net.rim.device.api.ui.component.RadioButtonGroup;

import com.yuchting.yuchberry.client.recvMain;
import com.yuchting.yuchberry.client.screen.SimpleOKCancelDlg;

public class IMStatusAddScreen extends SimpleOKCancelDlg implements FieldChangeListener{
	
	RadioButtonGroup		m_presenceGroup	= new RadioButtonGroup();
	RadioButtonField[]		m_presenceBut = 
	{
		new RadioButtonField(recvMain.sm_local.getString(yblocalResource.IM_STATUS_DEFAULT_AVAIL),m_presenceGroup,true),
		new RadioButtonField(recvMain.sm_local.getString(yblocalResource.IM_STATUS_DEFAULT_AWAY),m_presenceGroup,false),
		new RadioButtonField(recvMain.sm_local.getString(yblocalResource.IM_STATUS_DEFAULT_BUSY),m_presenceGroup,false),
	};
	
	AutoTextEditField	m_status	= new AutoTextEditField(recvMain.sm_local.getString(yblocalResource.IM_STATUS_LABEL_PROMPT),"");
	
	IMStatus			m_modifyStatus = null;
	MainIMScreen		m_mainScreen = null;
	
	public IMStatusAddScreen(MainIMScreen _screen,IMStatus _modifyStatus){
		super(_modifyStatus != null?yblocalResource.IM_MODIFY_STATUS:yblocalResource.IM_ADD_STATUS);
		
		m_mainScreen 		= _screen;
		m_modifyStatus 		= _modifyStatus;
		
		for(int i = 0 ;i < m_presenceBut.length;i++){
			m_middleMgr.add(m_presenceBut[i]);
		}
		
		m_status.setMaxSize(120);
		m_middleMgr.add(m_status);
			
		if(m_modifyStatus != null){
			m_presenceBut[m_modifyStatus.m_presence].setSelected(true);	
			m_status.setText(m_modifyStatus.m_status);
		}
	}
	
	protected boolean onCancel(){
		m_mainScreen.m_statusAddScreen = null;
		return true;
	}

	protected boolean onOK() {
		IMStatus t_status = null;
		
		if(m_modifyStatus != null){
			t_status = m_modifyStatus;
		}else{
			t_status = new IMStatus();
		}
		
		t_status.m_status = m_status.getText();
		
		for(int i = 0 ;i < m_presenceBut.length;i++){
			if(m_presenceBut[i].isSelected()){
				t_status.m_presence = i;
				break;
			}
		}
		
		if(m_modifyStatus != null){
			m_mainScreen.refreshStatusList();
			
			if(IMStatus.sm_currUseStatus == m_modifyStatus){
				m_mainScreen.sendUseStatus(m_modifyStatus);
			}
		}else{
			m_mainScreen.addStatus(t_status);
		}
		
		return true;
	}
}
