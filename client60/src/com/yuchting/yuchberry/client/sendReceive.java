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
package com.yuchting.yuchberry.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

import net.rim.device.api.compress.GZIPInputStream;
import net.rim.device.api.compress.GZIPOutputStream;


public class sendReceive extends Thread{
	
	public interface IStoreUpDownloadByte{
		public void store(long _uploadByte,long _downloadByte);
		public void debug(String _label,Exception _e);
		public void debug(String _info);
		
	}
	
	OutputStream		m_socketOutputStream = null;
	InputStream			m_socketInputStream = null;
	
	private Vector		m_unsendedPackage 		= new Vector();
	private Vector		m_unprocessedPackage 	= new Vector();
	
	int					m_sendBufferLen 		= 0;
	
	boolean			m_closed				= false;
	
	int					m_keepliveCounter		= 0;
	
	long				m_uploadByte			= 0;
	long				m_downloadByte			= 0;
	
	int					m_keepliveInterval		= 60 * 5;
	
	int					m_storeByteTimer		= 0;
	
	boolean			m_waitMoment			= false;
		
	static byte[]		sm_keepliveMsg 			= {1,0,0,0,msg_head.msgKeepLive};
	
	IStoreUpDownloadByte	m_storeInterface	= null;
		
	public sendReceive(OutputStream _socketOut,InputStream _socketIn){
		m_socketOutputStream = _socketOut;
		m_socketInputStream = _socketIn;
		
		start();
	}
	
	private void debugOut(String _info){
		if(m_storeInterface != null){
			m_storeInterface.debug(_info);
		}
	}
	
	private void debugOut(String _label,Exception _e){
		if(m_storeInterface != null){
			m_storeInterface.debug(_label,_e);
		}
	}
	
	public void SetKeepliveInterval(int _minutes){
		if(_minutes >= 0){
			m_keepliveInterval = _minutes * 60;
		}
	}
		
	public void RegisterStoreUpDownloadByte(IStoreUpDownloadByte _interface){
		m_storeInterface = _interface;
	}
	
	static final int	fsm_packageHeadLength = 4;
	
	//! send buffer
	public synchronized void SendBufferToSvr(byte[] _write,boolean _sendImm,
													boolean _wait)throws Exception{
		
		if(m_sendBufferLen + _write.length + fsm_packageHeadLength >= 65500){
			SendBufferToSvr_imple(PrepareOutputData());
		}
		
		m_sendBufferLen += _write.length + fsm_packageHeadLength;

		m_unsendedPackage.addElement(_write);
		
		if(_sendImm){
			SendBufferToSvr_imple(PrepareOutputData());
		}
				
		if(_wait){
			m_waitMoment = true;
		}
	}
	
	public synchronized void StoreUpDownloadByteImm(boolean _force){
		if(m_storeInterface != null){
			if(m_storeByteTimer++ > 5 || _force){
				m_storeByteTimer = 0;
				m_storeInterface.store(m_uploadByte,m_downloadByte);
				m_uploadByte = 0;
				m_downloadByte = 0;				
			}			
		}
	}
	
	public void CloseSendReceive(){
		
		if(m_closed == false){
			
			StoreUpDownloadByteImm(true);
			
			m_closed = true;
			
			m_unsendedPackage.removeAllElements();
			m_unprocessedPackage.removeAllElements();
			
			if(isAlive()){
				interrupt();
			}
			
			try{
				m_socketOutputStream.close();
				m_socketInputStream.close();
			}catch(Exception _e){
				debugOut("S_CSR",_e);
			}
				
			while(isAlive()){
				try{
					sleep(10);
				}catch(Exception _e){
					debugOut("S_CSR1",_e);
				};			
			}	
		}		
	}
	
	private byte[] PrepareOutputData()throws Exception{
		
		if(m_unsendedPackage.isEmpty()){
			return null;
		}
		
		ByteArrayOutputStream t_stream = new ByteArrayOutputStream();
		
		synchronized (m_unsendedPackage) {
					
			for(int i = 0;i < m_unsendedPackage.size();i++){
				byte[] t_package = (byte[])m_unsendedPackage.elementAt(i);	
				WriteInt(t_stream, t_package.length);		
				t_stream.write(t_package);
			}
		}
		
		m_unsendedPackage.removeAllElements();
		
		synchronized (this) {
			m_sendBufferLen = 0;
		}			
		
		return t_stream.toByteArray();
		
	}

	//! send buffer implement
	private  synchronized void SendBufferToSvr_imple(byte[] _write)throws Exception{
		
		if(_write == null){
			return;
		}
		
		synchronized(this){
			m_keepliveCounter = 0;
		}		
		
		ByteArrayOutputStream zos = new ByteArrayOutputStream();
		
		GZIPOutputStream zo = new GZIPOutputStream(zos,6);
		zo.write(_write);
		zo.close();	
		
		byte[] t_zipData = zos.toByteArray();
		
		if(t_zipData.length > _write.length){
			// if the ZIP data is large than original length
			// NOT convert
			//
			
			WriteInt(m_socketOutputStream,(_write.length << 16) & 0xffff0000);
			m_socketOutputStream.write(_write);
			m_socketOutputStream.flush();
			
			// 20 is TCP pack head length			
			m_uploadByte += _write.length + 4 + 20;
	
		}else{
			WriteInt(m_socketOutputStream,((_write.length << 16) & 0xffff0000) | t_zipData.length);
			m_socketOutputStream.write(t_zipData);
			m_socketOutputStream.flush();
				
			// 20 is TCP pack head length
			m_uploadByte += t_zipData.length + 4 + 20;
			
		}	
	}
	
	public void run(){
		
		try{
			boolean t_keeplive = false;
			
			while(!m_closed){
				
				SendBufferToSvr_imple(PrepareOutputData());
				
				sleep(1000);
				
				// wait moment
				//
				while(m_waitMoment){
					
					synchronized (this) {
						m_waitMoment = false;
					}
					
					sleep(300);
				}				
				
				synchronized (this){
					if(m_keepliveInterval != 0){
												
						m_keepliveCounter += 1;
						
						if(m_keepliveCounter > m_keepliveInterval){
							m_keepliveCounter = 0;
							t_keeplive = true;
						}
					}
					
				}				
				
				if(t_keeplive){
					t_keeplive = false;
										
					SendBufferToSvr_imple(sm_keepliveMsg);
					
					StoreUpDownloadByteImm(false);										
				}
			}
			
		}catch(Exception _e){
			debugOut("S_RUN",_e);
			
			try{
				m_socketOutputStream.close();
				m_socketInputStream.close();	
			}catch(Exception e){
				debugOut("S_RUN1",_e);
			}
		}
	}

	//! recv buffer
	public byte[] RecvBufferFromSvr()throws Exception{
		
		if(!m_unprocessedPackage.isEmpty()){
			byte[] t_ret = (byte[])m_unprocessedPackage.elementAt(0);
			m_unprocessedPackage.removeElementAt(0);
			
			return t_ret;
		}
		
		synchronized (this) {
			m_keepliveCounter = 0;
		}
		
		InputStream in = m_socketInputStream;

		final int t_len = ReadInt(in);
		if(t_len == -1){
			throw new Exception("socket ReadInt failed.");
		}
		
		final int t_ziplen = t_len & 0x0000ffff;
		final int t_orglen = t_len >>> 16;
				
		byte[] t_orgdata = new byte[t_orglen];
				
		if(t_ziplen == 0){
			
			ForceReadByte(in, t_orgdata, t_orglen);
			
			synchronized (this) {
				// 20 is TCP pack head length
				m_downloadByte += t_orglen + 4 + 20;
			}
			
		}else{
			
			byte[] t_zipdata = new byte[t_ziplen];
			
			ForceReadByte(in, t_zipdata, t_ziplen);
			
			synchronized (this) {
				// 20 is TCP pack head length
				m_downloadByte += t_ziplen + 4 + 20;
			}
			
			GZIPInputStream zi	= new GZIPInputStream(new ByteArrayInputStream(t_zipdata));

			ForceReadByte(zi,t_orgdata,t_orglen);
			
			zi.close();
		}
		
		byte[] t_ret = ParsePackage(t_orgdata);
		t_orgdata = null;
				
		
		return t_ret;
	}
	
	private byte[] ParsePackage(byte[] _wholePackage)throws Exception{
		
		ByteArrayInputStream t_packagein = new ByteArrayInputStream(_wholePackage);
		int t_len = ReadInt(t_packagein);
					
		byte[] t_ret = new byte[t_len];
		t_packagein.read(t_ret,0,t_len);
		
		t_len += 4;
		
		while(t_len < _wholePackage.length){
			
			final int t_packageLen = ReadInt(t_packagein); 
			
			byte[] t_package = new byte[t_packageLen];
			
			t_packagein.read(t_package,0,t_packageLen);
			t_len += t_packageLen + 4;
			
			m_unprocessedPackage.addElement(t_package);			
		}		
		
		return t_ret;		
	}
	// static function to input and output integer
	//
	static public void WriteStringVector(OutputStream _stream,Vector _vect)throws Exception{
		
		final int t_size = _vect.size();
		WriteInt(_stream,t_size);
		
		for(int i = 0;i < t_size;i++){
			WriteString(_stream,(String)_vect.elementAt(i));
		}
	}
	
	static public void WriteString(OutputStream _stream,String _string)throws Exception{
		if(_string == null){
			_string = "";
		}
		
		byte[] t_strByte;
		
		try{
			// if the UTF-8 decode sytem is NOT present in current system
			// will throw the exception
			//
			t_strByte = _string.getBytes("UTF-8");
		}catch(Exception e){
			t_strByte = _string.getBytes();
		}
		
		WriteInt(_stream,t_strByte.length);
		if(t_strByte.length != 0){
			_stream.write(t_strByte);
		}
	}
	
	static public void WriteDouble(OutputStream _stream,double _val)throws Exception{
		if(_val == 0){
			WriteInt(_stream,0);
		}else{
			String t_valString = Double.toString(_val);
			WriteString(_stream,t_valString);
		}		
	}
	
	static public void WriteFloat(OutputStream _stream,float _val)throws Exception{
		if(_val == 0){
			WriteInt(_stream,0);
		}else{
			String t_valString = Float.toString(_val);
			WriteString(_stream,t_valString);
		}
	}
	
	static public double ReadDouble(InputStream _stream)throws Exception{
		String t_valString = ReadString(_stream);
		if(t_valString.length() == 0){
			return 0;
		}else{
			return Double.valueOf(t_valString).doubleValue();			
		}
		
	}
	
	static public float ReadFloat(InputStream _stream)throws Exception{
		String t_valString = ReadString(_stream);
		if(t_valString.length() == 0){
			return 0;
		}else{
			return Float.valueOf(t_valString).floatValue();
		}
	}
	
	static public void WriteBoolean(OutputStream _stream,boolean _val)throws Exception{
		_stream.write(_val?1:0);
	}
	
	static public boolean ReadBoolean(InputStream _stream)throws Exception{
		
		int t_counter = 0;
		int t_val = 0;
		while(true){
			
			t_val = _stream.read();				
			
			if(t_val == -1){
				
				if(t_counter++ >= 20){
					return false;
				}
				
				// first sleep 
				//
				Thread.sleep(20);
				continue;
				
			}else{
				break;
			}
		}			

		return t_val == 1;		
	}
		
	static public void ReadStringVector(InputStream _stream,Vector _vect)throws Exception{
		
		_vect.removeAllElements();
		
		final int t_size = ReadInt(_stream);
				
		for(int i = 0;i < t_size;i++){
			_vect.addElement(ReadString(_stream));
		}
	}
	
	static public String ReadString(InputStream _stream)throws Exception{
		
		final int len = ReadInt(_stream);
		
		if(len != 0){
			byte[] t_buffer = new byte[len];
			
			ForceReadByte(_stream,t_buffer,len);

			try{
				// if the UTF-8 decode sytem is NOT present in current system
				// will throw the exception
				//
				return new String(t_buffer,"UTF-8");
			}catch(Exception e){}
			
			return new String(t_buffer);
			
		}
		
		return "";
		
	}
	
	static public int ReadInt(InputStream _stream)throws Exception{
		
		int[] t_byte = {0,0,0,0};
	
		int t_counter = 0;
		
		for(int i = 0;i < t_byte.length;i++){
			
			while(true){
				
				t_byte[i] = _stream.read();				
				
				if(t_byte[i] == -1){
					
					if(t_counter++ >= 20){
						return -1;
					}
					
					// first sleep 
					//
					Thread.sleep(20);					
					continue;
					
				}else{
					break;
				}
			}			
						
		}
		
		return t_byte[0] | (t_byte[1] << 8) | (t_byte[2]  << 16) | (t_byte[3] << 24);
			
	}
	
	static public long ReadLong(InputStream _stream)throws Exception{
		final int t_timeLow = sendReceive.ReadInt(_stream);
		final long t_timeHigh = sendReceive.ReadInt(_stream);
				
		if(t_timeLow >= 0){
			return ((t_timeHigh << 32) | (long)(t_timeLow));
		}else{
			return ((t_timeHigh << 32) | (((long)(t_timeLow & 0x7fffffff)) | 0x80000000L));
		}
	}
		
	static public void WriteLong(OutputStream _stream,long _val)throws Exception{		
		sendReceive.WriteInt(_stream,(int)_val);
		sendReceive.WriteInt(_stream,(int)(_val >>> 32));
	}
	
	static public void WriteInt(OutputStream _stream,int _val)throws Exception{
		_stream.write(_val);
		_stream.write(_val >>> 8 );
		_stream.write(_val >>> 16);
		_stream.write(_val >>> 24);
	}
	
	static public void ForceReadByte(InputStream _stream,byte[] _buffer,int _readLen)throws Exception{
		int t_readIndex = 0;
		int t_counter = 0;
		
		while(_readLen > t_readIndex){
			final int t_c = _stream.read(_buffer,t_readIndex,_readLen - t_readIndex);
			if(t_c > 0){
				t_readIndex += t_c;
			}else{
				
				if(++t_counter > 20){
					throw new Exception("FroceReadByte failed " + _readLen );
				}
				
				// first sleep 
				//
				Thread.sleep(20);	
			}		
		}
	}
	
}

