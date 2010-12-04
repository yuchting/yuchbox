package com.yuchting.yuchberry.client;

import javax.microedition.media.control.VolumeControl;

import net.rim.device.api.system.Alert;

public class reminder extends Thread{

	recvMain		m_mainApp = null;
	
	public reminder(recvMain _mainApp){
		m_mainApp = _mainApp;
		
		if(m_mainApp.m_vibrateTime != 0 || m_mainApp.m_soundVol != 0){
			start();
		}
	}
	
	public void run(){
		
		VolumeControl t_control = null;
		
		try{
			final int t_vibrate = 1000;
			
			for(int i = 0;i < m_mainApp.m_vibrateTime;i++){
				Alert.startVibrate(t_vibrate);
				sleep(t_vibrate * 4);
			}
			
			if(m_mainApp.m_soundVol != 0){
				t_control = (VolumeControl)m_mainApp.m_connectDeamon.m_newMailNotifier.getControl("VolumeControl");
				t_control.setLevel(m_mainApp.m_soundVol * 20);
				m_mainApp.m_connectDeamon.m_newMailNotifier.start();
			}
			
		}catch(Exception e){
			Alert.stopVibrate();
			
			if(t_control != null){
				try{
					m_mainApp.m_connectDeamon.m_newMailNotifier.stop();
					m_mainApp.m_connectDeamon.m_newMailNotifier.prefetch();
				}catch(Exception _e){
					m_mainApp.m_connectDeamon.LoadSound();
				}				
			}			
		}	
		
	}
}
