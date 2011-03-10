package com.yuchting.yuchberry.yuchsign.client;

import java.util.Date;
import java.util.Vector;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
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


class ContentTab extends TabPanel{
	
	final TextBox			m_account	= new TextBox();
	final PasswordTextBox	m_password	= new PasswordTextBox();
	
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
		
		public commonConfig(String _parserLine){
			String[] t_data = _parserLine.split(",");
						
			m_name 			= t_data[0];
			m_protocalName 	= t_data[1];
			m_host 			= t_data[2];
			m_port 			= t_data[3];
			m_host_send		= t_data[4];
			m_port_send		= t_data[5];			
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
		}
	}
	
	final commonConfig[]		m_commonConfigList = 
	{
		new commonConfig("Gmail IMAP SSL,imaps,imap.gmail.com,993,smtp.gmail.com,587"),
		new commonConfig("163邮箱IMAP,imap,imap.163.com,143,smtp.163.com,25"),
		new commonConfig("126邮箱IMAP,imap,imap.126.com,143,smtp.126.com,25"),
		new commonConfig("QQ 邮箱 POP3,pop3,pop.qq.com,110,smtp.qq.com,25"),
		new commonConfig("雅虎邮箱 POP3,pop3,pop.alibaba.com.cn,110,smtp.alibaba.com.cn,25"),
		new commonConfig("Hotmail POP3 SSL,pop3s,pop3.live.com,995,smtp.live.com,587"),
		new commonConfig("新浪邮箱 POP3,pop3,pop.sina.com,110,smtp.sina.com,25"),
		new commonConfig("139 邮箱 POP3,pop3,pop.139.com,110,smtp.139.com,25"),
		new commonConfig("TOM 邮箱 POP3,pop3,pop.tom.com,110,smtp.tom.com,25"),
		new commonConfig("21CN 邮箱 POP3,pop3,pop.21cn.com,110,smtp.21cn.com,25"),
		new commonConfig("Foxmail POP3,pop3,pop.foxmail.com,110,smtp.foxmail.com,25")
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
		
		KeyPressHandler t_socketPortHandler = new KeyPressHandler() {
			
			@Override
			public void onKeyPress(KeyPressEvent event){
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
		
		
		m_port.addKeyPressHandler(t_socketPortHandler);
		m_port_send.addKeyPressHandler(t_socketPortHandler);
		
		// the IE and firefox is Compatible but Chrome
		m_port.setStyleName("gwt-TextBox-SocketPort");
		m_port_send.setStyleName("gwt-TextBox-SocketPort");
		
		
		final FlowPanel t_subPane =new FlowPanel();
		
		BberPanel.AddLabelWidget(t_subPane, "邮件地址:", m_account);
		BberPanel.AddLabelWidget(t_subPane, "邮件密码:", m_password);

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
	
	public void AutoSelHost(){
		String t_account = m_account.getText().toLowerCase();
		final int t_atIndex = t_account.indexOf('@');
		if(t_atIndex != -1){
			
			String t_addr = t_account.substring(t_atIndex + 1);
			if(t_addr.indexOf('.') != -1){
				
				for(commonConfig t_config: m_commonConfigList){

					if(t_config.m_host.indexOf(t_addr) != -1){
						t_config.SetConfig(this);
						
						break;
					}
				}
			}
		}
	}
	
	public yuchEmail AddAccount(){
		
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
		
		yuchEmail t_email = new yuchEmail();
		t_email.m_appendHTML 		= m_appendHTML.getValue();
		t_email.m_fullnameSignIn	= m_usingFullname.getValue();
		
		t_email.m_emailAddr			= m_account.getText();
		t_email.m_password			= m_password.getText();
		
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
			
			m_host.setText("");
			m_port.setText("");
			
			m_host_send.setText("");
			m_port_send.setText("");
			
			m_protocal[0].setValue(true);
		}
	}
}

public class BberPanel extends TabPanel{

	final Label m_signinName 		= new Label();
	final Label m_connectHost		= new Label();
	final Label m_serverPort 		= new Label();	
	final Label m_pushInterval 		= new Label();
	final Label m_createTime		= new Label();
	final Label m_endTime		= new Label();
	
	final CheckBox m_usingSSL		= new CheckBox("使用SSL");
	final CheckBox m_convertToSimple = new CheckBox("转换繁体到简体");
	
	final TextArea m_signature		= new TextArea();
		
	final ListBox	m_pushList		= new ListBox(true);
	
	final Button	m_addPushAccountBut = new Button("添加");
	final Button	m_delPushAccountBut = new Button("删除");
	
	final ContentTab m_pushContent	= new ContentTab();
			
	yuchbber m_currentBber = null;
	
	public BberPanel(){
		
		AddAccountAttr();
		AddPushAttr();
		selectTab(0);
		
		RootPanel.get("mainTab").add(this);
		
	}
	
	private void AddAccountAttr(){
		
		m_signature.setPixelSize(420,100);
		
		final VerticalPanel t_attrPane = new VerticalPanel();
		final FlexTable  t_layout = new FlexTable();
		
		int t_line = 0;
		AddLabelWidget(t_layout,"用户名:",m_signinName,t_line++);
		AddLabelWidget(t_layout,"充值时间:",m_createTime,t_line++);
		AddLabelWidget(t_layout,"到期时间:",m_endTime,t_line++);
		AddLabelWidget(t_layout,"主机地址:",m_connectHost,t_line++);
		AddLabelWidget(t_layout,"端口:",m_serverPort,t_line++);
		AddLabelWidget(t_layout,"推送间隔:",m_pushInterval,t_line++);
		
		t_attrPane.add(t_layout);
		t_attrPane.add( m_usingSSL);
		t_attrPane.add(m_convertToSimple);
		t_attrPane.add(new HTML( "签名:<br />"));
		t_attrPane.add(m_signature);
		
		add(t_attrPane,"账户属性");
		
	}
	
	private void AddPushAttr(){
		
		m_pushList.setPixelSize(200, 200);
							
		final HorizontalPanel t_horzPane = new HorizontalPanel(); 
		t_horzPane.add(m_pushList);
		
		final VerticalPanel	 t_vertPane = new VerticalPanel();
		t_vertPane.setPixelSize(40, 80);
		t_vertPane.add(m_addPushAccountBut);
		t_vertPane.add(m_delPushAccountBut);
		
		t_horzPane.add(t_vertPane);
		t_horzPane.add(m_pushContent);
		

		add(t_horzPane,"推送列表");		
			
		m_addPushAccountBut.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				yuchEmail t_email = m_pushContent.AddAccount();
				if(t_email != null){
					m_currentBber.GetEmailList().add(t_email);
					RefreshPushList(m_currentBber);
					m_pushContent.RefreshEmail(null);
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
						}
					},null);
				}
			}
		});
	}
	
	public void SetYuchbberData(final yuchbber _bber){
		m_currentBber = _bber;
		
		m_signinName.setText(_bber.GetSigninName());
		m_connectHost.setText(_bber.GetConnectHost());
		
		Date date = new Date(_bber.GetCreateTime());
	    m_createTime.setText(DateTimeFormat.getFormat("yyyy-MM-dd HH:mm").format(date));
	    
	    date.setTime(_bber.GetCreateTime() + _bber.GetUsingHours() * 3600000);
	    m_endTime.setText(DateTimeFormat.getFormat("yyyy-MM-dd HH:mm").format(date));	    
	        
		m_serverPort.setText("" + _bber.GetServerPort());
		
		m_pushInterval.setText("" + _bber.GetPushInterval());
		
		m_usingSSL.setValue(_bber.IsUsingSSL());
		m_convertToSimple.setValue(_bber.IsConvertSimpleChar());
		
		m_signature.setText(_bber.GetSignature());
		
		RefreshPushList(_bber);
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
