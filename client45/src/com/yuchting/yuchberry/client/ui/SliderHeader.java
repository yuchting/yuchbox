package com.yuchting.yuchberry.client.ui;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Graphics;

import com.yuchting.yuchberry.client.recvMain;

public abstract class SliderHeader extends Field{
	
	public final static int fsm_headHeight = 45;
	
	protected final static int fsm_darkColor		  = 0xb1aeae;
			
	protected final static int fsm_stateBitmapInterval = 12;
	protected final static int fsm_stateBitmapSize = 32;
	
	protected final static int fsm_stateBitmapTop = (fsm_headHeight - fsm_stateBitmapSize) /2;
	
	protected final static int fsm_blockWidth = 50;
	
	protected ImageUnit[] m_stateBitmap = null;
	
	protected static ImageUnit sm_navigateBitmap = null;
	protected static ImageUnit sm_navigateSegBitmap = null;
	protected static ImageUnit sm_navigateBlockBitmap = null;
	protected static ImageUnit sm_navigateBlockBitmap_offline = null;
	protected static ImageUnit sm_navigatePointBitmap = null;
	
	recvMain			m_mainApp = null;
	
	protected int	m_currState = 0;
	
	static ImageUnit	sm_isBBerSign = null;
	static public ImageUnit GetBBerSignBitmap(){
		if(sm_isBBerSign == null){
			sm_isBBerSign = recvMain.sm_weiboUIImage.getImageUnit("BBSign");		
		}
		
		return sm_isBBerSign;
	}
	
	public SliderHeader(recvMain _mainApp,String[] _stateName){
		m_mainApp = _mainApp;
			
		if(sm_navigateBitmap == null){
			sm_navigateBitmap 				= recvMain.sm_weiboUIImage.getImageUnit("nav_bar");
			sm_navigateSegBitmap 			= recvMain.sm_weiboUIImage.getImageUnit("nav_bar_seg");
			sm_navigateBlockBitmap 			= recvMain.sm_weiboUIImage.getImageUnit("nav_bar_block");
			sm_navigateBlockBitmap_offline 	= recvMain.sm_weiboUIImage.getImageUnit("nav_bar_block_offline");
			sm_navigatePointBitmap 			= recvMain.sm_weiboUIImage.getImageUnit("nav_bar_point");
		}
		
		m_stateBitmap = new ImageUnit[_stateName.length];
		
		
		for(int i = 0;i < _stateName.length;i++){
			m_stateBitmap[i] = recvMain.sm_weiboUIImage.getImageUnit(_stateName[i]);
		}

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
		
	protected int				m_curr_x = 0;
	protected int				m_former_x = 0;
	protected int				m_former_state = 0;
	protected int				m_dest_x = 0;
	protected int				m_animationRunId = -1;
	protected int				m_deaccel_delta = 0; 
	
	public void setCurrState(int _state){
				
		if(_state >= 0 && _state < m_stateBitmap.length){
			
			if(m_currState != _state){
				
				m_former_state = m_currState;
				m_currState = _state;
									
				m_deaccel_delta = 0;
				m_former_x = m_curr_x;
				m_dest_x = m_currState * fsm_blockWidth;
				
				invalidate();
				
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
								
								if(m_curr_x == m_dest_x){
									synchronized (SliderHeader.this) {
										m_mainApp.cancelInvokeLater(m_animationRunId);
										m_animationRunId = -1;
									}
								}
								
								SliderHeader.this.invalidate();
							}
						},fsm_moveTime,true);
						
						// cant be allocated a usefull runnable id
						//
						if(m_animationRunId == -1){
							m_curr_x = m_dest_x;
						}
					}
				}
			}
			
		}
	}
	
	protected void paint( Graphics g ){

		recvMain.sm_weiboUIImage.drawBitmapLine(g, sm_navigateBitmap, 0, 0, recvMain.fsm_display_width);
		
		int t_x = 0;
		if(!m_mainApp.m_connectDeamon.isDisconnectState()){
			recvMain.sm_weiboUIImage.drawBitmapLine(g, sm_navigateBlockBitmap, m_curr_x, 0, fsm_blockWidth);
		}else{
			recvMain.sm_weiboUIImage.drawBitmapLine(g, sm_navigateBlockBitmap_offline, m_curr_x, 0, fsm_blockWidth);
		}
		t_x = m_curr_x + (fsm_blockWidth - sm_navigatePointBitmap.getWidth()) / 2;
		recvMain.sm_weiboUIImage.drawImage(g, sm_navigatePointBitmap, t_x, fsm_headHeight - sm_navigatePointBitmap.getHeight()); 
		
		t_x = (fsm_blockWidth - fsm_stateBitmapSize)/2;
		
		for(int i = 0 ;i < m_stateBitmap.length;i++){
			
			if(m_currState == i){
				// hover
				//					
				recvMain.sm_weiboUIImage.drawImage(g,m_stateBitmap[i],t_x,fsm_stateBitmapTop - 1 - fsm_moveSliderHeight);
			}else{
				// normal
				//
				recvMain.sm_weiboUIImage.drawImage(g,m_stateBitmap[i],t_x,fsm_stateBitmapTop);
			}				
							
			t_x += fsm_blockWidth;
			
			recvMain.sm_weiboUIImage.drawImage(g, sm_navigateSegBitmap, (i + 1) * fsm_blockWidth - sm_navigateSegBitmap.getWidth(), 0);
		}
		
    }
	
}
