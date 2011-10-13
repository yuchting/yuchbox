package com.yuchting.yuchdroid.client;

import java.util.Arrays;
import java.util.Vector;

public class SendingQueue extends Thread{
	
	final class SendingQueueData{
		public int msgType;
		public byte[] msgData;
		
		public SendingQueueData(int _type,byte[] _data){
			msgType = _type;
			msgData = _data;
		}
	}
	
	Vector<SendingQueueData>	m_sendingData = new Vector<SendingQueueData>();
	ConnectDeamon				m_mainDeamon = null;
	
	boolean					m_destory	= false;
	
	public SendingQueue(ConnectDeamon _deamon){
		m_mainDeamon = _deamon;
		start();
	}
	
	public void destory(){
		m_destory = true;
		
		if(isAlive()){
			interrupt();
		}
	}
	
	public boolean addSendingData(int _msgType ,byte[] _data,boolean _exceptSame)throws Exception{
		
		if(!m_mainDeamon.isDisconnectState()){
			m_mainDeamon.m_connect.SendBufferToSvr(_data, false, true);
		}else{
			synchronized (m_sendingData) {
				if(_exceptSame){
					for(int i = 0 ;i < m_sendingData.size();i++){
						SendingQueueData t_data = (SendingQueueData)m_sendingData.elementAt(i);
						if(t_data.msgType == _msgType && Arrays.equals(t_data.msgData,_data)){
							return false;
						}
					}
				}
				m_sendingData.addElement(new SendingQueueData(_msgType,_data));
				
			}				
		}
		
		return true;
	}
			
	public void run(){
		
		while(!m_destory){
			
			try{

				while(m_mainDeamon.isDisconnectState() || m_sendingData.isEmpty()){
					
					if(m_destory){
						return ;
					}
					
					try{
						sleep(5000);
					}catch(Exception e){}
				}
				
				synchronized (m_sendingData) {
					for(int i = 0 ;i < m_sendingData.size();i++){
						SendingQueueData t_data = (SendingQueueData)m_sendingData.elementAt(i);
						m_mainDeamon.m_connect.SendBufferToSvr(t_data.msgData, false, false);
					}
					
					m_sendingData.removeAllElements();
				}
				
			}catch(Exception e){
				m_mainDeamon.SetErrorString("SQ:"+ e.getMessage() + " " + e.getClass().getName());
			}
		}
	}
	

}
