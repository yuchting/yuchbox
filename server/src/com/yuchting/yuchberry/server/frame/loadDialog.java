package com.yuchting.yuchberry.server.frame;

import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.AbstractButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

public class loadDialog extends JDialog{

	JLabel			m_state = new JLabel();
	JLabel			m_state1 = new JLabel();
	JProgressBar	m_progress = new JProgressBar();
	
	mainFrame		m_mainFrame = null;
	
	final static int		fsm_width = 300;
	final static int		fsm_height = 110;
	
	public loadDialog(mainFrame _main){
		super(_main,"正在读取帐户...",true);
		
		setResizable(false);
		getContentPane().setLayout(new FlowLayout());
		
		setSize(fsm_width,fsm_height);
		setLocation(_main.getLocation().x + (_main.getWidth()- fsm_width) / 2,
					_main.getLocation().y + (_main.getHeight() -  fsm_height) / 2);
		
		m_state.setPreferredSize(new Dimension(fsm_width - 10, 20));
		getContentPane().add(m_state);
		
		m_state1.setPreferredSize(new Dimension(fsm_width - 10, 20));
		m_state1.setHorizontalTextPosition(AbstractButton.CENTER);
		getContentPane().add(m_state1);
		
		m_progress.setPreferredSize(new Dimension(fsm_width - 10, 25));
		getContentPane().add(m_progress);
				
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
	}
}
