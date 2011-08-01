package com.yuchting.yuchberry.yuchsign.client.account;

import java.util.Date;
import java.util.Vector;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.XMLParser;
import com.yuchting.yuchberry.yuchsign.client.YesNoHandler;
import com.yuchting.yuchberry.yuchsign.client.Yuchsign;


class ContentTab extends TabPanel implements SelectionHandler<Integer>{
	
	BberEmailPanel m_emailPanel = new BberEmailPanel();
	BberWeiboPanel m_weiboPanel = null;
	
	int m_currSelectionIndex = 0;
	
	public ContentTab(BberPanel	_mainPanel){
		setPixelSize(250, 200);
		
		m_weiboPanel = new BberWeiboPanel(_mainPanel);
		
		addSelectionHandler(this);
		
		add(m_emailPanel,"邮件");
		add(m_weiboPanel,"Weibo");
		selectTab(0);
	}
	
	public int getSelectedIndex(){return m_currSelectionIndex;}
	
	public void onSelection(SelectionEvent<Integer> event){
		m_currSelectionIndex = event.getSelectedItem().intValue();
	}
}


public class BberPanel extends TabPanel{

	final Label m_signinName 		= new Label();
	final Label m_connectHost		= new Label();
	final Label m_serverPort 		= new Label();	
	final Label m_pushInterval 		= new Label();
	final Label m_bberLev			= new Label();
	final Label m_endTime			= new Label();
	
	final CheckBox m_usingSSL		= new CheckBox("使用SSL");
	final CheckBox m_convertToSimple = new CheckBox("转换繁体到简体");
	
	final TextArea m_signature		= new TextArea();
		
	final ListBox	m_pushList		= new ListBox(true);
	
	final Button	m_addPushAccountBut = new Button("添加");
	final Button	m_delPushAccountBut = new Button("删除");
	final Button	m_refreshAccountBut	= new Button("更新");
	
	ContentTab m_pushContent	= null;
	
	final TextArea	m_logText		= new TextArea();
	
	Yuchsign	m_mainServer		= null;
			
	yuchbber 	m_currentBber 		= null;
	
	ChangePassDlg m_changePassDlg	= null;
	
	public static final KeyPressHandler 	fsm_socketPortHandler = new KeyPressHandler() {
		
		public void onKeyPress(KeyPressEvent event) {
			TextBox t_box = (TextBox) event.getSource();
			if(!Character.isDigit(event.getCharCode())) {
				t_box.cancelKey();
		    }
			
			final int t_maxPort = 65535;
			if(Integer.valueOf(t_box.getText() + event.getCharCode()).intValue() > t_maxPort){
				t_box.cancelKey();
				t_box.setText(Integer.toString(t_maxPort));
			}
		}
	};
	
	public BberPanel(final Yuchsign _sign){
		m_mainServer = _sign;
		m_pushContent = new ContentTab(this);
				
		AddAccountAttr();
		AddPushAttr();
		AddCheckLog();
		
		selectTab(0);		
	}
	
	public void ShowBberPanle(){
		RootPanel.get("mainTab").add(this);
	}
	
	public void HideBberPanel(){
		RootPanel.get("mainTab").remove(this);
	}
		
	private void AddAccountAttr(){
		
		final VerticalPanel t_attrPane = new VerticalPanel();
		
		Button t_syncBut = new Button("同步账户");
		t_syncBut.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				StartSync();
			}
		});
		
		final Button t_levelUpBut		= new Button("提升等级");
		t_levelUpBut.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				if((m_currentBber.GetLevel() + 1) >= yuchbber.fsm_levelMoney.length){
					Yuchsign.PopupPrompt("你已经是最高等级的用户了，无法再升级了。", t_attrPane);
					return;
				}
				
				PayLevDlg t_dlg = new PayLevDlg(m_mainServer, m_currentBber);
				t_dlg.show();	
				
			}
		});
		
		final Button t_payTime			= new Button("充值时间");
		t_payTime.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				PayTimeDlg t_dlg = new PayTimeDlg(m_mainServer, m_currentBber);
				t_dlg.show();				
			}
		});
		
		final Button t_signoutBut		= new Button("退出");
		t_signoutBut.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				m_mainServer.Signout();
			}
		});
		
		final Button t_sendActivateMailBut = new Button("重新发送激活邮件");
		
		t_sendActivateMailBut.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				sendActivateMail();
			}
		});
		
		final BberPanel t_currPane = this;
		final Button t_changePass = new Button("更改密码");
		t_changePass.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				if(m_changePassDlg == null){
					m_changePassDlg = new ChangePassDlg(t_currPane);
				}
				
				m_changePassDlg.show();				
			}
		});
		
		final Button t_getdownLev = new Button("降低等级");
		t_getdownLev.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				if(m_currentBber != null && m_currentBber.GetLevel() > 0){
					Yuchsign.PopupYesNoDlg("你真的要降低用户等级么?", new YesNoHandler() {
						
						@Override
						public void Process() {
							getdownLevProcess();							
						}
					},null);
					
				}else{
					Yuchsign.PopupPrompt("你是最低等级的用户无法再降低等级。", t_currPane);
				}
				
			}
		});	
		
		m_signature.setPixelSize(420,100);
				
		
		final FlexTable  t_layout = new FlexTable();	
		
		int t_line = 0;
		t_layout.setWidget(t_line,2,t_signoutBut);
		t_layout.setWidget(t_line,3,t_sendActivateMailBut);
		AddLabelWidget(t_layout,"用户名:",m_signinName,t_line++);
		
		t_layout.setWidget(t_line, 2, t_levelUpBut);
		t_layout.setWidget(t_line, 3, t_getdownLev);
		AddLabelWidget(t_layout,"用户等级:",m_bberLev,t_line++);
		
		t_layout.setWidget(t_line, 2, t_payTime);
		AddLabelWidget(t_layout,"到期时间:",m_endTime,t_line++);
		
		AddLabelWidget(t_layout,"主机地址:",m_connectHost,t_line++);
		AddLabelWidget(t_layout,"端口:",m_serverPort,t_line++);
		
		t_layout.setWidget(t_line,2,t_changePass);
		AddLabelWidget(t_layout,"用户密码:",new HTML("[YuchSign登录密码]"),t_line++);		
		
		AddLabelWidget(t_layout,"推送间隔(秒):",m_pushInterval,t_line++);
		
		t_attrPane.add(t_layout);
		t_attrPane.add( m_usingSSL);
		t_attrPane.add(m_convertToSimple);
		t_attrPane.add(new HTML( "签名:<br />"));
		t_attrPane.add(m_signature);
		
		t_attrPane.add(t_syncBut);
		
		add(t_attrPane,"账户属性");
		
	}
	
	private void sendActivateMail(){
		try{

			Yuchsign.PopupWaiting("正在请求发送……", this);
			m_mainServer.greetingService.sendActivateMail(m_currentBber.GetSigninName(),m_verfiyCode,new AsyncCallback<String>() {
				
				@Override
				public void onSuccess(String result) {
					if(result.startsWith("/verifycode")){
							new YuchVerifyCodeDlg(result, new InputVerfiyCode() {
							
							@Override
							public void InputCode(String code) {
								m_verfiyCode = code;
								sendActivateMail();
							}
						});									
					}else{
						
						Yuchsign.HideWaiting();
						Yuchsign.PopupPrompt(result,BberPanel.this);
					}							
				}
				
				@Override
				public void onFailure(Throwable caught) {
					Yuchsign.HideWaiting();
					Yuchsign.PopupPrompt("失败：" + caught.getMessage(),BberPanel.this);
				}
			});	
		}catch(Exception e){
			Yuchsign.HideWaiting();
			Yuchsign.PopupPrompt("异常：" + e.getMessage(),BberPanel.this);
		}
	}
	
	private void getdownLevProcess(){
		
		final BberPanel t_mainPane = this;
		try{
			Yuchsign.PopupWaiting("正在提交申请", this);
			
			m_mainServer.greetingService.getdownLev(m_currentBber.GetSigninName(),
			new AsyncCallback<String>() {
				
				@Override
				public void onSuccess(String result) {
					try{
						m_currentBber.InputXMLData(result);
						
						ShowYuchbberData(m_currentBber);
						
						Yuchsign.PopupPrompt("已经降级成功，同时删除了所有推送账户", t_mainPane);
					}catch(Exception e){
						Yuchsign.PopupPrompt("错误:" + e.getMessage(), t_mainPane);
					}
					
					Yuchsign.HideWaiting();										
				}
				
				@Override
				public void onFailure(Throwable caught) {
					Yuchsign.PopupPrompt(caught.getMessage(), t_mainPane);
					Yuchsign.HideWaiting();
				}
			});
			
		}catch(Exception e){
			Yuchsign.PopupPrompt("出现错误:"+e.getMessage(), t_mainPane);
			Yuchsign.HideWaiting();
		}	
		
	}
	
	
	
	private void AddPushAttr(){
		
		m_pushList.setPixelSize(200, 200);
		
		
		final HorizontalPanel t_horzPane = new HorizontalPanel(); 
		t_horzPane.add(m_pushList);
		
		final VerticalPanel	 t_vertPane = new VerticalPanel();
		t_vertPane.setPixelSize(40, 80);
		t_vertPane.add(m_addPushAccountBut);
		t_vertPane.add(m_refreshAccountBut);
		t_vertPane.add(m_delPushAccountBut);
		
		t_horzPane.add(t_vertPane);
		t_horzPane.add(m_pushContent);
		

		add(t_horzPane,"推送列表");
		
		m_pushList.addChangeHandler(new ChangeHandler() {
			
			@Override
			public void onChange(ChangeEvent event) {
				final int t_index = m_pushList.getSelectedIndex();
				if(t_index != -1){
					
					String t_itemText = m_pushList.getItemText(t_index);
					
					Object t_account = m_currentBber.findAccount(t_itemText);
					
					if(t_account != null){
						
						if(t_account instanceof yuchEmail){
							m_pushContent.m_emailPanel.RefreshEmail((yuchEmail)t_account);
							m_pushContent.selectTab(0);
						}else if(t_account instanceof yuchWeibo){
							m_pushContent.m_weiboPanel.RefreshWeibo((yuchWeibo)t_account);
							m_pushContent.selectTab(1);
						}
					}
					
					
				}
				
			}
		});
			
		m_addPushAccountBut.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {

				if(m_currentBber.getAccountTotalNum() >= m_currentBber.GetMaxPushNum()){
					Yuchsign.PopupPrompt("已经达到当前推送的最大数量:"+ m_currentBber.GetMaxPushNum() + "(需要升级)", m_pushList);
					return;
				}
				boolean t_succ = false;
				
				if(m_pushContent.getSelectedIndex() == 0){
					
					yuchEmail t_email = m_pushContent.m_emailPanel.AddAccount(null);
					if(t_email != null){
												
						for(yuchEmail mail : m_currentBber.GetEmailList()){
							if(t_email.m_emailAddr.equalsIgnoreCase(mail.m_emailAddr)){
								Yuchsign.PopupPrompt(mail.toString() + "账户重复!", m_pushList);
								return;
							}
						}
						
						m_currentBber.GetEmailList().add(t_email);
						RefreshPushList(m_currentBber);
						m_pushContent.m_emailPanel.RefreshEmail(null);
						
						t_succ = true;
					}
					
				}else if(m_pushContent.getSelectedIndex() == 1){
					
					yuchWeibo t_weibo = m_pushContent.m_weiboPanel.AddAccount(null);
					if(t_weibo != null){
												
						for(yuchWeibo weibo : m_currentBber.GetWeiboList()){
							if(t_weibo.m_typeName.equalsIgnoreCase(weibo.m_typeName)){
								Yuchsign.PopupPrompt(weibo.toString() + "Weibo重复!", m_pushList);
								return;
							}
						}
						
						m_currentBber.GetWeiboList().add(t_weibo);
						RefreshPushList(m_currentBber);
						m_pushContent.m_weiboPanel.RefreshWeibo(null);
						
						t_succ = true;
					}
				}
				
				if(t_succ){
					Yuchsign.PopupPrompt("添加用户之后需要到 [账户属性] 去同步，才能保存，开始推送", BberPanel.this);
				}				
				
			}
		});
		
		m_refreshAccountBut.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				final int t_index = m_pushList.getSelectedIndex();
								
				if(t_index != -1){
					
					String t_itemText = m_pushList.getItemText(t_index);
					Object t_account = m_currentBber.findAccount(t_itemText);
					
					boolean t_ok = false;
					if(t_account instanceof yuchEmail){
						t_ok = m_pushContent.m_emailPanel.AddAccount((yuchEmail)t_account) != null;
					}else if(t_account instanceof yuchWeibo){
						t_ok = m_pushContent.m_weiboPanel.AddAccount((yuchWeibo)t_account) != null;
					}
					
					if(t_ok){
						Yuchsign.PopupPrompt("更新数据成功，需要同步才能最终保存生效。",t_horzPane);
					}
				}
				
			}
		});
		
		m_delPushAccountBut.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event){

				final int t_index = m_pushList.getSelectedIndex();
				if(t_index != -1){
					

					String t_itemText = m_pushList.getItemText(t_index);
					final Object t_account = m_currentBber.findAccount(t_itemText);
					if(t_account != null){
						
						Yuchsign.PopupYesNoDlg("你确定要删除这个 "+ t_itemText +" 账户么?",new YesNoHandler(){
							
							public void Process(){
																
								if(t_account instanceof yuchEmail){
									
									m_currentBber.GetEmailList().remove(t_account);
									RefreshPushList(m_currentBber);
									m_pushContent.m_emailPanel.RefreshEmail(null);
									
								}else if(t_account instanceof yuchWeibo){
									m_currentBber.GetWeiboList().remove(t_account);
									RefreshPushList(m_currentBber);
									m_pushContent.m_weiboPanel.RefreshWeibo(null);
								}				
								
								
								Yuchsign.PopupPrompt("删除用户之后需要到 [账户属性] 同步，才能保存", BberPanel.this);
							}
							
						},null);
					}
					
					
				}
			}
		});
	}
	
	private void AddCheckLog(){
		final Button t_checkBut = new Button("检查日志");
		final Button t_seekHelp = new Button("查找帮助");
		
		m_logText.setSize("600px", "300px");
		m_logText.setReadOnly(true);
		
		final HorizontalPanel t_buttonPane = new HorizontalPanel();
		t_buttonPane.add(t_checkBut);
		t_buttonPane.add(t_seekHelp);
		
		
		final VerticalPanel t_mainPane = new VerticalPanel();
		t_mainPane.add(t_buttonPane);
		t_mainPane.add(m_logText);
		
		t_checkBut.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				try{
					
					if(m_currentBber.GetConnectHost().isEmpty()){
						Yuchsign.PopupPrompt("没有同步主机，无法获取日志，请先同步。", t_mainPane);
						return;
					}
					
					Yuchsign.PopupWaiting("正在查询日志", t_mainPane);
					
					m_mainServer.greetingService.checkAccountLog(m_currentBber.GetSigninName(),m_currentBber.GetPassword(),
					new AsyncCallback<String>() {
						
						@Override
						public void onSuccess(String result) {
							Yuchsign.HideWaiting();
							
							if(result.startsWith("<Error>")){
								int t_begin = result.indexOf("<Error>");
								int t_end = result.indexOf("</Error>");
								if(t_end != -1){
									PopupProblemAndSearchHelp(result.substring(t_begin + 7,t_end),t_mainPane);
								}else{
									PopupProblemAndSearchHelp(result,t_mainPane);
								}								
							
							}else{
								m_logText.setText(result);
							}						
						}
						
						@Override
						public void onFailure(Throwable caught) {
							PopupProblemAndSearchHelp(caught.getMessage(),t_mainPane);
							Yuchsign.HideWaiting();
						}
					});
				}catch(Exception e){
					PopupProblemAndSearchHelp(e.getMessage(),t_mainPane);
					Yuchsign.HideWaiting();
				}
				
				
			}
		});
		
		t_seekHelp.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				Window.open("http://code.google.com/p/yuchberry/wiki/Connect_Error_info#服务器条目", "_blank", "");
			}
		});
		
		
		add(t_mainPane,"查询日志");
	}
	
	String		m_verfiyCode = "";
	private void StartSync(){
		
		if(m_signature.getText().length() > 500){
			Yuchsign.PopupPrompt("签名不能大于500个字符", this);
			return;
		}
		
		m_currentBber.SetConvertSimpleChar(m_convertToSimple.getValue());
		m_currentBber.SetUsingSSL(m_usingSSL.getValue());
		m_currentBber.SetSignature(m_signature.getText());
		
		if(m_currentBber.GetEmailList().isEmpty()
			&& m_currentBber.GetWeiboList().isEmpty()){
			Yuchsign.PopupPrompt("没有推送账户，无法同步，请先添加推送账户。", this);
			selectTab(1);
			return;
		}
		
		Yuchsign.PopupWaiting("正在同步，可能需要3-5分钟，请耐心等待。如果长时间没反应，请刷新页面登录再试。", this);
		
		final Widget t_bberPanel = this;
		try{
			m_mainServer.greetingService.syncAccount(m_currentBber.OuputXMLData(),m_verfiyCode,new AsyncCallback<String>() {
				
				@Override
				public void onSuccess(String result) {
					if(result.startsWith("/verifycode")){
							new YuchVerifyCodeDlg(result, new InputVerfiyCode() {
							
							@Override
							public void InputCode(String code) {
								m_verfiyCode = code;
								StartSync();
							}
						});
					}else{
						SyncOnSuccess(result,t_bberPanel);
					}
					
				}
				
				@Override
				public void onFailure(Throwable caught) {
					PopupProblemAndSearchHelp(caught.getMessage(),t_bberPanel);
					Yuchsign.HideWaiting();
				}
			});
			
		}catch(Exception e){
			PopupProblemAndSearchHelp(e.getMessage(),this);
			Yuchsign.HideWaiting();
		}
	}
	
	Timer m_checkStateTimer		= null;
	private void SyncOnSuccess(String result,final Widget _panel){
		
		try{
			
			Document t_doc = XMLParser.parse(result);
			Element t_elem = t_doc.getDocumentElement();
			
			if(t_elem.getTagName().equals("Error")){
				PopupProblemAndSearchHelp(t_elem.getFirstChild().toString(),_panel);
				Yuchsign.HideWaiting();
			}else if(t_elem.getTagName().equals("Loading")){
				
				// Setup timer to refresh list automatically.
				m_checkStateTimer = new Timer() {
			    	@Override
			    	public void run() {
			    		CheckSyncStateTimer(_panel);
			    	}
			    };
			    
			    m_checkStateTimer.scheduleRepeating(15*1000);
			    
			}else{
				CheckSyncOnSuccess(result,_panel);				
			}	
		}catch(Exception ex){
			PopupProblemAndSearchHelp(ex.getMessage() + result,_panel);
			Yuchsign.HideWaiting();
		}			
	}
	
	private void CheckSyncStateTimer(final Widget _panel){
		try{
			m_mainServer.greetingService.syncAccount_check(m_currentBber.GetSigninName(),m_currentBber.GetPassword(),
	    		new AsyncCallback<String>() {
					
					@Override
					public void onSuccess(String result) {
						CheckSyncOnSuccess(result,_panel);
					}
					
					@Override
					public void onFailure(Throwable caught) {
						PopupProblemAndSearchHelp(caught.getMessage(),_panel);
						Yuchsign.HideWaiting();
						
						if(m_checkStateTimer != null){
							m_checkStateTimer.cancel();
							m_checkStateTimer = null;
						}
					}
				});
			
		}catch(Exception e){
			m_checkStateTimer.cancel();
			m_checkStateTimer = null;
			
			PopupProblemAndSearchHelp(e.getMessage(),_panel);
			Yuchsign.HideWaiting();
		}
		
	}
	
	private void CheckSyncOnSuccess(String _result,final Widget _panel){
		Document t_doc = XMLParser.parse(_result);
		Element t_elem = t_doc.getDocumentElement();
		
		if(t_elem.getTagName().equals("Error")){
			PopupProblemAndSearchHelp(t_elem.getFirstChild().toString(),_panel);
			Yuchsign.HideWaiting();
			
		}else if(t_elem.getTagName().equals("yuchbber")){	
			
			try{
				m_currentBber.InputXMLData(_result);
				Yuchsign.PopupPrompt("同步成功！可以使用手机连接服务器了。\n" +
						"注意：如果手机没有连接服务器的时间超过3天，\n" +
						"就需要再次同步。\n " +
						"<font size=\"5\" >同步成功端口："+m_currentBber.GetServerPort()+"</font>\n" +
						"重新同步账户之后，可能会改变端口，请注意。", _panel);
				
			}catch(Exception e){
				PopupProblemAndSearchHelp(e.getMessage(), _panel);
			}
			
			ShowYuchbberData(m_currentBber);
			Yuchsign.HideWaiting();		
		}
		
		if(m_checkStateTimer != null){
			m_checkStateTimer.cancel();
			m_checkStateTimer = null;
		}
	}
	
	private void PopupProblemAndSearchHelp(String _help,final Widget _panel){
		
		if(_help == null){
			_help = "null";
		}
		
		StringBuffer t_search = new StringBuffer();
		t_search.append("配置推送账户出现错误：").append(_help).append("\n");

		t_search.append("<a href=\"http://www.google.com.hk/search?hl=zh-CN&source=hp&q=").append(URL.encode(_help)).
				append("\" target=_blank>搜索获得帮助</a>，有任何疑问请联系:<a href=\"mailto:yuchberry@gmail.com\">yuchberry@gmail.com</a>");		
		
		Yuchsign.PopupPrompt(t_search.toString(),_panel);
	}
	
	public void ShowYuchbberData(final yuchbber _bber){
		m_currentBber = _bber;
		
		m_signinName.setText(_bber.GetSigninName());
		
		if(_bber.GetConnectHost().isEmpty()){
			m_connectHost.setText("<没有同步>");
		}else{
			m_connectHost.setText(_bber.GetConnectHost());
		}		
		
		m_bberLev.setText(GetBberLevelString(_bber.GetLevel()));
	    
		if(_bber.GetCreateTime() == 0){
		    m_endTime.setText("<没有同步>");
		}else{
			Date date = new Date(_bber.GetCreateTime() + _bber.GetUsingHours() * 3600000);
		    m_endTime.setText(DateTimeFormat.getFormat("yyyy-MM-dd HH:mm").format(date));
		}	    	    
	    
	    if(_bber.GetServerPort() != 0){
	    	m_serverPort.setText("" + _bber.GetServerPort());
	    }else{
	    	m_serverPort.setText("<没有同步>");
	    }	
		
		m_pushInterval.setText("" + _bber.GetPushInterval());
		
		m_usingSSL.setEnabled(false);
		m_usingSSL.setValue(_bber.IsUsingSSL());
		m_convertToSimple.setValue(_bber.IsConvertSimpleChar());
		
		m_signature.setText(_bber.GetSignature());
		
		m_logText.setText("");
		
		RefreshPushList(_bber);

	}
		
	private String GetBberLevelString(int _bberLev){
		switch(_bberLev){
		case 0: return "VIP0";
		case 1: return "VIP1";
		case 2: return "VIP2";
		default: return "VIP3";
		}
	}
	
	public void RefreshPushList(yuchbber _bber){
		while(m_pushList.getItemCount() != 0){
			m_pushList.removeItem(0);
		}
		
		Vector<yuchEmail> t_emailList = _bber.GetEmailList();
		if(t_emailList != null){

			for(yuchEmail email : t_emailList){
				m_pushList.addItem(email.toString());
			}
		}
		
		Vector<yuchWeibo> t_weiboList = _bber.GetWeiboList();
		if(t_weiboList != null){

			for(yuchWeibo weibo : t_weiboList){
				m_pushList.addItem(weibo.toString());
			}
		}
	}
	
	static void AddLabelWidget(Panel _pane,final String _label,final Widget _widget){
		_pane.add(new Label(_label));
		_pane.add(_widget);
	}
	
	static void AddLabelWidget(FlexTable _table,final String _label,final Widget _widget,int _line){
		_table.setHTML(_line, 0, _label);
		_table.setWidget(_line, 1, _widget);
	}

}
