package com.yuchting.yuchberry.server.frame;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

import javax.mail.Session;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.sun.mail.smtp.SMTPTransport;

public class broadcastDialog extends JDialog implements ActionListener{
	
	final static int		fsm_width = 520;
	final static int		fsm_height = 320;
	
	JTextField	m_username		= new JTextField();
	JTextField	m_host			= new JTextField();
	JTextField	m_port			= new JTextField();
	
	JTextArea	m_text			= new JTextArea();
	JButton		m_confirmBut	= new JButton("确定");
	
	mainFrame	m_mainFrame		= null;
	
	public broadcastDialog(mainFrame _main){
		super(_main,"广播消息",false);
		m_mainFrame = _main;
		
		setResizable(false);
		
		getContentPane().setLayout(new FlowLayout(FlowLayout.LEFT));
				
		setSize(fsm_width,fsm_height);
		setLocation(_main.getLocation().x + (_main.getWidth()- fsm_width) / 2,
					_main.getLocation().y + (_main.getHeight() -  fsm_height) / 2);
		
		createDialog.AddTextLabel(getContentPane(),"SMTP用户名（留空就是使用用户自己的帐户发）：",m_username,100,"");
		createDialog.AddTextLabel(getContentPane(),"SMTP主机：",m_host,80,"");
		createDialog.AddTextLabel(getContentPane(),"SMTP端口：",m_port,50,"");
		
		m_text.setLineWrap(true);
		m_text.setBorder(BorderFactory.createLineBorder(Color.gray,1));
		m_text.setPreferredSize(new Dimension(fsm_width - 15, 120));
		getContentPane().add(m_text);
		
		setVisible(true);
	}
	
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == m_confirmBut){
			if(!m_username.getText().isEmpty()){
				try{
					// initialize the smtp transfer
			    	//
					Properties t_sysProps_send = System.getProperties();
					t_sysProps_send.put("mail.smtp.auth", "true");
					t_sysProps_send.put("mail.smtp.port", m_port.getText());
			    			
					t_sysProps_send.put("mail.smtp.starttls.enable","true");
			    	
			    	
			    	Session t_session_send = Session.getInstance(t_sysProps_send, null);
			    	t_session_send.setDebug(false);
			    	
			    	SMTPTransport t_sendTransport = (SMTPTransport)m_session_send.getTransport(m_protocol_send);
			    	
			    	// test connected
			    	//
			    	m_sendTransport.connect(m_host_send,m_port_send,m_userName,m_password);
			        	    		
			    	}
				}catch(Exception e){
					
				}
				
			}
		}
	}
}
