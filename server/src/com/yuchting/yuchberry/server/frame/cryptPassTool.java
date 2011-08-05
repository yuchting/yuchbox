package com.yuchting.yuchberry.server.frame;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import com.yuchting.yuchberry.server.cryptPassword;

public class cryptPassTool extends JFrame implements ActionListener{

	JTextField	m_cryptKey		= new JTextField();
	JTextField	m_orgPass		= new JTextField();
	JTextField	m_cryptPass		= new JTextField();
	
	JButton		m_convert		= new JButton("转换");
	JButton		m_help			= new JButton("帮助");
	
	public cryptPassTool(){
		
		setTitle("加密密码工具");
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		
		setResizable(false);
		
		
		Container t_con = getContentPane();
		
		t_con.setLayout(new FlowLayout(FlowLayout.CENTER));
		
		m_cryptPass.setEditable(false);
		
		createDialog.AddTextLabel(t_con,"加密算子:",m_cryptKey,300,"");
		createDialog.AddTextLabel(t_con,"明文密码:",m_orgPass,300,"");
		createDialog.AddTextLabel(t_con,"加密密码:",m_cryptPass,300,"");
		
		t_con.add(m_convert);
		t_con.add(m_help);
		m_convert.addActionListener(this);
		m_help.addActionListener(this);
		
		setLocationRelativeTo(null);
		
		pack();
		setSize(400,170);
		setVisible(true);
	}
	
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == m_convert){
			if(m_cryptKey.getText().isEmpty()){
				JOptionPane.showMessageDialog(this,"请输入加密算子", "错误", JOptionPane.ERROR_MESSAGE);
				return ;
			}
			
			if(m_orgPass.getText().isEmpty()){
				JOptionPane.showMessageDialog(this,"请输入明文密码", "错误", JOptionPane.ERROR_MESSAGE);
				return ;
			}
			
			try{
				cryptPassword t_crypt = new cryptPassword(cryptPassword.md5(m_cryptKey.getText()));
				m_cryptPass.setText(t_crypt.encrypt(m_orgPass.getText()));
				
			}catch(Exception _e){
				JOptionPane.showMessageDialog(this,_e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
			}
			

		}else if(e.getSource() == m_help){
			try{
				mainFrame.OpenURL("http://code.google.com/p/yuchberry/wiki/Password_Crypt");
			}catch(Exception _e){
				JOptionPane.showMessageDialog(this,_e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
			}
			
		}
	}
	
	static public void main(String _arg[]){
		new cryptPassTool();
	}
	
}
