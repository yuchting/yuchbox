import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;


public class sendReceive extends Thread{

	
	int m_offeset = 0;	
	
	//! send buffer
	public static void SendBufferToSvr(Socket _socket,byte[] _write)throws Exception {
		OutputStream os = _socket.getOutputStream();
		os.write(_write.length);
		
		ZipOutputStream zo = new ZipOutputStream(os);  
		zo.write(_write);
	}

	//! recv buffer
	public byte[] RecvBufferFromSvr(Socket _socket)throws Exception{
		
		InputStream in = _socket.getInputStream(); 

		int t_len = in.read();
		byte[] t_read = new byte[t_len];
		
		ZipInputStream zin = new ZipInputStream(in);
		
		zin.read(t_read,m_offeset,m_offeset + t_len);
		m_offeset += t_len;
		
		return t_read;
	}
	
	public void run(){
		
	}
}