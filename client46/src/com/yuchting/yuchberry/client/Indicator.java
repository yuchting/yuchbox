package com.yuchting.yuchberry.client;

import net.rim.blackberry.api.messagelist.ApplicationIcon;
import net.rim.blackberry.api.messagelist.ApplicationIndicator;
import net.rim.blackberry.api.messagelist.ApplicationIndicatorRegistry;
import net.rim.device.api.system.EncodedImage;


/**
 * indicator (notification of current application)
 * @author yuch
 *
 */
public class Indicator {

	//ApplicationIndicator  
	static int	sm_weiboCount	= 	0;
	static int	sm_imCount		= 	0;
	
	static ApplicationIndicator		sm_weiboIndicator	= null;
	static ApplicationIndicator		sm_imIndicator		= null;
		
	/**
	 * register the indicator resource 
	 * it must be called at initialization of app
	 */
	public static void registerIndicator(){
		
		if(sm_weiboIndicator == null && sm_imIndicator == null){
			EncodedImage t_weiboImage = EncodedImage.getEncodedImageResource("weibo_indicator.png");
			EncodedImage t_imImage = EncodedImage.getEncodedImageResource("im_indicator.png");
			
	        try {
	            ApplicationIndicatorRegistry reg = ApplicationIndicatorRegistry.getInstance();
	            sm_weiboIndicator 	= reg.register(new ApplicationIcon(t_weiboImage), false, false);
	            sm_imIndicator		= reg.register(new ApplicationIcon(t_imImage), false, false);
	        }catch(Exception ex){}
		}	
	}
	
	/**
	 * unregister the indicator when app is closed
	 */
	public static void unregisterIndicator(){
		if(sm_weiboIndicator != null && sm_imIndicator != null){
			
			disableNotifiyWeibo();
			disableNotifyIM();
			
			try {
	            ApplicationIndicatorRegistry reg = ApplicationIndicatorRegistry.getInstance();
	            reg.unregister();
	        }catch(Exception ex){}
		}
	}
	
	public static void notifyWeibo(){
		if(sm_weiboIndicator != null){
			sm_weiboIndicator.setVisible(true);
			sm_weiboIndicator.setValue(++sm_weiboCount);
		}		
	}
	
	public static void notifyIM(){
		if(sm_imIndicator != null){
			sm_imIndicator.setVisible(true);
			sm_imIndicator.setValue(++sm_imCount);
		}
	}
	
	public static void disableNotifiyWeibo(){
		if(sm_weiboIndicator != null){
			sm_weiboIndicator.setVisible(false);
			sm_weiboCount = 0;
		}
	}
	
	public static void disableNotifyIM(){
		if(sm_imIndicator != null){
			sm_imIndicator.setVisible(false);
			sm_imCount = 0;
		}
	}
}
