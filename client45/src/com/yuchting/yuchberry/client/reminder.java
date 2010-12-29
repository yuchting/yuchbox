package com.yuchting.yuchberry.client;

import javax.microedition.media.control.VolumeControl;

import net.rim.device.api.system.Alert;
import net.rim.device.api.system.Backlight;

public class reminder extends Thread{

	recvMain		m_mainApp = null;
	
	public reminder(recvMain _mainApp){
		m_mainApp = _mainApp;
				
		start();
		
	}
	
	public void run(){
		
		m_mainApp.TriggerNotification();
		
		try{
			
//			final int 	t_vibrate 		= 1000;
//			final int 	t_vibrateFrag 	= 10;
//			
//			int 		t_vibrateNum 	= 0;
//			
//			for(int i = 0;i < m_mainApp.m_vibrateTime;i++){
//				Alert.startVibrate(t_vibrate);
//				
//				// check the BackLight is on
//				//
//				t_vibrateNum = 0;
//				
//				while(t_vibrateNum++ < t_vibrateFrag * 3){
//					
//					sleep(t_vibrate/t_vibrateFrag);
//					
//					if(Backlight.isEnabled()){
//						throw new Exception("");
//					}
//				}
//			}
					
		}catch(Exception e){
					

		}
		

		try{
			sleep(10000);
		}catch(Exception e){}
	}
}
