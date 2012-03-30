package com.yuchting.yuchberry.server.frame;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Random;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import weibo4j.http.PostParameter;

public class MassMailFrame extends JFrame implements ActionListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6771140126432612877L;
	
	final static int 	fsm_width 	= 800;
	final static int	fsm_height	= 600;
	
	String m_adminPass = null;
	
	JLabel		m_state				= new JLabel();
	JButton 	m_sendBtn 			= new JButton("发送");
	JButton 	m_stopBtn 			= new JButton("停止发送");
	
	JTextArea	m_mailAddr			= new JTextArea();
	JScrollPane	m_mailAddrScroll	= new JScrollPane(m_mailAddr);
	JTextField	m_mailSub			= new JTextField("语盒服务提醒");
	JTextArea	m_mailContain		= new JTextArea("您好！\n\n" +
													"您收到这封邮件，是因为您在语盒官方网站 http://www.yuchs.com 注册了账号，并成功添加了推送账户。\n\n\n\n" +
													"再次感谢您的支持和关注，如有打扰，敬请谅解！有任何疑问，请回复这封邮件让我知道，我会尽快答复您。\n\n" +
													"--\n语盒开发者敬上！");
	
	JProgressBar m_progress 		= new JProgressBar();
	
	String[]	m_mailList = null;
	
	public MassMailFrame(){
		LoadYuchsign();
		
		setTitle("语盒(Yuchs'Box)管理员群发邮件工具 beta");
		setSize(fsm_width,fsm_height);
		
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		
		getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));
			
		m_state.setAlignmentX(Component.LEFT_ALIGNMENT);	
		m_state.setPreferredSize(new Dimension(fsm_width, 25));
		m_mailAddrScroll.setPreferredSize(new Dimension(fsm_width, 200));
		m_mailContain.setPreferredSize(new Dimension(fsm_width, 335));
		m_progress.setPreferredSize(new Dimension(fsm_width, 25));
		
		JPanel t_btnPanel = new JPanel();
		t_btnPanel.setLayout(new BoxLayout(t_btnPanel, BoxLayout.X_AXIS));
		t_btnPanel.setPreferredSize(new Dimension(fsm_width, 25));
		t_btnPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		t_btnPanel.add(m_sendBtn);
		t_btnPanel.add(m_stopBtn);
		
		m_stopBtn.setEnabled(false);
		
		m_sendBtn.addActionListener(this);
		m_stopBtn.addActionListener(this);
		
		getContentPane().add(m_state);
		
		// mail address 
		JSeparator	t_tableSeparator	= new JSeparator();
		t_tableSeparator.setPreferredSize(new Dimension(fsm_width,3));
		getContentPane().add(t_tableSeparator);
		getContentPane().add(m_mailAddrScroll);

		// subject address
		t_tableSeparator	= new JSeparator();
		t_tableSeparator.setPreferredSize(new Dimension(fsm_width,3));
		getContentPane().add(t_tableSeparator);
		getContentPane().add(m_mailSub);

		// contain address
		t_tableSeparator	= new JSeparator();
		t_tableSeparator.setPreferredSize(new Dimension(fsm_width,3));
		getContentPane().add(t_tableSeparator);
		getContentPane().add(m_mailContain);
		
		getContentPane().add(t_btnPanel);		
		getContentPane().add(m_progress);
		
		m_mailAddr.getDocument().addDocumentListener(new DocumentListener() {
			
			public void removeUpdate(DocumentEvent e) {
				changedUpdate(e);
			}
			public void insertUpdate(DocumentEvent e) {
				changedUpdate(e);
			}
			
			public void changedUpdate(DocumentEvent e){
				m_mailList = m_mailAddr.getText().split("\n");
				int t_illegalNum = 0;
				for(String add:m_mailList){
					if(!add.isEmpty() && !add.matches("^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$")){
						t_illegalNum++;
					}
				}
				
				m_state.setText("邮件地址数量：" + m_mailList.length + " （非法数量：" + t_illegalNum + ")");
			}
		});
	}
	
	private void LoadYuchsign(){
		
		try{
			BufferedReader in = new BufferedReader(
									new InputStreamReader(
										new FileInputStream("yuchsign"),"UTF-8"));		
			try{
				m_adminPass 	= in.readLine();				
			}finally{
				in.close();
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	boolean m_stopSend = false;
	Thread m_sendThread = null;
	
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == m_sendBtn){
			
			if(m_mailList == null || m_mailList.length == 0){
				JOptionPane.showMessageDialog(this, "没有收件地址", "错误", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			final String t_sub = m_mailSub.getText();
			if(t_sub.isEmpty()){
				JOptionPane.showMessageDialog(this, "没有邮件标题", "错误", JOptionPane.ERROR_MESSAGE);
				return ;
			}
			
			final String t_body = m_mailContain.getText(); 
			if(t_body.isEmpty()){
				JOptionPane.showMessageDialog(this, "没有邮件内容", "错误", JOptionPane.ERROR_MESSAGE);
				return ;
			}		
			
			if(m_sendThread == null){
							
				synchronized (this) {
					
					m_stopBtn.setEnabled(true);
					m_sendBtn.setEnabled(false);					
					
					m_sendThread = new Thread(){
						public void run(){
							
							m_stopSend = false;
							
							m_progress.setMaximum(m_mailList.length);
							
							int t_count = 0;
							int t_tryCount = 0;
							for(String t_addr:m_mailList){
								
								t_count++;
								
								if(!t_addr.isEmpty() && !t_addr.matches("^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$")){
									continue;
								}
								
								m_progress.setValue(t_count);
								m_state.setText("("+ t_count + "/" + m_mailList.length + ")正在发送邮件至" + t_addr + "...");
								
								t_tryCount = 0;
								while(t_tryCount++ < 5 && !m_stopSend){

									try{
										if(sendMail(t_addr,t_body,t_sub,m_adminPass).indexOf("OK") != -1){
											break;
										}
									}catch(Exception e){
										m_state.setText("发送邮件至" + t_addr + " 失败: " + e.getMessage());
										
										try{
											sleep(10000);
										}catch(Exception ex){}
										
									}
								}
								
								if(m_stopSend){
									break;
								}
							}
							
							synchronized (MassMailFrame.this) {
								m_sendThread = null;
								m_stopBtn.setEnabled(false);
								m_sendBtn.setEnabled(true);
								
								JOptionPane.showMessageDialog(MassMailFrame.this, "发送完毕", "提示", JOptionPane.INFORMATION_MESSAGE);
							}
							
						}
					};
					
					m_sendThread.start();
				}
			}
		}else if(e.getSource() == m_stopBtn){
			if(m_sendThread != null){
				m_stopSend = true;
				m_sendThread.interrupt();
			}
		}
	}
	
	private static String sendMail(String _to,String _body,String _sub,String _pass)throws Exception{
		
		PostParameter[] t_params = 
		{
			new PostParameter("to", URLEncoder.encode(_to, "UTF-8")),
			new PostParameter("body", URLEncoder.encode(_body, "UTF-8")),
			new PostParameter("sub", URLEncoder.encode(_sub, "UTF-8")),
			new PostParameter("pass", _pass),
			new PostParameter("rand", new Random().nextInt()),
		};
		  
		return requestURL("http://api.yuchs.com/ssm.php", "POST", null, t_params);
	}
	
	public static PostParameter[] parseURLParam(String _urlParams){
		String[] t_arr = _urlParams.split("&");
		PostParameter[] ret = new PostParameter[t_arr.length];
		
		for(int i = 0;i < t_arr.length;i++){
			String[] parse = t_arr[i].split("=");
			ret[i] = new PostParameter(parse[0],parse[1]);
		}
		return ret;
	}
	
	public static String encodeURLParam(PostParameter[] _params){
		StringBuffer ret= new StringBuffer();
		for(int i = 0;i<_params.length;i++){
			PostParameter par = _params[i];
			ret.append(par.getName()).append("=").append(par.getValue());
			if(i + 1 < _params.length){
				ret.append("&");
			}
		}
		return ret.toString();
	}
	
	public static String requestURL(String _url,String _method,PostParameter[] _headerParams,PostParameter[] _urlParams)throws Exception{
		
		if(_method.equals("GET")){
			_url = _url + "?" + encodeURLParam(_urlParams);
		}
		
		URL url = new URL(_url);
		HttpURLConnection con = (HttpURLConnection)url.openConnection();
		try{
			con.setDoInput(true);
			if(_method.equals("POST")){
				con.setRequestMethod("POST");
				con.setDoOutput(true);
			}
			
			con.setAllowUserInteraction(false);

			if(_headerParams != null && _headerParams.length != 0){
				for(PostParameter par : _headerParams){
					con.setRequestProperty(par.getName(),par.getValue());
				}
			}
			
			if(_method.equals("POST")){
				String t_params = encodeURLParam(_urlParams);
				byte[] bytes = t_params.getBytes("UTF-8");
				
				con.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
				con.setRequestProperty("Content-Length",Integer.toString(bytes.length));
				
				OutputStream t_os = con.getOutputStream();
				try{
					t_os.write(bytes);
					t_os.flush();
				}finally{				
					t_os.close();
				}
			}
			
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
			try{				
				return in.readLine();
			}finally{
				in.close();
			}
		}finally{
			con.disconnect();
		}
	}
	
	public static void main(String[] _arg)throws Exception{
		new MassMailFrame().setVisible(true);
	}
}
