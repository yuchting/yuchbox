package com.yuchting.yuchberry.client.screen;

import local.yblocalResource;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.yuchting.yuchberry.client.recvMain;
import com.yuchting.yuchberry.client.im.MainIMScreen;

public abstract class SimpleOKCancelDlg extends PopupScreen implements FieldChangeListener {

	ButtonField		m_ok		= new ButtonField(recvMain.sm_local.getString(yblocalResource.IM_STATUS_SCREEN_OK),
			Field.FIELD_HCENTER | ButtonField.CONSUME_CLICK | ButtonField.NEVER_DIRTY);

	ButtonField		m_cancel	= new ButtonField(recvMain.sm_local.getString(yblocalResource.IM_STATUS_SCREEN_CANCEL),
			Field.FIELD_HCENTER | ButtonField.CONSUME_CLICK | ButtonField.NEVER_DIRTY);
			
	protected LabelField	m_titleLabel		= new LabelField();
	
	protected VerticalFieldManager m_middleMgr = new VerticalFieldManager(Manager.VERTICAL_SCROLL){
		public void sublayout(int width,int height){
			super.sublayout(width, height);
			
			setExtent(getExtent().width, SimpleOKCancelDlg.this.getDlgHeight());
		}
	};
	
	public SimpleOKCancelDlg(int _localResourceId){
		super(new VerticalFieldManager(Manager.NO_VERTICAL_SCROLL));
		onInit_impl(recvMain.sm_local.getString(_localResourceId));
	}
	
	public SimpleOKCancelDlg(String _title){
		super(new VerticalFieldManager(Manager.NO_VERTICAL_SCROLL));
		onInit_impl(_title);		
	}
	
	protected int getDlgHeight(){
		return recvMain.fsm_display_height * 1 / 2;
	}
	
	/**
	 * derive class override ok button clicked
	 * @return true if processed
	 */
	protected abstract boolean onOK();
	
	/**
	 * derive class override cancel button clicked
	 * @return
	 */
	protected boolean onCancel(){return true;}
	
	/**
	 * override escape key to close this dialog
	 */
	public boolean onClose(){
		if(onCancel()){
			close();			
			return true;
		}
		return false;
	}
	
	/**
	 * initialize this dialog for adding many widgets
	 * @param _title
	 */
	private void onInit_impl(String _title){		
		m_titleLabel.setFont(MainIMScreen.fsm_boldFont);
		m_titleLabel.setText(_title);
		
		add(m_titleLabel);
		add(new SeparatorField());
				
		add(m_middleMgr);
		add(new SeparatorField());
		
		HorizontalFieldManager t_btnMgr = new HorizontalFieldManager();
		t_btnMgr.add(m_ok);
		t_btnMgr.add(m_cancel);
		
		m_ok.setChangeListener(this);
		m_cancel.setChangeListener(this);
		
		add(t_btnMgr);
	}	
	
	public void fieldChanged(Field field, int context) {
		if(FieldChangeListener.PROGRAMMATIC != context){
			if(field == m_ok){
				
				if(onOK()){
					onClose();
				}
				
			}else if(field == m_cancel){
				if(onCancel()){
					onClose();
				}
			}
		}
	}

}
