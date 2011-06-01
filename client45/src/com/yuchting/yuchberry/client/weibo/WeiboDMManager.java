package com.yuchting.yuchberry.client.weibo;

import com.yuchting.yuchberry.client.recvMain;

public class WeiboDMManager extends WeiboMainManager{
	
	public WeiboDMManager(recvMain _mainApp,weiboTimeLineScreen _parentScreen,boolean _timelineManager){
		super(_mainApp,_parentScreen,false);		
	}
	
	public void AddWeibo(final fetchWeibo _weibo,final WeiboHeadImage _image,
							final boolean _resetSelectIdx){
		
		m_mainApp.invokeLater(new Runnable() {
			
			public void run() {
				
				int t_num = getFieldCount();
				for(int i = 0 ;i < t_num;i++){
					WeiboDMItemField field = (WeiboDMItemField)getField(i);
					if(field.AddSameSender(_weibo)){
						return;
					}
				}
				
				WeiboDMItemField t_field = new WeiboDMItemField(_weibo,_image);
				insert(t_field,0);
								
				if(_resetSelectIdx){
					
					m_selectWeiboItemIndex++;
					m_formerVerticalPos += WeiboItemField.fsm_closeHeight;
					
					if(WeiboItemField.sm_extendWeiboItem == null ){	
						RestoreScroll();							
					}
					
					m_hasNewWeibo = true;
				}
			}
		});					
	}
	
	public boolean DelWeibo(final fetchWeibo _weibo){
		
		int t_num = getFieldCount();
		for(int i = 0 ;i < t_num;i++){
			WeiboItemField t_field = (WeiboItemField)getField(i);
			if(t_field.m_weibo == _weibo){
				
				m_mainApp.invokeLater(new Runnable() {
					
					public void run() {
						
						int t_num = getFieldCount();
						
						for(int i = 0 ;i < t_num;i++){
							WeiboItemField t_field = (WeiboItemField)getField(i);
							if(t_field.m_weibo == _weibo){
								delete(t_field);
								
								break;
							}
						}
						
						if(WeiboItemField.sm_extendWeiboItem == null){			
							RestoreScroll();					
						}
					}
				});
				
				return true;
			}
		}
		
		return false;		
	}

}
