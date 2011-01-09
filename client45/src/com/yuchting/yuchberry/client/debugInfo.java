package com.yuchting.yuchberry.client;

import java.util.Vector;

import net.rim.device.api.i18n.SimpleDateFormat;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.FontFamily;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Ui;
import net.rim.device.api.ui.component.RichTextField;
import net.rim.device.api.ui.container.MainScreen;


class ErrorLabelText extends Field{
	Vector m_stringList;
	static final int		fsm_space = 1;
	
	static int sm_fontHeight = 15;
	
	static int sm_display_width	= Display.getWidth();
	static int sm_display_height	= Display.getHeight();
	
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
		}catch(Exception _e){}
	}
	
	public void layout(int _width,int _height){
			
		final int t_size 	= m_stringList.size();
		final int t_height = Math.max(0, (t_size - 1)) * fsm_space +  t_size * sm_fontHeight;
		
		setExtent(sm_display_width, t_height);
	}
	
	public void paint(Graphics _g){
		int t_y = 0;
		final int t_fontHeight = sm_fontHeight;
		
		SimpleDateFormat t_format = new SimpleDateFormat("HH:mm:ss");
		
		for(int i = m_stringList.size() -1 ;i >= 0 ;i--){
			recvMain.ErrorInfo t_info = (recvMain.ErrorInfo)m_stringList.elementAt(i);
			
			if(m_stringList.size() - m_selectLine - 1 == i){
				_g.drawText(t_format.format(t_info.m_time) + ": ssssss" + t_info.m_info,0,t_y,Graphics.DRAWSTYLE_SELECT );
			}else{
				_g.drawText(t_format.format(t_info.m_time) + ": " + t_info.m_info,0,t_y,Graphics.ELLIPSIS);
			}
			
			t_y += t_fontHeight + fsm_space;
		}
	}
	
	public void IncreaseRenderSize(int _dx,int _dy){
		m_movePixel_y += _dy;
		if(m_movePixel_y < 0){
			m_movePixel_y = 0;
		}
		
		if(m_movePixel_y > m_stringList.size() * sm_fontHeight){
			m_movePixel_y -= _dy;
		}
		
		boolean t_refreshFull = false;
		if(m_movePixel_y > sm_display_height){
			final int t_former_y = m_viewPixel_y;
			m_viewPixel_y = m_movePixel_y - sm_display_height;
			
			if(t_former_y != m_viewPixel_y){
				t_refreshFull = true;
			}
		}
		
		final int t_formerLine = m_selectLine;
		m_selectLine = m_movePixel_y / sm_fontHeight;
		
		if(t_refreshFull){
			invalidate();
		}else{
			if(t_formerLine != m_selectLine){
				
				final int t_pos_x = m_movePixel_y - m_viewPixel_y;
				final int t_line_x = t_pos_x / sm_fontHeight;
				
				if(m_selectLine > t_formerLine){
					invalidate(0,t_line_x - sm_fontHeight,Display.getWidth(),2 * sm_fontHeight);
				}else{
					invalidate(0,t_line_x,Display.getWidth(),2 * sm_fontHeight);
				}				
			}
		}
		
	}
}


public class debugInfo extends MainScreen{
	
	RichTextField 	m_editText	= null;
	recvMain		m_mainApp	= null;
	
	ErrorLabelText  m_errorText = null;
	
	public debugInfo(recvMain _mainApp){
		m_mainApp = _mainApp;
		
		m_errorText = new ErrorLabelText(m_mainApp.GetErrorString());
        add(m_errorText);
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
