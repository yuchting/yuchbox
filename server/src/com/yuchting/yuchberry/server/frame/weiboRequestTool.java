package com.yuchting.yuchberry.server.frame;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import weibo4j.http.AccessToken;

import com.yuchting.yuchberry.server.fetchSinaWeibo;
import com.yuchting.yuchberry.server.fetchTWeibo;
import com.yuchting.yuchberry.server.fetchWeibo;

public class weiboRequestTool extends JFrame implements ActionListener{

	JButton		m_openRequestURL	= new JButton("请求授权");
	JButton		m_genToken			= new JButton("生成令牌");
	
	JTextField	m_pin				= new JTextField();
	
	JTextField	m_accessToken		= new JTextField();
	JTextField	m_secretToken		= new JTextField();

	Object		m_requestToken	= null;
	
	int			m_style				= 0;
	
	public weiboRequestTool(int _style){

		m_style = _style;
		
		setTitle("请求weibo访问");
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setSize(400,170);
		setResizable(false);
		
		
		Container t_con = getContentPane();
		t_con.setLayout(new FlowLayout(FlowLayout.CENTER));
		
		m_openRequestURL.setPreferredSize(new Dimension(160,40));
		m_genToken.setPreferredSize(new Dimension(160,40));
		
		t_con.add(m_openRequestURL);
		t_con.add(m_genToken);
		
		m_openRequestURL.addActionListener(this);
		m_genToken.addActionListener(this);		
		
		m_accessToken.setEditable(false);
		m_secretToken.setEditable(false);
		
		createDialog.AddTextLabel(t_con,"应用授权码:",m_pin,300,"");
		createDialog.AddTextLabel(t_con,"访问令牌(accessToken):",m_accessToken,230,"");
		createDialog.AddTextLabel(t_con,"密码令牌(secretToken):",m_secretToken,233,"");
				
		setVisible(true);
	}
	
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == m_openRequestURL){
			
			try{
				if(m_requestToken == null){
					if(m_style == fetchWeibo.SINA_WEIBO_STYLE){
						m_requestToken = (new fetchSinaWeibo(null)).getRequestToken();
					}else if(m_style == fetchWeibo.TWITTER_WEIBO_STYLE){
						m_requestToken = (new fetchTWeibo(null)).getRequestToken();
					}
					
				}
				
				if(m_style == fetchWeibo.SINA_WEIBO_STYLE){
					mainFrame.OpenURL(((weibo4j.http.RequestToken)m_requestToken).getAuthorizationURL());
				}else if(m_style == fetchWeibo.TWITTER_WEIBO_STYLE){
					mainFrame.OpenURL(((twitter4j.auth.RequestToken)m_requestToken).getAuthorizationURL());
				}else{
					assert false;
				}
				
				
				JOptionPane.showMessageDialog(this,"请登录weibo，然后把【应用授权码】复制过来，再点击【生成令牌】按钮", "提示", JOptionPane.PLAIN_MESSAGE);
				
			}catch(Exception ex){
				JOptionPane.showMessageDialog(this,"出现错误:" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
			}
			
		}else if(e.getSource() == m_genToken){
			
			try{
				if(m_requestToken == null || m_pin.getText().isEmpty()){
					JOptionPane.showMessageDialog(this,"请先点击【请求授权】按钮，获得授权码", "提示", JOptionPane.PLAIN_MESSAGE);
					return;
				}
				
				if(m_style == fetchWeibo.SINA_WEIBO_STYLE){

					weibo4j.http.AccessToken accessToken = ((weibo4j.http.RequestToken)m_requestToken).getAccessToken(m_pin.getText());
					
					m_accessToken.setText(accessToken.getToken());
					m_secretToken.setText(accessToken.getTokenSecret());
					
				}else if(m_style == fetchWeibo.TWITTER_WEIBO_STYLE){

					twitter4j.auth.AccessToken accessToken = (new fetchTWeibo(null)).getTwitter()
									.getOAuthAccessToken((twitter4j.auth.RequestToken)m_requestToken,m_pin.getText());
					
					m_accessToken.setText(accessToken.getToken());
					m_secretToken.setText(accessToken.getTokenSecret());
				}else{
					assert false;
				}
				
				
				
			}catch(Exception ex){
				JOptionPane.showMessageDialog(this,"出现错误:" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	static public void main(String _arg[]){
		new weiboRequestTool(fetchWeibo.TWITTER_WEIBO_STYLE);
	}
}
