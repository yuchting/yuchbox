package com.yuchting.yuchberry.client.weibo;

import java.util.Vector;

import net.rim.device.api.ui.Graphics;

import com.yuchting.yuchberry.client.recvMain;

final class WeiboDMData{
	
	fetchWeibo		m_weibo;
	String			m_renderText;
	int				m_dataItemHeight;
	
	public WeiboDMData(fetchWeibo _weibo){
		m_weibo	= _weibo;
		
		StringBuffer t_string = new StringBuffer();
		t_string.append("@").append(_weibo.GetUserScreenName()).append(": ").
				append(_weibo.GetText()).append(" (").append(WeiboDMItemField.getTimeString(_weibo)).append(")");
		
		m_renderText = t_string.toString();
		t_string = null;
		
		WeiboDMItemField.sm_testTextArea.setText(m_renderText);
		m_dataItemHeight = WeiboDMItemField.sm_testTextArea.getHeight();
	
		int t_minHeight = WeiboItemField.fsm_weiboSignImageSize + WeiboItemField.fsm_headImageWidth;
		if(m_dataItemHeight < t_minHeight){
			m_dataItemHeight = t_minHeight;			
		}
	}
}


public class WeiboDMItemField extends WeiboItemField{
		
	//! the list of direct message
	Vector m_DMList		= new Vector();
	
	public WeiboDMItemField(fetchWeibo _weibo,WeiboHeadImage _headImage){
		super(_weibo,_headImage);
		m_DMList.addElement(_weibo);
	}
	
	public void AddDelControlField(boolean _add){
		if(_add){
			
		}else{			
			
		}							
	}
	
	public void subpaint(Graphics _g){
		
		if(sm_extendWeiboItem == this){
						
		}else{
			super.subpaint(_g);			
		}		
	}
	
	public void sublayout(int width, int height){
		
		if(sm_extendWeiboItem == this){
									
		}else{		
			height = fsm_closeHeight;
		}
		
		setExtent(recvMain.fsm_display_width,height);
	}
	
	public boolean AddSameSender(fetchWeibo _weibo	){
		for(int i = m_DMList.size() - 1;i >= 0;i++){
			
			fetchWeibo weibo = (fetchWeibo)m_DMList.elementAt(i);
			
			if(_weibo.GetWeiboStyle() == _weibo.GetWeiboStyle()){
				
				if((_weibo.GetUserId() == weibo.GetUserId()
					&& _weibo.GetReplyWeiboId() == weibo.GetReplyWeiboId())
					
				|| (_weibo.GetUserId() == weibo.GetReplyWeiboId()
					&& _weibo.GetReplyWeiboId() == weibo.GetUserId())){
					
					m_DMList.insertElementAt(_weibo,0);
					m_weibo = _weibo;
					
					return true;				
				}
			}
			
		}
		
		return false;
	}
}
