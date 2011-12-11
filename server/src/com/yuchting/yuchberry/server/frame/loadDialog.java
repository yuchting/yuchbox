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
		super(_main,"正在读取账户信息...",true);
		
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
