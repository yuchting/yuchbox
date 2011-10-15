package com.yuchting.yuchberry.client.im;

import local.localResource;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.RadioButtonField;
import net.rim.device.api.ui.component.RadioButtonGroup;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.yuchting.yuchberry.client.recvMain;

public class IMAddRosterDlg extends PopupScreen implements FieldChangeListener{
	
	RadioButtonGroup m_addTypeGroup = new RadioButtonGroup();
	RadioButtonField[] m_addType =
	{
		new RadioButtonField("GTalk",m_addTypeGroup,true),
		new RadioButtonField("MSN",m_addTypeGroup,false,Field.READONLY),
	};
	
	EditField		m_addr	= new EditField(recvMain.sm_local.getString(localResource.IM_ADD_ROSTER_DLG_ADDR),
									"",128, EditField.FILTER_DEFAULT);
	
	EditField		m_name	= new EditField(recvMain.sm_local.getString(localResource.IM_ADD_ROSTER_DLG_NAME),
									"",128, EditField.FILTER_DEFAULT);
	
	ButtonField		m_add = new ButtonField(recvMain.sm_local.getString(localResource.IM_STATUS_SCREEN_OK),
									Field.FIELD_HCENTER | ButtonField.CONSUME_CLICK | ButtonField.NEVER_DIRTY);
	
	ButtonField		m_cancel = new ButtonField(recvMain.sm_local.getString(localResource.IM_STATUS_SCREEN_CANCEL),
									Field.FIELD_HCENTER | ButtonField.CONSUME_CLICK | ButtonField.NEVER_DIRTY);
	
	MainIMScreen	m_mainScreen = null;
	
	public IMAddRosterDlg(MainIMScreen _screen){
		super(new VerticalFieldManager(Manager.VERTICAL_SCROLL));
		m_mainScreen = _screen;
		
		LabelField t_title = new LabelField(recvMain.sm_local.getString(localResource.IM_ADD_ROSTER_DLG_TITLE));
		t_title.setFont(MainIMScreen.fsm_boldFont);
		
		add(t_title);
		
		m_addType[1].setEditable(false);
		for(int i = 0 ; i < m_addType.length;i++){
			add(m_addType[i]);
		}
		
		add(m_addr);
		add(m_name);
		
		add(new SeparatorField());
		
		HorizontalFieldManager t_butMgr = new HorizontalFieldManager(Field.FIELD_HCENTER);
		t_butMgr.add(m_add);
		t_butMgr.add(m_cancel);
		
		m_add.setChangeListener(this);
		m_cancel.setChangeListener(this);
		
		add(t_butMgr);	
	}
	
	public void fieldChanged(Field _field,int _context){
		if(_context != FieldChangeListener.PROGRAMMATIC){
			if(_field == m_add){
				
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
						m_mainScreen.m_mainApp.DialogAlert(recvMain.sm_local.getString(localResource.IM_ADD_ROSTER_DLG_ERROR));
						return;
					}
				}
				
				String t_name = m_name.getText();
				
				m_mainScreen.sendAddRosterMsg(style, t_addr, t_name, 
						recvMain.sm_local.getString(localResource.IM_ADD_ROSTER_DLG_DEFAULT_GROUP));
				
				close();
				
			}else if(_field == m_cancel){
				close();
			}
		}
	}
	
	public boolean onClose(){
		close();
		return true;
	}
	
	public void close(){
		m_mainScreen.m_addRosterDlg = null;
		super.close();
	}
}
