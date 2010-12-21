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
		final int t_width = Display.getWidth();
			
		final int t_size 	= m_stringList.size();
		final int t_height = Math.max(0, (t_size - 1)) * fsm_space +  t_size * sm_fontHeight;
		
		setExtent(t_width, t_height);
	}
	
	public void paint(Graphics _g){
		int t_y = 0;
		final int t_fontHeight = sm_fontHeight;
		
		SimpleDateFormat t_format = new SimpleDateFormat("HH:mm:ss");
		
		for(int i = m_stringList.size() -1 ;i >= 0 ;i--){
			recvMain.ErrorInfo t_info = (recvMain.ErrorInfo)m_stringList.elementAt(i);
			_g.drawText(t_format.format(t_info.m_time) + ": " + t_info.m_info,0,t_y,Graphics.ELLIPSIS);
			
			t_y += t_fontHeight + fsm_space;
		}
	}
	
	public boolean isFocusable(){
		return false;
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

}
