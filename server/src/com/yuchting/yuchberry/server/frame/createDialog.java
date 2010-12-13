package com.yuchting.yuchberry.server.frame;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
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
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

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
	
	final static int		fsm_width = 300;
	final static int		fsm_height = 630;
	
	class commonConfig{
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
	
	mainFrame	m_mainFrame = null;
	
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
	
	
	JTextField	m_userPassword		= new JTextField();
	JTextField	m_serverPort		= new JTextField();
	
	JTextField	m_pushInterval		= new JTextField();
	
	JCheckBox	m_useSSL			= new JCheckBox("Push使用SSL加密");
	JCheckBox	m_convertToSimple	= new JCheckBox("转换繁体为简体");
	JTextField	m_expiredTime		= new JTextField();
	
	JTextArea	m_signature			= new JTextArea();
	
	JButton		m_confirmBut		= new JButton("确定");
	
	public createDialog(mainFrame _main,String _formerHost,String _formerPort,
										String _formerHost_send,String _formerPort_send,
										String _userPassword,String _serverPort,String _pushInterval,String _expiredTime){
		
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
		
		JLabel t_label = new JLabel("常用配置：");
		t_label.setPreferredSize(new Dimension(80,25));
		getContentPane().add(t_label);
		m_commonConfigList.setPreferredSize(new Dimension(200, 25));
		getContentPane().add(m_commonConfigList);
		
		JSeparator	t_separator			= new JSeparator();
		t_separator.setPreferredSize(new Dimension(fsm_width, 5));
		getContentPane().add(t_separator);
		
		AddTextLabel("帐号名称:",m_account,220,"");
		AddTextLabel("帐号密码:",m_password,220,"");
		AddTextLabel("主机地址:",m_host,120,_formerHost);
		AddTextLabel("端口:",m_port,60,_formerPort);

		getContentPane().add(new JLabel("协议:"));
		for(int i = 0;i < m_protocal.length;i++){
			m_protocalGroup.add(m_protocal[i]);
			getContentPane().add(m_protocal[i]);
		}
		
		AddTextLabel("发送主机地址:",m_send_host,100,_formerHost_send);
		AddTextLabel("端口:",m_send_port,60,_formerPort_send);
		
		t_separator = new JSeparator();
		t_separator.setPreferredSize(new Dimension(fsm_width, 5));
		getContentPane().add(t_separator);
		
		AddTextLabel("用户密码:",m_userPassword,100,_userPassword);
		AddTextLabel("用户端口:",m_serverPort,60,_serverPort);
		
		AddTextLabel("过期时间(单位小时，0为不过期):",m_expiredTime,90,_expiredTime);
		
		AddTextLabel("推送间隔（秒）：",m_pushInterval,180,_pushInterval);
		m_useSSL.setPreferredSize(new Dimension(fsm_width - 20, 25));
		getContentPane().add(m_useSSL);
		
		m_convertToSimple.setPreferredSize(new Dimension(fsm_width - 20,25));
		getContentPane().add(m_convertToSimple);
		
		t_label = new JLabel("签名：");
		t_label.setPreferredSize(new Dimension(fsm_width - 20,25));
		
		getContentPane().add(t_label);
		m_signature.setPreferredSize(new Dimension(fsm_width - 15, 170));
		m_signature.setText("send from my Blackberry-----\nPowered by yuchberry");
		getContentPane().add(m_signature);
		
		t_separator			= new JSeparator();
		t_separator.setPreferredSize(new Dimension(fsm_width, 5));
		getContentPane().add(t_separator);
		
		getContentPane().add(m_confirmBut);
		m_confirmBut.addActionListener(this);
		
		AutoSelectProtocal();
		
		AddCommonConfigList();
		
		setVisible(true);	
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
		if(e.getSource() == m_confirmBut){

			if(m_account.getText().length() == 0 || m_password.getText().length() == 0){
				JOptionPane.showMessageDialog(this, "账户名、密码不能为空", "错误", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			if(m_host.getText().length() == 0 || m_port.getText().length() == 0){
				JOptionPane.showMessageDialog(this, "邮件接受服务器地址、端口不能为空", "错误", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			if(m_send_host.getText().length() == 0 || Integer.valueOf(m_send_port.getText()).intValue() <= 0){
				JOptionPane.showMessageDialog(this, "邮件发送服务器地址不能为空，端口非法", "错误", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			final int t_listenPort = 3000;
			if(m_userPassword.getText().length() == 0 || Integer.valueOf(m_serverPort.getText()).intValue() <= t_listenPort){
				JOptionPane.showMessageDialog(this, "用户密码不能为空，监听yuchberry端口不能小于 " + t_listenPort, "错误", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			final int t_minPushInterval = 2;
			if(Integer.valueOf(m_pushInterval.getText()).intValue() <= t_minPushInterval){
				JOptionPane.showMessageDialog(this, "推送间隔不能小于 " + t_minPushInterval, "错误", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			if(CreateAccountAndTest()){
				setVisible(false);
				dispose();	
			}
		}
	}
	
	public void itemStateChanged(ItemEvent e){
		if(e.getSource() == m_commonConfigList){
			if(e.getStateChange() == ItemEvent.SELECTED){
				commonConfig t_config = (commonConfig)m_commonConfigData.elementAt(m_commonConfigList.getSelectedIndex());
				t_config.SetConfig(this);
			}
		}
	}
	
	private void AddTextLabel(String _label,JTextField _text,int _length,String _defaultVal){
		JLabel t_label = new JLabel(_label);
		getContentPane().add(t_label);
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
			BufferedReader in = new BufferedReader(
					new InputStreamReader(
							new FileInputStream("commonMailSvr.ini")));

			String line = null;
			while((line = in.readLine())!= null){
				if(!line.startsWith("#") && !fetchMgr.IsEmptyLine(line)){
					try{
						commonConfig t_config = new commonConfig(line);
						m_commonConfigData.addElement(t_config);
						m_commonConfigListModel.addElement(t_config.m_name);
						
					}catch(Exception e){
						JOptionPane.showMessageDialog(this, "读取" + "commonMailSvr.ini " + "出现问题：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
					}				
					
					line = line.replaceAll("userFetchIndex=[^\n]*", "userFetchIndex=0");
				}
			}
			
			in.close();
			
		}catch(Exception e){
			JOptionPane.showMessageDialog(this, "读取" + "commonMailSvr.ini " + "出现问题：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
		}	

	}
	
	private boolean CreateAccountAndTest(){
		
		File t_dir = new File(m_account.getText());
		if(!t_dir.exists() || !t_dir.isDirectory()){
			t_dir.mkdir();
		}
				
		final int t_serverPort = Integer.valueOf(m_serverPort.getText()).intValue();
		
		if(m_mainFrame.SearchAccountThread(m_account.getText(),t_serverPort) != null){
			JOptionPane.showMessageDialog(this,m_account.getText() + " 账户重复，或者服务端口" + t_serverPort + "已经被使用" , "错误", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		ServerSocket t_sockTest = null;
		try{
			t_sockTest = (new ServerSocket(t_serverPort));
			t_sockTest.close();
		}catch(Exception e){
			JOptionPane.showMessageDialog(this,"服务端口" + t_serverPort + "无法开启：" + e.getMessage() , "错误", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		String t_prefix = m_account.getText() + "/";		
		WriteSignature(t_prefix);
		
		fetchThread t_thread = null;

		try{
						
			try{
				
				CopyFile("config.ini" , t_prefix + "config.ini");
				
				WriteIniFile(t_prefix + "config.ini");				
			}catch(Exception e){
				JOptionPane.showMessageDialog(this, "复制创建" + t_prefix + "config.ini" + "出现问题：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
				return false;
			}			
			
			t_thread = new fetchThread(t_prefix,t_prefix + "config.ini",
									Long.valueOf(m_expiredTime.getText()).longValue(),(new Date()).getTime());
			
		}catch(Exception e){
			JOptionPane.showMessageDialog(this,e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
			return false;
		}
				
		m_mainFrame.AddAccountThread(t_thread,true);
				
		return true;
	}
	
	private void WriteSignature(String _prefix){
		try{
			
			if(m_signature.getText().length() != 0){
				FileOutputStream t_out = new FileOutputStream(_prefix + fetchMgr.fsm_signatureFilename);
				t_out.write(m_signature.getText().getBytes("GB2312"));
				t_out.flush();
				t_out.close();
			}
			
		}catch(Exception e){}
	}
	private void WriteIniFile(String _iniFile)throws Exception{
		
		BufferedReader in = new BufferedReader(
								new InputStreamReader(
										new FileInputStream(_iniFile)));
			
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
		os.write(t_contain.toString().getBytes("GB2312"));
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
			{"account=",m_account},
			{"password=",m_password},
			{"protocol=",m_protocal},
			{"host=",m_host},
			{"port=",m_port},
			{"host_send=",m_send_host},
			{"port_send=",m_send_port},
			{"userPassword=",m_userPassword},
			{"serverPort=",m_serverPort},
			{"pushInterval=",m_pushInterval},
			{"userSSL=",m_useSSL},
			{"convertoSimpleChar=",m_convertToSimple},
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
