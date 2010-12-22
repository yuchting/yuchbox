package com.yuchting.yuchberry.server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;

public class fakeMDSSvr extends Thread{
	
	final static int		fsm_MDSPort = 19781;
	final static int		fsm_activatePackageLength = 18;
	
	DatagramSocket 			m_udpSocket = null;
	
	public fakeMDSSvr(){
		
		try{
			m_udpSocket = new DatagramSocket(fsm_MDSPort);
			
			start();
		}catch(Exception e){}		
	}
	
	public void run(){
		
		// thanks RAiN that ID in FeelBlackberry
		//
		
		byte[] t_packageBuf = new byte[fsm_activatePackageLength];
		byte[] response = {0x10, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
		
		while(true){
			
			try{
				DatagramPacket t_pack = new DatagramPacket(t_packageBuf,t_packageBuf.length);
				m_udpSocket.receive(t_pack);
												
				if(t_packageBuf[0] != 0x10 || t_packageBuf[1] != 0x08 
				|| t_packageBuf[6] != 0 || t_packageBuf[7] != 0 || t_packageBuf[8] != 0 || t_packageBuf[9] != 0){
					
					// illegal client connect
					//
				}else{
					
					for(int i = 0;i < 4;i++){
						response[i + 6] = t_packageBuf[i + 2];
					}
					
					response[10] = (byte)(t_packageBuf[10] - (byte)0x80);

					DatagramPacket t_sendPack = new DatagramPacket(response,response.length);
					m_udpSocket.send(t_sendPack);
					
				}
			}catch(Exception e){
				
			}			
		}
	}

}
