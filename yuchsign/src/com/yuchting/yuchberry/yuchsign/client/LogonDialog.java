package com.yuchting.yuchberry.yuchsign.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.XMLParser;

public final class LogonDialog extends DialogBox{
	
	private Yuchsign m_clientSign = null;
	
	// logon Vertical panel
	final TextBox m_logonName 				= new TextBox();
	final PasswordTextBox m_logonPassword 	= new PasswordTextBox();
	final Button m_logonBut					= new Button("登录");
	
	// signIn Vertical panel
	//
	final TextBox m_signinName 				= new TextBox();
	final PasswordTextBox m_signinPass 		= new PasswordTextBox();
	final PasswordTextBox m_signinPass1 	= new PasswordTextBox();
	final Button m_signinBut 				= new Button("注册");
	
	final CheckBox m_agreeCheckbox			= new CheckBox("我同意");
	
	String m_verfiyCode						= "";
	
	boolean m_isSigninAccount				= false;
	
	public LogonDialog(Yuchsign _clientSign){
		m_clientSign = _clientSign;
		
		final Button t_forgetPass = new Button("忘记密码");
		
		
		final VerticalPanel t_logonPane = new VerticalPanel();
		t_logonPane.setStyleName("logonVPanel");
		
		t_logonPane.add(new HTML("用户名(邮箱地址):"));
		t_logonPane.add(m_logonName);
		t_logonPane.add(new HTML("密码(大于等于6位的字母或数字):"));
		t_logonPane.add(m_logonPassword);
		t_logonPane.add(m_logonBut);
		
		t_logonPane.add(new HTML("<br /><br />"));
		t_logonPane.add(t_forgetPass);
		
		t_forgetPass.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				findPassword(t_logonPane);
			}
		});
			
		final KeyUpHandler t_logonKeyup = new KeyUpHandler() {
			
			@Override
			public void onKeyUp(KeyUpEvent event) {
				if(event.getNativeKeyCode() ==  KeyCodes.KEY_ENTER){
					m_logonBut.click();
				}				
			}
		};
		
		m_logonName.addKeyUpHandler(t_logonKeyup);
		m_logonPassword.addKeyUpHandler(t_logonKeyup);
		
		m_logonBut.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				LogonEvent();
			}
		});
		
		final KeyUpHandler t_signinKeyup = new KeyUpHandler() {
			
			@Override
			public void onKeyUp(KeyUpEvent event) {
				if(event.getNativeKeyCode() ==  KeyCodes.KEY_ENTER){
					m_signinBut.click();
				}
			}
		};
		
		final VerticalPanel t_signinPane = new VerticalPanel();
		t_signinPane.setStyleName("signinVPanel");
		t_signinPane.add(new HTML("用户名(邮箱地址):"));
		t_signinPane.add(m_signinName);
		t_signinPane.add(new HTML("密码:"));
		t_signinPane.add(m_signinPass);
		t_signinPane.add(new HTML("确认密码:"));
		t_signinPane.add(m_signinPass1);
		
		final HorizontalPanel t_agreePane = new HorizontalPanel();
		t_agreePane.add(m_agreeCheckbox);
		t_agreePane.add(new HTML("<a  href=\"http://code.google.com/p/yuchberry/wiki/User_Agreement_YuchberrySign\" target=_blank>用户使用协议</a>"));
		t_signinPane.add(t_agreePane);
		
		t_signinPane.add(new HTML("<br />"));
		t_signinPane.add(m_signinBut);
		
		m_signinName.addKeyUpHandler(t_signinKeyup);
		m_signinPass.addKeyUpHandler(t_signinKeyup);
		m_signinPass1.addKeyUpHandler(t_signinKeyup);
		m_agreeCheckbox.addKeyUpHandler(t_signinKeyup);
		
		m_signinBut.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				SigninEvent();
			}
		});		
		
		// add the two panel to the dialog
		//
		final HorizontalPanel t_horzPane = new HorizontalPanel();
		t_horzPane.addStyleName("dialogHPanel");
		t_horzPane.add(t_logonPane);
		t_horzPane.add(t_signinPane);
		
		setText("登录|注册");
		setPopupPosition(0, 50);
		setAnimationEnabled(true);
		setWidget(t_horzPane);
	}
	
	private void findPassword(final Widget _pane){
		try{
			if(!IsValidEmail(m_logonName.getText()) ){
				Yuchsign.PopupPrompt("请输入正确的邮件地址作为用户名", m_logonName);
				return;
			}
			
			Yuchsign.PopupWaiting("正在提交申请", _pane);
			
			m_clientSign.greetingService.findPassword(m_logonName.getText(),m_verfiyCode,new AsyncCallback<String>() {
				
				public void onSuccess(String result) {
					if(result.startsWith("/verifycode")){
						
						Yuchsign.HideWaiting();
						
						new YuchVerifyCodeDlg(result, new InputVerfiyCode() {
							
							@Override
							public void InputCode(String code) {
								m_verfiyCode = code;
								findPassword(_pane);
							}
						});
					}else{
						Yuchsign.HideWaiting();
						Yuchsign.PopupPrompt(result,_pane);	
					}								
				}
				
				public void onFailure(Throwable caught) {
					Yuchsign.HideWaiting();
					Yuchsign.PopupPrompt("失败：" + caught.getMessage(),_pane);							
				}
			});
			
		}catch(Exception e){
			Yuchsign.HideWaiting();
			Yuchsign.PopupPrompt("错误："+ e.getMessage(), _pane);
		}
	}
	
	private void CallOnSuccess(String _result){
		
		try{
			Document t_doc = XMLParser.parse(_result);
			Element t_elem = t_doc.getDocumentElement();
			if(t_elem.getTagName().equals("Error")){
				Yuchsign.PopupPrompt(t_elem.getFirstChild().toString(),this);
			}else{
				
				m_signinName.setText("");
				m_signinPass.setText("");
				m_signinPass1.setText("");
				
				m_logonName.setText("");
				m_logonPassword.setText("");
				m_agreeCheckbox.setValue(false);
				
				m_clientSign.ShowYuchbberPanel(_result,m_isSigninAccount);
				
				m_isSigninAccount = false;
				
				hide();
			}			
			
		}catch(Exception e){
			Yuchsign.PopupPrompt(e.getMessage(),this);
		}
		
		Yuchsign.HideWaiting();
	}
	
	private void LogonEvent(){
		final String t_name = m_logonName.getText().toLowerCase();
		final String t_pass = m_logonPassword.getText();
		
		if(!IsValidEmail(t_name)){
			Yuchsign.PopupPrompt("用户名不是合法的Email地址！",m_logonName);
			return;
		}
		
		if(!IsValidPassword(t_pass)){
			Yuchsign.PopupPrompt("密码非法(大于等于6位的字母或数字)！",m_logonPassword);
			return;
		}
		
		try{
			
			Yuchsign.PopupWaiting("正在登录...",this);
			
			m_isSigninAccount = false;
			
			m_clientSign.greetingService.logonServer(t_name,t_pass,new AsyncCallback<String>(){
				public void onFailure(Throwable caught) {
					Yuchsign.PopupPrompt(caught.getMessage(),null);
					Yuchsign.HideWaiting();
				}

				public void onSuccess(String result) {
					CallOnSuccess(result);
				}
			});

		}catch(Exception e){
			Yuchsign.PopupPrompt(e.getMessage(),this);
			Yuchsign.HideWaiting();
		}
	}
	
	private void SigninEvent(){
		final String t_name = m_signinName.getText().toLowerCase();
		final String t_pass = m_signinPass.getText();
		final String t_pass1 = m_signinPass1.getText();
		
		if(!IsValidEmail(t_name)){
			Yuchsign.PopupPrompt("用户名不是合法的Email地址！",m_signinName);
			return;
		}
		
		if(!t_pass.equals(t_pass1)){
			Yuchsign.PopupPrompt("两次密码不一样！",m_signinPass1);
			return;
		}
		
		if(!IsValidPassword(t_pass)){
			Yuchsign.PopupPrompt("密码非法(大于等于6位的字母或数字)！",m_signinPass);
			return;
		}
		
		if(!m_agreeCheckbox.getValue()){
			Yuchsign.PopupPrompt("需要同意用户使用协议",m_agreeCheckbox);
			return ;
		}
		
		m_logonName.setText("");
		m_logonPassword.setText("");
		
		try{					
			Yuchsign.PopupWaiting("正在提交注册...",this);
			
			m_isSigninAccount = true;
			
			m_clientSign.greetingService.signinAccount(t_name,t_pass,m_verfiyCode,new AsyncCallback<String>(){
				public void onFailure(Throwable caught) {
					Yuchsign.PopupPrompt(caught.getMessage(),null);
					Yuchsign.HideWaiting();
				}

				public void onSuccess(String result) {
					if(result.startsWith("/verifycode")){
						
						Yuchsign.HideWaiting();
						
						new YuchVerifyCodeDlg(result, new InputVerfiyCode() {
							
							@Override
							public void InputCode(String code) {
								m_verfiyCode = code;
								SigninEvent();
							}
						});
					}else{
						CallOnSuccess(result);
					}					
				}
			});

		}catch(Exception e){
			Yuchsign.PopupPrompt(e.getMessage(),this);
			Yuchsign.HideWaiting();
		}
	}
	
	public native static boolean IsValidEmail(String name)/*-{
	    var regex = new RegExp("^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$");
	    return regex.test(name);
	}-*/;
	
	public static boolean IsValidPassword(String _password){
		
		if(_password.length() < 6){
			return false;
		}
		
		int t_index = 0;
		while(t_index < _password.length()){
			if(Character.isLetterOrDigit(_password.charAt(t_index)) == false){
				return false;
			}
			t_index++;
		}
		
		return true;
	}
	
}
