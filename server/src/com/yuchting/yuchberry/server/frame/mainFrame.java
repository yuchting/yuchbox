package com.yuchting.yuchberry.server.frame;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

class checkStateThread extends Thread{
	mainFrame	m_mainFrame	= null;
	
	public checkStateThread(mainFrame _main){
		m_mainFrame = _main;
		
		start();
	}
	
	public void run(){
		while(true){
			
			try{
				
				try{
					sleep(5000);
				}catch(Exception e){}
				
				m_mainFrame.RefreshState();
				
			}catch(Exception ex){}
		}
		
		
	}
}

public class mainFrame extends JFrame implements ActionListener{
	
	final static int 	fsm_width 	= 800;
	final static int	fsm_height	= 600;
	
	JButton		m_addAccount		= null;
	JLabel		m_stateLabel		= null;

	accountTable m_accountTable		= null;
	
	Vector		m_accountList		= new Vector();
	
	String		m_formerHost		= new String("imap.gmail.com");
	int			m_formerHost_port	= 993;
	
	String		m_formerHost_send		= new String("smtp.gmail.com");
	int			m_formerHost_port_send	= 587;
	
	int			m_formerServer_port		= 9716;
	int			m_pushInterval			= 10;
	int			m_expiredTime			= 0;
	
	static public void main(String _arg[]){
		new mainFrame();
	}
	
	public mainFrame(){
		setTitle("yuchberry setting frame");
		setSize(fsm_width,fsm_height);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));
		
		m_addAccount = new JButton("+ 添加账户");
		m_addAccount.setVerticalTextPosition(AbstractButton.CENTER);
		m_addAccount.setHorizontalTextPosition(AbstractButton.LEADING);
		m_addAccount.setAlignmentX(Component.CENTER_ALIGNMENT);  
		m_addAccount.setToolTipText("添加一个新的yuchberry账户");
		m_addAccount.addActionListener(this);
		
		getContentPane().add(m_addAccount);		
		
		m_stateLabel = new JLabel("state:");
		getContentPane().add(m_stateLabel);
		
		m_accountTable = new accountTable(this);
		getContentPane().add(new JScrollPane(m_accountTable));
		
		setVisible(true);
	}
	
	public fetchThread SearchAccountThread(String _accountName,int _port){
		
		for(int i = 0;i < m_accountList.size();i++){
			fetchThread t_fetch = (fetchThread)m_accountList.elementAt(i);
			if(t_fetch.m_fetchMgr.GetAccountName().equals(_accountName) 
			|| t_fetch.m_fetchMgr.GetServerPort() == _port){
				
				return t_fetch;
			}
		}
		
		return null;
	}
	
	public boolean AddAccountThread(fetchThread _thread){
		
		for(int i = 0;i < m_accountList.size();i++){
			fetchThread t_fetch = (fetchThread)m_accountList.elementAt(i);
			if(t_fetch.m_fetchMgr.GetAccountName().equals(_thread.m_fetchMgr.GetAccountName())){
				return false;
			}
		}
		
		m_accountList.addElement(_thread);
		m_accountTable.AddAccount(_thread);
		
		return true;
	}
	
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == m_addAccount){
			// open the dialog for creating account of yuchberry
			//
			new createDialog(this,m_formerHost,"" + m_formerHost_port,m_formerHost_send,
								"" + m_formerHost_port_send,GetRandomPassword(),
								"" + (m_formerServer_port++),"" + m_pushInterval,"" + m_expiredTime);
		}
		
	}
	
	public synchronized void RefreshState(){
		
	}
	
	static public String GetRandomPassword(){
		String t_pass = new String();
		
		final int ft_maxPasswordBit = 8;
		
		for(int i = 0;i < ft_maxPasswordBit;i++){
			final int style = (int)(Math.random() * 1000) % 3;
			if(style == 0){
				t_pass = t_pass + (char)((int)'0' + ((int)(Math.random() * 1000) % 10));
			}else if(style == 1){
				t_pass = t_pass + (char)((int)'a' + ((int)(Math.random() * 1000) % 26));
			}else{
				t_pass = t_pass + (char)((int)'A' + ((int)(Math.random() * 1000) % 26));
			}
		}
		
		return t_pass;
	}

}
