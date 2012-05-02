/**
 *  Dear developer:
 *  
 *   If you want to modify this file of project and re-publish this please visit:
 *  
 *     http://code.google.com/p/yuchberry/wiki/Project_files_header
 *     
 *   to check your responsibility and my humble proposal. Thanks!
 *   
 *  -- 
 *  Yuchs' Developer    
 *  
 *  
 *  
 *  
 *  尊敬的开发者：
 *   
 *    如果你想要修改这个项目中的文件，同时重新发布项目程序，请访问一下：
 *    
 *      http://code.google.com/p/yuchberry/wiki/Project_files_header
 *      
 *    了解你的责任，还有我卑微的建议。 谢谢！
 *   
 *  -- 
 *  语盒开发者
 *  
 */
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
import java.io.InputStreamReader;
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

import com.yuchting.yuchberry.server.fakeMDSSvr;
import com.yuchting.yuchberry.server.fetchMain;
import com.yuchting.yuchberry.server.fetchMgr;
import com.yuchting.yuchberry.server.fetchWeibo;

public class mainFrame extends JFrame implements ActionListener{
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8017119745783966162L;
	
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
	
	String		m_formerHost		= new String("imap.gmail.com");
	int			m_formerHost_port	= 993;
	
	String		m_formerHost_send		= new String("smtp.gmail.com");
	int			m_formerHost_port_send	= 587;
	
	JPopupMenu 	m_contextMenu			= new JPopupMenu();
	JMenuItem	m_checkAccountItem		= new JMenuItem("检查帐户");
	JMenuItem	m_delAccountItem		= new JMenuItem("删除帐户");
	JMenuItem	m_pauseAccountItem		= new JMenuItem("暂停");
	JMenuItem	m_continueAccountItem	= new JMenuItem("继续");
	JMenuItem	m_addAccountTime		= new JMenuItem("设置到期时间");
		
	loadDialog	m_loadDialog			= null;
	fetchThread	m_currentSelectThread	= null;
		
	YuchServer	m_mainServer			= null;
	Vector<fetchThread>	m_accountList	= null;
		
	static public void main(String _arg[])throws Exception{
		
		String Darg = System.getProperty("yuch.arg");
		if(Darg != null){
			_arg = Darg.split(";");
		}
				
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
		}else if(_arg.length >= 1 && _arg[0].equalsIgnoreCase("server")){
			new fakeMDSSvr();
			new YuchServer(null);
		}else if(_arg.length >= 1 && _arg[0].equalsIgnoreCase("clear")){
			new ClearAccount();			
		}else if(_arg.length >= 2 && _arg[0].equalsIgnoreCase("backup")){
			new BackupAccount(_arg[1]);
		}else{
			new fetchMain();
		}
	}	
	
	public mainFrame(String _arg[]){
		
		boolean t_hideWindow = _arg.length >= 2 && _arg[1].equalsIgnoreCase("hide");

		setTitle("语盒(Yuchs'Box)集成配置工具 bate");
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
		    		m_mainServer.storeAccountInfo();			    	
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
		
		m_loadDialog = new loadDialog(this);
		m_mainServer = new YuchServer(this);
		m_accountList = m_mainServer.m_accountList;
		
		if(!t_hideWindow){
			// don't set visible
			//
			setVisible(true);
		}		
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
							m_mainServer.m_logger.PrinterException(ex);
						}
						
					}else if(e.getSource() == m_delAccountItem){
						if(JOptionPane.showConfirmDialog(JFrame.getFrames()[0],"删除用户 " + t_thread.m_fetchMgr.GetAccountName() + "？", 
							"删除", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
					
							m_mainServer.DelAccoutThread(t_thread.m_fetchMgr.GetAccountName(),true);
							
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
						t_time.setDocument(new createDialog.NumberMaxMinLimitedDmt(99999999,t_time));
						t_time.setPreferredSize(new Dimension(200, 25));
						t_dlg.getContentPane().add(t_time);
						
						final JButton		t_confirmBut = new JButton("确定");
						t_dlg.getContentPane().add(t_confirmBut);
						
						t_confirmBut.addActionListener(new ActionListener() {
							
							public void actionPerformed(ActionEvent arg0) {
								if(!t_time.getText().isEmpty()){
									t_thread.m_usingHours = Integer.valueOf(t_time.getText()).longValue();
									t_thread.m_formerTimer = System.currentTimeMillis();
									
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
					
					m_mainServer.RefreshState();
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
		
	
	
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == m_addAccount){
			// open the dialog for creating account of yuchberry
			//
			new createDialog(this,m_formerHost,"" + m_formerHost_port,m_formerHost_send,
								"" + m_formerHost_port_send,GetRandomPassword(),
								"" + m_mainServer.GetAvailableServerPort(),"" + m_mainServer.m_pushInterval,"" + m_mainServer.m_expiredTime);
			
		}else if(e.getSource() == m_sponsor){

			final String t_sponsorURL = "http://code.google.com/p/yuchberry/wiki/Sponsor_yuchberry";
			
			try{
				OpenURL(t_sponsorURL);
			}catch(Exception ex){
				m_mainServer.m_logger.PrinterException(ex);
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
	
	public static String prepareXmlAttr(String _attr){
		return _attr.replace("&","&amp;")
						.replace("<","&lt;")
						.replace(">","&gt;")
						.replace("\"","&quot;")
						.replace("'","&apos;");
	}
}
