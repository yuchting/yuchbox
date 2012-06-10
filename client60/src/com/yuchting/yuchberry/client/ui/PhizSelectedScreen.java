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
package com.yuchting.yuchberry.client.ui;

import java.util.Vector;

import local.yblocalResource;
import net.rim.device.api.system.Display;
import net.rim.device.api.system.KeypadListener;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.component.AutoTextEditField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.MainScreen;

import com.yuchting.yuchberry.client.recvMain;

final class PhizMgr extends Manager{
	
	Vector 				m_phizList	= null;
	IPhizSelected		m_selectedCallback = null;
	
	final static int	fsm_phizInterval = 2;
	
	int 				m_move_x = 0;
	int 				m_move_y = 0;
	
	int					m_maxRowNum = 0;
	int					m_maxColNum = 0;
	
	int					m_phizSize = 0;
	
	public PhizMgr(Vector _phizList){
		super(Manager.VERTICAL_SCROLL);
				
		m_phizList = _phizList;
		
		for(int i = 0 ; i < m_phizList.size();i++){
			Phiz t_phiz = (Phiz)m_phizList.elementAt(i);
			add(t_phiz);
		}
		
		m_phizSize = Phiz.fsm_phizSize;
	}
	
	protected void sublayout(int width,int _height){
		
		if(m_phizList.isEmpty()){
			return ;
		}
		
		int t_x = 0; 
		int t_y = 0;
		
		m_maxRowNum = Display.getWidth() / (m_phizSize + fsm_phizInterval);
		m_maxColNum = m_phizList.size() / m_maxRowNum + 1;
		
		if(m_phizList.size() % m_maxRowNum == 0){
			m_maxColNum--;
		}
		
		for(int i = 0;i < m_phizList.size();i++){
			Phiz t_phiz = (Phiz)m_phizList.elementAt(i);
			
			setPositionChild(t_phiz,t_x,t_y);		
			layoutChild(t_phiz,m_phizSize,m_phizSize);
			
			t_x += Phiz.fsm_phizSize + fsm_phizInterval;
			if(t_x + Phiz.fsm_phizSize + fsm_phizInterval > Display.getWidth()){				
				t_x = 0;
				t_y += Phiz.fsm_phizSize + fsm_phizInterval;
			}
		}
		
		setExtent(Display.getWidth(), Display.getHeight() - 1 - getFont().getHeight());
	}
	
	public boolean fieldClicked(int status) {
		
		for(int i = 0 ;i < m_phizList.size();i++){
			
			Phiz t_phiz = (Phiz)m_phizList.elementAt(i);
			
			if(t_phiz.isFocus()){
				
				if(m_selectedCallback != null){
					m_selectedCallback.phizSelected(t_phiz.getPhizName());
				}				
				
				boolean t_shiftDown = (status & KeypadListener.STATUS_SHIFT) != 0;
				
				if(!t_shiftDown){
					getScreen().close();
				}
				
				return true;
			}	
		}
		
		return false;
	}
	
	public void setSelectedCallback(IPhizSelected _callback){
		m_selectedCallback = _callback;
	}
	
	protected boolean navigationClick(int status, int time){
		
		if(fieldClicked(status)){
			return true;
		}
		
		return super.navigationClick(status, time);
	}
	
	protected boolean keyDown(int keycode,int time){
			
		int key = Keypad.key(keycode);
		
		if(key == 10 && fieldClicked(Keypad.status(keycode))){
			return true;
		}
		
		return super.keyDown(keycode,time);
	}
		
	public void resetSelected(){
		m_move_x = m_move_y = 0;
		if(getFieldCount() > 0){
			Phiz t_phiz = (Phiz)getField(0);			
			t_phiz.setFocus();
			((PhizSelectedScreen)getScreen()).setPromptLabel(t_phiz.getPhizName());
		}		
	}
	
	protected boolean navigationMovement(int dx,int dy,int status,int time){
		
		if(m_phizList.isEmpty()){
			return false;
		}
		
		m_move_x += dx;
		if(m_move_x >= m_maxRowNum){
			m_move_x = 0;
			m_move_y++;
		}
		
		if(m_move_x < 0){
			if(m_move_y > 0){
				m_move_y--;
				m_move_x = m_maxRowNum - 1;
			}else{
				m_move_x = 0;
			}			
		}
		
		m_move_y += dy;
		if(m_move_y >= m_maxColNum){
			m_move_y -= dy;
		}
		
		if(m_move_y < 0){
			m_move_y = 0;
		}
		
		int t_index = m_move_y * m_maxRowNum + m_move_x;
		int t_fieldNum = getFieldCount();
		
		Phiz t_phiz = null;
		if(t_index < t_fieldNum){
			t_phiz = (Phiz)getField(t_index);
		}else{
			t_phiz = (Phiz)getField(t_fieldNum - 1);
			m_move_x = t_fieldNum % m_maxRowNum - 1;
			m_move_y = m_maxColNum - 1;
		}
		
		t_phiz.setFocus();
		((PhizSelectedScreen)getScreen()).setPromptLabel(t_phiz.getPhizName());
		
		return true;
	}
	
}

public class PhizSelectedScreen extends MainScreen{

	public static PhizSelectedScreen	sm_phizScreen 	= null;
	
	private AutoTextEditField	m_phizSelectingText = null;
    
    private IPhizSelected		m_phizSelected 	= new IPhizSelected() {
		
		public void phizSelected(String phizName) {
			if(m_phizSelectingText != null){
				int t_position = m_phizSelectingText.getCursorPosition();
				
				String t_text = m_phizSelectingText.getText();
				
				StringBuffer t_final = new StringBuffer();
				if(t_position != 0){
					t_final.append(t_text.substring(0,t_position));
				}
				t_final.append(phizName);
				
				t_final.append(t_text.substring(t_position,t_text.length()));
				
				m_phizSelectingText.setText(t_final.toString());
				m_phizSelectingText.setCursorPosition(t_position + phizName.length());
			}			
		}
	};
	
	PhizMgr		m_phizMgr;
	String		m_promptString = recvMain.sm_local.getString(yblocalResource.WEIBO_PHIZ_SCREEN_PROMPT);
	LabelField	m_promptLabel = new LabelField(m_promptString,Field.NON_FOCUSABLE);
	
	public PhizSelectedScreen(Vector _phizList){
		add(m_promptLabel);
		add(new SeparatorField());
				
		m_phizMgr = new PhizMgr(_phizList);
		add(m_phizMgr);
	}
	
	protected  void	onDisplay(){
		super.onDisplay();
		
		m_phizMgr.resetSelected();
	}
	
	public void setPromptLabel(String _phizName){
		m_promptLabel.setText(m_promptString + _phizName);
	}
	
	public void close(){
		if(m_phizSelectingText != null){
			try{
				// some error cause:
				// IllegalStateException: setFocus called on a field that is not attached to a screen.
				//
				m_phizSelectingText.setFocus();
			}catch(Exception e){
				
			}
		}
		
		super.close();
	}

	
	protected boolean navigationMovement(int dx,int dy,int status,int time){
		return m_phizMgr.navigationMovement(dx, dy, status, time);
	}
	
	static public PhizSelectedScreen getPhizScreen(AutoTextEditField _insertEdit){
		
		if(sm_phizScreen == null){
			sm_phizScreen = new PhizSelectedScreen(recvMain.sm_phizImageList);
		}
		
		sm_phizScreen.preparePhizScreen(_insertEdit);
		
		return sm_phizScreen;
	}
	
	public void preparePhizScreen(AutoTextEditField _insertEdit){
		m_phizSelectingText = _insertEdit;
		m_phizMgr.setSelectedCallback(m_phizSelected);
	}
}
