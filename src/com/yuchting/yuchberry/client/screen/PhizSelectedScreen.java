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
package com.yuchting.yuchberry.client.screen;

import java.util.Vector;

import local.yblocalResource;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Display;
import net.rim.device.api.system.KeypadListener;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.component.AutoTextEditField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.MainScreen;

import com.yuchting.yuchberry.client.recvMain;
import com.yuchting.yuchberry.client.ui.Phiz;

final class PhizMgr extends Manager{
	
	Vector 				m_phizList	= null;
	IPhizSelected		m_selectedCallback = null;
	
	final static int	fsm_phizInterval = 0; //RRR: 表情间距
	
	int 				m_move_x = 0;
	int 				m_move_y = 0;
	
	int					m_maxRowNum = 0;
	int					m_maxColNum = 0;
	
	int					m_phizSize = 0;
	
	Phiz				m_currSelected = null;
	
	public PhizMgr(Vector _phizList){
		super(Manager.VERTICAL_SCROLL);
		
		if(_phizList.size() == 0){
			throw new RuntimeException("_phizList.size() == 0");
		}
		
		m_phizList = _phizList;
		m_currSelected = (Phiz)m_phizList.elementAt(0);
		
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
			m_currSelected = (Phiz)getField(0);			
			m_currSelected.setFocus();
			
			((PhizSelectedScreen)getScreen()).setPromptLabel(m_currSelected.getPhizName());
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
		
		Phiz t_formerSelected = m_currSelected;
		if(t_index < t_fieldNum){
			m_currSelected = (Phiz)getField(t_index);
		}else{
			m_currSelected = (Phiz)getField(t_fieldNum - 1);
			m_move_x = t_fieldNum % m_maxRowNum - 1;
			m_move_y = m_maxColNum - 1;
		}
		
		m_currSelected.setFocus();
		((PhizSelectedScreen)getScreen()).setPromptLabel(m_currSelected.getPhizName());
		
		if(t_formerSelected != m_currSelected){
			getScreen().invalidate();
		}
		
		return true;
	}
	
}

public class PhizSelectedScreen extends MainScreen{
	
	static final int BIG_PHIZ_SIZE = 48; //RRR: 表情放大 recvMain.fsm_display_width / 4; 

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
	LabelField	m_prompt = new LabelField(m_promptString,Field.NON_FOCUSABLE);
	
	int[]		m_currPhizBuffer = null;
	Bitmap 		m_currPhizImage = null; 
	Bitmap 		m_bigPhizImage = null;
	
	recvMain	m_mainApp;
	
	boolean	m_showBigPhizImage = false;
	
	public PhizSelectedScreen(recvMain _mainApp){
		
		if(_mainApp.m_phizImageList.size() == 0){
			throw new RuntimeException("_phizList.size() == 0");
		}
		
		m_mainApp = _mainApp;
		
		// prepare th field 
		m_prompt = new LabelField(recvMain.sm_local.getString(yblocalResource.WEIBO_PHIZ_SCREEN_PROMPT),Field.NON_FOCUSABLE);
		add(m_prompt);
		add(new SeparatorField());
				
		m_phizMgr = new PhizMgr(_mainApp.m_phizImageList);
		add(m_phizMgr);
				
		// prepare the scale phiz variables
		//
		Phiz t_phiz = (Phiz)_mainApp.m_phizImageList.elementAt(0);
		m_currPhizBuffer	= new int[t_phiz.getImage().getWidth() * t_phiz.getImage().getHeight()];
		m_bigPhizImage 		= new Bitmap(BIG_PHIZ_SIZE,BIG_PHIZ_SIZE);
		m_currPhizImage		= new Bitmap(t_phiz.getImage().getWidth(),t_phiz.getImage().getHeight());
	}
	
	protected  void	onDisplay(){
		super.onDisplay();
		//m_phizMgr.resetSelected();
	}
	
	public void setPromptLabel(String _phizName){
		m_prompt.setText(m_promptString +" "+ _phizName);
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

	/**
	 * 
	*/
	/*
	protected void paint(Graphics g){
		super.paint(g);
				
		try{
			
			int t_x =  recvMain.fsm_display_width / 2 - 24;			//RRR:放大表情的X轴的坐标居中 int t_x = 0;			
			int t_y = recvMain.fsm_display_height - BIG_PHIZ_SIZE;
			if(m_phizMgr.m_move_y * Phiz.fsm_phizSize > recvMain.fsm_display_height / 2){
				t_y = m_prompt.getFont().getHeight() + 2;
			}
			
			m_mainApp.m_weiboUIImage.getImageUnitBuffer(m_phizMgr.m_currSelected.getImage(), m_currPhizBuffer);
			m_currPhizImage.setARGB(m_currPhizBuffer, 0, m_currPhizImage.getWidth(), 0, 0, m_currPhizImage.getWidth(),m_currPhizImage.getHeight());
			m_currPhizImage.scaleInto(m_bigPhizImage, Bitmap.FILTER_LANCZOS);
			
			int t_color = g.getColor();
			try{
				g.setColor(0xffffff);
				g.fillRect(t_x, t_y, BIG_PHIZ_SIZE, BIG_PHIZ_SIZE);
			}finally{
				g.setColor(t_color);
			}
			
			g.drawBitmap(t_x, t_y, m_bigPhizImage.getWidth(), m_bigPhizImage.getHeight(), m_bigPhizImage, 0, 0);
			
		}catch(Exception e){}
		
		
	}
 */		
	protected boolean trackwheelRoll(int amount,int status,int time){		
		return m_phizMgr.navigationMovement(amount, 0, status, time);
	}
	
	protected boolean navigationMovement(int dx,int dy,int status,int time){
		return m_phizMgr.navigationMovement(dx, dy, status, time);
	}
	
	static public PhizSelectedScreen getPhizScreen(recvMain _mainApp,AutoTextEditField _insertEdit){
		
		if(sm_phizScreen == null){
			sm_phizScreen = new PhizSelectedScreen(_mainApp);
		}
		
		sm_phizScreen.preparePhizScreen(_insertEdit);
		
		return sm_phizScreen;
	}

	public void preparePhizScreen(AutoTextEditField _insertEdit){
		m_phizSelectingText = _insertEdit;
		m_phizMgr.setSelectedCallback(m_phizSelected);
	}
}
