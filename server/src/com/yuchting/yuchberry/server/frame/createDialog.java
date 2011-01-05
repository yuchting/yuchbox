package com.yuchting.yuchberry.server.frame;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.util.Date;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;

import com.yuchting.yuchberry.server.fetchEmail;
import com.yuchting.yuchberry.server.fetchMgr;

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
	   
		if(m_max == -1 || Integer.valueOf(t_current).intValue() <= m_max){
		   
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

public class createDialog extends JDialog implements DocumentListener,
															ActionListener,
															ItemListener{
	
	final static int		fsm_width = 600;
	final static int		fsm_height = 560;
	
	JTextField	m_userPassword		= new JTextField();
	JTextField	m_serverPort		= new JTextField();
	
	JTextField	m_pushInterval		= new JTextField();
	
	JCheckBox	m_useSSL			= new JCheckBox("Push使用SSL加密");
	JCheckBox	m_convertToSimple	= new JCheckBox("转换繁体为简体");
	
	JTextField	m_expiredTime		= new JTextField();	
	JTextArea	m_signature			= new JTextArea();
	
	mainFrame	m_mainFrame = null;
	
	final private class commonConfig{
		String	m_name;
		String	m_protocalName;
		String	m_host;
		String	m_port;
		
		String	m_host_send;
		String	m_port_send;
		
		public commonConfig(String _parserLine)throws Exception{
			String[] t_data = _parserLine.split(",");
			
			if(t_data.length < 6){
				throw new Exception("commonMailSvr.ini file contain '" + _parserLine + "' read error");
			}
			
			for(int i = 0;i < t_data.length;i++){
				if(t_data[i].length() == 0){
					throw new Exception("commonMailSvr.ini file contain '" + _parserLine + "' read error");
				}
			}
			
			m_name 			= t_data[0];
			m_protocalName 	= t_data[1];
			m_host 			= t_data[2];
			m_port 			= t_data[3];
			m_host_send		= t_data[4];
			m_port_send		= t_data[5];			
		}
		
		public void SetConfig(createDialog _dlg){
			if(m_host.equals("0")){
				return;
			}
			
			_dlg.m_host.setText(m_host);
			_dlg.m_port.setText(m_port);
			
			for(int i = 0;i < _dlg.m_protocal.length;i++){
				if(m_protocal[i].getText().equals(m_protocalName)){
					m_protocal[i].setSelected(true);
					break;
				}
			}
			
			_dlg.m_send_host.setText(m_host_send);
			_dlg.m_send_port.setText(m_port_send);
		}
		
	}
	
	JButton		m_addAccountBut		= new JButton("添加");
	JButton		m_delAccountBut		= new JButton("删除");
	
	
	JList		m_accountList		= new JList();
	JScrollPane	m_accountListScroll	= new JScrollPane(m_accountList);
	
	JTabbedPane m_tabbedPane 		= new JTabbedPane();
	
	
	JComboBox	m_commonConfigList = new JComboBox();
	DefaultComboBoxModel m_commonConfigListModel = new DefaultComboBoxModel();
	Vector		m_commonConfigData	= new Vector();
	
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
	
	JCheckBox	m_signInAsFullname	= new JCheckBox("使用全地址作为用户名");	
	
	JButton		m_confirmBut		= new JButton("确定");
	
	Document	m_createConfigDoc	= DocumentFactory.getInstance().createDocument();
	Element		m_createConfigDoc_root = m_createConfigDoc.addElement("Yuchberry");
	
	public createDialog(mainFrame _main,String _formerHost,String _formerPort,
										String _formerHost_send,String _formerPort_send,
										String _userPassword,String _serverPort,
										String _pushInterval,String _expiredTime){
		
		super(_main,"添加一个账户",true);
		
		m_mainFrame = _main;
		
		setResizable(false);
								
		getContentPane().setLayout(new FlowLayout());
		
		setSize(fsm_width,fsm_height);
		setLocation(_main.getLocation().x + (_main.getWidth()- fsm_width) / 2,
					_main.getLocation().y + (_main.getHeight() -  fsm_height) / 2);
		
		m_commonConfigList.setModel(m_commonConfigListModel);
		m_commonConfigList.addItemListener(this);
		
		m_pushInterval.setDocument(new NumberMaxMinLimitedDmt(3600,m_pushInterval));
		m_serverPort.setDocument(new NumberMaxMinLimitedDmt(20000,m_serverPort));
		m_port.setDocument(new NumberMaxMinLimitedDmt(20000,m_port));
		m_send_port.setDocument(new NumberMaxMinLimitedDmt(20000,m_send_port));
		m_expiredTime.setDocument(new NumberMaxMinLimitedDmt(-1, m_expiredTime));
		
		m_account.getDocument().addDocumentListener(this);
		m_host.getDocument().addDocumentListener(this);
		m_signature.setLineWrap(true);
		m_signature.setBorder(BorderFactory.createLineBorder(Color.gray,1));
		
		m_confirmBut.addActionListener(this);
		m_addAccountBut.addActionListener(this);
		m_delAccountBut.addActionListener(this);		
		
		
		////////////////////////////////////////////////////////////////////
		// main push attribute data
		
		JPanel t_mainPanel = new JPanel();
		t_mainPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		t_mainPanel.setPreferredSize(new Dimension(fsm_width, fsm_height * 2 / 5));
				
		AddTextLabel(t_mainPanel,"用户密码:",m_userPassword,100,_userPassword);
		AddTextLabel(t_mainPanel,"用户端口:",m_serverPort,60,_serverPort);
		AddTextLabel(t_mainPanel,"推送间隔(秒):",m_pushInterval,60,_pushInterval);
		AddTextLabel(t_mainPanel,"过期时间(单位小时，0为不过期):",m_expiredTime,80,_expiredTime);
		
		t_mainPanel.add(m_useSSL);
		t_mainPanel.add(m_convertToSimple);
		
		JLabel t_label = new JLabel("签名：");
		t_label.setPreferredSize(new Dimension(fsm_width - 20,25));
		
		t_mainPanel.add(t_label);
		m_signature.setPreferredSize(new Dimension(fsm_width - 15, 120));
		m_signature.setText("--send from my Blackberry\nPowered by yuchberry");
		t_mainPanel.add(m_signature);		
		
		JSeparator t_separator	= new JSeparator();
		t_separator.setPreferredSize(new Dimension(fsm_width, 5));
		t_mainPanel.add(t_separator);
		
		getContentPane().add(t_mainPanel);
		
		
		
		////////////////////////////////////////////////////////////////////
		// account data
		
		JPanel t_accountMainPanel = new JPanel();
		t_accountMainPanel.setLayout(new FlowLayout());
		t_accountMainPanel.setPreferredSize(new Dimension(fsm_width, 240));
		
		m_accountListScroll.setPreferredSize(new Dimension(220,230));
		t_accountMainPanel.add(m_accountListScroll);
		
		t_accountMainPanel.add(PrepareAccountBut());
		
		t_accountMainPanel.add(PrepareAccountDataPanel(_formerHost,_formerPort,_formerHost_send,_formerPort_send));				
		
		getContentPane().add(t_accountMainPanel);
		
		
		
		////////////////////////////////////////////////////////////////////
		// confirm button data
		
		t_separator	= new JSeparator();
		t_separator.setPreferredSize(new Dimension(fsm_width, 5));
		getContentPane().add(t_separator);
		getContentPane().add(m_confirmBut);
	
		AutoSelectProtocal();
		
		AddCommonConfigList();
		
		setVisible(true);	
	}
	
	private JComponent PrepareAccountDataPanel(String _formerHost,String _formerPort,String _formerHost_send,String _formerPort_send){
				
		JPanel t_accountPanel = new JPanel();
		t_accountPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		JLabel t_label = new JLabel("常用配置：");
		t_label.setPreferredSize(new Dimension(80,25));
		t_accountPanel.add(t_label);
		m_commonConfigList.setPreferredSize(new Dimension(200, 25));
		t_accountPanel.add(m_commonConfigList);
		
		JSeparator	t_separator			= new JSeparator();
		t_separator.setPreferredSize(new Dimension(fsm_width, 5));
		t_accountPanel.add(t_separator);
		
		AddTextLabel(t_accountPanel,"帐号名称:",m_account,220,"");
		AddTextLabel(t_accountPanel,"帐号密码:",m_password,220,"");
		AddTextLabel(t_accountPanel,"主机地址:",m_host,120,_formerHost);
		AddTextLabel(t_accountPanel,"端口:",m_port,60,_formerPort);

		t_accountPanel.add(new JLabel("协议:"));
		for(int i = 0;i < m_protocal.length;i++){
			m_protocalGroup.add(m_protocal[i]);
			t_accountPanel.add(m_protocal[i]);
		}
		
		AddTextLabel(t_accountPanel,"发送主机地址:",m_send_host,100,_formerHost_send);
		AddTextLabel(t_accountPanel,"端口:",m_send_port,60,_formerPort_send);
		
		m_tabbedPane.addTab("邮件",null,t_accountPanel,"添加邮件账户");
		m_tabbedPane.setPreferredSize(new Dimension(300, 230));
		
		return m_tabbedPane;
	}
	
	private JComponent PrepareAccountBut(){

		JPanel t_butPanel = new JPanel();
		
		t_butPanel.setLayout(new BoxLayout(t_butPanel,BoxLayout.Y_AXIS));
		t_butPanel.add(m_addAccountBut);
		t_butPanel.add(m_delAccountBut);
		
		return t_butPanel;
	}
	
	//@{ DocumentListener for JTextField change
	public void changedUpdate(DocumentEvent e){
		
	}
	public void insertUpdate(DocumentEvent e){
		TextChangeEvent(e);
	}
	public void removeUpdate(DocumentEvent e){
		TextChangeEvent(e);
	}
	
	private void TextChangeEvent(DocumentEvent e){
		if(e.getDocument() == m_host.getDocument()){
			AutoSelectProtocal();
		}else if(e.getDocument() == m_account.getDocument()){
			AutoSelectMailHost();
		}
		
	}
	//@}
	
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == m_addAccountBut){
			
			String t_accountName	= m_account.getText();
			String t_password		= m_password.getText();
			String t_host			= m_host.getText();
			String t_port			= m_port.getText();
			
			String t_send_host		= m_send_host.getText();
			String t_send_port		= m_send_port.getText();
			
			try{
				CreateAccountAndTest(t_accountName,
										t_password,
										t_host,
										t_send_host);
				setVisible(false);
				dispose();	
				
			}catch(Exception ex){
				JOptionPane.showMessageDialog(this, ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
			}			
			
		}else if(e.getSource() == m_confirmBut){
			
			final int t_listenPort = 3000;
			if(m_serverPort.getText().length() == 0 || Integer.valueOf(m_serverPort.getText()).intValue() <= t_listenPort){
				JOptionPane.showMessageDialog(this, "用户密码不能为空，监听yuchberry端口不能小于 " + t_listenPort, "错误", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			final int t_minPushInterval = 2;
			if(Integer.valueOf(m_pushInterval.getText()).intValue() <= t_minPushInterval){
				JOptionPane.showMessageDialog(this, "推送间隔不能小于 " + t_minPushInterval, "错误", JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
	}
		
	private void CreateAccountAndTest(String _accountName,String _password,
											String _host,String _port,
											String _send_host,String _send_port,
											String _serverPort,String _signature)throws Exception{
	
		if(_accountName.length() == 0 || _password.length() == 0){
			throw new Exception("账户名、密码不能为空");
		}
		
		if(_host.length() == 0 || _port.length() == 0){
			throw new Exception("邮件接受服务器地址、端口不能为空");
		}
		
		if(_send_host.length() == 0 || Integer.valueOf(_send_port).intValue() <= 0){
			throw new Exception("邮件发送服务器地址不能为空，端口非法");
		}
		
		final int t_serverPort = Integer.valueOf(_serverPort).intValue();
		if(m_mainFrame.SearchAccountThread(_accountName,t_serverPort) != null){
			throw new Exception(_accountName + " 账户重复，或者服务端口" + t_serverPort + "已经被使用");
		}
		
		File t_dir = new File(m_account.getText());
		if(!t_dir.exists() || !t_dir.isDirectory()){
			t_dir.mkdir();
		}		
		
		ServerSocket t_sockTest = null;
		try{
			t_sockTest = (new ServerSocket(t_serverPort));
			t_sockTest.close();
		}catch(Exception e){
			throw new Exception("服务端口" + t_serverPort + "无法开启：" + e.getMessage());
		}
		
		String t_prefix = _accountName + "/";
		WriteSignature(t_prefix,_signature);
		
		fetchThread t_thread = null;
						
		try{				
			WriteIniFile(t_prefix + fetchMgr.fsm_configFilename);
		}catch(Exception e){
			throw new Exception("复制创建" + t_prefix + fetchMgr.fsm_configFilename + "出现问题：" + e.getMessage());
		}			
		
		t_thread = new fetchThread(t_prefix,t_prefix + fetchMgr.fsm_configFilename,
								Long.valueOf(m_expiredTime.getText()).longValue(),(new Date()).getTime(),true);
				
		m_mainFrame.AddAccountThread(t_thread,true);
		m_mainFrame.SelectAccount(t_thread.m_fetchMgr.GetAccountName());

	}

	
	public void itemStateChanged(ItemEvent e){
		if(e.getSource() == m_commonConfigList){
			if(e.getStateChange() == ItemEvent.SELECTED){
				commonConfig t_config = (commonConfig)m_commonConfigData.elementAt(m_commonConfigList.getSelectedIndex());
				t_config.SetConfig(this);
			}
		}
	}
	
	static public void AddTextLabel(JPanel _panel,String _label,JTextField _text,int _length,String _defaultVal){
		JLabel t_label = new JLabel(_label);
		_panel.add(t_label);
		_text.setPreferredSize(new Dimension(_length, 25));
		
		if(_defaultVal.length() != 0){
			_text.setText(_defaultVal);
		}
		
		_panel.add(_text);
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
	
	private void AutoSelectMailHost(){
		String t_account = m_account.getText().toLowerCase();
		final int t_atIndex = t_account.indexOf('@');
		if(t_atIndex != -1){
			
			String t_addr = t_account.substring(t_atIndex + 1);
			if(t_addr.indexOf('.') != -1){
				
				for(int i = 0;i < m_commonConfigData.size();i++){
					commonConfig t_config = (commonConfig)m_commonConfigData.elementAt(i);
					if(t_config.m_host.indexOf(t_addr) != -1){
						t_config.SetConfig(this);
						
						break;
					}
				}
			}
		}
	}
	private void AddCommonConfigList(){
		
		m_commonConfigData.removeAllElements();
		m_commonConfigListModel.removeAllElements();
			
		try{
			Vector t_lines = new Vector();
			fetchMgr.ReadSimpleIniFile("commonMailSvr.ini", "UTF-8", t_lines);
			
			for(int i = 0 ;i < t_lines.size();i++){
				String line = (String)t_lines.elementAt(i);
				if(!fetchMgr.IsEmptyLine(line)){
					try{
						commonConfig t_config = new commonConfig(line);
						m_commonConfigData.addElement(t_config);
						m_commonConfigListModel.addElement(t_config.m_name);
						
					}catch(Exception e){
						JOptionPane.showMessageDialog(this, "读取" + "commonMailSvr.ini " + "出现问题：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
			
			
		}catch(Exception e){
			JOptionPane.showMessageDialog(this, "读取" + "commonMailSvr.ini " + "出现问题：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
		}	

	}
	
	private void WriteSignature(String _prefix,String _signature)throws Exception{
		if(_signature.length() != 0){
			FileOutputStream t_out = new FileOutputStream(_prefix + fetchEmail.fsm_signatureFilename);
			t_out.write(m_signature.getText().getBytes("UTF-8"));
			t_out.flush();
			t_out.close();
		}
	}
	private void WriteIniFile(String _iniFile)throws Exception{
		
		CopyFile(fetchMgr.fsm_configFilename, _iniFile);
		
		BufferedReader in = new BufferedReader(
								new InputStreamReader(
										new FileInputStream(_iniFile),"UTF-8"));
			
		StringBuffer t_contain = new StringBuffer();
		
		String line = null;
		while((line = in.readLine())!= null){
			if(line.indexOf("userFetchIndex=") != -1){
				line = line.replaceAll("userFetchIndex=[^\n]*", "userFetchIndex=0");
			}else{
				line = CheckIniFileLine(line);
			}			
			t_contain.append(line + "\r\n");
		}		
		in.close();
		
				
		FileOutputStream os = new FileOutputStream(_iniFile);
		os.write(t_contain.toString().getBytes("UTF-8"));
		os.flush();
		os.close();
		
	}
	
	private String CheckIniFileLine(String _line){
		if(_line.startsWith("#")){
			return _line;
		}
		
		if(_line.startsWith("\n") || _line.startsWith("\r")){
			return _line;
		}
		
		final Object[][] t_replace = 
		{
			{"account="				,m_account},
			{"password="			,m_password},
			{"protocol="			,m_protocal},
			{"host="				,m_host},
			{"port="				,m_port},
			{"host_send="			,m_send_host},
			{"port_send="			,m_send_port},
			{"userPassword="		,m_userPassword},
			{"serverPort="			,m_serverPort},
			{"pushInterval="		,m_pushInterval},
			{"userSSL="				,m_useSSL},
			{"convertoSimpleChar="	,m_convertToSimple},
			{"useFullNameSignIn="	,m_signInAsFullname},
		};
		
		for(int i = 0;i < t_replace.length;i++){
			String t_segmentName = (String)t_replace[i][0];
			if(_line.startsWith(t_segmentName)){
				if(t_replace[i][1] instanceof JTextField){
					
					_line = _line.replaceFirst(t_segmentName + "[^\n]*", t_segmentName + ((JTextField)t_replace[i][1]).getText());
					
				}else if(t_replace[i][1] instanceof JCheckBox){
					
					JCheckBox t_check = (JCheckBox)t_replace[i][1];
					_line = _line.replaceFirst(t_segmentName + "[^\n]*", t_segmentName + (t_check.isSelected()?"1":"0"));
					
				}else if(t_replace[i][1] instanceof JRadioButton[]){
					
					JRadioButton[] t_radioButton = (JRadioButton[])t_replace[i][1];
					
					String t_replaceRadio = null;
					
					for(int j = 0; j < t_radioButton.length;j++){
						if(t_radioButton[j].isSelected()){
							t_replaceRadio = t_radioButton[j].getText();
							break;
						}
					}
					
					_line = _line.replaceFirst(t_segmentName + "[^\n]*", t_segmentName + t_replaceRadio);
					
				}
				
			}
		}

		return _line;		
		
	}
	
	
	public static void CopyFile(String sourceFile,String targetFile) throws IOException{

		FileInputStream input = new FileInputStream(sourceFile); 
		BufferedInputStream inBuff=new BufferedInputStream(input); 
		 
		FileOutputStream output = new FileOutputStream(targetFile); 
		BufferedOutputStream outBuff=new BufferedOutputStream(output); 
		
		byte[] b = new byte[1024 * 5]; 
		int len; 
		while ((len =inBuff.read(b)) != -1) { 
		    outBuff.write(b, 0, len); 
		}
		
		outBuff.flush(); 

	    inBuff.close(); 
	    outBuff.close(); 
	    output.close(); 
	    input.close(); 
	} 
}
