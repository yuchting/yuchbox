package com.yuchting.yuchberry.yuchsign.client.account;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;
import com.yuchting.yuchberry.yuchsign.client.Yuchsign;


class BberEmailPanel extends FlowPanel{
	
	final TextBox			m_account	= new TextBox();
	final PasswordTextBox	m_password	= new PasswordTextBox();
	final TextBox			m_username	= new TextBox();
	
	final TextBox			m_host		= new TextBox();
	final TextBox			m_port		= new TextBox();
	
	final ListBox			m_reminderBox = new ListBox(false);
	
	final RadioButton[]		m_protocal	= {
											new RadioButton("protocal","imap"),
											new RadioButton("protocal","imaps"),
											new RadioButton("protocal","pop3"),
											new RadioButton("protocal","pop3s"),
										  };
	
	final TextBox			m_host_send	= new TextBox();
	final TextBox			m_port_send = new TextBox();
	
	final CheckBox			m_usingFullname = new CheckBox("使用全地址作为用户名登录(GoogleApps 邮箱、QQ企业邮箱，hotmail等邮箱需要选择)");
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
		
		public void SetConfig(BberEmailPanel _dlg){
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
		new commonConfig("@live.com,pop3s,pop3.live.com,995,smtp.live.com,587,0,hotmail不支持imap，同时pop3连接时也会有许多问题，会导致推送不正常，不建议使用。"),
		new commonConfig("@sina.com,pop3,pop.sina.com,110,smtp.sina.com,25,0,请使用浏览器访问sina邮箱，确保其pop3选项已经打开\n<a href=\"http://mail.sina.com.cn/help2/client01.html\" target=_blank>查看帮助</a>"),
		new commonConfig("@139.com,imap,imap.139.com,143,smtp.139.com,25,0"),
		new commonConfig("@tom.com,pop3,pop.tom.com,110,smtp.tom.com,25,0"),
		new commonConfig("@21cn.com,pop3,pop.21cn.com,110,smtp.21cn.com,25,0"),
		new commonConfig("@foxmail.com,imap,imap.qq.com,143,smtp.qq.com,25,1,请使用浏览器访问foxmail邮箱，确保其pop3/imap选项已经打开\n<a href=\"http://service.mail.qq.com/cgi-bin/help?subtype=1&id=26&no=308\" target=_blank>查看帮助</a>"),
		new commonConfig("@sohu.com,imap,mail.sohu.com,143,mail.sohu.com,25,0")
	};
	
	commonConfig				m_googleApp = 
		new commonConfig("@gmail.com,imaps,imap.gmail.com,993,smtp.gmail.com,587,1");
	
	commonConfig				m_qqEnterprise = 
		new commonConfig("@qq.com,imap,imap.exmail.qq.com,143,smtp.exmail.qq.com,25,1");
	
	public BberEmailPanel(){
				
		m_account.addValueChangeHandler(new ValueChangeHandler<String>() {
			// this change hander class will be called when this TextBox lost focus
			// is NOT changed when press keyboard
			//
		    @Override
		    public void onValueChange(ValueChangeEvent<String> event) {
		    	AutoSelHost();
		    }
		});
		
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
				
		final FlowPanel t_subPane = this;
		
		BberPanel.AddLabelWidget(t_subPane, "邮件地址:", m_account);
		BberPanel.AddLabelWidget(t_subPane, "邮件密码:", m_password);
		BberPanel.AddLabelWidget(t_subPane,"名字:",m_username);

		final FlowPanel t_subPane1 = new FlowPanel();
		
		m_reminderBox.setWidth("200px");
		m_reminderBox.addItem("一般（填写通用邮件地址时自动填写）");
		m_reminderBox.addItem("Google App 企业邮箱");
		m_reminderBox.addItem("腾讯企业邮箱");
		for(commonConfig cfg:m_commonConfigList){
			m_reminderBox.addItem(cfg.m_name);
		}
		m_reminderBox.addChangeHandler(new ChangeHandler() {
			
			@Override
			public void onChange(ChangeEvent event) {
				int t_selectIndex = m_reminderBox.getSelectedIndex();
				
				switch(m_reminderBox.getSelectedIndex()){
				case 0:
					Yuchsign.PopupPrompt("请在地址栏输入一般信箱地址，程序会自动填写下面内容。", m_account);
					break;
				case 1:
					m_googleApp.SetConfig(BberEmailPanel.this);
					break;
				case 2:
					m_qqEnterprise.SetConfig(BberEmailPanel.this);
					break;
				}
				
				if(t_selectIndex >= 3){
					t_selectIndex -= 3;
					if(t_selectIndex < m_commonConfigList.length ){
						m_commonConfigList[t_selectIndex].SetConfig(BberEmailPanel.this);
					}				
				}
			}
		});
		
		t_subPane1.add(m_reminderBox);
		
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
		
		if(!LoginPanel.IsValidEmail(m_account.getText())){
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
