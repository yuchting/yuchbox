package com.yuchting.yuchberry.server.frame;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
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
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import com.yuchting.yuchberry.server.Logger;
import com.yuchting.yuchberry.server.fetchAccount;
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

final class createEmailData{
	
	// account attribute
	String m_accountName	;
	String m_password		;
	String m_protocal		;
	
	String m_host			;
	String m_port			;
	
	String m_send_host		;
	String m_send_port		;
	
	boolean m_appendHTML	;
	boolean m_useFullNameSignIn;
}

final class mainAttrData{
	
	// main attribute
	String m_serverProt		;
	String m_signature		;
	
	String m_pushInterval	;
	String m_userPassword	;
	
	boolean m_useSSL		;
		
	boolean m_convertToSimple;
}

public class createDialog extends JDialog implements DocumentListener,
															ActionListener,
															ItemListener{
	
	final static int		fsm_width = 520;
	final static int		fsm_height = 620;
	
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
	
	
	DefaultListModel m_accountlistModel = new DefaultListModel();
	JList		m_accountList		= new JList(m_accountlistModel);
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
	JCheckBox	m_appendHTML		= new JCheckBox("追加HTML到正文");
	
	JButton		m_confirmBut		= new JButton("确定");
	
	// write xml data
	Document	m_createConfigDoc	= DocumentFactory.getInstance().createDocument();
	Element		m_createConfigDoc_root = m_createConfigDoc.addElement("Yuchberry");
	
	Vector		m_createAccountList	= new Vector();

	fetchMgr	m_fetchMgrCreate	= new fetchMgr();
	
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
		t_mainPanel.setPreferredSize(new Dimension(fsm_width, 255));
				
		AddTextLabel(t_mainPanel,"用户密码:",m_userPassword,100,_userPassword);
		AddTextLabel(t_mainPanel,"用户端口:",m_serverPort,60,_serverPort);
		AddTextLabel(t_mainPanel,"推送间隔(秒):",m_pushInterval,60,_pushInterval);
		AddTextLabel(t_mainPanel,"过期时间(单位小时，0为不过期):",m_expiredTime,80,_expiredTime);
		
		m_useSSL.setPreferredSize(new Dimension(250, 20));
		t_mainPanel.add(m_useSSL);
		t_mainPanel.add(m_convertToSimple);
		
		JLabel t_label = new JLabel("签名：");
		t_label.setPreferredSize(new Dimension(fsm_width - 20,20));
		
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
		t_accountMainPanel.setPreferredSize(new Dimension(fsm_width, 280));
		
		m_accountListScroll.setPreferredSize(new Dimension(140,270));
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
		
		m_signInAsFullname.setPreferredSize(new Dimension(fsm_width, 20));
		t_accountPanel.add(m_signInAsFullname);
		
		m_appendHTML.setPreferredSize(new Dimension(fsm_width, 20));
		t_accountPanel.add(m_appendHTML);

		
		m_tabbedPane.addTab("邮件",null,t_accountPanel,"添加邮件账户");
		m_tabbedPane.setPreferredSize(new Dimension(300, 270));
		
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
						
			createEmailData t_email = new createEmailData();
			
			t_email.m_accountName	= m_account.getText();
			t_email.m_password		= m_password.getText();
			t_email.m_host			= m_host.getText();
			t_email.m_port			= m_port.getText();
			t_email.m_send_host		= m_send_host.getText();
			t_email.m_send_port		= m_send_port.getText();
			t_email.m_appendHTML	= m_appendHTML.isSelected();
			t_email.m_useFullNameSignIn = m_signInAsFullname.isSelected();
			
			for(int i = 0;i < m_protocal.length;i++){
				if(m_protocal[i].isSelected()){
					t_email.m_protocal = m_protocal[i].getText();
					break;
				}
			}
						
			try{
				
				CreateAccountAndTest(t_email);	
				
			}catch(Exception ex){
				JOptionPane.showMessageDialog(this, ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
			}			
			
		}else if(e.getSource() == m_delAccountBut){
			final int t_selectIndex = m_accountList.getSelectedIndex();
			if(t_selectIndex != -1){
				
				fetchAccount t_account = (fetchAccount)m_createAccountList.elementAt(t_selectIndex);
				
				if(JOptionPane.showConfirmDialog(this,"删除这个 <"+ t_account.GetAccountName() + "> 账户？", 
				    "删除？", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION){
					
					return;
				}
				
				Iterator elementIterator = m_createConfigDoc_root.elementIterator();
				while(elementIterator.hasNext()){
					Element element = (Element)elementIterator.next();
					if(element.attributeValue("account").equals(t_account.GetAccountName())){
						
						m_createConfigDoc_root.remove(element);
						break;
					}
			    }
				
				m_createAccountList.removeElementAt(t_selectIndex);
				
				RefreshAccountList();
			}
		}else if(e.getSource() == m_confirmBut){
			
			fetchAccount t_account = (fetchAccount)m_createAccountList.elementAt(0);
			try{
				
				String t_prefix = t_account.GetAccountName() + "/";
				WriteXmlFile(m_createConfigDoc,t_prefix + fetchMgr.fsm_configFilename);
				
				m_mainFrame.AddAccountThread(new fetchThread(m_fetchMgrCreate,t_prefix,
															Long.valueOf(m_expiredTime.getText()).longValue(),
															(new Date()).getTime(),false),
											true);
				
				setVisible(false);
				dispose();
				
			}catch(Exception ex){
				JOptionPane.showMessageDialog(this, ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
			}
						
		}
	}
	
	private void CheckMainAttr()throws Exception{
		mainAttrData t_mainData = new mainAttrData();
		
		t_mainData.m_userPassword		= m_userPassword.getText();
		t_mainData.m_serverProt			= m_serverPort.getText();
		t_mainData.m_pushInterval		= m_pushInterval.getText();
		t_mainData.m_useSSL				= m_useSSL.isSelected();
		t_mainData.m_convertToSimple	= m_convertToSimple.isSelected();
		t_mainData.m_signature			= m_signature.getText();
		
		
		CheckMainAttr(t_mainData);
	}
	
	public void CheckMainAttr(final mainAttrData _data)throws Exception{
		
		if(_data.m_userPassword.length() == 0){
			throw new Exception("用户密码不能为空");
		}
		
		final int t_serverPort = Integer.valueOf(_data.m_serverProt).intValue();
		final int t_listenPort = 3000;
		
		if(t_serverPort <= t_listenPort){
			throw new Exception("监听yuchberry端口不能小于 " + t_listenPort);
		}
		
		if(m_mainFrame.SearchAccountThread("",t_serverPort) != null){
			throw new Exception("服务端口" + t_serverPort + "已经被使用");
		}
		
		final int t_minPushInterval = 2;
		if(Integer.valueOf(_data.m_pushInterval).intValue() <= t_minPushInterval){
			throw new Exception("推送间隔不能小于 " + t_minPushInterval);
		}
		
		ServerSocket t_sockTest = null;
		try{
			t_sockTest = (new ServerSocket(t_serverPort));
			t_sockTest.close();
		}catch(Exception e){
			throw new Exception("服务端口" + t_serverPort + "无法开启：" + e.getMessage());
		}
					
		m_createConfigDoc_root.addAttribute("userPassword", _data.m_userPassword);
		m_createConfigDoc_root.addAttribute("serverPort", _data.m_serverProt);
		m_createConfigDoc_root.addAttribute("pushInterval", _data.m_pushInterval);
		m_createConfigDoc_root.addAttribute("userSSL", _data.m_useSSL?"1":"0");
		m_createConfigDoc_root.addAttribute("convertoSimpleChar", _data.m_convertToSimple?"1":"0");
		
		if(m_fetchMgrCreate.GetPrefixString() == null){
			
			final String t_tmpCreateDir = "tmpCreate/";
			File t_tmpFile = new File(t_tmpCreateDir);
			if(!t_tmpFile.exists()){
				t_tmpFile.mkdir();
			}
			
			WriteXmlFile(m_createConfigDoc,t_tmpCreateDir + fetchMgr.fsm_configFilename);
			
			m_fetchMgrCreate.InitConnect(t_tmpCreateDir,new Logger(t_tmpCreateDir));
		}
		
	}
		
	private void CreateAccountAndTest(final createEmailData _email)throws Exception{
		
		CheckMainAttr();
		
		if(_email.m_accountName.length() == 0 || _email.m_password.length() == 0){
			throw new Exception("账户名、密码不能为空");
		}
		
		if(_email.m_host.length() == 0 || _email.m_port.length() == 0){
			throw new Exception("邮件接受服务器地址、端口不能为空");
		}
		
		if(_email.m_send_host.length() == 0 || Integer.valueOf(_email.m_send_port).intValue() <= 0){
			throw new Exception("邮件发送服务器地址不能为空，端口非法");
		}		

		if(m_mainFrame.SearchAccountThread(_email.m_accountName,0) != null){
			throw new Exception(_email.m_accountName + " 账户重复");
		}	
		
		if(m_createAccountList.isEmpty()){
			File t_dir = new File(m_account.getText());
			if(!t_dir.exists() || !t_dir.isDirectory()){
				t_dir.mkdir();
			}	
		}else{
			for(int i = 0;i < m_createAccountList.size();i++){
				fetchAccount account = (fetchAccount)m_createAccountList.elementAt(i);
				if(account.GetAccountName().equals(_email.m_accountName)){
					throw new Exception(_email.m_accountName + " 账户已经添加");
				}				
			}
		}
		
		m_createAccountList.addElement(CheckEmailConnect(_email));
		
		RefreshAccountList();
	}
	
	private void RefreshAccountList(){
		m_accountlistModel.removeAllElements();
		
		for(int i = 0 ;i < m_createAccountList.size();i++){
			fetchAccount t_account = (fetchAccount)m_createAccountList.elementAt(i);
			m_accountlistModel.addElement(t_account);
		}
	}

	public fetchEmail CheckEmailConnect(final createEmailData _email)throws Exception{
		
		Element t_elem = DocumentFactory.getInstance().createDocument().addElement("EmailAccount");
		t_elem.addAttribute("account", _email.m_accountName);
		t_elem.addAttribute("password", _email.m_password);
		
		t_elem.addAttribute("useFullNameSignIn", _email.m_useFullNameSignIn?"1":"0");
		t_elem.addAttribute("protocol", _email.m_protocal);
		t_elem.addAttribute("host", _email.m_host);
		t_elem.addAttribute("port", _email.m_port);
		
		t_elem.addAttribute("protocol_send", "smtp");
		t_elem.addAttribute("host_send", _email.m_send_host);
		t_elem.addAttribute("port_send", _email.m_send_port);
		t_elem.addAttribute("appendHTML", _email.m_appendHTML?"1":"0");
				
		fetchEmail t_emailAccount = new fetchEmail(m_fetchMgrCreate);
		t_emailAccount.InitAccount(t_elem);
		
		t_emailAccount.ResetSession(true);
		t_emailAccount.DestroySession();
		
		m_createConfigDoc_root.add((Element)t_elem.clone());
		
		return t_emailAccount;
	}
	
	public void itemStateChanged(ItemEvent e){
		if(e.getSource() == m_commonConfigList){
			if(e.getStateChange() == ItemEvent.SELECTED){
				commonConfig t_config = (commonConfig)m_commonConfigData.elementAt(m_commonConfigList.getSelectedIndex());
				t_config.SetConfig(this);
			}
		}
	}
	
	static public void AddTextLabel(Container _panel,String _label,JTextField _text,int _length,String _defaultVal){
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
	
	static public  void WriteSignature(String _prefix,String _signature)throws Exception{
		
		if(_signature.length() != 0){
			FileOutputStream t_out = new FileOutputStream(_prefix + fetchEmail.fsm_signatureFilename);
			t_out.write(_signature.getBytes("UTF-8"));
			t_out.flush();
			t_out.close();
		}
	}
	
	static public void WriteXmlFile(Document _doc,String _iniFile)throws Exception{
				 
		OutputFormat outformat = OutputFormat.createPrettyPrint();
		outformat.setEncoding("UTF-8");
		XMLWriter writer = new XMLWriter(new FileOutputStream(_iniFile), outformat);
		writer.write(_doc);
		writer.flush();
		writer.close();		
	}	
	
	static public void CopyFile(String sourceFile,String targetFile) throws IOException{

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
