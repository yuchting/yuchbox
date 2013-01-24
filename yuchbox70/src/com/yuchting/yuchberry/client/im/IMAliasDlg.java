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
import net.rim.device.api.ui.component.EditField;

import com.yuchting.yuchberry.client.screen.SimpleOKCancelDlg;

public class IMAliasDlg extends SimpleOKCancelDlg implements FieldChangeListener{
	
	RosterItemField m_currRosterField = null;
	AutoTextEditField		m_name			= null;

	MainIMScreen	m_mainScreen = null;		
	
	public IMAliasDlg(MainIMScreen _mainScreen,RosterItemField _roster) {
		super(yblocalResource.IM_ALIAS_ROSTER_DLG_NAME);
		
		m_mainScreen		= _mainScreen;
		m_currRosterField	= _roster;
		
		m_name = new AutoTextEditField("",m_currRosterField.m_currRoster.m_roster.getName(),128, EditField.FILTER_DEFAULT);
		m_name.select(true);
		m_name.setCursorPosition(m_name.getTextLength());
		m_name.select(false);
		
		m_middleMgr.add(m_name);
	}

	protected boolean onCancel(){
		m_mainScreen.m_aliasDlg = null;
		return true;
	}

	protected boolean onOK() {

		String t_newName = m_name.getText();
		
		if(t_newName.length() == 0){
			return false;
		}
		
		if(!t_newName.equals(m_currRosterField.m_currRoster.m_roster.getName())){
			m_currRosterField.m_currRoster.m_roster.setName(t_newName);
			m_mainScreen.sendRosterAliasName(m_currRosterField.m_currRoster,t_newName);
			
			m_currRosterField.invalidate();
		}
		
		return true;
	}
}
