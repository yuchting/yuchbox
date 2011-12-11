/**
 *  Dear developer:
 *  
 *   If you want to modify this file of project and re-publish this please visit:
 *  
 *     http://code.google.com/p/yuchberry/wiki/Project_files_header
 *     
 *   to check your responsibility and my humble proposal. Thanks!
 *   
 *  -- 
 *  Yuchs' Developer    
 *  
 *  
 *  
 *  
 *  尊敬的开发者：
 *   
 *    如果你想要修改这个项目中的文件，同时重新发布项目程序，请访问一下：
 *    
 *      http://code.google.com/p/yuchberry/wiki/Project_files_header
 *      
 *    了解你的责任，还有我卑微的建议。 谢谢！
 *   
 *  -- 
 *  语盒开发者
 *  
 */
package com.yuchting.yuchberry.server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class fakeMDSSvr extends Thread{
	
	final static int		fsm_MDSPort = 19781;
	final static int		fsm_activatePackageLength = 18;
	
	DatagramSocket 			m_udpSocket = null;
	
	public fakeMDSSvr(){
		
		try{
			m_udpSocket = new DatagramSocket(fsm_MDSPort);
			
			start();
		}catch(Exception e){
			// illegal client connect
			//
			System.out.println("fakeMDSSvr Has Been exist!");
		}		
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
				
				InetAddress t_addr =  t_pack.getAddress();
				int t_port = t_pack.getPort();
				
				if(t_packageBuf[0] != 0x10 || t_packageBuf[1] != 0x08 
				|| t_packageBuf[6] != 0 || t_packageBuf[7] != 0 || t_packageBuf[8] != 0 || t_packageBuf[9] != 0){
					
					// illegal client connect
					//
					System.out.print("illegal client");
					
				}else{
					
					for(int i = 0;i < 4;i++){
						response[i + 6] = t_packageBuf[i + 2];
					}
					
					response[10] = (byte)(t_packageBuf[10] - (byte)0x80);

					DatagramPacket t_sendPack = new DatagramPacket(response,response.length,t_addr,t_port);
					m_udpSocket.send(t_sendPack);
					
				}
			}catch(Exception e){
				try{
					sleep(10000);
				}catch(Exception _e){
					break;
				}
			}			
		}
	}

}
