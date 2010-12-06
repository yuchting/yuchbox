package com.yuchting.yuchberry.server.frame;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.ButtonGroup;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
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

public class createDialog extends JDialog implements DocumentListener{
	
	final static int		fsm_width = 300;
	final static int		fsm_height = 500;
	
	JLabel		m_accountLable	= new JLabel("帐号名称:");
	JTextField 	m_account		= new JTextField("");
	
	JLabel		m_passwordLable	= new JLabel("帐号密码:");
	JTextField 	m_password		= new JTextField("");
	
	JLabel		m_hostLable		= new JLabel("主机地址:");
	JTextField 	m_host			= new JTextField("");
	
	JLabel		m_portLable		= new JLabel("端口:");
	JTextField 	m_port			= new JTextField("");
	
	ButtonGroup m_protocalGroup = new ButtonGroup();
	JRadioButton[]	m_protocal	= new JRadioButton[]{
									new JRadioButton("imap"),
									new JRadioButton("imaps"),
									new JRadioButton("pop3"),
									new JRadioButton("pop3s"),
									};
	
	JLabel		m_send_hostLabel	= new JLabel("发送主机地址:");
	JTextField 	m_send_host			= new JTextField("");
	
	JLabel		m_send_portLabel	= new JLabel("端口:");
	JTextField 	m_send_port			= new JTextField("");
	
	
	public createDialog(mainFrame _main,String _formerHost,String _formerPort,
										String _formerHost_send,String _formerPort_send){
		
		super(_main,"添加一个账户",true);
				
		setResizable(false);
		
		getContentPane().setLayout(new FlowLayout());
		
		setSize(fsm_width,fsm_height);
		setLocation(_main.getLocation().x + (_main.getWidth()- fsm_width) / 2,
					_main.getLocation().y + (_main.getHeight() -  fsm_height) / 2);
		
		AddTextLabel(m_accountLable,m_account,220,"");
		AddTextLabel(m_passwordLable,m_password,220,"");
		AddTextLabel(m_hostLable,m_host,120,_formerHost);
		AddTextLabel(m_portLable,m_port,60,_formerPort);
		
		getContentPane().add(new JSeparator(),BorderLayout.LINE_START);
		
		getContentPane().add(new JLabel("协议:"));
		for(int i = 0;i < m_protocal.length;i++){
			m_protocalGroup.add(m_protocal[i]);
			getContentPane().add(m_protocal[i]);
		}
		
		AddTextLabel(m_send_hostLabel,m_send_host,100,_formerHost_send);
		AddTextLabel(m_send_portLabel,m_send_port,60,_formerPort_send);
		
		getContentPane().add(new JSeparator());
				
		m_port.setDocument(new NumberMaxMinLimitedDmt(20000,m_port));
		m_send_port.setDocument(new NumberMaxMinLimitedDmt(20000,m_send_port));
		
		m_host.getDocument().addDocumentListener(this);
				
		AutoSelectProtocal();
		
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
	
	private void AddTextLabel(JLabel _label,JTextField _text,int _length,String _defaultVal){
		
		getContentPane().add(_label);
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
}
