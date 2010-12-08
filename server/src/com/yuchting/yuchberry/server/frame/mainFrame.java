package com.yuchting.yuchberry.server.frame;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import com.yuchting.yuchberry.server.fetchMgr;

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
	
	JPopupMenu 	m_contextMenu			= new JPopupMenu();
	JMenuItem	m_checkAccountItem		= new JMenuItem("检查帐户");
	JMenuItem	m_pauseAccountItem		= new JMenuItem("暂停");
	JMenuItem	m_continueAccountItem	= new JMenuItem("继续");
		
	loadDialog	m_loadDialog			= null;
	
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
				
		ConstructContextMenu();
		
		setVisible(true);
		
		LoadStoreAccountInfo();
		
		new checkStateThread(this);
	}
	
	public void LoadStoreAccountInfo(){
		
		Thread t_load = new Thread(){
			public void run(){
				mainFrame t_mainFrame = (mainFrame)JFrame.getFrames()[0];
				try{
					
					File t_file = new File("account.info");
					if(t_file.exists()){
						BufferedReader in = new BufferedReader(
												new InputStreamReader(
													new FileInputStream(t_file)));

						String line = null;
						Vector t_lineContain = new Vector();
						
						while((line = in.readLine())!= null){
							if(!fetchMgr.IsEmptyLine(line)){
								t_lineContain.addElement(line);
							}
						}
						t_mainFrame.m_loadDialog.m_progress.setMaximum(t_lineContain.size());
						
						for(int i = 0;i < t_lineContain.size();i++){
							line = (String)t_lineContain.elementAt(i);
							String t_data[] = line.split(",");
							String t_prefix = t_data[0] + "/";
							
							try{
								t_mainFrame.m_loadDialog.m_state.setText("一共有" + t_lineContain.size()  + "个用户，正在载入第" + (i + 1) + "个用户：");
								t_mainFrame.m_loadDialog.m_state1.setText(t_data[0]);
								
								fetchThread t_thread = new fetchThread(t_prefix,t_prefix + "config.ini",
											Long.valueOf(t_data[1]).longValue(),Long.valueOf(t_data[2]).longValue());
				
								t_mainFrame.AddAccountThread(t_thread,false);
								
								if(m_formerServer_port <= t_thread.m_fetchMgr.GetServerPort()){
									m_formerServer_port = t_thread.m_fetchMgr.GetServerPort() + 1;
								}
								
								t_mainFrame.m_loadDialog.m_progress.setValue(i + 1);
								
							}catch(Exception e){
								JOptionPane.showMessageDialog(t_mainFrame,"服务器账户连接错误：" + e.getMessage() , "错误", JOptionPane.ERROR_MESSAGE);
							}
						}
					}					
					
					t_mainFrame.m_loadDialog.setVisible(false);
					t_mainFrame.m_loadDialog.dispose();
					
				}catch(Exception e){
					JOptionPane.showMessageDialog(t_mainFrame,"服务器账户数据读取出错：" + e.getMessage() , "错误", JOptionPane.ERROR_MESSAGE);
				}
			}
		};
		
		m_loadDialog = new loadDialog(this);
		
		t_load.start();
		
		m_loadDialog.setVisible(true);		
	}
	
	public void ConstructContextMenu(){
		m_contextMenu.add(m_checkAccountItem);
		m_checkAccountItem.addActionListener(this);
		
		m_contextMenu.add(new JSeparator());
		
		m_contextMenu.add(m_pauseAccountItem);
		m_pauseAccountItem.addActionListener(this);
		
		m_contextMenu.add(m_continueAccountItem);
		m_continueAccountItem.addActionListener(this);
		
		m_accountTable.addMouseListener(new MouseListener() {
			
			public void mouseReleased(MouseEvent e) {
				
			}
			
			public void mousePressed(MouseEvent e) {
		
			}
			
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			public void mouseClicked(MouseEvent e) {

				if ( SwingUtilities.isRightMouseButton( e )){
					
					// TODO Auto-generated method stub
					mainFrame t_mainFrame = (mainFrame)JFrame.getFrames()[0];
										
					// get the coordinates of the mouse click
					Point p = e.getPoint();
		 
					// get the row index that contains that coordinate
					final int rowNumber = t_mainFrame.m_accountTable.rowAtPoint( p );
		 
					// Get the ListSelectionModel of the JTable
					ListSelectionModel model = t_mainFrame.m_accountTable.getSelectionModel();
		 
					// set the selected interval of rows. Using the "rowNumber"
					// variable for the beginning and end selects only that one row.
					model.setSelectionInterval( rowNumber, rowNumber );	
					
					if(rowNumber != -1){
						t_mainFrame.TableMouseEvent(e,rowNumber);
					}										
				}
			}
		});
	}
	
	public void TableMouseEvent(MouseEvent e,int _row){
		m_contextMenu.show(e.getComponent(),
							e.getX(), e.getY());
		
		fetchThread t_thread = (fetchThread)m_accountList.elementAt(_row);
		
		m_continueAccountItem.setEnabled(t_thread.m_pauseState);
		m_pauseAccountItem.setEnabled(!t_thread.m_pauseState);		
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
	
	public boolean AddAccountThread(fetchThread _thread,boolean _storeAccountInfo){
		
		for(int i = 0;i < m_accountList.size();i++){
			fetchThread t_fetch = (fetchThread)m_accountList.elementAt(i);
			if(t_fetch.m_fetchMgr.GetAccountName().equals(_thread.m_fetchMgr.GetAccountName())){
				return false;
			}
		}
		
		m_accountList.addElement(_thread);
		m_accountTable.AddAccount(_thread);
		
		if(_storeAccountInfo){
			StoreAccountInfo();
		}
		
		return true;
	}
	
	public void DelAccoutThread(String _accountName,boolean _storeAccountInfo){
		
		for(int i = 0;i < m_accountList.size();i++){
			
			fetchThread t_fetch = (fetchThread)m_accountList.elementAt(i);
			if(_accountName.equals(t_fetch.m_fetchMgr.GetAccountName())){
				
				t_fetch.Destroy();
				
				m_accountList.remove(t_fetch);
				m_accountTable.DelAccount(t_fetch);
				
				if(_storeAccountInfo){
					StoreAccountInfo();
				}				
				
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
			
		}else{
			final int t_selectIndex = m_accountTable.getSelectedRow();
			if(t_selectIndex != -1){
				fetchThread t_thread = (fetchThread)m_accountList.elementAt(t_selectIndex);
				
				if(e.getSource() == m_continueAccountItem){
					t_thread.Reuse();
				}else if(e.getSource() == m_pauseAccountItem){
					t_thread.Pause();
				}
				
				RefreshState();
			}
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
				t_file.write((t_thread.m_fetchMgr.GetAccountName() + "," + t_thread.m_expiredTime + "," + t_thread.m_formerTimer + "\r\n").getBytes("GB2312"));
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
