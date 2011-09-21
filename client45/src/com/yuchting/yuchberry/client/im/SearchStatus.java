package com.yuchting.yuchberry.client.im;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.component.AutoTextEditField;

import com.yuchting.yuchberry.client.recvMain;
import com.yuchting.yuchberry.client.ui.ImageButton;

public class SearchStatus extends Manager implements FieldChangeListener{

	public final class SearchEditText extends AutoTextEditField{
		public void setText(String _text){
			super.setText(_text);
			this.layout(m_textWidth,1000);
		}
		
		public int getPreferredWidth(){
			return m_textWidth;
		}
		
		public boolean keyDown(int keycode,int time){
			this.layout(m_textWidth,1000);
			return super.keyDown(keycode,time);
		}
		
		public boolean keyChar(char c,int status,int time){
			return super.keyChar(c,status,time);
		}
	}
	
	SearchEditText		m_editTextArea = new SearchEditText();
	
	int					m_textWidth		= 0;
	
	int					m_currSelected	= 0;
	
	MainIMScreen		m_mainScreen = null;
	
	public SearchStatus(MainIMScreen _screen){
		super(Manager.VERTICAL_SCROLL);
		m_mainScreen = _screen;
		
		m_textWidth = getPreferredWidth() - (InputManager.fsm_textBorder + InputManager.fsm_inputBubbleBorder)* 2;
		
		add(m_editTextArea);

		m_editTextArea.setChangeListener(this);
		
		InputManager.initInputBackground();
	}
	
	public int getPreferredWidth(){
		return recvMain.fsm_display_width;
	}
	
	public int getPreferredHeight(){
		return InputManager.fsm_minHeight;
	}
	
	protected void sublayout(int _width,int _height){
		setPositionChild(m_editTextArea,InputManager.fsm_textBorder + InputManager.fsm_inputBubbleBorder,
						InputManager.fsm_textBorder + InputManager.fsm_inputBubbleBorder);		
		layoutChild(m_editTextArea,m_editTextArea.getPreferredWidth(),m_editTextArea.getPreferredHeight());
						
		setExtent(getPreferredWidth(), getPreferredHeight());
	}
	
	protected void subpaint(Graphics _g){
		
		InputManager.drawInputBackground(_g, m_textWidth, getPreferredWidth(), getPreferredHeight());
		
		super.subpaint(_g);
	}
	
	public void fieldChanged(Field field,int _context){
		
		if(_context != FieldChangeListener.PROGRAMMATIC){
			
			String t_keyword = m_editTextArea.getText();
			if(t_keyword.length() != 0){
				m_mainScreen.selectedByKeyword(t_keyword,m_currSelected);
			}		
		}
	}
	
	public void searchNext(){
		
		String t_keyword = m_editTextArea.getText();
		if(t_keyword.length() != 0){
			
			if(!m_mainScreen.selectedByKeyword( t_keyword, ++m_currSelected )){

				// can't find or find the final matched
				//
				m_currSelected = 0;
				m_mainScreen.selectedByKeyword(t_keyword,m_currSelected);
			}
		}	
	}
}
