package com.yuchting.yuchberry.client;


public class Indicator {

	//ApplicationIndicator  
//	static ApplicationIndicator		sm_weiboIndicator	= null;
//	static ApplicationIndicator		sm_imIndicator		= null;
		
	public static void registerIndicator(){
		if(recvMain.fsm_OS_version.startsWith("4.2") || recvMain.fsm_OS_version.startsWith("4.5")){
			return;
		}
		
//		EncodedImage t_weiboImage = EncodedImage.getEncodedImageResource("res/Main.png");
//		EncodedImage t_imImage = EncodedImage.getEncodedImageResource("res/Main.png");
//		
//        try {
//            ApplicationIndicatorRegistry reg = ApplicationIndicatorRegistry.getInstance();
//            sm_weiboIndicator 	= reg.register(new ApplicationIcon(t_weiboImage), true, true);
//            sm_imIndicator		= reg.register(new ApplicationIcon(t_imImage), true, true);
//        }catch(Exception ex){}
	}
}
