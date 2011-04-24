package com.yuchting.yuchberry.client;

import java.util.Vector;

import local.localResource;
import net.rim.device.api.i18n.SimpleDateFormat;
import net.rim.device.api.system.Clipboard;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.FontFamily;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.Ui;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.component.RichTextField;
import net.rim.device.api.ui.container.MainScreen;


class ErrorLabelText extends Field{
	Vector m_stringList;
	static final int		fsm_space = 3;
	
	static int sm_fontHeight = 15;
	static int sm_lineHeight = sm_fontHeight + fsm_space;
		
	int			m_viewPixel_x		= 0;
	int			m_viewPixel_y		= 0;
	
	int			m_movePixel_y		= 0;
	int			m_movePixel_x		= 0;
		
	int			m_selectLine		= 0;
	

	
	public ErrorLabelText(Vector _stringList){
		super(Field.READONLY | Field.NON_FOCUSABLE | Field.USE_ALL_WIDTH);
		
		m_stringList = _stringList;
		try{
			Font myFont = FontFamily.forName("BBMillbankTall").getFont(Font.PLAIN,8,Ui.UNITS_pt);
			setFont(myFont);
			
			sm_fontHeight = myFont.getHeight() - 3;
			sm_lineHeight = sm_fontHeight + fsm_space;
			
		}catch(Exception _e){}
		
	}
	
	public void layout(int _width,int _height){
			
		final int t_size 	= m_stringList.size();
		final int t_height = t_size * sm_lineHeight;
		
		setExtent(recvMain.fsm_display_width, t_height);
	}
	
	public void paint(Graphics _g){
		
		int t_y = 0;
		int t_startIdx = 0;
		if(m_viewPixel_y < 0){
			t_startIdx = Math.abs(m_viewPixel_y) / sm_lineHeight;
		}
		
		SimpleDateFormat t_format = new SimpleDateFormat("HH:mm:ss");
		
		for(int i = m_stringList.size() - 1 - t_startIdx ;i >= 0 ;i--){
			recvMain.ErrorInfo t_info = (recvMain.ErrorInfo)m_stringList.elementAt(i);
			
			if(m_stringList.size() - m_selectLine - 1 == i){
				
				String t_text = t_format.format(t_info.m_time) + ":" + t_info.m_info;
				_g.drawText(t_text.substring(m_movePixel_x),1,t_y + 1,Graphics.ELLIPSIS);
				
				_g.drawRoundRect(0,t_y,recvMain.fsm_display_width,sm_lineHeight,1,1);
			}else{
				_g.drawText(t_format.format(t_info.m_time) + ":" + t_info.m_info,0,t_y,Graphics.ELLIPSIS);
			}
			
			t_y += sm_lineHeight;
		}
	}
	
	public void IncreaseRenderSize(int _dx,int _dy){
						
		_dy = _dy * sm_lineHeight;

		final int t_former_move_y = m_movePixel_y;
		
		final int t_former_view_y = m_viewPixel_y;		
		
		final int t_maxHeight = m_stringList.size() * sm_lineHeight;
		
		if(m_movePixel_y + _dy < Math.min(t_maxHeight,recvMain.fsm_display_height)){
			if(m_movePixel_y + _dy < 0 ){
				m_viewPixel_y -= m_movePixel_y + _dy;
			}else{
				if(m_movePixel_y + _dy - m_viewPixel_y < t_maxHeight){
					if(m_movePixel_y + _dy + sm_lineHeight <= recvMain.fsm_display_height){
						m_movePixel_y += _dy;
					}else{
						m_viewPixel_y -= _dy;
					}					
				}
			}			
		}else{
			if(m_movePixel_y + _dy - m_viewPixel_y < t_maxHeight){
				m_viewPixel_y -= _dy;
			}				
		}
		
		if(m_viewPixel_y > 0){
			m_viewPixel_y = 0;
			m_movePixel_y = 0;
		}
		
		final boolean t_refreshFull = (t_former_view_y != m_viewPixel_y);
				
		final int t_formerLine = m_selectLine;
		m_selectLine = (m_movePixel_y - m_viewPixel_y) / sm_lineHeight;
		
		if(t_refreshFull){
			invalidate();
		}else{
			
			
			if(t_formerLine != m_selectLine){
				
				RefreshRect(m_movePixel_y,t_former_move_y);	
				
				m_movePixel_x = 0;
				
			}else{
				
				final int t_former_x = m_movePixel_x;
				
				if(m_movePixel_x + _dx >= 0){
					m_movePixel_x += _dx;
				}
				
				if(t_former_x != m_movePixel_x){
					RefreshRect(m_movePixel_y,t_former_move_y);	
				}				
			}
		}		
	}
	
	private void RefreshRect(int _y,int _y1){
		if(_y < _y1){
			final int t_tmp = _y;
			_y = _y1;
			_y1 = t_tmp; 
		}
		
		invalidate(0,_y1,Display.getWidth(),_y - _y1 + sm_lineHeight);
	}
		
}


public class debugInfo extends MainScreen{
	
	RichTextField 	m_editText	= null;
	recvMain		m_mainApp	= null;
	
	ErrorLabelText  m_errorText = null;
	
	MenuItem 	m_helpMenu = new MenuItem(recvMain.sm_local.getString(localResource.DEBUG_INFO_HELP_MENU), 100, 10) {
		public void run() {
			recvMain.openURL("http://code.google.com/p/yuchberry/wiki/Connect_Error_info");	
		}
	};

	MenuItem 	m_clearMenu = new MenuItem(recvMain.sm_local.getString(localResource.DEBUG_INFO_CLEAR_MENU), 101, 10) {
		public void run() {
			m_mainApp.clearDebugMenu();											
		}
	};
	
	MenuItem 	m_copyMenu = new MenuItem(recvMain.sm_local.getString(localResource.COPY_TO_CLIPBOARD), 101, 10) {
		public void run() {		
			Clipboard.getClipboard().put(m_mainApp.GetAllErrorString());
			m_mainApp.DialogAlert(recvMain.sm_local.getString(localResource.COPY_TO_CLIPBOARD_SUCC));
		}
	};
	
	public debugInfo(recvMain _mainApp){
		m_mainApp = _mainApp;
		
		m_errorText = new ErrorLabelText(m_mainApp.GetErrorString());
        add(m_errorText);
	}
	
	protected void makeMenu(Menu _menu,int instance){
    	_menu.add(m_helpMenu);
    	_menu.add(m_clearMenu);
    	_menu.add(m_copyMenu);
    }
	
	protected boolean keyDown(int keycode,int time){
    	
		final int key = Keypad.key(keycode);
    	switch(key){
    	case 'H':
    		m_helpMenu.run();
    		return true;
    	case 'C':
    		m_clearMenu.run();
    		return true;
    	case 'A':
    		m_copyMenu.run();
    		return true;
    	}
    	
    	return false;    	
    }    
	
	public boolean onClose(){
		close();
		m_mainApp.m_debugInfoScreen = null;
		
		return true;
	}
	
	public void RefreshText(){
		m_mainApp.invokeLater(new Runnable() {
			public void run() {
				m_errorText.layout(0, 0);
				invalidate();			
			}
		});
	}
	  
	
	protected boolean navigationMovement(int dx,int dy,int status,int time){
		m_errorText.IncreaseRenderSize(dx,dy);
		return true;
	}

}
