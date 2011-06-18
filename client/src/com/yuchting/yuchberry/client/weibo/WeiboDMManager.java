package com.yuchting.yuchberry.client.weibo;

import net.rim.device.api.ui.Field;

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
					Field field = getField(i);
					WeiboDMItemField t_dmField;
					
					if(field instanceof WeiboItemFocusField){
						t_dmField = (WeiboDMItemField)((WeiboItemFocusField)field).m_itemField;
					}else{
						t_dmField = (WeiboDMItemField)(field);
					}
					
					if(t_dmField.AddSameSender(_weibo,_image)){
						return;
					}
				}
				
				WeiboDMItemField t_field = new WeiboDMItemField(_weibo,_image,WeiboDMManager.this);
	
				insert(t_field.m_focusField,0);
								
				if(_resetSelectIdx){
					m_hasNewWeibo = true;
				}
			}
		});					
	}
	
	public boolean DelWeibo(final fetchWeibo _weibo){
		
		int t_num = getFieldCount();
		for(int i = t_num - 1;i >= 0;i--){
			
			Field t_field = getField(i);
			
			if(t_field instanceof WeiboItemFocusField){
				
				final WeiboItemFocusField t_focusField = (WeiboItemFocusField)t_field;
				final WeiboDMItemField t_managerField = (WeiboDMItemField)t_focusField.m_itemField;
				
				if(t_managerField.delWeibo(_weibo) && t_managerField.isEmptyPost()){
					
					m_mainApp.invokeLater(new Runnable() {
						
						public void run() {
							delete(t_focusField);
						}
					});
					
					return true;
				}
				
			}else if(t_field instanceof WeiboDMItemField){
				
				final WeiboDMItemField t_managerField = (WeiboDMItemField)t_field;
				
				if(t_managerField.delWeibo(_weibo) && t_managerField.isEmptyPost()){
					
					m_mainApp.invokeLater(new Runnable() {
						
						public void run() {
				
							WeiboDMManager.this.EscapeKey(); //escape edit field 
							WeiboDMManager.this.EscapeKey(); //escape control field
							
							delete(t_managerField.getFocusField());
						}
					});
					
					return true;
				}
			}		
		}
		
		return false;		
	}

}
