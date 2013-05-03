package com.yuchting.yuchberry.client;

//import net.rim.blackberry.api.messagelist.ApplicationIcon;
//import net.rim.blackberry.api.messagelist.ApplicationIndicator;
//import net.rim.blackberry.api.messagelist.ApplicationIndicatorRegistry;
//import net.rim.device.api.system.EncodedImage;
//
//
///**
// * indicator (notification of current application)
// * @author yuch
// *
// */
//public class Indicator {
//
//	//ApplicationIndicator  
//	static int	sm_weiboCount	= 	0;
//	static int	sm_imCount		= 	0;
//	
//	static ApplicationIndicator		sm_indicator	= null;
//	
//	
//	static ApplicationIcon			sm_currIndicatorIcon = null;
//	
//	static ApplicationIcon	sm_weiboIcon = null;
//	static ApplicationIcon	sm_imIcon = null;
//	static{
//		EncodedImage t_weiboImage = EncodedImage.getEncodedImageResource("weibo_indicator.png");
//		EncodedImage t_imImage = EncodedImage.getEncodedImageResource("im_indicator.png");
//		sm_weiboIcon = new ApplicationIcon(t_weiboImage);
//		sm_imIcon	= new ApplicationIcon(t_imImage);
//	}
//	
//	/**
//	 * unregister the indicator when app is closed
//	 */
//	public static void unregisterIndicator(){
//		if(sm_indicator != null){
//
//			try {
//				sm_indicator.setValue(0);
//				sm_indicator.setVisible(false);
//				
//				sm_imCount = sm_weiboCount = 0;
//				
//	            ApplicationIndicatorRegistry reg = ApplicationIndicatorRegistry.getInstance();
//	            reg.unregister();
//	        }catch(Exception ex){}
//	        
//	        sm_indicator = null;
//		}
//	}
//	
//	public static void notifyWeibo(){
//		
//		try{
//			if(sm_currIndicatorIcon != sm_weiboIcon || sm_indicator == null){
//				unregisterIndicator();
//				
//				ApplicationIndicatorRegistry reg = ApplicationIndicatorRegistry.getInstance();
//				sm_indicator = reg.register(sm_weiboIcon,false,true);
//			}			
//			
//			sm_currIndicatorIcon = sm_weiboIcon;
//			sm_indicator.set(sm_weiboIcon,++sm_weiboCount);
//			sm_indicator.setVisible(true);
//		}catch(Exception ex){}		
//	}
//	
//	public static void notifyIM(){
//		try{
//			if(sm_currIndicatorIcon != sm_imIcon || sm_indicator == null){
//				unregisterIndicator();
//				
//	        	ApplicationIndicatorRegistry reg = ApplicationIndicatorRegistry.getInstance();
//	        	sm_indicator	= reg.register(sm_imIcon,false,true);
//			}
//			sm_currIndicatorIcon = sm_imIcon;
//    		sm_indicator.set(sm_imIcon,++sm_imCount);
//    		sm_indicator.setVisible(true);
//		}catch(Exception ex){}
//		
//	}
//	
//	public static void disableNotifiyWeibo(){
//		if(sm_indicator != null){
//			
//			try{	        	
//				if(sm_imCount != 0){		        	
//					sm_indicator.set(sm_imIcon,sm_imCount);
//				}else{
//					unregisterIndicator();
//				}
//		    
//			}catch(Exception ex){}
//			
//			sm_weiboCount = 0;			
//			
//		}
//	}
//	
//	public static void disableNotifyIM(){
//		if(sm_indicator != null){
//	
//			try{        	
//				if(sm_weiboCount != 0){		        	
//					sm_indicator.set(sm_weiboIcon,sm_weiboCount);
//				}else{
//					unregisterIndicator();
//				}
//
//			}catch(Exception ex){}
//						
//	        sm_imCount = 0;		
//		}
//	}
//}


/**
 * indicator (notification of current application)
 * @author yuch
 *
 */
public class Indicator {
	
	/**
	 * unregister the indicator when app is closed
	 */
	public static void unregisterIndicator(){ }
	
	public static void notifyWeibo(){	}
	
	public static void notifyIM(){ }
	
	public static void disableNotifiyWeibo(){ }
	
	public static void disableNotifyIM(){	}
}
