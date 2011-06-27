package com.yuchting.yuchberry.yuchsign.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class BberWeiboPanel extends FlowPanel{
	
	private static final String			fsm_genWeiboAuth = "生成授权信息";
	private static final String			fsm_genAccessToken = "请求用户授权";

	final RadioButton[]		m_type	= 
	{
		new RadioButton("type","sina"),
		new RadioButton("type","qq"),
	};
	
	final Button			m_requestButton	= new Button(fsm_genWeiboAuth);
	
	final TextBox			m_accessToken 		= new TextBox();
	final TextBox			m_secretToken 		= new TextBox();
	
	final RadioButton[]		m_pushType	= 
	{
		new RadioButton("push","主页手动刷新（耗电少，建议主页Weibo多的用户选择）"),
		new RadioButton("push","主页主动推送（微博控专用，实时推送主页Weibo）"),
	};
	
	BberPanel				m_mainPanel			= null;
	String					m_authURL			= null;
	
	public BberWeiboPanel(BberPanel _mainPanel){
		
		m_mainPanel = _mainPanel;
		
		ClickHandler t_typeHandler = new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				m_authURL = null;
				m_accessToken.setText("");
				m_secretToken.setText("");
				
				m_requestButton.setText(fsm_genWeiboAuth);
				m_requestButton.setEnabled(true);
				m_pushType[0].setValue(true);
			}
		};
		
		for(RadioButton but : m_type){
			add(but);
			but.addClickHandler(t_typeHandler);
		}
		
		m_type[0].setValue(true);
		
		m_accessToken.setPixelSize(200,18);
		m_secretToken.setPixelSize(200,18);
		
		m_accessToken.setEnabled(false);
		m_secretToken.setEnabled(false);
		
		final FlowPanel t_subPane = this;
		BberPanel.AddLabelWidget(t_subPane,"访问令牌:",m_accessToken);
		BberPanel.AddLabelWidget(t_subPane,"密码令牌:",m_secretToken);
		
		m_requestButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				
				if(m_authURL != null){
					Yuchsign.PopupWaiting("等待用户完成授权……", BberWeiboPanel.this);
					Window.open(m_authURL,"_blank","");
					requestAccessToken();
					return ;
				}
				
				String t_type = null;
				
				for(RadioButton but : m_type){
					if(but.getValue()){
						t_type = but.getText();
					}
				}
				if(t_type == null){
					Yuchsign.PopupPrompt("内部错误，请选择Weibo类型", BberWeiboPanel.this);
					return ;
				}
				
				Yuchsign.PopupWaiting("正在请求服务器生成授权信息，请等待……", BberWeiboPanel.this);
				
				m_mainPanel.m_mainServer.greetingService.getWeiboAuthURL(
						m_mainPanel.m_currentBber.GetSigninName(), t_type, 
				new AsyncCallback<String>() {
					@Override
					public void onSuccess(String result) {
						
						if(result.startsWith("http")){
							m_authURL = result;
							m_requestButton.setText(fsm_genAccessToken);
														
							Yuchsign.PopupPrompt("生成授权信息成功，\n请点击 ["+fsm_genAccessToken+"] 按钮获得授权码。",m_accessToken);
							Yuchsign.HideWaiting();
							
						}else{
							Yuchsign.PopupPrompt(result, BberWeiboPanel.this);
							Yuchsign.HideWaiting();
						}
					}
					
					@Override
					public void onFailure(Throwable caught) {
						Yuchsign.PopupPrompt(caught.getMessage(), BberWeiboPanel.this);
						Yuchsign.HideWaiting();
					}											
				});
			}
		});
		
		
		add(m_requestButton);
		
		VerticalPanel t_pushPanel = new VerticalPanel();
		
		for(RadioButton but : m_pushType){
			t_pushPanel.add(but);
		}
		m_pushType[0].setValue(true);
		
		add(t_pushPanel);
	}
	
	Timer m_checkStateTimer = null;
	private void requestAccessToken(){
		// Setup timer to refresh list automatically.
		m_checkStateTimer = new Timer() {
	    	@Override
	    	public void run() {
	    		m_mainPanel.m_mainServer.greetingService.getWeiboAccessToken(
						m_mainPanel.m_currentBber.GetSigninName(), 
				new AsyncCallback<String>() {
					@Override
					public void onSuccess(String result) {
						
						if(result.startsWith("<Error")){
							Yuchsign.HideWaiting();
							Yuchsign.PopupPrompt(result, BberWeiboPanel.this);
							
							m_checkStateTimer.cancel();
						}else if(result.indexOf("&") != -1){
							Yuchsign.HideWaiting();
							String[] t_arr = result.split("&");
							m_accessToken.setText(t_arr[0]);
							m_secretToken.setText(t_arr[1]);
							
							Yuchsign.PopupPrompt("授权成功！可以添加了！", BberWeiboPanel.this);
							m_checkStateTimer.cancel();
						}
					}
					
					@Override
					public void onFailure(Throwable caught) {
						Yuchsign.PopupPrompt(caught.getMessage(), BberWeiboPanel.this);
						Yuchsign.HideWaiting();
						m_checkStateTimer.cancel();
					}											
				});
	    	}
	    };
	    
	    m_checkStateTimer.scheduleRepeating(5*1000);
	}
	
	public void RefreshWeibo(yuchWeibo _weibo){
		
		if(_weibo != null){
			
			m_requestButton.setEnabled(false);
			
			for(RadioButton but : m_type){
				if(_weibo.m_typeName.equals(but.getText())){					
					
					but.setValue(true);
					m_accessToken.setText(_weibo.m_accessToken);
					m_secretToken.setText(_weibo.m_secretToken);
					
					break;
				}
			}
			
			if(_weibo.m_timelineSum < 0){
				m_pushType[0].setValue(true);
			}else{
				m_pushType[1].setValue(true);
			}
			
		}else{
		
			m_type[0].setValue(true);
			
			m_requestButton.setEnabled(true);
			m_requestButton.setText(fsm_genWeiboAuth);
			
			m_accessToken.setText("");
			m_secretToken.setText("");
		}
	}
	
	public yuchWeibo AddAccount(yuchWeibo _weibo){
		if(m_accessToken.getText().length() == 0){
			Yuchsign.PopupPrompt("请先完成授权", BberWeiboPanel.this);
			return null;
		}
		
		yuchWeibo t_ret;
		if(_weibo != null){
			t_ret = _weibo;
		}else{
			t_ret = new yuchWeibo();
		}
		t_ret.m_accessToken = m_accessToken.getText();
		t_ret.m_secretToken = m_secretToken.getText();
		
		if(m_pushType[0].getValue()){
			t_ret.m_timelineSum = -30;
		}else{
			t_ret.m_timelineSum = 50;
		}
		for(RadioButton but:m_type){
			if(but.getValue()){
				t_ret.m_typeName = but.getText();
				break;
			}
		}
		
		t_ret.m_atMeSum = 5;
		t_ret.m_commentMeSum = 5;
		t_ret.m_directMsgSum = 5;
		
		t_ret.m_accoutName = "WeiboAccount";
				
		return t_ret;
	}
	
	
}
