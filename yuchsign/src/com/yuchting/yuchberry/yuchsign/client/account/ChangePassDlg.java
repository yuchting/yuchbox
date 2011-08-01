package com.yuchting.yuchberry.yuchsign.client.account;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.yuchting.yuchberry.yuchsign.client.Yuchsign;


class ChangePassDlg extends DialogBox{
	
	PasswordTextBox		m_origPass	= null;
	PasswordTextBox		m_newPass	= null;
	PasswordTextBox		m_newPass1	= null;
	
	BberPanel		m_mainPane = null;
	
	public ChangePassDlg(final BberPanel _bberPane){
		super(false,false);
		
		m_mainPane = _bberPane;
		
		final VerticalPanel t_mainPane = new VerticalPanel();
		
		final FlexTable t_table 	= new FlexTable();
		int t_index = 0;
		
		m_origPass	= new PasswordTextBox();
		m_newPass	= new PasswordTextBox();
		m_newPass1	= new PasswordTextBox();
		
		BberPanel.AddLabelWidget(t_table, "旧密码：", m_origPass, t_index++);
		BberPanel.AddLabelWidget(t_table, "新密码：", m_newPass, t_index++);
		BberPanel.AddLabelWidget(t_table, "新密码确认：",m_newPass1, t_index++);
		
		t_mainPane.add(t_table);
		
		final HorizontalPanel t_butPane = new HorizontalPanel();
		t_butPane.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		t_butPane.setSpacing(20);
		final Button t_confirmBut = new Button("确定");
		final Button t_cancel = new Button("取消");
		
		t_butPane.add(t_confirmBut);
		t_butPane.add(t_cancel);
		
		t_confirmBut.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				commitChange();
			}
		});
		
		t_cancel.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});
		
		t_mainPane.add(t_butPane);
		
		setWidget(t_mainPane);
		
		setText("更改密码");
		setAnimationEnabled(true);
				
		setPopupPosition(50,100);
	}
	
	public void show(){
		super.show();
		
		m_origPass.setText("");
		m_newPass.setText("");
		m_newPass1.setText("");
	}
	
	private void commitChange(){
		
		String t_origPassString = m_origPass.getText();
		String t_newPassString	= m_newPass.getText();
		String t_newPass1String = m_newPass1.getText();
		
		if(!LoginPanel.IsValidPassword(t_origPassString)){
			Yuchsign.PopupPrompt("旧密码不符合规定，需要不小于6位的数字和字母组成。", m_origPass);
			return;
		}
		
		if(!LoginPanel.IsValidPassword(t_newPassString)){
			Yuchsign.PopupPrompt("新密码不符合规定，需要不小于6位的数字和字母组成。", m_newPass);
			return;
		}
		
		if(!t_newPassString.equals(t_newPass1String)){
			Yuchsign.PopupPrompt("新密码两次不一致。", m_newPass1);
			return;
		}
		
		if(m_mainPane.m_currentBber == null){
			Yuchsign.PopupPrompt("内不错误：_bberPane.m_currentBber == null 请重新登录。", this);
			return;
		}
		
		Yuchsign.PopupWaiting("正在提交更改...", this);
		
		final ChangePassDlg t_changeDlg = this;
		
		try{
			
			m_mainPane.m_mainServer.greetingService.changePassword(m_mainPane.m_currentBber.GetSigninName(), 
					m_mainPane.m_verfiyCode,t_origPassString, t_newPassString, 
			new AsyncCallback<String>() {
				@Override
				public void onSuccess(String result) {
					Yuchsign.HideWaiting();
					
					if(result.startsWith("/verifycode")){
							new YuchVerifyCodeDlg(result, new InputVerfiyCode() {
							
							@Override
							public void InputCode(String code) {
								m_mainPane.m_verfiyCode = code;
								commitChange();
							}
						});
					}else{
						hide();
						
						Yuchsign.PopupPrompt(result, m_mainPane);
					}
				}
				
				@Override
				public void onFailure(Throwable caught) {
					Yuchsign.PopupPrompt(caught.getMessage(), t_changeDlg);
					Yuchsign.HideWaiting();
				}											
			});
			
		}catch(Exception e){
			
			Yuchsign.PopupPrompt(e.getMessage(), this);
			Yuchsign.HideWaiting();
		}
		
	}
	
}