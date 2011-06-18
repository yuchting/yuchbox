package com.yuchting.yuchberry.client.weibo;

import com.yuchting.yuchberry.client.recvMain;

import local.localResource;
import net.rim.device.api.ui.Graphics;

public class WeiboUpdateField extends WeiboItemField{
	
	String m_sendUpdateText = "";
	
	public WeiboUpdateField(WeiboMainManager _mainManager){
		super(_mainManager);
	}
	
	public void AddDelControlField(boolean _add){
		AddDelEditTextArea(_add,m_sendUpdateText);
		if(_add){
			//m_parentManager.m_editTextArea.setSelection(0,true,m_sendUpdateText.length());
			
			if(m_sendUpdateText.length() != 0){
				m_parentManager.m_editTextArea.select(true);
			}
		}
		
	}
	
	public int getPreferredHeight() {
		if(m_parentManager.getCurrExtendedItem() == this){
			return sm_fontHeight + sm_editTextAreaHeight;
		}else{
			return sm_fontHeight;
		}
	}
	
	public void sublayout(int width, int height){
		
		if(m_parentManager.getCurrExtendedItem() == this){
					
			setPositionChild(m_parentManager.m_editTextArea,0,0);
			layoutChild(m_parentManager.m_editTextArea,getPreferredWidth(),sm_editTextAreaHeight);
			
			height = sm_fontHeight + sm_editTextAreaHeight;
			
		}else{		
			height = sm_fontHeight;
		}
		
		setExtent(fsm_weiboItemFieldWidth,height);
	}
		
	public void subpaint(Graphics _g){
		
		if(m_parentManager.getCurrExtendedItem() == this){
			
			paintChild(_g,m_parentManager.m_editTextArea);
			
			int t_y = getPreferredHeight() - 1;
			_g.drawLine(0,t_y, getPreferredWidth(),t_y);
			
		}else{
			paintFocus(_g,isFocus());
		}
	}
	
	public void paintFocus(Graphics _g,boolean _on){
		int oldColour = _g.getColor();
        try{
        	
			if(m_parentManager.getCurrExtendedItem() != null){
				_g.setColor(fsm_darkColor);
				_g.fillRect(0,0, getPreferredWidth(),fsm_closeHeight);
				_g.setColor(0);
			}
				
			if(_on){
				_g.setColor(fsm_selectedColor);
				_g.fillRect(1,1,getPreferredWidth() - 2,sm_fontHeight - 3);
				_g.setColor(0xffffff);
			}else{
				_g.drawLine(0,sm_fontHeight - 1,getPreferredWidth(),sm_fontHeight - 1);
				_g.setColor(fsm_spaceLineColor);
			}	
        	
        	_g.drawText(recvMain.sm_local.getString(localResource.WEIBO_UPDATE_WEIBO_LABEL),2,2,Graphics.ELLIPSIS);
        }finally{
        	_g.setColor( oldColour );
        }
	}
}