package com.yuchting.yuchberry.server.frame;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileOutputStream;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
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
		
		addWindowListener(new java.awt.event.WindowAdapter() {
		    public void windowClosing(java.awt.event.WindowEvent e) {
		    	mainFrame t_frame = (mainFrame)e.getWindow();
		    	t_frame.CloseProcess();
		    }
		});

		getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));
		
		m_addAccount = new JButton("+ 添加账户");
		m_addAccount.setVerticalTextPosition(AbstractButton.CENTER);
		m_addAccount.setHorizontalTextPosition(AbstractButton.LEADING);
		m_addAccount.setAlignmentX(Component.LEFT_ALIGNMENT);
		m_addAccount.setToolTipText("添加一个新的yuchberry账户");
		m_addAccount.addActionListener(this);
		
		getContentPane().add(m_addAccount);		
		
		m_stateLabel = new JLabel("state:");
		m_stateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		getContentPane().add(m_stateLabel);
		
		m_accountTable = new accountTable(this);
		getContentPane().add(new JScrollPane(m_accountTable));
		
		setVisible(true);
		
		new checkStateThread(this);
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
		
		StoreAccountInfo();
		
		return true;
	}
	
	public void DelAccoutThread(String _accountName){
		for(int i = 0;i < m_accountList.size();i++){
			fetchThread t_fetch = (fetchThread)m_accountList.elementAt(i);
			if(_accountName.equals(t_fetch.m_fetchMgr.GetAccountName())){
				
				t_fetch.Destroy();
				
				m_accountList.remove(t_fetch);
				m_accountTable.DelAccount(t_fetch);
				
				StoreAccountInfo();
				
				break;
			}
		}
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
	
	public void CloseProcess(){
		StoreAccountInfo();
	}
	
	public void StoreAccountInfo(){
		try{
			FileOutputStream t_file = new FileOutputStream("account.info");
			for(int i = 0;i < m_accountList.size();i++){
				fetchThread t_thread = (fetchThread)m_accountList.elementAt(i);
				t_file.write((t_thread.m_fetchMgr.GetAccountName() + "," + t_thread.m_formerTimer + "," + t_thread.m_expiredTime + "\r\n").getBytes("GB2312"));
			}
			t_file.flush();
			t_file.close();
			
		}catch(Exception e){
			JOptionPane.showMessageDialog(this,"服务器账户数据保存出错：" + e.getMessage() , "错误", JOptionPane.ERROR_MESSAGE);
		}	
	}
	
	public synchronized void RefreshState(){
		int t_connectNum = 0;
		
		for(int i = 0;i < m_accountList.size();i++){
			fetchThread t_thread = (fetchThread)m_accountList.elementAt(i);
			
			if(!t_thread.m_pauseState){
				t_connectNum++;
			}
		}
		
		m_stateLabel.setText("连接账户数：" + t_connectNum + "/" + m_accountList.size());
		
		m_accountTable.RefreshState();
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
