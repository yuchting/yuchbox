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

import local.yblocalResource;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;

import com.yuchting.yuchberry.client.recvMain;
import com.yuchting.yuchberry.client.ui.ImageUnit;

public class WeiboUpdateField extends Field{

	private final static int		fsm_textColor = recvMain.sm_standardUI?0x757575:0xffffff;
	private ImageUnit m_updateBitmap = null;
		
	private String m_updatePromptText = recvMain.sm_local.getString(yblocalResource.WEIBO_UPDATE_WEIBO_LABEL);
	
	String m_sendUpdateText = "";
	
	WeiboMainManager	m_parentManager = null;
	
	private ImageUnit	m_standardUI_selected = null;
	private ImageUnit	m_standardUI_background = null;
	
	public WeiboUpdateField(WeiboMainManager _mainManager){
		super(Field.FOCUSABLE);
		
		m_parentManager = _mainManager;
		
		if(recvMain.sm_standardUI){
			m_standardUI_selected 	= recvMain.sm_weiboUIImage.getImageUnit("refresh_selected_1");
			m_standardUI_background = recvMain.sm_weiboUIImage.getImageUnit("refresh_back_1");
			m_updateBitmap 			= recvMain.sm_weiboUIImage.getImageUnit("refresh_1");
		}else{
			m_standardUI_selected 	= recvMain.sm_weiboUIImage.getImageUnit("refresh_selected");
			m_standardUI_background = recvMain.sm_weiboUIImage.getImageUnit("refresh_back");
			m_updateBitmap 			= recvMain.sm_weiboUIImage.getImageUnit("refresh");
		}		
	}
	
	public int getPreferredHeight() {
		return WeiboItemField.sm_fontHeight + 5;
	}
		
	public int getPreferredWidth(){
		return WeiboItemField.fsm_weiboItemFieldWidth;
	}
	
	public void layout(int width, int height){
		setExtent(getPreferredWidth(), getPreferredHeight());
	}
		
	public void paint(Graphics _g){
		drawFocus(_g,isFocus());
	}
	
	protected void drawFocus(Graphics _g,boolean _on){
			
		int oldColour = _g.getColor();
		Font oldFont = _g.getFont();
		
        try{
			
        	int t_y = (getPreferredHeight() - WeiboItemField.sm_boldFont.getHeight()) / 2;
        	
			if(_on){	
				recvMain.sm_weiboUIImage.drawBitmapLine(_g, m_standardUI_selected, 0, 0, getPreferredWidth());			
			}else{
				if(!recvMain.sm_standardUI){
					recvMain.sm_weiboUIImage.drawBitmapLine(_g, m_standardUI_background, 0, 0, getPreferredWidth());
				}							
			}	
        	
			recvMain.sm_weiboUIImage.drawImage(_g,m_updateBitmap,2,t_y);
        	
			_g.setColor(fsm_textColor);
			_g.setFont(WeiboItemField.sm_boldFont);
        	_g.drawText(m_updatePromptText + m_parentManager.m_bufferedWeiboList.size() + " Weibo",
        			m_updateBitmap.getWidth() + 2,t_y,Graphics.ELLIPSIS);
        	
        }finally{
        	_g.setColor( oldColour );
        	_g.setFont(oldFont);
        }
	}
	
	protected void onUnfocus(){
    	super.onUnfocus();
    	invalidate();
    }
	
	public void invalidate(){
		super.invalidate();
	}
}