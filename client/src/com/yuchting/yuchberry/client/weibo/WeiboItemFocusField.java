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
	
	protected  void	layout(int width, int height){
		
		m_itemField.sublayout(width, height);
		
		XYRect t_rect = m_itemField.getExtent();
		setExtent(t_rect.width, t_rect.height);
	}
	
	protected  void	paint(Graphics graphics){
		m_itemField.subpaint(graphics);
	}
	
	protected void drawFocus(Graphics _g,boolean _on){
		
		if(_on && m_itemField.getParentManager().getCurrExtendedItem() == null){
			
			m_itemField.paintFocus(_g, true);
			m_itemField.getParentManager().setCurrSelectedItem(m_itemField);
		}else{
			m_itemField.paintFocus(_g, false);	
		}
		
		m_itemField.m_parentManager.invalidateScroll();
	}
}
