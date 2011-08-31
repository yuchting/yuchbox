package com.yuchting.yuchberry.client.ui;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Graphics;

import com.yuchting.yuchberry.client.recvMain;

public abstract class SliderHeader extends Field{
	
	public final static int fsm_headHeight = 45;
	
	protected final static int fsm_darkColor		  = 0xb1aeae;
	
	protected final static int fsm_linkedStateSize = 20;
		
	protected final static int fsm_stateBitmapInterval = 12;
	protected final static int fsm_stateBitmapSize = 32;
	
	protected final static int fsm_stateBitmapTop = (fsm_headHeight - fsm_stateBitmapSize) /2;
	
	protected ImageUnit[] m_stateBitmap = null;
	protected ImageUnit[] m_stateBitmap_hover = null;
	
	protected static ButtonSegImage sm_navigateBitmap = null;
	
	recvMain			m_mainApp = null;
	
	protected int	m_currState = 0;
	
	public SliderHeader(recvMain _mainApp,String[] _stateName,String[] _stateName_hover,int[][] _moveColor){
		m_mainApp = _mainApp;
			
		if(sm_navigateBitmap == null){
			sm_navigateBitmap = new ButtonSegImage(
					recvMain.sm_weiboUIImage.getImageUnit("nav_bar_left"),
					recvMain.sm_weiboUIImage.getImageUnit("nav_bar_mid"),
					recvMain.sm_weiboUIImage.getImageUnit("nav_bar_right"),
					recvMain.sm_weiboUIImage);
		}
		
		m_stateBitmap = new ImageUnit[_stateName.length];
		m_stateBitmap_hover = new ImageUnit[_stateName_hover.length];
		
		for(int i = 0;i < _stateName.length;i++){
			m_stateBitmap[i] = recvMain.sm_weiboUIImage.getImageUnit(_stateName[i]);
			m_stateBitmap_hover[i] = recvMain.sm_weiboUIImage.getImageUnit(_stateName_hover[i]);
		}
		
		m_moveColor = _moveColor;
		m_curr_color = (m_moveColor[0][0] << 16) | (m_moveColor[1][0] << 8) | (m_moveColor[2][0]);
	}
	
	protected void layout(int _width,int _height){
        setExtent(recvMain.fsm_display_width,fsm_headHeight);
    }
	
	public void invalidate(){
		super.invalidate();
	}
	
	public int getCurrState(){
		return m_currState;
	}
	
	private final static int fsm_moveSliderHeight = 4;
	private final static int	fsm_moveTime = 35;
	
	protected int[][]	m_moveColor = null;	
	
	protected int				m_curr_x = fsm_linkedStateSize;
	protected int				m_curr_color = 0;
	protected int				m_former_x = fsm_linkedStateSize;
	protected int				m_former_state = 0;
	protected int				m_dest_x = 0;
	protected int				m_animationRunId = -1;
	protected int				m_deaccel_delta = 0; 
	
	public void setCurrState(int _state){
		
		if(m_moveColor == null){
			return ;
		}
		
		if(_state >= 0 && _state < m_stateBitmap.length){
			
			if(m_currState != _state){
				
				m_former_state = m_currState;
				m_currState = _state;
									
				m_deaccel_delta = 0;
				m_former_x = m_curr_x;
				m_dest_x = m_currState * (recvMain.fsm_display_width / m_stateBitmap.length) + fsm_linkedStateSize;
				
				synchronized (this) {
					
					if(m_animationRunId == -1){
					
						m_animationRunId = m_mainApp.invokeLater(new Runnable() {
							public void run() {
								int t_distance = Math.abs(m_former_x - m_dest_x);
								
								if(m_former_x < m_dest_x){
									m_curr_x += t_distance / 3 - m_deaccel_delta;
									
									if(m_curr_x >= m_dest_x){
										m_curr_x = m_dest_x;
									}
								}else{
									m_curr_x -= t_distance / 3 - m_deaccel_delta;
									
									if(m_curr_x <= m_dest_x){
										m_curr_x = m_dest_x;
									}
								}
								m_deaccel_delta += 3;
								
								int r,g,b;
								if(m_curr_x == m_dest_x){
									
									r = (m_moveColor[0][m_currState]);
									g = (m_moveColor[1][m_currState]);
									b = (m_moveColor[2][m_currState]);
																		
									synchronized (SliderHeader.this) {
										m_mainApp.cancelInvokeLater(m_animationRunId);
										m_animationRunId = -1;
									}
									
								}else{
									r = (m_moveColor[0][m_currState] - m_moveColor[0][m_former_state]);
									r = (m_curr_x - m_former_x) * r / t_distance  + m_moveColor[0][m_former_state];
									
									g = (m_moveColor[1][m_currState] - m_moveColor[1][m_former_state]);
									g = (m_curr_x - m_former_x) * g / t_distance  + m_moveColor[1][m_former_state];
									
									b = (m_moveColor[2][m_currState] - m_moveColor[2][m_former_state]);
									b = (m_curr_x - m_former_x) * b / t_distance  + m_moveColor[2][m_former_state];
								}
								
								m_curr_color = (r << 16)| (g << 8) | b;
								
								SliderHeader.this.invalidate(0,sm_navigateBitmap.getImageHeight() - 1 - fsm_moveSliderHeight
															,recvMain.fsm_display_width,fsm_moveSliderHeight);
							}
						},fsm_moveTime,true);
						
						// cant be allocated a usefull runnable id
						//
						if(m_animationRunId == -1){
							m_curr_x = m_dest_x;
							int r,g,b;
							r = (m_moveColor[0][m_currState]);
							g = (m_moveColor[1][m_currState]);
							b = (m_moveColor[2][m_currState]);
							m_curr_color = (r << 16)| (g << 8) | b;	
						}
					}
				}
			}
			
		}
	}
	
	protected void paint( Graphics g ){
		
		int color = g.getColor();
		try{
			
			sm_navigateBitmap.draw(g,0,0,recvMain.fsm_display_width);
			
			int t_x = fsm_linkedStateSize;			
			for(int i = 0 ;i < m_stateBitmap.length;i++){
				
				if(m_currState == i){
					// hover
					//
					recvMain.sm_weiboUIImage.drawImage(g,m_stateBitmap_hover[i],t_x,fsm_stateBitmapTop - 1 - fsm_moveSliderHeight);
				}else{
					// normal
					//
					recvMain.sm_weiboUIImage.drawImage(g,m_stateBitmap[i],t_x,fsm_stateBitmapTop);
				}				
								
				t_x += recvMain.fsm_display_width / m_stateBitmap.length;
			}
			
			if(m_mainApp.m_connectDeamon.isDisconnectState()){
				g.setColor(fsm_darkColor);
			}else{
				g.setColor(m_curr_color);
			}
			
			g.fillRoundRect(m_curr_x,sm_navigateBitmap.getImageHeight() - 1 - fsm_moveSliderHeight,
							m_stateBitmap[0].getWidth(),fsm_moveSliderHeight,2,2);			
			
		}finally{
			g.setColor(color);
		}
		
    }
	
}
