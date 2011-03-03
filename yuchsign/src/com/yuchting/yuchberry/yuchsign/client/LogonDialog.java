package com.yuchting.yuchberry.yuchsign.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.XMLParser;

public final class LogonDialog extends DialogBox{
	
	private Yuchsign m_clientSign = null;
	
	// logon Vertical panel
	final TextBox m_logonName 				= new TextBox();
	final PasswordTextBox m_logonPassword 	= new PasswordTextBox();
	final Button m_logonBut					= new Button("登录");
	final Label m_errorLabel 				= new Label();
	
	// signIn Vertical panel
	//
	final TextBox m_signinName 				= new TextBox();
	final PasswordTextBox m_signinPass 		= new PasswordTextBox();
	final PasswordTextBox m_signinPass1 	= new PasswordTextBox();
	final Button m_signinBut 				= new Button("注册");
	
	private static class waitingLabel extends PopupPanel{

		final Label		m_label = new Label();
		
	    public waitingLabel() {
			// PopupPanel's constructor takes 'auto-hide' as its boolean parameter.
			// If this is set, the panel closes itself automatically when the user
			// clicks outside of it.
			super();
			
			// PopupPanel is a SimplePanel, so you have to set it's widget property to
			// whatever you want its contents to be.
			setWidget(m_label);
			  
			// waiting label
			setStyleName("waitingLabel");
			  
			// popup position
			setPopupPosition(100, 60);
	      
	    }
	    
	    public void ShowText(String _text){
	    	setModal(true);
	    	show();	    	
	    	m_label.setText(_text);
	    }
	    
	    public void Hide(){
	    	setModal(false);
	    	hide();	    	
	    }
	}
	  
	// waiting label
	final waitingLabel			m_waitingLable		= new waitingLabel();	

	public LogonDialog(Yuchsign _clientSign){
		m_clientSign = _clientSign;
		
		m_logonBut.setStyleName("defaultButton");		
		m_errorLabel.setStyleName("serverResponseLabelError");
		
		final VerticalPanel t_logonPane = new VerticalPanel();
		t_logonPane.setStyleName("logonVPanel");
		
		t_logonPane.add(new HTML("用户名(邮箱地址):"));
		t_logonPane.add(m_logonName);
		t_logonPane.add(new HTML("<br />密码(多于6位的字母或数字):"));
		t_logonPane.add(m_logonPassword);
		t_logonPane.add(new HTML("<br />"));
		t_logonPane.add(m_logonBut);
		t_logonPane.add(new HTML("<br />"));
		t_logonPane.add(m_errorLabel);
		
		final AsyncCallback<String> t_callback = new AsyncCallback<String>(){
			public void onFailure(Throwable caught) {
				m_errorLabel.setText(caught.getMessage());
				m_waitingLable.Hide();
			}

			public void onSuccess(String result) {
				CallOnSuccess(result);
			}
		};
		
		m_logonBut.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				final String t_name = m_logonName.getText();
				final String t_pass = m_logonPassword.getText();
				
				if(!IsValidEmail(t_name)){
					m_errorLabel.setText("用户名不是合法的Email地址！");
					return;
				}
				
				if(!IsValidPassword(t_pass)){
					m_errorLabel.setText("密码非法！");
					return;
				}
				
				try{
					
					m_waitingLable.ShowText("正在登录...");				
					m_clientSign.greetingService.logonServer(t_name,t_pass,t_callback);
	
				}catch(Exception e){
					m_errorLabel.setText(e.getMessage());
					m_waitingLable.Hide();
				}
			}
		});
		
		m_signinBut.setStyleName("defaultButton");
		
		final VerticalPanel t_signinPane = new VerticalPanel();
		t_signinPane.setStyleName("signinVPanel");
		t_signinPane.add(new HTML("用户名(邮箱地址):"));
		t_signinPane.add(m_signinName);
		t_signinPane.add(new HTML("<br />密码:"));
		t_signinPane.add(m_signinPass);
		t_signinPane.add(new HTML("<br />确认密码:"));
		t_signinPane.add(m_signinPass1);
		t_signinPane.add(m_signinBut);
		
		m_signinBut.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				final String t_name = m_signinName.getText();
				final String t_pass = m_signinPass.getText();
				final String t_pass1 = m_signinPass1.getText();
				
				if(!IsValidEmail(t_name)){
					m_errorLabel.setText("用户名不是合法的Email地址！");
					return;
				}
				
				if(!t_pass.equals(t_pass1)){
					m_errorLabel.setText("两次密码不一样！");
					return;
				}
				
				if(!IsValidPassword(t_pass)){
					m_errorLabel.setText("密码非法！");
					return;
				}						
				
				try{					
					m_waitingLable.ShowText("正在提交注册...");
					m_clientSign.greetingService.signinAccount(t_name,t_pass,t_callback);
	
				}catch(Exception e){
					m_errorLabel.setText(e.getMessage());
					m_waitingLable.Hide();
				}
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
	
	private void CallOnSuccess(String _result){
		
		
		try{
			Document t_doc = XMLParser.parse(_result);
			Element t_elem = t_doc.getDocumentElement();
			if(t_elem.getTagName().equals("Error")){
				m_errorLabel.setText(t_elem.getFirstChild().toString());
			}else{
				hide();
				m_clientSign.ShowYuchbberPanel(_result);
			}			
			
		}catch(Exception e){
			m_errorLabel.setText(e.getMessage());
		}
		
		m_waitingLable.Hide();
	}
	
	private native static boolean IsValidEmail(String name)/*-{
	    var regex = new RegExp("^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$");
	    return regex.test(name);
	}-*/;
	
	private static boolean IsValidPassword(String _password){
		
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
