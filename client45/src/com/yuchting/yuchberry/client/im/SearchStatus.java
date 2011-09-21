package com.yuchting.yuchberry.client.im;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.component.AutoTextEditField;

import com.yuchting.yuchberry.client.recvMain;
import com.yuchting.yuchberry.client.ui.ImageButton;

public class SearchStatus extends Manager implements FieldChangeListener{

	public AutoTextEditField 	m_editTextArea			= new AutoTextEditField(){
		public void setText(String _text){
			super.setText(_text);
			this.layout(m_textWidth,1000);
		}
		
		public int getPreferredWidth(){
			return m_textWidth;
		}
		
		protected boolean keyDown(int keycode,int time){
			this.layout(m_textWidth,1000);
			return super.keyDown(keycode,time);
		}
	};
	
	int					m_textWidth		= 0;
	int					m_currHeight	= InputManager.fsm_minHeight;
	ImageButton			m_nextButton 	= null;
	
	MainIMScreen	m_mainScreen = null;
	
	public SearchStatus(MainIMScreen _screen){
		super(Manager.VERTICAL_SCROLL);
		m_mainScreen = _screen;
				
		m_nextButton = new ImageButton("Phiz", 
									recvMain.sm_weiboUIImage.getImageUnit("phiz_button"), 
									recvMain.sm_weiboUIImage.getImageUnit("phiz_button_focus"), 
									recvMain.sm_weiboUIImage);
		
		m_textWidth = getPreferredWidth() - m_nextButton.getImageWidth() 
					- (InputManager.fsm_textBorder + InputManager.fsm_inputBubbleBorder)* 2;
		
		add(m_editTextArea);
		add(m_nextButton);
		
		m_editTextArea.setChangeListener(this);
	}
	
	public int getPreferredWidth(){
		return recvMain.fsm_display_width;
	}
	
	public int getPreferredHeight(){
		return m_currHeight;
	}
	
	protected void sublayout(int _width,int _height){
		setPositionChild(m_editTextArea,InputManager.fsm_textBorder + InputManager.fsm_inputBubbleBorder,
						InputManager.fsm_textBorder + InputManager.fsm_inputBubbleBorder);		
		layoutChild(m_editTextArea,m_editTextArea.getPreferredWidth(),m_editTextArea.getPreferredHeight());
		
		setPositionChild(m_nextButton,getPreferredWidth() - m_nextButton.getImageWidth(),
							(getPreferredHeight() - m_nextButton.getImageHeight()) /2);
		
		layoutChild(m_nextButton,m_nextButton.getImageWidth(),m_nextButton.getImageHeight());
				
		setExtent(getPreferredWidth(), getPreferredHeight());
	}
	
	public void fieldChanged(Field field,int _context){
		
		if(_context != FieldChangeListener.PROGRAMMATIC){
			
			m_currHeight = m_editTextArea.getHeight() + 
							(InputManager.fsm_textBorder + InputManager.fsm_inputBubbleBorder) * 2;
			
			if(m_currHeight < InputManager.fsm_minHeight){
				m_currHeight = InputManager.fsm_minHeight;
			}
			
			if(m_currHeight > InputManager.fsm_maxHeight){
				m_currHeight = InputManager.fsm_maxHeight;
			}
		}
	}
}
