package com.yuchting.yuchberry.client.weibo;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.XYRect;

public class WeiboItemFocusField extends Field{
	
	public WeiboItemField m_itemField;
	
	public WeiboItemFocusField(WeiboItemField _dataField){
		super(Field.FOCUSABLE);
		
		m_itemField = _dataField;
	}
	
	public int	getPreferredHeight(){
		return m_itemField.getPreferredHeight();
	}
	
	public int	getPreferredWidth(){
		return m_itemField.getPreferredWidth();
	}

//	public boolean isFocusable(){
//		return WeiboItemField.sm_extendWeiboItem == null;
//	}
	
	protected  void	layout(int width, int height){
		
		m_itemField.sublayout(width, height);
		
		XYRect t_rect = m_itemField.getExtent();
		setExtent(t_rect.width, t_rect.height);
	}
	
	protected  void	paint(Graphics graphics){
		m_itemField.subpaint(graphics);
	}
	
	protected void drawFocus(Graphics _g,boolean _on){
		m_itemField.paintFocus(_g, _on);
		if(_on){
			WeiboItemField.sm_selectWeiboItem = m_itemField;
		}
	}
}
