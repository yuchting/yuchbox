package com.yuchting.yuchberry.client.ui;

import net.rim.device.api.ui.Graphics;

public class BubbleImage {
	
	public final static int	NO_POINT_STYLE = -1;
	
	public final static int	LEFT_POINT_STYLE = 0;
	public final static int	TOP_POINT_STYLE = 1;
	public final static int	RIGHT_POINT_STYLE = 2;
	public final static int	BOTTOM_POINT_STYLE = 3;
	
	private ImageUnit		m_top_left;
	private ImageUnit		m_top;
	private ImageUnit		m_top_right;
	private ImageUnit		m_right;
	private ImageUnit		m_bottom_right;
	private ImageUnit		m_bottom;
	private ImageUnit		m_bottom_left;
	private ImageUnit		m_left;
	
	private ImageUnit		m_innerBlock;
	
	private ImageUnit[]	m_point;
	
	private ImageSets		m_parentSets;
	
	private boolean 		m_point_down = false;
	
	public BubbleImage(ImageUnit _top_left,ImageUnit _top,
						ImageUnit _top_right,ImageUnit _right,
						ImageUnit _bottom_right,ImageUnit _bottom,
						ImageUnit _bottom_left,ImageUnit _left,
						ImageUnit _innerBlock,ImageUnit[] _point,
						ImageSets _parentSets){
		
		m_top_left	= _top_left;
		m_top		= _top;
		m_top_right	= _top_right;
		m_right		= _right;
		
		m_bottom_right = _bottom_right;
		m_bottom		= _bottom;
		m_bottom_left	= _bottom_left;
		m_left			= _left;
		
		m_innerBlock	= _innerBlock;
		m_parentSets	= _parentSets;
		m_point			= _point;
	}
	
	public void setPointDown(boolean _down){
		m_point_down = _down;
	}
	
	public int getInnerBlockSize(){
		return m_innerBlock.getWidth();
	}
	
	public void draw(Graphics _g,int _x,int _y,int _width,int _height,int _pointStyle){
		
		// draw the corner
		//
		m_parentSets.drawImage(_g,m_top_left,_x,_y);
		m_parentSets.drawImage(_g,m_top_right,_x + (_width - m_top_right.m_width), _y);
		m_parentSets.drawImage(_g,m_bottom_right,_x + (_width - m_bottom_right.m_width), _y + (_height - m_bottom_right.m_height));
		m_parentSets.drawImage(_g,m_bottom_left,_x,_y + (_height - m_bottom_left.m_height));
		
		// draw horz edge
		//
		int t_horz_num =  (_width - m_top_left.m_width - m_top_right.m_width) / m_top.m_width;
		if(t_horz_num > 0){
			
			int t_horz_x = _x + m_top_left.m_width;
			for(int i = 0;i < t_horz_num;i++){
				m_parentSets.drawImage(_g,m_top,t_horz_x, _y);
				m_parentSets.drawImage(_g,m_bottom,t_horz_x, _y + (_height - m_bottom.m_height));
				t_horz_x += m_top.m_width;
			}
		}
		int t_horz_remain_width = (_width - m_top_left.m_width - m_top_right.m_width) % m_top.m_width;
		if(t_horz_remain_width > 0){
			int t_horz_x = _x + t_horz_num * m_top.m_width + m_top_left.m_width;
			
			m_parentSets.drawImage(_g,m_top,t_horz_x, _y,t_horz_remain_width, m_top.m_height);
			
			m_parentSets.drawImage(_g,m_bottom,t_horz_x, _y + (_height - m_bottom.m_height),
					t_horz_remain_width,m_bottom.m_height);
		}
		
		// draw vert edge
		//
		int t_vert_num =  (_height - m_top_left.m_height - m_bottom_left.m_height) / m_left.m_height;
		if(t_vert_num > 0){

			int t_vert_y = _y + m_top_left.m_height;
			for(int i = 0;i < t_vert_num;i++){
				m_parentSets.drawImage(_g,m_left,_x, t_vert_y);
				m_parentSets.drawImage(_g,m_right,_x + (_width - m_right.m_width),t_vert_y);
				
				t_vert_y += m_left.m_height;
			}
		}
		
		int t_vert_remain_height = (_height - m_top_left.m_height - m_bottom_left.m_height) % m_left.m_height;
		if(t_vert_remain_height > 0){
			
			int t_vert_y = _y + t_vert_num * m_left.m_height + m_top_left.m_height;
			
			m_parentSets.drawImage(_g,m_left,_x, t_vert_y,m_left.m_width, t_vert_remain_height);
			m_parentSets.drawImage(_g,m_right,_x + (_width - m_right.m_width),
					t_vert_y,m_right.m_width,t_vert_remain_height);
		}
		
		// draw the point 
		//
		if(m_point != null){
			switch(_pointStyle){
			case LEFT_POINT_STYLE:
				if(m_point[LEFT_POINT_STYLE] != null){
					if(m_point_down){
						m_parentSets.drawImage(_g,m_point[LEFT_POINT_STYLE],_x - m_point[LEFT_POINT_STYLE].m_width,
								_y + _height - (m_point[LEFT_POINT_STYLE].getHeight() * 3 / 2) );
					}else{
						m_parentSets.drawImage(_g,m_point[LEFT_POINT_STYLE],_x - m_point[LEFT_POINT_STYLE].m_width,
								_y + (m_point[LEFT_POINT_STYLE].getHeight() / 2) );
					}					
				}				
				break;
			case TOP_POINT_STYLE:
				if(m_point[TOP_POINT_STYLE] != null){
					m_parentSets.drawImage(_g,m_point[TOP_POINT_STYLE],_x + m_top_left.m_width ,
							_y - m_point[TOP_POINT_STYLE].m_height / 2);
				}
				
				break;
			case RIGHT_POINT_STYLE:
				if(m_point[RIGHT_POINT_STYLE] != null){
					if(m_point_down){
						m_parentSets.drawImage(_g,m_point[RIGHT_POINT_STYLE],_x + _width - m_point[RIGHT_POINT_STYLE].m_width / 2,
								_y + _height - (m_point[RIGHT_POINT_STYLE].getHeight() * 3 / 2));
					}else{
						m_parentSets.drawImage(_g,m_point[RIGHT_POINT_STYLE],_x + _width - m_point[RIGHT_POINT_STYLE].m_width / 2,
								_y + (m_point[RIGHT_POINT_STYLE].getHeight() / 2));
					}
					
				}				
				break;
			case BOTTOM_POINT_STYLE:
				if(m_point[BOTTOM_POINT_STYLE] != null){
					m_parentSets.drawImage(_g,m_point[BOTTOM_POINT_STYLE],_x + m_bottom_left.m_width * 2, 
							_y + _height - m_point[BOTTOM_POINT_STYLE].m_height / 2);
				}				
				break;
			}
		}		
		
		// fill the inner rectangle
		//
		m_parentSets.fillImageBlock(_g,m_innerBlock,_x + m_top_left.m_width ,_y + m_top_left.m_height,
			_width - m_top_left.m_width - m_top_right.m_width,_height - m_top_left.m_height - m_bottom_left.m_height);
	}
}