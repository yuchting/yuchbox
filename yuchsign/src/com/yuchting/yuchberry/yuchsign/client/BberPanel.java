package com.yuchting.yuchberry.yuchsign.client;

import java.util.Date;
import java.util.Vector;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.XMLParser;


class ContentTab extends TabPanel{
	
	final TextBox			m_account	= new TextBox();
	final PasswordTextBox	m_password	= new PasswordTextBox();
	final TextBox			m_username	= new TextBox();
	
	final TextBox			m_host		= new TextBox();
	final TextBox			m_port		= new TextBox();
	
	final RadioButton[]		m_protocal	= {
											new RadioButton("protocal","imap"),
											new RadioButton("protocal","imaps"),
											new RadioButton("protocal","pop3"),
											new RadioButton("protocal","pop3s"),
										  };
	
	final TextBox			m_host_send	= new TextBox();
	final TextBox			m_port_send = new TextBox();
	
	final CheckBox			m_usingFullname = new CheckBox("使用全地址作为用户名登录");
	final CheckBox			m_appendHTML	= new CheckBox("追加HTML到正文");
	
	final DisclosurePanel	m_advancedDisclosure = new DisclosurePanel("高级设置：");
	
	final private class commonConfig{
		
		String	m_name;
		String	m_protocalName;
		String	m_host;
		String	m_port;
		
		String	m_host_send;
		String	m_port_send;
		
		boolean m_useFullnameSignin = false;
		
		String  m_prompt;
		
		public commonConfig(String _parserLine){
			String[] t_data = _parserLine.split(",");
						
			m_name 			= t_data[0];
			m_protocalName 	= t_data[1];
			m_host 			= t_data[2];
			m_port 			= t_data[3];
			m_host_send		= t_data[4];
			m_port_send		= t_data[5];
			m_useFullnameSignin = t_data[6].equals("1");
			
			if(t_data.length >= 8){
				m_prompt = t_data[7];
			}
		}
		
		public void SetConfig(ContentTab _dlg){
			if(m_host.equals("0")){
				return;
			}
			
			_dlg.m_host.setText(m_host);
			_dlg.m_port.setText(m_port);
			
			for(int i = 0;i < _dlg.m_protocal.length;i++){
				if(m_protocal[i].getText().equals(m_protocalName)){
					m_protocal[i].setValue(true);
					break;
				}
			}
			
			_dlg.m_host_send.setText(m_host_send);
			_dlg.m_port_send.setText(m_port_send);
			
			_dlg.m_usingFullname.setValue(m_useFullnameSignin);
		}
	}
	
	final commonConfig[]		m_commonConfigList = 
	{
		new commonConfig("@gmail.com,imaps,imap.gmail.com,993,smtp.gmail.com,587,0"),
		new commonConfig("@163.com,imap,imap.163.com,143,smtp.163.com,25,0"),
		new commonConfig("@126.com,imap,imap.126.com,143,smtp.126.com,25,0"),
		new commonConfig("@qq.com,imap,imap.qq.com,143,smtp.qq.com,25,0,请使用浏览器访问qq邮箱，确保其pop3/imap选项已经打开\n<a href=\"http://service.mail.qq.com/cgi-bin/help?subtype=1&id=26&no=308\" target=_blank>查看帮助</a>"),
		new commonConfig("@vip.qq.com,imap,imap.qq.com,143,smtp.qq.com,25,1,请使用浏览器访问qq邮箱，确保其pop3/imap选项已经打开\n<a href=\"http://service.mail.qq.com/cgi-bin/help?subtype=1&id=26&no=308\" target=_blank>查看帮助</a>"),
		new commonConfig("@yahoo.com,imaps,apple.imap.mail.yahoo.com,993,smtp.mail.yahoo.com,587,0"),
		new commonConfig("@yahoo.com.cn,pop3s,pop.mail.yahoo.com.cn,995,smtp.mail.yahoo.com.cn,465,0,雅虎邮箱免费版貌似不支持pop的链接\n请确认可以在web界面配置pop选项 <a href=\"http://help.cn.yahoo.com/answerpage_3631.html target=_blank\">查看帮助</a>"),		
		new commonConfig("@yahoo.cn,pop3s,pop.mail.yahoo.cn,995,smtp.mail.yahoo.cn,465,0,雅虎邮箱免费版貌似不支持pop的链接\n请确认可以在web界面配置pop选项 <a href=\"http://help.cn.yahoo.com/answerpage_3631.html target=_blank\">查看帮助</a>"),
		new commonConfig("@hotmail.com,pop3s,pop3.live.com,995,smtp.live.com,587,1,hotmail不支持imap，同时pop3连接时也会有许多问题，会导致推送不正常，不建议使用。"),
		new commonConfig("@sina.com,pop3,pop.sina.com,110,smtp.sina.com,25,0,请使用浏览器访问sina邮箱，确保其pop3选项已经打开\n<a href=\"http://mail.sina.com.cn/help2/client01.html\" target=_blank>查看帮助</a>"),
		new commonConfig("@139.com,pop3,pop.139.com,110,smtp.139.com,25,0"),
		new commonConfig("@tom.com,pop3,pop.tom.com,110,smtp.tom.com,25,0"),
		new commonConfig("@21cn.com,pop3,pop.21cn.com,110,smtp.21cn.com,25,0"),
		new commonConfig("@foxmail.com,pop3,pop.qq.com,110,smtp.qq.com,25,1,请使用浏览器访问foxmail邮箱，确保其pop3选项已经打开\n<a href=\"http://service.mail.qq.com/cgi-bin/help?subtype=1&id=26&no=308\" target=_blank>查看帮助</a>"),
		new commonConfig("@sohu.com,imap,mail.sohu.com,143,mail.sohu.com,25,0")
	};
	
	public ContentTab(){
		setAnimationEnabled(true);
		setPixelSize(250, 200);
		
		m_account.addKeyUpHandler(new KeyUpHandler() {
			@Override
			public void onKeyUp(KeyUpEvent event) {
				AutoSelHost();				
			}
		});
		
		m_port.addKeyPressHandler(BberPanel.fsm_socketPortHandler);
		m_port_send.addKeyPressHandler(BberPanel.fsm_socketPortHandler);
		
		// the IE and firefox is Compatible but Chrome
		m_port.setStyleName("gwt-TextBox-SocketPort");
		m_port_send.setStyleName("gwt-TextBox-SocketPort");
		
		
		final FlowPanel t_subPane =new FlowPanel();
		
		BberPanel.AddLabelWidget(t_subPane, "邮件地址:", m_account);
		BberPanel.AddLabelWidget(t_subPane, "邮件密码:", m_password);
		BberPanel.AddLabelWidget(t_subPane,"名字:",m_username);

		final FlowPanel t_subPane1 = new FlowPanel();
		
		for(RadioButton but : m_protocal){
			t_subPane1.add(but);
		}
		
		final FlexTable t_hostPane = new FlexTable();
		m_host.setWidth("10em");
		m_port.setWidth("10em");
		BberPanel.AddLabelWidget(t_hostPane, "主机地址:", m_host,0);
		BberPanel.AddLabelWidget(t_hostPane, "端口:", m_port,1);
		t_subPane1.add(t_hostPane);
		
		final FlexTable t_hostPane_send = new FlexTable();
		m_host_send.setWidth("10em");
		m_port_send.setWidth("10em");
		BberPanel.AddLabelWidget(t_hostPane_send, "发送主机:", m_host_send,0);
		BberPanel.AddLabelWidget(t_hostPane_send, "发送端口:", m_port_send,1);
		t_subPane1.add(t_hostPane_send);
		
		t_subPane1.add(new HTML("<br />"));
		t_subPane1.add(m_usingFullname);
		t_subPane1.add(new HTML("<br />"));
		t_subPane1.add(m_appendHTML);
		
		m_advancedDisclosure.add(t_subPane1);
		
		t_subPane.add(m_advancedDisclosure);
		
		add(t_subPane,"邮件");
		selectTab(0);		
	}
	
	String m_addHostPrompt = null;
	
	public void AutoSelHost(){
		String t_account = m_account.getText().toLowerCase();
		final int t_atIndex = t_account.indexOf('@');
		
		m_addHostPrompt = null;
		
		if(t_atIndex != -1){
			
			String t_addr = t_account.substring(t_atIndex);
			if(t_addr.indexOf('.') != -1){
				
				for(commonConfig t_config: m_commonConfigList){

					if(t_addr.equalsIgnoreCase(t_config.m_name)){
						t_config.SetConfig(this);
						m_addHostPrompt = t_config.m_prompt;
						break;
					}
				}
			}
		}
	}
	
	public yuchEmail AddAccount(final yuchEmail _email){
		
		if(!LogonDialog.IsValidEmail(m_account.getText())){
			Yuchsign.PopupPrompt("不是合法的邮件地址", m_account);
			return null;
		}
		
		if(m_password.getText().length() == 0){
			Yuchsign.PopupPrompt("不是合法的邮件地址", m_password);
			return null;
		}
		
		if(m_host.getText().length() == 0){
			m_advancedDisclosure.setOpen(true);
			Yuchsign.PopupPrompt("邮件接受服务器地址不能为空", m_port);
			return null;
		}
		
		final int t_port = Integer.valueOf(m_port.getText()).intValue(); 
		if(t_port <= 0 || t_port >= 65535){
			m_advancedDisclosure.setOpen(true);
			Yuchsign.PopupPrompt("邮件接受服务器端口非法", m_port);
			return null;
		}
		
		if(m_host_send.getText().length() == 0){ 
			m_advancedDisclosure.setOpen(true);
			Yuchsign.PopupPrompt("邮件发送服务器地址不能为空", m_host_send);
			return null;
		}
		
		final int t_port_send = Integer.valueOf(m_port_send.getText()).intValue(); 
		if(t_port_send <= 0 || t_port_send >= 65535){
			m_advancedDisclosure.setOpen(true);
			Yuchsign.PopupPrompt("邮件发送服务器端口非法", m_port_send);
			return null;
		}
		
		if(m_password.getText().indexOf("+") != -1 || m_password.getText().indexOf("&") != -1){
			Yuchsign.PopupPrompt("实在抱歉，因为在数据传输过程中无法支持符号\"+&\"\n无法添加成功。", m_password);
			return null;
		}
		
		if(m_addHostPrompt != null){
			Yuchsign.PopupPrompt(m_addHostPrompt, getParent());
		}
		
		
		yuchEmail t_email = _email == null?(new yuchEmail()):_email;
		
		t_email.m_appendHTML 		= m_appendHTML.getValue();
		t_email.m_fullnameSignIn	= m_usingFullname.getValue();
				
		t_email.m_emailAddr			= m_account.getText();
		t_email.m_password			= m_password.getText();
		t_email.m_username			= m_username.getText();
		
		t_email.m_host				= m_host.getText();
		t_email.m_port				= Integer.valueOf(m_port.getText()).intValue();
		
		t_email.m_host_send			= m_host_send.getText();
		t_email.m_port_send			= Integer.valueOf(m_port_send.getText()).intValue();
		
		for(RadioButton but : m_protocal){
			if(but.getValue()){
				t_email.m_protocol = but.getText();
				break;
			}
		}		
		
		return t_email;
	}
	
	public void RefreshEmail(final yuchEmail _email){
		if(_email != null){
			m_appendHTML.setValue(_email.m_appendHTML);
			m_usingFullname.setValue(_email.m_fullnameSignIn);
			
			m_account.setText(_email.m_emailAddr);
			m_password.setText(_email.m_password);
			m_username.setText(_email.m_username);
			
			m_host.setText(_email.m_host);
			m_port.setText(Integer.toString(_email.m_port));
			
			m_host_send.setText(_email.m_host_send);
			m_port_send.setText(Integer.toString(_email.m_port_send));
			
			for(RadioButton but : m_protocal){
				if(but.getText().equals(_email.m_protocol)){
					but.setValue(true);
					break;
				}
			}
					
		}else{
			m_appendHTML.setValue(false);
			m_usingFullname.setValue(false);
			
			m_account.setText("");
			m_password.setText("");
			m_username.setText("");
			
			m_host.setText("");
			m_port.setText("");
			
			m_host_send.setText("");
			m_port_send.setText("");
			
			m_protocal[0].setValue(true);
		}	
	}
	
}

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
		
		if(!LogonDialog.IsValidPassword(t_origPassString)){
			Yuchsign.PopupPrompt("旧密码不符合规定，需要不小于6位的数字和字母组成。", m_origPass);
			return;
		}
		
		if(!LogonDialog.IsValidPassword(t_newPassString)){
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
	
	final ContentTab m_pushContent	= new ContentTab();
	
	final TextArea	m_logText		= new TextArea();
	
	Yuchsign	m_mainServer		= null;
			
	yuchbber 	m_currentBber 		= null;
	
	ChangePassDlg m_changePassDlg	= null;
	
	public static final KeyPressHandler 	fsm_socketPortHandler = new KeyPressHandler() {
		
		@Override
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
		AddLabelWidget(t_layout,"用户名:",m_signinName,t_line++);
		
		t_layout.setWidget(t_line, 2, t_levelUpBut);
		t_layout.setWidget(t_line, 3, t_getdownLev);
		AddLabelWidget(t_layout,"用户等级:",m_bberLev,t_line++);
		
		t_layout.setWidget(t_line, 2, t_payTime);
		AddLabelWidget(t_layout,"到期时间:",m_endTime,t_line++);
		
		AddLabelWidget(t_layout,"主机地址:",m_connectHost,t_line++);
		AddLabelWidget(t_layout,"端口:",m_serverPort,t_line++);
		
		t_layout.setWidget(t_line,2,t_changePass);
		AddLabelWidget(t_layout,"用户密码:",new HTML("<YuchSign登录密码>"),t_line++);		
		
		AddLabelWidget(t_layout,"推送间隔(秒):",m_pushInterval,t_line++);
		
		t_attrPane.add(t_layout);
		t_attrPane.add( m_usingSSL);
		t_attrPane.add(m_convertToSimple);
		t_attrPane.add(new HTML( "签名:<br />"));
		t_attrPane.add(m_signature);
		
		t_attrPane.add(t_syncBut);
		
		add(t_attrPane,"账户属性");
		
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
				if(t_index != -1 && t_index < m_currentBber.GetEmailList().size()){
					
					m_pushContent.RefreshEmail(m_currentBber.GetEmailList().get(t_index));
				}
				
			}
		});
			
		m_addPushAccountBut.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				yuchEmail t_email = m_pushContent.AddAccount(null);
				if(t_email != null){
					
					if(m_currentBber.GetEmailList().size() >= m_currentBber.GetMaxPushNum()){
						Yuchsign.PopupPrompt("已经达到当前推送的最大数量:"+ m_currentBber.GetMaxPushNum() + "(需要升级)", m_pushList);
						return;
					}
					
					for(yuchEmail mail : m_currentBber.GetEmailList()){
						if(t_email.m_emailAddr.equalsIgnoreCase(mail.m_emailAddr)){
							Yuchsign.PopupPrompt(mail.toString() + "账户重复!", m_pushList);
							return;
						}
					}
					
					m_currentBber.GetEmailList().add(t_email);
					RefreshPushList(m_currentBber);
					m_pushContent.RefreshEmail(null);
				}
			}
		});
		
		m_refreshAccountBut.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				final int t_index = m_pushList.getSelectedIndex();
				if(t_index != -1 && t_index < m_currentBber.GetEmailList().size()){
					
					yuchEmail t_email = m_currentBber.GetEmailList().elementAt(t_index);
					if(m_pushContent.AddAccount(t_email) != null){
						Yuchsign.PopupPrompt("更新数据成功",t_horzPane);
					}
				}
				
			}
		});
		
		m_delPushAccountBut.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event){

				final int t_index = m_pushList.getSelectedIndex();
				if(t_index != -1 && t_index < m_currentBber.GetEmailList().size()){
					
					Yuchsign.PopupYesNoDlg("你确定要删除这个 "+ m_currentBber.GetEmailList().elementAt(t_index).toString() +" 账户么?",new YesNoHandler(){
						public void Process(){
							m_currentBber.GetEmailList().remove(t_index);
							RefreshPushList(m_currentBber);
							m_pushContent.RefreshEmail(null);
						}
					},null);
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
		
		if(m_signature.getText().length() > 200){
			Yuchsign.PopupPrompt("签名不能大于200个字符", this);
			return;
		}
		
		m_currentBber.SetConvertSimpleChar(m_convertToSimple.getValue());
		m_currentBber.SetUsingSSL(m_usingSSL.getValue());
		m_currentBber.SetSignature(m_signature.getText());
		
		if(m_currentBber.GetEmailList().isEmpty()){
			Yuchsign.PopupPrompt("没有推送账户，无法同步，请先添加推送账户。", this);
			selectTab(1);
			return;
		}
		
		Yuchsign.PopupWaiting("正在同步，可能需要3-5分钟，请耐心等待。", this);
		
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
			
			Yuchsign.PopupPrompt("同步成功！可以使用手机连接服务器了。\n注意：如果手机没有连接服务器的时间超过3天，\n就需要再次同步。", _panel);
			Yuchsign.HideWaiting();
			
			try{
				m_currentBber.InputXMLData(_result);
			}catch(Exception e){
				PopupProblemAndSearchHelp(e.getMessage(), _panel);
			}
			
			ShowYuchbberData(m_currentBber);			
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
