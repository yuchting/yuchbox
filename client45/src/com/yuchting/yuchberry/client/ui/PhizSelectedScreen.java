package com.yuchting.yuchberry.client.ui;

import java.util.Vector;

import local.localResource;
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
	
	public PhizMgr(Vector _phizList){
		super(Manager.VERTICAL_SCROLL);
				
		m_phizList = _phizList;
		
		for(int i = 0 ; i < m_phizList.size();i++){
			Phiz t_phiz = (Phiz)m_phizList.elementAt(i);
			add(t_phiz);
		}
	}
	
	protected void sublayout(int width,int _height){
				
		int t_x = 0;
		int t_y = 0;
		
		m_maxRowNum = recvMain.fsm_display_width / (Phiz.fsm_phizSize + fsm_phizInterval);
		m_maxColNum = m_phizList.size() / m_maxRowNum + 1;
		
		if(m_phizList.size() % m_maxRowNum == 0){
			m_maxColNum--;
		}
		
		for(int i = 0;i < m_phizList.size();i++){
			Phiz t_phiz = (Phiz)m_phizList.elementAt(i);
			
			setPositionChild(t_phiz,t_x,t_y);		
			layoutChild(t_phiz,Phiz.fsm_phizSize,Phiz.fsm_phizSize);
			
			t_x += Phiz.fsm_phizSize + fsm_phizInterval; 
			if(t_x + Phiz.fsm_phizSize + fsm_phizInterval>= recvMain.fsm_display_width){				
				t_x = 0;
				t_y += Phiz.fsm_phizSize + fsm_phizInterval;
			}
		}
		
		setExtent(recvMain.fsm_display_width, recvMain.fsm_display_height - 1 - getFont().getHeight());
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
			getField(0).setFocus();
		}		
	}
	
	protected boolean navigationMovement(int dx,int dy,int status,int time){
		
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
		
		if(t_index < t_fieldNum){
			getField(t_index).setFocus();
		}else{
			getField(t_fieldNum - 1).setFocus();
			m_move_x = t_fieldNum % m_maxRowNum - 1;
			m_move_y = m_maxColNum - 1;
		}
		
		return true;
	}
	
}

public class PhizSelectedScreen extends MainScreen{

	public static PhizSelectedScreen	sm_phizScreen 	= null;
	
	static private AutoTextEditField	sm_phizSelectingText = null;
    
    static private IPhizSelected		sm_phizSelected 	= new IPhizSelected() {
		
		public void phizSelected(String phizName) {
			if(sm_phizSelectingText != null){
				int t_position = sm_phizSelectingText.getCursorPosition();
				
				String t_text = sm_phizSelectingText.getText();
				
				StringBuffer t_final = new StringBuffer();
				if(t_position != 0){
					t_final.append(t_text.substring(0,t_position));
				}
				t_final.append(phizName);
				
				t_final.append(t_text.substring(t_position,t_text.length()));
				
				sm_phizSelectingText.setText(t_final.toString());
				sm_phizSelectingText.setCursorPosition(t_position + phizName.length());
			}			
		}
	};
	
	PhizMgr		m_phizMgr;
	
	public PhizSelectedScreen(recvMain _mainApp,Vector _phizList){
		
		LabelField		t_prompt = new LabelField(recvMain.sm_local.getString(localResource.WEIBO_PHIZ_SCREEN_PROMPT),Field.NON_FOCUSABLE);
		
		add(t_prompt);
		add(new SeparatorField());
				
		m_phizMgr = new PhizMgr(_phizList);
		add(m_phizMgr);
	}
	
	protected  void	onDisplay(){
		super.onDisplay();
		
		m_phizMgr.resetSelected();
	}
	
	public void close(){
		super.close();
		
		if(sm_phizSelectingText != null){
			sm_phizSelectingText.setFocus();
		}
	}

	
	protected boolean navigationMovement(int dx,int dy,int status,int time){
		return m_phizMgr.navigationMovement(dx, dy, status, time);
	}
	
	static public PhizSelectedScreen getPhizScreen(recvMain _mainApp,AutoTextEditField _insertEdit){
		
		if(sm_phizScreen == null){
			sm_phizScreen = new PhizSelectedScreen(_mainApp, recvMain.sm_phizImageList);
		}
		
		sm_phizSelectingText = _insertEdit;
		sm_phizScreen.m_phizMgr.setSelectedCallback(sm_phizSelected);
		
		return sm_phizScreen;
	}
}
