package com.yuchting.yuchdroid.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Vector;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemClock;

public class sendReceive{
	
	public static interface IStoreUpDownloadByte{
		void store(long _uploadByte,long _downloadByte);
		int getPushInterval();
		void logOut(String _log);
		void acquireCPUWakeLock();
		void releaseCPUWakeLock();
	}
	
	public static String TAG = sendReceive.class.getName();
	static final int		fsm_packageHeadLength 	= 4;
	static final byte[] 	fsm_keepliveMsg 		= {1,0,0,0,msg_head.msgKeepLive};
	
	private Selector	m_selector				= null;
	private SocketChannel m_socketChn			= null;
			
	private Vector<byte[]>		m_unsendedPackage 		= new Vector<byte[]>();
	private Vector<byte[]>		m_unprocessedPackage 	= new Vector<byte[]>();
	
	int					m_sendBufferLen 		= 0;
	
	boolean			m_closed				= false;
	
	long				m_uploadByte			= 0;
	long				m_downloadByte			= 0;
		
	int					m_storeByteTimer		= 0;
	
	IStoreUpDownloadByte	m_storeInterface	= null;
	
	private final static String FILTER_PULSE	= TAG+"_FP";
	private PendingIntent	m_pulseAlarm = null;
	private BroadcastReceiver m_pulseAlarmRecv = null;
	private Context	m_context	= null;
	private int			m_formerPulseInterval = 0;
	
	private long		m_pulseTime				= SystemClock.elapsedRealtime();		
	private Vector<ByteBuffer>					m_sendBufferVect = new Vector<ByteBuffer>();
			
	public sendReceive(Context _ctx,Selector _selector,SocketChannel _chn,boolean _ssl,IStoreUpDownloadByte _callback)throws Exception{

		if(_ssl){
			throw new IllegalArgumentException(TAG + " Current YuchDroid can't support !");
		}
		
		m_context	= _ctx;
		m_selector	= _selector;
		m_socketChn = _chn;
						
		m_storeInterface = _callback;
		
		// register the selector 
		//
		m_socketChn.register(m_selector,SelectionKey.OP_WRITE);
		
		startAlarmForPulse();
	}
					
	public void RegisterStoreUpDownloadByte(IStoreUpDownloadByte _interface){
		m_storeInterface = _interface;
	}
	
	//! send buffer
	public void SendBufferToSvr(byte[] _write,boolean _sendImm)throws Exception{
		
		synchronized (m_unsendedPackage) {
			if(m_sendBufferLen + _write.length + fsm_packageHeadLength >= 65535){
				SendBufferToSvr_imple(PrepareOutputData());
			}		
			m_sendBufferLen += _write.length + fsm_packageHeadLength;

			m_unsendedPackage.addElement(_write);
		}
			
		m_selector.wakeup();			
	}
	
	public void StoreUpDownloadByteImm(boolean _force){
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
			
			try{
				m_selector.wakeup();
			}catch(Exception e){}
		}
		
		stopAlarmForPulse();
	}
	
	private void startAlarmForPulse(){
		if(m_pulseAlarm == null){
			
			m_formerPulseInterval = m_storeInterface.getPushInterval();
			
			AlarmManager t_msg = (AlarmManager)m_context.getSystemService(Context.ALARM_SERVICE);
			Intent notificationIntent = new Intent(FILTER_PULSE);
			m_pulseAlarm = PendingIntent.getBroadcast(m_context, 0, notificationIntent,0);
			t_msg.setRepeating(AlarmManager.RTC_WAKEUP, 
							System.currentTimeMillis() + m_formerPulseInterval, 
							m_formerPulseInterval, /* a bit less*/ 
							m_pulseAlarm);
			
			if(m_pulseAlarmRecv == null){
				m_pulseAlarmRecv = new BroadcastReceiver() {
					
					@Override
					public void onReceive(Context context, Intent intent){
						// acquire the cpu wake lock 
						// the Selector.wakeup function is not wakeup Selector.select in deep sleep CPU state     - -! 
						//
						m_storeInterface.acquireCPUWakeLock();
												
						try{
							// sleep 100 millisecond to wait CPU to wake up... 
							//
							SystemClock.sleep(100);
							
							m_selector.wakeup();
							m_storeInterface.logOut("keeplive wakeup");
						}catch(Exception e){}
						
						if(m_formerPulseInterval != m_storeInterface.getPushInterval()){
							m_formerPulseInterval = m_storeInterface.getPushInterval();
							stopAlarmForPulse();
							startAlarmForPulse();
						}
					}
				};
			}
			
			m_context.registerReceiver(m_pulseAlarmRecv, new IntentFilter(FILTER_PULSE));
		}
	}
	
	private void stopAlarmForPulse(){
		if(m_pulseAlarm != null){
			m_pulseAlarm.cancel();
			m_pulseAlarm = null;
			
			m_context.unregisterReceiver(m_pulseAlarmRecv);
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
			
			m_unsendedPackage.removeAllElements();
			m_sendBufferLen = 0;
		}	
		
		return t_stream.toByteArray();
	}
	
	boolean m_writekeeplive = false;
	private byte[] readData(int _len)throws Exception{
		
		ByteBuffer t_retBuf = ByteBuffer.allocate(_len);
		int t_readLen = 0;
		
		int t_selectkey = 0;
		while(true){
			
			t_selectkey = m_selector.select();
			
			if(m_closed){
				m_socketChn.close();
				m_selector.close();				
				throw new Exception(TAG + " Client own closed!");
			}
			
			if(!m_unsendedPackage.isEmpty()){
				SendBufferToSvr_imple(PrepareOutputData());				
				m_socketChn.keyFor(m_selector).interestOps(SelectionKey.OP_WRITE);
				
				// the next select will hold the write op
				//
				continue;
			}
				
			if(t_selectkey != 0){
				
				Iterator<SelectionKey> it = m_selector.selectedKeys().iterator();
				while(it.hasNext()){
					SelectionKey key = it.next();
					it.remove();
					
					if(key.isValid()){
						
						m_pulseTime = SystemClock.elapsedRealtime();;
						
						if(key.isReadable()){
							SocketChannel t_chn = (SocketChannel)key.channel();						
							
							int len = t_chn.read(t_retBuf);
							
							if(len == -1){
								throw new Exception(TAG + " Client read -1 to closed!"); 
							}
							
							t_readLen += len;
							if(t_readLen < _len){
								continue;
							}
							
							t_retBuf.flip(); 
							return t_retBuf.array();
							
						}else if(key.isWritable()){	
							
							sendDataByChn_impl(key);
							
							if(m_writekeeplive){
								m_writekeeplive = false;
								m_storeInterface.logOut("keeplive write!!");
								
								// release CPU wake lock to let CPU to deep sleep 
								// after sending keeplive packet over 
								//
								m_storeInterface.releaseCPUWakeLock();
							}
						}else{
							m_storeInterface.logOut("valid key (!isWritable && !isReadable)");
						}
					}
				}
				
			}else{
				
				if(!m_socketChn.isConnected()){
					throw new Exception(TAG + " Socket chn is not connected!");
				}else{
					long t_formerTimer = SystemClock.elapsedRealtime();
					
					if(Math.abs(t_formerTimer - m_pulseTime) >= m_formerPulseInterval - 200){
						
						m_pulseTime = t_formerTimer;
						
						// send the keeplive message
						//
						SendBufferToSvr_imple(fsm_keepliveMsg);
						m_socketChn.keyFor(m_selector).interestOps(SelectionKey.OP_WRITE);
						
						m_storeInterface.logOut("keeplive OR_WRITE");
						m_writekeeplive = true;
					}
				}
			}			
		}
	}
	
	private void sendDataByChn_impl(SelectionKey _key)throws Exception{
		
	    SocketChannel t_socketChn = (SocketChannel)_key.channel();

	    synchronized(m_sendBufferVect) {
	    	
	    	while(!m_sendBufferVect.isEmpty()){
	    		ByteBuffer t_sendBuffer = m_sendBufferVect.get(0);
	    		t_socketChn.write(t_sendBuffer);
	    		if(t_sendBuffer.remaining() > 0){
	    			break;
	    		}
	    		
	    		m_sendBufferVect.remove(0);
	    	}
	    	
	    	if(m_sendBufferVect.isEmpty()){
	    		_key.interestOps(SelectionKey.OP_READ);
	    	}
	    }
		  
	}
	
	private byte[] readDataByChn_impl()throws Exception{
		
		InputStream in = new ByteArrayInputStream(readData(4));
		
		int t_len = ReadInt(in);
		if(t_len == -1){
			throw new Exception("socket ReadInt failed.");
		}
		
		final int t_ziplen = t_len & 0x0000ffff;
		final int t_orglen = t_len >>> 16;
				
		byte[] t_orgdata;
				
		if(t_ziplen == 0){
		
			t_orgdata = readData(t_orglen);
																		
			synchronized (this) {
				// 20 is TCP pack head length
				m_downloadByte += t_orglen + 4 + 20;
			}
			
		}else{
						
			synchronized (this) {
				// 20 is TCP pack head length
				m_downloadByte += t_ziplen + 4 + 20;
			}
			
			t_orgdata = new byte[t_orglen];
			
			GZIPInputStream zi	= new GZIPInputStream(new ByteArrayInputStream(readData(t_ziplen)));

			ForceReadByte(zi,t_orgdata,t_orglen);
			
			zi.close();
		}
		
		byte[] t_ret = ParsePackage(t_orgdata);
		t_orgdata = null;									
		
		return t_ret;
	}

	//! send buffer implement
	private  void SendBufferToSvr_imple(byte[] _write)throws Exception{
		
		if(_write == null){
			return;
		}
				
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		ByteArrayOutputStream zos = new ByteArrayOutputStream();
		
		GZIPOutputStream zo = new GZIPOutputStream(zos,6);
		zo.write(_write);
		zo.close();	
		
		byte[] t_zipData = zos.toByteArray();
		
		if(t_zipData.length > _write.length){
			// if the ZIP data is large than original length
			// NOT convert
			//
			
			WriteInt(os,(_write.length << 16) & 0xffff0000);
			os.write(_write);
			os.flush();
			
			synchronized (this) {
				// 20 is TCP pack head length			
				m_uploadByte += _write.length + 4 + 20;
			}
	
		}else{
			
			WriteInt(os,((_write.length << 16) & 0xffff0000) | t_zipData.length);
			os.write(t_zipData);
			os.flush();
			
			synchronized (this) {
				// 20 is TCP pack head length
				m_uploadByte += t_zipData.length + 4 + 20;
			}
		}
		
		// send the buffer to blocking SocketChannel 
		//
		byte[] t_finalSend = os.toByteArray();
		
		ByteBuffer t_buffer = ByteBuffer.allocate(t_finalSend.length);
		t_buffer.put(t_finalSend);
		t_buffer.flip();
		
		m_sendBufferVect.add(t_buffer);
	}
	
	
	//! recv buffer
	public byte[] RecvBufferFromSvr()throws Exception{
		
		if(!m_unprocessedPackage.isEmpty()){
			byte[] t_ret = (byte[])m_unprocessedPackage.elementAt(0);
			m_unprocessedPackage.removeElementAt(0);
			
			return t_ret;
		}
			
		return readDataByChn_impl();
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
	static public void WriteStringVector(OutputStream _stream,Vector<String> _vect)throws Exception{
		
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
		
	static public void ReadStringVector(InputStream _stream,Vector<String> _vect)throws Exception{
		
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
		
		return new String("");
		
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

