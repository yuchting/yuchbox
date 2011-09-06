package com.yuchting.yuchberry.client.im;

import javax.microedition.lcdui.TextField;

import local.localResource;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.component.AutoTextEditField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.RadioButtonField;
import net.rim.device.api.ui.component.RadioButtonGroup;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.yuchting.yuchberry.client.recvMain;

public class IMStatusAddScreen extends PopupScreen implements FieldChangeListener{
	
	
	RadioButtonGroup		m_presenceGroup	= new RadioButtonGroup();
	RadioButtonField[]		m_presenceBut = 
	{
		new RadioButtonField(recvMain.sm_local.getString(localResource.IM_STATUS_DEFAULT_AVAIL),m_presenceGroup,true),
		new RadioButtonField(recvMain.sm_local.getString(localResource.IM_STATUS_DEFAULT_AWAY),m_presenceGroup,false),
		new RadioButtonField(recvMain.sm_local.getString(localResource.IM_STATUS_DEFAULT_BUSY),m_presenceGroup,false),
	};
	AutoTextEditField	m_status	= new AutoTextEditField(recvMain.sm_local.getString(localResource.IM_STATUS_LABEL_PROMPT),"");
	
	ButtonField			m_ok		= new ButtonField(recvMain.sm_local.getString(localResource.IM_STATUS_SCREEN_OK),
										Field.FIELD_HCENTER | ButtonField.CONSUME_CLICK | ButtonField.NEVER_DIRTY);
	
	ButtonField			m_cancel	= new ButtonField(recvMain.sm_local.getString(localResource.IM_STATUS_SCREEN_CANCEL),
										Field.FIELD_HCENTER | ButtonField.CONSUME_CLICK | ButtonField.NEVER_DIRTY);
	
	IMStatus			m_modifyStatus = null;
	MainIMScreen		m_mainScreen = null;
	
	public IMStatusAddScreen(MainIMScreen _screen,IMStatus _modifyStatus){
		super(new VerticalFieldManager(Manager.VERTICAL_SCROLL));
		m_mainScreen 		= _screen;
		m_modifyStatus 		= _modifyStatus;
		
		LabelField t_title = new LabelField();
		t_title.setFont(MainIMScreen.sm_boldFont);
		
		add(t_title);
		add(new SeparatorField());
		
		for(int i = 0 ;i < m_presenceBut.length;i++){
			add(m_presenceBut[i]);
		}
		
		m_status.setMaxSize(120);
		add(m_status);
			
		if(m_modifyStatus != null){
			t_title.setText(recvMain.sm_local.getString(localResource.IM_MODIFY_STATUS));
			m_presenceBut[m_modifyStatus.m_presence].setSelected(true);
			
			m_status.setText(m_modifyStatus.m_status);
		}else{
			t_title.setText(recvMain.sm_local.getString(localResource.IM_ADD_STATUS));
		}
		
		add(new SeparatorField());
		
		
		// control button
		//
		m_ok.setChangeListener(this);
		m_cancel.setChangeListener(this);
		
		HorizontalFieldManager t_buttonHorzMgr = new HorizontalFieldManager();
		t_buttonHorzMgr.add(m_ok);
		t_buttonHorzMgr.add(m_cancel);
		
		add(t_buttonHorzMgr);
	}
	
	public void fieldChanged(Field _field,int _context){
		if(_context != FieldChangeListener.PROGRAMMATIC){
			if(_field == m_ok){
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
				
				close();
				
			}else if(_field == m_cancel){
				close();
			}
		}
	}
	
	public void close(){
		super.close();
		m_mainScreen.m_statusAddScreen = null;
	}
}
