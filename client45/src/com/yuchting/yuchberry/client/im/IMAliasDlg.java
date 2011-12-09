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

import local.localResource;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.yuchting.yuchberry.client.recvMain;

public class IMAliasDlg extends PopupScreen implements FieldChangeListener{
	
	RosterItemField m_currRosterField = null;
	EditField		m_name			= null;
	
	ButtonField		m_ok		= new ButtonField(recvMain.sm_local.getString(localResource.IM_STATUS_SCREEN_OK),
											Field.FIELD_HCENTER | ButtonField.CONSUME_CLICK | ButtonField.NEVER_DIRTY);

	ButtonField		m_cancel	= new ButtonField(recvMain.sm_local.getString(localResource.IM_STATUS_SCREEN_CANCEL),
											Field.FIELD_HCENTER | ButtonField.CONSUME_CLICK | ButtonField.NEVER_DIRTY);
	
	MainIMScreen	m_mainScreen = null;		
	
	public IMAliasDlg(MainIMScreen _mainScreen,RosterItemField _roster) {
		super(new VerticalFieldManager());
		
		m_mainScreen		= _mainScreen;
		m_currRosterField	= _roster;
		
		m_name = new EditField(recvMain.sm_local.getString(localResource.IM_ALIAS_ROSTER_DLG_NAME),
							m_currRosterField.m_currRoster.m_roster.getName(),128, EditField.FILTER_DEFAULT);
		m_name.select(true);
		m_name.setCursorPosition(m_name.getTextLength());
		m_name.select(false);
		
		add(m_name);
		
		m_ok.setChangeListener(this);
		m_cancel.setChangeListener(this);
		
		HorizontalFieldManager t_butMgr = new HorizontalFieldManager(Field.FIELD_HCENTER);
		t_butMgr.add(m_ok);
		t_butMgr.add(m_cancel);
		
		add(t_butMgr);
		
	}
	
	public boolean onClose(){
		close();
		return true;
	}
	
	public void close(){
		m_mainScreen.m_aliasDlg = null;
		super.close();
	}

	public void fieldChanged(Field _field,int _context){
		if(_context != FieldChangeListener.PROGRAMMATIC){
			if(_field == m_ok){
				String t_newName = m_name.getText();
				
				if(t_newName.length() == 0){
					return ;
				}
				
				if(!t_newName.equals(m_currRosterField.m_currRoster.m_roster.getName())){
					m_currRosterField.m_currRoster.m_roster.setName(t_newName);
					m_mainScreen.sendRosterAliasName(m_currRosterField.m_currRoster,t_newName);
					
					m_currRosterField.invalidate();
				}
				
				close();
			}else if(_field == m_cancel){
				close();
			}
		}
	}
}
