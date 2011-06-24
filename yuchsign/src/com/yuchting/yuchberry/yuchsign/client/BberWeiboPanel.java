package com.yuchting.yuchberry.yuchsign.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;

public class BberWeiboPanel extends FlowPanel{
	

	final RadioButton[]		m_type	= {
										new RadioButton("type","sina"),
										new RadioButton("type","qq"),
									  };
	
	final Button			m_requestButton	= new Button("请求授权");
	
	final TextBox			m_weiboAccountName	= new TextBox();
	final TextBox			m_accessToken 		= new TextBox();
	final TextBox			m_secretToken 		= new TextBox();
	
	Yuchsign				m_mainSign			= null;
	
	public BberWeiboPanel(Yuchsign _mainSign){
		
		m_mainSign = _mainSign;
		
		for(RadioButton but : m_type){
			add(but);
		}
		
		m_type[0].setValue(true);
		
		m_weiboAccountName.setEnabled(false);
		m_accessToken.setEnabled(false);
		m_secretToken.setEnabled(false);
		
		final FlowPanel t_subPane = this;
		
		BberPanel.AddLabelWidget(t_subPane, "帐户名:", m_weiboAccountName);
		BberPanel.AddLabelWidget(t_subPane,"访问令牌:",m_accessToken);
		BberPanel.AddLabelWidget(t_subPane,"密码令牌:",m_secretToken);
		
		m_requestButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				//Yuchsign.PopupPrompt("正在完善，敬请期待！", t_subPane);
				m_mainSign.
			}
		});
		
		
		add(m_requestButton);		
	}
	
	public void RefreshWeibo(yuchWeibo _weibo){
		
		if(_weibo != null){
			
			for(RadioButton but : m_type){
				but.setEnabled(false);
			}
			m_requestButton.setEnabled(false);
			
			for(RadioButton but : m_type){
				if(_weibo.m_typeName.equals(but.getText())){					
					
					but.setValue(true);
					
					m_weiboAccountName.setText(_weibo.m_accoutName);
					m_accessToken.setText(_weibo.m_accessToken);
					m_secretToken.setText(_weibo.m_secretToken);
					
					break;
				}
			}
			
		}else{
			for(RadioButton but : m_type){
				but.setEnabled(true);
			}
			m_type[0].setValue(true);
			
			m_requestButton.setEnabled(true);
			m_requestButton.setText("请求授权");
			

			m_weiboAccountName.setText("");
			m_accessToken.setText("");
			m_secretToken.setText("");
		}
	}
	
	public yuchWeibo AddAccount(yuchWeibo _weibo){
		return null;
	}
	
	
}
