package com.yuchting.yuchberry.server.frame;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

class NumberMaxMinLimitedDmt extends PlainDocument {
	 
	private int 		m_max;
	private JTextField	m_ownText;
   
	public NumberMaxMinLimitedDmt(int max,JTextField _text) {
		super();
	   
		m_ownText = _text;
		m_max = max;
	}  
   
	public void insertString(int offset, String  str, AttributeSet attr) throws BadLocationException {   
		if(str == null){
			return;
		}
		if(str.charAt(0) < '0' || str.charAt(0) > '9'){
			return;
		}
	   
		String t_current = m_ownText.getText().substring(0,offset) + str + m_ownText.getText().substring(offset);
	   
		if(Integer.valueOf(t_current).intValue() <= m_max){
		   
			char[] upper = str.toCharArray();
			int length = 0;
	       
			for(int i = 0; i < upper.length; i++) {       
				if(upper[i]>='0'&&upper[i]<='9'){           
					upper[length++] = upper[i];
				}
			}
		       
			super.insertString(offset, new String(upper,0,length), attr);
		}
	}
	       
}

public class createDialog extends JDialog implements DocumentListener,ActionListener{
	
	final static int		fsm_width = 300;
	final static int		fsm_height = 600;
	
	JComboBox	m_commonConfigList = new JComboBox();
	DefaultComboBoxModel m_commonConfigListModel = new DefaultComboBoxModel();
	
	JTextField 	m_account		= new JTextField();
	JTextField 	m_password		= new JTextField();
	JTextField 	m_host			= new JTextField();
	JTextField 	m_port			= new JTextField();
	
	ButtonGroup m_protocalGroup = new ButtonGroup();
	JRadioButton[]	m_protocal	= new JRadioButton[]{
									new JRadioButton("imap"),
									new JRadioButton("imaps"),
									new JRadioButton("pop3"),
									new JRadioButton("pop3s"),
									};
	
	JTextField 	m_send_host			= new JTextField();
	JTextField 	m_send_port			= new JTextField();
	
	
	JTextField	m_userPassword		= new JTextField();
	JTextField	m_serverPort		= new JTextField();
	
	JTextField	m_pushInterval		= new JTextField();
	
	JCheckBox	m_useSSL			= new JCheckBox("使用SSL加密");
	JCheckBox	m_convertToSimple	= new JCheckBox("转换繁体为简体");
	
	JTextArea	m_signature			= new JTextArea();
	
	JButton		m_confirmBut		= new JButton("确定");
	
	public createDialog(mainFrame _main,String _formerHost,String _formerPort,
										String _formerHost_send,String _formerPort_send,
										String _userPassword,String _serverPort,String _pushInterval){
		
		super(_main,"添加一个账户",true);
				
		setResizable(false);
		getContentPane().setLayout(new FlowLayout());
		
		setSize(fsm_width,fsm_height);
		setLocation(_main.getLocation().x + (_main.getWidth()- fsm_width) / 2,
					_main.getLocation().y + (_main.getHeight() -  fsm_height) / 2);
		

		m_commonConfigList.setModel(m_commonConfigListModel);
		m_pushInterval.setDocument(new NumberMaxMinLimitedDmt(3600,m_pushInterval));
		m_serverPort.setDocument(new NumberMaxMinLimitedDmt(20000,m_serverPort));
		m_port.setDocument(new NumberMaxMinLimitedDmt(20000,m_port));
		m_send_port.setDocument(new NumberMaxMinLimitedDmt(20000,m_send_port));
		
		m_host.getDocument().addDocumentListener(this);
		m_signature.setLineWrap(true);
		m_signature.setBorder(BorderFactory.createLineBorder(Color.gray,1));		
		
		JLabel t_label = new JLabel("常用配置：");
		t_label.setPreferredSize(new Dimension(80,25));
		getContentPane().add(t_label);
		m_commonConfigList.setPreferredSize(new Dimension(200, 25));
		getContentPane().add(m_commonConfigList);
		
		JSeparator	t_separator			= new JSeparator();
		t_separator.setPreferredSize(new Dimension(fsm_width, 5));
		getContentPane().add(t_separator);
		
		AddTextLabel("帐号名称:",m_account,220,"");
		AddTextLabel("帐号密码:",m_password,220,"");
		AddTextLabel("主机地址:",m_host,120,_formerHost);
		AddTextLabel("端口:",m_port,60,_formerPort);

		getContentPane().add(new JLabel("协议:"));
		for(int i = 0;i < m_protocal.length;i++){
			m_protocalGroup.add(m_protocal[i]);
			getContentPane().add(m_protocal[i]);
		}
		
		AddTextLabel("发送主机地址:",m_send_host,100,_formerHost_send);
		AddTextLabel("端口:",m_send_port,60,_formerPort_send);
		
		t_separator = new JSeparator();
		t_separator.setPreferredSize(new Dimension(fsm_width, 5));
		getContentPane().add(t_separator);
		
		AddTextLabel("用户密码:",m_userPassword,100,_userPassword);
		AddTextLabel("用户端口:",m_serverPort,60,_serverPort);
		
		AddTextLabel("推送间隔（秒）：",m_pushInterval,180,_pushInterval);
		m_useSSL.setPreferredSize(new Dimension(fsm_width - 20, 25));
		getContentPane().add(m_useSSL);
		
		m_convertToSimple.setPreferredSize(new Dimension(fsm_width - 20,25));
		getContentPane().add(m_convertToSimple);
		
		t_label = new JLabel("签名：");
		t_label.setPreferredSize(new Dimension(fsm_width - 20,25));
		
		getContentPane().add(t_label);
		m_signature.setPreferredSize(new Dimension(fsm_width - 15, 170));
		getContentPane().add(m_signature);
		
		t_separator			= new JSeparator();
		t_separator.setPreferredSize(new Dimension(fsm_width, 5));
		getContentPane().add(t_separator);
		
		getContentPane().add(m_confirmBut);
		m_confirmBut.addActionListener(this);
		
		AutoSelectProtocal();
		
		AddCommonConfigList();
		
		setVisible(true);	
	}
	
	//@{ DocumentListener for JTextField change
	public void changedUpdate(DocumentEvent e){
		
	}
	public void insertUpdate(DocumentEvent e){
		AutoSelectProtocal();
	}
	public void removeUpdate(DocumentEvent e){
		AutoSelectProtocal();
	}
	//@}
	
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == m_confirmBut){
//			JTextField 	m_account		= new JTextField();
//			JTextField 	m_password		= new JTextField();
//			JTextField 	m_host			= new JTextField();
//			JTextField 	m_port			= new JTextField();
//			
//			ButtonGroup m_protocalGroup = new ButtonGroup();
//			JRadioButton[]	m_protocal	= new JRadioButton[]{
//											new JRadioButton("imap"),
//											new JRadioButton("imaps"),
//											new JRadioButton("pop3"),
//											new JRadioButton("pop3s"),
//											};
//			
//			JTextField 	m_send_host			= new JTextField();
//			JTextField 	m_send_port			= new JTextField();
//			
//			
//			JTextField	m_userPassword		= new JTextField();
//			JTextField	m_serverPort		= new JTextField();
//			
//			JTextField	m_pushInterval		= new JTextField();
//			
//			JCheckBox	m_useSSL			= new JCheckBox("使用SSL加密");
//			JCheckBox	m_convertToSimple	= new JCheckBox("转换繁体为简体");
//			
//			JTextArea	m_signature			= new JTextArea();
//			
//			JButton		m_confirmBut		= new JButton("确定");
			if(m_account.getText().length() == 0 || m_password.getText().length() == 0){
				JOptionPane.showMessageDialog(null, "账户名、密码不能为空", "错误", JOptionPane.ERROR_MESSAGE);
			}
			
			if(m_host.getText().length() == 0 || m_port.getText().length() == 0){
				JOptionPane.showMessageDialog(null, "邮件接受服务器地址、端口不能为空", "错误", JOptionPane.ERROR_MESSAGE);
			}
			
			if(m_send_host.getText().length() == 0 || Integer.valueOf(m_send_port.getText()).intValue() <= 0){
				JOptionPane.showMessageDialog(null, "邮件发送服务器地址不能为空，端口非法", "错误", JOptionPane.ERROR_MESSAGE);
			}
			
			final int t_listenPort = 3000;
			if(m_userPassword.getText().length() == 0 || Integer.valueOf(m_serverPort.getText()).intValue() <= t_listenPort){
				JOptionPane.showMessageDialog(null, "用户密码不能为空，监听yuchberry端口不能小于" + t_listenPort, "错误", JOptionPane.ERROR_MESSAGE);
			}
			
			
			
			setVisible(false);
			dispose();
		}		
	}
	
	private void AddTextLabel(String _label,JTextField _text,int _length,String _defaultVal){
		JLabel t_label = new JLabel(_label);
		getContentPane().add(t_label);
		_text.setPreferredSize(new Dimension(_length, 25));
		
		if(_defaultVal.length() != 0){
			_text.setText(_defaultVal);
		}
		
		getContentPane().add(_text);
		
	}
	
	private void AutoSelectProtocal(){
		
		String t_host = m_host.getText().toLowerCase();
		int t_selectIndex = 0;
		if(t_host.indexOf("imap") != -1){
			t_selectIndex = 0;
		}else if(t_host.indexOf("pop3") != -1){
			t_selectIndex = 2;
		}
		
		m_protocal[t_selectIndex].setSelected(true);
		
	}
	
	private void AddCommonConfigList(){
		m_commonConfigListModel.addElement("自定义");
		m_commonConfigListModel.addElement("hahah");
		m_commonConfigListModel.addElement("xxxx");
	}
}
