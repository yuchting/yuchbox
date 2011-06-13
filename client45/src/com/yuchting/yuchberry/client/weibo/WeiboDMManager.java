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
					if(field.AddSameSender(_weibo,_image)){
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
		for(int i = 0 ;i < t_num;i++){
			
			final WeiboItemFocusField t_field = (WeiboItemFocusField)getField(i);
			final WeiboDMItemField t_managerField = (WeiboDMItemField)t_field.m_itemField;
			
			if(t_managerField.delWeibo(_weibo)){
				
				m_mainApp.invokeLater(new Runnable() {
					
					public void run() {
							
						if(t_managerField.isEmptyPost()){
							// delete the field/manager item form the whole dm list
							//
							if(WeiboDMManager.this.getCurrExtendedItem() == t_managerField){
								
								WeiboDMManager.this.setCurrExtendedItem(null);
								WeiboDMManager.this.setCurrEditItem(null);
								
								delete(t_managerField);
							}else{
								delete(t_field);
							}
						}						
					}
				});
				
				return true;
			}
		}
		
		return false;		
	}

}
