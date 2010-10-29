import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Vector;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;


public class sendReceive extends Thread{
	
	Socket			m_socket = null;
	
	private Vector<byte[]>	m_unsendedPackage 		= new Vector<byte[]>();
	private Vector<byte[]>	m_unprocessedPackage 	= new Vector<byte[]>();
	
	
	public sendReceive(Socket _socket){
		m_socket = _socket;
	}
	
	//! send buffer
	public synchronized void SendBufferToSvr(byte[] _write)throws Exception{	
		m_unsendedPackage.addElement(_write);
	}
	
	private synchronized byte[] PrepareOutputData()throws Exception{
		
		if(m_unsendedPackage.isEmpty()){
			return null;
		}
		
		ByteArrayOutputStream t_stream = new ByteArrayOutputStream();
		
		for(int i = 0;i < m_unsendedPackage.size();i++){
			byte[] t_package = m_unsendedPackage.get(i);	
			
			fetchMail.WriteInt(t_stream, t_package.length);
			t_stream.write(t_package);
		}
		
		m_unsendedPackage.clear();
		
		return t_stream.toByteArray();
	}
	
	//! send buffer implement
	private void SendBufferToSvr_imple(byte[] _write)throws Exception{
		
		if(_write == null){
			return;
		}		
		
		OutputStream os = m_socket.getOutputStream();
		
		ByteArrayOutputStream zos = new ByteArrayOutputStream();
		GZIPOutputStream zo = new GZIPOutputStream(zos);
		zo.write(_write);
		zo.close();	
		
		byte[] t_zipData = zos.toByteArray();
		
		if(t_zipData.length > _write.length){
			// if the ZIP data is large than original length
			// NOT convert
			//
			fetchMail.WriteInt(os,_write.length << 16);
			os.write(_write);
		}else{
			fetchMail.WriteInt(os,(_write.length << 16) | t_zipData.length);
			os.write(t_zipData);
		}
		
	}
	
	public void run(){
		
		try{
			while(m_socket.isConnected()){
				wait(100);
				SendBufferToSvr_imple(PrepareOutputData());
			}
		}catch(Exception _e){
						
		}
	}

	//! recv buffer
	public byte[] RecvBufferFromSvr()throws Exception{
		
		if(!m_unprocessedPackage.isEmpty()){
			byte[] t_ret = m_unprocessedPackage.get(0);
			m_unprocessedPackage.remove(0);
			
			return t_ret;
		}
		
		InputStream in = m_socket.getInputStream();

		int t_len = fetchMail.ReadInt(in);
		
		final int t_ziplen = t_len & 0x0000ffff;
		final int t_orglen = t_len >>> 16;
		
		byte[] t_orgdata = new byte[t_orglen];
				
		if(t_ziplen == 0){
			t_len = fetchMail.ReadInt(in);
			in.read(t_orgdata,0,t_len);	
			return t_orgdata;
		}
		
		byte[] t_zipdata = new byte[t_ziplen];
		in.read(t_zipdata,0,t_ziplen);
		
		GZIPInputStream zi	= new GZIPInputStream(
								new ByteArrayInputStream(t_zipdata));
		zi.read(t_orgdata);
		zi.close();
		
		byte[] t_ret = ParsePackage(t_orgdata);
		t_orgdata = null;
		
		return t_ret;
	}
	
	private byte[] ParsePackage(byte[] _wholePackage)throws Exception{
		
		ByteArrayInputStream t_packagein = new ByteArrayInputStream(_wholePackage);
		int t_len = fetchMail.ReadInt(t_packagein);
			
		byte[] t_ret = new byte[t_len];
		t_packagein.read(t_ret,0,t_len);
		
		t_len += 4;
		
		while(t_len < _wholePackage.length){
			
			final int t_packageLen = fetchMail.ReadInt(t_packagein); 			
			byte[] t_package = new byte[t_packageLen];
			
			t_packagein.read(t_package,0,t_packageLen);
			t_len += t_packageLen + 4;
			
			m_unprocessedPackage.addElement(t_package);			
		}		
		
		return t_ret;		
	}
	
	
}