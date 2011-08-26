package com.yuchting.yuchberry.server.frame;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputListener;

import com.yuchting.yuchberry.server.Logger;
import com.yuchting.yuchberry.server.fakeMDSSvr;
import com.yuchting.yuchberry.server.fetchMain;
import com.yuchting.yuchberry.server.fetchMgr;
import com.yuchting.yuchberry.server.fetchWeibo;

class checkStateThread extends Thread{
	mainFrame	m_mainFrame	= null;
		
	public checkStateThread(mainFrame _main){
		m_mainFrame = _main;
		
		start();
	}
	
	public void run(){
		
		int t_counter = -1;
		
		while(true){
			
			try{
				
				try{
					sleep(240000);
				}catch(Exception e){}
				
				m_mainFrame.RefreshState();
				
				if(t_counter == -1 ||  t_counter > 120 * 2){
					
					t_counter = 0;
					
					URL is_gd = new URL("http://yuchberry.googlecode.com/files/latest_version?a="+(new Random()).nextInt());
					
			        URLConnection yc = is_gd.openConnection();
			        yc.setConnectTimeout(10000);
			        yc.setReadTimeout(50000);
			        BufferedReader in = new BufferedReader(
			                                new InputStreamReader(yc.getInputStream()));
			        
			        String t_version = in.readLine();
			        
			        synchronized (m_mainFrame.m_accountList) {
			        	for(fetchThread t_thread:m_mainFrame.m_accountList){
				        	t_thread.m_fetchMgr.SetLatestVersion(t_version);
				        }
					}

					in.close();
				}
				
				t_counter++;
				
			}catch(Exception ex){}
		}		
		
	}
}

public class mainFrame extends JFrame implements ActionListener{
	
	final static String fsm_accountDataFilename = "account.info";
	
	final static int 	fsm_width 	= 800;
	final static int	fsm_height	= 600;
	
	final static int	fsm_minAccountTableHeight = 200;
	final static int	fsm_maxAccountTableHeight = 600;
	
	JButton		m_addAccount		= new JButton("+ 添加账户");
	JButton		m_sponsor			= new JButton("赞助/技术支持");
	JLabel		m_stateLabel		= new JLabel("state:");

	JTextField	m_searchAccount		= new JTextField();
	JButton		m_searchButton		= new JButton("搜索帐户");
	
	accountTable m_accountTable		= new accountTable(this);
	JScrollPane	m_accountTableScroll= new JScrollPane(m_accountTable);
	
	JSeparator	m_tableSeparator	= new JSeparator();
	boolean	m_isResizeState		= false;
	JTextArea	m_logInfo			= new JTextArea();
	JScrollPane	m_logInfoScroll		= new JScrollPane(m_logInfo);
		
	JPanel		m_accountLogPane	= new JPanel();
	
	Vector<fetchThread>		m_accountList		= new Vector<fetchThread>();
	
	String		m_formerHost		= new String("imap.gmail.com");
	int			m_formerHost_port	= 993;
	
	String		m_formerHost_send		= new String("smtp.gmail.com");
	int			m_formerHost_port_send	= 587;
	
	int			m_pushInterval			= 30;
	long		m_expiredTime			= 0;
	
	JPopupMenu 	m_contextMenu			= new JPopupMenu();
	JMenuItem	m_checkAccountItem		= new JMenuItem("检查帐户");
	JMenuItem	m_delAccountItem		= new JMenuItem("删除帐户");
	JMenuItem	m_pauseAccountItem		= new JMenuItem("暂停");
	JMenuItem	m_continueAccountItem	= new JMenuItem("继续");
	JMenuItem	m_addAccountTime		= new JMenuItem("设置到期时间");
		
	loadDialog	m_loadDialog			= null;
	
	fetchThread	m_currentSelectThread	= null;
	
	NanoHTTPD	m_httpd					= null;
	
	Logger		m_logger				= new Logger("");
	
	int			m_currUsingAccount		= 0;
	int			m_currConnectAccount	= 0;
	
	Vector<fetchThread>	m_checkFolderStateThread = new Vector<fetchThread>();
		
	static public void main(String _arg[])throws Exception{
				
		if(_arg.length >= 1 && _arg[0].equalsIgnoreCase("cryptTool")){
			if(_arg.length == 2 && _arg[1].equalsIgnoreCase("console")){
				new cryptPassTool_c();
			}else{
				new cryptPassTool();
			}
			
		}else if(_arg.length >= 1 && _arg[0].equalsIgnoreCase("weiboReq")){
			if(_arg.length >= 2){
				if(_arg[1].equalsIgnoreCase("sina")){
					(new weiboRequestTool(fetchWeibo.SINA_WEIBO_STYLE)).startAuth();
				}else if(_arg[1].equalsIgnoreCase("tw")){
					(new weiboRequestTool(fetchWeibo.TWITTER_WEIBO_STYLE)).startAuth();
				}else if(_arg[1].equalsIgnoreCase("qq")){
					(new weiboRequestTool(fetchWeibo.QQ_WEIBO_STYLE)).startAuth();
				}
			}else{
				new weiboRequestTool(fetchWeibo.SINA_WEIBO_STYLE);
			}
			
		}else if(_arg.length >= 1 && _arg[0].equalsIgnoreCase("frame")){
			new fakeMDSSvr();
			new mainFrame(_arg);
		}else if(_arg.length >= 1 && _arg[0].equalsIgnoreCase("clear")){
			new ClearAccount();			
		}else{
			new fetchMain();
		}
	}	
	
	public mainFrame(String _arg[]){
		
		boolean t_hideWindow = _arg.length >= 2 && _arg[1].equalsIgnoreCase("hide");
		
		setTitle("yuchberry 集成配置工具 beta");
		setSize(fsm_width,fsm_height);
		
		if(!t_hideWindow){
			Image image = getToolkit().createImage(ClassLoader.getSystemResource("com/yuchting/yuchberry/server/frame/logo.png"));
			setIconImage(image);	
		}				
		
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		
		addWindowListener(new java.awt.event.WindowAdapter() {
		    public void windowClosing(java.awt.event.WindowEvent e) {
		    	  	
		    	if(m_accountList.isEmpty() || JOptionPane.showConfirmDialog(JFrame.getFrames()[0],"真的想关闭所有的用户并退出？", 
		    	"关闭？", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION){
		    		
		    		mainFrame t_frame = (mainFrame)e.getWindow();
			    	t_frame.CloseProcess();
			    	
			    	System.exit(0);
		    	}
		    }
		});

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel,BoxLayout.X_AXIS));

		m_addAccount.setVerticalTextPosition(AbstractButton.CENTER);
		m_addAccount.setToolTipText("添加一个新的yuchberry账户");
		m_addAccount.addActionListener(this);
		
		m_sponsor.setToolTipText("赞助yuchberry项目/付费获得技术支持或者二次开发");
		m_sponsor.addActionListener(this);
		m_sponsor.setVerticalTextPosition(AbstractButton.CENTER);
		
		m_searchAccount.setMaximumSize(new Dimension(200,25));
		
		m_searchButton.addActionListener(this);
		
		panel.add(m_addAccount);
		panel.add(m_sponsor);
		panel.add(m_searchAccount);
		panel.add(m_searchButton);
		
		getContentPane().add(panel,BorderLayout.PAGE_START);
		
		m_accountLogPane.setLayout(new BoxLayout(m_accountLogPane,BoxLayout.Y_AXIS));
		
		m_stateLabel.setPreferredSize(new Dimension(fsm_width * 2,25));
		m_stateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		m_accountLogPane.add(m_stateLabel);
		
		
		m_accountTableScroll.setPreferredSize(new Dimension(fsm_width, 350));
		m_accountLogPane.add(m_accountTableScroll);
		
		m_tableSeparator.setPreferredSize(new Dimension(fsm_width,8));
		m_accountLogPane.add(m_tableSeparator);
		
		m_logInfo.setEditable(false);
		m_logInfoScroll.setPreferredSize(new Dimension(fsm_width, fsm_height));
		m_accountLogPane.add(m_logInfoScroll);
		
		getContentPane().add(m_accountLogPane,BoxLayout.Y_AXIS);
		
		ConstructMoveSeparator();
		ConstructContextMenu();
				
		LoadStoreAccountInfo(t_hideWindow);
				
		new checkStateThread(this);
		
		if(!t_hideWindow){
			// don't set visible
			//
			setVisible(true);
		}	
		
		LoadYuchsign();
	}
			
	public void LoadStoreAccountInfo(final boolean _hideLoad){
		
		Thread t_load = new Thread(){
			public void run(){
				mainFrame t_mainFrame = (mainFrame)JFrame.getFrames()[0];
				try{
					
					File t_file = new File(fsm_accountDataFilename);
					if(t_file.exists()){
						BufferedReader in = new BufferedReader(
												new InputStreamReader(
													new FileInputStream(t_file),"UTF-8"));

						String line = null;
						Vector t_lineContain = new Vector();
						
						while((line = in.readLine())!= null){
							if(!fetchMgr.IsEmptyLine(line)){
								t_lineContain.addElement(line);
							}
						}
						in.close();
						
						t_mainFrame.m_loadDialog.m_progress.setMaximum(t_lineContain.size());
						
						for(int i = 0;i < t_lineContain.size();i++){
							line = (String)t_lineContain.elementAt(i);
							String t_data[] = line.split(",");
							String t_prefix = t_data[0] + "/";
							
							try{
								t_mainFrame.m_loadDialog.m_state.setText("一共有" + t_lineContain.size()  + "个用户，正在载入第" + (i + 1) + "个用户：");
								t_mainFrame.m_loadDialog.m_state1.setText(t_data[0]);
								
								fetchThread t_thread = new fetchThread(new fetchMgr(),t_prefix,
													Long.valueOf(t_data[1]).longValue(),Long.valueOf(t_data[2]).longValue(),false);
								
								if(t_data.length >= 4){
									t_thread.m_clientDisconnectTime = Long.valueOf(t_data[3]).longValue();
								}
				
								t_mainFrame.AddAccountThread(t_thread,false);
															
								t_mainFrame.m_loadDialog.m_progress.setValue(i + 1);
								
																
							}catch(Exception e){
								m_logger.PrinterException(e);
							}
						}
					}					
					

					t_mainFrame.m_loadDialog.setVisible(false);
					t_mainFrame.m_loadDialog.dispose();					
					
				}catch(Exception e){
					m_logger.PrinterException(e);
				}
			}
		};
		
		m_loadDialog = new loadDialog(this);
		
		t_load.start();
		
		if(!_hideLoad){
			m_loadDialog.setVisible(true);
		}
	}
	
	public void StoreAccountInfo(){
		
		m_logger.LogOut("StoreAccountInfo start");
		
		try{
			FileOutputStream t_file = new FileOutputStream(fsm_accountDataFilename);
			
			try{
				StringBuffer t_buffer = new StringBuffer();
				
				synchronized (m_accountList) {
					for(int i = 0;i < m_accountList.size();i++){
						fetchThread t_thread = (fetchThread)m_accountList.elementAt(i);
						t_buffer.append(t_thread.m_fetchMgr.GetAccountName()).append(",")
								.append((t_thread.m_usingHours)).append(",")
								.append(t_thread.m_formerTimer).append(",")
								.append(t_thread.m_clientDisconnectTime).append(",")
								.append("\r\n");
					}
				}
				
				
				t_file.write(t_buffer.toString().getBytes("UTF-8"));
				t_file.flush();
				
			}finally{
				t_file.close();
			}			
			
		}catch(Exception e){
			m_logger.PrinterException(e);
		}
		
		m_logger.LogOut("StoreAccountInfo end");
	}
	
	
	public void ConstructMoveSeparator(){
		
		MouseInputListener t_lister = new MouseInputListener() {
			
			Point startPos = null;
			
			public void mouseMoved(MouseEvent arg0) {
				if(m_tableSeparator.hasFocus()){
					setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
				}
			}
			
			public void mouseDragged(MouseEvent arg0) {
				if(startPos != null){
					
					int t_height = m_accountTableScroll.getPreferredSize().height + (arg0.getY() - startPos.y);
					
					if(t_height > fsm_maxAccountTableHeight){
						t_height = fsm_maxAccountTableHeight;
					}
					
					if(t_height < fsm_minAccountTableHeight){
						t_height = fsm_minAccountTableHeight;
					}
					
					m_accountTableScroll.setPreferredSize(new Dimension(fsm_width,t_height));
					SwingUtilities.updateComponentTreeUI(m_accountLogPane);
				}				
				
			}
			
			public void mouseReleased(MouseEvent arg0) {
				startPos = null;
			}
			
			public void mousePressed(MouseEvent arg0) {
				startPos = arg0.getPoint();
				m_tableSeparator.requestFocus();
			}
			
			public void mouseExited(MouseEvent arg0) {
				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
			
			public void mouseEntered(MouseEvent arg0) {
				setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
			}
			
			public void mouseClicked(MouseEvent arg0) {
								
			}
		};
		
		m_tableSeparator.addMouseMotionListener(t_lister);
		m_tableSeparator.addMouseListener(t_lister);
	}
	public void ConstructContextMenu(){
		
		ActionListener t_menuListener = new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				
				final int t_selectIndex = m_accountTable.getSelectedRow();
				if(t_selectIndex != -1){
					final fetchThread t_thread = (fetchThread)m_accountList.elementAt(t_selectIndex);
					
					if(e.getSource() == m_continueAccountItem){
						try{
							t_thread.Reuse();
						}catch(Exception ex){}
						
					}else if(e.getSource() == m_pauseAccountItem){
						t_thread.Pause();
					}else if(e.getSource() == m_checkAccountItem){
						final String t_configFile = t_thread.m_fetchMgr.GetPrefixString() + fetchMgr.fsm_configFilename ;
						
						try{
							OpenFileEdit(t_configFile);
						}catch(Exception ex){
							m_logger.PrinterException(ex);
						}
						
					}else if(e.getSource() == m_delAccountItem){
						if(JOptionPane.showConfirmDialog(JFrame.getFrames()[0],"删除用户 " + t_thread.m_fetchMgr.GetAccountName() + "？", 
							"删除", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
					
							DelAccoutThread(t_thread.m_fetchMgr.GetAccountName(),true);
							
						}else{
							return;
						}
					}else if(e.getSource() == m_addAccountTime){
						
						final int ct_width = 300;
						final int ct_height = 60;
						
						Frame t_frame = JFrame.getFrames()[0];
						final JDialog t_dlg = new JDialog(t_frame,"设置账户 <"+t_thread.m_fetchMgr.GetAccountName()+"> 时间",true);

						t_dlg.setResizable(false);
						t_dlg.getContentPane().setLayout(new FlowLayout());
						
						final JTextField 	t_time = new JTextField();
						t_time.setDocument(new NumberMaxMinLimitedDmt(99999999,t_time));
						t_time.setPreferredSize(new Dimension(200, 25));
						t_dlg.getContentPane().add(t_time);
						
						final JButton		t_confirmBut = new JButton("确定");
						t_dlg.getContentPane().add(t_confirmBut);
						
						t_confirmBut.addActionListener(new ActionListener() {
							
							public void actionPerformed(ActionEvent arg0) {
								if(!t_time.getText().isEmpty()){
									t_thread.m_usingHours = Integer.valueOf(t_time.getText()).longValue();
									t_thread.m_formerTimer = (new Date()).getTime();
									
									t_dlg.setVisible(false);
									t_dlg.dispose();
								}						
							}
						});
						
						t_dlg.setLocation(t_frame.getLocation().x + (t_frame.getWidth()- ct_width) / 2,
								t_frame.getLocation().y + (t_frame.getHeight() -  ct_height) / 2);
						
						t_dlg.setSize(ct_width,ct_height);
						t_dlg.setVisible(true);
						
					}
					
					RefreshState();
				}
				
			}
		};
		
		m_contextMenu.add(m_checkAccountItem);
		m_checkAccountItem.addActionListener(t_menuListener);
		
		m_contextMenu.add(m_delAccountItem);
		m_delAccountItem.addActionListener(t_menuListener);
		
		m_contextMenu.add(new JSeparator());
		
		m_contextMenu.add(m_pauseAccountItem);
		m_pauseAccountItem.addActionListener(t_menuListener);
		
		m_contextMenu.add(m_continueAccountItem);
		m_continueAccountItem.addActionListener(t_menuListener);
		
		m_contextMenu.add(m_addAccountTime);
		m_addAccountTime.addActionListener(t_menuListener);
		
		m_accountTable.addMouseListener(new MouseListener() {
			
			public void mouseReleased(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseClicked(MouseEvent e) {
									
				// get the coordinates of the mouse click
				Point p = e.getPoint();
	 
				// get the row index that contains that coordinate
				final int rowNumber = m_accountTable.rowAtPoint( p );
	 
				// Get the ListSelectionModel of the JTable
				ListSelectionModel model = m_accountTable.getSelectionModel();
	 
				// set the selected interval of rows. Using the "rowNumber"
				// variable for the beginning and end selects only that one row.
				model.setSelectionInterval( rowNumber, rowNumber );	
				
				if(rowNumber != -1){
					if(SwingUtilities.isRightMouseButton(e)){
						TableMouseEvent(e,rowNumber);
					}else if(SwingUtilities.isLeftMouseButton(e)){
						CheckLogInfo(rowNumber);
					}
					
				}else{
					m_currentSelectThread = null;
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
	
	public void CheckLogInfo(int _row){
		m_currentSelectThread = (fetchThread)m_accountList.elementAt(_row);
		
		FillLogInfo(m_currentSelectThread);
	}
	
	public void FillLogInfo(fetchThread _thread){
		
		StringBuffer t_buffer = new StringBuffer();
		
		try{
			BufferedReader in = new BufferedReader(
									new InputStreamReader(
										new FileInputStream(_thread.m_logger.GetLogFileName()),"UTF-8"));

			
			
			String t_line;
			while((t_line = in.readLine()) != null){
				t_buffer.append(t_line + "\n");
			}
			
			in.close();
			
		}catch(Exception e){
			t_buffer.append("read log error : " + e.getMessage() + " " + e.getClass().getName() );
		}
		
		m_logInfo.setText(t_buffer.toString());
		m_logInfo.setCaretPosition(m_logInfo.getDocument().getLength());
	}
		
	public fetchThread SearchAccountThread(String _accountName,int _port){
		
		m_logger.LogOut("SearchAccountThread start name="+_accountName);
		
		synchronized (m_accountList) {
			for(int i = 0;i < m_accountList.size();i++){
				fetchThread t_fetch = (fetchThread)m_accountList.elementAt(i);
				if(t_fetch.m_fetchMgr.GetAccountName().equalsIgnoreCase(_accountName) 
				|| t_fetch.m_fetchMgr.GetServerPort() == _port){
					
					m_logger.LogOut("SearchAccountThread end");
					
					return t_fetch;
				}
			}
		}
		
		
		m_logger.LogOut("SearchAccountThread end name="+_accountName);
		
		return null;
		
	}
	
	public boolean AddAccountThread(fetchThread _thread,boolean _storeAccountInfo){
		
		m_logger.LogOut("AddAccountThread start");
		
		synchronized (m_accountList){
			for(int i = 0;i < m_accountList.size();i++){
				fetchThread t_fetch = (fetchThread)m_accountList.elementAt(i);
				if(t_fetch.m_fetchMgr.GetAccountName().equalsIgnoreCase(_thread.m_fetchMgr.GetAccountName())){
					
					m_logger.LogOut("AddAccountThread end");
					return false;
				}
			}
			
			m_accountList.addElement(_thread);
		}
		
		m_accountTable.AddAccount(_thread);
				
//		m_formerHost		= _thread.m_fetchMgr.GetHost();
//		m_formerHost_port	= _thread.m_fetchMgr.GetHostPort();
//		
//		m_formerHost_send		= _thread.m_fetchMgr.GetSendHost();
//		m_formerHost_port_send	= _thread.m_fetchMgr.GetSendPort();
		
		m_pushInterval		= _thread.m_fetchMgr.GetPushInterval();
		m_expiredTime		= _thread.m_usingHours;
		
		if(_storeAccountInfo){
			StoreAccountInfo();
			
			m_accountTable.setRowSelectionInterval(m_accountList.size() - 1, m_accountList.size() - 1);
		}
		
		
		m_logger.LogOut("AddAccountThread end");
		return true;
	}
	
	public void DelAccoutThread(String _accountName,boolean _storeAccountInfo){
		
		m_logger.LogOut("DelAccoutThread start name="+_accountName);
		
		synchronized (m_accountList) {
			for(int i = 0;i < m_accountList.size();i++){
				
				fetchThread t_fetch = (fetchThread)m_accountList.elementAt(i);
				if(_accountName.equalsIgnoreCase(t_fetch.m_fetchMgr.GetAccountName())){
					
					t_fetch.Destroy();
					
					DelDirectory(t_fetch.m_fetchMgr.GetAccountName());
					
					m_accountList.remove(t_fetch);
					m_accountTable.DelAccount(t_fetch);
					
					if(_storeAccountInfo){
						StoreAccountInfo();
					}				
					
					break;
				}
			}
		}		
		
		m_logger.LogOut("DelAccoutThread end name="+_accountName);
	}
	
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == m_addAccount){
			// open the dialog for creating account of yuchberry
			//
			new createDialog(this,m_formerHost,"" + m_formerHost_port,m_formerHost_send,
								"" + m_formerHost_port_send,GetRandomPassword(),
								"" + GetAvailableServerPort(),"" + m_pushInterval,"" + m_expiredTime);
			
		}else if(e.getSource() == m_sponsor){

			final String t_sponsorURL = "http://code.google.com/p/yuchberry/wiki/Sponsor_yuchberry";
			
			try{
				OpenURL(t_sponsorURL);
			}catch(Exception ex){
				m_logger.PrinterException(ex);
			}
			
		}else if(e.getSource() == m_searchButton){
			SelectAccount(m_searchAccount.getText());
		}
	}
	
	public void SelectAccount(final String _text){
		
		synchronized (m_accountList) {
			for(int i = 0;i < m_accountList.size();i++){
				fetchThread t_fetch = (fetchThread)m_accountList.elementAt(i);
				if(t_fetch.m_fetchMgr.GetAccountName().indexOf(_text) != -1){
					
					m_accountTable.setRowSelectionInterval(i,i);
					m_currentSelectThread = t_fetch;
					
					CheckLogInfo(i);
					
					Rectangle rect = m_accountTable.getCellRect(i, 0, true);
					m_accountTable.scrollRectToVisible(rect);
					
					break;				
				}
			}
		}
		
	}
	
	static public void DelDirectory(final String _dir){
		
		File t_file = new File(_dir);
		
		if(t_file.exists()){
			if(t_file.isFile()){
				t_file.delete();
			}else if(t_file.isDirectory()){
				File[] t_files = t_file.listFiles();
				for(int i = 0;i < t_files.length;i++){
					DelDirectory(t_files[i].getAbsolutePath());
				}
				t_file.delete();
			}
		}
	}		
	
	public void CloseProcess(){
		StoreAccountInfo();
	}
	
	public void RefreshState(){
		
		m_currUsingAccount		= 0;
		m_currConnectAccount	= 0;
		
		m_checkFolderStateThread.removeAllElements();
				
		final long t_currTime = (new Date()).getTime();
		
		Vector<fetchThread> t_deadPool = new Vector<fetchThread>();
		
		synchronized (m_accountList) {
			for(int i = 0;i < m_accountList.size();i++){
				fetchThread t_thread = (fetchThread)m_accountList.elementAt(i);
							
				if(!t_thread.m_pauseState){
					
					if(t_thread.m_fetchMgr.IsCheckFolderState()){
						m_checkFolderStateThread.add(t_thread);
					}
					
					if(t_thread.GetLastTime(t_currTime) < 0){
						m_logger.LogOut("帐户暂停： " + t_thread.m_fetchMgr.GetAccountName());
						t_thread.Pause();
					}
					
					m_currUsingAccount++;
					
					final long t_lastTime = t_thread.GetLastTime(t_currTime);
					
					if(!t_thread.m_sendTimeupMail && t_lastTime > 0 && t_lastTime < 24 * 3600 * 1000){
						
						t_thread.m_sendTimeupMail = true;
						
						m_logger.LogOut("到期帐户，发送邮件： " + t_thread.m_fetchMgr.GetAccountName());
						
						SendTimeupMail(t_thread);
					}
					
					if(t_thread.m_fetchMgr.GetClientConnected() != null){
						m_currConnectAccount++;
						t_thread.m_clientDisconnectTime = 0;
					}else{
						if(t_thread.m_clientDisconnectTime == 0){
							t_thread.m_clientDisconnectTime = t_currTime;
						}
					}
					
				}else{
					if(t_thread.m_clientDisconnectTime == 0){
						t_thread.m_clientDisconnectTime = t_currTime;
					}
				}
				
				if(t_thread.m_clientDisconnectTime != 0 && t_thread.m_usingHours != 0){
					
					final long t_delTime = t_thread.m_pauseState?(12 * 3600 * 1000):(5 * 24 * 3600 * 1000);
					
					if(Math.abs(t_currTime - t_thread.m_clientDisconnectTime) >= t_delTime  ){
						t_deadPool.add(t_thread);
					}
				}
			}
		}
		
			
		// delete the disconnect time-up thread
		//
		for(fetchThread thread : t_deadPool){
			m_logger.LogOut("未连接时间超过5天，删除帐户 " + thread.m_fetchMgr.GetAccountName() + " 账户暂停状态：" + thread.m_pauseState);
			
			DelAccoutThread(thread.m_fetchMgr.GetAccountName(), false);			
		}
			
		m_stateLabel.setText("check/连接账户/使用帐户/总帐户：" + m_checkFolderStateThread.size() + "/" + m_currConnectAccount + "/" + m_currUsingAccount + "/" + m_accountList.size());
		m_accountTable.RefreshState();
		
		if(m_currentSelectThread != null){
			FillLogInfo(m_currentSelectThread);
		}
				
		// close the timeup Bber Request
		//
		boolean t_deleteThread = false;
		
		synchronized (m_bberRequestList) {
			for(int i = 0;i < m_bberRequestList.size();i++){
				BberRequestThread bber = m_bberRequestList.get(i);
				
				if(Math.abs(t_currTime - bber.m_requestTime) > 10 * 60 * 1000){
					
					if(bber.m_orgThread != null){
						
						bber.m_orgThread.Destroy();
						DelAccoutThread(bber.m_orgThread.m_fetchMgr.GetAccountName(), false);
						
						m_logger.LogOut("同步没有响应，删除帐户 " + bber.m_orgThread.m_fetchMgr.GetAccountName());
						
						t_deleteThread = true;
					}else{
						bber.m_mainMgr.EndListening();
						
						m_logger.LogOut("同步没有响应，停止帐户 " + bber.m_mainMgr.GetAccountName());
					}
					
					bber.interrupt();
					m_bberRequestList.remove(i);
					
					i--;
				}
			}
		}		
		
		if(!t_deadPool.isEmpty() || t_deleteThread){		
			StoreAccountInfo();
		}
	}
	
	public void SendTimeupMail(fetchThread _thread){
		try{
			final String t_contain = fetchMgr.ReadSimpleIniFile("timeupMail.txt","UTF-8",null);
			
			_thread.m_fetchMgr.SendImmMail("yuchberry 提示", t_contain, "\"YuchBerry\" <yuchberry@gmail.com>");
			
		}catch(Exception e){}		
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
	
	static public void OpenURL(String _URL)throws Exception{
		
		if( !java.awt.Desktop.isDesktopSupported() ) {
            throw new Exception( "java.awt.Desktop is not supported (fatal)" );
        }

        java.awt.Desktop desktop = java.awt.Desktop.getDesktop();

        if( !desktop.isSupported( java.awt.Desktop.Action.BROWSE ) ) {
        	throw new Exception( "Desktop doesn't support the browse action (fatal)" );
        }

	    java.net.URI uri = new java.net.URI( _URL );
	    desktop.browse( uri );      
        
	}
	
	static public void OpenFileEdit(String _filename)throws Exception{
		
		if( !java.awt.Desktop.isDesktopSupported() ) {
            throw new Exception( "java.awt.Desktop is not supported (fatal)" );
        }

        java.awt.Desktop desktop = java.awt.Desktop.getDesktop();

        if( !desktop.isSupported( java.awt.Desktop.Action.EDIT ) ) {
        	throw new Exception( "java.awt.Desktop doesn't support the edit action (fatal)" );
        }

	    desktop.open(new File(_filename));    
	}
		
	/*
	 * the HTTP process... 
	 */
	public int GetAvailableServerPort(){
		
		m_logger.LogOut("GetAvailableServerPort start");
		
		try{
			
			if(m_accountList.isEmpty() && m_bberRequestList.isEmpty()){
				return 9717;
			}
			
			List<Integer> t_portList = new ArrayList<Integer>();
							
			synchronized (m_accountList) {
				for(fetchThread acc : m_accountList){
					t_portList.add(new Integer(acc.m_fetchMgr.GetServerPort()));
				}
			}
			
			
			synchronized (m_bberRequestList) {
				for(BberRequestThread req : m_bberRequestList){
					t_portList.add(new Integer(req.m_serverPort));
				}
			}
			
			
			Collections.sort(t_portList);
			
			for(int i = 0 ;i < t_portList.size() - 1;i++){
				
				int port = t_portList.get(i).intValue() + 1;
				
				if(port < t_portList.get(i + 1).intValue()){
					try{
						ServerSocket t_sock = new ServerSocket(port);
						t_sock.close();
						
						return port;
						
					}catch(Exception e){
						
					}
				
				}
			}
			
			int t_port = t_portList.get(t_portList.size() - 1).intValue() + 1;
			
			while(t_port < 12000){
				try{
					ServerSocket t_sock = new ServerSocket(t_port);
					t_sock.close();
					
					break;
					
				}catch(Exception e){
					t_port++;
				}			
			}
			
			return t_port;
		}finally{
			m_logger.LogOut("GetAvailableServerPort end");
		}		
	}
	/*
	 * the process thread to create/response the GAE URL requeset
	 */
	final class BberRequestThread extends Thread{
		String		m_result		= null;
		mainFrame	m_mainApp		= null;
		
		yuchbber	m_currbber		= null;
		
		long		m_requestTime	= (new Date()).getTime();
		int			m_serverPort	= 0;
		
		String		m_prefix		= null;
		
		fetchMgr	m_mainMgr		= null;
		
		boolean	m_prepareSucc 	= false;
		
		fetchThread	m_orgThread = null;
				
		BberRequestThread(mainFrame _frame,yuchbber _bber,fetchThread _orgThread){

			m_mainApp	= _frame;
			m_currbber	= _bber;
			
			m_orgThread = _orgThread; 
			
			if(m_orgThread != null){
				m_serverPort	= _orgThread.m_fetchMgr.GetServerPort();
				m_prefix		= _orgThread.m_fetchMgr.GetPrefixString();
				
				
				// end listening
				//
				m_orgThread.Pause();
				
			}else{
				m_prefix = _bber.GetSigninName() + "_tmpCreate/";
				
				File t_tmpFolder = new File(m_prefix);
				if(!t_tmpFolder.exists()){
					t_tmpFolder.mkdir();
				}
				
				m_serverPort = GetAvailableServerPort();
			}
						
			if(m_currbber.GetEmailList().isEmpty()
			&& m_currbber.GetWeiboList().isEmpty()){
				m_result = "<Error>没有账户信息</Error>";
			}else{
				start();
			}
		}
				
		@Override
		public void run(){
			try{
				
				FileOutputStream t_config_file = new FileOutputStream(m_prefix + fetchMgr.fsm_configFilename);
				try{
					StringBuffer t_configBuffer = new StringBuffer();
					t_configBuffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
					
					t_configBuffer.append("<Yuchberry userPassword=\"").append(m_currbber.GetPassword())
											.append("\" serverPort=\"").append(m_serverPort)
											.append("\" pushInterval=\"").append(m_currbber.GetPushInterval())
											
											// caution!!
											//
											//	can't use the SSL
											//
											.append("\" userSSL=\"").append(0/*m_currbber.IsUsingSSL()?1:0*/)
											.append("\" convertoSimpleChar=\"").append(m_currbber.IsConvertSimpleChar()?1:0)
								.append("\" >\n");					
					
					for(yuchEmail email:m_currbber.GetEmailList()){
						t_configBuffer.append("<EmailAccount account=\"").append(email.m_emailAddr)
														.append("\" password=\"").append(email.m_password)
														.append("\" sendName=\"").append(email.m_username)
														.append("\" useFullNameSignIn=\"").append(email.m_fullnameSignIn?1:0)
														.append("\" protocol=\"").append(email.m_protocol)
														.append("\" host=\"").append(email.m_host)
														.append("\" port=\"").append(email.m_port)
														.append("\" protocol_send=\"").append("smtp")
														.append("\" host_send=\"").append(email.m_host_send)
														.append("\" port_send=\"").append(email.m_port_send)
														.append("\" appendHTML=\"").append(email.m_appendHTML?1:0)
										.append("\" />\n");
					}
					
					for(yuchWeibo weibo:m_currbber.GetWeiboList()){
						t_configBuffer.append("<WeiboAccount type=\"").append(weibo.m_typeName)
														.append("\" account=\"").append(weibo.m_accoutName)
														.append("\" accessToken=\"").append(weibo.m_accessToken)
														.append("\" secretToken=\"").append(weibo.m_secretToken)
														.append("\" timelineSum=\"").append(weibo.m_timelineSum)
														.append("\" directMessageSum=\"").append(weibo.m_directMsgSum)
														.append("\" atMeSum=\"").append(weibo.m_atMeSum)
														.append("\" commentSum=\"").append(weibo.m_commentMeSum)
														
										.append("\" />\n");
					}
					
					t_configBuffer.append("</Yuchberry>");
					t_config_file.write(t_configBuffer.toString().getBytes("UTF-8"));
					
				}finally{
					t_config_file.flush();
					t_config_file.close();
				}
											
				if(m_orgThread != null){
					
					m_logger.LogOut(m_orgThread.m_fetchMgr.GetAccountName() + " start sync current fetchThread.");
					
					m_orgThread.m_fetchMgr.InitConnect(m_orgThread.m_fetchMgr.GetPrefixString(),m_orgThread.m_logger);
					m_orgThread.m_fetchMgr.ResetAllAccountSession(true);
					
					m_orgThread.Reuse();
					
					m_orgThread.m_usingHours = m_currbber.GetUsingHours();
					m_orgThread.m_formerTimer = m_currbber.GetCreateTime();
					
					createDialog.WriteSignatureAndGooglePos(m_orgThread.m_fetchMgr.GetPrefixString(), m_currbber.GetSignature());
					
				}else{
					
					m_logger.LogOut(m_currbber.GetSigninName() + " start sync new bber.");
					
					m_mainMgr = new fetchMgr();
					Logger t_logger = new Logger();
					try{
						m_mainMgr.InitConnect(m_prefix,t_logger);
						
						m_mainMgr.ResetAllAccountSession(true);	
												
						// copy the account information 
						//
						String t_copyPrefix = m_currbber.GetSigninName() + "/";
						
						File t_tmpFolder = new File(m_prefix);
						if(t_tmpFolder.exists()){
							File t_copyDir = new File(t_copyPrefix);
							if(t_copyDir.exists()){
								DelDirectory(t_copyPrefix);
							}
							
							t_tmpFolder.renameTo(t_copyDir);
							
						}else{
							
							File t_accountFolder = new File(t_copyPrefix);
							if(!t_accountFolder.exists()){
								t_accountFolder.mkdir();
							}
							
							createDialog.CopyFile(m_prefix + fetchMgr.fsm_configFilename,
									t_copyPrefix + fetchMgr.fsm_configFilename);
						}					
												
						createDialog.WriteSignatureAndGooglePos(t_copyPrefix, m_currbber.GetSignature());
												
					}catch(Exception e){
						
						m_mainMgr.EndListening();
						t_logger.StopLogging();
						
						throw e;
					}
					
				}
				
				m_prepareSucc = true;			
				
			}catch(Exception e){
				m_result = "<Error>" + e.getMessage() + "</Error>";
			}
			
			m_logger.LogOut(m_prefix + " end sync result:" + m_result);
		}
		
		public void CheckStartRequest(){

			try{
				
				if(m_prepareSucc){

					if(m_orgThread == null){

						String t_copyPrefix = m_currbber.GetSigninName() + "/";
						
						fetchThread t_newThread = new fetchThread(m_mainMgr,t_copyPrefix,
																	m_currbber.GetUsingHours(),
																	m_currbber.GetCreateTime(),false);

						AddAccountThread(t_newThread, true);
						
					}else{
						m_orgThread.Reuse();
					}
					
					// to tell the web server port
					//
					m_result = "<Port>" + m_serverPort +"</Port>";
					
				}
				
			}catch(Exception e){
				
				m_result = "<Error>" + e.getMessage() +"</Error>";
			}
			
			m_logger.LogOut(m_currbber.GetSigninName() + " 同步结果: " + m_result);
		}
		
	}
	
	/*
	 * current request thread
	 */
	Vector<BberRequestThread> m_bberRequestList = new Vector<BberRequestThread>();
	
	String 		m_yuchsignFramePass = null;
	int 	  	m_yuchsignMaxBber = 0;
	
	private void LoadYuchsign(){
		
		try{
			BufferedReader in = new BufferedReader(
									new InputStreamReader(
										new FileInputStream("yuchsign"),"UTF-8"));
						
			try{
				m_yuchsignFramePass 	= in.readLine();
				String	t_port 			= in.readLine();
				m_yuchsignMaxBber 		= Integer.valueOf(in.readLine()).intValue();
				
				if(m_yuchsignFramePass == null || m_yuchsignFramePass.isEmpty()){
					return ;
				}
				
				m_httpd = new NanoHTTPD(Integer.valueOf(t_port).intValue()){
					public Response serve( String uri, String method, Properties header, Properties parms, Properties files ){
								
						m_logger.LogOut("start serve");
						try{
							String pass = header.getProperty("pass");
						
							if(pass == null || !pass.equals(m_yuchsignFramePass)){
							
								pass = parms.getProperty("pass");
							
								if(pass == null || !pass.equals(m_yuchsignFramePass)){
									return new NanoHTTPD.Response( HTTP_FORBIDDEN, MIME_PLAINTEXT, "");
								}else{
									String t_type = parms.getProperty("type");
									String t_bber = parms.getProperty("bber");
								
									if(t_type != null){
										return new NanoHTTPD.Response( HTTP_OK, MIME_PLAINTEXT, ProcessAdminCheck(t_type,t_bber));
									}else{
										return new NanoHTTPD.Response( HTTP_FORBIDDEN, MIME_PLAINTEXT, "");
									}
								}
							}
						
							return new NanoHTTPD.Response( HTTP_OK, MIME_PLAINTEXT, ProcessHTTPD(method,header,parms));
							
						}finally{
							m_logger.LogOut("end serve");
						}
						
					}
				};
				
			}finally{
				in.close();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private String ProcessAdminCheck(String _type,String _bber){
		
		m_logger.LogOut("ProcessAdminCheck start fill admin data type: " + _type + " _bber:" + _bber);
				
		try{
			if(_type.equals("0")){
				
				StringBuffer t_result = new StringBuffer();
				
				t_result.append("state:").append(m_checkFolderStateThread.size()).append("/")
									.append(m_currConnectAccount).append("/").append(m_currUsingAccount)
									.append("/").append(m_accountList.size()).append("\n");
				
				long t_currTime = (new Date()).getTime();
				
				if(!m_accountList.isEmpty()){
					
					synchronized (m_accountList) {
						for(int i = m_accountList.size() - 1; i >= 0;i--){
							fetchThread t_acc = m_accountList.elementAt(i);
							t_result.append(t_acc.m_fetchMgr.GetAccountName()).append("\t\t");
							t_result.append(t_acc.m_fetchMgr.GetServerPort()).append("\t\t");
							
							if(!t_acc.m_pauseState){
						
								long t_remainTime = t_acc.GetLastTime(t_currTime);
								t_result.append(t_remainTime / 3600000).append("h");
								
							}else{
								t_result.append("--");
							}
							
							t_result.append("\t\t").append(t_acc.GetStateString()).append("\n");
							
						}
					}
				}				
							
				return t_result.toString();
				
			}else if(_type.equals("1")){
				
				StringBuffer t_result = new StringBuffer();
				
				
				t_result.append("state:").append(m_checkFolderStateThread.size()).append("/")
										.append(m_currConnectAccount).append("/").append(m_currUsingAccount)
										.append("/").append(m_accountList.size()).append("\n");
				
				
				if(!m_accountList.isEmpty()){
					
					synchronized (m_accountList) {
						
						for(int i = m_accountList.size() - 1; i >= 0;i--){
							fetchThread t_acc = m_accountList.elementAt(i);
							
							if(t_acc.m_fetchMgr.GetClientConnected() == null){
								t_result.append(t_acc.m_fetchMgr.GetAccountName()).append("\t\t");
								t_result.append("\t\t").append(t_acc.GetStateString()).append("\n");
							}						
						}
					}
					
				}
							
				return t_result.toString();
				
			}else if(_type.equals("2") && _bber != null){
				return ProcessLogQuery(_bber);
			}
			
			return "";
		}finally{
			m_logger.LogOut("ProcessAdminCheck end");
		}
		
	}
	
	private String ProcessHTTPD(String method, Properties header, Properties parms){
		
		m_logger.LogOut("start ProcessHTTPD 0");
		
		String t_string = parms.getProperty("bber");
		if( t_string == null){
			return "<Error>没有bber参数的URL</Error>";
		}
		
		try{
			
			String t_bberParam = URLDecoder.decode(t_string, "UTF-8");
			
			if((parms.getProperty("log") != null)){
				return ProcessLogQuery(t_bberParam);
			}
			
			if((parms.getProperty("create") != null)){
				return ProcessCreateTimeQuery(parms);
			}
			
			m_logger.LogOut("start ProcessHTTPD 1");
			
			boolean t_checkState = (parms.getProperty("check") != null);		
					
			String t_signinName = null;
			yuchbber t_bber = null;
			if(t_checkState){
				t_signinName = t_bberParam;
			}else{
				t_bber = new yuchbber();
				t_bber.InputXMLData(t_bberParam);
				
				t_signinName = t_bber.GetSigninName();
			}
			
			synchronized (m_bberRequestList) {
				// find the requested bber
				//
				for(BberRequestThread bber : m_bberRequestList){
					if(bber.m_currbber.GetSigninName().equals(t_signinName)){

						if(bber.isAlive()){
							return "<Loading />";
						}else{
							
							bber.CheckStartRequest();
							
							m_bberRequestList.remove(bber);
							
							return bber.m_result;
						}									 
					}
				}
			}

			// search the former thread
			//
			fetchThread t_thread = SearchAccountThread(t_bber.GetSigninName(),0);
			
			if(t_thread == null && m_accountList.size() >= m_yuchsignMaxBber){
				// if reach the max bber
				//
				return "<Max />";
			}
			
			// the constructor of BberRequestThread has fetchThread.Pause
			// that function will be suspended by some wired reason
			//
			BberRequestThread t_request = new BberRequestThread(this,t_bber,t_thread);
			
			synchronized (m_bberRequestList) {
				// create new request thread
				//
				m_bberRequestList.add(t_request);
			}
			
			
			m_logger.LogOut("bber <" + t_bber.GetSigninName() + "> sync, current syncing Thread:" + m_bberRequestList.size());
			
			return "<Loading />";			
			
		}catch(Exception e){
			
			return "<Error>" + e.getMessage() + "</Error>";
		}
		
		
	}
	
	private String ProcessCreateTimeQuery(Properties header){
		
		m_logger.LogOut("ProcessCreateTimeQuery start");
		try{
			try{
				
				final String t_bber 		= URLDecoder.decode(header.getProperty("bber"),"UTF-8");
				final String t_createTime	= header.getProperty("create");
				final String t_usingHours	= header.getProperty("time");
				
				if(t_bber == null || t_createTime == null || t_usingHours == null){
					return "参数错误";
				}
				
				m_logger.LogOut("t_bber="+ t_bber+ " ProcessCreateTimeQuery");
				
				synchronized (m_accountList) {
					for(fetchThread thread:m_accountList){
						if(thread.m_fetchMgr.GetAccountName().equalsIgnoreCase(t_bber)){
							
							thread.m_usingHours = Long.valueOf(t_usingHours).longValue();
							thread.m_formerTimer = Long.valueOf(t_createTime).longValue();
											
							StoreAccountInfo();
							
							if(thread.m_pauseState){
								thread.Reuse();
							}
							
							return "<OK />";
						}
					}
				}
				
				
				return "原有账户已经因为长时间没有链接而删除，需要重新同步";
				
			}catch(Exception e){
				return "服务器重新启用账户出现异常，需要手动同步";
			}
		}finally{
			m_logger.LogOut("ProcessCreateTimeQuery end");
		}
		
	}
	
	static final int fsm_maxReadLogLen = 4096;
	static final byte[] fsm_readLogBuffer = new byte[4096]; 
	
	private String ProcessLogQuery(String _bberName){
		
		m_logger.LogOut(_bberName + " query log.");
		
		// search the former thread
		//
		fetchThread t_thread = SearchAccountThread(_bberName,0);
		if(t_thread == null){
			m_logger.LogOut(_bberName + " query log error.");
			return "<Error>在主机上查询不到账户，请先同步，获得有效主机。</Error>";
		}
		
		try{
			FileInputStream t_stream = new FileInputStream(t_thread.m_logger.GetLogFileName());
			try{
				int t_fileLen = t_stream.available();
				if(t_fileLen == 0){
					m_logger.LogOut(_bberName + " query log null.");
					return "null";
				}
				
				t_stream.skip(Math.max(t_fileLen - fsm_maxReadLogLen,0));
				
				
				final int t_bufferLen = Math.min(t_fileLen - 1, fsm_maxReadLogLen);
				t_stream.read(fsm_readLogBuffer,0,t_bufferLen);
				
				return (new String(fsm_readLogBuffer,0,t_bufferLen,"UTF-8"));
				
			}finally{
				
				t_stream.close();
				m_logger.LogOut(_bberName + " query log done.");
			}
		}catch(Exception e){
			return "<Error>读取文件失败:"+e.getMessage()+"</Error>";
		}
		
	}
	
	
	

}
